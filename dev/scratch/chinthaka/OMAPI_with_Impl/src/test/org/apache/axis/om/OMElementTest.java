<<<<<<< .mine
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
 * Date: Oct 14, 2004
 * Time: 12:42:46 PM
 */
package org.apache.axis.om;

import junit.framework.TestCase;
import org.apache.axis.om.impl.OMXmlPullParserWrapper;
import org.apache.axis.om.util.OMNodeBuilder;
import org.apache.xml.utils.QName;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.FileReader;
import java.util.Iterator;


public class OMElementTest extends TestCase {
    private static final String IN_FILE_NAME = "resources/soapmessage.xml";
    private OMElement root;
    private OMXmlPullParserWrapper omXmlPullParserWrapper;


    public static void main(String[] args) {
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        root = getOMBuilder().getDocument().getRootElement();

    }

    private OMXmlPullParserWrapper getOMBuilder() throws Exception {
        XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
        parser.setInput(new FileReader(IN_FILE_NAME));
        omXmlPullParserWrapper = new OMXmlPullParserWrapper(parser);
        return omXmlPullParserWrapper;
    }

    public final void testAddChild() {

        System.out.print("Testing addChild(OMNode) .....");

        // add new child with the name TestElement
        OMNamespace omNamespace = OMNodeBuilder.createOMNamespace("http://opensource.lk/chinthaka", "prefix");
        root.addChild(OMNodeBuilder.createOMElement("TestElement", omNamespace, root, omXmlPullParserWrapper));

        // search for the child
        boolean newChildFound = false;
        Iterator iterator = root.getChildren();
        while (iterator.hasNext()) {
            OMNode omNode = (OMNode) iterator.next();
            if (omNode.getValue().equalsIgnoreCase("TestElement")) {
                newChildFound = true;
            }
        }
        assertTrue(newChildFound);

        System.out.println(" \t\t OK");
    }


    public final void testGetChildrenWithName() {
        System.out.print("Testing getChildrenWithQName(QName) ......");

        // add new child with the name TestElement
        OMNamespace omNamespace = OMNodeBuilder.createOMNamespace("http://opensource.lk/chinthaka", "prefix");
        root.addChild(OMNodeBuilder.createOMElement("TestElement", omNamespace, root, omXmlPullParserWrapper));

        Iterator iter = root.getChildrenWithName(new QName("http://opensource.lk/chinthaka", "TestElement"));
        int childrenCount = 0;
        while (iter.hasNext()) {
            iter.next();
            childrenCount++;
        }

        assertTrue("getChildrenWithName is not working properly", childrenCount == 1);

        System.out.println(" \t\t OK");


    }

    public final void testGetChildren() {
        System.out.print("Testing getChildren ..............");
        Iterator childrenIterator = root.getChildren();

        int totalNumberOfChildren = 0;
        int elementNodes = 0;
        int textNodes = 0;
        OMNode omNode;
        short nodeType;
        String elementLocalName;

        while (childrenIterator.hasNext()) {
            omNode = (OMNode) childrenIterator.next();
            nodeType = omNode.getType();
            if (nodeType == OMNode.TEXT_NODE) {
                textNodes++;
            } else if (nodeType == OMNode.ELEMENT_NODE) {
                elementLocalName = omNode.getValue();
                assertTrue("No Header or Body element exists .......", (elementLocalName.equalsIgnoreCase("Header") || elementLocalName.equalsIgnoreCase("Body")));
                elementNodes++;
            }
            totalNumberOfChildren++;
        }

        assertEquals("Two Elements should exist", 2, elementNodes);
        assertEquals("Three Text nodes should exist", 3, textNodes);
        assertEquals("Five children should exist", 5, totalNumberOfChildren);

        System.out.println(" \t\t OK");

    }

