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

//todo

import junit.framework.TestCase;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.OutInAxisOperation;
import org.apache.axis2.description.OutOnlyAxisOperation;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.transport.mail.SimpleMailListener;

import javax.xml.namespace.QName;

public class MailOneWayRawXMLTest extends TestCase {
    private EndpointReference targetEPR =
            new EndpointReference("mail:foo@127.0.0.1" +
                    "/axis2/services/EchoXMLService/echoOMElement");
    private QName serviceName = new QName("EchoXMLService");
    private QName operationName = new QName("echoOMElement");


    private ConfigurationContext clientConfigContext;

    public MailOneWayRawXMLTest() {
        super(MailOneWayRawXMLTest.class.getName());
    }

    public MailOneWayRawXMLTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        //start the mail server      
        ConfigurationContext configContext = UtilsMailServer.start();
        clientConfigContext = UtilsMailServer.createClientConfigurationContext();

        SimpleMailListener ml = new SimpleMailListener();
        ml.init(configContext,
                configContext.getAxisConfiguration().getTransportIn(Constants.TRANSPORT_MAIL));
        ml.start();

        AxisService service = new AxisService(serviceName.getLocalPart());
        AxisOperation axisOperation = new OutInAxisOperation(
        );
        axisOperation.setName(operationName);
        axisOperation.setMessageReceiver(new MessageReceiver() {
            public void receive(MessageContext messageCtx) {
                messageCtx.getEnvelope();
            }
        });
        service.addOperation(axisOperation);
        configContext.getAxisConfiguration().addService(service);
    }

    protected void tearDown() throws Exception {
        UtilsMailServer.stop();
    }

    private OMElement createEnvelope() {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://localhost/my", "my");
        OMElement method = fac.createOMElement("echoOMElement", omNs);
        OMElement value = fac.createOMElement("myValue", omNs);
        value.addChild(
                fac.createOMText(value, "Isaac Asimov, The Foundation Trilogy"));
        method.addChild(value);

        return method;
    }

    public void testOneWay() throws Exception {
        AxisService service = new AxisService(serviceName.getLocalPart());
        AxisOperation axisOperation = new OutOnlyAxisOperation(
        );
        axisOperation.setName(operationName);
        axisOperation.setMessageReceiver(new MessageReceiver() {
            public void receive(MessageContext messageCtx) {
                messageCtx.getEnvelope();
            }
        });
        service.addOperation(axisOperation);
        //configContext.getAxisConfiguration().addService(service);

        OMElement payload = createEnvelope();

        ServiceClient servicClient = new ServiceClient(clientConfigContext, service);

        Options options = new Options();
        options.setTo(targetEPR);

        servicClient.setOptions(options);

        servicClient.fireAndForget(operationName, payload);
    }
}
