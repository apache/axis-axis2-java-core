package org.apache.axis2.interopt.sun.round4.simple.util;

import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMFactory;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.soap.SOAP11Constants;

/**
 * Created by IntelliJ IDEA.
 * User: Nadana
 * Date: Aug 6, 2005
 * Time: 3:25:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class EchoIntArrayFaultClientUtil implements SunGroupHClientUtil {


    public OMElement getEchoOMElement() {
        OMFactory fac = OMAbstractFactory.getOMFactory();

        OMNamespace omNs = fac.createOMNamespace("http://soapinterop.org/wsdl", "m");

        OMElement method = fac.createOMElement("echoIntArrayFault", omNs);
        OMNamespace soapEnvNS = method.declareNamespace(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI,
                SOAP11Constants.SOAP_DEFAULT_NAMESPACE_PREFIX);
        method.addAttribute("encodingStyle", "http://schemas.xmlsoap.org/soap/encoding/", soapEnvNS);


        OMElement value1 = fac.createOMElement("param", null);
        method.addChild(value1);
        value1.addAttribute("soapenc:arrayType","nsa:int[3]",null);
        value1.addAttribute("soapenc:offset","[0]",null);
        value1.addAttribute("xmlns:soapenc","http://schemas.xmlsoap.org/soap/encoding/",null);
        value1.addAttribute("xmlns:nsa","http://www.w3.org/2001/XMLSchema",null);
        OMElement value2 = fac.createOMElement("Item", null);
        OMElement value3 = fac.createOMElement("Item", null);
        OMElement value4 = fac.createOMElement("Item", null);

        value1.addChild(value2);
        value1.addChild(value3);
        value1.addChild(value4);




        value2.addChild(fac.createText(value2, "99"));
        value3.addChild(fac.createText(value3, "10"));
        value4.addChild(fac.createText(value4, "12"));




        return method;
    }






}
