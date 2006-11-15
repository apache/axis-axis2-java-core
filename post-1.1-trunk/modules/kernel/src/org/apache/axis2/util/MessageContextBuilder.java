/*
* Copyright 2004,2005,2006 The Apache Software Foundation.
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

package org.apache.axis2.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFault;
import org.apache.axiom.soap.SOAPFaultCode;
import org.apache.axiom.soap.SOAPFaultDetail;
import org.apache.axiom.soap.SOAPFaultNode;
import org.apache.axiom.soap.SOAPFaultReason;
import org.apache.axiom.soap.SOAPFaultRole;
import org.apache.axiom.soap.SOAPProcessingException;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.AddressingHelper;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.RelatesTo;
import org.apache.axis2.addressing.AddressingConstants.Final;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.wsdl.WSDLConstants;

public class MessageContextBuilder {
    
    /**
     * Creates a new 'response' message context based on a 'request' message context
     * Only deals with properties/fields that are the same for both 'normal' and fault responses.
     */
    private static MessageContext createResponseMessageContext(MessageContext inMessageContext) throws AxisFault {
        MessageContext newmsgCtx = new MessageContext();

        newmsgCtx.setConfigurationContext(inMessageContext.getConfigurationContext());
        newmsgCtx.setSessionContext(inMessageContext.getSessionContext());
        newmsgCtx.setTransportIn(inMessageContext.getTransportIn());
        newmsgCtx.setTransportOut(inMessageContext.getTransportOut());
        newmsgCtx.setMessageID(UUIDGenerator.getUUID());
        newmsgCtx.addRelatesTo(new RelatesTo(inMessageContext.getOptions().getMessageId()));
        
        newmsgCtx.setProperty(AddressingConstants.WS_ADDRESSING_VERSION,
                inMessageContext.getProperty(AddressingConstants.WS_ADDRESSING_VERSION));
        newmsgCtx.setProperty(AddressingConstants.DISABLE_ADDRESSING_FOR_OUT_MESSAGES,
                inMessageContext.getProperty(AddressingConstants.DISABLE_ADDRESSING_FOR_OUT_MESSAGES));

        // Setting the charater set encoding
        newmsgCtx.setProperty(Constants.Configuration.CHARACTER_SET_ENCODING,
                inMessageContext.getProperty(Constants.Configuration.CHARACTER_SET_ENCODING));
        newmsgCtx.setDoingREST(inMessageContext.isDoingREST());

        newmsgCtx.setOperationContext(inMessageContext.getOperationContext());
        
        ServiceContext serviceContext = inMessageContext.getServiceContext();
        if (serviceContext != null) {
            newmsgCtx.setServiceContext(serviceContext);
        }
        
        newmsgCtx.setProperty(MessageContext.TRANSPORT_OUT,
                inMessageContext.getProperty(MessageContext.TRANSPORT_OUT));
        newmsgCtx.setProperty(Constants.OUT_TRANSPORT_INFO,
                inMessageContext.getProperty(Constants.OUT_TRANSPORT_INFO));
        
        return newmsgCtx;
    }
    
    /**
     * Creates a MessageContext for use with a non-fault response based on an request MessageContext
     */
    public static MessageContext createOutMessageContext(MessageContext inMessageContext) throws AxisFault {
        
        // Create a basic response MessageContext with basic fields copied
        MessageContext newmsgCtx = createResponseMessageContext(inMessageContext); 
        
        // Simple response so set To to value of inbound ReplyTo
        newmsgCtx.setTo(inMessageContext.getReplyTo());

        // do Target Resolution
        newmsgCtx.getConfigurationContext().getAxisConfiguration().getTargetResolverChain().resolveTarget(newmsgCtx);

        // Determine ReplyTo for respome message. Normally 'None URI' but has a value if SOAP Session support is in use
        AxisService axisService = inMessageContext.getAxisService();
        if (axisService != null && Constants.SCOPE_SOAP_SESSION.equals(axisService.getScope())) {
            newmsgCtx.setReplyTo(new EndpointReference(AddressingConstants.Final.WSA_ANONYMOUS_URL));
            // add the service group id as a reference parameter
            String serviceGroupContextId = inMessageContext.getServiceGroupContextId();
            if (serviceGroupContextId != null && !"".equals(serviceGroupContextId)) {
                EndpointReference replyToEPR = newmsgCtx.getReplyTo();
                replyToEPR.addReferenceParameter(new QName(Constants.AXIS2_NAMESPACE_URI,
                        Constants.SERVICE_GROUP_ID, Constants.AXIS2_NAMESPACE_PREFIX), serviceGroupContextId);
            }
        } else {
            newmsgCtx.setReplyTo(new EndpointReference(AddressingConstants.Final.WSA_NONE_URI));            
        }
        
        // Set wsa:Action for response message
        // Use specified value if available
        AxisOperation ao = inMessageContext.getAxisOperation();
        if ((ao!=null) && (ao.getOutputAction() != null)) {
            newmsgCtx.setWSAAction(ao.getOutputAction());
        } else { // If not, simply copy the request value. Almost always invalid.
            newmsgCtx.setWSAAction(inMessageContext.getWSAAction());
        }
        
        newmsgCtx.setAxisMessage(ao.getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE));

        newmsgCtx.setDoingMTOM(inMessageContext.isDoingMTOM());
        newmsgCtx.setServerSide(inMessageContext.isServerSide());
        newmsgCtx.setServiceGroupContextId(inMessageContext.getServiceGroupContextId());

        // Ensure transport settings match the scheme for the To EPR
        setupCorrectTransportOut(newmsgCtx);
        
        return newmsgCtx;
    }

    /**
     * This method is called to handle any error that occurs at inflow or outflow. But if the
     * method is called twice, it implies that sending the error handling has failed, in which case
     * the method logs the error and exits.
     */
    public static MessageContext createFaultMessageContext(MessageContext processingContext, Throwable e)
            throws AxisFault {
        if (processingContext.isProcessingFault()) {
            // We get the error file processing the fault. nothing we can do
            throw new AxisFault(Messages.getMessage("errorwhileProcessingFault"));
        }

        // Create a basic response MessageContext with basic fields copied
        MessageContext faultContext = createResponseMessageContext(processingContext);
        
        // Register the fault message context
        if (processingContext.getAxisOperation() != null && processingContext.getOperationContext() != null) {
            processingContext.getAxisOperation().addFaultMessageContext(faultContext, processingContext.getOperationContext());
        }

        faultContext.setProcessingFault(true);
        faultContext.setServerSide(true);
        
        // Not worth setting up the session information on a fault flow
        faultContext.setReplyTo(new EndpointReference(AddressingConstants.Final.WSA_NONE_URI));

        // Set wsa:Action for response message
        // Use specified value if available
        AxisOperation op = processingContext.getAxisOperation();
        if(op != null && op.getFaultAction()!=null){
            faultContext.setWSAAction(op.getFaultAction());
        }else{ //If, for some reason there is no value set, should use a sensible action.
            faultContext.setWSAAction(Final.WSA_SOAP_FAULT_ACTION);
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
        boolean shouldSendFaultToFaultTo = AddressingHelper.shouldSendFaultToFaultTo(processingContext);
        EndpointReference faultTo = processingContext.getFaultTo();
        if (faultTo != null && shouldSendFaultToFaultTo) {
            faultContext.setTo(faultTo);
        } else {
            faultContext.setTo(processingContext.getReplyTo());
        }
        
        // do Target Resolution
        faultContext.getConfigurationContext().getAxisConfiguration().getTargetResolverChain().resolveTarget(faultContext);

        // Ensure transport settings match the scheme for the To EPR
        setupCorrectTransportOut(faultContext);

        SOAPEnvelope envelope = createFaultEnvelope(processingContext, e);
        faultContext.setEnvelope(envelope);

        return faultContext;
    }
    
    /**
     * Ensure that if the scheme of the To EPR for the response is different than the
     * transport used for the request that the correct TransportOut is available
     */
    private static void setupCorrectTransportOut(MessageContext context) throws AxisFault{
        // Determine that we have the correct transport available.
        TransportOutDescription transportOut = context.getTransportOut();

        try {
            EndpointReference responseEPR = context.getTo();
            if (context.isServerSide() && responseEPR != null) {
                if (!responseEPR.hasAnonymousAddress() && !responseEPR.hasNoneAddress()) {
                    URI uri = new URI(responseEPR.getAddress());
                    String scheme = uri.getScheme();
                    if (!transportOut.getName().getLocalPart().equals(scheme)) {
                        ConfigurationContext configurationContext = context.getConfigurationContext();
                        transportOut = configurationContext.getAxisConfiguration()
                                .getTransportOut(new QName(scheme));
                        if (transportOut == null) {
                            throw new AxisFault("Can not find the transport sender : " + scheme);
                        }
                        context.setTransportOut(transportOut);
                    }
                }
            }
        } catch (URISyntaxException urise) {
            throw new AxisFault(urise);
        }
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
    private static SOAPEnvelope createFaultEnvelope(MessageContext context, Throwable e) {
        SOAPEnvelope envelope;

        if (context.isSOAP11()) {
            envelope = OMAbstractFactory.getSOAP11Factory().getDefaultFaultEnvelope();
        } else {
            // Following will make SOAP 1.2 as the default, too.
            envelope = OMAbstractFactory.getSOAP12Factory().getDefaultFaultEnvelope();
        }
        SOAPFault fault = envelope.getBody().getFault();
        
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
        OperationContext oc = context.getOperationContext();
        Object flagFromContext = null;
        if(oc!=null){
            flagFromContext = context.getOperationContext().getProperty(Constants.Configuration.SEND_STACKTRACE_DETAILS_WITH_FAULTS);
        }
        if(flagFromContext!=null){
            sendStacktraceDetailsWithFaults = JavaUtils.isTrue(flagFromContext);
        }else{
            Parameter param = context.getParameter(Constants.Configuration.SEND_STACKTRACE_DETAILS_WITH_FAULTS);
            sendStacktraceDetailsWithFaults = JavaUtils.isTrue(param.getValue());    
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

        return envelope;
    }
    
    /**
     * By the time the exception comes here it can be wrapped by so many levels. This will crip down
     * to the root cause and get the initial error depending on the property
     *
     * @param e
     */
    private static String getFaultReasonFromException(Throwable e, MessageContext context) {
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
    
    private static String getSenderFaultCode(OMNamespace soapNamespace) {
        return SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(soapNamespace.getNamespaceURI())
                ? SOAP12Constants.SOAP_DEFAULT_NAMESPACE_PREFIX + ":"
                + SOAP12Constants.FAULT_CODE_SENDER
                : SOAP12Constants.SOAP_DEFAULT_NAMESPACE_PREFIX + ":"
                + SOAP11Constants.FAULT_CODE_SENDER;
    }
}
