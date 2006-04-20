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
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.async.AsyncResult;
import org.apache.axis2.client.async.Callback;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.OutInAxisOperation;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.Echo;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.integration.UtilsJMSServer;
import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

public class JMSEchoRawXMLTest extends TestCase {
    private EndpointReference targetEPR =
            new EndpointReference("jms:/dynamicQueues/BAR?ConnectionFactoryJNDIName=ConnectionFactory&java.naming.factory.initial=org.activemq.jndi.ActiveMQInitialContextFactory&java.naming.provider.url=tcp://localhost:61616");
    private QName serviceName = new QName("EchoXMLService");
    private QName operationName = new QName("echoOMElement");

    private AxisService service;
    private AxisService clientService;
    private ConfigurationContext configContext;

    private boolean finish = false;
    private Log log = LogFactory.getLog(getClass());

    public JMSEchoRawXMLTest() {
        super(JMSEchoRawXMLTest.class.getName());
    }

    public JMSEchoRawXMLTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        UtilsJMSServer.start();

        //create and deploy the service
        service =
                Utils.createSimpleService(serviceName,
                        Echo.class.getName(),
                        operationName);
        UtilsJMSServer.deployService(service);
        clientService = Utils.createSimpleServiceforClient(serviceName,
                Echo.class.getName(),
                operationName);
        configContext = UtilServer.createClientConfigurationContext();
    }

    protected void tearDown() throws Exception {
        UtilsJMSServer.stop();
    }

    private OMElement createPayload() {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://localhost/axis2/services/EchoXMLService", "my");
        OMElement method = fac.createOMElement("echoOMElement", omNs);
        OMElement value = fac.createOMElement("myValue", omNs);
        value.addChild(
                fac.createOMText(value, "Isaac Asimov, The Foundation Trilogy"));
        method.addChild(value);

        return method;
    }

    public void testEchoXMLASync() throws Exception {
        OMElement payload = createPayload();
        Options options = new Options();
        options.setTo(targetEPR);
        options.setTransportInProtocol(Constants.TRANSPORT_JMS);
        options.setAction(serviceName.getLocalPart());

        Callback callback = new Callback() {
            public void onComplete(AsyncResult result) {
                try {
                    result.getResponseEnvelope().serialize(XMLOutputFactory.newInstance()
                            .createXMLStreamWriter(System.out));
                } catch (XMLStreamException e) {
                    onError(e);
                } finally {
                    finish = true;
                }
            }

            public void onError(Exception e) {
                log.info(e.getMessage());
                finish = true;
            }
        };

        ServiceClient sender = new ServiceClient(configContext, clientService);
        sender.setOptions(options);
        sender.sendReceiveNonBlocking(operationName, payload, callback);


        int index = 0;
        while (!finish) {
            Thread.sleep(10000);
            index++;
            if (index > 10) {
                throw new AxisFault(
                        "Server was shutdown as the async response take too long to complete");
            }
        }
    }

    public void testEchoXMLSync() throws Exception {
        OMElement payload = createPayload();
        Options options = new Options();
        options.setTo(targetEPR);
        options.setTransportInProtocol(Constants.TRANSPORT_JMS);
        options.setAction(serviceName.getLocalPart());
        ServiceClient sender = new ServiceClient(configContext, clientService);
        sender.setOptions(options);

        OMElement result = sender.sendReceive(operationName, payload);


        result.serialize(XMLOutputFactory.newInstance().createXMLStreamWriter(
                System.out));

    }

    public void testEchoXMLCompleteSync() throws Exception {

        OMFactory fac = OMAbstractFactory.getOMFactory();

        OMNamespace omNs = fac.createOMNamespace("http://localhost/axis2/services/EchoXMLService", "my");
        OMElement payloadElement = fac.createOMElement("echoOMElement", omNs);
        OMElement value = fac.createOMElement("myValue", omNs);
        value.setText("Isaac Asimov, The Foundation Trilogy");
        payloadElement.addChild(value);

        Options options = new Options();
        options.setTo(targetEPR);
        options.setAction(operationName.getLocalPart());
        options.setTransportInProtocol(Constants.TRANSPORT_JMS);
        options.setUseSeparateListener(true);

        ServiceClient sender = new ServiceClient(configContext, clientService);
        sender.setOptions(options);

        OMElement result = sender.sendReceive(operationName, payloadElement);

        result.serialize(XMLOutputFactory.newInstance().createXMLStreamWriter(
                System.out));

    }

    public void testEchoXMLSyncMC() throws Exception {
        ConfigurationContext configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(Constants.TESTING_REPOSITORY,null);

        AxisOperation opdesc = new OutInAxisOperation(new QName("echoOMElement"));
        Options options = new Options();
        options.setTo(targetEPR);
        options.setAction(operationName.getLocalPart());
        options.setTransportInProtocol(Constants.TRANSPORT_JMS);

        OMFactory fac = OMAbstractFactory.getOMFactory();

        OMNamespace omNs = fac.createOMNamespace("http://localhost/axis2/services/EchoXMLService", "my");
        OMElement method = fac.createOMElement("echoOMElement", omNs);
        OMElement value = fac.createOMElement("myValue", omNs);
        value.setText("Isaac Asimov, The Foundation Trilogy");
        method.addChild(value);
//        SOAPFactory factory = OMAbstractFactory.getSOAP11Factory();
//        SOAPEnvelope envelope = factory.getDefaultEnvelope();
//        envelope.getBody().addChild(method);
//
//        MessageContext requestContext = new MessageContext(configContext);
        AxisService srevice = new AxisService(serviceName.getLocalPart());
        srevice.addOperation(opdesc);
//        configContext.getAxisConfiguration().addService(srevice);
//        requestContext.setAxisService(service);
//        requestContext.setAxisOperation(opdesc);

//        requestContext.setEnvelope(envelope);
        //  call.invokeBlocking(opdesc, requestContext);

//        SOAPEnvelope env = call.invokeBlocking("echoOMElement", envelope);

        AxisConfiguration axisConfig = configContext.getAxisConfiguration();
        axisConfig.addService(srevice);

        ServiceClient sender = new ServiceClient(configContext, clientService);
        sender.setOptions(options);

        OMElement result = sender.sendReceive(operationName, method);


        result.serialize(XMLOutputFactory.newInstance().createXMLStreamWriter(
                System.out));
    }
}
