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

public class Round1StringUtil implements SunClientUtil {

    public SOAPEnvelope getEchoSoapEnvelope() {
        SOAPFactory omFactory = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope reqEnv = omFactory.getDefaultEnvelope();

        reqEnv.declareNamespace("http://soapinterop.org/", "ns1");
        reqEnv.declareNamespace("http://schemas.xmlsoap.org/soap/encoding/", "SOAP-ENC");
        reqEnv.declareNamespace("http://www.w3.org/1999/XMLSchema", "xsd");
        OMNamespace typeNs =
                reqEnv.declareNamespace("http://www.w3.org/1999/XMLSchema-instance/", "xsi");

        OMElement method = omFactory.createOMElement("echoString", "http://soapinterop.org/", null);
        reqEnv.getBody().addChild(method);
        method.addAttribute("encordingStyle", "http://schemas.xmlsoap.org/soap/encoding/", null);

        OMElement value = omFactory.createOMElement("inputString", "http://soapinterop.org/", null);
        value.declareNamespace(typeNs);
        value.addAttribute("type", "xsd:string", typeNs);
        value.addChild(omFactory.createOMText("Lanka Software Foundation"));
        method.addChild(value);

        return reqEnv;
    }

}
