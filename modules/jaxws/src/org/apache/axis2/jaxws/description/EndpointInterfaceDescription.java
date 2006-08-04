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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import javax.jws.SOAPBinding;
import javax.wsdl.Binding;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.xml.namespace.QName;

import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.jaxws.ExceptionFactory;

/**
 * 
 */
/*
Java Name: SEI Class name

Axis2 Delegate: none

JSR-181 Annotations: 
@WebService Note this can be specified on the endpoint Impl without an SEI
- name its the PortType Class Name, one you get with getPort() call in Service Delegate [NT]
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
    private EndpointDescription parentEndpointDescription;
    private ArrayList<OperationDescription> operationDescriptions = new ArrayList<OperationDescription>();
    private Class seiClass;
    
    void addOperation(OperationDescription operation) {
        // TODO: This does not support overloaded operations.  While not supported by WS-I, it IS supported by JAX-WS (p11).
        //       Note that this also requires support in Axis2; currently WSDL11ToAxisServiceBuilder.populateOperations does not
        //       support overloaded methods in the WSDL; the operations are stored on AxisService as children in a HashMap with the wsdl
        //       operation name as the key.
        // TODO: (JLB) Could make this a List collection and allow lookups on seiMethod (what Proxy might use) as a workaround for now.
        operationDescriptions.add(operation);
    }
    
    EndpointInterfaceDescription(Class sei, EndpointDescription parent) {
        // Per JSR-181 all methods on the SEI are mapped to operations regardless
        // of whether they include an @WebMethod annotation.  That annotation may
        // be present to customize the mapping, but is not required (p14)
        // TODO: (JLB) Testcases that do and do not include @WebMethod anno
        seiClass = sei;
        
        Method[] seiMethods = seiClass.getMethods();
        for (Method method:seiMethods) {
            if (!Modifier.isPublic(method.getModifiers())) {
                // JSR-181 says methods must be public (p14)
                // TODO NLS
                ExceptionFactory.makeWebServiceException("SEI methods must be public");
            }
            // TODO: (JLB) other validation per JSR-181
            
            OperationDescription operation = new OperationDescription(method, this);
            addOperation(operation);
        }

    }
    
    public OperationDescription[] getOperations() {
        return operationDescriptions.toArray(new OperationDescription[0]);
    }
    
    /**
     * Return an array of Operations given an operation QName.  Note that an array is returned
     * since a WSDL operation may be overloaded per JAX-WS.
     * @param operationQName
     * @return
     */
    public OperationDescription[] getOperation(QName operationQName) {
        OperationDescription[] returnOperations = null;
        if (!DescriptionUtils.isEmpty(operationQName)) {
            ArrayList<OperationDescription> matchingOperations = new ArrayList<OperationDescription>();
            OperationDescription[] allOperations = getOperations();
            for (OperationDescription operation:allOperations) {
                if (operation.getName().equals(operationQName)) {
                    matchingOperations.add(operation);
                }
            }
            // Only return an array if there's anything in it
            if (matchingOperations.size() > 0) {
                returnOperations = matchingOperations.toArray(new OperationDescription[0]);
            }
        }
        return returnOperations;
    }
    
    /**
     * Return an OperationDescription for the corresponding SEI method.  Note that this ONLY works
     * if the OperationDescriptions were created from introspecting an SEI.  If the were created with a WSDL
     * then use the getOperation(QName) method, which can return > 1 operation.
     * @param seiMethod The java.lang.Method from the SEI for which an OperationDescription is wanted
     * @return
     */
    public OperationDescription getOperation(Method seiMethod) {
        OperationDescription returnOperation = null;
        if (seiMethod != null) {
            OperationDescription[] allOperations = getOperations();
            for (OperationDescription operation:allOperations) {
                if (operation.getSEIMethod() != null && operation.getSEIMethod().equals(seiMethod)) {
                    returnOperation = operation;
                }
            }
        }
        return returnOperation;
    }
    
    /**
     * Build from AxisService
     * @param parent
     */
    EndpointInterfaceDescription(EndpointDescription parent) {
        parentEndpointDescription = parent;
        
        AxisService axisService = parentEndpointDescription.getServiceDescription().getAxisService();
        if (axisService != null) {
            ArrayList publishedOperations = axisService.getPublishedOperations();
            Iterator operationsIterator = publishedOperations.iterator();
            while (operationsIterator.hasNext()) {
                AxisOperation axisOperation = (AxisOperation) operationsIterator.next();
                addOperation(new OperationDescription(axisOperation, this));
            }
        }
        
    }
    public Class getSEIClass() {
        return seiClass;
    }
    // Annotation-realted getters
    public SOAPBinding getSoapBinding(){
        // TODO: (JLB) Test with sei Null, not null, SOAP Binding annotated, not annotated
        return (seiClass != null ? (SOAPBinding) seiClass.getAnnotation(SOAPBinding.class) : null);
    }

}
