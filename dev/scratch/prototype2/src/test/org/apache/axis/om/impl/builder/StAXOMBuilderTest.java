package org.apache.axis.om.impl.builder;

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
 * User: Eran Chinthaka - Lanka Software Foundation
 * Date: Dec 7, 2004
 * Time: 2:29:07 PM
 */

import org.apache.axis.AbstractTestCase;
import org.apache.axis.impl.llom.factory.OMXMLBuilderFactory;
import org.apache.axis.impl.llom.builder.StAXOMBuilder;
import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMFactory;
import org.apache.axis.om.OMNode;
import org.apache.axis.om.OMText;

import javax.xml.stream.XMLInputFactory;
import java.io.FileReader;
import java.util.Iterator;

public class StAXOMBuilderTest extends AbstractTestCase {
    StAXOMBuilder stAXOMBuilder;
    FileReader testFile;
    private OMElement rootElement;

    /**
     * Constructor.
     */
    public StAXOMBuilderTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        testFile = new FileReader(getTestResourceFile("non_soap.xml"));
        stAXOMBuilder = OMXMLBuilderFactory.createStAXOMBuilder(OMFactory.newInstance(), XMLInputFactory.newInstance().createXMLStreamReader(testFile));
    }

    public void testGetRootElement() throws Exception {
        rootElement = stAXOMBuilder.getDocumentElement();

        System.out.println("Checking nullity of Root Element");
        assertTrue("Root element can not be null", rootElement != null);

        System.out.println("Checking for correct root element name");
        assertTrue(" Name of the root element is wrong", rootElement.getLocalName().equalsIgnoreCase("Root"));

        System.out.println("Checking number of children");
        // get the first OMElement child
        OMNode omnode = rootElement.getFirstChild();
        while(omnode instanceof OMText){
            omnode = omnode.getNextSibling();
        }

        Iterator children = ((OMElement)omnode).getChildren();
        int childrenCount = 0;
        while (children.hasNext()) {
            OMNode node = (OMNode) children.next();
            if(node instanceof OMElement)
                childrenCount++;
        }
        assertTrue(childrenCount == 5);
    }
}