package org.apache.axis2.databinding.schema.populate.derived;

import junit.framework.TestCase;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLInputFactory;
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

public abstract class AbstractDerivedPopulater extends TestCase {

    // force others to implement this method
    public abstract void testPopulate() throws Exception;

    // Simple reusable method to make object instances via reflection
    protected Object process(String testString,String className) throws Exception{
        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new ByteArrayInputStream(testString.getBytes()));
        Class clazz = Class.forName(className);
        Method parseMethod = clazz.getMethod("parse",new Class[]{XMLStreamReader.class});
        Object obj = parseMethod.invoke(null,new Object[]{reader});
        assertNotNull(obj);

        return obj;


    }
}
