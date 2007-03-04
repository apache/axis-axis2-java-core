/*
* Copyright 2004,2005 The Apache Software Foundation.
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

import edu.emory.mathcs.backport.java.util.concurrent.ExecutorService;
import edu.emory.mathcs.backport.java.util.concurrent.LinkedBlockingQueue;
import edu.emory.mathcs.backport.java.util.concurrent.ThreadPoolExecutor;
import edu.emory.mathcs.backport.java.util.concurrent.TimeUnit;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.SessionContext;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.ParameterIncludeImpl;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisEvent;
import org.apache.axis2.engine.AxisObserver;
import org.apache.axis2.transport.TransportListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jms.JMSException;
import javax.naming.Context;
import javax.naming.NamingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * The JMS Transport listener implementation. A JMS Listner will hold one or
 * more JMS connection factories, which would be created at initialization
 * time. This implementation does not support the creation of connection
 * factories at runtime. This JMS Listener registers with Axis to be notified
 * of service deployment/undeployment/start and stop, and enables or disables
 * listening for messages on the destinations as appropriate.
 * <p/>
 * A Service could state the JMS connection factory name and the destination
 * name for use as Parameters in its services.xml as shown in the example
 * below. If the connection name was not specified, it will use the connection
 * factory named "default" (JMSConstants.DEFAULT_CONFAC_NAME) - if such a
 * factory is defined in the Axis2.xml. If the destination name is not specified
 * it will default to a JMS queue by the name of the service. If the destination
 * should be a Topic, it should be created on the JMS implementation, and
 * specified in the services.xml of the service.
 * <p/>
 * <parameter name="transport.jms.ConnectionFactory" locked="true">
 * myTopicConnectionFactory</parameter>
 * <parameter name="transport.jms.Destination" locked="true">
 * dynamicTopics/something.TestTopic</parameter>
 */
public class JMSListener implements TransportListener {

    private static final Log log = LogFactory.getLog(JMSListener.class);

    /**
     * The maximum number of threads used for the worker thread pool
     */
    private static final int WORKERS_MAX_THREADS = 100;
    /**
     * The keep alive time of an idle worker thread
     */
    private static final long WORKER_KEEP_ALIVE = 60L;
    /**
     * The worker thread timeout time unit
     */
    private static final TimeUnit TIME_UNIT = TimeUnit.SECONDS;

    /**
     * A Map containing the connection factories managed by this, keyed by name
     */
    private Map connectionFactories = new HashMap();
    /**
     * A Map of service name to the JMS EPR addresses
     */
    private Map serviceNameToEprMap = new HashMap();
    /**
     * The Axis2 Configuration context
     */
    private ConfigurationContext axisConf = null;

    /**
     * This is the TransportListener initialization method invoked by Axis2
     *
     * @param axisConf   the Axis configuration context
     * @param transprtIn the TransportIn description
     */
    public void init(ConfigurationContext axisConf,
                     TransportInDescription transprtIn) {

        // save reference to the configuration context
        this.axisConf = axisConf;

        // initialize the defined connection factories
        initializeConnectionFactories(transprtIn);

        // if no connection factories are defined, we cannot listen
        if (connectionFactories.isEmpty()) {
            log.warn("No JMS connection factories are defined." +
                    "Will not listen for any JMS messages");
            return;
        }

        // iterate through deployed services and validate connection factory
        // names, and mark services as faulty where appropriate.
        Iterator services =
                axisConf.getAxisConfiguration().getServices().values().iterator();

        while (services.hasNext()) {
            AxisService service = (AxisService) services.next();
            if (JMSUtils.isJMSService(service)) {
                processService(service);
            }
        }

        // register to receive updates on services for lifetime management
        axisConf.getAxisConfiguration().addObservers(new JMSAxisObserver());

        log.info("JMS Transport Receiver (Listener) initialized...");
    }


    /**
     * Prepare to listen for JMS messages on behalf of this service
     *
     * @param service
     */
    private void processService(AxisService service) {
        JMSConnectionFactory cf = getConnectionFactory(service);
        if (cf == null) {
            String msg = "Service " + service.getName() + " does not specify" +
                    "a JMS connection factory or refers to an invalid factory. " +
                    "This service is being marked as faulty and will not be " +
                    "available over the JMS transport";
            log.warn(msg);
            JMSUtils.markServiceAsFaulty(
                    service.getName(), msg, service.getAxisConfiguration());
            return;
        }

        String destination = JMSUtils.getDestination(service);

        // compute service EPR and keep for later use
        serviceNameToEprMap.put(service.getName(), getEPR(cf, destination));

        // add the specified or implicit destination of this service
        // to its connection factory
        cf.addDestination(destination, service.getName());
    }

