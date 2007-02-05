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

package test.interop.whitemesa.round1.util;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import test.interop.whitemesa.SunClientUtil;

public class Round1StringArrayUtil implements SunClientUtil {

    public SOAPEnvelope getEchoSoapEnvelope() {

        SOAPFactory omfactory = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope reqEnv = omfactory.getDefaultEnvelope();

        OMNamespace envNs = reqEnv.declareNamespace("http://schemas.xmlsoap.org/soap/envelope/", "soapenv");
        OMNamespace typeNs = reqEnv.declareNamespace("http://www.w3.org/1999/XMLSchema-instance/", "xsi");
        OMNamespace encNs = reqEnv.declareNamespace("http://schemas.xmlsoap.org/soap/encoding/", "SOAPENC");
        reqEnv.declareNamespace("http://www.w3.org/2001/XMLSchema", "xsd");
        reqEnv.declareNamespace("http://soapinterop.org/", "tns");
        reqEnv.declareNamespace("http://soapinterop.org/xsd", "s");

        OMElement operation = omfactory.createOMElement("echoStringArray", "http://soapinterop.org/", null);
        reqEnv.getBody().addChild(operation);
        operation.declareNamespace(envNs);
        operation.addAttribute("encodingStyle", "http://schemas.xmlsoap.org/soap/encoding/", envNs);

        OMElement part = omfactory.createOMElement("inputStringArray", null);
        part.declareNamespace(typeNs);
        part.declareNamespace(encNs);
        part.addAttribute("type", "SOAPENC:Array", typeNs);
        part.addAttribute("arrayType", "xsd:string[3]", encNs);

        OMElement value0 = omfactory.createOMElement("item", "", null);
        value0.declareNamespace(typeNs);
        value0.addAttribute("type", "xsd:string", typeNs);
        value0.addChild(omfactory.createOMText("Apache Axis2"));
        OMElement value1 = omfactory.createOMElement("item", "", null);
        value1.declareNamespace(typeNs);
        value1.addAttribute("type", "xsd:string", typeNs);
        value1.addChild(omfactory.createOMText("Lanka Software Foundation"));
        OMElement value2 = omfactory.createOMElement("item", "", null);
        value2.declareNamespace(typeNs);
        value2.addAttribute("type", "xsd:string", typeNs);
        value2.addChild(omfactory.createOMText("www.opensource.lk"));

        part.addChild(value0);
        part.addChild(value1);
        part.addChild(value2);

        operation.addChild(part);
        return reqEnv;
    }
}
