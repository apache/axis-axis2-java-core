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
package org.apache.axis2.jaxws.description;

import java.lang.reflect.Method;

import javax.xml.namespace.QName;

/**
 * An EndpointInterfaceDescription corresponds to a particular SEI-based Service
 * Implementation. It can correspond to either either a client to that impl or
 * the actual service impl.
 * 
 * The EndpointInterfaceDescription contains information that is relevant only
 * to an SEI-based (aka Endpoint-based or Java-based) enpdoint; Provider-based
 * endpoint, which are not operation based and do not have an associated SEI,
 * will not have an an EndpointInterfaceDescription class and sub-hierachy.
 * 
 * <pre>
 * <b>EndpointInterfaceDescription details</b>
 * 
 *     CORRESPONDS TO:      An SEI (on both Client and Server)      
 *         
 *     AXIS2 DELEGATE:      none
 *     
 *     CHILDREN:            1..n OperationDescription
 *     
 *     ANNOTATIONS:
 *         SOAPBinding [181]
 *     
 *     WSDL ELEMENTS:
 *         portType
 *         
 *  </pre>       
 */

public interface EndpointInterfaceDescription {
    public abstract EndpointDescription getEndpointDescription();
    
    public abstract String getTargetNamespace();
    
    public abstract OperationDescription getOperation(Method seiMethod);
    public abstract OperationDescription[] getOperation(QName operationQName);
    public abstract OperationDescription getOperation(String operationName);
    public abstract OperationDescription[] getOperations();
    public abstract OperationDescription[] getOperationForJavaMethod(String javaMethodName);
    
    public abstract Class getSEIClass();

    // TODO: These should return a locally defined Enums
    public abstract javax.jws.soap.SOAPBinding.ParameterStyle getSoapBindingParameterStyle();
    public abstract javax.jws.soap.SOAPBinding.Style getSoapBindingStyle();
    public abstract javax.jws.soap.SOAPBinding.Use getSoapBindingUse();
    
}