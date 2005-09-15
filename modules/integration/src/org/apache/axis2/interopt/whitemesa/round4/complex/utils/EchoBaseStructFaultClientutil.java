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

import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMFactory;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMNamespace;

public class EchoBaseStructFaultClientutil implements WhitemesaR4ClientUtil{

    public OMElement getEchoOMElement() {

        OMFactory fac = OMAbstractFactory.getOMFactory();

        OMNamespace omNs = fac.createOMNamespace("http://soapinterop.org/wsdl", "m");

        OMElement method = fac.createOMElement("echoBaseStructFault", omNs);
        method.addAttribute("soapenv:encodingStyle","http://schemas.xmlsoap.org/soap/encoding/",null);
        method.addAttribute("xmlns:xsi","http://www.w3.org/2001/XMLSchema-instance",null);
        method.addAttribute("xmlns:xsd","http://www.w3.org/2001/XMLSchema",null);
        method.addAttribute("xmlns:ns2", "http://soapinterop.org/types", null);
        method.addAttribute("xmlns:wsdl", "http://schemas.xmlsoap.org/wsdl/", null);


        OMElement value1 = fac.createOMElement("param", null);
        OMElement value2=fac.createOMElement("floatMessage",null);
        OMElement value3=fac.createOMElement("shortMessage ",null);

        value2.addChild(fac.createText(value2, "10.3"));
        value3.addChild(fac.createText(value3, "1.55"));


        value1.addChild(value2);
        value1.addChild(value3);
        method.addChild(value1);


        return method;
    }

}
