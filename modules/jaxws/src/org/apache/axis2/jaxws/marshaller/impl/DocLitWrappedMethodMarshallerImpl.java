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
import java.util.Map;
import java.util.WeakHashMap;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.ws.WebServiceException;

import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.OperationDescription;
import org.apache.axis2.jaxws.description.OperationDescriptionJava;
import org.apache.axis2.jaxws.description.ParameterDescription;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.marshaller.MethodParameter;
import org.apache.axis2.jaxws.message.Block;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.MessageException;
import org.apache.axis2.jaxws.message.Protocol;
import org.apache.axis2.jaxws.message.databinding.JAXBBlockContext;
import org.apache.axis2.jaxws.message.factory.MessageFactory;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.axis2.jaxws.util.XMLRootElementUtil;
import org.apache.axis2.jaxws.wrapper.JAXBWrapperTool;
import org.apache.axis2.jaxws.wrapper.impl.JAXBWrapperException;
import org.apache.axis2.jaxws.wrapper.impl.JAXBWrapperToolImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DocLitWrappedMethodMarshallerImpl extends MethodMarshallerImpl {
	private static Log log = LogFactory.getLog(DocLitWrappedMethodMarshallerImpl.class);

	/**
	 * @param serviceDesc
	 * @param endpointDesc
	 * @param operationDesc
	 */
	public DocLitWrappedMethodMarshallerImpl(ServiceDescription serviceDesc,
			EndpointDescription endpointDesc, OperationDescription operationDesc, Protocol protocol) {
		super(serviceDesc, endpointDesc, operationDesc, protocol);
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.convertor.impl.MessageConvertorImpl#toJAXBObject(org.apache.axis2.jaxws.message.Message)
	 */
	@Override
	public Object demarshalResponse(Message message, Object[] inputArgs) throws WebServiceException {
		
        try {
        	if (log.isDebugEnabled()) {
    		    log.debug("Attempting to demarshal a document/literal wrapped response");
            }
            
    		String className = operationDesc.getResponseWrapperClassName();
            Object businessObject = null;
            
            //TODO Move this to Operation Description.
            Class wrapperClazz = null;
            if (className == null || (className != null && className.length() == 0)) {
    			wrapperClazz = getReturnType();
    		}
    		else {		
                wrapperClazz = loadClass(className);
    		}
    		
            String resultName = operationDesc.getResultName();
    		businessObject = createBusinessObject(createContextPackageSet(), message);
            assignHolderValues(businessObject, inputArgs, false);
            
            // REVIEW: Is the the appropriate logic, to be checking for the existence of the annotation
            //         as the decision point for getting into the property logic?  Note that even if the annotation
            //         is not present, a default result name will be returned.
            // If the WebResult annotation is present, then look up the result Name
            if(((OperationDescriptionJava) operationDesc).isWebResultAnnotationSpecified()){
                // If the return type is not of same type as the JAXBBlock business Object then 
                // look for resultName in Business Object and return that.
            	Object resultObject = findProperty(resultName, businessObject);
            	return resultObject;
            }
            return businessObject;
        } catch (Exception e) {
        	// Firewall.  Only WebServiceExceptions are thrown
            throw ExceptionFactory.makeWebServiceException(e);
        }
       
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.convertor.impl.MessageConvertorImpl#toObjects(org.apache.axis2.jaxws.message.Message)
	 */
	@Override
	public Object[] demarshalRequest(Message message) throws WebServiceException {
		try {
			String className = operationDesc.getRequestWrapperClassName();

			ArrayList<MethodParameter> mps;

			Class requestWrapperClazz = loadClass(className);
			Object jaxbObject = createBusinessObject(createContextPackageSet(), message);

			if (log.isDebugEnabled()) {
				log.debug("reading input method parameters");
			}

			mps = createParameterForSEIMethod(jaxbObject);


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
		} catch (Exception e) {
			// Firewall.  Only WebServiceExceptions are thrown
			throw ExceptionFactory.makeWebServiceException(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.convertor.impl.MessageConvertorImpl#fromJAXBObject(java.lang.Object)
	 */
	@Override
	public Message marshalResponse(Object returnObject, Object[] holderObjects) throws WebServiceException {

		try {
			// Get the necessary information from the OperationDesc
			Class wrapperClazz = null;
			String wrapperClazzName = operationDesc.getResponseWrapperClassName();
			String wrapperLocalName = operationDesc.getResponseWrapperLocalName();
			String wrapperTNS = operationDesc.getResponseWrapperTargetNamespace();
			String webResult = operationDesc.getResultName();

			//TODO Move this to Operation Description.
			if (wrapperClazzName == null || (wrapperClazzName != null && wrapperClazzName.length() == 0)) {
				if (log.isDebugEnabled()) {
					log.debug("No ResponseWrapper annotation found, using return type of method as response object");
				}
				wrapperClazz = getReturnType();
				wrapperClazzName = wrapperClazz.getName();
			}
			else {		
				wrapperClazz = loadClass(wrapperClazzName);
			}


			// Create all holders list
			ParameterDescription[] paramDescs = operationDesc.getParameterDescriptions();
			ArrayList<Object> objectList = new ArrayList<Object>();
			int index =0;
			for(ParameterDescription pd:paramDescs){
				Object value = holderObjects[index];
				if(pd.isHolderType()){
					objectList.add(value);
				}
				index++;
			}

			ArrayList<MethodParameter> mps = null;

			mps = new ArrayList<MethodParameter>();
			if(objectList.size() == 0 && wrapperClazz.getName().equals("void")){
				//No holders and return type void example --> public void someMethod() I will return empty ResponseWrapper in message body for this case.
				//doNothing as there is nothing to wrap
			}
			if(objectList.size() == 0 && !wrapperClazz.getName().equals("void")){
				//No holders but a return type example --> public ReturnType someMethod()
				mps = createResponseWrapperParameter(returnObject);
			}
			else{
				//Holders found and return type or no return type. example --> public ReturnType someMethod(Holder<String>) or public void someMethod(Holder<String>)
				mps = createResponseWrapperParameter(returnObject, objectList.toArray());
			}

			Object wrapper = wrap(wrapperClazz, mps);
			
            // If the wrapper class does not represent an root element, then make
            // the appropriate JAXBElement
            if (!XMLRootElementUtil.isElementEnabled(wrapperClazz)) {
                wrapper = XMLRootElementUtil.getElementEnabledObject(wrapperTNS, 
                        wrapperLocalName, wrapperClazz, wrapper, false);
            }
			Message message = createMessage(wrapper);


			return message;
		} catch (Exception e) {
			// Firewall.  Only WebServiceExceptions are thrown
			throw ExceptionFactory.makeWebServiceException(e);
		}

	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.convertor.impl.MessageConvertorImpl#fromObjects(java.lang.Object[])
	 */
	@Override
	public Message marshalRequest(Object[] objects) throws WebServiceException {
		try {
			String className = operationDesc.getRequestWrapperClassName();
			String wrapperLocalName = operationDesc.getRequestWrapperLocalName();
			String wrapperTNS = operationDesc.getRequestWrapperTargetNamespace();

			Class wrapperClazz = null;

			wrapperClazz = loadClass(className);

			//Get Name Value pair for input parameter Objects, skip AsyncHandler and identify Holders.
			Object jaxbObject = null;

			ArrayList<MethodParameter> methodParameters = createRequestWrapperParameters(objects);

			jaxbObject = wrap(wrapperClazz, methodParameters);
			

            // If the wrapper class does not represent an root element, then make
            // the appropriate JAXBElement
            if (!XMLRootElementUtil.isElementEnabled(wrapperClazz)) {
                jaxbObject = 
                    XMLRootElementUtil.getElementEnabledObject(wrapperTNS, 
                            wrapperLocalName,
                            wrapperClazz, 
                            jaxbObject, false);
            }
			Message message = createMessage(jaxbObject);


			return message;

		} catch (Exception e) {
			// Firewall.  Only WebServiceExceptions are thrown
			throw ExceptionFactory.makeWebServiceException(e);
		}
	}

    // FIXME: This is wrong.  We first need to get the ClassLoader from the 
    // AxisService if there is one on there.  Then, if that does not exist
    // we can grab the thread's context ClassLoader.
	private Class loadClass(String className) throws ClassNotFoundException {
		// TODO J2W AccessController Needed
		// Don't make this public, its a security exposure
        Class c = Class.forName(className, true, Thread.currentThread().getContextClassLoader());
		return c;
	}
    
    /**
     * @param jaxbElement object representing the element to marshal (JAXBElement or object has @XmlRootElement)
     * @return
     * @throws JAXBException
     * @throws MessageException
     * @throws XMLStreamException
     */
    private Message createMessage(Object jaxbElement)throws JAXBException, MessageException, XMLStreamException{
            Block bodyBlock = null;
            
            // Get the object that is the type
            Object jaxbType = (jaxbElement instanceof JAXBElement) ? ((JAXBElement) jaxbElement).getValue() : jaxbElement; 
     
            // Create the context
            JAXBBlockContext ctx = new JAXBBlockContext(createContextPackageSet());
            bodyBlock = createJAXBBlock(jaxbElement, ctx);
            
            if (log.isDebugEnabled()) {
                log.debug("JAXBBlock Created");
            }
            
            MessageFactory mf = (MessageFactory)FactoryRegistry.getFactory(MessageFactory.class);
            
            Message m = mf.create(protocol);
            m.setBodyBlock(0,bodyBlock);
            return m;
        }
    
    private Object wrap(Class jaxbClass, ArrayList<MethodParameter> mps) throws JAXBWrapperException{
        if (log.isDebugEnabled()) {
            log.debug("start: Create Doc Lit Wrapper");
        }
        if(mps == null){
            throw new JAXBWrapperException(Messages.getMessage("JAXBWrapperErr7"));
        }
        ArrayList<String> nameList = new ArrayList<String>();
        Map<String, Object> objectList = new WeakHashMap<String, Object>();
        for(MethodParameter mp:mps){
            ParameterDescription pd = mp.getParameterDescription();
            String name = null;
            if(!mp.isWebResult()){
                name = pd.getParameterName();
            }else{
                name = mp.getWebResultName();
            }
            Object object = mp.getValue();
            
            nameList.add(name);
            objectList.put(name, object);
        }
        JAXBWrapperTool wrapperTool = new JAXBWrapperToolImpl();

        Object wrapper  = wrapperTool.wrap(jaxbClass, nameList, objectList);
        if (log.isDebugEnabled()) {
            log.debug("end: Create Doc Lit Wrapper");
        }
        return wrapper;
    }
}
