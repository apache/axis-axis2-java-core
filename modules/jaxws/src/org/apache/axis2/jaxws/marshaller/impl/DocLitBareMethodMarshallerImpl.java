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

import java.util.ArrayList;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.OperationDescription;
import org.apache.axis2.jaxws.description.ParameterDescription;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.marshaller.DocLitBareMethodMarshaller;
import org.apache.axis2.jaxws.marshaller.MarshalException;
import org.apache.axis2.jaxws.marshaller.MethodParameter;
import org.apache.axis2.jaxws.marshaller.UnmarshalException;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.MessageException;
import org.apache.axis2.jaxws.message.Protocol;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DocLitBareMethodMarshallerImpl extends MethodMarshallerImpl implements DocLitBareMethodMarshaller {
    
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
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.convertor.impl.MessageConvertorImpl#toJAXBObject(org.apache.axis2.jaxws.message.Message)
	 */
	@Override
	public Object demarshalResponse(Message message, Object[] inputArgs) throws UnmarshalException {
		Class returnType = getReturnType();
		
        ArrayList<Object> holderArgs = null;
        ArrayList<MethodParameter> mps = null;
        ArrayList<MethodParameter> holdermps = null;
        try {
            holderArgs = new ArrayList<Object>();
            mps = new ArrayList<MethodParameter>();
            mps = extractHolderParameters(inputArgs);
            holdermps = new ArrayList<MethodParameter>(mps);
        } catch (IllegalAccessException e) {
            throw new UnmarshalException("Unable to get holder method parameters", e);
        } catch (InstantiationException e) {
            throw new UnmarshalException("Unable to get holder method parameters", e);
        } catch (ClassNotFoundException e) {
            throw new UnmarshalException("Unable to get holder method parameters", e);
        }

        // Remove everything except holders from input arguments.
        int index = 0;
        for(Object inputArg: inputArgs){
			if(inputArg !=null && isHolder(inputArg)){
				holderArgs.add(inputArg);
			}
			index++;
		}
		
        try {
            if(holdermps.size() == 0 && returnType.getName().equals("void")){
            	// No holders and return type void example --> public void someMethod() 
                // I will return null for this case.
            	// doNothing as there is nothing to return.
            	return null;
            }
            else if(holdermps.size() == 0 && !returnType.getName().equals("void")){
            	// No holders but a return type example --> public ReturnType someMethod()
            	Object bo = createBusinessObject(returnType, message);
            	return bo;
            }
            else if(holdermps.size()>0 && returnType.getName().equals("void")){
            	// Holders found and no return type example --> public void someMethod(Holder<AHolder>)	
            	assignHolderValues(holdermps, holderArgs, message);
            }
            else{
            	// Holders found and return type example --> public ReturnType someMethod(Holder<AHolder>)
            	// Note that SEI implementation will wrap return type in a holder if method has a return 
                // type and input param as holder.
            	// WSGen and WsImport Generate Holders with return type as one of the Holder JAXBObject 
                // property, if wsdl schema forces a holder and a return type.
            	assignHolderValues(holdermps, holderArgs, message);
            	Object bo = createBusinessObject(returnType, message);
            	return bo;
            }
        } catch (JAXBException e) {
            throw new UnmarshalException("Unable to demarshal response", e);
        } catch (MessageException e) {
            throw new UnmarshalException("Unable to demarshal response", e);
        } catch (XMLStreamException e) {
            throw new UnmarshalException("Unable to demarshal response", e);
        }

        return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.convertor.impl.MessageConvertorImpl#toObjects(org.apache.axis2.jaxws.message.Message)
	 */
	@Override
	public Object[] demarshalRequest(Message message) throws UnmarshalException {
	    if (log.isDebugEnabled()) {
	        log.debug("Attempting to demarshal a document/literal request.");
        }
        
        ArrayList<Class> inputParams = getInputTypes();
		
        // If the method has no input parameters, then we're done.
		if(inputParams.size() == 0){
			return null;
		}
        
        ArrayList<MethodParameter> mps;
        try {
            mps = createParameterForSEIMethod(message);
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("An error occured while demarshalling the request" + e.getMessage());
            }
            throw new UnmarshalException("Unable to demarshal the request message.", e);
        }
        
        ArrayList<Object> objectList = new ArrayList<Object>();
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

	/* 
     * (non-Javadoc)
	 * @see org.apache.axis2.jaxws.convertor.impl.MessageConvertorImpl#fromJAXBObject(java.lang.Object)
	 */
	@Override
	public Message marshalResponse(Object returnObject, Object[] holderObjects) throws MarshalException {
		// Response wrapper is basically the return type. So the return object 
        // is a JAXB object. If there is a holder objects then that is the 
        // responsewrapper.
		Class wrapperClazz = getReturnType();
		String wrapperClazzName = operationDesc.getResultName();
		if (wrapperClazzName == null || wrapperClazzName.trim().length() == 0) {
			wrapperClazzName = wrapperClazz.getName();
		}
		String wrapperTNS = operationDesc.getResultTargetNamespace();
		
		ArrayList<MethodParameter> holdersNreturnObject;
        try {
            holdersNreturnObject = extractHolderParameters(holderObjects);
        } catch (IllegalAccessException e) {
            throw new MarshalException("Unable to extract holder params", e);
        } catch (InstantiationException e) {
            throw new MarshalException("Unable to extract holder params", e);
        } catch (ClassNotFoundException e) {
            throw new MarshalException("Unable to extract holder params", e);
        }
		
        Message message = null;
		try {
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
        } catch (JAXBException e) {
            throw new MarshalException("An error occured while marshalling the response message.", e);
        } catch (MessageException e) {
            throw new MarshalException("An error occured while marshalling the response message.", e);
        } catch (XMLStreamException e) {
            throw new MarshalException("An error occured while marshalling the response message.", e);
        }
		
		return message;
	}

	/* 
     * (non-Javadoc)
	 * @see org.apache.axis2.jaxws.convertor.impl.MessageConvertorImpl#fromObjects(java.lang.Object[])
	 */
	@Override
	public Message marshalRequest(Object[] objects) throws MarshalException {
		if (log.isDebugEnabled()) {
		    log.debug("Attempting to marshal document/literal request");
        }
        
        ArrayList<MethodParameter> mps = null;
        try {
            mps = createRequestWrapperParameters(objects);
        } catch (IllegalAccessException e) {
            throw new MarshalException("Unable to create request wrapper parameters", e);
        } catch (InstantiationException e) {
            throw new MarshalException("Unable to create request wrapper parameters", e);
        } catch (ClassNotFoundException e) {
            throw new MarshalException("Unable to create request wrapper parameters", e);
        }
		
        
        //WSDL wrapped and running wsImport with non-wrap binding or wsdl un-Wrapped and running wsImport with no binding, EITHER WAYS 
		//there can be only 0 or 1 Body parts as per WS-I. 
		if(mps.size()> SIZE){
			int numberOfBodyPart =0;
			for(MethodParameter mp:mps){
				ParameterDescription pd = mp.getParameterDescription();
				if(!pd.isHeader()){
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
		
		try {
            if (mps.size() !=0) {
            	message = createMessage(mps);
            }
            //no message part case or no input parameter
            if (mps.size() == 0) {
            	message = createEmptyMessage();
            }
        } catch (JAXBException e) {
            throw new MarshalException("An error occured while creating the request message", e);
        } catch (MessageException e) {
            throw new MarshalException("An error occured while creating the request message", e);
        } catch (XMLStreamException e) {
            throw new MarshalException("An error occured while creating the request message", e);
        }
		
		return message;
	}
}
