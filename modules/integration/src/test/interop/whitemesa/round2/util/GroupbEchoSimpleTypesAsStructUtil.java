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
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.SOAPFactory;

public class GroupbEchoSimpleTypesAsStructUtil implements SunRound2ClientUtil {

    public SOAPEnvelope getEchoSoapEnvelope() {

        SOAPFactory omfactory = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope reqEnv = omfactory.getDefaultEnvelope();

        reqEnv.declareNamespace("http://www.w3.org/2001/XMLSchema", "xsd");
        reqEnv.declareNamespace("http://schemas.xmlsoap.org/soap/encoding/", "SOAP-ENC");
        reqEnv.declareNamespace("http://soapinterop.org/", "tns");
        reqEnv.declareNamespace("http://soapinterop.org/xsd", "s");
        reqEnv.declareNamespace("http://www.w3.org/2001/XMLSchema-instance","xsi");

        OMElement operation = omfactory.createOMElement("echoSimpleTypesAsStruct", "http://soapinterop.org/", null);
        reqEnv.getBody().addChild(operation);
        operation.addAttribute("soapenv:encodingStyle", "http://schemas.xmlsoap.org/soap/encoding/", null);

        OMElement part0 = omfactory.createOMElement("inputString", "", null);
        part0.addAttribute("xsi:type", "xsd:string", null);
        part0.addChild(omfactory.createText("45ascasc  acasa asd52"));

        OMElement part1 = omfactory.createOMElement("inputInteger", "", null);
        part1.addAttribute("xsi:type", "xsd:int", null);
        part1.addChild(omfactory.createText("4552"));

        OMElement part2 = omfactory.createOMElement("inputFloat", "", null);
        part2.addAttribute("xsi:type", "xsd:float", null);
        part2.addChild(omfactory.createText("450.52"));

        operation.addChild(part0);
        operation.addChild(part1);
        operation.addChild(part2); //reqEnv.getBody().addChild(method);

        return reqEnv;
    }
}
