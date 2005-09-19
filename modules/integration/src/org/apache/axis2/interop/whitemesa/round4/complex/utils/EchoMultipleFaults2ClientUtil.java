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

package org.apache.axis2.interopt.whitemesa.round4.complex.utils;

import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMFactory;
import org.apache.axis2.om.OMNamespace;

public class EchoMultipleFaults2ClientUtil implements WhitemesaR4ClientUtil{

    public OMElement getEchoOMElement() {

        OMFactory fac = OMAbstractFactory.getOMFactory();

        OMNamespace omNs = fac.createOMNamespace("http://soapinterop.org/wsdl", "m");

        OMElement method = fac.createOMElement("echoMultipleFaults2", omNs);
        method.addAttribute("soapenv:encodingStyle","http://schemas.xmlsoap.org/soap/encoding/",null);

        OMElement value1 = fac.createOMElement("whichFault", null);
        OMElement value2=fac.createOMElement("param1",null);
        OMElement value3=fac.createOMElement("param2",null);
        OMElement value4=fac.createOMElement("param3",null);
        OMElement value5 = fac.createOMElement("floatMessage", null);
        OMElement value6=fac.createOMElement("shortMessage",null);
        OMElement value7=fac.createOMElement("floatMessage",null);
        OMElement value8=fac.createOMElement("shortMessage",null);
        OMElement value9 = fac.createOMElement("stringMessage", null);
        OMElement value10=fac.createOMElement("intMessage",null);
        OMElement value11=fac.createOMElement("anotherIntMessage",null);
        OMElement value12=fac.createOMElement("floatMessage",null);
        OMElement value13=fac.createOMElement("shortMessage",null);
        OMElement value14=fac.createOMElement("stringMessage",null);
        OMElement value15=fac.createOMElement("intMessage",null);
        OMElement value16=fac.createOMElement("anotherIntMessage",null);
        OMElement value17=fac.createOMElement("booleanMessage",null);


        value1.addChild(fac.createText(value1, "1"));
        value5.addChild(fac.createText(value5, "0.10"));
        value6.addChild(fac.createText(value6, "10"));
        value7.addChild(fac.createText(value7, "0.210"));
        value8.addChild(fac.createText(value8, "10"));
        value9.addChild(fac.createText(value9, "hi"));
        value10.addChild(fac.createText(value10, "10"));
        value11.addChild(fac.createText(value11, "15"));
        value12.addChild(fac.createText(value12, "0.569"));
        value13.addChild(fac.createText(value13, "10"));
        value14.addChild(fac.createText(value14, "Nadana"));
        value15.addChild(fac.createText(value15, "14"));
        value16.addChild(fac.createText(value16, "89"));
        value17.addChild(fac.createText(value17, "1"));

        value2.addChild(value5);
        value2.addChild(value6);
        value3.addChild(value7);
        value3.addChild(value8);
        value3.addChild(value9);
        value3.addChild(value10);
        value3.addChild(value11);
        value4.addChild(value12);
        value4.addChild(value13);
        value4.addChild(value14);
        value4.addChild(value15);
        value4.addChild(value16);
        value4.addChild(value17);



        method.addChild(value1);
        method.addChild(value2);
        method.addChild(value3);
        method.addChild(value4);

        return method;
    }








}
