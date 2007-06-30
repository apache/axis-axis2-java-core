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

public class EchoSOAPStructFaultClientUtil implements SunClientUtil {

    public SOAPEnvelope getEchoSoapEnvelope() {
        SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope reqEnv = fac.getDefaultEnvelope();

        OMNamespace omNs = reqEnv.declareNamespace("http://soapinterop.org/wsdl", "m");
        OMNamespace typeNs =
                reqEnv.declareNamespace("http://www.w3.org/2001/XMLSchema-instance", "xsi");
        reqEnv.declareNamespace("http://schemas.xmlsoap.org/soap/encoding/", "SOAP-ENC");

        reqEnv.declareNamespace("http://www.w3.org/2001/XMLSchema", "xsd");
        reqEnv.declareNamespace("http://soapinterop.org/types", "m0");

        OMElement method = fac.createOMElement("echoSOAPStructFault", omNs);
        OMNamespace soapEnvNs =
                method.declareNamespace("http://schemas.xmlsoap.org/soap/envelope/", "SOAP-ENV");
        method.addAttribute("encodingStyle", "http://schemas.xmlsoap.org/soap/encoding/",
                            soapEnvNs);

        reqEnv.getBody().addChild(method);

        OMElement param = fac.createOMElement("param", null);
        OMElement sstruct = fac.createOMElement("soapStruct", null);
        OMElement vstring = fac.createOMElement("varString", null);
        OMElement vint = fac.createOMElement("varInt", null);
        OMElement vfloat = fac.createOMElement("varFloat", null);

        param.declareNamespace(typeNs);
        sstruct.declareNamespace(typeNs);
        vstring.declareNamespace(typeNs);
        vint.declareNamespace(typeNs);
        vfloat.declareNamespace(typeNs);

        param.addAttribute("type", "m0:SOAPStructFault", typeNs);
        sstruct.addAttribute("type", "m0:SOAPStruct", typeNs);
        vstring.addAttribute("type", "xsd:string", typeNs);
        vint.addAttribute("type", "xsd:int", typeNs);
        vfloat.addAttribute("type", "xsd:float", typeNs);

        vstring.addChild(fac.createOMText(vstring, "String"));
        vint.addChild(fac.createOMText(vint, "0"));
        vfloat.addChild(fac.createOMText(vfloat, "3.14159E0"));

        sstruct.addChild(vstring);
        sstruct.addChild(vint);
        sstruct.addChild(vfloat);
        param.addChild(sstruct);
        method.addChild(param);

        return reqEnv;
    }
}
