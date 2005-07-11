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

package org.apache.axis2.xpath;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.axis2.om.impl.llom.builder.StAXOMBuilder;
import org.apache.axis2.om.xpath.DocumentNavigator;
import org.jaxen.FunctionCallException;
import org.jaxen.Navigator;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.FileInputStream;

public class AXIOMXPathTest extends XPathTestBase {
    public AXIOMXPathTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(AXIOMXPathTest.class);
    }

    public Navigator getNavigator() {
        return new DocumentNavigator();
    }

    public Object getDocument(String uri) throws Exception {
        try {
            XMLStreamReader parser =
                    XMLInputFactory.newInstance().createXMLStreamReader(
                            new FileInputStream(uri));
            StAXOMBuilder builder =
                    new StAXOMBuilder(parser);
            return builder.getDocumentElement();
        } catch (Exception e) {
            throw new FunctionCallException(e);
        }
    }
}
