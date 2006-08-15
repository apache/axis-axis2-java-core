/*
* Copyright 2001, 2002,2004 The Apache Software Foundation.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/


package org.apache.axis2.transport.jms;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.engine.ListenerManager;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.transport.TransportListener;
import org.apache.axis2.util.OptionsParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jms.BytesMessage;
import javax.jms.MessageListener;
import javax.xml.namespace.QName;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;


/**
 * SimpleJMSListener implements the javax.jms.MessageListener interface. Its
 * basic purpose is listen asynchronously for messages and to pass them off
 * to SimpleJMSWorker for processing.
 * <p/>
 * Note: This is a simple JMS listener that does not pool worker threads and
 * is not otherwise tuned for performance. As such, its intended use is not
 * for production code, but for demos, debugging, and performance profiling.
 */
public class SimpleJMSListener implements MessageListener, TransportListener {
	private static final Log log = LogFactory.getLog(SimpleJMSListener.class);

    // Do we use (multiple) threads to process incoming messages?
    private boolean doThreads = true;
    protected ConfigurationContext configurationContext;
    private JMSConnector connector;
    private String destination;
    private JMSEndpoint endpoint;
    private HashMap properties;
    private String user = null;
    private String password = null;

    public SimpleJMSListener() {
    }

    public SimpleJMSListener(String repositoryDirectory, HashMap connectorMap, HashMap cfMap,
                             String destination, String username, String password,
                             boolean doThreads)
            throws Exception {
        //TODO : modify this constructor to take locatiom of axis2.xml
        File repo = new File(repositoryDirectory);
        if (repo.exists()) {
            this.configurationContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(
                    repositoryDirectory, repositoryDirectory + "/conf/axis2.xml");
        } else {
            throw new Exception("repository not found");
        }

        ListenerManager listenerManager = configurationContext.getListenerManager();
        TransportInDescription trsIn = new TransportInDescription(
                new QName(Constants.TRANSPORT_JMS));
        trsIn.setReceiver(this);
        if (listenerManager == null) {
            listenerManager = new ListenerManager();
            listenerManager.init(configurationContext);
        }
        listenerManager.addListener(trsIn, true);
        this.doThreads = doThreads;
        this.properties = new HashMap(connectorMap);
        this.properties.putAll(cfMap);
        this.destination = destination;
    }

    public static final HashMap createCFMap(OptionsParser optionsParser) throws IOException {
        String cfFile = optionsParser.isValueSet('c');
        if (cfFile == null) {
            return null;
        }
        Properties cfProps = new Properties();
        cfProps.load(new BufferedInputStream(new FileInputStream(cfFile)));
        HashMap cfMap = new HashMap(cfProps);
        return cfMap;
    }

    public static final HashMap createConnectorMap(
            org.apache.axis2.util.OptionsParser optionsParser) {
        HashMap connectorMap = new HashMap();
        if (optionsParser.isFlagSet('t') > 0) {
            // queue is default so only setup map if topic domain is required
            connectorMap.put(JMSConstants.DOMAIN, JMSConstants.DOMAIN_TOPIC);
        }
        return connectorMap;
    }

    public void init(ConfigurationContext axisConf, TransportInDescription transprtIn)
            throws AxisFault {
        try {
            this.configurationContext = axisConf;
            HashMap params = new HashMap();
            Iterator iterator = transprtIn.getParameters().iterator();
            while (iterator.hasNext()) {
                Parameter param = (Parameter) iterator.next();
                params.put(param.getName(), param.getValue());
            }
            if (transprtIn.getParameter(JNDIVendorAdapter.USER) != null) {
                user = (String) transprtIn.getParameter(JNDIVendorAdapter.USER).getValue();
            }
            if (transprtIn.getParameter(JNDIVendorAdapter.PASSWORD) != null) {
                password = (String) transprtIn.getParameter(JNDIVendorAdapter.PASSWORD).getValue();
            }
            if (transprtIn.getParameter(JNDIVendorAdapter.DESTINATION) != null) {
                destination =
                        (String) transprtIn.getParameter(JNDIVendorAdapter.DESTINATION).getValue();
            }
            this.properties = new HashMap(params);
        } catch (Exception e1) {
            throw new AxisFault(e1);
        }
    }

