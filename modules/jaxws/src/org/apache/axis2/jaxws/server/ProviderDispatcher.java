/*
 * Copyright 2006 The Apache Software Foundation.
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

import javax.activation.DataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.ws.Provider;

import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.core.util.MessageContextUtils;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.message.Block;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.Protocol;
import org.apache.axis2.jaxws.message.factory.BlockFactory;
import org.apache.axis2.jaxws.message.factory.MessageFactory;
import org.apache.axis2.jaxws.message.factory.SourceBlockFactory;
import org.apache.axis2.jaxws.message.factory.XMLStringBlockFactory;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The ProviderDispatcher is used to invoke instances of a target endpoint
 * that implement the {@link javax.xml.ws.Provider} interface.
 * 
 * The Provider<T> is a generic class, with certain restrictions on the
 * parameterized type T.  This implementation supports the following types:
 * 
 * java.lang.String
 * javax.activation.DataSource
 * javax.xml.soap.SOAPMessage
 * javax.xml.transform.Source
 *
 */
public class ProviderDispatcher extends EndpointDispatcher{
	
    private static Log log = LogFactory.getLog(ProviderDispatcher.class);
    
    private BlockFactory blockFactory = null;
    private Class svcImplClass = null;
	private Class providerType = null;
    private Provider providerInstance = null;
    private Message message = null;
    private Protocol messageProtocol;

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
    public MessageContext invoke(MessageContext mc) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Preparing to invoke javax.xml.ws.Provider based endpoint");
        }

        providerInstance = getProviderInstance();
        
        // First we need to know what kind of Provider instance we're going
        // to be invoking against
        providerType = getProviderType();
        
        // Now that we know what kind of Provider we have, we can create the 
        // right type of Block for the request parameter data
        Object requestParamValue = null;
        Message message = mc.getMessage();
        if (message != null) {
            // Save off the protocol info so we can use it when creating
            // the response message.
            messageProtocol = message.getProtocol();
            
            BlockFactory factory = createBlockFactory(providerType);
            Block block = message.getBodyBlock(0, null, factory);
            requestParamValue = block.getBusinessObject(true);
        }

        Object input = providerType.cast(requestParamValue);

        if (log.isDebugEnabled()) {
            log.debug("Invoking Provider<" + providerType.getName() + "> with " +
                    "parameter of type " + input.getClass().getName());
        }

        // Invoke the actual Provider.invoke() method
        Object responseParamValue = null;
        try {
            responseParamValue = providerInstance.invoke(input);
        } catch (Exception e) {
            e.printStackTrace();
            throw ExceptionFactory.makeWebServiceException(e);
        }

        // Create the response MessageContext from the returned value
        Message responseMsg = createMessageFromValue(responseParamValue);
        MessageContext responseMsgCtx = MessageContextUtils.
            createMessageMessageContext(mc);
        
        responseMsgCtx.setMessage(responseMsg);
        
        return responseMsgCtx;        
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
	public Message getMessage()throws Exception {
		return message;
	}

	/**
	 * Set the parameter for a given endpoint invocation
	 * 
	 * @param _parameter
	 */
	public void setMessage(Message msg) {
		this.message = msg;
	}
    
    /*
     * Create a Message object out of the value object that was returned.
     */
    private Message createMessageFromValue(Object value) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Creating response Message from value of type " + 
                    value.getClass().getName());
        }
        
        BlockFactory factory = createBlockFactory(providerType);
        MessageFactory msgFactory = (MessageFactory) FactoryRegistry.getFactory(
                MessageFactory.class);
        
        Message message = msgFactory.create(messageProtocol);
            
        // Since we know this isn't going to be a JAX-B block, it's ok if
        // the context is null.  The QName can be null for now as well.
        Block block = factory.createFrom(value, null, null);
        message.setBodyBlock(0, block);

        return message;
    }

	/*
	 * Determine the Provider type for this instance
	 */
	private Provider getProviderInstance() throws Exception{
    	Class<?> clazz = getProviderType();
    	
        if(!isValidProviderType(clazz)){
    		//TODO This will change once deployment code it in place
    		throw new Exception(Messages.getMessage("InvalidProvider", clazz.getName()));
    	}
        
        Provider provider = null;
    	if(clazz == String.class){
    		provider = (Provider<String>) this.svcImplClass.newInstance();
    	}
        else if(clazz == Source.class){
    		provider = (Provider<Source>) this.svcImplClass.newInstance();
    	}
        else if(clazz == SOAPMessage.class){
    		provider = (Provider<SOAPMessage>) this.svcImplClass.newInstance();
    	}
        else if(clazz == JAXBContext.class){
    		provider = (Provider<JAXBContext>)this.svcImplClass.newInstance();
    	}
    	
        if (provider == null) {
            throw ExceptionFactory.makeWebServiceException(Messages.getMessage("InvalidProviderCreate", clazz.getName()));
        }
        
    	return provider;
    	
    }
    
    /*
     * Get the provider type from a given implemention class instance
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
    
    /*
     * Validate whether or not the Class passed in is a valid type of 
     * javax.xml.ws.Provider<T>.  Per the JAX-WS 2.0 specification, the 
     * parameterized type of a Provider can only be: 
     * 
     *   javax.xml.transform.Source
     *   javax.xml.soap.SOAPMessage
     *   javax.activation.DataSource
     *   
     * We've also added support for String types which is NOT dictated
     * by the spec.
     */
    private boolean isValidProviderType(Class clazz){	
    	boolean valid = clazz == String.class || 
            clazz == SOAPMessage.class || 
            clazz == Source.class ||
            clazz == DataSource.class;
        
        if (log.isDebugEnabled()) {
            log.debug("Class " + clazz.getName() + " is not a valid Provider<T> type");
        }
        
        return valid; 
    }
    
    /*
     * Given a target class type for a payload, load the appropriate BlockFactory.
     */
    private BlockFactory createBlockFactory(Class type) {
        if (blockFactory != null)
            return blockFactory;
        
        if (type.equals(String.class)) {
            blockFactory = (XMLStringBlockFactory) FactoryRegistry.getFactory(
                    XMLStringBlockFactory.class);
        }
        else if (type.equals(Source.class)) {
            blockFactory = (SourceBlockFactory) FactoryRegistry.getFactory(
                    SourceBlockFactory.class);
        }
        else {
            ExceptionFactory.makeWebServiceException("Unable to find BlockFactory " +
                    "for type: " + type.getClass().getName());
        }
        
        return blockFactory;
    }

}
