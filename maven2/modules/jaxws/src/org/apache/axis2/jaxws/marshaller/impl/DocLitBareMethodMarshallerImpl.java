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
import java.util.Set;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import javax.xml.ws.WebServiceException;

import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.description.OperationDescription;
import org.apache.axis2.jaxws.description.ParameterDescription;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.marshaller.MethodParameter;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.MessageException;
import org.apache.axis2.jaxws.message.Protocol;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DocLitBareMethodMarshallerImpl extends MethodMarshallerImpl  {
    
	private static int SIZE = 1;
	private static Log log = LogFactory.getLog(DocLitBareMethodMarshallerImpl.class);
	
    /**
	 * @param serviceDesc
	 * @param endpointDesc
	 * @param operationDesc
	 */
	public DocLitBareMethodMarshallerImpl() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.convertor.impl.MessageConvertorImpl#toJAXBObject(org.apache.axis2.jaxws.message.Message)
	 */
	@Override
	public Object demarshalResponse(Message message, Object[] inputArgs, OperationDescription operationDesc) throws WebServiceException {
		try {

			Class returnType = getReturnType(operationDesc);

			ArrayList<Object> holderArgs = null;
			ArrayList<MethodParameter> mps = null;
			ArrayList<MethodParameter> holdermps = null;

			holderArgs = new ArrayList<Object>();
			mps = new ArrayList<MethodParameter>();
			mps = extractHolderParameters(inputArgs, operationDesc);
			holdermps = new ArrayList<MethodParameter>(mps);


			// Remove everything except holders from input arguments.
			int index = 0;
			for(Object inputArg: inputArgs){
				if(inputArg !=null && isHolder(inputArg)){
					holderArgs.add(inputArg);
				}
				index++;
			}

            Object bo = null;
			if(holdermps.size() == 0 && returnType.getName().equals("void")){
				// No holders and return type void example --> public void someMethod() 
				// I will return null for this case.
				// doNothing as there is nothing to return.
				
			}
			else if(holdermps.size() == 0 && !returnType.getName().equals("void")){
				// No holders but a return type example --> public ReturnType someMethod()
				bo = createBusinessObject(createContextPackageSet(operationDesc), message);
			}
			else if(holdermps.size()>0 && returnType.getName().equals("void")){
				// Holders found and no return type example --> public void someMethod(Holder<AHolder>)	
				assignHolderValues(holdermps, holderArgs, message, operationDesc);
			}
			else{
				// Holders found and return type example --> public ReturnType someMethod(Holder<AHolder>)
				// Note that SEI implementation will wrap return type in a holder if method has a return 
				// type and input param as holder.
				// WSGen and WsImport Generate Holders with return type as one of the Holder JAXBObject 
				// property, if wsdl schema forces a holder and a return type.
				assignHolderValues(holdermps, holderArgs, message, operationDesc);
				bo = createBusinessObject(createContextPackageSet(operationDesc), message);
			}

            if (bo instanceof JAXBElement) {
                bo = ((JAXBElement) bo).getValue();
            }

			return bo;
		} catch (Exception e) {
			// Firewall.  Only WebServiceExceptions are thrown
			throw ExceptionFactory.makeWebServiceException(e);
		}
        
    }
	private ArrayList<MethodParameter> createParameterForSEIMethod(Message message, OperationDescription operationDesc)throws IllegalAccessException, InstantiationException, ClassNotFoundException, MessageException, XMLStreamException, JAXBException{
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
            // Create a set of context packages that will be needed to demarshal
            // the jaxb object.  For now just consider the actualType
            Set<String> contextPackages = createContextPackageSet(operationDesc);
            
            // Create the business object
            if(isHeader){
	            bo = createBOFromHeaderBlock(contextPackages, message, paramTNS, paramName);
	        }
	        else{
	            bo = createBOFromBodyBlock(contextPackages,message);
	        }
	        
            // The resulting business object may be a JAXBElement.
            // In such cases get the contained type
            if ( (actualType != JAXBElement.class) &&  
                    bo instanceof JAXBElement) {
                bo = ((JAXBElement) bo).getValue();
            }
            
            // Now create an argument from the business object
            Object arg = bo;
	        if (paramDesc.isHolderType()) {
                // If the parameter requires a holder, create a holder
                // object containting the parameter
	            arg = createHolder(paramDesc.getParameterType(), bo);
	        } 
	        paramValues.add(arg);
	    }
	    mps = createParameters(paramDescs, paramValues, operationDesc);
	    
	    return mps;
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.convertor.impl.MessageConvertorImpl#toObjects(org.apache.axis2.jaxws.message.Message)
	 */
	@Override
	public Object[] demarshalRequest(Message message, OperationDescription operationDesc) throws WebServiceException {
		try {
			if (log.isDebugEnabled()) {
				log.debug("Attempting to demarshal a document/literal request.");
			}

			ArrayList<Class> inputParams = getInputTypes(operationDesc);

			// If the method has no input parameters, then we're done.
			if(inputParams.size() == 0){
				return null;
			}

			ArrayList<MethodParameter> mps = createParameterForSEIMethod(message, operationDesc);

			ArrayList<Object> objectList = new ArrayList<Object>();
			if (log.isDebugEnabled()) {
				log.debug("reading input method parameters");
			}
			for(MethodParameter mp:mps){
				objectList.add(mp.getValue());
			}
			return objectList.toArray();		
		} catch (Exception e) {
			// Firewall.  Only WebServiceExceptions are thrown
			throw ExceptionFactory.makeWebServiceException(e);
		}
	}

	/* 
     * (non-Javadoc)
	 * @see org.apache.axis2.jaxws.convertor.impl.MessageConvertorImpl#fromJAXBObject(java.lang.Object)
	 */
	@Override
	public Message marshalResponse(Object returnObject, Object[] holderObjects, OperationDescription operationDesc, Protocol protocol ) throws WebServiceException {
		try {
			// Response wrapper is basically the return type. So the return object 
			// is a JAXB object. If there is a holder objects then that is the 
			// responsewrapper.
			Class wrapperClazz = getReturnType(operationDesc);
			String wrapperClazzName = operationDesc.getResultName();
			if (wrapperClazzName == null || wrapperClazzName.trim().length() == 0) {
				wrapperClazzName = wrapperClazz.getName();
			}
			String wrapperTNS = operationDesc.getResultTargetNamespace();

			ArrayList<MethodParameter> holdersNreturnObject = extractHolderParameters(holderObjects, operationDesc);


			Message message = null;

			if(holdersNreturnObject.size() == 0 && wrapperClazz.getName().equals("void")){
				//No holders and return type void example --> public void someMethod() I will return empty ResponseWrapper in message body for this case.
				//doNothing as there is nothing to wrap
				message = createEmptyMessage(operationDesc);
			}
			else if(holdersNreturnObject.size() == 0 && !wrapperClazz.getName().equals("void")){
				//No holders but a return type example --> public ReturnType someMethod()
				MethodParameter mp = new MethodParameter(wrapperClazzName,wrapperTNS, wrapperClazz, returnObject);
				holdersNreturnObject.add(mp);
				message = createMessage(holdersNreturnObject, operationDesc);
			}
			else if(holdersNreturnObject.size()>0 && wrapperClazz.getName().equals("void")){
				//Holders found and no return type example --> public void someMethod(Holder<AHolder>)	
				message = createMessage(holdersNreturnObject, operationDesc);
			}
			else{
				//Holders found and return type example --> public ReturnType someMethod(Holder<AHolder>)
				//Note that SEI implementation will wrap return type in a holder if method has a return type and input param as holder.
				//WSGen and WsImport Generate Holders with return type as one of the Holder JAXBObject property, if wsdl schema forces a holder and a return type.
				MethodParameter mp = new MethodParameter(wrapperClazzName,wrapperTNS, wrapperClazz, returnObject);
				holdersNreturnObject.add(mp);
				message = createMessage(holdersNreturnObject, operationDesc);
			}


			return message;
		} catch (Exception e) {
			// Firewall.  Only WebServiceExceptions are thrown
			throw ExceptionFactory.makeWebServiceException(e);
		}
	}

	/* 
     * (non-Javadoc)
	 * @see org.apache.axis2.jaxws.convertor.impl.MessageConvertorImpl#fromObjects(java.lang.Object[])
	 */
	@Override
	public Message marshalRequest(Object[] objects, OperationDescription operationDesc) throws WebServiceException {
		try {
			if (log.isDebugEnabled()) {
				log.debug("Attempting to marshal document/literal request");
			}

			ArrayList<MethodParameter> mps = createRequestWrapperParameters(objects, operationDesc);

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

			if (mps.size() !=0) {
				message = createMessage(mps, operationDesc);
			}
			//no message part case or no input parameter
			if (mps.size() == 0) {
				message = createEmptyMessage(operationDesc);
			}


			return message;
		} catch (Exception e) {
			// Firewall.  Only WebServiceExceptions are thrown
			throw ExceptionFactory.makeWebServiceException(e);
		}
	}
	
	
}