    public final void testNamespaceOperations() {
        String uri = "http://opensource.lk/chinthaka";
        String prefix = "prefix";

        System.out.print("Testing create namespace .......");
        root.createNamespace(uri, prefix);
        OMNamespace namespace = root.resolveNamespace(uri, prefix);
        assertTrue("namespace has not been set properly ", namespace.equals(uri, prefix));
        System.out.println(" \t\t OK");

        System.out.print("Testing resolve namespace .......");
        OMNamespace dummyNamespace = root.resolveNamespace("dummy uri", " dummy prefix");
        assertTrue(dummyNamespace == null);
        System.out.println(" \t\t OK");

        System.out.print("Testing get Namespace ...");
        namespace = root.getNamespace();
        assertTrue(namespace.equals("http://schemas.xmlsoap.org/soap/envelope/", "soapenv"));
        System.out.println(" \t\t\t\t OK");

        System.out.print("Testing set namespace .......");
        namespace = OMNodeBuilder.createOMNamespace(uri, prefix);
        root.setNamespace(namespace);
        assertTrue(root.getNamespace().equals(namespace));
        System.out.println(" \t\t\t OK");

    }


    public final void testGetAttributeWithQName() {
        System.out.print("Testing getAttributeWithQName ...");


        Iterator toElementIter = getToElementIterator();
        if (toElementIter == null) {
            assertFalse("No To element found in the header element", true);
        } else {
            if (toElementIter.hasNext()) {
                OMElement toElement = (OMElement) toElementIter.next();
                Iterator attrubuteIter = toElement.getAttributeWithQName(new QName("http://schemas.xmlsoap.org/soap/envelope/", "mustUnderstand"));
                if (attrubuteIter.hasNext()) {
                    OMAttribute omAttribute = (OMAttribute) attrubuteIter.next();
                    assertTrue(omAttribute.getLocalName().equalsIgnoreCase("mustUnderstand") && omAttribute.getValue().equals("0"));
                }

            }
        }

        System.out.println(" \t\t OK");

    }

    private Iterator getToElementIterator() {
        Iterator headerElementIter = root.getChildrenWithName(new QName("http://schemas.xmlsoap.org/soap/envelope/", "Header"));
        if (headerElementIter.hasNext()) {
            OMElement omElement = (OMElement) headerElementIter.next();
            return omElement.getChildrenWithName(new QName("http://schemas.xmlsoap.org/ws/2004/03/addressing", "To"));
        }

        return null;
    }

    public final void testGetAttributes() {
        System.out.print("Testing getAttributes ...");

        Iterator toElementIter = getToElementIterator();
        if (toElementIter == null) {
            assertFalse("No To element found in the header element", true);
        } else {
            if (toElementIter.hasNext()) {
                OMElement toElement = (OMElement) toElementIter.next();
                Iterator toElementAttributeIter = toElement.getAttributes();

                int attribCount = 0;
                while (toElementAttributeIter.hasNext()) {
                    OMAttribute omAttribute = (OMAttribute) toElementAttributeIter.next();
                    assertTrue("To element having attributes, other than mustUnderstand", omAttribute.getLocalName().equalsIgnoreCase("mustUnderstand"));
                    attribCount++;
                }

                assertTrue("To element having more than one attribute", attribCount == 1);
            }
        }

        System.out.println(" \t\t OK");

    }

    public final void testInsertAttribute() {
        System.out.print("Testing insertAttribute ....");

        Iterator toElementIter = getToElementIterator();
        if (toElementIter == null) {
            assertFalse("No To element found in the header element", true);
        } else {
            if (toElementIter.hasNext()) {
                OMElement toElement = (OMElement) toElementIter.next();
                toElement.insertAttribute(OMNodeBuilder.createOMAttribute("AttributeOne", null, "AttributeValueOne", toElement));
                toElement.insertAttribute(OMNodeBuilder.createOMAttribute("AttributeTwo", null, "AttributeValueTwo", toElement));

                Iterator toElementAttributeIter = toElement.getAttributes();
                boolean attribOneFound = false;
                boolean attribTwoFound = false;

                int attribCount = 0;
                while (toElementAttributeIter.hasNext()) {
                    OMAttribute omAttribute = (OMAttribute) toElementAttributeIter.next();
                    if (omAttribute.getLocalName().equalsIgnoreCase("AttributeOne")) {
                        attribOneFound = true;
                    } else if (omAttribute.getLocalName().equalsIgnoreCase("AttributeTwo")) {
                        attribTwoFound = true;
                    }
                    attribCount++;

                }

                assertTrue("insertAttribute is not working properly", attribCount == 3);
                assertTrue("Attribute insertion has not worked properly", attribOneFound && attribTwoFound);

            }
        }

        System.out.println(" \t\t OK");
    }

