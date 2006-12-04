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
import javax.xml.ws.WebServiceException;

import org.apache.axis2.jaxws.i18n.Messages;

public class PortInfoImpl implements PortData {
	private QName serviceName = null;
	private QName portName = null;
	private String bindingId = null;
	private String serviceEndpoint = null;
	

	/**
	 * @param serviceName
	 * @param portName
	 * @param bindingId
	 * @param serviceEndpoint
	 */
	public PortInfoImpl(QName serviceName, QName portName, String bindingId, String serviceEndpoint) {
		super();
		if (serviceName == null) {
			throw new WebServiceException(Messages.getMessage("portInfoErr0", "<null>"));
		}
		if (portName == null) {
			throw new WebServiceException(Messages.getMessage("portInfoErr1", "<null>"));
		}
		if (bindingId == null) {
			throw new WebServiceException(Messages.getMessage("portInfoErr2", "<null>"));
		}
		this.serviceName = serviceName;
		this.portName = portName;
		this.bindingId = bindingId;
		this.serviceEndpoint = serviceEndpoint;
	}

	public QName getServiceName() {
		return serviceName;
	}

	public QName getPortName() {
		return portName;
	}

	public String getBindingID() {
		return bindingId;
	}
	
	public String getEndpointAddress(){
		return serviceEndpoint;
	}

	/* TODO:  I don't think we need the setters, let's leave the commented for now...

    public void setServiceName(QName serviceName){
        if (serviceName == null)
            throw new RuntimeException("serviceName cannot be null");
        this.serviceName = serviceName;
    }
    
	
	public void setPortName(QName portName){
		if (portName == null)
			throw new RuntimeException("portName cannot be null");
		this.portName = portName;
	}
	
	public void setBindingID(String bindingId){
		if (bindingId == null)
			throw new RuntimeException("bindingId cannot be null");
		this.bindingId = bindingId;
	}
	
	public void setEndPointAddress(String serviceEndpoint){
		if (serviceEndpoint == null)
			throw new RuntimeException("serviceEndpoint cannot be null");
		this.serviceEndpoint = serviceEndpoint;
	}
	*/
	

	/*
	 * PortInfo may be used as a key in a HandlerResolver Map cache, so
	 * let's override Object.equals and Object.hashcode
	 */
	public boolean equals(Object obj) {
		if (obj instanceof PortData) {
			PortData info = (PortData) obj;
			if (bindingId.equals(info.getBindingID())
					&& portName.equals(info.getPortName())
					&& serviceName.equals(info.getServiceName())) {
				return true;
			}
		}
		return false;
	}
	
	/*
	 * PortInfo is used as a key in the HandlerResolver cache object, so
	 * we must override Object.equals and Object.hashcode (just use someone
	 * else's hashcode that we know works).
	 */
	public int hashCode() {
		return bindingId.hashCode();
	}
}
