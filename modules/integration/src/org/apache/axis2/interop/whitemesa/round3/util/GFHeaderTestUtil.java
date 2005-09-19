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

package org.apache.axis2.interopt.whitemesa.round3.util;

import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.soap.SOAPBody;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.SOAPFactory;
import org.apache.axis2.soap.SOAPHeader;
import org.apache.axis2.soap.SOAPHeaderBlock;


public class GFHeaderTestUtil implements SunRound3ClientUtil{

    public SOAPEnvelope getEchoSoapEnvelope() {

        SOAPFactory omfactory = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope reqEnv = omfactory.createSOAPEnvelope();
        reqEnv.declareNamespace("http://schemas.xmlsoap.org/soap/envelope/", "soapenv");
        reqEnv.declareNamespace("http://schemas.xmlsoap.org/wsdl/", "xmlns");
        reqEnv.declareNamespace("http://schemas.xmlsoap.org/wsdl/soap/", "soap");
        reqEnv.declareNamespace("http://www.w3.org/2001/XMLSchema", "xsd");
        reqEnv.declareNamespace("http://schemas.xmlsoap.org/soap/encoding/", "SOAP-ENC");
        reqEnv.declareNamespace("http://soapinterop.org/", "tns");
        reqEnv.declareNamespace("http://soapinterop.org/xsd", "s");
        reqEnv.declareNamespace("http://schemas.xmlsoap.org/wsdl/", "wsdl");
        reqEnv.declareNamespace("http://www.w3.org/2001/XMLSchema-instance","xsi");

        SOAPHeader header = omfactory.createSOAPHeader(reqEnv);
        OMNamespace ns = header.declareNamespace("http://soapinterop.org/xsd", "ns0");
        header.addAttribute("soapenv:encodingStyle", "http://schemas.xmlsoap.org/soap/encoding/", null);
        SOAPHeaderBlock blk1 = header.addHeaderBlock("Header1", ns);
        OMElement h1Value1 = omfactory.createOMElement("string", ns);
        h1Value1.addChild(omfactory.createText("String at header1"));
        OMElement h1Value2 = omfactory.createOMElement("int", ns);
        h1Value2.addChild(omfactory.createText("561565"));
        blk1.addChild(h1Value1);
        blk1.addChild(h1Value2);

        SOAPHeaderBlock blk2 = header.addHeaderBlock("Header2", ns);
        OMElement h2Value1 = omfactory.createOMElement("string", ns);
        h2Value1.addChild(omfactory.createText("String at header2"));
        OMElement h2Value2 = omfactory.createOMElement("int", ns);
        h2Value2.addChild(omfactory.createText("55"));
        blk2.addChild(h2Value2);
        blk2.addChild(h2Value1);

        OMElement operation = omfactory.createOMElement("echoStringParam", "http://soapinterop.org/xsd", null);
        SOAPBody body = omfactory.createSOAPBody(reqEnv);
        body.addChild(operation);
        operation.addChild(omfactory.createText("apache axis2"));
        return reqEnv;

    }


}
