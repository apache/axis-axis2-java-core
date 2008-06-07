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

package org.apache.axis2.jaxws.description.impl;

/**
 * 
 */

import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.EndpointReferenceHelper;
import org.apache.axis2.addressing.metadata.ServiceName;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.jaxws.ClientConfigurationFactory;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.description.DescriptionFactory;
import org.apache.axis2.jaxws.description.DescriptionKey;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.ResolvedHandlersDescription;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.description.builder.DescriptionBuilderComposite;
import org.apache.axis2.jaxws.description.builder.converter.JavaClassToDBCConverter;
import org.apache.axis2.jaxws.description.validator.EndpointDescriptionValidator;
import org.apache.axis2.jaxws.description.validator.ServiceDescriptionValidator;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Creates the JAX-WS metadata descritpion hierachy from some combinations of WSDL, Java classes
 * with annotations, and (in the future) deployment descriptors.  This is the implementation and is
 * not intended to be a public API.  The API is:
 *
 * @see org.apache.axis2.jaxws.description.DescriptionFactory
 */
public class DescriptionFactoryImpl {
    private static final Log log = LogFactory.getLog(DescriptionFactoryImpl.class);
    private static Map<DescriptionKey, ServiceDescription> cache =
            new Hashtable<DescriptionKey, ServiceDescription>();

    /** A DescrptionFactory can not be instantiated; all methods are static. */
    private DescriptionFactoryImpl() {
    }

    /**
     * @see org.apache.axis2.jaxws.description.DescriptionFactory#createServiceDescription(URL, QName, Class, DescriptionBuilderComposite)
     */
    public static ServiceDescription createServiceDescription(URL wsdlURL, 
                                                              QName serviceQName, 
                                                              Class serviceClass) {
        return createServiceDescription(wsdlURL, serviceQName, serviceClass, null, null);
        
    }

    /**
     * @see org.apache.axis2.jaxws.description.DescriptionFactory#createServiceDescription(URL,
     *      QName, Class)
     */
    public static ServiceDescription createServiceDescription(URL wsdlURL,
                                                              QName serviceQName,
                                                              Class serviceClass,
                                                              DescriptionBuilderComposite sparseComposite,
                                                              Object sparseCompositeKey) {
        ConfigurationContext configContext = DescriptionFactory.createClientConfigurationFactory()
                .getClientConfigurationContext();
        DescriptionKey key = new DescriptionKey(serviceQName, wsdlURL, serviceClass, configContext);
        if (log.isDebugEnabled()) {
            log.debug("Cache Map = " + cache.toString());
            if (key != null)
                log.debug("Description Key = " + key.printKey());

        }
        ServiceDescription serviceDesc = null;
        synchronized(configContext) {
            serviceDesc = cache.get(key);
            if (log.isDebugEnabled()) {
                log.debug("Check to see if ServiceDescription is found in cache");
            }
            if (serviceDesc != null) {
                if (log.isDebugEnabled()) {
                    log.debug("ServiceDescription found in the cache");
                    log.debug(serviceDesc.toString());
                }
            }
            if (serviceDesc == null) {
                if (log.isDebugEnabled()) {
                    log.debug("ServiceDescription not found in the cache");
                    log.debug(" creating new ServiceDescriptionImpl");
                }

                ServiceDescriptionImpl serviceDescImpl = null;
                if (sparseComposite != null) {
                    serviceDescImpl = new ServiceDescriptionImpl(wsdlURL, serviceQName,
                                                                 serviceClass, sparseComposite, 
                                                                 sparseCompositeKey);
                    if (log.isDebugEnabled()) {
                        log.debug("Client-side service description created with service class: " + serviceClass
                                  + ", Service QN: " + serviceQName
                                  + ", and DBC: " + sparseComposite);
                        log.debug(serviceDescImpl.toString());
                    }

                } else {
                    serviceDescImpl = new ServiceDescriptionImpl(wsdlURL, serviceQName, serviceClass);
                }
                serviceDescImpl.setAxisConfigContext(configContext);
                
                serviceDesc = serviceDescImpl;
                if (log.isDebugEnabled()) {
                    log.debug("ServiceDescription created with WSDL URL: " + wsdlURL + "; QName: " +
                        serviceQName + "; Class: " + serviceClass);
                    log.debug(serviceDesc.toString());
                }
                if (log.isDebugEnabled()) {
                    log.debug("Caching new ServiceDescription in the cache");
                }
                cache.put(key, serviceDesc);
            } else {
                // A service description was found in the cache.  If a sparse composite was
                // specified, then set it on the found service desc
                ((ServiceDescriptionImpl) serviceDesc).getDescriptionBuilderComposite().
                    setSparseComposite(sparseCompositeKey, sparseComposite);
            }
        }
        return serviceDesc;
    }

