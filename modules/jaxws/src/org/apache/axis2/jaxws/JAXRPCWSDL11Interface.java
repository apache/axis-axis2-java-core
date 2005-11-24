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
package org.apache.axis2.jaxws;

import javax.wsdl.*;
import java.net.URL;
import javax.xml.namespace.QName;

/**
 * @author sunja07
 *
 */
public interface JAXRPCWSDL11Interface extends JAXRPCWSDLInterface {
	
	public Service getService(URL wsdlLocation, QName serviceName);
	
	public Port getPort(Service service, QName portName);
	
	public Port[] getPorts(URL wsdlLocation, QName serviceName);
	
	public Port[] getPorts(Service service);
	
	public String[] getPortNames(URL wsdlLocation, QName serviceName);
	
	public Binding getBinding(URL wsdlLocation, QName portName);
	
	public Binding getBinding(Port port);
	
	public QName getPortTypeName(Binding binding);
	
	public PortType getPortType(URL wsdlLocation, QName portTypeName);
	
	public PortType getPortType(Binding binding);
	
	public Operation[] getOperations(URL wsdlLocation, QName portTypeName);
	
	public Operation[] getOperations(PortType portType);
	
	public Operation getOperation(URL wsdlLocation, QName portTypeName, QName opName);
	
	public Operation getOperation(PortType portType, QName opName);

}
