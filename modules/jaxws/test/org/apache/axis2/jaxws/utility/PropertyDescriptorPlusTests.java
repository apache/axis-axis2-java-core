/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.axis2.jaxws.utility;

import junit.framework.TestCase;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Arrays;

public class PropertyDescriptorPlusTests extends TestCase {
    public void testJAXBElement() throws Exception {
        InvokeAction object = new InvokeAction();

        BeanInfo structBeanInfo = Introspector.getBeanInfo(InvokeAction.class);
        PropertyDescriptor[] descriptors = structBeanInfo.getPropertyDescriptors();
        assertNotNull(descriptors);
        assertEquals(descriptors.length, 2);

        QName qName = new QName("", "args0");
        PropertyDescriptorPlus plus = new PropertyDescriptorPlus(descriptors[0], qName);
        byte[] testValue = {0xd, 0xe, 0xa, 0xd, 0xb, 0xe, 0xe, 0xf};
        plus.set(object, testValue);

        JAXBElement<byte[]> arg0 = object.getArg0();
        assertEquals(arg0.getDeclaredType(), byte[].class);
        assertEquals(arg0.getName(), qName);
        assertTrue(Arrays.equals(testValue, arg0.getValue()));

        Object value = plus.get(object);
        assertEquals(value.getClass(), byte[].class);
        assertTrue(Arrays.equals(testValue, (byte[]) value));
    }
}
