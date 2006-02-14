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
import org.apache.axis2.engine.util.FaultHandler;
import org.apache.axis2.engine.util.TestConstants;
import org.apache.axis2.integration.TestingUtils;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.ws.commons.om.OMAbstractFactory;
import org.apache.ws.commons.om.OMElement;
import org.apache.ws.commons.soap.SOAP11Constants;
import org.apache.ws.commons.soap.SOAP12Constants;
import org.apache.ws.commons.soap.SOAPEnvelope;
import org.apache.ws.commons.soap.SOAPFactory;
import org.apache.ws.commons.soap.SOAPFault;
import org.apache.wsdl.WSDLConstants;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.util.ArrayList;

public class FaultHandlingTest extends TestCase implements TestConstants {

    protected String testResourceDir = "test-resources";

    protected void setUp() throws Exception {
        UtilServer.start();
    }

    public void testFaultHandling() throws AxisFault {
        ConfigurationContext configurationContext = UtilServer.getConfigurationContext();
        ArrayList inPhasesUptoAndIncludingPostDispatch = configurationContext.getAxisConfiguration().getInPhasesUptoAndIncludingPostDispatch();
        Phase phaseOne = (Phase) inPhasesUptoAndIncludingPostDispatch.get(0);
        phaseOne.addHandler(new FaultHandler());


        OMElement payload = TestingUtils.createDummyOMElement();
        Options options = new Options();
        options.setTo(targetEPR);
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
        options.setExceptionToBeThrownOnSOAPFault(false);
        options.setProperty(HTTPConstants.CONNECTION_TIMEOUT, new Integer(60000 *20));
        options.setProperty(HTTPConstants.SO_TIMEOUT, new Integer(60000 *20));

        ConfigurationContext configContext =
                ConfigurationContextFactory.createConfigurationContextFromFileSystem("target/test-resources/integrationRepo", null);
        ServiceClient sender = new ServiceClient(configContext, null);
        sender.setOptions(options);

        OMElement result = sender.sendReceive(payload);

        System.out.println("result = " + result);


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
