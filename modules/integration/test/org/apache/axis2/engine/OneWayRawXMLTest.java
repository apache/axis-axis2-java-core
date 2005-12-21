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

package org.apache.axis2.engine;

//todo

import junit.framework.TestCase;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.MessageSender;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.OutInAxisOperation;
import org.apache.axis2.engine.util.TestConstants;
import org.apache.axis2.integration.TestingUtils;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.SOAPFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class OneWayRawXMLTest extends TestCase implements TestConstants {

    private Log log = LogFactory.getLog(getClass());

    private SOAPEnvelope envelope;

    private boolean finish = false;

    public OneWayRawXMLTest() {
        super(OneWayRawXMLTest.class.getName());
    }

    public OneWayRawXMLTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        UtilServer.start();

        AxisService service = new AxisService(serviceName.getLocalPart());
        AxisOperation axisOperation = new OutInAxisOperation(
                operationName);
        axisOperation.setMessageReceiver(new MessageReceiver() {
            public void receive(MessageContext messgeCtx) throws AxisFault {
                envelope = messgeCtx.getEnvelope();
                TestingUtils.campareWithCreatedOMElement(
                        envelope.getBody().getFirstElement());
            }
        });
        service.addOperation(axisOperation);
        UtilServer.deployService(service);
    }


    protected void tearDown() throws Exception {
        UtilServer.unDeployService(serviceName);
        UtilServer.stop();
    }


    public void testEchoXMLSync() throws Exception {
        SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();

        OMElement payload = TestingUtils.createDummyOMElement();

//        MessageSender sender = new MessageSender("target/test-resources/integrationRepo");
        ConfigurationContextFactory factory = new ConfigurationContextFactory();
        ConfigurationContext configContext =
                factory.buildClientConfigurationContext("target/test-resources/integrationRepo");
        ServiceClient sender = new ServiceClient(configContext);

        Options options = new Options();
        sender.setOptions(options);
//        sender.setClientOptions(options);
        options.setTo(targetEPR);
        sender.fireAndForget(payload);
//        sender.send(operationName.getLocalPart(), payload);
        int index = 0;
        while (envelope == null) {
            Thread.sleep(4000);
            index++;
            if (index == 5) {
                throw new AxisFault("error Occured");
            }
        }
    }

}
