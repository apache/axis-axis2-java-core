/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.jaxws.spi;

import javax.xml.ws.handler.HandlerResolver;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.java.security.AccessController;
import org.apache.axis2.jaxws.binding.BindingImpl;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.client.PropertyMigrator;
import org.apache.axis2.jaxws.client.dispatch.JAXBDispatch;
import org.apache.axis2.jaxws.client.dispatch.XMLDispatch;
import org.apache.axis2.jaxws.client.proxy.JAXWSProxyHandler;
import org.apache.axis2.jaxws.description.DescriptionFactory;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.description.ServiceDescriptionWSDL;
import org.apache.axis2.jaxws.handler.HandlerResolverImpl;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.axis2.jaxws.spi.migrator.ApplicationContextMigratorUtil;
import org.apache.axis2.jaxws.util.WSDLWrapper;
import org.apache.axis2.jaxws.utility.ExecutorFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.activation.DataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.Service.Mode;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.http.HTTPBinding;
import javax.xml.ws.soap.SOAPBinding;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Iterator;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * The ServiceDelegate serves as the backing implementation for all of the methods in the {@link
 * javax.xml.ws.Service} API.  This is the plug point for the client implementation.
 */
public class ServiceDelegate extends javax.xml.ws.spi.ServiceDelegate {
    private static final Log log = LogFactory.getLog(ServiceDelegate.class);
    private Executor executor;

    private ServiceDescription serviceDescription;
    private QName serviceQname;
    private ServiceClient serviceClient = null;

    private HandlerResolver handlerResolver = null;
    
    public ServiceDelegate(URL url, QName qname, Class clazz) throws WebServiceException {
        super();
        this.serviceQname = qname;

        if (!isValidServiceName()) {
            throw ExceptionFactory
                    .makeWebServiceException(Messages.getMessage("serviceDelegateConstruct0", ""));
        }
        serviceDescription = DescriptionFactory.createServiceDescription(url, serviceQname, clazz);
        // TODO: This check should be done when the Service Description is created above; that should throw this exception.
        // That is because we (following the behavior of the RI) require the WSDL be fully specified (not partial) on the client
        if (isValidWSDLLocation()) {
            if (!isServiceDefined(serviceQname)) {
                throw ExceptionFactory.makeWebServiceException(Messages.getMessage(
                        "serviceDelegateConstruct0", serviceQname.toString(), url.toString()));
            }
        }

        // Register the necessary ApplicationContextMigrators
        ApplicationContextMigratorUtil
                .addApplicationContextMigrator(serviceDescription.getAxisConfigContext(),
                                               Constants.APPLICATION_CONTEXT_MIGRATOR_LIST_ID,
                                               new PropertyMigrator());
    }

    //================================================
    // JAX-WS API methods
    //================================================

    /*
    * (non-Javadoc)
    * @see javax.xml.ws.spi.ServiceDelegate#addPort(javax.xml.namespace.QName, java.lang.String, java.lang.String)
    */
    // Creates a DISPATCH ONLY port.  Per JAXWS Sec 4.1 javax.xm..ws.Service, p. 49, ports added via addPort method
    // are only suitibale for creating Distpach instances.
    public void addPort(QName portName, String bindingId, String endpointAddress)
            throws WebServiceException {
    	if(endpointAddress!=null && endpointAddress.trim().length()==0){
    		ExceptionFactory.makeWebServiceException(Messages.getMessage("addPortErr1", (portName!=null)?portName.getLocalPart():"", endpointAddress));
    	}
        EndpointDescription endpointDesc =
                DescriptionFactory.updateEndpoint(serviceDescription, null, portName,
                                                  DescriptionFactory.UpdateType.ADD_PORT);
        // TODO: Need to set endpointAddress and set or check bindingId on the EndpointDesc
        endpointDesc.setEndpointAddress(endpointAddress);
        endpointDesc.setClientBindingID(bindingId);
    }

