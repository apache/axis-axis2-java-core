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

package org.apache.axis.engine;

//todo

import junit.framework.TestCase;
import org.apache.axis.Constants;
import org.apache.axis.addressing.AddressingConstants;
import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.clientapi.MessageSender;
import org.apache.axis.context.ConfigurationContext;
import org.apache.axis.context.MessageContext;
import org.apache.axis.description.OperationDescription;
import org.apache.axis.description.ParameterImpl;
import org.apache.axis.description.ServiceDescription;
import org.apache.axis.om.OMAbstractFactory;
import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMFactory;
import org.apache.axis.om.OMNamespace;
import org.apache.axis.receivers.AbstractMessageReceiver;
import org.apache.axis.receivers.RawXMLINOnlyMessageReceiver;
import org.apache.axis.soap.SOAPEnvelope;
import org.apache.axis.soap.SOAPFactory;
import org.apache.axis.soap.impl.llom.soap11.SOAP11Constants;
import org.apache.axis.transport.local.LocalTransportReceiver;
import org.apache.axis.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;

public class MessageContextInjectionTest extends TestCase {
    private EndpointReference targetEPR =
        new EndpointReference(
            AddressingConstants.WSA_TO,"/axis/services/EchoXMLService/echoOMElement");
    private Log log = LogFactory.getLog(getClass());
    private QName serviceName = new QName("EchoXMLService");
    private QName operationName = new QName("echoOMElement");


    private AxisConfiguration engineRegistry;
    private MessageContext mc;

    private SOAPEnvelope envelope;

    private boolean finish = false;

    public MessageContextInjectionTest() {
        super(MessageContextInjectionTest.class.getName());
    }

    public MessageContextInjectionTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        ConfigurationContext configContext = new ConfigurationContext(new AxisConfigurationImpl());
        LocalTransportReceiver.CONFIG_CONTEXT = configContext;

        ServiceDescription service = new ServiceDescription(serviceName);
        service.addParameter(new ParameterImpl(AbstractMessageReceiver.SERVICE_CLASS, MessageContextEnabledEcho.class.getName()));
        OperationDescription operation = new OperationDescription(operationName);
        operation.setMessageReciever(new RawXMLINOnlyMessageReceiver());
        service.addOperation(operation);
        service.setClassLoader(Thread.currentThread().getContextClassLoader());
        LocalTransportReceiver.CONFIG_CONTEXT.getAxisConfiguration().addService(service);
        Utils.resolvePhases(LocalTransportReceiver.CONFIG_CONTEXT.getAxisConfiguration(), service);
    }

    protected void tearDown() throws Exception {
    }

    private OMElement createEnvelope() {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://localhost/my", "my");
        OMElement method = fac.createOMElement("echoOMElement", omNs);
        OMElement value = fac.createOMElement("myValue", omNs);
        value.addChild(fac.createText(value, "Isaac Assimov, the foundation Sega"));
        method.addChild(value);

        return method;
    }

    public void testEchoXMLSync() throws Exception {
        SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();

        OMElement payload = createEnvelope();

        MessageSender sender = new MessageSender();

        sender.setTo(targetEPR);
        sender.setSenderTransport(Constants.TRANSPORT_LOCAL);
        sender.setSoapVersionURI(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);
        sender.send(operationName.getLocalPart(), payload);
        
    }

}
