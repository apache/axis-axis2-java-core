/*
 * Copyright 2006 The Apache Software Foundation.
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

import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.Service.Mode;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.http.HTTPBinding;
import javax.xml.ws.soap.SOAPBinding;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.jaxws.ClientConfigurationFactory;
import org.apache.axis2.jaxws.ClientMediator;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.JAXWSClientContext;
import org.apache.axis2.jaxws.client.JAXBDispatch;
import org.apache.axis2.jaxws.client.XMLDispatch;
import org.apache.axis2.jaxws.client.factory.DescriptorFactory;
import org.apache.axis2.jaxws.client.factory.ProxyHandlerFactory;
import org.apache.axis2.jaxws.client.proxy.BaseProxyHandler;
import org.apache.axis2.jaxws.client.proxy.ProxyDescriptor;
import org.apache.axis2.jaxws.description.DescriptionFactory;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.handler.PortData;
import org.apache.axis2.jaxws.handler.PortInfoImpl;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.axis2.jaxws.util.WSDLWrapper;

/**
 * The ServiceDelegate serves as the backing implementation for all of the 
 * methods in the {@link javax.xml.ws.Service} API.  This is the plug 
 * point for the client implementation. 
 */
public class ServiceDelegate extends javax.xml.ws.spi.ServiceDelegate {
    private Executor executor;
    private Map<QName, org.apache.axis2.jaxws.handler.PortData> ports;

    private ServiceDescription serviceDescription;
    private QName serviceQname;
    private ClientMediator mediator = null;
    private ServiceClient serviceClient = null;
    // If no binding ID is available, use this one
    private static String DEFAULT_BINDING_ID = SOAPBinding.SOAP11HTTP_BINDING;
    
    public ServiceDelegate(URL url, QName qname, Class clazz) throws WebServiceException{
    	super();
    	this.serviceQname = qname;
    	ports = new Hashtable<QName, PortData>();
    	mediator = new ClientMediator();

        if(!isValidServiceName()){
        	// TODO NLS
    		throw ExceptionFactory.makeWebServiceException("Invalid Service QName, Service Name cannot be null or empty");
    	}

        serviceDescription = DescriptionFactory.createServiceDescription(url, serviceQname, clazz);
        if (isValidWSDLLocation()) {
            if(!isServiceDefined(serviceQname)){
            	// TODO NLS
                throw new WebServiceException("Service " + serviceQname + " not defined in WSDL");
            }
            readPorts();
        }
    }
    
    //================================================
    // JAX-WS API methods
    //================================================
    
    /*
     * (non-Javadoc)
     * @see javax.xml.ws.spi.ServiceDelegate#addPort(javax.xml.namespace.QName, java.lang.String, java.lang.String)
     */
    public void addPort(QName portName, String bindingId, String endpointAddress)
        throws WebServiceException {
    	if(portName == null ){
    		// TODO NLS
    		throw ExceptionFactory.makeWebServiceException("Invalid port, port cannot be null");
    	}
    	if("".equals(portName)){
    		// TODO NLS
    		throw ExceptionFactory.makeWebServiceException("Invalid port name");
    	}
    	if (endpointAddress == null) {
    		// TODO NLS
    		throw ExceptionFactory.makeWebServiceException("Invalid endpointAddress," +
                    " endpointAddress cannot be null");
    	}
    	
    	if(bindingId!=null && !bindingId.equals(SOAPBinding.SOAP11HTTP_BINDING)){
    		// TODO NLS
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
    		// TODO NLS
    		throw new WebServiceException("Port is already added");
    	}
    }

