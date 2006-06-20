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
package org.apache.axis2.jaxws.handler;

import javax.xml.namespace.QName;

public class PortInfoImpl implements PortData {
	private QName serviceName = null;
	private QName portName = null;
	private String bindingId = null;
	private String serviceEndpoint = null;
	

	public PortInfoImpl(QName serviceName, QName portName, String bindingId, String serviceEndpoint) {
		super();
		// TODO Auto-generated constructor stub
		this.serviceName = serviceName;
		this.portName = portName;
		this.bindingId = bindingId;
		this.serviceEndpoint = serviceEndpoint;
	}

	public QName getServiceName() {
		// TODO Auto-generated method stub
		return serviceName;
	}

	public QName getPortName() {
		// TODO Auto-generated method stub
		return portName;
	}

	public String getBindingID() {
		// TODO Auto-generated method stub
		return bindingId;
	}
	
	public void setServiceName(QName serviceName){
		this.serviceName = serviceName;
	}
	
	public void setPortName(QName portName){
		this.portName = portName;
	}
	
	public void setBindingID(String bindingId){
		this.bindingId = bindingId;
	}
	
	public void setEndPointAddress(String serviceEndpoint){
		this.serviceEndpoint = serviceEndpoint;
	}
	
	public String getEndpointAddress(){
		return serviceEndpoint;
	}
}

