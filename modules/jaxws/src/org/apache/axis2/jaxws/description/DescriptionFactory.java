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


package org.apache.axis2.jaxws.description;

import java.net.URL;
import java.util.HashMap;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axis2.description.AxisService;
import org.apache.axis2.jaxws.description.builder.DescriptionBuilderComposite;
import org.apache.axis2.jaxws.description.impl.DescriptionFactoryImpl;

/**
 * Creates the JAX-WS metadata descritpion hierachy from some combinations of
 * WSDL, Java classes with annotations, and (in the future) deployment descriptors.
 */
public class DescriptionFactory {
    /**
     * A DescrptionFactory can not be instantiated; all methods are static.
     */
    private DescriptionFactory() {
    }
    
    public static ServiceDescription createServiceDescription(URL wsdlURL, QName serviceQName, Class serviceClass) {
        return DescriptionFactoryImpl.createServiceDescription(wsdlURL, serviceQName, serviceClass);
    }
    
    // TODO: Taking an AxisService is only temporary; the AxisService should be created when creating the ServiceDesc
    public static ServiceDescription createServiceDescriptionFromServiceImpl(Class serviceImplClass, AxisService axisService) {
        return DescriptionFactoryImpl.createServiceDescriptionFromServiceImpl(serviceImplClass, axisService);
    }
    
    //TODO: Determine whether this method is necessary...we may want to always build a 
    //ServiceDescription based on a particular impl class
    public static List<ServiceDescription> createServiceDescriptionFromDBCMap (
    		HashMap<String, DescriptionBuilderComposite> dbcMap) {
    	return DescriptionFactoryImpl.createServiceDescriptionFromDBCMap(dbcMap);
    }

    /**
     * Update an existing ServiceDescription with an annotated SEI
     * @param serviceDescription
     * @param seiClass 
     * @param portName Can be null
     * @return
     */
    public static ServiceDescription updateEndpoint(ServiceDescription serviceDescription, Class sei, QName portQName, ServiceDescription.UpdateType updateType ) {
        return DescriptionFactoryImpl.updateEndpoint(serviceDescription, sei, portQName, updateType);
    }
}
