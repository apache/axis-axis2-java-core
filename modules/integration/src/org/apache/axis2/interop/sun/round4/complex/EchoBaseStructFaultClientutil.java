/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.axis2.interop.sun.round4.complex;

import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMFactory;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.soap.SOAP11Constants;

/**
 * Created by IntelliJ IDEA.
 * User: Jaliya
 * Date: Aug 8, 2005
 * Time: 9:26:41 AM
 * To change this template use File | Settings | File Templates.
 */
public class EchoBaseStructFaultClientutil implements SunGroupHClientUtil{

    public OMElement getEchoOMElement() {


        OMFactory fac = OMAbstractFactory.getOMFactory();

        OMNamespace omNs = fac.createOMNamespace("http://soapinterop.org/wsdl", "mns");

        OMElement method = fac.createOMElement("echoBaseStructFault", omNs);
        OMNamespace soapEnvNS = method.declareNamespace(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI,
                SOAP11Constants.SOAP_DEFAULT_NAMESPACE_PREFIX);
        method.addAttribute("encodingStyle", "http://schemas.xmlsoap.org/soap/encoding/", soapEnvNS);



        OMElement value1 = fac.createOMElement("param", null);
        method.addChild(value1);

        OMElement value2 = fac.createOMElement("floatMessage", null);
        OMElement value3 = fac.createOMElement("shortMessage ", null);

        value1.addChild(value2);
        value1.addChild(value3);
        method.addChild(value1);

        value2.addChild(fac.createText(value2, "10.3"));
        value3.addChild(fac.createText(value3, "1"));

        return method;
    }

}
