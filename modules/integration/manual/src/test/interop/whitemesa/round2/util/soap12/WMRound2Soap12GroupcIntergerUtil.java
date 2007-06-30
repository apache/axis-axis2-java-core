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

package test.interop.whitemesa.round2.util.soap12;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.SOAPHeaderBlock;
import test.interop.whitemesa.SunClientUtil;

public class WMRound2Soap12GroupcIntergerUtil implements SunClientUtil {

    public SOAPEnvelope getEchoSoapEnvelope() {

        SOAPFactory omfactory = OMAbstractFactory.getSOAP12Factory();
        SOAPEnvelope reqEnv = omfactory.getDefaultEnvelope();
        reqEnv.declareNamespace("http://www.w3.org/2001/XMLSchema", "xsd");
        reqEnv.declareNamespace("http://schemas.xmlsoap.org/soap/encoding/", "SOAP-ENC");
        reqEnv.declareNamespace("http://www.w3.org/2001/XMLSchema-instance", "xsi");
        reqEnv.declareNamespace("http://soapinterop.org/xsd", "s");
        reqEnv.declareNamespace("http://soapinterop.org/", "m");
        reqEnv.declareNamespace("http://soapinterop.org", "m1");

        SOAPHeader header = omfactory.createSOAPHeader(reqEnv);
        OMNamespace hns = reqEnv.declareNamespace("http://soapinterop.org/echoheader/", "hns");
        SOAPHeaderBlock block1 = header.addHeaderBlock("echoMeStringRequest", hns);
        block1.addAttribute("xsi:type", "xsd:string", null);
        block1.addChild(omfactory.createOMText("string"));
        header.addChild(block1);

        SOAPHeaderBlock block2 = header.addHeaderBlock("echoMeStructRequest", hns);
        block2.addAttribute("xsi:type", "s:SOAPStruct", null);

        OMElement h2Val1 = omfactory.createOMElement("varString", null);
        h2Val1.addAttribute("xsi:type", "xsd:string", null);
        h2Val1.addChild(omfactory.createOMText("string"));

        OMElement h2Val2 = omfactory.createOMElement("varInt", null);
        h2Val2.addAttribute("xsi:type", "xsd:int", null);
        h2Val2.addChild(omfactory.createOMText("150"));

        OMElement h2Val3 = omfactory.createOMElement("varFloat", null);
        h2Val3.addAttribute("xsi:type", "xsd:float", null);
        h2Val3.addChild(omfactory.createOMText("456.321"));

        block2.addChild(h2Val1);
        block2.addChild(h2Val2);
        block2.addChild(h2Val3);

        OMElement operation =
                omfactory.createOMElement("echoInteger", "http://soapinterop.org/", null);

        reqEnv.getBody().addChild(operation);
        operation.addAttribute("soapenv:encodingStyle", "http://www.w3.org/2003/05/soap-encoding",
                               null);

        OMElement part = omfactory.createOMElement("inputInteger", null);
        part.addAttribute("xsi:type", "xsd:int", null);
        part.addChild(omfactory.createOMText("456"));

        operation.addChild(part);
        return reqEnv;

    }
}
