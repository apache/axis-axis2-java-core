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

package org.apache.axis2.jms;

import junit.framework.TestCase;
import org.apache.activemq.broker.BrokerService;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.InOnlyAxisOperation;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.Echo;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.integration.UtilsJMSServer;
import org.apache.axis2.receivers.RawXMLINOnlyMessageReceiver;
import org.apache.axis2.transport.jms.JMSConstants;
import org.apache.axis2.wsdl.WSDLConstants;

import javax.naming.Context;
import javax.xml.namespace.QName;

/**
 * This class contains unit tests for the jms implementation. Embedded ActiveMQ broker is used as
 * JMS provider.
 */

/*
TODO: 
 - UtilServer.createClientConfigurationContext() uses 
   target\test-resources\integrationRepo\ instead of 
   target\test-resources\jms-enabled-client-repository
   and thus jms-enabled-client-axis2.xml is not used.
 
 - JMSEchoRawXMLTest and this class can be combined.
 
 - No fault case(calling echoFault, for example) is tested
 
 - No message security(Rampart) over JMS is tested (I did but couldn't find
  time and easy way for adding it to Integration module :) )
 
 - No JMS transport security(secure connection, for example) is tested
    - JMS implementaton doesn't support secure connection 
      (conFac.createConnection(username, password)) 
      
      Standalone or embedded ActiveMQ can be configured for secure connection 
      as mentioned here: http://issues.apache.org/activemq/browse/AMQ-982.
*/
public class JMSEchoRawXMLOneWayTest extends TestCase {

    private static final String INITIAL_CONTEXT_FACTORY =
            "org.apache.activemq.jndi.ActiveMQInitialContextFactory";
    private static final String PROVIDER_URL = "tcp://localhost:61616";
    private static final String CONNECTION_FACTORY_NAME = "QueueConnectionFactory";
    private static final String DESTINATION_NAME = "TestQueue";
    // USERNAME and PASSWORD are required only if broker has been
    // configured for authanticated connection to the context.
    // Works for SonicMQ but, couldn't find a way to do this for ActiveMQ :)
    private static final String USERNAME = null;
    private static final String PASSWORD = null;

    // ActiveMQ settings
    private BrokerService broker = null;
    private static final boolean USE_EMBEDDED_BROKER =
            true; // If false, standalone ActiveMQ broker is required.

    // Client (service consumer/JMS sender) variables
    private String targetEprUrl;
    private ConfigurationContext clientConfigContext;

    public JMSEchoRawXMLOneWayTest() throws AxisFault {
        super(JMSEchoRawXMLOneWayTest.class.getName());
    }

    public JMSEchoRawXMLOneWayTest(String testName) throws AxisFault {
        super(testName);
    }

    protected void setUp() throws Exception {
        initializeServerSide();
        initializeClientSide();
    }

    protected void tearDown() throws Exception {
        shutdownServerSide();
    }

    ////////////////////////////////////////////////////////////////////
    // HELPER METHODS
    ////////////////////////////////////////////////////////////////////

    private void initializeServerSide() throws Exception {
        if (USE_EMBEDDED_BROKER) {
            broker = new BrokerService();
            broker.setUseJmx(false);
            broker.setPersistent(false);
            // To enable security for embedded broker please
            // follow http://issues.apache.org/activemq/browse/AMQ-982
            //broker.setPlugins(new BrokerPlugin[] { new JaasAuthenticationPlugin () });
            broker.addConnector(PROVIDER_URL);
            broker.start();
        }

        UtilsJMSServer.start();
    }

    private void shutdownServerSide() throws Exception {
        // Gracefully shutdown the server
        UtilsJMSServer.stop();
        if (USE_EMBEDDED_BROKER) {
            broker.stop();
        }
    }

    private void initializeClientSide() throws Exception {
        targetEprUrl = createJmsEndpointURL();
        clientConfigContext = UtilServer.createClientConfigurationContext();
    }

