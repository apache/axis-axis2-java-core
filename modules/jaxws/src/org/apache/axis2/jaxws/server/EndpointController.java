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

import javax.xml.ws.Provider;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.jaxws.param.ParameterFactory;


public class EndpointController {
	private static String PARAM_SERVICE_CLASS = "ServiceClass";
	private MessageContext msgContext = null;
    
    /**
     * @param _msgContext
     */
    public EndpointController(MessageContext _msgContext) {
		this.msgContext = _msgContext;
	}

	/**
     * Get OMElement from message context
     * 
     * @return ome
     */
    private OMElement getOMElement() throws Exception{
        SOAPEnvelope env = msgContext.getEnvelope();
        SOAPBody body = env.getBody();
        OMElement ome = body.getFirstElement();
       
        return ome;
    }
    
    /**
     * Given a MessageContext, get the contents out and turn them into a Parameter
     * instance based on what the Provider expects.
     * 
     * @param msgContext
     * @return
     */
    private org.apache.axis2.jaxws.param.Parameter getParam(Class _class) throws Exception{
        Class<?> clazz = getClassType(_class);  
        org.apache.axis2.jaxws.param.Parameter param = ParameterFactory.createParameter(clazz);        
        return param;
    }
	
	/**
	 * Get the appropriate dispatcher for a given service endpoint.
	 * 
	 * @return EndpointDispatcher
	 * @throws Exception
	 */
	public EndpointDispatcher getDispatcher() throws Exception {
		EndpointDispatcher dispatcherInstance = null;
    	AxisService as = msgContext.getAxisService();
    	Parameter asp = as.getParameter(PARAM_SERVICE_CLASS);
    	
    	Class cls = getImplClass(as,asp);
    	if(cls.getSuperclass().isInstance(Provider.class)){
    		ProviderDispatcher pd = new ProviderDispatcher(cls);
    		if(pd.getProvider() != null){
    			org.apache.axis2.jaxws.param.Parameter param = getParam(cls);
    			param.fromOM(getOMElement());
    			pd.setParam(param);
    		}
    		dispatcherInstance = pd;
    	}
    	
    	return dispatcherInstance;
    }
	
	/**
	 * @param as
	 * @param asp
	 * @return Class
	 */
	private Class getImplClass(AxisService as, Parameter asp){
		Class _class = null;
		try{
			String className = ((String) asp.getValue()).trim();
			_class = Class.forName(className, true, as.getClassLoader());
			
		}catch(java.lang.ClassNotFoundException cnf ){
			cnf.printStackTrace();
		}
		
		return _class;
	}
	
    /**
     * 
     * @param _class
     * @return
     * @throws Exception
     */
    private Class<?> getClassType(Class _class)throws Exception{

    	Class classType = null;
    	
    	Type[] giTypes = _class.getGenericInterfaces();
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
    			classType = (Class)paramType.getActualTypeArguments()[0];
    		}
    	}
        return classType;
    }

}
