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

import java.util.ArrayList;

import javax.naming.OperationNotSupportedException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import javax.xml.ws.Holder;

import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.OperationDescription;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.marshaller.DocLitBareMethodMarshaller;
import org.apache.axis2.jaxws.marshaller.MethodParameter;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.MessageException;
import org.apache.axis2.jaxws.message.Protocol;
import org.apache.axis2.jaxws.wrapper.impl.JAXBWrapperException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DocLitBareMethodMarshallerImpl extends MethodMarshallerImpl
		implements DocLitBareMethodMarshaller {
	private static int SIZE = 1;
	private static Log log = LogFactory.getLog(DocLitBareMethodMarshallerImpl.class);
	/**
	 * @param serviceDesc
	 * @param endpointDesc
	 * @param operationDesc
	 */
	public DocLitBareMethodMarshallerImpl(ServiceDescription serviceDesc,
			EndpointDescription endpointDesc, OperationDescription operationDesc, Protocol protocol) {
		super(serviceDesc, endpointDesc, operationDesc, protocol);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.convertor.impl.MessageConvertorImpl#toJAXBObject(org.apache.axis2.jaxws.message.Message)
	 */
	@Override
	public Object demarshalResponse(Message message, Object[] inputArgs) throws IllegalAccessException, InstantiationException, ClassNotFoundException, JAXBWrapperException, JAXBException, XMLStreamException, MessageException{
		
		Class returnType = getReturnType();
		String resultName = operationDesc.getWebResultName();
		Object bo = null;
		if(returnType.getName().equals("void")){
			ArrayList<MethodParameter> mps = toInputMethodParameters(inputArgs);
			for(MethodParameter mp:mps){
				if(mp.isHolder()){
					returnType = mp.getActualType();
				}
			}
			
		}
		bo = createBusinessObject(returnType, message);
		//In a bare case there should not be a situation where there is a return type and a holder.
		createResponseHolders(bo, inputArgs, true);
		
		return bo;
		
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.convertor.impl.MessageConvertorImpl#toObjects(org.apache.axis2.jaxws.message.Message)
	 */
	@Override
	public Object[] demarshalRequest(Message message)throws ClassNotFoundException, JAXBException, MessageException, JAXBWrapperException, XMLStreamException, InstantiationException, IllegalAccessException{
		
		ArrayList<Class> inputParams = getInputTypes();
		//Method has no input parameters
		if(inputParams.size() == 0){
			return null;
		}
		if(inputParams.size() > SIZE){
			if (log.isDebugEnabled()) {
	            log.debug("As per WS-I compliance, Multi part WSDL not allowed for Doc/Lit NON Wrapped request, Method invoked has multiple input parameter");
	        }
			throw ExceptionFactory.makeWebServiceException(Messages.getMessage("DocLitProxyHandlerErr1"));
		}
		
		Class jaxbClass = inputParams.get(0);
		
		Object jaxbObject = createBusinessObject(jaxbClass, message);
        
        if (log.isDebugEnabled()) {
            log.debug("reading input method parameters");
        }
        Class rawType = operationDesc.getSEIMethod().getParameterTypes()[0];
       
        if(isHolder(rawType)){
        	
        	return new Object[]{createHolder(rawType, jaxbObject)};
        }
        else{
        	return new Object[]{jaxbObject};
        }
       
		
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.convertor.impl.MessageConvertorImpl#fromJAXBObject(java.lang.Object)
	 */
	@Override
	public Message marshalResponse(Object returnObject, Object[] holderObjects) throws ClassNotFoundException, JAXBException, MessageException, JAXBWrapperException, XMLStreamException, InstantiationException, IllegalAccessException{
		// Response wrapper is basically the return type. so the return object is a jaxbObject. If there is a holder objects then that is the responsewrapper.
		Class wrapperClazz = getReturnType();
		String wrapperClazzName = wrapperClazz.getName();
		String webResult = operationDesc.getWebResultName();
		
		//create all holders list
		ArrayList<Object> objectList = new ArrayList<Object>();
		if(holderObjects!=null){
			objectList = toArrayList(holderObjects);
			for(Object obj:holderObjects){
				if(!(isHolder(obj))){
					objectList.remove(obj);
				}
			}
		}
		if(objectList.size()> SIZE){
			//More than one holder input parameter found, this is a WS-I violation.
			if (log.isDebugEnabled()) {
	            log.debug("As per WS-I compliance, Multi part WSDL not allowed for Doc/Lit NON Wrapped request, Method invoked has multiple input parameter");
	        }
			throw ExceptionFactory.makeWebServiceException(Messages.getMessage("DocLitProxyHandlerErr1"));
		}
		Message message = null;
		ArrayList<MethodParameter> mps = new ArrayList<MethodParameter>();
		if(objectList.size() == 0 && wrapperClazz.getName().equals("void")){
			//No holders and return type void example --> public void someMethod() I will return empty ResponseWrapper in message body for this case.
			//doNothing as there is nothing to wrap
		
		}
		if(objectList.size() == 0 && !wrapperClazz.getName().equals("void")){
			//No holders but a return type example --> public ReturnType someMethod()
			
			message = createMessage(returnObject, wrapperClazz, wrapperClazzName);
		}
		else{
			//Holders found and return type or no return type. example --> public ReturnType someMethod(Holder<AHolder>) or public void someMethod(Holder<AHolder>)
			//Note that SEI implementation will wrap return type in a holder if method has a return type and input param as holder.
			//WSGen and WsImport Generate Holders with return type as one of the Holder JAXBObject property, if wsdl schema forces a holder and a return type.
			ArrayList<Class> holderType = getInputTypes();
			Holder holder = (Holder)objectList.get(0);
			Object value = holder.value;
			message = createMessage(value, holderType.get(0), holderType.get(0).getName());
		}
		
		return message;
		
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.convertor.impl.MessageConvertorImpl#fromObjects(java.lang.Object[])
	 */
	@Override
	public Message marshalRequest(Object[] objects) throws IllegalAccessException, InstantiationException, ClassNotFoundException, JAXBWrapperException, JAXBException, MessageException, javax.xml.stream.XMLStreamException{
		
		
		ArrayList<MethodParameter> mps = toInputMethodParameters(objects);
		
		//WSDL wrapped and running wsImport with non-wrap binding or wsdl un-Wrapped and running wsImport with no binding, EITHER WAYS 
		//there can be only 0 or 1 Body parts as per WS-I. 
		if(mps.size()> SIZE){
			if (log.isDebugEnabled()) {
	            log.debug("As per WS-I compliance, Multi part WSDL not allowed for Doc/Lit NON Wrapped request, Method invoked has multiple input parameter");
	        }
			throw ExceptionFactory.makeWebServiceException(Messages.getMessage("DocLitProxyHandlerErr1"));
		}
		
		//Lets handle case where there is one message part or one input parameter
		Message message = null;
		if(mps.size() !=0){
			Object requestObject = null;
			String requestObjectName = null;
			Class requestObjectType = null;
			JAXBContext ctx = null;
			for(MethodParameter mp : mps){
				requestObject = mp.getValue();
				requestObjectName = mp.getName();
				requestObjectType = mp.getActualType();
				if(requestObject == null){
					if (log.isDebugEnabled()) {
			            log.debug("Method Input parameter for NON Wrapped Request cannot be null");
			        }
					throw ExceptionFactory.makeWebServiceException(Messages.getMessage("DocLitProxyHandlerErr2"));
				}
			}
				
			message = createMessage(requestObject, requestObjectType, requestObjectName);
		}
		//no message part case or no input parameter
		if(mps.size() == 0){
			message = createEmptyMessage();
		}
		
		return message;
	}

	@Override
	public Object demarshalFaultResponse(Message message) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isFault(Message message) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Message marshalFaultResponse(Throwable throwable) {
		throw new UnsupportedOperationException();
	}
}
