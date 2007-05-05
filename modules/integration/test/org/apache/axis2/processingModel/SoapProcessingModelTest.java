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

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.Echo;
import org.apache.axis2.engine.util.TestConstants;
import org.apache.axis2.integration.TestingUtils;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.integration.UtilServerBasedTestCase;
import org.apache.axis2.util.Utils;

public class SoapProcessingModelTest extends UtilServerBasedTestCase implements TestConstants {

    private AxisService clientService;

    public SoapProcessingModelTest() {
        super(SoapProcessingModelTest.class.getName());
    }

    public SoapProcessingModelTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return getTestSetup(new TestSuite(SoapProcessingModelTest.class));
    }

    protected void setUp() throws Exception {
        AxisService service = Utils.createSimpleService(serviceName, Echo.class.getName(),
                                                        operationName);
        clientService = Utils.createSimpleServiceforClient(serviceName, Echo.class.getName(),
                                                           operationName);
        UtilServer.deployService(service);

    }

    protected void tearDown() throws Exception {
        UtilServer.unDeployService(serviceName);
    }

    public void sendMessageWithHeader(SOAPEnvelope envelope) throws Exception {
        ServiceClient serviceClient;

        try {
            ConfigurationContext configContext = Utils
                    .getNewConfigurationContext(Constants.TESTING_REPOSITORY);
            serviceClient = new ServiceClient(configContext, clientService);

            MessageContext msgctx = configContext.createMessageContext();

            msgctx.setEnvelope(envelope);

            Options options = new Options();
            serviceClient.setOptions(options);
            options.setTo(targetEPR);
            options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
            options.setAction(Constants.AXIS2_NAMESPACE_URI + "/" + operationName.getLocalPart());

            OperationClient opClient = serviceClient.createClient(operationName);
            opClient.addMessageContext(msgctx);
            opClient.setOptions(options);
            opClient.execute(true);

        } catch (AxisFault fault) {
            // This should be a MustUnderstand fault
            assertEquals(fault.getFaultCode(), SOAP12Constants.QNAME_MU_FAULTCODE);
            return;
        }
        fail("MU header was processed");
    }

    public void testSendingMustUnderstandWithNextRole() throws Exception {
        SOAPFactory fac = OMAbstractFactory.getSOAP12Factory();
        SOAPEnvelope envelope = fac.getDefaultEnvelope();
        OMNamespace headerNs = fac
                .createOMNamespace("http://dummyHeader", "dh");
        SOAPHeaderBlock h1 = fac.createSOAPHeaderBlock("DummyHeader", headerNs,
                                                       envelope.getHeader());
        h1.setMustUnderstand(true);
        h1.addChild(fac.createOMText("Dummy String"));
        h1.setRole(SOAP12Constants.SOAP_ROLE_NEXT);
        OMElement payload = TestingUtils.createDummyOMElement();
        envelope.getBody().addChild(payload);
        sendMessageWithHeader(envelope);
    }
}
