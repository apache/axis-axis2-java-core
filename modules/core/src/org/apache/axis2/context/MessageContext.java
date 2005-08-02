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
package org.apache.axis2.context;

import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.MessageInformationHeaders;
import org.apache.axis2.addressing.miheaders.RelatesTo;
import org.apache.axis2.description.OperationDescription;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.ServiceDescription;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.impl.llom.soap11.SOAP11Constants;
import org.apache.axis2.soap.impl.llom.soap12.SOAP12Constants;
import org.apache.axis2.AxisFault;

import javax.xml.namespace.QName;

/**
 * The palce where all the service specific states are kept.
 * All the Global states kept in the <code>EngineRegistry</code> and all the
 * Service states kept in the <code>MessageContext</code>. Other runtime
 * artifacts does not keep states foward from the execution.
 */
public class MessageContext extends AbstractContext {

    public static final String TRANSPORT_HEADERS = "TRANSPORT_HEADERS";
    /**
     * Field TRANSPORT_WRITER
     */
    public static final String TRANSPORT_OUT = "TRANSPORT_OUT";

    /**
     * Field TRANSPORT_READER
     */
    public static final String TRANSPORT_IN = "TRANSPORT_IN";

    /**
     * Field  CHARACTER_SET_ENCODING
     */
    public static final String CHARACTER_SET_ENCODING =
        "CHARACTER_SET_ENCODING";

    /**
     * Field UTF_8
     * This is the 'utf-8' value for CHARACTER_SET_ENCODING property
     */
    public static final String UTF_8 = "utf-8";

    /**
     * Field UTF_8
     * This is the 'utf-8' value for CHARACTER_SET_ENCODING property
     */
    public static final String UTF_16 = "utf-16";

    /**
     * Field DEFAULT_CHAR_SET_ENCODING 
     * This is the default value for CHARACTER_SET_ENCODING property
     */
    public static final String DEFAULT_CHAR_SET_ENCODING = UTF_8;

    /**
     * Field TRANSPORT_SUCCEED
     */
    public static final String TRANSPORT_SUCCEED = "TRANSPORT_SUCCEED";

    /**
     * Field processingFault
     */
    private boolean processingFault = false;

    /**
     * Addressing Information for Axis 2
     * Following Properties will be kept inside this, these fields will be initially filled by
     * the transport. Then later a addressing handler will make relevant changes to this, if addressing
     * information is present in the SOAP header.
     */

    private MessageInformationHeaders messageInformationHeaders;

    private OperationContext operationContext;
    private ServiceContext serviceContext;
    private ConfigurationContext configurationContext;

    private TransportInDescription transportIn;

    private TransportOutDescription transportOut;

    /**
     * Field sessionContext
     */
    private final SessionContext sessionContext;

    /**
     * Field service
     */

    /**
     * Field envelope
     */
    private SOAPEnvelope envelope;

    /**
     * Field responseWritten
     */
    private boolean responseWritten;

    /**
     * Field inFaultFlow
     */
    private boolean inFaultFlow;

    /**
     * Field serverSide
     */
    private boolean serverSide;

    /**
     * Field messageID
     */
    private String messageID;

    /**
     * Field newThreadRequired
     */
    private boolean newThreadRequired = false;

    private boolean paused = false;

    public boolean outPutWritten = false;

    private String serviceInstanceID;

    private String pausedPhaseName;

    private QName pausedHandlerName;

    private String soapAction;

    //Are we doing MTOM now?
    private boolean doingMTOM = false;
    //Are we doing REST now?
    private boolean doingREST = false;
    //Rest through GET of HTTP
    private boolean doRESTthroughPOST = true;

    private boolean isSOAP11 = true;

    /**
     * Conveniance Method, but before call engine.send() or  engine.receive() one must send transport in/out
     *
     * @param engineContext
     * @throws AxisFault
     */

    public MessageContext(ConfigurationContext engineContext)
        throws AxisFault {
        this(engineContext, null, null, null);
    }

    public MessageContext(
        ConfigurationContext engineContext,
        TransportInDescription transportIn,
        TransportOutDescription transportOut)
        throws AxisFault {
        this(engineContext, null, transportIn, transportOut);
    }

    /**
     * @param sessionContext
     * @param transportIn
     * @param transportOut
     * @throws AxisFault
     */

