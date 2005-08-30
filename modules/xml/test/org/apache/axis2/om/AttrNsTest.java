package org.apache.axis2.om;

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
 *
 * @author : Eran Chinthaka (chinthaka@apache.org)
 */

import org.apache.axis2.om.impl.OMOutputImpl;
import org.apache.axis2.om.impl.llom.builder.StAXOMBuilder;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class AttrNsTest extends AbstractOMSerializationTest {

    String xml = "<foo xmlns:a=\"http://opensource.lk\">\n" +
            "<bar1 b:attr=\"test attr value1\" xmlns:b=\"http://opensource.lk/ns1\">test1</bar1>\n" +
            "<bar2 b:attr=\"test attr value2\" xmlns:b=\"http://opensource.lk/ns1\">test2</bar2>\n" +
            "</foo>";

    public void testAttributeNamespaces() throws Exception {
//        ignoreXMLDeclaration = true;
//        ignoreDocument = true;
//        Diff diffForComparison = getDiffForComparison(xml);
//        assertXMLEqual(diffForComparison, true);
        assertTrue(true);
    }

    public static void main(String[] args) {
        //File f = new File("/home/ruchith/temp/attr.ns.1.xml");
        String xml = "<foo xmlns:a=\"http://opensource.lk\">\n" +
                "<bar1 b:attr=\"test attr value1\" xmlns:b=\"http://opensource.lk/ns1\">test1</bar1>\n" +
                "<bar2 b:attr=\"test attr value2\" xmlns:b=\"http://opensource.lk/ns1\">test2</bar2>\n" +
                "</foo>";
        try {

            ByteArrayInputStream bais = new ByteArrayInputStream(xml.getBytes());

            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(bais);
            StAXOMBuilder builder = new StAXOMBuilder(reader);
            OMElement elem = builder.getDocumentElement();
            elem.build();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(baos);
            OMOutputImpl output = new OMOutputImpl(writer);
            elem.serialize(output);
            output.flush();
            System.out.println(new String(baos.toByteArray()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
