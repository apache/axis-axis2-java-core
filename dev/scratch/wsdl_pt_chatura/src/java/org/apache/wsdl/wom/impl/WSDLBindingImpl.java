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
package org.apache.wsdl.wom.impl;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.wsdl.wom.WSDLBinding;
import org.apache.wsdl.wom.WSDLInterface;

/**
 * @author chathura@opensource.lk
 *
 */
public class WSDLBindingImpl extends ComponentImpl implements WSDLBinding  {

	private QName name;
	
	private URI targetNameSpace;
	
	private WSDLInterface boundInterface;
	
	private List faults;
	
	private List operations;
	
	private List features = new LinkedList();
	
	private List properties = new LinkedList();	
	
	
	 
	public List getFeatures() {
		return features;
	}
	public void setFeatures(List features) {
		this.features = features;
	}
	public List getProperties() {
		return properties;
	}
	public void setProperties(List properties) {
		this.properties = properties;
	}	
	
	public WSDLInterface getBoundInterface() {
		return boundInterface;
	}
	public void setBoundInterface(WSDLInterface boundInterface) {
		this.boundInterface = boundInterface;
	}
	public List getFaults() {
		return faults;
	}
	public void setFaults(List faults) {
		this.faults = faults;
	}
	public QName getName() {
		return name;
	}
	public void setName(QName name) {
		this.name = name;
	}
	public List getOperations() {
		return operations;
	}
	public void setOperations(List operations) {
		this.operations = operations;
	}
	public URI getTargetNameSpace() {
		return targetNameSpace;
	}
	public void setTargetNameSpace(URI targetNameSpace) {
		this.targetNameSpace = targetNameSpace;
	}
}
