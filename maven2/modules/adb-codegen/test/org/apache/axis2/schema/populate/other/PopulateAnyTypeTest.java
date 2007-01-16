package org.apache.axis2.schema.populate.other;

import junit.framework.TestCase;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.lang.reflect.Method;

import org.apache.axiom.om.util.StAXUtils;
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

public class PopulateAnyTypeTest extends TestCase {

     private String xmlString = "<myObject xmlns=\"http://soapinterop.org/xsd2\">" +
            "<soapStructures>" +
            "<varFloat>3.3</varFloat>" +
            "<varInt>5</varInt>" +
            "<varString>Hello11</varString>" +
            "<varString>Hello11</varString>" +
            "<varString>Hello12</varString>" +
            "<varString>Hello13</varString>" +
            "</soapStructures>" +
            "</myObject>";

    public void testPopulate() throws Exception{

               XMLStreamReader reader = StAXUtils.createXMLStreamReader(new ByteArrayInputStream(xmlString.getBytes()));
               Class clazz = Class.forName("org.soapinterop.xsd2.MyObject");
               Class innerClazz = clazz.getDeclaredClasses()[0];
               Method parseMethod = innerClazz.getMethod("parse",new Class[]{XMLStreamReader.class});
               Object obj = parseMethod.invoke(null,new Object[]{reader});




    }

}
