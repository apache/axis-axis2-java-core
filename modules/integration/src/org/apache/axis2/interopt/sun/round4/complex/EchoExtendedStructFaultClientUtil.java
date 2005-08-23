package org.apache.axis2.interopt.sun.round4.complex;

import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMFactory;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMNamespace;

/**
 * Created by IntelliJ IDEA.
 * User: Nadana
 * Date: Aug 8, 2005
 * Time: 10:30:10 AM
 * To change this template use File | Settings | File Templates.
 */
public class EchoExtendedStructFaultClientUtil implements SunGroupHClientUtil {

    public OMElement getEchoOMElement() {

        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://soapinterop.org/wsdl", "mns");

        OMElement method = fac.createOMElement("echoExtendedStructFault", omNs);
        method.addAttribute("soapenv:encodingStyle", "http://schemas.xmlsoap.org/soap/encoding/", null);
        method.addAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance", null);
        method.addAttribute("xmlns:xsd", "http://www.w3.org/2001/XMLSchema", null);
        method.addAttribute("xmlns:ns2", "http://soapinterop.org/types", null);
        method.addAttribute("xmlns:wsdl", "http://schemas.xmlsoap.org/wsdl/", null);


        OMElement value1 = fac.createOMElement("param", null);
        OMElement value2 = fac.createOMElement("stringMessage", null);
        OMElement value3 = fac.createOMElement("intMessage", null);
        OMElement value4 = fac.createOMElement("anotherIntMessage", null);

        value1.addAttribute("xsi:type", "ns2:ExtendedStruct", null);
        value2.addAttribute("xsi:type", "xsd:string", null);
        value3.addAttribute("xsi:type", "xsd:int", null);
        value4.addAttribute("xsi:type", "xsd:int", null);

        value2.addChild(fac.createText(value2, "hi"));
        value3.addChild(fac.createText(value3, "1"));
        value4.addChild(fac.createText(value4, "155"));

        value1.addChild(value2);
        value1.addChild(value3);
        value1.addChild(value4);
        method.addChild(value1);


        return method;
    }


}
