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
package org.apache.wsdl.impl;

import org.apache.wsdl.WSDLEndpoint;
import org.apache.wsdl.WSDLInterface;
import org.apache.wsdl.WSDLService;

import javax.xml.namespace.QName;
import java.util.HashMap;


/**
 * @author chathura@opensource.lk
 */
public class WSDLServiceImpl extends ComponentImpl implements WSDLService {

    /**
     * The QName that identifies the Service. This namespace of the QName
     * should be the target namespace defined in the Definitions component.
     */
    private QName name;


    /**
     * The Interface that this Service is an instance of.
     */
    private WSDLInterface serviceInterface;

    /**
     * 
     */
    private HashMap endpoints = new HashMap();


    public HashMap getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(HashMap endpoints) {
        this.endpoints = endpoints;
    }

    /**
     * Will add a WSDLEndpoint object to the WOM keyed with qname;
     */
    public void setEndpoint(WSDLEndpoint endpoint) {
        this.endpoints.put(endpoint.getName(), endpoint);
    }

    /**
     * Endpoint will be retrived by its qname.
     *
     * @param qname qname of the Service
     * @return WSDLService Object or will throw an WSDLProcessingException in the case of object not found.
     */
    public WSDLService getEndpoint(QName qName) {
        WSDLService temp = (WSDLService) this.endpoints.get(qName);
        if (null == temp) throw new WSDLProcessingException("Service not found for NCName " + qName);
        return temp;
    }

    public QName getName() {
        return name;
    }

    public void setName(QName name) {
        this.name = name;
    }

    /**
     * If the Name of the <code>WSDLService</code> is not set a
     * <code>WSDLProcessingException</code> will be thrown.
     *
     * @return Target Namespace as a <code>String</code>
     */
    public String getNamespace() {
        if (null == this.name) throw new WSDLProcessingException("Target Namespace not set and the Service Name is null");

        return this.name.getNamespaceURI();
    }

    public WSDLInterface getServiceInterface() {
        return serviceInterface;
    }

    public void setServiceInterface(WSDLInterface serviceInterface) {
        this.serviceInterface = serviceInterface;
    }
}