    /**
     * Clears the entire ServiceDescription cache.
     * 
     * <h4>Note</h4>     
     * This function might cause unpredictable results when configuration contexts are being reused
     * and/or there are outstanding requests using the cached ServiceDescription objects. Also, 
     * in-flight requests (both client and server) using ServiceDelegates MUST be done and out of
     * scope before this method is called.
     * 
     */
    public static void clearServiceDescriptionCache() {
        cache.clear();
    }
    
    /**
     * Clears all the ServiceDescription objects in the cache associated with the specified 
     * configuration context.
     * 
     * <h4>Note</h4>
     * This function should only be used to clear the cache when the specified configuration context
     * will not be used anymore and there are no outstanding requests using the associated 
     * ServiceDescription objects. Also, in-flight requests (both client and server) using 
     * ServiceDelegates MUST be done and out of scope before this method is called.      
     * Otherwise, unpredictable results might occur.
     * 
     * @param configContext The configuration context associated with the ServiceDescription 
     *                      objects in the cache.
     */
    public static void clearServiceDescriptionCache(ConfigurationContext configContext) {
        if (configContext == null) {
            return;
        }
        synchronized (configContext) {
            synchronized (cache) {
                Iterator<DescriptionKey> iter = cache.keySet().iterator();
                while (iter.hasNext()) {
                    DescriptionKey key = iter.next();
                    if (key.getConfigContext() == configContext) {
                        iter.remove();
                    }
                }
            }
        }
    }    
    
    /** @see org.apache.axis2.jaxws.description.DescriptionFactory#createServiceDescription(Class) */
    public static ServiceDescription createServiceDescription(Class serviceImplClass) {
        return createServiceDescription(serviceImplClass, null);
    }

    /** @see org.apache.axis2.jaxws.description.DescriptionFactory#createServiceDescription(Class) */
    public static ServiceDescription createServiceDescription(Class serviceImplClass, ConfigurationContext configContext) {
        ServiceDescription serviceDesc = null;

        if (serviceImplClass != null) {
            JavaClassToDBCConverter converter = new JavaClassToDBCConverter(serviceImplClass);
            HashMap<String, DescriptionBuilderComposite> dbcMap = converter.produceDBC();
            List<ServiceDescription> serviceDescList = createServiceDescriptionFromDBCMap(dbcMap, configContext);
            if (serviceDescList != null && serviceDescList.size() > 0) {
                serviceDesc = serviceDescList.get(0);
                if (log.isDebugEnabled()) {
                    log.debug("ServiceDescription created with class: " + serviceImplClass);
                    log.debug(serviceDesc);
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("ServiceDesciption was not created for class: " + serviceImplClass);
                }
                throw ExceptionFactory.makeWebServiceException(Messages.getMessage("createServiceDescrErr", serviceImplClass.getName()));
            }
        }
        return serviceDesc;
    }