    /**
     * Return the connection factory name for this service. If this service
     * refers to an invalid factory or defaults to a non-existent default
     * factory, this returns null
     *
     * @param service the AxisService
     * @return the JMSConnectionFactory to be used, or null if reference is invalid
     */
    private JMSConnectionFactory getConnectionFactory(AxisService service) {
        Parameter conFacParam = service.getParameter(JMSConstants.CONFAC_PARAM);

        // validate connection factory name (specified or default)
        if (conFacParam != null) {
            String conFac = (String) conFacParam.getValue();
            if (connectionFactories.containsKey(conFac)) {
                return (JMSConnectionFactory) connectionFactories.get(conFac);
            } else {
                return null;
            }

        } else if (connectionFactories.containsKey(JMSConstants.DEFAULT_CONFAC_NAME)) {
            return (JMSConnectionFactory) connectionFactories.
                    get(JMSConstants.DEFAULT_CONFAC_NAME);

        } else {
            return null;
        }
    }

    /**
     * Initialize the defined connection factories, parsing the TransportIn
     * descriptions
     *
     * @param transprtIn The Axis2 Transport in for the JMS
     */
    private void initializeConnectionFactories(TransportInDescription transprtIn) {
        // iterate through all defined connection factories
        Iterator conFacIter = transprtIn.getParameters().iterator();

        while (conFacIter.hasNext()) {

            Parameter param = (Parameter) conFacIter.next();
            JMSConnectionFactory jmsConFactory =
                    new JMSConnectionFactory(param.getName());

            ParameterIncludeImpl pi = new ParameterIncludeImpl();
            try {
                pi.deserializeParameters((OMElement) param.getValue());
            } catch (AxisFault axisFault) {
                handleException("Error reading Parameters for JMS connection " +
                        "factory" + jmsConFactory.getName(), axisFault);
            }

            // read connection facotry properties
            Iterator params = pi.getParameters().iterator();

            while (params.hasNext()) {
                Parameter p = (Parameter) params.next();

                if (Context.INITIAL_CONTEXT_FACTORY.equals(p.getName())) {
                    jmsConFactory.addProperty(
                            Context.INITIAL_CONTEXT_FACTORY, (String) p.getValue());
                } else if (Context.PROVIDER_URL.equals(p.getName())) {
                    jmsConFactory.addProperty(
                            Context.PROVIDER_URL, (String) p.getValue());
                } else if (Context.SECURITY_PRINCIPAL.equals(p.getName())) {
                    jmsConFactory.addProperty(
                            Context.SECURITY_PRINCIPAL, (String) p.getValue());
                } else if (Context.SECURITY_CREDENTIALS.equals(p.getName())) {
                    jmsConFactory.addProperty(
                            Context.SECURITY_CREDENTIALS, (String) p.getValue());
                } else if (JMSConstants.CONFAC_JNDI_NAME_PARAM.equals(p.getName())) {
                    jmsConFactory.setJndiName((String) p.getValue());
                } else if (JMSConstants.DEST_PARAM.equals(p.getName())) {
                    StringTokenizer st =
                            new StringTokenizer((String) p.getValue(), " ,");
                    while (st.hasMoreTokens()) {
                        jmsConFactory.addDestination(st.nextToken(), null);
                    }
                }
            }

            // connect to the actual connection factory
            try {
                jmsConFactory.connect();
                connectionFactories.put(jmsConFactory.getName(), jmsConFactory);
            } catch (NamingException e) {
                handleException("Error connecting to JMS connection factory : " +
                        jmsConFactory.getJndiName(), e);
            }
        }
    }

    /**
     * Get the EPR for the given JMS connection factory and destination
     * the form of the URL is
     * jms:/<destination>?[<key>=<value>&]*
     *
     * @param cf          the Axis2 JMS connection factory
     * @param destination the JNDI name of the destination
     * @return the EPR as a String
     */
    private static String getEPR(JMSConnectionFactory cf, String destination) {
        StringBuffer sb = new StringBuffer();
        sb.append(JMSConstants.JMS_PREFIX).append(destination);
        sb.append("?").append(JMSConstants.CONFAC_JNDI_NAME_PARAM).
                append("=").append(cf.getJndiName());
        Iterator props = cf.getProperties().keySet().iterator();
        while (props.hasNext()) {
            String key = (String) props.next();
            String value = (String) cf.getProperties().get(key);
            sb.append("&").append(key).append("=").append(value);
        }
        return sb.toString();
    }

    /**
     * Start this JMS Listener (Transport Listener)
     *
     * @throws AxisFault
     */
    public void start() throws AxisFault {
        // create thread pool of workers
        ExecutorService workerPool = new ThreadPoolExecutor(
                1,
                WORKERS_MAX_THREADS, WORKER_KEEP_ALIVE, TIME_UNIT,
                new LinkedBlockingQueue(),
                new org.apache.axis2.util.threadpool.DefaultThreadFactory(
                        new ThreadGroup("JMS Worker thread group"),
                        "JMSWorker"));

        Iterator iter = connectionFactories.values().iterator();
        while (iter.hasNext()) {
            JMSConnectionFactory conFac = (JMSConnectionFactory) iter.next();
            JMSMessageReceiver msgRcvr =
                    new JMSMessageReceiver(conFac, workerPool, axisConf);

            try {
                conFac.listen(msgRcvr);
            } catch (JMSException e) {
                handleException("Error starting connection factory : " +
                        conFac.getName(), e);
            }
        }
    }

