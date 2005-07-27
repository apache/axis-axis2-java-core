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

package org.apache.axis2.processingModel;

//todo

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.clientapi.InOutMEPClient;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.ServiceDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.Echo;
import org.apache.axis2.integration.TestingUtils;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.SOAPFactory;
import org.apache.axis2.soap.SOAPHeaderBlock;
import org.apache.axis2.soap.impl.llom.soap12.SOAP12Constants;
import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SoapProcessingModelTest extends TestCase {
    private EndpointReference targetEPR =
        new EndpointReference(
            AddressingConstants.WSA_TO,
            "http://127.0.0.1:"
                + (UtilServer.TESTING_PORT)
                + "/axis/services/EchoXMLService/echoOMElement");
    private Log log = LogFactory.getLog(getClass());
    private QName serviceName = new QName("EchoXMLService");
    private QName operationName = new QName("echoOMElement");
    private QName transportName = new QName("http://localhost/my", "NullTransport");

    private AxisConfiguration engineRegistry;
    private MessageContext mc;
    private ServiceContext serviceContext;
    private ServiceDescription service;

    private boolean finish = false;

    public SoapProcessingModelTest() {
        super(SoapProcessingModelTest.class.getName());
    }

    public SoapProcessingModelTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        UtilServer.start();
        service = Utils.createSimpleService(serviceName, Echo.class.getName(), operationName);
        UtilServer.deployService(service);
        serviceContext =
            UtilServer.getConfigurationContext().createServiceContext(service.getName());

    }

    protected void tearDown() throws Exception {
        UtilServer.unDeployService(serviceName);
        UtilServer.stop();
    }
    
    
    public void sendMessageWithHeader(SOAPEnvelope envelope) throws AxisFault{
        InOutMEPClient inOutMC;
        ServiceContext serviceContext =
            UtilServer.getConfigurationContext().createServiceContext(service.getName());
        inOutMC = new InOutMEPClient(serviceContext);
        try{
            MessageContext msgctx = new MessageContext(serviceContext.getEngineContext());

            msgctx.setEnvelope(envelope);

            inOutMC.setTo(targetEPR);
            inOutMC.setTransportInfo(Constants.TRANSPORT_HTTP, Constants.TRANSPORT_HTTP, false);

            MessageContext result =
                inOutMC.invokeBlocking(
                    serviceContext.getServiceConfig().getOperation(operationName),
                    msgctx);
        }finally{
            inOutMC.close();
        }
    }
    
    public void testSendingMustUnderstandWithNextRole() throws Exception {
        SOAPFactory fac = OMAbstractFactory.getSOAP12Factory();
        SOAPEnvelope envelope = fac.getDefaultEnvelope();
        OMNamespace headerNs = fac.createOMNamespace("http://dummyHeader", "dh");
        SOAPHeaderBlock h1 =
            fac.createSOAPHeaderBlock("DummyHeader", headerNs, envelope.getHeader());
        h1.setMustUnderstand(true);
        h1.addChild(fac.createText("Dummy String"));
        h1.setRole(SOAP12Constants.SOAP_ROLE_NEXT);
        OMElement payload = TestingUtils.createDummyOMElement();
        envelope.getBody().addChild(payload);
        sendMessageWithHeader(envelope);

    }

    public void testSendingMustUnderstandWithArbitaryRole() throws Exception {
        try {
            SOAPFactory fac = OMAbstractFactory.getSOAP12Factory();
            SOAPEnvelope envelope = fac.getDefaultEnvelope();
            OMNamespace headerNs = fac.createOMNamespace("http://dummyHeader", "dh");
            SOAPHeaderBlock h1 =
                fac.createSOAPHeaderBlock("DummyHeader", headerNs, envelope.getHeader());
            h1.setMustUnderstand(true);
            h1.addChild(fac.createText("Dummy String"));
            h1.setRole("http://myOwnRole");
            OMElement payload = TestingUtils.createDummyOMElement();
            envelope.getBody().addChild(payload);
            sendMessageWithHeader(envelope);

        } catch (Exception e) {
            assertTrue(e.getMessage().indexOf("Must Understand check failed")>-1);
        }
    }
}
