/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

package org.apache.axis.clientapi;

import javax.xml.namespace.QName;

import org.apache.axis.context.EngineContextFactory;
import org.apache.axis.context.ConfigurationContext;
import org.apache.axis.context.ServiceContext;
import org.apache.axis.context.MessageContext;
import org.apache.axis.deployment.DeploymentException;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.description.ServiceDescription;
import org.apache.axis.description.OperationDescription;
import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.addressing.AddressingConstants;
import org.apache.axis.soap.SOAPEnvelope;
import org.apache.axis.om.OMAbstractFactory;
import org.apache.axis.om.OMElement;

/**
 * @author chathura@opensource.lk
 *
 */
public abstract class Stub {
	
	protected ConfigurationContext _configurationContext;
	protected static ServiceDescription _service;
	protected ServiceContext _serviceContext;
    protected EndpointReference toEPR ;



	/**
	 * If _maintainSession is set to True all the calls will use the same 
	 * ServiceContext and the user can Share information through that 
	 * ServiceContext across operations.
	 */
	protected boolean _maintainSession = false;
	protected String _currentSessionId = null;
	
	
	protected Stub()throws DeploymentException, AxisFault{

	}
	
//	public abstract void _setSessionInfo(Object key, Object value) throws Exception;
//
//	public abstract Object _getSessionInfo(Object key) throws Exception ;

     public void _setSessionInfo(Object key, Object value)throws java.lang.Exception{
		if(!_maintainSession){
			//TODO Comeup with a Exception
			throw new java.lang.Exception("Client is running the session OFF mode: Start session before saving to a session ");
		}
		_configurationContext.getServiceContext(_currentSessionId).setProperty(key, value);
	}


	public Object _getSessionInfo(Object key) throws java.lang.Exception{
		if(!_maintainSession){
			//TODO Comeup with a Exception
			throw new java.lang.Exception("Client is running the session OFF mode: Start session before saving to a session ");
		}
		return _configurationContext.getServiceContext(_currentSessionId).getProperty(key);
	}

	public void _startSession(){
		_maintainSession = true;
		_currentSessionId = getID() ;
	}
	
	public void _endSession(){
		_maintainSession = false;
	}
	
	protected String _getServiceContextID(){
		if(_maintainSession)
			return _currentSessionId;
		else
			return getID();
	}
	
	private String getID(){
		//TODO Get the UUID generator to generate values
		return Long.toString(System.currentTimeMillis());
	}

    protected SOAPEnvelope createEnvelope(OMElement omElement){
        SOAPEnvelope env = OMAbstractFactory.getSOAP11Factory().getDefaultEnvelope();
        env.getBody().addChild(omElement);
        return env;
    }
     /**
     * get the message context
     */
    protected MessageContext getMessageContext() throws AxisFault {
            return new MessageContext(null,null,null,_configurationContext);
    }
}

