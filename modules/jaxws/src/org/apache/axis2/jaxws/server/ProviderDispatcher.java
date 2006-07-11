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

package org.apache.axis2.jaxws.server;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.ws.Provider;
import org.apache.axis2.jaxws.param.Parameter;
import javax.xml.bind.JAXBContext;

/**
 * From a given service class, determine and invoke the appropriate endpoint with associated parameter.
 *
 */
public class ProviderDispatcher extends EndpointDispatcher{
	private Class svcImplClass = null;
	private Provider providerInstance = null;
	private Parameter parameter = null;

	/**
	 * Constructor
	 * 
	 * @param _class
	 */
	public ProviderDispatcher(Class _class) {
		this.svcImplClass = _class;
	}
	
	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.server.EndpointDispatcher#execute()
	 */
	public Object execute()throws Exception{
		
		Class paramType = getProviderType();
    	Object input = paramType.cast(getParam().getValue());

    	String input1 = getParam().getValue().getClass().getName();
    	System.out.println(">> invoking Provider with param [" + input1 + "]");
		
		return providerInstance.invoke(input);
	}
	
	/**
	 * Get the endpoint provider instance
	 * 
	 * @return Provider
	 * @throws Exception
	 */
	public Provider getProvider() throws Exception{
		Provider p = getProviderInstance();
		setProvider(p);
		return p;
	}
	
	/**
	 * Set the endpoint provider instance
	 * 
	 * @param _provider
	 */
	public void setProvider(Provider _provider) {
		this.providerInstance = _provider;
	}

	/**
	 * Get the parameter for a given endpoint invocation  
	 * 
	 * @return
	 * @throws Exception
	 */
	public Parameter getParam()throws Exception {
		return parameter;
	}

	/**
	 * Set the parameter for a given endpoint invocation
	 * 
	 * @param _parameter
	 */
	public void setParam(Parameter _parameter) {
		this.parameter = _parameter;
	}

	/**
	 * Determine the Provider type for this instance
	 * 
	 * @return Provider
	 * @throws Exception
	 */
	private Provider getProviderInstance()throws Exception{
    	Provider provider = null;
    	Class<?> clazz =getProviderType();
    	if(!isValidProviderType(clazz)){
    		//TODO This will change once deployment code it in place
    		throw new Exception("Invalid Provider Implementation, Only String, Source, SOAPMessage and JAXBContext Supported by JAX-WS ");
    	}
    	if(clazz == String.class){
    		return (Provider<String>) this.svcImplClass.newInstance();
    	}
    	if(clazz == Source.class){
    		return (Provider<Source>) this.svcImplClass.newInstance();
    	}
    	if(clazz == SOAPMessage.class){
    		return (Provider<SOAPMessage>) this.svcImplClass.newInstance();
    	}
    	if(clazz == JAXBContext.class){
    		return (Provider<JAXBContext>)this.svcImplClass.newInstance();
    	}
    	
    	return provider;
    	
    }
    
    /**
     * Get the provider type from a given implemention class instance
     * 
     * @return class
     * @throws Exception
     */
    private Class<?> getProviderType()throws Exception{

    	Class providerType = null;
    	
    	Type[] giTypes = this.svcImplClass.getGenericInterfaces();
    	for(Type giType : giTypes){
    		ParameterizedType paramType = null;
    		try{
    			paramType = (ParameterizedType)giType;
    		}catch(ClassCastException e){
    			throw new Exception("Provider based SEI Class has to implement javax.xml.ws.Provider as javax.xml.ws.Provider<String>, javax.xml.ws.Provider<SOAPMessage>, javax.xml.ws.Provider<Source> or javax.xml.ws.Provider<JAXBContext>");
    		}
    		Class interfaceName = (Class)paramType.getRawType();
    		System.out.println(">> Intereface name is [" + interfaceName.getName() + "]");
    		
    		if(interfaceName == javax.xml.ws.Provider.class){
    			if(paramType.getActualTypeArguments().length > 1){
    				throw new Exception("Provider cannot have more than one Generic Types defined as Per JAX-WS Specification");
    			}
    			providerType = (Class)paramType.getActualTypeArguments()[0];
    		}
    	}
        return providerType;
    }
    
    /**
     * Validate provider type against require types for the Provider interface.
     * 
     * @param clazz
     * @return boolean
     */
    private boolean isValidProviderType(Class clazz){	
    	return clazz == String.class || clazz == SOAPMessage.class || clazz == Source.class;
    }

}
