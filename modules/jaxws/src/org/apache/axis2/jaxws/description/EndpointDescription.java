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

import java.util.ArrayList;
import java.util.List;

import javax.jws.WebService;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.BindingType;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceProvider;

import org.apache.axis2.jaxws.ExceptionFactory;


/*
Working-design information.

Java Name: none [client]; Endpoint implementation class [server]

Axis2 Delegate: none; Axis2 put this information into AxisService

JSR-181 Annotations: TBD

WSDL Elements
<port

JAX-WS Annotations: 
@ServiceMode (value) [Server, jaxws.Provider?]
@WebServiceProvider(wsdllocation, serviceName, portName, targetNamespace) [Server, jaxws.Provider]
@BindingType(value) [Server, endpoint impl]
TBD

Properties available to JAXWS runtime: TBD

 */
/*
 * TODO: EndpointDescription should be created via AxisService objects and not directly from WSDL
 * IMPORTANT NOTE: Axis2 currently only supports 1 service and 1 port under that service.  When that is
 * fixed, that will probably have an impact on this class.  In particular, I think this should be created 
 * somehow from an AxisService/AxisPort combination, and not directly from the WSDL.
 */
// TODO: (JLB) With Lori's change to name WSDL11 services as the port, this might mean that the EndpointDescription corresponds to 
//       the AxisService rather than the ServiceDescription.
/**
 * 
 */
public class EndpointDescription {
    private ServiceDescription parentServiceDescription;
    private QName portQName;
    // Note that an EndpointInterfaceDescription will ONLY be set for an Endpoint-based implementation;
    // it will NOT be set for a Provider-based implementation
    private EndpointInterfaceDescription endpointInterfaceDescription;

    // This is only set on the service-side, not the client side.  It could
    // be either an SEI class or a service implementation class.
    private Class implOrSEIClass;
    // Only one of these two annotations will be set; they are mutually exclusive
    private WebService          webServiceAnnotation;
    private WebServiceProvider  webServiceProviderAnnotation;
    // Information common to both WebService and WebServiceProvider annotations
    private String              annotation_WsdlLocation;
    private String              annotation_ServiceName;
    private String              annotation_PortName;
    private String              annotation_TargetNamespace;
    // Information only set on WebService annotation
    private String              webService_EndpointInterface;
    private String              webService_Name;

    // ServiceMode annotation (only valid on a Provider-based endpoint)
    private ServiceMode         serviceModeAnnotation;
    private Service.Mode        serviceModeValue;
    
    // BindingType annotation
    private BindingType         bindingTypeAnnotation;
    private String              bindingTypeValue;
    
    // TODO: This needs to be a collection of handler descriptions; use JAX-WS Appendix B Handler Chain Configuration File Schema as a starting point
    private ArrayList<String> handlerList = new ArrayList<String>();
    
    /**
     * Create an EndpointDescription based on the WSDL port.  Note that per the JAX-WS Spec (Final Release, 4/19/2006
     * Section 4.2.3 Proxies, page 55)the "namespace component of the port is the target namespace of the WSDL 
     * definition document".
     * Note this is currently only used on the client-side (this may change).
     * 
     * @param wsdlPort The WSDL Port tag for this EndpointDescription.
     * @param definition The WSDL Definition target namespace used to create the port QName
     */
    EndpointDescription(Port wsdlPort, Definition definition, ServiceDescription parent) {
        parentServiceDescription = parent;
        String localPart = wsdlPort.getName();
        String namespace = definition.getTargetNamespace();
        portQName = new QName(namespace, localPart);
        endpointInterfaceDescription = new EndpointInterfaceDescription(this);
    }
    
