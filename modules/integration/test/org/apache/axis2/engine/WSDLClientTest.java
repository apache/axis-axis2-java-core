package org.apache.axis2.engine;

import junit.framework.TestCase;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.util.TestConstants;
import org.apache.axis2.integration.TestingUtils;
import org.apache.axis2.integration.UtilServer;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.net.URL;
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
*
* @author : Deepal Jayasinghe (deepal@apache.org)
*
*/

public class WSDLClientTest extends TestCase implements TestConstants {

    protected AxisService service;

    protected void setUp() throws Exception {
        UtilServer.start();
        service = AxisService.createService(Echo.class.getName(),
                UtilServer.getConfigurationContext().getAxisConfiguration());
        service.setName(serviceName.getLocalPart());
        UtilServer.deployService(service);
    }

    protected void tearDown() throws Exception {
        UtilServer.unDeployService(serviceName);
        UtilServer.stop();
        UtilServer.unDeployClientService();
    }

    public void testWSDLClient() throws AxisFault {
        try {
            URL wsdlURL = new URL("http://localhost:" + UtilServer.TESTING_PORT +
                    "/axis2/services/EchoXMLService?wsdl");
            ServiceClient serviceClient = new ServiceClient(null, wsdlURL,
                    new QName("http://org.apache.axis2/", "EchoXMLService"),
                    "EchoXMLServicePortType0");
            OMElement payload = TestingUtils.createDummyOMElement();
            OMElement response = serviceClient.sendReceive(
                    new QName("http://org.apache.axis2/xsd", "echoOMElement"), payload);
            assertNotNull(response);
            String textValue = response.getText();
            assertEquals(textValue, "Isaac Asimov, The Foundation Trilogy");
        } catch (IOException e) {
            throw new AxisFault(e);
        }
    }

}
