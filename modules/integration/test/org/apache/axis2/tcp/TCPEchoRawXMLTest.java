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

package org.apache.axis2.tcp;


import junit.framework.TestCase;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.async.AsyncResult;
import org.apache.axis2.client.async.Callback;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.OutInAxisOperation;
import org.apache.axis2.engine.Echo;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.integration.UtilsTCPServer;
import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

public class TCPEchoRawXMLTest extends TestCase {
    private EndpointReference targetEPR =
            new EndpointReference("tcp://127.0.0.1:"
                    + (UtilServer.TESTING_PORT)
                    + "/axis2/services/EchoXMLService/echoOMElement");
    private QName serviceName = new QName("EchoXMLService");
    private QName operationName = new QName("echoOMElement");

    private AxisService service;
    private AxisService clientService;
    private ConfigurationContext configContext;

    private boolean finish = false;
	private static final Log log = LogFactory.getLog(TCPEchoRawXMLTest.class);

    public TCPEchoRawXMLTest() {
        super(TCPEchoRawXMLTest.class.getName());
    }

    public TCPEchoRawXMLTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        UtilsTCPServer.start();

        //create and deploy the service
        service =
                Utils.createSimpleService(serviceName,
                        Echo.class.getName(),
                        operationName);
        UtilsTCPServer.deployService(service);
        clientService = Utils.createSimpleServiceforClient(serviceName,
                Echo.class.getName(),
                operationName);
        configContext = UtilServer.createClientConfigurationContext();
    }

    protected void tearDown() throws Exception {
        UtilsTCPServer.stop();
    }

    private OMElement createPayload() {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://localhost/my", "my");
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
        options.setTransportInProtocol(Constants.TRANSPORT_TCP);
        options.setAction(Constants.AXIS2_NAMESPACE_URI+"/"+operationName.getLocalPart());

        Callback callback = new Callback() {
            public void onComplete(AsyncResult result) {
                try {
                    result.getResponseEnvelope().serialize(StAXUtils
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
            Thread.sleep(1000);
            index++;
            if (index > 10) {
                throw new AxisFault(
                        "Server was shutdown as the async response take too long to complete");
            }
        }
        sender.cleanup();
    }

//    public void testEchoXMLSync() throws Exception {
//        OMElement payload = createPayload();
//
//        Options options = new Options();
//        options.setTo(targetEPR);
//        options.setTransportInProtocol(Constants.TRANSPORT_TCP);
//        options.setAction(Constants.AXIS2_NAMESPACE_URI+"/"+operationName.getLocalPart());
//
//        ServiceClient sender = new ServiceClient(configContext, clientService);
//        sender.setOptions(options);
//        OMElement result = sender.sendReceive(operationName, payload);
//
//        result.serialize(StAXUtils.createXMLStreamWriter(
//                System.out));
//        sender.cleanup();
//    }

//    public void testEchoXMLCompleteSync() throws Exception {
//        OMFactory fac = OMAbstractFactory.getOMFactory();
//
//        OMNamespace omNs = fac.createOMNamespace("http://localhost/my", "my");
//        OMElement payloadElement = fac.createOMElement("echoOMElement", omNs);
//        OMElement value = fac.createOMElement("myValue", omNs);
//        value.setText("Isaac Asimov, The Foundation Trilogy");
//        payloadElement.addChild(value);
//
//        Options options = new Options();
//        options.setTo(targetEPR);
//        options.setAction(Constants.AXIS2_NAMESPACE_URI+"/"+operationName.getLocalPart());
//        options.setTransportInProtocol(Constants.TRANSPORT_TCP);
//        options.setUseSeparateListener(true);
//
//        ServiceClient sender = new ServiceClient(configContext, clientService);
//        sender.setOptions(options);
//        OMElement result = sender.sendReceive(operationName, payloadElement);
//
//        result.serialize(StAXUtils.createXMLStreamWriter(
//                System.out));
//        sender.cleanup();
//
//    }
//
//    public void testEchoXMLSyncMC() throws Exception {
//        ConfigurationContext configContext =
//                ConfigurationContextFactory.createConfigurationContextFromFileSystem(Constants.TESTING_REPOSITORY, Constants.TESTING_REPOSITORY + "/conf/axis2.xml");
//
//        AxisOperation opdesc = new OutInAxisOperation(new QName("echoOMElement"));
//        Options options = new Options();
//        options.setTo(targetEPR);
//        options.setAction(operationName.getLocalPart());
//        options.setTransportInProtocol(Constants.TRANSPORT_TCP);
//
//        OMFactory fac = OMAbstractFactory.getOMFactory();
//
//        OMNamespace omNs = fac.createOMNamespace("http://localhost/my", "my");
//        OMElement method = fac.createOMElement("echoOMElement", omNs);
//        OMElement value = fac.createOMElement("myValue", omNs);
//        value.setText("Isaac Asimov, The Foundation Trilogy");
//        method.addChild(value);
//        SOAPFactory factory = OMAbstractFactory.getSOAP11Factory();
//        SOAPEnvelope envelope = factory.getDefaultEnvelope();
//        envelope.getBody().addChild(method);
//
//        MessageContext requestContext = new MessageContext();
//        requestContext.setConfigurationContext(configContext);
//        requestContext.setAxisService(clientService);
//        requestContext.setAxisOperation(opdesc);
//        requestContext.setEnvelope(envelope);
//
//        ServiceClient sender = new ServiceClient(configContext, clientService);
//        sender.setOptions(options);
//        OperationClient opClient = sender.createClient(new QName("echoOMElement"));
//        opClient.addMessageContext(requestContext);
//        opClient.setOptions(options);
//        opClient.execute(true);
//
//        MessageContext response = opClient.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
//        SOAPEnvelope env = response.getEnvelope();
//        assertNotNull(env);
//        env.getBody().serialize(StAXUtils.createXMLStreamWriter(
//                System.out));
//        sender.cleanup();
//    }


}
