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
package org.apache.axis2.jaxws.server.endpoint.lifecycle.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.xml.ws.WebServiceContext;

import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.context.WebServiceContextImpl;
import org.apache.axis2.jaxws.context.factory.MessageContextFactory;
import org.apache.axis2.jaxws.context.utils.ContextUtils;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.handler.SoapMessageContext;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.runtime.description.injection.ResourceInjectionServiceRuntimeDescription;
import org.apache.axis2.jaxws.runtime.description.injection.ResourceInjectionServiceRuntimeDescriptionFactory;
import org.apache.axis2.jaxws.server.endpoint.injection.ResourceInjector;
import org.apache.axis2.jaxws.server.endpoint.injection.WebServiceContextInjector;
import org.apache.axis2.jaxws.server.endpoint.injection.factory.ResourceInjectionFactory;
import org.apache.axis2.jaxws.server.endpoint.injection.impl.ResourceInjectionException;
import org.apache.axis2.jaxws.server.endpoint.lifecycle.EndpointLifecycleException;
import org.apache.axis2.jaxws.server.endpoint.lifecycle.EndpointLifecycleManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class EndpointLifecycleManagerImpl implements EndpointLifecycleManager {
	public static final String WEBSERVICE_MESSAGE_CONTEXT = "javax.xml.ws.WebServiceContext";
	private static final Log log = LogFactory.getLog(EndpointLifecycleManagerImpl.class);
	private Object endpointInstance = null;
	private Class endpointClazz = null;
    
	public EndpointLifecycleManagerImpl(Object endpointInstance) {
		super();
		this.endpointInstance = endpointInstance;
		if(endpointInstance != null){
			endpointClazz = endpointInstance.getClass();
		}
	}
	
	public EndpointLifecycleManagerImpl() {
		super();
	}
	
	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.server.endpoint.lifecycle.EndpointLifecycleManager#createServiceInstance(org.apache.axis2.jaxws.core.MessageContext, java.lang.Class)
	 */
	public Object createServiceInstance(MessageContext mc, Class serviceImplClass) throws EndpointLifecycleException, ResourceInjectionException{
		org.apache.axis2.context.MessageContext msgContext = mc.getAxisMessageContext();
        
		// Get the ServiceDescription and injectionDesc which contain
        // cached information
        ServiceDescription serviceDesc = mc.getEndpointDescription().getServiceDescription();
        ResourceInjectionServiceRuntimeDescription injectionDesc = null;
        if (serviceDesc != null) {
            injectionDesc = 
                ResourceInjectionServiceRuntimeDescriptionFactory.get(serviceDesc, serviceImplClass);
        }
        
        // See if there is an existing service object
        ServiceContext serviceContext = msgContext.getServiceContext();
        Object serviceimpl = serviceContext.getProperty(ServiceContext.SERVICE_OBJECT);
        if (serviceimpl != null) {
            this.endpointInstance = serviceimpl;
            this.endpointClazz = serviceImplClass;
            
        	if (log.isDebugEnabled()) {
                log.debug("Service Instance found in the service context, reusing the instance");
            }
            
            // If resource injection is needed, create the SOAPMessageContext and update the WebServiceContext
        	// Create MessageContext for current invocation.
            if (injectionDesc != null && injectionDesc.hasResourceAnnotation()) {
                javax.xml.ws.handler.MessageContext soapMessageContext = createSOAPMessageContext(mc);
                //Get WebServiceContext from ServiceContext
                WebServiceContext ws = (WebServiceContext)serviceContext.getProperty(WEBSERVICE_MESSAGE_CONTEXT);
                //Add the MessageContext for current invocation
                if(ws !=null){
                    updateWebServiceContext(ws, soapMessageContext);
                }
            }
            
        	 //since service impl is there in service context , take that from there
            return serviceimpl;
        } else {
            // create a new service impl class for that service
            serviceimpl = createServiceInstance(serviceImplClass);
            this.endpointInstance = serviceimpl;
            this.endpointClazz = serviceImplClass;
            if (log.isDebugEnabled()) {
                log.debug("New Service Instance created");
            }
            
            // If resource injection is needed, create the SOAPMessageContext and build the WebServiceContext
            // Create MessageContext for current invocation.
            if (injectionDesc != null && injectionDesc.hasResourceAnnotation()) {
                javax.xml.ws.handler.MessageContext soapMessageContext = createSOAPMessageContext(mc);
                // Create WebServiceContext
                WebServiceContextImpl wsContext = new WebServiceContextImpl();
                //Add MessageContext for this request.
                wsContext.setSoapMessageContext(soapMessageContext);
                // Inject WebServiceContext
                injectWebServiceContext(mc, wsContext, serviceimpl);
                serviceContext.setProperty(WEBSERVICE_MESSAGE_CONTEXT, wsContext);
                
            }
            
            //Invoke PostConstruct
            if (injectionDesc != null && injectionDesc.getPostConstructMethod() != null) {
                invokePostConstruct(injectionDesc.getPostConstructMethod());
            }
            serviceContext.setProperty(ServiceContext.SERVICE_OBJECT, serviceimpl);
            return serviceimpl;
        }
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.server.endpoint.lifecycle.EndpointLifecycleManager#invokePostConstruct()
	 */
	public void invokePostConstruct() throws EndpointLifecycleException{
		if(endpointInstance == null){
			throw new EndpointLifecycleException(Messages.getMessage("EndpointLifecycleManagerImplErr1"));
		}
		Method method = getPostConstructMethod();
		if(method != null){
			invokePostConstruct(method);
		}
	}
    
    private void invokePostConstruct(Method method) throws EndpointLifecycleException {
        if(log.isDebugEnabled()){
            log.debug("Invoking Method with @PostConstruct annotation");
        }
        invokeMethod(method, null);
        if(log.isDebugEnabled()){
            log.debug("Completed invoke on Method with @PostConstruct annotation");
        }
    }

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.server.endpoint.lifecycle.EndpointLifecycleManager#invokePreDestroy()
	 */
	public void invokePreDestroy() throws EndpointLifecycleException {
		if(endpointInstance == null){
			throw new EndpointLifecycleException(Messages.getMessage("EndpointLifecycleManagerImplErr1"));
		}
		Method method = getPreDestroyMethod();
		if(method != null){
			invokePreDestroy(method);
		}
	}
    
    private void invokePreDestroy(Method method) throws EndpointLifecycleException {
        if(log.isDebugEnabled()){
            log.debug("Invoking Method with @PreDestroy annotation");
        }
        invokeMethod(method, null);
        if(log.isDebugEnabled()){
            log.debug("Completed invoke on Method with @PreDestroy annotation");
        }
    }
	
	private void invokeMethod(Method m, Object[] params) throws EndpointLifecycleException{
		try{
			m.invoke(endpointInstance, params);
		}catch(InvocationTargetException e){
			throw new EndpointLifecycleException(e);
		}catch(IllegalAccessException e){
			throw new EndpointLifecycleException(e);
		}
	}
	
	private Method getPostConstructMethod(){
	    // REVIEW: This method should not be called in performant situations.
        // Plus the super class methods are not being considered 
        
		//return Method with @PostConstruct Annotation.
		if(endpointInstance != null){
			Class endpointClazz = endpointInstance.getClass();
			Method[] methods = endpointClazz.getMethods();
			
			for(Method method:methods){
				if(isPostConstruct(method)){
					return method;
				}
			}
		}
		return null;
	}
	
	private Method getPreDestroyMethod(){
        // REVIEW: This method should not be called in performant situations.
        // Plus the super class methods are not being considered 
		//return Method with @PreDestroy Annotation
		if(endpointInstance != null){
			Class endpointClazz = endpointInstance.getClass();
			Method[] methods = endpointClazz.getMethods();
			
			for(Method method:methods){
				if(isPreDestroy(method)){
					return method;
				}
			}
		}
		return null;
	}
	
	private boolean isPostConstruct(Method method){
		Annotation[] annotations = method.getDeclaredAnnotations();
		for(Annotation annotation:annotations){
			return PostConstruct.class.isAssignableFrom(annotation.annotationType());
		}
		return false;
	}
	
	private boolean isPreDestroy(Method method){
		Annotation[] annotations = method.getDeclaredAnnotations();
		for(Annotation annotation:annotations){
			return PreDestroy.class.isAssignableFrom(annotation.annotationType());
		}
		return false;
	}
	
	 private Object createServiceInstance(Class serviceImplClass) {
	        if (log.isDebugEnabled()) {
	            log.debug("Creating new instance of service endpoint");
	        }
	        
	        if (serviceImplClass == null) {
	            throw ExceptionFactory.makeWebServiceException(Messages.getMessage(
	                    "EndpointControllerErr5"));
	        }
	        
	        Object instance = null;
	        try {
	            instance = serviceImplClass.newInstance();
	        } catch (IllegalAccessException e) {
	            throw ExceptionFactory.makeWebServiceException(Messages.getMessage(
	                    "EndpointControllerErr6", serviceImplClass.getName()));
	        } catch (InstantiationException e) {
	            throw ExceptionFactory.makeWebServiceException(Messages.getMessage(
	                    "EndpointControllerErr6", serviceImplClass.getName()));
	        }
	        
	        return instance;
	   }
	
	 private javax.xml.ws.handler.MessageContext createSOAPMessageContext(MessageContext mc){
		SoapMessageContext soapMessageContext = (SoapMessageContext)MessageContextFactory.createSoapMessageContext(mc);
		ContextUtils.addProperties(soapMessageContext, mc);
		return soapMessageContext;
	 }
	 
	private void injectWebServiceContext(MessageContext mc, WebServiceContext wsContext, Object serviceInstance) throws ResourceInjectionException{
       ResourceInjector ri =ResourceInjectionFactory.createResourceInjector(WebServiceContext.class);
       ri.inject(wsContext, serviceInstance);   
	}
	   
	private void updateWebServiceContext(WebServiceContext wsContext, javax.xml.ws.handler.MessageContext soapMessageContext) throws ResourceInjectionException{
	   WebServiceContextInjector wci =(WebServiceContextInjector)ResourceInjectionFactory.createResourceInjector(WebServiceContext.class);
	   wci.addMessageContext(wsContext, soapMessageContext);
		   
	}
	   
}
