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

import java.util.ArrayList;

import javax.xml.namespace.QName;

import org.apache.axis.addressing.AddressingConstants;
import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.context.MessageContext;
import org.apache.axis.context.OperationContextFactory;
import org.apache.axis.context.SystemContext;
import org.apache.axis.description.AxisGlobal;
import org.apache.axis.description.AxisOperation;
import org.apache.axis.description.AxisService;
import org.apache.axis.description.AxisTransportIn;
import org.apache.axis.description.AxisTransportOut;
import org.apache.axis.om.OMAbstractFactory;
import org.apache.axis.om.SOAPFactory;
import org.apache.wsdl.WSDLConstants;
import org.apache.wsdl.WSDLService;

public class EngineWithoutPhaseResolvingTest extends AbstractEngineTest {
    private MessageContext mc;
    private ArrayList executedHandlers = new ArrayList();
    private AxisSystem engineRegistry;
    private QName serviceName = new QName("axis/services/NullService");
    private QName opearationName = new QName("NullOperation");
    private AxisService service;
    private SystemContext engineContext;

    public EngineWithoutPhaseResolvingTest() {
    }

    public EngineWithoutPhaseResolvingTest(String arg0) {
        super(arg0);
    }
    protected void setUp() throws Exception {

        engineRegistry = new AxisSystemImpl(new AxisGlobal());
        engineContext = new SystemContext(engineRegistry);
        AxisTransportOut transport = new AxisTransportOut(new QName("null"));
        transport.setSender(new NullTransportSender());

        AxisTransportIn transportIn = new AxisTransportIn(new QName("null"));
        AxisOperation axisOp = new AxisOperation(opearationName);

        mc =
            new MessageContext(
                engineContext,
               null,
                transportIn,
                transport,
        OperationContextFactory.createMEPContext(WSDLConstants.MEP_CONSTANT_IN_OUT,false,axisOp, null));
        mc.setTransportOut(transport);
        mc.setServerSide(true);
        SOAPFactory omFac = OMAbstractFactory.getSOAP11Factory();
        mc.setEnvelope(omFac.getDefaultEnvelope());
        service = new AxisService(serviceName);
        axisOp.setMessageReciever(new NullMessageReceiver());
        engineRegistry.addService(service);
        service.setStyle(WSDLService.STYLE_DOC);
        service.addOperation(axisOp);

        mc.setTo(new EndpointReference(AddressingConstants.WSA_TO, "axis/services/NullService"));
        mc.setWSAAction(opearationName.getLocalPart());

    }

    public void testServerSend() throws Exception {
        AxisEngine engine = new AxisEngine(engineContext);
        mc.setServerSide(true);
        fail();
       //TODO mc.setServiceContext(new ServiceContext(service, engineContext));
        engine.send(mc);
    }

    public void testClientSend() throws Exception {
        AxisEngine engine = new AxisEngine(engineContext);
        mc.setServerSide(false); fail();
        //TODO mc.setServiceContext(new ServiceContext(service,engineContext));
        engine.send(mc);
    }

    public void testServerReceive() throws Exception {
        AxisEngine engine = new AxisEngine(engineContext);
        mc.setServerSide(true);
        engine.receive(mc);
    }

    public void testClientReceive() throws Exception {
        AxisEngine engine = new AxisEngine(engineContext);
        mc.setServerSide(false);
        engine.receive(mc);
    }
}
