package org.apache.axis2.interopt.sun.round4.complex;

import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMFactory;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.soap.SOAP11Constants;

/**
 * Created by IntelliJ IDEA.
 * User: Nadana
 * Date: Aug 8, 2005
 * Time: 8:38:19 AM
 * To change this template use File | Settings | File Templates.
 */
public class EchoSOAPStructFaultClientUtil implements SunGroupHClientUtil{

    public OMElement getEchoOMElement() {

        OMFactory fac = OMAbstractFactory.getOMFactory();

        OMNamespace omNs = fac.createOMNamespace("http://soapinterop.org/wsdl", "m");

        OMElement method = fac.createOMElement("echoSOAPStructFault", omNs);
        OMNamespace soapEnvNS = method.declareNamespace(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI,
                SOAP11Constants.SOAP_DEFAULT_NAMESPACE_PREFIX);
        method.addAttribute("encodingStyle", "http://schemas.xmlsoap.org/soap/encoding/", soapEnvNS);


        OMElement value1 = fac.createOMElement("param", null);
        method.addChild(value1);


        OMElement value2 = fac.createOMElement("soapStruct", null);
        OMElement value3 = fac.createOMElement("varInt", null);
        OMElement value4 = fac.createOMElement("varFloat", null);
        OMElement value5 = fac.createOMElement("varString", null);


        value2.addChild(value3);
        value2.addChild(value4);
        value2.addChild(value5);

        value1.addChild(value2);
        value3.addChild(fac.createText(value3, "10"));
        value4.addChild(fac.createText(value4, "0.568"));
        value5.addChild(fac.createText(value5, "Hi"));

        return method;
    }



}