    public MessageContext(
        ConfigurationContext engineContext,
        SessionContext sessionContext,
        TransportInDescription transportIn,
        TransportOutDescription transportOut)
        throws AxisFault {
        super(null);

        if (sessionContext == null) {
            this.sessionContext = new SessionContext(null);
        } else {
            this.sessionContext = sessionContext;
        }
        messageInformationHeaders = new MessageInformationHeaders();
        this.transportIn = transportIn;
        this.transportOut = transportOut;
        this.configurationContext = engineContext;

    }

    /**
     * @return
     */
    public EndpointReference getFaultTo() {
        return messageInformationHeaders.getFaultTo();
    }

    /**
     * @return
     */
    public EndpointReference getFrom() {
        return messageInformationHeaders.getFrom();
    }

    /**
     * @return
     */
    public boolean isInFaultFlow() {
        return inFaultFlow;
    }

    /**
     * @return
     */
    public SOAPEnvelope getEnvelope() {
        return envelope;
    }

    /**
     * @return
     */
    public String getMessageID() {
        return messageInformationHeaders.getMessageId();
    }

    /**
     * @return
     */
    public boolean isProcessingFault() {
        return processingFault;
    }

    /**
     * @return
     */
    public RelatesTo getRelatesTo() {
        return messageInformationHeaders.getRelatesTo();
    }

    /**
     * @return
     */
    public EndpointReference getReplyTo() {
        return messageInformationHeaders.getReplyTo();
    }

    /**
     * @return
     */
    public boolean isResponseWritten() {
        return responseWritten;
    }

    /**
     * @return
     */
    public boolean isServerSide() {
        return serverSide;
    }

    /**
     * @return
     */
    public SessionContext getSessionContext() {
        return sessionContext;
    }

    /**
     * @return
     */
    public EndpointReference getTo() {
        return messageInformationHeaders.getTo();
    }

    /**
     * @param reference
     */
    public void setFaultTo(EndpointReference reference) {
        messageInformationHeaders.setFaultTo(reference);
    }

    /**
     * @param reference
     */
    public void setFrom(EndpointReference reference) {
        messageInformationHeaders.setFrom(reference);
    }

    /**
     * @param b
     */
    public void setInFaultFlow(boolean b) {
        inFaultFlow = b;
    }

    /**
     * @param envelope
     */
    public void setEnvelope(SOAPEnvelope envelope) throws AxisFault {
        this.envelope = envelope;
        String soapNamespaceURI = envelope.getNamespace().getName();
        if (SOAP12Constants
            .SOAP_ENVELOPE_NAMESPACE_URI
            .equals(soapNamespaceURI)) {
            isSOAP11 = false;
        } else if (
            SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(
                soapNamespaceURI)) {
            isSOAP11 = true;
        } else {
            throw new AxisFault("Unknown SOAP Version. Current Axis handles only SOAP 1.1 and SOAP 1.2 messages");
        }

    }

    /**
     * @param string
     */
    public void setMessageID(String string) {
        messageInformationHeaders.setMessageId(string);
    }

    /**
     * @param b
     */
    public void setProcessingFault(boolean b) {
        processingFault = b;
    }

    /**
     * @param reference
     */
    public void setRelatesTo(RelatesTo reference) {
        messageInformationHeaders.setRelatesTo(reference);
    }

    /**
     * @param referance
     */
    public void setReplyTo(EndpointReference referance) {
        messageInformationHeaders.setReplyTo(referance);
    }

    /**
     * @param b
     */
    public void setResponseWritten(boolean b) {
        responseWritten = b;
    }

    /**
     * @param b
     */
    public void setServerSide(boolean b) {
        serverSide = b;
    }

    /**
     * @param referance
     */
    public void setTo(EndpointReference referance) {
        messageInformationHeaders.setTo(referance);
    }

    /**
     * @return
     */
    public boolean isNewThreadRequired() {
        return newThreadRequired;
    }

    /**
     * @param b
     */
    public void setNewThreadRequired(boolean b) {
        newThreadRequired = b;
    }

    /**
     * Method getExecutionChain
     */

    public void setWSAAction(String actionURI) {
        messageInformationHeaders.setAction(actionURI);
    }

    public String getWSAAction() {
        return messageInformationHeaders.getAction();
    }

    public void setWSAMessageId(String messageID) {
        messageInformationHeaders.setMessageId(messageID);
    }

    public String getWSAMessageId() {
        return messageInformationHeaders.getMessageId();
    }

    public MessageInformationHeaders getMessageInformationHeaders() {
        return messageInformationHeaders;
    }

    /**
     * @return
     */
    public boolean isPaused() {
        return paused;
    }

    /**
     */
    public void setPausedTrue(QName handlerName) {
        paused = true;
        this.pausedHandlerName = handlerName;
    }

