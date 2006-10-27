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
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;
import javax.jws.WebService;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.xml.namespace.QName;

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
import org.apache.axis2.jaxws.description.builder.DescriptionBuilderComposite;
import org.apache.axis2.jaxws.description.builder.MethodDescriptionComposite;
import org.apache.axis2.jaxws.description.builder.WebMethodAnnot;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.util.WSDL4JWrapper;
import org.apache.axis2.jaxws.util.WSDLWrapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A ServiceDescription corresponds to a Service under which there can be a
 * collection of enpdoints. In WSDL 1.1 terms, then, a ServiceDescription
 * corresponds to a wsdl:Service under which there are one or more wsdl:Port
 * entries. The ServiceDescription is the root of the metdata abstraction
 * Description hierachy.
 * 
 * The Description hierachy is:
 * <pre>
 * ServiceDescription
 *     EndpointDescription[]
 *         EndpointInterfaceDescription
 *             OperationDescription[]
 *                 ParameterDescription[]
 *                 FaultDescription[]       (Note: Not implemented yet)
 *
 * <b>ServiceDescription details</b>
 * 
 *     CORRESPONDS TO:      
 *         On the Client: The JAX-WS Service class or generated subclass.
 *         
 *         On the Server: The Service implementation.  Note that there is a 1..1 
 *         correspondence between a ServiceDescription and EndpointDescription 
 *         on the server side.
 *        
 *     AXIS2 DELEGATE:      None
 *     
 *     CHILDREN:            1..n EndpointDescription
 *     
 *     ANNOTATIONS:
 *         None
 *     
 *     WSDL ELEMENTS:
 *         service
 *         
 *  </pre>       
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

    private HashMap<String, DescriptionBuilderComposite> dbcMap = null;
    
    private DescriptionBuilderComposite	composite = null;
    private boolean isServerSide = false;
    
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

    /**
     * This is (currently) the service-provider-side-only constructor.
     * Create a service Description based on a service implementation class
     * 
     * @param serviceImplClass
     */
    ServiceDescription(	
    		HashMap<String, DescriptionBuilderComposite> dbcMap,
    		DescriptionBuilderComposite composite ) {
    	this.composite = composite;
    	
    	String serviceImplName = this.composite.getClassName();
    	
    	this.dbcMap = dbcMap;
//TODO: How to we get this when called from server side, create here for now
    	this.isServerSide = true;
	
    	//capture the WSDL, if there is any...to be used for later processing
    	setupWsdlDefinition();
    	
		// Do a first pass validation for this DescriptionBuilderComposite.
    	// This is not intended to be a full integrity check, but rather a fail-fast mechanism
    	validateDBCLIntegrity();
    	
        // The ServiceQName instance variable is set based on annotation or default
        //TODO: When we get this, need to consider verifying service name between WSDL
        //      and annotations, so
    	String targetNamespace;
    	String serviceName;
    	if(this.composite.getWebServiceAnnot() != null) {
    		targetNamespace = this.composite.getWebServiceAnnot().targetNamespace();
    		serviceName = this.composite.getWebServiceAnnot().serviceName();
    	}
    	else {
    		targetNamespace = this.composite.getWebServiceProviderAnnot().targetNamespace();
    		serviceName = this.composite.getWebServiceProviderAnnot().serviceName();
    	}
		this.serviceQName = new QName(targetNamespace, serviceName);

        // Create the EndpointDescription hierachy from the service impl annotations; Since the PortQName is null, 
        // it will be set to the annotation value.
        //EndpointDescription endpointDescription = new EndpointDescription(null, this, serviceImplName);
        EndpointDescription endpointDescription = new EndpointDescription(this, serviceImplName);
        addEndpointDescription(endpointDescription);       
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
    
    public DescriptionBuilderComposite getDescriptionBuilderComposite() {
    	return composite;
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
    
    /*
     * @return True - if we are processing with the DBC List instead of reflection
     */
    public boolean isDBCMap() {
    	if (dbcMap == null)
    		return false;
    	else
    		return true;
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
    	
    	if (isDBCMap()) {

    		if (composite.getWsdlDefinition() != null) {
    			this.wsdlURL = composite.getWsdlURL();
                
    			try {
                    this.wsdlWrapper = new WSDL4JWrapper(this.wsdlURL, 
                    				composite.getWsdlDefinition());

                } catch (WSDLException e) {
                    throw ExceptionFactory.makeWebServiceException(Messages.getMessage("wsdlException", e.getMessage()), e);
                }
    		}
        //Deprecate this code block when MDQ is fully integrated
    	} else if (wsdlURL != null) {
            try {
                this.wsdlWrapper = new WSDL4JWrapper(this.wsdlURL);
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
    	//It is assumed that this will always be set in the constructor rather than 
    	//built up from the class or DBC 
        return serviceQName;
    }


    public boolean isServerSide() {
 		return isServerSide;
    } 

    public HashMap<String, DescriptionBuilderComposite> getDBCMap() {
    	return dbcMap;
    }
    
	private void validateDBCLIntegrity(){
		
		//First, check the integrity of this input composite
		//and retrieve
		//the composite that represents this impl
		
//TODO: Currently, we are calling this method on the DBC. However, the DBC
//will eventually need access to to the whole DBC map to do proper validation.
//We don't want to pass the map of DBC's back into a single DBC.
//So, for starters, this method and all the privates that it calls should be 
// moved to here. At some point, we should consider using a new class that we
//can implement scenarios of, like validateServiceImpl implements validator
		
		try {
			validateIntegrity();
		}
		catch (Exception ex) {
			//com.ibm.ws.ffdc.FFDCFilter.processException(ex, "org.apache.axis2.jaxws.description.ServiceDescription", "329", this);				
			//Tr.error(_tc, msg, inserts);
		}
	}

	/*
	 * Validates the integrity of an impl. class. This should not be called directly for an SEI composite
	 */
	public void validateIntegrity() {
		//TODO: Consider moving this to a utils area, do we really want a public
		//      method that checks integrity...possibly
		
		//In General, this integrity checker should do gross level checking
		//It should not be setting spec-defined default values, but can look
		//at things like empty strings or null values
		
		//TODO: This method will validate the integrity of this object. Basically, if 
		//consumer set this up improperly, then we should fail fast, should consider placing
		//this method in a utils class within the 'description' package
		
		
		//Verify that WebService and WebServiceProvider are not both specified
		//per JAXWS - Sec. 7.7
		if (composite.getWebServiceAnnot() != null && composite.getWebServiceProviderAnnot() != null) {
			throw ExceptionFactory.makeWebServiceException("DescriptionBuilderComposite: WebService annotation and WebServiceProvider annotation cannot coexist");
		}
		
//		Make sure that we're only validating against WSDL, if there is WSDL...duh
		if (composite.getWebServiceProviderAnnot() != null ) {
			// TODO: Verify that this Provider based WebService has a method named invoke
			
		} else if (composite.getWebServiceAnnot() != null) {
			
			if ( composite.getServiceModeAnnot() != null) {
				throw ExceptionFactory.makeWebServiceException("DescriptionBuilderComposite: ServiceMode annotation can only be specified for WebServiceProvider");
			}
			
			//TODO: hmmm, will we ever actually validate an interface directly...don't think so
			if (!composite.isInterface()) {
				// TODO: Validate on the class that this.classModifiers Array does not contain the strings
				//        FINAL or ABSTRACT, but does contain PUBLIC
				// TODO: Validate on the class that a public constructor exists
				// TODO: Validate on the class that a finalize() method does not exist
				if (!DescriptionUtils.isEmpty(composite.getWebServiceAnnot().wsdlLocation())) {
					if (composite.getWsdlDefinition() == null || composite.getWsdlURL() == null) {
						throw ExceptionFactory.makeWebServiceException("DescriptionBuilderComposite: cannot find WSDL Definition pertaining to this WebService annotation");
					}
				}
				
				//		setWebServiceAnnotDefaults(true=impl); Must happen before we start checking annot
				if (!DescriptionUtils.isEmpty(composite.getWebServiceAnnot().endpointInterface())) {
					
					DescriptionBuilderComposite seic = 
						dbcMap.get(composite.getWebServiceAnnot().endpointInterface());
					
					//Verify that we can find the SEI in the composite list
					if (seic == null){
						throw ExceptionFactory.makeWebServiceException("DescriptionBuilderComposite: cannot find SEI composite specified by the endpoint interface");
					}
					
					//Verify that the only class annotations are WebService and HandlerChain
					//(per JSR181 Sec. 3.1)
					if ( composite.getBindingTypeAnnot()!= null 
							|| composite.getSoapBindingAnnot() != null
							|| composite.getWebFaultAnnot() != null
							|| composite.getWebServiceClientAnnot() != null
							|| composite.getWebServiceContextAnnot()!= null
							|| composite.getAllWebServiceRefAnnots() != null
					) {
						throw ExceptionFactory.makeWebServiceException("DescriptionBuilderComposite: invalid annotations specified when WebService annotation specifies an endpoint interface");
					}
					
					//Verify that WebService annotation does not contain a name attribute
					//(per JSR181 Sec. 3.1)
					if (composite.getWebServiceAnnot().name() != null) {
						throw ExceptionFactory.makeWebServiceException("DescriptionBuilderComposite: invalid annotations specified when WebService annotation specifies an endpoint interface");
					}
					
					//Verify that that this implementation class implements all methods in the interface
					validateImplementation(seic);
					
					//Verify that this impl. class does not contain any @WebMethod annotations
					if (webMethodAnnotationsExist()) {
						throw ExceptionFactory.makeWebServiceException("DescriptionBuilderComposite: WebMethod annotations cannot exist on class when WebService.endpointInterface is set");	
					}
					
					//TODO: validateSEI() ...this should really be done here. No sense in validating
					//		an interface just because its in the list
					validateSEI(seic);
					
				} else { //this is an implicit SEI (i.e. impl w/out endpointInterface
					
					checkImplicitSEIAgainstWSDL();
					//	TODO:	Call ValidateWebMethodAnnots()
					//			- this method will check that all methods are public - ???
					//
				}
			} else { //this is an interface...we should not be processing interfaces here
				throw ExceptionFactory.makeWebServiceException("ValidateIntegrity: Improper usage: cannot invoke this method with an interface");	
			}
					
			//TODO: don't think this is necessary
			checkMethodsAgainstWSDL();
		}
	}
	
	private void validateImplementation(DescriptionBuilderComposite seic) {
		/*
		 *	Verify that an impl class implements all the methods of the SEI. We
		 *  have to verify this because an impl class is not required to actually use
		 *  the 'implements' clause. So, if it doesn't, the Java compiler won't 
		 *	catch it. Don't need to worry about chaining because only one EndpointInterface
		 *  can be specified, and the SEI cannot specify an EndpointInterface, so the Java
		 *	compiler will take care of everything else.
		 */
		
		HashMap compositeHashMap = new HashMap();
		Iterator<MethodDescriptionComposite> compIterator = 
					composite.getMethodDescriptionsList().iterator();

		while (compIterator.hasNext()) {
			MethodDescriptionComposite mdc = compIterator.next();
			compositeHashMap.put(mdc.getMethodName(),mdc);
		}
		
		Iterator<MethodDescriptionComposite> seiIterator = 
					seic.getMethodDescriptionsList().iterator();
		
		while (seiIterator.hasNext()) {
			MethodDescriptionComposite mdc = seiIterator.next();

			if (compositeHashMap.get(mdc.getMethodName()) == null) {
				throw ExceptionFactory.makeWebServiceException("ServiceDescription: subclass does not implement method on specified interface");				
			}
		}
		
	}
	
	/*
	 * This method verifies that, if there are any WebMethod with exclude == false, then
	 * make sure that we find all of those methods represented in the wsdl. However, if 
	 * there are no exclusions == false, or there are no WebMethod annotations, then verify
	 * that all the public methods are in the wsdl
	 */
	private void checkMethodsAgainstWSDL() {	
//Verify that, for ImplicitSEI, that all methods that should exist(if one false found, then
//only look for WebMethods w/ False, else take all public methods but ignore those with
//exclude == true
		if (webMethodAnnotationsExist()) {
			if (DescriptionUtils.falseExclusionsExist(composite))
				verifyFalseExclusionsWithWSDL();
			else
				verifyPublicMethodsWithWSDL();
		} else {
			verifyPublicMethodsWithWSDL();
		}
	}
	
	private void checkImplicitSEIAgainstWSDL() {
		
		//TODO: If there is a WSDL, then verify that all WebMethods on this class and in the
		//		superclasses chain are represented in the WSDL...Look at logic below to make
		//		sure this really happening
		
		
		if (webMethodAnnotationsExist()) {
			if (DescriptionUtils.falseExclusionsExist(composite))
				verifyFalseExclusionsWithWSDL();
			else
				verifyPublicMethodsWithWSDL();
		} else {
			verifyPublicMethodsWithWSDL();
		}
		
	}
	
	private void checkSEIAgainstWSDL() {
		//TODO: Place logic here to verify that each publicMethod with WebMethod annot
		//      is contained in the WSDL (If there is a WSDL) If we find
		//	    a WebMethod annotation, use its values for looking in the WSDL

	}
	
	private void validateSEI(DescriptionBuilderComposite seic) {
		
		//TODO: Validate SEI superclasses -- hmmm, may be doing this below
		//		
		
		if (!seic.getWebServiceAnnot().endpointInterface().equals("")) {
			throw ExceptionFactory.makeWebServiceException("DescriptionBuilderComposite: WebService annotation contains a non-empty field for the SEI");
		}

		checkSEIAgainstWSDL();
		
		//TODO: More validation here
		
		//TODO: Make sure we don't find any WebMethod annotations with exclude == true 
		//		anywhere in the superclasses chain
		
		//TODO: Check that all WebMethod annotations in the superclass chain are represented in 
		//		WSDL, assuming there is WSDL
		
		
		//TODO:	Validate that the interface is public 
		
		if (!composite.getWebServiceAnnot().endpointInterface().equals("")) {
			throw ExceptionFactory.makeWebServiceException("DescriptionBuilderComposite: WebService annotation contains a non-empty field for the SEI");
		}
		//		Call ValidateWebMethodAnnots()
		//
		
		//This will perform validation for all methods, regardless of WebMethod annotations
		//It is called for the SEI, and an impl. class that does not specify an endpointInterface
		validateMethods();
	}
	
	/**
	 * @return Returns TRUE if we find just one WebMethod Annotation 
	 */
	private boolean webMethodAnnotationsExist() {
		MethodDescriptionComposite mdc = null;
		Iterator<MethodDescriptionComposite> iter = composite.getMethodDescriptionsList().iterator();
		
		while (iter.hasNext()) {
			mdc = iter.next();

			if (mdc.getWebMethodAnnot() != null)
				return true;
		}
		
		return false;
	}
	
	private void verifyFalseExclusionsWithWSDL() {
		//TODO: Place logic here to verify that each exclude==false WebMethod annot we find
		//      is contained in the WSDL
	}
	
	private void verifyPublicMethodsWithWSDL() {
		//TODO: Place logic here to verify that each publicMethod with no WebMethod annot
		//      is contained in the WSDL

	}

	
	/**
	 */
	private void validateMethods() {
		//TODO: Fill this out to validate all MethodDescriptionComposite (and their inclusive
		//      annotations on this SEI (SEI is assumed here)
		//check oneway
		//
		
		//This could be an SEI, or an impl. class that doesn' specify an EndpointInterface (so, it
		//is implicitly an SEI...need to consider this
		//
		
		//TODO: Verify that, if this is an interface...that there are no Methods with WebMethod
		//      annotations that contain exclude == true
	
		//TODO: Verify that, if a SOAPBinding annotation exists, that its style be set to
		//      only DOCUMENT JSR181-Sec 4.7.1
	
	}
	
	private void validateWSDLOperations() {
		//Verifies that all operations on the wsdl are found in the impl/sei class
	}
}
