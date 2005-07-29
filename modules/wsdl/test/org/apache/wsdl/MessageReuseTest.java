/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

package org.apache.wsdl;

import java.util.Iterator;

import javax.wsdl.Definition;
import javax.xml.namespace.QName;

import org.apache.axis2.wsdl.WSDLVersionWrapper;
import org.apache.axis2.wsdl.builder.WOMBuilderFactory;
import org.apache.wsdl.extensions.ExtensionConstants;
import org.apache.wsdl.extensions.Schema;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author chathura@opensource.lk
 */
public class MessageReuseTest extends AbstractTestCase {

    private WSDLDescription womDescription;

    private Definition wsdl4jDefinition;

    public MessageReuseTest(String arg) {
        super(arg);
    }

    protected void setUp() throws Exception {

        WSDLVersionWrapper wsdlVersionWrapper = null;
        if (null == this.womDescription) {
            String path = getTestResourceFile("BookQuote.wsdl").getAbsolutePath();
            wsdlVersionWrapper =
                    WOMBuilderFactory.getBuilder(WOMBuilderFactory.WSDL11)
                    .build(path);
            this.womDescription = wsdlVersionWrapper.getDescription();
        }
        if (null == wsdl4jDefinition) {
            this.wsdl4jDefinition = wsdlVersionWrapper.getDefinition();
        }

    }

    public void testMultipartmessageReuse() throws Exception {

        WSDLInterface interface1 = this.womDescription.getInterface(
                new QName("http://www.Monson-Haefel.com/jwsbook/BookQuote",
                        "BookQuote"));
        WSDLOperation operation1 = (WSDLOperation) interface1.getAllOperations()
                .get("getBookPrice");
        QName element1 = operation1.getInputMessage().getElement();
        WSDLOperation operation2 = (WSDLOperation) interface1.getAllOperations()
                .get("getBookPriceNonRobust");
        QName element2 = operation2.getInputMessage().getElement();
        assertEquals(element1, element2);

        Iterator iterator = womDescription.getTypes().getExtensibilityElements()
                .iterator();
        Schema types = null;
        while (iterator.hasNext()) {
            WSDLExtensibilityElement temp = (WSDLExtensibilityElement) iterator.next();
            if (ExtensionConstants.SCHEMA.equals(temp.getType())) {
                types = (Schema) temp;
            }
        }
        int numberOfBookQuote_getBookPrice = 0;
        NodeList childNodes = types.getElelment().getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node item = childNodes.item(i);
            if (item instanceof Element) {
                Element temp = (Element) item;
                if ("complexType".equals(temp.getNodeName()) &&
                        "BookQuote_getBookPrice".equals(
                                temp.getAttribute("name"))) {
                    numberOfBookQuote_getBookPrice++;
                }

            }
        }
        assertEquals(numberOfBookQuote_getBookPrice, 1);


    }
}