    /*
    * (non-Javadoc)
    * @see javax.xml.ws.spi.ServiceDelegate#createDispatch(javax.xml.namespace.QName, java.lang.Class, javax.xml.ws.Service.Mode)
    */
    public <T> Dispatch<T> createDispatch(QName qname, Class<T> clazz, Mode mode)
            throws WebServiceException {
        if (qname == null) {
            throw ExceptionFactory
                    .makeWebServiceException(Messages.getMessage("createDispatchFail0"));
        }
        if (!isValidDispatchType(clazz)) {
            throw ExceptionFactory
                    .makeWebServiceException(Messages.getMessage("dispatchInvalidType"));
        }

        EndpointDescription endpointDesc =
                DescriptionFactory.updateEndpoint(serviceDescription, null, qname,
                                                  DescriptionFactory.UpdateType.CREATE_DISPATCH);
        if (endpointDesc == null) {
            throw ExceptionFactory.makeWebServiceException(
                    Messages.getMessage("createDispatchFail2", qname.toString()));
        }

        XMLDispatch<T> dispatch = new XMLDispatch<T>(this, endpointDesc);
        
        // FIXME: This call needs to be revisited.  Not really sure what we're trying to do here. 
        dispatch.setBinding(addBinding(endpointDesc, endpointDesc.getClientBindingID()));
        if (mode != null) {
            dispatch.setMode(mode);
        } else {
            dispatch.setMode(Service.Mode.PAYLOAD);
        }

        if (serviceClient == null)
            serviceClient = getServiceClient(qname);

        dispatch.setServiceClient(serviceClient);
        dispatch.setType(clazz);
        return dispatch;
    }

    /*
    * (non-Javadoc)
    * @see javax.xml.ws.spi.ServiceDelegate#createDispatch(javax.xml.namespace.QName, javax.xml.bind.JAXBContext, javax.xml.ws.Service.Mode)
    */
    public Dispatch<java.lang.Object> createDispatch(QName qname, JAXBContext context, Mode mode) {
        if (qname == null) {
            throw ExceptionFactory
                    .makeWebServiceException(Messages.getMessage("createDispatchFail0"));
        }

        EndpointDescription endpointDesc =
                DescriptionFactory.updateEndpoint(serviceDescription, null, qname,
                                                  DescriptionFactory.UpdateType.CREATE_DISPATCH);
        if (endpointDesc == null) {
            throw ExceptionFactory.makeWebServiceException(
                    Messages.getMessage("createDispatchFail2", qname.toString()));
        }

        JAXBDispatch<Object> dispatch = new JAXBDispatch(this, endpointDesc);
        dispatch.setBinding(addBinding(endpointDesc, endpointDesc.getClientBindingID()));

        if (mode != null) {
            dispatch.setMode(mode);
        } else {
            dispatch.setMode(Service.Mode.PAYLOAD);
        }

        if (serviceClient == null)
            serviceClient = getServiceClient(qname);

        dispatch.setJAXBContext(context);
        dispatch.setServiceClient(serviceClient);

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
         * (JLB): I'm not sure lack of WSDL should cause an exception
         */


        if (!isValidWSDLLocation()) {
            //TODO: Should I throw Exception if no WSDL
            //throw ExceptionFactory.makeWebServiceException("WSLD Not found");
        }
        if (sei == null) {
            throw ExceptionFactory.makeWebServiceException(
                    Messages.getMessage("getPortInvalidSEI", portName.toString(), "null"));
        }

        EndpointDescription endpointDesc =
                DescriptionFactory.updateEndpoint(serviceDescription, sei, portName,
                                                  DescriptionFactory.UpdateType.GET_PORT);
        if (endpointDesc == null) {
            // TODO: NLS
            throw ExceptionFactory.makeWebServiceException(
                    "Unable to getPort for port QName " + portName.toString());
        }

        String[] interfacesNames = 
            new String [] {sei.getName(), org.apache.axis2.jaxws.spi.BindingProvider.class.getName()};
        
        // As required by java.lang.reflect.Proxy, ensure that the interfaces
        // for the proxy are loadable by the same class loader. 
        Class[] interfaces = null;
        // First, let's try loading the interfaces with the SEI classLoader
        ClassLoader classLoader = getClassLoader(sei);
        try {
            interfaces = loadClasses(classLoader, interfacesNames);
        } catch (ClassNotFoundException e1) {
            // Let's try with context classLoader now
            classLoader = getContextClassLoader();
            try {
                interfaces = loadClasses(classLoader, interfacesNames);
            } catch (ClassNotFoundException e2) {
                // TODO: NLS
                throw ExceptionFactory.makeWebServiceException("Unable to load proxy classes", e2);
            }
        }
        
        JAXWSProxyHandler proxyHandler = new JAXWSProxyHandler(this, interfaces[0], endpointDesc);
        Object proxyClass = Proxy.newProxyInstance(classLoader, interfaces, proxyHandler);
        return sei.cast(proxyClass);
    }

