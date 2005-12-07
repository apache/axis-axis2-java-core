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
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.transport.TransportListener;
import org.apache.axis2.util.OptionsParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jms.BytesMessage;
import javax.jms.MessageListener;
import java.io.BufferedInputStream;
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
public class SimpleJMSListener extends TransportListener implements MessageListener {
    protected static Log log =
            LogFactory.getLog(SimpleJMSListener.class.getName());

    // Do we use (multiple) threads to process incoming messages?
    private boolean doThreads = true;

    private JMSConnector connector;
    private JMSEndpoint endpoint;
    private HashMap properties;
    private String destination;
    protected ConfigurationContext configurationContext;

    public SimpleJMSListener() {

    }

    public void init(ConfigurationContext axisConf, TransportInDescription transprtIn) throws AxisFault {
        try {
            this.configurationContext = axisConf;
            HashMap params = new HashMap();
            Iterator iterator = transprtIn.getParameters().iterator();
            while (iterator.hasNext()) {
                Parameter param = (Parameter) iterator.next();
                params.put(param.getName(), param.getValue());
            }
            String user = null, password = null, destination = null;
            if (transprtIn.getParameter(JNDIVendorAdapter.USER) != null) {
                user = (String) transprtIn.getParameter(JNDIVendorAdapter.USER).getValue();
            }
            if (transprtIn.getParameter(JNDIVendorAdapter.PASSWORD) != null) {
                password = (String) transprtIn.getParameter(JNDIVendorAdapter.PASSWORD).getValue();
            }
            if (transprtIn.getParameter(JNDIVendorAdapter.DESTINATION) != null) {
                destination = (String) transprtIn.getParameter(JNDIVendorAdapter.DESTINATION).getValue();
            }
            initListener(params, params, user, password, destination);
        } catch (Exception e1) {
            throw new AxisFault(e1);
        }
    }

    public SimpleJMSListener(String repositoryDirectory, HashMap connectorMap, HashMap cfMap,
                             String destination, String username,
                             String password, boolean doThreads)
            throws Exception {
        ConfigurationContextFactory erfac = new ConfigurationContextFactory();
        this.configurationContext = erfac.buildConfigurationContext(repositoryDirectory);
        this.doThreads = doThreads;

        initListener(connectorMap, cfMap, username, password, destination);
    }

    private void initListener(HashMap connectorMap, HashMap cfMap, String username, String password, String destination) throws Exception {
        try {
            // create a JMS connector using the default vendor adapter
            JMSVendorAdapter adapter = JMSVendorAdapterFactory.getJMSVendorAdapter();
            this.connector = JMSConnectorFactory.createServerConnector(connectorMap,
                    cfMap,
                    username,
                    password,
                    adapter);
            this.properties = new HashMap(connectorMap);
            this.properties.putAll(cfMap);
            this.destination = destination;
        } catch (Exception e) {
            log.error(Messages.getMessage("exception00"), e);
            throw e;
        }

        // create the appropriate endpoint for the indicated destination
        endpoint = connector.createEndpoint(destination);
    }

    protected JMSConnector getConnector() {
        return connector;
    }

    public ConfigurationContext getSystemContext() {
        return this.configurationContext;
    }

    /**
     * This method is called asynchronously whenever a message arrives.
     *
     * @param message
     */
    public void onMessage(javax.jms.Message message) {
        try {
            // pass off the message to a worker as a BytesMessage
            SimpleJMSWorker worker = new SimpleJMSWorker(configurationContext, this, (BytesMessage) message);

            // do we allow multi-threaded workers?
            if (doThreads) {
                Thread t = new Thread(worker);
                t.start();
            } else {
                worker.run();
            }
        }
        catch (ClassCastException cce) {
            log.error(Messages.getMessage("exception00"), cce);
            cce.printStackTrace();
            return;
        }
    }

    public void start() {
        try {
            endpoint.registerListener(this, properties);
        } catch (Exception e) {
            log.error(Messages.getMessage("exception00"), e);
            e.printStackTrace();
        }
        connector.start();
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

    public EndpointReference getReplyToEPR(String serviceName) throws AxisFault {
        try {
            JMSURLHelper url = new JMSURLHelper("jms:/" + destination);
            url.getProperties().putAll(properties);
            return new EndpointReference(url.getURLString());
        } catch (Exception e) {
            log.error(Messages.getMessage("exception00"), e);
            throw AxisFault.makeFault(e);
        }
    }

    public static final HashMap createConnectorMap(org.apache.axis2.util.OptionsParser optionsParser) {
        HashMap connectorMap = new HashMap();
        if (optionsParser.isFlagSet('t') > 0) {
            //queue is default so only setup map if topic domain is required
            connectorMap.put(JMSConstants.DOMAIN, JMSConstants.DOMAIN_TOPIC);
        }
        return connectorMap;
    }

    public static final HashMap createCFMap(OptionsParser optionsParser)
            throws IOException {
        String cfFile = optionsParser.isValueSet('c');
        if (cfFile == null)
            return null;

        Properties cfProps = new Properties();
        cfProps.load(new BufferedInputStream(new FileInputStream(cfFile)));
        HashMap cfMap = new HashMap(cfProps);
        return cfMap;
    }

    public static void main(String[] args) throws Exception {
        OptionsParser optionsParser = new OptionsParser(args);

        // first check if we should print usage
        if ((optionsParser.isFlagSet('?') > 0) || (optionsParser.isFlagSet('h') > 0))
            printUsage();

        SimpleJMSListener listener = new SimpleJMSListener(
                optionsParser.isValueSet('r'),
                createConnectorMap(optionsParser),
                createCFMap(optionsParser),
                optionsParser.isValueSet('d'),
                optionsParser.getUser(),
                optionsParser.getPassword(),
                optionsParser.isFlagSet('s') > 0);
        listener.start();
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
}
