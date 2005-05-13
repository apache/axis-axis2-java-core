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

import org.apache.axis.context.MessageContext;
import org.apache.axis.context.OperationContext;
import org.apache.axis.context.ConfigurationContext;
import org.apache.axis.description.OperationDescription;
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
    private ConfigurationContext engineContext;

    /**
     * Constructor AxisEngine
     *
     *
     */
    public AxisEngine(ConfigurationContext engineContext) {
        log.info("Axis Engine Started");
        this.engineContext = engineContext;
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
    public void send(MessageContext msgContext) throws AxisFault {
        try {
            verifyContextBuilt(msgContext);
            OperationContext operationContext = msgContext.getOperationContext();

            ArrayList phases = operationContext.getAxisOperation().getPhasesOutFlow();
            invokePhases(phases, msgContext);

            TransportSender sender = msgContext.getTransportOut().getSender();
            sender.invoke(msgContext);
        } catch (Throwable e) {
            handleFault(msgContext, e);
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
            ConfigurationContext sysCtx = context.getSystemContext();
            ArrayList phases = sysCtx.getEngineConfig().getInPhasesUptoAndIncludingPostDispatch();
            invokePhases(phases, context);
            
            OperationContext operationContext = context.getOperationContext();
            phases = operationContext.getAxisOperation().getPhasesOutFlow();
            invokePhases(phases, context);
            if (context.isServerSide()) {
                // add invoke Phase
                MessageReceiver reciver =
                    context.getOperationContext().getAxisOperation().getMessageReciever();
                reciver.recieve(context);
            }
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
    public void handleFault(MessageContext context, Throwable e) throws AxisFault {
        boolean serverSide = context.isServerSide();
        log.error("Error Ocurred", e);
        if (serverSide && !context.isProcessingFault()) {
            context.setProcessingFault(true);

            // create a SOAP envelope with the Fault
            MessageContext faultContext =
                new MessageContext(
                    context.getSessionContext(),
                    context.getTransportIn(),
                    context.getTransportOut(),
                    engineContext);
            faultContext.setOperationContext(context.getOperationContext());
            faultContext.setProcessingFault(true);
            faultContext.setServerSide(true);
            SOAPEnvelope envelope = OMAbstractFactory.getSOAP11Factory().getDefaultEnvelope();

            // TODO do we need to set old Headers back?
            SOAPBody body = envelope.getBody();
            e.printStackTrace();
            body.addFault(new AxisFault(e.getMessage(), e));
            faultContext.setEnvelope(envelope);

            OperationContext opContext  = context.getOperationContext();
            if(opContext != null){
                OperationDescription axisOperation = opContext.getAxisOperation();
                ArrayList phases = axisOperation.getPhasesOutFaultFlow();
                invokePhases(phases, context);
            }
            // Write the the error
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

    private void verifyContextBuilt(MessageContext msgctx) throws AxisFault {
        if (msgctx.getSystemContext() == null) {
            throw new AxisFault("ConfigurationContext can not be null");
        }
        if (msgctx.getOperationContext() == null) {
            throw new AxisFault("OperationContext can not be null");
        }
        if (msgctx.getServiceContext() == null) {
            throw new AxisFault("ServiceContext can not be null");
        }
    }

    private void invokePhases(ArrayList phases, MessageContext msgctx) throws AxisFault {
        int count = phases.size();
        for (int i = 0; i < count; i++) {
            Phase phase = (Phase) phases.get(i);
            phase.invoke(msgctx);
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
    public Object store(ConfigurationContext context, Object obj) {
        return context.getStorage().put(obj);
    }

    /**
     * retrieves an object from the underlying storage
     * @see #store(org.apache.axis.context.EngineContext, Object)
     * @param context
     * @param key
     * @return
     */
    public Object retrieve(ConfigurationContext context, Object key) {
        return context.getStorage().get(key);
    }

    /**
     * removes an object from the underlying storage
     * @param context
     * @param key
     * @return  the object removed
     */
    public Object remove(ConfigurationContext context, Object key) {
        return context.getStorage().remove(key);
    }

    /**
     * Clears the underlying storage
     * @param context
     * @return
     */
    public boolean clearStorage(ConfigurationContext context) {
        return context.getStorage().clean();
    }
}
