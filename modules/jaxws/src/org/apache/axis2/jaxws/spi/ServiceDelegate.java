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

package org.apache.axis2.jaxws.spi;

import java.net.URL;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.wsdl.WSDLException;
import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.ws.Dispatch;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.Service.Mode;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.http.HTTPBinding;
import javax.xml.ws.soap.SOAPBinding;

import org.apache.axis2.jaxws.ClientMediator;
import org.apache.axis2.jaxws.JAXWSClientContext;
import org.apache.axis2.jaxws.client.JAXBDispatch;
import org.apache.axis2.jaxws.client.XMLDispatch;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.handler.PortData;
import org.apache.axis2.jaxws.handler.PortInfoImpl;
import org.apache.axis2.jaxws.util.WSDL4JWrapper;
import org.apache.axis2.jaxws.util.WSDLWrapper;
import org.apache.commons.logging.LogFactory;

public class ServiceDelegate extends javax.xml.ws.spi.ServiceDelegate {
	private Executor executor;
    private Map<QName, org.apache.axis2.jaxws.handler.PortData> ports;

    private ServiceDescription serviceDescription;
    private QName serviceQname;
    private ClientMediator mediator = null;

    // If no binding ID is available, use this one
    private static String DEFAULT_BINDING_ID = SOAPBinding.SOAP11HTTP_BINDING;
    
    public ServiceDelegate(URL url, QName qname, Class clazz) throws WebServiceException{
    	super();
    	this.serviceQname = qname;
    	ports = new Hashtable<QName, PortData>();
    	mediator = new ClientMediator();

        if(!isValidServiceName()){
    		throw new WebServiceException("Invalid Service QName, Service Name cannot be null or empty");
    	}

        serviceDescription = new ServiceDescription(url, serviceQname, clazz);
        if (isValidWSDLLocation()) {
            if(!isServiceDefined(serviceQname)){
                throw new WebServiceException("Service " + serviceQname + " not defined in WSDL");
            }
            readPorts();
        }
    }
     
    public void addPort(QName portName, String bindingId, String endpointAddress) throws WebServiceException{
        // TODO Auto-generated method stub
    	if(portName == null ){
    		throw new WebServiceException("Invalid port, port cannot be null");
    	}
    	if("".equals(portName)){
    		throw new WebServiceException("Invalid port name");
    	}
    	if (endpointAddress == null) {
    		throw new WebServiceException("Invalid endpointAddress, endpointAddress cannot be null");
    	}
    	
    	if(bindingId!=null && !bindingId.equals(SOAPBinding.SOAP11HTTP_BINDING)){
    		throw new UnsupportedOperationException("Only SOAP11HTTP_BINDING supported at this time.");
    	}
        
        if (bindingId == null) {
            bindingId = DEFAULT_BINDING_ID;
        }
    	if(!ports.containsKey(portName)){	
    		PortData port = new PortInfoImpl(serviceQname, portName, bindingId, endpointAddress);
    		ports.put(portName, port);
    	}
    	else{
    		//TODO: Can same port have two different set of SOAPAddress
    		/*PortInfoImpl port =(PortInfoImpl) ports.get(portName);
    		port.setBindingID(bindingId);
    		port.setEndPointAddress(endpointAddress);
    		*/
    		
    		throw new WebServiceException("Port is already added");
    	}
    }
    private <T> JAXWSClientContext<T> createClientContext(PortData portData, Class<T> clazz, Mode mode){
    	JAXWSClientContext<T> clientContext = new JAXWSClientContext<T>();
        clientContext.setServiceDescription(serviceDescription);
    	clientContext.setPort(portData);
    	clientContext.setClazz(clazz);
    	clientContext.setServiceMode(mode);
    	clientContext.setExecutor(this.getExecutor());	
    	return clientContext;
    }

    public <T> Dispatch<T> createDispatch(QName qname, Class<T> clazz, Mode mode) throws WebServiceException {
    	if(qname == null){
    		throw new WebServiceException("Failed to create Dispatch port cannot be null.");
    	}
    	
    	if(!isPortValid(qname)){
    		throw new WebServiceException("Failed to create Dispatch, Port "+qname+" not found, add port to Service before calling dispatch.");
    	}
    	PortData portData = (PortData)ports.get(qname);
    	if(portData == null){
    		//Internal error 
    	}
    	
    	addBinding(portData.getBindingID());
    	
    	JAXWSClientContext<T> clientContext = createClientContext(portData, clazz, mode);
    	XMLDispatch<T> dispatch = mediator.createXMLDispatch(clientContext);
    	dispatch.setServiceDelegate(this);
    	return dispatch;        
    }
    
    public Dispatch<java.lang.Object> createDispatch(QName qname, JAXBContext context, Mode mode) {
        if (qname == null) {
            throw new WebServiceException("Failed to create Dispatch port cannot be null.");
        }
        
        if (!isPortValid(qname)) {
            throw new WebServiceException("Failed to create Dispatch, Port "+qname+" not found, add port to Service before calling dispatch.");
        }
        
        PortData portData = (PortData) ports.get(qname);
        
        addBinding(portData.getBindingID());
        
        JAXWSClientContext clientCtx = createClientContext(portData, Object.class, mode);
        clientCtx.setJAXBContext(context);
        
        JAXBDispatch<Object> dispatch = mediator.createJAXBDispatch(clientCtx);
        dispatch.setServiceDelegate(this);
        return dispatch;
    }
    
