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

package org.apache.axis.context;

import org.apache.axis.description.AxisOperation;
import org.apache.axis.description.AxisTransport;
import org.apache.axis.engine.*;
import org.apache.axis.impl.context.SimpleSessionContext;
import org.apache.axis.impl.description.AxisService;
import org.apache.axis.om.SOAPEnvelope;
import org.apache.axis.addressing.EndpointReferenceType;
import org.apache.axis.addressing.miheaders.RelatesTo;

import java.util.HashMap;

/**
 * The palce where all the service specific states are kept.
 * All the Global states kept in the <code>EngineRegistry</code> and all the
 * Service states kept in the <code>MessageContext</code>. Other runtime
 * artifacts does not keep states foward from the execution.
 */
public class MessageContext {
    private int messageStyle = Constants.SOAP_STYLE_RPC_ENCODED;
//    private HashMap messages = new HashMap();

    public static final String USER_NAME = "USER";
    public static final String PASSWARD = "PASSWD";
    public static final String TRANSPORT_TYPE = "TRANSPORT_TYPE";
    public static final String SOAP_ACTION = "SOAP_ACTION";
    public static final String TRANSPORT_DATA = "TRANSPORT_DATA";
    public static final String REQUEST_URL = "REQUEST_URL";

    private boolean processingFault = false;
    private EndpointReferenceType to;
    private EndpointReferenceType from;
    private RelatesTo relatesTo;
    private EndpointReferenceType replyTo;
    private EndpointReferenceType faultTo;
    
    private ExecutionChain chain;
    private AxisTransport transport;

    //there is a no use cas found to set those proprties 
    //so declare them final    
    private final HashMap properties;
    private final GlobalContext globalContext;

    private SessionContext sessionContext;
    private AxisService service;
    private SOAPEnvelope envelope;
    private boolean responseWritten;
    private boolean infaultFlow;
    private boolean serverSide;
    private String messageID;
    private AxisOperation operation;
    private boolean newThreadRequired = false;

    public MessageContext(EngineRegistry er) throws AxisFault {
        this.globalContext = new GlobalContext(er);
        this.sessionContext = new SimpleSessionContext();
        properties = new HashMap();
        chain = new ExecutionChain();
    }


    /**
     * @return
     */
    public EndpointReferenceType getFaultTo() {
        return faultTo;
    }

    /**
     * @return
     */
    public EndpointReferenceType getFrom() {
        return from;
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
    public boolean isInfaultFlow() {
        return infaultFlow;
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
     * @return
     */
    public Object getProperty(Object key) {
        return properties.get(key);
    }

    /**
     * @return
     */
    public RelatesTo getRelatesTo() {
        return relatesTo;
    }

    /**
     * @return
     */
    public EndpointReferenceType getReplyTo() {
        return replyTo;
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
    public EndpointReferenceType getTo() {
        return to;
    }

    /**
     * @param referance
     */
    public void setFaultTo(EndpointReferenceType referance) {
        faultTo = referance;
    }

    /**
     * @param referance
     */
    public void setFrom(EndpointReferenceType referance) {
        from = referance;
    }

    /**
     * @param b
     */
    public void setInfaultFlow(boolean b) {
        infaultFlow = b;
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
     *
     * @param key
     * @param value
     */
    public void setProperty(Object key, Object value) {
        properties.put(key, value);
    }

    /**
     * @param referance
     */
    public void setRelatesTo(RelatesTo referance) {
        relatesTo = referance;
    }

    /**
     * @param referance
     */
    public void setReplyTo(EndpointReferenceType referance) {
        replyTo = referance;
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
    public void setTo(EndpointReferenceType referance) {
        to = referance;
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
     * @param context
     */
    public void setSessionContext(SessionContext context) {
        sessionContext = context;
    }

    /**
     * @return
     */
    public int getMessageStyle() {
        return messageStyle;
    }

    /**
     * @param i
     */
    public void setMessageStyle(int i) {
        messageStyle = i;
    }
    

     public ExecutionChain getExecutionChain() {
        return this.chain;
    }

    /**
     * @param chain
     */
    public void setExecutionChain(ExecutionChain chain) {
        this.chain = chain;
    }


    /**
     * @return
     */
    public AxisTransport getTransport() {
        return transport;
    }

    /**
     * @param transport
     */
    public void setTransport(AxisTransport transport) {
        this.transport = transport;
    }

}
