/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *      
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.axis2.jaxws.description.impl;

/**
 * 
 */

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axis2.description.AxisService;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.description.DescriptionFactory;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.description.builder.DescriptionBuilderComposite;
import org.apache.axis2.jaxws.description.builder.converter.JavaClassToDBCConverter;
import org.apache.axis2.jaxws.description.validator.ServiceDescriptionValidator;
import org.apache.axis2.jaxws.description.validator.ValidationFailures;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Creates the JAX-WS metadata descritpion hierachy from some combinations of
 * WSDL, Java classes with annotations, and (in the future) deployment
 * descriptors.  This is the implementation and is not intended to be a public API.  The API is:
 * @see org.apache.axis2.jaxws.description.DescriptionFactory
 */
public class DescriptionFactoryImpl {
    private static final Log log = LogFactory.getLog(DescriptionFactoryImpl.class);

    /**
     * A DescrptionFactory can not be instantiated; all methods are static.
     */
    private DescriptionFactoryImpl() {
    }

    /**
     * @see org.apache.axis2.jaxws.description.DescriptionFactory#createServiceDescription(URL, QName, Class)
     */
    public static ServiceDescription createServiceDescription(URL wsdlURL,
            QName serviceQName, Class serviceClass) {
        ServiceDescription serviceDesc = new ServiceDescriptionImpl(wsdlURL, serviceQName, serviceClass);
        
        if (log.isDebugEnabled()) {
            log.debug("ServiceDescription created with WSDL URL: " + wsdlURL + "; QName: " + serviceQName + "; Class: " + serviceClass);
            log.debug(serviceDesc.toString());
        }
        return serviceDesc;
    }

    /**
     * @deprecated
     * @see org.apache.axis2.jaxws.description.DescriptionFactory#createServiceDescriptionFromServiceImpl(Class, AxisService)
     */
    public static ServiceDescription createServiceDescriptionFromServiceImpl(
            Class serviceImplClass, AxisService axisService) {
        ServiceDescription serviceDesc = new ServiceDescriptionImpl(serviceImplClass, axisService);
        if (log.isDebugEnabled()) {
            log.debug("Deprecated method used!  ServiceDescription created with Class: " + serviceImplClass + "; AxisService: " + axisService);
            log.debug(serviceDesc.toString());
        }
        return serviceDesc;
    }
    
    /**
     * @see org.apache.axis2.jaxws.description.DescriptionFactory#createServiceDescription(Class)
     */
    public static ServiceDescription createServiceDescription(Class serviceImplClass) {
        ServiceDescription serviceDesc = null;
        
        if (serviceImplClass != null) {
            JavaClassToDBCConverter converter = new JavaClassToDBCConverter(serviceImplClass);
            HashMap<String, DescriptionBuilderComposite> dbcMap = converter.produceDBC();
            List<ServiceDescription> serviceDescList =  createServiceDescriptionFromDBCMap(dbcMap);
            if (serviceDescList != null && serviceDescList.size() > 0) {
                serviceDesc = serviceDescList.get(0);
                if (log.isDebugEnabled()) {
                    log.debug("ServiceDescription created with class: " + serviceImplClass);
                    log.debug(serviceDesc);
                }
            }
            else {
                if (log.isDebugEnabled()) {
                    log.debug("ServiceDesciption was not created for class: " + serviceImplClass);
                }
                // TODO: NLS & RAS
                throw ExceptionFactory.makeWebServiceException("A ServiceDescription was not created for " + serviceImplClass);
            }
        }
        return serviceDesc;
    }


    /**
     * @see org.apache.axis2.jaxws.description.DescriptionFactory#createServiceDescriptionFromDBCMap(HashMap)
     */
    public static List<ServiceDescription> createServiceDescriptionFromDBCMap(
            HashMap<String, DescriptionBuilderComposite> dbcMap) {

        List<ServiceDescription> serviceDescriptionList = new ArrayList<ServiceDescription>();

        for (Iterator<DescriptionBuilderComposite> nameIter = dbcMap.values()
                .iterator(); nameIter.hasNext();) {
            DescriptionBuilderComposite serviceImplComposite = nameIter.next();
            if (isImpl(serviceImplComposite)) {
                // process this impl class
                ServiceDescription serviceDescription = new ServiceDescriptionImpl(
                        dbcMap, serviceImplComposite);
                ServiceDescriptionValidator validator = new ServiceDescriptionValidator(serviceDescription);
                if (validator.validate()) {
                    serviceDescriptionList.add(serviceDescription);
                    if (log.isDebugEnabled()) {
                        log.debug("Service Description created from DescriptionComposite: " + serviceDescription);
                    }
                }
                else {
                	
                	String msg = "The ServiceDescription failed to validate due to the following errors: \n" +
                	validator.toString();
                    
                    if (log.isDebugEnabled()) {
                        log.debug("Validation Phase 2 failure: " + msg);
                        log.debug("Failing composite: " + serviceImplComposite.toString());
                        log.debug("Failing Service Description: " + serviceDescription.toString());
                    }
                	
                    // TODO: Validate all service descriptions before failing
                    // TODO: Get more detailed failure information from the Validator
                    throw ExceptionFactory.makeWebServiceException(msg);
                }
            }
            else {
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
     * @see org.apache.axis2.jaxws.description.DescriptionFactory#updateEndpoint(ServiceDescription, Class, QName, org.apache.axis2.jaxws.description.DescriptionFactory.UpdateType)
     */
    public static EndpointDescription updateEndpoint(
            ServiceDescription serviceDescription, Class sei, QName portQName,
            DescriptionFactory.UpdateType updateType) {
        EndpointDescription endpointDesc = 
            ((ServiceDescriptionImpl) serviceDescription).updateEndpointDescription(sei, portQName, updateType);
        if (log.isDebugEnabled()) {
            log.debug("EndpointDescription updated: " + endpointDesc);
        }
        return endpointDesc;
    }

    /**
     * Builds a list of DescriptionBuilderComposite which is relevant to the
     * particular class
     * 
     * @param List<>
     *            A list of DescriptionBuilderComposite objects
     * @param serviceImplName
     * @return List<>
     */
    private static List<DescriptionBuilderComposite> buildRelevantCompositeList(
            List<DescriptionBuilderComposite> compositeList,
            String serviceImplName) {

        List<DescriptionBuilderComposite> relevantList = compositeList;

        // TODO: Find the composite which represents this serviceImplName

        // TODO: Go through input list to find composites relevant to this one
        // and add
        // to 'relevant list'

        return relevantList;
    }

    /**
     * This method will be used to determine if a given DBC represents a Web
     * service implementation.
     * 
     * @param dbc -
     *            <code>DescriptionBuilderComposite</code>
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

}
