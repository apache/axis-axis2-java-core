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

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class SOAPFaultTest extends AbstractTestCase {
    private SOAPEnvelope soapEnvelope;
    private XMLStreamWriter writer;

    /**
     * Constructor.
     */
    public SOAPFaultTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
        writer = XMLOutputFactory.newInstance().
                createXMLStreamWriter(System.out);
    }

    public void testSOAPFault() throws Exception {
//        soapEnvelope = (SOAPEnvelope) OMTestUtils.getOMBuilder(getTestResourceFile("soap/minimalMessage.xml")).getDocumentElement();
//        SOAPBody soapBody = soapEnvelope.getBody();
//        SOAPFault soapFault = OMFactory.newInstance().createSOAPFault(soapBody, new Exception("Something has gone wrong som where !!"));
//        soapBody.addFault(soapFault);
//        soapFault.setCode(null);
//        assertEquals("faultcode returned is incorrect", soapFault.getCode().getLocalName(), "Axis");
//        assertEquals("faultcode returned is incorrect", soapFault.getCode().getNamespace().getPrefix(), "SOAP-ENV");



    }

    private void print() throws XMLStreamException {
        soapEnvelope.serializeWithCache(writer);
        writer.flush();
    }
}
