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
import javax.xml.ws.Service.Mode;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.jaxws.param.Parameter;
import org.apache.axis2.jaxws.param.ParameterFactory;
import org.apache.axis2.util.Utils;


/**
 * The JAXWSMessageReceiver is the entry point, from the server's perspective,
 * to the JAX-WS code.  This will be called by the Axis Engine and is the end
 * of the chain from an Axis2 perspective.
 */
public class JAXWSMessageReceiver implements MessageReceiver {

    private static String PARAM_SERVICE_CLASS = "ServiceClass";
    
    private String className;
    private Class implClass;
    private Provider endpointInstance;
    private Class providerType = null;
    
    /**
     * We should have already determined which AxisService we're targetting at
     * this point.  So now, just get the service implementation and invoke
     * the appropriate method.
     */
    public void receive(MessageContext reqMsgContext) throws AxisFault {
        // Let's go ahead and create the MessageContext for the response and add
        // it to the list.
        MessageContext rspMsgContext = Utils.createOutMessageContext(reqMsgContext);
        reqMsgContext.getOperationContext().addMessageContext(rspMsgContext);
        
        // Get the name of the service impl that was stored as a parameter
        // inside of the services.xml.
        AxisService service = reqMsgContext.getAxisService();
        org.apache.axis2.description.Parameter svcClassParam = service.getParameter(PARAM_SERVICE_CLASS);
        
        // Create an instance of the Provider class that will be dispatched to.        
        try {
            if (svcClassParam != null) { 
                className = ((String) svcClassParam.getValue()).trim();
                implClass = Class.forName(className, true, service.getClassLoader());
                endpointInstance = getProviderInstance();
            }
            else {
                throw new RuntimeException("No service class was found for this AxisService");
            }
            
            Parameter param = extractParam(reqMsgContext);
            if (endpointInstance != null) {
            	Class paramType = getProviderType();
            	
            	Object input = paramType.cast(param.getValue());
            	
            	/*
            	 String input = (String) param.getValue();
            	 System.out.println(">> invoking Provider with param [" + input + "]");
                */
            	
                // TODO : Params will not always be Strings.  Add more code to 
                // handle different param types and different numbers of params (not for
                // Provider but for other endpoints)
            	// Added more code to ensure we are handling all param types. NVT
                Object response = endpointInstance.invoke(input);
                
                Parameter rspParam = ParameterFactory.createParameter(response);
                SOAPEnvelope rspEnvelope = rspParam.toEnvelope(Mode.PAYLOAD, null);
                rspMsgContext.setEnvelope(rspEnvelope);
            }
            
            // Create the AxisEngine for the reponse and send it.
            AxisEngine engine = new AxisEngine(rspMsgContext.getConfigurationContext());
            engine.send(rspMsgContext);
        } catch (Exception e) {
        	//TODO: This temp code for alpha till we add fault processing on client code.
        	Exception ex = new Exception("Server Side Exception :" +e.getMessage());
            throw AxisFault.makeFault(ex);
        } 
        //Lets extract jax-ws parameter that we can use to create the response message context. 
        
        
    }
    
    /**
     * Given a MessageContext, get the contents out and turn them into a Parameter
     * instance based on what the Provider expects.
     * 
     * @param msgContext
     * @return
     */
    private Parameter extractParam(MessageContext msgContext) throws Exception{
        SOAPEnvelope env = msgContext.getEnvelope();
        SOAPBody body = env.getBody();
        OMElement om = body.getFirstElement();
        //Using Provider implClass and getting the Invoke method's parameter type, this will be the jax-ws parameter.
        Class<?> clazz = getProviderType();  
        Parameter param = ParameterFactory.createParameter(clazz);
        param.fromOM(om);        
        return param;
    }
    
    private Class<?> getProviderType()throws Exception{
    	if(providerType != null){
    		return providerType;
    	}
    	
    	Type[] giTypes = implClass.getGenericInterfaces();
    	for(Type giType : giTypes){
    		ParameterizedType paramType = null;
    		try{
    			paramType = (ParameterizedType)giType;
    		}catch(ClassCastException e){
    			throw new Exception("Provider based SEI Class has to implement javax.xml.ws.Provider as javax.xml.ws.Provider<String>, javax.xml.ws.Provider<SOAPMessage>, javax.xml.ws.Provider<Source> or javax.xml.ws.Provider<JAXBContext>");
    		}
    		Class interfaceName = (Class)paramType.getRawType();
    		if(interfaceName == javax.xml.ws.Provider.class){
    			if(paramType.getActualTypeArguments().length > 1){
    				throw new Exception("Provider cannot have more than one Generic Types defined as Per JAX-WS Specification");
    			}
    			providerType = (Class)paramType.getActualTypeArguments()[0];
    		}
    	}
        return providerType;
    }
    
    private Provider getProviderInstance()throws Exception{
    	Provider provider = null;
    	Class<?> clazz =getProviderType();
    	if(!isValidProviderType(clazz)){
    		//TODO This will change once deployment code it in place
    		throw new Exception("Invalid Provider Implementation, Only String, Source, SOAPMessage and JAXBContext Supported by JAX-WS ");
    	}
    	if(clazz == String.class){
    		return (Provider<String>) implClass.newInstance();
    	}
    	if(clazz == Source.class){
    		return (Provider<Source>) implClass.newInstance();
    	}
    	if(clazz == SOAPMessage.class){
    		return (Provider<SOAPMessage>) implClass.newInstance();
    	}
    	/* TODO: Wait till we get SUN RI binary for JAXB
    	if(clazz == JAXContext.class){
    		return (Provider<JAXBContext>)implClass.newInstance();
    	}
    	*/
    	return provider;
    	
    }
    
    private boolean isValidProviderType(Class clazz){
    	
    	return clazz == String.class || clazz == SOAPMessage.class || clazz == Source.class;
    	//TODO: clazz == JAXBContext.class
    }
}
