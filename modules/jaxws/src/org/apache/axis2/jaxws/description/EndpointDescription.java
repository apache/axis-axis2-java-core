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

import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.xml.namespace.QName;


/**
 * 
 */
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
 * TODO: EndpointDescription should be created AxisService objects and not directly from WSDL
 * IMPORTANT NOTE: Axis2 currently only supports 1 service and 1 port under that service.  When that is
 * fixed, that will probably have an impact on this class.  In particular, I think this should be created 
 * somehow from an AxisService/AxisPort combination, and not directly from the WSDL.
 */
public class EndpointDescription {
    private QName portQName;
    // TODO: This needs to be a collection of handler descriptions; use JAX-WS Appendix B Handler Chain Configuration File Schema as a starting point
    private ArrayList<String> handlerList = new ArrayList<String>();
    
    /**
     * Create an EndpointDescription based on the WSDL port.  Note that per the JAX-WS Spec (Final Release, 4/19/2006
     * Section 4.2.3 Proxies, page 55)the "namespace component of the port is the target namespace of the WSDL 
     * definition document".
     * 
     * @param wsdlPort The WSDL Port tag for this EndpointDescription.
     * @param definition The WSDL Definition target namespace used to create the port QName
     */
    EndpointDescription(Port wsdlPort, Definition definition) {
        String localPart = wsdlPort.getName();
        String namespace = definition.getTargetNamespace();
        portQName = new QName(namespace, localPart);
    }
    public QName getPortQName() {
        return portQName;
    }
    
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
