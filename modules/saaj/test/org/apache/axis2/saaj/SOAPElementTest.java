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
import java.util.List;

/**
 * @author Ashutosh Shahi ashutosh.shahi@gmail.com
 */
public class SOAPElementTest extends TestCase {

    private SOAPElement soapElem;

    protected void setUp() throws Exception {
        soapElem =
                SOAPFactoryImpl.newInstance().createElement("Test",
                        "test",
                        "http://test.apache.org/");
    }

    public void testAddTextNode() throws Exception {
        assertNotNull(soapElem);
        final String value = "foo";
        soapElem.addTextNode(value);
        assertEquals(value, soapElem.getValue());
        TextImpl text = assertContainsText(soapElem);
        assertEquals(value, text.getValue());
    }

    private TextImpl assertContainsText(SOAPElement soapElem) {
        assertTrue(soapElem.hasChildNodes());
        List childElems = toList(soapElem.getChildElements());
        assertTrue(childElems.size() == 1);
        NodeImpl node = (NodeImpl) childElems.get(0);
        assertTrue(node instanceof TextImpl);
        return (TextImpl) node;
    }

    private List toList(java.util.Iterator iter) {
        List list = new java.util.ArrayList();
        while (iter.hasNext()) {
            list.add(iter.next());
        }
        return list;
    }
}
