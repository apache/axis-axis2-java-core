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
import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.util.TestConstants;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.integration.UtilServerBasedTestCase;
import org.apache.axis2.phaseresolver.PhaseMetadata;
import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.stream.XMLOutputFactory;
import java.util.ArrayList;


public class HandlerFailureTest extends UtilServerBasedTestCase implements TestConstants {
	private static final Log log = LogFactory.getLog(HandlerFailureTest.class);

    public HandlerFailureTest() {
        super(HandlerFailureTest.class.getName());
    }

    public HandlerFailureTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return getTestSetup(new TestSuite(HandlerFailureTest.class));
    }


    public void testFailureAtServerRequestFlow() throws Exception {

        AxisService service = Utils.createSimpleService(serviceName,
                Echo.class.getName(),
                operationName);

        UtilServer.deployService(service);
        AxisOperation operation = service.getOperation(operationName);
        ArrayList phasec = new ArrayList();
        phasec.add(new Phase(PhaseMetadata.PHASE_POLICY_DETERMINATION));
        operation.setRemainingPhasesInFlow(phasec);
        ArrayList phase = operation.getRemainingPhasesInFlow();
        for (int i = 0; i < phase.size(); i++) {
            Phase phase1 = (Phase) phase.get(i);
            if (PhaseMetadata.PHASE_POLICY_DETERMINATION.equals(phase1.getPhaseName())) {
                phase1.addHandler(culprit);
            }
        }
        try {
            callTheService();
        } finally {
            UtilServer.unDeployService(serviceName);
        }
    }

    private void callTheService() throws Exception {
        try {
            SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();

            OMNamespace omNs = fac.createOMNamespace("http://localhost/my",
                    "my");
            OMElement method = fac.createOMElement("echoOMElement", omNs);
            OMElement value = fac.createOMElement("myValue", omNs);
            value.setText("Isaac Asimov, The Foundation Trilogy");
            method.addChild(value);
            Options options = new Options();
            options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
            options.setTo(targetEPR);
            ConfigurationContext configContext =
                    ConfigurationContextFactory.createConfigurationContextFromFileSystem(null,null);
            ServiceClient sender = new ServiceClient(configContext, null);
            sender.setOptions(options);

            OMElement result = sender.sendReceive(method);


            result.serializeAndConsume(StAXUtils.createXMLStreamWriter(
                    System.out));
            fail("the test must fail due to the intentional failure of the \"culprit\" handler");
        } catch (AxisFault e) {
            log.info(e.getMessage());
            String message = e.getMessage();
            assertTrue((message.indexOf(UtilServer.FAILURE_MESSAGE)) >= 0);
        }

    }

    private Handler culprit = new AbstractHandler() {
        public InvocationResponse invoke(MessageContext msgContext) throws AxisFault {
            throw new AxisFault(UtilServer.FAILURE_MESSAGE);
        }
    };
}

