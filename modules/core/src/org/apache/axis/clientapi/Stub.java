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
import org.apache.axis.deployment.DeploymentException;
import org.apache.axis.engine.AxisFault;

/**
 * @author chathura@opensource.lk
 *
 */
public abstract class Stub {
	
	protected org.apache.axis.context.ConfigurationContext _configurationContext;
	protected org.apache.axis.description.ServiceDescription _service;
	protected static org.apache.axis.description.OperationDescription[] _operations;
	
	/**
	 * If _maintainSession is set to True all the calls will use the same 
	 * ServiceContext and the user can Share information through that 
	 * ServiceContext across operations.
	 */
	protected boolean _maintainSession = false;
	protected String _currentSessionId = null;
	
	
	protected Stub(QName serviceName, String axis2Home)throws DeploymentException, AxisFault{
		_configurationContext = new EngineContextFactory().buildClientEngineContext(axis2Home);
		_service = new org.apache.axis.description.ServiceDescription();		
		_service.setName(serviceName);
		
		for (int i = 0; i < _operations.length; i++) {
			_service.addOperation(_operations[i]);
		}
		
		_configurationContext.getEngineConfig().addService(_service);
	}
	
	public abstract void _setSessionInfo(Object key, Object value) throws Exception;
	
	public abstract Object _getSessionInfo(Object key) throws Exception ;
	
	
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
}
