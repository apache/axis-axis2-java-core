/*
 * Copyright 2001-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of thWSDLInterfaceImple License at
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
import java.util.LinkedList;
import java.util.List;

import org.apache.axis.wsdl.wom.WSDLDefinitions;

/**
 * @author chathura@opensource.lk
 *
 */
public class WSDLDefinitionsImpl implements WSDLDefinitions {

	//TODO local name and the naspace name to be made static or through a Constant class.
	
	// The attrebute information items
	
	//TODO required; thus check it up
	private URI targetNameSpace;
	
	//private NamespaceMappings[] namespaceDefinitions;
	
	//Element Infotmation Items.
	
	private Object types;
	
	/**
	 * This List will be a list of <code>WSDLInterface</code> objects.
	 */
	private List wsdlInterfaces = new LinkedList();
	
	/**
	 * This <code>List </code> is a list of <code>WSDLBinding </code> objects. 
	 */
	private List bindings = new LinkedList();
	
	/**
	 * This <code>List </code> is a list of <code>WSDLService </code> objects.
	 * Support of multiple is backed by the requirements in the specification.
	 */
	private List services = new LinkedList();
	
	
	
	
	
	
	
	public List getBindings() {
		return bindings;
	}
	public void setBindings(List bindings) {
		this.bindings = bindings;
	}
	public List getServices() {
		return services;
	}
	public void setServices(List services) {
		this.services = services;
	}
	public URI getTargetNameSpace() {
		return targetNameSpace;
	}
	public void setTargetNameSpace(URI targetNameSpace) {
		this.targetNameSpace = targetNameSpace;
	}
	public Object getTypes() {
		return types;
	}
	public void setTypes(Object types) {
		this.types = types;
	}
	public List getWsdlInterfaces() {
		return wsdlInterfaces;
	}
	public void setWsdlInterfaces(List wsdlInterfaces) {
		this.wsdlInterfaces = wsdlInterfaces;
	}
}
