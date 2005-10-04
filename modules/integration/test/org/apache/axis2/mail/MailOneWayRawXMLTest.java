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

import junit.framework.TestCase;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.clientapi.MessageSender;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.OperationDescription;
import org.apache.axis2.description.ServiceDescription;
import org.apache.axis2.description.OutInOperationDescription;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMFactory;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.SOAPFactory;
import org.apache.axis2.transport.mail.SimpleMailListener;
import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;

public class MailOneWayRawXMLTest extends TestCase {
    private EndpointReference targetEPR =
            new EndpointReference("foo@127.0.0.1" +
            "/axis/services/EchoXMLService/echoOMElement");
    private Log log = LogFactory.getLog(getClass());
    private QName serviceName = new QName("EchoXMLService");
    private QName operationName = new QName("echoOMElement");


    private ConfigurationContext configContext;

    private SOAPEnvelope envelope;

    private boolean finish = false;

    public MailOneWayRawXMLTest() {
        super(MailOneWayRawXMLTest.class.getName());
    }

    public MailOneWayRawXMLTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        //start the mail server      
        configContext = UtilsMailServer.start();

        SimpleMailListener ml = new SimpleMailListener();
        ml.init(configContext,
                configContext.getAxisConfiguration().getTransportIn(
                        new QName(Constants.TRANSPORT_MAIL)));
        ml.start();

        ServiceDescription service = new ServiceDescription(serviceName);
        OperationDescription operation = new OutInOperationDescription(
                operationName);
        operation.setMessageReceiver(new MessageReceiver() {
            public void receive(MessageContext messgeCtx) throws AxisFault {
                envelope = messgeCtx.getEnvelope();
            }
        });
        service.addOperation(operation);
        configContext.getAxisConfiguration().addService(service);
        Utils.resolvePhases(configContext.getAxisConfiguration(), service);
        ServiceContext serviceContext = Utils.fillContextInformation(operation,  service, configContext);
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

    public void testOneWay() throws Exception {
        ServiceDescription service = new ServiceDescription(serviceName);
        OperationDescription operation = new OutInOperationDescription(
                operationName);
        operation.setMessageReceiver(new MessageReceiver() {
            public void receive(MessageContext messgeCtx) throws AxisFault {
                envelope = messgeCtx.getEnvelope();
            }
        });
        service.addOperation(operation);
        configContext.getAxisConfiguration().addService(service);
        Utils.resolvePhases(configContext.getAxisConfiguration(), service);
        ServiceContext serviceContext = Utils.fillContextInformation(operation,  service, configContext);

        SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();

        OMElement payload = createEnvelope();

        MessageSender sender = new MessageSender(serviceContext);

        sender.setTo(targetEPR);
        sender.setSenderTransport(Constants.TRANSPORT_MAIL);

        sender.send(operationName.getLocalPart(), payload);
        int index = 0;
        while (envelope == null) {
//          if(index < 10){
            Thread.sleep(4000);
//                index++;
//            }else{
//                fail("The messsge was not delivered even after 40 seconds");
//            }
        }
    }

//    public ConfigurationContext createNewConfigurationContext() throws Exception {
//        File file = new File(MAIL_TRANSPORT_ENABLED_REPO_PATH);
//        assertTrue(
//                "Mail repository directory " + file.getAbsolutePath() +
//                " does not exsist",
//                file.exists());
//        ConfigurationContextFactory builder = new ConfigurationContextFactory();
//        ConfigurationContext configContext =
//                builder.buildConfigurationContext(file.getAbsolutePath());
//        return configContext;
//    }
//
}
