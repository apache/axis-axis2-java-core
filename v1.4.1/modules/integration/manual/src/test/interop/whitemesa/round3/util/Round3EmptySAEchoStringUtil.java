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

package test.interop.whitemesa.round3.util;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import test.interop.whitemesa.SunClientUtil;


public class Round3EmptySAEchoStringUtil implements SunClientUtil {
    public SOAPEnvelope getEchoSoapEnvelope() {

        SOAPFactory omfactory = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope reqEnv = omfactory.getDefaultEnvelope();
        reqEnv.declareNamespace("http://schemas.xmlsoap.org/soap/envelope/", "soapenv");
        reqEnv.declareNamespace("http://www.w3.org/2001/XMLSchema", "xsd");
        reqEnv.declareNamespace("http://schemas.xmlsoap.org/soap/encoding/", "SOAP-ENC");
        reqEnv.declareNamespace("http://soapinterop/", "tns");
        reqEnv.declareNamespace("http://soapinterop.org/xsd", "s");
        reqEnv.declareNamespace("http://www.w3.org/2001/XMLSchema-instance", "xsi");

        OMElement operation = omfactory.createOMElement("echoString", "http://soapinterop/", null);
        reqEnv.getBody().addChild(operation);
        operation.addAttribute("soapenv:encodingStyle", "http://schemas.xmlsoap.org/soap/encoding/",
                               null);

        OMElement part = omfactory.createOMElement("a", "", null);
        part.addAttribute("xsi:type", "xsd:string", null);
        part.addChild(omfactory.createOMText("strss fdfing1"));

        operation.addChild(part);
        return reqEnv;

    }

}
