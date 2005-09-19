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

public class EchoSOAPStructFaultClientUtil implements WhitemesaR4ClientUtil{

    public OMElement getEchoOMElement() {

        OMFactory fac = OMAbstractFactory.getOMFactory();

        OMNamespace omNs = fac.createOMNamespace("http://soapinterop.org/wsdl", "m");

        OMElement method = fac.createOMElement("echoSOAPStructFault", omNs);
        method.addAttribute("soapenv:encodingStyle","http://schemas.xmlsoap.org/soap/encoding/",null);
        method.addAttribute("xmlns:xsi","http://www.w3.org/2001/XMLSchema-instance",null);

        method.addAttribute("xmlns:ns2", "http://soapinterop.org/types", null);
        method.addAttribute("xmlns:wsdl", "http://schemas.xmlsoap.org/wsdl/", null);

        OMElement value1 = fac.createOMElement("param", null);
        OMElement value2 = fac.createOMElement("soapStruct", null);
        OMElement value3 = fac.createOMElement("varString", null);
        OMElement value4 = fac.createOMElement("varInt", null);
        OMElement value5 = fac.createOMElement("varFloat", null);


        value3.addChild(fac.createText(value3, "hi"));
        value4.addChild(fac.createText(value4, "10"));
        value5.addChild(fac.createText(value5, "5.3"));

                                  
        value2.addChild(value5);
        value2.addChild(value4);
        value2.addChild(value3);
        value1.addChild(value2);
        method.addChild(value1);


        return method;
    }


}