    /*
    * (non-Javadoc)
    * @see javax.xml.ws.spi.ServiceDelegate#getExecutor()
    */
    public Executor getExecutor() {
        //FIXME: Use client provider executor too.
        if (executor == null) {
            executor = getDefaultExecutor();
        }
        return executor;
    }

    /*
    * (non-Javadoc)
    * @see javax.xml.ws.spi.ServiceDelegate#getHandlerResolver()
    */
    public HandlerResolver getHandlerResolver() {
        if (handlerResolver == null) {
            handlerResolver = new HandlerResolverImpl(serviceDescription);
        }
        return handlerResolver;
    }

    /*
    * (non-Javadoc)
    * @see javax.xml.ws.spi.ServiceDelegate#getPorts()
    */
    public Iterator<QName> getPorts() {
        return getServiceDescription().getPorts().iterator();
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
        return ((ServiceDescriptionWSDL)serviceDescription).getWSDLLocation();
    }

    /*
    * (non-Javadoc)
    * @see javax.xml.ws.spi.ServiceDelegate#setExecutor(java.util.concurrent.Executor)
    */
    public void setExecutor(Executor e) {
        if (e == null) {
            throw ExceptionFactory
                    .makeWebServiceException(Messages.getMessage("cannotSetExcutorToNull"));
        }

        executor = e;
    }

    /*
    * (non-Javadoc)
    * @see javax.xml.ws.spi.ServiceDelegate#setHandlerResolver(javax.xml.ws.handler.HandlerResolver)
    */
    public void setHandlerResolver(HandlerResolver handlerresolver) {
        this.handlerResolver = handlerresolver;
    }

    //================================================
    // Internal public APIs
    //================================================

    /** Get the ServiceDescription tree that this ServiceDelegate */
    public ServiceDescription getServiceDescription() {
        return serviceDescription;
    }

    //TODO Change when ServiceDescription has to return ServiceClient or OperationClient

    /**
     * 
     */
    public ServiceClient getServiceClient(QName portQName) throws WebServiceException {
        return serviceDescription.getServiceClient(portQName);
    }

    //================================================
    // Impl methods
    //================================================

    //TODO: Need to make the default number of threads configurable
    private Executor getDefaultExecutor() {
    	ExecutorFactory executorFactory = (ExecutorFactory) FactoryRegistry.getFactory(
    			ExecutorFactory.class);
    	return executorFactory.getExecutorInstance();
    }

    private boolean isValidServiceName() {
        return serviceQname != null && !"".equals(serviceQname.toString().trim());
    }

    private boolean isValidWSDLLocation() {
        URL wsdlLocation = getWSDLDocumentLocation();
        return wsdlLocation != null && !"".equals(wsdlLocation.toString().trim());
    }

    // TODO: Remove this method and put the WSDLWrapper methods on the ServiceDescriptor directly
    private WSDLWrapper getWSDLWrapper() {
        return ((ServiceDescriptionWSDL)serviceDescription).getWSDLWrapper();
    }

