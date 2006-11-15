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
package org.apache.axis2.jaxws.marshaller.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Future;

import javax.jws.WebParam.Mode;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBIntrospector;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Holder;
import javax.xml.ws.Response;
import javax.xml.ws.WebServiceException;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.FaultDescription;
import org.apache.axis2.jaxws.description.OperationDescription;
import org.apache.axis2.jaxws.description.ParameterDescription;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.marshaller.MethodMarshaller;
import org.apache.axis2.jaxws.marshaller.MethodParameter;
import org.apache.axis2.jaxws.message.Block;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.MessageException;
import org.apache.axis2.jaxws.message.Protocol;
import org.apache.axis2.jaxws.message.XMLFault;
import org.apache.axis2.jaxws.message.XMLFaultReason;
import org.apache.axis2.jaxws.message.databinding.JAXBBlockContext;
import org.apache.axis2.jaxws.message.databinding.JAXBUtils;
import org.apache.axis2.jaxws.message.factory.JAXBBlockFactory;
import org.apache.axis2.jaxws.message.factory.MessageFactory;
import org.apache.axis2.jaxws.message.factory.XMLStringBlockFactory;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.axis2.jaxws.util.ClassUtils;
import org.apache.axis2.jaxws.wrapper.JAXBWrapperTool;
import org.apache.axis2.jaxws.wrapper.impl.JAXBWrapperException;
import org.apache.axis2.jaxws.wrapper.impl.JAXBWrapperToolImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class MethodMarshallerImpl implements MethodMarshaller {
	private static QName SOAPENV_QNAME = new QName("http://schemas.xmlsoap.org/soap/envelope/", "Envelope");
	private static Log log = LogFactory.getLog(MethodMarshallerImpl.class);
	protected ServiceDescription serviceDesc = null;
	protected EndpointDescription endpointDesc = null;
	protected OperationDescription operationDesc = null;
	protected Protocol protocol = Protocol.soap11;
	
	public MethodMarshallerImpl(ServiceDescription serviceDesc, EndpointDescription endpointDesc, OperationDescription operationDesc, Protocol protocol){
		this.serviceDesc = serviceDesc;
		this.endpointDesc = endpointDesc;
		this.operationDesc = operationDesc;
		this.protocol = protocol;
	}

	
	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.marshaller.MethodMarshaller#demarshalResponse(org.apache.axis2.jaxws.message.Message, java.lang.Object[])
	 */
	public abstract Object demarshalResponse(Message message, Object[] inputArgs) throws WebServiceException; 

	
	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.marshaller.MethodMarshaller#demarshalRequest(org.apache.axis2.jaxws.message.Message)
	 */
	public abstract Object[] demarshalRequest(Message message) throws WebServiceException;

	
	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.marshaller.MethodMarshaller#marshalResponse(java.lang.Object, java.lang.Object[])
	 */
	public abstract Message marshalResponse(Object returnObject, Object[] holderObjects)throws WebServiceException; 
	
	
	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.marshaller.MethodMarshaller#marshalRequest(java.lang.Object[])
	 */
	public abstract Message marshalRequest(Object[] object)throws WebServiceException; 
	
	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.marshaller.MethodMarshaller#demarshalFaultResponse(org.apache.axis2.jaxws.message.Message)
	 */
	public Object demarshalFaultResponse(Message message) throws WebServiceException {
		
		Exception exception = null;
        
		try {
			// Get the fault from the message and get the detail blocks (probably one)
			XMLFault xmlfault = message.getXMLFault();
			Block[] blocks = xmlfault.getDetailBlocks();
            
			
			if ((operationDesc.getFaultDescriptions().length == 0) || (blocks == null))  {
				// This is a system exception if the method does not throw a checked exception or if 
				// there is nothing in the detail element.
                // Shouldn't this create 
                
                // TODO Shouldn't we create a SOAPFaultException
				exception = createGenericException(xmlfault.getReason()
						.getText());
			} else {
				// Create a JAXBContext object that can handle any of the 
				// checked exceptions defined on this operation
                HashSet<Package> contextPackages = new HashSet<Package>();
				for(int i=0; i<operationDesc.getFaultDescriptions().length; i++) {
					FaultDescription fd = operationDesc.getFaultDescriptions()[i];
					contextPackages.add(loadClass(fd.getFaultBean()).getPackage());
				}
				
				// TODO what if there are multiple blocks in the detail ?
				// We should create a generic fault with the appropriate detail
				Block block = blocks[0];
				
				// Now demarshal the block to get a business object (faultbean)
                // Capture the qname of the element, which will be used to find the JAX-WS Exception
				Object obj = createFaultBusinessObject(block);
                QName faultQName = null;
                if (obj instanceof JAXBElement) {
                    faultQName = ((JAXBElement)obj).getName();
                    obj = ((JAXBElement)obj).getValue();
                } else {
                    faultQName = ClassUtils.getXmlRootElementQName(obj);
                }
                
				// Find the JAX-WS exception using a qname match
				Class exceptionClass = null;
                Class faultBeanFormalClass = null;
				for(int i=0; i<operationDesc.getFaultDescriptions().length && exceptionClass == null; i++) {
					FaultDescription fd = operationDesc.getFaultDescriptions()[i];
                    QName tryQName = new QName(fd.getTargetNamespace(), fd.getName());
                                    
					if (faultQName == null || faultQName.equals(tryQName)) {
						exceptionClass = loadClass(fd.getExceptionClassName());
                        faultBeanFormalClass = loadClass(fd.getFaultBean());
					}
				}
				
				// Now create the JAX-WS Exception class 
				if (exceptionClass == null) {
					throw ExceptionFactory.makeWebServiceException(Messages.getMessage("MethodMarshallerErr1", obj.getClass().toString()));
				}
                return createCustomException(xmlfault.getReason().getText(), exceptionClass, obj, faultBeanFormalClass);
			}
		} catch (Exception e) {
			// Catch all nested exceptions and throw WebServiceException
			throw ExceptionFactory.makeWebServiceException(e);
		}

		return exception;
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.marshaller.MethodMarshaller#marshalFaultResponse(java.lang.Throwable)
	 */
	public Message marshalFaultResponse(Throwable throwable) throws WebServiceException {
		try {
			Throwable t = ClassUtils.getRootCause(throwable);

			XMLFault xmlfault = null;
			
			Message message = createEmptyMessage();
			
			// Get the FaultDescriptor matching this Exception.
			// If FaultDescriptor is found, this is a JAX-B Service Exception.
			// If not found, this is a System Exception
			FaultDescription fd = operationDesc.resolveFaultByExceptionName(t.getClass().getName());

			String text = null;
			if (fd != null) {
				// Service Exception.  Create an XMLFault with the fault bean
            	Method getFaultInfo = t.getClass().getMethod("getFaultInfo", null);
            	Object faultBean = getFaultInfo.invoke(t, null);
            	JAXBBlockContext context = createJAXBBlockContext(createContextPackageSet());
            	Block[] detailBlocks = new Block[1];
                
                // Make sure to createJAXBBlock with an object that is 
                // a JAXBElement or has the XMLRootElement annotation
                // The actual faultBean object's class is used (because
                // the actual object may be a derived type of the formal declaration)
            	if (!ClassUtils.isXmlRootElementDefined(faultBean.getClass())) {
                    QName faultQName = new QName(fd.getTargetNamespace(), fd.getName());
                    faultBean = new JAXBElement(faultQName, faultBean.getClass(), faultBean);
                }
            	detailBlocks[0] = createJAXBBlock(faultBean, context);
                text = t.getMessage();
                xmlfault = new XMLFault(null, new XMLFaultReason(text), detailBlocks);
            } else {
                // System Exception
            	xmlfault = new XMLFault(null,       // Use the default XMLFaultCode
                        new XMLFaultReason(text));  // Assumes text is the language supported by the current Locale
            }
			// Add the fault to the message
            message.setXMLFault(xmlfault);
            return message;
        } catch (Exception e) {
        	// Catch all nested exceptions and throw WebServiceException
			throw ExceptionFactory.makeWebServiceException(e);
        }
	}
		
	/*
	 * Creates method output parameter/return parameter. reads webResult annotation and then matches them with the response/result value of Invoked method
	 * and creates a name value pair.
	 * Also handles situation where ResponseWrapper is a holder.
	 */
	protected ArrayList<MethodParameter> createResponseWrapperParameter(Object webResultValue) {
		ArrayList<MethodParameter> mps = new ArrayList<MethodParameter>();
		if(webResultValue == null){
			return mps;
		}
		String webResultName = operationDesc.getResultName();
		String webResultTNS = operationDesc.getResultTargetNamespace();
		Class webResultClass = null;
		if(webResultValue !=null){
			webResultClass = webResultValue.getClass();
		}
		MethodParameter mp = new MethodParameter(webResultName, webResultTNS, webResultClass, webResultValue);
		mps.add(mp);
		return mps;
	}
	
    protected ArrayList<MethodParameter> createResponseWrapperParameter(Object webResultObject, Object[] holderObjects)
        throws IllegalAccessException, InstantiationException, ClassNotFoundException {
		ParameterDescription[] paramDescs = operationDesc.getParameterDescriptions();
		ArrayList<ParameterDescription> pds = new ArrayList<ParameterDescription>();
		pds = toArrayList(paramDescs);
        
		// Remove all non holder meta data. Holders cannot be of Mode.IN so we 
        // don't have to worry about removing params with Mode.IN.
		for (int index = 0; index < paramDescs.length; index++) {
			ParameterDescription paramDesc = paramDescs[index];
			if (!(paramDesc.isHolderType())) {
				pds.remove(paramDesc);
			}
		}
		
		ArrayList<Object> paramValues = new ArrayList<Object>();
        //ArrayList<Object> paramValues = toArrayList(holderObjects);
        int index =0;
        for(ParameterDescription pd :pds){
            Object value = holderObjects[index];
                if (value != null && isHolder(value) && 
                    pd.isHolderType()) {
                        Object holderValue = getHolderValue(pd.getMode(), value);
                        value = holderValue;
                    }
                    paramValues.add(value);
                    index++;
                }
                ArrayList<MethodParameter> mps = createParameters(pds.toArray(new ParameterDescription[0]), paramValues);
        
		if(webResultObject!=null){
			MethodParameter outputResult = new MethodParameter(operationDesc.getResultName(), operationDesc.getResultTargetNamespace(), webResultObject.getClass(), webResultObject);
			mps.add(outputResult);
		}
		return mps;
		
	}
	
	/*
	 * Request Parameter are those where webParam Mode is IN or INOUT
	 */
    protected ArrayList<MethodParameter> createRequestWrapperParameters(Object[] objects)throws IllegalAccessException, InstantiationException, ClassNotFoundException{
		ArrayList<MethodParameter> mps = new ArrayList<MethodParameter>();
		//Hand no input parameters
		if(objects == null){
			return mps;
		}
		if(objects!=null && objects.length==0){
			return mps;
		}
		
		ParameterDescription[] paramDescs = operationDesc.getParameterDescriptions();
		
		if (paramDescs.length != objects.length) {
			throw ExceptionFactory.makeWebServiceException(Messages
					.getMessage("InvalidWebParams"));
		}
		ArrayList<Object> paramValues = new ArrayList<Object>();
		int index =0;
		//Request Parameters are one that have IN or INOUT parameter mode.
		for(ParameterDescription pd :paramDescs){
			if(pd.getMode() == Mode.INOUT || pd.getMode() == Mode.IN){
				Object value = objects[index];
				//If paramType is holder then get the holder value, this is done as requestWrapper does not have holder but a actual type of Holder.
				if (value != null && isHolder(value)
						&& pd.isHolderType()) {
					Object holderValue = getHolderValue(pd.getMode(),
							value);
					value = holderValue;
				}
				paramValues.add(value);
			}
			index++;
		}
		if (log.isDebugEnabled()) {
			log.debug("Attempting to create Method Parameters");
		}
		mps = createParameters(paramDescs, paramValues);

		if (log.isDebugEnabled()) {
			log.debug("Method Parameters created");
		}
					
		return mps;
	}
	protected ArrayList<MethodParameter> createParameterForSEIMethod(Object jaxbObject) throws JAXBWrapperException, IllegalAccessException, InstantiationException, ClassNotFoundException{
		ArrayList<MethodParameter> mps = new ArrayList<MethodParameter>();
		if(jaxbObject == null){
			return mps;
		}
		ParameterDescription[] paramDescs = operationDesc.getParameterDescriptions();
		
        ArrayList<String> webParam = new ArrayList<String>();
       
        //Get names of all IN and INOUT parameters, those are the ones that have been sent out by client
        for(ParameterDescription pd : paramDescs){
        	Mode mode = pd.getMode();
        	if(mode == Mode.IN || mode == Mode.INOUT){
        		webParam.add(pd.getParameterName());
        	}
        }
    
        if (log.isDebugEnabled()) {
            log.debug("Attempting to unwrap object from WrapperClazz");
        }
        JAXBWrapperTool wrapperTool = new JAXBWrapperToolImpl();
        Object[] objects = wrapperTool.unWrap(jaxbObject, webParam);
        if (log.isDebugEnabled()) {
            log.debug("Object unwrapped");
        }
      
        //Now that Objects with Mode IN and INOUT are unwrapped, let me get all the OUT parameters assign them NULL values,
        //so we can call the method with right number of parameters. Also lets ensure that we create holders whereever the method
        //parameter is defined as holder type.
        ArrayList<Object> objectList = new ArrayList<Object>();
        int paramIndex = 0;
        int objectIndex = 0;
        for(ParameterDescription pd : paramDescs){
        	Object value = null;
        	Mode mode = pd.getMode();
        	
        	if(mode == Mode.IN || mode == Mode.INOUT){
        		value = objects[objectIndex];
        		if (value != null && !isHolder(value)
        				&& pd.isHolderType()) {
        			Holder<Object> holder = createHolder(pd.getParameterType(),
        					value);
        			value = holder;
        		}
        		objectIndex++;
        	}
        	
        	else if(mode == Mode.OUT){
        		if(value == null && pd.isHolderType()){
        			Holder<Object> holder = createHolder(pd.getParameterType(),
        					value);
        			value = holder;
        		}
        		
        	}
        	objectList.add(paramIndex, value);
        	paramIndex++;
        }
        
        return createParameters(paramDescs, objectList);
        
	}
	
	
	/*
	 * Extract Holder parameter from supplied parameters and add annotation data for these parameters.
	 */

	protected ArrayList<MethodParameter> extractHolderParameters(Object jaxbObject) throws JAXBWrapperException, IllegalAccessException, InstantiationException, ClassNotFoundException{
		ArrayList<MethodParameter> mps = new ArrayList<MethodParameter>();
		if(jaxbObject == null){
			return mps;
		}
		
        ArrayList<String> webParam = new ArrayList<String>();
        ParameterDescription[] paramDescs = operationDesc.getParameterDescriptions();
        ArrayList<ParameterDescription> paramDescList = new ArrayList<ParameterDescription>();
		// Remove all non holder meta data. Holders cannot be of Mode.IN so I dont have to worry about removing params with Mode.IN.
		for (int index = 0; index < paramDescs.length; index++) {
			ParameterDescription paramDesc = paramDescs[index];
			if (paramDesc.isHolderType()) {
				paramDescList.add(paramDesc);
				webParam.add(paramDesc.getParameterName());
			}
		}      
		
        if (log.isDebugEnabled()) {
            log.debug("Attempting to unwrap object from WrapperClazz");
        }
        JAXBWrapperTool wrapperTool = new JAXBWrapperToolImpl();
        Object[] objects = wrapperTool.unWrap(jaxbObject, webParam);
        
        if (log.isDebugEnabled()) {
            log.debug("Object unwrapped");
        }
        if (log.isDebugEnabled()) {
			log.debug("Attempting to create Holder Method Parameters");
		}
        ArrayList<Object> paramValues = new ArrayList<Object>();
        int index = 0;
        for(ParameterDescription pd:paramDescList){
        	Object value = objects[index];
        	if (value != null && !isHolder(value)
					&& pd.isHolderType()) {
				Holder<Object> holder = createHolder(pd.getParameterType(),
						value);
				value = holder;
        	}
        	else if(value == null && pd.isHolderType()){
        		Holder<Object> holder = createHolder(pd.getParameterType(),
						value);
				value = holder;
        	}
        	paramValues.add(value);
        	index++;
        }
		mps = createParameters(paramDescList.toArray(new ParameterDescription[0]), paramValues);

		if (log.isDebugEnabled()) {
			log.debug("Holder Method Parameters created");
		}
					
		return mps;
	}
	
	/*
	 * Extract Holder parameter from supplied parameters and add annotation data for these parameters.
	 */
	protected ArrayList<MethodParameter> extractHolderParameters(Object[] objects)throws IllegalAccessException, InstantiationException, ClassNotFoundException{
		ArrayList<MethodParameter> mps = new ArrayList<MethodParameter>();
		//Hand no input parameters
		if(objects == null){
			return mps;
		}
		if(objects!=null && objects.length==0){
			return mps;
		}
		
		ParameterDescription[] paramDescs = operationDesc.getParameterDescriptions();
		ArrayList<ParameterDescription> paramDescList = new ArrayList<ParameterDescription>();
		if (paramDescs.length != objects.length) {
			throw ExceptionFactory.makeWebServiceException(Messages
					.getMessage("InvalidWebParams"));
		}
		ArrayList<Object> paramValues = new ArrayList<Object>();
		int index =0;
		//Add only Holder parameters. 
		for(ParameterDescription pd : paramDescs){
			Object value = objects[index];
			if(pd.isHolderType()){
				if (value != null && isHolder(value)
						&& pd.isHolderType()) {
					Object holderValue = getHolderValue(pd.getMode(),
							value);
					value = holderValue;
				}
				paramValues.add(value);
				paramDescList.add(pd);
			}
			index++;
		}
		
			
		if (log.isDebugEnabled()) {
			log.debug("Attempting to create Holder Method Parameters");
		}
		mps = createParameters(paramDescList.toArray(new ParameterDescription[0]), paramValues);

		if (log.isDebugEnabled()) {
			log.debug("Holder Method Parameters created");
		}
					
		return mps;
	}
    
	protected ArrayList<MethodParameter> createParameters(
			ParameterDescription[] paramDescs, ArrayList<Object> paramValues){
		ArrayList<MethodParameter> mps = new ArrayList<MethodParameter>();
		int index = 0;
		for (Object paramValue : paramValues){
			ParameterDescription paramDesc = paramDescs[index];
			MethodParameter mp = null;
			if (!isParamAsyncHandler(paramDesc.getParameterName(), paramValue)){
				mp = new MethodParameter(paramDesc, paramValue);
				mps.add(mp);
			}
			index++;
		}
		return mps;
	}
	
	
	private ArrayList<MethodParameter> createMethodParameters(ParameterDescription[] paramDescs, ArrayList<Object> paramValues)
        throws InstantiationException, ClassNotFoundException, IllegalAccessException {
		ArrayList<MethodParameter> mps = new ArrayList<MethodParameter>();
		int index = 0;
		for (Object paramValue : paramValues) {
			ParameterDescription paramDesc = paramDescs[index];
			Class paramType = paramDesc.getParameterType();
			Mode paramMode = paramDesc.getMode();
			boolean isHolderType = paramDesc.isHolderType();
			MethodParameter mp = null;
			// If call is Async call then lets filter AsyncHandler object name
			// and value;
			if (!isParamAsyncHandler(paramDesc.getParameterName(), paramValue)) {
				if (paramType != null) {
				    if(paramValue == null && isHolderType){
                        Holder<Object> holder = createHolder(paramType, paramValue);
						mp = new MethodParameter(paramDesc, holder);
					}
					// Identify that param value is not Holders however if the
					// method parameter is holder type then create Holder, this
					// will mostly be called during server side call
					else if (paramValue != null && !isHolder(paramValue)
							&& isHolderType) {
						Holder<Object> holder = createHolder(paramType, paramValue);
						mp = new MethodParameter(paramDesc, holder);
					} 
				    // Identify Holders and get Holder Values, this if condition
				    // will mostly execute during client side call
					else if (paramValue != null && isHolder(paramValue) && isHolderType) {
					    Object holderValue = getHolderValue(paramMode, paramValue);
					    mp = new MethodParameter(paramDesc, holderValue);
					}
					else {
						mp = new MethodParameter(paramDesc, paramValue);
					}
				}
				if (paramType == null) {
					if (isHolder(paramValue)) {
						Object holderValue = getHolderValue(paramMode,
								paramValue);
						mp = new MethodParameter(paramDesc, holderValue);
					} else {
						mp = new MethodParameter(paramDesc, paramValues);
					}
				}
				mps.add(mp);
			}

			index++;
		}
		return mps;

	}
			
	protected ArrayList<Class> getInputTypes(){
		Method seiMethod = operationDesc.getSEIMethod();
		ArrayList<Class> paramTypes = new ArrayList<Class>();
		Type[] types = seiMethod.getGenericParameterTypes();
		for(Type type:types){
			if(ParameterizedType.class.isAssignableFrom(type.getClass())){
				ParameterizedType pType = (ParameterizedType) type;
				Class rawClazz = (Class)pType.getRawType();
				Class actualClazz = (Class)pType.getActualTypeArguments()[0];
				paramTypes.add(actualClazz);
			}
			else{
				Class formalClazz= (Class)type;
				paramTypes.add(formalClazz);
			}
		}
		return paramTypes;
	}
	
	protected boolean isAsync(){
		Method method = operationDesc.getSEIMethod();
		if(method == null){
			return false;
		}
		String methodName = method.getName();
		Class returnType = method.getReturnType();
		return methodName.endsWith("Async") && (returnType.isAssignableFrom(Response.class) || returnType.isAssignableFrom(Future.class));
	}
	
	
	
	protected <T> ArrayList<T> toArrayList(T[] objects){
		return (objects!=null)? new ArrayList<T>(Arrays.asList(objects)):new ArrayList<T>();
	}
	
	protected Block createJAXBBlock(Object jaxbObject, JAXBBlockContext context) throws MessageException{
		JAXBBlockFactory factory = (JAXBBlockFactory)FactoryRegistry.getFactory(JAXBBlockFactory.class);
		return factory.createFrom(jaxbObject,context,null);
		
	}
	
	protected Block createJAXBBlock(String name, 
			Object jaxbObject, 
			JAXBBlockContext context, 
			String targetNamespace) throws MessageException, JAXBException {
		
		JAXBIntrospector i  = JAXBUtils.getJAXBIntrospector(context.getJAXBContext());
		boolean isElement = i.isElement(jaxbObject);
		JAXBUtils.releaseJAXBIntrospector(context.getJAXBContext(), i);
		if(isElement){
			return createJAXBBlock(jaxbObject, context);
		}
		else{
			//Create JAXBElement then use that to create JAXBBlock.
			Class objectType = jaxbObject.getClass();
		
			JAXBElement<Object> element = null;
			if (name != null) {
				element = new JAXBElement<Object>(new QName(targetNamespace,
						name), objectType, jaxbObject);
			} else {
				String xmlName = readXMLTypeName(objectType);
				element = new JAXBElement<Object>(new QName(targetNamespace, xmlName), objectType, jaxbObject);
			}
			
			JAXBBlockFactory factory = (JAXBBlockFactory)FactoryRegistry.getFactory(JAXBBlockFactory.class);
			return factory.createFrom(element,context ,null);
		}
		
	}
	
	protected Block createJAXBBlock(OMElement om, JAXBBlockContext context)throws  XMLStreamException, MessageException {
		JAXBBlockFactory factory = (JAXBBlockFactory)FactoryRegistry.getFactory(JAXBBlockFactory.class);
		return factory.createFrom(om,context,null);
		
	}
	
	protected String readXMLTypeName(Class jaxbClazz){
		XmlType type = (XmlType)jaxbClazz.getAnnotation(XmlType.class);
		if(type !=null){
			return type.name();
		}
		return null;
	}
	
	
	private Class loadClass(String className)throws ClassNotFoundException{
		// TODO J2W AccessController Needed
		// Don't make this public, its a security exposure
		return Class.forName(className, true, Thread.currentThread().getContextClassLoader());
	}
	
	/**
	 * In this method I am trying get the return type of the method.
	 * if SEI method is Async pooling implmentation then return type is actual type in Generic Response, example Response<ClassName>.
	 * if SEI method is Async Callback implementation then return type is actual type of method parameter type AsyncHandler, example AsyncHandler<ClassName>
	 * I use java reflection to get the return type.
	 * @param isAsync
	 * @return
	 */
	protected Class getReturnType(){
		Method seiMethod = operationDesc.getSEIMethod();
		Class returnType = seiMethod.getReturnType();
		if(isAsync()){
			//pooling implementation
			if(Response.class.isAssignableFrom(returnType)){
				Type type = seiMethod.getGenericReturnType();
				ParameterizedType pType = (ParameterizedType) type;
				return (Class)pType.getActualTypeArguments()[0];	
			}
			//Callback Implementation
			else{
				Type[] type = seiMethod.getGenericParameterTypes();
				Class parameters[]= seiMethod.getParameterTypes();
				int i=0;
				for(Class param:parameters){
					if(AsyncHandler.class.isAssignableFrom(param)){
						ParameterizedType pType = (ParameterizedType)type[i];
						return (Class)pType.getActualTypeArguments()[0];
					}
					i++;
				}
			}
			
		}
		
		return returnType;	
	}
	
	private boolean isParamAsyncHandler(String name, Object value){
		if(value!=null && value instanceof AsyncHandler){
			if(log.isDebugEnabled()){
				log.debug("Parameter is AsycnHandler Object");
			}
			if(!isAsync()){
				if (log.isDebugEnabled()) {
		            log.debug("Method parameter type javax.xml.ws.AsyncHandler should only be used with Async Callback operations, method is Async if it returns a Future<?> and endswith letters 'Async'");
		        }
				throw ExceptionFactory.makeWebServiceException(Messages.getMessage("DocLitProxyHandlerErr3"));
			}
			return true;
		}
		return false;
		
	}
	
	protected boolean isHolder(Object value){
		return value!=null && Holder.class.isAssignableFrom(value.getClass());
	}
	
	protected boolean isHolder(Class type){
		return type!=null && Holder.class.isAssignableFrom(type);
	}

    protected Object getHolderValue(Mode mode, Object value){
		if(!Holder.class.isAssignableFrom(value.getClass())){
			if(log.isDebugEnabled()){
				log.debug("Object Not a Holder type");
			}
			ExceptionFactory.makeWebServiceException(Messages.getMessage("DocLitProxyHandlerErr5"));
		}
		if(mode !=null && mode.equals(Mode.IN)){
			if(log.isDebugEnabled()){
				log.debug("WebParam annotation's Mode cannot be IN for input parameter of type Holder");
			}
			throw ExceptionFactory.makeWebServiceException(Messages.getMessage("DocLitProxyHandlerErr4"));
		}
		Holder holder = (Holder)value;
		return holder.value;
		
	}
	
	protected <T> Holder<T> createHolder(Class paramType, T value) throws IllegalAccessException, InstantiationException, ClassNotFoundException{
		if(Holder.class.isAssignableFrom(paramType)){
    		Class holderClazz = loadClass(paramType.getName());
    		Holder<T> holder = (Holder<T>) holderClazz.newInstance();
    		holder.value = value;
    		return holder;
    	}
		return null;
	}
	
	protected Message createMessage(ArrayList<MethodParameter> mps) throws JAXBException, MessageException, XMLStreamException{
		Block block = null;
		Object object = null;
		String objectName = null;
		String objectTNS = null;
		Class objectType = null;
		boolean isHeader =false;
		
		MessageFactory mf = (MessageFactory)FactoryRegistry.getFactory(MessageFactory.class);
		Message m = mf.create(protocol);
		
		for(MethodParameter mp : mps){

			if (!mp.isWebResult()) {
				ParameterDescription pd = mp.getParameterDescription();
				object = mp.getValue();
				objectName = pd.getParameterName();
				objectType = pd.getParameterActualType();
				objectTNS = pd.getTargetNamespace();
				isHeader = pd.isHeader();
			} else {
				object = mp.getValue();
				objectName = mp.getWebResultName();
				objectType = mp.getWebResultType();
				objectTNS = mp.getWebResultTNS();
				isHeader = false;
			}
			if (!isHeader && object == null) {
				if (log.isDebugEnabled()) {
		            log.debug("Method Input parameter for NON Wrapped Request cannot be null");
		        }
				throw ExceptionFactory.makeWebServiceException(Messages.getMessage("DocLitProxyHandlerErr2"));
			}
			JAXBBlockContext ctx = createJAXBBlockContext(createContextPackageSet());
			if (log.isDebugEnabled()) {
	            log.debug("Attempting to create Block");
	        }
			if(ClassUtils.isXmlRootElementDefined(objectType)){
				block = createJAXBBlock(object, ctx);
			}
			else{
				
				block =  createJAXBBlock(objectName, object, ctx, objectTNS);
			}
			if (log.isDebugEnabled()) {
	            log.debug("JAXBBlock Created");
	        }	
			if(isHeader){
				m.setHeaderBlock(objectTNS, objectName, block);
				if (log.isDebugEnabled()) {
		            log.debug("Header Block Created");
		        }
			}
			else{
				m.setBodyBlock(0,block);
				if (log.isDebugEnabled()) {
		            log.debug("Body Block Created");
		        }
			}
		}
		return m;
	}
	
	protected Message createFaultMessage(OMElement element) throws XMLStreamException, MessageException {
		MessageFactory mf = (MessageFactory)FactoryRegistry.getFactory(MessageFactory.class);
		return mf.createFrom(element);
	}
	
	protected Message createEmptyMessage() throws JAXBException, MessageException, XMLStreamException {
		MessageFactory mf = (MessageFactory)FactoryRegistry.getFactory(MessageFactory.class);
		Message m = mf.create(protocol);
		return m;
	}
	
	protected Object createBOFromHeaderBlock(Set<Package> contextPackages, Message message, String targetNamespace, String localPart) throws JAXBException, MessageException, XMLStreamException{
		
		JAXBBlockContext blockContext = createJAXBBlockContext(contextPackages);
		
		// Get a JAXBBlockFactory instance.  We'll need this to get the JAXBBlock
        // out of the Message
        JAXBBlockFactory factory = (JAXBBlockFactory)FactoryRegistry.getFactory(JAXBBlockFactory.class); 
        Block block = message.getHeaderBlock(targetNamespace, localPart, blockContext, factory);
        return block.getBusinessObject(true);
	}
	
	protected Object createBOFromBodyBlock(Set<Package> contextPackages, Message message) throws JAXBException, MessageException, XMLStreamException{
		return createBusinessObject(contextPackages, message);
	}

	
	protected Object createBusinessObject(Set<Package> contextPackages, Message message) throws JAXBException, MessageException, XMLStreamException{
		JAXBBlockContext blockContext = createJAXBBlockContext(contextPackages);
		
		// Get a JAXBBlockFactory instance.  We'll need this to get the JAXBBlock
        // out of the Message
        JAXBBlockFactory factory = (JAXBBlockFactory)FactoryRegistry.getFactory(JAXBBlockFactory.class);
        
        Block block = message.getBodyBlock(0, blockContext, factory);
        return block.getBusinessObject(true);
	}
	
	private JAXBBlockContext createJAXBBlockContext(Set<Package> contextPackages) throws JAXBException, MessageException {
		JAXBBlockContext blockContext = new JAXBBlockContext(contextPackages);
		return blockContext;
	}

	/**
	 * @param contextPackages
	 * @param block
	 * @return
	 * @throws JAXBException
	 * @throws MessageException
	 * @throws XMLStreamException
	 */
	protected Object createFaultBusinessObject(Block block)
			throws JAXBException, MessageException, XMLStreamException {
		JAXBBlockContext blockContext = new JAXBBlockContext(createContextPackageSet());		
		// Get a JAXBBlockFactory instance. 
        JAXBBlockFactory factory = (JAXBBlockFactory)FactoryRegistry.getFactory(JAXBBlockFactory.class);
        
        Block jaxbBlock = factory.createFrom(block, blockContext);
        return jaxbBlock.getBusinessObject(true); 
	}
	
    protected void assignHolderValues(ArrayList<MethodParameter> mps, ArrayList<Object> inputArgHolders, Message message)
            throws JAXBException, MessageException, XMLStreamException{
		Object bo = null;
		int index = 0;
		for(MethodParameter mp:mps){
			ParameterDescription pd = mp.getParameterDescription();
			if (pd.isHeader() && pd.isHolderType()) {
				bo = createBOFromHeaderBlock(createContextPackageSet(),
						message, pd.getTargetNamespace(), pd
								.getParameterName());
			}
			else if(!pd.isHeader() && pd.isHolderType()){
				bo = createBOFromBodyBlock(createContextPackageSet(), message);
			}
			try{
				Holder inputArgHolder = (Holder)inputArgHolders.get(index);
				inputArgHolder.value = bo;
				index++;
			}catch(Exception e){
				ExceptionFactory.makeWebServiceException(e);
			}
		}
	}
    
    /**
     * Simple utility to create package set from a single class
     * @param cls
     * @return
     */
    protected Set<Package> createContextPackageSet() {
         return operationDesc.getEndpointInterfaceDescription().getEndpointDescription().getPackages();
    }
	
    protected void assignHolderValues(Object bo, Object[] inputArgs, boolean isBare)throws JAXBWrapperException, InstantiationException, ClassNotFoundException, IllegalAccessException{
		if(inputArgs == null){
			return;
		}
		//Remove everything except for Holders from input parameter provided by client application.
		ArrayList<Object> objList = new ArrayList<Object>();
		ParameterDescription[] pds = operationDesc.getParameterDescriptions();
		int index = 0;
		//Read Holders from Input parameteres.
		for(ParameterDescription pd : pds){
		    if(pd.isHolderType()) {
		        objList.add(inputArgs[index]);
            }
		    index++;
		}
		//If no holder params in method
		if(objList.size()<=0){
			return;
		}

		//Next get all the holder objects from Business Object created from Response Message
		ArrayList<MethodParameter> mps = extractHolderParameters(bo);
        
		if(mps.size() <=0){
			return;
		}

		index=0;
		//assign Holder values from Business Object to input client parameters.
		for(Object inputArg: objList){
		    if(inputArg!=null){
		        Holder inputHolder = (Holder)inputArg;
		        MethodParameter mp = mps.get(index);
		        Holder responseHolder = (Holder)mp.getValue();
		        inputHolder.value = responseHolder.value;
		    }
			index++;
		}
		
	}
	
	protected Object findProperty(String propertyName, Object jaxbObject)throws JAXBWrapperException{
		JAXBWrapperTool wrapTool = new JAXBWrapperToolImpl();
		if (log.isDebugEnabled()) {
            log.debug("Attempting to unwrap objects");
        }
		Object[] webResult = wrapTool.unWrap(jaxbObject,new ArrayList<String>(Arrays.asList(new String[]{propertyName})));
		if (log.isDebugEnabled()) {
            log.debug("Object unwrapped");
        }
		//this is a parameter in JAXBObject
		return webResult[0];
	}

	
	/*
	 * Utility methods below are used entiredly by marshalFaultResponse and
	 * demarshalFaultResponse
	 */
	
	
    private static Exception createCustomException(String message, Class exceptionclass, Object bean, Class beanFormalType) throws InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchMethodException {
		// All webservice exception classes are required to have a constructor that takes a (String, bean) argument
    	// TODO necessary to be more careful here with instantiating, cassting, etc?
		if (log.isDebugEnabled()) {
		    log.debug("Constructing JAX-WS Exception:" + exceptionclass);
        }
        Constructor constructor = exceptionclass.getConstructor(new Class[] { String.class, beanFormalType });
		Object exception = constructor.newInstance(new Object[] { message, bean });

		return (Exception) exception;

    }
    
    private static Exception createGenericException(String message) {
    	return ExceptionFactory.makeWebServiceException(message);
    }
    
}
