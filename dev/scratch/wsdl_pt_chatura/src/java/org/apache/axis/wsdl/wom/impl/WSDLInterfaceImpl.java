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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;

import org.apache.axis.wsdl.wom.WSDLInterface;
import org.apache.axis.wsdl.wom.WSDLOperation;
import javax.xml.namespace.QName;





/**
 * @author Chathura Herath
 *  
 */
public class WSDLInterfaceImpl extends ComponentImpl implements  WSDLInterface {
	
	
	private String name;

	private URI targetnamespace;

	private HashMap superInterfaces = new HashMap();

	private List faults = new LinkedList();

	private HashMap operations = new HashMap();
	
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
	public HashMap getDefinedOperations(WSDLInterface wsdlInterface){
		Object temp = this.superInterfaces.get(wsdlInterface.getName());
		if(null == temp ) throw new WSDLProcessingException(wsdlInterface.getName()+" is not a valid super interface of the "+this.getName()+". Interface cannot be located.");
		return ((WSDLInterface)temp).getDefinedOperations();
	}
	
	
	public HashMap getDefinedOperations(){
		
		return this.operations;
		
	}
	/**
	 * Will return a map of all this <code>WSDLOperation</code>s that 
	 * are defined and inherited from super interfaces.
	 */
	public HashMap getAllOperations(){
	    
	    HashMap all = (HashMap)this.operations.clone();
	    
	    
	    if(this.superInterfaces.size() ==0 ){
	        return all;
	    }else{
	        Iterator superIterator = this.superInterfaces.values().iterator();
	        Iterator operationIterator;
	        WSDLInterface superInterface;
	        WSDLOperation superInterfaceOperation;
	        Iterator thisIterator = all.values().iterator();
	        WSDLOperation thisOperation;
	        boolean tobeAdded = false;
	        while(superIterator.hasNext()){
	            superInterface = (WSDLInterface)superIterator.next();
	            operationIterator = superInterface.getAllOperations().values().iterator();
	            while(operationIterator.hasNext()){
	                superInterfaceOperation = (WSDLOperation)operationIterator.next();
	                tobeAdded = true;
	                while(thisIterator.hasNext()){
	                    thisOperation = (WSDLOperation)thisIterator.next();
	                    
	                    if(thisOperation.getName() == superInterfaceOperation.getName() && !tobeAdded){
	                        if(thisOperation.getTargetnemespace().getPath().equals(superInterfaceOperation.getTargetnemespace().getPath())){
	                            //Both are the same Operation; the one inherited and
	                            //the one that is already in the map(may or maynot be inherited)
	                            tobeAdded = false;
	                        }
	                        else{
	                            //same name but target namespces dont match 
	                            //TODO Think this is an error
	                            throw new WSDLProcessingException("The Interface " +this.getName() +" has more than one Operation that has the same name but not the same interface ");
	                        }
	                    }
	                }
	                if(tobeAdded){
	                    //This one is not in the list already developped
	                    all.put(superInterfaceOperation.getName(), superInterfaceOperation);
	                }
	                
	            }
	        }
	        return all;
	    	
	    }
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
	public HashMap getOperations() {
		return operations;
	}
	
	public WSDLOperation getOperation(QName qName){
	    this.checkValidityOfNamespaceWRTWSDLContext(qName);
	    return this.getOperation(qName.getLocalPart());
	}
	
	public WSDLOperation getOperation(String nCName){
	    Object temp = this.operations.get(nCName);
	    if(null == temp) throw new WSDLProcessingException("No Operation found with the QName with ncname/ ncname with "+nCName);
	    return (WSDLOperation)temp;
	}

	/**
	 * @return
	 */
	public HashMap getSuperInterfaces() {
		return superInterfaces;
	}
	
	public WSDLInterface getSuperInterface(QName qName){
	    return (WSDLInterface)this.superInterfaces.get(qName);
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
	public void setOperations(HashMap list) {
		operations = list;
	}
	/**
	 * The Operation will be added to the interfce's operations.
	 * Though the Qname is required the actual storage will be from the 
	 * NCName of the operation, but the namespace URI of the QName 
	 * should match that of the Namespaces defined in the WSDLConstants interface. 
	 * @param qName
	 * @param operation
	 */
	public void setOperation(QName qName, WSDLOperation operation){
	    this.checkValidityOfNamespaceWRTWSDLContext(qName);
	    this.setOperation(qName.getLocalPart(), operation);
	}
	
	/**
	 * The operation is added by its ncname.
	 * @param nCName
	 * @param operation
	 */
	public void setOperation(String nCName, WSDLOperation operation){
	    this.operations.put(nCName, operation);
	}

	/**
	 * @param list
	 */
	public void setSuperInterfaces(HashMap list) {
		superInterfaces = list;
	}
	
	/**
	 * The Inteface will be added to the list of super interfaces keyed with 
	 * the QName.
	 * @param qName The QName of the Inteface
	 * @param interfaceComponent WSDLInterface Object
	 */
	public void addSuperInterface(QName qName, WSDLInterface interfaceComponent){
	    this.superInterfaces.put(qName, interfaceComponent);
	}

	/**
	 * @param uri
	 */
	public void setTargetnamespace(URI uri) {
		targetnamespace = uri;
	}

}
