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
import java.lang.reflect.Type;
import java.util.ArrayList;

import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebParam.Mode;
import javax.jws.soap.SOAPBinding;
import javax.xml.namespace.QName;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import javax.xml.ws.WebFault;

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
    private ParameterDescription[] parameterDescriptions;

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
    // Note this is the Method-level annotation.  See EndpointInterfaceDescription for the Type-level annotation
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
    // Default value per JSR-181 MR Sec 4.2, pg 17
    public static final String  WebMethod_Action_DEFAULT = "";
    private String              webMethodAction;
    // Default value per JSR-181 MR sec 4.2, pg 17
    public static final Boolean WebMethod_Exclude_DEFAULT = new Boolean(false);
    private Boolean             webMethodExclude;
    
    // ANNOTATION: @WebParam
    // TODO: Should WebParam annotation be moved to the ParameterDescription?
    private WebParam[]          webParamAnnotations;
    private String[]            webParamNames;
    private Mode[]				webParamMode;
    private String[]            webParamTargetNamespace;

    
    // ANNOTATION: @WebResult
    private WebResult           webResultAnnotation;
    private String              webResultName;
    private String              webResultPartName;
    // Default value per JSR-181 MR Sec 4.5.1, pg 23
    public static final String  WebResult_TargetNamespace_DEFAULT = "";
    private String              webResultTargetNamespace;
    // Default value per JSR-181 MR sec 4.5, pg 24
    public static final Boolean WebResult_Header_DEFAULT = new Boolean(false);
    private Boolean             webResultHeader;

    // ANNOTATION @WebFault
    private WebFault[]          webFaultAnnotations;
    private String[]            webFaultNames;
    private String[]            webExceptionNames;  // the fully-qualified names of declared exceptions with WebFault annotations

    OperationDescription(Method method, EndpointInterfaceDescription parent) {
        // TODO: Look for WebMethod anno; get name and action off of it
        parentEndpointInterfaceDescription = parent;
        setSEIMethod(method);

        
        this.operationName = new QName(getWebMethodOperationName());
    }
    OperationDescription(AxisOperation operation, EndpointInterfaceDescription parent) {
        parentEndpointInterfaceDescription = parent;
        axisOperation = operation;
        this.operationName = axisOperation.getName();
    }

    public void setSEIMethod(Method method) {
        if (seiMethod != null) {
            // TODO: This is probably an error, but error processing logic is incorrect
            throw new UnsupportedOperationException("Can not set an SEI method once it has been set.");
        }
        else  {
            seiMethod = method;
            webMethodAnnotation = seiMethod.getAnnotation(WebMethod.class);
            parameterDescriptions = createParameterDescriptions();
        }
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
    
    private ParameterDescription[] createParameterDescriptions() {
       Class[] parameters = seiMethod.getParameterTypes();
       Type[] paramaterTypes = seiMethod.getGenericParameterTypes();
       Annotation[][] annotations = seiMethod.getParameterAnnotations();
       ArrayList<ParameterDescription> buildParameterList = new ArrayList<ParameterDescription>();
       for(int i = 0; i < parameters.length; i++) {
           ParameterDescription paramDesc = new ParameterDescription(i, parameters[i], paramaterTypes[i], annotations[i], this);
           buildParameterList.add(paramDesc);
       }
       return buildParameterList.toArray(new ParameterDescription[buildParameterList.size()]);
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
    
    public String getWebMethodAction() {
        if (webMethodAction == null) {
            if (getWebMethod() != null && !DescriptionUtils.isEmpty(getWebMethod().action())) {
                webMethodAction = getWebMethod().action();
            }
            else {
                webMethodAction = WebMethod_Action_DEFAULT;
            }
        }
        return webMethodAction;
    }
    
    public boolean getWebMethodExclude() {
        if (webMethodExclude == null) {
            // TODO: Validation: if this attribute specified, no other elements allowed per JSR-181 MR Sec 4.2, pg 17
            // TODO: Validation: This element is not allowed on endpoint interfaces
            // Unlike the elements with a String value, if the annotation is present, exclude will always 
            // return a usable value since it will default to FALSE if the element is not present.
            if (getWebMethod() != null) {
                webMethodExclude = new Boolean(getWebMethod().exclude());
            }
            else {
                webMethodExclude = WebMethod_Exclude_DEFAULT;
            }
        }
        
        return webMethodExclude.booleanValue();
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
                // TODO: Implement getting the TNS from the SEI 
//                requestWrapperTargetNamespace = getEndpointInterfaceDescription().getWebServiceTargetNamespace();
                throw new UnsupportedOperationException("RequestWrapper.targetNamespace default not implented yet");            }
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
                // The default value for targetNamespace is the target namespace of the SEI. [JAX-WS Sec 7.3, p. 80]
                // TODO: Implement getting the TNS from the SEI 
//                responseWrapperTargetNamespace = getEndpointInterfaceDescription().getWebServiceTargetNamespace();
                throw new UnsupportedOperationException("RequestWrapper.targetNamespace default not implented yet");
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
    // ANNOTATION: WebFault
    // ===========================================

    /*
     * TODO some of the WebFault stuff should be moved to FaultDescription
     */
    
    /*
     *  TODO:  this will need revisited.  The problem is that a WebFault is not mapped 1:1 to an
     *  OperationDescription.  We should do a better job caching the information.  For now, I'm
     *  following the getWebParam() pattern.
     *  
     *  This is gonna get complicated.  One other thing to consider is that a method (opdesc) may declare
     *  several types of exceptions it throws
     *  
     */
    private WebFault[] getWebResponseFaults() {
        if (webFaultAnnotations == null) {
        	Class[] webFaultClasses = seiMethod.getExceptionTypes();

        	ArrayList<WebFault> webFaultList = new ArrayList<WebFault>();
            for(Class wfClass:webFaultClasses) {
            	for (Annotation anno:wfClass.getAnnotations()) {
            		if (anno.annotationType() == WebFault.class) {
            			webFaultList.add((WebFault)anno);
            		}
            	}
            }
            webFaultAnnotations = webFaultList.toArray(new WebFault[0]);
        }
        return webFaultAnnotations;
    }

    /*
     * TODO:  also will need revisited upon the re-working of getResponseFaults()
     */
    private String[] getWebFaultClassNames() {
        if (webFaultNames == null) {
        	// get exceptions this method "throws"
        	Class[] webFaultClasses = seiMethod.getExceptionTypes();

        	ArrayList<String> webFaultList = new ArrayList<String>();
            for(Class wfClass:webFaultClasses) {
            	for (Annotation anno:wfClass.getAnnotations()) {
            		if (anno.annotationType() == WebFault.class) {
            			webFaultList.add(((WebFault)anno).faultBean());
            		}
            	}
            }
            webFaultNames = webFaultList.toArray(new String[0]);
        }
        return webFaultNames;
    }
    
    /*
     * TODO:  also will need revisited upon the re-working of getResponseFaults()
     */
    private String[] getWebExceptionClassNames() {
        if (webExceptionNames == null) {
        	// get exceptions this method "throws"
        	Class[] webFaultClasses = seiMethod.getExceptionTypes();

        	ArrayList<String> webFaultList = new ArrayList<String>();
            for(Class wfClass:webFaultClasses) {
            	for (Annotation anno:wfClass.getAnnotations()) {
            		if (anno.annotationType() == WebFault.class) {
            			webFaultList.add(wfClass.getCanonicalName());
            		}
            	}
            }
            webExceptionNames = webFaultList.toArray(new String[0]);
        }
        return webExceptionNames;
    }
    
    public String getWebFaultClassName() {
    	// TODO will need to pass in the exception class to compare with the names???
    	return getWebFaultClassNames()[0];
    }
    
    public String getWebExceptionClassName() {
    	// TODO will need to pass in the fault detail child element name (as a string) to
    	// compare with the WebFault of the declared exceptions
    	return getWebExceptionClassNames()[0];
    }
    // ===========================================
    // ANNOTATION: WebParam
    // ===========================================
    // Note that this annotation is handled by the ParameterDescripton.
    // Methods are provided on OperationDescription as convenience methods.
    public ParameterDescription[] getParameterDescriptions() {
        return parameterDescriptions;
    }
    
    public ParameterDescription getParameterDescription(String parameterName) {
        // TODO: Validation: For BARE paramaterUse, only a single IN our INOUT paramater and a single output (either return or OUT or INOUT) is allowed 
        //       Per JSR-224, Sec 3.6.2.2, pg 37
        ParameterDescription matchingParamDesc = null;
        if (parameterName != null && !parameterName.equals("")) {
            for (ParameterDescription paramDesc:parameterDescriptions) {
                if (parameterName.equals(paramDesc.getWebParamName())) {
                    matchingParamDesc = paramDesc;
                    break;
                }
            }
        }
        return matchingParamDesc;
    }
    
    public ParameterDescription getParameterDescription(int parameterNumber) {
        return parameterDescriptions[parameterNumber];
    }
    
    public String[] getWebParamNames() {
        if (webParamNames == null) {
            ArrayList<String> buildNames = new ArrayList<String>();
            ParameterDescription[] paramDescs = getParameterDescriptions();
            for (ParameterDescription currentParamDesc:paramDescs) {
                buildNames.add(currentParamDesc.getWebParamName());
            }
            webParamNames = buildNames.toArray(new String[0]);
        }
        return webParamNames;
    }
    
    public String[] getWebParamTargetNamespaces(){
        if (webParamTargetNamespace == null) {
            ArrayList<String> buildTargetNS = new ArrayList<String>();
            ParameterDescription[] paramDescs = getParameterDescriptions();
            for (ParameterDescription currentParamDesc:paramDescs) {
                buildTargetNS.add(currentParamDesc.getWebParamTargetNamespace());
            }
            webParamTargetNamespace = buildTargetNS.toArray(new String[0]);
        }
        return webParamTargetNamespace;
    }

    public String getWebParamTargetNamespace(String name){
        String returnTargetNS = null;
        ParameterDescription paramDesc = getParameterDescription(name);
        if (paramDesc != null) {
            returnTargetNS = paramDesc.getWebParamTargetNamespace();
        }
        return returnTargetNS;
    }
    
             
    public Mode[] getWebParamModes(){
    	if(webParamMode == null){
    		ArrayList<Mode> buildModes = new ArrayList<Mode>();
            ParameterDescription[] paramDescs = getParameterDescriptions();
            for (ParameterDescription currentParamDesc:paramDescs) {
                buildModes.add(currentParamDesc.getWebParamMode());
            }
    		 webParamMode = buildModes.toArray(new Mode[0]);
    	}
    	return webParamMode;
    }
    public boolean isWebParamHeader(String name){
    	ParameterDescription paramDesc = getParameterDescription(name);
    	if (paramDesc != null) {
            return paramDesc.getWebParamHeader();
        }
        return false;    
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

    public boolean isOperationReturningResult() {
        return !isOneWay() && (seiMethod.getReturnType() != Void.TYPE);
    }

    public String getWebResultName() {
        if (!isOperationReturningResult()) {
            return null;
        }
        if (webResultName == null) {
            if (getWebResult() != null && !DescriptionUtils.isEmpty(getWebResult().name())) {
                webResultName = getWebResult().name();
            }
            else if (getSoapBindingStyle() == SOAPBinding.Style.DOCUMENT
                    && getSoapBindingParameterStyle() == SOAPBinding.ParameterStyle.BARE) {
                // Default for operation style DOCUMENT and paramater style BARE per JSR 181 MR Sec 4.5.1, pg 23
                webResultName = getWebMethodOperationName() + "Response";
                
            }
            else {
                // Defeault value is "return" per JSR-181 MR Sec. 4.5.1, p. 22
                webResultName = "return";
            }
        }
        return webResultName;
    }
    
    public String getWebResultPartName() {
        if (!isOperationReturningResult()) {
            return null;
        }
        if (webResultPartName == null) {
            if (getWebResult() != null && !DescriptionUtils.isEmpty(getWebResult().partName())) {
                webResultPartName = getWebResult().partName();
            }
            else {
                // Default is the WebResult.name per JSR-181 MR Sec 4.5.1, pg 23
                webResultPartName = getWebResultName();
            }
        }
        return webResultPartName;
    }
    
    public String getWebResultTargetNamespace() {
        if (!isOperationReturningResult()) {
            return null;
        }
        if (webResultTargetNamespace == null) {
            if (getWebResult() != null && !DescriptionUtils.isEmpty(getWebResult().targetNamespace())) {
                webResultTargetNamespace = getWebResult().targetNamespace();
            }
            else if (getSoapBindingStyle() == SOAPBinding.Style.DOCUMENT
                    && getSoapBindingParameterStyle() == SOAPBinding.ParameterStyle.WRAPPED
                    && !getWebResultHeader()) {
                // Default for operation style DOCUMENT and paramater style WRAPPED and the return value
                // does not map to a header per JSR-181 MR Sec 4.5.1, pg 23-24
                webResultTargetNamespace = WebResult_TargetNamespace_DEFAULT;
            }
            else {
                // Default is the namespace from the WebService per JSR-181 MR Sec 4.5.1, pg 23-24
                webResultTargetNamespace = getEndpointInterfaceDescription().getEndpointDescription().getWebServiceTargetNamespace();
            }
            
        }
        return webResultTargetNamespace;
    }
    
    public boolean getWebResultHeader() {
        if (!isOperationReturningResult()) {
            return false;
        }
        if (webResultHeader == null) {
            if (getWebResult() != null) {
                // Unlike the elements with a String value, if the annotation is present, exclude will always 
                // return a usable value since it will default to FALSE if the element is not present.
                webResultHeader = new Boolean(getWebResult().header());
            }
            else {
                webResultHeader = WebResult_Header_DEFAULT;
            }
        }
        return webResultHeader.booleanValue();
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
