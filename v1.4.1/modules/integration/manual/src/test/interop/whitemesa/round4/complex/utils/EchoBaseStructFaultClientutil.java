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

public class EchoBaseStructFaultClientutil implements SunClientUtil {

    public SOAPEnvelope getEchoSoapEnvelope() {

        SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope reqEnv = fac.getDefaultEnvelope();

        OMNamespace omNs = fac.createOMNamespace("http://soapinterop.org/wsdl", "m");

        OMElement method = fac.createOMElement("echoBaseStructFault", omNs);
        method.addAttribute("soapenv:encodingStyle", "http://schemas.xmlsoap.org/soap/encoding/",
                            null);
        method.addAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance", null);
        method.addAttribute("xmlns:xsd", "http://www.w3.org/2001/XMLSchema", null);
        method.addAttribute("xmlns:ns2", "http://soapinterop.org/types", null);
        method.addAttribute("xmlns:wsdl", "http://schemas.xmlsoap.org/wsdl/", null);

        reqEnv.getBody().addChild(method);

        OMElement para = fac.createOMElement("param", null);
        OMElement floatMsg = fac.createOMElement("floatMessage", null);
        OMElement shortMsg = fac.createOMElement("shortMessage ", null);

        floatMsg.addChild(fac.createOMText(floatMsg, "10.3"));
        shortMsg.addChild(fac.createOMText(shortMsg, "10"));

        para.addChild(floatMsg);
        para.addChild(shortMsg);
        method.addChild(para);

        return reqEnv;
    }

}
