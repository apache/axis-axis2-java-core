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

package org.apache.axis.mail;

//todo

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import junit.framework.TestCase;

import org.apache.axis.Constants;
import org.apache.axis.addressing.AddressingConstants;
import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.clientapi.AsyncResult;
import org.apache.axis.clientapi.Callback;
import org.apache.axis.context.ConfigurationContext;
import org.apache.axis.context.ConfigurationContextFactory;
import org.apache.axis.context.MessageContext;
import org.apache.axis.context.ServiceContext;
import org.apache.axis.description.OperationDescription;
import org.apache.axis.description.ParameterImpl;
import org.apache.axis.description.ServiceDescription;
import org.apache.axis.description.TransportInDescription;
import org.apache.axis.description.TransportOutDescription;
import org.apache.axis.engine.AxisConfiguration;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.engine.Echo;
import org.apache.axis.engine.MessageReceiver;
import org.apache.axis.om.OMAbstractFactory;
import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMFactory;
import org.apache.axis.om.OMNamespace;
import org.apache.axis.soap.SOAPEnvelope;
import org.apache.axis.transport.mail.MailTransportSender;
import org.apache.axis.transport.mail.SimpleMailListener;
import org.apache.axis.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MailRequestResponseRawXMLTest extends TestCase {
    private EndpointReference targetEPR =
        new EndpointReference(
            AddressingConstants.WSA_TO,
            "axis2-server@127.0.0.1" + "/axis/services/EchoXMLService/echoOMElement");
    private Log log = LogFactory.getLog(getClass());
    private QName serviceName = new QName("EchoXMLService");
    private QName operationName = new QName("echoOMElement");
    private QName transportName = new QName("http://localhost/my", "NullTransport");

    private AxisConfiguration engineRegistry;
    private MessageContext mc;

    private SOAPEnvelope envelope;

    private boolean finish = false;

    public MailRequestResponseRawXMLTest() {
        super(MailRequestResponseRawXMLTest.class.getName());
    }

    public MailRequestResponseRawXMLTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        SimpleMailListener ml = new SimpleMailListener();

        ConfigurationContext configContext = createServerConfigurationContext();
        ml.init(
            configContext,
            configContext.getAxisConfiguration().getTransportIn(
                new QName(Constants.TRANSPORT_MAIL)));
        ml.start();
        configContext.getAxisConfiguration().engageModule(new QName(Constants.MODULE_ADDRESSING));
        ServiceDescription service =
            Utils.createSimpleService(serviceName, Echo.class.getName(), operationName);
        configContext.getAxisConfiguration().addService(service);
        Utils.resolvePhases(configContext.getAxisConfiguration(), service);
        ServiceContext serviceContext = configContext.createServiceContext(serviceName);
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

    public void testEchoXMLCompleteASync() throws Exception {

        ConfigurationContext configContext = createClientConfigurationContext();
        ServiceDescription service = new ServiceDescription(serviceName);
        OperationDescription operation = new OperationDescription(operationName);
        operation.setMessageReciever(new MessageReceiver() {
            public void recieve(MessageContext messgeCtx) throws AxisFault {
                envelope = messgeCtx.getEnvelope();
            }
        });
        service.addOperation(operation);
        configContext.getAxisConfiguration().addService(service);
        Utils.resolvePhases(configContext.getAxisConfiguration(), service);
        ServiceContext serviceContext = configContext.createServiceContext(serviceName);

        org.apache.axis.clientapi.Call call = new org.apache.axis.clientapi.Call(serviceContext);
        call.engageModule(new QName(Constants.MODULE_ADDRESSING));

        try {
            call.setTo(targetEPR);
            call.setTransportInfo(Constants.TRANSPORT_MAIL, Constants.TRANSPORT_MAIL, true);
            Callback callback = new Callback() {
                public void onComplete(AsyncResult result) {
                    try {
                        result.getResponseEnvelope().serialize(
                            XMLOutputFactory.newInstance().createXMLStreamWriter(System.out));
                    } catch (XMLStreamException e) {
                        reportError(e);
                    } finally {
                        finish = true;
                    }
                }

                public void reportError(Exception e) {
                    e.printStackTrace();
                    finish = true;
                }
            };

            call.invokeNonBlocking(operationName.getLocalPart(), createEnvelope(), callback);
            int index = 0;
            while (!finish) {
                Thread.sleep(1000);
                index++;
//                if (index > 10) {
//                    throw new AxisFault("Server is shutdown as the Async response take too longs time");
//                }
            }
        } finally {
            call.close();
        }

    }
    public ConfigurationContext createServerConfigurationContext() throws Exception {
        ConfigurationContextFactory builder = new ConfigurationContextFactory();
        ConfigurationContext configContext =
            builder.buildEngineContext(org.apache.axis.Constants.TESTING_REPOSITORY);

        TransportInDescription transportIn =
            new TransportInDescription(new QName(Constants.TRANSPORT_MAIL));
        transportIn.addParameter(new ParameterImpl("transport.mail.pop3.host", "127.0.0.1"));
        transportIn.addParameter(new ParameterImpl("transport.mail.pop3.user", "axis2-server"));
        transportIn.addParameter(new ParameterImpl("transport.mail.pop3.password", "axis2"));
        transportIn.addParameter(new ParameterImpl("transport.mail.pop3.port", "110"));
        transportIn.addParameter(
            new ParameterImpl("transport.mail.replyToAddress", "axis2-server@127.0.0.1"));
        transportIn.setReciver(new SimpleMailListener());

        TransportOutDescription transportOut =
            new TransportOutDescription(new QName(Constants.TRANSPORT_MAIL));

        transportOut.addParameter(new ParameterImpl("transport.mail.smtp.host", "127.0.0.1"));
        transportOut.addParameter(new ParameterImpl("transport.mail.smtp.user", "axis2-server"));
        transportOut.addParameter(new ParameterImpl("transport.mail.smtp.password", "axis2"));
        transportOut.addParameter(new ParameterImpl("transport.mail.smtp.port", "25"));
        transportOut.setSender(new MailTransportSender());

        configContext.getAxisConfiguration().addTransportIn(transportIn);
        configContext.getAxisConfiguration().addTransportOut(transportOut);
        return configContext;
    }

    public ConfigurationContext createClientConfigurationContext() throws Exception {
        ConfigurationContextFactory builder = new ConfigurationContextFactory();
        ConfigurationContext configContext =
            builder.buildEngineContext(org.apache.axis.Constants.TESTING_REPOSITORY);

        TransportInDescription transportIn =
            new TransportInDescription(new QName(Constants.TRANSPORT_MAIL));
        transportIn.addParameter(new ParameterImpl("transport.mail.pop3.host", "127.0.0.1"));
        transportIn.addParameter(new ParameterImpl("transport.mail.pop3.user", "axis2-client"));
        transportIn.addParameter(new ParameterImpl("transport.mail.pop3.password", "axis2"));
        transportIn.addParameter(new ParameterImpl("transport.mail.pop3.port", "110"));
        transportIn.addParameter(
            new ParameterImpl("transport.mail.replyToAddress", "axis2-client@127.0.0.1"));
        transportIn.setReciver(new SimpleMailListener());

        TransportOutDescription transportOut =
            new TransportOutDescription(new QName(Constants.TRANSPORT_MAIL));

        transportOut.addParameter(new ParameterImpl("transport.mail.smtp.host", "127.0.0.1"));
        transportOut.addParameter(new ParameterImpl("transport.mail.smtp.user", "axis2-client"));
        transportOut.addParameter(new ParameterImpl("transport.mail.smtp.password", "axis2"));
        transportOut.addParameter(new ParameterImpl("transport.mail.smtp.port", "25"));
        transportOut.setSender(new MailTransportSender());

        configContext.getAxisConfiguration().addTransportIn(transportIn);
        configContext.getAxisConfiguration().addTransportOut(transportOut);
        return configContext;
    }

}
