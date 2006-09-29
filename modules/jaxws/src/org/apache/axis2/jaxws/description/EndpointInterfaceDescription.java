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

import javax.jws.soap.SOAPBinding;
import javax.wsdl.Binding;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.xml.namespace.QName;

import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.jaxws.ExceptionFactory;

/**
 * An EndpointInterfaceDescription corresponds to a particular SEI-based Service
 * Implementation. It can correspond to either either a client to that impl or
 * the actual service impl.
 * 
 * The EndpointInterfaceDescription contains information that is relevant only
 * to an SEI-based (aka Endpoint-based or Java-based) enpdoint; Provider-based
 * endpoint, which are not operation based and do not have an associated SEI,
 * will not have an an EndpointInterfaceDescription class and sub-hierachy.
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
    // This may be an actual Service Endpoint Interface -OR- it may be a service implementation class that did not 
    // specify an @WebService.endpointInterface.
    private Class seiClass;
    
    // ===========================================
    // ANNOTATION related information
    // ===========================================
    
    // ANNOTATION: @SOAPBinding
    // Note this is the Type-level annotation.  See OperationDescription for the Method-level annotation
    private SOAPBinding         soapBindingAnnotation;
    // TODO: Should this be using the jaxws annotation values or should that be wrappered?
    private javax.jws.soap.SOAPBinding.Style            soapBindingStyle;
    // Default value per JSR-181 MR Sec 4.7 "Annotation: javax.jws.soap.SOAPBinding" pg 28
    public static final javax.jws.soap.SOAPBinding.Style SOAPBinding_Style_DEFAULT = javax.jws.soap.SOAPBinding.Style.DOCUMENT;
    private javax.jws.soap.SOAPBinding.Use              soapBindingUse;
    // Default value per JSR-181 MR Sec 4.7 "Annotation: javax.jws.soap.SOAPBinding" pg 28
    public static final javax.jws.soap.SOAPBinding.Use  SOAPBinding_Use_DEFAULT = javax.jws.soap.SOAPBinding.Use.LITERAL;
    private javax.jws.soap.SOAPBinding.ParameterStyle   soapParameterStyle;
    // Default value per JSR-181 MR Sec 4.7 "Annotation: javax.jws.soap.SOAPBinding" pg 28
    public static final javax.jws.soap.SOAPBinding.ParameterStyle SOAPBinding_ParameterStyle_DEFAULT = javax.jws.soap.SOAPBinding.ParameterStyle.WRAPPED;
    
    void addOperation(OperationDescription operation) {
        operationDescriptions.add(operation);
    }
    
    EndpointInterfaceDescription(Class sei, EndpointDescription parent) {
        seiClass = sei;
        
        // Per JSR-181 all methods on the SEI are mapped to operations regardless
        // of whether they include an @WebMethod annotation.  That annotation may
        // be present to customize the mapping, but is not required (p14)
        // TODO:  Testcases that do and do not include @WebMethod anno
        for (Method method:getSEIMethods(seiClass)) {
            OperationDescription operation = new OperationDescription(method, this);
            addOperation(operation);
        }
        
        parentEndpointDescription = parent;
    }
    
    private static Method[] getSEIMethods(Class sei) {
        // Per JSR-181 all methods on the SEI are mapped to operations regardless
        // of whether they include an @WebMethod annotation.  That annotation may
        // be present to customize the mapping, but is not required (p14)
        Method[] seiMethods = sei.getMethods();
        if (sei != null) {
            for (Method method:seiMethods) {
                if (!Modifier.isPublic(method.getModifiers())) {
                    // JSR-181 says methods must be public (p14)
                    // TODO NLS
                    ExceptionFactory.makeWebServiceException("SEI methods must be public");
                }
                // TODO: other validation per JSR-181
            }
            
        }
        return seiMethods;
    }
    
    /**
     * Update a previously created EndpointInterfaceDescription with information from an
     * annotated SEI.  This should only be necessary when the this was created with WSDL.  
     * In this case, the information from the WSDL is augmented based on the annotated SEI.
     * @param sei
     */
    public void updateWithSEI(Class sei) {
        if (seiClass != null && seiClass != sei)
            // TODO: It probably is invalid to try reset the SEI; but this isn't the right error processing
            throw new UnsupportedOperationException("The seiClass is already set; reseting it is not supported");
        else if (seiClass != null && seiClass == sei)
            // We've already done the necessary updates for this SEI
            return;
        else if (sei != null) {
            seiClass = sei;
            // Update (or possibly add) the OperationDescription for each of the methods on the SEI.
            for (Method seiMethod:getSEIMethods(seiClass)) {

                if (getOperation(seiMethod) != null) {
                    // If an OpDesc already exists with this java method set on it, then the OpDesc has already
                    // been updated for this method, so skip it.
                    continue;
                }
                // At this point (for now at least) the operations were created with WSDL previously.
                // If they had been created from an annotated class and no WSDL, then the seiClass would have 
                // already been set so we would have taken other branches in this if test.  (Note this could
                // change once AxisServices can be built from annotations by the ServiceDescription class).
                // Since the operations were created from WSDL, they will not have a java method, which
                // comes from the SEI, set on them yet.
                //
                // Another consideration is that currently Axis2 does not support overloaded WSDL operations.
                // That means there will only be one OperationDesc build from WSDL.  Still another consideration is
                // that the JAXWS async methods which may exist on the SEI will NOT exist in the WSDL.  An example
                // of these methods for the WSDL operation:
                //     String echo(String)
                // optionally generated JAX-WS SEI methods from the tooling; take note of the annotation specifying the 
                // operation name
                //     @WebMethod(operationName="echo" ...)
                //     Response<String> echoStringAsync(String)
                //     @WebMethod(operationName="echo" ...)
                //     Future<?> echoStringAsync(String, AsyncHandler)
                //
                // So given all the above, the code does the following based on the operation QName
                // (which might also be the java method name; see determineOperationQName for details)
                // (1) If an operationDesc does not exist, add it.
                // (2) If an operationDesc does exist but does not have a java method set on it, set it
                // (3) If an operationDesc does exist and has a java method set on it already, add a new one. 
                //
                // TODO: May need to change when Axis2 supports overloaded WSDL operations
                // TODO: May need to change when ServiceDescription can build an AxisService from annotations
                
                // Get the QName for this java method and then update (or add) the appropriate OperationDescription
                // See comments below for imporant notes about the current implementation.
                // NOTE ON OVERLOADED OPERATIONS
                // Axis2 does NOT currently support overloading WSDL operations.
                QName seiOperationQName = OperationDescription.determineOperationQName(seiMethod);
                OperationDescription[] updateOpDesc = getOperation(seiOperationQName);
                if (updateOpDesc == null || updateOpDesc.length == 0) {
                    // This operation wasn't defined in the WSDL.  Note that the JAX-WS async methods
                    // which are defined on the SEI are not defined as operations in the WSDL.
                    // Although they usually specific the same OperationName as the WSDL operation, 
                    // there may be cases where they do not.
                    // TODO: Is this path an error path, or can the async methods specify different operation names than the 
                    //       WSDL operation?
                    OperationDescription operation = new OperationDescription(seiMethod, this);
                    addOperation(operation);
                }
                else { 
                    // Currently Axis2 does not support overloaded operations.  That means that even if the WSDL
                    // defined overloaded operations, there would still only be a single AxisOperation, and it
                    // would be the last operation encounterd.
                    // HOWEVER the generated JAX-WS async methods (see above) may (will always?) have the same
                    // operation name and so will come down this path; they need to be added.
                    // TODO: When Axis2 starts supporting overloaded operations, then this logic will need to be changed
                    // TODO: Should we verify that these are the async methods before adding them, and treat it as an error otherwise?

                    // Loop through all the opdescs; if one doesn't currently have a java method set, set it
                    // If all have java methods set, then add a new one.  Assume we'll need to add a new one.
                    boolean addOpDesc = true;
                    for (OperationDescription checkOpDesc:updateOpDesc) {
                        if (checkOpDesc.getSEIMethod() == null) {
                            // TODO: Should this be checking (somehow) that the signature matches?  Probably not an issue until overloaded WSDL ops are supported.
                            checkOpDesc.setSEIMethod(seiMethod);
                            addOpDesc = false;
                            break;
                        }
                    }
                    if (addOpDesc) {
                        OperationDescription operation = new OperationDescription(seiMethod, this);
                        addOperation(operation);
                    }
                }
            }
        }
    }

    /**
     * Return the OperationDescriptions corresponding to a particular Java method name.
     * Note that an array is returned because a method could be overloaded.
     * 
     * @param javaMethodName String representing a Java Method Name
     * @return
     */
    // FIXME: This is confusing; some getOperations use the QName from the WSDL or annotation; this one uses the java method name; rename this signature I think; add on that takes a String but does a QName lookup against the WSDL/Annotation
    public OperationDescription[] getOperation(String javaMethodName) {
        if (javaMethodName == null) {
            return null;
        }
        
        ArrayList<OperationDescription> matchingOperations = new ArrayList<OperationDescription>();
        for (OperationDescription operation:getOperations()) {
            if (javaMethodName.equals(operation.getJavaMethodName())) {
                matchingOperations.add(operation);
            }
        }
        
        if (matchingOperations.size() == 0)
            return null;
        else
            return matchingOperations.toArray(new OperationDescription[0]);
    }
    
    public OperationDescription[] getOperations() {
        return operationDescriptions.toArray(new OperationDescription[0]);
    }
    
    public EndpointDescription getEndpointDescription() {
        return parentEndpointDescription;
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
        
        AxisService axisService = parentEndpointDescription.getAxisService();
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
    
    // ========================================
    // SOAP Binding annotation realted methods
    // ========================================
    SOAPBinding getSoapBinding(){
        // TODO: Test with sei Null, not null, SOAP Binding annotated, not annotated
        if (soapBindingAnnotation == null && seiClass != null) {
            soapBindingAnnotation = (SOAPBinding) seiClass.getAnnotation(SOAPBinding.class);
        }
        return soapBindingAnnotation;
    }
    
    public javax.jws.soap.SOAPBinding.Style getSoapBindingStyle() {
        if (soapBindingStyle == null) {
            if (getSoapBinding() != null && getSoapBinding().style() != null) {
                soapBindingStyle = getSoapBinding().style();
            }
            else {
                soapBindingStyle = SOAPBinding_Style_DEFAULT;
            }
        }
        return soapBindingStyle;
    }
    
    public javax.jws.soap.SOAPBinding.Use getSoapBindingUse() {
        if (soapBindingUse == null) {
            if (getSoapBinding() != null && getSoapBinding().use() != null) {
                soapBindingUse = getSoapBinding().use();
            }
            else {
                soapBindingUse = SOAPBinding_Use_DEFAULT;
            }
        }
        return soapBindingUse;
    }
    
    public javax.jws.soap.SOAPBinding.ParameterStyle getSoapBindingParameterStyle(){
    	if (soapParameterStyle == null) {
            if (getSoapBinding() != null && getSoapBinding().parameterStyle() != null) {
            	soapParameterStyle = getSoapBinding().parameterStyle();
            }
            else {
            	soapParameterStyle = SOAPBinding_ParameterStyle_DEFAULT;
            }
        }
        return soapParameterStyle;
    }
}
