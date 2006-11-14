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
package org.apache.axis2.jaxws.client.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.Future;

import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.ParameterStyle;
import javax.xml.namespace.QName;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Binding;
import javax.xml.ws.Response;

import org.apache.axis2.jaxws.BindingProvider;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.client.async.AsyncResponse;
import org.apache.axis2.jaxws.core.InvocationContext;
import org.apache.axis2.jaxws.core.InvocationContextFactory;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.core.controller.AxisInvocationController;
import org.apache.axis2.jaxws.core.controller.InvocationController;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.OperationDescription;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.description.ServiceDescriptionWSDL;
import org.apache.axis2.jaxws.handler.PortData;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.marshaller.MethodMarshaller;
import org.apache.axis2.jaxws.marshaller.factory.MethodMarshallerFactory;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.Protocol;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.axis2.jaxws.spi.ServiceDelegate;
import org.apache.axis2.jaxws.util.WSDLWrapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * ProxyHandler is the java.lang.reflect.InvocationHandler implementation.
 * When jaxws client calls the method on proxy object that it gets using the getPort
 * ServiceDelegate api, the Inovke method on ProxyHandler is Invoked.
 * ProxyHandler uses EndpointInterfaceDescriptor and finds out if 
 * 1) The client call is Document Literal or Rpc Literal
 * 2) The WSDL is wrapped or unWrapped. 
 * 
 * ProxyHandler then reads OperationDescription using Method name called by Client
 * From OperationDescription it does the following 
 * 1) if the wsdl isWrapped() reads RequestWrapper Class and responseWrapperClass
 * 2) then reads the webParams for the Operation.
 * 
 * isWrapped() = true  and DocLiteral then
 * ProxyHandler then uses WrapperTool to create Request that is a Wrapped JAXBObject.
 * Creates JAXBBlock using JAXBBlockFactory
 * Creates MessageContext->Message and sets JAXBBlock to xmlPart as RequestMsgCtx in InvocationContext.
 * Makes call to InvocationController.
 * Reads ResponseMsgCtx ->MessageCtx->Message->XMLPart.
 * Converts that to JAXBlock using JAXBBlockFactory and returns the BO from this JAXBBlock.
 * 
 * isWrapped() != true and DocLiteral then
 * ProxyHandler creates the JAXBBlock for the input request creates a 
 * MessageContext that is then used by IbvocationController to invoke.
 * Response is read and return object is derived using @Webresult annotation.
 * A JAXBBlock is created from the Response and the BO from JAXBBlock is
 * returned.  
 * 
 * RPCLiteral 
 * TBD
 * 
 */

