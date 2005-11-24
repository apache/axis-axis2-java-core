/*
 * Copyright 2004,2005 The Apache Software Foundation.
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
package org.apache.axis2.jaxws.client;

import java.net.URI;
import java.net.URL;
import java.rmi.Remote;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.rpc.encoding.TypeMappingRegistry;
import javax.xml.ws.Call;
import javax.xml.ws.Dispatch;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.ServiceException;
import javax.xml.ws.Stub;
import javax.xml.ws.Service.Mode;
import javax.xml.ws.handler.AbstractHandler;
import javax.xml.ws.handler.HandlerInfo;
import javax.xml.ws.handler.HandlerRegistry;
import javax.xml.ws.security.SecurityConfiguration;

import javax.wsdl.*;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPBinding;

import org.apache.axis2.jaxws.JAXRPCWSDLInterface;
import org.apache.axis2.jaxws.JAXRPCWSDL11Interface;
import org.apache.axis2.jaxws.factory.WSDLFactoryImpl;
import org.apache.axis2.jaxws.handler.Axis2Handler;
import org.apache.axis2.clientapi.ListenerManager;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.description.OperationDescription;
import org.apache.axis2.description.ServiceDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisConfigurationImpl;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.AxisFault;
import org.apache.axis2.engine.Phase;
import org.apache.axis2.phaseresolver.PhaseResolver;

/**
 * @author sunja07
 * Class ServiceImpl
 */
public class ServiceImpl implements javax.xml.ws.Service {
	
	private HandlerRegistryImpl handlerRegistry = new HandlerRegistryImpl();
	
	private TypeMappingRegistry typeMappingRegistry;

	public boolean JAXB_USAGE = true;
	
	public String wsdlLoc = null;
	
	private JAXRPCWSDLInterface parserWrapper=null;
	
	private javax.wsdl.Service wsdlService = null;
	
	private Phase createAxis2Phase(BindingProviderImpl bp){
		Phase jaxRpcPhase = null;
		List jaxRpcHandlerList = null;
		List list = bp.getHandlerChain();
		if(list != null){
			jaxRpcPhase = new Phase("JAXRPCPhase");
			jaxRpcHandlerList = new ArrayList();
			jaxRpcHandlerList.addAll(list);
			Iterator handlerIter = jaxRpcHandlerList.iterator();
			while(handlerIter.hasNext()){
				HandlerInfo handlerInfoObject = (HandlerInfo)handlerIter.next();
				Object handlerObject = null;
				try{
				handlerObject = handlerInfoObject.getHandlerClass().newInstance();
				} catch(Exception e){
					e.printStackTrace();
				}
				Axis2Handler axisHandler = null;
				if(handlerObject instanceof AbstractHandler){
					axisHandler = new Axis2Handler((AbstractHandler)handlerObject);
					//Pass as much information as possible from HandlerInfo to HandlerDescription:
					//Don't see a way to pass the configuration or headers to HandlerDescription
					//directly
					HandlerDescription axisHandlerDesc = new HandlerDescription(new QName("Jax-Rpc Handler"));
					axisHandler.init(axisHandlerDesc);
				}
				if(axisHandler != null){
					jaxRpcPhase.addHandler(axisHandler);
				}
			}
			return jaxRpcPhase;
		}
		return null;
	}
	
	private ServiceContext getAxis2Service()throws AxisFault{
		/*AxisConfiguration axisConfig = new AxisConfigurationImpl();
		ConfigurationContext configContext = new ConfigurationContext(axisConfig);
		AxisEngine engine = new AxisEngine(configContext);
		return engine;*/
		ServiceDescription description = new ServiceDescription();

	    ConfigurationContext sysContext = null;
	    try{
		    if (ListenerManager.configurationContext == null) {
		            ConfigurationContextFactory efac = new ConfigurationContextFactory();
		            sysContext = efac.buildClientConfigurationContext(null);
		    } else {
		            sysContext = ListenerManager.configurationContext;
		    }
	    } catch(Exception e){
	    	e.printStackTrace();
	    }
	
		sysContext.getAxisConfiguration().addService(description);
	    
		ServiceContext sContext = new ServiceContext(description, sysContext);
		return sContext;
	}
	
