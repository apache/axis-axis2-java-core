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
 * Time: 11:08:12 AM
 * To change this template use File | Settings | File Templates.
 */
public class EchoMultipleFaults1ClientUtil implements SunGroupHClientUtil{

    public OMElement getEchoOMElement() {

        OMFactory fac = OMAbstractFactory.getOMFactory();

        OMNamespace omNs = fac.createOMNamespace("http://soapinterop.org/wsdl", "m");

        OMElement method = fac.createOMElement("echoMultipleFaults1", omNs);
        OMNamespace soapEnvNS = method.declareNamespace(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI,
                SOAP11Constants.SOAP_DEFAULT_NAMESPACE_PREFIX);
        method.addAttribute("encodingStyle", "http://schemas.xmlsoap.org/soap/encoding/", soapEnvNS);

        OMElement value1 = fac.createOMElement("whichFault", null);
        method.addChild(value1);


        OMElement value2 = fac.createOMElement("param1", null);
        OMElement value3 = fac.createOMElement("varInt", null);
        OMElement value4 = fac.createOMElement("varFloat", null);
        OMElement value5 = fac.createOMElement("varString", null);
        OMElement value6 = fac.createOMElement("param2", null);
        OMElement value7 = fac.createOMElement("floatMessage", null);
        OMElement value8 = fac.createOMElement("shortMessage", null);


        value2.addChild(value3);
        value2.addChild(value4);
        value2.addChild(value5);

        value6.addChild(value7);
        value6.addChild(value8);


        method.addChild(value2);
        method.addChild(value6);

        value1.addChild(fac.createText(value1, "1"));
        value3.addChild(fac.createText(value3, "210"));
        value4.addChild(fac.createText(value4, "0.256"));
        value5.addChild(fac.createText(value5, "hi"));
        value7.addChild(fac.createText(value7, "0.569"));
        value8.addChild(fac.createText(value8, "56"));



        return method;


    }




}