    // Creates vendor specific JMS endpoint URL.
    private String createJmsEndpointURL() {
        return new StringBuffer("jms:/").append(DESTINATION_NAME)
                .append("?").append(JMSConstants.CONFAC_JNDI_NAME_PARAM).append("=")
                .append(CONNECTION_FACTORY_NAME)
                .append("&").append(Context.INITIAL_CONTEXT_FACTORY).append("=")
                .append(INITIAL_CONTEXT_FACTORY)
                .append("&").append(Context.PROVIDER_URL).append("=").append(PROVIDER_URL)
                .append("&").append(Context.SECURITY_PRINCIPAL).append("=").append(USERNAME)
                .append("&").append(Context.SECURITY_CREDENTIALS).append("=").append(PASSWORD)
                .toString();
    }

    private OMElement createPayload(QName serviceName, QName operationName) {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://localhost/axis2/services/"
                + serviceName.getLocalPart(), "my");
        OMElement method = fac.createOMElement(operationName.getLocalPart(), omNs);
        OMElement value = fac.createOMElement("myValue", omNs);
        value.addChild(fac.createOMText(value, "Isaac Asimov, The Foundation Trilogy"));
        method.addChild(value);

        return method;
    }


    protected AxisService createInOnlyService(QName serviceName, String serviceClassName,
                                              QName operationName)
            throws AxisFault {

        // Creates service that maps to the given service class
        AxisService service = new AxisService(serviceName.getLocalPart());
        service.setClassLoader(Thread.currentThread().getContextClassLoader());
        service.addParameter(new Parameter(
                Constants.SERVICE_CLASS, serviceClassName));

        // Adds an operation who's MEP is In-Only.
        AxisOperation operation = new InOnlyAxisOperation(operationName);
        operation.setMessageReceiver(new RawXMLINOnlyMessageReceiver());
        operation.setStyle(WSDLConstants.STYLE_RPC);
        service.addOperation(operation);

        return service;
    }

    /** Adds JMS configuration to the service. These parameters are normally loaded from service.xml */
    protected AxisService addDestination(AxisService service, String destinationName)
            throws AxisFault {

        //service.addParameter(new Parameter(JMSConstants.CONFAC_PARAM,
        //		"default"));
        service.addParameter(new Parameter(JMSConstants.DEST_PARAM,
                                           destinationName));

        return service;
    }

    ////////////////////////////////////////////////////////////////////
    // UNIT TESTS
    ////////////////////////////////////////////////////////////////////


    /**
     * Calls 'echoOMElementNoResponse' operation of the EchoXMLService service in one-way fashion.
     * Therefore, we don't expect a response from the service.
     * <p/>
     * When this test sends message to the queue, the server retrieves and delegates it to the
     * 'echoOMElementNoResponse' method of Echo class.
     */
    public void test01EchoOMElementNoResponse() throws Exception {
        try {
            // Service and transport variables common both to service provider and
            // service consumer
            Class serviceClass = Echo.class;
            QName serviceName = new QName("EchoXMLService");
            QName operationName = new QName("echoOMElementNoResponse");

            ////////////////////////////////////////////////////
            // Service side processes
            ////////////////////////////////////////////////////

            //create and deploy the service
            AxisService service = createInOnlyService(serviceName,
                                                      serviceClass.getName(),
                                                      operationName);

            service = addDestination(service, DESTINATION_NAME);

            UtilsJMSServer.deployService(service);

            ////////////////////////////////////////////////////
            // Client side processes.
            //
            // We don't need to create an AxisService for client side.
            ////////////////////////////////////////////////////

            OMElement payload = createPayload(serviceName, operationName);
            Options options = new Options();
            options.setTo(new EndpointReference(targetEprUrl));
            options.setTransportInProtocol(Constants.TRANSPORT_JMS);
            options.setAction(serviceName.getLocalPart());
            options.setUseSeparateListener(true);

            ServiceClient sender = new ServiceClient(clientConfigContext, null);
            sender.setOptions(options);
            sender.fireAndForget(payload);

            // Wait while the message being sent to the queue. Otherwise,
            // message sending process will be canceled while exiting
            // the test and there will be no message in queue.
            Thread.sleep(3500);

            // Undeploy the service (don't listen to it anymore)
            UtilsJMSServer.unDeployService(serviceName);

        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
            fail();
        }
    }
}
