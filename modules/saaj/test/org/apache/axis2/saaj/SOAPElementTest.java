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
import org.apache.axis2.om.impl.dom.NodeImpl;

import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.Node;
import javax.xml.soap.Text;
import java.util.List;
import java.util.Iterator;

public class SOAPElementTest extends TestCase {

    private SOAPElement soapEle;

    protected void setUp() throws Exception {
        soapEle =
                SOAPFactoryImpl.newInstance().createElement("Test",
                                                            "test",
                                                            "http://test.apache.org/");
    }

    public void testAddTextNode() {
        assertNotNull(soapEle);
        String value = "foo";
        try {
            soapEle.addTextNode(value);
        } catch (SOAPException e) {
            fail("Unexpected Exception " + e);
        }
        assertEquals(value, soapEle.getValue());
        TextImplEx text = assertContainsText(soapEle);
        assertEquals(value, text.getValue());
    }

    public void testChildren() {
        try {
            soapEle.addTextNode("foo");
            SOAPElement childEle1 =
                    SOAPFactoryImpl.newInstance().createElement("Child1",
                                                                "ch",
                                                                "http://test.apache.org/");
            SOAPElement childEle2 =
                    SOAPFactoryImpl.newInstance().createElement("Child2",
                                                                "ch",
                                                                "http://test.apache.org/");
            soapEle.addChildElement(childEle1);
            soapEle.addChildElement(childEle2);
        } catch (SOAPException e) {
            fail("Unexpected Exception " + e);
        }

        Object o = soapEle.getChildElements().next();
        Object o2 = soapEle.getChildElements().next();

        assertSame(o, o2); // both elements should be the same SAAJ Node
        assertEquals(((javax.xml.soap.Text) o).getValue(),
                     ((javax.xml.soap.Text) o2).getValue());

        int childrenCount = 0;
        for (Iterator iter = soapEle.getChildElements(); iter.hasNext();) {
            iter.next();
            childrenCount ++;
        }
        assertEquals(3, childrenCount);

        Object z1 = soapEle.getChildNodes().item(0);
        Object z2 = soapEle.getFirstChild();

        assertSame(o, z1);   // should be same SAAJ Node
        assertSame(z1, z2);  // should be same SAAJ Node

        assertEquals(((javax.xml.soap.Text) z1).getNodeValue(),
                     ((javax.xml.soap.Text) z2).getNodeValue());

        Node lastChildNode = (Node) soapEle.getLastChild();
        SOAPElement lastChildSOAPEle = (SOAPElement) lastChildNode;

        assertEquals("Child2", lastChildSOAPEle.getLocalName());
        assertEquals("http://test.apache.org/", lastChildSOAPEle.getNamespaceURI());
        assertEquals("ch", lastChildSOAPEle.getPrefix());
    }

