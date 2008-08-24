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

public class EchoMultipleFaults2ClientUtil implements SunClientUtil {

    public SOAPEnvelope getEchoSoapEnvelope() {

        SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope reqEnv = fac.getDefaultEnvelope();

        OMNamespace omNs = fac.createOMNamespace("http://soapinterop.org/wsdl", "m");
        OMNamespace envNs =
                reqEnv.declareNamespace("http://schemas.xmlsoap.org/soap/envelope/", "SOAP-ENV");
        reqEnv.declareNamespace("http://schemas.xmlsoap.org/soap/encoding/", "SOAP-ENC");
        reqEnv.declareNamespace("http://www.w3.org/2001/XMLSchema-instance", "xsi");
        reqEnv.declareNamespace("http://www.w3.org/2001/XMLSchema", "xsd");
        reqEnv.declareNamespace("http://soapinterop.org/types", "m0");

        OMElement method = fac.createOMElement("echoMultipleFaults2", omNs);
        method.declareNamespace(envNs);
        method.addAttribute("encodingStyle", "http://schemas.xmlsoap.org/soap/encoding/", envNs);
        reqEnv.getBody().addChild(method);

        OMElement wfault = fac.createOMElement("whichFault", null);
        OMElement para1 = fac.createOMElement("param1", null);
        OMElement para2 = fac.createOMElement("param2", null);
        OMElement para3 = fac.createOMElement("param3", null);
        OMElement floatMsg1 = fac.createOMElement("floatMessage", null);
        OMElement shortMsg1 = fac.createOMElement("shortMessage", null);
        OMElement floatMsg2 = fac.createOMElement("floatMessage", null);
        OMElement shortMsg2 = fac.createOMElement("shortMessage", null);
        OMElement stringMsg2 = fac.createOMElement("stringMessage", null);
        OMElement intMsg2 = fac.createOMElement("intMessage", null);
        OMElement anotherMsg2 = fac.createOMElement("anotherIntMessage", null);
        OMElement floatMsg3 = fac.createOMElement("floatMessage", null);
        OMElement shortMsg3 = fac.createOMElement("shortMessage", null);
        OMElement stringMsg3 = fac.createOMElement("stringMessage", null);
        OMElement intMsg3 = fac.createOMElement("intMessage", null);
        OMElement anotherMsg3 = fac.createOMElement("anotherIntMessage", null);
        OMElement booleanMsg3 = fac.createOMElement("booleanMessage", null);

        wfault.addChild(fac.createOMText(wfault, "0"));
        floatMsg1.addChild(fac.createOMText(floatMsg1, "7.22"));
        shortMsg1.addChild(fac.createOMText(shortMsg1, "10"));
        floatMsg2.addChild(fac.createOMText(floatMsg2, "1.414"));
        shortMsg2.addChild(fac.createOMText(shortMsg2, "10"));
        stringMsg2.addChild(fac.createOMText(stringMsg2, "String Value1"));
        intMsg2.addChild(fac.createOMText(intMsg2, "1"));
        anotherMsg2.addChild(fac.createOMText(anotherMsg2, "15"));
        floatMsg3.addChild(fac.createOMText(floatMsg3, "0.569"));
        shortMsg3.addChild(fac.createOMText(shortMsg3, "2"));
        stringMsg3.addChild(fac.createOMText(stringMsg3, "String Value3"));
        intMsg3.addChild(fac.createOMText(intMsg3, "14"));
        anotherMsg3.addChild(fac.createOMText(anotherMsg3, "11"));
        booleanMsg3.addChild(fac.createOMText(booleanMsg3, "true"));

        para1.addChild(floatMsg1);
        para1.addChild(shortMsg1);
        para2.addChild(floatMsg2);
        para2.addChild(shortMsg2);
        para2.addChild(stringMsg2);
        para2.addChild(intMsg2);
        para2.addChild(anotherMsg2);
        para3.addChild(floatMsg3);
        para3.addChild(shortMsg3);
        para3.addChild(stringMsg3);
        para3.addChild(intMsg3);
        para3.addChild(anotherMsg3);
        para3.addChild(booleanMsg3);

        method.addChild(wfault);
        method.addChild(para1);
        method.addChild(para2);
        method.addChild(para3);

        return reqEnv;
    }
}
