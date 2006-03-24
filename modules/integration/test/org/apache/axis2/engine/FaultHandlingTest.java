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
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPFault;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
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
import org.apache.wsdl.WSDLConstants;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;

public class FaultHandlingTest extends TestCase implements TestConstants {

    protected String testResourceDir = "test-resources";

    protected void setUp() throws Exception {
        UtilServer.start();
    }

    public void testFaultHandling() throws AxisFault {
        ConfigurationContext configurationContext = UtilServer.getConfigurationContext();
        ArrayList inPhasesUptoAndIncludingPostDispatch = configurationContext.getAxisConfiguration().getGlobalInFlow();
        Phase phaseOne = (Phase) inPhasesUptoAndIncludingPostDispatch.get(0);
        phaseOne.addHandler(new FaultHandler());

        ConfigurationContext configContext =
                ConfigurationContextFactory.createConfigurationContextFromFileSystem("target/test-resources/integrationRepo", null);
        ServiceClient sender = new ServiceClient(configContext, null);

        OMElement payload = TestingUtils.createDummyOMElement();

        // test with SOAP 1.2
        Options options = new Options();
        options.setTo(targetEPR);
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
        options.setExceptionToBeThrownOnSOAPFault(false);
        options.setSoapVersionURI(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
        sender.setOptions(options);

        String result = sender.sendReceive(payload).toString();

        assertTrue(result.indexOf(FaultHandler.M_FAULT_EXCEPTION) > -1);
        assertTrue(result.indexOf(FaultHandler.DETAIL_MORE_INFO) > -1);
        assertTrue(result.indexOf(FaultHandler.FAULT_REASON) > -1);

        // test with SOAP 1.1
        options = new Options();
        options.setTo(targetEPR);
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
        options.setExceptionToBeThrownOnSOAPFault(false);
        options.setSoapVersionURI(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);
        sender.setOptions(options);

        result = sender.sendReceive(payload).toString();

        assertTrue(result.indexOf(FaultHandler.M_FAULT_EXCEPTION) > -1);
        assertTrue(result.indexOf(FaultHandler.DETAIL_MORE_INFO) > -1);
        assertTrue(result.indexOf(FaultHandler.FAULT_REASON) > -1);

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
     public void testRefParamsWithFaultTo() throws AxisFault, XMLStreamException {
        SOAPEnvelope soapEnvelope = getSOAPEnvelopeWithRefParamsInFaultTo();
        SOAPEnvelope resposeEnvelope = getResponse(soapEnvelope);

         System.out.println("resposeEnvelope = " + resposeEnvelope);
    }

    private SOAPEnvelope getSOAPEnvelopeWithRefParamsInFaultTo() throws XMLStreamException {
        String soap = "<env:Envelope xmlns:env=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:wsa=\"http://www.w3.org/2005/08/addressing\">  \n" +
                "      <env:Header>    \n" +
                "         <wsa:Action>http://example.org/action/echoIn</wsa:Action>    \n" +
                "         <wsa:To>http://www-lk.wso2.com:9762/axis2/services/wsaTestService/</wsa:To>    \n" +
                "         <wsa:MessageID>urn:uuid:BAB79B77-E9AE-4B9F-A8B4-624BB9E7E919</wsa:MessageID>    \n" +
                "         <wsa:ReplyTo>      \n" +
                "            <wsa:Address>http://www.w3.org/2005/08/addressing/anonymous</wsa:Address>      \n" +
                "            <wsa:ReferenceParameters xmlns:customer=\"http://example.org/customer\">        \n" +
                "               <customer:CustomerKey>Key#123456789</customer:CustomerKey>      \n" +
                "            </wsa:ReferenceParameters>    \n" +
                "         </wsa:ReplyTo>    \n" +
                "         <wsa:FaultTo>      \n" +
                "            <wsa:Address>http://www.w3.org/2005/08/addressing/anonymous</wsa:Address>      \n" +
                "            <wsa:ReferenceParameters xmlns:customer=\"http://example.org/customer\">        \n" +
                "               <customer:CustomerKey>Fault#123456789</customer:CustomerKey>      \n" +
                "            </wsa:ReferenceParameters>    \n" +
                "         </wsa:FaultTo>  \n" +
                "      </env:Header>  \n" +
                "      <env:Body>    \n" +
                "         <m:echoIn xmlns:m=\"http://example.org/echo\" />  \n" +
                "      </env:Body>\n" +
                "   </env:Envelope>";
        return (SOAPEnvelope) new StAXSOAPModelBuilder(XMLInputFactory.newInstance().createXMLStreamReader(new ByteArrayInputStream(soap.getBytes())), SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI).getDocumentElement();
    }


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
