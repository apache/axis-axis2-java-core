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

package test.interop.whitemesa.round4.complex.utils;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import test.interop.whitemesa.SunClientUtil;

public class EchoExtendedStructFaultClientUtil implements SunClientUtil {
    public SOAPEnvelope getEchoSoapEnvelope() {

        SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope reqEnv = fac.getDefaultEnvelope();
        OMNamespace omNs = fac.createOMNamespace("http://soapinterop.org/wsdl", "m");
        OMNamespace envNs =
                reqEnv.declareNamespace("http://schemas.xmlsoap.org/soap/envelope/", "SOAP-ENV");

        OMElement method = fac.createOMElement("echoExtendedStructFault", omNs);
        method.declareNamespace(envNs);
        method.addAttribute("encodingStyle", "http://schemas.xmlsoap.org/soap/encoding/", envNs);
        reqEnv.getBody().addChild(method);

        OMElement param = fac.createOMElement("param", null);
        OMElement floatMsg = fac.createOMElement("floatMessage", null);
        OMElement shortMsg = fac.createOMElement("shortMessage", null);
        OMElement stringMsg = fac.createOMElement("stringMessage", null);
        OMElement intMsg = fac.createOMElement("intMessage", null);
        OMElement anotherMsg = fac.createOMElement("anotherIntMessage", null);

        floatMsg.addChild(fac.createOMText(floatMsg, "2.023"));
        shortMsg.addChild(fac.createOMText(shortMsg, "13"));
        stringMsg.addChild(fac.createOMText(stringMsg, "String"));
        intMsg.addChild(fac.createOMText(intMsg, "6"));
        anotherMsg.addChild(fac.createOMText(anotherMsg, "10"));

        param.addChild(floatMsg);
        param.addChild(shortMsg);
        param.addChild(stringMsg);
        param.addChild(intMsg);
        param.addChild(anotherMsg);

        method.addChild(param);

        return reqEnv;
    }
}
