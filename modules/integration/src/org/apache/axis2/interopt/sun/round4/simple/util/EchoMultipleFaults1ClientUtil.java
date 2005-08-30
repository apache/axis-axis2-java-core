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
 * Time: 4:28:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class EchoMultipleFaults1ClientUtil implements SunGroupHClientUtil {

    public OMElement getEchoOMElement() {

        OMFactory fac = OMAbstractFactory.getOMFactory();

        OMNamespace omNs = fac.createOMNamespace("http://soapinterop.org/wsdl", "m");

        OMElement method = fac.createOMElement("echoMultipleFaults1", omNs);
        OMNamespace soapEnvNS = method.declareNamespace(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI,
                SOAP11Constants.SOAP_DEFAULT_NAMESPACE_PREFIX);
        method.addAttribute("encodingStyle", "http://schemas.xmlsoap.org/soap/encoding/", soapEnvNS);

        OMNamespace xsiNs = method.declareNamespace("http://www.w3.org/2001/XMLSchema-instance","xsi");
        OMNamespace ns2 = method.declareNamespace("http://soapinterop.org/types","ns2");
        method.declareNamespace("http://schemas.xmlsoap.org/wsdl/","wsdl");

        OMElement value = fac.createOMElement("whichFault", null);
        method.addChild(value);
        OMElement value1 = fac.createOMElement("param1", null);
        OMElement value2 = fac.createOMElement("param2", null);
        OMElement value3 = fac.createOMElement("Item", null);
        OMElement value4 = fac.createOMElement("Item", null);
        OMElement value5 = fac.createOMElement("Item", null);

        value2.addAttribute("soapenc:arrayType","nsa:float[3]",null);
        value2.addAttribute("soapenc:offset","[0]",null);
        value2.addAttribute("xmlns:soapenc","http://schemas.xmlsoap.org/soap/encoding/",null);
        value2.addAttribute("xmlns:nsa","http://www.w3.org/2001/XMLSchema",null);


        value.addChild(fac.createText(value, "10"));
        value1.addChild(fac.createText(value1, "hi"));
        value3.addChild(fac.createText(value3, "1.0"));
        value4.addChild(fac.createText(value4, "20.6"));
        value5.addChild(fac.createText(value5, "2.6"));

        value2.addChild(value3);
        value2.addChild(value4);
        value2.addChild(value5);


        method.addChild(value1);
        method.addChild(value2);



        return method;
    }


}
