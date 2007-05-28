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

package test.interop.whitemesa.round4.simple.utils;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import test.interop.whitemesa.SunClientUtil;

public class EchoMultipleFaults1ClientUtil implements SunClientUtil {
    public SOAPEnvelope getEchoSoapEnvelope() {

        SOAPFactory omfactory = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope reqEnv = omfactory.getDefaultEnvelope();

        reqEnv.declareNamespace("http://schemas.xmlsoap.org/soap/encoding/", "m0");
        reqEnv.declareNamespace("http://www.w3.org/2001/XMLSchema", "xsd");
        OMNamespace omNs = omfactory.createOMNamespace("http://soapinterop.org/wsdl", "m");
        OMNamespace envNs =
                reqEnv.declareNamespace("http://schemas.xmlsoap.org/soap/envelope/", "SOAP-ENV");
        OMNamespace typeNs =
                reqEnv.declareNamespace("http://www.w3.org/2001/XMLSchema-instance", "xsi");
        OMNamespace encNs1 =
                reqEnv.declareNamespace("http://schemas.xmlsoap.org/soap/encoding/", "SOAP-ENC");

        OMElement operation = omfactory.createOMElement("echoMultipleFaults1", omNs);
        operation.declareNamespace(envNs);
        operation.addAttribute("encodingStyle", "http://schemas.xmlsoap.org/soap/encoding/", envNs);
        reqEnv.getBody().addChild(operation);

        OMElement wfault = omfactory.createOMElement("whichFault", null);
        OMElement param1 = omfactory.createOMElement("param1", null);
        OMElement param2 = omfactory.createOMElement("param2", null);
        OMElement item0 = omfactory.createOMElement("item0", null);

        wfault.declareNamespace(typeNs);
        param1.declareNamespace(typeNs);
        param2.declareNamespace(typeNs);
        param2.declareNamespace(encNs1);
        item0.declareNamespace(typeNs);

        wfault.addAttribute("type", "xsd:int", typeNs);
        param1.addAttribute("type", "xsd:string", typeNs);
        param2.addAttribute("type", "SOAP-ENC:Array", typeNs);
        param2.addAttribute("arrayType", "xsd:float[1]", encNs1);
        item0.addAttribute("type", "xsd:float", typeNs);

        wfault.addChild(omfactory.createOMText(wfault, "0"));
        param1.addChild(omfactory.createOMText(param1, "String"));
        item0.addChild(omfactory.createOMText(item0, "1.3456"));
        param2.addChild(item0);

        operation.addChild(wfault);
        operation.addChild(param1);
        operation.addChild(param2);

        return reqEnv;

    }
}
