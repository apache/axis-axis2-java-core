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

package org.apache.axis2.jaxws;

import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

import javax.xml.bind.JAXBContext;
import javax.xml.ws.Service.Mode;

import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.handler.PortData;
import org.apache.axis2.jaxws.util.WSDLWrapper;


public class JAXWSClientContext<T> {
	private PortData port = null;
	private Mode serviceMode = null;
	private ExecutorService executor = null;
    private ServiceDescription serviceDescription;
	private Class<T> clazz = null; //SEI class for Proxy or Implementation type for Dispatch
	private JAXBContext jaxbContext = null;
    
	public ExecutorService getExecutor() {
		return executor;
	}
	public void setExecutor(Executor executor) {
		this.executor = (ExecutorService)executor;
	}
	public PortData getPort() {
		return port;
	}
	public void setPort(PortData port) {
		this.port = port;
	}
	public Mode getServiceMode() {
		return serviceMode;
	}
	public void setServiceMode(Mode serviceMode) {
		this.serviceMode = serviceMode;
	}
	public ServiceDescription getServiceDescription() {
        return serviceDescription;
    }
    public void setServiceDescription(ServiceDescription serviceDescription) {
        this.serviceDescription = serviceDescription;
    }
    public WSDLWrapper getWsdlContext() {
		return (serviceDescription != null) ? serviceDescription.getWSDLWrapper() : null;
	}
    public URL getWSDLLocation(){
		return (serviceDescription != null) ? serviceDescription.getWSDLLocation() : null; 	
	}
	public Class<T> getClazz() {
		return clazz;
	}
	public void setClazz(Class<T> clazz) {
		this.clazz = clazz;
	}
    
    public JAXBContext getJAXBContext() {
        return jaxbContext;
    }
    
    public void setJAXBContext(JAXBContext jbc) {
        jaxbContext = jbc;
    }
	
}
