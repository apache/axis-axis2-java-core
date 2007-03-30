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
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import test.interop.whitemesa.SunClientUtil;
import test.interop.whitemesa.WhiteMesaConstants;

public class Round2EchoStructArrayClientUtil implements SunClientUtil {

    public SOAPEnvelope getEchoSoapEnvelope() {

        SOAPFactory omfactory = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope reqEnv = omfactory.getDefaultEnvelope();

        reqEnv.declareNamespace("http://www.w3.org/2001/XMLSchema", "xsd");
        reqEnv.declareNamespace("http://soapinterop.org/", "tns");
        reqEnv.declareNamespace("http://soapinterop.org/xsd", "s");
        reqEnv.declareNamespace("http://www.w3.org/2001/XMLSchema-instance", "xsi");
        OMNamespace encNs =
                reqEnv.declareNamespace("http://schemas.xmlsoap.org/soap/encoding/", "SOAP-ENC");

        OMElement operation =
                omfactory.createOMElement("echoStructArray", "http://soapinterop.org/", null);
        reqEnv.getBody().addChild(operation);
        operation.addAttribute("soapenv:encodingStyle", "http://schemas.xmlsoap.org/soap/encoding/",
                               null);

        OMElement part = omfactory.createOMElement("inputStructArray", null);
        part.addAttribute("xsi:type", "SOAP-ENC:Array", null);
        part.declareNamespace(encNs);
        part.addAttribute("arrayType", "s:SOAPStruct[3]", encNs);

        OMElement item0 = omfactory.createOMElement("item0", null);

        OMElement value00 = omfactory.createOMElement("varString", null);
        value00.addAttribute("xsi:type", "xsd:string", null);
        value00.addChild(omfactory.createOMText(WhiteMesaConstants.ECHO_STRUCT_ARRAY_STR_1));
        OMElement value01 = omfactory.createOMElement("varInt", null);
        value01.addAttribute("xsi:type", "xsd:int", null);
        value01.addChild(omfactory.createOMText(WhiteMesaConstants.ECHO_STRUCT_ARRAY_INT_1));
        OMElement value02 = omfactory.createOMElement("varFloat", null);
        value02.addAttribute("xsi:type", "xsd:float", null);
        value02.addChild(omfactory.createOMText(WhiteMesaConstants.ECHO_STRUCT_ARRAY_FLOAT_1));

        OMElement item1 = omfactory.createOMElement("item0", null);

        OMElement value10 = omfactory.createOMElement("varString", null);
        value10.addAttribute("xsi:type", "xsd:string", null);
        value10.addChild(omfactory.createOMText(WhiteMesaConstants.ECHO_STRUCT_ARRAY_STR_2));
        OMElement value11 = omfactory.createOMElement("varInt", null);
        value11.addAttribute("xsi:type", "xsd:int", null);
        value11.addChild(omfactory.createOMText(WhiteMesaConstants.ECHO_STRUCT_ARRAY_INT_2));
        OMElement value12 = omfactory.createOMElement("varFloat", null);
        value12.addAttribute("xsi:type", "xsd:float", null);
        value12.addChild(omfactory.createOMText(WhiteMesaConstants.ECHO_STRUCT_ARRAY_FLOAT_2));

        OMElement item2 = omfactory.createOMElement("item0", null);

        OMElement value20 = omfactory.createOMElement("varString", null);
        value20.addAttribute("xsi:type", "xsd:string", null);
        value20.addChild(omfactory.createOMText(WhiteMesaConstants.ECHO_STRUCT_ARRAY_STR_3));
        OMElement value21 = omfactory.createOMElement("varInt", null);
        value21.addAttribute("xsi:type", "xsd:int", null);
        value21.addChild(omfactory.createOMText(WhiteMesaConstants.ECHO_STRUCT_ARRAY_INT_3));
        OMElement value22 = omfactory.createOMElement("varFloat", null);
        value22.addAttribute("xsi:type", "xsd:float", null);
        value22.addChild(omfactory.createOMText(WhiteMesaConstants.ECHO_STRUCT_ARRAY_FLOAT_3));

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

        //reqEnv.getBody().addChild(method);
        return reqEnv;

    }
}
