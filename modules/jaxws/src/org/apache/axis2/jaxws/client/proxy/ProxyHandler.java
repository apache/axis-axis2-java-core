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
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.ws.WebServiceException;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.jaxws.AxisController;
import org.apache.axis2.jaxws.BindingProvider;
import org.apache.axis2.jaxws.core.InvocationContext;
import org.apache.axis2.jaxws.core.InvocationContextFactory;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.core.controller.AxisInvocationController;
import org.apache.axis2.jaxws.core.controller.InvocationController;
import org.apache.axis2.jaxws.message.Block;
import org.apache.axis2.jaxws.message.MessageException;
import org.apache.axis2.jaxws.message.factory.JAXBBlockFactory;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.axis2.jaxws.spi.ServiceDelegate;
import org.apache.axis2.jaxws.util.WSDLWrapper;
import org.apache.axis2.jaxws.wrapper.JAXBWrapperTool;
import org.apache.axis2.jaxws.wrapper.impl.JAXBWrapperException;
import org.apache.axis2.jaxws.wrapper.impl.JAXBWrapperToolImpl;


/**
 * ProxyHandler is the java.lang.reflect.InvocationHandler implementation.
 * When jaxws client calls the method on proxy object that it gets using the getPort
 * ServiceDelegate api, the Invoke method on ProxyHandler is invoked.
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
 * isWrapped() != true and DocLiteral;
 * TBD
 * 
 * RPCLiteral 
 * TBD
 * 
 */

public class ProxyHandler extends BindingProvider implements InvocationHandler {

	//TODO remove axisController once InvocationController code is build.
	private AxisController axisController = null;
	//Reference to ServiceDelegate instance that was used to create the Proxy
	private ServiceDelegate delegate = null;
	private ProxyDescriptor proxyDescriptor = null;
	public ProxyHandler(AxisController ac, ServiceDelegate delegate) {
		super();
		this.axisController = ac;
		this.delegate = delegate;
		setRequestContext();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
	 * 
	 * Invoke method checks to see if BindingProvider method was invoked by client if yes, it uses reflection and invokes the BindingProvider method.
	 * If SEI method was called then it delegates to InvokeSEIMethod().
	 */
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		if(!isValidMethodCall(method)){
			throw new WebServiceException("Invalid Method-"+method.getName()+ " Method not found in javax.xml.ws.BindingProvider or "+axisController.getClientContext().getClazz() );
		}
		
		if(isBindingProviderInvoked(method)){
			return method.invoke(this, args);
		}
		else{
			return InvokeSEIMethod(method, args);
		}
		
	}
	
	/**
	 * InvokeSEIMethod reads EndpointInterfaceDescription and finds out if the request is document literal or RPC Literal and check to see if the WSDL is 
	 * wrapped or unWrapped. It then reads OperationDescription using Method and in case of doc/lit wrapped Request:
	 * 1) creates request message context for doc/lit wrapped Request.
	 * 2) creates response message context by calling InvocationController with request message context as input.
	 * 3) create wrapped response and returns it to the client method call.
	 * 
	 * In case of doc/lit unWrapped Request:
	 * TBD
	 * 
	 * In case of RPC/Lit wrapped Request:
	 * TBD
	 * 
	 * In case of RPC/Lit unWrapped Request:
	 * TBD
	 * 
	 * @param method - Method called by Client
	 * @param args - Argument object to the method call
	 * @return - returns the returnType of the method call.
	 * @throws ClassNotFoundException
	 * @throws JAXBWrapperException
	 * @throws JAXBException
	 * @throws MessageException
	 * @throws XMLStreamException
	 * @throws IllegalAccessException
	 * @throws IntrospectionException
	 * @throws NoSuchFieldException
	 * @throws InvocationTargetException
	 * 
	 * 
	 */
	private Object InvokeSEIMethod(Method method, Object[] args)throws ClassNotFoundException, JAXBWrapperException, JAXBException, MessageException, XMLStreamException, IllegalAccessException,IntrospectionException, NoSuchFieldException, InvocationTargetException{
		
		/*TODO:ProxyHandler uses EndpointInterfaceDescriptor and finds out if 
		 * 1) The client call is Document Literal or Rpc Literal
		 * 2) The WSDL is wrapped or unWrapped. 
		 * 
		 * ProxyHandler then reads OperationDescription using Method name called by Client
		 * From OperationDescription it does the following 
		 * 1) if the wsdl isWrapped() reads RequestWrapper Class and responseWrapperClass
		 * 2) then reads the webParams for the Operation.
		 */
		
		proxyDescriptor = new ProxyDescriptor(method);
		if(isDocLitWrapped()){
			MessageContext requestCtx = createDocLitWrappedRequest(method, args);
			MessageContext responseContext = execute(requestCtx);
			Object wrappedResponse = createDocLitWrappedResponse(method, responseContext);
			return wrappedResponse;
			
		}
		return null;
	}
	
