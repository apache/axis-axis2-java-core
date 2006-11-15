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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jws.WebService;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.BindingType;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceProvider;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.OutInAxisOperation;
import org.apache.axis2.description.OutOnlyAxisOperation;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.WSDL11ToAllAxisServicesBuilder;
import org.apache.axis2.description.WSDL11ToAxisServiceBuilder;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * An EndpointDescription corresponds to a particular Service Implementation. It
 * can correspond to either either a client to that impl or the actual service
 * impl.
 * 
 * The EndpointDescription contains information that is relevant to both a
 * Provider-based and SEI-based (aka Endpoint-based or Java-based) enpdoints.
 * SEI-based endpoints (whether they have an explicit or implcit SEI) will have
 * addtional metadata information in an EndpointInterfaceDescription class and
 * sub-hierachy; Provider-based endpoitns to not have such a hierachy.
 */

/*
Working-design information.

Java Name: none [client]; Endpoint implementation class [server]

Axis2 Delegate: AxisService

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
public class EndpointDescription {
    private ServiceDescription parentServiceDescription;
    private AxisService axisService;

    private QName portQName;
    // Corresponds to a port that was added dynamically via addPort and is not declared (either in WSDL or annotations)
    private boolean isDynamicPort;
    // Note that an EndpointInterfaceDescription will ONLY be set for an Endpoint-based implementation;
    // it will NOT be set for a Provider-based implementation
    private EndpointInterfaceDescription endpointInterfaceDescription;

    // This can be an SEI (on the client or server) or a Service implentation (server only)
    // Note that for clients that are Dispatch, this will be null.  Also note that a client that was initially
    // dispatch (sei = null) could later do a getPort(sei), at which time the original EndpointDescription will be
    // updated with the SEI information.
    private Class implOrSEIClass;

    //On Client side, there should be One ServiceClient instance per AxisSerivce
    private ServiceClient serviceClient = null;
    
    public static final String AXIS_SERVICE_PARAMETER = "org.apache.axis2.jaxws.description.EndpointDescription";
    private static final Log log = LogFactory.getLog(EndpointDescription.class);

    
    // ===========================================
    // ANNOTATION related information
    // ===========================================
    
    // ANNOTATION: @WebService and @WebServiceProvider
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

    // ANNOTATION: @ServiceMode
    // Note this is only valid on a Provider-based endpoint
    private ServiceMode         serviceModeAnnotation;
    private Service.Mode        serviceModeValue;
    // Default ServiceMode.value per JAXWS Spec 7.1 "javax.xml.ServiceMode" pg 79
    public static final javax.xml.ws.Service.Mode  ServiceMode_DEFAULT = javax.xml.ws.Service.Mode.PAYLOAD;
    
    // ANNOTATION: @BindingType
    private BindingType         bindingTypeAnnotation;
    private String              bindingTypeValue;
    // Default BindingType.value per JAXWS Spec Sec 7.8 "javax.xml.ws.BindingType" pg 83 
    // and Sec 1.4 "SOAP Transport and Transfer Bindings" pg 119
    public static final String  BindingType_DEFAULT = javax.xml.ws.soap.SOAPBinding.SOAP11HTTP_BINDING;
    
    // ANNOTATION: @HandlerChain
    // TODO: @HandlerChain support
    // TODO: This needs to be a collection of handler descriptions; use JAX-WS Appendix B Handler Chain Configuration File Schema as a starting point
    private ArrayList<String> handlerList = new ArrayList<String>();

    /**
     * Create an EndpointDescription based on the WSDL port.  Note that per the JAX-WS Spec (Final Release, 4/19/2006
     * Section 4.2.3 Proxies, page 55)the "namespace component of the port is the target namespace of the WSDL 
     * definition document".
     * Note this is currently only used on the client-side (this may change).
     * 
     * @param theClass The SEI or Impl class.  This will be NULL for Dispatch clients
     *                 since they don't use an SEI
     */
    public EndpointDescription(Class theClass, QName portName, ServiceDescription parent) {
        this(theClass, portName, false, parent);
    }
    public EndpointDescription(Class theClass, QName portName, boolean dynamicPort, ServiceDescription parent) {
        // TODO: This and the other constructor will (eventually) take the same args, so the logic needs to be combined
        // TODO: If there is WSDL, could compare the namespace of the defn against the portQName.namespace
        this.parentServiceDescription = parent;
        this.portQName = portName;
        this.implOrSEIClass = theClass;
        this.isDynamicPort = dynamicPort;
        
        // TODO: Refactor this with the consideration of no WSDL/Generic Service/Annotated SEI
        setupAxisService();
        addToAxisService();

        buildDescriptionHierachy();
        addAnonymousAxisOperations();
        // This will set the serviceClient field after adding the AxisService to the AxisConfig
        getServiceClient();
        // Give the configuration builder a chance to finalize configuration for this service
        try {
            getServiceDescription().getClientConfigurationFactory().completeAxis2Configuration(axisService);
        } catch (DeploymentException e) {
            // TODO RAS
            // TODO NLS
            // TODO: Remove this println
            System.out.println("Caught exception in ServiceDescription.ServiceDescription: " + e);
            e.printStackTrace();
//            throw ExceptionFactory.makeWebServiceException("ServiceDescription caught " + e);
        } catch (Exception e) {
            // TODO RAS
            // TODO NLS
            // TODO: Remove this println
            System.out.println("Caught exception in ServiceDescription.ServiceDescription: " + e);
            e.printStackTrace();
//            throw ExceptionFactory.makeWebServiceException("ServiceDescription caught " + e);
        }
    }
    
    /**
     * Create from an annotated implementation or SEI class.
     * Note this is currently used only on the server-side (this probably won't change).
     *
     * @param theClass An implemntation or SEI class
     * @param portName May be null; if so the annotation is used
     * @param parent
     */
    // TODO: Remove axisService as paramater when the AxisService can be constructed from the annotations
    EndpointDescription(Class theClass, QName portName, AxisService axisService, ServiceDescription parent) {
        this.parentServiceDescription = parent;
        this.portQName = portName;
        this.implOrSEIClass = theClass;
        this.axisService = axisService;
        
        addToAxisService();

        buildEndpointDescriptionFromNoWSDL();
        
        // The anonymous AxisOperations are currently NOT added here.  The reason 
        // is that (for now) this is a SERVER-SIDE code path, and the anonymous operations
        // are only needed on the client side.
    }

    private void addToAxisService() {
        // Add a reference to this EndpointDescription object to the AxisService
        if (axisService != null) {
            Parameter parameter = new Parameter();
            parameter.setName(AXIS_SERVICE_PARAMETER);
            parameter.setValue(this);
            // TODO: What to do if AxisFault
            try {
                axisService.addParameter(parameter);
            } catch (AxisFault e) {
                // TODO: Throwing wrong exception
                e.printStackTrace();
                throw new UnsupportedOperationException("Can't add AxisService param: " + e);
            }
        }
    }

    private void buildEndpointDescriptionFromNoWSDL() {
        // TODO: The comments below are not quite correct; this method is used on BOTH the 
        //       client and server.  On the client the class is always an SEI.  On the server it is always a service impl
        //       which may be a provider or endpoint based; endpoint based may reference an SEI class
        
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
        // TODO: Get portName from annotation if it is null.

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
                seiClass = implOrSEIClass;
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

    public AxisService getAxisService() {
        return axisService;
    }
    
    public boolean isDynamicPort() {
        return isDynamicPort;
    }
    
    public void updateWithSEI(Class sei) {
        // Updating with an SEI is only valid for declared ports; it is not valid for dynamic ports.
        if (isDynamicPort()) {
            // TODO: RAS and NLS
            throw ExceptionFactory.makeWebServiceException("Can not update an SEI on a dynamic port.  PortQName:" + portQName);
        }
        if (sei == null) {
            // TODO: RAS and NLS
            throw ExceptionFactory.makeWebServiceException("EndpointDescription.updateWithSEI was passed a null SEI.  PortQName:" + portQName);
        }

        if (endpointInterfaceDescription != null) {
            // The EndpointInterfaceDescription was created previously based on the port declaration (i.e. WSDL)
            // so update that with information from the SEI annotations
            endpointInterfaceDescription.updateWithSEI(sei);
        }
        else {
            // An EndpointInterfaceDescription does not exist yet.  This currently happens in the case where there is 
            // NO WSDL provided and a Dispatch client is created for prior to a getPort being done for that port.
            // There was no WSDL to create the EndpointInterfaceDescription from and there was no annotated SEI to
            // use at that time.  Now we have an annotated SEI, so create the EndpointInterfaceDescription now.
            endpointInterfaceDescription = new EndpointInterfaceDescription(sei, this);
        }
        return;
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
        // This annotation is only valid on Provider-based endpoints. 
        if (isProviderBased() && serviceModeValue == null) {
            if (getServiceMode() != null) {
                serviceModeValue = getServiceMode().value();
            }
            else {
                serviceModeValue = ServiceMode_DEFAULT; 
            }
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
        if (bindingTypeValue == null) {
            if (getBindingType() != null) {
                bindingTypeValue = getBindingType().value();
            }
            else {
                // No BindingType annotation present; use default value
                bindingTypeValue = BindingType_DEFAULT;
            }
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
    
    private void setupAxisService() {
        // TODO: Need to use MetaDataQuery validator to merge WSDL (if any) and annotations (if any)
        // Build up the AxisService.  Note that if this is a dynamic port, then we don't use the
        // WSDL to build up the AxisService since the port added to the Service by the client is not
        // one that will be present in the WSDL.  A null class passed in as the SEI indicates this 
        // is a dispatch client.
        if (!isDynamicPort && getServiceDescription().getWSDLWrapper() != null) {
            buildAxisServiceFromWSDL();
        }
        else {
            buildAxisServiceFromNoWSDL();
        }
        
        if (axisService == null) {
            // TODO: RAS & NLS
            throw ExceptionFactory.makeWebServiceException("Unable to create AxisService for "
                    + createAxisServiceName());
        }

        // Save the Service QName as a parameter.
        Parameter serviceNameParameter = new Parameter();
        serviceNameParameter.setName(WSDL11ToAllAxisServicesBuilder.WSDL_SERVICE_QNAME);
        serviceNameParameter.setValue(getServiceDescription().getServiceQName());
        
        // Save the Port name.  Note: Axis does not expect a QName since the namespace for the port is the ns from the WSDL definition 
        Parameter portParameter = new Parameter();
        portParameter.setName(WSDL11ToAllAxisServicesBuilder.WSDL_PORT);
        portParameter.setValue(portQName.getLocalPart());

        try {
            axisService.addParameter(serviceNameParameter);
            axisService.addParameter(portParameter);                        
        } 
        catch (AxisFault e) {
            // TODO RAS
            e.printStackTrace();
        }
    }

    private void buildAxisServiceFromWSDL() {
        // TODO: Change this to use WSDLToAxisServiceBuilder superclass
        // Note that the axis service builder takes only the localpart of the port qname.
        // TODO:: This should check that the namespace of the definition matches the namespace of the portQName per JAXRPC spec
        WSDL11ToAxisServiceBuilder serviceBuilder = new WSDL11ToAxisServiceBuilder(getServiceDescription().getWSDLWrapper().getDefinition(), 
                getServiceDescription().getServiceQName(), portQName.getLocalPart());
        // TODO: Currently this only builds the client-side AxisService; it needs to do client and server somehow.
        // Patterned after AxisService.createClientSideAxisService
        serviceBuilder.setServerSide(false);
        try {
            axisService = serviceBuilder.populateService();
            axisService.setName(createAxisServiceName());
        } catch (AxisFault e) {
            // TODO We should not swallow a fault here.
            log.warn(Messages.getMessage("warnAxisFault", e.toString()));
        }
    }
    
    private void buildAxisServiceFromNoWSDL() {
        // TODO: Refactor this to create from annotations.
        String serviceName = null;
        if (portQName != null) {
            serviceName = createAxisServiceName();
        }
        else {
            // REVIEW: Can the portQName ever be null?
            // Make this service name unique.  The Axis2 engine assumes that a service it can not find is a client-side service.
            serviceName = ServiceClient.ANON_SERVICE + this.hashCode() + System.currentTimeMillis();
        }
        axisService = new AxisService(serviceName);
    }
    
    private void buildDescriptionHierachy() {
        // Build up the Description Hierachy.  Note that if this is a dynamic port, then we don't use the
        // WSDL to build up the hierachy since the port added to the Service by the client is not
        // one that will be present in the WSDL.
        if (!isDynamicPort && getServiceDescription().getWSDLWrapper() != null) {
            buildEndpointDescriptionFromWSDL();
        }
        else if (implOrSEIClass != null){
            // Create the rest of the description hierachy from annotations on the class.
            // If there is no SEI class, then this is a Distpach case, and we currently 
            // don't create the rest of the description hierachy (since it is not an SEI and thus
            // not operation-based client.
            buildEndpointDescriptionFromNoWSDL();
        }
    }
    
    private void buildEndpointDescriptionFromWSDL() {
        Definition wsdlDefinition = getServiceDescription().getWSDLWrapper().getDefinition();
        javax.wsdl.Service wsdlService = wsdlDefinition.getService(getServiceDescription().getServiceQName());
        if (wsdlService == null) {
            throw ExceptionFactory.makeWebServiceException(Messages.getMessage("serviceDescErr2", createAxisServiceName()));
        }
        
        Map wsdlPorts = wsdlService.getPorts();
        boolean wsdlPortFound = false;
        if (wsdlPorts != null && wsdlPorts.size() > 0) {
            Iterator wsdlPortIterator = wsdlPorts.values().iterator();
            while (wsdlPortIterator.hasNext() && !wsdlPortFound) {
                Port wsdlPort = (Port) wsdlPortIterator.next();
                // Note the namespace is not included on the WSDL Port.
                if (wsdlPort.getName().equals(portQName.getLocalPart())) {
                    // Create the Endpoint Interface Description based on the WSDL.
                    endpointInterfaceDescription = new EndpointInterfaceDescription(this);
                    // Update the EndpointInterfaceDescription created with WSDL with information from the
                    // annotations in the SEI
                    endpointInterfaceDescription.updateWithSEI(implOrSEIClass);
                    wsdlPortFound = true;
                }
            }
        }
        
        if (!wsdlPortFound) {
            // TODO: NLS and RAS
            throw ExceptionFactory.makeWebServiceException("WSDL Port not found for port " + portQName.getLocalPart());  
        }
    }
    
    /**
     * Adds the anonymous axis operations to the AxisService.  Note that this is only needed on 
     * the client side, and they are currently used in two cases
     * (1) For Dispatch clients (which don't use SEIs and thus don't use operations)
     * (2) TEMPORARLIY for Services created without WSDL (and thus which have no AxisOperations created)
     *  See the AxisInvocationController invoke methods for more details.
     *  
     *   Based on ServiceClient.createAnonymouService
     */
    private void addAnonymousAxisOperations() {
        if (axisService != null) {
            OutOnlyAxisOperation outOnlyOperation = new OutOnlyAxisOperation(ServiceClient.ANON_OUT_ONLY_OP);
            axisService.addOperation(outOnlyOperation);

            OutInAxisOperation outInOperation = new OutInAxisOperation(ServiceClient.ANON_OUT_IN_OP);
            axisService.addOperation(outInOperation);
        }
    }
    
    public ServiceClient getServiceClient(){
        try {
            if(serviceClient == null) {
                ConfigurationContext configCtx = getServiceDescription().getAxisConfigContext();
                AxisService axisSvc = getAxisService();
                serviceClient = new ServiceClient(configCtx, axisSvc);
            }
        } catch (AxisFault e) {
            throw ExceptionFactory.makeWebServiceException(
                    Messages.getMessage("serviceClientCreateError"), e);
        }
        return serviceClient;
    }
 
    private String createAxisServiceName() {
        String portName = null;
        if (portQName != null) {
            portName = portQName.getLocalPart();
        }
        else {
            portName = "NoPortNameSpecified";

        }
        return getServiceDescription().getServiceQName().getLocalPart() + "." + portName;
    }

}
