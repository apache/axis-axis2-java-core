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

import javax.jws.soap.SOAPBinding.ParameterStyle;
import javax.jws.soap.SOAPBinding.Style;
import javax.xml.namespace.QName;
import javax.xml.ws.soap.SOAPBinding;

import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.core.util.MessageContextUtils;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.EndpointInterfaceDescription;
import org.apache.axis2.jaxws.description.OperationDescription;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.marshaller.MethodMarshaller;
import org.apache.axis2.jaxws.marshaller.factory.MethodMarshallerFactory;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.Protocol;
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
    private ServiceDescription serviceDesc = null;
    private EndpointDescription endpointDesc = null;
    private OperationDescription operationDesc = null;
    private MethodMarshaller methodMarshaller = null;
    
    public JavaBeanDispatcher(Class implClass, Object serviceInstance) {
        super(implClass, serviceInstance);
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
        
        initialize(mc);
        methodMarshaller = createMethodMarshaller(mc.getMessage().getProtocol());
        Object[] methodInputParams = methodMarshaller.demarshalRequest(mc.getMessage());
        Method target = getJavaMethod(mc, serviceImplClass);

        //At this point, we have the method that is going to be invoked and
        //the parameter data to invoke it with, so we use the instance and 
        //do the invoke.
        //Passing method input params to grab holder values, if any.
        Object response = null;
        try {
        	response = target.invoke(serviceInstance, methodInputParams);
        } catch (Exception e) {
        	response = e;
            if (log.isDebugEnabled()) {
                log.debug("Exception invoking a method of " + 
                        serviceImplClass.toString() + " of instance " +
                        serviceInstance.toString());
                        
                log.debug("Method = " + target.toGenericString());
              
                for (int i=0; i<methodInputParams.length; i++) {
                    String value = (methodInputParams[i] == null) ? "null" :
                        methodInputParams[i].getClass().toString();
                    log.debug(" Argument[" + i +"] is " + value);
                }
            }
        }
        
        Message message = null;
        // If the operation is one-way, then we can just return null because
        // we cannot create a MessageContext for one-way responses.
        if(operationDesc.isOneWay()){
        	return null;
        }
        else if (response instanceof Throwable) {
        	message = methodMarshaller.marshalFaultResponse((Throwable)response); 
        }
        else if(target.getReturnType().getName().equals("void")){
        	message = methodMarshaller.marshalResponse(null, methodInputParams);
        }
        else{
        	message = methodMarshaller.marshalResponse(response, methodInputParams);
        }
        
        MessageContext responseMsgCtx = MessageContextUtils.createMessageMessageContext(mc);
        responseMsgCtx.setMessage(message);
        
        //Enable MTOM if necessary
        EndpointInterfaceDescription epInterfaceDesc = operationDesc.getEndpointInterfaceDescription();
        EndpointDescription epDesc = epInterfaceDesc.getEndpointDescription();
        
        String bindingType = epDesc.getBindingType();
        if (bindingType != null) {
            if (bindingType.equals(SOAPBinding.SOAP11HTTP_MTOM_BINDING) ||
                bindingType.equals(SOAPBinding.SOAP12HTTP_MTOM_BINDING)) {
                message.setMTOMEnabled(true);
            }
        }
         
        return responseMsgCtx;
    }
    
    private void initialize(MessageContext mc){
    	mc.setOperationName(mc.getAxisMessageContext().getAxisOperation().getName());
    	serviceDesc = getServiceDescription(mc);
        endpointDesc = getEndpointDescription(mc);
        operationDesc = getOperationDescription(mc);
        mc.setOperationDescription(operationDesc);
        methodMarshaller = null;
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
        String methodName = mc.getOperationName().getLocalPart();
        for (OperationDescription op:ops) {
        	Method method = op.getSEIMethod();
        	if (method.getName().equals(methodName)) {
        		if (log.isDebugEnabled()) {
                    log.debug("wsdl operation: " + op.getName());
                    log.debug("   java method: " + op.getJavaMethodName());
                }
        		return op;
        	}
        			
        }
        OperationDescription op = ops[0];
        
        if (log.isDebugEnabled()) {
            log.debug("wsdl operation: " + op.getName());
            log.debug("   java method: " + op.getJavaMethodName());
        }
        
        return op;        
    }
    
    private ServiceDescription getServiceDescription(MessageContext mc){
    	return mc.getServiceDescription();
    }
    
    private EndpointDescription getEndpointDescription(MessageContext mc){
    	ServiceDescription sd = mc.getServiceDescription();
    	EndpointDescription[] eds = sd.getEndpointDescriptions();
        EndpointDescription ed = eds[0];
        return ed;
    }
    
    private MethodMarshaller createMethodMarshaller(Protocol protocol){
    	javax.jws.soap.SOAPBinding.Style styleOnSEI = endpointDesc.getEndpointInterfaceDescription().getSoapBindingStyle();
		javax.jws.soap.SOAPBinding.Style styleOnMethod = operationDesc.getSoapBindingStyle();
		if(styleOnMethod!=null && styleOnSEI!=styleOnMethod){
			throw ExceptionFactory.makeWebServiceException(Messages.getMessage("proxyErr2"));
		}
		if(styleOnSEI == javax.jws.soap.SOAPBinding.Style.RPC){
			throw new UnsupportedOperationException("RPC/LIT not supported.");
		}
		
		MethodMarshallerFactory cf = (MethodMarshallerFactory) FactoryRegistry.getFactory(MethodMarshallerFactory.class);
		
		if(styleOnSEI == javax.jws.soap.SOAPBinding.Style.DOCUMENT){
			return createDocLitMessageConvertor(cf, protocol);
		}
		if(styleOnSEI == javax.jws.soap.SOAPBinding.Style.RPC){
			return createRPCLitMessageConvertor(cf, protocol);
			
		}
		return null;
    }
    
    private MethodMarshaller createDocLitMessageConvertor(MethodMarshallerFactory cf, Protocol protocol){
		ParameterStyle parameterStyle = null;
		if(isDocLitBare(endpointDesc, operationDesc)){
			parameterStyle = javax.jws.soap.SOAPBinding.ParameterStyle.BARE;
		}
		if(isDocLitWrapped(endpointDesc, operationDesc)){
			parameterStyle = javax.jws.soap.SOAPBinding.ParameterStyle.WRAPPED;
		}
        return cf.createMethodMarshaller(Style.DOCUMENT, parameterStyle, 
                serviceDesc, endpointDesc, operationDesc, protocol, false);
	}
	
	private MethodMarshaller createRPCLitMessageConvertor(MethodMarshallerFactory cf, Protocol protocol){
        return cf.createMethodMarshaller(Style.RPC, ParameterStyle.WRAPPED, 
                serviceDesc, endpointDesc, operationDesc, protocol, false);
	}
	
    
    public Method getJavaMethod(MessageContext mc, Class serviceImplClass) {
		 QName opName = mc.getOperationName();
		 
	        if (opName == null)
	            // TODO: NLS
	            throw ExceptionFactory.makeWebServiceException("Operation name was not set");
	        
	        String localPart = opName.getLocalPart();
	        Method[] methods = serviceImplClass.getMethods();
	        for (int i = 0; i < methods.length; ++i) {
	        	String webMethodName = operationDesc.getOperationName();
	            if (localPart.equals(methods[i].getName())){
	                return methods[i];
	            }
	            if(webMethodName.equals(methods[i].getName())){
	            	return methods[i];
	            }
	            
	        }
	        
	        if (log.isDebugEnabled()) {
	            log.debug("No Java method found for the operation");
	        }
	        // TODO: NLS
	        throw ExceptionFactory.makeWebServiceException(Messages.getMessage("JavaBeanDispatcherErr1"));
	}
    
    protected boolean isDocLitBare(EndpointDescription endpointDesc, OperationDescription operationDesc){
		javax.jws.soap.SOAPBinding.ParameterStyle methodParamStyle = operationDesc.getSoapBindingParameterStyle();
		if(methodParamStyle!=null){
			return methodParamStyle == javax.jws.soap.SOAPBinding.ParameterStyle.BARE;
		}
		else{
			javax.jws.soap.SOAPBinding.ParameterStyle SEIParamStyle = endpointDesc.getEndpointInterfaceDescription().getSoapBindingParameterStyle();
			return SEIParamStyle == javax.jws.soap.SOAPBinding.ParameterStyle.BARE;
		}
	}
	
	protected boolean isDocLitWrapped(EndpointDescription endpointDesc, OperationDescription operationDesc){
		javax.jws.soap.SOAPBinding.ParameterStyle methodParamStyle = operationDesc.getSoapBindingParameterStyle();
		if(methodParamStyle!=null){
			return methodParamStyle == javax.jws.soap.SOAPBinding.ParameterStyle.WRAPPED;
		}
		else{
		javax.jws.soap.SOAPBinding.ParameterStyle SEIParamStyle = endpointDesc.getEndpointInterfaceDescription().getSoapBindingParameterStyle();
		return SEIParamStyle == javax.jws.soap.SOAPBinding.ParameterStyle.WRAPPED;
		}
	}
    
}
