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
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.OutInAxisOperation;
import org.apache.axis2.description.OutOnlyAxisOperation;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.RobustOutOnlyAxisOperation;
import org.apache.axis2.description.WSDL11ToAxisServiceBuilder;
import org.apache.axis2.engine.AbstractDispatcher;
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

Axis2 Delegate: AxisService

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
 * 
 */
public class ServiceDescription {
    private AxisService axisService;

    private URL wsdlURL;
    private QName serviceQName;
    
    // Only ONE of the following will be set in a ServiceDescription, depending on whether this Description
    // was created from a service-requester or service-provider flow. 
    private Class serviceClass;         // A service-requester generated service or generic service class
    private Class serviceImplClass;     // A service-provider service implementation class
    
    // TODO: Possibly remove Definition and delegate to the Defn on the AxisSerivce set as a paramater by WSDLtoAxisServicBuilder?
    private WSDLWrapper wsdlWrapper; 
    
    private Hashtable<QName, EndpointDescription> endpointDescriptions = new Hashtable<QName, EndpointDescription>();
    
    private static final Log log = LogFactory.getLog(AbstractDispatcher.class);

    public static final String AXIS_SERVICE_PARAMETER = "org.apache.axis2.jaxws.description.ServiceDescription";
    
    /**
     * ServiceDescription contains the metadata (e.g. WSDL, annotations) relating to a Service.
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
        this.serviceQName = serviceQName;
        this.serviceClass = serviceClass;

        
        setupWsdlDefinition();
        // TODO: (JLB) Refactor this with the consideration of no WSDL/Generic Service/Annotated SEI
        //       Possibly defer creation of AxisService to the getPort() call?
        setupAxisService();
        buildDescriptionHierachy();
    }

    ServiceDescription(Class serviceImplClass, AxisService axisService) {
        this.serviceImplClass = serviceImplClass;
        this.axisService = axisService;
        // Add a reference to this ServiceDescription object to the AxisService
        if (axisService != null) {
            Parameter parameter = new Parameter();
            parameter.setName(AXIS_SERVICE_PARAMETER);
            parameter.setValue(this);
            // TODO: (JLB) What to do if AxisFault
            try {
                axisService.addParameter(parameter);
            } catch (AxisFault e) {
                // TODO: (JLB) Throwing wrong exception
                e.printStackTrace();
                throw new UnsupportedOperationException("Can't add AxisService param: " + e);
            }
        }
        // Look for @WebService
        // If @WebService.endpointInterface != null, use that
        // Else use the public (including inherited) methods
        WebService webServiceAnnotation = (WebService) this.serviceImplClass.getAnnotation(WebService.class);

        // TODO: (JLB) Initial code path requires the @WebService annotation
        if (webServiceAnnotation == null) {
            throw new UnsupportedOperationException("TEMPORARY CODE: ServiceImpl bean must have @WebServiceAnnotation");
        }
        if (webServiceAnnotation.endpointInterface() == null) {
            throw new UnsupportedOperationException("TEMPORARY CODE: ServiceImpl bean must have @WebServiceAnnotation.endpointInterface set");
        }
        
        // TODO: (JLB) Using Class.forName() is probably not the best long-term way to get the SEI class from the annotation
        Class seiClass = null;
        try {
            seiClass = Class.forName(webServiceAnnotation.endpointInterface());
        } catch (ClassNotFoundException e) {
            // TODO: (JLB) Throwing wrong exception
            e.printStackTrace();
            throw new UnsupportedOperationException("Can't create SEI class: " + e);
        }
        // Create the EndpointDescription hierachy from the SEI annotations; Since the PortQName is null, 
        // it will be set to the annotation value.
        EndpointDescription endpointDescription = new EndpointDescription(seiClass, null, this);
        addEndpointDescription(endpointDescription);

    }


    
    /*=======================================================================*/
    /*=======================================================================*/
    // START of public accessor methods
    
