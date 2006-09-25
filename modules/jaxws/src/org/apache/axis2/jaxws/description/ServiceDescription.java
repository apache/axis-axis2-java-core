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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;
import javax.jws.WebService;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.OutInAxisOperation;
import org.apache.axis2.description.OutOnlyAxisOperation;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.RobustOutOnlyAxisOperation;
import org.apache.axis2.description.WSDL11ToAllAxisServicesBuilder;
import org.apache.axis2.description.WSDL11ToAxisServiceBuilder;
import org.apache.axis2.engine.AbstractDispatcher;
import org.apache.axis2.jaxws.ClientConfigurationFactory;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.util.WSDL4JWrapper;
import org.apache.axis2.jaxws.util.WSDLWrapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The JAX-WS Service metadata and root of the JAX-WS Descritpion hierachy.
 */

/*
Working-design information.

Description hierachy
    ServiceDescription
        EndpointDescription[]
            EndpointInterfaceDescription
                OperationDescription[]
                    ParameterDescription Input[]
                    ParameterDescription Output[]
                    FaultDescription

ServiceDescription:
Corresponds to the generated Service class [client]; TBD [server]

Java Name: Generated service class or null if dynamically configured service [client]; null [server]

Axis2 Delegate: None (AxisService corresponds to a port which corresponds to the EndpointDescription)

JSR-181 Annotations: 
@HandlerChain(file, name) [per JAXWS p. 105] Affects all proxies and dispatches created using any port on this service
TBD

WSDL Elements: 
<service

JAX-WS Annotations: 
@WebServiceClient(name, targetNamespace, wsdlLocation)
@WebEndpoint(name) This is specified on the getPortName() methods on the service
TBD

Properties available to JAXWS runtime:
getEndpointDescription(QName port) Needed by HandlerResolver
TBD

 */

/**
 * ServiceDescription contains the metadata (e.g. WSDL, annotations) relating to a Service on both the
 * service-requester (aka client) and service-provider (aka server) sides.
 * 
 */
public class ServiceDescription {
    private ClientConfigurationFactory clientConfigFactory;
    private ConfigurationContext configContext;

    private URL wsdlURL;
    private QName serviceQName;
    
    // Only ONE of the following will be set in a ServiceDescription, depending on whether this Description
    // was created from a service-requester or service-provider flow. 
    private Class serviceClass;         // A service-requester generated service or generic service class
    private Class serviceImplClass;     // A service-provider service implementation class.  The impl
                                        // could be a Provider (no SEI operations) or an Endpoint (SEI based operations) 
    
    // TODO: Possibly remove Definition and delegate to the Defn on the AxisSerivce set as a paramater by WSDLtoAxisServicBuilder?
    private WSDLWrapper wsdlWrapper; 
    
    private Hashtable<QName, EndpointDescription> endpointDescriptions = new Hashtable<QName, EndpointDescription>();
    
    private static final Log log = LogFactory.getLog(ServiceDescription.class);

    /**
     * This is (currently) the client-side-only constructor
     * Construct a service description hierachy based on WSDL (may be null), the Service class, and 
     * a service QName.
     * 
     * @param wsdlURL  The WSDL file (this may be null).
     * @param serviceQName  The name of the service in the WSDL.  This can not be null since a 
     *   javax.xml.ws.Service can not be created with a null service QName.
     * @param serviceClass  The JAX-WS service class.  This could be an instance of
     *   javax.xml.ws.Service or a generated service subclass thereof.  This will not be null.
     */
    ServiceDescription(URL wsdlURL, QName serviceQName, Class serviceClass) {
        if (serviceQName == null) {
            throw ExceptionFactory.makeWebServiceException(Messages.getMessage("serviceDescErr0"));
        }
        if (serviceClass == null) {
            throw ExceptionFactory.makeWebServiceException(Messages.getMessage("serviceDescErr1", "null"));
        }
        if (!javax.xml.ws.Service.class.isAssignableFrom(serviceClass)) {
            throw ExceptionFactory.makeWebServiceException(Messages.getMessage("serviceDescErr1", serviceClass.getName()));
        }
        
        this.wsdlURL = wsdlURL;
        // TODO: The serviceQName needs to be verified between the argument/WSDL/Annotation
        this.serviceQName = serviceQName;
        this.serviceClass = serviceClass;
        
        setupWsdlDefinition();
    }

    /**
     * This is (currently) the service-provider-side-only constructor.
     * Create a service Description based on a service implementation class
     * 
     * @param serviceImplClass
     */
    // NOTE: Taking an axisService on the call is TEMPORARY!  Eventually the AxisService should be constructed
    //       based on the annotations in the ServiceImpl class.
    // TODO: Remove axisService as paramater when the AxisService can be constructed from the annotations
    ServiceDescription(Class serviceImplClass, AxisService axisService) {
        this.serviceImplClass = serviceImplClass;
        // Create the EndpointDescription hierachy from the service impl annotations; Since the PortQName is null, 
        // it will be set to the annotation value.
        EndpointDescription endpointDescription = new EndpointDescription(serviceImplClass, null, axisService, this);
        addEndpointDescription(endpointDescription);
        
        // TODO: The ServiceQName instance variable should be set based on annotation or default
    }


    
    /*=======================================================================*/
    /*=======================================================================*/
    // START of public accessor methods
    
