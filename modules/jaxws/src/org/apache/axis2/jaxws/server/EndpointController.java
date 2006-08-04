/*
 * Copyright 2004,2005 The Apache Software Foundation.
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
package org.apache.axis2.jaxws.server;

import javax.xml.ws.Provider;

import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.core.InvocationContext;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.factory.MessageFactory;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The EndpointController is the server side equivalent to the
 * InvocationController on the client side.  It is an abstraction of the server
 * side endpoint invocation that encapsulates all of the Axis2 semantics.
 * 
 * Like the InvocationController, this class is responsible for invoking the 
 * JAX-WS application handler chain along with taking all of the provided 
 * information and setting up what's needed to perform the actual invocation 
 * of the endpoint.
 *
 */
public class EndpointController {
    
    private static final Log log = LogFactory.getLog(EndpointController.class);
	private static final String PARAM_SERVICE_CLASS = "ServiceClass";

    public EndpointController() {}

    /**
     * This method is used to start the JAX-WS invocation of a target endpoint.
     * It takes an InvocationContext, which must have a MessageContext specied
     * for the request.  Once the invocation is complete, the information will
     * be stored  
     */
    public InvocationContext invoke(InvocationContext ic) {
        try {
            MessageContext requestMsgCtx = ic.getRequestMessageContext();
            org.apache.axis2.context.MessageContext axisRequestMsgCtx = 
                requestMsgCtx.getAxisMessageContext();
            
            // As the request comes in, the SOAP message will first exist only
            // on the Axis2 MessageContext.  We need to get it from there and 
            // wrap it with the JAX-WS Message model abstraction.  This will 
            // allow us to get the params out in a number of forms later on.
            SOAPEnvelope soapEnv = axisRequestMsgCtx.getEnvelope();
            if (soapEnv == null) {
                throw ExceptionFactory.makeWebServiceException(Messages.getMessage("EndpointControllerErr1"));
            }
            
            MessageFactory msgFactory = (MessageFactory) FactoryRegistry.getFactory(MessageFactory.class);
            Message requestMsg = msgFactory.createFrom(soapEnv);
            requestMsgCtx.setMessage(requestMsg);
            
            // Get the appropriate EndpointDispatcher instance based on the 
            // information availabe in the MessageContext.
            EndpointDispatcher dispatcher = getDispatcher(axisRequestMsgCtx);
            
            MessageContext responseMsgContext = dispatcher.invoke(requestMsgCtx);
            
            // The response MessageContext should be set on the InvocationContext
            ic.setResponseMessageContext(responseMsgContext);
        } catch (Exception e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
        
        return ic;
    }
    
    /*
	 * Get the appropriate dispatcher for a given service endpoint.  If the 
     * target endpoint implements the javax.xml.ws.Provider<T> interface, then
     * the ProviderDispatcher should be returned.  Other wise, it should be
     * the JavaBeanDispatcher.
	 */
	private EndpointDispatcher getDispatcher(org.apache.axis2.context.MessageContext msgContext) throws Exception {
		EndpointDispatcher dispatcherInstance = null;
    	
        // The PARAM_SERVICE_CLASS property that is set on the AxisService
        // will tell us what the implementation
        AxisService as = msgContext.getAxisService();
    	Parameter asp = as.getParameter(PARAM_SERVICE_CLASS);
    	
        // If there was no implementation class, we should not go any further
        if (asp == null) {
            throw ExceptionFactory.makeWebServiceException(Messages.getMessage("EndpointControllerErr2"));
        }
        
        // Load the service implementation class  
    	Class cls = getImplClass(as.getClassLoader(), asp);
        
        // Check to see whether the class that was specified is an instance
        // of the javax.xml.ws.Provider.  If not, stop processing.
    	if(cls.getSuperclass().isInstance(Provider.class)) {
    		ProviderDispatcher pd = new ProviderDispatcher(cls);
    		dispatcherInstance = pd;
    	}
        else {
            throw ExceptionFactory.makeWebServiceException(Messages.getMessage("EndpointControllerErr3", cls.getName()));
        }
    	
    	return dispatcherInstance;
    }
	
	/*
     * Tries to load the implementation class that was specified for the
     * target endpoint using the supplied ClassLoader.  
	 */
	private Class getImplClass(ClassLoader cl, Parameter param){
		Class _class = null;
        
        // TODO: What should be done if the supplied ClassLoader is null?
		String className = null;
		try{
			className = ((String) param.getValue()).trim();
			
            if (log.isDebugEnabled()) {
                log.debug("Attempting to load service impl class: " + className);
            }
            
            _class = Class.forName(className, true, cl);
		}catch(java.lang.ClassNotFoundException cnf ){
			throw ExceptionFactory.makeWebServiceException(Messages.getMessage("EndpointControllerErr4", className));
		}
		
		return _class;
	}
	
}
