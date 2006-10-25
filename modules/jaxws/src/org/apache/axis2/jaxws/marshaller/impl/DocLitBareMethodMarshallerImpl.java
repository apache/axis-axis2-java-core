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
import org.apache.axis2.jaxws.description.ParameterDescription;
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
		ArrayList<Object> holderArgs = toArrayList(inputArgs);
		ArrayList<MethodParameter> mps = new ArrayList<MethodParameter>();
		mps = toInputMethodParameters(inputArgs);
		ArrayList<MethodParameter> holdermps = new ArrayList<MethodParameter>(mps);
		int index =0;
		//Remove everything except holders from method parameters and input arguments.
		for(MethodParameter mp: mps){
			ParameterDescription pd = mp.getParameterDescription();
			if(!pd.isHolderType()){
				holdermps.remove(mp);
				holderArgs.remove(mp.getValue());
			}
			index++;
		}
		mps = null;
		if(holdermps.size() == 0 && returnType.getName().equals("void")){
			//No holders and return type void example --> public void someMethod() I will return null for this case.
			//doNothing as there is nothing to return.
			
			return null;
		
		}
		else if(holdermps.size() == 0 && !returnType.getName().equals("void")){
			//No holders but a return type example --> public ReturnType someMethod()
			Object bo = createBusinessObject(returnType, message);
			return bo;
			
		}
		else if(holdermps.size()>0 && returnType.getName().equals("void")){
			//Holders found and no return type example --> public void someMethod(Holder<AHolder>)	
			createResponseHolders(holdermps, holderArgs, message);
			
		}
		else{
			//Holders found and return type example --> public ReturnType someMethod(Holder<AHolder>)
			//Note that SEI implementation will wrap return type in a holder if method has a return type and input param as holder.
			//WSGen and WsImport Generate Holders with return type as one of the Holder JAXBObject property, if wsdl schema forces a holder and a return type.
			createResponseHolders(holdermps, holderArgs, message);
			Object bo = createBusinessObject(returnType, message);
			return bo;
		}
		return null;
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
		ArrayList<Object> objectList = new ArrayList<Object>();
		int index =0;
		ArrayList<MethodParameter> mps = toInputMethodParameter(message);
		if (log.isDebugEnabled()) {
            log.debug("reading input method parameters");
        }
		for(MethodParameter mp:mps){
			ParameterDescription pd = mp.getParameterDescription();
			if(pd.isHolderType()){
	        	Object holderObject = mp.getValue();
	        	objectList.add(holderObject);
	        }
	        else{
	        	objectList.add(mp.getValue());
	        }
		}
       return objectList.toArray();		
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.convertor.impl.MessageConvertorImpl#fromJAXBObject(java.lang.Object)
	 */
	@Override
	public Message marshalResponse(Object returnObject, Object[] holderObjects) throws ClassNotFoundException, JAXBException, MessageException, JAXBWrapperException, XMLStreamException, InstantiationException, IllegalAccessException{
		// Response wrapper is basically the return type. so the return object is a jaxbObject. If there is a holder objects then that is the responsewrapper.
		Class wrapperClazz = getReturnType();
		String wrapperClazzName = operationDesc.getWebResultName();
		if(wrapperClazzName == null || wrapperClazzName.trim().length() == 0){
			wrapperClazzName = wrapperClazz.getName();
		}
		String wrapperTNS = operationDesc.getWebResultTargetNamespace();
		
		ArrayList<MethodParameter> mps = new ArrayList<MethodParameter>();
		mps = toInputMethodParameters(holderObjects);
		ArrayList<MethodParameter> holdersNreturnObject = new ArrayList<MethodParameter>(mps);
		
		//Remove everything except holders
		for(MethodParameter mp: mps){
			ParameterDescription pd = mp.getParameterDescription();
			if(!pd.isHolderType()){
				holdersNreturnObject.remove(mp);
			}
		}
		
		mps = null;
		Message message = null;
		
		if(holdersNreturnObject.size() == 0 && wrapperClazz.getName().equals("void")){
			//No holders and return type void example --> public void someMethod() I will return empty ResponseWrapper in message body for this case.
			//doNothing as there is nothing to wrap
			
			message = createEmptyMessage();
		
		}
		else if(holdersNreturnObject.size() == 0 && !wrapperClazz.getName().equals("void")){
			//No holders but a return type example --> public ReturnType someMethod()
			MethodParameter mp = new MethodParameter(wrapperClazzName,wrapperTNS, wrapperClazz, returnObject);
			holdersNreturnObject.add(mp);
			message = createMessage(holdersNreturnObject);
			
		}
		else if(holdersNreturnObject.size()>0 && wrapperClazz.getName().equals("void")){
			//Holders found and no return type example --> public void someMethod(Holder<AHolder>)	
			message = createMessage(holdersNreturnObject);
			
		}
		else{
			//Holders found and return type example --> public ReturnType someMethod(Holder<AHolder>)
			//Note that SEI implementation will wrap return type in a holder if method has a return type and input param as holder.
			//WSGen and WsImport Generate Holders with return type as one of the Holder JAXBObject property, if wsdl schema forces a holder and a return type.
			
			MethodParameter mp = new MethodParameter(wrapperClazzName,wrapperTNS, wrapperClazz, returnObject);
			holdersNreturnObject.add(mp);
			message = createMessage(holdersNreturnObject);
			
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
			int numberOfBodyPart =0;
			for(MethodParameter mp:mps){
				ParameterDescription pd = mp.getParameterDescription();
				if(!pd.getWebParamHeader()){
					numberOfBodyPart++;
				}
			}
			if(numberOfBodyPart > SIZE){
				if (log.isDebugEnabled()) {
		            log.debug("As per WS-I compliance, Multi part WSDL with more than one body part not allowed for Doc/Lit NON Wrapped request, Method invoked has multiple input parameter");
		        }
				throw ExceptionFactory.makeWebServiceException(Messages.getMessage("DocLitProxyHandlerErr1"));
			}
		}
		
		//Lets handle case where there is one message part or one input parameter
		Message message = null;
		
		if(mps.size() !=0){
			message = createMessage(mps);
		}
		//no message part case or no input parameter
		if(mps.size() == 0){
			message = createEmptyMessage();
		}
		
		return message;
	}
}