    public void setPausedFalse() {
        paused = false;
    }

    /**
     * @return
     */
    public TransportInDescription getTransportIn() {
        return transportIn;
    }

    /**
     * @return
     */
    public TransportOutDescription getTransportOut() {
        return transportOut;
    }

    /**
     * @param in
     */
    public void setTransportIn(TransportInDescription in) {
        transportIn = in;
    }

    /**
     * @param out
     */
    public void setTransportOut(TransportOutDescription out) {
        transportOut = out;
    }

    /**
     * @return
     */
    public OperationContext getOperationContext() {
        return operationContext;
    }

    /**
     * @param context
     */
    public void setOperationContext(OperationContext context) {
        operationContext = context;
        if (serviceContext != null && operationContext.getParent() == null) {
            operationContext.setParent(serviceContext);
        }
        this.setParent(operationContext);
    }

    /**
     * @return
     */
    public boolean isOutPutWritten() {
        return outPutWritten;
    }

    /**
     * @param b
     */
    public void setOutPutWritten(boolean b) {
        outPutWritten = b;
    }

    /**
     * @return Returns the serviceInstanceID.
     */
    public String getServiceInstanceID() {
        return serviceInstanceID;
    }

    /**
     * @param serviceInstanceID The serviceInstanceID to set.
     */
    public void setServiceInstanceID(String serviceInstanceID) {
        this.serviceInstanceID = serviceInstanceID;
    }

    public ConfigurationContext getSystemContext() {
        return configurationContext;
    }

    /**
     * @return
     */
    public ServiceContext getServiceContext() {
        return serviceContext;
    }

    /**
     * @param context
     */
    public void setConfigurationContext(ConfigurationContext context) {
        configurationContext = context;
    }

    /**
     * @param context
     */
    public void setServiceContext(ServiceContext context) {
        serviceContext = context;
        if (operationContext != null && operationContext.getParent() != null) {
            operationContext.setParent(context);
        }
    }

    /**
     * @param collection
     */
    public void setMessageInformationHeaders(MessageInformationHeaders collection) {
        messageInformationHeaders = collection;
    }

    /* (non-Javadoc)
    * @see org.apache.axis2.context.AbstractContext#getProperty(java.lang.Object, boolean)
    */
    public Object getProperty(String key, boolean persistent) {
        Object obj = super.getProperty(key, persistent);

        //The context hirachy might not have constructed fully, the check should
        //look for the disconnected grandparents
        if (obj == null
            && operationContext == null
            && serviceContext != null) {
            obj = serviceContext.getProperty(key, persistent);
        }
        if (obj == null && operationContext == null) {
            obj = configurationContext.getProperty(key, persistent);
        }
        //Search the configurations
        Parameter param = null;
        if (obj == null && operationContext != null) {
            OperationDescription opDesc = operationContext.getAxisOperation();
            param = opDesc.getParameter(key);
        }
        if (param == null && serviceContext != null) {
            ServiceDescription serviceDesc = serviceContext.getServiceConfig();
            param = serviceDesc.getParameter(key);
        }
        if (param == null && configurationContext != null) {
            AxisConfiguration baseConfig =
                configurationContext.getAxisConfiguration();
            param = baseConfig.getParameter(key);
        }
        if (param != null) {
            obj = param.getValue();
        }
        return obj;
    }

    /**
     * @return
     */
    public QName getPausedHandlerName() {
        return pausedHandlerName;
    }

    /**
     * @return
     */
    public String getPausedPhaseName() {
        return pausedPhaseName;
    }

    /**
     * @param name
     */
    public void setPausedPhaseName(String name) {
        pausedPhaseName = name;
    }

    /**
     * @return
     */
    public String getSoapAction() {
        return soapAction;
    }

    /**
     * @param string
     */
    public void setSoapAction(String string) {
        soapAction = string;
    }

    /**
     * @return
     */
    public boolean isDoingMTOM() {
        return doingMTOM;
    }

    /**
     * @param b
     */
    public void setDoingMTOM(boolean b) {
        doingMTOM = b;
    }

    /**
     * @return
     */
    public boolean isDoingREST() {
        return doingREST;
    }

    /**
     * @param b
     */
    public void setDoingREST(boolean b) {
        doingREST = b;
    }

    public void setRestThroughPOST(boolean b) {
        doRESTthroughPOST = b;
    }

    public boolean isRestThroughPOST() {
        return doRESTthroughPOST;
    }

    public boolean isSOAP11() {
        return isSOAP11;
    }
}
