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

package test.interop.whitemesa.round4.complex.utils;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import test.interop.whitemesa.SunClientUtil;

public class EchoMultipleFaults1ClientUtil implements SunClientUtil {

    public SOAPEnvelope getEchoSoapEnvelope() {

        SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope reqEnv = fac.getDefaultEnvelope();

        OMNamespace omNs = fac.createOMNamespace("http://soapinterop.org/wsdl", "m");
        OMNamespace envNs = reqEnv.declareNamespace("http://schemas.xmlsoap.org/soap/envelope/", "SOAP-ENV");

        OMElement method = fac.createOMElement("echoMultipleFaults1", omNs);
        method.declareNamespace(envNs);
        method.addAttribute("encodingStyle", "http://schemas.xmlsoap.org/soap/encoding/", envNs);
        reqEnv.getBody().addChild(method);

        OMElement whichfault = fac.createOMElement("whichFault", null);
        OMElement param1 = fac.createOMElement("param1", null);
        OMElement param2 = fac.createOMElement("param2", null);
        OMElement vInt = fac.createOMElement("varInt", null);
        OMElement vFloat = fac.createOMElement("varFloat", null);
        OMElement vString = fac.createOMElement("varString", null);
        OMElement floatMsg = fac.createOMElement("floatMessage", null);
        OMElement shortMsg = fac.createOMElement("shortMessage", null);

        whichfault.addChild(fac.createOMText(whichfault, "10"));
        vInt.addChild(fac.createOMText(vInt, "1"));
        vFloat.addChild(fac.createOMText(vFloat, "1.0"));
        vString.addChild(fac.createOMText(vString, "String"));
        floatMsg.addChild(fac.createOMText(floatMsg, "0.23"));
        shortMsg.addChild(fac.createOMText(shortMsg, "45"));

        param1.addChild(vString);
        param1.addChild(vInt);
        param1.addChild(vFloat);

        param2.addChild(floatMsg);
        param2.addChild(shortMsg);
        method.addChild(whichfault);
        method.addChild(param1);
        method.addChild(param2);

        return reqEnv;
    }
}
