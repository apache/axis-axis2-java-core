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

package test.interop.whitemesa.round1.util;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import test.interop.whitemesa.SunClientUtil;

public class Round1StructArrayUtil implements SunClientUtil {

    public SOAPEnvelope getEchoSoapEnvelope() {

        SOAPFactory omfactory = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope reqEnv = omfactory.getDefaultEnvelope();

        OMNamespace envNs =
                reqEnv.declareNamespace("http://schemas.xmlsoap.org/soap/envelope/", "soapenv");
        OMNamespace typeNs =
                reqEnv.declareNamespace("http://www.w3.org/2001/XMLSchema-instance", "xsi");
        OMNamespace encNs =
                reqEnv.declareNamespace("http://schemas.xmlsoap.org/soap/encoding/", "SOAP-ENC");
        reqEnv.declareNamespace("http://www.w3.org/2001/XMLSchema", "xsd");
        reqEnv.declareNamespace("http://soapinterop.org/", "tns");
        reqEnv.declareNamespace("http://soapinterop.org/xsd", "s");

        OMElement operation =
                omfactory.createOMElement("echoStructArray", "http://soapinterop.org/", null);
        reqEnv.getBody().addChild(operation);
        operation.declareNamespace(envNs);
        operation.addAttribute("encodingStyle", "http://schemas.xmlsoap.org/soap/encoding/", envNs);

        OMElement part = omfactory.createOMElement("inputStructArray", null);
        part.declareNamespace(encNs);
        part.declareNamespace(typeNs);
        part.addAttribute("type", "SOAP-ENC:Array", typeNs);
        part.addAttribute("arrayType", "s:SOAPStruct[3]", encNs);

        OMElement item0 = omfactory.createOMElement("item0", null);

        OMElement value00 = omfactory.createOMElement("varString", null);
        value00.declareNamespace(typeNs);
        value00.addAttribute("type", "xsd:string", typeNs);
        value00.addChild(omfactory.createOMText("strss fdfing1"));
        OMElement value01 = omfactory.createOMElement("varInt", null);
        value01.declareNamespace(typeNs);
        value01.addAttribute("type", "xsd:int", typeNs);
        value01.addChild(omfactory.createOMText("25"));
        OMElement value02 = omfactory.createOMElement("varFloat", null);
        value02.declareNamespace(typeNs);
        value02.addAttribute("type", "xsd:float", typeNs);
        value02.addChild(omfactory.createOMText("25.23"));

        OMElement item1 = omfactory.createOMElement("item0", null);

        OMElement value10 = omfactory.createOMElement("varString", null);
        value10.declareNamespace(typeNs);
        value10.addAttribute("type", "xsd:string", typeNs);
        value10.addChild(omfactory.createOMText("strss fdfing1"));
        OMElement value11 = omfactory.createOMElement("varInt", null);
        value11.declareNamespace(typeNs);
        value11.addAttribute("type", "xsd:int", typeNs);
        value11.addChild(omfactory.createOMText("25"));
        OMElement value12 = omfactory.createOMElement("varFloat", null);
        value12.declareNamespace(typeNs);
        value12.addAttribute("type", "xsd:float", typeNs);
        value12.addChild(omfactory.createOMText("25.23"));

        OMElement item2 = omfactory.createOMElement("item0", null);

        OMElement value20 = omfactory.createOMElement("varString", null);
        value20.declareNamespace(typeNs);
        value20.addAttribute("type", "xsd:string", typeNs);
        value20.addChild(omfactory.createOMText("strss fdfing1"));
        OMElement value21 = omfactory.createOMElement("varInt", null);
        value21.declareNamespace(typeNs);
        value21.addAttribute("type", "xsd:int", typeNs);
        value21.addChild(omfactory.createOMText("25"));
        OMElement value22 = omfactory.createOMElement("varFloat", null);
        value22.declareNamespace(typeNs);
        value22.addAttribute("type", "xsd:float", typeNs);
        value22.addChild(omfactory.createOMText("25.23"));

        item0.addChild(value00);
        item0.addChild(value01);
        item0.addChild(value02);

        item1.addChild(value10);
        item1.addChild(value11);
        item1.addChild(value12);

        item2.addChild(value20);
        item2.addChild(value21);
        item2.addChild(value22);

        part.addChild(item0);
        part.addChild(item1);
        part.addChild(item2);

        operation.addChild(part);

        return reqEnv;

    }
}
