package org.apache.axis2.schema.populate.simple;

import junit.framework.TestCase;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.ByteArrayInputStream;
import java.lang.reflect.Method;
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

public abstract class AbstractSimplePopulater extends TestCase {

    // force others to implement this method
    public abstract void testPopulate() throws Exception;
    protected  String className= null;
    protected Class propertyClass = null;

    // Simple reusable method to make object instances via reflection
    protected Object process(String testString,String className) throws Exception{
        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new ByteArrayInputStream(testString.getBytes()));
        Class clazz = Class.forName(className);
        Method parseMethod = clazz.getMethod("parse",new Class[]{XMLStreamReader.class});
        Object obj = parseMethod.invoke(null,new Object[]{reader});
        assertNotNull(obj);

        return obj;

    }

    protected void checkValue(String xmlToSet, String value) throws Exception {
        Object o = process(xmlToSet, className);
        Class beanClass = Class.forName(className);
        BeanInfo info = Introspector.getBeanInfo(beanClass);
        PropertyDescriptor[] propDescs = info.getPropertyDescriptors();
        for (int i = 0; i < propDescs.length; i++) {
            PropertyDescriptor propDesc = propDescs[i];
            if  (propDesc.getPropertyType().equals(propertyClass)){
                String s = convertToString(propDesc.getReadMethod().invoke(o, null));
                compare(value,s);
            }

        }

    }

    protected void compare(String val1,String val2){
         assertEquals(val1,val2);
    }

    protected String convertToString(Object o){
        return o.toString();
    }
}
