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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
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
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import org.apache.axis2.java.security.AccessController;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class WSDL4JWrapper implements WSDLWrapper {
	private static final Log log = LogFactory.getLog(WSDL4JWrapper.class);
	private Definition wsdlDefinition = null;
	private URL wsdlURL;
	
    public WSDL4JWrapper(URL wsdlURL)throws FileNotFoundException, UnknownHostException, 
    	ConnectException, WSDLException{
	super();
	this.wsdlURL = wsdlURL;
	try {
		URL url=wsdlURL;
		boolean isFileProtocol = (url!=null && "file".equals(url.getProtocol()))?true:false;
		if(isFileProtocol){
			String filePath = (url!=null)?url.getPath():null;
			//Check is the uri has relative path i.e path is not absolute and is not starting with a "/"
			boolean isRelativePath = (filePath!=null && !new File(filePath).isAbsolute())?true:false;
			if(isRelativePath){
				if(log.isDebugEnabled()){
					log.debug("WSDL URL has a relative path");
				}
				ClassLoader loader = Thread.currentThread().getContextClassLoader();
				//Lets read the complete WSDL URL for relative path from class loader
				//Use relative path of url to fetch complete URL.              
				url = loader.getResource(filePath);
				if(url == null){
					if(log.isDebugEnabled()){
						log.debug("WSDL URL for relative path not found in ClassLoader");
						log.warn("Unable to read WSDL from relative path, check the relative path");
						log.info("Relative path example: file:/WEB-INF/wsdl/<wsdlfilename>");
						log.warn("Using relative path as default wsdl URL to create wsdl Definition.");
					}
					url = wsdlURL;
				}     
			}
		}
            
	    URLConnection urlCon = url.openConnection();
	    InputStream is = urlCon.getInputStream();
	    is.close();
	    final String explicitWsdl = urlCon.getURL().toString();
	    try{
		wsdlDefinition = (Definition)AccessController.doPrivileged(
                    new PrivilegedExceptionAction() {
                        public Object run() throws WSDLException {
                            WSDLReader reader = getWSDLReader();
                            return reader.readWSDL(explicitWsdl);
                        }
                    }
        	 );
	    }catch(PrivilegedActionException e){
	        if (log.isDebugEnabled()) {
	                log.debug("Exception thrown from AccessController: " + e);
	        }
	        throw ExceptionFactory.makeWebServiceException(e.getException());
	    }
		
	}catch(FileNotFoundException ex) {
	    throw ex;
	}catch(UnknownHostException ex) {
	    throw ex;
	}catch(ConnectException ex) {
	    throw ex;
	}catch (Exception ex) {
            throw new WSDLException("WSDL4JWrapper : ", ex.getMessage());
	}
    }

    private static WSDLReader getWSDLReader() throws WSDLException {
        // Keep this method private
        WSDLReader reader;
        try {
            reader = (WSDLReader) AccessController.doPrivileged(
                    new PrivilegedExceptionAction() {
                        public Object run() throws WSDLException {
                            WSDLFactory factory = WSDLFactory.newInstance();
                            return factory.newWSDLReader();
                        }
                    });
        } catch (PrivilegedActionException e) {
            throw (WSDLException) e.getException();
        }
        return reader;
    }
    
    public WSDL4JWrapper(URL wsdlURL, Definition wsdlDefinition) throws WSDLException{
		super();
		this.wsdlURL = wsdlURL;
		this.wsdlDefinition = wsdlDefinition;
		
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
	
	public URL getWSDLLocation() {
		// TODO Auto-generated method stub
		return this.wsdlURL;
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