	/**
	 * createDocLitWrappedRequest create request message context. It reads RequestWrapper annotation from OperationDescription and reads the calss name, then reads
	 * all the webParam annotation on the method and uses JAXBWrapTool to wrap the request as jaxbObject. Create JAXBblock from the jaxbObject and sets OMElement on 
	 * Request MessageContext, reads Biniding provider properties and set them on request message context and return request message context.
	 * @param method
	 * @param objects
	 * @return
	 * @throws ClassNotFoundException
	 * @throws JAXBWrapperException
	 * @throws JAXBException
	 * @throws MessageException
	 * @throws javax.xml.stream.XMLStreamException
	 */
	//TODO Refactor this once OperationDescription is implemented.
	private MessageContext createDocLitWrappedRequest(Method method, Object[] objects)throws ClassNotFoundException, JAXBWrapperException, JAXBException, MessageException, javax.xml.stream.XMLStreamException{
		/*TODO : getOperationDesc from method name
		 * and call 
		 * createDocLitWrapperRequest(od, values);
		 */
		Class wrapperClazz = proxyDescriptor.getRequestWrapperClass();
		ArrayList<String> names = proxyDescriptor.getParamNames();
		String localName = proxyDescriptor.getResponseWrapperLocalName();
		Map<String, Object> values = getParamValues(names, objects);
		JAXBWrapperTool wrapTool = new JAXBWrapperToolImpl();
		
		//TODO:if(@XmlRootElement) annotation found or defined
		Object jaxbObject = wrapTool.wrap(wrapperClazz, localName,names, values);
		//TODO: if (!@XmlRootElement) annotation not found or not defined then can I use JAXBElement?
		//JAXBElement jaxbObject = wrapTool.wrapAsJAXBElement(wrapperClazz, requestWrapper.localName(),names, values);
		JAXBContext ctx = JAXBContext.newInstance(new Class[]{wrapperClazz});
		Block reqBlock = createJAXBBlock(jaxbObject, ctx);
		MessageContext requestCtx = initializeRequest(reqBlock);
		return requestCtx;
		
	}
	
	
	/**
	 * invokes Axis engine using methods on InvocationController. Create request Invocation context, instantiates AxisInvocationController and runs invoke.
	 * @param request
	 * @return
	 */
	private MessageContext execute(MessageContext request){
		//TODO: How do I get binding information.
		
		InvocationContext requestIC = InvocationContextFactory.createInvocationContext(null);
		requestIC.setRequestMessageContext(request);
		InvocationController controller = new AxisInvocationController();
		//FIXME: Fix based on how InvocationContext changes to get ServiceClient.
		requestIC.setServiceClient(axisController.getServiceClient());
		
		//TODO: check if the call is OneWay, Async or Sync
		InvocationContext responseIC = controller.invoke(requestIC);
		return responseIC.getResponseMessageContext();
	}

	
	/**
	 * CreateDocLitWrappedResponse creates return result that client expects from the method call. It reads response wrapper annotation then reads OM from the
	 * response message context and creates JAXBBlock from the OMElement on messageContext. It then reads the webresult annotation to gather the return parameter
	 * name and creates the result object for it by reading the property object from JAXBBlock's business object using PropertyDescriptor. 
	 * @param method
	 * @param response
	 * @return
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws JAXBWrapperException
	 * @throws JAXBException
	 * @throws javax.xml.stream.XMLStreamException
	 * @throws MessageException
	 * @throws IntrospectionException
	 * @throws NoSuchFieldException
	 * @throws InvocationTargetException
	 */
//	TODO Refactor this once OperationDescription is implemented.
	private Object createDocLitWrappedResponse(Method method, MessageContext response)throws IllegalAccessException, ClassNotFoundException, JAXBWrapperException, JAXBException, javax.xml.stream.XMLStreamException, MessageException, IntrospectionException, NoSuchFieldException, InvocationTargetException{
		Class wrapperClazz = proxyDescriptor.getResponseWrapperClass();
		String resultName = proxyDescriptor.getWebResultName();
		JAXBContext ctx = JAXBContext.newInstance(new Class[]{wrapperClazz});
		//TODO: I should go away from using messageAsOM and see if I can fetch Block from messageContext!!
		OMElement om = response.getMessageAsOM();
		Block resBlock = createJAXBBlock(om, ctx);
		Object bo = resBlock.getBusinessObject(true);
		return getWebResultObject(wrapperClazz, bo, resultName);
	}
	
	private Block createJAXBBlock(Object jaxbObject, JAXBContext context) throws MessageException{
		JAXBBlockFactory factory = (JAXBBlockFactory)FactoryRegistry.getFactory(JAXBBlockFactory.class);
		return factory.createFrom(jaxbObject,context,null);
		
	}
	
	private Block createJAXBBlock(OMElement om, JAXBContext context)throws javax.xml.stream.XMLStreamException{
		JAXBBlockFactory factory = (JAXBBlockFactory)FactoryRegistry.getFactory(JAXBBlockFactory.class);
		return factory.createFrom(om,context,null);
		
	}

