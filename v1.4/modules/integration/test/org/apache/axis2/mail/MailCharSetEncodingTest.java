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

package org.apache.axis2.mail;

import junit.framework.TestCase;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.async.AsyncResult;
import org.apache.axis2.client.async.Callback;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.OutInAxisOperation;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.Echo;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.transport.mail.SimpleMailListener;
import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;

/** These tests willcheck wheather the mail transport works ok with charactor set encoding changes. */
public class MailCharSetEncodingTest extends TestCase {

    private EndpointReference targetEPR = new EndpointReference("mail:foo@127.0.0.1"
            + "/axis2/services/EchoXMLService/echoOMElement");

    private static final Log log = LogFactory.getLog(MailCharSetEncodingTest.class);

    private String stringServiceName = "EchoXMLService";

    private QName serviceName = new QName(stringServiceName);

    private QName operationName = new QName("echoOMElement");

    OMElement resultElem = null;

    private AxisConfiguration engineRegistry;

    private SOAPEnvelope envelope;

    private boolean finish = false;

    ServiceContext clientServiceContext;

    ConfigurationContext clientConfigContext;

    ConfigurationContext serverConfigContext;

    public MailCharSetEncodingTest() {
        super(MailCharSetEncodingTest.class.getName());
    }

    protected void setUp() throws Exception {
        serverConfigContext = UtilsMailServer.start();
        if (serverConfigContext.getAxisConfiguration().getService(stringServiceName) == null) {
            AxisService service = Utils.createSimpleService(serviceName,
                                                            Echo.class.getName(), operationName);
            serverConfigContext.getAxisConfiguration().addService(service);
        }

        SimpleMailListener ml = new SimpleMailListener();

        ml.init(serverConfigContext, serverConfigContext.getAxisConfiguration()
                .getTransportIn(Constants.TRANSPORT_MAIL));
        ml.start();

    }

    protected void tearDown() throws Exception {
        UtilsMailServer.stop();
    }

    public void runTest(String value) throws Exception {
        finish = false;
        resultElem = null;
        envelope = null;
        ConfigurationContext configContext = UtilsMailServer
                .createClientConfigurationContext();
        AxisService service = null;
        AxisOperation axisOperation = null;
        if (configContext.getAxisConfiguration().getService(stringServiceName) != null) {
            configContext.getAxisConfiguration().removeService(stringServiceName);
        }
        service = new AxisService(serviceName.getLocalPart());
        axisOperation = new OutInAxisOperation();
        axisOperation.setName(operationName);
        axisOperation.setMessageReceiver(new MessageReceiver() {
            public void receive(MessageContext messageCtx) {
                envelope = messageCtx.getEnvelope();
            }
        });
        service.addOperation(axisOperation);
        configContext.getAxisConfiguration().addService(service);

        Options options = new Options();
        options.setTo(targetEPR);
        options.setAction(operationName.getLocalPart());
        options.setTransportInProtocol(Constants.TRANSPORT_MAIL);
        options.setUseSeparateListener(true);

        Callback callback = new Callback() {
            public void onComplete(AsyncResult result) {
                resultElem = result.getResponseEnvelope();
                finish = true;
            }

            public void onError(Exception e) {
                log.info(e.getMessage());
                finish = true;
            }
        };

        ServiceClient sender = new ServiceClient(configContext, service);
        sender.setOptions(options);
        //options.setTo(targetEPR);
        sender.sendReceiveNonBlocking(operationName, createEnvelope(value), callback);

        int index = 0;
        while (!finish) {
            Thread.sleep(1000);
            index++;
            if (index > 10) {
                throw new AxisFault(
                        "Server was shutdown as the async response is taking too long to complete.");
            }
        }
        assertNotNull("Result is null", resultElem);
        String result = ((OMElement)resultElem.getFirstOMChild()
                .getNextOMSibling()).getFirstElement().getFirstElement()
                .getText();

        assertNotNull("Result value is null", result);
        assertEquals("Expected result not received.", value, result);

    }

    public void testSimpleString() throws Exception {
        runTest("a simple string");
    }

    public void testStringWithApostrophes() throws Exception {
        runTest("this isn't a simple string");
    }

    public void testStringWithEntities() throws Exception {
        runTest("&amp;&lt;&gt;&apos;&quot;");
    }

    public void testStringWithRawEntities() throws Exception {
        runTest("&<>'\"");
    }

    public void testStringWithLeadingAndTrailingSpaces() throws Exception {
        runTest("          centered          ");
    }

    private OMElement createEnvelope(String text) {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://localhost/my", "my");
        OMElement method = fac.createOMElement("echoOMElement", omNs);
        OMElement value = fac.createOMElement("myValue", omNs);
        value.addChild(fac.createOMText(value, text));
        method.addChild(value);

        return method;
    }
}