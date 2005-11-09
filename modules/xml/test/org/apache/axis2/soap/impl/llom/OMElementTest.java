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

package org.apache.axis2.soap.impl.llom;

import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMConstants;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMFactory;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.om.OMTestCase;
import org.apache.axis2.om.OMText;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.impl.llom.builder.StAXSOAPModelBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import java.util.Iterator;

public class OMElementTest extends OMTestCase implements OMConstants {
    private static final String WSA_URI = "http://schemas.xmlsoap.org/ws/2004/03/addressing";
    private static final String WSA_TO = "To";
    private Log log = LogFactory.getLog(getClass());

    OMFactory factory = OMAbstractFactory.getOMFactory();
    private OMElement firstElement;
    private OMElement secondElement;


    public OMElementTest(String testName) {
        super(testName);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
       OMNamespace testingNamespace = factory.createOMNamespace(
                        "http://testing.axis2.org", "axis2");
        firstElement = factory.createOMElement("FirstElement", testingNamespace);
        secondElement = factory.createOMElement("SecondElement", factory.createOMNamespace(
                                "http://moretesting.axis2.org", "axis2"), firstElement);
    }

    public void testGetText() {
        try {
            StAXSOAPModelBuilder soapBuilder = getOMBuilder(
                    "soap/OMElementTest.xml");
            SOAPEnvelope soapEnvelope = (SOAPEnvelope) soapBuilder.getDocumentElement();
            OMElement wsaTo = soapEnvelope.getHeader().getFirstChildWithName(
                    new QName(WSA_URI, WSA_TO));

            String expectedString = "http://localhost:8081/axis/services/BankPort";
            assertEquals("getText is not returning the correct value",
                    wsaTo.getText().trim(),
                    expectedString);
        } catch (Exception e) {
            log.info(e.getMessage());
        }
    }

    public void testConstructors(){

        try {
            OMElement elementWithNoLocalName = factory.createOMElement("", null);
            fail("This should fail as OMElement should not be allowed to create without a local name ");
        } catch (Exception e) {
            assertTrue(true);
        }

        assertEquals("Namespace having same information, declared in the same context, should share" +
                " the same namespace object",firstElement.getNamespace(), secondElement.getNamespace());
        assertEquals("OMElement children addition has not worked properly", secondElement, firstElement.getFirstElement());

        OMNamespace testNamespace2 = factory.createOMNamespace("ftp://anotherTest.axis2.org", "axis2");
        firstElement.declareNamespace(testNamespace2);

        OMNamespace inheritedSecondNamespace = secondElement.findNamespace(testNamespace2.getName(),
                testNamespace2.getPrefix());
        assertNotNull("Children should inherit namespaces declared in parent", inheritedSecondNamespace);
        assertEquals("inherited namespace uri should be equal", inheritedSecondNamespace.getName(), testNamespace2.getName());
        assertEquals("inherited namespace prefix should be equal", inheritedSecondNamespace.getPrefix(), testNamespace2.getPrefix());


    }

    public void testChildDetachment() {
        OMNamespace testNamespace2 = factory.createOMNamespace("ftp://anotherTest.axis2.org", "axis2");
        
        secondElement.detach();
        assertTrue("OMElement children detachment has not worked properly", !secondElement.equals(firstElement.getFirstElement()));
        assertNull("First Element should not contain elements after detaching. ", firstElement.getFirstElement());
        assertNull("First Element should not contain elements after detaching. ", firstElement.getFirstOMChild());
        assertNull(secondElement.findNamespace(testNamespace2.getName(), testNamespace2.getPrefix()));

        firstElement.addChild(secondElement);
        firstElement.setText("Some Sample Text");

        assertTrue("First added child must be the first child", secondElement.equals(firstElement.getFirstOMChild()));
        Iterator children = firstElement.getChildren();
        int childCount = 0;
        while (children.hasNext()) {
            Object o = children.next();
            childCount++;
        }
        assertEquals("Children count should be two", childCount, 2);

        secondElement.detach();
        assertTrue("First child should be the text child", firstElement.getFirstOMChild() instanceof OMText);


    }

}
