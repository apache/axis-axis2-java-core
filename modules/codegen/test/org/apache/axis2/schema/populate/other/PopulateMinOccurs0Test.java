package org.apache.axis2.schema.populate.other;

import junit.framework.TestCase;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.IntrospectionException;
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

public class PopulateMinOccurs0Test extends TestCase {

    private String xmlString1 = "<root xmlns=\"http://test.org\">" +
            "<A>I am A</A>" +
            "<B>I am B1</B>" +
            "<B>I am B2</B>" +
            "<C>I am B2</C>" +
            "<C>I am B2</C>" +
            "</root>";

    private String xmlString2 = "<root xmlns=\"http://test.org\">" +
            "<A>I am A</A>" +
            "<C>I am B2</C>" +
            "<C>I am B2</C>" +
            "</root>";

    public void testPopulate1() throws Exception{
        populateAndAssert(xmlString1,2);
    }

     public void testPopulate2() throws Exception{
         populateAndAssert(xmlString2,0);
    }

    private void populateAndAssert(String s,int expectedCount) throws XMLStreamException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, IntrospectionException {
        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new ByteArrayInputStream(s.getBytes()));
        Class clazz = Class.forName("org.test.Root");
        Class innerClazz = clazz.getDeclaredClasses()[0];
        Method parseMethod = innerClazz.getMethod("parse",new Class[]{XMLStreamReader.class});
        Object obj = parseMethod.invoke(null,new Object[]{reader});

        assertNotNull(obj);

        Object stringArray = null;
        BeanInfo beanInfo =  Introspector.getBeanInfo(obj.getClass());
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        Method readMethod;
        for (int i = 0; i < propertyDescriptors.length; i++) {
            PropertyDescriptor propertyDescriptor = propertyDescriptors[i];
            if ("b".equals(propertyDescriptor.getDisplayName())){
                readMethod = propertyDescriptor.getReadMethod();
                stringArray = readMethod.invoke(obj, null);
                break;
            }
        }

        assertNotNull(stringArray);
        String[] array = (String[])stringArray;
        assertEquals(array.length,expectedCount);
    }



}