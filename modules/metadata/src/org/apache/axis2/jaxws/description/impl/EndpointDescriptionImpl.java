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
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.java.security.AccessController;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.EndpointDescriptionJava;
import org.apache.axis2.jaxws.description.EndpointDescriptionWSDL;
import org.apache.axis2.jaxws.description.EndpointInterfaceDescription;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.description.ServiceDescriptionWSDL;
import org.apache.axis2.jaxws.description.builder.DescriptionBuilderComposite;
import org.apache.axis2.jaxws.description.builder.MDQConstants;
import org.apache.axis2.jaxws.description.builder.WsdlComposite;
import org.apache.axis2.jaxws.description.xml.handler.HandlerChainsType;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.util.WSDL4JWrapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.wsdl.Binding;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.http.HTTPBinding;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap12.SOAP12Address;
import javax.wsdl.extensions.soap12.SOAP12Binding;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingType;
import javax.xml.ws.Service;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceProvider;
import javax.xml.ws.handler.PortInfo;
import javax.xml.ws.soap.SOAPBinding;
import java.io.InputStream;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/** @see ../EndpointDescription */
/*
 * TODO: EndpointDescription should be created via AxisService objects and not directly from WSDL
 * IMPORTANT NOTE: Axis2 currently only supports 1 service and 1 port under that service.  When that is
 * fixed, that will probably have an impact on this class.  In particular, I think this should be created 
 * somehow from an AxisService/AxisPort combination, and not directly from the WSDL.
 */
