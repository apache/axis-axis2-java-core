package org.apache.axis2.interopt.whitemesa.round2.util.soap12;

import org.apache.axis2.interopt.whitemesa.round2.util.SunRound2ClientUtil;
import org.apache.axis2.soap.*;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.om.OMElement;

/**
 * Created by IntelliJ IDEA.
 * User: Gayan
 * Date: Sep 12, 2005
 * Time: 5:35:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class WMRound2Soap12GroupcIntegerArrayUtil implements SunRound2ClientUtil {

    public SOAPEnvelope getEchoSoapEnvelope() {

        SOAPFactory omfactory = OMAbstractFactory.getSOAP12Factory();
        SOAPEnvelope reqEnv = omfactory.createSOAPEnvelope();
        //reqEnv.declareNamespace("http://schemas.xmlsoap.org/soap/envelope/", "soapenv");
        reqEnv.declareNamespace("http://www.w3.org/2001/XMLSchema", "xsd");
        reqEnv.declareNamespace("http://schemas.xmlsoap.org/soap/encoding/", "SOAP-ENC"); //xmlns:SOAP-ENC="http://schemas.xmlsoap.org/soap/encoding/"
        reqEnv.declareNamespace("http://www.w3.org/2001/XMLSchema-instance", "xsi");
        reqEnv.declareNamespace("http://soapinterop.org/xsd", "s");
        reqEnv.declareNamespace("http://soapinterop.org/", "m");
        reqEnv.declareNamespace("http://soapinterop.org", "m1");
        reqEnv.declareNamespace("http://schemas.xmlsoap.org/wsdl/soap12/", "soap12");

        SOAPHeader header = omfactory.createSOAPHeader(reqEnv);
        OMNamespace hns = reqEnv.declareNamespace("http://soapinterop.org/echoheader/", "hns"); //xmlns:m0="http://soapinterop.org/echoheader/
        SOAPHeaderBlock block1 = header.addHeaderBlock("echoMeStringRequest", hns);
        block1.addAttribute("xsi:type", "xsd:string", null);
        block1.addChild(omfactory.createText("string"));
        // header.addChild(headerChild);
        header.addChild(block1);

        SOAPHeaderBlock block2 = header.addHeaderBlock("echoMeStructRequest", hns);
        block2.addAttribute("xsi:type", "s:SOAPStruct", null);

        OMElement h2Val1 = omfactory.createOMElement("varString", null);
        h2Val1.addAttribute("xsi:type", "xsd:string", null);
        h2Val1.addChild(omfactory.createText("string"));

        OMElement h2Val2 = omfactory.createOMElement("varInt", null);
        h2Val2.addAttribute("xsi:type", "xsd:int", null);
        h2Val2.addChild(omfactory.createText("150"));

        OMElement h2Val3 = omfactory.createOMElement("varFloat", null);
        h2Val3.addAttribute("xsi:type", "xsd:float", null);
        h2Val3.addChild(omfactory.createText("456.321"));

        block2.addChild(h2Val1);
        block2.addChild(h2Val2);
        block2.addChild(h2Val3);

        OMElement operation = omfactory.createOMElement("echoIntegerArray", "http://soapinterop.org/", null);
        SOAPBody body = omfactory.createSOAPBody(reqEnv);
        body.addChild(operation);
        operation.addAttribute("soapenv:encodingStyle", "http://www.w3.org/2003/05/soap-encoding", null);
        OMElement part = omfactory.createOMElement("inputIntegerArray", "", null);
        part.addAttribute("xsi:type", "SOAP-ENC:Array", null);
        part.addAttribute("SOAP-ENC:arrayType", "xsd:int[3]", null);
        OMElement value0 = omfactory.createOMElement("varString", "", null);
        value0.addAttribute("xsi:type", "xsd:int", null);
        value0.addChild(omfactory.createText("451"));
        OMElement value1 = omfactory.createOMElement("varString", "", null);
        value1.addAttribute("xsi:type", "xsd:int", null);
        value1.addChild(omfactory.createText("425"));
        OMElement value2 = omfactory.createOMElement("varString", "", null);
        value2.addAttribute("xsi:type", "xsd:int", null);
        value2.addChild(omfactory.createText("2523"));

        part.addChild(value0);
        part.addChild(value1);
        part.addChild(value2);

        operation.addChild(part);

        //reqEnv.getBody().addChild(method);
        return reqEnv;
    }
}