    private Executor getDefaultExecutor(){
    	return Executors.newFixedThreadPool(3);
    }

    public Executor getExecutor() {
       if(this.executor == null){
    	   this.executor = getDefaultExecutor();
       }
       
        return this.executor;
    }

    public HandlerResolver getHandlerResolver() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> T getPort(Class<T> sei) {
        // TODO Auto-generated method stub
    	if(sei == null){
    		return null;
    	}
    	JAXWSClientContext<T> clientContext = new JAXWSClientContext<T>();
    	//Set all the required properties for JAXWSClientContext.
    	
    	return mediator.createProxy(clientContext);
    	
    	
    	/* TODO move this code to ClientMediators CreateProxy() 
    	Class[] seiClazz = new Class[]{sei};
    	Object proxyClass = Proxy.newProxyInstance(sei.getClassLoader(), seiClazz, proxyHandler);
    	createAxisService();
    	proxyHandler.setAxisService(axisService);
    	proxyHandler.setServiceClient(serviceClient);
    	*/
    }
     
    @Override
    public <T> T getPort(QName qname, Class<T> sei) {
        // TODO Auto-generated method stub
    	if(sei == null){
    		return null;
    	}
    	
    	JAXWSClientContext<T> clientContext = new JAXWSClientContext<T>();
    	//Set all the required properties for JAXWSClientContext.
    	
    	return mediator.createProxy(clientContext);
    	/*TODO move this code to ClientMediators CreateProxy() 
    	this.portQname = qname;
    	Proxies proxyHandler = new Proxies();
    	Class[] seiClazz = new Class[]{sei};
    	Object proxyClass = Proxy.newProxyInstance(sei.getClassLoader(), seiClazz, proxyHandler);
    	createAxisService();
    	proxyHandler.setAxisService(axisService);
    	proxyHandler.setServiceClient(serviceClient);
    	return sei.cast(proxyClass);
    	*/
      
    }
    
    @Override
    public Iterator<QName> getPorts() {
        // TODO Auto-generated method stub
        return null;
    }
    
    public QName getServiceName() {
        // TODO Auto-generated method stub
        return serviceQname;
    }
    
    public ServiceDescription getServiceDescription() {
        return serviceDescription;
    }
    
    public URL getWSDLDocumentLocation() {
        return serviceDescription.getWSDLLocation();
    }
    
    private boolean isPortValid(QName portName){
    	return ports!=null && ports.size() >0 && ports.containsKey(portName);
    }

    private boolean isValidServiceName(){
    	return serviceQname != null && !"".equals(serviceQname.toString().trim());	
    }

    private boolean isValidWSDLLocation(){
        URL wsdlLocation = getWSDLDocumentLocation();
    	return wsdlLocation != null && !"".equals(wsdlLocation.toString().trim());
    }
    
    private void readPorts(){
    	String[] portNames = getWSDLWrapper().getPorts(serviceQname);
    	String targetNamespace = getWSDLWrapper().getTargetNamespace();
    	for(String portName: portNames){
    		QName portQname = new QName(targetNamespace, portName);
    		String address = getWSDLWrapper().getSOAPAddress(serviceQname, portQname);
    		//TODO: get Binding ID from WSDL and add it here.
    		PortData portInfo = new PortInfoImpl(serviceQname, portQname, DEFAULT_BINDING_ID, address);
    		ports.put(portQname, portInfo);
    	}
    }
    
    public void setExecutor(Executor executor) {
        // TODO Auto-generated method stub
        this.executor = executor;
        if(executor == null){
        	this.executor = getDefaultExecutor();
        	
        }
    }
    
    public void setHandlerResolver(HandlerResolver handlerresolver) {
        // TODO Auto-generated method stub 
    }
    
    // TODO: Remove this method and put the WSDLWrapper methods on the ServiceDescriptor directly
    private WSDLWrapper getWSDLWrapper() {
    	return serviceDescription.getWSDLWrapper();
    }
    
    private boolean isServiceDefined(QName serviceName){
    	return getWSDLWrapper().getService(serviceName)!= null;
    }
    
    private void addBinding(String bindingId){
//    	TODO: before creating binding do I have to do something with Handlers ... how is Binding related to Handler, this mistry sucks!!!
        if(bindingId != null){
	        //TODO: create all the bindings here
	        if(bindingId.equals(SOAPBinding.SOAP11HTTP_BINDING)){
	        	//instantiate soap11 binding implementation here and call setBinding in BindingProvider
	        }
	        
	        if(bindingId.equals(SOAPBinding.SOAP12HTTP_BINDING)){
	        	//instantiate soap11 binding implementation here and call setBinding in BindingProvider
	        }
	        
	        if(bindingId.equals(HTTPBinding.HTTP_BINDING)){
	        	//instantiate http binding implementation here and call setBinding in BindingProvider
	        }
        }
    }
}
