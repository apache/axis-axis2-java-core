package org.apache.axis2.databinding.utils;

import junit.framework.TestCase;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.namespace.QName;
import java.io.StringReader;

import org.apache.ws.commons.om.OMElement;
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

public class NamedStaxOMBuilderTest extends TestCase {

    public void testNamedOMBulder() throws Exception{

        String xmlDoc="<wrapper><myIntVal>200</myIntVal></wrapper>";
        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(
                new StringReader(xmlDoc));

        NamedStaxOMBuilder  sm = new NamedStaxOMBuilder(reader,new QName("wrapper"));
        OMElement elt = sm.getOMElement();

        assertNotNull(elt);



    }

}
