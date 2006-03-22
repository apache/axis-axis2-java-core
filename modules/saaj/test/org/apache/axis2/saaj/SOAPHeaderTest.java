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
package org.apache.axis2.saaj;

import junit.framework.TestCase;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import java.util.Iterator;

public class SOAPHeaderTest extends TestCase {
    private MessageFactory mf = null;
    private SOAPMessage msg = null;
    private SOAPPart sp = null;
    private SOAPEnvelope envelope = null;
    private SOAPHeader hdr = null;
    private SOAPHeaderElement she1 = null;
    private SOAPHeaderElement she2 = null;

    public SOAPHeaderTest(String name) {
        super(name);
    }

    public void testAddHeaderElements() throws Exception {
        javax.xml.soap.SOAPMessage soapMessage =
                javax.xml.soap.MessageFactory.newInstance().createMessage();
        javax.xml.soap.SOAPEnvelope soapEnv =
                soapMessage.getSOAPPart().getEnvelope();
        javax.xml.soap.SOAPHeader header = soapEnv.getHeader();
        assertTrue(header.addChildElement("ebxmlms1") instanceof SOAPHeaderElement);
        assertTrue(header.addChildElement("ebxmlms2", "ch2", "http;//test.apache.org") instanceof SOAPHeaderElement);
        assertTrue(header.addHeaderElement(soapEnv.createName("ebxmlms3", "ch3", "http://test2.apache.org")) != null);
        assertTrue(header.addHeaderElement(soapEnv.createName("ebxmlms4")) != null);
        assertTrue(header.addHeaderElement(new PrefixedQName("http://test3.apache.org", "ebxmlms5", "ch5")) != null);

        SOAPHeaderElement firstChild = (SOAPHeaderElement) header.getFirstChild();
        assertEquals("ebxmlms1", firstChild.getLocalName());
        assertEquals("", firstChild.getPrefix());
        assertEquals("", firstChild.getNamespaceURI());

        SOAPHeaderElement secondChild = (SOAPHeaderElement) firstChild.getNextSibling();
        assertEquals("ebxmlms2", secondChild.getLocalName());
        assertEquals("ch2", secondChild.getPrefix());
        assertEquals("http;//test.apache.org", secondChild.getNamespaceURI());

        SOAPHeaderElement thirdChild = (SOAPHeaderElement) secondChild.getNextSibling();
        assertEquals("ebxmlms3", thirdChild.getLocalName());
        assertEquals("ch3", thirdChild.getPrefix());
        assertEquals("http://test2.apache.org", thirdChild.getNamespaceURI());

        SOAPHeaderElement lastChild = (SOAPHeaderElement) header.getLastChild();
        assertEquals("ebxmlms5", lastChild.getLocalName());
        assertEquals("ch5", lastChild.getPrefix());
        assertEquals("http://test3.apache.org", lastChild.getNamespaceURI());

        SOAPHeaderElement fourthChild = (SOAPHeaderElement) lastChild.getPreviousSibling();
        assertEquals("ebxmlms4", fourthChild.getLocalName());
        assertEquals("", fourthChild.getPrefix());
        assertEquals("", fourthChild.getNamespaceURI());

        Iterator it = header.getChildElements();
        int numOfHeaderElements = 0;
        while (it.hasNext()) {
            Object o = it.next();
            assertTrue(o instanceof SOAPHeaderElement);
            SOAPHeaderElement el = (SOAPHeaderElement) o;
            String lName = el.getLocalName();
            assertTrue(lName.equals("ebxmlms" + ++numOfHeaderElements));
        }
        assertEquals(5, numOfHeaderElements);
    }

    public void testHeaders() {
        try {
            // Create message factory and SOAP factory
            MessageFactory messageFactory = MessageFactory.newInstance();
            SOAPFactory soapFactory = SOAPFactory.newInstance();

            // Create a message
            SOAPMessage message = messageFactory.createMessage();

            // Get the SOAP header from the message and
            //  add headers to it
            SOAPHeader header = message.getSOAPHeader();

            String nameSpace = "ns";
            String nameSpaceURI = "http://gizmos.com/NSURI";

            Name order =
                    soapFactory.createName("orderDesk", nameSpace, nameSpaceURI);
            SOAPHeaderElement orderHeader = header.addHeaderElement(order);
            orderHeader.setActor("http://gizmos.com/orders");

            Name shipping =
                    soapFactory.createName("shippingDesk", nameSpace, nameSpaceURI);
            SOAPHeaderElement shippingHeader =
                    header.addHeaderElement(shipping);
            shippingHeader.setActor("http://gizmos.com/shipping");

            Name confirmation =
                    soapFactory.createName("confirmationDesk", nameSpace,
                                           nameSpaceURI);
            SOAPHeaderElement confirmationHeader =
                    header.addHeaderElement(confirmation);
            confirmationHeader.setActor("http://gizmos.com/confirmations");

            Name billing =
                    soapFactory.createName("billingDesk", nameSpace, nameSpaceURI);
            SOAPHeaderElement billingHeader = header.addHeaderElement(billing);
            billingHeader.setActor("http://gizmos.com/billing");

            // Add header with mustUnderstand attribute
            Name tName =
                    soapFactory.createName("Transaction", "t",
                                           "http://gizmos.com/orders");

            SOAPHeaderElement transaction = header.addHeaderElement(tName);
            transaction.setMustUnderstand(true);
            transaction.addTextNode("5");

            // Get the SOAP body from the message but leave
            // it empty
            SOAPBody body = message.getSOAPBody();

            message.saveChanges();

            // Display the message that would be sent
            System.out.println("\n----- Request Message ----\n");
            message.writeTo(System.out);

            // Look at the headers
            Iterator allHeaders = header.examineAllHeaderElements();

            while (allHeaders.hasNext()) {
                SOAPHeaderElement headerElement =
                        (SOAPHeaderElement) allHeaders.next();
                Name headerName = headerElement.getElementName();
                System.out.println("\nHeader name is " +
                                   headerName.getQualifiedName());
                System.out.println("Actor is " + headerElement.getActor());
                System.out.println("mustUnderstand is " +
                                   headerElement.getMustUnderstand());
            }
        } catch (Exception e) {
            fail("Enexpected Exception " + e);
        }
    }

    protected void setUp() throws Exception {
        msg = MessageFactory.newInstance().createMessage();
        sp = msg.getSOAPPart();
        envelope = sp.getEnvelope();
        hdr = envelope.getHeader();
    }

    public void testExamineHeader() {
        SOAPHeaderElement she = null;

        try {
            she1 = hdr.addHeaderElement(envelope.createName("foo1", "f1", "foo1-URI"));
            she1.setActor("actor-URI");
            Iterator iterator = hdr.examineAllHeaderElements();
            int cnt = 0;
            while (iterator.hasNext()) {
                cnt++;
                she = (SOAPHeaderElement) iterator.next();
                if (!she.equals(she1)) {
                    fail("SOAP Header Elements do not match");
                }
            }

            if (cnt != 1) {
                fail("SOAPHeaderElement count mismatch: expected 1, received " + cnt);
            }

            iterator = hdr.examineAllHeaderElements();
            if (!iterator.hasNext()) {
                fail("no elements in iterator - unexpected");
            }

        } catch (Exception e) {
            fail("Unexpected Exception: " + e);
        }
    }
}
