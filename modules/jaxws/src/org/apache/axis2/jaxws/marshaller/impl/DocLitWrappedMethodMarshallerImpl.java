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
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.OperationDescription;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.marshaller.DocLitWrappedMethodMarshaller;
import org.apache.axis2.jaxws.marshaller.MethodParameter;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.MessageException;
import org.apache.axis2.jaxws.message.Protocol;
import org.apache.axis2.jaxws.wrapper.JAXBWrapperTool;
import org.apache.axis2.jaxws.wrapper.impl.JAXBWrapperException;
import org.apache.axis2.jaxws.wrapper.impl.JAXBWrapperToolImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DocLitWrappedMethodMarshallerImpl extends MethodMarshallerImpl
		implements DocLitWrappedMethodMarshaller {
	private static Log log = LogFactory.getLog(DocLitWrappedMethodMarshallerImpl.class);

	/**
	 * @param serviceDesc
	 * @param endpointDesc
	 * @param operationDesc
	 */
	public DocLitWrappedMethodMarshallerImpl(ServiceDescription serviceDesc,
			EndpointDescription endpointDesc, OperationDescription operationDesc, Protocol protocol) {
		super(serviceDesc, endpointDesc, operationDesc, protocol);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.convertor.impl.MessageConvertorImpl#toJAXBObject(org.apache.axis2.jaxws.message.Message)
	 */
	@Override
	public Object demarshalResponse(Message message, Object[] inputArgs) throws IllegalAccessException, InstantiationException, ClassNotFoundException, JAXBWrapperException, JAXBException, javax.xml.stream.XMLStreamException, MessageException{
		Class wrapperClazz = null;
		String className = operationDesc.getResponseWrapperClassName();
		//TODO Move this to Operation Description.
		if(className == null || (className!=null && className.length()==0)){
			wrapperClazz = getReturnType();
		}
		else{		
			wrapperClazz = loadClass(className);
		}
		String resultName = operationDesc.getWebResultName();
		Object bo = createBusinessObject(wrapperClazz, message);
		createResponseHolders(bo, inputArgs, false);
        // REVIEW: Is the the appropriate logic, to be checking for the existence of the annotation
        //         as the decision point for getting into the property logic?  Note that even if the annotation
        //         is not present, a default result name will be returned.
		// If the WebResult annotation is present, then look up the result Name
		if(operationDesc.isWebResultAnnotationSpecified()){
		//if ReturnType is not of same type as JAXBBlock business Object then I will look for resultName in Business Object and return that.
			Object resultObject = findProperty(resultName, bo);
			return resultObject;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.convertor.impl.MessageConvertorImpl#toObjects(org.apache.axis2.jaxws.message.Message)
	 */
	@Override
	public Object[] demarshalRequest(Message message)throws ClassNotFoundException, JAXBException, MessageException, JAXBWrapperException, XMLStreamException, InstantiationException, IllegalAccessException {
        String className = operationDesc.getRequestWrapperClassName();
        Class requestWrapperClazz = loadClass(className);
        Object jaxbObject = createBusinessObject(requestWrapperClazz, message);
        
        if (log.isDebugEnabled()) {
            log.debug("reading input method parameters");
        }
        ArrayList<MethodParameter> mps = toInputMethodParameter(jaxbObject);
        if (log.isDebugEnabled()) {
            log.debug("done reading input method parameters");
        }
        
        Object[] contents = new Object[mps.size()];
        int i =0;
        for (MethodParameter mp:mps){
        	contents[i++] =mp.getValue();
        }
        
        if (log.isDebugEnabled()) {
            log.debug("Object unwrapped");
        }
        
        return contents;
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.convertor.impl.MessageConvertorImpl#fromJAXBObject(java.lang.Object)
	 */
	@Override
	public Message marshalResponse(Object returnObject, Object[] holderObjects)throws ClassNotFoundException, JAXBException, MessageException, JAXBWrapperException, XMLStreamException, InstantiationException, IllegalAccessException {
		Class wrapperClazz = null;
		String wrapperClazzName = operationDesc.getResponseWrapperClassName();
		String wrapperTNS = operationDesc.getResponseWrapperTargetNamespace();
		String webResult = operationDesc.getWebResultName();
		//TODO Move this to Operation Description.
		if(wrapperClazzName == null || (wrapperClazzName!=null && wrapperClazzName.length()==0)){
			wrapperClazz = getReturnType();
			wrapperClazzName = wrapperClazz.getName();
			if(log.isDebugEnabled()){
				log.debug("No ResponseWrapper annotation found, using return type of method as response object");
			}
		}
		else{		
			wrapperClazz = loadClass(wrapperClazzName);
		}
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
		//No Holders found 
		ArrayList<MethodParameter> mps = new ArrayList<MethodParameter>();
		if(objectList.size() == 0 && wrapperClazz.getName().equals("void")){
			//No holders and return type void example --> public void someMethod() I will return empty ResponseWrapper in message body for this case.
			//doNothing as there is nothing to wrap
		}
		if(objectList.size() == 0 && !wrapperClazz.getName().equals("void")){
			//No holders but a return type example --> public ReturnType someMethod()
			mps = toOutputMethodParameter(returnObject);
		}
		else{
			//Holders found and return type or no return type. example --> public ReturnType someMethod(Holder<String>) or public void someMethod(Holder<String>)
			mps = toOutputMethodParameter(returnObject, objectList.toArray());
		}
		
        JAXBWrapperTool wrapperTool = new JAXBWrapperToolImpl();
        Object wrapper = wrapperTool.wrap(wrapperClazz, 
        		wrapperClazzName, mps);
        
		Message message = createMessage(wrapper, wrapperClazz, wrapperClazzName, wrapperTNS);
		return message;
		
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.convertor.impl.MessageConvertorImpl#fromObjects(java.lang.Object[])
	 */
	@Override
	public Message marshalRequest(Object[] objects) throws IllegalAccessException, InstantiationException, ClassNotFoundException, JAXBWrapperException, JAXBException, MessageException, javax.xml.stream.XMLStreamException {
		
		String className = operationDesc.getRequestWrapperClassName();
		Class wrapperClazz = loadClass(className);
		String localName = operationDesc.getRequestWrapperLocalName();
		String wrapperTNS = operationDesc.getRequestWrapperTargetNamespace();
		
		//Get Name Value pair for input parameter Objects, skip AsyncHandler and identify Holders.
		ArrayList<MethodParameter> methodParameters = toInputMethodParameters(objects);
		JAXBWrapperTool wrapTool = new JAXBWrapperToolImpl();
		if (log.isDebugEnabled()) {
            log.debug("JAXBWrapperTool attempting to wrap propertes in WrapperClass :" + wrapperClazz);
        }
	
		Object jaxbObject = wrapTool.wrap(wrapperClazz, localName, methodParameters);
		if (log.isDebugEnabled()) {
            log.debug("JAXBWrapperTool wrapped following propertes :");
        }
		
		Message message = createMessage(jaxbObject, wrapperClazz, localName, wrapperTNS);
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
