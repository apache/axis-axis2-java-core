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
import org.apache.axis2.engine.util.MyInOutMEPClient;
import org.apache.axis2.engine.util.TestConstants;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.soap.SOAP11Constants;
import org.apache.axis2.soap.SOAP12Constants;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.SOAPFactory;
import org.apache.axis2.soap.SOAPFault;
import org.apache.axis2.soap.impl.llom.builder.StAXSOAPModelBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileReader;

public class FaultHandlingTest extends TestCase implements TestConstants {

    private Log log = LogFactory.getLog(getClass());

    protected String testResourceDir = "test-resources";
    private MyInOutMEPClient inOutMEPClient;


    private boolean finish = false;

    protected void setUp() throws Exception {
            UtilServer.start();
            inOutMEPClient = getMyInOutMEPClient();
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
            inOutMEPClient.setExceptionToBeThrownOnSOAPFault(false);
            return inOutMEPClient.invokeBlockingWithEnvelopeOut(operationName.getLocalPart(), inEnvelope);
    }

    private SOAPEnvelope getTwoHeadersSOAPEnvelope(SOAPFactory fac) {
        SOAPEnvelope soapEnvelope = fac.createSOAPEnvelope();
        fac.createSOAPHeader(soapEnvelope);
        fac.createSOAPHeader(soapEnvelope);
        fac.createSOAPBody(soapEnvelope);
        return soapEnvelope;
    }

    private MyInOutMEPClient getMyInOutMEPClient() throws AxisFault {
        MyInOutMEPClient inOutMEPClient = new MyInOutMEPClient("target/test-resources/intregrationRepo");
        inOutMEPClient.setSoapVersionURI(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
        inOutMEPClient.setTo(targetEPR);
        inOutMEPClient.setTransportInfo(Constants.TRANSPORT_HTTP,
                Constants.TRANSPORT_HTTP,
                false);
        return inOutMEPClient;
    }

    private SOAPEnvelope createEnvelope(String fileName) throws Exception {
        if (fileName == "" || fileName == null) {
            throw new Exception("A SOAP file name must be provided !!");
        }
        XMLStreamReader parser = XMLInputFactory.newInstance()
                .createXMLStreamReader(new FileReader(getTestResourceFile(fileName)));

        return (SOAPEnvelope) new StAXSOAPModelBuilder(parser, null).getDocumentElement();
    }

    public File getTestResourceFile(String relativePath) {
        return new File(testResourceDir, relativePath);
    }

    protected void tearDown() throws Exception {
        UtilServer.stop();
        inOutMEPClient.close();
    }

}
