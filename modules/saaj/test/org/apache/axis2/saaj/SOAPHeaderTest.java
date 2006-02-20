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

import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.namespace.QName;
import java.util.Iterator;

public class SOAPHeaderTest extends TestCase {

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

}