    public final void testRemoveAttribute() {
        System.out.print("Testing insertAttribute ....");

        Iterator toElementIter = getToElementIterator();
        if (toElementIter == null) {
            assertFalse("No To element found in the header element", true);
        } else {
            if (toElementIter.hasNext()) {
                OMElement toElement = (OMElement) toElementIter.next();
                OMAttribute omAttribute = OMNodeBuilder.createOMAttribute("AttributeOne", null, "AttributeValueOne", toElement);
                toElement.insertAttribute(omAttribute);

                Iterator toElementAttributeIter = toElement.getAttributes();

                int attribCount = 0;
                while (toElementAttributeIter.hasNext()) {
                    toElementAttributeIter.next();
                    attribCount++;
                }
                assertTrue("Attribute addition has not been done properly", attribCount == 2);

                toElement.removeAttribute(omAttribute);

                toElementAttributeIter = toElement.getAttributes();
                attribCount = 0;
                while (toElementAttributeIter.hasNext()) {
                    toElementAttributeIter.next();
                    attribCount++;
                }
                assertTrue("Attribute removal has not been done properly", attribCount == 1);

            }
        }
        System.out.println(" \t\t OK");

    }

    public final void testGetLocalName() {
        System.out.print("Testing getLocalName ...");
        assertEquals("getLocalName not working properly ..", "Envelope", root.getLocalName());
        System.out.println(" \t\t OK");
    }

    public final void testSetLocalName() {
        System.out.print("Testing setLocalName ...");
        root.setLocalName("NewLocalName");
        assertEquals("setLocalName not working properly ..", "NewLocalName", root.getLocalName());
        System.out.println(" \t\t OK");
    }


    public final void testGetParent() {
        System.out.print("Testing getParent ...");

        Iterator rootChildrenIter = root.getChildren();
        while (rootChildrenIter.hasNext()) {
            OMNode omNode = (OMNode) rootChildrenIter.next();
            assertTrue("getParent has some problems", omNode.getParent() == root);
        }
        System.out.println(" \t\t OK");

    }


    public final void testGetNextSibling() {
        System.out.print("Testing getNextSibling ...");

        Iterator rootChildrenIter = root.getChildren();
        OMNode previousSibling = null;
        while (rootChildrenIter.hasNext()) {
            OMNode omNode = (OMNode) rootChildrenIter.next();
            if (previousSibling != null) {
                assertTrue("getNextSibling is not working properly ... ", previousSibling.getNextSibling() == omNode);
            }
        }

        System.out.println(" \t\t OK");

    }

    public final void testSetNextSibling() {
        System.out.print("Testing getNextSibling ...");

        Iterator rootChildrenIter = root.getChildren();

        OMNode firstChild = getNextChild(rootChildrenIter);
        OMNode secondChild = getNextChild(rootChildrenIter);
        OMNode thirdChild = getNextChild(rootChildrenIter);

        firstChild.setNextSibling(thirdChild);

        assertTrue(firstChild.getNextSibling() == thirdChild);
        System.out.println(" \t\t OK");

    }

    private OMNode getNextChild(Iterator rootChildrenIter) {
        if (rootChildrenIter.hasNext()) {
            return (OMNode) rootChildrenIter.next();
        }

        return null;
    }

    public final void testGetValue() {
        System.out.print("Testing getValue ...");
        assertEquals("Local Name is not equal to value of Element", root.getValue(), root.getLocalName());
        System.out.println(" \t\t OK");
    }

    public final void testIsComplete() {
        System.out.print("Testing isComplete ...");

        Iterator rootChildrenIter = root.getChildren();

        OMNode firstChild = getNextChild(rootChildrenIter);
        getNextChild(rootChildrenIter);

        assertTrue("Complete has not been set properly", firstChild.isComplete());
        System.out.println(" \t\t OK");

    }

