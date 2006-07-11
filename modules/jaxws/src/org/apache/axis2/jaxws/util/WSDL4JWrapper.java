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

package org.apache.axis2.jaxws.util;


import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;



public class WSDL4JWrapper implements WSDLWrapper {
    
	private Definition wsdlDefinition = null;
	private URL wsdlURL;
	
    public WSDL4JWrapper(URL wsdlURL)throws WSDLException{
		super();
		this.wsdlURL = wsdlURL;
		WSDLFactory factory = WSDLFactory.newInstance();
		WSDLReader reader = factory.newWSDLReader();
		wsdlDefinition = reader.readWSDL(wsdlURL.toString());
		
	}
	//TODO: Perform validations for each method to check for null parameters on QName.
	
	public Definition getDefinition(){
		return wsdlDefinition;
	}

	
	public Binding getFirstPortBinding(QName serviceQname) {
		// TODO Auto-generated method stub
		Service service = getService(serviceQname);
		if(service == null){
			return null;
		}
		Map map = getService(serviceQname).getPorts();
		if(map == null || map.isEmpty() ){
    		return null;
    	}
    	for(Object listObject : map.values()){
    		Port wsdlPort = (Port)listObject;
    		return wsdlPort.getBinding();
    		
    	}
    	return null;
		
	}
	
	public String getOperationName(QName serviceQname, QName portQname){
		Port port = getPort(serviceQname, portQname);
		Binding binding = port.getBinding();
		if(binding==null){
			return null;
		}
		
		List operations = binding.getBindingOperations();
		for(Object opObj : operations){
			BindingOperation operation = (BindingOperation)opObj;
			return operation.getName();
		}
		return null;
	}

	private Port getPort(QName serviceQname, QName eprQname){
		Service service = getService(serviceQname);
		if(service == null){
			return null;
		}
		return service.getPort(eprQname.getLocalPart());
		
	}
	
	public  ArrayList getPortBinding(QName serviceQname) {
		// TODO Auto-generated method stub
		Map map = this.getService(serviceQname).getPorts();
    	if(map == null || map.isEmpty() ){
    		return null;
    	}
    	ArrayList<Binding> portBindings = new ArrayList<Binding>();
    	for(Object listObject : map.values()){
    		Port wsdlPort = (Port)listObject;
    		Binding binding = wsdlPort.getBinding();
    		if(binding !=null){
    			portBindings.add(binding);
    		}
    	
    	}
    	return portBindings;
		
	}

	public String getPortBinding(QName serviceQname, QName portQname){
		Port port = getPort(serviceQname, portQname);
		if(port == null){
			return null;
		}
		Binding binding = port.getBinding();
		return binding.getQName().getLocalPart();
	}
	
	public String[] getPorts(QName serviceQname){
		String[] portNames = null;
		Service service = this.getService(serviceQname);
		if(service == null){
			return null;
		}
		Map map = service.getPorts();
		if(map == null || map.isEmpty()){
			return null;
		}
		portNames = new String[map.values().size()];
		Iterator iter = map.values().iterator();
		for(int i=0; iter.hasNext(); i++){
			Port wsdlPort = (Port)iter.next();
			if(wsdlPort!=null){
				portNames[i] = wsdlPort.getName();
			}
		}
		return portNames;
	}
	
	public Service getService(QName serviceQname) {
		// TODO Auto-generated method stub
		if(serviceQname == null){
			return null;
		}
		return wsdlDefinition.getService(serviceQname);
		
	}
	
	public String getSOAPAction(QName serviceQname) {
		// TODO Auto-generated method stub
		Binding binding = getFirstPortBinding(serviceQname);
		if(binding==null){
			return null;
		}
		List operations = binding.getBindingOperations();
		for(Object opObj : operations){
			BindingOperation operation = (BindingOperation)opObj;
			List exElements =operation.getExtensibilityElements();
			for(Object elObj:exElements){
				ExtensibilityElement exElement = (ExtensibilityElement)elObj;
				if(isSoapOperation(exElement)){
					SOAPOperation soapOperation = (SOAPOperation)exElement;
					return soapOperation.getSoapActionURI();
				}
			}
		}
		return null;
	}
	
	public String getSOAPAction(QName serviceQname, QName portQname) {
		// TODO Auto-generated method stub
		Port port = getPort(serviceQname, portQname);
		if(port == null){
			return null;
		}
		Binding binding = port.getBinding();
		if(binding==null){
			return null;
		}
		List operations = binding.getBindingOperations();
		for(Object opObj : operations){
			BindingOperation operation = (BindingOperation)opObj;
			List exElements =operation.getExtensibilityElements();
			for(Object elObj:exElements){
				ExtensibilityElement exElement = (ExtensibilityElement)elObj;
				if(isSoapOperation(exElement)){
					SOAPOperation soapOperation = (SOAPOperation)exElement;
						return soapOperation.getSoapActionURI();
				}
			}
		}
		return null;
	}

	public String getSOAPAction(QName serviceQname, QName portQname, QName operationQname) {
		Port port = getPort(serviceQname, portQname);
		if(port == null){
			return null;
		}
		Binding binding = port.getBinding();
		if(binding==null){
			return null;
		}
		List operations = binding.getBindingOperations();
		if(operations == null){
			return null;
		}
		BindingOperation operation = null;
		for(Object opObj : operations){
			operation = (BindingOperation)opObj;
		}
		List exElements =operation.getExtensibilityElements();
		for(Object elObj:exElements){
			ExtensibilityElement exElement = (ExtensibilityElement)elObj;
			if(isSoapOperation(exElement)){
				SOAPOperation soapOperation = (SOAPOperation)exElement;
				if(soapOperation.getElementType().equals(operationQname)){
					return soapOperation.getSoapActionURI();
				}
			}
		}
		
		return null;
	}
	public String getSOAPAddress(QName serviceQname, QName eprQname) {
		// TODO Auto-generated method stub
		List list =getPort(serviceQname, eprQname).getExtensibilityElements();
		for(Object obj : list){
			ExtensibilityElement element = (ExtensibilityElement)obj;
			if(isSoapAddress(element)){
				SOAPAddress address = (SOAPAddress)element;
				return address.getLocationURI();
			}
		}
		
		return null;
	}
	
	
	
	public URL getWSDLLocation() {
		// TODO Auto-generated method stub
		return this.wsdlURL;
	}
	private boolean isSoapAddress(ExtensibilityElement exElement){
		return WSDLWrapper.SOAP_11_ADDRESS.equals(exElement.getElementType()); 
		//TODO: Add soap12 support later
		//|| WSDLWrapper.SOAP_12_ADDRESS.equals(exElement.getElementType());
	}
	
	private boolean isSoapOperation(ExtensibilityElement exElement){
		return WSDLWrapper.SOAP_11_OPERATION.equals(exElement.getElementType()); 
		//TODO: Add Soap12 support later
		// || WSDLWrapper.SOAP_12_OPERATION.equals(exElement.getElementType());
	}
	public String getTargetNamespace() {
		// TODO Auto-generated method stub
		return wsdlDefinition.getTargetNamespace();
	}
	
}
