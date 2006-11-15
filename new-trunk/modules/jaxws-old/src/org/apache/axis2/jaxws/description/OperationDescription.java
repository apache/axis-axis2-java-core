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
 * An OperationDescripton corresponds to a method on an SEI.  That SEI could be explicit
 * (i.e. @WebService.endpointInterface=sei.class) or implicit (i.e. public methods on the service implementation
 * are the contract and thus the implicit SEI).  Note that while OperationDescriptions are created on both the client
 * and service side, implicit SEIs will only occur on the service side.
 * 
 * OperationDescriptons contain information that is only relevent for and SEI-based service, i.e. one that is invoked via specific
 * methods.  This class does not exist for Provider-based services (i.e. those that specify @WebServiceProvider)
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

    // ===========================================
    // ANNOTATION related information
    // ===========================================
    
    // ANNOTATION: @Oneway
    private Oneway              onewayAnnotation;
    private Boolean             onewayIsOneway;
    
    // ANNOTATION: @RequestWrapper
    private RequestWrapper      requestWrapperAnnotation;
    private String              requestWrapperTargetNamespace;
    private String              requestWrapperLocalName;
    private String              requestWrapperClassName;
    
    // ANNOTATION: @ResponseWrapper
    private ResponseWrapper     responseWrapperAnnotation;
    private String              responseWrapperLocalName;
    private String              responseWrapperTargetNamespace;
    private String              responseWrapperClassName;
    
    // ANNOTATION: @SOAPBinding
    // Note this is the Method-level annotation.  See EndpointInterfaceDescription for the Method-level annotation
    // Also note this annotation is only allowed on methods if SOAPBinding.Style is DOCUMENT and if the method-level
    // annotation is absent, the behavior defined on the Type is used.
    // per JSR-181 MR Sec 4.7 "Annotation: javax.jws.soap.SOAPBinding" pg 28
    private SOAPBinding         soapBindingAnnotation;
    // REVIEW: Should this be using the jaxws annotation values or should that be wrappered?
    private javax.jws.soap.SOAPBinding.Style            soapBindingStyle;
    public static final javax.jws.soap.SOAPBinding.Style SoapBinding_Style_VALID = javax.jws.soap.SOAPBinding.Style.DOCUMENT;
    private javax.jws.soap.SOAPBinding.Use              soapBindingUse;
    // Default value per JSR-181 MR Sec 4.7 "Annotation: javax.jws.soap.SOAPBinding" pg 28
    public static final javax.jws.soap.SOAPBinding.Use  SOAPBinding_Use_DEFAULT = javax.jws.soap.SOAPBinding.Use.LITERAL;
    private javax.jws.soap.SOAPBinding.ParameterStyle   soapBindingParameterStyle;
    // Default value per JSR-181 MR Sec 4.7 "Annotation: javax.jws.soap.SOAPBinding" pg 28
    public static final javax.jws.soap.SOAPBinding.ParameterStyle SOAPBinding_ParameterStyle_DEFAULT = javax.jws.soap.SOAPBinding.ParameterStyle.WRAPPED;

    
    // ANNOTATION: @WebMethod
    private WebMethod           webMethodAnnotation;
    private String              webMethodOperationName;
    
    // ANNOTATION: @WebParam
    // TODO: Should WebParam annotation be moved to the ParameterDescription?
    private WebParam[]          webParamAnnotations;
    private String[]            webParamNames;
    private String[]            webParamTNS;

    
    // ANNOTATION: @WebResult
    private WebResult           webResultAnnotation;
    private String              webResultName;
    
    OperationDescription(Method method, EndpointInterfaceDescription parent) {
        // TODO: Look for WebMethod anno; get name and action off of it
        parentEndpointInterfaceDescription = parent;
        setSEIMethod(method);
        webMethodAnnotation = seiMethod.getAnnotation(WebMethod.class);
        
        this.operationName = new QName(getWebMethodOperationName());
    }
    OperationDescription(AxisOperation operation, EndpointInterfaceDescription parent) {
        parentEndpointInterfaceDescription = parent;
        axisOperation = operation;
        this.operationName = axisOperation.getName();
    }

    public void setSEIMethod(Method method) {
        if (seiMethod != null)
            // TODO: This is probably an error, but error processing logic is incorrect
            throw new UnsupportedOperationException("Can not set an SEI method once it has been set.");
        else 
            seiMethod = method;
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
        // TODO: This is different than the rest, which return null instead of an empty array
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
    
    private boolean isWrappedParameters() {
        // TODO: WSDL may need to be considered in this check as well
        return getSoapBindingParameterStyle() == javax.jws.soap.SOAPBinding.ParameterStyle.WRAPPED;
    }

    // =====================================
    // ANNOTATION: WebMethod
    // =====================================
    WebMethod getWebMethod() {
        return webMethodAnnotation;
    }
    
    static QName determineOperationQName(Method javaMethod) {
        return new QName(determineOperationName(javaMethod));
    }
    
    private static String determineOperationName(Method javaMethod) {
        String operationName = null;
        WebMethod wmAnnotation = javaMethod.getAnnotation(WebMethod.class);
        // Per JSR-181 MR Sec 4.2 "Annotation: javax.jws.WebMethod" pg 17,
        // if @WebMethod specifies and operation name, use that.  Otherwise
        // default is the Java method name
        if (wmAnnotation != null && !DescriptionUtils.isEmpty(wmAnnotation.operationName())) {
            operationName = wmAnnotation.operationName();
        }
        else {
            operationName = javaMethod.getName();
        }
        return operationName;
        
    }
    
    public String getWebMethodOperationName() {
        if (webMethodOperationName == null) {
            webMethodOperationName = determineOperationName(seiMethod);
        }
        return webMethodOperationName;
    }
    
    // ==========================================
    // ANNOTATION: RequestWrapper
    // ==========================================
    RequestWrapper getRequestWrapper() {
        if (requestWrapperAnnotation == null) {
            requestWrapperAnnotation = seiMethod.getAnnotation(RequestWrapper.class); 
        }
        return requestWrapperAnnotation;
    }
    
    /**
     * For wrapped parameter style (based on the annotation and the WSDL), returns the 
     * wrapper value.  For non-wrapped (i.e. bare) parameter style, returns null.
     * @return
     */
    public String getRequestWrapperLocalName() {
        if (!isWrappedParameters()) {
            // A wrapper is only meaningful for wrapped parameters
            return null;
        }
        if (requestWrapperLocalName == null) {
            if (getRequestWrapper() != null
                    && !DescriptionUtils.isEmpty(getRequestWrapper().localName())) {
                requestWrapperLocalName = getRequestWrapper().localName();
            } else {
                // The default value of localName is the value of operationName as
                // defined in the WebMethod annotation. [JAX-WS Sec. 7.3, p. 80]
                requestWrapperLocalName = getWebMethodOperationName();
            }
        }
        return requestWrapperLocalName;
    }
    
    /**
     * For wrapped parameter style (based on the annotation and the WSDL), returns the 
     * wrapper value.  For non-wrapped (i.e. bare) parameter style, returns null.
     * @return
     */
    public String getRequestWrapperTargetNamespace() {
        if (!isWrappedParameters()) {
            // A wrapper is only meaningful for wrapped parameters
            return null;
        }
        if (requestWrapperTargetNamespace == null) {
            if (getRequestWrapper() != null && !DescriptionUtils.isEmpty(getRequestWrapper().targetNamespace())) {
                requestWrapperTargetNamespace = getRequestWrapper().targetNamespace();
            }
            else {
                // The default value for targetNamespace is the target namespace of the SEI. [JAX-WS Sec 7.3, p. 80]
                // TODO: Get the TNS from the SEI via the endpoint interface desc.
                throw new UnsupportedOperationException("RequestWrapper.targetNamespace default not implented yet");
            }
        }
        return requestWrapperTargetNamespace;
    }
    
    /**
     * For wrapped parameter style (based on the annotation and the WSDL), returns the 
     * wrapper value.  For non-wrapped (i.e. bare) parameter style, returns null.
     * @return
     */
    public String getRequestWrapperClassName() {
        if (!isWrappedParameters()) {
            // A wrapper is only meaningful for wrapped parameters
            return null;
        }
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
    // ANNOTATION: ResponseWrapper
    // ===========================================
    ResponseWrapper getResponseWrapper() {
        if (responseWrapperAnnotation == null) {
            responseWrapperAnnotation = seiMethod.getAnnotation(ResponseWrapper.class);
        }
        return responseWrapperAnnotation;
    }
    
    /**
     * For wrapped parameter style (based on the annotation and the WSDL), returns the 
     * wrapper value.  For non-wrapped (i.e. bare) parameter style, returns null.
     * @return
     */
    public String getResponseWrapperLocalName() {
        if (!isWrappedParameters()) {
            // A wrapper is only meaningful for wrapped parameters
            return null;
        }
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
    
    /**
     * For wrapped parameter style (based on the annotation and the WSDL), returns the 
     * wrapper value.  For non-wrapped (i.e. bare) parameter style, returns null.
     * @return
     */
    public String getResponseWrapperTargetNamespace() {
        if (!isWrappedParameters()) {
            // A wrapper is only meaningful for wrapped parameters
            return null;
        }
        if (responseWrapperTargetNamespace == null) {
            if (getResponseWrapper() != null && !DescriptionUtils.isEmpty(getResponseWrapper().targetNamespace())) {
                responseWrapperTargetNamespace = getResponseWrapper().targetNamespace();
            }
            else {
                // The default value for targetNamespace is the target namespace of the SEI. [JAX-WS Sec 7.4, p. 81]
                // TODO: Get the TNS from the SEI via the endpoint interface desc.
                throw new UnsupportedOperationException("ResponseWrapper.targetNamespace default not implented yet");
            }
        }
        return responseWrapperTargetNamespace;
    }
    
    /**
     * For wrapped parameter style (based on the annotation and the WSDL), returns the 
     * wrapper value.  For non-wrapped (i.e. bare) parameter style, returns null.
     * @return
     */
    public String getResponseWrapperClassName() {
        if (!isWrappedParameters()) {
            // A wrapper is only meaningful for wrapped parameters
            return null;
        }
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
    // ANNOTATION: WebParam
    // ===========================================
    // TODO: Should this annotation be moved to ParameterDescription 
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
                // TODO: Is skipping param names of "asyncHandler" correct?  This came from original ProxyDescription class and ProxyTest fails without this code
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
    
    public String[] getWebParamTNS(){
        if (webParamTNS == null) {
            ArrayList<String> buildNames = new ArrayList<String>();
            WebParam[] webParams = getWebParam();
            for (WebParam currentParam:webParams) {
                // TODO: Is skipping param names of "asyncHandler" correct?  This came from original ProxyDescription class and ProxyTest fails without this code
                //       Due to code in DocLitProxyHandler.getParamValues() which does not add values for AsyncHandler objects.
                //       It probably DOES need to be skipped, albeit more robustly (check that the type of the param is javax.xml.ws.AsyncHandler also)
                //       The reason is that the handler is part of the JAX-WS async callback programming model; it is NOT part of the formal params
                //       to the actual method and therefore is NOT part of the JAXB request wrapper
                if(!currentParam.name().equals("asyncHandler")){
                    buildNames.add(currentParam.targetNamespace());
                }
            }
            webParamTNS = buildNames.toArray(new String[0]);
        }
        return webParamTNS;
    }
             
    public String getWebParamTNS(String name){
       WebParam[] webParams = getWebParam();
       for (WebParam currentParam:webParams){
           if(currentParam.name().equals(name)){
                return currentParam.targetNamespace();
            }
        }
        return null;    
     }
    
    // ===========================================
    // ANNOTATION: WebResult
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

    // TODO: This method returns null if the annotation is not specified; others return default values.  I think null is the correct thing to return; change the others
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
    // ANNOTATION: SOAPBinding
    // ===========================================
    SOAPBinding getSoapBinding() {
        // TODO: VALIDATION: Only style of DOCUMENT allowed on Method annotation; remember to check the Type's style setting also
        //       JSR-181 Sec 4.7 p. 28
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
                // Per JSR-181 MR Sec 4.7, pg 28: if not specified, use the Type value.
                soapBindingStyle = getEndpointInterfaceDescription().getSoapBindingStyle(); 
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
                // Per JSR-181 MR Sec 4.7, pg 28: if not specified, use the Type value.
                soapBindingUse = getEndpointInterfaceDescription().getSoapBindingUse(); 
            }
        }
        return soapBindingUse;
    }

    public javax.jws.soap.SOAPBinding.ParameterStyle getSoapBindingParameterStyle() {
        if (soapBindingParameterStyle == null) {
            if (getSoapBinding() != null && getSoapBinding().use() != null) {
                soapBindingParameterStyle = getSoapBinding().parameterStyle();
            }
            else {
                // Per JSR-181 MR Sec 4.7, pg 28: if not specified, use the Type value.
                soapBindingParameterStyle = getEndpointInterfaceDescription().getSoapBindingParameterStyle(); 
            }
        }
        return soapBindingParameterStyle;
    }
    
    // ===========================================
    // ANNOTATION: OneWay
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
