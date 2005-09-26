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

import org.apache.axis2.AxisFault;
import org.apache.axis2.SOAPFaultException;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.description.OperationDescription;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.soap.SOAP11Constants;
import org.apache.axis2.soap.SOAP12Constants;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.SOAPFault;
import org.apache.axis2.soap.SOAPFaultCode;
import org.apache.axis2.soap.SOAPFaultDetail;
import org.apache.axis2.soap.SOAPFaultReason;
import org.apache.axis2.soap.impl.llom.SOAPProcessingException;
import org.apache.axis2.transport.TransportSender;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
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
        verifyContextBuilt(msgContext);

        //find and invoke the Phases        
        OperationContext operationContext = msgContext.getOperationContext();
        ArrayList phases =
            operationContext.getAxisOperation().getPhasesOutFlow();
        if (msgContext.isPaused()) {
            // the message has paused, so rerun them from the position they stoped. The Handler
            //who paused the Message will be the first one to run
            //resume fixed, global precalulated phases
            resumeInvocationPhases(phases, msgContext);
        } else {
            invokePhases(phases, msgContext);
        }

        if (!msgContext.isPaused()) {
            //write the Message to the Wire
            TransportOutDescription transportOut = msgContext.getTransportOut();
            TransportSender sender = transportOut.getSender();
            sender.invoke(msgContext);
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

        ConfigurationContext sysCtx = msgContext.getSystemContext();
        OperationDescription operationDescription = null;
        ArrayList preCalculatedPhases =
            sysCtx
                .getAxisConfiguration()
                .getInPhasesUptoAndIncludingPostDispatch();
        ArrayList operationSpecificPhases = null;

        if (msgContext.isPaused()) {
            // the message has paused, so rerun them from the position they stoped. The Handler
            //who paused the Message will be the first one to run
            //resume fixed, global precalulated phases
            resumeInvocationPhases(preCalculatedPhases, msgContext);
            verifyContextBuilt(msgContext);
            //resume operation specific phases
            OperationContext operationContext =
                msgContext.getOperationContext();
            operationDescription = operationContext.getAxisOperation();
            operationSpecificPhases =
                operationDescription.getRemainingPhasesInFlow();
            resumeInvocationPhases(operationSpecificPhases, msgContext);
        } else {
            invokePhases(preCalculatedPhases, msgContext);
            verifyContextBuilt(msgContext);
            OperationContext operationContext =
                msgContext.getOperationContext();
            operationDescription = operationContext.getAxisOperation();
            operationSpecificPhases =
                operationDescription.getRemainingPhasesInFlow();
            invokePhases(operationSpecificPhases, msgContext);
        }

        if (msgContext.isServerSide() && !msgContext.isPaused()) {
            // invoke the Message Receivers
            MessageReceiver receiver =
                operationDescription.getMessageReceiver();
            receiver.receive(msgContext);
        }
    }

    /**
     * This Method Send the SOAP Fault to a Other SOAP Node
     *
     * @param msgContext
     * @throws AxisFault
     */
    public void sendFault(MessageContext msgContext) throws AxisFault {
        OperationContext opContext = msgContext.getOperationContext();
        //find and execute the Fault Out Flow Handlers
        if (opContext != null) {
            OperationDescription axisOperation = opContext.getAxisOperation();
            ArrayList phases = axisOperation.getPhasesOutFaultFlow();
            if (msgContext.isPaused()) {
                resumeInvocationPhases(phases, msgContext);
            } else {
                invokePhases(phases, msgContext);
            }
        }
        //it is possible that Operation Context is Null as the error occered before the 
        //Dispatcher. We do not run Handlers in that case 

        if (!msgContext.isPaused()) {
            //Actually send the SOAP Fault
            TransportSender sender = msgContext.getTransportOut().getSender();
            sender.invoke(msgContext);
        }
    }

    /**
     * This is invoked when a SOAP Fault is received from a Other SOAP Node
     *
     * @param msgContext
     * @throws AxisFault
     */
    public void receiveFault(MessageContext msgContext) throws AxisFault {

        OperationContext opContext = msgContext.getOperationContext();
        if (opContext == null) {
            //If we do not have a OperationContext that means this may be a incoming 
            //Dual Channel response. So try to dispatch the Service 
            ConfigurationContext sysCtx = msgContext.getSystemContext();
            ArrayList phases =
                sysCtx
                    .getAxisConfiguration()
                    .getInPhasesUptoAndIncludingPostDispatch();

            if (msgContext.isPaused()) {
                resumeInvocationPhases(phases, msgContext);
            } else {
                invokePhases(phases, msgContext);
            }
            verifyContextBuilt(msgContext);
        }
        opContext = msgContext.getOperationContext();
        //find and execute the Fault In Flow Handlers
        if (opContext != null) {
            OperationDescription axisOperation = opContext.getAxisOperation();
            ArrayList phases = axisOperation.getPhasesInFaultFlow();
            if (msgContext.isPaused()) {
                resumeInvocationPhases(phases, msgContext);
            } else {
                invokePhases(phases, msgContext);
            }
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
    public MessageContext createFaultMessageContext(
        MessageContext processingContext,
        Throwable e)
        throws AxisFault {
        if (processingContext.isProcessingFault()) {
            //We get the error file processing the fault. nothing we can do
            throw new AxisFault(
                Messages.getMessage("errorwhileProcessingFault"));
        }

        MessageContext faultContext =
            new MessageContext(
                engineContext,
                processingContext.getSessionContext(),
                processingContext.getTransportIn(),
                processingContext.getTransportOut());

        faultContext.setProcessingFault(true);
        if (processingContext.getFaultTo() != null) {
            faultContext.setFaultTo(processingContext.getFaultTo());
        } else {
            Object writer =
                processingContext.getProperty(MessageContext.TRANSPORT_OUT);
            if (writer != null) {
                faultContext.setProperty(MessageContext.TRANSPORT_OUT, writer);
            } else {
                throw new AxisFault(Messages.getMessage("nowhereToSendError"));
            }
        }

        faultContext.setOperationContext(processingContext.getOperationContext());
        faultContext.setProcessingFault(true);
        faultContext.setServerSide(true);
        SOAPEnvelope envelope = null;

         faultContext.setProperty(HTTPConstants.HTTPOutTransportInfo,
                processingContext.getProperty(HTTPConstants.HTTPOutTransportInfo));

        if (processingContext.isSOAP11()) {
            envelope =
                OMAbstractFactory.getSOAP11Factory().getDefaultFaultEnvelope();
        } else {
            // Following will make SOAP 1.2 as the default, too.
            envelope =
                OMAbstractFactory.getSOAP12Factory().getDefaultFaultEnvelope();
        }

        // TODO do we need to set old Headers back?

        //            body.addFault(new AxisFault(e.getMessage(), e));
        //        body.getFault().setException(new AxisFault(e));
        extractFaultInformationFromMessageContext(
            processingContext,
            envelope.getBody().getFault(),
            e);

        faultContext.setEnvelope(envelope);
        faultContext.setProperty(HTTPConstants.HTTPOutTransportInfo, processingContext.getProperty(HTTPConstants.HTTPOutTransportInfo));
        return faultContext;
    }

    /**
     * Information to create the SOAPFault can be extracted from different places.
     * 1. Those information may have been put in to the message context by some handler. When someone
     * is putting like that, he must make sure the SOAPElements he is putting must be from the
     * correct SOAP Version.
     * 2. SOAPProcessingException is flexible enough to carry information about the fault. For example
     * it has an attribute to store the fault code. The fault reason can be extracted from the
     * message of the exception. I opted to put the stacktrace under the detail element.
     * eg : <Detail>
     * <Exception> stack trace goes here </Exception>
     * <Detail>
     * <p/>
     * If those information can not be extracted from any of the above places, I default the soap
     * fault values to following.
     * <Fault>
     * <Code>
     * <Value>env:Receiver</Value>
     * </Code>
     * <Reason>
     * <Text>unknown</Text>
     * </Reason>
     * <Role/>
     * <Node/>
     * <Detail/>
     * </Fault>
     * <p/>
     * -- EC
     *
     * @param context
     * @param fault
     * @param e
     */
    private void extractFaultInformationFromMessageContext(
        MessageContext context,
        SOAPFault fault,
        Throwable e) {
        SOAPFaultException soapException = null;
        String soapNamespaceURI = "";

        // get the current SOAP version
        if (!context.isSOAP11()) {
            // defaulting to SOAP 1.2
            soapNamespaceURI = SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI;
        } else {
            soapNamespaceURI = SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI;
        }

        if (e instanceof SOAPProcessingException) {
            soapException = (SOAPProcessingException) e;
        } else if (e instanceof AxisFault) {
            if (e.getCause() instanceof SOAPProcessingException) {
                soapException = (SOAPProcessingException) e.getCause();
            } else  {
                soapException = (SOAPFaultException) e;
            }
        } else {
            // we have recd an instance of just the Exception class
        }

        Object faultCode =
            context.getProperty(SOAP12Constants.SOAP_FAULT_CODE_LOCAL_NAME);
        String soapFaultCode = "";
        if (faultCode != null) {
            fault.setCode((SOAPFaultCode) faultCode);
        } else if (soapException != null) {
            soapFaultCode = soapException.getFaultCode();

            // defaulting to fault code Sender, if no message is available
            soapFaultCode =
                ("".equals(soapFaultCode) || soapFaultCode == null)
                    ? getSenderFaultCode(soapNamespaceURI)
                    : soapFaultCode;
            fault.getCode().getValue().setText(soapFaultCode);
        }

        Object faultReason =
            context.getProperty(SOAP12Constants.SOAP_FAULT_REASON_LOCAL_NAME);
        String message = "";
        if (faultReason != null) {
            fault.setReason((SOAPFaultReason) faultReason);
        } else if (soapException != null) {
            message = soapException.getMessage();

            // defaulting to reason, unknown, if no reason is available
            message =
                ("".equals(message) || message == null) ? "unknown" : message;
            fault.getReason().getSOAPText().setText(message);
        }

        Object faultRole =
            context.getProperty(SOAP12Constants.SOAP_FAULT_ROLE_LOCAL_NAME);
        if (faultRole != null) {
            fault.getRole().setText((String) faultRole);
        } else {
            // TODO : get the role of this server and assign it here
            fault.getRole().setText("http://myAxisServer/role/default");
        }

        Object faultNode =
            context.getProperty(SOAP12Constants.SOAP_FAULT_NODE_LOCAL_NAME);
        if (faultNode != null) {
            fault.getNode().setText((String) faultNode);
        } else {
            // TODO : get the node of this server and assign it here
            fault.getNode().setText("http://myAxisServer/role/default");

        }

        Object faultDetail =
            context.getProperty(SOAP12Constants.SOAP_FAULT_DETAIL_LOCAL_NAME);
        if (faultDetail != null) {
            fault.setDetail((SOAPFaultDetail) faultDetail);
        } else if (fault.getException() == null) {
            if(e instanceof Exception){
                fault.setException((Exception)e);
            }else{
                fault.setException(new Exception(e));
            }

        }
    }

    private void verifyContextBuilt(MessageContext msgctx) throws AxisFault {
        if (msgctx.getSystemContext() == null) {
            throw new AxisFault(
                Messages.getMessage("cannotBeNullConfigurationContext"));
        }
        if (msgctx.getOperationContext() == null) {
            throw new AxisFault(
                Messages.getMessage("cannotBeNullOperationContext"));
        }
        if (msgctx.getServiceContext() == null) {
            throw new AxisFault(
                Messages.getMessage("cannotBeNullServiceContext"));
        }
    }

    private void invokePhases(ArrayList phases, MessageContext msgctx)
        throws AxisFault {
        int count = phases.size();
        for (int i = 0;(i < count && !msgctx.isPaused()); i++) {
            Phase phase = (Phase) phases.get(i);
            phase.invoke(msgctx);
        }
    }

    public void resumeInvocationPhases(ArrayList phases, MessageContext msgctx)
        throws AxisFault {
        msgctx.setPausedFalse();
        int count = phases.size();
        boolean foundMatch = false;

        for (int i = 0; i < count && !msgctx.isPaused(); i++) {
            Phase phase = (Phase) phases.get(i);
            if (phase.getPhaseName().equals(msgctx.getPausedPhaseName())) {
                foundMatch = true;
                phase.invokeStartFromHandler(
                    msgctx.getPausedHandlerName(),
                    msgctx);
            } else {
                if (foundMatch) {
                    phase.invoke(msgctx);
                }

            }
        }
    }

    /* --------------------------------------------------------------------------------------------*/
    /* -----------------   Methods related to storage ----------------------------------------------*/
    /**
     * Stores an object in the underlying storage
     *
     * @param context The relevant engine context
     * @param obj     the object to be stored
     * @return the storage key
     */
    public Object store(ConfigurationContext context, Object obj) {
        return context.getStorage().put(obj);
    }

    /**
     * retrieves an object from the underlying storage
     *
     * @param context
     * @param key
     * @return
     */
    public Object retrieve(ConfigurationContext context, Object key) {
        return context.getStorage().get(key);
    }

    /**
     * removes an object from the underlying storage
     *
     * @param context
     * @param key
     * @return the object removed
     */
    public Object remove(ConfigurationContext context, Object key) {
        return context.getStorage().remove(key);
    }

    /**
     * Clears the underlying storage
     *
     * @param context
     * @return
     */
    public boolean clearStorage(ConfigurationContext context) {
        return context.getStorage().clean();
    }



    private String getSenderFaultCode(String soapNamespace) {
        return SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(
            soapNamespace)
            ? SOAP12Constants.FAULT_CODE_SENDER
            : SOAP11Constants.FAULT_CODE_SENDER;
    }

    private String getReceiverFaultCode(String soapNamespace) {
        return SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(
            soapNamespace)
            ? SOAP12Constants.FAULT_CODE_RECEIVER
            : SOAP11Constants.FAULT_CODE_RECEIVER;
    }

    /**
     * To serilze the entier context heirarachy to a given location from top to bottom
     * @throws AxisFault
     */
    public synchronized void   serialize() {
        try {
             String serailzeLocaion =".";
            //output location
            Parameter parameter = engineContext.getAxisConfiguration().getParameter("seralizeLocation");
            if (parameter !=null) {
                serailzeLocaion = ((String)parameter.getValue()).trim();
            }
            FileOutputStream fileOut = new FileOutputStream(new File(serailzeLocaion,"Axis2.obj"));
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(engineContext);
        } catch (FileNotFoundException e) {
            //todo has to be improved
        } catch (IOException e) {
             //todo has to be improved
        }

    }

}
