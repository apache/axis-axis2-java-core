package org.apache.axis2.interopt.whitemesa.round2.util.soap12;

import org.apache.axis2.interopt.whitemesa.round2.util.SunRound2ClientUtil;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.soap.SOAPBody;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.SOAPFactory;

/**
 * Created by IntelliJ IDEA.
 * User: Gayan
 * Date: Sep 5, 2005
 * Time: 5:18:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class Round2Soap12EchoBooleanClientUtil implements SunRound2ClientUtil {

    public SOAPEnvelope getEchoSoapEnvelope() {

        SOAPFactory omfactory = OMAbstractFactory.getSOAP12Factory();
        SOAPEnvelope reqEnv = omfactory.createSOAPEnvelope();
        //reqEnv.declareNamespace("http://schemas.xmlsoap.org/soap/envelope/", "soapenv");
        //reqEnv.declareNamespace("http://schemas.xmlsoap.org/wsdl/", "xmlns");
        //reqEnv.declareNamespace("http://schemas.xmlsoap.org/wsdl/soap/", "soap");
        reqEnv.declareNamespace("http://www.w3.org/2001/XMLSchema", "xsd");
        reqEnv.declareNamespace("http://schemas.xmlsoap.org/soap/encoding/", "SOAP-ENC");
        reqEnv.declareNamespace("http://soapinterop.org/", "tns");
        reqEnv.declareNamespace("http://soapinterop.org/xsd", "s");
        //reqEnv.declareNamespace("http://schemas.xmlsoap.org/wsdl/", "wsdl");
        reqEnv.declareNamespace("http://www.w3.org/2001/XMLSchema-instance", "xsi");

        OMElement operation = omfactory.createOMElement("echoBoolean", "http://soapinterop.org/", null);
        SOAPBody body = omfactory.createSOAPBody(reqEnv);
        body.addChild(operation);
        operation.addAttribute("soapenv:encodingStyle", "http://www.w3.org/2003/05/soap-encoding", null);

        OMElement part = omfactory.createOMElement("inputBoolean", "", null);
        part.addAttribute("xsi:type", "xsd:boolean", null);
        part.addChild(omfactory.createText("true"));

        operation.addChild(part);
        //reqEnv.getBody().addChild(method);
        return reqEnv;

    }
}