    private boolean isServiceDefined(QName serviceName) {
        return getWSDLWrapper().getService(serviceName) != null;
    }

    private BindingImpl addBinding(EndpointDescription endpointDesc, String bindingId) {
        // TODO: before creating binding do I have to do something with Handlers ... how is Binding related to Handler, this mistry sucks!!!
        if (bindingId != null) {
            //TODO: create all the bindings here
            if (bindingId.equals(SOAPBinding.SOAP11HTTP_BINDING)) {
                //instantiate soap11 binding implementation here and call setBinding in BindingProvider
                return new org.apache.axis2.jaxws.binding.SOAPBinding(endpointDesc);
            }

            if (bindingId.equals(SOAPBinding.SOAP12HTTP_BINDING)) {
                //instantiate soap11 binding implementation here and call setBinding in BindingProvider
                return new org.apache.axis2.jaxws.binding.SOAPBinding(endpointDesc);
            }

            if (bindingId.equals(HTTPBinding.HTTP_BINDING)) {
                //instantiate http binding implementation here and call setBinding in BindingProvider
                return new org.apache.axis2.jaxws.binding.HTTPBinding(endpointDesc);
            }
        } 
        return new org.apache.axis2.jaxws.binding.SOAPBinding(endpointDesc);
    }

    private boolean isValidDispatchType(Class clazz) {
        return clazz != null && (clazz == String.class ||
                clazz == Source.class ||
                clazz == DataSource.class ||
                clazz == SOAPMessage.class);
    }

    /** @return ClassLoader */
    private static ClassLoader getClassLoader(final Class cls) {
        // NOTE: This method must remain private because it uses AccessController
        ClassLoader cl = null;
        try {
            cl = (ClassLoader)AccessController.doPrivileged(
                    new PrivilegedExceptionAction() {
                        public Object run() throws ClassNotFoundException {
                            return cls.getClassLoader();
                        }
                    }
            );
        } catch (PrivilegedActionException e) {
            if (log.isDebugEnabled()) {
                log.debug("Exception thrown from AccessController: " + e);
            }
            throw ExceptionFactory.makeWebServiceException(e.getException());
        }

        return cl;
    }
    
    /** @return ClassLoader */
    private static ClassLoader getContextClassLoader() {
        // NOTE: This method must remain private because it uses AccessController
        ClassLoader cl = null;
        try {
            cl = (ClassLoader)AccessController.doPrivileged(
                    new PrivilegedExceptionAction() {
                        public Object run() throws ClassNotFoundException {
                            return Thread.currentThread().getContextClassLoader();
                        }
                    }
            );
        } catch (PrivilegedActionException e) {
            if (log.isDebugEnabled()) {
                log.debug("Exception thrown from AccessController: " + e);
            }
            throw ExceptionFactory.makeWebServiceException(e.getException());
        }

        return cl;
    }
    
    /**
     * Return the class for this name
     *
     * @return Class
     */
    private static Class forName(final String className, final boolean initialize,
                                 final ClassLoader classLoader) throws ClassNotFoundException {
        // NOTE: This method must remain protected because it uses AccessController
        Class cl = null;
        try {
            cl = (Class)AccessController.doPrivileged(
                    new PrivilegedExceptionAction() {
                        public Object run() throws ClassNotFoundException {
                            return Class.forName(className, initialize, classLoader);
                        }
                    }
            );
        } catch (PrivilegedActionException e) {
            if (log.isDebugEnabled()) {
                log.debug("Exception thrown from AccessController: " + e);
            }
            throw (ClassNotFoundException)e.getException();
        }

        return cl;
    }
    
    private static Class[] loadClasses(ClassLoader classLoader, String[] classNames)
        throws ClassNotFoundException {
        Class[] classes = new Class[classNames.length];
        for (int i = 0; i < classNames.length; i++) {
            classes[i] = forName(classNames[i], true, classLoader);
        }
        return classes;
    }
    
}
