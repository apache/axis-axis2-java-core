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
 * WSDL, Java class information including annotations, and (in the future) deployment descriptors.
 */
public class DescriptionFactory {
    /**
     * The type of update being done for a particular Port.  This is used by the JAX-WS
     * service delegate on the CLIENT side. This is used as a parameter to the 
     * updateEndpoint factory method.  An EndpointDescription will be returned corresponding
     * to the port.
     * GET_PORT:   Return an SEI-based pre-existing port
     * ADD_PORT:   Return a Dispatch-only non-pre-existing port
     * CREATE_DISPATCH: Return a Dispatch port; this is valid on either a 
     *                  pre-existing port (e.g. GET_PORT) or dynamic port (ADD_PORT)
     *                   
     */
    public static enum UpdateType {GET_PORT, ADD_PORT, CREATE_DISPATCH}
    /**
     * A DescrptionFactory can not be instantiated; all methods are static.
     */
    private DescriptionFactory() {
    }
    
    /**
     * Create the initial ServiceDescription hierachy on the CLIENT side.  This is intended to be
     * called when the client creates a ServiceDelegate.  Note that it will only create
     * the ServiceDescription at this point.  The EndpointDescription hierachy under this 
     * ServiceDescription will be created by the updateEndpoint factory method, which will be called
     * by the ServiceDelegate once the port is known (i.e. addPort, getPort, or createDispatch).
     * 
     * @see #updateEndpoint(ServiceDescription, Class, QName, org.apache.axis2.jaxws.description.ServiceDescription.UpdateType)
     * 
     * @param wsdlURL URL to the WSDL file to use; this may be null
     * @param serviceQName The ServiceQName for this service; may not be null
     * @param serviceClass The Service class; may not be null and 
     *        must be assignable from javax.xml.ws.Service
     * @return A ServiceDescription instance for a CLIENT access to the service.
     */
    public static ServiceDescription createServiceDescription(URL wsdlURL, QName serviceQName, Class serviceClass) {
        return DescriptionFactoryImpl.createServiceDescription(wsdlURL, serviceQName, serviceClass);
    }

    /**
     * Retrieve or create the EndpointDescription hierachy associated with an existing CLIENT side
     * ServiceDescription for a particular port.  If an EndpointDescritption already exists, it will
     * be returned; if one does not already exist, it will be created.  Note that if the SEI is null
     * then the EndpointDescription returned will be for a Dispatch client only and it will not have
     * an EndpointInterfaceDescription hierachy associated with it.  If, at a later point, the same
     * port is requested and an SEI is provided, the existing EndpointDescription will be updated
     * with a newly-created EndpointInterfaceDescription hieracy.
     * 
     * @see #createServiceDescription(URL, QName, Class)
     * @see DescriptionFactory.UpdateType
     *
     * @param serviceDescription An existing client-side ServiceDescription.  This must not be null.
     * @param sei The ServiceInterface class.  This can be null for adding a port or creating a 
     *            Dispatch; it can not be null when getting a port.
     * @param portQName The QName of the port.  If this is null, the runtime will attempt to
     *        to select an appropriate port to use.
     * @param updateType The type of the update: adding a port, creating a dispatch, or
     *                   getting an SEI-based port.  
     * @return An EndpointDescription corresponding to the port.
     */
    public static EndpointDescription updateEndpoint(ServiceDescription serviceDescription, Class sei, QName portQName, DescriptionFactory.UpdateType updateType ) {
        return DescriptionFactoryImpl.updateEndpoint(serviceDescription, sei, portQName, updateType);
    }

    /**
     * Create a full ServiceDescription hierachy on the SERVER side for EACH 
     * service implementation entry in the DescriptionBuilderComposite (DBC) map.  Note that the 
     * associated SERVER side Axis description objects are also created.  To create a single
     * ServiceDescription hierarchy for a single service implementation class, use the 
     * factory method that takes a single class and returns a single ServiceDescription.  
     * 
     * A service implementation DBC entry is one that:
     * (1) Is a class and not an interface
     * (2) Carries a WebService or WebServiceProvider annotation.
     * 
     * A DBC represents the information found in the service implementation class.  There
     * will be other DBC entries in the map for classes and interfaces associated with the 
     * service implementation, such as super classes, super interfaces, fault classes, and 
     * such.
     * 
     *  Note that map may contain > 1 service implementation DBC.  A full ServiceDescriptionHierachy 
     *  will be created for each service implementation DBC entry.
     *  
     *  Note that each ServiceDescription will have exactly one EndpointDescription corresponding
     *  to each service implementation.
     * 
     * @param dbcMap A HashMap keyed on class name with a value for the DBC for that classname
     * @return A List of ServiceDescriptions with the associated SERVER side hierachy created.
     */
    public static List<ServiceDescription> createServiceDescriptionFromDBCMap (
    		HashMap<String, DescriptionBuilderComposite> dbcMap) {
    	return DescriptionFactoryImpl.createServiceDescriptionFromDBCMap(dbcMap);
    }

    /**
     * Create a full ServiceDescription hierachy on the SERVER side for a single service
     * implementation class.  To create process more than one service implementation at one time
     * or to process them without causing the service implemenation classes to be loaded, use
     * the factory method that takes a collection of DescriptionBuilderComposite objects and
     * returns a collection of ServiceDescriptions.
     *
     *  Note that the ServiceDescription will have exactly one EndpointDescription corresponding
     *  to the service implementation.
     * 
     * @param serviceImplClass A Web Service implementation class (i.e. one that carries an 
     * WebService or WebServiceProvider annotation).
     * @return A ServiceDescription with the associated SERVER side hierachy created.
     */
    public static ServiceDescription createServiceDescription(Class serviceImplClass) {
        return DescriptionFactoryImpl.createServiceDescription(serviceImplClass);
    }
    /**
     * DO NOT USE THIS METHOD FOR PRODUCTION CODE.  It has been deprecated and is only used
     * to drive some testing.  Note that the AxisService and associated Axis description objects
     * ARE NOT created or updated by this factory method.
     * 
     * @deprecated Use {@link #createServiceDescriptionFromDBCMap(HashMap)}
     * 
     * @param serviceImplClass A service implementation class with annotations
     * @param axisService A FULLY POPULATED AxisService including all of the underlying
     *        description objects such as AxisOperations.
     * @return A ServiceDescription hierachy constructed (via Java reflection) from the service
     * implementation class and tied via properties to the existing AxisService object. 
     */
    // TODO: Taking an AxisService is only temporary; the AxisService should be created when creating the ServiceDesc
    public static ServiceDescription createServiceDescriptionFromServiceImpl(Class serviceImplClass, AxisService axisService) {
        return DescriptionFactoryImpl.createServiceDescriptionFromServiceImpl(serviceImplClass, axisService);
    }
    

}
