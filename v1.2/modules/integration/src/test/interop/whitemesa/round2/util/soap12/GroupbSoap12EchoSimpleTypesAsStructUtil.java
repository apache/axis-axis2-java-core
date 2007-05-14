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

package test.interop.whitemesa.round2.util.soap12;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import test.interop.whitemesa.SunClientUtil;

public class GroupbSoap12EchoSimpleTypesAsStructUtil implements SunClientUtil {

    public SOAPEnvelope getEchoSoapEnvelope() {

        SOAPFactory omfactory = OMAbstractFactory.getSOAP12Factory();
        SOAPEnvelope reqEnv = omfactory.getDefaultEnvelope();
        reqEnv.declareNamespace("http://www.w3.org/2001/XMLSchema", "xsd");
        reqEnv.declareNamespace("http://schemas.xmlsoap.org/soap/encoding/", "SOAP-ENC");
        reqEnv.declareNamespace("http://soapinterop.org/", "tns");
        reqEnv.declareNamespace("http://soapinterop.org/xsd", "s");
        reqEnv.declareNamespace("http://www.w3.org/2001/XMLSchema-instance", "xsi");

        OMElement operation = omfactory
                .createOMElement("echoSimpleTypesAsStruct", "http://soapinterop.org/", null);
        reqEnv.getBody().addChild(operation);
        operation.addAttribute("soapenv:encodingStyle", "http://www.w3.org/2003/05/soap-encoding",
                               null);

        OMElement part0 = omfactory.createOMElement("inputString", "", null);
        part0.addAttribute("xsi:type", "xsd:string", null);
        part0.addChild(omfactory.createOMText("45ascasc  acasa asd52"));

        OMElement part1 = omfactory.createOMElement("inputInteger", "", null);
        part1.addAttribute("xsi:type", "xsd:int", null);
        part1.addChild(omfactory.createOMText("4552"));

        OMElement part2 = omfactory.createOMElement("inputFloat", "", null);
        part2.addAttribute("xsi:type", "xsd:float", null);
        part2.addChild(omfactory.createOMText("450.52"));

        operation.addChild(part0);
        operation.addChild(part1);
        operation.addChild(part2);

        return reqEnv;
    }
}
