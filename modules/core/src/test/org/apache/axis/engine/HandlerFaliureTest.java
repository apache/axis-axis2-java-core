/*
* Copyright 2003,2004 The Apache Software Foundation.
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

import org.apache.axis.AbstractTestCase;
import org.apache.axis.addressing.AddressingConstants;
import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.context.MessageContext;
import org.apache.axis.description.*;
import org.apache.axis.handlers.AbstractHandler;
import org.apache.axis.integration.UtilServer;
import org.apache.axis.om.*;
import org.apache.axis.providers.RawXMLProvider;
import org.apache.axis.transport.http.SimpleHTTPServer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;


public class HandlerFaliureTest extends AbstractTestCase {
    private Log log = LogFactory.getLog(getClass());
    private QName serviceName = new QName("", "EchoXMLService");
    private QName operationName = new QName("http://localhost/my", "echoOMElement");


    private MessageContext mc;
    private Thread thisThread;
    private SimpleHTTPServer sas;

    public HandlerFaliureTest() {
        super(HandlerFaliureTest.class.getName());
    }

    public HandlerFaliureTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }


    public void testFailureAtServerRequestFlow() throws Exception {
        AxisService service = new AxisService(serviceName);

        Flow flow = new FlowImpl();
        EngineUtils.addHandler(flow, new SpeakingHandler());
        EngineUtils.addHandler(flow, new SpeakingHandler());
        EngineUtils.addHandler(flow, new SpeakingHandler());
        EngineUtils.addHandler(flow, new SpeakingHandler());
        EngineUtils.addHandler(flow, culprit);
        EngineUtils.addHandler(flow, new SpeakingHandler());
        service.setInFlow(flow);

        service.setClassLoader(Thread.currentThread().getContextClassLoader());
        Parameter classParam = new ParameterImpl("className", Echo.class.getName());
        service.addParameter(classParam);
        service.setProvider(new RawXMLProvider());
        AxisOperation operation = new SimpleAxisOperationImpl(operationName);

        service.addOperation(operation);

        EngineUtils.createExecutionChains(service);

        UtilServer.start();
        UtilServer.deployService(service);
        try {
            callTheService();
        } finally {
            UtilServer.unDeployService(serviceName);
            UtilServer.stop();
        }
    }

    public void testFailureAtServerResponseFlow() throws Exception {
        AxisService service = new AxisService(serviceName);

        Flow flow = new FlowImpl();
        EngineUtils.addHandler(flow, new SpeakingHandler());
        EngineUtils.addHandler(flow, new SpeakingHandler());
        EngineUtils.addHandler(flow, new SpeakingHandler());
        EngineUtils.addHandler(flow, new SpeakingHandler());
        EngineUtils.addHandler(flow, new SpeakingHandler());
        service.setInFlow(flow);


        flow = new FlowImpl();
        EngineUtils.addHandler(flow, new SpeakingHandler());
        EngineUtils.addHandler(flow, new SpeakingHandler());
        EngineUtils.addHandler(flow, new SpeakingHandler());
        EngineUtils.addHandler(flow, new SpeakingHandler());
        EngineUtils.addHandler(flow, culprit);
        EngineUtils.addHandler(flow, new SpeakingHandler());
        service.setInFlow(flow);

        service.setClassLoader(Thread.currentThread().getContextClassLoader());
        Parameter classParam = new ParameterImpl("className", Echo.class.getName());
        service.addParameter(classParam);
        service.setProvider(new RawXMLProvider());
        AxisOperation operation = new SimpleAxisOperationImpl(operationName);

        service.addOperation(operation);

        EngineUtils.createExecutionChains(service);
        UtilServer.start();
        UtilServer.deployService(service);
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
            //EndpointReference targetEPR = new EndpointReference(AddressingConstants.WSA_TO, "http://127.0.0.1:" + EngineUtils.TESTING_PORT + "/axis/services/EchoXMLService");
            EndpointReference targetEPR = new EndpointReference(AddressingConstants.WSA_TO, "http://127.0.0.1:" + (EngineUtils.TESTING_PORT) + "/axis/services/EchoXMLService");
            call.setTo(targetEPR);
            SOAPEnvelope resEnv = call.sendReceive(reqEnv);


            SOAPBody sb = resEnv.getBody();
            if (sb.hasFault()) {
                String message = sb.getFault().getException().getMessage();
                throw new AxisFault(message);
            }
            fail("the test must fail due to bad service Name");
        } catch (AxisFault e) {
            assertTrue((e.getMessage().indexOf(EngineUtils.FAILURE_MESSAGE)) > 0);
            return;
        }

    }

    private Handler culprit = new AbstractHandler() {
        public void invoke(MessageContext msgContext) throws AxisFault {
            throw new AxisFault(EngineUtils.FAILURE_MESSAGE);
        }
    };
}

