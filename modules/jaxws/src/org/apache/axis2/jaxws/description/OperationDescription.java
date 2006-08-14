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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;

import javax.jws.Oneway;
import javax.jws.SOAPBinding;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.xml.namespace.QName;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

import org.apache.axis2.description.AxisOperation;

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
// TODO: Axis2 does not support overloaded operations, although EndpointInterfaceDescription.addOperation() does support overloading
//       of methods represented by OperationDescription classes.  However, the AxisOperation contained in an OperationDescription
//       does NOT support overloaded methods.
//
//       While overloading is not supported by WS-I, it IS supported by JAX-WS (p11).
//       Note that this requires support in Axis2; currently WSDL11ToAxisServiceBuilder.populateOperations does not
//       support overloaded methods in the WSDL; the operations are stored on AxisService as children in a HashMap with the wsdl
//       operation name as the key.

public class OperationDescription {
    private EndpointInterfaceDescription parentEndpointInterfaceDescription;
    private AxisOperation axisOperation;
    private QName operationName;
    private Method seiMethod;
    private WebMethod webMethodAnnotation;
    
    OperationDescription(Method method, EndpointInterfaceDescription parent) {
        // TODO: (JLB) Look for WebMethod anno; get name and action off of it
        parentEndpointInterfaceDescription = parent;
        seiMethod = method;
        webMethodAnnotation = seiMethod.getAnnotation(WebMethod.class);
        
        // Per JSR-181, if @WebMethod specifies and operation name, use that.  Otherwise
        // default is the Java method name
        String methodName;
        if (webMethodAnnotation != null && webMethodAnnotation.operationName() != null && !"".equals(webMethodAnnotation.operationName()))
            methodName = webMethodAnnotation.operationName();
        else
            methodName = method.getName();
        this.operationName = new QName(methodName);
    }
    OperationDescription(AxisOperation operation, EndpointInterfaceDescription parent) {
        parentEndpointInterfaceDescription = parent;
        axisOperation = operation;       
    }

    public EndpointInterfaceDescription getEndpointInterfaceDescription() {
        return parentEndpointInterfaceDescription;
    }
    
    public AxisOperation getAxisOperation() {
        return axisOperation;
    }
    
    public QName getName() {
        return operationName;
    }
    
    // Java-related getters
    public String getJavaMethodName() {
        String returnString = null;
        if (seiMethod != null) {
            returnString = seiMethod.getName();
        }
        return returnString;
    }
    public String[] getJavaParameters() {
        ArrayList<String> returnParameters = new ArrayList<String>();
        if (seiMethod != null) {
            Class[] paramaters = seiMethod.getParameterTypes();
            for (Class param:paramaters) {
                returnParameters.add(param.getName());
            }
        }
        // TODO: (JLB) This is different than the rest, which return null instead of an empty array
        return returnParameters.toArray(new String[0]);
    }
    /**
     * Note this will return NULL unless the operation was built via introspection on the SEI.
     * In other words, it will return null if the operation was built with WSDL.
     * @return
     */
    public Method getSEIMethod() {
        return seiMethod;
    }

    // Annotation-related getters
    // TODO: (JLB) The getters should return processed information rather than the actual annotations?
    // TODO: (JLB) Should there be protected getters to return annotations and WSDL constructs directly
    // TODO: (JLB) These should cache the information rather than re-getting it each time.
    public RequestWrapper getRequestWrapper() {
        return seiMethod.getAnnotation(RequestWrapper.class);
    }
    public ResponseWrapper getResponseWrapper() {
        return seiMethod.getAnnotation(ResponseWrapper.class);
    }
    public WebParam[] getWebParam() {
        Annotation[][] paramAnnotation = seiMethod.getParameterAnnotations();
        ArrayList<WebParam> webParamList = new ArrayList<WebParam>();
        for(Annotation[] pa:paramAnnotation){
            for(Annotation webParam:pa){
                if(webParam.annotationType() == WebParam.class){
                    webParamList.add((WebParam)webParam);
                }
            }
        }
        return webParamList.toArray(new WebParam[0]);
    }
    public WebResult getWebResult() {
        return seiMethod.getAnnotation(WebResult.class);
    }
    public SOAPBinding getSoapBinding() {
        return seiMethod.getAnnotation(SOAPBinding.class);
    }
    public boolean isOneWay() {
        return seiMethod.isAnnotationPresent(Oneway.class);
    }

}
