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

package org.apache.axis2.mtom;

import junit.framework.TestCase;
import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.Echo;
import org.apache.axis2.engine.util.TestConstants;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.om.*;
import org.apache.axis2.om.impl.llom.OMTextImpl;
import org.apache.axis2.soap.SOAP12Constants;
import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.activation.DataHandler;

public class EchoRawMTOMLoadTest extends TestCase implements TestConstants {

    private Log log = LogFactory.getLog(getClass());

    private ServiceContext serviceContext;

    private AxisService service;

    private OMText textData;


    byte[] expectedByteArray;

    public EchoRawMTOMLoadTest() {
        super(EchoRawMTOMLoadTest.class.getName());
    }

    public EchoRawMTOMLoadTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        UtilServer.start(Constants.TESTING_PATH + "MTOM-enabledRepository");
        service = Utils.createSimpleService(serviceName, Echo.class.getName(),
                operationName);
        UtilServer.deployService(service);
    }

    protected void tearDown() throws Exception {
        UtilServer.unDeployService(serviceName);
        UtilServer.stop();
        UtilServer.unDeployClientService();
    }


    protected OMElement createEnvelope() {

        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://localhost/my", "my");
        OMElement rpcWrapEle = fac.createOMElement("echoOMElement", omNs);
        OMElement data = fac.createOMElement("data", omNs);
        expectedByteArray = new byte[]{13, 56, 65, 32, 12, 12, 7, -3, -2, -1,
                98};
        for (int i = 0; i < 4; i++) {
            OMElement subData = fac.createOMElement("subData", omNs);
            DataHandler dataHandler = new DataHandler("Thilina", "text/plain");
            //new ByteArrayDataSource(expectedByteArray));
            textData = new OMTextImpl(dataHandler, true);
            subData.addChild(textData);
            data.addChild(subData);

        }

        rpcWrapEle.addChild(data);
        return rpcWrapEle;
    }

    public void testEchoXMLSync() throws Exception {
        for (int i = 0; i < 10; i++) {
            OMElement payload = createEnvelope();
            Options options = new Options();
            options.setTo(targetEPR);
            options.setProperty(Constants.Configuration.ENABLE_MTOM,
                    Constants.VALUE_TRUE);
            options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
            options.setSoapVersionURI(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);

            ConfigurationContextFactory factory = new ConfigurationContextFactory();
            ConfigurationContext configContext =
                    factory.buildConfigurationContext("target/test-resources/integrationRepo");
            ServiceClient sender = new ServiceClient(configContext);
            sender.setOptions(options);
            OMElement result = sender.sendReceive(payload);

            OMElement ele = (OMElement) result.getFirstOMChild();
            OMElement ele1 = (OMElement) ele.getFirstOMChild();
            OMText binaryNode = (OMText) ele1.getFirstOMChild();
            compareWithActualOMText(binaryNode);
            log.info("" + i);
            sender.finalizeInvoke();
            UtilServer.unDeployClientService();
        }
    }

    protected void compareWithActualOMText(OMText binaryNode) {
        assertEquals(textData.getText(), binaryNode.getText());
    }

}