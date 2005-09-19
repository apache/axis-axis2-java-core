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
 * Time: 11:54:36 AM
 * To change this template use File | Settings | File Templates.
 */
public class EchoMultipleFaults2ClientUtil implements SunGroupHClientUtil{

    public OMElement getEchoOMElement() {
        OMFactory fac = OMAbstractFactory.getOMFactory();

        OMNamespace omNs = fac.createOMNamespace("http://soapinterop.org/wsdl", "m");

        OMElement method = fac.createOMElement("echoMultipleFaults2", omNs);
        OMNamespace soapEnvNS = method.declareNamespace(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI,
                SOAP11Constants.SOAP_DEFAULT_NAMESPACE_PREFIX);
        method.addAttribute("encodingStyle", "http://schemas.xmlsoap.org/soap/encoding/", soapEnvNS);



        OMElement value1 = fac.createOMElement("whichFault", null);
        method.addChild(value1);


        OMElement value2 = fac.createOMElement("param1", null);

        OMElement value3 = fac.createOMElement("floatMessage", null);
        OMElement value4 = fac.createOMElement("shortMessage", null);
        OMElement value5 = fac.createOMElement("param2", null);
        OMElement value6 = fac.createOMElement("floatMessage", null);
        OMElement value7 = fac.createOMElement("shortMessage", null);
        OMElement value8 = fac.createOMElement("stringMessage", null);
        OMElement value9 = fac.createOMElement("intMessage", null);
        OMElement value10 = fac.createOMElement("anotherIntMessage", null);
        OMElement value11 = fac.createOMElement("param3", null);
        OMElement value12 = fac.createOMElement("floatMessage", null);
        OMElement value13 = fac.createOMElement("shortMessage", null);
        OMElement value14 = fac.createOMElement("stringMessage", null);
        OMElement value15 = fac.createOMElement("intMessage", null);
        OMElement value16 = fac.createOMElement("anotherIntMessage", null);
        OMElement value17 = fac.createOMElement("booleanMessage", null);


        value2.addChild(value3);
        value2.addChild(value4);
        value5.addChild(value6);
        value5.addChild(value7);
        value5.addChild(value8);
        value5.addChild(value9);
        value5.addChild(value10);
        value11.addChild(value12);
        value11.addChild(value13);
        value11.addChild(value14);
        value11.addChild(value15);
        value11.addChild(value16);
        value11.addChild(value17);


        method.addChild(value2);
        method.addChild(value5);
        method.addChild(value11);


        value1.addChild(fac.createText(value1, "2"));
        value3.addChild(fac.createText(value3, "0.21"));
        value4.addChild(fac.createText(value4, "6"));
        value6.addChild(fac.createText(value6, "0.555"));
        value7.addChild(fac.createText(value7, "9"));
        value8.addChild(fac.createText(value8, "hi"));
        value9.addChild(fac.createText(value9, "10"));

        value10.addChild(fac.createText(value10, "20"));

        value12.addChild(fac.createText(value12, "0.111"));

        value13.addChild(fac.createText(value13, "11"));

        value14.addChild(fac.createText(value14, "hi"));
        value15.addChild(fac.createText(value15, "8"));
        value16.addChild(fac.createText(value16, "9"));
        value17.addChild(fac.createText(value17, "1"));

        return method;
    }








}