    /**
     * Stop this transport listener and shutdown all of the connection factories
     */
    public void stop() {
        Iterator iter = connectionFactories.values().iterator();
        while (iter.hasNext()) {
            ((JMSConnectionFactory) iter.next()).stop();
        }
    }

    /**
     * Returns EPRs for the given service and IP. (Picks up precomputed EPR)
     *
     * @param serviceName service name
     * @param ip          ignored
     * @return the EPR for the service
     * @throws AxisFault not used
     */
    public EndpointReference[] getEPRsForService(String serviceName, String ip) throws AxisFault {
        //Strip out the operation name
        if (serviceName.indexOf('/') != -1) {
            serviceName = serviceName.substring(0, serviceName.indexOf('/'));
        }
        return new EndpointReference[]{
                new EndpointReference((String) serviceNameToEprMap.get(serviceName))};
    }

    /**
     * Returns the EPR for the given service and IP. (Picks up precomputed EPR)
     *
     * @param serviceName service name
     * @param ip          ignored
     * @return the EPR for the service
     * @throws AxisFault not used
     */
    public EndpointReference getEPRForService(String serviceName, String ip) throws AxisFault {
        return getEPRsForService(serviceName, ip)[0];
    }

    /**
     * Starts listening for messages on this service
     *
     * @param service the AxisService just deployed
     */
    private void startListeningForService(AxisService service) {
        processService(service);
        JMSConnectionFactory cf = getConnectionFactory(service);
        if (cf == null) {
            String msg = "Service " + service.getName() + " does not specify" +
                    "a JMS connection factory or refers to an invalid factory." +
                    "This service is being marked as faulty and will not be " +
                    "available over the JMS transport";
            log.warn(msg);
            JMSUtils.markServiceAsFaulty(
                    service.getName(), msg, service.getAxisConfiguration());
            return;
        }

        String destination = JMSUtils.getDestination(service);
        try {
            cf.listenOnDestination(destination);
            log.info("Started listening on destination : " + destination +
                    " for service " + service.getName());

        } catch (JMSException e) {
            handleException(
                    "Could not listen on JMS for service " + service.getName(), e);
            JMSUtils.markServiceAsFaulty(
                    service.getName(), e.getMessage(), service.getAxisConfiguration());
        }
    }

    /**
     * Stops listening for messages for the service undeployed
     *
     * @param service the AxisService just undeployed
     */
    private void stopListeningForService(AxisService service) {

        JMSConnectionFactory cf = getConnectionFactory(service);
        if (cf == null) {
            String msg = "Service " + service.getName() + " does not specify" +
                    "a JMS connection factory or refers to an invalid factory." +
                    "This service is being marked as faulty and will not be " +
                    "available over the JMS transport";
            log.warn(msg);
            JMSUtils.markServiceAsFaulty(
                    service.getName(), msg, service.getAxisConfiguration());
            return;
        }

        // remove from the serviceNameToEprMap
        serviceNameToEprMap.remove(service.getName());

        String destination = JMSUtils.getDestination(service);
        try {
            cf.removeDestination(destination);
        } catch (JMSException e) {
            handleException(
                    "Error while terminating listening on JMS destination : " + destination, e);
        }
    }

    private void handleException(String msg, Exception e) {
        log.error(msg, e);
        throw new AxisJMSException(msg, e);
    }

    /**
     * An AxisObserver which will start listening for newly deployed services,
     * and stop listening when services are undeployed.
     */
    class JMSAxisObserver implements AxisObserver {

        // The initilization code will go here
        public void init(AxisConfiguration axisConfig) {
        }

        public void serviceUpdate(AxisEvent event, AxisService service) {

            if (JMSUtils.isJMSService(service)) {
                switch (event.getEventType()) {
                    case AxisEvent.SERVICE_DEPLOY :
                        startListeningForService(service);
                        break;
                    case AxisEvent.SERVICE_REMOVE :
                        stopListeningForService(service);
                        break;
                    case AxisEvent.SERVICE_START  :
                        startListeningForService(service);
                        break;
                    case AxisEvent.SERVICE_STOP   :
                        stopListeningForService(service);
                        break;
                }
            }
        }

        public void moduleUpdate(AxisEvent event, AxisModule module) {
        }

        //--------------------------------------------------------
        public void addParameter(Parameter param) throws AxisFault {
        }

        public void removeParameter(Parameter param) throws AxisFault {
        }

        public void deserializeParameters(OMElement parameterElement) throws AxisFault {
        }

        public Parameter getParameter(String name) {
            return null;
        }

        public ArrayList getParameters() {
            return null;
        }

        public boolean isParameterLocked(String parameterName) {
            return false;
        }

        public void serviceGroupUpdate(AxisEvent event, AxisServiceGroup serviceGroup) {
        }
    }

    public ConfigurationContext getConfigurationContext() {
        return this.axisConf;
    }


    public SessionContext getSessionContext(MessageContext messageContext) {
        return null;
    }
}
