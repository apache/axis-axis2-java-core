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

import axis2.apache.org.FixedString;
import axis2.apache.org.FixedStringElement;

import junit.framework.TestCase;

public class FixedValueTest extends TestCase {

    public static String ERROR_MSG = "Input values do not follow defined XSD restrictions";

    public void testFixedStringElement1() throws Exception {
        FixedStringElement fixElement = new FixedStringElement();
        FixedString fixedString = new FixedString();
        fixedString.setMsg("XYZ");
        fixElement.setFixedStringElement(fixedString);
        OMElement omElement = fixElement.getOMElement(FixedStringElement.MY_QNAME,
                OMAbstractFactory.getSOAP11Factory());
    }

    public void testFixedStringElement2() throws Exception {
        FixedStringElement fixElement = new FixedStringElement();
        FixedString fixedString = new FixedString();
        fixedString.setMsg("");
        fixElement.setFixedStringElement(fixedString);
        OMElement omElement = fixElement.getOMElement(FixedStringElement.MY_QNAME,
                OMAbstractFactory.getSOAP11Factory());
    }

    public void testFixedStringElement3() throws Exception {
        FixedStringElement fixElement = new FixedStringElement();
        FixedString fixedString = new FixedString();
        fixedString.setMsg("");
        fixElement.setFixedStringElement(fixedString);
        OMElement omElement = fixElement.getOMElement(FixedStringElement.MY_QNAME,
                OMAbstractFactory.getSOAP11Factory());
    }

    public void testFixedStringElementParse1() throws Exception {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement element = factory.createOMElement(new QName("", "fixedStringElement"));
        OMElement msg = factory.createOMElement(new QName("", "msg"));
        msg.setText("xyz");
        element.addChild(msg);
        FixedStringElement fixedStringElement = FixedStringElement.Factory.parse(element
                .getXMLStreamReader());
        assertNotNull(fixedStringElement.getFixedStringElement().getMsg());
        assertEquals("ABC", fixedStringElement.getFixedStringElement().getMsg());
        assertFalse("xyz".equalsIgnoreCase(fixedStringElement.getFixedStringElement().getMsg()));

    }

    public void testFixedStringElementParse2() throws Exception {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement element = factory.createOMElement(new QName("", "fixedStringElement"));
        OMElement msg = factory.createOMElement(new QName("", "msg"));
        msg.setText("");
        element.addChild(msg);
        FixedStringElement fixedStringElement = FixedStringElement.Factory.parse(element
                .getXMLStreamReader());
        assertNotNull(fixedStringElement.getFixedStringElement().getMsg());
        assertEquals("ABC", fixedStringElement.getFixedStringElement().getMsg());
        assertFalse("".equalsIgnoreCase(fixedStringElement.getFixedStringElement().getMsg()));

    }

    public void testFixedStringElementParse3() throws Exception {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement element = factory.createOMElement(new QName("", "fixedStringElement"));
        OMElement msg = factory.createOMElement(new QName("", "msg"));
        element.addChild(msg);
        FixedStringElement fixedStringElement = FixedStringElement.Factory.parse(element
                .getXMLStreamReader());
        assertNotNull(fixedStringElement.getFixedStringElement().getMsg());
        assertEquals("ABC", fixedStringElement.getFixedStringElement().getMsg());
        assertFalse(fixedStringElement.getFixedStringElement().getMsg() == null);

    }

}