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

package test.interop.sun.round4.complex;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAP11Constants;

public class EchoMultipleFaults2ClientUtil implements SunGroupHClientUtil{

    public OMElement getEchoOMElement() {
        OMFactory fac = OMAbstractFactory.getOMFactory();

        OMNamespace omNs = fac.createOMNamespace("http://soapinterop.org/wsdl", "m");

        OMElement method = fac.createOMElement("echoMultipleFaults2", omNs);
        OMNamespace soapEnvNS = method.declareNamespace(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI,
                SOAP11Constants.SOAP_DEFAULT_NAMESPACE_PREFIX);
        method.addAttribute("encodingStyle", "http://schemas.xmlsoap.org/soap/encoding/", soapEnvNS);



        OMElement value1 = fac.createOMElement("whichFault", null);
        method.addChild(value1);


        OMElement value2 = fac.createOMElement("param1", null);

        OMElement value3 = fac.createOMElement("floatMessage", null);
        OMElement value4 = fac.createOMElement("shortMessage", null);
        OMElement value5 = fac.createOMElement("param2", null);
        OMElement value6 = fac.createOMElement("floatMessage", null);
        OMElement value7 = fac.createOMElement("shortMessage", null);
        OMElement value8 = fac.createOMElement("stringMessage", null);
        OMElement value9 = fac.createOMElement("intMessage", null);
        OMElement value10 = fac.createOMElement("anotherIntMessage", null);
        OMElement value11 = fac.createOMElement("param3", null);
        OMElement value12 = fac.createOMElement("floatMessage", null);
        OMElement value13 = fac.createOMElement("shortMessage", null);
        OMElement value14 = fac.createOMElement("stringMessage", null);
        OMElement value15 = fac.createOMElement("intMessage", null);
        OMElement value16 = fac.createOMElement("anotherIntMessage", null);
        OMElement value17 = fac.createOMElement("booleanMessage", null);


        value2.addChild(value3);
        value2.addChild(value4);
        value5.addChild(value6);
        value5.addChild(value7);
        value5.addChild(value8);
        value5.addChild(value9);
        value5.addChild(value10);
        value11.addChild(value12);
        value11.addChild(value13);
        value11.addChild(value14);
        value11.addChild(value15);
        value11.addChild(value16);
        value11.addChild(value17);


        method.addChild(value2);
        method.addChild(value5);
        method.addChild(value11);


        value1.addChild(fac.createOMText(value1, "2"));
        value3.addChild(fac.createOMText(value3, "0.21"));
        value4.addChild(fac.createOMText(value4, "6"));
        value6.addChild(fac.createOMText(value6, "0.555"));
        value7.addChild(fac.createOMText(value7, "9"));
        value8.addChild(fac.createOMText(value8, "hi"));
        value9.addChild(fac.createOMText(value9, "10"));

        value10.addChild(fac.createOMText(value10, "20"));

        value12.addChild(fac.createOMText(value12, "0.111"));

        value13.addChild(fac.createOMText(value13, "11"));

        value14.addChild(fac.createOMText(value14, "hi"));
        value15.addChild(fac.createOMText(value15, "8"));
        value16.addChild(fac.createOMText(value16, "9"));
        value17.addChild(fac.createOMText(value17, "1"));

        return method;
    }








}