	/**
	 * Method createCall
	 * Creates a Call object not associated with specific operation or target 
	 * service endpoint. This Call object needs to be configured using the 
	 * setter methods on the Call interface.
	 * @return Call object
	 * @throws ServiceException If any error in the creation of the Call object
	 */
	public Call createCall() throws ServiceException {
		Call call = new CallImpl();
		((CallImpl)call).setService(this);
		((CallImpl)call).serviceHandlerChain = this.handlerRegistry.serviceHandlerChain;
		((CallImpl)call).portHandlerChain = this.handlerRegistry.portHandlerChain;
		((CallImpl)call).bindingHandlerChain = this.handlerRegistry.bindingHandlerChain;
		((CallImpl)call).setBinding(new BindingImpl());
		//CREATE SOME WRAPPER FUNCTION HERE TO CONVERT ALL THE JAX-RPC
		//INFORMATION TO AXIS2 INFORMATION(SPECIFICALLY HANDLER INFO) 
		try{
			((CallImpl)call).jaxRpcPhase = createAxis2Phase((CallImpl)call);
			if(((CallImpl)call).jaxRpcPhase != null)
				((CallImpl)call).sContext = getAxis2Service();
		} catch (Exception e){
			e.printStackTrace();
		}
		return call;
	}
	
	protected class HandlerRegistryImpl implements HandlerRegistry{
		
		//Adding as an inner class to ServiceImpl.
		// Might need to revisit later, if it needs to be an independent one.
		
		private List<HandlerInfo> serviceHandlerChain;
		private Map<URI, List> bindingHandlerChain;
		private Map<QName, List> portHandlerChain;
		
		private void confirmPort(javax.xml.namespace.QName portName){
			Iterator<QName> ports = null;
			try{
				ports = getPorts();
				while(ports.hasNext()){
					if(ports.next()== portName)
						return;
				}
			}catch(ServiceException se){
				
			}
			throw new java.lang.IllegalArgumentException("Invalid portName");
		}
		
		private void confirmBindingId(java.net.URI bindingId){
			
			if(bindingId.toString().equalsIgnoreCase("http://schemas.xmlsoap.org/wsdl/soap/http"))
				return;
			
			throw new java.lang.IllegalArgumentException("Invalid bindingId");
		}
		
		public java.util.List getHandlerChain(javax.xml.namespace.QName portName) throws java.lang.IllegalArgumentException{
			
			confirmPort(portName);
			if(portHandlerChain == null)
				portHandlerChain = new HashMap<QName, List>();
			if(portHandlerChain.get(portName) == null){
				setHandlerChain(portName, new ArrayList());
			}
			return portHandlerChain.get(portName);
		}
		
		public void setHandlerChain(javax.xml.namespace.QName portName,
				java.util.List chain) throws WebServiceException, java.lang.UnsupportedOperationException,
				java.lang.IllegalArgumentException{
			
			confirmPort(portName);
			if(portHandlerChain == null)
				portHandlerChain = new HashMap<QName, List>();
			portHandlerChain.put(portName, chain);
		}
		
		public java.util.List<HandlerInfo> getHandlerChain(){
			if(serviceHandlerChain == null)
				serviceHandlerChain = new ArrayList<HandlerInfo>();
			return serviceHandlerChain;
		}
		
		public void setHandlerChain(java.util.List<HandlerInfo> chain) throws java.lang.UnsupportedOperationException, WebServiceException{
			serviceHandlerChain = chain;
		}
		
		public java.util.List<HandlerInfo> getHandlerChain(java.net.URI bindingId) throws java.lang.IllegalArgumentException{
			confirmBindingId(bindingId);
			if(bindingHandlerChain == null)
				bindingHandlerChain = new HashMap<URI, List>();
			if(bindingHandlerChain.get(bindingId) == null){
				setHandlerChain(bindingId, new ArrayList());
			}
			return bindingHandlerChain.get(bindingId);
		}
		
		public void setHandlerChain(java.net.URI bindingId,
				java.util.List<HandlerInfo> chain) throws WebServiceException,
				java.lang.UnsupportedOperationException, java.lang.IllegalArgumentException{
			
			confirmBindingId(bindingId);
			if(bindingHandlerChain == null)
				bindingHandlerChain = new HashMap<URI, List>();
			bindingHandlerChain.put(bindingId, chain);
		}
	}

