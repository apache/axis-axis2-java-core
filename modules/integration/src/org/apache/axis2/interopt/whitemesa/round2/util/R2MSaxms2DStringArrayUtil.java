package org.apache.axis2.interopt.whitemesa.round2.util;

import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.SOAPFactory;
import org.apache.axis2.soap.SOAPBody;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.om.OMElement;

/**
 * Created by IntelliJ IDEA.
 * User: Gayan
 * Date: Aug 29, 2005
 * Time: 12:23:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class R2MSaxms2DStringArrayUtil implements SunRound2ClientUtil{

    public SOAPEnvelope getEchoSoapEnvelope() {

        SOAPFactory omfactory = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope reqEnv = omfactory.createSOAPEnvelope();
        OMNamespace namespace = reqEnv.declareNamespace("http://schemas.xmlsoap.org/soap/envelope/", "soapenv");
        OMNamespace namespace0 = reqEnv.declareNamespace("http://schemas.xmlsoap.org/wsdl/", "xmlns");
        OMNamespace namespace1 = reqEnv.declareNamespace("http://schemas.xmlsoap.org/wsdl/soap/", "soap");
        OMNamespace namespace2 = reqEnv.declareNamespace("http://www.w3.org/2001/XMLSchema", "xsd");
        OMNamespace namespace3 = reqEnv.declareNamespace("http://schemas.xmlsoap.org/soap/encoding/", "SOAP-ENC");
        OMNamespace namespace4 = reqEnv.declareNamespace("http://soapinterop.org/", "tns");
        OMNamespace namespace5 = reqEnv.declareNamespace("http://soapinterop.org/xsd", "s");
        OMNamespace namespace6 = reqEnv.declareNamespace("http://schemas.xmlsoap.org/wsdl/", "wsdl");
        reqEnv.declareNamespace("http://www.w3.org/2001/XMLSchema-instance","xsi");

        OMElement operation = omfactory.createOMElement("echo2DStringArray", "http://soapinterop.org/", null);
        SOAPBody body = omfactory.createSOAPBody(reqEnv);
        body.addChild(operation);
        operation.addAttribute("soapenv:encodingStyle", "http://schemas.xmlsoap.org/soap/encoding/", null);

        OMElement part = omfactory.createOMElement("input2DStringArray", "", null);
        part.addAttribute("xsi:type", "SOAP-ENC:Array", null);
        part.addAttribute("SOAP-ENC:arrayType", "xsd:string[2]", null);

        OMElement value0 = omfactory.createOMElement("varString", "", null);
        value0.addAttribute("xsi:type", "xsd:string", null);
        value0.addChild(omfactory.createText("Apache Axis2"));
        OMElement value1 = omfactory.createOMElement("varString", "", null);
        value1.addAttribute("xsi:type", "xsd:string", null);
        value1.addChild(omfactory.createText("Lanka Software Foundation"));
        OMElement value2 = omfactory.createOMElement("varString", "", null);
        value2.addAttribute("xsi:type", "xsd:string", null);
        value2.addChild(omfactory.createText("www.opensource.lk"));

        part.addChild(value0);
        part.addChild(value1);
        part.addChild(value2);

        operation.addChild(part);
        //reqEnv.getBody().addChild(method);
        return reqEnv;
    }
}
