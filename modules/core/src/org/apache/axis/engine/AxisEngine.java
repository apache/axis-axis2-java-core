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

import org.apache.axis.context.EngineContext;
import org.apache.axis.context.MessageContext;
import org.apache.axis.context.ServiceContext;
import org.apache.axis.description.AxisTransportIn;
import org.apache.axis.handlers.addressing.AddressingInHandler;
import org.apache.axis.handlers.addressing.AddressingOutHandler;
import org.apache.axis.om.OMAbstractFactory;
import org.apache.axis.om.SOAPBody;
import org.apache.axis.om.SOAPEnvelope;
import org.apache.axis.transport.TransportSender;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * There is one engine for the Server and the Client. the send() and receive()
 * Methods are the basic operations the Sync, Async messageing are build on top.
 */
public class AxisEngine {
    /**
     * Field log
     */
    private Log log = LogFactory.getLog(getClass());

    /**
     * Constructor AxisEngine
     *
     *
     */
    public AxisEngine() {
        log.info("Axis Engine Started");
    }

    /**
     * This methods represents the outflow of the Axis, this could be either at the server side or the client side.
     * Here the <code>ExecutionChain</code> is created using the Phases. The Handlers at the each Phases is ordered in
     * deployment time by the deployment module
     *
     * @param context
     * @throws AxisFault
     * @see MessageContext
     * @see ExecutionChain
     * @see Phase
     * @see Handler
     */
    public void send(MessageContext context) throws AxisFault {
        try {
            if(context.getOperationContext() == null){
                throw new AxisFault("Out flow must have a MEPContext set on the MessageContext");
            }
            
            
            ExecutionChain chain = context.getExecutionChain();
            ServiceContext serviceContext = context.getOperationContext().getServiceContext();;

            /*
             * There is a two cases, at the server side(response) / client side
             * but in the server side there must be a Service object object set, as before the 
             * out flow is started the user knows the services that will be invoked. 
             */

            if (serviceContext != null) {

                // what are we suppose to do in the client side
                // how the client side handlers are deployed ??? this is a hack and no client side handlers
                chain.addPhases(
                    serviceContext.getPhases(EngineConfiguration.OUTFLOW));
            } else {
                if (context.isServerSide() && !context.isProcessingFault()) {
                    throw new AxisFault("At the Send there must be a Service Object set at the Server Side");
                }
            }

            // Add the phases that are are at Global scope
            chain.addPhases(
                context.getEngineContext().getPhases(
                    EngineConfiguration.OUTFLOW));
            Phase addressingPhase = new SimplePhase("addressing");
            addressingPhase.addHandler(new AddressingOutHandler());
            chain.addPhase(addressingPhase);

            // Receiving is always a matter of running the transport handlers first

            AxisTransportIn transport = context.getTransportIn();
            chain.addPhases(transport.getPhases(EngineConfiguration.OUTFLOW));

            // startet rolling
            chain.invoke(context);

            TransportSender sender = context.getTransportOut().getSender();
            sender.invoke(context);

        } catch (AxisFault error) {
            //error.printStackTrace();
            handleFault(context, error);
        }

    }