	/**
	 * Method createCall
	 * Creates a Call instance. 
	 * @param portName Qualified name for the target service endpoint
	 * @param operationName Qualified Name of the operation for which this 
	 * Call object is to be created.
	 * @return Call instance 
	 * @throws ServiceException If any error in the creation of the Call object
	 */
	public Call createCall(QName portName, QName operationName) throws 
	ServiceException {
		
		Call call = createCall(portName);
		// Question: Should we not prefill more information from operationName
		// something like input params and return type.
		// Answer: Spec allows for overloaded operation names, so lets wait
		// till user configures if he wants to configure any params and we
		// will do the check and default operation pickup (in case params are
		// not configured etc. things in Call.invoke
		call.setOperationName(operationName);
		return call;
	}

	/**
	 * Method createCall
	 * Creates a Call instance. 
	 * @param portName Qualified name for the target service endpoint
	 * @param operationName Name of the operation for which this Call object 
	 * is to be created.
	 * @return Call instance
	 * @throws ServiceException If any error in the creation of the Call 
	 * object
	 */
	public Call createCall(QName portName, String operationName) throws 
	ServiceException {
		QName dummyQNameForOperationName = new QName(operationName);
		return createCall(portName, dummyQNameForOperationName);
	}

	/**
	 * Method createCall
	 * Creates a Call instance.
	 * @param portName Qualified name for the target service endpoint 
	 * @return Call instance 
	 * @throws ServiceException If any error in the creation of the Call 
	 * object
	 */
	public Call createCall(QName portName) throws ServiceException {
		
		if(wsdlService == null)
			throw new ServiceException("A service wasn't yet created from wsdl");
		
		Call call = createCall();

		URL wsdlLocationURL;
		try {
			wsdlLocationURL = new URL(wsdlLoc);
		} catch (Exception e) {
			throw new ServiceException(e);
		}
		
		if(parserWrapper==null) {
			//Here am hard coding the parser choice. Should think of better
			//flexible implementation
			parserWrapper = WSDLFactoryImpl.getParser(0, wsdlLocationURL);
		}
		
		if (parserWrapper.getWSDLVersion().equals("1.1")) {
			JAXRPCWSDL11Interface parser = (JAXRPCWSDL11Interface)parserWrapper;
			
			Port port = wsdlService.getPort(portName.getLocalPart());
			/* Is there someway we can populate the targetEndpointAddress?
			 * 
			 * soap:address extensibility element can give that for us
			 */ 
			List extElList = port.getExtensibilityElements();
			if (extElList!=null) {
				for(int i=0; i<extElList.size(); i++) {
					ExtensibilityElement extElement = (ExtensibilityElement)extElList.get(i);
					//We will just do SOAP for now. For HTTP, MIME support we
					//can always revisit.
					if (extElement instanceof SOAPAddress) {
						String tgtEndptAddr = ((SOAPAddress)extElement).getLocationURI();
						call.setTargetEndpointAddress(tgtEndptAddr);
						//spec says only one address should be mentioned
						//So its a waste to iterate over other ext elems,if any
						break; 
					}
				}
			}
			
			Binding binding = parser.getBinding(port);
			QName portTypeName = parser.getPortTypeName(binding);
			call.setPortTypeName(portTypeName);
			// looks like beyond setting the portTypeName and targetEndpointAddress 
			// I can't do more configuring of Call object
		}
		
		return call;
	}
	
