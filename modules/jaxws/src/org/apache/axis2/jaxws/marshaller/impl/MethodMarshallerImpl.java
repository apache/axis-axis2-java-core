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
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Future;

import javax.jws.WebParam.Mode;
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
import javax.xml.ws.Holder;
import javax.xml.ws.Response;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.OperationDescription;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.marshaller.MethodMarshaller;
import org.apache.axis2.jaxws.marshaller.MethodParameter;
import org.apache.axis2.jaxws.message.Block;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.MessageException;
import org.apache.axis2.jaxws.message.Protocol;
import org.apache.axis2.jaxws.message.factory.JAXBBlockFactory;
import org.apache.axis2.jaxws.message.factory.MessageFactory;
import org.apache.axis2.jaxws.message.factory.XMLStringBlockFactory;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.axis2.jaxws.wrapper.JAXBWrapperTool;
import org.apache.axis2.jaxws.wrapper.impl.JAXBWrapperException;
import org.apache.axis2.jaxws.wrapper.impl.JAXBWrapperToolImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class MethodMarshallerImpl implements MethodMarshaller {

	private static String DEFAULT_ARG="arg";
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
	public abstract Object demarshalFaultResponse(Message message); 

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.marshaller.MethodMarshaller#isFault(org.apache.axis2.jaxws.message.Message)
	 */
	public abstract boolean isFault(Message message); 
		

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.marshaller.MethodMarshaller#marshalFaultResponse(java.lang.Throwable)
	 */
	public abstract Message marshalFaultResponse(Throwable throwable); 
		
	/*
	 * Creates method output parameter/return parameter. reads webResult annotation and then matches them with the response/result value of Invoked method
	 * and creates a name value pair.
	 * Also hadnles situation where ResponseWrapper is a holder.
	 */
	
	protected ArrayList<MethodParameter> toOutputMethodParameter(Object webResultValue){
		ArrayList<MethodParameter> mps = new ArrayList<MethodParameter>();
		String webResult = operationDesc.getWebResultName();
		MethodParameter mp = new MethodParameter(webResult, webResultValue, null);
		mps.add(mp);
		return mps;
	}
	
	protected ArrayList<MethodParameter> toOutputMethodParameter(Object webResultObject, Object[] holderObjects)throws IllegalAccessException, InstantiationException, ClassNotFoundException{
		//Get all names of input parameters
		ArrayList<String> paramNames = new ArrayList<String>();
		paramNames =toArrayList(operationDesc.getWebParamNames());
		
		//Get all modes for params
		ArrayList<Mode> paramMode = new ArrayList<Mode>();
		paramMode = toArrayList(operationDesc.getWebParamModes());
		
		Method seiMethod = operationDesc.getSEIMethod();
	    Class[] types = seiMethod.getParameterTypes();
	    ArrayList<Class> paramTypes = toArrayList(types);
	    ArrayList<Class> actualTypes = getInputTypes();
	    
		for(int i =0;i<types.length; i++){
			if(!(types[i].isAssignableFrom(Holder.class))){
				paramNames.remove(i);
				paramMode.remove(i);
				paramTypes.remove(i);
			}
		}
		
		ArrayList<Object> paramValues = toArrayList(holderObjects);
		if(webResultObject!=null){
			paramValues.add(webResultObject);
			paramNames.add(operationDesc.getWebResultName());
			//dummy mode for return object
			paramMode.add(Mode.IN);
		}
		//lets create name value pair.
		return createMethodParameters(paramNames, paramValues, paramMode, paramTypes, actualTypes);
		
	}
	
	protected ArrayList<MethodParameter> toInputMethodParameter(Object jaxbObject) throws JAXBWrapperException, IllegalAccessException, InstantiationException, ClassNotFoundException{
		ArrayList<MethodParameter> mps = new ArrayList<MethodParameter>();
		if(jaxbObject == null){
			return mps;
		}
        ArrayList<String> webParam = toArrayList(operationDesc.getWebParamNames());
        ArrayList<Mode> modes = toArrayList(operationDesc.getWebParamModes());
        
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
		
		//Get all names of input parameters
		ArrayList<String> paramNames = new ArrayList<String>();
		paramNames =toArrayList(operationDesc.getWebParamNames());
		//Get all the objects of input parameters
		ArrayList<Object> paramValues = new ArrayList<Object>(); 
		paramValues = toArrayList(objects);
		//Get all modes for params
		ArrayList<Mode> paramMode = new ArrayList<Mode>();
		paramMode = toArrayList(operationDesc.getWebParamModes());
		
		Method seiMethod = operationDesc.getSEIMethod();
		
		Class[] clazz = seiMethod.getParameterTypes();
		ArrayList<Class> paramTypes = toArrayList(clazz);
     
		ArrayList<Class> actualTypes = getInputTypes();
		int i =0;
		
		//if no webParam defined then lets get default names.
		if(paramNames.size() == 0 && paramValues.size()>0){
			while(i< paramValues.size()){
				paramNames.add(DEFAULT_ARG + i++);
			}
		}
		if(paramNames.size() != paramValues.size()){
			throw ExceptionFactory.makeWebServiceException(Messages.getMessage("InvalidWebParams"));
		}
		//lets create name value pair.
		mps = createMethodParameters(paramNames, paramValues, paramMode, paramTypes, actualTypes);
		
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
				/*
				if(rawClazz.isAssignableFrom(Holder.class)){
					Class actualClazz = (Class)pType.getActualTypeArguments()[0];
					paramTypes.add(actualClazz);
				}
				*/
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
	
	protected boolean isXmlRootElementDefined(Class jaxbClass){
		XmlRootElement root = (XmlRootElement) jaxbClass.getAnnotation(XmlRootElement.class);
		return root !=null;
	}
	
	protected <T> ArrayList<T> toArrayList(T[] objects){
		return (objects!=null)? new ArrayList<T>(Arrays.asList(objects)):new ArrayList<T>();
	}
	
	protected Block createJAXBBlock(Object jaxbObject, JAXBContext context) throws MessageException{
		JAXBBlockFactory factory = (JAXBBlockFactory)FactoryRegistry.getFactory(JAXBBlockFactory.class);
		return factory.createFrom(jaxbObject,context,null);
		
	}
	
	protected Block createJAXBBlock(String name, Object jaxbObject, JAXBContext context) throws MessageException{
		
		JAXBIntrospector introspector = context.createJAXBIntrospector();
		if(introspector.isElement(jaxbObject)){
			return createJAXBBlock(jaxbObject, context);
		}
		else{
			//Create JAXBElement then use that to create JAXBBlock.
			Class clazz = jaxbObject.getClass();
			JAXBElement<Object> element = new JAXBElement<Object>(new QName(name), clazz, jaxbObject);
			JAXBBlockFactory factory = (JAXBBlockFactory)FactoryRegistry.getFactory(JAXBBlockFactory.class);
			return factory.createFrom(element,context ,null);
		}
		
	}
	protected Block createJAXBBlock(OMElement om, JAXBContext context)throws javax.xml.stream.XMLStreamException{
		JAXBBlockFactory factory = (JAXBBlockFactory)FactoryRegistry.getFactory(JAXBBlockFactory.class);
		return factory.createFrom(om,context,null);
		
	}

	protected Block createEmptyBodyBlock()throws MessageException{
		String emptyBody = "";
		XMLStringBlockFactory stringFactory = (XMLStringBlockFactory) FactoryRegistry.getFactory(XMLStringBlockFactory.class);
		return stringFactory.createFrom(emptyBody, null, SOAPENV_QNAME);
	}
	
	protected JAXBContext createJAXBContext(String wrapperClazzName) throws ClassNotFoundException, JAXBException {
		Class wrapperClazz = loadClass(wrapperClazzName);
		return createJAXBContext(wrapperClazz);
        
    }
	
	protected JAXBContext createJAXBContext(Class wrapperClazz) throws JAXBException{
		return JAXBContext.newInstance(new Class[]{wrapperClazz});
	}
	
	protected Class loadClass(String className)throws ClassNotFoundException{
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
	
	protected ArrayList<MethodParameter> createMethodParameters(ArrayList<String> paramNames, ArrayList<Object>paramValues, ArrayList<Mode> paramModes, ArrayList<Class> paramTypes, ArrayList<Class> actualTypes) throws IllegalAccessException, InstantiationException, ClassNotFoundException{
		ArrayList<MethodParameter> mps = new ArrayList<MethodParameter>();
		int i=0;
		for(String paramName:paramNames){
			Object paramValue = paramValues.get(i);
			
			//initialize to default value.
			Mode paramMode = Mode.IN;
			if(paramModes !=null && paramModes.size() >0){
				paramMode =paramModes.get(i);
			}
			Class paramType = null;
			Class actualType = null;
			if(paramTypes !=null){
				paramType = paramTypes.get(i);
			}
			if(actualTypes!=null){
				actualType = actualTypes.get(i);
			}
			MethodParameter mp = null;
			//If call is Async call then lets filter AsyncHandler object name and value;
			if(!isParamAsyncHandler(paramName, paramValue)){
				
				if(paramType !=null){
					//Identify Holders and get Holder Values, this if condition will mostly execute during client side call
					if(isHolder(paramValue) && isHolder(paramType)){
						Object holderValue = getHolderValue(paramMode, paramValue);
						mp = new MethodParameter(paramName, holderValue, paramMode, paramType, actualType, true);
						
					}
					//Identify that param value is not Holders however if the method parameter is holder type and create Holder, this will mostly be called during server side call 
					else if(!isHolder(paramValue) && isHolder(paramType)){
						Holder<Object> holder = createHolder(paramType, paramValue);
						mp=new MethodParameter(paramName, holder, paramMode, paramType, actualType, true);
					}
					else{
						mp = new MethodParameter(paramName, paramValue, paramMode, paramType, actualType, false);
					}
				}
				if(paramType == null){
					if(isHolder(paramValue)){
						Object holderValue = getHolderValue(paramMode, paramValue);
						mp = new MethodParameter(paramName, holderValue, paramMode);	
					}
					else{
						mp = new MethodParameter(paramName, paramValues, paramMode);
					}
				}
				mps.add(mp);
			}
			
			i++;
		}
		return mps;
	}
	
	private boolean isParamAsyncHandler(String name, Object value){
		//TODO I would like to check the name of the parameter to "asyncHandler" As per the JAX-WS specification
		//However the RI tooling has a bug where it generates partName="asyncHandler" for doc/lit bare case instead of name="asyncHandler".
		//Once fixed we can also check for name but for now this will work
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
	
	protected Message createMessage(Object jaxbObject, Class jaxbClazz, String jaxbClassName)throws JAXBException, MessageException, XMLStreamException{
		Block bodyBlock = null;
		JAXBContext ctx = createJAXBContext(jaxbClazz);
		if (log.isDebugEnabled()) {
            log.debug("Attempting to create Block");
        }
		if(isXmlRootElementDefined(jaxbClazz)){
			bodyBlock = createJAXBBlock(jaxbObject, ctx);
		}
		else{
			bodyBlock =  createJAXBBlock(jaxbClassName, jaxbObject, ctx);
		}
		if (log.isDebugEnabled()) {
            log.debug("JAXBBlock Created");
        }
		
		MessageFactory mf = (MessageFactory)FactoryRegistry.getFactory(MessageFactory.class);
		
		Message m = mf.create(protocol);
		m.setBodyBlock(0,bodyBlock);
		return m;
	}
	protected Message createEmptyMessage()throws JAXBException, MessageException, XMLStreamException{
		Block emptyBodyBlock = createEmptyBodyBlock();
		MessageFactory mf = (MessageFactory)FactoryRegistry.getFactory(MessageFactory.class);
		Message m = mf.create(protocol);
		m.setBodyBlock(0,emptyBodyBlock);
		return m;
	}
	
	protected Object createBusinessObject(Class jaxbClazz, Message message) throws JAXBException, MessageException, XMLStreamException{
		JAXBContext ctx = createJAXBContext(jaxbClazz);
		
		// Get a JAXBBlockFactory instance.  We'll need this to get the JAXBBlock
        // out of the Message
        JAXBBlockFactory factory = (JAXBBlockFactory)FactoryRegistry.getFactory(JAXBBlockFactory.class);
        
        Block block = message.getBodyBlock(0, ctx, factory);
        try{
        	return block.getBusinessObject(true);
        }catch(Exception e){
        //FIXME: this is the bare case where child of body is not a method but a primitive data type. Reader from Block is throwing exception.
        	block = message.getBodyBlock(0, ctx,factory);
        	OMElement om = block.getOMElement();
        	
        	XMLInputFactory xmlFactory = XMLInputFactory.newInstance();
		
        	Unmarshaller u = ctx.createUnmarshaller();
        	Reader inputReader = new InputStreamReader(new ByteArrayInputStream(om.toString().getBytes()));
        	XMLStreamReader sr = xmlFactory.createXMLStreamReader(inputReader);
        	JAXBElement o =u.unmarshal(sr, jaxbClazz);
        	return o.getValue();
        
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
			if(!mp.isHolder()){
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

	
	
}
