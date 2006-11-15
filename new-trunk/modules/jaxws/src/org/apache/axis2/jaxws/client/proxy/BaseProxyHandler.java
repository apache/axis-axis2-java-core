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
package org.apache.axis2.jaxws.client.proxy;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Future;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;
import javax.xml.ws.WebServiceException;

import org.apache.axis2.AxisFault;
import org.apache.axis2.jaxws.AxisController;
import org.apache.axis2.jaxws.BindingProvider;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.core.InvocationContext;
import org.apache.axis2.jaxws.core.InvocationContextFactory;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.core.controller.AxisInvocationController;
import org.apache.axis2.jaxws.core.controller.InvocationController;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.impl.AsyncListener;
import org.apache.axis2.jaxws.message.MessageException;
import org.apache.axis2.jaxws.spi.ServiceDelegate;
import org.apache.axis2.jaxws.util.WSDLWrapper;
import org.apache.axis2.jaxws.wrapper.impl.JAXBWrapperException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// import com.sun.xml.bind.v2.runtime.reflect.Lister;

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

public abstract class BaseProxyHandler extends BindingProvider implements
		InvocationHandler {
	private static Log log = LogFactory.getLog(BaseProxyHandler.class);
//	TODO remove axisController once InvocationController code is build.
	private AxisController axisController = null;
	//Reference to ServiceDelegate instance that was used to create the Proxy
	private ServiceDelegate delegate = null;
	protected ProxyDescriptor proxyDescriptor = null;
	
	public BaseProxyHandler(ProxyDescriptor pd, ServiceDelegate delegate) {
		super();
		this.proxyDescriptor = pd;
		this.delegate = delegate;
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
		if(!isValidMethodCall(method)){
			throw ExceptionFactory.makeWebServiceException(Messages.getMessage("proxyErr1",method.getName(), axisController.getClientContext().getClazz().getName()));
		}
		
		if(isBindingProviderInvoked(method)){
			if (log.isDebugEnabled()) {
	            log.debug("Invoking method on Binding Provider");
	        }
			return method.invoke(this, args);
			
		}
		else{
			proxyDescriptor.setSeiMethod(method);
			return InvokeSEIMethod(method, args);
		}
	}
	
	/**
	 * InvokeSEIMethod invokes Axis engine using methods on InvocationController. Create request Invocation context, instantiates AxisInvocationController and 
	 * runs invoke.
	 * 
	 */
	private Object InvokeSEIMethod(Method method, Object[] args)throws ClassNotFoundException, JAXBWrapperException, JAXBException, MessageException, XMLStreamException, IllegalAccessException,IntrospectionException, NoSuchFieldException, InvocationTargetException{
		if (log.isDebugEnabled()) {
            log.debug("Attempting to Invoke SEI Method "+ method.getName());
        }
		
		//TODO make sure the method is a public method and it is declared in SEI.
		
		InvocationContext requestIC = InvocationContextFactory.createInvocationContext(null);
		MessageContext requestContext = createRequest(method, args);
        requestContext.setOperationDescription(proxyDescriptor.getOperationDescription());
		requestIC.setRequestMessageContext(requestContext);
		InvocationController controller = new AxisInvocationController();
		//FIXME: Fix based on how InvocationContext changes to get ServiceClient.
		requestIC.setServiceClient(proxyDescriptor.getEndpointDescription().getServiceClient());
		
		//check if the call is OneWay, Async or Sync
		if(proxyDescriptor.isOneWay() || method.getReturnType().getName().equals("void")){
			if(log.isDebugEnabled()){
				log.debug("OneWay Call");
			}
			controller.invokeOneWay(requestIC);
		}
		
		if(method.getReturnType().isAssignableFrom(Future.class)){
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
			AsyncListener listener = createProxyListener();
			requestIC.setAsyncListener(listener);
			requestIC.setExecutor(delegate.getExecutor());
			return controller.invokeAsync(requestIC, asyncHandler);
		}
		
		if(method.getReturnType().isAssignableFrom(Response.class)){
			if(log.isDebugEnabled()){
				log.debug("Async Polling");
			}
			AsyncListener listener = createProxyListener();
			requestIC.setAsyncListener(listener);
			requestIC.setExecutor(delegate.getExecutor());
			return controller.invokeAsync(requestIC);
		}
		
		if(!proxyDescriptor.isOneWay()){
			InvocationContext responseIC = controller.invoke(requestIC);
		
			MessageContext responseContext = responseIC.getResponseMessageContext();
			Object responseObj = createResponse(method, responseContext);
			return responseObj;
		}
		return null;
	}
	
	private AsyncListener createProxyListener(){
		ProxyAsyncListener listener = new ProxyAsyncListener();
		listener.setHandler(this);
		return listener;
	}
	
	protected boolean isAsync(){
		String methodName = proxyDescriptor.getSeiMethod().getName();
		Class returnType = proxyDescriptor.getSeiMethod().getReturnType();
		return methodName.endsWith("Async") && (returnType.isAssignableFrom(Response.class) || returnType.isAssignableFrom(Future.class));
	}
	/**
	 * Create request context for the method call. This request context will be used by InvocationController to route the method call to axis engine.
	 * @param method
	 * @param args
	 * @return
	 */
	protected abstract MessageContext createRequest(Method method, Object[] args) throws ClassNotFoundException, JAXBWrapperException, JAXBException, MessageException, javax.xml.stream.XMLStreamException;
	
	/**
	 * Creates response context for the method call. This response context will be used to create response result to the client call.
	 * @param method
	 * @param responseContext
	 * @return
	 */
	protected abstract Object createResponse(Method method, MessageContext responseContext)throws IllegalAccessException, ClassNotFoundException, JAXBWrapperException, JAXBException, javax.xml.stream.XMLStreamException, MessageException, IntrospectionException, NoSuchFieldException, InvocationTargetException;
	
	private boolean isBindingProviderInvoked(Method method){
		Class SEIClass = proxyDescriptor.getSeiClazz();
		Class methodsClass = method.getDeclaringClass();
		return (SEIClass == methodsClass)?false:true;
	}
	
	private boolean isValidMethodCall(Method method){
		Class SEIClass = proxyDescriptor.getSeiClazz();
		Class clazz = method.getDeclaringClass();
		if(clazz == javax.xml.ws.BindingProvider.class || clazz == SEIClass){
			return true;
		}
		return false;
	}
	//TODO: remove reference to AxisController.
	protected void setAxisController(AxisController ac) {
		this.axisController = ac;
	}
	
	public void setDelegate(ServiceDelegate delegate) {
		this.delegate = delegate;
	}
	
	protected void initRequestContext() {
		String soapAddress = null;
		String soapAction = null;
		String endPointAddress = proxyDescriptor.getPort().getEndpointAddress();
		WSDLWrapper wsdl = delegate.getServiceDescription().getWSDLWrapper();
		QName serviceName = delegate.getServiceName();
		QName portName = proxyDescriptor.getPort().getPortName();
		if (wsdl != null) {
			soapAddress = wsdl.getSOAPAddress(serviceName, portName);
			soapAction = wsdl.getSOAPAction(serviceName, portName);
		}
		super.initRequestContext(endPointAddress, soapAddress, soapAction);
	}

	protected ServiceDelegate getDelegate() {
		return delegate;
	}

}
