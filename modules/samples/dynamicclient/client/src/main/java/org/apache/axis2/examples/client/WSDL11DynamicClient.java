/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.examples.client;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.ServiceClient;

public class WSDL11DynamicClient {

    public static void main(String[] args) throws MalformedURLException, AxisFault {

        URL wsdlURL = new URL("http://localhost:8080/axis2/services/CalculatorService?wsdl");
        QName serviceName = new QName("http://server.examples.axis2.apache.org", "CalculatorService");
        String port = "CalculatorServiceHttpEndpoint";
        ServiceClient serviceClient = new ServiceClient(null, wsdlURL, serviceName, port);
        OMElement res = serviceClient.sendReceive(new QName(
                "http://server.examples.axis2.apache.org", "add"), creatMsg());
        System.out.println(res);

    }

    public static OMElement creatMsg() {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://server.examples.axis2.apache.org", "ns1");
        // creating the payload
        OMElement method = fac.createOMElement("add", omNs);
        OMElement value1 = fac.createOMElement("value1", omNs);
        OMElement value2 = fac.createOMElement("value2", omNs);

        value1.setText("4");
        value2.setText("3");

        method.addChild(value1);
        method.addChild(value2);

        return method;
    }

}