    public final void testDetach() {
        System.out.println("Testing detach ....");
        Iterator childrenIterator = root.getChildren();
        while (childrenIterator.hasNext()) {
            OMNode omNode = (OMNode) childrenIterator.next();
            if (omNode.getType() == OMNode.TEXT_NODE) {
                omNode.detach();
            }
        }

        childrenIterator = root.getChildren();
        while (childrenIterator.hasNext()) {
            OMNode omNode = (OMNode) childrenIterator.next();
            if (omNode.getType() == OMNode.TEXT_NODE) {
                assertTrue("Detaching is not working", false);
            }
        }

        System.out.println(" \t\t OK");

    }

    public final void testInsertSiblingAfter() {
        System.out.print("Testing insertSiblingAfter ...");

        Iterator rootChildrenIter = root.getChildren();

        OMNode firstChild = getNextChild(rootChildrenIter);
        OMNode secondChild = getNextChild(rootChildrenIter);
        OMNode thirdChild = getNextChild(rootChildrenIter);

        firstChild.insertSiblingAfter(thirdChild);

        assertTrue("insertSiblingAfter is not woking properly", firstChild.getNextSibling() == thirdChild);
        System.out.println(" \t\t OK");

    }

    public final void testInsertSiblingBefore() {
        System.out.print("Testing insertSiblingBefore  ...");

        Iterator rootChildrenIter = root.getChildren();

        OMNode firstChild = getNextChild(rootChildrenIter);
        OMNode secondChild = getNextChild(rootChildrenIter);
        OMNode thirdChild = getNextChild(rootChildrenIter);

        firstChild.insertSiblingBefore(thirdChild);

        assertTrue("insertSiblingBefore is not woking properly", thirdChild.getNextSibling() == firstChild);
        System.out.println(" \t\t OK");
    }

    public final void testGetType() {
        assertTrue("Element type is not set properly ", root.getType() == OMNode.ELEMENT_NODE);
    }

    
}
=======
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
 * Date: Oct 14, 2004
 * Time: 12:42:46 PM
 */
package org.apache.axis.om;

import junit.framework.TestCase;
import org.apache.axis.om.impl.OMXmlPullParserWrapper;
import org.apache.axis.om.util.OMNodeBuilder;
import org.apache.xml.utils.QName;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.FileReader;
import java.util.Iterator;


public class OMElementTest extends TestCase {
    private static final String IN_FILE_NAME = "resources/soapmessage.xml";
    private OMElement root;
    private OMXmlPullParserWrapper omXmlPullParserWrapper;


    public static void main(String[] args) {
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        root = getOMBuilder().getDocument().getRootElement();

    }

    private OMXmlPullParserWrapper getOMBuilder() throws Exception {
        XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
        parser.setInput(new FileReader(IN_FILE_NAME));
        omXmlPullParserWrapper = new OMXmlPullParserWrapper(parser);
        return omXmlPullParserWrapper;
    }

    public final void testAddChild() {

        System.out.print("Testing addChild(OMNode) .....");

        // add new child with the name TestElement
        OMNamespace omNamespace = OMNodeBuilder.createOMNamespace("http://opensource.lk/chinthaka", "prefix");
        root.addChild(OMNodeBuilder.createOMElement("TestElement", omNamespace, root, omXmlPullParserWrapper));

        // search for the child
        boolean newChildFound = false;
        Iterator iterator = root.getChildren();
        while (iterator.hasNext()) {
            OMNode omNode = (OMNode) iterator.next();
            if (omNode.getValue().equalsIgnoreCase("TestElement")) {
                newChildFound = true;
            }
        }
        assertTrue(newChildFound);

        System.out.println(" \t\t OK");
    }