    /** @see org.apache.axis2.jaxws.description.DescriptionFactory#createServiceDescriptionFromDBCMap(HashMap) */
    public static List<ServiceDescription> createServiceDescriptionFromDBCMap(
            HashMap<String, DescriptionBuilderComposite> dbcMap, ConfigurationContext configContext) {
        if (log.isDebugEnabled()) {
            log.debug("createServiceDescriptionFromDBCMap(Hashmap<String,DescriptionBuilderComposite>,ConfigurationContext " );
        }

        List<ServiceDescription> serviceDescriptionList = new ArrayList<ServiceDescription>();
        for (Iterator<DescriptionBuilderComposite> nameIter = dbcMap.values()
                .iterator(); nameIter.hasNext();) {
            DescriptionBuilderComposite serviceImplComposite = nameIter.next();
            if (isImpl(serviceImplComposite)) {
                // process this impl class
                ServiceDescriptionImpl serviceDescription = new ServiceDescriptionImpl(
                        dbcMap, serviceImplComposite, configContext);
                ServiceDescriptionValidator validator =
                        new ServiceDescriptionValidator(serviceDescription);
                if (validator.validate()) {
                    serviceDescriptionList.add(serviceDescription);
                    if (log.isDebugEnabled()) {
                        log.debug("Service Description created from DescriptionComposite: " +
                                serviceDescription);
                    }
                } else {

                    String msg = Messages.getMessage("createSrvcDescrDBCMapErr",
                    		                         validator.toString(),
                    		                         serviceImplComposite.toString(),
                    		                         serviceDescription.toString());
                    throw ExceptionFactory.makeWebServiceException(msg);
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("DBC is not a service impl: " + serviceImplComposite.toString());
                }
            }
        }

        // TODO: Process all composites that are WebFaults...current thinking is
        // that
        // since WebFault annotations only exist on exception classes, then they
        // should be processed by themselves, and at this level

        return serviceDescriptionList;
    }

    /**
     * @see org.apache.axis2.jaxws.description.DescriptionFactory#updateEndpoint(ServiceDescription,
     *      Class, QName, org.apache.axis2.jaxws.description.DescriptionFactory.UpdateType)
     */
    public static EndpointDescription updateEndpoint(
            ServiceDescription serviceDescription, Class sei, QName portQName,
            DescriptionFactory.UpdateType updateType,
            String bindingId, String endpointAddress) {
        return updateEndpoint(serviceDescription, sei, portQName, updateType, null, null, bindingId, endpointAddress);
    }
    
    /**
     * @see org.apache.axis2.jaxws.description.DescriptionFactory#updateEndpoint(ServiceDescription,
     *      Class, QName, org.apache.axis2.jaxws.description.DescriptionFactory.UpdateType)
     */
    public static EndpointDescription updateEndpoint(
            ServiceDescription serviceDescription, Class sei, QName portQName,
            DescriptionFactory.UpdateType updateType, Object serviceDelegateKey, String bindingId, String endpointAddress) {
        return updateEndpoint(serviceDescription, sei, portQName, updateType, null, serviceDelegateKey, bindingId, endpointAddress);
    }
    
    /**
     * @see org.apache.axis2.jaxws.description.DescriptionFactory#updateEndpoint(ServiceDescription, Class, QName, org.apache.axis2.jaxws.description.DescriptionFactory.UpdateType, DescriptionBuilderComposite)
     */
    public static EndpointDescription updateEndpoint(
            ServiceDescription serviceDescription, Class sei, QName portQName,
            DescriptionFactory.UpdateType updateType, 
            DescriptionBuilderComposite composite,
            Object serviceDelegateKey,
            String bindingId, 
            String endpointAddress) {
        EndpointDescription endpointDesc = null;
        synchronized(serviceDescription) {
                endpointDesc = 
                ((ServiceDescriptionImpl)serviceDescription)
                        .updateEndpointDescription(sei, portQName, updateType, composite, serviceDelegateKey, bindingId, endpointAddress);
        }
        EndpointDescriptionValidator endpointValidator = new EndpointDescriptionValidator(endpointDesc);
        
        boolean isEndpointValid = endpointValidator.validate();
        
        if (!isEndpointValid) {
            String msg = Messages.getMessage("endpointDescriptionValidationErrors",
                                             endpointValidator.toString());
            throw ExceptionFactory.makeWebServiceException(msg);
        }
        if (log.isDebugEnabled()) {
            log.debug("EndpointDescription updated: " + endpointDesc);
        }
        
        setPropertiesOnEndpointDesc(endpointDesc, composite);
        
        return endpointDesc;
    }

    /**
     * @see org.apache.axis2.jaxws.description.DescriptionFactory#updateEndpoint(ServiceDescription,
     * Class, EndpointReference, String, DescriptionFactory.UpdateType)
     */
    public static EndpointDescription updateEndpoint(
            ServiceDescription serviceDescription, Class sei, EndpointReference epr,
            String addressingNamespace,
            DescriptionFactory.UpdateType updateType, String bindingId, String endpointAddress) {
        return updateEndpoint(serviceDescription, sei, epr, addressingNamespace, updateType, null, null, bindingId, endpointAddress);
    }

