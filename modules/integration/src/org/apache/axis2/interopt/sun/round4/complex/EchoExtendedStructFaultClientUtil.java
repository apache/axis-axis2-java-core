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
 * Time: 10:30:10 AM
 * To change this template use File | Settings | File Templates.
 */
public class EchoExtendedStructFaultClientUtil implements SunGroupHClientUtil{

    public OMElement getEchoOMElement() {

        OMFactory fac = OMAbstractFactory.getOMFactory();

        OMNamespace omNs = fac.createOMNamespace("http://soapinterop.org/wsdl", "m");

        OMElement method = fac.createOMElement("echoExtendedStructFault", omNs);
        OMNamespace soapEnvNS = method.declareNamespace(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI,
                SOAP11Constants.SOAP_DEFAULT_NAMESPACE_PREFIX);
        method.addAttribute("encodingStyle", "http://schemas.xmlsoap.org/soap/encoding/", soapEnvNS);



        OMElement value1 = fac.createOMElement("param", null);
        method.addChild(value1);

        OMElement value2 = fac.createOMElement("floatMessage", null);
        OMElement value3 = fac.createOMElement("shortMessage", null);
        OMElement value4 = fac.createOMElement("stringMessage", null);
        OMElement value5 = fac.createOMElement("intMessage", null);
        OMElement value6 = fac.createOMElement("anotherIntMessage", null);

        value1.addChild(value2);
        value1.addChild(value3);
        value1.addChild(value4);
        value1.addChild(value5);
        value1.addChild(value6);

        method.addChild(value1);

        value2.addChild(fac.createText(value2, "0.99"));
        value3.addChild(fac.createText(value3, "10"));
        value4.addChild(fac.createText(value4, "hi"));
        value5.addChild(fac.createText(value5, "1"));
        value6.addChild(fac.createText(value6, "56"));



        return method;
    }



}