    public final void testGetChildrenWithName() {
        System.out.print("Testing getChildrenWithQName(QName) ......");

        // add new child with the name TestElement
        OMNamespace omNamespace = OMNodeBuilder.createOMNamespace("http://opensource.lk/chinthaka", "prefix");
        root.addChild(OMNodeBuilder.createOMElement("TestElement", omNamespace, root, omXmlPullParserWrapper));

        Iterator iter = root.getChildrenWithName(new QName("http://opensource.lk/chinthaka", "TestElement"));
        int childrenCount = 0;
        while (iter.hasNext()) {
            iter.next();
            childrenCount++;
        }

        assertTrue("getChildrenWithName is not working properly", childrenCount == 1);

        System.out.println(" \t\t OK");


    }

    public final void testGetChildren() {
        System.out.print("Testing getChildren ..............");
        Iterator childrenIterator = root.getChildren();

        int totalNumberOfChildren = 0;
        int elementNodes = 0;
        int textNodes = 0;
        OMNode omNode;
        short nodeType;
        String elementLocalName;

        while (childrenIterator.hasNext()) {
            omNode = (OMNode) childrenIterator.next();
            nodeType = omNode.getType();
            if (nodeType == OMNode.TEXT_NODE) {
                textNodes++;
            } else if (nodeType == OMNode.ELEMENT_NODE) {
                elementLocalName = omNode.getValue();
                assertTrue("No Header or Body element exists .......", (elementLocalName.equalsIgnoreCase("Header") || elementLocalName.equalsIgnoreCase("Body")));
                elementNodes++;
            }
            totalNumberOfChildren++;
        }

        assertEquals("Two Elements should exist", 2, elementNodes);
        assertEquals("Three Text nodes should exist", 3, textNodes);
        assertEquals("Five children should exist", 5, totalNumberOfChildren);

        System.out.println(" \t\t OK");

    }

    public final void testNamespaceOperations() {
        String uri = "http://opensource.lk/chinthaka";
        String prefix = "prefix";

        System.out.print("Testing create namespace .......");
        root.createNamespace(uri, prefix);
        OMNamespace namespace = root.resolveNamespace(uri, prefix);
        assertTrue("namespace has not been set properly ", namespace.equals(uri, prefix));
        System.out.println(" \t\t OK");

        System.out.print("Testing resolve namespace .......");
        OMNamespace dummyNamespace = root.resolveNamespace("dummy uri", " dummy prefix");
        assertTrue(dummyNamespace == null);
        System.out.println(" \t\t OK");

        System.out.print("Testing get Namespace ...");
        namespace = root.getNamespace();
        assertTrue(namespace.equals("http://schemas.xmlsoap.org/soap/envelope/", "soapenv"));
        System.out.println(" \t\t\t\t OK");

        System.out.print("Testing set namespace .......");
        namespace = OMNodeBuilder.createOMNamespace(uri, prefix);
        root.setNamespace(namespace);
        assertTrue(root.getNamespace().equals(namespace));
        System.out.println(" \t\t\t OK");

    }


    public final void testGetAttributeWithQName() {
        System.out.print("Testing getAttributeWithQName ...");


        Iterator toElementIter = getToElementIterator();
        if (toElementIter == null) {
            assertFalse("No To element found in the header element", true);
        } else {
            if (toElementIter.hasNext()) {
                OMElement toElement = (OMElement) toElementIter.next();
                Iterator attrubuteIter = toElement.getAttributeWithQName(new QName("http://schemas.xmlsoap.org/soap/envelope/", "mustUnderstand"));
                if (attrubuteIter.hasNext()) {
                    OMAttribute omAttribute = (OMAttribute) attrubuteIter.next();
                    assertTrue(omAttribute.getLocalName().equalsIgnoreCase("mustUnderstand") && omAttribute.getValue().equals("0"));
                }

            }
        }

        System.out.println(" \t\t OK");

    }

    private Iterator getToElementIterator() {
        Iterator headerElementIter = root.getChildrenWithName(new QName("http://schemas.xmlsoap.org/soap/envelope/", "Header"));
        if (headerElementIter.hasNext()) {
            OMElement omElement = (OMElement) headerElementIter.next();
            return omElement.getChildrenWithName(new QName("http://schemas.xmlsoap.org/ws/2004/03/addressing", "To"));
        }

        return null;
    }

