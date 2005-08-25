/*
 * Copyright 2004,2005 The Apache Software Foundation.
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

package org.apache.axis2.security.handler;

import javax.xml.namespace.QName;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.description.OperationDescription;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.ServiceDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.Handler;
import org.apache.axis2.security.util.WSHandlerConstantsMapper;
import org.apache.ws.security.handler.RequestData;
import org.apache.ws.security.handler.WSHandler;

/**
 * Class WSDoAllHandler
 */
public abstract class WSDoAllHandler extends WSHandler implements Handler {

    /**
     * Field EMPTY_HANDLER_METADATA
     */
    private static HandlerDescription EMPTY_HANDLER_METADATA =
            new HandlerDescription(new QName("deafult Handler"));

    private final static String WSS_PASSWORD = "password";
    
    private final static String WSS_USERNAME = "username";
    
    /**
     * Field handlerDesc
     */
    protected HandlerDescription handlerDesc;
    
    /**
     * This is used to get hold of the message context to extract the
     * configuration information (from axis2.xml and service.xml)
     * out of it 
     */
    protected RequestData reqData;
    
    /**
     * In Axis2 the user cannot set inflow and outflow parameters
     * Therefore we need to map the Axis2 specific inflow and outflow 
     * parameters to WSS4J params
     * 
     * Knowledge of inhandler and out handler is used to get the mapped value
     */
    protected boolean inHandler;
    
    /**
     * Constructor AbstractHandler
     */
    public WSDoAllHandler() {
        handlerDesc = EMPTY_HANDLER_METADATA;
    }

    /**
     * Method getName
     *
     * @return name
     */
    public QName getName() {
        return handlerDesc.getName();
    }

    /**
     * Method revoke
     *
     * @param msgContext
     */
    public void revoke(MessageContext msgContext) {
    }

    /**
     * Method cleanup
     *
     * @throws org.apache.axis2.AxisFault
     */
    public void cleanup() throws AxisFault {
    }

    /**
     * Method getParameter
     *
     * @param name
     * @return parameter
     */
    public Parameter getParameter(String name) {
        return handlerDesc.getParameter(name);
    }

    /**
     * Method init
     *
     * @param handlerdesc
     */
    public void init(HandlerDescription handlerdesc) {
        this.handlerDesc = handlerdesc;
    }

    /**
     * To get the phaseRule of a handler it is required to get the HnadlerDescription of the handler
     * so the argumnet pass when it call return as HnadlerDescription
     *
     * @return handler description
     */
    public HandlerDescription getHandlerDesc() {
        return handlerDesc;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        QName name = this.getName();
        return (name != null) ? name.toString() : null;
    }


    public Object getProperty(Object msgContext, String axisKey) {
    	String key = WSHandlerConstantsMapper.getMapping(axisKey,inHandler);
        return ((MessageContext)msgContext).getProperty(key);
    }

    public String getPassword(Object msgContext) {
        return (String)((MessageContext)msgContext).getProperty(WSS_PASSWORD);
    }

    public void setPassword(Object msgContext, String password) {
        ((MessageContext)msgContext).setProperty(WSS_PASSWORD,password);
    }
    
    public String getUsername(Object msgContext) {
        return (String)((MessageContext)msgContext).getProperty(WSS_USERNAME);
    }

    public void setUsername(Object msgContext, String username) {
        ((MessageContext)msgContext).setProperty(WSS_USERNAME,username);
    }
    
	/**
	 * Get optoin should extract the configuration 
	 * values from the service.xml and/or axis2.xml
	 * Values set in the service.xml takes prority over values of the
	 * axis2.xml
	 */
    public Object getOption(String axisKey) {
    	
    	String key  = WSHandlerConstantsMapper.getMapping(axisKey,inHandler);
    	
    	MessageContext msgContext = (MessageContext)this.reqData.getMsgContext();
    	
    	//If the parameters are set globally in the axis2.xml 
    	AxisConfiguration axisConfig = msgContext.getSystemContext().getAxisConfiguration();
    	
    	//If the parameters are set in the scope of the service in service.xml
    	ServiceDescription serviceDesc = msgContext.getServiceContext().getServiceConfig();
    	
    	//If the parameters are set in the scope of an peration in service.xml
    	OperationDescription operationDesc = msgContext.getOperationContext().getAxisOperation();
    	
    	Object value = null;
    	
    	//if the operation desc is available
    	if(operationDesc != null) {
    		Parameter parameter = operationDesc.getParameter(key);
			value = (parameter!=null)?parameter.getValue():null; 
    	}
    	
    	//If the parameter is not found in the operation desc and if the 
    	//service desc is available
    	if(value == null && serviceDesc != null) {
    		Parameter parameter = serviceDesc.getParameter(key);
			value = (parameter!=null)?parameter.getValue():null;
    	}
    		
    	//If the parameter is not found in the service desc the look at the 
    	//global config - axis config
    	if(value == null && axisConfig != null) {
    		Parameter parameter = axisConfig.getParameter(key);
			value = (parameter!=null)?parameter.getValue():null;
    	}
    	
    	//---------------------------------------------------------------------
    	//If value is still null this point then the user has not set the value
    	  
    	
    	//Look in the handlerDesc for the value
    	if(value == null) {
    		Parameter parameter = this.handlerDesc.getParameter(key);
    		value = (parameter== null)?null:parameter.getValue();
    	}
    	
    	return value;
    }
}
