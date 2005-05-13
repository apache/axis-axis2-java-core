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
import org.apache.axis.context.MessageContext;
import org.apache.axis.description.Flow;
import org.apache.axis.description.FlowImpl;
import org.apache.axis.description.OperationDescription;
import org.apache.axis.description.ServiceDescription;
import org.apache.axis.handlers.AbstractHandler;
import org.apache.axis.integration.UtilServer;
import org.apache.axis.om.OMAbstractFactory;
import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMNamespace;
import org.apache.axis.soap.SOAPBody;
import org.apache.axis.soap.SOAPEnvelope;
import org.apache.axis.soap.SOAPFactory;
import org.apache.axis.transport.http.SimpleHTTPServer;
import org.apache.axis.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;


public class HandlerFailureTest extends TestCase {
    private Log log = LogFactory.getLog(getClass());
    private static final String SERVICE_NAME = "EchoXMLService";
    private static final String OPERATION_NAME = "echoOMElement";
    
    
    private static final String ADDRESS = "http://127.0.0.1:" + (UtilServer.TESTING_PORT) +
            "/axis/services/" + SERVICE_NAME + "/" +OPERATION_NAME;
//    private static final String ADDRESS = "http://127.0.0.1:8080/axis/services/" + SERVICE_NAME;
    private EndpointReference targetEPR = new EndpointReference(AddressingConstants.WSA_TO, ADDRESS);
    private QName serviceName = new QName("", SERVICE_NAME);
    //private QName serviceName = new QName("", targetEPR.getValue());

    private QName operationName = new QName(OPERATION_NAME);


    private MessageContext mc;
    private Thread thisThread;
    private SimpleHTTPServer sas;

    public HandlerFailureTest() {
        super(HandlerFailureTest.class.getName());
    }

    public HandlerFailureTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }


    public void testFailureAtServerRequestFlow() throws Exception {
        Flow flow = new FlowImpl();
        Utils.addHandler(flow, new SpeakingHandler());
        Utils.addHandler(flow, new SpeakingHandler());
        Utils.addHandler(flow, new SpeakingHandler());
        Utils.addHandler(flow, new SpeakingHandler());
        Utils.addHandler(flow, culprit);
        Utils.addHandler(flow, new SpeakingHandler());
        
        ServiceDescription service = Utils.createSimpleService(serviceName,org.apache.axis.engine.Echo.class.getName(),operationName);
        service.setInFlow(flow);
        
        UtilServer.start();
        UtilServer.deployService(Utils.createServiceContext(service,null));
        try {
            callTheService();
        } finally {
            UtilServer.unDeployService(serviceName);
            UtilServer.stop();
        }
    }

    public void testFailureAtServerResponseFlow() throws Exception {
        ServiceDescription service = Utils.createSimpleService(serviceName,org.apache.axis.engine.Echo.class.getName(),operationName);
 

        Flow flow = new FlowImpl();
        Utils.addHandler(flow, new SpeakingHandler());
        Utils.addHandler(flow, new SpeakingHandler());
        Utils.addHandler(flow, new SpeakingHandler());
        Utils.addHandler(flow, new SpeakingHandler());
        Utils.addHandler(flow, new SpeakingHandler());
        service.setInFlow(flow);


        flow = new FlowImpl();
        Utils.addHandler(flow, new SpeakingHandler());
        Utils.addHandler(flow, new SpeakingHandler());
        Utils.addHandler(flow, new SpeakingHandler());
        Utils.addHandler(flow, new SpeakingHandler());
        Utils.addHandler(flow, culprit);
        Utils.addHandler(flow, new SpeakingHandler());
        service.setInFlow(flow);

        OperationDescription operation = new OperationDescription(operationName);
        service.addOperation(operation);

        UtilServer.start();
        UtilServer.deployService(Utils.createServiceContext(service,null));
        try {
            callTheService();
        } finally {
            UtilServer.unDeployService(serviceName);
            UtilServer.stop();
        }
    }


    protected void tearDown() throws Exception {

    }


    private void callTheService() throws Exception {
        try {
            SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
                            
            SOAPEnvelope reqEnv = fac.getDefaultEnvelope();
            OMNamespace omNs = fac.createOMNamespace("http://localhost/my", "my");
            OMElement method = fac.createOMElement("echoOMElement", omNs);
            OMElement value = fac.createOMElement("myValue", omNs);
            value.setText("Isaac Assimov, the foundation Sega");
            method.addChild(value);
            reqEnv.getBody().addChild(method);

            org.apache.axis.clientapi.Call call = new org.apache.axis.clientapi.Call();
            //EndpointReference targetEPR = new EndpointReference(AddressingConstants.WSA_TO, "http://127.0.0.1:" + Utils.TESTING_PORT + "/axis/services/EchoXMLService");
            
            call.setTransportInfo(Constants.TRANSPORT_HTTP,Constants.TRANSPORT_HTTP,false);
            call.setTo(targetEPR);
            SOAPEnvelope resEnv = (SOAPEnvelope)call.invokeBlocking(operationName.getLocalPart(),reqEnv);
            
            SOAPBody sb = resEnv.getBody();

            if (sb.hasFault()) {
                String message = sb.getFault().getException().getMessage();
                throw new AxisFault(message);
            }
            fail("the test must fail due to bad service Name");
        } catch (AxisFault e) {
            assertTrue((e.getMessage().indexOf(UtilServer.FAILURE_MESSAGE)) > 0);
            return;
        }

    }

    private Handler culprit = new AbstractHandler() {
        public void invoke(MessageContext msgContext) throws AxisFault {
            throw new AxisFault(UtilServer.FAILURE_MESSAGE);
        }
    };
}

