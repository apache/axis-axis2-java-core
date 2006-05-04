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

package test.interop.whitemesa.round2.util;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.SOAPHeaderBlock;
import test.interop.whitemesa.SunClientUtil;

public class GroupcEchoStringUtil implements SunClientUtil {

    public SOAPEnvelope getEchoSoapEnvelope() {

        SOAPFactory omfactory = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope reqEnv = omfactory.createSOAPEnvelope();
        OMNamespace envNs = reqEnv.declareNamespace("http://schemas.xmlsoap.org/soap/envelope/", "soapenv");
        OMNamespace typeNs = reqEnv.declareNamespace("http://www.w3.org/2001/XMLSchema-instance", "xsi");
        reqEnv.declareNamespace("http://schemas.xmlsoap.org/soap/encoding/", "SOAP-ENC");
        reqEnv.declareNamespace("http://www.w3.org/2001/XMLSchema", "xsd");
        reqEnv.declareNamespace("http://soapinterop.org/xsd", "s");
        reqEnv.declareNamespace("http://soapinterop.org/", "m");

        SOAPHeader header = omfactory.createSOAPHeader(reqEnv);
        OMNamespace hns = reqEnv.declareNamespace("http://soapinterop.org/echoheader/", "hns");
        SOAPHeaderBlock block1 = header.addHeaderBlock("echoMeStringRequest", hns);
        block1.declareNamespace(typeNs);
        block1.addAttribute("type", "xsd:string", typeNs);
        block1.addChild(omfactory.createOMText("string"));
        header.addChild(block1);

        SOAPHeaderBlock block2 = header.addHeaderBlock("echoMeStructRequest", hns);
        block2.declareNamespace(typeNs);
        block2.addAttribute("type", "s:SOAPStruct", typeNs);

        OMElement h2Val1 = omfactory.createOMElement("varString", null);
        h2Val1.declareNamespace(typeNs);
        h2Val1.addAttribute("type", "xsd:string", typeNs);
        h2Val1.addChild(omfactory.createOMText("string"));

        OMElement h2Val2 = omfactory.createOMElement("varInt", null);
        h2Val2.declareNamespace(typeNs);
        h2Val2.addAttribute("type", "xsd:int", typeNs);
        h2Val2.addChild(omfactory.createOMText("150"));

        OMElement h2Val3 = omfactory.createOMElement("varFloat", null);
        h2Val3.declareNamespace(typeNs);
        h2Val3.addAttribute("type", "xsd:float", typeNs);
        h2Val3.addChild(omfactory.createOMText("456.321"));

        block2.addChild(h2Val1);
        block2.addChild(h2Val2);
        block2.addChild(h2Val3);

        OMElement operation = omfactory.createOMElement("echoString", "http://soapinterop.org/", null);

        SOAPBody body = omfactory.createSOAPBody(reqEnv);
        body.addChild(operation);
        operation.declareNamespace(envNs);
        operation.addAttribute("encodingStyle", "http://schemas.xmlsoap.org/soap/encoding/", envNs);

        OMElement part = omfactory.createOMElement("inputString", null);
        part.declareNamespace(typeNs);
        part.addAttribute("type", "xsd:string", typeNs);
        part.addChild(omfactory.createOMText("strssfdfing1"));

        operation.addChild(part);
        return reqEnv;

    }
}