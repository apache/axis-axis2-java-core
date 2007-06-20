/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *      
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.axis2.jaxws.core;

import javax.xml.ws.handler.MessageContext.Scope;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.OperationDescription;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.util.MessageUtils;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service.Mode;
import javax.xml.ws.WebServiceException;
import java.util.HashMap;
import java.util.Map;

/**
 * The <code>org.apache.axis2.jaxws.core.MessageContext</code> is an interface that extends the
 * JAX-WS 2.0 <code>javax.xml.ws.handler.MessageContext</code> defined in the spec.  This
 * encapsulates all of the functionality needed of the MessageContext for the other JAX-WS spec
 * pieces (the handlers for example) and also provides the needed bits of contextual information for
 * the rest of the JAX-WS implementation.
 * <p/>
 * Specifically, this is responsible for providing APIs so that the client and server implementation
 * portions can get to the Message, defined by the Message Model format and also any metadata that
 * is available.
 */
public class MessageContext {

    private InvocationContext invocationCtx;
    private org.apache.axis2.context.MessageContext axisMsgCtx;
    private Map<String, Object> properties;
    private EndpointDescription endpointDesc;
    private OperationDescription operationDesc;
    private QName operationName;    //FIXME: This should become the OperationDescription
    private Message message;
    private Mode mode;
    
    /*
     * JAXWS runtime uses a request and response mc, but we need to know the pair.
     * We will use this mepCtx as a wrapper to the request and response message contexts
     * where the requestMC and responseMC have the same parent MEPContext to
     * preserve the relationship.
     */
    private MEPContext mepCtx;

    // If a local exception is thrown, the exception is placed on the message context.
    // It is not converted into a Message.
    private Throwable localException = null;

    public MessageContext() {
        axisMsgCtx = new org.apache.axis2.context.MessageContext();
        properties = new HashMap<String, Object>();
    }

    public MessageContext(org.apache.axis2.context.MessageContext mc) throws WebServiceException {
        properties = new HashMap<String, Object>();

        /*
         * Instead of creating a member MEPContext object every time, we will
         * rely on users of this MessageContext class to create a new
         * MEPContext and call setMEPContext(MEPContext)
         */
        
        if (mc != null) {
            axisMsgCtx = mc;
            message = MessageUtils.getMessageFromMessageContext(mc);
            if (message != null) {
                message.setMessageContext(this);
            }
        } else {
            axisMsgCtx = new org.apache.axis2.context.MessageContext();
        }
    }

    public InvocationContext getInvocationContext() {
        return invocationCtx;
    }

    public void setInvocationContext(InvocationContext ic) {
        invocationCtx = ic;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public EndpointDescription getEndpointDescription() {
        return endpointDesc;
    }

    public void setEndpointDescription(EndpointDescription ed) {
        endpointDesc = ed;
    }

    public OperationDescription getOperationDescription() {
        return operationDesc;
    }

    public void setOperationDescription(OperationDescription od) {
        operationDesc = od;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode m) {
        mode = m;
    }

    //FIXME: This should become the OperationDescription
    public QName getOperationName() {
        return operationName;
    }

    //FIXME: This should become the OperationDescription
    public void setOperationName(QName op) {
        operationName = op;
    }

    public void setMessage(Message msg) {
        message = msg;
        msg.setMessageContext(this);
    }

    public Message getMessage() {
        return message;
    }

    public org.apache.axis2.context.MessageContext getAxisMessageContext() {
        return axisMsgCtx;
    }

    public ClassLoader getClassLoader() {
        AxisService svc = axisMsgCtx.getAxisService();
        if (svc != null)
            return svc.getClassLoader();
        else
            return null;
    }

    /**
     * Used to determine whether or not session state has been enabled.
     *
     * @return
     */
    public boolean isMaintainSession() {
        boolean maintainSession = false;

        Boolean value = (Boolean)properties.get(BindingProvider.SESSION_MAINTAIN_PROPERTY);
        if (value != null && value.booleanValue()) {
            maintainSession = true;
        }

        return maintainSession;
    }

    /**
     * The local exception is the Throwable object held on the Message from a problem that occurred
     * due to something other than the server.  In other words, no message ever travelled across
     * the wire.
     *
     * @return the Throwable object or null
     */
    public Throwable getLocalException() {
        return localException;
    }

    /**
     * The local exception is the Throwable object held on the Message from a problem that occurred
     * due to something other than the server.  In other words, no message ever travelled across the
     * wire.
     *
     * @param t
     * @see Throwable
     */
    public void setLocalException(Throwable t) {
        localException = t;
    }
    
    /**
     * Set the wrapper MEPContext.  Internally, this method also sets
     * the MEPContext's children so the pointer is bi-directional; you can
     * get the MEPContext from the MessageContext and vice-versa.
     * 
     * @param mepCtx
     */
    public void setMEPContext(MEPContext mepCtx) {
        this.mepCtx = mepCtx;
        // and set parent's child pointer
        this.mepCtx.setResponseMessageContext(this);
    }

    public MEPContext getMEPContext() {
        return mepCtx;
    }
}
