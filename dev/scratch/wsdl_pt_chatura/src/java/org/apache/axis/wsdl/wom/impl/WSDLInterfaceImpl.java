/**
 * Copyright 2001-2004 The Apache Software Foundation.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
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

import org.apache.axis.wsdl.wom.WSDLInterface;





/**
 * @author Chathura Herath
 *  
 */
public class WSDLInterfaceImpl extends ComponentImpl implements  WSDLInterface {
	
	
	private String name;

	private URI targetnamespace;

	private List superInterfaces = new LinkedList();

	private List faults = new LinkedList();

	private List operations = new LinkedList();
	
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
	public List getDefinedOperations(WSDLInterface wsdlInterface){
		throw new UnsupportedOperationException("To be implementaed");
	}
	
	
	public List getDefinedOperations(){
		
		return this.getDefinedOperations(this);
		
	}
	

	/**
	 * @return
	 */
	public List getFaults() {
		return faults;
	}

	/**
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return
	 */
	public List getOperations() {
		return operations;
	}

	/**
	 * @return
	 */
	public List getSuperInterfaces() {
		return superInterfaces;
	}

	/**
	 * @return
	 */
	public URI getTargetnamespace() {
		return targetnamespace;
	}

	/**
	 * @param list
	 */
	public void setFaults(List list) {
		faults = list;
	}

	/**
	 * @param string
	 */
	public void setName(String string) {
		name = string;
	}

	/**
	 * @param list
	 */
	public void setOperations(List list) {
		operations = list;
	}

	/**
	 * @param list
	 */
	public void setSuperInterfaces(List list) {
		superInterfaces = list;
	}

	/**
	 * @param uri
	 */
	public void setTargetnamespace(URI uri) {
		targetnamespace = uri;
	}

}
