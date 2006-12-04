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
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBIntrospector;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.ws.AsyncHandler;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.message.Block;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.MessageException;
import org.apache.axis2.jaxws.message.Protocol;
import org.apache.axis2.jaxws.message.factory.JAXBBlockFactory;
import org.apache.axis2.jaxws.message.factory.MessageFactory;
import org.apache.axis2.jaxws.message.factory.XMLStringBlockFactory;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.axis2.jaxws.spi.ServiceDelegate;
import org.apache.axis2.jaxws.wrapper.JAXBWrapperTool;
import org.apache.axis2.jaxws.wrapper.impl.JAXBWrapperException;
import org.apache.axis2.jaxws.wrapper.impl.JAXBWrapperToolImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class DocLitProxyHandler extends BaseProxyHandler {
	private static Log log = LogFactory.getLog(DocLitProxyHandler.class);
	private static int SIZE = 1;
	private static String DEFAULT_ARG="arg";
	private static QName SOAPENV_QNAME= new QName("http://schemas.xmlsoap.org/soap/envelop/", "Envelope");
	private ArrayList<Object> argList = null;
	/**
	 * @param pd
	 * @param delegate
	 */
	public DocLitProxyHandler(ProxyDescriptor pd, ServiceDelegate delegate) {
		super(pd, delegate);
	}

	@Override
	protected MessageContext createRequest(Method method, Object[] args) throws ClassNotFoundException, JAXBWrapperException, JAXBException, MessageException, javax.xml.stream.XMLStreamException {
		MessageContext requestCtx = null;
		if(isDocLitWrapped()){
			if (log.isDebugEnabled()) {
	            log.debug("Creating Doc Lit Wrapped Request for method : " +method.getName());
	        }
			return createDocLitWrappedRequest(method, args);
		}
		if(isDocLitBare()){
			if (log.isDebugEnabled()) {
	            log.debug("Creating Doc Lit Bare Request for method : " +method.getName());
	        }
			return createDocLitNONWrappedRequest(method, args);
		}
		return requestCtx;
	}

	@Override
	protected Object createResponse(Method method, MessageContext responseContext) throws IllegalAccessException, ClassNotFoundException, JAXBWrapperException, JAXBException, javax.xml.stream.XMLStreamException, MessageException, IntrospectionException, NoSuchFieldException, InvocationTargetException{
		Object result = null;
		if(isDocLitWrapped()){
			if (log.isDebugEnabled()) {
	            log.debug("Creating Doc Lit Wrapped Response ");
	        }
			return createDocLitWrappedResponse(method, responseContext);
		}
		if(isDocLitBare()){
			if (log.isDebugEnabled()) {
	            log.debug("Creating Doc Lit Bare Request ");
	        }
			return createDocLitNONWrappedResponse(method, responseContext);
		}
		return result;
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
	private MessageContext createDocLitWrappedRequest(Method method, Object[] objects)throws ClassNotFoundException, JAXBWrapperException, JAXBException, MessageException, javax.xml.stream.XMLStreamException{
		
		Class wrapperClazz = proxyDescriptor.getRequestWrapperClass(isAsync());
		ArrayList<String> names = getParamNames(objects);
		String localName = proxyDescriptor.getRequestWrapperLocalName();
		Map<String, Object> values = getParamValues(objects, names);
		JAXBWrapperTool wrapTool = new JAXBWrapperToolImpl();
		if (log.isDebugEnabled()) {
            log.debug("JAXBWrapperTool attempting to wrap propertes in WrapperClass :" + wrapperClazz);
        }
		//TODO:if(@XmlRootElement) annotation found or defined
		Object jaxbObject = wrapTool.wrap(wrapperClazz, localName,names, values);
		if (log.isDebugEnabled()) {
            log.debug("JAXBWrapperTool wrapped following propertes :");
        }
		//TODO: if (!@XmlRootElement) annotation not found or not defined then can I use JAXBElement?
		//JAXBElement jaxbObject = wrapTool.wrapAsJAXBElement(wrapperClazz, requestWrapper.localName(),names, values);
		JAXBContext ctx = JAXBContext.newInstance(new Class[]{wrapperClazz});
		if (log.isDebugEnabled()) {
            log.debug("Attempting to create Block");
        }
		XmlRootElement root = null;
		Block reqBlock = null;
		root =(XmlRootElement) wrapperClazz.getAnnotation(XmlRootElement.class);
		if(root != null){
		 reqBlock = createJAXBBlock(jaxbObject, ctx);
		 if (log.isDebugEnabled()) {
	            log.debug("JAXBBlock Created");
	        }
		}
		else{
			reqBlock = createJAXBBlock(localName, jaxbObject, ctx);
			if (log.isDebugEnabled()) {
	            log.debug("JAXBBlock Created");
	        }
		}
		
		MessageContext requestCtx = initializeRequest(reqBlock);
		return requestCtx;
		
	}
	
	
	/**
	 * CreateDocLitNONWrappedRequest creates a request message context. The input object to a non wrapped wsdl will be a object (mainly a JAXB Object) that will
	 * have all the payload data or method parameter data already setup. So the message context will be created by converting input object in to a JAXBBlock and
	 * attaching the Block Message to soap body.
	 * @param method
	 * @param objects
	 * @return
	 */
	private MessageContext createDocLitNONWrappedRequest(Method method, Object[] objects) throws JAXBException, MessageException, XMLStreamException{
		MessageContext requestCtx = null;
		
		ArrayList<String> names = getParamNames(objects);
		ArrayList<String> tns = proxyDescriptor.getParamtns();
		Map<String, Object> values = getParamValues(objects, names);
		if(names.size()> SIZE || values.size() > SIZE){
			if (log.isDebugEnabled()) {
	            log.debug("As per WS-I compliance, Multi part WSDL not allowed for Doc/Lit NON Wrapped request, Method invoked has multiple input parameter");
	        }
			throw ExceptionFactory.makeWebServiceException(Messages.getMessage("DocLitProxyHandlerErr1"));
		}
		if(names.size() !=0){
			JAXBContext ctx = null;
			Object requestObject = null;
			String requestObjectName = null;
			for(String name:names){
				requestObject = values.get(name);
				requestObjectName = name;
				if(requestObject == null){
					if (log.isDebugEnabled()) {
			            log.debug("Method Input parameter for NON Wrapped Request cannot be null");
			        }
					throw ExceptionFactory.makeWebServiceException(Messages.getMessage("DocLitProxyHandlerErr2"));
				}
			}
				
			ctx = JAXBContext.newInstance(new Class[]{requestObject.getClass()});
			if (log.isDebugEnabled()) {
	            log.debug("Attempting to create Block");
	        }
			Block reqBlock = createJAXBBlock(requestObjectName, requestObject, ctx);
			if (log.isDebugEnabled()) {
	            log.debug("Block Created");
	        }
			requestCtx = initializeRequest(reqBlock);
		}
		if(names.size() == 0){
			Block emptyBodyBlock = createEmptyBodyBlock();
			requestCtx = initializeRequest(emptyBodyBlock);
		}
		return requestCtx;
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

	private Object createDocLitWrappedResponse(Method method, MessageContext response)throws ClassNotFoundException, JAXBWrapperException, JAXBException, javax.xml.stream.XMLStreamException, MessageException{
		Class wrapperClazz = proxyDescriptor.getResponseWrapperClass(isAsync());
		String resultName = proxyDescriptor.getWebResultName(isAsync());
		JAXBContext ctx = JAXBContext.newInstance(new Class[]{wrapperClazz});
		
		// Get a JAXBBlockFactory instance.  We'll need this to get the JAXBBlock
        // out of the Message
        JAXBBlockFactory factory = (JAXBBlockFactory)FactoryRegistry.getFactory(JAXBBlockFactory.class);
        
        Message responseMsg = response.getMessage();
        Block resBlock = responseMsg.getBodyBlock(0, ctx, factory);
		Object bo = resBlock.getBusinessObject(true);
		
		//if ReturnType/WrapperClass is assignable from bo then return bo;
		if(resultName==null && wrapperClazz.isAssignableFrom(bo.getClass())){
			return bo;
		}
		
		//if ReturnType is not of same type as JAXBBlock business Object then I will look for resultName in Business Object and return that.
		return getWebResultObject(wrapperClazz, bo, resultName);
			
	}
	
	/**
	 * CreateDocLitNONWrappedResponse creates return result that client expects from the method call. This method reads the method return type
	 * or uses webResult annotation and creates JAXBBlock from the response context and returns the business object associated with the JAXBBlock.
	 * @param method
	 * @param response
	 * @return
	 */
	private Object createDocLitNONWrappedResponse(Method method, MessageContext response) throws JAXBWrapperException, JAXBException, MessageException, XMLStreamException{
		
		Message responseMsg = response.getMessage();
		Class returnType = proxyDescriptor.getReturnType(isAsync());
		JAXBContext ctx = JAXBContext.newInstance(returnType);
		JAXBBlockFactory factory = (JAXBBlockFactory)FactoryRegistry.getFactory(JAXBBlockFactory.class);
		Block resBlock = responseMsg.getBodyBlock(0, ctx, factory);
		Object bo = null;
		try{
			 bo = resBlock.getBusinessObject(true);
		}catch(Exception e){
			//FIXME: this is the bare case where child of body is not a method but a primitive data type. Reader from Block is throwing exception.
			OMElement om = resBlock.getOMElement();
			
			XMLInputFactory xmlFactory = XMLInputFactory.newInstance();
			
			Unmarshaller u = ctx.createUnmarshaller();
			Reader inputReader = new InputStreamReader(new ByteArrayInputStream(om.toString().getBytes()));
			XMLStreamReader sr = xmlFactory.createXMLStreamReader(inputReader);
			JAXBElement o =u.unmarshal(sr, returnType);
			bo = o.getValue();
		}
		if(returnType.isAssignableFrom(bo.getClass())){
			return bo;
		}
		//If returnType is different than JAXBBlock Business Object, I will look for resultName in BusinessObject and return that.
		String resultName = proxyDescriptor.getWebResultName(isAsync());
		return getWebResultObject(returnType, bo, resultName);
	}
	
	private Block createJAXBBlock(Object jaxbObject, JAXBContext context) throws MessageException{
		JAXBBlockFactory factory = (JAXBBlockFactory)FactoryRegistry.getFactory(JAXBBlockFactory.class);
		return factory.createFrom(jaxbObject,context,null);
		
	}
	
	private Block createJAXBBlock(String name, Object jaxbObject, JAXBContext context) throws MessageException{
		JAXBBlockFactory factory = (JAXBBlockFactory)FactoryRegistry.getFactory(JAXBBlockFactory.class);
		JAXBIntrospector introspector = context.createJAXBIntrospector();
		if(introspector.isElement(jaxbObject)){
			return factory.createFrom(jaxbObject, context, null);
		}
		else{
			Class clazz = jaxbObject.getClass();
			String tns = proxyDescriptor.getParamtns(name);
			JAXBElement<Object> element = new JAXBElement<Object>(new QName(tns, name), clazz, jaxbObject);
			return factory.createFrom(element,context,null);
		}
		
	}
	
	private Block createEmptyBodyBlock() throws MessageException{
		String emptyBody = "";
		XMLStringBlockFactory stringFactory = (XMLStringBlockFactory) FactoryRegistry.getFactory(XMLStringBlockFactory.class);
		return stringFactory.createFrom(emptyBody, null, SOAPENV_QNAME);
	}
	
	private Block createJAXBBlock(OMElement om, JAXBContext context)throws javax.xml.stream.XMLStreamException{
		JAXBBlockFactory factory = (JAXBBlockFactory)FactoryRegistry.getFactory(JAXBBlockFactory.class);
		return factory.createFrom(om,context,null);
		
	}

	
	
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
	private Object getWebResultObject(Class wrapperClazz, Object businessObject, String propertyName) throws JAXBWrapperException{
		
		JAXBWrapperTool wrapTool = new JAXBWrapperToolImpl();
		if (log.isDebugEnabled()) {
            log.debug("Attempting to unwrap object from WrapperClazz: "+wrapperClazz);
        }
		Object[] webResult = wrapTool.unWrap(businessObject,new ArrayList<String>(Arrays.asList(new String[]{propertyName})));
		if (log.isDebugEnabled()) {
            log.debug("Object unwrapped");
        }
		return webResult[0];
		
	}
	
	private MessageContext initializeRequest(Block messageBlock) throws XMLStreamException, MessageException{
		MessageContext request = new MessageContext();
		MessageFactory mf = (MessageFactory)FactoryRegistry.getFactory(MessageFactory.class);
		//FIXME: The protocol should actually come from the binding information included in
        // either the WSDL or an annotation.
		Message m = mf.create(Protocol.soap11);
		m.setBodyBlock(0,messageBlock);
		request.setMessage(m);
		request.getProperties().putAll(getRequestContext());
	
		return request;
		
	}
	
	private boolean isDocLitBare(){
		/* TODO: if(EndPoinInterfaceDescriptor.clientCall == Doc/literal) && OperationDescriptor.isWrapped() == false){ 
		 * return true; 
		 * else
		 * return false;
		 */
		return proxyDescriptor.isClazzDocLitBare();
	}
	
	private boolean isDocLitWrapped(){
		/* TODO: if(EndPoinInterfaceDescriptor.clientCall == Doc/literal) && OperationDescriptor.isWrapped() == true){ 
		 * return true; 
		 * else
		 * return false;
		 */
		return proxyDescriptor.isClazzDocLitWrapped();
	}
	
	public ArrayList<String> getParamNames(Object[] objects){ 
        ArrayList<String> names = proxyDescriptor.getParamNames();
        //TODO Should this logic be moved to Operation Description.
        argList = null;
		ArrayList<Object> paramValues = createArgList(names, objects);
		if(names.size() == paramValues.size()){
			return names;
		}
		if(names.size() > 0 && names.size() != paramValues.size()){
			throw ExceptionFactory.makeWebServiceException(Messages.getMessage("InvalidWebParams"));
		}
		//if no webparams found but there method has input parameter I will create default input param names. Java reflection does not allow reading
		//formal parameter names, hence I will create defautl argument names to be arg0, arg1 ....
		int i=0;
		if(names.size() ==0){
			for(Object paramValue:paramValues){
				names.add(DEFAULT_ARG + i++);
			}
		}
		return names;
	}
	//TODO: Should we move this to OperationDescription.
	public Map<String, Object> getParamValues(Object[] objects, ArrayList<String> names){
		Map<String, Object> values = new HashMap<String, Object>();
		
		if(objects == null){
			return values;
		}
		//if object array not null check if there is only AsyncHandler object param, if yes then its Async call 
		//with no parameter. Lets filter AsyncHandler and check for return objects, if they are 0 return value;
		ArrayList<Object> paramValues = createArgList(names, objects);
		
		//@webparams and paramValues identified in method should match. 
		if(names.size() > 0 && names.size() != paramValues.size()){
			throw ExceptionFactory.makeWebServiceException(Messages.getMessage("InvalidWebParams"));
		}
		
		if(paramValues.size() == 0){
			//No method parameters
			return values;
		}
		//If no webParam annotation defined read default names of the object and let's use those.
		boolean readDefault = false;
		if(names.size() ==0){
			readDefault = true;
		}
		int i =0;
		for(Object paramValue: paramValues){
			if(readDefault){
				//Java Reflection does not allow you to read names of forma parameter, so I will default the method argument names to arg0, arg1 ....
				values.put(DEFAULT_ARG + i++, paramValue);
			}else{
				values.put(names.get(i++), paramValue);
			}
		}
		return values;
	}
	//TODO Implement createNameValuePair method so we can remove getParamName and getParamObject and only call this method once. 
	private ArrayList<Object> createArgList(ArrayList<String> names, Object[] objects){
		if(argList !=null){
			return argList;
		}
		argList = new ArrayList<Object>();
		
		if(objects == null){
			return argList;
		}
		int i =0;
		for(Object obj:objects){
			//skip AsycHandler Object
			if(obj instanceof AsyncHandler){
				if(isAsync() && proxyDescriptor.isClazzDocLitBare()){
					//doeble check and remove the name of AsyncHandler from names list, work around for how wsImport generates doc/lit bare art-effects.
					names.remove(i);
				}
				i++;
				continue;
			}
			
			argList.add(obj);
			i++;
		}
		return argList;
	}
	
}
