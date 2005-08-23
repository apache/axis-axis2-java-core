package org.apache.axis2.interopt.sun.round4.complex;

import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMFactory;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.soap.SOAP11Constants;

/**
 * Created by IntelliJ IDEA.
 * User: Jaliya
 * Date: Aug 8, 2005
 * Time: 9:26:41 AM
 * To change this template use File | Settings | File Templates.
 */
public class EchoBaseStructFaultClientutil implements SunGroupHClientUtil {

    public OMElement getEchoOMElement() {


        OMFactory fac = OMAbstractFactory.getOMFactory();

        OMNamespace omNs = fac.createOMNamespace("http://soapinterop.org/wsdl", "mns");

        OMElement method = fac.createOMElement("echoBaseStructFault", omNs);
        OMNamespace soapEnvNS = method.declareNamespace(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI,
                SOAP11Constants.SOAP_DEFAULT_NAMESPACE_PREFIX);
        method.addAttribute("encodingStyle", "http://schemas.xmlsoap.org/soap/encoding/", soapEnvNS);

        OMNamespace xsiNs = method.declareNamespace("http://www.w3.org/2001/XMLSchema-instance","xsi");
        OMNamespace ns2 = method.declareNamespace("http://soapinterop.org/types","ns2");
        method.declareNamespace("http://schemas.xmlsoap.org/wsdl/","wsdl");


        OMElement value1 = fac.createOMElement("param", null);
        method.addChild(value1);

        value1.addAttribute("type","ns2:BaseStruct",xsiNs)  ;



        OMElement value2 = fac.createOMElement("floatMessage", null);
        OMElement value3 = fac.createOMElement("shortMessage ", null);

        value1.addChild(value2);
        value1.addChild(value3);
        method.addChild(value1);

        value2.addAttribute("type", "xsd:float", xsiNs);
        value3.addAttribute("type", "xsd:short", xsiNs);

        value2.addChild(fac.createText(value2, "10.3"));
        value3.addChild(fac.createText(value3, "1"));

        return method;
    }

}
