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
import java.util.HashMap;

import org.apache.axis.wsdl.wom.WSDLBinding;
import org.apache.axis.wsdl.wom.WSDLDefinitions;
import org.apache.axis.wsdl.wom.WSDLInterface;
import org.apache.axis.wsdl.wom.WSDLService;
import org.apache.xml.utils.QName;



/**
 * @author chathura@opensource.lk
 *
 */
public class WSDLDefinitionsImpl extends ComponentImpl implements WSDLDefinitions {

	//TODO local name and the naspace name to be made static or through a Constant class.
	
	// The attrebute information items
	
	//TODO required; thus check it up
	private URI targetNameSpace;
	
	//private NamespaceMappings[] namespaceDefinitions;
	
	//TODO The object structure of some external xml data binding is going to be pluged here eventually.  
	
	private Object types;
	
	/**
	 * This List will be a list of <code>WSDLInterface</code> objects.
	 */
	private HashMap wsdlInterfaces = new HashMap();
	
	/**
	 * This <code>HashMap </code> is a Map of <code>WSDLBinding </code> objects. 
	 */
	private HashMap bindings = new HashMap();
	
	/**
	 * This <code>HashMap </code> is a list of <code>WSDLService </code> objects.
	 * Support of multiple is backed by the requirements in the specification.
	 */
	private HashMap services = new HashMap();
	
		
	
	public HashMap getBindings() {
		return bindings;
	}
	public void setBindings(HashMap bindings) {
		this.bindings = bindings;
	}
	/**
	 * The binding will be added despite its namespace being either of
	 * that specified in the WSDLConstants class(WSDL_NAMESPACES) otherwise the checkValidityOfNamespaceWRTWSDLContext() 
	 * method will throw an exception.
	 * @param qName The QName of the binding
	 * @param binding Binding Object
	 */
	public void addBinding(QName qName, WSDLBinding binding){
	    checkValidityOfNamespaceWRTWSDLContext(qName);
	    this.addBinding(qName.getLocalPart(), binding);
	}
	
	/**
	 * Inserts a WSDLBinding to the Collection by keying its NCName.
	 * @param nCName NCName of the Binding
	 * @param binding Binding Object
	 */
	public void addBinding(String nCName, WSDLBinding binding){
	    this.bindings.put(nCName, binding);	    
	}
	
	
	/**
	 * The binding will be retrived despite its namespace being either of
	 * that specified in the WSDLConstants class(WSDL_NAMESPACES) otherwise the checkValidityOfNamespaceWRTWSDLContext() 
	 * method will throw an exception.
	 * @param qName THe Namespace of the QName should be either of the WSDL_NAMESPACES
	 * mentioned in the WSDLConstants interface.
	 * @return The Binding with the relavent QName which have a namespace
	 * that qualifies that of the versions in the WSDLConstants interface.
	 */
	public WSDLBinding getBinding(QName qName){
	    checkValidityOfNamespaceWRTWSDLContext(qName);
	    return this.getBinding(qName.getLocalPart());
	}
	/**
	 * Binding will be retrived by its NCName and the Namespace of the QName
	 * is assumed to be in line with that of the WSDL_NAMESPACES in the WSDLConstants
	 * interface, Thus no namespace checking will be done.
	 * @param nCName NCName of the Binding
	 * @return WSDLBinding Object or will throw an WSDLProcessingException in the case of object not found. 
	 */
	public WSDLBinding getBinding(String nCName){
	    WSDLBinding temp = (WSDLBinding)this.bindings.get(nCName);
	    if(null == temp) throw new WSDLProcessingException("Binding not found for NCName "+nCName);
	    return temp;
	}

	/**
	 * The Inteface will be added despite its namespace being either of
	 * that specified in the WSDLConstants class(WSDL_NAMESPACES) otherwise the checkValidityOfNamespaceWRTWSDLContext() 
	 * method will throw an exception.
	 * @param qName The QName of the Inteface
	 * @param interfaceComponent WSDLInterface Object
	 */
	public void addInterface(QName qName, WSDLInterface interfaceComponent){
	    checkValidityOfNamespaceWRTWSDLContext(qName);
	    this.wsdlInterfaces.put(qName.getLocalPart(), interfaceComponent);
	}
	
	/**
	 * Inserts a WSDLInterface to the Collection by keying its NCName.
	 * @param nCName NCName of the WSDLInterface
	 * @param interfaceComponent WSDLInterface Object
	 */
	public void addInterface(String nCName, WSDLInterface interfaceComponent){
	    this.wsdlInterfaces.put(nCName, interfaceComponent);	    
	}
	
	
	/**
	 * The Interface Component will be retrived despite its namespace being either of
	 * that specified in the WSDLConstants class(WSDL_NAMESPACES) otherwise the checkValidityOfNamespaceWRTWSDLContext() 
	 * method will throw an exception.
	 * @param qName THe Namespace of the QName should be either of the WSDL_NAMESPACES
	 * mentioned in the WSDLConstants interface.
	 * @return The Interface Component with the relavent QName which have a namespace
	 * that qualifies that of the versions in the WSDLConstants interface.
	 */
	public WSDLInterface getInterface(QName qName){
	    checkValidityOfNamespaceWRTWSDLContext(qName);
	    return this.getInterface(qName.getLocalPart());
	}
	/**
	 * Interface Component will be retrived by its NCName and the Namespace of the QName
	 * is assumed to be in line with that of the WSDL_NAMESPACES in the WSDLConstants
	 * interface, Thus no namespace checking will be done.
	 * @param nCName NCName of the Interface Component
	 * @return WSDLInterface Object or will throw an WSDLProcessingException in the case of object not found. 
	 */
	public WSDLInterface getInterface(String nCName){
	    WSDLInterface temp = (WSDLInterface)this.wsdlInterfaces.get(nCName);
	    if(null == temp) throw new WSDLProcessingException("Interface Component not found for NCName "+nCName);
	    return temp;
	}
	
    public HashMap getServices() {
        return services;
    }
    public void setServices(HashMap services) {
        this.services = services;
    }
    
    /**
	 * The Service will be retrived despite its namespace being either of
	 * that specified in the WSDLConstants class(WSDL_NAMESPACES) otherwise the checkValidityOfNamespaceWRTWSDLContext() 
	 * method will throw an exception.
	 * @param qName THe Namespace of the QName should be either of the WSDL_NAMESPACES
	 * mentioned in the WSDLConstants interface.
	 * @return The Service with the relavent QName which have a namespace
	 * that qualifies that of the versions in the WSDLConstants interface.
	 */
	public WSDLService getService(QName qName){
	    checkValidityOfNamespaceWRTWSDLContext(qName);
	    return this.getService(qName.getLocalPart());
	}
	/**
	 * Service will be retrived by its NCName and the Namespace of the QName
	 * is assumed to be in line with that of the WSDL_NAMESPACES in the WSDLConstants
	 * interface, Thus no namespace checking will be done.
	 * @param nCName NCName of the Service
	 * @return WSDLService Object or will throw an WSDLProcessingException in the case of object not found. 
	 */
	public WSDLService getService(String nCName){
	    WSDLService temp = (WSDLService)this.services.get(nCName);
	    if(null == temp) throw new WSDLProcessingException("Service not found for NCName "+nCName);
	    return temp;
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
	public HashMap getWsdlInterfaces() {
		return wsdlInterfaces;
	}
	public void setWsdlInterfaces(HashMap wsdlInterfaces) {
		this.wsdlInterfaces = wsdlInterfaces;
	}
}
