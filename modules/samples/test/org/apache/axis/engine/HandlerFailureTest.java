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

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.axis.addressing.AddressingConstants;
import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.context.MessageContext;
import org.apache.axis.description.AxisOperation;
import org.apache.axis.description.AxisService;
import org.apache.axis.description.Flow;
import org.apache.axis.description.FlowImpl;
import org.apache.axis.handlers.AbstractHandler;
import org.apache.axis.integration.UtilServer;
import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMFactory;
import org.apache.axis.om.OMNamespace;
import org.apache.axis.om.SOAPBody;
import org.apache.axis.om.SOAPEnvelope;
import org.apache.axis.transport.http.SimpleHTTPServer;
import org.apache.axis.util.Utils;
import org.apache.axis.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class HandlerFailureTest extends TestCase {
    private Log log = LogFactory.getLog(getClass());
    private EndpointReference targetEPR = new EndpointReference(AddressingConstants.WSA_TO, "http://127.0.0.1:" + (UtilServer.TESTING_PORT) + "/axis/services/EchoXMLService");
    private QName serviceName = new QName("", targetEPR.getAddress());
    private QName operationName = new QName("echoOMElement");


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
        
        AxisService service = Utils.createSimpleService(serviceName,org.apache.axis.engine.Echo.class.getName());
        service.setInFlow(flow);
        AxisOperation operation = new AxisOperation(operationName);
        service.addOperation(operation);

        UtilServer.start();
        UtilServer.deployService(Utils.createServiceContext(service));
        try {
            callTheService();
        } finally {
            UtilServer.unDeployService(serviceName);
            UtilServer.stop();
        }
    }

    public void testFailureAtServerResponseFlow() throws Exception {
        AxisService service = Utils.createSimpleService(serviceName,org.apache.axis.engine.Echo.class.getName());
 

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

        AxisOperation operation = new AxisOperation(operationName);
        service.addOperation(operation);

        UtilServer.start();
        UtilServer.deployService(Utils.createServiceContext(service));
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
            OMFactory fac = OMFactory.newInstance();

            SOAPEnvelope reqEnv = fac.getDefaultEnvelope();
            OMNamespace omNs = fac.createOMNamespace("http://localhost/my", "my");
            OMElement method = fac.createOMElement("echoOMElement", omNs);
            OMElement value = fac.createOMElement("myValue", omNs);
            value.setValue("Isaac Assimov, the foundation Sega");
            method.addChild(value);
            reqEnv.getBody().addChild(method);

            org.apache.axis.clientapi.Call call = new org.apache.axis.clientapi.Call();
            //EndpointReference targetEPR = new EndpointReference(AddressingConstants.WSA_TO, "http://127.0.0.1:" + Utils.TESTING_PORT + "/axis/services/EchoXMLService");
            
            call.setTransport(Constants.TRANSPORT_HTTP);
            call.setTo(targetEPR);
            call.setAction(operationName.getLocalPart());
            SOAPEnvelope resEnv = call.sendReceiveSync(reqEnv);


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

