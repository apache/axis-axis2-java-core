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
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.transport.TransportSender;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.commons.om.OMAbstractFactory;
import org.apache.ws.commons.om.OMElement;
import org.apache.ws.commons.om.OMNamespace;
import org.apache.ws.commons.soap.SOAP11Constants;
import org.apache.ws.commons.soap.SOAP12Constants;
import org.apache.ws.commons.soap.SOAPConstants;
import org.apache.ws.commons.soap.SOAPEnvelope;
import org.apache.ws.commons.soap.SOAPFault;
import org.apache.ws.commons.soap.SOAPFaultCode;
import org.apache.ws.commons.soap.SOAPFaultDetail;
import org.apache.ws.commons.soap.SOAPFaultReason;
import org.apache.ws.commons.soap.SOAPHeaderBlock;
import org.apache.ws.commons.soap.SOAPProcessingException;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;

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
        log.debug("Axis Engine Started");
        this.engineContext = engineContext;
    }

    private void checkMustUnderstand(MessageContext msgContext) throws AxisFault {

        // todo : need to move this to pre-condition of the MessageReceiver Phase
        SOAPEnvelope se = msgContext.getEnvelope();

        if (se.getHeader() == null) {
            return;
        }

        Iterator hbs = se.getHeader().examineAllHeaderBlocks();

        while (hbs.hasNext()) {
            SOAPHeaderBlock hb = (SOAPHeaderBlock) hbs.next();

            // if this header block has been processed or mustUnderstand isn't
            // turned on then its cool
            if (hb.isProcessed() || !hb.getMustUnderstand()) {
                continue;
            }

            // if this header block is not targetted to me then its not my
            // problem. Currently this code only supports the "next" role; we
            // need to fix this to allow the engine/service to be in one or more
            // additional roles and then to check that any headers targetted for
            // that role too have been dealt with.

            String role = hb.getRole();

            String prefix = se.getNamespace().getPrefix();

            if (!msgContext.isSOAP11()) {

                // if must understand and soap 1.2 the Role should be NEXT , if it is null we considerr
                // it to be NEXT
                if (prefix == null || "".equals(prefix)) {
                    prefix = SOAPConstants.SOAPFAULT_NAMESPACE_PREFIX;
                }
                if (role != null) {
                    if (!SOAP12Constants.SOAP_ROLE_NEXT.equals(role)) {
                        throw new AxisFault("Must Understand check failed",
                                prefix + ":" + SOAP12Constants.FAULT_CODE_MUST_UNDERSTAND);
                    }
                } else {
                    // This is the ultimate receiver, throw an error.
                    throw new AxisFault("Must Understand check failed",
                            prefix + ":" + SOAP12Constants.FAULT_CODE_MUST_UNDERSTAND);
                }
            } else {

                // if must understand and soap 1.1 the actor should be NEXT , if it is null we considerr
                // it to be NEXT
                if ((role != null) && !SOAP11Constants.SOAP_ACTOR_NEXT.equals(role)) {
                    throw new AxisFault("Must Understand check failed",
                            prefix + ":" + SOAP11Constants.FAULT_CODE_MUST_UNDERSTAND);
                }
            }
        }
    }

    /**
     * This method is called to handle any error that occurs at inflow or outflow. But if the
     * method is called twice, it implies that sending the error handling has failed, in which case
     * the method logs the error and exists.
     *
     * @param processingContext
     * @param e
     * @throws AxisFault
     */
    public MessageContext createFaultMessageContext(MessageContext processingContext, Throwable e)
            throws AxisFault {
        if (processingContext.isProcessingFault()) {

            // We get the error file processing the fault. nothing we can do
            throw new AxisFault(Messages.getMessage("errorwhileProcessingFault"));
        }

        MessageContext faultContext = new MessageContext();
        faultContext.setConfigurationContext(engineContext);
        faultContext.setSessionContext(processingContext.getSessionContext());
        faultContext.setTransportIn(processingContext.getTransportIn());
        faultContext.setTransportOut(processingContext.getTransportOut());

        // register the fault message context
        if (processingContext.getAxisOperation() != null && processingContext.getOperationContext() != null)
        {
            processingContext.getAxisOperation().addFaultMessageContext(faultContext, processingContext.getOperationContext());
        }

        faultContext.setProcessingFault(true);

        // there are some information  that the fault thrower wants to pass to the fault path.
        Object faultInfoForHeaders = processingContext.getProperty(Constants.FAULT_INFORMATION_FOR_HEADERS);
        if (faultInfoForHeaders != null) {
            faultContext.setProperty(Constants.FAULT_INFORMATION_FOR_HEADERS, faultInfoForHeaders);
        }

        EndpointReference faultTo = processingContext.getFaultTo();
        if (faultTo != null) {
            faultContext.setTo(processingContext.getFaultTo());
        } else
        if (processingContext.getEnvelope().getHeader() != null && processingContext.getEnvelope().getHeader().getFirstChildWithName(new QName("FaultTo")) != null)
        {
            OMElement faultToElement = processingContext.getEnvelope().getHeader().getFirstChildWithName(new QName("FaultTo"));
            faultTo = new EndpointReference("");
            faultTo.fromOM(faultToElement);
            faultContext.setTo(faultTo);
        }

        if (faultTo == null || AddressingConstants.Final.WSA_ANONYMOUS_URL.equals(faultTo.getAddress())
                || AddressingConstants.Submission.WSA_ANONYMOUS_URL.equals(faultTo.getAddress())
                || AddressingConstants.Final.WSA_NONE_URI.equals(faultTo.getAddress())) {
            Object writer = processingContext.getProperty(MessageContext.TRANSPORT_OUT);
            if (writer != null) {
                faultContext.setProperty(MessageContext.TRANSPORT_OUT, writer);
            } else {
                throw new AxisFault(Messages.getMessage("nowhereToSendError"));
            }
        }

        faultContext.setOperationContext(processingContext.getOperationContext());
        faultContext.setProcessingFault(true);
        faultContext.setServerSide(true);

        SOAPEnvelope envelope;

        faultContext.setProperty(Constants.OUT_TRANSPORT_INFO,
                processingContext.getProperty(Constants.OUT_TRANSPORT_INFO));

        if (processingContext.isSOAP11()) {
            envelope = OMAbstractFactory.getSOAP11Factory().getDefaultFaultEnvelope();
        } else {

            // Following will make SOAP 1.2 as the default, too.
            envelope = OMAbstractFactory.getSOAP12Factory().getDefaultFaultEnvelope();
        }

        extractFaultInformationFromMessageContext(processingContext, envelope.getBody().getFault(),
                e);
        faultContext.setEnvelope(envelope);
        faultContext.setProperty(Constants.OUT_TRANSPORT_INFO,
                processingContext.getProperty(Constants.OUT_TRANSPORT_INFO));

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
    private void extractFaultInformationFromMessageContext(MessageContext context, SOAPFault fault,
                                                           Throwable e) {
        SOAPProcessingException soapException = null;
        String soapNamespaceURI;

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
            }
        } else {
            // we have recd an instance of just the Exception class
        }

        Object faultCode = context.getProperty(SOAP12Constants.SOAP_FAULT_CODE_LOCAL_NAME);
        String soapFaultCode = "";

        Throwable exception = null;
        if (faultCode != null) {
            fault.setCode((SOAPFaultCode) faultCode);
        } else if (soapException != null) {
            soapFaultCode = soapException.getFaultCode();
        } else
        if (((exception = e) instanceof AxisFault || (exception = e.getCause()) instanceof AxisFault))
        {
            QName faultCodeQName = ((AxisFault) exception).getFaultCode();
            if (faultCodeQName != null && faultCodeQName.getLocalPart().indexOf(":") == -1) {
                String prefix = faultCodeQName.getPrefix();
                String uri = faultCodeQName.getNamespaceURI();
                prefix = prefix == null || "".equals(prefix) ? Constants.AXIS2_NAMESPACE_PREFIX : prefix;
                uri = uri == null || "".equals(uri) ? Constants.AXIS2_NAMESPACE_URI : uri;
                soapFaultCode = prefix + ":" + faultCodeQName.getLocalPart();
                fault.declareNamespace(uri, prefix);
            }
        }

        // defaulting to fault code Sender, if no message is available
        soapFaultCode = ("".equals(soapFaultCode) || (soapFaultCode == null))
                ? getSenderFaultCode(context.getEnvelope().getNamespace())
                : soapFaultCode;
        fault.getCode().getValue().setText(soapFaultCode);

        Object faultReason = context.getProperty(SOAP12Constants.SOAP_FAULT_REASON_LOCAL_NAME);
        String message = "";

        if (faultReason != null) {
            fault.setReason((SOAPFaultReason) faultReason);
            message = fault.getReason().getSOAPText().getText();
        } else if (soapException != null) {
            message = soapException.getMessage();
        } else if (e instanceof AxisFault) {
            message = ((AxisFault) e).getReason();
            message = message != null && "".equals(message) ? message : e.getMessage();

        }

        // defaulting to reason, unknown, if no reason is available
        message = ("".equals(message) || (message == null))
                ? "unknown"
                : message;
        fault.getReason().getSOAPText().setText(message);

        Object faultRole = context.getProperty(SOAP12Constants.SOAP_FAULT_ROLE_LOCAL_NAME);

        if (faultRole != null) {
            fault.getRole().setText((String) faultRole);
        }

        Object faultNode = context.getProperty(SOAP12Constants.SOAP_FAULT_NODE_LOCAL_NAME);

        if (faultNode != null) {
            fault.getNode().setText((String) faultNode);
        }

        Object faultDetail = context.getProperty(SOAP12Constants.SOAP_FAULT_DETAIL_LOCAL_NAME);

        if (faultDetail != null) {
            fault.setDetail((SOAPFaultDetail) faultDetail);
        }

        if (fault.getException() == null) {
            if (e instanceof Exception) {
                fault.setException((Exception) e);
            } else {
                fault.setException(new Exception(e));
            }
        }


    }

    /**
     * This methods represents the inflow of the Axis, this could be either at the server side or the client side.
     * Here the <code>ExecutionChain</code> is created using the Phases. The Handlers at the each Phases is ordered in
     * deployment time by the deployment module
     *
     * @throws AxisFault
     * @see MessageContext
     * @see Phase
     * @see Handler
     */
    public void receive(MessageContext msgContext) throws AxisFault {
        ConfigurationContext confContext = msgContext.getConfigurationContext();
        ArrayList preCalculatedPhases =
                confContext.getAxisConfiguration().getInPhasesUptoAndIncludingPostDispatch();

        // Set the initial execution chain in the MessageContext to a *copy* of what
        // we got above.  This allows individual message processing to change the chain without
        // affecting later messages.
        msgContext.setExecutionChain((ArrayList) preCalculatedPhases.clone());
        msgContext.setFLOW(MessageContext.IN_FLOW);
        invoke(msgContext);

        if (msgContext.isServerSide() && !msgContext.isPaused()) {

            // invoke the Message Receivers
            checkMustUnderstand(msgContext);

            MessageReceiver receiver = msgContext.getAxisOperation().getMessageReceiver();

            receiver.receive(msgContext);
        }
    }

    /**
     * Take the execution chain from the msgContext , and then take the current Index
     * and invoke all the phases in the arraylist
     * if the msgContext is pauesd then the execution will be breaked
     *
     * @param msgContext
     * @throws AxisFault
     */
    public void invoke(MessageContext msgContext) throws AxisFault {
        if (msgContext.getCurrentHandlerIndex() == -1) {
            msgContext.setCurrentHandlerIndex(0);
        }
        while (msgContext.getCurrentHandlerIndex() < msgContext.getExecutionChain().size()) {
            //todo : This might cause for performance drawback
            Handler currentHandler = (Handler) msgContext.getExecutionChain().
                    get(msgContext.getCurrentHandlerIndex());
            currentHandler.invoke(msgContext);

            if (msgContext.isPaused()) {
                break;
            }
            msgContext.setCurrentHandlerIndex(msgContext.getCurrentHandlerIndex() + 1);
        }
    }

    /**
     * If the msgConetext is puased and try to invoke then
     * first invoke the phase list and after the message receiver
     *
     * @param msgContext
     * @throws AxisFault
     */
    public void resumeReceive(MessageContext msgContext) throws AxisFault {
        //invoke the phases
        invoke(msgContext);
        //invoking the MR
        if (msgContext.isServerSide() && !msgContext.isPaused()) {
            // invoke the Message Receivers
            checkMustUnderstand(msgContext);
            MessageReceiver receiver = msgContext.getAxisOperation().getMessageReceiver();
            receiver.receive(msgContext);
        }
    }

    /**
     * To resume the invocation at the send path , this is neened since it is require to call
     * TranportSender at the end
     *
     * @param msgContext
     */
    public void resumeSend(MessageContext msgContext) throws AxisFault {
        //invoke the phases
        invoke(msgContext);
        //Invoking Tarnsport Sender
        if (!msgContext.isPaused()) {
            // write the Message to the Wire
            TransportOutDescription transportOut = msgContext.getTransportOut();
            TransportSender sender = transportOut.getSender();
            sender.invoke(msgContext);
        }
    }


    /**
     * This is invoked when a SOAP Fault is received from a Other SOAP Node
     * Receives a SOAP fault from another SOAP node.
     *
     * @param msgContext
     * @throws AxisFault
     */
    public void receiveFault(MessageContext msgContext) throws AxisFault {
        // TODO : rationalize fault handling!
    }

    public void resume(MessageContext msgctx) throws AxisFault {
        msgctx.setPaused(false);
        if (msgctx.getFLOW() == MessageContext.IN_FLOW) {
            resumeReceive(msgctx);
        } else {
            resumeSend(msgctx);
        }
    }

    /**
     * This methods represents the outflow of the Axis, this could be either at the server side or the client side.
     * Here the <code>ExecutionChain</code> is created using the Phases. The Handlers at the each Phases is ordered in
     * deployment time by the deployment module
     *
     * @param msgContext
     * @throws AxisFault
     * @see MessageContext
     * @see Phase
     * @see Handler
     */
    public void send(MessageContext msgContext) throws AxisFault {

        // find and invoke the Phases
        OperationContext operationContext = msgContext.getOperationContext();
        ArrayList executionChain = operationContext.getAxisOperation().getPhasesOutFlow();
        //rather than having two steps added both oparation and global chain together
        ArrayList outPhases = new ArrayList();
        outPhases.addAll((ArrayList) executionChain.clone());
        outPhases.addAll((ArrayList) msgContext.getConfigurationContext()
                .getAxisConfiguration().getGlobalOutPhases().clone());
        msgContext.setExecutionChain(outPhases);
        msgContext.setFLOW(MessageContext.OUT_FLOW);
        invoke(msgContext);

        if (!msgContext.isPaused()) {

            // write the Message to the Wire
            TransportOutDescription transportOut = msgContext.getTransportOut();
            TransportSender sender = transportOut.getSender();

            sender.invoke(msgContext);
        }
    }

    /**
     * Sends the SOAP Fault to another SOAP node.
     *
     * @param msgContext
     * @throws AxisFault
     */
    public void sendFault(MessageContext msgContext) throws AxisFault {
        OperationContext opContext = msgContext.getOperationContext();

        // find and execute the Fault Out Flow Handlers
        if (opContext != null) {
            AxisOperation axisOperation = opContext.getAxisOperation();
            ArrayList faultExecutionChain = axisOperation.getPhasesOutFaultFlow();

            msgContext.setExecutionChain((ArrayList) faultExecutionChain.clone());
            msgContext.setFLOW(MessageContext.OUT_FLOW);
            invoke(msgContext);
        }

        // TODO: Make this clearer - should we have transport senders and messagereceivers as Handlers?
        if (!msgContext.isPaused()) {

            msgContext.setExecutionChain((ArrayList) msgContext.getConfigurationContext().getAxisConfiguration().getOutFaultFlow().clone());
            msgContext.setFLOW(MessageContext.OUT_FLOW);
            invoke(msgContext);

            // Actually send the SOAP Fault
            TransportSender sender = msgContext.getTransportOut().getSender();

            sender.invoke(msgContext);
        }
    }

    private String getSenderFaultCode(OMNamespace soapNamespace) {
        return SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(soapNamespace.getName())
                ? soapNamespace.getPrefix() + ":" + SOAP12Constants.FAULT_CODE_SENDER
                : soapNamespace.getPrefix() + ":" + SOAP11Constants.FAULT_CODE_SENDER;
    }
}
