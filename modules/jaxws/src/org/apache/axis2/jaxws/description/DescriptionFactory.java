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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.jws.WebService;
import javax.xml.namespace.QName;

import org.apache.axis2.description.AxisService;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.description.builder.DescriptionBuilderComposite;

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
        return new ServiceDescription(wsdlURL, serviceQName, serviceClass);
    }
    
    // TODO: Taking an AxisService is only temporary; the AxisService should be created when creating the ServiceDesc
    public static ServiceDescription createServiceDescriptionFromServiceImpl(Class serviceImplClass, AxisService axisService) {
        return new ServiceDescription(serviceImplClass, axisService);
    }
    
    //TODO: Determine whether this method is necessary...we may want to always build a 
    //ServiceDescription based on a particular impl class
    public static List<ServiceDescription> createServiceDescriptionFromDBCMap (
    		HashMap<String, DescriptionBuilderComposite> dbcMap) {

    	List<ServiceDescription> serviceDescriptionList = new ArrayList<ServiceDescription>();

    	for (Iterator<DescriptionBuilderComposite> nameIter = dbcMap.values().iterator(); 
    		nameIter.hasNext();) {
    		DescriptionBuilderComposite serviceImplComposite = nameIter.next();
    		if(isImpl(serviceImplComposite)) {
				// process this impl class
        		ServiceDescription serviceDescription = new ServiceDescription(dbcMap, 
        				serviceImplComposite);
        	   	serviceDescriptionList.add(serviceDescription);
    		}
    	}
    	
    	//For each impl class and each SEI, build a ServiceDescription
    	//TODO: Probably not the best way to process SEI's ...need to look at this
    	/*
    	HashMap<String, DescriptionBuilderComposite> seiMap = 
    		sortedDBCList.getMap(DBCInputListSorter.seiMapKey);

    	for (int i=1; i < sortedDBCList.getImplClassesList().size(); i++ ) {
    		//process this sei class
    		
    		String seiName = sortedDBCList.getSeiClassesList().get(i);
    		DescriptionBuilderComposite seiComposite = seiMap.get(seiName);
            
    		if (seiComposite == null)
            	throw ExceptionFactory.makeWebServiceException("ServiceDescription.constructor: Can not find DBC represents associated serviceImplName:  " + serviceImplName);
    		
    		ServiceDescription serviceDescription = new ServiceDescription( sortedDBCList, seiComposite);
    	   	serviceDescriptionList.add(serviceDescription);
    	}
    	*/  	
    	
    	return serviceDescriptionList;
    }

    /**
     * Update an existing ServiceDescription with an annotated SEI
     * @param serviceDescription
     * @param seiClass 
     * @param portName Can be null
     * @return
     */
    public static ServiceDescription updateEndpoint(ServiceDescription serviceDescription, Class sei, QName portQName, ServiceDescription.UpdateType updateType ) {
        serviceDescription.updateEndpointDescription(sei, portQName, updateType);
        return serviceDescription;
    }
    
    /**
     * Builds a list of DescriptionBuilderComposite which is relevant to the particular
     * class
     * @param List<> A list of DescriptionBuilderComposite objects
     * @param serviceImplName 
     * @return List<>
     */ 
	private static List<DescriptionBuilderComposite> BuildRelevantCompositeList(
				List<DescriptionBuilderComposite> compositeList,
				String serviceImplName) {
		
		List<DescriptionBuilderComposite> relevantList = compositeList;
		
		//TODO: Find the composite which represents this serviceImplName
		
		//TODO: Go through input list to find composites relevant to this one and add
		//      to 'relevant list'
		
		return relevantList;
	}
	
	/**
	 * This method will be used to determine if a given DBC represents a
	 * Web service implementation.
	 * @param dbc - <code>DescriptionBuilderComposite</code>
	 * @return - <code>boolean</code>
	 */
	private static boolean isImpl(DescriptionBuilderComposite dbc) {
		if(!dbc.isInterface() && (dbc.getWebServiceAnnot() != null || 
				dbc.getWebServiceProviderAnnot() != null)) {
			return true;
		}
		return false;
	}

}
