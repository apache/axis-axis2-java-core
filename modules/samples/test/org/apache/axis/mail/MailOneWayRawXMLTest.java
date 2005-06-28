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

import java.io.File;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.axis.Constants;
import org.apache.axis.addressing.AddressingConstants;
import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.clientapi.MessageSender;
import org.apache.axis.context.ConfigurationContext;
import org.apache.axis.context.ConfigurationContextFactory;
import org.apache.axis.context.MessageContext;
import org.apache.axis.context.ServiceContext;
import org.apache.axis.description.OperationDescription;
import org.apache.axis.description.ServiceDescription;
import org.apache.axis.engine.AxisConfiguration;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.engine.MessageReceiver;
import org.apache.axis.om.OMAbstractFactory;
import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMFactory;
import org.apache.axis.om.OMNamespace;
import org.apache.axis.soap.SOAPEnvelope;
import org.apache.axis.soap.SOAPFactory;
import org.apache.axis.transport.mail.SimpleMailListener;
import org.apache.axis.transport.mail.server.MailConstants;
import org.apache.axis.transport.mail.server.MailServer;
import org.apache.axis.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MailOneWayRawXMLTest extends TestCase {
    private static final String MAIL_TRANSPORT_ENABLED_REPO_PATH = Constants.TESTING_PATH+ "mail-transport-enabledRepository"; 
    
    
    private EndpointReference targetEPR =
        new EndpointReference(
            AddressingConstants.WSA_TO,
            "axis2@127.0.0.1" + "/axis/services/EchoXMLService/echoOMElement");
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
        configContext = createNewConfigurationContext();  
        //start the mail server      
        MailServer server = new MailServer(configContext,MailConstants.POP_SERVER_PORT,MailConstants.SMTP_SERVER_PORT);
        
        SimpleMailListener ml = new SimpleMailListener();
        ml.init(
            configContext,
            configContext.getAxisConfiguration().getTransportIn(
                new QName(Constants.TRANSPORT_MAIL)));
        ml.start();

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

    public void testOneWay() throws Exception {
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

    public ConfigurationContext createNewConfigurationContext() throws Exception {
        File file = new File(MAIL_TRANSPORT_ENABLED_REPO_PATH);
        assertTrue("Mail repository directory "+ file.getAbsolutePath() + " does not exsist",file.exists());
        ConfigurationContextFactory builder = new ConfigurationContextFactory();
        ConfigurationContext configContext =
            builder.buildConfigurationContext(file.getAbsolutePath());
        return configContext;
    }
    
}