    private void startListener()
            throws Exception {
        try {
            // create a JMS connector using the default vendor adapter
            JMSVendorAdapter adapter = JMSVendorAdapterFactory.getJMSVendorAdapter();
            this.connector = JMSConnectorFactory.createServerConnector(properties, properties,
                    user, password, adapter);
        } catch (Exception e) {
            log.error(Messages.getMessage("exception00"), e);
            throw e;
        }

        // create the appropriate endpoint for the indicated destination
        endpoint = connector.createEndpoint(destination);
    }

    public static void main(String[] args) throws Exception {
        OptionsParser optionsParser = new OptionsParser(args);

        // first check if we should print usage
        if ((optionsParser.isFlagSet('?') > 0) || (optionsParser.isFlagSet('h') > 0)) {
            printUsage();
        }

        SimpleJMSListener listener = new SimpleJMSListener(optionsParser.isValueSet('r'),
                createConnectorMap(optionsParser),
                createCFMap(optionsParser), optionsParser.isValueSet('d'),
                optionsParser.getUser(), optionsParser.getPassword(),
                optionsParser.isFlagSet('s') > 0);

        listener.start();
    }

    /**
     * This method is called asynchronously whenever a message arrives.
     *
     * @param message
     */
    public void onMessage(javax.jms.Message message) {
        try {
            // pass off the message to a worker as a BytesMessage
            SimpleJMSWorker worker = new SimpleJMSWorker
                (configurationContext, this, message);

            // do we allow multi-threaded workers?
            if (doThreads) {
                Thread t = new Thread(worker);
                t.start();
            } else {
                worker.run();
            }
        } catch (ClassCastException cce) {
            log.error(Messages.getMessage("exception00"), cce);
            cce.printStackTrace();

            return;
        }
    }

    public static void printUsage() {
        System.out.println("Usage: SimpleJMSListener [options]");
        System.out.println(" Opts: -? this message");
        System.out.println();
        System.out.println("       -r repository directory location");
        System.out.println("       -c connection factory properties filename");
        System.out.println("       -d destination");
        System.out.println("       -t topic [absence of -t indicates queue]");
        System.out.println();
        System.out.println("       -u username");
        System.out.println("       -w password");
        System.out.println();
        System.out.println("       -s single-threaded listener");
        System.out.println("          [absence of option => multithreaded]");
        System.exit(1);
    }

    public void start() {
        try {
            startListener();
            endpoint.registerListener(this, properties);
            connector.start();
        } catch (Exception e) {
            log.error(Messages.getMessage("exception00"), e);
            e.printStackTrace();
        }
    }

    public void stop() throws AxisFault {
        try {
            endpoint.unregisterListener(this);
            connector.stop();
            connector.shutdown();
        } catch (Exception e) {
            log.error(Messages.getMessage("exception00"), e);
            e.printStackTrace();
        }
    }

    public ConfigurationContext getConfigurationContext() {
        return this.configurationContext;
    }

    protected JMSConnector getConnector() {
        return connector;
    }

    public HashMap getProperties() {
        return properties;
    }

    public EndpointReference getEPRForService(String serviceName, String ip) throws AxisFault {
        try {
            JMSURLHelper url = new JMSURLHelper("jms:/" + destination);
            if (url != null && url.getProperties() != null && properties != null) {
                url.getProperties().putAll(properties);
                return new EndpointReference(url.getURLString());
            } else {
                return null;
            }
        } catch (Exception e) {
            log.error(Messages.getMessage("exception00"), e);
            return null;
        }
    }
}
