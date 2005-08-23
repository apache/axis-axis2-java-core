package org.apache.axis2.interopt.sun.round4.complex;

import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMFactory;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMNamespace;

/**
 * Created by IntelliJ IDEA.
 * User: Nadana
 * Date: Aug 8, 2005
 * Time: 11:54:36 AM
 * To change this template use File | Settings | File Templates.
 */
public class EchoMultipleFaults2ClientUtil implements SunGroupHClientUtil {

    public OMElement getEchoOMElement() {

        OMFactory fac = OMAbstractFactory.getOMFactory();

        OMNamespace omNs = fac.createOMNamespace("http://soapinterop.org/wsdl", "mns");

        OMElement method = fac.createOMElement("echoMultipleFaults2", omNs);
        method.addAttribute("soapenv:encodingStyle", "http://schemas.xmlsoap.org/soap/encoding/", null);
        method.addAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance", null);
        method.addAttribute("xmlns:xsd", "http://www.w3.org/2001/XMLSchema", null);
        method.addAttribute("xmlns:ns2", "http://soapinterop.org/types", null);
        method.addAttribute("xmlns:wsdl", "http://schemas.xmlsoap.org/wsdl/", null);


        OMElement value1 = fac.createOMElement("whichFault", null);
        OMElement value2 = fac.createOMElement("param1", null);
        OMElement value3 = fac.createOMElement("param2", null);
        OMElement value4 = fac.createOMElement("param3", null);

        value1.addAttribute("xsi:type", "xsd:int", null);
        value2.addAttribute("xsi:type", "ns2:BaseStruct", null);
        value3.addAttribute("xsi:type", "ns2:ExtendedStruct", null);
        value4.addAttribute("xsi:type", "ns2:MoreExtendedStruct", null);


        value1.addChild(fac.createText(value1, "10"));

        method.addChild(value1);
        method.addChild(value2);
        method.addChild(value3);
        method.addChild(value4);


        return method;
    }


}