    /**
     * Create from an annotated implementation or SEI class.
     * Note this is currently used only on the server-side (this probably won't change).
     *
     * @param theClass An implemntation or SEI class
     * @param portName May be null; if so the annotation is used
     * @param parent
     */
    EndpointDescription(Class theClass, QName portName, ServiceDescription parent) {
        parentServiceDescription = parent;
        implOrSEIClass = theClass;

        // The Service Implementation class could be either Provider-based or Endpoint-based.  The 
        // annotations that are present are similar but different.  Conformance requirements 
        // per JAX-WS
        // - A Provider based implementation MUST carry the @WebServiceProvider annotation
        //   per section 5.1 javax.xml.ws.Provider on page 63
        // - An Endpoint based implementation MUST carry the @WebService annotation per JSR-181 
        //   (reference TBD) and JAX-WS (reference TBD)
        // - An Endpoint based implementation @WebService annotation MAY reference an endpoint
        //   interface 
        // - The @WebService and @WebServiceProvider annotations can not appear in the same class per 
        //   JAX-WS section 7.7 on page 82.

        // Verify that one (and only one) of the required annotations is present.
        // TODO: Add tests to verify this error checking
        webServiceAnnotation = (WebService) implOrSEIClass.getAnnotation(WebService.class);
        webServiceProviderAnnotation = (WebServiceProvider) implOrSEIClass.getAnnotation(WebServiceProvider.class);
        
        if (webServiceAnnotation == null && webServiceProviderAnnotation == null)
            // TODO: NLS
            throw ExceptionFactory.makeWebServiceException("Either WebService or WebServiceProvider annotation must be present on " + implOrSEIClass);
        else if (webServiceAnnotation != null && webServiceProviderAnnotation != null)
            // TODO: NLS
            throw ExceptionFactory.makeWebServiceException("Both WebService or WebServiceProvider annotations cannot be presenton " + implOrSEIClass);

        // If portName was specified, set it.  Otherwise, we will get it from the appropriate
        // annotation when the getter is called.
        // TODO: If the portName is specified, should we verify it against the annotation?
        // TODO: Add tests: null portName, !null portName, portName != annotation value
        if (portName != null)
            portQName = portName;

        // If this is an Endpoint-based service implementation (i.e. not a 
        // Provider-based one), then create the EndpointInterfaceDescription to contain
        // the operations on the endpoint.  Provider-based endpoints don't have operations
        // associated with them, so they don't have an EndpointInterfaceDescription.
        if (webServiceAnnotation != null) {
            // If this impl class references an SEI, then use that SEI to create the EndpointInterfaceDesc.
            // TODO: Add support for service impl endpoints that don't reference an SEI; remember that this is also called with just an SEI interface from svcDesc.updateWithSEI()
            String seiClassName = getEndpointInterface();
            Class seiClass = null;
            if (DescriptionUtils.isEmpty(seiClassName)) {
                // For now, just build the EndpointInterfaceDesc based on the class itself.
                // TODO: The EID ctor doesn't correctly handle anything but an SEI at this point; e.g. it doesn't publish the correct methods of just an impl.
                seiClass = theClass;
            }
            else { 
                try {
                    // TODO: Using Class.forName() is probably not the best long-term way to get the SEI class from the annotation
                    seiClass = Class.forName(seiClassName, false, Thread.currentThread().getContextClassLoader());
                } catch (ClassNotFoundException e) {
                    // TODO: Throwing wrong exception
                    e.printStackTrace();
                    throw new UnsupportedOperationException("Can't create SEI class: " + e);
                }
            }
            endpointInterfaceDescription = new EndpointInterfaceDescription(seiClass, this);
        }
    }
    
    public void updateWithSEI(Class sei) {
        // Updating with an SEI is only valid for Endpoint-based implementations;
        // it is not valid for a Provider-based implementation.
        if (endpointInterfaceDescription == null)
            // TODO: Correct error processing
            throw new UnsupportedOperationException("Can not update an SEI on a Provider implementation.  Rejected SEI = " + sei);
        if (sei != null) {
            endpointInterfaceDescription.updateWithSEI(sei);
        }
        return;
    }

    public QName getPortQName() {
        if (portQName == null) {
            // The name was not set by the constructors, so get it from the
            // appropriate annotaion.
            String name = getPortName();
            String tns = getTargetNamespace();
            // TODO: Check for name &/| tns null or empty string and add tests for same
            portQName = new QName(tns, name);
        }
        return portQName;
    }
    
    public ServiceDescription getServiceDescription() {
        return parentServiceDescription;
    }
    
    public EndpointInterfaceDescription getEndpointInterfaceDescription() {
        return endpointInterfaceDescription;
    }
    
    // ==========================================
    // Annotation-related methods
    // ==========================================
    
    public boolean isProviderBased() {
        return webServiceProviderAnnotation != null;
    }
    
    public boolean isEndpointBased() {
        return webServiceAnnotation != null;
    }
    