    /**
     * Updates the ServiceDescription based on the SEI class and its annotations.
     * @param sei
     * @param portQName
     */
    public void updateEndpointDescription(Class sei, QName portQName) {
        
        // TODO: Add support: portQName can be null when called from Service.getPort(Class)
        if (portQName == null) {
            throw new UnsupportedOperationException("ServiceDescription.updateEndpointDescription null PortQName not supported");
        }
        
        // If a Dispatch client is created on a service for which WSDL was supplied, it is an error
        // to attempt to add a port with the same name as a port that exists in the WSDL.
        // REVIEW: Is this a correct check to be making?
        // TODO: Add logic to check the portQN namespace against the WSDL Definition NS
        if (sei == null && getWSDLWrapper() != null) {
            Definition wsdlDefn = getWSDLWrapper().getDefinition();
            Service wsdlService = wsdlDefn.getService(serviceQName);
            Port wsdlPort = wsdlService.getPort(portQName.getLocalPart());
            if (wsdlPort != null) {
                // TODO: RAS & NLS
                throw ExceptionFactory.makeWebServiceException("ServiceDescription.updateEndpointDescription: Can not do an addPort with a PortQN that exists in the WSDL.  PortQN: " + portQName.toString());
            }
        }
        

        if (getEndpointDescription(portQName) != null) {
            // The port has already been created, so the SEI settings must match.  Either
            // this port was created as an SEI-based one via a getPort() call or
            // a Dispatch-based one via an addPort() call.  Those two calls can not be
            // done for the same portQName.  Additionally, if this is an SEI-based 
            // endpoint, the SEIs for each subsequent getPort() must match.
            // REVIEW: It is probably OK of the SEIs are functionally equivilent
            //         they probably don't need to be the same class, so this check needs
            //         to be expanded in that case.
            Class endpointSEI = null;
            EndpointInterfaceDescription endpointInterfaceDesc = getEndpointDescription(portQName).getEndpointInterfaceDescription();
            if (endpointInterfaceDesc != null )
                endpointSEI = endpointInterfaceDesc.getSEIClass();
            
            if (sei != endpointSEI)
                // TODO: RAS & NLS
                throw ExceptionFactory.makeWebServiceException("ServiceDescription.updateEndpointDescription: SEIs don't match");
        }
        else {
            // Use the SEI Class and its annotations to finish creating the Description hierachy: Endpoint, EndpointInterface, Operations, Parameters, etc.
            // TODO: Need to create the Axis Description objects after we have all the config info (i.e. from this SEI)
            EndpointDescription endpointDescription = new EndpointDescription(sei, portQName, this);
            addEndpointDescription(endpointDescription);
        }
    }
    
    public EndpointDescription[] getEndpointDescriptions() {
        return endpointDescriptions.values().toArray(new EndpointDescription[0]);
    }
    public EndpointDescription getEndpointDescription(QName portQName) {
        return endpointDescriptions.get(portQName);
    }
    /**
     * Return the EndpointDescriptions corresponding to the SEI class.  Note that
     * Dispatch endpoints will never be returned because they do not have an associated SEI.
     * @param seiClass
     * @return
     */
    public EndpointDescription[] getEndpointDescription(Class seiClass) {
        EndpointDescription[] returnEndpointDesc = null;
        ArrayList<EndpointDescription> matchingEndpoints = new ArrayList<EndpointDescription>();
        Enumeration<EndpointDescription> endpointEnumeration = endpointDescriptions.elements();
        while (endpointEnumeration.hasMoreElements()) {
            EndpointDescription endpointDescription = endpointEnumeration.nextElement();
            EndpointInterfaceDescription endpointInterfaceDesc = endpointDescription.getEndpointInterfaceDescription();
            // Note that Dispatch endpoints will not have an endpointInterface because the do not have an associated SEI
            if (endpointInterfaceDesc != null) {
                Class endpointSEIClass = endpointInterfaceDesc.getSEIClass(); 
                if (endpointSEIClass != null && endpointSEIClass.equals(seiClass)) {
                    matchingEndpoints.add(endpointDescription);
                }
            }
        }
        if (matchingEndpoints.size() > 0) {
            returnEndpointDesc = matchingEndpoints.toArray(new EndpointDescription[0]);
        }
        return returnEndpointDesc;
    }
    // END of public accessor methods
    /*=======================================================================*/
    /*=======================================================================*/
    private void addEndpointDescription(EndpointDescription endpoint) {
        endpointDescriptions.put(endpoint.getPortQName(), endpoint);
    }

    private void setupWsdlDefinition() {
        // Note that there may be no WSDL provided, for example when called from 
        // Service.create(QName serviceName).
        if (wsdlURL != null) {
            try {
                wsdlWrapper = new WSDL4JWrapper(this.wsdlURL);
            } catch (WSDLException e) {
                throw ExceptionFactory.makeWebServiceException(Messages.getMessage("wsdlException", e.getMessage()), e);
            }
        }
    }

    // TODO: Remove these and replace with appropraite get* methods for WSDL information
    public WSDLWrapper getWSDLWrapper() {
        return wsdlWrapper;
    }
    public URL getWSDLLocation() {
        return wsdlURL;
    }
    
    public ConfigurationContext getAxisConfigContext() {
        if (configContext == null) {
            configContext = getClientConfigurationFactory().getClientConfigurationContext();
        }
    	return configContext;
    	
    }
    
    ClientConfigurationFactory getClientConfigurationFactory() {
        
        if (clientConfigFactory == null ) {
            clientConfigFactory = ClientConfigurationFactory.newInstance();
        }
        return clientConfigFactory;
    }
    
    public ServiceClient getServiceClient(QName portQName) {
        // TODO: RAS if no portQName found
        return getEndpointDescription(portQName).getServiceClient();
    }
    
    public QName getServiceQName() {
        return serviceQName;
    }
}
