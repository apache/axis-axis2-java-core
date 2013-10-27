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

package org.apache.axis2.schema.extension;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axis2.databinding.types.Language;
import org.apache.axis2.schema.AbstractTestCase;
import org.apache.axis2.schema.restriction.LimitedString;
import org.apache.axis2.schema.restriction.LimitedStringE;

public class SimpleExtensionTest extends AbstractTestCase {

    public void testSimpleTypeComplexExtension() throws Exception {
        FullName fullName = new FullName();
        fullName.setFirst("amila");
        fullName.setMiddle("chinthaka");
        fullName.setLast("suriarachchi");
        fullName.setLanguage(new Language("singhala"));
        fullName.setAttribute1(BaseType.Factory.fromString(BaseType._s1, ""));

        fullName.setAttribute2(SimpleType.Factory.fromString("ATTRIBUTE", ""));

        testSerializeDeserialize(fullName);
    }
    
    public void testSimpleAmmountElementGetOMElement() throws Exception {
        SimpleAmmountElement ammountElement = new SimpleAmmountElement();
        SimpleAmmount param = new SimpleAmmount();
        param.setCurrency("SLR");
        param.setString("1000");
        ammountElement.setSimpleAmmountElement(param);
        OMElement omElement = ammountElement.getOMElement(SimpleAmmountElement.MY_QNAME,
                OMAbstractFactory.getSOAP11Factory());
    }

    public void testSimpleAmmountElementParse() throws Exception {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement element = factory.createOMElement(new QName(
                "http://apache.org/axis2/schema/extension", "SimpleAmmountElement"));
        element.addAttribute("currency", "SLR",
                             factory.createOMNamespace("http://apache.org/axis2/schema/extension", null));
        element.setText("1000");
        SimpleAmmountElement ammountElement = SimpleAmmountElement.Factory.parse(element
                .getXMLStreamReader());
        assertNotNull(ammountElement);
        assertEquals("SLR", ammountElement.getSimpleAmmountElement().getCurrency());
        assertEquals("1000", ammountElement.getSimpleAmmountElement().getString());
    }

    public void testPaymentAmountElementGetOMElement() throws Exception {
        PaymentAmountElement ammountElement = new PaymentAmountElement();
        PaymentAmount param = new PaymentAmount();
        param.setCurrency("SLR");
        param.setString("2000");
        ammountElement.setPaymentAmountElement(param);
        OMElement omElement = ammountElement.getOMElement(PaymentAmountElement.MY_QNAME,
                OMAbstractFactory.getSOAP11Factory());
    }

    public void testPaymentAmountElementParse() throws Exception {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement element = factory.createOMElement(new QName(
                "http://apache.org/axis2/schema/extension", "PaymentAmountElement"));
        element.addAttribute("currency", "SLR",
                             factory.createOMNamespace("http://apache.org/axis2/schema/extension", null));
        element.setText("2000");
        PaymentAmountElement ammountElement = PaymentAmountElement.Factory.parse(element
                .getXMLStreamReader());
        assertNotNull(ammountElement);
        assertEquals("SLR", ammountElement.getPaymentAmountElement().getCurrency());
        assertEquals("2000", ammountElement.getPaymentAmountElement().getString());
    }

    public void testFullpersoninfoElementGetOMElement() throws Exception {
        FullpersoninfoElement fullpersoninfoElement = new FullpersoninfoElement();
        Fullpersoninfo param = new Fullpersoninfo();
        param.setAddress("123 Main Street");
        param.setCity("Kandy");
        param.setCountry("Sri Lanka");
        param.setFirstname("Sagara");
        param.setLastname("Gunathunga");
        fullpersoninfoElement.setFullpersoninfoElement(param);
        OMElement omElement = fullpersoninfoElement.getOMElement(FullpersoninfoElement.MY_QNAME,
                OMAbstractFactory.getSOAP11Factory());
    }

    public void testFullpersoninfoElementParse() throws Exception {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement element = factory.createOMElement(new QName(
                "http://apache.org/axis2/schema/extension", "fullpersoninfoElement"));
        OMElement firstname = factory.createOMElement(new QName(
                "http://apache.org/axis2/schema/extension", "firstname"));
        firstname.setText("Sagara");
        OMElement lastname = factory.createOMElement(new QName(
                "http://apache.org/axis2/schema/extension", "lastname"));
        lastname.setText("Gunathunga");
        OMElement address = factory.createOMElement(new QName(
                "http://apache.org/axis2/schema/extension", "address"));
        address.setText("123 Main Street");
        OMElement city = factory.createOMElement(new QName(
                "http://apache.org/axis2/schema/extension", "city"));
        city.setText("Kandy");
        OMElement country = factory.createOMElement(new QName(
                "http://apache.org/axis2/schema/extension", "country"));
        country.setText("Sri Lanka");
        element.addChild(firstname);
        element.addChild(lastname);
        element.addChild(address);
        element.addChild(city);
        element.addChild(country);
        FullpersoninfoElement fullpersoninfoElement = FullpersoninfoElement.Factory.parse(element
                .getXMLStreamReader());
        assertNotNull(fullpersoninfoElement);
        assertEquals("Sagara", fullpersoninfoElement.getFullpersoninfoElement().getFirstname());
        assertEquals("Gunathunga", fullpersoninfoElement.getFullpersoninfoElement().getLastname());
        assertEquals("123 Main Street", fullpersoninfoElement.getFullpersoninfoElement()
                .getAddress());
        assertEquals("Kandy", fullpersoninfoElement.getFullpersoninfoElement().getCity());
        assertEquals("Sri Lanka", fullpersoninfoElement.getFullpersoninfoElement().getCountry());

    }
    
    public void testReproStringTypeElementGetOMElement() throws Exception {
        ReproStringTypeElement reproStringTypeElement = new ReproStringTypeElement();   
        Language lang = new Language();
        lang.setValue("EN");
        reproStringTypeElement.setLang(lang );
        reproStringTypeElement.setReproStringType("Value");
        OMElement omElement = reproStringTypeElement.getOMElement(ReproStringTypeElement.MY_QNAME,
                OMAbstractFactory.getSOAP11Factory());
        omElement.serialize(System.out);
    }

    public void testReproStringTypeElementParse() throws Exception {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement element = factory.createOMElement(new QName(
                "http://apache.org/axis2/schema/extension", "ReproStringTypeElement"));
        element.addAttribute("lang", "EN",
                             factory.createOMNamespace("http://apache.org/axis2/schema/extension", null));
        element.setText("Value");
       
        ReproStringTypeElement reproStringTypeElement = ReproStringTypeElement.Factory.parse(element
                .getXMLStreamReader());
        assertNotNull(reproStringTypeElement);
        assertEquals("EN", reproStringTypeElement.getLang().toString());
        assertEquals("Value", reproStringTypeElement.getReproStringType());
       

    }
    
    
    
}
