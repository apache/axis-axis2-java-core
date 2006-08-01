/*
 * Copyright 2006 The Apache Software Foundation.
 * Copyright 2006 International Business Machines Corp.
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
package org.apache.axis2.jaxws.core;

import java.util.HashMap;
import java.util.Map;

import javax.xml.ws.Service.Mode;

import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.message.Message;

/**
 * The <code>org.apache.axis2.jaxws.core.MessageContext</code> is
 * an interface that extends the JAX-WS 2.0 <code>javax.xml.ws.handler.MessageContext</code>
 * defined in the spec.  This encapsulates all of the functionality needed
 * of the MessageContext for the other JAX-WS spec pieces (the handlers 
 * for example) and also provides the needed bits of contextual information 
 * for the rest of the JAX-WS implementation.
 * 
 * Specifically, this is responsible for providing APIs so that the client 
 * and server implementation portions can get to the Message, defined by the 
 * Message Model format and also any metadata that is available.
 */
public class MessageContext {

    private org.apache.axis2.context.MessageContext axisMsgCtx;
    private Map<String, Object> properties;
    private ServiceDescription serviceDesc;
    private Message message;
    private Mode mode;
        
    public MessageContext() {
        axisMsgCtx = new org.apache.axis2.context.MessageContext();
        properties = new HashMap<String, Object>();
    }
    
    public MessageContext(org.apache.axis2.context.MessageContext mc) {
        axisMsgCtx = mc;
        properties = new HashMap<String, Object>();
    }
    
    public Map<String, Object> getProperties() {   
        return properties;
    }
    
    public ServiceDescription getServiceDescription() {
        return serviceDesc;
    }
    
    public void setServiceDescription(ServiceDescription sd) {
        serviceDesc = sd;
    }
    
    public Mode getMode() {
        return mode;
    }
    
    public void setMode(Mode m) {
        mode = m;
    }
    
    public String getOperationName() {
        return null;
    }
    
    public void setMessage(Message msg) {
        message = msg;
    }
    
    public Message getMessage() {
        return message;
    }
    
    public org.apache.axis2.context.MessageContext getAxisMessageContext() {
        return axisMsgCtx;
    }
}
