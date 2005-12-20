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

package org.apache.axis2.databinding;

import junit.framework.TestCase;
import org.apache.axis2.databinding.metadata.ElementDesc;
import org.apache.axis2.databinding.metadata.TypeDesc;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.impl.llom.builder.StAXOMBuilder;
import org.apache.axis2.om.impl.llom.factory.OMXMLBuilderFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * ArrayTest
 */
public class ArrayTest extends TestCase {
    public static class TestBean {
        static TypeDesc typeDesc;

        public static TypeDesc getTypeDesc() {
            if (typeDesc == null) {
                typeDesc = new TypeDesc();
                typeDesc.setJavaClass(ArrayTest.class);
                ElementDesc desc = new ElementDesc();
                desc.setFieldName("collection");
                desc.setQName(new QName("item"));
                desc.setMaxOccurs(-1);
                typeDesc.addField(desc);
            }
            return typeDesc;
        }

        ArrayList coll;

        public TestBean() {
            coll = new ArrayList();
        }

        public String [] getCollection() {
            return (String[])(coll.toArray(new String [coll.size()]));
        }

        public void setCollection(int index, String val) {
            if (index + 1 > coll.size()) {
               while (index + 1 > coll.size()) {
                   coll.add(null);
               }
            }
            coll.set(index, val);
        }

        public String getCollection(int index) {
            return (String)coll.get(index);
        }
    }

    public void testArray() throws Exception {
        InputStream is = new FileInputStream("test-resources/xmls/array1.xml");
        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(is);

        StAXOMBuilder builder = OMXMLBuilderFactory.createStAXOMBuilder(OMAbstractFactory.getOMFactory(), reader);
        OMElement el = builder.getDocumentElement();
        XMLStreamReader omReader = el.getXMLStreamReaderWithoutCaching();
        omReader.next();
        DeserializationContext context = new DeserializationContext();
        Object value = context.deserializeToClass(omReader, TestBean.class);

        assertNotNull(value);
        assertTrue(value instanceof TestBean);
        TestBean bean = (TestBean)value;
        assertEquals("Wrong # of items", 3, bean.coll.size());
        assertEquals("one", bean.getCollection(0));
        assertEquals("two", bean.getCollection(1));
        assertEquals("three", bean.getCollection(2));
    }

/*
    public void testMultiDimArray() throws Exception {
        InputStream is = new FileInputStream("test-resources/xmls/MultiDimArray.xml");
        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(is);

        StAXOMBuilder builder = OMXMLBuilderFactory.createStAXOMBuilder(OMAbstractFactory.getOMFactory(), reader);
        OMElement el = builder.getDocumentElement();
        XMLStreamReader omReader = el.getXMLStreamReaderWithoutCaching();
        omReader.next();
        DeserializationContext context = new DeserializationContext();
        Object value = context.deserializeToClass(omReader, TestBean.class);

        assertNotNull(value);
        assertTrue(value instanceof TestBean);
        TestBean bean = (TestBean)value;
        assertEquals("Wrong # of items", 3, bean.coll.size());
        assertEquals("one", bean.getCollection(0));
        assertEquals("two", bean.getCollection(1));
        assertEquals("three", bean.getCollection(2));
    }
*/
}
