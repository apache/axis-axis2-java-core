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

import org.apache.axiom.om.OMElement;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.core.controller.AxisInvocationController;
import org.apache.axis2.jaxws.message.Block;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.MessageException;
import org.apache.axis2.jaxws.message.factory.JAXBBlockFactory;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.axis2.jaxws.spi.ServiceDelegate;
import org.apache.axis2.jaxws.wrapper.JAXBWrapperTool;
import org.apache.axis2.jaxws.wrapper.impl.JAXBWrapperException;
import org.apache.axis2.jaxws.wrapper.impl.JAXBWrapperToolImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class DocLitProxyHandler extends BaseProxyHandler {
	private static Log log = LogFactory.getLog(DocLitProxyHandler.class);
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
			requestCtx = createDocLitWrappedRequest(method, args);
		}
		return requestCtx;
	}

	@Override
	protected Object createResponse(Method method, MessageContext responseContext) throws IllegalAccessException, ClassNotFoundException, JAXBWrapperException, JAXBException, javax.xml.stream.XMLStreamException, MessageException, IntrospectionException, NoSuchFieldException, InvocationTargetException{
		Object result = null;
		if(isDocLitWrapped()){
			 result = createDocLitWrappedResponse(method, responseContext);
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
		
        // Get a JAXBBlockFactory instance.  We'll need this to get the JAXBBlock
        // out of the Message
        JAXBBlockFactory factory = (JAXBBlockFactory)FactoryRegistry.getFactory(JAXBBlockFactory.class);
        
        Message responseMsg = response.getMessage();
        Block resBlock = responseMsg.getBodyBlock(0, ctx, factory);
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
		//request.setMessageAsOM(messageBlock.getOMElement());
		request.getProperties().putAll(getRequestContext());
	
		return request;
		
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
}