    /**
     * @see org.apache.axis2.jaxws.description.DescriptionFactory#updateEndpoint(ServiceDescription,
     * Class, EndpointReference, String, DescriptionFactory.UpdateType, Object)
     */
    public static EndpointDescription updateEndpoint(
            ServiceDescription serviceDescription, Class sei, EndpointReference epr,
            String addressingNamespace,
            DescriptionFactory.UpdateType updateType,
            Object sparseCompositeKey, String bindingId, String endpointAddress) {
        return updateEndpoint(serviceDescription, sei, epr, addressingNamespace, updateType, null, sparseCompositeKey, bindingId, endpointAddress);
    }

    /**
     * @see org.apache.axis2.jaxws.description.DescriptionFactory#updateEndpoint(ServiceDescription,
     * Class, EndpointReference, String, DescriptionFactory.UpdateType, DescriptionBuilderComposite, Object)
     */
    public static EndpointDescription updateEndpoint(
            ServiceDescription serviceDescription, Class sei, EndpointReference epr,
            String addressingNamespace,
            DescriptionFactory.UpdateType updateType,
            DescriptionBuilderComposite composite,
            Object sparseCompositeKey, String bindingId, String endpointAddress) {
        QName portQName = null;
        
        try {
            ServiceName serviceName = EndpointReferenceHelper.getServiceNameMetadata(epr, addressingNamespace);
            QName serviceQName = serviceDescription.getServiceQName();
            
            //We need to throw an exception if the service name in the EPR metadata does not
            //match the service name associated with the JAX-WS service instance.
            if (serviceName.getName() != null && !serviceQName.equals(serviceName.getName())) {
                throw ExceptionFactory.makeWebServiceException(
                       Messages.getMessage("serviceNameMismatch", 
                                           serviceName.getName().toString(), 
                                           serviceQName.toString()));
            }
            //If a port name is available from the EPR metadata then use that, otherwise
            //leave it to the runtime to find a suitable port, based on WSDL/annotations.
            if (serviceName.getEndpointName() != null) {
                portQName = new QName(serviceQName.getNamespaceURI(), serviceName.getEndpointName());
            }
        }
        catch (Exception e) {
            throw ExceptionFactory.makeWebServiceException(
                 Messages.getMessage("updateEndpointError", e.getMessage()));
        }
        
        return updateEndpoint(serviceDescription, sei, portQName, updateType, composite, sparseCompositeKey, bindingId, endpointAddress);
    }

    public static ClientConfigurationFactory getClientConfigurationFactory() {
        return ClientConfigurationFactory.newInstance();
    }

    /**
     * This method will be used to determine if a given DBC represents a Web service
     * implementation.
     *
     * @param dbc - <code>DescriptionBuilderComposite</code>
     * @return - <code>boolean</code>
     */
    private static boolean isImpl(DescriptionBuilderComposite dbc) {
        if (!dbc.isInterface()
                && (dbc.getWebServiceAnnot() != null || dbc
                .getWebServiceProviderAnnot() != null)) {
            return true;
        }
        return false;
    }
    
    /**
     * This method will set any properties supplied on the DescriptionBuilderComposite instance
     * on the EndpointDescription. If the DBC is null or there are no properties present, this
     * method will have no effect.
     */
    static void setPropertiesOnEndpointDesc(EndpointDescription endpointDesc, DescriptionBuilderComposite 
                                     composite) {
        if(composite != null 
                && 
                composite.getProperties() != null
                &&
                !composite.getProperties().isEmpty()) {
            for(String key : composite.getProperties().keySet()) {
                Object value = composite.getProperties().get(key);
                if(log.isDebugEnabled()) {
                    log.debug("Setting property from DBC onto EndpointDescription, key= " + 
                              key + ", value= " + value);
                }
                endpointDesc.setProperty(key, value);
            }
        }
    }

    public static ResolvedHandlersDescription createResolvedHandlersDescription() {
        return new ResolvedHandlersDescriptionImpl();
    }

}
