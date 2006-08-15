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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPConstants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFault;
import org.apache.axiom.soap.SOAPFaultCode;
import org.apache.axiom.soap.SOAPFaultDetail;
import org.apache.axiom.soap.SOAPFaultNode;
import org.apache.axiom.soap.SOAPFaultReason;
import org.apache.axiom.soap.SOAPFaultRole;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axiom.soap.SOAPProcessingException;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.RelatesTo;
import org.apache.axis2.addressing.AddressingConstants.Final;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.transport.TransportSender;
import org.apache.axis2.util.JavaUtils;
import org.apache.axis2.util.UUIDGenerator;
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
    private static final Log log = LogFactory.getLog(AxisEngine.class);
    private ConfigurationContext engineContext;

    /**
     * Constructor AxisEngine
     */
    public AxisEngine(ConfigurationContext engineContext) {
        this.engineContext = engineContext;
    }

    private void checkMustUnderstand(MessageContext msgContext) throws AxisFault {
        if (!msgContext.isHeaderPresent()) {
            return;
        }
        SOAPEnvelope envelope = msgContext.getEnvelope();
        if (envelope.getHeader() == null) {
            return;
        }
        Iterator headerBlocks = envelope.getHeader().examineAllHeaderBlocks();
        while (headerBlocks.hasNext()) {
            SOAPHeaderBlock headerBlock = (SOAPHeaderBlock) headerBlocks.next();
            // if this header block has been processed or mustUnderstand isn't
            // turned on then its cool
            if (headerBlock.isProcessed() || !headerBlock.getMustUnderstand()) {
                continue;
            }
            // if this header block is not targetted to me then its not my
            // problem. Currently this code only supports the "next" role; we
            // need to fix this to allow the engine/service to be in one or more
            // additional roles and then to check that any headers targetted for
            // that role too have been dealt with.

            String role = headerBlock.getRole();

            String prefix = envelope.getNamespace().getPrefix();

            if (!msgContext.isSOAP11()) {

                // if must understand and soap 1.2 the Role should be NEXT , if it is null we consider
                // it to be NEXT
                if (prefix == null || "".equals(prefix)) {
                    prefix = SOAPConstants.SOAP_DEFAULT_NAMESPACE_PREFIX;
                }
                if (role != null) {
                    if (!SOAP12Constants.SOAP_ROLE_NEXT.equals(role)) {
                        throw new AxisFault(Messages.getMessage(
                                "mustunderstandfailed",
                                prefix, SOAP12Constants.FAULT_CODE_MUST_UNDERSTAND));
                    }
                } else {
                    throw new AxisFault(Messages.getMessage(
                            "mustunderstandfailed",
                            prefix, SOAP12Constants.FAULT_CODE_MUST_UNDERSTAND));
                }
            } else {

                // if must understand and soap 1.1 the actor should be NEXT , if it is null we considerr
                // it to be NEXT
                if ((role != null) && !SOAP11Constants.SOAP_ACTOR_NEXT.equals(role)) {
                    throw new AxisFault(Messages.getMessage(
                            "mustunderstandfailed",
                            prefix, SOAP12Constants.FAULT_CODE_MUST_UNDERSTAND));
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

        faultContext.setMessageID(UUIDGenerator.getUUID());
        faultContext.addRelatesTo(
                new RelatesTo(
                        processingContext.getOptions().getMessageId(),
                        AddressingConstants.Final.WSA_RELATES_TO_RELATIONSHIP_TYPE_DEFAULT_VALUE));
        faultContext.setProperty(Constants.Configuration.CHARACTER_SET_ENCODING,
                processingContext.getProperty(Constants.Configuration.CHARACTER_SET_ENCODING));

        // register the fault message context
        if (processingContext.getAxisOperation() != null && processingContext.getOperationContext() != null) {
            processingContext.getAxisOperation().addFaultMessageContext(faultContext, processingContext.getOperationContext());
        }

        ServiceContext serviceContext = processingContext.getServiceContext();
        if (serviceContext != null) {
            faultContext.setServiceContext(serviceContext);
        }

        faultContext.setProcessingFault(true);
        // Add correct Action
        AxisOperation op = processingContext.getAxisOperation();
        if(op != null && op.getFaultAction()!=null){
            faultContext.setWSAAction(processingContext.getAxisOperation().getFaultAction());
        }else{ //If, for some reason there is no value set, should use a sensible action.
            faultContext.setWSAAction(Final.WSA_FAULT_ACTION);
        }
                
        // there are some information  that the fault thrower wants to pass to the fault path.
        // Means that the fault is a ws-addressing one hence use the ws-addressing fault action.
        Object faultInfoForHeaders = processingContext.getProperty(Constants.FAULT_INFORMATION_FOR_HEADERS);
        if (faultInfoForHeaders != null) {
            faultContext.setProperty(Constants.FAULT_INFORMATION_FOR_HEADERS, faultInfoForHeaders);
            faultContext.setWSAAction(Final.WSA_FAULT_ACTION);
        }

        // if the exception is due to a problem in the faultTo header itself, we can not use those
        // fault informatio to send the error. Try to send using replyTo, leave it to transport
        boolean doNotSendFaultUsingFaultTo = false;
        if (faultInfoForHeaders != null) {
            String problemHeaderName = (String) ((Map) faultInfoForHeaders).get(AddressingConstants.Final.FAULT_HEADER_PROB_HEADER_QNAME);
            doNotSendFaultUsingFaultTo = (problemHeaderName != null && (AddressingConstants.WSA_DEFAULT_PREFIX + ":" + AddressingConstants.WSA_FAULT_TO).equals(problemHeaderName));
        }

        EndpointReference faultTo = processingContext.getFaultTo();
        SOAPEnvelope env = processingContext.getEnvelope();
        if (faultTo != null && !doNotSendFaultUsingFaultTo) {
            faultContext.setTo(processingContext.getFaultTo());
        } else if (env != null && !doNotSendFaultUsingFaultTo && processingContext.isHeaderPresent() && env.getHeader().getFirstChildWithName(new QName("FaultTo")) != null) {
            OMElement faultToElement = env.getHeader().getFirstChildWithName(new QName("FaultTo"));
            faultTo = new EndpointReference("");
            faultTo.fromOM(faultToElement);
            faultContext.setTo(faultTo);
        } else if (processingContext.getReplyTo() != null) {
            faultContext.setTo(processingContext.getReplyTo());
        }
        
        // do Target Resolution
        faultContext.getConfigurationContext().getAxisConfiguration().getTargetResolverChain().resolveTarget(faultContext);

        //Determine that we have the correct transport available.
        TransportOutDescription transportOut = faultContext.getTransportOut();

        try {
            if (faultContext.isServerSide() && faultContext.getTo() != null) {
                String replyToAddress = faultContext.getTo().getAddress();
                if (!(AddressingConstants.Final.WSA_ANONYMOUS_URL.equals(replyToAddress)
                        || AddressingConstants.Submission.WSA_ANONYMOUS_URL.equals(replyToAddress)
                        || AddressingConstants.Final.WSA_NONE_URI.equals(replyToAddress))) {
                    URI uri = new URI(replyToAddress);
                    String scheme = uri.getScheme();
                    if (!transportOut.getName().getLocalPart().equals(scheme)) {
                        ConfigurationContext configurationContext = faultContext.getConfigurationContext();
                        transportOut = configurationContext.getAxisConfiguration()
                                .getTransportOut(new QName(scheme));
                        if (transportOut == null) {
                            throw new AxisFault("Can not find the transport sender : " + scheme);
                        }
                        faultContext.setTransportOut(transportOut);
                    }
                }
            }
        } catch (URISyntaxException urise) {
            throw new AxisFault(urise);
        }

        faultContext.setOperationContext(processingContext.getOperationContext());
        faultContext.setProcessingFault(true);
        faultContext.setServerSide(true);

        SOAPEnvelope envelope;

        if (processingContext.isSOAP11()) {
            envelope = OMAbstractFactory.getSOAP11Factory().getDefaultFaultEnvelope();
        } else {

            // Following will make SOAP 1.2 as the default, too.
            envelope = OMAbstractFactory.getSOAP12Factory().getDefaultFaultEnvelope();
        }

        extractFaultInformationFromMessageContext(processingContext, envelope.getBody().getFault(),
                e);
        faultContext.setEnvelope(envelope);
        faultContext.setProperty(MessageContext.TRANSPORT_OUT,
                processingContext.getProperty(MessageContext.TRANSPORT_OUT));
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
        AxisFault axisFault = null;

        if (e != null) {
            if (e instanceof AxisFault) {
                axisFault = (AxisFault) e;
            } else if (e.getCause() instanceof AxisFault) {
                axisFault = (AxisFault) e.getCause();
            }
        }

        if (e instanceof SOAPProcessingException) {
            soapException = (SOAPProcessingException) e;
        } else if (axisFault != null) {
            if (axisFault.getCause() instanceof SOAPProcessingException) {
                soapException = (SOAPProcessingException) axisFault.getCause();
            }
        } else {
            // we have recd an instance of just the Exception class
        }

        // user can set the fault information to the message context or to the AxisFault itself.
        // whatever user sets to the message context, supercedes eerything.

        Object faultCode = context.getProperty(SOAP12Constants.SOAP_FAULT_CODE_LOCAL_NAME);
        String soapFaultCode = "";


        if (faultCode != null) {
            fault.setCode((SOAPFaultCode) faultCode);
        } else if (soapException != null) {
            soapFaultCode = soapException.getFaultCode();
        } else if (axisFault != null) {

            Map faultElementsMap = axisFault.getFaultElements();
            if (faultElementsMap != null && faultElementsMap.get(SOAP12Constants.SOAP_FAULT_CODE_LOCAL_NAME) != null) {
                fault.setCode((SOAPFaultCode) faultElementsMap.get(SOAP12Constants.SOAP_FAULT_CODE_LOCAL_NAME));
            } else {
                QName faultCodeQName = axisFault.getFaultCode();
                if (faultCodeQName != null) {
                    if (faultCodeQName.getLocalPart().indexOf(":") == -1) {
                        String prefix = faultCodeQName.getPrefix();
                        String uri = faultCodeQName.getNamespaceURI();
                        prefix = prefix == null || "".equals(prefix) ? Constants.AXIS2_NAMESPACE_PREFIX : prefix;
                        uri = uri == null || "".equals(uri) ? Constants.AXIS2_NAMESPACE_URI : uri;
                        soapFaultCode = prefix + ":" + faultCodeQName.getLocalPart();
                        fault.declareNamespace(uri, prefix);
                    } else {
                        soapFaultCode = faultCodeQName.getLocalPart();
                    }
                }
            }
        }

        // defaulting to fault code Sender, if no message is available
        if (faultCode == null && context.getEnvelope() != null) {
            soapFaultCode = ("".equals(soapFaultCode) || (soapFaultCode == null))
                    ? getSenderFaultCode(context.getEnvelope().getNamespace())
                    : soapFaultCode;
            fault.getCode().getValue().setText(soapFaultCode);
        }

        Object faultReason = context.getProperty(SOAP12Constants.SOAP_FAULT_REASON_LOCAL_NAME);
        String message = "";

        if (faultReason != null) {
            fault.setReason((SOAPFaultReason) faultReason);
            message = fault.getReason().getFirstSOAPText().getText();
        } else if (soapException != null) {
            message = soapException.getMessage();
        } else if (axisFault != null) {
            Map faultElementsMap = axisFault.getFaultElements();
            if (faultElementsMap != null && faultElementsMap.get(SOAP12Constants.SOAP_FAULT_REASON_LOCAL_NAME) != null) {
                fault.setReason((SOAPFaultReason) faultElementsMap.get(SOAP12Constants.SOAP_FAULT_REASON_LOCAL_NAME));
            } else {
                message = axisFault.getReason();
                if (message == null || "".equals(message)) {
                    message = getFaultReasonFromException(e, context);
                }
//                message = message != null && "".equals(message) ? message : e.getMessage();
            }


        }

        // defaulting to reason, unknown, if no reason is available
        if (faultReason == null) {
            message = ("".equals(message) || (message == null))
                    ? "unknown"
                    : message;
            fault.getReason().getFirstSOAPText().setLang("en-US");
            fault.getReason().getFirstSOAPText().setText(message);
        }


        Object faultRole = context.getProperty(SOAP12Constants.SOAP_FAULT_ROLE_LOCAL_NAME);
        if (faultRole != null) {
            fault.getRole().setText((String) faultRole);
        } else if (axisFault != null) {
            Map faultElementsMap = axisFault.getFaultElements();
            if (faultElementsMap != null && faultElementsMap.get(SOAP12Constants.SOAP_FAULT_ROLE_LOCAL_NAME) != null) {
                fault.setRole((SOAPFaultRole) faultElementsMap.get(SOAP12Constants.SOAP_FAULT_ROLE_LOCAL_NAME));
            }
        }

        Object faultNode = context.getProperty(SOAP12Constants.SOAP_FAULT_NODE_LOCAL_NAME);
        if (faultNode != null) {
            fault.getNode().setText((String) faultNode);
        } else if (axisFault != null) {
            Map faultElementsMap = axisFault.getFaultElements();
            if (faultElementsMap != null && faultElementsMap.get(SOAP12Constants.SOAP_FAULT_NODE_LOCAL_NAME) != null) {
                fault.setNode((SOAPFaultNode) faultElementsMap.get(SOAP12Constants.SOAP_FAULT_NODE_LOCAL_NAME));
            }
        }

        // Allow handlers to override the sendStacktraceDetailsWithFaults setting from the Configuration to allow
        // WS-* protocol faults to not include the exception.
        boolean sendStacktraceDetailsWithFaults = false;
        Object flagFromContext = context.getOperationContext().getProperty(Constants.Configuration.SEND_STACKTRACE_DETAILS_WITH_FAULTS);
        if(flagFromContext!=null){
            sendStacktraceDetailsWithFaults = JavaUtils.isTrue(flagFromContext);
        }else{
            Parameter param = context.getParameter(Constants.Configuration.SEND_STACKTRACE_DETAILS_WITH_FAULTS);
            sendStacktraceDetailsWithFaults = JavaUtils.isTrue(param);    
        }

        Object faultDetail = context.getProperty(SOAP12Constants.SOAP_FAULT_DETAIL_LOCAL_NAME);
        if (faultDetail != null) {
            fault.setDetail((SOAPFaultDetail) faultDetail);
        } else if (axisFault != null) {
            Map faultElementsMap = axisFault.getFaultElements();
            if (faultElementsMap != null && faultElementsMap.get(SOAP12Constants.SOAP_FAULT_DETAIL_LOCAL_NAME) != null) {
                fault.setDetail((SOAPFaultDetail) faultElementsMap.get(SOAP12Constants.SOAP_FAULT_DETAIL_LOCAL_NAME));
            } else {
                OMElement detail = axisFault.getDetail();
                if (detail != null) {
                    fault.getDetail().addDetailEntry(detail);
                } else if (sendStacktraceDetailsWithFaults) {
                    fault.setException(axisFault);
                }
            }
        } else if (fault.getException() == null && sendStacktraceDetailsWithFaults) {
            if (e instanceof Exception) {
                fault.setException((Exception) e);
            } else {
                fault.setException(new Exception(e));
            }
        }


    }

    /**
     * By the time the exception comes here it can be wrapped by so many levels. This will crip down
     * to the root cause and get the initial error depending on the property
     *
     * @param e
     */
    private String getFaultReasonFromException(Throwable e, MessageContext context) {
        Throwable throwable = e;
        Parameter param = context.getParameter(Constants.Configuration.DRILL_DOWN_TO_ROOT_CAUSE_FOR_FAULT_REASON);
        boolean drillDownToRootCauseForFaultReason = param != null && ((String) param.getValue()).equalsIgnoreCase("true");
        if (drillDownToRootCauseForFaultReason) {
            while (throwable.getCause() != null) {
                throwable = throwable.getCause();
            }
        }
        return throwable.getMessage();
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
                confContext.getAxisConfiguration().getGlobalInFlow();
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
     * TransportSender at the end
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

    	log.info(Messages.getMessage("receivederrormessage",
                msgContext.getMessageID()));
        ConfigurationContext confContext = msgContext.getConfigurationContext();
        ArrayList preCalculatedPhases =
                confContext.getAxisConfiguration().getInFaultFlow();
        // Set the initial execution chain in the MessageContext to a *copy* of what
        // we got above.  This allows individual message processing to change the chain without
        // affecting later messages.
        msgContext.setExecutionChain((ArrayList) preCalculatedPhases.clone());
        msgContext.setFLOW(MessageContext.IN_FAULT_FLOW);
        invoke(msgContext);
        
        if (msgContext.isServerSide() && !msgContext.isPaused()) {

            // invoke the Message Receivers
            checkMustUnderstand(msgContext);

            MessageReceiver receiver = msgContext.getAxisOperation().getMessageReceiver();

            receiver.receive(msgContext);
        }
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
            // This boolean property only used in client side fireAndForget invocation
            //It will set a property into message context and if some one has set the
            //property then transport sender will invoke in a diffrent thread
            Object isTransportNonBlocking = msgContext.getProperty(
                    MessageContext.TRANSPORT_NON_BLOCKING);
            if (isTransportNonBlocking != null && ((Boolean) isTransportNonBlocking).booleanValue()) {
                msgContext.getConfigurationContext().getThreadPool().execute(
                        new TransportNonBlockingInvocationWorker(msgContext, sender));
            } else {
                sender.invoke(msgContext);
            }
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

            //adding both operation specific and global out fault flows.
            
            ArrayList outFaultPhases = new ArrayList();
            outFaultPhases.addAll((ArrayList) faultExecutionChain.clone());
            outFaultPhases.addAll((ArrayList) msgContext.getConfigurationContext()
                    .getAxisConfiguration().getOutFaultFlow().clone());
            
            msgContext.setExecutionChain((ArrayList) outFaultPhases.clone());
            msgContext.setFLOW(MessageContext.OUT_FAULT_FLOW);
            invoke(msgContext);
        }

        if (!msgContext.isPaused()) {
            msgContext.setExecutionChain(
                    (ArrayList) msgContext.getConfigurationContext()
                            .getAxisConfiguration().getOutFaultFlow().clone());
            msgContext.setFLOW(MessageContext.OUT_FLOW);
            invoke(msgContext);

            // Actually send the SOAP Fault
            TransportSender sender = msgContext.getTransportOut().getSender();

            sender.invoke(msgContext);
        }
    }

    private String getSenderFaultCode(OMNamespace soapNamespace) {
        return SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(soapNamespace.getNamespaceURI())
                ? SOAP12Constants.SOAP_DEFAULT_NAMESPACE_PREFIX + ":"
                + SOAP12Constants.FAULT_CODE_SENDER
                : SOAP12Constants.SOAP_DEFAULT_NAMESPACE_PREFIX + ":"
                + SOAP11Constants.FAULT_CODE_SENDER;
    }

    /**
     * This class is the workhorse for a non-blocking invocation that uses a two
     * way transport.
     */
    private class TransportNonBlockingInvocationWorker implements Runnable {
        private MessageContext msgctx;
        private TransportSender sender;
        private boolean done;

        public TransportNonBlockingInvocationWorker(MessageContext msgctx,
                                                    TransportSender sender) {
            this.msgctx = msgctx;
            this.sender = sender;
        }

        public void run() {
            try {
                while (!done) {
                    sender.invoke(msgctx);
                    done = true;
                }
            } catch (Exception e) {
                log.info(e.getMessage());
            }
        }
    }
}
