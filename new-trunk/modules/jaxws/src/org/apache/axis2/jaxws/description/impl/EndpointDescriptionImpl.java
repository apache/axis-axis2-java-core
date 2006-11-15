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


package org.apache.axis2.jaxws.description.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.jws.WebService;
import javax.wsdl.Binding;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingType;
import javax.xml.ws.Service;
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
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.EndpointDescriptionJava;
import org.apache.axis2.jaxws.description.EndpointDescriptionWSDL;
import org.apache.axis2.jaxws.description.EndpointInterfaceDescription;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.description.ServiceDescriptionWSDL;
import org.apache.axis2.jaxws.description.builder.DescriptionBuilderComposite;
import org.apache.axis2.jaxws.description.builder.MDQConstants;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.util.WSDL4JWrapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
/**
 * @see ../EndpointDescription
 *
 */
/*
 * TODO: EndpointDescription should be created via AxisService objects and not directly from WSDL
 * IMPORTANT NOTE: Axis2 currently only supports 1 service and 1 port under that service.  When that is
 * fixed, that will probably have an impact on this class.  In particular, I think this should be created 
 * somehow from an AxisService/AxisPort combination, and not directly from the WSDL.
 */
class EndpointDescriptionImpl implements EndpointDescription, EndpointDescriptionJava, EndpointDescriptionWSDL {
    
    private ServiceDescriptionImpl parentServiceDescription;
    private AxisService axisService;

    private QName portQName;

    // Corresponds to a port that was added dynamically via addPort and is not declared (either in WSDL or annotations)
    private boolean isDynamicPort;
    
    // If the WSDL is fully specified, we could build the AxisService from the WSDL
    private boolean isAxisServiceBuiltFromWSDL;
    
    private String serviceImplName;	//class name of the service impl or SEI
    
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
    Set<Package> packages = null;
    
