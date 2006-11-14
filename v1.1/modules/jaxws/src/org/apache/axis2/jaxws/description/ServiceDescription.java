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
     * Update or create an EndpointDescription. Updates to existing
     * EndpointDescriptons will be based on the SEI class and its annotations.  Both declared
     * ports and dynamic ports can be updated.  A declared port is one that is defined (e.g. in WSDL or
     * via annotations); a dyamic port is one that is not defined (e.g. not via WSDL or annotations) and 
     * has been added via Serivce.addPort.  
     * 
     * Notes on how an EndpointDescription can be updated or created:
     * 1) Service.createDispatch can create a Dispatch client for either a declared or dynamic port
     * 2) Note that creating a Dispatch does not associate an SEI with an endpoint
     * 3) Service.getPort will associate an SEI with a port
     * 4) A getPort on an endpoint which was originally created for a Distpatch will update that
     *    EndpointDescription with the SEI provided on the getPort
     * 5) Service.getPort can not be called on a dynamic port (per the JAX-WS spec)
     * 6) Service.addPort can not be called for a declared port
     * 
     * @param sei
     *            This will be non-null if the update is of type GET_PORT; it
     *            will be null if the update is ADD_PORT or CREATE_DISPATCH
     * @param portQName
     * @param updateType
     *            Indicates what is causing the update GET_PORT is an attempt to
     *            get a declared SEI-based port ADD_PORT is an attempt to add a
     *            previously non-existent dynamic port CREATE_DISPATCH is an
     *            attempt to create a Dispatch-based client to either a declared
     *            port or a pre-existing dynamic port.
     */
    public enum UpdateType {GET_PORT, ADD_PORT, CREATE_DISPATCH}
    public void updateEndpointDescription(Class sei, QName portQName, UpdateType updateType) {
        
        // TODO: Add support: portQName can be null when called from Service.getPort(Class)
        if (portQName == null) {
            throw new UnsupportedOperationException("ServiceDescription.updateEndpointDescription null PortQName not supported");
        }
        
        EndpointDescription endpointDescription = getEndpointDescription(portQName);
        boolean isPortDeclared = isPortDeclared(portQName);

        switch (updateType) {

        case ADD_PORT:
            // Port must NOT be declared (e.g. can not already exist in WSDL)
            // If an EndpointDesc doesn't exist; create it as long as it doesn't exist in the WSDL
            // TODO: This test can be simplified once isPortDeclared(QName) understands annotations and WSDL as ways to declare a port.
            if (getWSDLWrapper() != null && isPortDeclared) {
                // TODO: RAS & NLS
                throw ExceptionFactory.makeWebServiceException("ServiceDescription.updateEndpointDescription: Can not do an addPort with a PortQN that exists in the WSDL.  PortQN: " + portQName.toString());
            }
            else if (endpointDescription == null) {
                // Use the SEI Class and its annotations to finish creating the Description hierachy.  Note that EndpointInterface, Operations, Parameters, etc.
                // are not created for dynamic ports.  It would be an error to later do a getPort against a dynamic port (per the JAX-WS spec)
                endpointDescription = new EndpointDescription(sei, portQName, true, this);
                addEndpointDescription(endpointDescription);
            }
            else {
                // All error check above passed, the EndpointDescription already exists and needs no updating
            }
            break;

        case GET_PORT:
            // If an endpointDesc doesn't exist, and the port exists in the WSDL, create it
            // If an endpointDesc already exists and has an associated SEI already, make sure they match
            // If an endpointDesc already exists and was created for Dispatch (no SEI), update that with the SEI provided on the getPort

            // Port must be declared (e.g. in WSDL or via annotations)
            // TODO: Once isPortDeclared understands annotations and not just WSDL, the 2nd part of this check can possibly be removed.
            //       Although consider the check below that updates an existing EndpointDescritpion with an SEI.
            if (!isPortDeclared || (endpointDescription != null && endpointDescription.isDynamicPort())) {
                // This guards against the case where an addPort was done previously and now a getPort is done on it.
                // TODO: RAS & NLS
                throw ExceptionFactory.makeWebServiceException("ServiceDescription.updateEndpointDescription: Can not do a getPort on a port added via addPort().  PortQN: " + portQName.toString());
            }
            else if (sei == null) {
                // TODO: RAS & NLS
                throw ExceptionFactory.makeWebServiceException("ServiceDescription.updateEndpointDescription: Can not do a getPort with a null SEI.  PortQN: " + portQName.toString());
            }
            else if (endpointDescription == null) {
                // Use the SEI Class and its annotations to finish creating the Description hierachy: Endpoint, EndpointInterface, Operations, Parameters, etc.
                // TODO: Need to create the Axis Description objects after we have all the config info (i.e. from this SEI)
                endpointDescription = new EndpointDescription(sei, portQName, this);
                addEndpointDescription(endpointDescription);
            }
            else if (getEndpointSEI(portQName) == null && !endpointDescription.isDynamicPort()) {
                // Existing endpointDesc from a declared port needs to be updated with an SEI
                // Note that an EndpointDescritption created from an addPort (i.e. a dynamic port) can not do this.
                endpointDescription.updateWithSEI(sei);
            }
            else if (getEndpointSEI(portQName) != sei) {
                // TODO: RAS & NLS
                throw ExceptionFactory.makeWebServiceException("ServiceDescription.updateEndpointDescription: Can't do a getPort() specifiying a different SEI than the previous getPort().  PortQN: " 
                        + portQName + "; current SEI: " + sei + "; previous SEI: " + getEndpointSEI(portQName));
            }
            else {
                // All error check above passed, the EndpointDescription already exists and needs no updating
            }
            break;

        case CREATE_DISPATCH:
            // Port may or may not exist in WSDL.  
            // If an endpointDesc doesn't exist and it is in the WSDL, it can be created
            // Otherwise, it is an error.
            if (endpointDescription != null) {
                // The EndpoingDescription already exists; nothing needs to be done
            }
            else if (sei != null) {
                // The Dispatch should not have an SEI associated with it on the update call.
                // REVIEW: Is this a valid check?
                throw ExceptionFactory.makeWebServiceException("ServiceDescription.updateEndpointDescription: Can not specify an SEI when creating a Dispatch. PortQN: " + portQName);
            }
            else if (isPortDeclared) {
                // EndpointDescription doesn't exist and this is a declared Port, so create one
                // Use the SEI Class and its annotations to finish creating the Description hierachy.  Note that EndpointInterface, Operations, Parameters, etc.
                // are not created for Dipsatch-based ports, but might be updated later if a getPort is done against the same declared port.
                // TODO: Need to create the Axis Description objects after we have all the config info (i.e. from this SEI)
                endpointDescription = new EndpointDescription(sei, portQName, this);
                addEndpointDescription(endpointDescription);
            }
            else {
                // The port is not a declared port and it does not have an EndpointDescription, meaning an addPort has not been done for it
                // This is an error.
                // TODO: RAS & NLS
                throw ExceptionFactory.makeWebServiceException("ServiceDescription.updateEndpointDescription: Attempt to create a Dispatch for a non-existant Dynamic por  PortQN: " + portQName);
            }
            break;
        }
    }

    private Class getEndpointSEI(QName portQName) {
        Class endpointSEI = null;
        EndpointInterfaceDescription endpointInterfaceDesc = getEndpointDescription(portQName).getEndpointInterfaceDescription();
        if (endpointInterfaceDesc != null ) {
            endpointSEI = endpointInterfaceDesc.getSEIClass();
        }
        return endpointSEI;
    }

    private boolean isPortDeclared(QName portQName) {
        // TODO: This needs to account for declaration of the port via annotations in addition to just WSDL
        // TODO: Add logic to check the portQN namespace against the WSDL Definition NS
        boolean portIsDeclared = false;
        if (getWSDLWrapper() != null) {
            Definition wsdlDefn = getWSDLWrapper().getDefinition();
            Service wsdlService = wsdlDefn.getService(serviceQName);
            Port wsdlPort = wsdlService.getPort(portQName.getLocalPart());
            portIsDeclared = (wsdlPort != null);
        }
        else {
            // TODO: Add logic to determine if port is declared via annotations when no WSDL is present.  For now, we have to assume it is declared 
            // so getPort(...) and createDispatch(...) calls work when there is no WSDL.
            portIsDeclared = true;
        }
        return portIsDeclared;
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
