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

package org.apache.axis2.interopt.whitemesa.round4.simple.utils;

import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMFactory;
import org.apache.axis2.om.OMNamespace;

public class EchoMultipleFaults4ClientUtil implements WhitemesaR4ClientUtil{
    public OMElement getEchoOMElement() {
        OMFactory fac = OMAbstractFactory.getOMFactory();

        OMNamespace omNs = fac.createOMNamespace("http://soapinterop.org/wsdl", "m");

        OMElement method = fac.createOMElement("echoMultipleFaults4", omNs);
        method.addAttribute("soapenv:encodingStyle", "http://schemas.xmlsoap.org/soap/encoding/",null);
//        method.addAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance", null);
//        method.addAttribute("xmlns:xsd", "http://www.w3.org/2001/XMLSchema", null);
//        method.addAttribute("xmlns:ns2", "http://soapinterop.org/types", null);
//        method.addAttribute("xmlns:soap-enc", "http://schemas.xmlsoap.org/soap/encoding/", null);
//        method.addAttribute("xmlns:wsdl", "http://schemas.xmlsoap.org/wsdl/", null);

        OMElement value = fac.createOMElement("whichFault", null);
        OMElement value1 = fac.createOMElement("param1", null);
        OMElement value2 = fac.createOMElement("param2", null);

//        value.addAttribute("xsi:type", "xsd:int", null);
//        value1.addAttribute("xsi:type", "xsd:int", null);
//        value2.addAttribute("xsi:type", "ns2:Enum", null);

        value.addChild(fac.createText(value, "10"));
        value1.addChild(fac.createText(value1, "11"));
        value2.addChild(fac.createText(value2, "1"));


        method.addChild(value);
        method.addChild(value1);
        method.addChild(value2);



        return method;
    }



}
