package org.apache.axis2.interopt.whitemesa.round1.util;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.soap.SOAPBody;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.SOAPFactory;

/**
 * Created by IntelliJ IDEA.
 * User: Gayan
 * Date: Aug 14, 2005
 * Time: 2:09:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class Round1StringArrayUtil implements Round1ClientUtil{

    public SOAPEnvelope getEchoSoapEnvelope() {

        SOAPFactory omfactory = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope reqEnv = omfactory.createSOAPEnvelope();
        reqEnv.declareNamespace("http://schemas.xmlsoap.org/soap/envelope/", "soapenv");
        reqEnv.declareNamespace("http://www.w3.org/1999/XMLSchema-instance/", "xsi");
        reqEnv.declareNamespace("http://schemas.xmlsoap.org/wsdl/soap/", "soap");
        reqEnv.declareNamespace("http://www.w3.org/2001/XMLSchema", "xsd");
        reqEnv.declareNamespace("http://schemas.xmlsoap.org/soap/encoding/", "SOAPENC");
        reqEnv.declareNamespace("http://soapinterop.org/", "tns");
        reqEnv.declareNamespace("http://soapinterop.org/xsd", "s");
        reqEnv.declareNamespace("http://schemas.xmlsoap.org/wsdl/", "wsdl");

        OMElement operation = omfactory.createOMElement("echoStringArray", "http://soapinterop.org/", null);
        SOAPBody body = omfactory.createSOAPBody(reqEnv);
        body.addChild(operation);
        operation.addAttribute("soapenv:encodingStyle", "http://schemas.xmlsoap.org/soap/encoding/", null);

        OMElement part = omfactory.createOMElement("inputStringArray", null);
        part.addAttribute("xsi:type", "SOAPENC:Array", null);
        part.addAttribute("SOAPENC:arrayType", "xsd:string[3]", null);

        OMElement value0 = omfactory.createOMElement("item", "", null);
        value0.addAttribute("xsi:type", "xsd:string", null);
        value0.addChild(omfactory.createText("Apache Axis2"));
        OMElement value1 = omfactory.createOMElement("item", "", null);
        value1.addAttribute("xsi:type", "xsd:string", null);
        value1.addChild(omfactory.createText("Lanka Software Foundation"));
        OMElement value2 = omfactory.createOMElement("item", "", null);
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
