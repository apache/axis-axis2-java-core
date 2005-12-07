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

package org.apache.axis2.engine;

//todo

import junit.framework.TestCase;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.client.Call;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.util.TestConstants;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.phaseresolver.PhaseMetadata;
import org.apache.axis2.soap.SOAPFactory;
import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.stream.XMLOutputFactory;
import java.util.ArrayList;


public class HandlerFailureTest extends TestCase implements TestConstants {
    private Log log = LogFactory.getLog(getClass());

    public HandlerFailureTest() {
        super(HandlerFailureTest.class.getName());
    }

    public HandlerFailureTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }


    public void testFailureAtServerRequestFlow() throws Exception {
//        Flow flow = new FlowImpl();
//        Utils.addHandler(flow,
//                new SpeakingHandler(),
//                PhaseMetadata.PHASE_POLICY_DETERMINATION);
//        Utils.addHandler(flow,
//                new SpeakingHandler(),
//                PhaseMetadata.PHASE_POLICY_DETERMINATION);
//        Utils.addHandler(flow,
//                new SpeakingHandler(),
//                PhaseMetadata.PHASE_POLICY_DETERMINATION);
//        Utils.addHandler(flow,
//                new SpeakingHandler(),
//                PhaseMetadata.PHASE_POLICY_DETERMINATION);
//        Utils.addHandler(flow,
//                culprit,
//                PhaseMetadata.PHASE_POLICY_DETERMINATION);
//        Utils.addHandler(flow,
//                new SpeakingHandler(),
//                PhaseMetadata.PHASE_POLICY_DETERMINATION);

        AxisService service = Utils.createSimpleService(serviceName,
                Echo.class.getName(),
                operationName);
//        service.setInFlow(flow); // we do not support this

        UtilServer.start();
        UtilServer.deployService(service);
        AxisOperation operation = service.getOperation(operationName);
        ArrayList phasec= new ArrayList();
        phasec.add(new Phase(PhaseMetadata.PHASE_POLICY_DETERMINATION));
        operation.setRemainingPhasesInFlow(phasec);
        ArrayList phase = operation.getRemainingPhasesInFlow();
        for (int i = 0; i < phase.size(); i++) {
            Phase phase1 = (Phase) phase.get(i);
            if(PhaseMetadata.PHASE_POLICY_DETERMINATION.equals(phase1.getPhaseName())){
                phase1.addHandler(culprit);
            }
        }
        try {
            callTheService();
        } finally {
            UtilServer.unDeployService(serviceName);
            UtilServer.stop();
        }
    }

//    public void testFailureAtServerResponseFlow() throws Exception {
//        AxisService service = Utils.createSimpleService(serviceName,org.apache.axis2.engine.Echo.class.getName(),operationName);
// 
//
//        Flow flow = new FlowImpl();
//        Utils.addHandler(flow, new SpeakingHandler(),PhaseMetadata.PHASE_POLICY_DETERMINATION);
//        Utils.addHandler(flow, new SpeakingHandler(),PhaseMetadata.PHASE_POLICY_DETERMINATION);
//        Utils.addHandler(flow, new SpeakingHandler(),PhaseMetadata.PHASE_POLICY_DETERMINATION);
//        Utils.addHandler(flow, new SpeakingHandler(),PhaseMetadata.PHASE_POLICY_DETERMINATION);
//        Utils.addHandler(flow, new SpeakingHandler(),PhaseMetadata.PHASE_POLICY_DETERMINATION);
//        service.setInFlow(flow);
//
//
//        flow = new FlowImpl();
//        Utils.addHandler(flow, new SpeakingHandler(),PhaseMetadata.PHASE_POLICY_DETERMINATION);
//        Utils.addHandler(flow, new SpeakingHandler(),PhaseMetadata.PHASE_POLICY_DETERMINATION);
//        Utils.addHandler(flow, new SpeakingHandler(),PhaseMetadata.PHASE_POLICY_DETERMINATION);
//        Utils.addHandler(flow, new SpeakingHandler(),PhaseMetadata.PHASE_POLICY_DETERMINATION);
//        Utils.addHandler(flow, culprit,PhaseMetadata.PHASE_POLICY_DETERMINATION);
//        Utils.addHandler(flow, new SpeakingHandler(),PhaseMetadata.PHASE_POLICY_DETERMINATION);
//        service.setInFlow(flow);
//
//        AxisOperation operation = new AxisOperation(operationName);
//        service.addOperation(operation);
//
//        UtilServer.start();
//        UtilServer.deployService(service);
//        try {
//            callTheService();
//        } finally {
//            UtilServer.unDeployService(serviceName);
//            UtilServer.stop();
//        }
//    }


    protected void tearDown() throws Exception {

    }


    private void callTheService() throws Exception {
        try {
            SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();

            OMNamespace omNs = fac.createOMNamespace("http://localhost/my",
                    "my");
            OMElement method = fac.createOMElement("echoOMElement", omNs);
            OMElement value = fac.createOMElement("myValue", omNs);
            value.setText("Isaac Assimov, the foundation Sega");
            method.addChild(value);

            String clientHome = "target/test-resources/integrationRepo";

            Options options = new Options();
            options.setListenerTransportProtocol(Constants.TRANSPORT_HTTP);
            options.setTo(targetEPR);

            Call call = new Call(clientHome);
            call.setClientOptions(options);

            OMElement result = call.invokeBlocking(
                    operationName.getLocalPart(), method);
            result.serializeAndConsume(XMLOutputFactory.newInstance().createXMLStreamWriter(
                    System.out));
            fail("the test must fail due to bad service Name");
        } catch (AxisFault e) {
            log.info(e.getMessage());
            String message = e.getMessage();
            assertTrue((message.indexOf(UtilServer.FAILURE_MESSAGE)) >= 0);
            return;
        }

    }

    private Handler culprit = new AbstractHandler() {
        public void invoke(MessageContext msgContext) throws AxisFault {
            throw new AxisFault(UtilServer.FAILURE_MESSAGE);
        }
    };
}

