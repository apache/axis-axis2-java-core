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

/**
 * 
 */
/*
Java Name: SEI Class name

Axis2 Delegate: none

JSR-181 Annotations: 
@WebService Note this can be specified on the endpoint Impl without an SEI
- name it’s the PortType Class Name, one you get with getPort() call in Service Delegate [NT]
- targetNamespace
- serviceName default is portType+Service. Should we use this if Service.create call does not provide/have ServiceQname?[NT]
- wsdlLocation if no wsdl location provided the read this annotation. Should this override what is client sets?[NT]
- endpointInterface Will not be present on interfaces (SEI), so I will use this to figure out if the client Call is Extension of Service or is SEI by looking at this annotation. [NT]
- portName ok so JSR 181 spec I have does not have this annotation but JAXWS spec I have has this. So if ServiceDelegate.getPort() does not have port name use this annotation and derive portName [NT]
@SOAPBinding This one is important for Proxy especially. [NT]
- style: DOCUMENT | RPC tells me if it is doc or rpc[NT]
- use: LITERAL | ENCODED Always literal for IBM[NT]
- parameterStyle:  BARE | WRAPPED tells me if the wsdl is wrapped or not wrapped [NT]
@HandlerChain(file, name)
TBD

WSDL Elements
<portType
<binding used for operation parameter bindings below

Properties available to JAXWS runtime: 
getHandlerList() returns a live List of handlers which can be modified; this MUST be cloned before being used as an actual handler chain; Note this needs to consider if any @HandlerChain annotations are in the ServiceDescription as well
TBD

 */
public class EndpointInterfaceDescription {

}
