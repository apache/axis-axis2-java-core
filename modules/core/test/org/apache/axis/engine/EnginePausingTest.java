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

import junit.framework.TestCase;
import org.apache.axis.addressing.AddressingConstants;
import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.context.ConfigurationContext;
import org.apache.axis.context.MessageContext;
import org.apache.axis.context.ServiceContext;
import org.apache.axis.description.OperationDescription;
import org.apache.axis.description.ServiceDescription;
import org.apache.axis.description.TransportInDescription;
import org.apache.axis.description.TransportOutDescription;
import org.apache.axis.handlers.AbstractHandler;
import org.apache.axis.om.OMAbstractFactory;
import org.apache.axis.soap.SOAPFactory;
import org.apache.axis.transport.http.HTTPTransportSender;
import org.apache.wsdl.WSDLService;

import javax.xml.namespace.QName;
import java.io.OutputStreamWriter;
import java.util.ArrayList;


public class EnginePausingTest extends TestCase {

    private QName serviceName = new QName("NullService");
    private QName operationName = new QName("DummyOp");
    private ConfigurationContext engineContext;

    private TransportOutDescription transportOut;
    private TransportInDescription transportIn;
    private MessageContext mc;
    private ArrayList executedHandlers;

    public EnginePausingTest(String arg0) throws AxisFault {
        super(arg0);
        executedHandlers = new ArrayList();
        AxisConfiguration engineRegistry = new AxisSystemImpl();
        engineContext = new ConfigurationContext(engineRegistry);
        transportOut = new TransportOutDescription(new QName("null"));
        transportOut.setSender(new HTTPTransportSender());
        transportIn = new TransportInDescription(new QName("null"));

    }

    protected void setUp() throws Exception {

        ServiceDescription service = new ServiceDescription(serviceName);
        service.setStyle(WSDLService.STYLE_DOC);
        engineContext.getEngineConfig().addService(service);

        OperationDescription axisOp = new OperationDescription(operationName);
        axisOp.setMessageReciever(new MessageReceiver() {
            public void recieve(MessageContext messgeCtx) throws AxisFault {

            }
        });
        service.addOperation(axisOp);

        mc = new MessageContext(null, transportIn, transportOut, engineContext);

        mc.setTransportOut(transportOut);
        mc.setServerSide(true);
        mc.setProperty(MessageContext.TRANSPORT_WRITER, new OutputStreamWriter(System.out));
        SOAPFactory omFac = OMAbstractFactory.getSOAP11Factory();
        mc.setEnvelope(omFac.getDefaultEnvelope());

        ArrayList phases = new ArrayList();

        Phase phase = new Phase("1");
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

        phase = new Phase("2");
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

        Phase phase1 = new Phase("3");
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

        ServiceContext serviceContext = new ServiceContext(service, engineContext);
        engineContext.registerServiceContext(serviceContext.getServiceInstanceID(), serviceContext);

        //TODO
        axisOp.getRemainingPhasesInFlow().addAll(phases);

        mc.setWSAAction(operationName.getLocalPart());
        System.out.flush();

    }

    public void testReceive() throws Exception {
        mc.setTo(new EndpointReference(AddressingConstants.WSA_TO, "axis/services/NullService/DummyOp"));
        AxisEngine engine = new AxisEngine(engineContext);
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

    public class TempHandler extends AbstractHandler {
        private Integer index;
        private boolean pause = false;

        public TempHandler(int index, boolean pause) {
            this.index = new Integer(index);
            this.pause = pause;
        }

        public TempHandler(int index) {
            this.index = new Integer(index);
        }

        public void invoke(MessageContext msgContext) throws AxisFault {
            executedHandlers.add(index);
            if (pause) {
                msgContext.setPaused(true);
            }
        }

    }
}
