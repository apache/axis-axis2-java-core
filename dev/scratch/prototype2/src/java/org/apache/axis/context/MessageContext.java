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

import org.apache.axis.Constants;
import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.addressing.miheaders.RelatesTo;
import org.apache.axis.description.AxisOperation;
import org.apache.axis.description.AxisService;
import org.apache.axis.description.AxisTransport;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.engine.EngineRegistry;
import org.apache.axis.engine.ExecutionChain;
import org.apache.axis.om.OMElement;
import org.apache.axis.om.SOAPEnvelope;

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
    private int messageStyle = Constants.SOAP_STYLE_RPC_ENCODED;
    
    /**
     *  Follwing are the defined properties that are stored in the message Context 
     */
    public static final String USER_NAME = "USER";
    public static final String PASSWORD = "PASSWD";
    public static final String TRANSPORT_TYPE = "TRANSPORT_TYPE";
    public static final String SOAP_ACTION = "SOAP_ACTION";
    public static final String TRANSPORT_WRITER = "TRANSPORT_WRITER";
    public static final String TRANSPORT_READER = "TRANSPORT_READER";
    public static final String TRANSPORT_IN = "TRANSPORT_IN";

    public static final String TRANSPORT_SUCCEED = "TRANSPORT_SUCCEED";
    //public static final String REQUEST_URL = "REQUEST_URL";

    private boolean processingFault = false;
    private EndpointReference to;
    private EndpointReference from;
    private RelatesTo relatesTo;
    private EndpointReference replyTo;
    private EndpointReference faultTo;

    private ExecutionChain chain;
    private AxisTransport transport;

    //there is a no use cas found to set those proprties 
    //so declare them final    
    private final Map properties;
    private final GlobalContext globalContext;

    private SessionContext sessionContext;
    private AxisService service;
    private SOAPEnvelope envelope;
    private boolean responseWritten;
    private boolean inFaultFlow;
    private boolean serverSide;
    private String messageID;
    private AxisOperation operation;
    private boolean newThreadRequired = false;
    private XMLStreamReader xpp;
    private OMElement soapOperationElement;
    
    /**
     * 
     * @param Engine registry
     * @param initialProperties of the message context, should be null if no properties  
     * @param sessionContext of the message context, should be null if no sessionContext
     * @throws AxisFault
     */

    public MessageContext(EngineRegistry er, Map initialProperties,SessionContext sessionContext) throws AxisFault {
        this.globalContext = new GlobalContext(er);
        if(sessionContext == null){
            this.sessionContext = new SimpleSessionContext();
        }
        
        if (initialProperties == null) {
            initialProperties = new HashMap();
        }
        properties = initialProperties;
        chain = new ExecutionChain();
    }


    /**
     * @return
     */
    public EndpointReference getFaultTo() {
        return faultTo;
    }

    /**
     * @return
     */
    public EndpointReference getFrom() {
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
    public EndpointReference getReplyTo() {
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
    public EndpointReference getTo() {
        return to;
    }

    /**
     * @param referance
     */
    public void setFaultTo(EndpointReference referance) {
        faultTo = referance;
    }

    /**
     * @param referance
     */
    public void setFrom(EndpointReference referance) {
        from = referance;
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
     * @param referance
     */
    public void setRelatesTo(RelatesTo referance) {
        relatesTo = referance;
    }

    /**
     * @param referance
     */
    public void setReplyTo(EndpointReference referance) {
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
    public void setTo(EndpointReference referance) {
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

}