    /*
     * (non-Javadoc)
     * @see javax.xml.ws.spi.ServiceDelegate#createDispatch(javax.xml.namespace.QName, java.lang.Class, javax.xml.ws.Service.Mode)
     */
    public <T> Dispatch<T> createDispatch(QName qname, Class<T> clazz, Mode mode) throws WebServiceException {
    	if(qname == null){
    		// TODO NLS
    		throw ExceptionFactory.makeWebServiceException("Failed to create Dispatch port cannot be null.");
    	}
    	
    	if(!isPortValid(qname)){
    		// TODO NLS
    		throw ExceptionFactory.makeWebServiceException("Failed to create Dispatch, Port "+qname+" not found, add port to Service before calling dispatch.");
    	}
    	
        PortData portData = (PortData) ports.get(qname);
    	
        if(portData == null){
    		throw ExceptionFactory.makeWebServiceException("Could not find Port info"); 
    	}
    	
    	addBinding(portData.getBindingID());
    	
    	//JAXWSClientContext<T> clientContext = createClientContext(portData, clazz, mode);
        
        XMLDispatch<T> dispatch = new XMLDispatch<T>(portData);
        
        
        if (mode != null) {
            dispatch.setMode(mode);
        }
        else {
            dispatch.setMode(Service.Mode.PAYLOAD);
        }
        
        //XMLDispatch<T> dispatch = mediator.createXMLDispatch(clientContext);

        if (serviceClient == null)
            serviceClient = getServiceClient();
        
        dispatch.setServiceClient(serviceClient);
        dispatch.setServiceDelegate(this);
    	
        return dispatch;        
    }
    
    /*
     * (non-Javadoc)
     * @see javax.xml.ws.spi.ServiceDelegate#createDispatch(javax.xml.namespace.QName, javax.xml.bind.JAXBContext, javax.xml.ws.Service.Mode)
     */
    public Dispatch<java.lang.Object> createDispatch(QName qname, JAXBContext context, Mode mode) {
        if (qname == null) {
        	// TODO NLS
            throw ExceptionFactory.makeWebServiceException("Dispatch creation " +
                    "failed.  Port QName cannot be null.");
        }
        
        if (!isPortValid(qname)) {
        	// TODO NLS
            throw ExceptionFactory.makeWebServiceException("Dispatch creation " +
                    "failed.  Port " + qname + " was not found.  Make sure the " +
                    "port has been added to the Service.");
        }
        
        PortData portData = (PortData) ports.get(qname);
        
        addBinding(portData.getBindingID());
        
        JAXWSClientContext clientCtx = createClientContext(portData, Object.class, mode);
        clientCtx.setJAXBContext(context);
        
        JAXBDispatch<Object> dispatch = new JAXBDispatch(portData);
        
        if (mode != null) {
            dispatch.setMode(mode);
        }
        else {
            dispatch.setMode(Service.Mode.PAYLOAD);
        }
        
        if (serviceClient == null)
            serviceClient = getServiceClient();
        
        dispatch.setJAXBContext(context);
        dispatch.setServiceClient(serviceClient);
        dispatch.setServiceDelegate(this);
        
        return dispatch;
    }

    /*
     * (non-Javadoc)
     * @see javax.xml.ws.spi.ServiceDelegate#getPort(java.lang.Class)
     */
    public <T> T getPort(Class<T> sei) throws WebServiceException {
       return getPort(null, sei);
    }
    
    /*
     * (non-Javadoc)
     * @see javax.xml.ws.spi.ServiceDelegate#getPort(javax.xml.namespace.QName, java.lang.Class)
     */
    public <T> T getPort(QName portName, Class<T> sei) throws WebServiceException {
        /* TODO Check to see if WSDL Location is provided.
         * if not check WebService annotation's WSDLLocation
         * if both are not provided then throw exception.
         */
        
        if(!isValidWSDLLocation()){
            //TODO: Should I throw Exception if no WSDL
            //throw ExceptionFactory.makeWebServiceException("WSLD Not found");
        }
        if(sei == null){
            // TODO NLS
            throw ExceptionFactory.makeWebServiceException("Invalid Service Endpoint Interface Class");
        }
        /*TODO: if portQname is null then fetch it from annotation. 
         * if portQname is provided then add that to the ports table.
         */
        if(portName!=null){
            String address = "";
            if(isValidWSDLLocation()){
                address = getWSDLWrapper().getSOAPAddress(serviceQname, portName);
            }
            if(ports.get(portName)==null){
                addPort(portName, null, address);
            }
        }
        DescriptorFactory df = (DescriptorFactory)FactoryRegistry.getFactory(DescriptorFactory.class);
        ProxyDescriptor pd = df.create(sei);
        pd.setPort(ports.get(portName));
        ProxyHandlerFactory phf =(ProxyHandlerFactory) FactoryRegistry.getFactory(ProxyHandlerFactory.class);
        BaseProxyHandler proxyHandler = phf.create(pd, this);
        
        Class[] seiClazz = new Class[]{sei, BindingProvider.class};
        Object proxyClass = Proxy.newProxyInstance(sei.getClassLoader(), seiClazz, proxyHandler);
        
        return sei.cast(proxyClass);
    }
    
