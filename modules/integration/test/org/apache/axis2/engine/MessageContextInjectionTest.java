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

import junit.framework.TestCase;
import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.*;
import org.apache.axis2.engine.util.TestConstants;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMFactory;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.receivers.AbstractMessageReceiver;
import org.apache.axis2.receivers.RawXMLINOnlyMessageReceiver;
import org.apache.axis2.soap.SOAP11Constants;
import org.apache.axis2.transport.local.LocalTransportReceiver;
import org.apache.axis2.transport.local.LocalTransportSender;

import javax.xml.namespace.QName;

public class MessageContextInjectionTest extends TestCase implements TestConstants {
    private TransportOutDescription tOut;

    public MessageContextInjectionTest() {
        super(MessageContextInjectionTest.class.getName());
    }

    public MessageContextInjectionTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        AxisConfiguration config = new AxisConfiguration();

        TransportInDescription tIn = new TransportInDescription(new QName(Constants.TRANSPORT_LOCAL));
        config.addTransportIn(tIn);

        tOut = new TransportOutDescription(new QName(Constants.TRANSPORT_LOCAL));
        tOut.setSender(new LocalTransportSender());
        config.addTransportOut(tOut);

        LocalTransportReceiver.CONFIG_CONTEXT = new ConfigurationContext(
                config);

        AxisService service = new AxisService(serviceName.getLocalPart());
        service.addParameter(
                new ParameterImpl(AbstractMessageReceiver.SERVICE_CLASS,
                        MessageContextEnabledEcho.class.getName()));
        AxisOperation axisOperation = new InOnlyAxisOperation(
                operationName);
        axisOperation.setMessageReceiver(new RawXMLINOnlyMessageReceiver());
        service.addOperation(axisOperation);
        service.setClassLoader(Thread.currentThread().getContextClassLoader());
        LocalTransportReceiver.CONFIG_CONTEXT.getAxisConfiguration()
                .addService(service);
    }

    protected void tearDown() throws Exception {
    }

    private OMElement createEnvelope() {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://localhost/my", "my");
        OMElement method = fac.createOMElement("echoOMElement", omNs);
        OMElement value = fac.createOMElement("myValue", omNs);
        value.addChild(
                fac.createText(value, "Isaac Asimov, The Foundation Trilogy"));
        method.addChild(value);

        return method;
    }

    public void testEchoXMLSync() throws Exception {
        OMElement payload = createEnvelope();

//        MessageSender sender = new MessageSender("target/test-resources/integrationRepo");
        ConfigurationContextFactory factory = new ConfigurationContextFactory();
        ConfigurationContext configContext =
                factory.buildConfigurationContext("target/test-resources/integrationRepo");
        ServiceClient sender = new ServiceClient(configContext);
        Options options = new Options();
        sender.setOptions(options);
        options.setTo(targetEPR);
        options.setSenderTransport(tOut);
        options.setSoapVersionURI(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);
        sender.fireAndForget(payload);

    }

}