    public final void testGetAttributes() {
        System.out.print("Testing getAttributes ...");

        Iterator toElementIter = getToElementIterator();
        if (toElementIter == null) {
            assertFalse("No To element found in the header element", true);
        } else {
            if (toElementIter.hasNext()) {
                OMElement toElement = (OMElement) toElementIter.next();
                Iterator toElementAttributeIter = toElement.getAttributes();

                int attribCount = 0;
                while (toElementAttributeIter.hasNext()) {
                    OMAttribute omAttribute = (OMAttribute) toElementAttributeIter.next();
                    assertTrue("To element having attributes, other than mustUnderstand", omAttribute.getLocalName().equalsIgnoreCase("mustUnderstand"));
                    attribCount++;
                }

                assertTrue("To element having more than one attribute", attribCount == 1);
            }
        }

        System.out.println(" \t\t OK");

    }

    public final void testInsertAttribute() {
        System.out.print("Testing insertAttribute ....");

        Iterator toElementIter = getToElementIterator();
        if (toElementIter == null) {
            assertFalse("No To element found in the header element", true);
        } else {
            if (toElementIter.hasNext()) {
                OMElement toElement = (OMElement) toElementIter.next();
                toElement.insertAttribute(OMNodeBuilder.createOMAttribute("AttributeOne", null, "AttributeValueOne", toElement));
                toElement.insertAttribute(OMNodeBuilder.createOMAttribute("AttributeTwo", null, "AttributeValueTwo", toElement));

                Iterator toElementAttributeIter = toElement.getAttributes();
                boolean attribOneFound = false;
                boolean attribTwoFound = false;

                int attribCount = 0;
                while (toElementAttributeIter.hasNext()) {
                    OMAttribute omAttribute = (OMAttribute) toElementAttributeIter.next();
                    if (omAttribute.getLocalName().equalsIgnoreCase("AttributeOne")) {
                        attribOneFound = true;
                    } else if (omAttribute.getLocalName().equalsIgnoreCase("AttributeTwo")) {
                        attribTwoFound = true;
                    }
                    attribCount++;

                }

                assertTrue("insertAttribute is not working properly", attribCount == 3);
                assertTrue("Attribute insertion has not worked properly", attribOneFound && attribTwoFound);

            }
        }

        System.out.println(" \t\t OK");
    }

    public final void testRemoveAttribute() {
        System.out.print("Testing insertAttribute ....");

        Iterator toElementIter = getToElementIterator();
        if (toElementIter == null) {
            assertFalse("No To element found in the header element", true);
        } else {
            if (toElementIter.hasNext()) {
                OMElement toElement = (OMElement) toElementIter.next();
                OMAttribute omAttribute = OMNodeBuilder.createOMAttribute("AttributeOne", null, "AttributeValueOne", toElement);
                toElement.insertAttribute(omAttribute);

                Iterator toElementAttributeIter = toElement.getAttributes();

                int attribCount = 0;
                while (toElementAttributeIter.hasNext()) {
                    toElementAttributeIter.next();
                    attribCount++;
                }
                assertTrue("Attribute addition has not been done properly", attribCount == 2);

                toElement.removeAttribute(omAttribute);

                toElementAttributeIter = toElement.getAttributes();
                attribCount = 0;
                while (toElementAttributeIter.hasNext()) {
                    toElementAttributeIter.next();
                    attribCount++;
                }
                assertTrue("Attribute removal has not been done properly", attribCount == 1);

            }
        }
        System.out.println(" \t\t OK");

    }

    public final void testGetLocalName() {
        System.out.print("Testing getLocalName ...");
        assertEquals("getLocalName not working properly ..", "Envelope", root.getLocalName());
        System.out.println(" \t\t OK");
    }

    public final void testSetLocalName() {
        System.out.print("Testing setLocalName ...");
        root.setLocalName("NewLocalName");
        assertEquals("setLocalName not working properly ..", "NewLocalName", root.getLocalName());
        System.out.println(" \t\t OK");
    }


