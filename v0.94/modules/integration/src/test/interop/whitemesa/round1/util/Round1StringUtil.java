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

import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.SOAPFactory;

public class Round1StringUtil implements Round1ClientUtil {

    public SOAPEnvelope getEchoSoapEnvelope() {
        SOAPFactory omFactory = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope reqEnv = omFactory.getDefaultEnvelope();
        //OMNamespace namespace = reqEnv.declareNamespace("http://sample1.org/sample1", "sample1");
        reqEnv.declareNamespace("http://soapinterop.org/", "ns1");
        reqEnv.declareNamespace("http://schemas.xmlsoap.org/soap/encoding/", "SOAP-ENC");
        reqEnv.declareNamespace("http://www.w3.org/1999/XMLSchema-instance/", "xsi");
        reqEnv.declareNamespace("http://www.w3.org/1999/XMLSchema", "xsd");


        OMElement method = omFactory.createOMElement("echoString", "http://soapinterop.org/", null);
        reqEnv.getBody().addChild(method);
        method.addAttribute("encordingStyle", "http://schemas.xmlsoap.org/soap/encoding/", null);

        OMElement value = omFactory.createOMElement("inputString", "http://soapinterop.org/", null);
        value.addAttribute("xsi:type", "xsd:string", null);
        value.addChild(omFactory.createText("Lanka Software Foundation"));
        method.addChild(value);

        return reqEnv;
    }

}
