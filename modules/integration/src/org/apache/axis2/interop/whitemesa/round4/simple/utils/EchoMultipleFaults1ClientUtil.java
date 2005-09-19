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

package org.apache.axis2.interop.whitemesa.round4.simple.utils;

import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMFactory;
import org.apache.axis2.om.OMNamespace;

public class EchoMultipleFaults1ClientUtil implements WhitemesaR4ClientUtil {

    public OMElement getEchoOMElement() {

        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://soapinterop.org/wsdl", "m");

        OMElement method = fac.createOMElement("echoMultipleFaults1", omNs);
        method.addAttribute("soapenv:encodingStyle","http://schemas.xmlsoap.org/soap/encoding/",null);


        OMElement value = fac.createOMElement("whichFault", null);
        OMElement value1 = fac.createOMElement("param1", null);
        OMElement value2 = fac.createOMElement("param2", null);
        OMElement value3 = fac.createOMElement("Item", null);
        OMElement value4 = fac.createOMElement("Item", null);
        OMElement value5 = fac.createOMElement("Item", null);



        value.addChild(fac.createText(value, "10"));
        value1.addChild(fac.createText(value1, "hi"));
        value3.addChild(fac.createText(value3, "1.0"));
        value4.addChild(fac.createText(value4, "20.6"));
        value5.addChild(fac.createText(value5, "2.6"));

        value2.addChild(value3);
        value2.addChild(value4);
        value2.addChild(value5);

        method.addChild(value);
        method.addChild(value1);
        method.addChild(value2);


        return method;
    }


}
