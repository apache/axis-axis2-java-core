/**
 * Copyright 2001-2004 The Apache Software Foundation.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p/>
 * Author: Eran Chinthaka - Lanka Software Foundation
 * Date: Dec 23, 2004
 * Time: 12:22:56 PM
 */
package org.apache.axis.om.impl.traverse;

import org.apache.axis.addressing.AddressingConstants;
import org.apache.axis.impl.llom.traverse.OMChildrenWithSpecificAttributeIterator;
import org.apache.axis.om.OMConstants;
import org.apache.axis.om.OMTestCase;
import org.apache.axis.om.SOAPHeader;
import org.apache.axis.om.SOAPHeaderBlock;

import javax.xml.namespace.QName;
import java.io.ByteArrayInputStream;
import java.util.Iterator;


public class OMChildrenWithSpecificAttributeIteratorTest extends OMTestCase implements OMConstants {

    private String sampleSOAPMessage = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:wsa=\"http://schemas.xmlsoap.org/ws/2004/03/addressing\">\n" +
            "    <soapenv:Header>\n" +
            "        <wsa:MessageID soapenv:mustUnderstand=\"1\">uuid:920C5190-0B8F-11D9-8CED-F22EDEEBF7E5</wsa:MessageID>\n" +
            "        <wsa:To soapenv:mustUnderstand=\"0\">http://localhost:8081/axis/services/BankPort</wsa:To>\n" +
            "        <wsa:From soapenv:mustUnderstand=\"1\">\n" +
            "            <Address xmlns=\"http://schemas.xmlsoap.org/ws/2004/03/addressing\">http://schemas.xmlsoap.org/ws/2004/03/addressing/role/anonymous</Address>\n" +
            "        </wsa:From>\n" +
            "    </soapenv:Header>\n" +
            "    <soapenv:Body>\n" +
            "        <axis2:echoVoid xmlns:axis2=\"http://ws.apache.org/axis2\" ></axis2:echoVoid>\n" +
            "    </soapenv:Body>\n" +
            "</soapenv:Envelope>";

    public OMChildrenWithSpecificAttributeIteratorTest(String testName) {
        super(testName);
    }


    public static void main(String[] args) {
        junit.textui.TestRunner
                .run(OMChildrenWithSpecificAttributeIteratorTest.class);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testChildrenRetrievalWithNoDetach() {
        try {
            soapEnvelope = getOMBuilder(new ByteArrayInputStream(sampleSOAPMessage.getBytes())).getSOAPEnvelope();
            SOAPHeader soapHeader = soapEnvelope.getHeader();

            // getting header blocks with mustUnderstand="0"
            OMChildrenWithSpecificAttributeIterator iter = new OMChildrenWithSpecificAttributeIterator(soapHeader.getFirstChild(), new QName(SOAP_ENVELOPE_NAMESPACE_URI, ATTR_MUSTUNDERSTAND), "0", false);

            if (iter.hasNext()) {
                SOAPHeaderBlock soapHeaderBlock = (SOAPHeaderBlock) iter.next();
                assertEquals("Header Block with mustUnderstand=\")0\" has not been retrieved properly", soapHeaderBlock.getLocalName(), AddressingConstants.WSA_TO);
            }

            if (iter.hasNext()) {
                fail("Given sample SOAP doesn't have more than one mustunderstand false header blocks");
            }


        } catch (Exception e) {
            e.printStackTrace();
            fail("Something has gone wrong in accessing the test xml file");
        }
    }

    public void testChildrenRetrievalWithDetach() throws Exception {
        soapEnvelope = getOMBuilder(new ByteArrayInputStream(sampleSOAPMessage.getBytes())).getSOAPEnvelope();
        SOAPHeader soapHeader = soapEnvelope.getHeader();

        OMChildrenWithSpecificAttributeIterator iter = new OMChildrenWithSpecificAttributeIterator(soapHeader.getFirstChild(), new QName(SOAP_ENVELOPE_NAMESPACE_URI, ATTR_MUSTUNDERSTAND), "0", true);
        if (iter.hasNext()) {
            SOAPHeaderBlock soapHeaderBlock = (SOAPHeaderBlock) iter.next();
            assertEquals("Header Block with mustUnderstand=\")0\" has not been retrieved properly", soapHeaderBlock.getLocalName(), AddressingConstants.WSA_TO);
        }

        Iterator soapHeaderChildrenIter = soapHeader.getChildren();
        int childrenCount = 0;
        while (soapHeaderChildrenIter.hasNext()) {
            Object o = soapHeaderChildrenIter.next();
             childrenCount++;
        }

        assertEquals("OMChildrenWithSpecificAttributeIterator with detach true, not working properly", childrenCount, 6); // here this 6 includes white spaces as OMText

    }


}
