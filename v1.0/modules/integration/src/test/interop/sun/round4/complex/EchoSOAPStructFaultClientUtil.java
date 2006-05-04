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

package test.interop.sun.round4.complex;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAP11Constants;

public class EchoSOAPStructFaultClientUtil implements SunGroupHClientUtil{

    public OMElement getEchoOMElement() {

        OMFactory fac = OMAbstractFactory.getOMFactory();

        OMNamespace omNs = fac.createOMNamespace("http://soapinterop.org/wsdl", "m");

        OMElement method = fac.createOMElement("echoSOAPStructFault", omNs);
        OMNamespace soapEnvNS = method.declareNamespace(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI,
                SOAP11Constants.SOAP_DEFAULT_NAMESPACE_PREFIX);
        method.addAttribute("encodingStyle", "http://schemas.xmlsoap.org/soap/encoding/", soapEnvNS);


        OMElement value1 = fac.createOMElement("param", null);
        method.addChild(value1);


        OMElement value2 = fac.createOMElement("soapStruct", null);
        OMElement value3 = fac.createOMElement("varInt", null);
        OMElement value4 = fac.createOMElement("varFloat", null);
        OMElement value5 = fac.createOMElement("varString", null);


        value2.addChild(value3);
        value2.addChild(value4);
        value2.addChild(value5);

        value1.addChild(value2);
        value3.addChild(fac.createOMText(value3, "10"));
        value4.addChild(fac.createOMText(value4, "0.568"));
        value5.addChild(fac.createOMText(value5, "String"));

        return method;
    }



}