	// This involves generics, needs a revisit
	/**
	 * Method createDispatch
	 * Creates a Dispatch instance for use with objects of the users choosing.
	 * 
	 * @param portName - Qualified name for the target service endpoint
	 * @param type - The class of object used to messages or message payloads. 
	 * Implementations are required to support javax.xml.transform.Source and 
	 * javax.xml.soap.SOAPMessage.
	 * @param mode - Controls whether the created dispatch instance is message or 
	 *  payload oriented, i.e. whether the user will work with complete protocol
	 *  messages or message payloads. E.g. when using the SOAP protocol, this 
	 *  parameter controls whether the user will work with SOAP messages or the
	 *  contents of a SOAP body. Mode must be MESSAGE when type is SOAPMessage.
	 * @return Dispatch instance 
	 * @throws ServiceException - If any error in the creation of the Dispatch
	 *  object
	 * @see javax.xml.transform.Source, javax.xml.soap.SOAPMessage
	 */
	public <T> Dispatch<T> createDispatch(QName portName, Class<T> type, 
			Mode mode) throws ServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	// This involves generics, needs a revisit
	/**
	 * Method createDispatch
	 * Creates a Dispatch instance for use with JAXB generated objects.
	 * 
	 * @param portName - Qualified name for the target service endpoint
	 * @param context - The JAXB context used to marshall and unmarshall 
	 * messages or message payloads.
	 * @param mode - Controls whether the created dispatch instance is message
	 *  or payload oriented, i.e. whether the user will work with complete 
	 *  protocol messages or message payloads. E.g. when using the SOAP 
	 *  protocol, this parameter controls whether the user will work with 
	 *  SOAP messages or the contents of a SOAP body.
	 * @return Dispatch instance 
	 * @throws ServiceException - If any error in the creation of the Dispatch
	 *  object
	 * @see JAXBContext
	 */
	public Dispatch<Object> createDispatch(QName portName, JAXBContext context,
			Mode mode) throws ServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Method createPort
	 * Creates a new port for the service. Ports created in this way contain 
	 * no WSDL port type information and can only be used for creating Dispatch
	 *  and Call instances.
	 * @param portName Qualified name for the target service endpoint
	 * @param bindingId A URI identifier of a binding.
	 * @param endpointAddress Address of the target service endpoint as a URI 
	 * @throws ServiceException If any error in the creation of the port
	 * @see javax.xml.ws.soap.SOAPBinding.SOAP11HTTP_BINDING
	 */
	public void createPort(QName portName, URI bindingId, 
			String endpointAddress) throws ServiceException {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Method getCalls
	 * Gets an array of preconfigured Call objects for invoking operations on 
	 * the specified port. There is one Call object per operation that can be 
	 * invoked on the specified port. Each Call object is pre-configured and 
	 * does not need to be configured using the setter methods on Call 
	 * interface.
	 * Each invocation of the getCalls method returns a new array of 
	 * preconfigured Call objects 
	 * This method requires the Service implementation class to have access to
	 * the WSDL related metadata.
	 * @param portName Qualified name for the target service endpoint 
	 * @return Call[] Array of pre-configured Call objects 
	 * @throws ServiceException If this Service class does not have access to 
	 * the required WSDL metadata or if an illegal portName is specified.
	 */
	public Call[] getCalls(QName portName) throws ServiceException {
		
		//Logic here is that corresponding to this portName
		//identify the portType and within it identify all the
		//operations in it.
		//For each operation, create a call object, configure it and
		//add it to an array of call objects. Configuring of call
		//objects would be typically setting the TargetEndPointAddress, 
		//setting the operationName, portName, portType, adding 
		//parameters and returnType.

		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Method getHandlerRegistry
	 * Returns the configured HandlerRegistry instance for this Service 
	 * instance.
	 * @return HandlerRegistry
	 * @throws java.lang.UnsupportedOperationException if the Service class 
	 * does not support the configuration of a HandlerRegistry
	 */
	public HandlerRegistry getHandlerRegistry() throws 
	UnsupportedOperationException {
		//Actually at the deployment configuration time positively the handler
		//registry must be populated with handler chain info.
		return handlerRegistry;
	}

	/**
	 * Method getPort
	 * The getPort method returns either an instance of a generated stub 
	 * implementation class or a dynamic proxy. The parameter 
	 * serviceEndpointInterface specifies the service endpoint interface that 
	 * is supported by the returned stub or proxy. In the implementation of 
	 * this method, the JAX-RPC runtime system takes the responsibility of 
	 * selecting a protocol binding (and a port) and configuring the stub 
	 * accordingly. The returned Stub instance should not be reconfigured by 
	 * the client.
	 * @param serviceEndpointInterface Service endpoint interface 
	 * @return Stub instance or dynamic proxy that supports the specified 
	 * service endpoint interface 
	 * @throws ServiceException This exception is thrown in the following 
	 * cases:
	 * 1. If there is an error during creation of stub instance or dynamic 
	 * proxy 
	 * 2. If there is any missing WSDL metadata as required by this method
	 * 3. Optionally, if an illegal serviceEndpointInterface is specified 
	 */
	public Remote getPort(Class serviceEndpointInterface) throws 
	ServiceException {
		//will return a generated stub if exists. else since binding
		//choice is left to the implementation we will create a SOAPBinding
		//proxy and return that.
		Stub generatedStub = getGeneratedStub(serviceEndpointInterface);
		if (generatedStub != null)
			return (Remote)generatedStub;
		else {
			Stub dynamicProxy = createSOAPBindingProxy(serviceEndpointInterface);
			return (Remote)dynamicProxy;
		}
	}

	/**
	 * Method getPort
	 * The getPort method returns either an instance of a generated stub 
	 * implementation class or a dynamic proxy. A service client uses this 
	 * dynamic proxy to invoke operations on the target service endpoint. The 
	 * serviceEndpointInterface specifies the service endpoint interface that 
	 * is supported by the created dynamic proxy or stub instance.
	 * @param portName Qualified name of the service endpoint in the WSDL 
	 * service description
	 * @param serviceEndpointInterface Service endpoint interface supported by 
	 * the dynamic proxy or stub instance 
	 * @return java.rmi.Remote Stub instance or dynamic proxy that supports the 
	 * specified service endpoint interface 
	 * @throws ServiceException This exception is thrown in the following 
	 * cases:
	 * 1. If there is an error in creation of the dynamic proxy or stub 
	 * instance
	 * 2. If there is any missing WSDL metadata as required by this method 
	 * 3. Optionally, if an illegal serviceEndpointInterface or portName is 
	 * specified
	 * @see java.lang.reflect.Proxy, java.lang.reflect.InvocationHandler 
	 */
	public Remote getPort(QName portName, Class serviceEndpointInterface) 
	throws ServiceException {
		//Given SEI is the java interface corresponding to a portType and that
		//which should be supported by the returned Stub instance
		
		//We will first try to return a generated stub instance. If that fails
		//we will create a dynamic proxy and return that
		Stub generatedStub = getGeneratedStub(serviceEndpointInterface);
		if(generatedStub != null)
			return (Remote)generatedStub;
		else {
			//should create a dynamic proxy and return instance of that.
			
			/*(i) A dynamic proxy that we create should have to implement the
			 * provided serviceEndpointInterface
			 *(ii) The sei given ofcourse should be checked to extend Remote
			 * and that it is an interface.
			 *(iii) Should we make it to extend our StubImpl.java(?). We should I guess
			 *(iv) Before creating the stub, we should look at the kind of
			 * binding that we should use for the Stub and appropriately call
			 * either createSOAPBindingStub or createHTTPBindingStub etc.
			 */ 
			
			Port port = wsdlService.getPort(portName.getLocalPart());
			if(port==null)
				throw new ServiceException("No port exists with given portName");
			Binding binding = port.getBinding();
			List extElList = binding.getExtensibilityElements();
			if(extElList!=null) {
				for(int i=0; extElList.size()> i; i++) {
					ExtensibilityElement extEl = (ExtensibilityElement)extElList.get(i);
					//we will only worry of SOAPBinding
					if(extEl instanceof SOAPBinding) {
						//identified binding linkage is SOAPBinding
						Stub dynamicProxy = createSOAPBindingProxy(serviceEndpointInterface);
						//Since only one protocl MUST be specified, we needn't
						//iterate over other extensible elements.
						return (Remote)dynamicProxy;
					}
				}
				throw new ServiceException("Binding protocol not supported. Failed to create dynamic proxy");
			}
			throw new ServiceException("No binding protocol identified. Try giving SOAP binding.");
		}
		
	}

	/**
	 * 
	 */
	private Stub getGeneratedStub(Class serviceEndpointInterface) throws ServiceException{
		
		//first, error checking plz!
		if(!serviceEndpointInterface.isInterface()) {
			throw new ServiceException("To create a dynamic proxy, provided SEI should be an interface");
		}
		if(!(java.rmi.Remote.class.isAssignableFrom(serviceEndpointInterface))) {
			throw new ServiceException("Provided SEI MUST extend java.rmi.Remote");
		}
		
		//The logic here is to interpet a name with which a Stub might
		//have been created, if at all generated. And try to instantiate it
		//and return the instance.
		//If not a bingo. We MUST return null.
		//During class loading and instantiation should an exception arise, they
		//would be wrapped as ServiceException and thrown
		
		//TODO method incomplete
		return null;
	}
	
	/**
	 * 
	 */
	private Stub createSOAPBindingProxy(Class serviceEndpointInterface) throws ServiceException {
		/*
		 * (i) A dynamic proxy that we create should have to implement the
		 * provided serviceEndpointInterface
		 *(ii) The sei given ofcourse should be checked to extend Remote
		 * and that it is an interface.
		 *(iii) Should we make it to extend our StubImpl.java(?). We should I guess
		 *(iv) Lot more to do...yet to decide what all I should do here
		 */
		if(!serviceEndpointInterface.isInterface()) {
			throw new ServiceException("To create a dynamic proxy, provided SEI should be an interface");
		}
		if(!(java.rmi.Remote.class.isAssignableFrom(serviceEndpointInterface))) {
			throw new ServiceException("Provided SEI MUST extend java.rmi.Remote");
		}
		
		//TODO method incomplete
		return null;
	}
	
	/**
	 * Method getPorts
	 * Returns an Iterator for the list of QNames of service endpoints grouped 
	 * by this service
	 * @return Returns java.util.Iterator with elements of type 
	 * javax.xml.namespace.QName 
	 * @throws ServiceException If this Service class does not have access to 
	 * the required WSDL metadata
	 */
	public Iterator getPorts() throws ServiceException {
		if(wsdlService==null)
			throw new ServiceException("No wsdl service. Check WSDL once");
		
		Map portsMap = wsdlService.getPorts();
		Object[] portNames = portsMap.keySet().toArray();
		ArrayList<QName> portQNames = new ArrayList<QName>();
		for(int i=0; portNames[i]!=null; i++) {
			QName qNameOfPort = new QName(portNames[i].toString());
			portQNames.add(qNameOfPort);
		}
		return portQNames.iterator();
	}

	/**
	 * Method getSecurityConfiguration
	 * Gets the SecurityConfiguration for this Service object. The returned 
	 * SecurityConfiguration instance is used to initialize the security 
	 * configuration of BindingProvider instance created using this Service 
	 * object.
	 * @return The SecurityConfiguration for this Service object.
	 * @throws java.lang.UnsupportedOperationException if the Service class 
	 * does not support the configuration of SecurityConfiguration.
	 */
	public SecurityConfiguration getSecurityConfiguration() throws 
	UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	/**
	 * Method getServiceName
	 * Gets the name of this service.
	 * @return Qualified name of this service
	 */
	public QName getServiceName() {
		if (wsdlService!=null)
			return null;
		return wsdlService.getQName();
	}

	/**
	 * Method getTypeMappingRegistry
	 * Gets the TypeMappingRegistry for this Service object. The returned 
	 * TypeMappingRegistry instance is pre-configured to support the standard 
	 * type mapping between XML and Java types types as required by the 
	 * JAX-RPC specification.
	 * @return The TypeMappingRegistry for this Service object.
	 * @throws java.lang.UnsupportedOperationException if the Service class 
	 * does not support the configuration of TypeMappingRegistry.
	 */
	public TypeMappingRegistry getTypeMappingRegistry() throws 
	UnsupportedOperationException {
		//If the implementation is using JAXB, we should throw exception
		//Usage of JAXB might be using a flag. This flag, I feel, is most
		//appropriate if placed in the way the ServiceFactory itself is 
		//instantiated. ServiceFactory.newInstance(boolean JAXB_USAGE)
		//The flag can be copied to a boolean value in the Service object
		//that gets created with sf.createService(...) method.
		
		if (JAXB_USAGE)
			throw new UnsupportedOperationException();
		else
			return typeMappingRegistry;
			//But positively at some point of execution flow
			//typeMappingRegistry must be populated.
	}

	/**
	 * Method getWSDLDocumentLocation
	 * Gets the location of the WSDL document for this Service. 
	 * @return URL for the location of the WSDL document for this service
	 */
	public URL getWSDLDocumentLocation() {
		try {
			URL returnValue = new URL(wsdlLoc);
			return returnValue;
		} catch (Exception e) {
			return null;
		}
	}

	public ServiceImpl() {
		super();
	}
	
	public ServiceImpl(JAXRPCWSDLInterface parserWrap, Service wsdlSvc) {
		super();
		this.parserWrapper = parserWrap;
		this.wsdlService = wsdlSvc;
	}
	
	public ServiceImpl(JAXRPCWSDLInterface parserWrap, Service wsdlSvc, boolean jaxbUsage) {
		super();
		this.parserWrapper = parserWrap;
		this.wsdlService = wsdlSvc;
		this.JAXB_USAGE = jaxbUsage;
	}

	/**
	 * @return Returns the JAXB_USAGE.
	 */
	public boolean isJAXB_USAGE() {
		return this.JAXB_USAGE;
	}

	/**
	 * @param jaxb_usage The JAXB_USAGE to set.
	 */
	public void setJAXB_USAGE(boolean jaxb_usage) {
		this.JAXB_USAGE = jaxb_usage;
	}

}
