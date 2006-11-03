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
package org.apache.axis2.jaxws.marshaller.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Future;

import javax.jws.WebParam.Mode;
import javax.management.openmbean.SimpleType;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBIntrospector;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Holder;
import javax.xml.ws.Response;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.FaultDescription;
import org.apache.axis2.jaxws.description.OperationDescription;
import org.apache.axis2.jaxws.description.ParameterDescription;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.marshaller.ClassUtils;
import org.apache.axis2.jaxws.marshaller.MethodMarshaller;
import org.apache.axis2.jaxws.marshaller.MethodParameter;
import org.apache.axis2.jaxws.message.Block;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.MessageException;
import org.apache.axis2.jaxws.message.Protocol;
import org.apache.axis2.jaxws.message.XMLFault;
import org.apache.axis2.jaxws.message.XMLFaultReason;
import org.apache.axis2.jaxws.message.databinding.JAXBBlockContext;
import org.apache.axis2.jaxws.message.factory.JAXBBlockFactory;
import org.apache.axis2.jaxws.message.factory.MessageFactory;
import org.apache.axis2.jaxws.message.factory.XMLStringBlockFactory;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.axis2.jaxws.wrapper.JAXBWrapperTool;
import org.apache.axis2.jaxws.wrapper.impl.JAXBWrapperException;
import org.apache.axis2.jaxws.wrapper.impl.JAXBWrapperToolImpl;
import org.apache.axis2.util.XMLUtils;
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
	 * @see org.apache.axis2.jaxws.helper.XMLMessageConvertor#toJAXBObject(org.apache.axis2.jaxws.message.Message)
	 */
	public abstract Object demarshalResponse(Message message, Object[] inputArgs) throws IllegalAccessException, InstantiationException, ClassNotFoundException, JAXBWrapperException, JAXBException, XMLStreamException, MessageException; 

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.helper.XMLMessageConvertor#toObjects(org.apache.axis2.jaxws.message.Message)
	 */
	public abstract Object[] demarshalRequest(Message message) throws ClassNotFoundException, JAXBException, MessageException, JAXBWrapperException, XMLStreamException, InstantiationException, IllegalAccessException;

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.helper.XMLMessageConvertor#fromJAXBObject(java.lang.Object)
	 */
	public abstract Message marshalResponse(Object returnObject, Object[] holderObjects)throws ClassNotFoundException, JAXBException, MessageException, JAXBWrapperException, XMLStreamException, InstantiationException, IllegalAccessException; 
	
	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.helper.XMLMessageConvertor#fromObjects(java.lang.Object[])
	 */
	public abstract Message marshalRequest(Object[] object)throws IllegalAccessException, InstantiationException, ClassNotFoundException, JAXBWrapperException, JAXBException, MessageException, XMLStreamException; 
	
	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.marshaller.MethodMarshaller#demarshalFaultResponse(org.apache.axis2.jaxws.message.Message)
	 */
	public Object demarshalFaultResponse(Message message) {
		
		Exception exception = null;
        
		try {
			XMLFault xmlfault = message.getXMLFault();
			//Class beanclass = (jaxbClassName == null || jaxbClassName.length() ==0 || className == null || className.length() == 0) ? null : loadClass(jaxbClassName);
			Block[] blocks = xmlfault.getDetailBlocks();
            
			if ((operationDesc.getFaultDescriptions().length == 0) || (blocks == null)) {
				exception = createGenericException(xmlfault.getReason()
						.getText());
			} else {
                for(FaultDescription fd: operationDesc.getFaultDescriptions()) {
                    for (Block block: blocks) {
                        Object obj = createFaultBusinessObject(loadClass(fd.getBeanName()), block);
                        if (obj.getClass().getName().equals(fd.getBeanName())) {
                            // create the exception we actually want to throw
                            Class exceptionclass = loadClass(fd.getExceptionClassName());
                            return createCustomException(xmlfault.getReason().getText(), exceptionclass, obj);
                        }
                    }
                }
                // if we get out of the for loop without returning anything, that means an endpoint
                // has thrown a custom exception that doesn't have an annotation -- that's illegal!
                exception = ExceptionFactory.makeWebServiceException("Endpoint has thrown a custom exception that has no annotation.");
			}
		} catch (Exception e) {
			// TODO if we have problems creating the exception object, we'll end up here,
            // where we should return at least a meaningfull exception to the user
			exception = ExceptionFactory.makeWebServiceException(e.toString());
		}

		return exception;
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.marshaller.MethodMarshaller#marshalFaultResponse(java.lang.Throwable)
	 */
	public Message marshalFaultResponse(Throwable throwable) throws IllegalAccessException, InvocationTargetException, JAXBException, ClassNotFoundException, NoSuchMethodException, MessageException, XMLStreamException {
		Throwable t = ClassUtils.getRootCause(throwable);
		
        FaultDescription fd = operationDesc.resolveFaultByExceptionName(t.getClass().getName());

		// if faultClazzName is still null, don't create a detail block.  If it's non-null, we need a detail block.
		XMLFault xmlfault = null;
        ArrayList<Block> detailBlocks = new ArrayList<Block>();
        
        String text = null;
		if (fd != null) {
			Method getFaultInfo = t.getClass().getMethod("getFaultInfo", null);
			Object faultBean = getFaultInfo.invoke(t, null);
			Class faultClazz = loadClass(fd.getBeanName());
			JAXBBlockContext context = createJAXBBlockContext(faultClazz);
			detailBlocks.add(createJAXBBlock(faultBean, context));
            text = t.getMessage();
        } else {
            // TODO probably want to set text to the stacktrace
            text = t.toString();
        }
		xmlfault = new XMLFault(null, // Use the default XMLFaultCode
                    new XMLFaultReason(text),  // Assumes text is the language supported by the current Locale
                    detailBlocks.toArray(new Block[0]));
				
		Message message = null;
		message = createEmptyMessage();
		message.setXMLFault(xmlfault);
		
		return message;
		
		//throw new UnsupportedOperationException();
	}
		
	/*
	 * Creates method output parameter/return parameter. reads webResult annotation and then matches them with the response/result value of Invoked method
	 * and creates a name value pair.
	 * Also hadnles situation where ResponseWrapper is a holder.
	 */
	
	protected ArrayList<MethodParameter> toOutputMethodParameter(Object webResultValue){
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
	
	protected ArrayList<MethodParameter> toOutputMethodParameter(Object webResultObject, Object[] holderObjects)throws IllegalAccessException, InstantiationException, ClassNotFoundException{
		ParameterDescription[] paramDescs = operationDesc.getParameterDescriptions();
		ArrayList<ParameterDescription> pds = new ArrayList<ParameterDescription>();
		pds = toArrayList(paramDescs);
		// Remove all non holder meta data.
		for (int index = 0; index < paramDescs.length; index++) {
			ParameterDescription paramDesc = paramDescs[index];
			if (!(paramDesc.isHolderType())) {
				pds.remove(index);
			}
		}
		
		ArrayList<Object> paramValues = toArrayList(holderObjects);
		ArrayList<MethodParameter> mps = createMethodParameters(pds.toArray(new ParameterDescription[0]), paramValues);
		if(webResultObject!=null){
			MethodParameter outputResult = new MethodParameter(operationDesc.getResultName(), operationDesc.getResultTargetNamespace(), webResultObject.getClass(), webResultObject);
			mps.add(outputResult);
		}
		return mps;
		
	}
	
	protected ArrayList<MethodParameter> toInputMethodParameter(Object jaxbObject) throws JAXBWrapperException, IllegalAccessException, InstantiationException, ClassNotFoundException{
		ArrayList<MethodParameter> mps = new ArrayList<MethodParameter>();
		if(jaxbObject == null){
			return mps;
		}
        ArrayList<String> webParam = toArrayList(operationDesc.getParamNames());
                
        if (log.isDebugEnabled()) {
            log.debug("Attempting to unwrap object from WrapperClazz");
        }
        JAXBWrapperTool wrapperTool = new JAXBWrapperToolImpl();
        Object[] objects = wrapperTool.unWrap(jaxbObject, webParam);
        if (log.isDebugEnabled()) {
            log.debug("Object unwrapped");
        }
        return toInputMethodParameters(objects);
	}
	/*
	 * Creates method input parameters, reads webparam annotation and then matches them to the input parameters that the the invoked method was supplied 
	 * and creates a name value pair.
	 */
	protected ArrayList<MethodParameter> toInputMethodParameters(Object[] objects)throws IllegalAccessException, InstantiationException, ClassNotFoundException{
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
		paramValues = toArrayList(objects);
		if (log.isDebugEnabled()) {
			log.debug("Attempting to create Method Parameters");
		}
		mps = createMethodParameters(paramDescs, paramValues);

		if (log.isDebugEnabled()) {
			log.debug("Method Parameters created");
		}
					
		return mps;
	}
	
	protected ArrayList<MethodParameter> toInputMethodParameter(Message message)throws IllegalAccessException, InstantiationException, ClassNotFoundException, MessageException, XMLStreamException, JAXBException{
		ArrayList<MethodParameter> mps = new ArrayList<MethodParameter>();
 		if(message == null){
 			return null;
 		}
		ParameterDescription[] paramDescs = operationDesc.getParameterDescriptions();
		
		ArrayList<Object> paramValues = new ArrayList<Object>(); 
		for (int index = 0; index < paramDescs.length; index++) {
			ParameterDescription paramDesc = paramDescs[index];
			String paramName = paramDesc.getParameterName();
			String paramTNS = paramDesc.getTargetNamespace();
			boolean isHeader = paramDesc.isHeader();
			Class actualType = paramDesc.getParameterActualType();
			Object bo = null;
			if(isHeader){
				bo = createBOFromHeaderBlock(actualType, message, paramTNS, paramName);
			}
			else{
				bo = createBOFromBodyBlock(actualType,message);
			}
			paramValues.add(bo);
		}
		mps = createMethodParameters(paramDescs, paramValues);
		
		return mps;
	}
	protected ArrayList<MethodParameter> createMethodParameters(
			ParameterDescription[] paramDescs, ArrayList<Object> paramValues)
			throws IllegalAccessException, InstantiationException,
			ClassNotFoundException {
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
					// Identify Holders and get Holder Values, this if condition
					// will mostly execute during client side call
					if (paramValue != null && isHolder(paramValue)
							&& isHolderType) {
						Object holderValue = getHolderValue(paramMode,
								paramValue);
						mp = new MethodParameter(paramDesc, holderValue);
					}
					// Identify that param value is not Holders however if the
					// method parameter is holder type and create Holder, this
					// will mostly be called during server side call
					else if (paramValue != null && !isHolder(paramValue)
							&& isHolderType) {
						Holder<Object> holder = createHolder(paramType,
								paramValue);
						mp = new MethodParameter(paramDesc, holder);
					} else {
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
		
		JAXBIntrospector introspector = context.getIntrospector();
		if(introspector.isElement(jaxbObject)){
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

	protected Block createEmptyBodyBlock()throws MessageException{
		String emptyBody = "";
		XMLStringBlockFactory stringFactory = (XMLStringBlockFactory) FactoryRegistry.getFactory(XMLStringBlockFactory.class);
		return stringFactory.createFrom(emptyBody, null, SOAPENV_QNAME);
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
			JAXBBlockContext ctx = createJAXBBlockContext(objectType);
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
	
	protected Message createMessage(Object jaxbObject, Class jaxbClazz, String jaxbClassName, String targetNamespace)throws JAXBException, MessageException, XMLStreamException{
		Block bodyBlock = null;
		JAXBBlockContext ctx = createJAXBBlockContext(jaxbClazz);
		if (log.isDebugEnabled()) {
            log.debug("Attempting to create Block");
        }
		if(ClassUtils.isXmlRootElementDefined(jaxbClazz)){
			bodyBlock = createJAXBBlock(jaxbObject, ctx);
		}
		else{
			bodyBlock =  createJAXBBlock(jaxbClassName, jaxbObject, ctx, targetNamespace);
		}
		if (log.isDebugEnabled()) {
            log.debug("JAXBBlock Created");
        }
		
		MessageFactory mf = (MessageFactory)FactoryRegistry.getFactory(MessageFactory.class);
		
		Message m = mf.create(protocol);
		m.setBodyBlock(0,bodyBlock);
		return m;
	}
	
	protected Message createFaultMessage(OMElement element) throws XMLStreamException, MessageException {
		MessageFactory mf = (MessageFactory)FactoryRegistry.getFactory(MessageFactory.class);
		return mf.createFrom(element);
	}
	
	protected Message createEmptyMessage()throws JAXBException, MessageException, XMLStreamException{
		Block emptyBodyBlock = createEmptyBodyBlock();
		MessageFactory mf = (MessageFactory)FactoryRegistry.getFactory(MessageFactory.class);
		Message m = mf.create(protocol);
		m.setBodyBlock(0,emptyBodyBlock);
		return m;
	}
	
	protected Object createBOFromHeaderBlock(Class jaxbClazz, Message message, String targetNamespace, String localPart) throws JAXBException, MessageException, XMLStreamException{
		
		JAXBBlockContext blockContext = createJAXBBlockContext(jaxbClazz);
		
		// Get a JAXBBlockFactory instance.  We'll need this to get the JAXBBlock
        // out of the Message
        JAXBBlockFactory factory = (JAXBBlockFactory)FactoryRegistry.getFactory(JAXBBlockFactory.class); 
        Block block = message.getHeaderBlock(targetNamespace, localPart, blockContext, factory);
        return block.getBusinessObject(true);
	}
	
	protected Object createBOFromBodyBlock(Class jaxbClazz, Message message) throws JAXBException, MessageException, XMLStreamException{
		return createBusinessObject(jaxbClazz, message);
	}

	
	protected Object createBusinessObject(Class jaxbClazz, Message message) throws JAXBException, MessageException, XMLStreamException{
		JAXBBlockContext blockContext = createJAXBBlockContext(jaxbClazz);
		
		// Get a JAXBBlockFactory instance.  We'll need this to get the JAXBBlock
        // out of the Message
        JAXBBlockFactory factory = (JAXBBlockFactory)FactoryRegistry.getFactory(JAXBBlockFactory.class);
        
        Block block = message.getBodyBlock(0, blockContext, factory);
        return block.getBusinessObject(true);
	}
	
	private JAXBBlockContext createJAXBBlockContext(Class jaxbClazz) throws JAXBException, MessageException {
		// Primitives, simpleTypes and classes without a root element must be represented as a JAXBElement
		boolean useJAXBElement = !ClassUtils.isXmlRootElementDefined(jaxbClazz);
			
		JAXBBlockContext blockContext = new JAXBBlockContext(jaxbClazz, useJAXBElement);
		
		return blockContext;
	}

	protected Object createFaultBusinessObject(Class jaxbClazz, Block block)
			throws JAXBException, MessageException, XMLStreamException {
		JAXBBlockContext blockContext = createJAXBBlockContext(jaxbClazz);
		
		OMElement om = block.getOMElement();

		XMLInputFactory xmlFactory = XMLInputFactory.newInstance();

		Unmarshaller u = blockContext.getUnmarshaller();
		Reader inputReader = new InputStreamReader(new ByteArrayInputStream(om
				.toString().getBytes()));
		XMLStreamReader sr = xmlFactory.createXMLStreamReader(inputReader);
		JAXBElement o = u.unmarshal(sr, jaxbClazz);
		return o.getValue();

	}
	protected void createResponseHolders(ArrayList<MethodParameter> mps, ArrayList<Object> inputArgHolders, Message message)throws JAXBException, MessageException, XMLStreamException{
		Object bo = null;
		int index = 0;
		for(MethodParameter mp:mps){
			ParameterDescription pd = mp.getParameterDescription();
			if (pd.isHeader() && pd.isHolderType()) {
				bo = createBOFromHeaderBlock(pd.getParameterActualType(),
						message, pd.getTargetNamespace(), pd
								.getParameterName());
			}
			else if(!pd.isHeader() && pd.isHolderType()){
				bo = createBOFromBodyBlock(pd.getParameterActualType(), message);
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

	
	protected void createResponseHolders(Object bo, Object[] inputArgs, boolean isBare)throws JAXBWrapperException, InstantiationException, ClassNotFoundException, IllegalAccessException{
		if(inputArgs == null){
			return;
		}
		ArrayList<Object> objList = toArrayList(inputArgs);
		for(Object arg:inputArgs){
			if(arg == null){
				objList.remove(arg);
			}
			else if(arg!=null && !Holder.class.isAssignableFrom(arg.getClass())){
				objList.remove(arg);
			}
			
		}
		if(objList.size()<=0){
			return;
		}
		ArrayList<MethodParameter> mps = null;
		if(isBare){
			mps = toInputMethodParameters(new Object[]{bo});
			
		}
		else{
			mps = toInputMethodParameter(bo);
		}
			
		MethodParameter[] mpArray = mps.toArray(new MethodParameter[0]);
		for(MethodParameter mp:mpArray){
			ParameterDescription pd = mp.getParameterDescription();
			if(!pd.isHolderType()){
				mps.remove(mp);
			}
		}
		if(mps.size() <=0){
			return;
		}
		mpArray = null;
		int index=0;
		for(Object inputArg: objList){
			Holder inputHolder = (Holder)inputArg;
			MethodParameter mp = mps.get(index);
			Holder responseHolder = (Holder)mp.getValue();
			inputHolder.value = responseHolder.value;
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
	
	
    private static Exception createCustomException(String message, Class exceptionclass, Object bean) throws InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchMethodException {
		// All webservice exception classes are required to have a constructor that takes a (String, bean) argument
    	// TODO necessary to be more careful here with instantiating, cassting, etc?
		Constructor constructor = exceptionclass.getConstructor(new Class[] { String.class, bean.getClass() });
		Object exception = constructor.newInstance(new Object[] { message, bean });

		return (Exception) exception;

    }
    
    private static Exception createGenericException(String message) {
    	return ExceptionFactory.makeWebServiceException(message);
    }
    
}