    /**
     * Updates the ServiceDescription based on the SEI class and its annotations.
     * @param sei
     * @param portQName
     */
    public void updateEndpointInterfaceDescription(Class sei, QName portQName) {
        
        if (getEndpointDescription(portQName) != null) {
            // TODO: (JLB) Implement validating SEI against existing port built from WSDL
            throw new UnsupportedOperationException("Not implemented yet");
        }
        else {
            // Use the SEI Class and its annotations to finish creating the Description hierachy: Endpoint, EndpointInterface, Operations, Parameters, etc.
            // TODO: (JLB) Need to create the Axis Description objects after we have all the config info (i.e. from this SEI)
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
     * this will return NULL unless the Descriptions were built by introspection on the SEI
     * and its annotations.
     * @param seiClass
     * @return
     */
    public EndpointDescription[] getEndpointDescription(Class seiClass) {
        EndpointDescription[] returnEndpointDesc = null;
        ArrayList<EndpointDescription> matchingEndpoints = new ArrayList<EndpointDescription>();
        Enumeration<EndpointDescription> endpointEnumeration = endpointDescriptions.elements();
        while (endpointEnumeration.hasMoreElements()) {
            EndpointDescription endpointDescription = endpointEnumeration.nextElement();
            Class endpointSEIClass = endpointDescription.getEndpointInterfaceDescription().getSEIClass(); 
            if (endpointSEIClass != null && endpointSEIClass.equals(seiClass)) {
                matchingEndpoints.add(endpointDescription);
            }
        }
        if (matchingEndpoints.size() > 0) {
            returnEndpointDesc = matchingEndpoints.toArray(new EndpointDescription[0]);
        }
        return returnEndpointDesc;
    }
    public AxisService getAxisService() {
        return axisService;
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

    private void setupAxisService() {
        // TODO: Need to use MetaDataQuery validator to merge WSDL (if any) and annotations (if any)
        
        if (wsdlWrapper != null) {
            buildAxisServiceFromWSDL();
        }
        else {
            buildAxisServiceFromNoWSDL();
        }
    }

    private void buildAxisServiceFromWSDL() {
        // TODO: Change this to use WSDLToAxisServiceBuilder superclass
        WSDL11ToAxisServiceBuilder serviceBuilder = new WSDL11ToAxisServiceBuilder(wsdlWrapper.getDefinition(), serviceQName, null);
        try {
            axisService = serviceBuilder.populateService();
        } catch (AxisFault e) {
            // TODO We should not swallow a fault here.
            log.warn(Messages.getMessage("warnAxisFault", e.toString()));
        }
    }
    
    private void buildAxisServiceFromNoWSDL() {
        // TODO: (JLB) Refactor this; probably just remove this method
//        if (true) return;
//        
//        // Patterned after ServiceClient.createAnonymousService()
//        String serviceName = null;
//        if (serviceQName != null) {
//            serviceName = serviceQName.getLocalPart();
//        }
//        else {
//            serviceName = ServiceClient.ANON_SERVICE;
//        }
//        // Make this service name unique.  The Axis2 engine assumes that a service it can not find is a client-side service.
//        // See org.apache.axis2.client.ServiceClient.configureServiceClient()
//        axisService = new AxisService(serviceName + this.hashCode());
//        axisService.addOperation(new RobustOutOnlyAxisOperation(ServiceClient.ANON_ROBUST_OUT_ONLY_OP));
//        axisService.addOperation(new OutOnlyAxisOperation(ServiceClient.ANON_OUT_ONLY_OP));
//        axisService.addOperation(new OutInAxisOperation(ServiceClient.ANON_OUT_IN_OP));
    }
    
    private void buildDescriptionHierachy() {
        // Create the EndpointDescription corresponding to the WSDL <port> tags
        if (wsdlWrapper != null) {
            buildEndpointDescriptionsFromWSDL();
        }
        // TODO: Need to create from Annotations (if no WSDL) and modify created ones based on annotations (if WSDL)
        
    }
    
    private void buildEndpointDescriptionsFromWSDL() {
        // TODO: Currently Axis2 only supports 1 service and 1 port; that fix will likely affect this code
        //       Until then, build the EndpointDescriptions directly from the WSDL.
        Definition definition = wsdlWrapper.getDefinition();
        Service service = definition.getService(serviceQName);
        if (service == null) {
            throw ExceptionFactory.makeWebServiceException(Messages.getMessage("serviceDescErr2", serviceQName.toString()));
        }
        
        Map ports = service.getPorts();
        if (ports != null && ports.size() > 0) {
            Iterator portIterator = ports.values().iterator();
            while (portIterator.hasNext()) {
                Port wsdlPort = (Port) portIterator.next();
                EndpointDescription endpointDescription = new EndpointDescription(wsdlPort, definition, this);
                addEndpointDescription(endpointDescription); 
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
}
