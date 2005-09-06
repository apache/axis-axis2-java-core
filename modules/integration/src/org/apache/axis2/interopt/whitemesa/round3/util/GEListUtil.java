package org.apache.axis2.interopt.whitemesa.round3.util;

import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.soap.SOAPBody;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.SOAPFactory;


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
*
*
*/

/**
 * Author: Gayan Asanka
 * Date: Aug 23, 2005
 * Time: 4:27:20 PM
 */

public class GEListUtil implements SunRound3ClientUtil {

    public SOAPEnvelope getEchoSoapEnvelope() {

        SOAPFactory omfactory = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope reqEnv = omfactory.createSOAPEnvelope();
        reqEnv.declareNamespace("http://schemas.xmlsoap.org/soap/envelope/", "soapenv");
        reqEnv.declareNamespace("http://schemas.xmlsoap.org/wsdl/", "xmlns");
        reqEnv.declareNamespace("http://schemas.xmlsoap.org/wsdl/soap/", "soap");
        reqEnv.declareNamespace("http://www.w3.org/2001/XMLSchema", "xsd");
        reqEnv.declareNamespace("http://schemas.xmlsoap.org/soap/encoding/", "SOAP-ENC");
        reqEnv.declareNamespace("http://soapinterop.org/", "tns");
        reqEnv.declareNamespace("http://soapinterop.org/xsd", "s");
        reqEnv.declareNamespace("http://schemas.xmlsoap.org/wsdl/", "wsdl");
        reqEnv.declareNamespace("http://soapinterop.org/WSDLInteropTestRpcEnc", "ns1");
        reqEnv.declareNamespace("http://soapinterop.org/WSDLInteropTestList", "ns2");
        reqEnv.declareNamespace("http://www.w3.org/2001/XMLSchema-instance","xsi");

        OMElement operation = omfactory.createOMElement("echoLinkedList", "http://soapinterop.org/WSDLInteropTestRpcEnc", null);
        SOAPBody body = omfactory.createSOAPBody(reqEnv);
        body.addChild(operation);
        operation.addAttribute("soapenv:encodingStyle", "http://schemas.xmlsoap.org/soap/encoding/", null);

        OMElement part = omfactory.createOMElement("param0", "", null);
        part.addAttribute("xsi:type", "s:List", null);


        OMElement value00 = omfactory.createOMElement("varInt", "", null);
        value00.addAttribute("xsi:type", "xsd:int", null);
        value00.addChild(omfactory.createText("255"));
        OMElement value01 = omfactory.createOMElement("varString", "", null);
        value01.addAttribute("xsi:type", "xsd:string", null);
        value01.addChild(omfactory.createText("Axis2"));
        OMElement value02 = omfactory.createOMElement("child", "", null);
        //value02.addAttribute("xsi:type", "xsd:anyType", null);
        value02.addAttribute("href", "#ID1", null);


        OMElement part2 = omfactory.createOMElement("item0", null);
        part2.addAttribute("soapenv:encodingStyle", "http://schemas.xmlsoap.org/soap/encoding/", null);

        part2.addAttribute("xsi:type", "s:List", null);
        part2.addAttribute("id", "ID1", null);


        OMElement value10 = omfactory.createOMElement("varInt", "", null);
        value10.addAttribute("xsi:type", "xsd:int", null);
        value10.addChild(omfactory.createText("21"));
        OMElement value11 = omfactory.createOMElement("varString", "", null);
        value11.addAttribute("xsi:type", "xsd:string", null);
        value11.addChild(omfactory.createText("LSF"));
        OMElement value12 = omfactory.createOMElement("child", "", null);
        value12.addAttribute("xsi:type", "xsd:anyType", null);
        value12.addAttribute(" xsi:nil", "1", null);
//
//        OMElement item2 = omfactory.createOMElement("item0", null);
//
//        OMElement value20 = omfactory.createOMElement("varString", "", null);
//        value20.addAttribute("xsi:type", "xsd:string", null);
//        value20.addChild(omfactory.createText("strss fdfing1"));
//        OMElement value21 = omfactory.createOMElement("varInt", "", null);
//        value21.addAttribute("xsi:type", "xsd:int", null);
//        value21.addChild(omfactory.createText("25"));
//        OMElement value22 = omfactory.createOMElement("varFloat", "", null);
//        value22.addAttribute("xsi:type", "xsd:float", null);
//        value22.addChild(omfactory.createText("25.23"));

        part.addChild(value00);
        part.addChild(value01);
        part.addChild(value02);

        part2.addChild(value10);
        part2.addChild(value11);
        part2.addChild(value12);
//
//        item2.addChild(value20);
//        item2.addChild(value21);
//        item2.addChild(value22);

//        part.addChild(item0);
//        part.addChild(item1);
//        part.addChild(item2);

        operation.addChild(part);
        body.addChild(part2);

        //reqEnv.getBody().addChild(method);
        return reqEnv;

    }
}
