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

//todo

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import junit.framework.TestCase;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.clientapi.AsyncResult;
import org.apache.axis2.clientapi.Callback;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.OperationDescription;
import org.apache.axis2.description.ServiceDescription;
import org.apache.axis2.engine.AxisConfiguration;
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

public class MailRequestResponseRawXMLTest extends TestCase {
    private EndpointReference targetEPR =
            new EndpointReference("foo@127.0.0.1" +
            "/axis/services/EchoXMLService/echoOMElement");
    private Log log = LogFactory.getLog(getClass());
    private QName serviceName = new QName("EchoXMLService");
    private QName operationName = new QName("echoOMElement");
    private QName transportName = new QName("http://localhost/my",
            "NullTransport");

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
        ConfigurationContext configContext = UtilsMailServer.start();
        
        SimpleMailListener ml = new SimpleMailListener();


        ml.init(configContext,
                configContext.getAxisConfiguration().getTransportIn(
                        new QName(Constants.TRANSPORT_MAIL)));
        ml.start();
//        configContext.getAxisConfiguration().engageModule(
//                new QName(Constants.MODULE_ADDRESSING));
        ServiceDescription service =
                Utils.createSimpleService(serviceName,
                        Echo.class.getName(),
                        operationName);
        configContext.getAxisConfiguration().addService(service);
        Utils.resolvePhases(configContext.getAxisConfiguration(), service);
        ServiceContext serviceContext = configContext.createServiceContext(
                serviceName);
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
                fac.createText(value, "Isaac Assimov, the foundation Sega"));
        method.addChild(value);

        return method;
    }

    public void testEchoXMLCompleteASync() throws Exception {

        ConfigurationContext configContext = UtilsMailServer.createClientConfigurationContext();
        ServiceDescription service = new ServiceDescription(serviceName);
        OperationDescription operation = new OperationDescription(
                operationName);
        operation.setMessageReceiver(new MessageReceiver() {
            public void receive(MessageContext messgeCtx) throws AxisFault {
                envelope = messgeCtx.getEnvelope();
            }
        });
        service.addOperation(operation);
        configContext.getAxisConfiguration().addService(service);
        Utils.resolvePhases(configContext.getAxisConfiguration(), service);
        ServiceContext serviceContext = configContext.createServiceContext(
                serviceName);

        org.apache.axis2.clientapi.Call call = new org.apache.axis2.clientapi.Call(
                serviceContext);
//        call.engageModule(new QName(Constants.MODULE_ADDRESSING));

        call.setTo(targetEPR);
        call.setTransportInfo(Constants.TRANSPORT_MAIL,
                Constants.TRANSPORT_MAIL,
                true);
        Callback callback = new Callback() {
            public void onComplete(AsyncResult result) {
                try {
                    result.getResponseEnvelope().serialize(XMLOutputFactory.newInstance()
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

        call.invokeNonBlocking(operationName.getLocalPart(),
                createEnvelope(),
                callback);
        int index = 0;
        while (!finish) {
            Thread.sleep(1000);
            index++;
            if (index > 10) {
                throw new AxisFault(
                        "Server is shutdown as the Async response take too longs time");
            }
        }
        call.close();

    }

//    public ConfigurationContext createServerConfigurationContext() throws Exception {
//        ConfigurationContextFactory builder = new ConfigurationContextFactory();
//        ConfigurationContext configContext =
//                builder.buildConfigurationContext(
//                        org.apache.axis2.Constants.TESTING_REPOSITORY);
//
//        TransportInDescription transportIn =
//                new TransportInDescription(new QName(Constants.TRANSPORT_MAIL));
//        transportIn.addParameter(
//                new ParameterImpl("transport.mail.pop3.host", "127.0.0.1"));
//        transportIn.addParameter(
//                new ParameterImpl("transport.mail.pop3.user", "foo@127.0.0.1"));
//        transportIn.addParameter(
//                new ParameterImpl("transport.mail.pop3.password", "axis2"));
//        transportIn.addParameter(
//                new ParameterImpl("transport.mail.pop3.port", "1134"));
//        transportIn.addParameter(
//                new ParameterImpl("transport.mail.replyToAddress",
//                        "foo@127.0.0.1"));
//        transportIn.setReceiver(new SimpleMailListener());
//        transportIn.getReceiver().init(configContext, transportIn);
//
//        TransportOutDescription transportOut =
//                new TransportOutDescription(
//                        new QName(Constants.TRANSPORT_MAIL));
//
//        transportOut.addParameter(
//                new ParameterImpl("transport.mail.smtp.host", "127.0.0.1"));
//        transportOut.addParameter(
//                new ParameterImpl("transport.mail.smtp.user", "foo"));
//        transportOut.addParameter(
//                new ParameterImpl("transport.mail.smtp.password", "axis2"));
//        transportOut.addParameter(
//                new ParameterImpl("transport.mail.smtp.port", "1049"));
//        transportOut.setSender(new MailTransportSender());
//        transportOut.getSender().init(configContext, transportOut);
//
//        configContext.getAxisConfiguration().addTransportIn(transportIn);
//        configContext.getAxisConfiguration().addTransportOut(transportOut);
//        return configContext;
//    }
//
//    public ConfigurationContext createClientConfigurationContext() throws Exception {
//        ConfigurationContextFactory builder = new ConfigurationContextFactory();
//        ConfigurationContext configContext =
//                builder.buildConfigurationContext(
//                        org.apache.axis2.Constants.TESTING_REPOSITORY);
//
//        TransportInDescription transportIn =
//                new TransportInDescription(new QName(Constants.TRANSPORT_MAIL));
//        transportIn.addParameter(
//                new ParameterImpl("transport.mail.pop3.host", "127.0.0.1"));
//        transportIn.addParameter(
//                new ParameterImpl("transport.mail.pop3.user", "bar@127.0.0.1"));
//        transportIn.addParameter(
//                new ParameterImpl("transport.mail.pop3.password", "axis2"));
//        transportIn.addParameter(
//                new ParameterImpl("transport.mail.pop3.port", "1134"));
//        transportIn.addParameter(
//                new ParameterImpl("transport.mail.replyToAddress",
//                        "bar@127.0.0.1"));
//        transportIn.setReceiver(new SimpleMailListener());
//        transportIn.getReceiver().init(configContext, transportIn);
//
//        TransportOutDescription transportOut =
//                new TransportOutDescription(
//                        new QName(Constants.TRANSPORT_MAIL));
//
//        transportOut.addParameter(
//                new ParameterImpl("transport.mail.smtp.host", "127.0.0.1"));
//        transportOut.addParameter(
//                new ParameterImpl("transport.mail.smtp.user", "bar"));
//        transportOut.addParameter(
//                new ParameterImpl("transport.mail.smtp.password", "axis2"));
//        transportOut.addParameter(
//                new ParameterImpl("transport.mail.smtp.port", "1049"));
//        transportOut.setSender(new MailTransportSender());
//        transportOut.getSender().init(configContext, transportOut);
//
//        configContext.getAxisConfiguration().addTransportIn(transportIn);
//        configContext.getAxisConfiguration().addTransportOut(transportOut);
//        return configContext;
//    }

}
