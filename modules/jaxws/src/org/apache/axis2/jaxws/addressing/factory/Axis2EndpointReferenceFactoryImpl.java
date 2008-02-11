/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *      
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.axis2.jaxws.addressing.factory;

import java.net.URL;

import javax.xml.namespace.QName;

import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.EndpointReferenceHelper;
import org.apache.axis2.addressing.metadata.ServiceName;
import org.apache.axis2.addressing.metadata.WSDLLocation;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.addressing.util.EndpointKey;
import org.apache.axis2.jaxws.addressing.util.EndpointMap;
import org.apache.axis2.jaxws.addressing.util.EndpointMapManager;
import org.apache.axis2.jaxws.util.WSDL4JWrapper;
import org.apache.axis2.jaxws.util.WSDLWrapper;

public class Axis2EndpointReferenceFactoryImpl implements Axis2EndpointReferenceFactory {
    public Axis2EndpointReferenceFactoryImpl() {
    	super();
    }
    
    public EndpointReference createEndpointReference(String address) {
        if (address == null)
            throw new IllegalStateException("The endpoint address URI is null.");

        return new EndpointReference(address);
    }
    
    public EndpointReference createEndpointReference(QName serviceName, QName endpoint) {
        EndpointKey key = new EndpointKey(serviceName, endpoint);
        EndpointMap map = EndpointMapManager.getEndpointMap();
        String address = map.get(key);
        
        return createEndpointReference(address);
    }
    
    public EndpointReference createEndpointReference(String address, QName serviceName, QName portName, String wsdlDocumentLocation, String addressingNamespace) {
        EndpointReference axis2EPR = null;
        
        if (address != null) {
            //TODO NLS enable.
        	if (serviceName == null && portName != null)
                throw new IllegalStateException("Cannot create an endpoint reference because the service name is null, and the port name is not null.");
        		
            axis2EPR = createEndpointReference(address);
        }
        else if (serviceName != null && portName != null) {
            axis2EPR = createEndpointReference(serviceName, portName);
        }
        else {
            //TODO NLS enable.
            throw new IllegalStateException("Cannot create an endpoint reference because the address, service name, and/or port name are null.");
        }
        
        try {
            //TODO If no service name and port name are specified, but the wsdl location is
            //specified, and the WSDL only contains one service and one port then maybe we
            //should simply use those.
            if (serviceName != null && portName != null) {
                ServiceName service = new ServiceName(serviceName, portName.getLocalPart());
                EndpointReferenceHelper.setServiceNameMetadata(axis2EPR, addressingNamespace, service);
            }

            if (wsdlDocumentLocation != null) {
            	URL wsdlURL = new URL(wsdlDocumentLocation);
            	WSDLWrapper wrapper = new WSDL4JWrapper(wsdlURL);
            	
            	if (serviceName != null) {
            		//TODO NLS
            		if (wrapper.getService(serviceName) == null)
            			throw new IllegalStateException("The specified service name does not exist in the WSDL from the specified location.");
                	
                	if (portName != null) {
                		String[] ports = wrapper.getPorts(serviceName);
                		String portLocalName = portName.getLocalPart();
                		boolean found = false;
                		
                		if (ports != null) {
                			for (String port : ports) {
                				if (port.equals(portLocalName)) {
                					found = true;
                					break;
                				}
                			}
                		}
                		
                		//TODO NLS
                		if (!found)
                			throw new IllegalStateException("The specified port name does not exist in the specified WSDL service.");
                	}
            	}
            	
                WSDLLocation wsdlLocation = new WSDLLocation(portName.getNamespaceURI(), wsdlDocumentLocation);
                EndpointReferenceHelper.setWSDLLocationMetadata(axis2EPR, addressingNamespace, wsdlLocation);
            }
        }
        catch (IllegalStateException ise) {
        	throw ise;
        }
        catch (Exception e) {
            //TODO NLS enable.
            throw ExceptionFactory.makeWebServiceException("A problem occured during the creation of an endpoint reference. See the nested exception for details.", e);
        }
        
        return axis2EPR;
    }
}
