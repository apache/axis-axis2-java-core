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

package org.apache.axis2.engine;

import junit.framework.TestCase;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.util.TestConstants;
import org.apache.axis2.integration.UtilServer;
import org.apache.ws.commons.om.OMAbstractFactory;
import org.apache.ws.commons.soap.*;
import org.apache.wsdl.WSDLConstants;

import javax.xml.stream.XMLStreamException;
import java.io.File;

public class FaultHandlingTest extends TestCase implements TestConstants {

    protected String testResourceDir = "test-resources";

    protected void setUp() throws Exception {
        UtilServer.start();
    }

    public void testTwoHeadersSOAPMessage() throws AxisFault, XMLStreamException {
        SOAPFactory fac = OMAbstractFactory.getSOAP12Factory();
        SOAPEnvelope soapEnvelope = getTwoHeadersSOAPEnvelope(fac);
        SOAPEnvelope resposeEnvelope = getResponse(soapEnvelope);

        checkSOAPFaultContent(resposeEnvelope);
        SOAPFault fault = resposeEnvelope.getBody().getFault();
        assertEquals(fault.getCode().getValue().getText().trim(), SOAP12Constants.FAULT_CODE_SENDER);

        fac = OMAbstractFactory.getSOAP11Factory();
        soapEnvelope = getTwoHeadersSOAPEnvelope(fac);
        resposeEnvelope = getResponse(soapEnvelope);

        checkSOAPFaultContent(resposeEnvelope);
        fault = resposeEnvelope.getBody().getFault();
        assertEquals(fault.getCode().getValue().getText().trim(), SOAP11Constants.FAULT_CODE_SENDER);

    }

//    public void testSOAPFaultSerializing(){
//        try {
//            SOAPEnvelope envelope = createEnvelope("soap/fault/test.xml");
//            SOAPEnvelope response = getResponse(envelope);
//             printElement(response);
//            assertTrue(true);
//        } catch (Exception e) {
//        }
//    }

    private void checkSOAPFaultContent(SOAPEnvelope soapEnvelope) {
        assertTrue(soapEnvelope.getBody().hasFault());
        SOAPFault fault = soapEnvelope.getBody().getFault();
        assertNotNull(fault.getCode());
        assertNotNull(fault.getCode().getValue());
        assertNotNull(fault.getReason());
        assertNotNull(fault.getReason().getText());
    }

    private SOAPEnvelope getResponse(SOAPEnvelope inEnvelope) throws AxisFault {
        ConfigurationContext confctx = ConfigurationContextFactory.
                createConfigurationContextFromFileSystem("target/test-resources/integrationRepo", null);
        ServiceClient client = new ServiceClient(confctx, null);
        Options options = new Options();
        client.setOptions(options);
        options.setSoapVersionURI(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
        options.setTo(targetEPR);
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
        options.setExceptionToBeThrownOnSOAPFault(false);
        MessageContext msgctx = new MessageContext();
        msgctx.setEnvelope(inEnvelope);
        OperationClient opClient = client.createClient(ServiceClient.ANON_OUT_IN_OP);
        opClient.addMessageContext(msgctx);
        opClient.execute(true);
        return opClient.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE).getEnvelope();
    }

    private SOAPEnvelope getTwoHeadersSOAPEnvelope(SOAPFactory fac) {
        SOAPEnvelope soapEnvelope = fac.createSOAPEnvelope();
        fac.createSOAPHeader(soapEnvelope);
        fac.createSOAPHeader(soapEnvelope);
        fac.createSOAPBody(soapEnvelope);
        return soapEnvelope;
    }

    public File getTestResourceFile(String relativePath) {
        return new File(testResourceDir, relativePath);
    }

    protected void tearDown() throws Exception {
        UtilServer.stop();
    }

}
