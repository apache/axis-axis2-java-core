package org.apache.axis2.interopt.whitemesa.round4.simple.utils;

import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMFactory;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMNamespace;

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
 *
 *
 */
public class EchoMultipleFaults2ClientUtil implements WhitemesaR4ClientUtil{

    public OMElement getEchoOMElement() {
        OMFactory fac = OMAbstractFactory.getOMFactory();

        OMNamespace omNs = fac.createOMNamespace("http://soapinterop.org/wsdl", "m");


        OMElement method = fac.createOMElement("echoMultipleFaults2", omNs);
        method.addAttribute("soapenv:encodingStyle", "http://schemas.xmlsoap.org/soap/encoding/",null);
//        method.addAttribute("xmlns:xsi","http://www.w3.org/2001/XMLSchema-instance", null);
//        method.addAttribute("xmlns:xsd","http://www.w3.org/2001/XMLSchema", null);
//        method.addAttribute("xmlns:ns2","http://soapinterop.org/types", null);
//        method.addAttribute("xmlns:soap-enc","http://schemas.xmlsoap.org/soap/encoding/", null);
//        method.addAttribute("xmlns:wsdl","http://schemas.xmlsoap.org/wsdl/", null);

        OMElement value = fac.createOMElement("whichFault", null);
        OMElement value1 = fac.createOMElement("param1", null);
        OMElement value2 = fac.createOMElement("param2", null);
        OMElement value3 = fac.createOMElement("param3", null);

        OMElement value4 = fac.createOMElement("Item", null);
        OMElement value5 = fac.createOMElement("Item", null);
       OMElement value6 = fac.createOMElement("Item", null);


//        value.addAttribute("xsi:type", "xsd:int", null);
//        value1.addAttribute("xsi:type", "xsd:string", null);
//

        value3.addAttribute("xmlns:nsa","http://www.w3.org/2001/XMLSchema",null);
        value3.addAttribute("soapenc:arrayType","nsa:string[3]", null);
        value3.addAttribute("soapenc:offset","[0]", null);
        value3.addAttribute("xmlns:soapenc","http://schemas.xmlsoap.org/soap/encoding/" , null);

        value.addChild(fac.createText(value, "10"));
        value1.addChild(fac.createText(value1, "hi"));
        value2.addChild(fac.createText(value3, "0.236"));
        value4.addChild(fac.createText(value4, "String1"));
        value5.addChild(fac.createText(value5, "String2"));
        value6.addChild(fac.createText(value6, "String3"));

        value3.addChild(value4);
        value3.addChild(value5);
        value3.addChild(value6);

        method.addChild(value);
        method.addChild(value1);
        method.addChild(value2);
        method.addChild(value3);


        return method;
    }

}
