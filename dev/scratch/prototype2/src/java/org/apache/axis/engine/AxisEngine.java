/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

import org.apache.axis.context.MessageContext;
import org.apache.axis.description.AxisGlobal;
import org.apache.axis.description.AxisService;
import org.apache.axis.description.AxisTransport;
import org.apache.axis.om.OMFactory;
import org.apache.axis.om.SOAPEnvelope;
import org.apache.axis.transport.TransportSenderLocator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * There is one engine for the Server and the Client. the send() and receive()
 * Methods are the basic operations the Sync, Async messageing are build on top.
 */
public class AxisEngine {
    private Log log = LogFactory.getLog(getClass());
    private EngineRegistry registry;

    public AxisEngine(EngineRegistry registry) {
        log.info("Axis Engine Started");
        this.registry = registry;
    }

    /**
     * This methods represents the outflow of the Axis, this could be either at the server side or the client side.
     * Here the <code>ExecutionChain</code> is created using the Phases. The Handlers at the each Phases is ordered in 
     * deployment time by the deployment module
     * @see MessageContext
     * @see ExecutionChain
     * @see Phase
     * @see Handler
     * @param context
     * @throws AxisFault
     */
    public void send(MessageContext context) throws AxisFault {
        executeOutFlow(context, EngineRegistry.OUTFLOW);
        log.info("end the send()");
    }

    /**
     * This methods represents the inflow of the Axis, this could be either at the server side or the client side.
     * Here the <code>ExecutionChain</code> is created using the Phases. The Handlers at the each Phases is ordered in 
     * deployment time by the deployment module
     * @see MessageContext
     * @see ExecutionChain
     * @see Phase
     * @see Handler
     * @param context
     * @throws AxisFault
     */

    public void receive(MessageContext context) throws AxisFault {
        try {
            //          org.TimeRecorder.START = System.currentTimeMillis();
            log.info("starting the out flow");
            //let us always start with a fresh EC
            context.setExecutionChain(new ExecutionChain());
            ExecutionChain chain = context.getExecutionChain();

            // Receiving is always a matter of running the transport handlers first
            AxisTransport transport = context.getTransport();
            if (transport != null) {
                log.info("Using the transport" + transport.getName());
                chain.addPhases(transport.getPhases(EngineRegistry.INFLOW));
            }
            //Add the phases that are are at Global scope
            AxisGlobal global =
                context.getGlobalContext().getRegistry().getGlobal();
            chain.addPhases(global.getPhases(EngineRegistry.INFLOW));

            //create a Dispatch Phase and add it to the Execution Chain
            Dispatcher dispatcher = new Dispatcher();
            Phase dispatchPhase = new Phase("DispatchPhase");
            dispatchPhase.addHandler(dispatcher);
            chain.addPhase(dispatchPhase);

            //Start rolling the Service Handlers will,be added by the Dispatcher 
            chain.invoke(context);
            log.info("ending the out flow");
            //            org.TimeRecorder.END = System.currentTimeMillis();
            //            org.TimeRecorder.dump();
        } catch (Throwable e) {
            handleFault(context, e);
        }
    }

    /**
     * If error occurs at inflow or the out flow this method will call to handle the error. But if the 
     * execution reach this method twice, means the sending the error handling failed an in that case the 
     * this method just log the error and exit</p> 
     * @param context
     * @param e
     * @throws AxisFault
     */
    private void handleFault(MessageContext context, Throwable e)
        throws AxisFault {
        boolean serverSide = context.isServerSide();
        log.error("Error Ocurred", e);
        if (serverSide && !context.isProcessingFault()) {
            context.setProcessingFault(true);

            //create a SOAP envelope with the Fault
            SOAPEnvelope envelope =
                OMFactory.newInstance().getDefaultEnvelope();
            //TODO do we need to set old Headers back?
            envelope.getBody().addFault(new AxisFault(e.getMessage(), e));
            context.setEnvelope(envelope);
            //send the error
            executeOutFlow(context, EngineRegistry.FAULTFLOW);
        } else if (!serverSide) {
            //if at the client side throw the exception
            throw new AxisFault("", e);
        } else {
            //TODO log and exit
            log.error("Error in fault flow", e);
        }
    }
    
    /**
     *  <p>This method shows the execution of sending the SOAP message. That can be either a 
     * sending the message at the Client side or the sending the response at the Server side or the 
     * Seding the fault flow at the Server side.</p>  
     * @param context
     * @param flow
     * @throws AxisFault
     */
    private void executeOutFlow(MessageContext context, int flow)
        throws AxisFault {
        try {
            context.setExecutionChain(new ExecutionChain());
            ExecutionChain chain = context.getExecutionChain();

            AxisService service = context.getService();
            if (service != null) {
                //what are we suppose to do in the client side 
                //how the client side handlers are deployed ??? this is a hack and no client side handlers
                chain.addPhases(service.getPhases(flow));
            } else {
                if (context.isServerSide() && !context.isProcessingFault()) {
                    throw new AxisFault("in Server Side there must be service object");
                }
            }
            //Add the phases that are are at Global scope
            AxisGlobal global =
                context.getGlobalContext().getRegistry().getGlobal();
            chain.addPhases(global.getPhases(flow));

            // Receiving is always a matter of running the transport handlers first
            AxisTransport transport = context.getTransport();
            if (transport != null) {
                chain.addPhases(transport.getPhases(flow));
            }
            Phase sendPhase = new Phase(Phase.SENDING_PHASE);
            sendPhase.addHandler(TransportSenderLocator.locate(context));
            chain.addPhase(sendPhase);
            //startet rolling
            chain.invoke(context);
        } catch (AxisFault error) {
            handleFault(context, error);
        }
    }
}
