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

import java.util.HashMap;

import javax.xml.namespace.QName;

import org.apache.wsdl.WSDLBinding;
import org.apache.wsdl.WSDLBindingFault;
import org.apache.wsdl.WSDLBindingOperation;
import org.apache.wsdl.WSDLInterface;

/**
 * @author chathura@opensource.lk
 *
 */
public class WSDLBindingImpl extends ExtensibleComponentImpl implements WSDLBinding   {

	private QName name;
	
	private WSDLInterface boundInterface;
	
	private HashMap bindingFaults = new HashMap();
	
	private HashMap bindingOperations = new HashMap();
	
	
	
	 
	
	
	public WSDLInterface getBoundInterface() {
		return boundInterface;
	}
	public void setBoundInterface(WSDLInterface boundInterface) {
		this.boundInterface = boundInterface;
	}
	
	public QName getName() {
		return name;
	}
	public void setName(QName name) {
		this.name = name;
	}
	
	public String getTargetNameSpace() {
		return this.name.getLocalPart();
	}
	
	
   
    public HashMap getBindingFaults() {
        return bindingFaults;
    }
    public void setBindingFaults(HashMap bindingFaults) {
        this.bindingFaults = bindingFaults;
    }
    public HashMap getBindingOperations() {
        return bindingOperations;
    }
    public void setBindingOperations(HashMap bindingOperations) {
        this.bindingOperations = bindingOperations;
    }
	public void addBindingOperation(WSDLBindingOperation bindingOperation){
	    if(null != bindingOperation)
	        this.bindingOperations.put(bindingOperation.getName(), bindingOperation);
	}
	public WSDLBindingOperation getBindingOperation(QName qName){
	    return (WSDLBindingOperation)this.bindingOperations.get(qName);
	}
	
	public void addBindingFaults(WSDLBindingFault bindingFault){
	    if(null != bindingFault)
	        this.bindingFaults.put(bindingFault.getRef(), bindingFault);	    
	}
	
	public WSDLBindingFault getBindingFault(QName ref){
	    return (WSDLBindingFault)this.bindingFaults.get(ref);	    
	}
	
}
