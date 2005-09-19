package org.apache.axis2.interopt.sun.round4.simple.util;

import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMFactory;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.soap.SOAP11Constants;

/**
 * Created by IntelliJ IDEA.
 * User: Nadana
 * Date: Aug 8, 2005
 * Time: 8:06:26 AM
 * To change this template use File | Settings | File Templates.
 */
public class EchoMultipleFaults3Clientutil implements SunGroupHClientUtil{
    public OMElement getEchoOMElement() {

        OMFactory fac = OMAbstractFactory.getOMFactory();

        OMNamespace omNs = fac.createOMNamespace("http://soapinterop.org/wsdl", "m");

        OMElement method = fac.createOMElement("echoMultipleFaults3", omNs);
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



        value.addChild(fac.createText(value, "3"));
        value1.addChild(fac.createText(value1, "param 1"));
        value2.addChild(fac.createText(value2, "Param 2"));





        method.addChild(value1);
        method.addChild(value2);


        return method;
    }


}
