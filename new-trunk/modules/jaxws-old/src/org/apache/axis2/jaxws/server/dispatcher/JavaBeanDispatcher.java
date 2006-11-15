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
package org.apache.axis2.jaxws.server.dispatcher;

import java.lang.reflect.Method;

import javax.xml.ws.Holder;
import javax.xml.ws.soap.SOAPBinding;

import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.core.util.MessageContextUtils;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.EndpointInterfaceDescription;
import org.apache.axis2.jaxws.description.OperationDescription;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.message.Block;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.factory.MessageFactory;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The JavaBeanDispatcher is used to manage creating an instance of a 
 * JAX-WS service implementation bean and dispatching the inbound 
 * request to that instance.
 */
public class JavaBeanDispatcher extends JavaDispatcher {

    private static final Log log = LogFactory.getLog(JavaBeanDispatcher.class);
    
    private Object serviceInstance;
    
    public JavaBeanDispatcher(Class implClass) {
        super(implClass);
    }
    
    /*
     * (non-Javadoc)
     * @see org.apache.axis2.jaxws.server.EndpointDispatcher#invoke(org.apache.axis2.jaxws.core.MessageContext)
     */
    public MessageContext invoke(MessageContext mc) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Preparing to invoke service endpoint implementation " +
                    "class: " + serviceImplClass.getName());
        }

        mc.setOperationName(mc.getAxisMessageContext().getAxisOperation().getName());
        OperationDescription opDesc = getOperationDescription(mc);
        mc.setOperationDescription(opDesc);
        
        Mapper mapper = new MapperImpl();
        Method target = mapper.getJavaMethod(mc, serviceImplClass);
        Object[] params = mapper.getInputParameterData(mc, target);
        if(params!=null){
        	for(Object param:params){
        		if(param!=null && param instanceof Holder){
        			throw new UnsupportedOperationException("Holders not supported yet");
        		}
        	}
        }
        //At this point, we have the method that is going to be invoked and
        //the parameter data to invoke it with, so create an instance and 
        //do the invoke.
        serviceInstance = createServiceInstance();
        Object response = target.invoke(serviceInstance, params);
        
        if(opDesc.isOneWay()){
        	//Dont return response message context if its a one way operation.
        	return null;
        }
        
        //TODO:Support Holders 
        if(!opDesc.isOneWay() && target.getReturnType().getName().equals("void")){
        	//process everything that does not have holders two way with void is empty responsewrapper in soap body
        	
        }
        Block responseBlock = mapper.getOutputParameterBlock(mc, response, target);
       
        //Create the Message for the response
        MessageFactory factory = (MessageFactory) FactoryRegistry.getFactory(
                MessageFactory.class);
        Message message = factory.create(mc.getMessage().getProtocol());
        message.setBodyBlock(0, responseBlock);
        
        MessageContext responseMsgCtx = MessageContextUtils.createMessageMessageContext(mc);
        responseMsgCtx.setMessage(message);
        
        //Enable MTOM if necessary
        EndpointInterfaceDescription epInterfaceDesc = opDesc.getEndpointInterfaceDescription();
        EndpointDescription epDesc = epInterfaceDesc.getEndpointDescription();
        
        String bindingType = epDesc.getBindingTypeValue();
        if (bindingType != null) {
            if (bindingType.equals(SOAPBinding.SOAP11HTTP_MTOM_BINDING) ||
                bindingType.equals(SOAPBinding.SOAP12HTTP_MTOM_BINDING)) {
                message.setMTOMEnabled(true);
            }
        }
         
        return responseMsgCtx;
    }
    
    /*
     * Gets the OperationDescription associated with the request that is currently
     * being processed.
     * 
     *  Note that this is not done in the EndpointController since operations are only relevant
     *  to Endpoint-based implementation (i.e. not to Proxy-based ones)s
     */

    private OperationDescription getOperationDescription(MessageContext mc) {
        ServiceDescription sd = mc.getServiceDescription();
        EndpointDescription[] eds = sd.getEndpointDescriptions();
        EndpointDescription ed = eds[0];
        EndpointInterfaceDescription eid = ed.getEndpointInterfaceDescription();
        
        OperationDescription[] ops = eid.getOperation(mc.getOperationName());
        OperationDescription op = ops[0];
        
        if (log.isDebugEnabled()) {
            log.debug("wsdl operation: " + op.getName());
            log.debug("   java method: " + op.getJavaMethodName());
        }
        
        return op;        
    }
    
    
}