public class JAXWSProxyHandler extends BindingProvider implements
		InvocationHandler {
	private static Log log = LogFactory.getLog(JAXWSProxyHandler.class);

	//Reference to ServiceDelegate instance that was used to create the Proxy
	private ServiceDelegate delegate = null;
	protected ServiceDescription serviceDesc = null;
	protected EndpointDescription endpointDesc = null;
	protected OperationDescription operationDesc = null;
	protected MethodMarshaller methodMarshaller = null;
	private PortData port = null;
	private Class seiClazz = null;
	private Method method = null;
	
	public JAXWSProxyHandler(ServiceDelegate delegate, Class seiClazz, PortData port) {
		super();
		this.delegate = delegate;
		this.seiClazz = seiClazz;
		this.port = port;
		this.serviceDesc=delegate.getServiceDescription();
//		FIXME: This probably needs to be more robust; can there be > 1 endpoints; if so, how choose which one?
		this.endpointDesc = serviceDesc.getEndpointDescription(seiClazz)[0];
		initRequestContext();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
	 * 
	 * Invoke method checks to see if BindingProvider method was invoked by client if yes, it uses reflection and invokes the BindingProvider method.
	 * If SEI method was called then it delegates to InvokeSEIMethod().
	 */
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		if (log.isDebugEnabled()) {
            log.debug("Attemping to invoke Method: " +method.getName());
        }
        
		this.method = method;
		
		if(!isValidMethodCall(method)){
			throw ExceptionFactory.makeWebServiceException(Messages.getMessage("proxyErr1",method.getName(), seiClazz.getName()));
		}
		
		if(!isPublic(method)){
			throw ExceptionFactory.makeWebServiceException("Invalid Method Call, Method "+method.getName() + " not a public method"); 
		}
		
		if(isBindingProviderInvoked(method)){
			if (log.isDebugEnabled()) {
	            log.debug("Invoking method on Binding Provider");
	        }
			try{
				return method.invoke(this, args);
			}catch(Throwable e){
				throw ExceptionFactory.makeMessageException(e);
			}
			
		}
		else{
			operationDesc = endpointDesc.getEndpointInterfaceDescription().getOperation(method);
			if(isMethodExcluded()){
				throw ExceptionFactory.makeWebServiceException("Invalid Method Call, Method "+method.getName() + " has been excluded using @webMethod annotation");
			}
			return InvokeSEIMethod(method, args);
		}
	}
	
	/**
	 * InvokeSEIMethod invokes Axis engine using methods on InvocationController. Create request Invocation context, instantiates AxisInvocationController and 
	 * runs invoke.
	 * 
	 */
	private Object InvokeSEIMethod(Method method, Object[] args)throws Throwable{
		if (log.isDebugEnabled()) {
            log.debug("Attempting to Invoke SEI Method "+ method.getName());
        }
		
		initialize();
		InvocationContext requestIC = InvocationContextFactory.createInvocationContext(null);
		MessageContext requestContext = createRequest(method, args);
		//Enable MTOM on the Message if the property was
        //set on the SOAPBinding.
        Binding bnd = getBinding();
        if (bnd != null && bnd instanceof SOAPBinding) {
            javax.xml.ws.soap.SOAPBinding soapBnd = (javax.xml.ws.soap.SOAPBinding) bnd;
            if (soapBnd.isMTOMEnabled()) {
                Message requestMsg = requestContext.getMessage();
                requestMsg.setMTOMEnabled(true);
            }
        }
        requestContext.setOperationDescription(operationDesc);
		requestIC.setRequestMessageContext(requestContext);
		InvocationController controller = new AxisInvocationController();
		requestIC.setServiceClient(delegate.getServiceClient(port.getPortName()));
		
		//check if the call is OneWay, Async or Sync
		//if(operationDesc.isOneWay() || method.getReturnType().getName().equals("void")){
		if(operationDesc.isOneWay()){
			if(log.isDebugEnabled()){
				log.debug("OneWay Call");
			}
			controller.invokeOneWay(requestIC);
			
            //Check to see if we need to maintain session state
            if (requestContext.isMaintainSession()) {
                //TODO: Need to figure out a cleaner way to make this call. 
                setupSessionContext(requestIC.getServiceClient().getServiceContext().getProperties());
            }
		}
		
		//if(method.getReturnType().isAssignableFrom(Future.class))
		if(method.getReturnType() == Future.class){
			if(log.isDebugEnabled()){
				log.debug("Async Callback");
			}
			//Get AsyncHandler from Objects and sent that to InvokeAsync
			AsyncHandler asyncHandler = null;
			for(Object obj:args){
				if(obj !=null && AsyncHandler.class.isAssignableFrom(obj.getClass())){
					asyncHandler = (AsyncHandler)obj;
					break;
				}
			}
			if(asyncHandler == null){
				throw ExceptionFactory.makeWebServiceException("AynchHandler null for Async callback, Invalid AsyncHandler callback Object");
			}
			AsyncResponse listener = createProxyListener(args);
			requestIC.setAsyncResponseListener(listener);
			requestIC.setExecutor(delegate.getExecutor());
				        
	        Future<?> future = controller.invokeAsync(requestIC, asyncHandler);
	        
            //Check to see if we need to maintain session state
            if (requestContext.isMaintainSession()) {
                //TODO: Need to figure out a cleaner way to make this call. 
                setupSessionContext(requestIC.getServiceClient().getServiceContext().getProperties());
            }
	        
	        return future;
		}
		
		//if(method.getReturnType().isAssignableFrom(Response.class))
		if(method.getReturnType() == Response.class){
			if(log.isDebugEnabled()){
				log.debug("Async Polling");
			}
			AsyncResponse listener = createProxyListener(args);
			requestIC.setAsyncResponseListener(listener);
			requestIC.setExecutor(delegate.getExecutor());
	        
			Response response = controller.invokeAsync(requestIC);
			
            //Check to see if we need to maintain session state
            if (requestContext.isMaintainSession()) {
                //TODO: Need to figure out a cleaner way to make this call. 
                setupSessionContext(requestIC.getServiceClient().getServiceContext().getProperties());
            }
	        
	        return response;
		}
		
		if(!operationDesc.isOneWay()){
			InvocationContext responseIC = controller.invoke(requestIC);
		
            //Check to see if we need to maintain session state
            if (requestContext.isMaintainSession()) {
                //TODO: Need to figure out a cleaner way to make this call. 
                setupSessionContext(requestIC.getServiceClient().getServiceContext().getProperties());
            }
	        
			MessageContext responseContext = responseIC.getResponseMessageContext();
			Object responseObj = createResponse(method, args, responseContext);
			return responseObj;
		}
		return null;
	}
	
	private AsyncResponse createProxyListener(Object[] args){
		ProxyAsyncListener listener = new ProxyAsyncListener();
		listener.setHandler(this);
		listener.setInputArgs(args);
		return listener;
	}
	
	protected boolean isAsync(){
		String methodName = method.getName();
		Class returnType = method.getReturnType();
		return methodName.endsWith("Async") && (returnType.isAssignableFrom(Response.class) || returnType.isAssignableFrom(Future.class));
	}
	/**
	 * Create request context for the method call. This request context will be used by InvocationController to route the method call to axis engine.
	 * @param method
	 * @param args
	 * @return
	 */
	protected MessageContext createRequest(Method method, Object[] args) throws Throwable{
		if (log.isDebugEnabled()) {
            log.debug("Converting objects to Message");
        }
		Message message = methodMarshaller.marshalRequest(args);
		
		if (log.isDebugEnabled()) {
            log.debug("Objects converted to Message");
        }
		MessageContext request = new MessageContext();
		request.setMessage(message);
		request.getProperties().putAll(getRequestContext());
		if (log.isDebugEnabled()) {
            log.debug("Request Created");
        }
		return request;	
	}
	
	/**
	 * Creates response context for the method call. This response context will be used to create response result to the client call.
	 * @param method
	 * @param responseContext
	 * @return
	 */
	protected Object createResponse(Method method, Object[] args, MessageContext responseContext)throws Throwable{
		Message responseMsg = responseContext.getMessage();
		if (log.isDebugEnabled()) {
            log.debug("Converting Message to Response Object");
        }
		if (responseMsg.isFault()) {
		    Object object = methodMarshaller.demarshalFaultResponse(responseMsg);
		    if (log.isDebugEnabled()) {
		        log.debug("Message Converted to response Throwable.  Throwing back to client.");
		    }
		    
		    throw (Throwable)object;
		} else if (responseContext.getLocalException() != null) {
		    // use the factory, it'll throw the right thing:
		    throw ExceptionFactory.makeWebServiceException(responseContext.getLocalException());
		}
		Object object = methodMarshaller.demarshalResponse(responseMsg, args);
		if (log.isDebugEnabled()) {
            log.debug("Message Converted to response Object");
        }
		return object;
	}
	
	private boolean isBindingProviderInvoked(Method method){
		Class methodsClass = method.getDeclaringClass();
		return (seiClazz == methodsClass)?false:true;
	}
	
	private boolean isValidMethodCall(Method method){
		Class clazz = method.getDeclaringClass();
		if(clazz.isAssignableFrom(javax.xml.ws.BindingProvider.class) || clazz.isAssignableFrom(seiClazz)){
			return true;
		}
		return false;
	}
	
	public void setDelegate(ServiceDelegate delegate) {
		this.delegate = delegate;
	}
	
	protected void initRequestContext() {
		String soapAddress = null;
		String soapAction = null;
		String endPointAddress = port.getEndpointAddress();
		WSDLWrapper wsdl = ((ServiceDescriptionWSDL) delegate.getServiceDescription()).getWSDLWrapper();
		QName serviceName = delegate.getServiceName();
		QName portName = port.getPortName();
		if (wsdl != null) {
			soapAddress = wsdl.getSOAPAddress(serviceName, portName);
			soapAction = wsdl.getSOAPAction(serviceName, portName);
		}
		super.initRequestContext(endPointAddress, soapAddress, soapAction);
	}

	protected ServiceDelegate getDelegate() {
		return delegate;
	}
	
	private boolean isPublic(Method method){
		return Modifier.isPublic(method.getModifiers());
	}
	
	private boolean isMethodExcluded(){
		return operationDesc.isExcluded();
	}

	public PortData getPort() {
		return port;
	}

	public void setPort(PortData port) {
		this.port = port;
	}

	public Class getSeiClazz() {
		return seiClazz;
	}

	public void setSeiClazz(Class seiClazz) {
		this.seiClazz = seiClazz;
	}
	private void initialize(){
		SOAPBinding.Style style = operationDesc.getSoapBindingStyle();
		
		MethodMarshallerFactory cf = (MethodMarshallerFactory) FactoryRegistry.getFactory(MethodMarshallerFactory.class);
		if(style == SOAPBinding.Style.DOCUMENT){
			methodMarshaller = createDocLitMethodMarshaller(cf);
		}
		if(style == SOAPBinding.Style.RPC){
			methodMarshaller = createRPCLitMethodMarshaller(cf);
			
		}
	
	}
	private MethodMarshaller createDocLitMethodMarshaller(MethodMarshallerFactory cf){
		ParameterStyle parameterStyle = null;
		if(isDocLitBare()){
			parameterStyle = SOAPBinding.ParameterStyle.BARE;
		}
		if(isDocLitWrapped()){
			parameterStyle = SOAPBinding.ParameterStyle.WRAPPED;
		}
		//FIXME: The protocol should actually come from the binding information included in
	    // either the WSDL or an annotation.
		return cf.createMethodMarshaller(SOAPBinding.Style.DOCUMENT, parameterStyle, 
                serviceDesc, endpointDesc, operationDesc, Protocol.soap11);
	}
	
	private MethodMarshaller createRPCLitMethodMarshaller(MethodMarshallerFactory cf){
		return cf.createMethodMarshaller(SOAPBinding.Style.DOCUMENT, SOAPBinding.ParameterStyle.WRAPPED,
                serviceDesc, endpointDesc, operationDesc, Protocol.soap11);
	}
	protected boolean isDocLitBare(){
		SOAPBinding.ParameterStyle methodParamStyle = operationDesc.getSoapBindingParameterStyle();
		if(methodParamStyle!=null){
			return methodParamStyle == SOAPBinding.ParameterStyle.BARE;
		}
		else{
			SOAPBinding.ParameterStyle SEIParamStyle = endpointDesc.getEndpointInterfaceDescription().getSoapBindingParameterStyle();
			return SEIParamStyle == SOAPBinding.ParameterStyle.BARE;
		}
	}
	
	protected boolean isDocLitWrapped(){
		SOAPBinding.ParameterStyle methodParamStyle = operationDesc.getSoapBindingParameterStyle();
		if(methodParamStyle!=null){
			return methodParamStyle == SOAPBinding.ParameterStyle.WRAPPED;
		}
		else{
		SOAPBinding.ParameterStyle SEIParamStyle = endpointDesc.getEndpointInterfaceDescription().getSoapBindingParameterStyle();
		return SEIParamStyle == SOAPBinding.ParameterStyle.WRAPPED;
		}
	}
}
