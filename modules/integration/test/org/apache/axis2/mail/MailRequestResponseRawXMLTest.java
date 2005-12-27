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

package org.apache.axis2.mail;

// todo

import junit.framework.TestCase;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.async.AsyncResult;
import org.apache.axis2.client.async.Callback;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.OutInAxisOperation;
import org.apache.axis2.engine.Echo;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMFactory;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.transport.mail.SimpleMailListener;
import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

public class MailRequestResponseRawXMLTest extends TestCase {
    private EndpointReference targetEPR = new EndpointReference("foo@127.0.0.1"
            + "/axis/services/EchoXMLService/echoOMElement");

    private Log log = LogFactory.getLog(getClass());

    private QName serviceName = new QName("EchoXMLService");

    private QName operationName = new QName("echoOMElement");

    private boolean finish = false;

    private SOAPEnvelope envelope;

    public MailRequestResponseRawXMLTest() {
        super(MailRequestResponseRawXMLTest.class.getName());
    }

    public MailRequestResponseRawXMLTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        ConfigurationContext configContext = UtilsMailServer.start();

        SimpleMailListener ml = new SimpleMailListener();

        ml.init(configContext, configContext.getAxisConfiguration()
                .getTransportIn(new QName(Constants.TRANSPORT_MAIL)));
        ml.start();
        // configContext.getAxisConfiguration().engageModule(
        // new QName(Constants.MODULE_ADDRESSING));
        AxisService service = Utils.createSimpleService(serviceName, Echo.class
                .getName(), operationName);
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
        value.addChild(fac.createText(value,
                "Isaac Asimov, The Foundation Trilogy"));
        method.addChild(value);

        return method;
    }

    public void testEchoXMLCompleteASync() throws Exception {

        ConfigurationContext configContext = UtilsMailServer
                .createClientConfigurationContext();
        AxisService service = new AxisService(serviceName.getLocalPart());
        AxisOperation axisOperation = new OutInAxisOperation();
        axisOperation.setName(operationName);
        axisOperation.setMessageReceiver(new MessageReceiver() {
            public void receive(MessageContext messageCtx) {
                envelope = messageCtx.getEnvelope();
            }
        });
        service.addOperation(axisOperation);
        configContext.getAxisConfiguration().addService(service);
        ServiceContext serviceContext = new ServiceGroupContext(configContext,
                service.getParent()).getServiceContext(service);

        org.apache.axis2.client.Call call = new org.apache.axis2.client.Call(
                serviceContext);

        Options options = new Options();
        options.setTo(targetEPR);
        options.setTransportInProtocol(Constants.TRANSPORT_MAIL);
        options.setUseSeparateListener(true);
        call.setClientOptions(options);

        Callback callback = new Callback() {
            public void onComplete(AsyncResult result) {
                try {
                    result.getResponseEnvelope().serializeAndConsume(
                            XMLOutputFactory.newInstance()
                                    .createXMLStreamWriter(System.out));
                } catch (XMLStreamException e) {
                    reportError(e);
                } finally {
                    finish = true;
                }
            }

            public void reportError(Exception e) {
                log.info(e.getMessage());
                finish = true;
            }
        };

        call.invokeNonBlocking(operationName.getLocalPart(), createEnvelope(),
                callback);
        int index = 0;
        while (!finish) {
            Thread.sleep(1000);
            index++;
            if (index > 10) {
                throw new AxisFault(
                        "Server was shutdown as the async response take too long to complete");
            }
        }
        call.close();

    }

    // public ConfigurationContext createServerConfigurationContext() throws
    // Exception {
    // ConfigurationContextFactory builder = new ConfigurationContextFactory();
    // ConfigurationContext configContext =
    // builder.buildConfigurationContext(
    // org.apache.axis2.Constants.TESTING_REPOSITORY);
    //
    // TransportInDescription transportIn =
    // new TransportInDescription(new QName(Constants.TRANSPORT_MAIL));
    // transportIn.addParameter(
    // new ParameterImpl("transport.mail.pop3.host", "127.0.0.1"));
    // transportIn.addParameter(
    // new ParameterImpl("transport.mail.pop3.user", "foo@127.0.0.1"));
    // transportIn.addParameter(
    // new ParameterImpl("transport.mail.pop3.password", "axis2"));
    // transportIn.addParameter(
    // new ParameterImpl("transport.mail.pop3.port", "1134"));
    // transportIn.addParameter(
    // new ParameterImpl("transport.mail.replyToAddress",
    // "foo@127.0.0.1"));
    // transportIn.setReceiver(new SimpleMailListener());
    // transportIn.getReceiver().init(configContext, transportIn);
    //
    // TransportOutDescription transportOut =
    // new TransportOutDescription(
    // new QName(Constants.TRANSPORT_MAIL));
    //
    // transportOut.addParameter(
    // new ParameterImpl("transport.mail.smtp.host", "127.0.0.1"));
    // transportOut.addParameter(
    // new ParameterImpl("transport.mail.smtp.user", "foo"));
    // transportOut.addParameter(
    // new ParameterImpl("transport.mail.smtp.password", "axis2"));
    // transportOut.addParameter(
    // new ParameterImpl("transport.mail.smtp.port", "1049"));
    // transportOut.setSender(new MailTransportSender());
    // transportOut.getSender().init(configContext, transportOut);
    //
    // configContext.getAxisConfiguration().addTransportIn(transportIn);
    // configContext.getAxisConfiguration().addTransportOut(transportOut);
    // return configContext;
    // }
    //
    // public ConfigurationContext createClientConfigurationContext() throws
    // Exception {
    // ConfigurationContextFactory builder = new ConfigurationContextFactory();
    // ConfigurationContext configContext =
    // builder.buildConfigurationContext(
    // org.apache.axis2.Constants.TESTING_REPOSITORY);
    //
    // TransportInDescription transportIn =
    // new TransportInDescription(new QName(Constants.TRANSPORT_MAIL));
    // transportIn.addParameter(
    // new ParameterImpl("transport.mail.pop3.host", "127.0.0.1"));
    // transportIn.addParameter(
    // new ParameterImpl("transport.mail.pop3.user", "bar@127.0.0.1"));
    // transportIn.addParameter(
    // new ParameterImpl("transport.mail.pop3.password", "axis2"));
    // transportIn.addParameter(
    // new ParameterImpl("transport.mail.pop3.port", "1134"));
    // transportIn.addParameter(
    // new ParameterImpl("transport.mail.replyToAddress",
    // "bar@127.0.0.1"));
    // transportIn.setReceiver(new SimpleMailListener());
    // transportIn.getReceiver().init(configContext, transportIn);
    //
    // TransportOutDescription transportOut =
    // new TransportOutDescription(
    // new QName(Constants.TRANSPORT_MAIL));
    //
    // transportOut.addParameter(
    // new ParameterImpl("transport.mail.smtp.host", "127.0.0.1"));
    // transportOut.addParameter(
    // new ParameterImpl("transport.mail.smtp.user", "bar"));
    // transportOut.addParameter(
    // new ParameterImpl("transport.mail.smtp.password", "axis2"));
    // transportOut.addParameter(
    // new ParameterImpl("transport.mail.smtp.port", "1049"));
    // transportOut.setSender(new MailTransportSender());
    // transportOut.getSender().init(configContext, transportOut);
    //
    // configContext.getAxisConfiguration().addTransportIn(transportIn);
    // configContext.getAxisConfiguration().addTransportOut(transportOut);
    // return configContext;
    // }

}
