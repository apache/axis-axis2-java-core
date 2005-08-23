package org.apache.axis2.interopt.sun.round4.complex;

import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMFactory;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.soap.SOAP11Constants;

/**
 * Created by IntelliJ IDEA.
 * User: Nadana
 * Date: Aug 8, 2005
 * Time: 8:38:19 AM
 * To change this template use File | Settings | File Templates.
 */
public class EchoSOAPStructFaultClientUtil implements SunGroupHClientUtil {

    public OMElement getEchoOMElement() {

        OMFactory fac = OMAbstractFactory.getOMFactory();

        OMNamespace omNs = fac.createOMNamespace("http://soapinterop.org/wsdl", "mns");

        OMElement method = fac.createOMElement("echoSOAPStructFault", omNs);
        OMNamespace soapEnvNS = method.declareNamespace(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI,
                SOAP11Constants.SOAP_DEFAULT_NAMESPACE_PREFIX);
        method.addAttribute("encodingStyle", "http://schemas.xmlsoap.org/soap/encoding/", soapEnvNS);

        OMNamespace xsiNs = method.declareNamespace("http://www.w3.org/2001/XMLSchema-instance","xsi");
        OMNamespace ns2 = method.declareNamespace("http://soapinterop.org/types","ns2");
        method.declareNamespace("http://schemas.xmlsoap.org/wsdl/","wsdl");


        OMElement value1 = fac.createOMElement("param", null);
        method.addChild(value1);

        value1.addAttribute("type","ns2:SOAPStructFault",xsiNs)  ;

        return method;
    }


}
