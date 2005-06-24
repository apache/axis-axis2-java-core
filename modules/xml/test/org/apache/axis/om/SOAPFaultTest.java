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
package org.apache.axis.om;

import org.apache.axis.om.impl.llom.OMOutput;
import org.apache.axis.soap.SOAPEnvelope;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;


public class SOAPFaultTest extends AbstractTestCase {
    private SOAPEnvelope soapEnvelope;
    private OMOutput omOutput;

    /**
     * Constructor.
     */
    public SOAPFaultTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
        omOutput = new OMOutput(XMLOutputFactory.newInstance().
                createXMLStreamWriter(System.out));
    }

    public void testSOAPFault() throws Exception {
//        soapEnvelope = (SOAPEnvelope) OMTestUtils.getOMBuilder(getTestResourceFile("soap/minimalMessage.xml")).getDocumentElement();
//        SOAPBody soapBody = soapEnvelope.getBody();
//        SOAPFault soapFault = OMAbstractFactory.getSOAP11Factory().createSOAPFault(soapBody, new Exception("Something has gone wrong som where !!"));
//        soapBody.addFault(soapFault);
//        soapFault.setCode(null);
//        assertEquals("faultcode returned is incorrect", soapFault.getCode().getLocalName(), "Axis");
//        assertEquals("faultcode returned is incorrect", soapFault.getCode().getNamespace().getPrefix(), "SOAP-ENV");



    }

    private void print() throws XMLStreamException {
        soapEnvelope.serializeWithCache(omOutput);
        omOutput.flush();
    }
}
