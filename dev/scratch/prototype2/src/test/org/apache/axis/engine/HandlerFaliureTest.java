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
import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.addressing.AddressingConstants;
import org.apache.axis.context.MessageContext;
import org.apache.axis.description.AxisOperation;
import org.apache.axis.description.Flow;
import org.apache.axis.description.Parameter;
import org.apache.axis.description.SpeakingHandler;
import org.apache.axis.impl.description.AxisService;
import org.apache.axis.impl.description.FlowImpl;
import org.apache.axis.impl.description.ParameterImpl;
import org.apache.axis.impl.description.SimpleAxisOperationImpl;
import org.apache.axis.impl.handlers.AbstractHandler;
import org.apache.axis.impl.providers.RawXMLProvider;
import org.apache.axis.impl.transport.http.SimpleHTTPReceiver;
import org.apache.axis.om.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;


public class HandlerFaliureTest extends AbstractTestCase {
    private Log log = LogFactory.getLog(getClass());
    private QName serviceName = new QName("", "EchoXMLService");
    private QName operationName = new QName("http://localhost/my", "echoOMElement");
    private QName transportName = new QName("http://localhost/my", "NullTransport");

    private EngineRegistry engineRegistry;
    private MessageContext mc;
    private Thread thisThread = null;
    private SimpleHTTPReceiver sas;
    private int testingPort = 7777;
    private int testCount = 0;

    public HandlerFaliureTest() {
        super(HandlerFaliureTest.class.getName());
    }

    public HandlerFaliureTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        engineRegistry = EngineUtils.createMockRegistry(serviceName, operationName, transportName);
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

        engineRegistry.addService(service);
        sas = EngineUtils.startServer(engineRegistry);
        try{
	        callTheService();
        }finally{
			EngineUtils.stopServer();
			Thread.sleep(1000);
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
        engineRegistry.addService(service);
        sas = EngineUtils.startServer(engineRegistry);
		try{
			callTheService();
		}finally{
			EngineUtils.stopServer();
			Thread.sleep(1000);
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
			EndpointReference targetEPR = new EndpointReference(AddressingConstants.WSA_TO, "http://127.0.0.1:" + (EngineUtils.TESTING_PORT )+ "/axis/services/EchoXMLService");
            call.setTo(targetEPR);
            SOAPEnvelope resEnv = call.sendReceive(reqEnv);

            SOAPBody sb = resEnv.getBody();
            if (sb.hasFault()) {
                throw new AxisFault(sb.getFault().getFaultString());
            }
        } catch (AxisFault e) {
            assertTrue((e.getMessage().indexOf(EngineUtils.FAILURE_MESSAGE)) > 0);
            tearDown();
            return;
        }
        fail("the test must fail due to bad service Name");
    }

    private Handler culprit = new AbstractHandler() {
        public void invoke(MessageContext msgContext) throws AxisFault {
            throw new AxisFault(EngineUtils.FAILURE_MESSAGE);
        }
    };
}