    private static final Log log = LogFactory.getLog(EndpointDescriptionImpl.class);

    
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
    // ANNOTATION: @WebService
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
    EndpointDescriptionImpl(Class theClass, QName portName, ServiceDescriptionImpl parent) {
        this(theClass, portName, false, parent);
    }
    EndpointDescriptionImpl(Class theClass, QName portName, boolean dynamicPort, ServiceDescriptionImpl parent) {
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
            getServiceDescriptionImpl().getClientConfigurationFactory().completeAxis2Configuration(axisService);
        } catch (DeploymentException e) {
            // TODO RAS
            // TODO NLS
            if (log.isDebugEnabled()) {
                log.debug("Caught exception in ServiceDescription.ServiceDescription: " + e);
                log.debug("Exception:", e);
            }
//            throw ExceptionFactory.makeWebServiceException("ServiceDescription caught " + e);
        } catch (Exception e) {
            // TODO RAS
            // TODO NLS
            if (log.isDebugEnabled()) {
                log.debug("Caught exception in ServiceDescription.ServiceDescription: " + e);
                log.debug("Exception:", e);
            }
//            throw ExceptionFactory.makeWebServiceException("ServiceDescription caught " + e);
        }
    }
    
    /**
     * Create an EndpointDescription based on the DescriptionBuilderComposite.  
     * Note that per the JAX-WS Spec (Final Release, 4/19/2006
     * Section 4.2.3 Proxies, page 55)the "namespace component of the port is the target namespace of the WSDL 
     * definition document".
     * 
     * @param theClass The SEI or Impl class.  This will be NULL for Dispatch clients
     *                 since they don't use an SEI
     */
    EndpointDescriptionImpl(ServiceDescriptionImpl parent, String serviceImplName) {
    	
    	// TODO: This and the other constructor will (eventually) take the same args, so the logic needs to be combined
        // TODO: If there is WSDL, could compare the namespace of the defn against the portQName.namespace
        this.parentServiceDescription = parent;
        this.serviceImplName = serviceImplName;
        this.implOrSEIClass = null;

 		composite = getServiceDescriptionImpl().getDescriptionBuilderComposite();
		if (composite == null){
            throw ExceptionFactory.makeWebServiceException("EndpointDescription.EndpointDescription: parents DBC is null");
		}

        //Set the base level of annotation that we are processing...currently
        // a 'WebService' or a 'WebServiceProvider'
        if (composite.getWebServiceAnnot() != null)
        	webServiceAnnotation = composite.getWebServiceAnnot();
        else
        	webServiceProviderAnnotation = composite.getWebServiceProviderAnnot();
        
        // REVIEW: Maybe this should be an error if the name has already been set and it doesn't match
        getServiceDescriptionImpl().setServiceQName(getServiceQName());
        //Call the getter to insure the qualified port name is set. 
        getPortQName();
		
        // TODO: Refactor this with the consideration of no WSDL/Generic Service/Annotated SEI
        setupAxisServiceFromDBL();
        addToAxisService();	//Add a reference to this EndpointDescription to the AxisService

        //TODO: Need to remove operations from AxisService that have 'exclude = true
        //      then call 'validateOperations' to verify that WSDL and AxisService match up,
        //      Remember that this will only happen when we generate an AxisService from existing
        //		WSDL and then need to perform further processing because we have annotations as well
        //		If there is no WSDL, we would never process the Method to begin with.
        
        buildDescriptionHierachy();
        
        //Invoke the callback for generating the wsdl
        if (composite.getCustomWsdlGenerator() != null) {
        	Definition wsdlDef = 
        		composite.getCustomWsdlGenerator().generateWsdl((Class)axisService.getParameterValue(MDQConstants.SERVICE_CLASS));
   					
        	try {
    			WSDL4JWrapper wsdl4jWrapper = new WSDL4JWrapper(composite.getWsdlURL(), wsdlDef);
    			getServiceDescriptionImpl().setGeneratedWsdlWrapper(wsdl4jWrapper);
            } catch (Exception e) {
                throw ExceptionFactory.makeWebServiceException("EndpointDescriptionImpl: WSDLException thrown when attempting to instantiate WSDL4JWrapper ");
            }
        }

        //Save the WSDL Definition
        // REVIEW: This could be a PARTIAL WSDL; not sure if that will cause trouble later on.
        //			Maybe we should always be setting it to generated WSDL
        Parameter wsdlDefParameter = new Parameter();
        wsdlDefParameter.setName(MDQConstants.WSDL_DEFINITION);       
        
        if (getServiceDescriptionImpl().getWSDLWrapper() != null) {
            wsdlDefParameter.setValue(getServiceDescriptionImpl().getWSDLWrapper().getDefinition());
        } else {
        	if (getServiceDescriptionImpl().getGeneratedWsdlWrapper() != null) {
        		wsdlDefParameter.setValue(getServiceDescriptionImpl().getGeneratedWsdlWrapper().getDefinition());
        	} else {
        		//TODO: Hmmm, this should probably be an exception, will probably always need to set wsdl	
        	}
        }
        
        //Save the WSDL Location
        //REVIEW: hmm, this won't always be set
        Parameter wsdlLocationParameter = new Parameter();
        wsdlLocationParameter.setName(MDQConstants.WSDL_LOCATION);
        wsdlLocationParameter.setValue(getAnnoWebServiceWSDLLocation());

        try {
        	axisService.addParameter(wsdlDefParameter);
        	axisService.addParameter(wsdlLocationParameter);                        
        } catch (Exception e) {
        	throw ExceptionFactory.makeWebServiceException("EndpointDescription: Unable to add parms. to AxisService");
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
    EndpointDescriptionImpl(Class theClass, QName portName, AxisService axisService, ServiceDescriptionImpl parent) {
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
    		
    		webServiceAnnotation = (WebService) implOrSEIClass.getAnnotation(WebService.class);
    		webServiceProviderAnnotation = (WebServiceProvider) implOrSEIClass.getAnnotation(WebServiceProvider.class);
    				
    		if (webServiceAnnotation == null && webServiceProviderAnnotation == null)
    			// TODO: NLS
    			throw ExceptionFactory.makeWebServiceException("Either WebService or WebServiceProvider annotation must be present on " + implOrSEIClass);
    		else if (webServiceAnnotation != null && webServiceProviderAnnotation != null)
    			// TODO: NLS
    			throw ExceptionFactory.makeWebServiceException("Both WebService or WebServiceProvider annotations cannot be presenton " + implOrSEIClass);
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
    		
    		if (!getServiceDescriptionImpl().isDBCMap()){
    			Class seiClass = null;
    			if (DescriptionUtils.isEmpty(seiClassName)) {
    				// For now, just build the EndpointInterfaceDesc based on the class itself.
    				// TODO: The EID ctor doesn't correctly handle anything but an SEI at this 
    				//       point; e.g. it doesn't publish the correct methods of just an impl.
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
    			endpointInterfaceDescription = new EndpointInterfaceDescriptionImpl(seiClass, this);
    		} else {
    			//TODO: Determine if we need logic here to determine implied SEI or not. This logic
    			//		may be handled by EndpointInterfaceDescription
    			
    			if (DescriptionUtils.isEmpty(getAnnoWebServiceEndpointInterface())) {
    			    				
    				//TODO: Build the EndpointInterfaceDesc based on the class itself
    				endpointInterfaceDescription = new EndpointInterfaceDescriptionImpl(composite, true, this);
    				
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
        // TODO: (JLB) Implement WSDL/Annotation merge? May be OK as is; not sure how would know WHICH port Qname to get out of the WSDL if 
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
        // REVIEW: Does this need to be cached in an instance variable like the others?
        String localPart = getAnnoWebServiceServiceName();
        String tns = getAnnoWebServiceTargetNamespace();
        return new QName(tns, localPart);
    }
    
    public ServiceDescription getServiceDescription() {
        return parentServiceDescription;
    }
    
    ServiceDescriptionImpl getServiceDescriptionImpl() {
        return (ServiceDescriptionImpl) parentServiceDescription;
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
            throw ExceptionFactory.makeWebServiceException("Can not update an SEI on a dynamic port.  PortQName:" + portQName);
        }
        if (sei == null) {
            // TODO: RAS and NLS
            throw ExceptionFactory.makeWebServiceException("EndpointDescription.updateWithSEI was passed a null SEI.  PortQName:" + portQName);
        }

        if (endpointInterfaceDescription != null) {
            // The EndpointInterfaceDescription was created previously based on the port declaration (i.e. WSDL)
            // so update that with information from the SEI annotations
            ((EndpointInterfaceDescriptionImpl) endpointInterfaceDescription).updateWithSEI(sei);
        }
        else {
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
        }
        else {
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
        serviceClassNameParameter.setValue(DescriptionUtils.javifyClassName(composite.getClassName()));
           
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
    			new WSDL11ToAxisServiceBuilder( getServiceDescriptionImpl().getWSDLWrapper().getDefinition(), 
    					getServiceDescription().getServiceQName(), 
    					getPortQName().getLocalPart());
    		
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
    		// TODO We should not swallow a fault here.
    		log.warn(Messages.getMessage("warnAxisFault", e.toString()));
            isBuiltFromWSDL = false;
    	}
        return isBuiltFromWSDL;
    }
    
    private void buildAxisServiceFromAnnotations() {
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
    		}
    		else if (implOrSEIClass != null){
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
                    endpointInterfaceDescription = new EndpointInterfaceDescriptionImpl(this);
 
                    // Update the EndpointInterfaceDescription created with WSDL with information from the
                    // annotations in the SEI
                    ((EndpointInterfaceDescriptionImpl) endpointInterfaceDescription).updateWithSEI(implOrSEIClass);
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
 
    //This should eventually be deprecated in favor 'createAxisServiceNameFromDBL
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
            if (getAnnoWebService() != null 
                    && !DescriptionUtils.isEmpty(getAnnoWebService().wsdlLocation())) {
                annotation_WsdlLocation = getAnnoWebService().wsdlLocation();
            }
            else if (getAnnoWebServiceProvider() != null 
                    && !DescriptionUtils.isEmpty(getAnnoWebServiceProvider().wsdlLocation())) {
                annotation_WsdlLocation = getAnnoWebServiceProvider().wsdlLocation();
            }
            else {
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
            }
            else if (getAnnoWebServiceProvider() != null 
                    && !DescriptionUtils.isEmpty(getAnnoWebServiceProvider().serviceName())) {
                annotation_ServiceName = getAnnoWebServiceProvider().serviceName();
            }
            else {
                // Default value is the "simple name" of the class or interface + "Service"
                // Per JSR-181 MR Sec 4.1, pg 15
            	if (getServiceDescriptionImpl().isDBCMap()) {
                	annotation_ServiceName = getSimpleJavaClassName(composite.getClassName()) + "Service";
            	} else {
                    annotation_ServiceName = getSimpleJavaClassName(implOrSEIClass) + "Service";
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
            }
            else if (getAnnoWebServiceProvider() != null
                    && !DescriptionUtils.isEmpty(getAnnoWebServiceProvider().portName())) {
                annotation_PortName = getAnnoWebServiceProvider().portName();
            }
            else {
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
                 			getSimpleJavaClassName(composite.getClassName()) + "Port"
                 			: getSimpleJavaClassName(implOrSEIClass) + "Port";
               }
                else {
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
            }
            else if (getAnnoWebServiceProvider() != null
                    && !DescriptionUtils.isEmpty(getAnnoWebServiceProvider().targetNamespace())) {
                annotation_TargetNamespace = getAnnoWebServiceProvider().targetNamespace();
            }
            else {
                // Default value per JSR-181 MR Sec 4.1 pg 15 defers to "Implementation defined, 
                // as described in JAX-WS 2.0, section 3.2" which is JAX-WS 2.0 Sec 3.2, pg 29.
                // FIXME: Hardcoded protocol for namespace
            	if (getServiceDescriptionImpl().isDBCMap())
            		annotation_TargetNamespace = 
            			makeNamespaceFromPackageName(getJavaPackageName(composite.getClassName()), "http");
            	else
            		annotation_TargetNamespace = 
            			makeNamespaceFromPackageName(getJavaPackageName(implOrSEIClass), "http");

            }
        }
        return annotation_TargetNamespace;
    }
    
    
    /**
     * Return the name of the class without any package qualifier.
     * This method should be DEPRECATED when DBC support is complete
     * @param theClass
     * @return the name of the class sans package qualification.
     */
    private static String getSimpleJavaClassName(Class theClass) {
        String returnName = null;
        if (theClass != null) {
            String fqName = theClass.getName();
            // We need the "simple name", so strip off any package information from the name
            int endOfPackageIndex = fqName.lastIndexOf('.');
            int startOfClassIndex = endOfPackageIndex + 1;
            returnName = fqName.substring(startOfClassIndex);
        }
        else {
            // TODO: RAS and NLS
            throw new UnsupportedOperationException("Java class is null");
        }
        return returnName;
    }
    
    /**
     * Return the name of the class without any package qualifier.
     * @param theClass
     * @return the name of the class sans package qualification.
     */
    private static String getSimpleJavaClassName(String name) {
        String returnName = null;
        
        if (name != null) {
            String fqName = name;
            
            // We need the "simple name", so strip off any package information from the name
            int endOfPackageIndex = fqName.lastIndexOf('.');
            int startOfClassIndex = endOfPackageIndex + 1;
            returnName = fqName.substring(startOfClassIndex);
        }
        else {
            // TODO: RAS and NLS
            throw new UnsupportedOperationException("Java class is null");
        }
        return returnName;
    }
    
    /**
     * Returns the package name from the class.  If no package, then returns null
     * This method should be DEPRECATED when DBC support is complete
     * @param theClass
     * @return
     */
    private static String getJavaPackageName(Class theClass) {
        String returnPackage = null;
        if (theClass != null) {
            String fqName = theClass.getName();
            // Get the package name, if there is one
            int endOfPackageIndex = fqName.lastIndexOf('.');
            if (endOfPackageIndex >= 0) {
                returnPackage = fqName.substring(0, endOfPackageIndex);
            }
        }
        else {
            // TODO: RAS and NLS
            throw new UnsupportedOperationException("Java class is null");
        }
        return returnPackage;
    }
    
    /**
     * Returns the package name from the class.  If no package, then returns null
     * @param theClassName
     * @return
     */
    private static String getJavaPackageName(String theClassName) {
        String returnPackage = null;
        if (theClassName != null) {
            String fqName = theClassName;
            // Get the package name, if there is one
            int endOfPackageIndex = fqName.lastIndexOf('.');
            if (endOfPackageIndex >= 0) {
                returnPackage = fqName.substring(0, endOfPackageIndex);
            }
        }
        else {
            // TODO: RAS and NLS
            throw new UnsupportedOperationException("Java class is null");
        }
        return returnPackage;
    }
    
    /**
     * Create a JAX-WS namespace based on the package name
     * @param packageName
     * @param protocol
     * @return
     */
    private static final String NO_PACKAGE_HOST_NAME = "DefaultNamespace";

    private static String makeNamespaceFromPackageName(String packageName, String protocol) {
        if (DescriptionUtils.isEmpty(protocol)) {
            protocol = "http";
        }
        if (DescriptionUtils.isEmpty(packageName)) {
            return protocol + "://" + NO_PACKAGE_HOST_NAME;
        }
        StringTokenizer st = new StringTokenizer( packageName, "." );
        String[] words = new String[ st.countTokens() ];
        for(int i = 0; i < words.length; ++i)
            words[i] = st.nextToken();

        StringBuffer sb = new StringBuffer(80);
        for(int i = words.length-1; i >= 0; --i) {
            String word = words[i];
            // seperate with dot
            if( i != words.length-1 )
                sb.append('.');
            sb.append( word );
        }
        return protocol + "://" + sb.toString() + "/";
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
            }
            else {
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
     			}
    			else {
    				if (getServiceDescriptionImpl().isDBCMap()) {
    					//The name is the simple name of the class or interface
    					webService_Name = getSimpleJavaClassName(composite.getClassName());
    				} else {
    					// Default per JSR-181 Sec 4.1, pg 15
    					webService_Name = getSimpleJavaClassName(implOrSEIClass);
    				}
    			}                	
    		}
    		else {
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
        			serviceModeAnnotation = (ServiceMode) implOrSEIClass.getAnnotation(ServiceMode.class);
        		}
        	}
        }
        return serviceModeAnnotation;
    }
    
    public Service.Mode getServiceMode() {
        // TODO: (JLB) WSDL/Anno Merge
        return getAnnoServiceModeValue();
    }
    
    public Service.Mode getAnnoServiceModeValue() {
        // This annotation is only valid on Provider-based endpoints. 
        if (isProviderBased() && serviceModeValue == null) {
            if (getAnnoServiceMode() != null) {
                serviceModeValue = getAnnoServiceMode().value();
            }
            else {
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
                    bindingTypeAnnotation = (BindingType) implOrSEIClass.getAnnotation(BindingType.class);
        		}
        	}
        }
        return bindingTypeAnnotation;
    }
    
    public String getBindingType() {
        // TODO: (JLB) Implement WSDL/Anno merge
        return getAnnoBindingTypeValue();
    }
    
    public String getAnnoBindingTypeValue() {
        if (bindingTypeValue == null) {
            if (getAnnoBindingType() != null) {
                bindingTypeValue = getAnnoBindingType().value();
            }
            else {
                // No BindingType annotation present; use default value
                bindingTypeValue = BindingType_DEFAULT;
            }
        }
        return bindingTypeValue;
    }

    // ===========================================
    // ANNOTATION: HandlerChain
    // ===========================================

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
    private Definition getWSDLDefinition() {
        return ((ServiceDescriptionWSDL) getServiceDescription()).getWSDLDefinition();
    }
    public javax.wsdl.Service getWSDLService() {
        Definition defn = getWSDLDefinition();
        if (defn != null) {
            return defn.getService(getServiceQName());
        }
        else {
            return null;
        }
    }
    public Port getWSDLPort() {
        javax.wsdl.Service service = getWSDLService();
        if (service != null) {
            return service.getPort(getPortQName().getLocalPart());
        }
        else {
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
            wsdlBindingType = wsdlBinding.getQName().getNamespaceURI();
        }
        return wsdlBindingType;
    }
    public String getName() {
        return getAnnoWebServiceName();
    }
    public String getTargetNamespace() {
        return getAnnoWebServiceTargetNamespace();
    }
    /**
     * Returns the packages that are needed to marshal/unmarshal the 
     * data objects.  Example: this set of packages is used to construct a 
     * JAXBContext.
     * @return Set<Package>
     */
    public Set<Package> getPackages() {
        // @REVIEW Currently the package set is stored on the
        // EndpointDescription.  We may consider moving this to 
        // ServiceDescription. 
        
        // The set of packages is calcuated once and saved
        if (packages == null) {
            synchronized(this) {
                // @TODO There are two ways to get the packages.
                // Schema Walk (prefered) and Annotation Walk.
                // The Schema walk requires an existing or generated schema.
                // For now, we will force the use of annotation walk
                // @See PackageSetBuilder for details
                boolean useSchemaWalk = false;
                if (useSchemaWalk) {
                    packages = PackageSetBuilder.getPackagesFromSchema(this.getServiceDescription());
                } else {
                    packages = PackageSetBuilder.getPackagesFromAnnotations(this);
                }
            }
        }
        return packages;
    }
}


