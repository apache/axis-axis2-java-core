package org.apache.axis2.interopt.whitemesa.round1.util;

import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.SOAPFactory;

/**
 * Created by IntelliJ IDEA.
 * User: Gayan
 * Date: Apr 22, 2005
 * Time: 2:57:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class Round1StringUtil implements Round1ClientUtil {

    public SOAPEnvelope getEchoSoapEnvelope() {
        SOAPFactory omFactory = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope reqEnv = omFactory.getDefaultEnvelope();
        //OMNamespace namespace = reqEnv.declareNamespace("http://sample1.org/sample1", "sample1");
        reqEnv.declareNamespace("http://soapinterop.org/", "ns1");
        reqEnv.declareNamespace("http://schemas.xmlsoap.org/soap/encoding/", "SOAP-ENC");
        reqEnv.declareNamespace("http://www.w3.org/1999/XMLSchema-instance/", "xsi");
        reqEnv.declareNamespace("http://www.w3.org/1999/XMLSchema", "xsd");


        OMElement method = omFactory.createOMElement("echoString", "http://soapinterop.org/", null);
        reqEnv.getBody().addChild(method);
        method.addAttribute("encordingStyle", "http://schemas.xmlsoap.org/soap/encoding/", null);

        OMElement value = omFactory.createOMElement("inputString", "http://soapinterop.org/", null);
        value.addAttribute("xsi:type", "xsd:string", null);
        value.addChild(omFactory.createText("Lanka Software Foundation"));
        method.addChild(value);

        return reqEnv;
    }

}
