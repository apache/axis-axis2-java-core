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
    }


    public final void testGetChildrenWithName() {
        System.out.println("Testing getChildrenWithQName(QName) ......");

       // add new child with the name TestElement
        OMNamespace omNamespace = OMNodeBuilder.createOMNamespace("http://opensource.lk/chinthaka", "prefix");
        root.addChild(OMNodeBuilder.createOMElement("TestElement", omNamespace, root, omXmlPullParserWrapper));

        Iterator iter = root.getChildrenWithName(new QName("http://opensource.lk/chinthaka", "TestElement"));
        int childrenCount = 0;
        while (iter.hasNext()) {
            iter.next();
            childrenCount ++;
        }

        assertTrue("getChildrenWithName is not working properly", childrenCount == 1);

    }

    public final void testGetChildren() {
        Iterator childrenIterator = root.getChildren();

        while (childrenIterator.hasNext()) {
            OMNode omNode = (OMNode) childrenIterator.next();

        }
    }

    public final void testCreateNamespace() {
        //TODO Implement createNamespace().
    }

    public final void testResolveNamespace() {
        //TODO Implement resolveNamespace().
    }

    public final void testGetAttributeWithQName() {
        //TODO Implement getAttributeWithQName().
    }

    public final void testGetAttributes() {
        //TODO Implement getAttributes().
    }

    public final void testInsertAttribute() {
        //TODO Implement insertAttribute().
    }

    public final void testRemoveAttribute() {
        //TODO Implement removeAttribute().
    }

    public final void testGetLocalName() {
        //TODO Implement getLocalName().
    }

    public final void testSetLocalName() {
        //TODO Implement setLocalName().
    }

    public final void testGetNamespace() {
        //TODO Implement getNamespace().
    }

    public final void testSetNamespace() {
        //TODO Implement setNamespace().
    }

    public final void testGetParent() {
        //TODO Implement getParent().
    }

    public final void testSetParent() {
        //TODO Implement setParent().
    }

    public final void testGetNextSibling() {
        //TODO Implement getNextSibling().
    }

    public final void testSetNextSibling() {
        //TODO Implement setNextSibling().
    }

    public final void testGetValue() {
        //TODO Implement getValue().
    }

    public final void testSetValue() {
        //TODO Implement setValue().
    }

    public final void testIsComplete() {
        //TODO Implement isComplete().
    }

    public final void testSetComplete() {
        //TODO Implement setComplete().
    }

    public final void testDetach() {
        //TODO Implement detach().
    }

    public final void testInsertSiblingAfter() {
        //TODO Implement insertSiblingAfter().
    }

    public final void testInsertSiblingBefore() {
        //TODO Implement insertSiblingBefore().
    }

    public final void testGetType() {
        //TODO Implement getType().
    }

    public final void testSetType() {
        //TODO Implement setType().
    }

}
