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
import org.apache.axis.context.EngineContext;
import org.apache.axis.context.MessageContext;
import org.apache.axis.context.ServiceContext;
import org.apache.axis.description.AxisGlobal;
import org.apache.axis.description.AxisService;
import org.apache.axis.description.AxisTransportIn;
import org.apache.axis.description.AxisTransportOut;
import org.apache.axis.om.OMFactory;
import org.apache.wsdl.WSDLService;

public class EnginePausingTest extends AbstractEngineTest {
    private MessageContext mc;
    private ArrayList executedHandlers = new ArrayList();
    private EngineConfiguration engineRegistry;
    private QName serviceName = new QName("NullService");

    public EnginePausingTest() {
    }

    public EnginePausingTest(String arg0) {
        super(arg0);
    }
    protected void setUp() throws Exception {
        engineRegistry = new EngineConfigurationImpl(new AxisGlobal());

        AxisTransportOut transportOut = new AxisTransportOut(new QName("null"));
        transportOut.setSender(new NullTransportSender());

        AxisTransportIn transportIn = new AxisTransportIn(new QName("null"));
        
        EngineContext engineContext = new EngineContext(engineRegistry);

        mc = new MessageContext(engineContext, null, null, transportIn,transportOut);
        mc.setTransportOut(transportOut);
        mc.setServerSide(true);
        OMFactory omFac = OMFactory.newInstance();
        mc.setEnvelope(omFac.getDefaultEnvelope());
        AxisService service = new AxisService(serviceName);
        service.setMessageReceiver(new NullMessageReceiver());
        ArrayList phases = new ArrayList();

        SimplePhase phase = new SimplePhase("1");
        phase.addHandler(new TempHandler(1));
        phase.addHandler(new TempHandler(2));
        phase.addHandler(new TempHandler(3));
        phase.addHandler(new TempHandler(4));
        phase.addHandler(new TempHandler(5));
        phase.addHandler(new TempHandler(6));
        phase.addHandler(new TempHandler(7));
        phase.addHandler(new TempHandler(8));
        phase.addHandler(new TempHandler(9));
        phases.add(phase);

        phase = new SimplePhase("2");
        phase.addHandler(new TempHandler(10));
        phase.addHandler(new TempHandler(11));
        phase.addHandler(new TempHandler(12));
        phase.addHandler(new TempHandler(13));
        phase.addHandler(new TempHandler(14));
        phase.addHandler(new TempHandler(15, true));
        phase.addHandler(new TempHandler(16));
        phase.addHandler(new TempHandler(17));
        phase.addHandler(new TempHandler(18));
        phases.add(phase);

        SimplePhase phase1 = new SimplePhase("3");
        phase1.addHandler(new TempHandler(19));
        phase1.addHandler(new TempHandler(20));
        phase1.addHandler(new TempHandler(21));
        phase1.addHandler(new TempHandler(22));
        phase1.addHandler(new TempHandler(23));
        phase1.addHandler(new TempHandler(24));
        phase1.addHandler(new TempHandler(25));
        phase1.addHandler(new TempHandler(26));
        phase1.addHandler(new TempHandler(27));
        phases.add(phase1);
        
        ServiceContext serviceContext = new ServiceContext(service);
        engineContext.addService(serviceContext);
        
        serviceContext.setPhases(phases, EngineConfiguration.INFLOW);
        engineRegistry.addService(service);
        service.setStyle(WSDLService.STYLE_DOC);
        mc.setTo(
            new EndpointReference(
                AddressingConstants.WSA_TO,
                "http://127.0.0.1:8080/axis/services/NullService"));

    }

    public void testSend() throws Exception {
        AxisEngine engine = new AxisEngine();
        engine.receive(mc);
        assertEquals(executedHandlers.size(), 15);
        for (int i = 0; i < 15; i++) {
            assertEquals(((Integer) executedHandlers.get(i)).intValue(), i + 1);
        }
        mc.setPaused(false);
        engine.receive(mc);

        assertEquals(executedHandlers.size(), 27);
        for (int i = 15; i < 27; i++) {
            assertEquals(((Integer) executedHandlers.get(i)).intValue(), i + 1);
        }

    }
}
