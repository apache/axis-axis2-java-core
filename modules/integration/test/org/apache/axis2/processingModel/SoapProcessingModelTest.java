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

package org.apache.axis2.processingModel;

// todo

import junit.framework.TestCase;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.engine.Echo;
import org.apache.axis2.engine.util.TestConstants;
import org.apache.axis2.integration.TestingUtils;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.util.Utils;
import org.apache.ws.commons.om.OMAbstractFactory;
import org.apache.ws.commons.om.OMElement;
import org.apache.ws.commons.om.OMNamespace;
import org.apache.ws.commons.soap.SOAP12Constants;
import org.apache.ws.commons.soap.SOAPEnvelope;
import org.apache.ws.commons.soap.SOAPFactory;
import org.apache.ws.commons.soap.SOAPHeaderBlock;

public class SoapProcessingModelTest extends TestCase implements TestConstants {

    private AxisService service;
    private AxisService clientService;

    private boolean finish = false;

    public SoapProcessingModelTest() {
        super(SoapProcessingModelTest.class.getName());
    }

    public SoapProcessingModelTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        UtilServer.start();
        service = Utils.createSimpleService(serviceName, Echo.class.getName(),
                operationName);
        clientService = Utils.createSimpleServiceforClient(serviceName, Echo.class.getName(),
                operationName);
        UtilServer.deployService(service);

    }

    protected void tearDown() throws Exception {
        UtilServer.unDeployService(serviceName);
        UtilServer.stop();
    }

    public void sendMessageWithHeader(SOAPEnvelope envelope) throws AxisFault {
        ServiceClient serviceClient = null;

        try {
            ConfigurationContext configContext = Utils
                    .getNewConfigurationContext(Constants.TESTING_REPOSITORY);
            configContext.getAxisConfiguration().addService(clientService);

            ServiceContext serviceContext = new ServiceGroupContext(
                    configContext, (AxisServiceGroup) clientService.getParent())
                    .getServiceContext(clientService);
            serviceClient = new ServiceClient(configContext, clientService);

            MessageContext msgctx = new MessageContext();
            msgctx.setConfigurationContext(serviceContext.getConfigurationContext());

            msgctx.setEnvelope(envelope);

            Options options = new Options();
            serviceClient.setOptions(options);
            options.setTo(targetEPR);
            options.setTransportInProtocol(Constants.TRANSPORT_HTTP);

            OperationClient opClient = serviceClient.createClient(operationName);
            opClient.addMessageContext(msgctx);
            opClient.setOptions(options);
            opClient.execute(true);

        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception Occurred !! ." + e.getMessage());
            throw new AxisFault(e);
        } finally {
        }
    }

    public void testSendingMustUnderstandWithNextRole() throws Exception {
        SOAPFactory fac = OMAbstractFactory.getSOAP12Factory();
        SOAPEnvelope envelope = fac.getDefaultEnvelope();
        OMNamespace headerNs = fac
                .createOMNamespace("http://dummyHeader", "dh");
        SOAPHeaderBlock h1 = fac.createSOAPHeaderBlock("DummyHeader", headerNs,
                envelope.getHeader());
        h1.setMustUnderstand(true);
        h1.addChild(fac.createText("Dummy String"));
        h1.setRole(SOAP12Constants.SOAP_ROLE_NEXT);
        OMElement payload = TestingUtils.createDummyOMElement();
        envelope.getBody().addChild(payload);
        sendMessageWithHeader(envelope);

    }

    // public void testSendingMustUnderstandWithArbitaryRole() throws Exception
    // {
    // try {
    // SOAPFactory fac = OMAbstractFactory.getSOAP12Factory();
    // SOAPEnvelope envelope = fac.getDefaultEnvelope();
    // OMNamespace headerNs = fac.createOMNamespace("http://dummyHeader", "dh");
    // SOAPHeaderBlock h1 =
    // fac.createSOAPHeaderBlock("DummyHeader", headerNs, envelope.getHeader());
    // h1.setMustUnderstand(true);
    // h1.addChild(fac.createText("Dummy String"));
    // h1.setRole("http://myOwnRole");
    // OMElement payload = TestingUtils.createDummyOMElement();
    // envelope.getBody().addChild(payload);
    // sendMessageWithHeader(envelope);
    //
    // } catch (Exception e) {
    // e.printStackTrace();
    // assertTrue(e.getMessage().indexOf("Must Understand check failed") > -1);
    // }
    // }
}
