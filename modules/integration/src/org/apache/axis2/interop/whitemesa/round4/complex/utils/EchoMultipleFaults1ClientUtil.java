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
public class EchoMultipleFaults1ClientUtil implements WhitemesaR4ClientUtil{

    public OMElement getEchoOMElement() {

        OMFactory fac = OMAbstractFactory.getOMFactory();

        OMNamespace omNs = fac.createOMNamespace("http://soapinterop.org/wsdl", "m");

        OMElement method = fac.createOMElement("echoMultipleFaults1", omNs);
        method.addAttribute("soapenv:encodingStyle","http://schemas.xmlsoap.org/soap/encoding/",null);

        OMElement value1 = fac.createOMElement("whichFault", null);
        OMElement value2=fac.createOMElement("param1",null);
        OMElement value3=fac.createOMElement("param2",null);
        OMElement value4 = fac.createOMElement("varInt", null);
        OMElement value5=fac.createOMElement("varFloat",null);
        OMElement value6=fac.createOMElement("varString",null);
        OMElement value7 = fac.createOMElement("floatMessage", null);
        OMElement value8=fac.createOMElement("shortMessage",null);

        value1.addChild(fac.createText(value1, "10"));
        value4.addChild(fac.createText(value4, "1"));
        value5.addChild(fac.createText(value5, "1.0"));
        value6.addChild(fac.createText(value6, "hi"));
        value7.addChild(fac.createText(value7, "0.23"));
        value8.addChild(fac.createText(value8, "45"));


        value2.addChild(value4);
        value2.addChild(value5);
        value2.addChild(value6);

        value3.addChild(value7);
        value3.addChild(value8);
        method.addChild(value1);
        method.addChild(value2);
        method.addChild(value3);


        return method;
    }



}
