package org.apache.axis2.interopt.whitemesa.round4.complex.utils;

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
public class EchoExtendedStructFaultClientUtil implements WhitemesaR4ClientUtil{
    
    public OMElement getEchoOMElement() {

        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://soapinterop.org/wsdl", "m");

        OMElement method = fac.createOMElement("echoExtendedStructFault", omNs);
        method.addAttribute("soapenv:encodingStyle","http://schemas.xmlsoap.org/soap/encoding/",null);


        OMElement value1 = fac.createOMElement("param", null);
        OMElement value2=fac.createOMElement("floatMessage",null);
        OMElement value3=fac.createOMElement("shortMessage",null);
        OMElement value4=fac.createOMElement("stringMessage",null);
        OMElement value5=fac.createOMElement("intMessage",null);
        OMElement value6=fac.createOMElement("anotherIntMessage",null);



        value2.addChild(fac.createText(value2, ".023"));
        value3.addChild(fac.createText(value3, "1.3"));
        value4.addChild(fac.createText(value4, "hi"));
        value5.addChild(fac.createText(value5, "6"));
        value6.addChild(fac.createText(value6, "78"));



        value1.addChild(value2);
        value1.addChild(value3);
        value1.addChild(value4);
        value1.addChild(value5);
        value1.addChild(value6);

        method.addChild(value1);


        return method;
    }




}
