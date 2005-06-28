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

import org.apache.axis.context.ConfigurationContext;
import org.apache.axis.context.MessageContext;
import org.apache.axis.context.OperationContext;
import org.apache.axis.description.OperationDescription;
import org.apache.axis.description.TransportOutDescription;
import org.apache.axis.om.OMAbstractFactory;
import org.apache.axis.om.OMOutput;
import org.apache.axis.soap.SOAPBody;
import org.apache.axis.soap.SOAPEnvelope;
import org.apache.axis.soap.impl.llom.SOAPProcessingException;
import org.apache.axis.transport.TransportSender;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;

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
            if (msgContext.isPaused()) {
                resumeInvocationPhases(phases, msgContext);
            } else {
                invokePhases(phases, msgContext);
            }

            TransportOutDescription transportOut = msgContext.getTransportOut();

            TransportSender sender = transportOut.getSender();
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
    public void receive(MessageContext msgContext) throws AxisFault {
        boolean paused = msgContext.isPaused();
        try {
            ConfigurationContext sysCtx = msgContext.getSystemContext();
            ArrayList phases =
                sysCtx.getAxisConfiguration().getInPhasesUptoAndIncludingPostDispatch();

            if (paused) {
                resumeInvocationPhases(phases, msgContext);
            } else {
                invokePhases(phases, msgContext);
            }
            verifyContextBuilt(msgContext);

            OperationContext operationContext = msgContext.getOperationContext();
            OperationDescription operationDescription = operationContext.getAxisOperation();
            phases = operationDescription.getRemainingPhasesInFlow();
            if (paused) {
                resumeInvocationPhases(phases, msgContext);
            } else {
                invokePhases(phases, msgContext);
            }
            if (msgContext.isServerSide()) {
                // add invoke Phase
                MessageReceiver reciver = operationDescription.getMessageReciever();
                reciver.recieve(msgContext);
            }
        } catch (Throwable e) {
            handleFault(msgContext, e);
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
        e.printStackTrace();
        boolean serverSide = context.isServerSide();
        log.error("Error Ocurred", e);
        if (serverSide && !context.isProcessingFault()) {
            context.setProcessingFault(true);

            // create a SOAP envelope with the Fault
            MessageContext faultContext =
                new MessageContext(
                    engineContext,
                    context.getSessionContext(),
                    context.getTransportIn(),
                    context.getTransportOut());

            if (context.getFaultTo() != null) {
                faultContext.setFaultTo(context.getFaultTo());
            } else {
                Object writer = context.getProperty(MessageContext.TRANSPORT_OUT);
                if (writer != null) {
                    faultContext.setProperty(MessageContext.TRANSPORT_OUT, writer);
                } else {
                    //TODO Opps there are no place to send this, we will log and should we throw the exception? 
                    log.error("Error in fault flow", e);
                    e.printStackTrace();
                }
            }

            faultContext.setOperationContext(context.getOperationContext());
            faultContext.setProcessingFault(true);
            faultContext.setServerSide(true);
            SOAPEnvelope envelope = null;
            try {
                envelope = OMAbstractFactory.getSOAP11Factory().getDefaultEnvelope();
            } catch (SOAPProcessingException e1) {
                throw new AxisFault(e1);
            }

            // TODO do we need to set old Headers back?
            SOAPBody body = envelope.getBody();
            e.printStackTrace();
            body.addFault(new AxisFault(e.getMessage(), e));

            faultContext.setEnvelope(envelope);

            OperationContext opContext = context.getOperationContext();
            if (opContext != null) {
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
        for (int i = 0; (i < count && !msgctx.isPaused()); i++) {
            Phase phase = (Phase) phases.get(i);
            phase.invoke(msgctx);
        }
    }

    public void resumeInvocationPhases(ArrayList phases, MessageContext msgctx) throws AxisFault {
        msgctx.setPausedFalse();
        int count = phases.size();
        boolean foudMatch = false;

        for (int i = 0; i < count && !msgctx.isPaused(); i++) {
            Phase phase = (Phase) phases.get(i);
            if (phase.getPhaseName().equals(msgctx.getPausedPhaseName())) {
                foudMatch = true;
                phase.invokeStartFromHandler(msgctx.getPausedHandlerName(), msgctx);
            } else {
                if (foudMatch) {
                    phase.invoke(msgctx);
                }

            }
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
