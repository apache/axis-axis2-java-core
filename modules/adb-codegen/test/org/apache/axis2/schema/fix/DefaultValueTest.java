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

package org.apache.axis2.schema.fix;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;

import axis2.apache.org.DefaultString;
import axis2.apache.org.DefaultStringElement;
import axis2.apache.org.FixedStringElement;

import junit.framework.TestCase;

public class DefaultValueTest extends TestCase {

    public static String ERROR_MSG = "Input values do not follow defined XSD restrictions";

    public void testDefaultStringElement1() throws Exception {
        DefaultStringElement defaultElement = new DefaultStringElement();
        DefaultString defaultString = new DefaultString();
        defaultString.setMsg("XYZ");
        defaultElement.setDefaultStringElement(defaultString);
        OMElement omElement = defaultElement.getOMElement(FixedStringElement.MY_QNAME,
                OMAbstractFactory.getSOAP11Factory());
    }

    public void testDefaultStringElement2() throws Exception {
        DefaultStringElement defaultElement = new DefaultStringElement();
        DefaultString defaultString = new DefaultString();
        defaultString.setMsg("XYZ");
        defaultElement.setDefaultStringElement(defaultString);
        OMElement omElement = defaultElement.getOMElement(FixedStringElement.MY_QNAME,
                OMAbstractFactory.getSOAP11Factory());
    }

    public void testDefaultStringElementParse1() throws Exception {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement element = factory.createOMElement(new QName("", "defaultStringElement"));
        OMElement msg = factory.createOMElement(new QName("", "msg"));
        element.addChild(msg);
        DefaultStringElement defaultStringElement = DefaultStringElement.Factory.parse(element
                .getXMLStreamReader());
        assertNotNull(defaultStringElement.getDefaultStringElement().getMsg());
        assertEquals("", defaultStringElement.getDefaultStringElement().getMsg());
    }

    public void testDefaultStringElementParse2() throws Exception {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement element = factory.createOMElement(new QName("", "defaultStringElement"));
        OMElement msg = factory.createOMElement(new QName("", "msg"));
        msg.setText("xyz");
        element.addChild(msg);
        DefaultStringElement defaultStringElement = DefaultStringElement.Factory.parse(element
                .getXMLStreamReader());
        System.out.println(defaultStringElement.getDefaultStringElement().getMsg());
        assertNotNull(defaultStringElement.getDefaultStringElement().getMsg());
        assertEquals("xyz", defaultStringElement.getDefaultStringElement().getMsg());

    }

    public void testDefaultStringElementParse3() throws Exception {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement element = factory.createOMElement(new QName("", "defaultStringElement"));
        DefaultStringElement defaultStringElement = DefaultStringElement.Factory.parse(element
                .getXMLStreamReader());
        assertNotNull(defaultStringElement.getDefaultStringElement().getMsg());
        assertEquals("ABC", defaultStringElement.getDefaultStringElement().getMsg());

    }

}