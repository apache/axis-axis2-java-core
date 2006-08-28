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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.stream.XMLStreamException;
import javax.xml.ws.AsyncHandler;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.message.Block;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.MessageException;
import org.apache.axis2.jaxws.message.Protocol;
import org.apache.axis2.jaxws.message.factory.JAXBBlockFactory;
import org.apache.axis2.jaxws.message.factory.MessageFactory;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.axis2.jaxws.spi.ServiceDelegate;
import org.apache.axis2.jaxws.util.WSDLWrapper;
import org.apache.axis2.jaxws.wrapper.JAXBWrapperTool;
import org.apache.axis2.jaxws.wrapper.impl.JAXBWrapperException;
import org.apache.axis2.jaxws.wrapper.impl.JAXBWrapperToolImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;



public class DocLitProxyHandler extends BaseProxyHandler {
	private static Log log = LogFactory.getLog(DocLitProxyHandler.class);
	private static int SIZE = 1;
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
			return createDocLitWrappedRequest(method, args);
		}
		if(isDocLitBare()){
			return createDocLitNONWrappedRequest(method, args);
		}
		return requestCtx;
	}

	@Override
	protected Object createResponse(Method method, MessageContext responseContext) throws IllegalAccessException, ClassNotFoundException, JAXBWrapperException, JAXBException, javax.xml.stream.XMLStreamException, MessageException, IntrospectionException, NoSuchFieldException, InvocationTargetException{
		Object result = null;
		if(isDocLitWrapped()){
			return createDocLitWrappedResponse(method, responseContext);
		}
		if(isDocLitBare()){
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
	 * CreateDocLitNONWrappedRequest creates a request message context. The input object to a non wrapped wsdl will be a object (mainly a JAXB Object) that will
	 * have all the payload data or method parameter data already setup. So the message context will be created by converting input object in to a JAXBBlock and
	 * attaching the Block Message to soap body.
	 * @param method
	 * @param objects
	 * @return
	 */
	private MessageContext createDocLitNONWrappedRequest(Method method, Object[] objects) throws JAXBException, MessageException, XMLStreamException{
		JAXBContext ctx = null;
		Object requestObject = null;
		ArrayList<String> names = proxyDescriptor.getParamNames();
		Map<String, Object> values = getParamValues(names, objects);
		if(names.size()> SIZE || values.size() > SIZE){
			throw ExceptionFactory.makeWebServiceException("As per WS-I compliance, Multi part WSDL not allowed for Doc/Lit NON Wrapped request, Method invoked has multiple input parameter");
		}
		for(String name:names){
			requestObject = values.get(name);
			if(requestObject == null){
				throw ExceptionFactory.makeWebServiceException("Method Input parameter for NON Wrapped Request cannot be null");
			}
		}
			
		ctx = JAXBContext.newInstance(new Class[]{requestObject.getClass()});
		Block reqBlock = createJAXBBlock(requestObject, ctx);
		MessageContext requestCtx = initializeRequest(reqBlock);
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

	private Object createDocLitWrappedResponse(Method method, MessageContext response)throws IllegalAccessException, ClassNotFoundException, JAXBWrapperException, JAXBException, javax.xml.stream.XMLStreamException, MessageException, IntrospectionException, NoSuchFieldException, InvocationTargetException{
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
	private Object createDocLitNONWrappedResponse(Method method, MessageContext response) throws IllegalAccessException, JAXBException, MessageException, XMLStreamException, InvocationTargetException, IntrospectionException, NoSuchFieldException{
		
		Message responseMsg = response.getMessage();
		Class returnType = proxyDescriptor.getReturnType(isAsync());
		JAXBContext ctx = JAXBContext.newInstance(returnType);
		JAXBBlockFactory factory = (JAXBBlockFactory)FactoryRegistry.getFactory(JAXBBlockFactory.class);
		Block resBlock = responseMsg.getBodyBlock(0, ctx, factory);
		Object bo = resBlock.getBusinessObject(true);
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
	
	private Block createJAXBBlock(OMElement om, JAXBContext context)throws javax.xml.stream.XMLStreamException{
		JAXBBlockFactory factory = (JAXBBlockFactory)FactoryRegistry.getFactory(JAXBBlockFactory.class);
		return factory.createFrom(om,context,null);
		
	}

	/*
	 * this method return property descriptor to read or write the property value.
	 * It reads all the property descriptor on the java bean. Then determins if java field name is the the actual property who's pd has been requested.
	 * if true it returns pd.
	 * Also the propertyName supplied is compared with java field name and if they dont match than xmlElement annotation on JAXB Object is lookedup. if they
	 * dont match then xmlElement is compared with java field name using lowercases and finally return the pd.
	 * if property descriptor not found for a perticular property just return null.
	 */
	private PropertyDescriptor getPropertyDescriptor(Class returnClazz, String propertyName)throws IntrospectionException, NoSuchFieldException{
		PropertyDescriptor[] allPds = Introspector.getBeanInfo(returnClazz).getPropertyDescriptors();
		Field[] fields = returnClazz.getDeclaredFields();
		for(PropertyDescriptor pd:allPds){
			for(Field field:fields){
				if(propertyName == null){
					//if this happens I will get the first property that has datatype same as return clazz and return its descriptor.
					if(field.getType() == returnClazz){
						return pd;
					}
				}
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
		
		// TODO Shouldn't this be a WebServiceException ?
		throw ExceptionFactory.makeWebServiceException(Messages.getMessage("noWebResultForProperty", propertyName, returnClazz.getName()));
	}
	//TODO: refactor this once PropertyDescriptor is implemented.
	private Map<String, Object> getParamValues(ArrayList<String> names, Object[] objects){
		Map<String, Object> values = new Hashtable<String, Object>();
		int i=0;
		for(Object obj:objects){
			//skip AsycHandler Object
			if(obj instanceof AsyncHandler){
				i++;
				continue;
			}
			
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
		PropertyDescriptor pd = getPropertyDescriptor(wrapperClazz, propertyName);
		if(pd == null){
			//TODO: what happens if pd not found.
		}
		Method readMethod = pd.getReadMethod();
		Object webResult = readMethod.invoke(wrapperClazz.cast(businessObject), null);
		return webResult;
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
	
}
