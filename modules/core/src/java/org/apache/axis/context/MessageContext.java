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
package org.apache.axis.context;

import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.addressing.om.MessageInformationHeadersCollection;
import org.apache.axis.addressing.miheaders.RelatesTo;
import org.apache.axis.description.AxisOperation;
import org.apache.axis.description.AxisService;
import org.apache.axis.description.AxisTransport;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.engine.EngineRegistry;
import org.apache.axis.engine.ExecutionChain;
import org.apache.axis.om.OMElement;
import org.apache.axis.om.SOAPEnvelope;
import org.apache.wsdl.WSDLService;

import javax.xml.stream.XMLStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * The palce where all the service specific states are kept.
 * All the Global states kept in the <code>EngineRegistry</code> and all the
 * Service states kept in the <code>MessageContext</code>. Other runtime
 * artifacts does not keep states foward from the execution.
 */
public class MessageContext {
    /**
     * Field messageStyle
     */
    private String messageStyle = WSDLService.STYLE_RPC;

    /**
     * Follwing are the defined properties that are stored in the message Context
     */
    public static final String USER_NAME = "USER";

    /**
     * Field PASSWORD
     */
    public static final String PASSWORD = "PASSWD";


    /**
     * Field SOAP_ACTION
     */
    public static final String SOAP_ACTION = "SOAP_ACTION";

    /**
     * Field TRANSPORT_WRITER
     */
    public static final String TRANSPORT_WRITER = "TRANSPORT_WRITER";

    /**
     * Field TRANSPORT_READER
     */
    public static final String TRANSPORT_READER = "TRANSPORT_READER";

    /**
     * Field TRANSPORT_IN
     */
    public static final String TRANSPORT_IN = "TRANSPORT_IN";

    /**
     * Field TRANSPORT_SUCCEED
     */
    public static final String TRANSPORT_SUCCEED = "TRANSPORT_SUCCEED";

    // public static final String REQUEST_URL = "REQUEST_URL";

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

    MessageInformationHeadersCollection messageInformationHeaders;


    private final ExecutionChain chain;

    private final AxisTransport transport;

    /**
     * Field properties
     */
    private final Map properties;

    /**
     * Field globalContext
     */
    private final GlobalContext globalContext;

    /**
     * Field sessionContext
     */
    private final SessionContext sessionContext;

    /**
     * Field service
     */
    private AxisService service;

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
     * Field operation
     */
    private AxisOperation operation;

    /**
     * Field newThreadRequired
     */
    private boolean newThreadRequired = false;

    /**
     * Field xpp
     */
    private XMLStreamReader xpp;

    /**
     * Field soapOperationElement
     */
    private OMElement soapOperationElement;
    
    
    private boolean paused = false;
    

    /**
     * @param er            registry
     * @param initialProperties of the message context, should be null if no properties
     * @param sessionContext    of the message context, should be null if no sessionContext
     * @throws AxisFault
     */
    public MessageContext(EngineRegistry er, 
            Map initialProperties, 
            SessionContext sessionContext,
            AxisTransport transport)
            throws AxisFault {
        this.globalContext = new GlobalContext(er);
        if (sessionContext == null) {
            this.sessionContext = new SimpleSessionContext();
        } else {
            this.sessionContext = sessionContext;
        }
        if (initialProperties == null) {
            initialProperties = new HashMap();
        }
        properties = initialProperties;
        chain = new ExecutionChain();
        messageInformationHeaders = new MessageInformationHeadersCollection();
        this.transport = transport;
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
    public GlobalContext getGlobalContext() {
        return globalContext;
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
        return messageID;
    }

    /**
     * @return
     */
    public boolean isProcessingFault() {
        return processingFault;
    }

    /**
     * @param key
     * @return
     */
    public Object getProperty(Object key) {
        return properties.get(key);
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
    public void setEnvelope(SOAPEnvelope envelope) {
        this.envelope = envelope;
    }

    /**
     * @param string
     */
    public void setMessageID(String string) {
        messageID = string;
    }

    /**
     * @param b
     */
    public void setProcessingFault(boolean b) {
        processingFault = b;
    }

    /**
     * @param key
     * @param value
     */
    public void setProperty(Object key, Object value) {
        properties.put(key, value);
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
    public AxisService getService() {
        return service;
    }

    /**
     * @return
     */
    public AxisOperation getOperation() {
        return operation;
    }

    /**
     * @param operation
     */
    public void setOperation(AxisOperation operation) {
        this.operation = operation;
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
     * @param service
     */
    public void setService(AxisService service) {
        this.service = service;
    }

    /**
     * @return
     */
    public String getMessageStyle() {
        return messageStyle;
    }

    /**
     * @param i
     */
    public void setMessageStyle(String i) {
        if (i != null) {
            messageStyle = i;
        }
    }

    /**
     * Method getExecutionChain
     *
     * @return
     */
    public ExecutionChain getExecutionChain() {
        return this.chain;
    }

     /**
     * @return
     */
    public AxisTransport getTransport() {
        return transport;
    }

//    /**
//     * @param transport
//     */
//    public void setTransport(AxisTransport transport) {
//        this.transport = transport;
//    }

    /**
     * @return
     */
    public XMLStreamReader getXpp() {
        return xpp;
    }

    /**
     * @param reader
     */
    public void setXpp(XMLStreamReader reader) {
        xpp = reader;
    }

    /**
     * @return
     */
    public OMElement getSoapOperationElement() {
        return soapOperationElement;
    }

    /**
     * @param element
     */
    public void setSoapOperationElement(OMElement element) {
        soapOperationElement = element;
    }

    /**
     * @return
     */
    public Map getProperties() {
        return properties;
    }

    public void setWSAAction(String actionURI){
        messageInformationHeaders.setAction(actionURI);
    }
    public String getWSAAction(){
        return messageInformationHeaders.getAction();
    }
    public void setWSAMessageId(String messageID){
        messageInformationHeaders.setMessageId(messageID);
    }
    public String getWSAMessageId(){
        return messageInformationHeaders.getMessageId();
    }

    public MessageInformationHeadersCollection getMessageInformationHeaders() {
        return messageInformationHeaders;
    }

    public void setMessageInformationHeaders(MessageInformationHeadersCollection messageInformationHeaders) {
        this.messageInformationHeaders = messageInformationHeaders;
    }
    /**
     * @return
     */
    public boolean isPaused() {
        return paused;
    }

    /**
     * @param b
     */
    public void setPaused(boolean b) {
        paused = b;
    }
}
