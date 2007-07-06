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

package test.interop.sun.round4.simple.util;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAP11Constants;

public class EchoIntArrayFaultClientUtil implements SunGroupHClientUtil {


    public OMElement getEchoOMElement() {
        OMFactory fac = OMAbstractFactory.getOMFactory();

        OMNamespace omNs = fac.createOMNamespace("http://soapinterop.org/wsdl", "m");

        OMElement method = fac.createOMElement("echoIntArrayFault", omNs);
        OMNamespace soapEnvNS = method.declareNamespace(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI,
                                                        SOAP11Constants.SOAP_DEFAULT_NAMESPACE_PREFIX);
        method.addAttribute("encodingStyle", "http://schemas.xmlsoap.org/soap/encoding/",
                            soapEnvNS);


        OMElement value1 = fac.createOMElement("param", null);
        method.addChild(value1);
        value1.addAttribute("soapenc:arrayType", "nsa:int[3]", null);
        value1.addAttribute("soapenc:offset", "[0]", null);
        value1.addAttribute("xmlns:soapenc", "http://schemas.xmlsoap.org/soap/encoding/", null);
        value1.addAttribute("xmlns:nsa", "http://www.w3.org/2001/XMLSchema", null);
        OMElement value2 = fac.createOMElement("Item", null);
        OMElement value3 = fac.createOMElement("Item", null);
        OMElement value4 = fac.createOMElement("Item", null);

        value1.addChild(value2);
        value1.addChild(value3);
        value1.addChild(value4);


        value2.addChild(fac.createOMText(value2, "99"));
        value3.addChild(fac.createOMText(value3, "10"));
        value4.addChild(fac.createOMText(value4, "12"));


        return method;
    }


}
