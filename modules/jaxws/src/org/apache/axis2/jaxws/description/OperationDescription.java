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
Java Name: Method name from SEI

Axis2 Delegate: AxisOperation

JSR-181 Annotations: 
@WebMethod
- operationName
- action
- exclude
@Oneway So basically even if an operation has a return parameter it could be one way and in this case should we set the AxisOperatio mep to oneway?[NT]
TBD

WSDL Elements
<portType  <operation

JAX-WS Annotations
@RequestWrapper
- localName
- targetNamespace
- className
@ResponseWrapper
- localName
- targetNamespace
- className
TBD

Properties available to JAXWS runtime: 
isWrapper()
String getRequestWrapper JAXB Class
String getResponseWrapper JAXB Class
TBD

 */
public class OperationDescription {

}
