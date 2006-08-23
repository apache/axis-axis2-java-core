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
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.soap.SOAPBinding;
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

// TODO: Need tests for all the "default" code paths in the annotation getters.
// TODO: Need tests for each when annotation is not present where that is allowed by the spec. 
public class OperationDescription {
    private EndpointInterfaceDescription parentEndpointInterfaceDescription;
    private AxisOperation axisOperation;
    private QName operationName;
    private Method seiMethod;

    // Annotations and related cached values
    private Oneway              onewayAnnotation;
    private Boolean             onewayIsOneway;
    
    private RequestWrapper      requestWrapperAnnotation;
    private String              requestWrapperTargetNamespace;
    private String              requestWrapperLocalName;
    private String              requestWrapperClassName;
    
    private ResponseWrapper     responseWrapperAnnotation;
    private String              responseWrapperLocalName;
    private String              responseWrapperTargetNamespace;
    private String              responseWrapperClassName;
    
    private SOAPBinding         soapBindingAnnotation;
    // TODO: (JLB) Should this be using the jaxws annotation values or should that be wrappered?
    private javax.jws.soap.SOAPBinding.Style soapBindingStyle;
    
    private WebMethod           webMethodAnnotation;
    private String              webMethodOperationName;
    
    // TODO: (JLB) Should WebParam annotation be moved to the ParameterDescription?
    private WebParam[]          webParamAnnotations;
    private String[]            webParamNames;
    
    private WebResult           webResultAnnotation;
    private String              webResultName;
    