    // ------------------------------------------
    // Common WebService and WebServiceProvider annotation-related getters
    // ------------------------------------------
    
    public String getWSDLLocation() {
        if (annotation_WsdlLocation == null) {
            if (webServiceAnnotation != null) {
                annotation_WsdlLocation = webServiceAnnotation.wsdlLocation();
            }
            else if (webServiceProviderAnnotation != null) {
                annotation_WsdlLocation = webServiceProviderAnnotation.wsdlLocation();
            }
        }
        return annotation_WsdlLocation;
    }

    public String getServiceName() {
        if (annotation_ServiceName == null) {
            if (webServiceAnnotation != null) {
                annotation_ServiceName = webServiceAnnotation.serviceName();
            }
            else if (webServiceProviderAnnotation != null) {
                annotation_ServiceName = webServiceProviderAnnotation.serviceName();
            }
        }
        return annotation_ServiceName;
    }
    
    public String getPortName() {
        if (annotation_PortName == null) {
            if (webServiceAnnotation != null) {
                // REVIEW: Should this be portName() or just name()?
                annotation_PortName = webServiceAnnotation.portName();
            }
            else if (webServiceProviderAnnotation != null) {
                annotation_PortName = webServiceProviderAnnotation.portName();
            }
        }
        return annotation_PortName;
    }

    public String getTargetNamespace() {
        if (annotation_TargetNamespace == null) {
            if (webServiceAnnotation != null) {
                annotation_TargetNamespace = webServiceAnnotation.targetNamespace();
            }
            else if (webServiceProviderAnnotation != null) {
                annotation_TargetNamespace = webServiceProviderAnnotation.targetNamespace();
            }
        }
        return annotation_TargetNamespace;
    }
    // ------------------------------------------
    // WebServiceProvider annotation related getters
    // ------------------------------------------

    WebServiceProvider getWebServiceProviderAnnotation() {
        return webServiceProviderAnnotation;
    }

    // ------------------------------------------
    // WebService annotation related getters
    // ------------------------------------------

    WebService getWebServiceAnnotation() {
        return webServiceAnnotation;
    }
    
    public String getEndpointInterface() {
        if (webService_EndpointInterface == null && webServiceAnnotation != null) {
            webService_EndpointInterface = webServiceAnnotation.endpointInterface();
        }
        return webService_EndpointInterface;
    }
    
    public String getName() {
        if (webService_Name == null && webServiceAnnotation != null) {
            webService_Name = webServiceAnnotation.name();
        }
        return webService_Name;
    }
    
    // ------------------------------------------
    // ServiceMode annotation related getters
    // ------------------------------------------
    // REVIEW: Should this be returning an enum other than the one defined within the annotation?
    ServiceMode getServiceMode() {
        if (serviceModeAnnotation == null && implOrSEIClass != null) {
            serviceModeAnnotation = (ServiceMode) implOrSEIClass.getAnnotation(ServiceMode.class);
        }
        return serviceModeAnnotation;
    }
    
    public Service.Mode getServiceModeValue() {
        // This 
        if (serviceModeValue == null && isProviderBased() && getServiceMode() != null) {
            serviceModeValue = getServiceMode().value();
        }
        return serviceModeValue;
    }
    
    // ------------------------------------------
    // BindingType annotation related getters
    // ------------------------------------------

    BindingType getBindingType() {
        if (bindingTypeAnnotation == null && implOrSEIClass != null) {
            bindingTypeAnnotation = (BindingType) implOrSEIClass.getAnnotation(BindingType.class);
        }
        return bindingTypeAnnotation;
    }
    
    public String getBindingTypeValue() {
        if (bindingTypeValue == null && getBindingType() != null) {
            bindingTypeValue = getBindingType().value();
        }
        return bindingTypeValue;
    }

    // ------------------------------------------
    // HandlerChaing annotation related getters
    // ------------------------------------------

    /**
     * Returns a live list describing the handlers on this port.
     * TODO: This is currently returning List<String>, but it should return a HandlerDescritpion
     * object that can represent a handler description from various Metadata (annotation, deployment descriptors, etc);
     * use JAX-WS Appendix B Handler Chain Configuration File Schema as a starting point for HandlerDescription.
     *  
     * @return A List of handlers for this port.  The actual list is returned, and therefore can be modified.
     */
    public List<String> getHandlerList() {
        return handlerList;
    }
}
