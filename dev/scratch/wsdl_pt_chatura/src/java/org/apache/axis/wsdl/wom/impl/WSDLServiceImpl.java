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
package org.apache.axis.wsdl.wom.impl;

import java.net.URI;
import java.util.HashMap;

import org.apache.axis.wsdl.wom.WSDLEndpoint;
import org.apache.axis.wsdl.wom.WSDLInterface;
import org.apache.axis.wsdl.wom.WSDLService;

/**
 * @author chathura@opensource.lk
 *
 */
public class WSDLServiceImpl implements  WSDLService {

    /**
     * The NCName that identifies the Service.
     */
    private String name;
    
    /**
     * Namespace of the target namespace of the Definition Component's targetNamespace 
     * attrebute information item.
     */
    private URI namespaceURI;
    
    /**
     * The Interface that this Service is an instance of.
     */
    private WSDLInterface serviceInterface;
    
    /**
     * 
     */
    private HashMap endpoints;
    
    
    public HashMap getEndpoints() {
        return endpoints;
    }
    public void setEndpoints(HashMap endpoints) {
        this.endpoints = endpoints;
    }
    
    /**
     * Will add a WSDLEndpoint object to the WOM keyed with NCName;
     */
    public void setEndpoint(WSDLEndpoint endpoint, String nCName){
        this.endpoints.put(nCName, endpoint);
    }
    
    /**
	 * Endpoint will be retrived by its NCName.
	 * @param nCName NCName of the Service
	 * @return WSDLService Object or will throw an WSDLProcessingException in the case of object not found. 
	 */
	public WSDLService getService(String nCName){
	    WSDLService temp = (WSDLService)this.endpoints.get(nCName);
	    if(null == temp) throw new WSDLProcessingException("Service not found for NCName "+nCName);
	    return temp;
	}
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public URI getNamespaceURI() {
        return namespaceURI;
    }
    public void setNamespaceURI(URI namespaceURI) {
        this.namespaceURI = namespaceURI;
    }
    public WSDLInterface getServiceInterface() {
        return serviceInterface;
    }
    public void setServiceInterface(WSDLInterface serviceInterface) {
        this.serviceInterface = serviceInterface;
    }
}