    public final void testGetParent() {
        System.out.print("Testing getParent ...");

        Iterator rootChildrenIter = root.getChildren();
        while (rootChildrenIter.hasNext()) {
            OMNode omNode = (OMNode) rootChildrenIter.next();
            assertTrue("getParent has some problems", omNode.getParent() == root);
        }
        System.out.println(" \t\t OK");

    }


    public final void testGetNextSibling() {
        System.out.print("Testing getNextSibling ...");

        Iterator rootChildrenIter = root.getChildren();
        OMNode previousSibling = null;
        while (rootChildrenIter.hasNext()) {
            OMNode omNode = (OMNode) rootChildrenIter.next();
            if (previousSibling != null) {
                assertTrue("getNextSibling is not working properly ... ", previousSibling.getNextSibling() == omNode);
            }
        }

        System.out.println(" \t\t OK");

    }

    public final void testSetNextSibling() {
        System.out.print("Testing getNextSibling ...");

        Iterator rootChildrenIter = root.getChildren();

        OMNode firstChild = getNextChild(rootChildrenIter);
        OMNode secondChild = getNextChild(rootChildrenIter);
        OMNode thirdChild = getNextChild(rootChildrenIter);

        firstChild.setNextSibling(thirdChild);

        assertTrue(firstChild.getNextSibling() == thirdChild);
        System.out.println(" \t\t OK");

    }

    private OMNode getNextChild(Iterator rootChildrenIter) {
        if (rootChildrenIter.hasNext()) {
            return (OMNode) rootChildrenIter.next();
        }

        return null;
    }

    public final void testGetValue() {
        System.out.print("Testing getValue ...");
        assertEquals("Local Name is not equal to value of Element", root.getValue(), root.getLocalName());
        System.out.println(" \t\t OK");
    }

    public final void testIsComplete() {
        System.out.print("Testing isComplete ...");

        Iterator rootChildrenIter = root.getChildren();

        OMNode firstChild = getNextChild(rootChildrenIter);
        getNextChild(rootChildrenIter);

        assertTrue("Complete has not been set properly", firstChild.isComplete());
        System.out.println(" \t\t OK");

    }

    public final void testDetach() {
        System.out.println("Testing detach ....");
        Iterator childrenIterator = root.getChildren();
        while (childrenIterator.hasNext()) {
            OMNode omNode = (OMNode) childrenIterator.next();
            if (omNode.getType() == OMNode.TEXT_NODE) {
                omNode.detach();
            }
        }

        childrenIterator = root.getChildren();
        while (childrenIterator.hasNext()) {
            OMNode omNode = (OMNode) childrenIterator.next();
            if (omNode.getType() == OMNode.TEXT_NODE) {
                assertTrue("Detaching is not working", false);
            }
        }

        System.out.println(" \t\t OK");

    }

    public final void testInsertSiblingAfter() {
        System.out.print("Testing insertSiblingAfter ...");

        Iterator rootChildrenIter = root.getChildren();

        OMNode firstChild = getNextChild(rootChildrenIter);
        OMNode secondChild = getNextChild(rootChildrenIter);
        OMNode thirdChild = getNextChild(rootChildrenIter);

        firstChild.insertSiblingAfter(thirdChild);

        assertTrue("insertSiblingAfter is not woking properly", firstChild.getNextSibling() == thirdChild);
        System.out.println(" \t\t OK");

    }

    public final void testInsertSiblingBefore() {
        System.out.print("Testing insertSiblingBefore  ...");

        Iterator rootChildrenIter = root.getChildren();

        OMNode firstChild = getNextChild(rootChildrenIter);
        OMNode secondChild = getNextChild(rootChildrenIter);
        OMNode thirdChild = getNextChild(rootChildrenIter);

        firstChild.insertSiblingBefore(thirdChild);

        assertTrue("insertSiblingBefore is not woking properly", thirdChild.getNextSibling() == firstChild);
        System.out.println(" \t\t OK");
    }

    public final void testGetType() {
        assertTrue("Element type is not set properly ", root.getType() == OMNode.ELEMENT_NODE);
    }

    
}
>>>>>>> .r55205