    OperationDescription(Method method, EndpointInterfaceDescription parent) {
        // TODO: (JLB) Look for WebMethod anno; get name and action off of it
        parentEndpointInterfaceDescription = parent;
        seiMethod = method;
        webMethodAnnotation = seiMethod.getAnnotation(WebMethod.class);
        
        this.operationName = new QName(getWebMethodOperationName());
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
    
    // =====================================
    // WebMethod annotation related methods
    // =====================================
    WebMethod getWebMethod() {
        return webMethodAnnotation;
    }
    public String getWebMethodOperationName() {
        if (webMethodOperationName == null) {
            // Per JSR-181, if @WebMethod specifies and operation name, use that.  Otherwise
            // default is the Java method name
            if (getWebMethod() != null && !DescriptionUtils.isEmpty(getWebMethod().operationName())) {
                webMethodOperationName = getWebMethod().operationName();
            }
            else {
                webMethodOperationName = seiMethod.getName();
            }
        }
        return webMethodOperationName;
    }
    
    // ==========================================
    // RequestWrapper Annotation related methods
    // ==========================================
    RequestWrapper getRequestWrapper() {
        if (requestWrapperAnnotation == null) {
            requestWrapperAnnotation = seiMethod.getAnnotation(RequestWrapper.class); 
        }
        return requestWrapperAnnotation;
    }
    
    public String getRequestWrapperLocalName() {
        if (requestWrapperLocalName == null) {
            if (getRequestWrapper() != null && !DescriptionUtils.isEmpty(getRequestWrapper().localName())) {
                requestWrapperLocalName = getRequestWrapper().localName();
            }
            else { 
                // The default value of localName is the value of operationName as 
                // defined in the WebMethod annotation. [JAX-WS Sec. 7.3, p. 80]
                requestWrapperLocalName = getWebMethodOperationName();
            }
        }
        return requestWrapperLocalName;
    }
    
    public String getRequestWrapperTargetNamespace() {
        if (requestWrapperTargetNamespace == null) {
            if (getRequestWrapper() != null && !DescriptionUtils.isEmpty(getRequestWrapper().targetNamespace())) {
                requestWrapperTargetNamespace = getRequestWrapper().targetNamespace();
            }
            else {
                // The default value for targetNamespace is the target namespace of the SEI. [JAX-WS Sec 7.3, p. 80]
                // TODO: (JLB) Get the TNS from the SEI via the endpoint interface desc.
                throw new UnsupportedOperationException("RequestWrapper.targetNamespace default not implented yet");
            }
        }
        return requestWrapperTargetNamespace;
    }
    
    public String getRequestWrapperClassName() {
        if (requestWrapperClassName == null) {
            if (getRequestWrapper() != null && !DescriptionUtils.isEmpty(getRequestWrapper().className())) {
                requestWrapperClassName = getRequestWrapper().className();
            }
            else {
                // Not sure what the default value should be (if any).  None is listed in Sec. 7.3 on p. 80 of
                // the JAX-WS spec, BUT Conformance(Using javax.xml.ws.RequestWrapper) in Sec 2.3.1.2 on p. 13
                // says the entire annotation "...MAY be omitted if all its properties would have default vaules."
                // implying there IS some sort of default.  We'll try this for now:
                Class clazz = seiMethod.getDeclaringClass();
                String packageName = clazz.getPackage().getName();
                String className = DescriptionUtils.javaMethodtoClassName(seiMethod.getName());
                requestWrapperClassName = packageName + "." + className;
            }
        }
        return requestWrapperClassName;
    }
    
    // ===========================================
    // ResponseWrapper Annotation related methods
    // ===========================================
    ResponseWrapper getResponseWrapper() {
        if (responseWrapperAnnotation == null) {
            responseWrapperAnnotation = seiMethod.getAnnotation(ResponseWrapper.class);
        }
        return responseWrapperAnnotation;
    }
    
    public String getResponseWrapperLocalName() {
        if (responseWrapperLocalName == null) {
            if (getResponseWrapper() != null && !DescriptionUtils.isEmpty(getResponseWrapper().localName())) {
                responseWrapperLocalName = getResponseWrapper().localName();
            }
            else { 
                // The default value of localName is the value of operationName as 
                // defined in the WebMethod annotation appended with "Response". [JAX-WS Sec. 7.4, p. 81]
                responseWrapperLocalName = getWebMethodOperationName() + "Response";
            }
        }
        return responseWrapperLocalName;
    }
    
    public String getResponseWrapperTargetNamespace() {
        if (responseWrapperTargetNamespace == null) {
            if (getResponseWrapper() != null && !DescriptionUtils.isEmpty(getResponseWrapper().targetNamespace())) {
                responseWrapperTargetNamespace = getResponseWrapper().targetNamespace();
            }
            else {
                // The default value for targetNamespace is the target namespace of the SEI. [JAX-WS Sec 7.4, p. 81]
                // TODO: (JLB) Get the TNS from the SEI via the endpoint interface desc.
                throw new UnsupportedOperationException("ResponseWrapper.targetNamespace default not implented yet");
            }
        }
        return responseWrapperTargetNamespace;
    }
    
    public String getResponseWrapperClassName() {
        if (responseWrapperClassName == null) {
            if (getResponseWrapper() != null && !DescriptionUtils.isEmpty(getResponseWrapper().className())) {
                responseWrapperClassName = getResponseWrapper().className();
            }
            else {
                // Not sure what the default value should be (if any).  None is listed in Sec. 7.4 on p. 81 of
                // the JAX-WS spec, BUT Conformance(Using javax.xml.ws.ResponseWrapper) in Sec 2.3.1.2 on p. 13
                // says the entire annotation "...MAY be omitted if all its properties would have default vaules."
                // implying there IS some sort of default.  We'll try this for now:
                Class clazz = seiMethod.getDeclaringClass();
                String packageName = clazz.getPackage().getName();
                String className = DescriptionUtils.javaMethodtoClassName(seiMethod.getName());
                responseWrapperClassName = packageName + "." + className;
            }
        }
        return responseWrapperClassName;
    }

    // ===========================================
    // WebParam Annotation related methods
    // ===========================================
    // TODO: (JLB) Should this annotation be moved to ParameterDescription 
    WebParam[] getWebParam() {
        if (webParamAnnotations == null) {
            Annotation[][] paramAnnotation = seiMethod.getParameterAnnotations();
            ArrayList<WebParam> webParamList = new ArrayList<WebParam>();
            for(Annotation[] pa:paramAnnotation){
                for(Annotation webParam:pa){
                    if(webParam.annotationType() == WebParam.class){
                        webParamList.add((WebParam)webParam);
                    }
                }
            }
            webParamAnnotations = webParamList.toArray(new WebParam[0]);
        }
        return webParamAnnotations;
    }
    
    public String[] getWebParamNames() {
        if (webParamNames == null) {
            ArrayList<String> buildNames = new ArrayList<String>();
            WebParam[] webParams = getWebParam();
            for (WebParam currentParam:webParams) {
                // TODO: (JLB) Is skipping param names of "asyncHandler" correct?  This came from original ProxyDescription class and ProxyTest fails without this code
                //       Due to code in DocLitProxyHandler.getParamValues() which does not add values for AsyncHandler objects.
                //       It probably DOES need to be skipped, albeit more robustly (check that the type of the param is javax.xml.ws.AsyncHandler also)
                //       The reason is that the handler is part of the JAX-WS async callback programming model; it is NOT part of the formal params
                //       to the actual method and therefore is NOT part of the JAXB request wrapper
                if(!currentParam.name().equals("asyncHandler")){
                    buildNames.add(currentParam.name());
                }
            }
            webParamNames = buildNames.toArray(new String[0]);
        }
        return webParamNames;
        
    }
    
    // ===========================================
    // WebResult Annotation related methods
    // ===========================================
    WebResult getWebResult() {
        if (webResultAnnotation == null) {
            webResultAnnotation = seiMethod.getAnnotation(WebResult.class);
        }
        return webResultAnnotation;
    }
    public boolean isWebResultAnnotationSpecified() {
        return getWebResult() != null;
    }

    // TODO: (JLB) This method returns null if the annotation is not specified; others return default values.  I think null is the correct thing to return; change the others
    public String getWebResultName() {
        if (isWebResultAnnotationSpecified() && webResultName == null) {
            if (!DescriptionUtils.isEmpty(getWebResult().name())) {
                webResultName = getWebResult().name();
            }
            else {
                // Defeault value is "return" per JSR-181 Sec. 4.5.1, p. 22
                webResultName = "return";
            }
        }
        return webResultName;
    }

    // ===========================================
    // SOAPBinding Annotation related methods
    // ===========================================
    SOAPBinding getSoapBinding() {
        if (soapBindingAnnotation == null) {
            soapBindingAnnotation = seiMethod.getAnnotation(SOAPBinding.class);
        }
        return soapBindingAnnotation;
    }
    
    public javax.jws.soap.SOAPBinding.Style getSoapBindingStyle() {
        if (soapBindingStyle == null) {
            if (getSoapBinding() != null && getSoapBinding().style() != null) {
                soapBindingStyle = getSoapBinding().style();
            }
            else {
                soapBindingStyle = javax.jws.soap.SOAPBinding.Style.DOCUMENT;
            }
        }
        return soapBindingStyle;
    }
    
    // ===========================================
    // OneWay Annotation related methods
    // ===========================================
    Oneway getOnewayAnnotation() {
        if (onewayAnnotation == null) {
            onewayAnnotation = seiMethod.getAnnotation(Oneway.class);
        }
        return onewayAnnotation;
    }

    public boolean isOneWay() {
        if (onewayIsOneway == null) {
            if (getOnewayAnnotation() != null) {
                // The presence of the annotation indicates the method is oneway
                onewayIsOneway = new Boolean(true);
            }
            else {
                // If the annotation is not present, the default is this is NOT a One Way method
                onewayIsOneway = new Boolean(false);
            }
        }
        return onewayIsOneway.booleanValue();   
    }
}