class EndpointDescriptionImpl
        implements EndpointDescription, EndpointDescriptionJava, EndpointDescriptionWSDL {

    private ServiceDescriptionImpl parentServiceDescription;
    private AxisService axisService;

    private QName portQName;
    private QName serviceQName;

    // Corresponds to a port that was added dynamically via addPort and is not declared (either in WSDL or annotations)
    private boolean isDynamicPort;

    // If the WSDL is fully specified, we could build the AxisService from the WSDL
    private boolean isAxisServiceBuiltFromWSDL;

    private String serviceImplName;    //class name of the service impl or SEI

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

    //This is the base WebService or WebServiceProvider that we are processing
    DescriptionBuilderComposite composite = null;

    // Set of packages that are needed to marshal/unmashal data (used to set JAXBContext)
    TreeSet<String> packages = null;

    // The JAX-WS Handler port information corresponding to this endpoint
    private PortInfo portInfo;

    private String clientBindingID;
    // The effective endpoint address.  It could be set by the client or come from the WSDL SOAP address
    private String endpointAddress;
    // The endpoint address from the WSDL soap:address extensibility element if present.
    private String wsdlSOAPAddress;

    private static final Log log = LogFactory.getLog(EndpointDescriptionImpl.class);

    // ===========================================
    // ANNOTATION related information
    // ===========================================

    // ANNOTATION: @WebService and @WebServiceProvider
    // Only one of these two annotations will be set; they are mutually exclusive
    private WebService webServiceAnnotation;
    private WebServiceProvider webServiceProviderAnnotation;

    //ANNOTATION: @HandlerChain
    private HandlerChain handlerChainAnnotation;
    private HandlerChainsType handlerChainsType;

    // Information common to both WebService and WebServiceProvider annotations
    private String annotation_WsdlLocation;
    private String annotation_ServiceName;
    private String annotation_PortName;
    private String annotation_TargetNamespace;

    // Information only set on WebService annotation
    // ANNOTATION: @WebService
    private String webService_EndpointInterface;
    private String webService_Name;

    // ANNOTATION: @ServiceMode
    // Note this is only valid on a Provider-based endpoint
    private ServiceMode serviceModeAnnotation;
    private Service.Mode serviceModeValue;
    // Default ServiceMode.value per JAXWS Spec 7.1 "javax.xml.ServiceMode" pg 79
    public static final javax.xml.ws.Service.Mode ServiceMode_DEFAULT =
            javax.xml.ws.Service.Mode.PAYLOAD;

    // ANNOTATION: @BindingType
    private BindingType bindingTypeAnnotation;
    private String bindingTypeValue;
    // Default BindingType.value per JAXWS Spec Sec 7.8 "javax.xml.ws.BindingType" pg 83 
    // and Sec 1.4 "SOAP Transport and Transfer Bindings" pg 119
    public static final String BindingType_DEFAULT =
            javax.xml.ws.soap.SOAPBinding.SOAP11HTTP_BINDING;

    /**
     * Create an EndpointDescription based on the WSDL port.  Note that per the JAX-WS Spec (Final
     * Release, 4/19/2006 Section 4.2.3 Proxies, page 55)the "namespace component of the port is the
     * target namespace of the WSDL definition document". Note this is currently only used on the
     * client-side (this may change).
     *
     * @param theClass The SEI or Impl class.  This will be NULL for Dispatch clients since they
     *                 don't use an SEI
     */
    EndpointDescriptionImpl(Class theClass, QName portName, ServiceDescriptionImpl parent) {
        this(theClass, portName, false, parent);
    }

    EndpointDescriptionImpl(Class theClass, QName portName, boolean dynamicPort,
                            ServiceDescriptionImpl parent) {
        // TODO: This and the other constructor will (eventually) take the same args, so the logic needs to be combined
        // TODO: If there is WSDL, could compare the namespace of the defn against the portQName.namespace
        this.parentServiceDescription = parent;
        this.implOrSEIClass = theClass;
        // REVIEW: setting these should probably be done in the getters!  It needs to be done before we try to select a 
        //         port to use if one wasn't specified because we'll try to get to the annotations to get the PortType
        if (this.implOrSEIClass != null) {
            webServiceAnnotation = (WebService)implOrSEIClass.getAnnotation(WebService.class);
            webServiceProviderAnnotation =
                    (WebServiceProvider)implOrSEIClass.getAnnotation(WebServiceProvider.class);
        }
        this.isDynamicPort = dynamicPort;
        if (DescriptionUtils.isEmpty(portName)) {
            // If the port name is null, then per JAX-WS 2.0 spec p. 55, the runtime is responsible for selecting the port.
            this.portQName = selectPortToUse();
        } else {
            this.portQName = portName;
        }
        // At this point, there must be a port QName set, either as passed in, or determined from the WSDL and/or annotations.
        // If not, that is an error.
        if (this.portQName == null) {
            if (log.isDebugEnabled()) {
                log.debug("PortQName was null and could not be determined by runtime.  Class: " +
                        theClass + "; ServiceDescription: " + parent);
            }
            throw ExceptionFactory.makeWebServiceException(
                    "EndpointDescription: portQName could not be determined for class " + theClass);
        }

        // TODO: Refactor this with the consideration of no WSDL/Generic Service/Annotated SEI
        setupAxisService();
        addToAxisService();

        buildDescriptionHierachy();
        addAnonymousAxisOperations();

        // This will set the serviceClient field after adding the AxisService to the AxisConfig
        getServiceClient();
        // Give the configuration builder a chance to finalize configuration for this service
        try {
            getServiceDescriptionImpl().getClientConfigurationFactory()
                    .completeAxis2Configuration(axisService);
        } catch (DeploymentException e) {
            // TODO RAS & NLS
            if (log.isDebugEnabled()) {
                log.debug(
                        "Caught DeploymentException attempting to complete configuration on AxisService: "
                                + axisService + " for ServiceDesription: " + parent, e);
            }
            throw ExceptionFactory.makeWebServiceException(
                    "Unable to complete configuration due to exception " + e, e);
        } catch (Exception e) {
            // TODO RAS & NLS
            if (log.isDebugEnabled()) {
                log.debug("Caught Exception attempting to complete configuration on AxisService: "
                        + axisService + " for ServiceDesription: " + parent, e);
            }
            throw ExceptionFactory.makeWebServiceException(
                    "Unable to complete configuration due to exception " + e, e);
        }
    }

    /**
     * Create an EndpointDescription based on the DescriptionBuilderComposite. Note that per the
     * JAX-WS Spec (Final Release, 4/19/2006 Section 4.2.3 Proxies, page 55)the "namespace component
     * of the port is the target namespace of the WSDL definition document".
     *
     * @param theClass The SEI or Impl class.  This will be NULL for Dispatch clients since they
     *                 don't use an SEI
     */
    EndpointDescriptionImpl(ServiceDescriptionImpl parent, String serviceImplName) {

        // TODO: This and the other constructor will (eventually) take the same args, so the logic needs to be combined
        // TODO: If there is WSDL, could compare the namespace of the defn against the portQName.namespace
        this.parentServiceDescription = parent;
        this.serviceImplName = serviceImplName;
        this.implOrSEIClass = null;

        composite = getServiceDescriptionImpl().getDescriptionBuilderComposite();
        if (composite == null) {
            throw ExceptionFactory.makeWebServiceException(
                    "EndpointDescription.EndpointDescription: parents DBC is null");
        }

        //Set the base level of annotation that we are processing...currently
        // a 'WebService' or a 'WebServiceProvider'
        if (composite.getWebServiceAnnot() != null)
            webServiceAnnotation = composite.getWebServiceAnnot();
        else
            webServiceProviderAnnotation = composite.getWebServiceProviderAnnot();

        // REVIEW: Maybe this should be an error if the name has already been set and it doesn't match
        // Note that on the client side, the service QN should be set; on the server side it will not be.
        if (DescriptionUtils.isEmpty(getServiceDescription().getServiceQName())) {
            getServiceDescriptionImpl().setServiceQName(getServiceQName());
        }
        //Call the getter to insure the qualified port name is set. 
        getPortQName();

        // TODO: Refactor this with the consideration of no WSDL/Generic Service/Annotated SEI
        setupAxisServiceFromDBL();
        addToAxisService();    //Add a reference to this EndpointDescription to the AxisService

        //TODO: Need to remove operations from AxisService that have 'exclude = true
        //      then call 'validateOperations' to verify that WSDL and AxisService match up,
        //      Remember that this will only happen when we generate an AxisService from existing
        //		WSDL and then need to perform further processing because we have annotations as well
        //		If there is no WSDL, we would never process the Method to begin with.

        buildDescriptionHierachy();

        WsdlComposite wsdlComposite = null;
        
        String bindingType = getBindingType();
        boolean isSOAP12 = (bindingType.equals( javax.xml.ws.soap.SOAPBinding.SOAP12HTTP_BINDING) 
                            || bindingType.equals(javax.xml.ws.soap.SOAPBinding.SOAP12HTTP_MTOM_BINDING)) 
                            ? true : false;

        //Determine if we need to generate WSDL
        //First, make sure that this is not a SOAP 1.2 based binding, per JAXWS spec. we cannot 
        //generate WSDL if the binding type is SOAP 1.2 based.
        //Then, assuming the composite does not contain a 
        //Wsdl Definition, go ahead and generate it
        // REVIEW: I think this should this be isSOAP11 so the generators are only called for 
        //         SOAP11; i.e. NOT for SOAP12 or XML/HTTP bindings.
        if (!isSOAP12) {
            if (
                    (isEndpointBased() &&
                            DescriptionUtils.isEmpty(getAnnoWebServiceEndpointInterface()))
                            ||
                            (!isEndpointBased())
                    ) {
                //This is either an implicit SEI, or a WebService Provider
    
                wsdlComposite = generateWSDL(composite);
    
            } else if (isEndpointBased()) {
                //This impl class specifies an SEI...this is a special case. There is a bug
                //in the tooling that allows for the wsdllocation to be specifed on either the
                //impl. class, or the SEI, or both. So, we need to look for the wsdl as follows:
                //			1. If the Wsdl exists on the SEI, then check for it on the impl.
                //			2. If it is not found in either location, in that order, then generate
    
                DescriptionBuilderComposite seic =
                        getServiceDescriptionImpl().getDBCMap()
                                .get(composite.getWebServiceAnnot().endpointInterface());
    
                //Only generate WSDL if a definition doesn't already exist
                if (seic.getWsdlDefinition() == null)
                    wsdlComposite = generateWSDL(composite);
            }

        } else if (composite.getWsdlDefinition() == null) {
            //This is a SOAP12 binding that does not contain a WSDL definition, log a WARNING
            log.warn("This implementation does not contain a WSDL definition and uses a SOAP 1.2 based binding. " +
                    "Per JAXWS spec. - a WSDL definition cannot be generated for this implementation. Name: "
                    + composite.getClassName());
        }

        if (!isSOAP12) {
    
            //Save the WSDL Location and the WsdlDefinition, value depends on whether wsdl was generated
            Parameter wsdlLocationParameter = new Parameter();
            wsdlLocationParameter.setName(MDQConstants.WSDL_LOCATION);
    
            Parameter wsdlDefParameter = new Parameter();
            wsdlDefParameter.setName(MDQConstants.WSDL_DEFINITION);
    
            Parameter wsdlCompositeParameter = new Parameter();
            wsdlCompositeParameter.setName(MDQConstants.WSDL_COMPOSITE);
    
            if (wsdlComposite != null) {
    
                //We have a wsdl composite, so set these values for the generated wsdl
                wsdlCompositeParameter.setValue(wsdlComposite);
                wsdlLocationParameter.setValue(wsdlComposite.getWsdlFileName());
                wsdlDefParameter.setValue(
                        getServiceDescriptionImpl().getGeneratedWsdlWrapper().getDefinition());
            } else if (getServiceDescriptionImpl().getWSDLWrapper() != null) {
                //No wsdl composite because wsdl already exists
                wsdlLocationParameter.setValue(getAnnoWebServiceWSDLLocation());
                wsdlDefParameter.setValue(getServiceDescriptionImpl().getWSDLWrapper().getDefinition());
            } else {
                //There is no wsdl composite and there is NOT a wsdl definition
                wsdlLocationParameter.setValue(null);
                wsdlDefParameter.setValue(null);
    
            }
    
            try {
                if (wsdlComposite != null) {
                    axisService.addParameter(wsdlCompositeParameter);
                }
                axisService.addParameter(wsdlDefParameter);
                axisService.addParameter(wsdlLocationParameter);
            } catch (Exception e) {
                throw ExceptionFactory.makeWebServiceException(
                        "EndpointDescription: Unable to add parameters to AxisService");
            }
        }
        else {
            // Need to account for SOAP 1.2 WSDL when supplied with application
            Parameter wsdlDefParameter = new Parameter();
            wsdlDefParameter.setName(MDQConstants.WSDL_DEFINITION);
            Parameter wsdlLocationParameter = new Parameter();
            wsdlLocationParameter.setName(MDQConstants.WSDL_LOCATION);
            if (getServiceDescriptionImpl().getWSDLWrapper() != null) {
                wsdlLocationParameter.setValue(getAnnoWebServiceWSDLLocation());
                wsdlDefParameter.setValue(getServiceDescriptionImpl().getWSDLWrapper()
                    .getDefinition());
            }
            // No WSDL supplied and we do not generate for non-SOAP 1.1/HTTP
            // endpoints
            else {
                wsdlLocationParameter.setValue(null);
                wsdlDefParameter.setValue(null);
            }
            try {
                axisService.addParameter(wsdlDefParameter);
                axisService.addParameter(wsdlLocationParameter);

            } catch (Exception e) {
                throw ExceptionFactory
                    .makeWebServiceException("EndpointDescription: Unable to add parameters to AxisService");
            }
        }
    }

    /**
     * Create from an annotated implementation or SEI class. Note this is
     * currently used only on the server-side (this probably won't change).
     * 
     * @param theClass An implemntation or SEI class
     * @param portName May be null; if so the annotation is used
     * @param parent
     */
    EndpointDescriptionImpl(Class theClass, QName portName, AxisService axisService,
                            ServiceDescriptionImpl parent) {
        this.parentServiceDescription = parent;
        this.portQName = portName;
        this.implOrSEIClass = theClass;
        this.axisService = axisService;

        addToAxisService();

        buildEndpointDescriptionFromAnnotations();

        // The anonymous AxisOperations are currently NOT added here.  The reason 
        // is that (for now) this is a SERVER-SIDE code path, and the anonymous operations
        // are only needed on the client side.
    }

    private void addToAxisService() {
        // Add a reference to this EndpointDescription object to the AxisService
        if (axisService != null) {
            Parameter parameter = new Parameter();
            parameter.setName(EndpointDescription.AXIS_SERVICE_PARAMETER);
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

    private void buildEndpointDescriptionFromAnnotations() {
        // TODO: The comments below are not quite correct; this method is used on BOTH the 
        //       client and server.  On the client the class is always an SEI.  On the server it 
        //		 is always a service impl which may be a provider or endpoint based;
        //		 endpoint based may reference an SEI class

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


        if (!getServiceDescriptionImpl().isDBCMap()) {

            webServiceAnnotation = (WebService)implOrSEIClass.getAnnotation(WebService.class);
            webServiceProviderAnnotation =
                    (WebServiceProvider)implOrSEIClass.getAnnotation(WebServiceProvider.class);

            if (webServiceAnnotation == null && webServiceProviderAnnotation == null)
                // TODO: NLS
                throw ExceptionFactory.makeWebServiceException(
                        "Either WebService or WebServiceProvider annotation must be present on " +
                                implOrSEIClass);
            else if (webServiceAnnotation != null && webServiceProviderAnnotation != null)
                // TODO: NLS
                throw ExceptionFactory.makeWebServiceException(
                        "Both WebService or WebServiceProvider annotations cannot be presenton " +
                                implOrSEIClass);
        }
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
            // TODO: Add support for service impl endpoints that don't reference an SEI; remember
            //       that this is also called with just an SEI interface from svcDesc.updateWithSEI()
            String seiClassName = getAnnoWebServiceEndpointInterface();

            if (!getServiceDescriptionImpl().isDBCMap()) {
                Class seiClass = null;
                if (DescriptionUtils.isEmpty(seiClassName)) {
                    // For now, just build the EndpointInterfaceDesc based on the class itself.
                    // TODO: The EID ctor doesn't correctly handle anything but an SEI at this
                    //       point; e.g. it doesn't publish the correct methods of just an impl.
                    seiClass = implOrSEIClass;
                } else {
                    try {
                        // TODO: Using Class forName() is probably not the best long-term way to get the SEI class from the annotation
                        seiClass = forName(seiClassName, false,
                                           getContextClassLoader());
                        // Catch Throwable as ClassLoader can throw an NoClassDefFoundError that
                        // does not extend Exception, so lets catch everything that extends Throwable
                        // rather than just Exception.
                    } catch (Throwable e) {
                        // TODO: Throwing wrong exception
                        e.printStackTrace();
                        throw new UnsupportedOperationException("Can't create SEI class: " + e);
                    }
                }
                endpointInterfaceDescription = new EndpointInterfaceDescriptionImpl(seiClass, this);
            } else {
                //TODO: Determine if we need logic here to determine implied SEI or not. This logic
                //		may be handled by EndpointInterfaceDescription

                if (DescriptionUtils.isEmpty(getAnnoWebServiceEndpointInterface())) {

                    //TODO: Build the EndpointInterfaceDesc based on the class itself
                    endpointInterfaceDescription =
                            new EndpointInterfaceDescriptionImpl(composite, true, this);

                } else {
                    //Otherwise, build the EID based on the SEI composite
                    endpointInterfaceDescription = new EndpointInterfaceDescriptionImpl(
                            getServiceDescriptionImpl().getDBCMap().get(seiClassName),
                            false,
                            this);
                }
            }
        } else {
            //TODO: process a WebServiceProvider
        }
    }

    public QName getPortQName() {
        // REVIEW: Implement WSDL/Annotation merge? May be OK as is; not sure how would know WHICH port Qname to get out of the WSDL if 
        //       we didn't use annotations.
        if (portQName == null) {
            // The name was not set by the constructors, so get it from the
            // appropriate annotation.
            String name = getAnnoWebServicePortName();
            String tns = getAnnoWebServiceTargetNamespace();

            // TODO: Check for name &/| tns null or empty string and add tests for same
            portQName = new QName(tns, name);
        }
        return portQName;
    }

    public QName getServiceQName() {
        if (serviceQName == null) {
            // If the service name has been set on the Service, use that.  Otherwise
            // get the name off the annotations
            QName serviceDescQName = getServiceDescription().getServiceQName();
            if (!DescriptionUtils.isEmpty(serviceDescQName)) {
                serviceQName = serviceDescQName;
            } else {
                String localPart = getAnnoWebServiceServiceName();
                String tns = getAnnoWebServiceTargetNamespace();
                serviceQName = new QName(tns, localPart);
            }
        }
        return serviceQName;
    }

    public ServiceDescription getServiceDescription() {
        return parentServiceDescription;
    }

    ServiceDescriptionImpl getServiceDescriptionImpl() {
        return (ServiceDescriptionImpl)parentServiceDescription;
    }

    public EndpointInterfaceDescription getEndpointInterfaceDescription() {
        return endpointInterfaceDescription;
    }

    public AxisService getAxisService() {
        return axisService;
    }

    boolean isDynamicPort() {
        return isDynamicPort;
    }

    void updateWithSEI(Class sei) {
        // Updating with an SEI is only valid for declared ports; it is not valid for dynamic ports.
        if (isDynamicPort()) {
            // TODO: RAS and NLS
            throw ExceptionFactory.makeWebServiceException(
                    "Can not update an SEI on a dynamic port.  PortQName:" + portQName);
        }
        if (sei == null) {
            // TODO: RAS and NLS
            throw ExceptionFactory.makeWebServiceException(
                    "EndpointDescription.updateWithSEI was passed a null SEI.  PortQName:" +
                            portQName);
        }

        if (endpointInterfaceDescription != null) {
            // The EndpointInterfaceDescription was created previously based on the port declaration (i.e. WSDL)
            // so update that with information from the SEI annotations
            ((EndpointInterfaceDescriptionImpl)endpointInterfaceDescription).updateWithSEI(sei);
        } else {
            // An EndpointInterfaceDescription does not exist yet.  This currently happens in the case where there is 
            // NO WSDL provided and a Dispatch client is created for prior to a getPort being done for that port.
            // There was no WSDL to create the EndpointInterfaceDescription from and there was no annotated SEI to
            // use at that time.  Now we have an annotated SEI, so create the EndpointInterfaceDescription now.
            endpointInterfaceDescription = new EndpointInterfaceDescriptionImpl(sei, this);
        }
        return;
    }

    private void setupAxisService() {
        // TODO: Need to use MetaDataQuery validator to merge WSDL (if any) and annotations (if any)
        // Build up the AxisService.  Note that if this is a dynamic port, then we don't use the
        // WSDL to build up the AxisService since the port added to the Service by the client is not
        // one that will be present in the WSDL.  A null class passed in as the SEI indicates this 
        // is a dispatch client.
        if (!isDynamicPort && getServiceDescriptionImpl().getWSDLWrapper() != null) {
            isAxisServiceBuiltFromWSDL = buildAxisServiceFromWSDL();
        } else {
            buildAxisServiceFromAnnotations();
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

    /*
     * This setups and builds the AxisService using only the DescriptionBuilderCompositeList
     * 
     */
    private void setupAxisServiceFromDBL() {
        // TODO: Need to use MetaDataQuery validator to merge WSDL (if any) and annotations (if any)
        // Build up the AxisService.  Note that if this is a dispatch client, then we don't use the
        // WSDL to build up the AxisService since the port added to the Service by the client is not
        // one that will be present in the WSDL.  A null class passed in as the SEI indicates this 
        // is a dispatch client.

        // If WSDL is present, it may be full or only partial.  If we can create the AxisService from 
        // the WSDL, that WSDL is fully specified.  Otherwise, it is "partial WSDL".  In that case
        // we use annotaions to build the AxisService
        isAxisServiceBuiltFromWSDL = false;
        if (getServiceDescriptionImpl().getWSDLWrapper() != null) {
            isAxisServiceBuiltFromWSDL = buildAxisServiceFromWSDL();

        }

        if (!isAxisServiceBuiltFromWSDL) {
            //generateWSDL(composite);
            buildAxisServiceFromAnnotations();
        }

        if (axisService == null) {
            // TODO: RAS & NLS
            throw ExceptionFactory.makeWebServiceException("Unable to create AxisService for "
                    + createAxisServiceName());
        }

        //Save the Port Type name
        Parameter portTypeNameParameter = new Parameter();
        portTypeNameParameter.setName(MDQConstants.WSDL_PORTTYPE_NAME);
        portTypeNameParameter.setValue(getName());

        // Save the Service QName as a parameter.
        Parameter serviceNameParameter = new Parameter();
        serviceNameParameter.setName(MDQConstants.WSDL_SERVICE_QNAME);
        serviceNameParameter.setValue(getServiceDescription().getServiceQName());

        // Save the Port name.  Note: Axis does not expect a QName since the namespace
        //   for the port is the ns from the WSDL definition 
        Parameter portParameter = new Parameter();
        portParameter.setName(MDQConstants.WSDL_PORT);
        portParameter.setValue(getPortQName().getLocalPart());

        //Save the fully qualified class name for the serviceImpl
        Parameter serviceClassNameParameter = new Parameter();
        serviceClassNameParameter.setName(MDQConstants.SERVICE_CLASS);
        serviceClassNameParameter
                .setValue(DescriptionUtils.javifyClassName(composite.getClassName()));

        try {
            axisService.addParameter(portTypeNameParameter);
            axisService.addParameter(serviceNameParameter);
            axisService.addParameter(portParameter);
            axisService.addParameter(serviceClassNameParameter);
        }
        catch (AxisFault e) {
            // TODO RAS
            e.printStackTrace();
        }
    }

    private boolean buildAxisServiceFromWSDL() {
        boolean isBuiltFromWSDL = false;
        try {

            // TODO: Change this to use WSDLToAxisServiceBuilder superclass
            // Note that the axis service builder takes only the localpart of the port qname.
            // TODO:: This should check that the namespace of the definition matches the namespace of the portQName per JAXRPC spec

            WSDL11ToAxisServiceBuilder serviceBuilder =
                    new WSDL11ToAxisServiceBuilder(
                            getServiceDescriptionImpl().getWSDLWrapper().getDefinition(),
                            getServiceDescription().getServiceQName(),
                            getPortQName().getLocalPart());

            if (getServiceDescriptionImpl().isDBCMap()) {
                //this.class.getClass().getClassLoader();
                URIResolverImpl uriResolver =
                        new URIResolverImpl(composite.getClassLoader());
                serviceBuilder.setCustomResolver(uriResolver);
            } else {
                ClassLoader classLoader = (ClassLoader)AccessController.doPrivileged(new
                        PrivilegedAction() {
                            public Object run() {
                                return Thread.currentThread().getContextClassLoader();
                            }
                        });
                URIResolverImpl uriResolver = new URIResolverImpl(classLoader);
                serviceBuilder.setCustomResolver(uriResolver);
            }

            // TODO: Currently this only builds the client-side AxisService;
            // it needs to do client and server somehow.
            // Patterned after AxisService.createClientSideAxisService
            if (getServiceDescriptionImpl().isServerSide())
                serviceBuilder.setServerSide(true);
            else
                serviceBuilder.setServerSide(false);

            axisService = serviceBuilder.populateService();
            axisService.setName(createAxisServiceName());
            isBuiltFromWSDL = true;

        } catch (AxisFault e) {
            // REVIEW: If we couldn't use the WSDL, should we fail instead of continuing to process using annotations?
            //         Note that if we choose to fail, we need to distinguish the partial WSDL case (which can not fail)
            // TODO: RAS/NLS  Need to update the message with the appropriate inserts
//    		log.warn(Messages.getMessage("warnAxisFault", e.toString()), e);
            String wsdlLocation = (getServiceDescriptionImpl().getWSDLLocation() != null) ?
                    getServiceDescriptionImpl().getWSDLLocation().toString() : null;
            String implClassName = null;
            if (getServiceDescriptionImpl().isDBCMap()) {
                implClassName = composite.getClassName();
            } else {
                implClassName = (implOrSEIClass != null) ? implOrSEIClass.getName() : null;
            }
            log.warn(
                    "The WSDL file could not be used due to an exception.  The WSDL will be ignored and annotations will be used.  Implementaiton class: "
                            + implClassName + "; WSDL Location: " + wsdlLocation + "; Exception: " +
                            e.toString(), e);
            isBuiltFromWSDL = false;
            return isBuiltFromWSDL;
        }
        return isBuiltFromWSDL;
    }

    private void buildAxisServiceFromAnnotations() {
        // TODO: Refactor this to create from annotations.
        String serviceName = null;
        if (portQName != null) {
            serviceName = createAxisServiceName();
        } else {
            // REVIEW: Can the portQName ever be null?
            // Make this service name unique.  The Axis2 engine assumes that a service it can not find is a client-side service.
            serviceName = ServiceClient.ANON_SERVICE + this.hashCode() + System.currentTimeMillis();
        }
        axisService = new AxisService(serviceName);

        //TODO: Set other things on AxisService here, this function may have to be
        //      moved to after we create all the AxisOperations
    }

    private void buildDescriptionHierachy() {
        // Build up the Description Hierachy.  Note that if this is a dynamic port, then we don't use the
        // WSDL to build up the hierachy since the port added to the Service by the client is not
        // one that will be present in the WSDL.

        //First, check to see if we can build this with the DBC List
        //TODO: When MDQ input is the only possible input, then we can remove the check for
        //      the DBC list, until that time the code in here may appear somewhat redundant
        if (getServiceDescriptionImpl().isDBCMap()) {
            if (!isDynamicPort && isWSDLFullySpecified())
                buildEndpointDescriptionFromWSDL();
            else
                buildEndpointDescriptionFromAnnotations();
        } else {
            //Still processing annotations from the class
            // This path was not updated
            if (!isDynamicPort && isWSDLFullySpecified()) {
                buildEndpointDescriptionFromWSDL();
            } else if (implOrSEIClass != null) {
                // Create the rest of the description hierachy from annotations on the class.
                // If there is no SEI class, then this is a Distpach case, and we currently
                // don't create the rest of the description hierachy (since it is not an SEI and thus
                // not operation-based client.
                buildEndpointDescriptionFromAnnotations();
            }
        }
    }

    private void buildEndpointDescriptionFromWSDL() {
        Definition wsdlDefinition = getServiceDescriptionImpl().getWSDLWrapper().getDefinition();
        javax.wsdl.Service wsdlService =
                wsdlDefinition.getService(getServiceDescription().getServiceQName());
        if (wsdlService == null) {
            throw ExceptionFactory.makeWebServiceException(
                    Messages.getMessage("serviceDescErr2", createAxisServiceName()));
        }

        Map wsdlPorts = wsdlService.getPorts();
        boolean wsdlPortFound = false;
        if (wsdlPorts != null && wsdlPorts.size() > 0) {
            Iterator wsdlPortIterator = wsdlPorts.values().iterator();

            while (wsdlPortIterator.hasNext() && !wsdlPortFound) {
                Port wsdlPort = (Port)wsdlPortIterator.next();
                // Note the namespace is not included on the WSDL Port.
                if (wsdlPort.getName().equals(portQName.getLocalPart())) {

                    // Build the EndpointInterface based on the specified SEI if there is one
                    // or on the service impl class (i.e. an implicit SEI).
                    if (getServiceDescriptionImpl().isDBCMap()) {
                        String seiClassName = getAnnoWebServiceEndpointInterface();
                        if (DescriptionUtils.isEmpty(seiClassName)) {
                            // No SEI specified, so use the service impl as an implicit SEI
                            endpointInterfaceDescription =
                                    new EndpointInterfaceDescriptionImpl(composite, true, this);
                        } else {
                            // Otherwise, build the EID based on the SEI composite
                            endpointInterfaceDescription = new EndpointInterfaceDescriptionImpl(
                                    getServiceDescriptionImpl().getDBCMap().get(seiClassName),
                                    false,
                                    this);
                        }

                    } else {
                        // Create the Endpoint Interface Description based on the WSDL.
                        endpointInterfaceDescription = new EndpointInterfaceDescriptionImpl(this);

                        // Update the EndpointInterfaceDescription created with WSDL with information from the
                        // annotations in the SEI
                        ((EndpointInterfaceDescriptionImpl)endpointInterfaceDescription)
                                .updateWithSEI(implOrSEIClass);
                    }
                    wsdlPortFound = true;
                }
            }
        }

        if (!wsdlPortFound) {
            // TODO: NLS and RAS
            throw ExceptionFactory.makeWebServiceException(
                    "WSDL Port not found for port " + portQName.getLocalPart());
        }
    }

    /**
     * Adds the anonymous axis operations to the AxisService.  Note that this is only needed on the
     * client side, and they are currently used in two cases (1) For Dispatch clients (which don't
     * use SEIs and thus don't use operations) (2) TEMPORARLIY for Services created without WSDL
     * (and thus which have no AxisOperations created) See the AxisInvocationController invoke
     * methods for more details.
     * <p/>
     * Based on ServiceClient.createAnonymouService
     */
    private void addAnonymousAxisOperations() {
        if (axisService != null) {
            OutOnlyAxisOperation outOnlyOperation =
                    new OutOnlyAxisOperation(ServiceClient.ANON_OUT_ONLY_OP);
            axisService.addOperation(outOnlyOperation);

            OutInAxisOperation outInOperation =
                    new OutInAxisOperation(ServiceClient.ANON_OUT_IN_OP);
            axisService.addOperation(outInOperation);
        }
    }

    public ServiceClient getServiceClient() {
        try {
            if (serviceClient == null) {
                ConfigurationContext configCtx = getServiceDescription().getAxisConfigContext();
                AxisService axisSvc = getAxisService();
                AxisConfiguration axisCfg = configCtx.getAxisConfiguration();
                if (axisCfg.getService(axisSvc.getName()) != null) {
                    axisSvc.setName(axisSvc.getName() + this.hashCode());
                }
                serviceClient = new ServiceClient(configCtx, axisSvc);
            }
        } catch (AxisFault e) {
            throw ExceptionFactory.makeWebServiceException(
                    Messages.getMessage("serviceClientCreateError"), e);
        }
        return serviceClient;
    }

    //This should eventually be deprecated in favor 'createAxisServiceNameFromDBL
    private String createAxisServiceName() {
        String portName = null;
        if (portQName != null) {
            portName = portQName.getLocalPart();
        } else {
            portName = "NoPortNameSpecified";

        }
        return getServiceDescription().getServiceQName().getLocalPart() + "." + portName;
    }

    public boolean isWSDLFullySpecified() {
        return isAxisServiceBuiltFromWSDL;
    }

    public boolean isProviderBased() {
        return webServiceProviderAnnotation != null;
    }

    public boolean isEndpointBased() {
        return webServiceAnnotation != null;
    }

    // ===========================================
    // ANNOTATION: WebService and WebServiceProvider
    // ===========================================

    public String getAnnoWebServiceWSDLLocation() {
        if (annotation_WsdlLocation == null) {

            if (getAnnoWebService() != null) {
                annotation_WsdlLocation = getAnnoWebService().wsdlLocation();

                //If this is not an implicit SEI, then make sure that its not on the SEI
                if (getServiceDescriptionImpl().isDBCMap()) {
                    if (!DescriptionUtils.isEmpty(getAnnoWebServiceEndpointInterface())) {

                        DescriptionBuilderComposite seic =
                                getServiceDescriptionImpl().getDBCMap()
                                        .get(composite.getWebServiceAnnot().endpointInterface());
                        if (!DescriptionUtils.isEmpty(seic.getWebServiceAnnot().wsdlLocation())) {
                            annotation_WsdlLocation = seic.getWebServiceAnnot().wsdlLocation();
                        }
                    }
                }
            } else if (getAnnoWebServiceProvider() != null
                    && !DescriptionUtils.isEmpty(getAnnoWebServiceProvider().wsdlLocation())) {
                annotation_WsdlLocation = getAnnoWebServiceProvider().wsdlLocation();
            } else {
                // There is no default value per JSR-181 MR Sec 4.1 pg 16
                annotation_WsdlLocation = "";
            }
        }
        return annotation_WsdlLocation;
    }

    public String getAnnoWebServiceServiceName() {
        if (annotation_ServiceName == null) {
            if (getAnnoWebService() != null
                    && !DescriptionUtils.isEmpty(getAnnoWebService().serviceName())) {
                annotation_ServiceName = getAnnoWebService().serviceName();
            } else if (getAnnoWebServiceProvider() != null
                    && !DescriptionUtils.isEmpty(getAnnoWebServiceProvider().serviceName())) {
                annotation_ServiceName = getAnnoWebServiceProvider().serviceName();
            } else {
                // Default value is the "simple name" of the class or interface + "Service"
                // Per JSR-181 MR Sec 4.1, pg 15
                if (getServiceDescriptionImpl().isDBCMap()) {
                    annotation_ServiceName = DescriptionUtils
                            .getSimpleJavaClassName(composite.getClassName()) + "Service";
                } else {
                    annotation_ServiceName =
                            DescriptionUtils.getSimpleJavaClassName(implOrSEIClass) + "Service";
                }
            }
        }
        return annotation_ServiceName;
    }

    public String getAnnoWebServicePortName() {
        if (annotation_PortName == null) {
            if (getAnnoWebService() != null
                    && !DescriptionUtils.isEmpty(getAnnoWebService().portName())) {
                annotation_PortName = getAnnoWebService().portName();
            } else if (getAnnoWebServiceProvider() != null
                    && !DescriptionUtils.isEmpty(getAnnoWebServiceProvider().portName())) {
                annotation_PortName = getAnnoWebServiceProvider().portName();
            } else {
                // Default the value
                if (isProviderBased()) {
                    // This is the @WebServiceProvider annotation path
                    // Default value is not specified in JSR-224, but we can assume it is 
                    // similar to the default in the WebService case, however there is no
                    // name attribute for a WebServiceProvider.  So in this case we use 
                    // the default value for WebService.name per JSR-181 MR sec 4.1 pg 15.
                    // Note that this is really the same thing as the call to getWebServiceName() 
                    // in the WebService case; it is done sepertely just to be clear there is no 
                    // name element on the WebServiceProvider annotation

                    annotation_PortName = (getServiceDescriptionImpl().isDBCMap()) ?
                            DescriptionUtils.getSimpleJavaClassName(composite.getClassName()) +
                                    "Port"
                            : DescriptionUtils.getSimpleJavaClassName(implOrSEIClass) + "Port";
                } else {
                    // This is the @WebService annotation path
                    // Default value is the @WebService.name of the class or interface + "Port"
                    // Per JSR-181 MR Sec 4.1, pg 15
                    annotation_PortName = getAnnoWebServiceName() + "Port";
                }
            }
        }
        return annotation_PortName;
    }

    public String getAnnoWebServiceTargetNamespace() {
        if (annotation_TargetNamespace == null) {
            if (getAnnoWebService() != null
                    && !DescriptionUtils.isEmpty(getAnnoWebService().targetNamespace())) {
                annotation_TargetNamespace = getAnnoWebService().targetNamespace();
            } else if (getAnnoWebServiceProvider() != null
                    && !DescriptionUtils.isEmpty(getAnnoWebServiceProvider().targetNamespace())) {
                annotation_TargetNamespace = getAnnoWebServiceProvider().targetNamespace();
            } else {
                // Default value per JSR-181 MR Sec 4.1 pg 15 defers to "Implementation defined, 
                // as described in JAX-WS 2.0, section 3.2" which is JAX-WS 2.0 Sec 3.2, pg 29.
                // FIXME: Hardcoded protocol for namespace
                if (getServiceDescriptionImpl().isDBCMap())
                    annotation_TargetNamespace =
                            DescriptionUtils.makeNamespaceFromPackageName(
                                    DescriptionUtils.getJavaPackageName(composite.getClassName()),
                                    "http");
                else
                    annotation_TargetNamespace =
                            DescriptionUtils.makeNamespaceFromPackageName(
                                    DescriptionUtils.getJavaPackageName(implOrSEIClass), "http");

            }
        }
        return annotation_TargetNamespace;
    }

    // ===========================================
    // ANNOTATION: WebServiceProvider
    // ===========================================

    public WebServiceProvider getAnnoWebServiceProvider() {
        return webServiceProviderAnnotation;
    }

    // ===========================================
    // ANNOTATION: WebService
    // ===========================================

    public WebService getAnnoWebService() {
        return webServiceAnnotation;
    }

    public String getAnnoWebServiceEndpointInterface() {
        // TODO: Validation: Not allowed on WebServiceProvider
        if (webService_EndpointInterface == null) {
            if (!isProviderBased() && getAnnoWebService() != null
                    && !DescriptionUtils.isEmpty(getAnnoWebService().endpointInterface())) {
                webService_EndpointInterface = getAnnoWebService().endpointInterface();
            } else {
                // This element is not valid on a WebServiceProvider annotation
                // REVIEW: Is this a correct thing to return if this is called against a WebServiceProvier
                //         which does not support this element?
                webService_EndpointInterface = "";
            }
        }
        return webService_EndpointInterface;
    }

    public String getAnnoWebServiceName() {
        // TODO: Validation: Not allowed on WebServiceProvider

        //TODO: Per JSR109 v1.2 Sec. 5.3.2.1
        //      If not specified then we can use the default value as specified in JSR 181
        //		(but only if it is unique within the module)...or If the name is
        //		not specified in the Service Implementation Bean then fully
        //		qualified name of the Bean class is used to guarantee uniqueness
        //		If the above is not unique then fully qualified name of the
        //		Bean class is used to guarantee uniqueness

        if (webService_Name == null) {
            if (!isProviderBased()) {
                if (getAnnoWebService() != null
                        && !DescriptionUtils.isEmpty(getAnnoWebService().name())) {
                    webService_Name = getAnnoWebService().name();
                } else {
                    if (getServiceDescriptionImpl().isDBCMap()) {
                        //The name is the simple name of the class or interface
                        webService_Name =
                                DescriptionUtils.getSimpleJavaClassName(composite.getClassName());
                    } else {
                        // Default per JSR-181 Sec 4.1, pg 15
                        webService_Name = DescriptionUtils.getSimpleJavaClassName(implOrSEIClass);
                    }
                }
            } else {
                // This element is not valid on a WebServiceProvider annotation
                // REVIEW: Is this a correct thing to return if this is called against a WebServiceProvier
                //         which does not support this element?
                webService_Name = "";
            }
        }
        return webService_Name;
    }

    // ===========================================
    // ANNOTATION: ServiceMode
    // ===========================================
    public ServiceMode getAnnoServiceMode() {

        if (serviceModeAnnotation == null) {
            if (getServiceDescriptionImpl().isDBCMap()) {
                serviceModeAnnotation = composite.getServiceModeAnnot();
            } else {
                if (implOrSEIClass != null) {
                    serviceModeAnnotation =
                            (ServiceMode)implOrSEIClass.getAnnotation(ServiceMode.class);
                }
            }
        }
        return serviceModeAnnotation;
    }

    public Service.Mode getServiceMode() {
        // REVIEW: WSDL/Anno Merge
        return getAnnoServiceModeValue();
    }

    public Service.Mode getAnnoServiceModeValue() {
        // This annotation is only valid on Provider-based endpoints. 
        if (isProviderBased() && serviceModeValue == null) {
            if (getAnnoServiceMode() != null) {
                serviceModeValue = getAnnoServiceMode().value();
            } else {
                serviceModeValue = ServiceMode_DEFAULT;
            }
        }
        return serviceModeValue;
    }

    // ===========================================
    // ANNOTATION: BindingType
    // ===========================================

    public BindingType getAnnoBindingType() {
        if (bindingTypeAnnotation == null) {
            if (getServiceDescriptionImpl().isDBCMap()) {
                bindingTypeAnnotation = composite.getBindingTypeAnnot();
            } else {
                if (implOrSEIClass != null) {
                    bindingTypeAnnotation =
                            (BindingType)implOrSEIClass.getAnnotation(BindingType.class);
                }
            }
        }
        return bindingTypeAnnotation;
    }

    public String getBindingType() {
        // REVIEW: Implement WSDL/Anno merge?
        return getAnnoBindingTypeValue();
    }

    public String getAnnoBindingTypeValue() {
        if (bindingTypeValue == null) {
            if (getAnnoBindingType() != null &&
                    !DescriptionUtils.isEmpty(getAnnoBindingType().value())) {
                bindingTypeValue = getAnnoBindingType().value();
            } else {
                // No BindingType annotation present or value was empty; use default value
                bindingTypeValue = BindingType_DEFAULT;
            }
        }
        return bindingTypeValue;
    }

    // ===========================================
    // ANNOTATION: HandlerChain
    // ===========================================

    /**
     * Returns a schema derived java class containing the the handler configuration filel
     *
     * @return HandlerChainsType This is the top-level element for the Handler configuration file
     */
    public HandlerChainsType getHandlerChain() {
        // TODO: This needs to work for DBC or class
        if (handlerChainsType == null) {
            getAnnoHandlerChainAnnotation();
            if (handlerChainAnnotation != null) {
                String handlerFileName = handlerChainAnnotation.file();

                // TODO RAS & NLS
                if (log.isDebugEnabled()) {
                    log.debug("EndpointDescriptionImpl.getHandlerList: fileName: "
                            + handlerFileName
                            + " className: "
                            + composite.getClassName());
                }

                String className = getServiceDescriptionImpl().isDBCMap() ?
                        composite.getClassName() : implOrSEIClass.getName();

                ClassLoader classLoader = getServiceDescriptionImpl().isDBCMap() ?
                        composite.getClassLoader() : this.getClass().getClassLoader();

                InputStream is = DescriptionUtils.openHandlerConfigStream(
                        handlerFileName,
                        className,
                        classLoader);

                if(is == null) {
                    log.warn("Unable to load handlers from file: " + handlerFileName);                    
                } else {
                    try {
                        // All the classes we need should be part of this package
                        JAXBContext jc = JAXBContext
                                .newInstance("org.apache.axis2.jaxws.description.xml.handler",
                                             this.getClass().getClassLoader());
    
                        Unmarshaller u = jc.createUnmarshaller();
    
                        JAXBElement<?> o = (JAXBElement<?>)u.unmarshal(is);
                        handlerChainsType = (HandlerChainsType)o.getValue();
    
                    } catch (Exception e) {
                        throw ExceptionFactory
                                .makeWebServiceException(
                                        "EndpointDescriptionImpl: getHandlerList: thrown when attempting to unmarshall JAXB content");
                    }
                }
            }
        }
        return handlerChainsType;
    }

    public HandlerChain getAnnoHandlerChainAnnotation() {
        if (this.handlerChainAnnotation == null) {
            if (getServiceDescriptionImpl().isDBCMap()) {
                handlerChainAnnotation = composite.getHandlerChainAnnot();
            } else {
                if (implOrSEIClass != null) {
                    handlerChainAnnotation =
                            (HandlerChain)implOrSEIClass.getAnnotation(HandlerChain.class);
                }
            }
        }

        return handlerChainAnnotation;
    }

    /*
     * Returns a live list describing the handlers on this port.
     * TODO: This is currently returning List<String>, but it should return a HandlerDescritpion
     * object that can represent a handler description from various Metadata (annotation, deployment descriptors, etc);
     * use JAX-WS Appendix B Handler Chain Configuration File Schema as a starting point for HandlerDescription.
     *  
     * @return A List of handlers for this port.  The actual list is returned, and therefore can be modified.
    
    public List<String> getHandlerList() {
        return handlerList;
    }
    */

    private Definition getWSDLDefinition() {
        return ((ServiceDescriptionWSDL)getServiceDescription()).getWSDLDefinition();
    }

    public javax.wsdl.Service getWSDLService() {
        Definition defn = getWSDLDefinition();
        if (defn != null) {
            return defn.getService(getServiceQName());
        } else {
            return null;
        }
    }

    public Port getWSDLPort() {
        javax.wsdl.Service service = getWSDLService();
        if (service != null) {
            return service.getPort(getPortQName().getLocalPart());
        } else {
            return null;
        }
    }

    public Binding getWSDLBinding() {
        Binding wsdlBinding = null;
        Port wsdlPort = getWSDLPort();
        Definition wsdlDef = getWSDLDefinition();
        if (wsdlPort != null && wsdlDef != null) {
            wsdlBinding = wsdlPort.getBinding();
        }
        return wsdlBinding;
    }

    public String getWSDLBindingType() {
        String wsdlBindingType = null;
        Binding wsdlBinding = getWSDLBinding();
        if (wsdlBinding != null) {
            // If a WSDL binding was found, we need to find the proper extensibility
            // element and return the namespace.  The namespace will be different
            // for SOAP 1.1 vs. SOAP 1.2 bindings and HTTP.
            // TODO: What do we do if no extensibility element exists?
            List<ExtensibilityElement> elements = wsdlBinding.getExtensibilityElements();
            Iterator<ExtensibilityElement> itr = elements.iterator();
            while (itr.hasNext()) {
                ExtensibilityElement e = itr.next();
                if (javax.wsdl.extensions.soap.SOAPBinding.class.isAssignableFrom(e.getClass())) {
                    javax.wsdl.extensions.soap.SOAPBinding soapBnd =
                            (javax.wsdl.extensions.soap.SOAPBinding)e;
                    wsdlBindingType = soapBnd.getElementType().getNamespaceURI();
                    break;
                } else if (SOAP12Binding.class.isAssignableFrom(e.getClass())) {
                    SOAP12Binding soapBnd = (SOAP12Binding)e;
                    wsdlBindingType = soapBnd.getElementType().getNamespaceURI();
                    break;
                } else if (HTTPBinding.class.isAssignableFrom(e.getClass())) {
                    HTTPBinding httpBnd = (HTTPBinding)e;
                    wsdlBindingType = httpBnd.getElementType().getNamespaceURI();
                    break;
                }
            }
        }
        return wsdlBindingType;
    }

    public String getName() {
        return getAnnoWebServiceName();
    }

    public String getTargetNamespace() {
        return getAnnoWebServiceTargetNamespace();
    }

    public PortInfo getPortInfo() {
        if (portInfo == null) {
            portInfo = new PortInfoImpl(getServiceQName(), getPortQName(), getBindingType());
        }
        return portInfo;
    }

    public void setClientBindingID(String clientBindingID) {

        if (clientBindingID == null) {
            this.clientBindingID = DEFAULT_CLIENT_BINDING_ID;
        } else if (validateClientBindingID(clientBindingID)) {
            this.clientBindingID = clientBindingID;
        } else {
            throw ExceptionFactory.makeWebServiceException(
                    Messages.getMessage("addPortErr0", getPortQName().toString()));
        }
    }

    private boolean validateClientBindingID(String bindingId) {
        boolean isValid = true;
        if (bindingId != null && !(bindingId.equals(SOAPBinding.SOAP11HTTP_BINDING) ||
                bindingId.equals(javax.xml.ws.http.HTTPBinding.HTTP_BINDING) ||
                bindingId.equals(SOAPBinding.SOAP12HTTP_BINDING) ||
                bindingId.equals(SOAPBinding.SOAP11HTTP_MTOM_BINDING) ||
                bindingId.equals(SOAPBinding.SOAP12HTTP_MTOM_BINDING))) {
            throw ExceptionFactory.makeWebServiceException(
                    Messages.getMessage("addPortErr0", getPortQName().toString()));
        }
        return isValid;
    }

    public String getClientBindingID() {
        if (clientBindingID == null) {
            if (getWSDLDefinition() != null) {
                clientBindingID = getWSDLBindingType();
            } else {
                clientBindingID = DEFAULT_CLIENT_BINDING_ID;
            }
        }
        return clientBindingID;
    }

    public void setEndpointAddress(String endpointAddress) {
        // REVIEW: Should this be called whenever BindingProvider.ENDPOINT_ADDRESS_PROPERTY is set by the client?
        if (!DescriptionUtils.isEmpty(endpointAddress)) {
            this.endpointAddress = endpointAddress;
        } else {
            // Since a port can be added without setting an endpoint address, this is not an error.
            if (log.isDebugEnabled())
                log.debug("A null or empty endpoint address was attempted to be set",
                          new Throwable("Stack Traceback"));
        }
    }

    public String getEndpointAddress() {
        if (endpointAddress == null) {
            // If the endpointAddress has not been set explicitly by a call to setEndpointAddress()
            // then try to get it from the WSDL
            endpointAddress = getWSDLSOAPAddress();
        }
        return endpointAddress;
    }

    /**
     * Return the SOAP Address from the WSDL for this port.
     *
     * @return The SOAP Address from the WSDL for this port or null.
     */
    public String getWSDLSOAPAddress() {
        if (wsdlSOAPAddress == null) {
            Port wsdlPort = getWSDLPort();
            if (wsdlPort != null) {
                // The port is in the WSDL, so see if it has a SOAP address extensibility element specified.
                List extElementList = wsdlPort.getExtensibilityElements();
                for (Object listElement : extElementList) {
                    ExtensibilityElement extElement = (ExtensibilityElement)listElement;
                    if (isSOAPAddressElement(extElement)) {
                        String soapAddress = getSOAPAddressFromElement(extElement);
                        if (!DescriptionUtils.isEmpty(soapAddress)) {
                            wsdlSOAPAddress = soapAddress;
                        }
                    }
                }
            }
        }
        return wsdlSOAPAddress;
    }

    /**
     * Determine if the WSDL Extensibility element corresponds to the SOAP Address element.
     *
     * @param exElement
     * @return
     */
    static boolean isSOAPAddressElement(ExtensibilityElement exElement) {
        boolean isAddress = false;
        if (exElement != null) {
            isAddress = (SOAP_11_ADDRESS_ELEMENT.equals(exElement.getElementType())
                    ||
                    (SOAP_12_ADDRESS_ELEMENT.equals(exElement.getElementType())));
        }
        return isAddress;
    }

    static String getSOAPAddressFromElement(ExtensibilityElement extElement) {
        String returnAddress = null;

        if (extElement != null) {
            if (SOAP_11_ADDRESS_ELEMENT.equals(extElement.getElementType())) {
                returnAddress = ((SOAPAddress)extElement).getLocationURI();
            } else if (SOAP_12_ADDRESS_ELEMENT.equals(extElement.getElementType())) {
                returnAddress = ((SOAP12Address)extElement).getLocationURI();
            }
        }

        return returnAddress;
    }

    /**
     * Selects a port to use in the case where a portQName was not specified by the client on the
     * Service.getPort(Class) call.  If WSDL is present, then an appropriate port is looked for
     * under the service element, and an exception is thrown if none can be found.  If WSDL is not
     * present, then the selected port is simply the one determined by annotations.
     *
     * @return A QName representing the port that is to be used.
     */
    private QName selectPortToUse() {
        QName portToUse = null;
        // If WSDL Service for this port is present, then we'll find an appropriate port defined in there and set 
        // the name accordingly.  If no WSDL is present, the the PortQName getter will use annotations to set the value.
        if (getWSDLService() != null) {
            portToUse = selectWSDLPortToUse();
        } else {
            // No WSDL, so the port to use is the one defined by the annotations.
            portToUse = getPortQName();
        }
        return portToUse;
    }

    /**
     * Look through the WSDL Service for a port that should be used.  If none can be found, then
     * throw an exception.
     *
     * @param wsdlService
     * @return A QName representing the port from the WSDL that should be used.
     */
    private QName selectWSDLPortToUse() {
        QName wsdlPortToUse = null;

        // To select which WSDL Port to use, we do the following
        // 1) Find the subset of all ports under the service that use the PortType represented by the SEI
        // 2) From the subset in (1) find all those ports that specify a SOAP Address
        // 3) Use the first port from (2)
        // REVIEW: Should we be looking at the binding type or something else to determin which subset of ports to use;
        //         i.e. instead of just finding ports that specify a SOAP Address?

        // Per JSR-181, 
        // - The portType name corresponds to the WebService.name annotation value, which is
        //   returned by getName()
        // - The portType namespace corresponds to the WebService.targetNamespace annotation, which
        //   is returned by getTargetNamespace()
        String portTypeLP = getName();
        String portTypeTNS = getTargetNamespace();
        QName portTypeQN = new QName(portTypeTNS, portTypeLP);

        ServiceDescriptionWSDL serviceDescWSDL = (ServiceDescriptionWSDL)getServiceDescription();

        List<Port> wsdlPortsUsingPortType = serviceDescWSDL.getWSDLPortsUsingPortType(portTypeQN);
        List<Port> wsdlPortsUsingSOAPAddresses =
                serviceDescWSDL.getWSDLPortsUsingSOAPAddress(wsdlPortsUsingPortType);
        if (wsdlPortsUsingSOAPAddresses != null && !wsdlPortsUsingSOAPAddresses.isEmpty()) {
            // We return the first port that uses the particluar PortType and has a SOAP address.
            // HOWEVER, that is not necessarily the first one in the WSDL that meets that criteria!  
            // The problem is that WSDL4J Service.getPorts(), which is used to get a Map of ports under the service 
            // DOES NOT return the ports in the order they are defined in the WSDL.  
            // Therefore, we can't necessarily predict which one we'll get back as the "first" one in the collection.
            // REVIEW: Note the above comment; is there anything more predictible and determinstic we can do?
            Port portToUse = (Port)wsdlPortsUsingSOAPAddresses.toArray()[0];
            String portLocalPart = portToUse.getName();
            String portNamespace = serviceDescWSDL.getWSDLService().getQName().getNamespaceURI();
            wsdlPortToUse = new QName(portNamespace, portLocalPart);
        }

        return wsdlPortToUse;
    }

    private WsdlComposite generateWSDL(DescriptionBuilderComposite dbc) {

        WsdlComposite wsdlComposite = null;
        Definition defn = dbc.getWsdlDefinition();
        if (defn == null || !isAxisServiceBuiltFromWSDL) {

            //Invoke the callback for generating the wsdl
            if (dbc.getCustomWsdlGenerator() != null) {
                String implName = null;
                if (axisService == null) {
                    implName = DescriptionUtils.javifyClassName(composite.getClassName());
                } else {
                    implName = (String)axisService.getParameterValue(MDQConstants.SERVICE_CLASS);
                }
                wsdlComposite =
                        dbc.getCustomWsdlGenerator().generateWsdl(implName, getBindingType());

                if (wsdlComposite != null) {
                    wsdlComposite.setWsdlFileName(
                            (this.getAnnoWebServiceServiceName() + ".wsdl").toLowerCase());

                    Definition wsdlDef = wsdlComposite.getRootWsdlDefinition();

                    try {
                        WSDL4JWrapper wsdl4jWrapper = new WSDL4JWrapper(dbc.getWsdlURL(), wsdlDef);
                        getServiceDescriptionImpl().setGeneratedWsdlWrapper(wsdl4jWrapper);
                    } catch (Exception e) {
                        throw ExceptionFactory.makeWebServiceException(
                                "EndpointDescriptionImpl: WSDLException thrown when attempting to instantiate WSDL4JWrapper ");
                    }
                } else {
                    // REVIEW:Determine if we should always throw an exception on this, or at this point
                    //throw ExceptionFactory.makeWebServiceException("EndpointDescriptionImpl: Unable to find custom WSDL generator");
                    if (log.isDebugEnabled()) {
                        log.debug(
                                "The custom WSDL generator returned null, so no generated WSDL is available");
                    }

                }
            } else {
                // REVIEW: This used to throw an exception, but it seems we shouldn't require
                // a wsdl generator be provided.
//                throw ExceptionFactory.makeWebServiceException("EndpointDescriptionImpl: Unable to find custom WSDL generator");
                if (log.isDebugEnabled()) {
                    log.debug(
                            "No custom WSDL generator was supplied, so WSDL can not be generated");
                }
            }
        }
        return wsdlComposite;
    }

    /**
     * Return the class for this name
     *
     * @return Class
     */
    private static Class forName(final String className, final boolean initialize,
                                 final ClassLoader classloader) throws ClassNotFoundException {
        // NOTE: This method must remain protected because it uses AccessController
        Class cl = null;
        try {
            cl = (Class)AccessController.doPrivileged(
                    new PrivilegedExceptionAction() {
                        public Object run() throws ClassNotFoundException {
                            return Class.forName(className, initialize, classloader);
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
            throw (RuntimeException)e.getException();
        }

        return cl;
    }

    public String toString() {
        final String newline = "\n";
        final String sameline = "; ";
        StringBuffer string = new StringBuffer();
        try {
            string.append(super.toString());
            string.append(newline);
            string.append("Name: " + getName());
            string.append(sameline);
            string.append("Endpoint Address: " + getEndpointAddress());
            //
            string.append(newline);
            string.append("ServiceQName: " + getServiceQName());
            string.append(sameline);
            string.append("PortQName: " + getPortQName());
            string.append(sameline);
            string.append("TargetNamespace: " + getTargetNamespace());
            //
            string.append(newline);
            string.append("Service Mode: " + getServiceMode());
            string.append(sameline);
            string.append("Binding Type: " + getBindingType());
            string.append(sameline);
            string.append("Client Binding Type: " + getClientBindingID());
            //
            string.append(newline);
            string.append("Is provider-based: " + (isProviderBased() == true));
            string.append(sameline);
            string.append("Is proxy-based: " + (isEndpointBased() == true));
            string.append(sameline);
            string.append("Is WSDL fully specified: " + (isWSDLFullySpecified() == true));
            //
            string.append(newline);
            string.append("AxisService: " + getAxisService());
            //
            string.append(newline);
            EndpointInterfaceDescription endpointInterfaceDesc = getEndpointInterfaceDescription();
            if (endpointInterfaceDesc != null) {
                string.append("EndpointInterfaceDescription: " + endpointInterfaceDesc.toString());
            } else {
                string.append("EndpointInterfaceDescription is null.");
            }
        }
        catch (Throwable t) {
            string.append(newline);
            string.append("Complete debug information not currently available for " +
                    "EndpointDescription");
            return string.toString();
        }
        return string.toString();
    }
}


