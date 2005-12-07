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

package org.apache.axis2.rest;


import junit.framework.TestCase;
import org.apache.axis2.Constants;
import org.apache.axis2.client.Call;
import org.apache.axis2.client.Options;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.ParameterImpl;
import org.apache.axis2.engine.Echo;
import org.apache.axis2.engine.util.TestConstants;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMFactory;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.util.Utils;

import javax.xml.stream.XMLOutputFactory;


public class RESTBasedEchoRawXMLTest extends TestCase implements TestConstants {

    private AxisService service;

    public RESTBasedEchoRawXMLTest() {
        super(RESTBasedEchoRawXMLTest.class.getName());
    }

    public RESTBasedEchoRawXMLTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        UtilServer.start();
        Parameter parameter = new ParameterImpl(
                Constants.Configuration.ENABLE_REST, "true");
        UtilServer.getConfigurationContext()
                .getAxisConfiguration().addParameter(parameter);
        service =
                Utils.createSimpleService(serviceName,
                        Echo.class.getName(),
                        operationName);
        UtilServer.deployService(service);
    }

    protected void tearDown() throws Exception {
        UtilServer.unDeployService(serviceName);
        UtilServer.stop();
    }

    private OMElement createEnvelope() {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://localhost/my", "my");
        OMElement method = fac.createOMElement("echoOMElement", omNs);
        OMElement value = fac.createOMElement("myValue", omNs);
        value.addChild(
                fac.createText(value, "Isaac Assimov, the foundation Sega"));
        method.addChild(value);

        return method;
    }


    public void testEchoXMLSync() throws Exception {
        OMElement payload = createEnvelope();

        Call call = new Call("target/test-resources/integrationRepo");

        Options options = new Options();
        options.setTo(targetEPR);
        options.setListenerTransportProtocol(Constants.TRANSPORT_HTTP);
        options.setProperty(Constants.Configuration.ENABLE_REST, Constants.VALUE_TRUE);

        call.setClientOptions(options);
        OMElement result =
                call.invokeBlocking(operationName.getLocalPart(),
                        payload);
        result.serialize(XMLOutputFactory.newInstance().createXMLStreamWriter(
                System.out));


        call.close();
    }
}
