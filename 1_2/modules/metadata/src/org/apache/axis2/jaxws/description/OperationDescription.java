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

import javax.xml.namespace.QName;

import org.apache.axis2.description.AxisOperation;

/**
 * An OperationDescripton corresponds to a method on an SEI.  That SEI could be explicit
 * (i.e. WebService.endpointInterface=sei.class) or implicit (i.e. public methods on the service implementation
 * are the contract and thus the implicit SEI).  Note that while OperationDescriptions are created on both the client
 * and service side, implicit SEIs will only occur on the service side.
 * 
 * OperationDescriptons contain information that is only relevent for and SEI-based service, i.e. one that is invoked via specific
 * methods.  This class does not exist for Provider-based services (i.e. those that specify WebServiceProvider)
 * 
 * <pre>
 * <b>OperationDescription details</b>
 * 
 *     CORRESPONDS TO:      A single operation on an SEI (on both Client and Server)      
 *         
 *     AXIS2 DELEGATE:      AxisOperation
 *     
 *     CHILDREN:            0..n ParameterDescription
 *                          0..n FaultDescription (Note: Not fully implemented)
 *     
 *     ANNOTATIONS:
 *         WebMethod [181]
 *         SOAPBinding [181]
 *         Oneway [181]
 *         WebResult [181]
 *         RequestWrapper [224]
 *         ResponseWrapper [224]
 *     
 *     WSDL ELEMENTS:
 *         operation
 *         
 *  </pre>       
 */
public interface OperationDescription {
    public EndpointInterfaceDescription getEndpointInterfaceDescription();
    public FaultDescription[] getFaultDescriptions();
    public FaultDescription resolveFaultByExceptionName(String exceptionClassName);
    public ParameterDescription getParameterDescription(int parameterNumber);
    public ParameterDescription getParameterDescription(String parameterName);
    public ParameterDescription[] getParameterDescriptions();
    
    public abstract AxisOperation getAxisOperation();
    
    public String getJavaMethodName();
    public String getJavaDeclaringClassName();
    public String[] getJavaParameters();
    // TODO: Fix up the difference between getSEIMethod and getMethodFromServiceImpl when java reflection is removed.
    /**
     * Client side and non-DBC service side only! Return the SEI method for which a service.getPort(Class SEIClass) created
     * the EndpointDescriptionInterface and the associated OperationDescriptions.  Returns null on the
     * service implementation side. 
     * @return
     */
    public Method getSEIMethod();
    /**
     * Service implementation side only!  Given a service implementation class, find the method
     * on that class that corresponds to this operation description.  This is necessary because on
     * the service impl side, the OperationDescriptions can be built using byte-scanning and without the
     * class actually having been loaded.
     * @param serviceImpl
     * @return
     */
    public Method getMethodFromServiceImpl(Class serviceImpl);
    
    /**
     * Answer if this operation corresponds to the JAX-WS Client-only async methods.  These methods
     * are of the form:
     *   javax.xml.ws.Response<T> method(...)
     *   java.util.concurrent.Future<?> method(..., javax.xml.ws.AsyncHandler<T>)
     *
     * @return
     */
    public boolean isJAXWSAsyncClientMethod(); 
    
    public QName getName();
    public String getOperationName();
    public String getAction();
    public boolean isOneWay();
    public boolean isExcluded();
    public boolean isOperationReturningResult();

    public String getResultName();
    public String getResultTargetNamespace();
    public String getResultPartName();
    public boolean isResultHeader();
   
    
    
    /**
     * Return the Class of the return type.  For JAX-WS async returns of
     * type Response<T> or AsyncHandler<T>, the class associated with Response
     * or AsyncHanler respectively is returned.  To get the class associated with 
     * <T>
     * @see getResultActualType()
     * @return Class
     */
    public Class getResultType();
    
    /**
     * Return the actual Class of the type.  For a JAX-WS async return
     * type of Response<T> or AsyncHandler<T>, the class associated with <T>
     * is returned.  For non-JAX-WS async returns, the class associated with the 
     * return type is returned.  Note that for a Generic return type, such as 
     * List<Foo>, the class associated with List will be returned.
     * @return actual Class
     */
    public Class getResultActualType();
    
    /**
     * @return the class name of the wrapper class.  
     * NOTE: This method will return null if the request wrapper class is not known during the description layer processing.
     * In such cases the implementation may use proprietary code to find the class.
     * For example, JAXWS may look for a matching class in the sei package, in a special jaxws package or
     * proceed without the class name
     */
    public String getRequestWrapperClassName();
    public String getRequestWrapperTargetNamespace();
    public String getRequestWrapperLocalName();

    /**
     * @return the class name of the wrapper class.  
     * NOTE: This method will return null if the request wrapper class is not known during the description layer processing.
     * In such cases the implementation may use proprietary code to find the class.
     * For example, JAXWS may look for a matching class in the sei package, in a special jaxws package or
     * proceed without the class name
     */
    public String getResponseWrapperClassName();
    public String getResponseWrapperTargetNamespace();
    public String getResponseWrapperLocalName();
    
    public String[] getParamNames();
    
    // TODO: These should return Enums defined on this interface, not from the Annotation
    public javax.jws.soap.SOAPBinding.ParameterStyle getSoapBindingParameterStyle();
    public javax.jws.soap.SOAPBinding.Style getSoapBindingStyle();
    public javax.jws.soap.SOAPBinding.Use getSoapBindingUse();

    public OperationRuntimeDescription getOperationRuntimeDesc(String name);
    public void setOperationRuntimeDesc(OperationRuntimeDescription ord);
    public OperationDescription getSyncOperation();
}