	//TODO: should I unwrap the bo or use property descriptor?
	private PropertyDescriptor gerPropertyDescriptor(Class returnClazz, String propertyName)throws IntrospectionException, NoSuchFieldException{
		PropertyDescriptor[] allPds = Introspector.getBeanInfo(returnClazz).getPropertyDescriptors();
		Field[] fields = returnClazz.getDeclaredFields();
		for(PropertyDescriptor pd:allPds){
			for(Field field:fields){
				String javaFieldName = field.getName();
				String pdName = pd.getDisplayName();
				if(javaFieldName.equals(pdName)){
					if(javaFieldName.equals(propertyName)){
						return pd;
						
					}else{
						XmlElement xmlElement =field.getAnnotation(XmlElement.class);
						if(xmlElement == null){
							//TODO:What happens if xmlElement not defined.
							
						}
						String xmlName =xmlElement.name();
						if(xmlName.equals(propertyName)){
							return pd;
						}
						if(xmlName.toLowerCase().equals(propertyName.toLowerCase())){
							return pd;
						}
					}
				}
			}
		}
		return null;
	}
	//TODO: refactor this once PropertyDescriptor is implemented.
	private Map<String, Object> getParamValues(ArrayList<String> names, Object[] objects){
		Map<String, Object> values = new Hashtable<String, Object>();
		int i=0;
		for(Object obj:objects){
			values.put(names.get(i++), obj);
		}
		return values;
	}
	//TODO remove this once OperationDescription is implemented
	
	/** 
	 * reads PropertyDescritpr and invokes  get method on result property and returns the object.
	 * @param wrapperClazz
	 * @param businessObject
	 * @param propertyName
	 * @return
	 * @throws NoSuchFieldException
	 * @throws IntrospectionException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 */
	private Object getWebResultObject(Class wrapperClazz, Object businessObject, String propertyName) throws NoSuchFieldException, IntrospectionException,InvocationTargetException, IllegalAccessException{
		PropertyDescriptor pd = gerPropertyDescriptor(wrapperClazz, propertyName);
		if(pd == null){
			//TODO: what happens if pd not found.
		}
		Method readMethod = pd.getReadMethod();
		Object webResult = readMethod.invoke(wrapperClazz.cast(businessObject), null);
		return webResult;
	}
	
	private MessageContext initializeRequest(Block messageBlock) throws XMLStreamException, MessageException{
		MessageContext request = new MessageContext();
		request.setMessageAsOM(messageBlock.getOMElement());
		request.getProperties().putAll(getRequestContext());
	
		return request;
		
	}

	private boolean isBindingProviderInvoked(Method method){
		Class SEIClass = axisController.getClientContext().getClazz();
		Class methodsClass = method.getDeclaringClass();
		return (SEIClass == methodsClass)?false:true;
	}
	
	private boolean isDocLitRaw(){
		/* TODO: if(EndPoinInterfaceDescriptor.clientCall == Doc/literal) && OperationDescriptor.isWrapped() == false){ 
		 * return true; 
		 * else
		 * return false;
		 */
		return false;
	}
	
	private boolean isDocLitWrapped(){
		/* TODO: if(EndPoinInterfaceDescriptor.clientCall == Doc/literal) && OperationDescriptor.isWrapped() == true){ 
		 * return true; 
		 * else
		 * return false;
		 */
		return true;
	}
	
	private boolean isRPCLitRaw(){
		/* TODO: if(EndPoinInterfaceDescriptor.clientCall == RPC/literal) && OperationDescriptor.isWrapped() == false){ 
		 * return true; 
		 * else
		 * return false;
		 */
		return false;
	}
	
	private boolean isRPCLitWrapped(){
		/* TODO: if(EndPoinInterfaceDescriptor.clientCall == RPC/literal) && OperationDescriptor.isWrapped() == true){ 
		 * return true; 
		 * else
		 * return false;
		 */
		return false;
	}
	private boolean isValidMethodCall(Method method){
		//TODO: remove reference to axisController
		Class SEIClass = axisController.getClientContext().getClazz();
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
	
	protected void setRequestContext() {
		String endPointAddress = axisController.getEndpointAddress();
		WSDLWrapper wsdl = delegate.getServiceDescription().getWSDLWrapper();
		QName serviceName = delegate.getServiceName();
		QName portName = axisController.getPortName();
		if (endPointAddress != null && !"".equals(endPointAddress)) {
			getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
					endPointAddress);
		} else if (wsdl != null) {
			String soapAddress = wsdl.getSOAPAddress(serviceName, portName);
			getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
					soapAddress);
		}

		if (wsdl != null) {
			String soapAction = wsdl.getSOAPAction(serviceName, portName);
			getRequestContext().put(BindingProvider.SOAPACTION_URI_PROPERTY,
					soapAction);
		}
	}
	
	

}
