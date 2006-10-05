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

import javax.xml.namespace.QName;
import javax.xml.ws.Service.Mode;

import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.description.OperationDescription;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.factory.MessageFactory;
import org.apache.axis2.jaxws.registry.FactoryRegistry;

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
    private OperationDescription operationDesc;
    private QName operationName;    //FIXME: This should become the OperationDescription
    private Message message;
    private Mode mode;
        
    public MessageContext() {
        axisMsgCtx = new org.apache.axis2.context.MessageContext();
        properties = new HashMap<String, Object>();
    }
    
    public MessageContext(org.apache.axis2.context.MessageContext mc) {
        axisMsgCtx = mc;
        properties = new HashMap<String, Object>();
        
        //If the Axis2 MessageContext that was passed in already had a SOAPEnvelope
        //set on it, grab that and create a JAX-WS Message out of it.
        SOAPEnvelope soapEnv = mc.getEnvelope();
        if (soapEnv != null) {
            MessageFactory msgFactory = (MessageFactory) FactoryRegistry.getFactory(MessageFactory.class);
            Message newMessage = null;
            try {
                newMessage = msgFactory.createFrom(soapEnv);
            } catch (Exception e) {
                throw ExceptionFactory.makeWebServiceException("Could not create new Message");
            }
            
            message = newMessage;
        }

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
}
