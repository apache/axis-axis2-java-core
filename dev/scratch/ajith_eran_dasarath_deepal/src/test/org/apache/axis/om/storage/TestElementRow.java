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
 * Date: Sep 21, 2004
 * Time: 11:21:38 AM
 */
package org.apache.axis.om.storage;

import junit.framework.TestCase;
import org.apache.axis.om.StreamingOMBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.FileReader;


public class TestElementRow extends TestCase {

    Element ele = null;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(TestElementRow.class);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();

        //Create the builder
        XmlPullParser pullparser = XmlPullParserFactory.newInstance().newPullParser();
        pullparser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
        pullparser.setInput(new FileReader("./resource/testelement.xml"));
        StreamingOMBuilder builder = new StreamingOMBuilder(pullparser);

        Document doc = builder.getDocument();
        ele = doc.getDocumentElement();

    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGetURI() {
        String expectedURI = "http://schemas.xmlsoap.org/soap/envelope/";
        assertEquals("Problem in handling namespaces in Elements", expectedURI, ele.getNamespaceURI());
    }

    public void testGetPrefix() {
        String expectedPrefix = "soapenv";
        assertEquals("Problem in handling prefixes in Elements", expectedPrefix, ele.getPrefix());
    }

    public void testSetPrefix() {
        // this is avoided as this throws a runtime exception
    }

    public void testGetLocalName() {
        assertEquals("Problem in handling local names in Elements", "Envelope", ele.getLocalName());
    }

    public void testSetLocalName() {
        //TODO Implement setLocalName().
    }

    public void testSetNextSibling() {
        //TODO Implement setNextSibling().
    }

    public void testIsDone() {
        //TODO Implement isDone().
    }

    public void testSetDone() {
        //TODO Implement setDone().
    }

    public void testGetNodeName() {
        //TODO Implement getNodeName().
    }

    public void testGetNodeValue() {
        //TODO Implement getNodeValue().
    }

    public void testSetNodeValue() {
        //TODO Implement setNodeValue().
    }

    public void testGetNodeType() {
        //TODO Implement getNodeType().
    }

    public void testGetParentNode() {
        //TODO Implement getParentNode().
    }

    public void testGetChildNodes() {
        NodeList nodeList = ele.getChildNodes();
        NodeRow nodeRow = (NodeRow) nodeList.item(1);

        assertEquals("Children retrieval is not working in the ElementNodeList", Node.ELEMENT_NODE, nodeRow.getNodeType());

        nodeList = nodeRow.getChildNodes();
        nodeRow = (NodeRow) nodeList.item(3);
       assertEquals("Children retrieval is not working in the ElementNodeList", Node.TEXT_NODE, nodeRow.getNodeType());


    }

    public void testGetFirstChild() {
        //TODO Implement getFirstChild().
    }

    public void testGetLastChild() {
        //TODO Implement getLastChild().
    }

    public void testGetPreviousSibling() {
        //TODO Implement getPreviousSibling().
    }

    public void testGetNextSibling() {
        //TODO Implement getNextSibling().
    }

    public void testGetAttributes() {
        //TODO Implement getAttributes().
    }

    public void testGetOwnerDocument() {
        //TODO Implement getOwnerDocument().
    }

    public void testInsertBefore() {
        //TODO Implement insertBefore().
    }

    public void testReplaceChild() {
        //TODO Implement replaceChild().
    }

    public void testRemoveChild() {
        //TODO Implement removeChild().
    }

    public void testAppendChild() {
        //TODO Implement appendChild().
    }

    public void testHasChildNodes() {
        //TODO Implement hasChildNodes().
    }

    public void testCloneNode() {
        //TODO Implement cloneNode().
    }

    public void testNormalize() {
        //TODO Implement normalize().
    }

    public void testIsSupported() {
        //TODO Implement isSupported().
    }

    public void testGetNamespaceURI() {
        //TODO Implement getNamespaceURI().
    }


    public void testHasAttributes() {
        //TODO Implement hasAttributes().
    }

    public void testGetTagName() {
        //TODO Implement getTagName().
    }

    public void testGetAttribute() {
    }

    public void testSetAttribute() {
        //TODO Implement setAttribute().
    }

    public void testRemoveAttribute() {
        //TODO Implement removeAttribute().
    }

    public void testGetAttributeNode() {
        //TODO Implement getAttributeNode().
    }

    public void testSetAttributeNode() {
        //TODO Implement setAttributeNode().
    }

    public void testRemoveAttributeNode() {
        //TODO Implement removeAttributeNode().
    }

    public void testGetElementsByTagName() {
        //TODO Implement getElementsByTagName().
    }

    public void testGetAttributeNS() {
        //TODO Implement getAttributeNS().
    }

    public void testSetAttributeNS() {
        //TODO Implement setAttributeNS().
    }

    public void testRemoveAttributeNS() {
        //TODO Implement removeAttributeNS().
    }

    public void testGetAttributeNodeNS() {
        //TODO Implement getAttributeNodeNS().
    }

    public void testSetAttributeNodeNS() {
        //TODO Implement setAttributeNodeNS().
    }

    public void testGetElementsByTagNameNS() {
        //TODO Implement getElementsByTagNameNS().
    }

    public void testHasAttribute() {
        //TODO Implement hasAttribute().
    }

    public void testHasAttributeNS() {
        //TODO Implement hasAttributeNS().
    }

    public void testGetParent() {
        //TODO Implement getParent().
    }

    public void testSetParent() {
        //TODO Implement setParent().
    }

}