    /**
     * This methods represents the inflow of the Axis, this could be either at the server side or the client side.
     * Here the <code>ExecutionChain</code> is created using the Phases. The Handlers at the each Phases is ordered in
     * deployment time by the deployment module
     *
     * @param context
     * @throws AxisFault
     * @see MessageContext
     * @see ExecutionChain
     * @see Phase
     * @see Handler
     */
    public void receive(MessageContext context) throws AxisFault {
        try {

            log.info("starting the out flow");

            EngineContext engContext = context.getEngineContext();

            // let us always start with a fresh EC
            ExecutionChain chain = context.getExecutionChain();

            // Construct the transport part of the Handlers
            AxisTransportIn transport = context.getTransportIn();
            if (transport != null) {
                log.info("Using the transport" + transport.getName());
                chain.addPhases(
                    transport.getPhases(EngineConfiguration.INFLOW));
            }
            
            Phase addressingPhase = new SimplePhase("addressing");
            addressingPhase.addHandler(new AddressingInHandler());
            chain.addPhase(addressingPhase);
            //add the Global flow
            chain.addPhases(engContext.getPhases(EngineConfiguration.INFLOW));

            // create a Dispatch Phase and add it to the Execution Chain
            Phase dispatchPhase = chain.getPhase(SimplePhase.DISPATCH_PHASE);
            if (dispatchPhase == null) {
                dispatchPhase = new SimplePhase(SimplePhase.DISPATCH_PHASE);
            }

            if (context.isServerSide()) {
                //This chain is the default Service diaptacher, the users may opt to overide this by 
                //adding an Handlers to the DispatchPhase. 
                dispatchPhase.addHandler(new RequestURIBasedDispatcher());
                AddressingBasedDispatcher dispatcher =
                    new AddressingBasedDispatcher();
                dispatchPhase.addHandler(dispatcher);

            }

            //Service handlers are added to ExecutionChain by this Handler
            ServiceHandlersChainBuilder handlerChainBuilder =
                new ServiceHandlersChainBuilder();
            dispatchPhase.addHandler(handlerChainBuilder);
            chain.addPhase(dispatchPhase);

            // Start rolling the Service Handlers will,be added by the Dispatcher
            chain.invoke(context);

            if (context.isServerSide()) {
                // add invoke Phase
                MessageReceiver reciver =
                    context.getoperationConfig().getMessageReciever();
                reciver.recieve(context);
            }

            log.info("ending the out flow");
        } catch (Throwable e) {
            handleFault(context, e);
            
        }
    }

    /**
     * If error occurs at inflow or the out flow this method will call to handle the error. But if the
     * execution reach this method twice, means the sending the error handling failed an in that case the
     * this method just log the error and exit</p>
     *
     * @param context
     * @param e
     * @throws AxisFault
     */
    public void handleFault(MessageContext context, Throwable e)
        throws AxisFault {
        boolean serverSide = context.isServerSide();
        log.error("Error Ocurred", e);
        if (serverSide && !context.isProcessingFault()) {
            context.setProcessingFault(true);

            // create a SOAP envelope with the Fault
            MessageContext faultContext =
                new MessageContext(
                    context.getEngineContext(),
                    context.getProperties(),
                    context.getSessionContext(),
                    context.getTransportIn(),
                    context.getTransportOut(),context.getOperationContext());
            faultContext.setProcessingFault(true);
            faultContext.setServerSide(true);
            SOAPEnvelope envelope =
                OMAbstractFactory.getSOAP11Factory().getDefaultEnvelope();

            // TODO do we need to set old Headers back?
            SOAPBody body = envelope.getBody();
            e.printStackTrace();
            body.addFault(new AxisFault(e.getMessage(), e));
            faultContext.setEnvelope(envelope);

            ExecutionChain chain = faultContext.getExecutionChain();

            ServiceContext serviceContext = context.getOperationContext().getServiceContext();
            if (serviceContext != null) {
                chain.addPhases(
                    serviceContext.getPhases(
                        EngineConfiguration.FAULT_IN_FLOW));
            }

            chain.invoke(faultContext);
            // send the error
            TransportSender sender = context.getTransportOut().getSender();
            sender.invoke(faultContext);
        } else if (!serverSide) {

            // if at the client side throw the exception
            throw new AxisFault("", e);
        } else {

            // TODO log and exit
            log.error("Error in fault flow", e);
        }
    }

    /* --------------------------------------------------------------------------------------------*/
    /* -----------------   Methods related to storage ----------------------------------------------*/
    /**
     * Stores an object in the underlying storage
     * @param context The relevant engine context
     * @param obj the object to be stored
     * @return the storage key
     */
    public Object store(EngineContext context, Object obj) {
        return context.getStorage().put(obj);
    }

    /**
     * retrieves an object from the underlying storage
     * @see #store(org.apache.axis.context.EngineContext, Object)
     * @param context
     * @param key
     * @return
     */
    public Object retrieve(EngineContext context, Object key) {
        return context.getStorage().get(key);
    }

    /**
     * removes an object from the underlying storage
     * @param context
     * @param key
     * @return  the object removed
     */
    public Object remove(EngineContext context, Object key) {
        return context.getStorage().remove(key);
    }

    /**
     * Clears the underlying storage
     * @param context
     * @return
     */
    public boolean clearStorage(EngineContext context) {
        return context.getStorage().clean();
    }
}