    public void testChildrenAndSiblings() {
        try {
            soapEle.addTextNode("foo");
            soapEle.addChildElement("Child1", "ch", "http://test.apache.org/");
            soapEle.addChildElement("Child2", "ch", "http://test.apache.org/");
        } catch (SOAPException e) {
            fail("Unexpected Exception " + e);
        }

        Object o = soapEle.getChildElements().next();
        Object o2 = soapEle.getChildElements().next();
        assertSame(o, o2); // both elements should be the same SAAJ Node
        assertEquals(((javax.xml.soap.Text) o).getValue(),
                     ((javax.xml.soap.Text) o2).getValue());

        int childrenCount = 0;
        for (Iterator iter = soapEle.getChildElements(); iter.hasNext();) {
            iter.next();
            childrenCount ++;
        }
        assertEquals(3, childrenCount);

        Object z1 = soapEle.getChildNodes().item(0);
        Object z2 = soapEle.getFirstChild();
        assertSame(o, z1);   // should be same SAAJ Node
        assertSame(z1, z2);  // should be same SAAJ Node
        assertEquals(((javax.xml.soap.Text) z1).getNodeValue(),
                     ((javax.xml.soap.Text) z2).getNodeValue());

        SOAPElement lastChildSOAPEle = (SOAPElement) soapEle.getLastChild();

        assertEquals("Child2", lastChildSOAPEle.getLocalName());
        assertEquals("ch:Child2", lastChildSOAPEle.getNodeName());
        assertEquals("http://test.apache.org/", lastChildSOAPEle.getNamespaceURI());
        assertEquals("ch", lastChildSOAPEle.getPrefix());
        assertNotNull(lastChildSOAPEle.getParentNode());
        assertTrue(lastChildSOAPEle.getPreviousSibling() instanceof javax.xml.soap.SOAPElement);
        assertNull(lastChildSOAPEle.getNextSibling());

        javax.xml.soap.Node firstChild = (javax.xml.soap.Node) soapEle.getFirstChild();
        javax.xml.soap.Node nextSibling = (javax.xml.soap.Node) (firstChild.getNextSibling());
        assertNull(firstChild.getPreviousSibling());

        assertTrue(firstChild instanceof javax.xml.soap.Text);
        assertTrue(nextSibling instanceof javax.xml.soap.SOAPElement);
        assertTrue(nextSibling.getPreviousSibling() instanceof javax.xml.soap.Text);
        assertEquals("Child1", nextSibling.getLocalName());
        assertEquals("ch:Child1", nextSibling.getNodeName());
        assertEquals("http://test.apache.org/", nextSibling.getNamespaceURI());
        assertEquals("ch", nextSibling.getPrefix());

        javax.xml.soap.Node nextSibling2 = (javax.xml.soap.Node) nextSibling.getNextSibling();
        assertEquals("Child2", nextSibling2.getLocalName());
        assertEquals("ch:Child2", nextSibling2.getNodeName());
        assertEquals("http://test.apache.org/", lastChildSOAPEle.getNamespaceURI());
        assertEquals("ch", nextSibling2.getPrefix());
        assertNull(nextSibling2.getNextSibling());
    }

    public void testCommentSibling() {
        try {
            soapEle.addTextNode("foo");
            soapEle.addChildElement("Child1", "ch", "http://test.apache.org/");
            soapEle.addTextNode("<!-- This is a Comment-->");
            soapEle.addChildElement("Child2", "ch", "http://test.apache.org/");
        } catch (SOAPException e) {
            fail("Unexpected Exception " + e);
        }

        assertTrue(((Text) soapEle.getFirstChild().getNextSibling().getNextSibling()).isComment());
        assertTrue(((Text) soapEle.getLastChild().getPreviousSibling()).isComment());
    }

    public void testCommentSibling2() {
        try {
            soapEle.addTextNode("foo");
            soapEle.addTextNode("<!-- This is a Comment-->");
            soapEle.addTextNode("bar");
            soapEle.addChildElement("Child1", "ch", "http://test.apache.org/");
            soapEle.addChildElement("Child2", "ch", "http://test.apache.org/");
        } catch (SOAPException e) {
            fail("Unexpected Exception " + e);
        }

        assertTrue(((Text) soapEle.getFirstChild().getNextSibling()).isComment());
        assertFalse(((Text) soapEle.getLastChild().getPreviousSibling().getPreviousSibling()).isComment());
        assertFalse(((Text) soapEle.getLastChild().getPreviousSibling().getPreviousSibling()).isComment());
    }

    private TextImplEx assertContainsText(SOAPElement soapElem) {
        assertTrue(soapElem.hasChildNodes());
        List childElems = toList(soapElem.getChildElements());
        assertTrue(childElems.size() == 1);
        NodeImpl node = (NodeImpl) childElems.get(0);
        assertTrue(node instanceof TextImplEx);
        return (TextImplEx) node;
    }

    private List toList(java.util.Iterator iter) {
        List list = new java.util.ArrayList();
        while (iter.hasNext()) {
            list.add(iter.next());
        }
        return list;
    }
}
