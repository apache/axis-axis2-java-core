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

public class EchoMultipleFaults2ClientUtil implements SunClientUtil {

    public SOAPEnvelope getEchoSoapEnvelope() {

        SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope reqEnv = fac.getDefaultEnvelope();

        OMNamespace omNs = fac.createOMNamespace("http://soapinterop.org/wsdl", "m");
        OMNamespace envNs = reqEnv.declareNamespace("http://schemas.xmlsoap.org/soap/envelope/", "SOAP-ENV");
        OMNamespace encNs = reqEnv.declareNamespace("http://schemas.xmlsoap.org/soap/encoding/", "SOAP-ENC");
        OMNamespace typeNs = reqEnv.declareNamespace("http://www.w3.org/2001/XMLSchema-instance", "xsi");
        reqEnv.declareNamespace("http://schemas.xmlsoap.org/soap/encoding/", "m0");
        reqEnv.declareNamespace("http://www.w3.org/2001/XMLSchema", "xsd");

        OMElement operation = fac.createOMElement("echoMultipleFaults2", omNs);
        operation.declareNamespace(envNs);
        operation.addAttribute("encodingStyle", "http://schemas.xmlsoap.org/soap/encoding/", envNs);
        reqEnv.getBody().addChild(operation);

        OMElement wfault = fac.createOMElement("whichFault", null);
        wfault.declareNamespace(typeNs);
        wfault.addAttribute("type", "xsd:int", typeNs);
        OMElement para1 = fac.createOMElement("param1", null);
        para1.declareNamespace(typeNs);
        para1.addAttribute("type", "xsd:string", typeNs);
        OMElement para2 = fac.createOMElement("param2", null);
        para2.declareNamespace(typeNs);
        para2.addAttribute("type", "xsd:float", typeNs);
        OMElement para3 = fac.createOMElement("param3", null);
        para3.declareNamespace(typeNs);
        para3.declareNamespace(encNs);
        para3.addAttribute("type", "SOAP-ENC:Array", typeNs);
        para3.addAttribute("arrayType", "xsd:string[3]", encNs);

        OMElement item0 = fac.createOMElement("Item", null);
        OMElement item1 = fac.createOMElement("Item", null);
        OMElement item2 = fac.createOMElement("Item", null);

        item0.declareNamespace(typeNs);
        item1.declareNamespace(typeNs);
        item2.declareNamespace(typeNs);
        item0.addAttribute("type", "xsd:string", typeNs);
        item1.addAttribute("type", "xsd:string", typeNs);
        item2.addAttribute("type", "xsd:string", typeNs);

        wfault.addChild(fac.createOMText(wfault, "10"));
        para1.addChild(fac.createOMText(para1, "String"));
        para2.addChild(fac.createOMText(para2, "1.2365"));
        item0.addChild(fac.createOMText(item0, "StringValue0"));
        item1.addChild(fac.createOMText(item1, "StringValue1"));
        item2.addChild(fac.createOMText(item2, "StringValue2"));

        para3.addChild(item0);
        para3.addChild(item1);
        para3.addChild(item2);

        operation.addChild(wfault);
        operation.addChild(para1);
        operation.addChild(para2);
        operation.addChild(para3);

        return reqEnv;
    }

}