    /*
     * (non-Javadoc)
     * @see javax.xml.ws.spi.ServiceDelegate#getExecutor()
     */
    public Executor getExecutor() {
        if(executor == null){
           executor = getDefaultExecutor();
        }
        
         return executor;
     }
    
    /*
     * (non-Javadoc)
     * @see javax.xml.ws.spi.ServiceDelegate#getHandlerResolver()
     */
    public HandlerResolver getHandlerResolver() {
        return null;
    }
    
    /*
     * (non-Javadoc)
     * @see javax.xml.ws.spi.ServiceDelegate#getPorts()
     */
    public Iterator<QName> getPorts() {
        return null;
    }
    
    /*
     * (non-Javadoc)
     * @see javax.xml.ws.spi.ServiceDelegate#getServiceName()
     */
    public QName getServiceName() {
        return serviceQname;
    }
    
    /*
     * (non-Javadoc)
     * @see javax.xml.ws.spi.ServiceDelegate#getWSDLDocumentLocation()
     */
    public URL getWSDLDocumentLocation() {
        return serviceDescription.getWSDLLocation();
    }
    
    /*
     * (non-Javadoc)
     * @see javax.xml.ws.spi.ServiceDelegate#setExecutor(java.util.concurrent.Executor)
     */
    public void setExecutor(Executor e) {
        if (e == null) {
            throw ExceptionFactory.makeWebServiceException("Cannot set Executor to null");
        }
        
        executor = e;
    }
    
    /*
     * (non-Javadoc)
     * @see javax.xml.ws.spi.ServiceDelegate#setHandlerResolver(javax.xml.ws.handler.HandlerResolver)
     */
    public void setHandlerResolver(HandlerResolver handlerresolver) {
        
    }
    
    //================================================
    // Internal public APIs
    //================================================
    
    /**
     * Get the ServiceDescription tree that this ServiceDelegate 
     */
    public ServiceDescription getServiceDescription() {
        return serviceDescription;
    }
    
    //TODO Change when ServiceDescription has to return ServiceClient or OperationClient
    /**
     * 
     */
    public ServiceClient getServiceClient() throws WebServiceException {
        try {
            if(serviceClient == null) {
                ConfigurationContext configCtx = getAxisConfigContext();
                AxisService axisSvc = serviceDescription.getAxisService();
                
                serviceClient = new ServiceClient(configCtx, axisSvc);
            }
        } catch (AxisFault e) {
            throw ExceptionFactory.makeWebServiceException("An error occured " +
                    "while creating the ServiceClient", e);
        }
        
        return serviceClient;        
    }

    //================================================
    // Impl methods
    //================================================
    
    //TODO: Need to make the default number of threads configurable
    private Executor getDefaultExecutor(){
        return Executors.newFixedThreadPool(3);
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
    
    // TODO: Remove this method and put the WSDLWrapper methods on the ServiceDescriptor directly
    private WSDLWrapper getWSDLWrapper() {
    	return serviceDescription.getWSDLWrapper();
    }
    
    private boolean isServiceDefined(QName serviceName){
    	return getWSDLWrapper().getService(serviceName)!= null;
    }
    
    private void addBinding(String bindingId){
        // TODO: before creating binding do I have to do something with Handlers ... how is Binding related to Handler, this mistry sucks!!!
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
    
    //TODO We should hang AxisConfiguration from ServiceDescription or something parent to ServiceDescription
    private ConfigurationContext getAxisConfigContext() {
    	ClientConfigurationFactory factory = ClientConfigurationFactory.newInstance(); 
    	ConfigurationContext configCtx = factory.getClientConfigurationContext();
    	return configCtx;
    	
    }

}
