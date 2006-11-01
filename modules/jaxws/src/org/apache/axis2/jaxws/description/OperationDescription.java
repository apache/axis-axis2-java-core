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
import java.util.Iterator;
import java.util.List;

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

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisOperationFactory;
import org.apache.axis2.description.InOnlyAxisOperation;
import org.apache.axis2.description.InOutAxisOperation;
import org.apache.axis2.description.OutInAxisOperation;
import org.apache.axis2.description.OutOnlyAxisOperation;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.jaxws.description.builder.HandlerChainAnnot;
import org.apache.axis2.jaxws.description.builder.MethodDescriptionComposite;
import org.apache.axis2.jaxws.description.builder.OneWayAnnot;
import org.apache.axis2.jaxws.description.builder.ParameterDescriptionComposite;
import org.apache.axis2.jaxws.description.builder.RequestWrapperAnnot;
import org.apache.axis2.jaxws.description.builder.ResponseWrapperAnnot;
import org.apache.axis2.jaxws.description.builder.SoapBindingAnnot;
import org.apache.axis2.jaxws.description.builder.WebEndpointAnnot;
import org.apache.axis2.jaxws.description.builder.WebMethodAnnot;
import org.apache.axis2.jaxws.description.builder.WebResultAnnot;
import org.apache.axis2.jaxws.description.builder.WebServiceContextAnnot;
import org.apache.axis2.jaxws.description.builder.WebServiceRefAnnot;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.axis2.wsdl.WSDLConstants.WSDL20_2004Constants;
import org.apache.axis2.wsdl.WSDLConstants.WSDL20_2006Constants;

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
    private MethodDescriptionComposite methodComposite;
    private ParameterDescription[] parameterDescriptions;
    private FaultDescription[] faultDescriptions;

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

    OperationDescription(MethodDescriptionComposite mdc, EndpointInterfaceDescription parent) {

    	parentEndpointInterfaceDescription = parent;
		methodComposite = mdc;
        this.operationName = new QName(getWebMethodOperationName());
		webMethodAnnotation = methodComposite.getWebMethodAnnot();

		AxisOperation axisOperation = null;
		
		try {
			if (isOneWay()) {				
				axisOperation = AxisOperationFactory.getOperationDescription(WSDLConstants.WSDL20_2004Constants.MEP_URI_IN_ONLY);
			} else {
				axisOperation = AxisOperationFactory.getOperationDescription(WSDLConstants.WSDL20_2004Constants.MEP_URI_IN_OUT);
			}
			//TODO: There are several other MEP's, such as: OUT_ONLY, IN_OPTIONAL_OUT, OUT_IN, OUT_OPTIONAL_IN, ROBUST_OUT_ONLY,
			//												ROBUST_IN_ONLY
			//      Determine how these MEP's should be handled, if at all
					
		} catch (Exception e) {
			AxisFault ex = new AxisFault("OperationDescription:cons - unable to build AxisOperation ");
		}
		    
		if (axisOperation != null){
			
			axisOperation.setName(determineOperationQName(this.methodComposite));
			axisOperation.setSoapAction(this.getWebMethodAction());

		
			//TODO: Determine other axisOperation values that may need to be set
			//      Currently, the following values are being set on AxisOperation in 
			//      ServiceBuilder.populateService which we are not setting:
			//			AxisOperation.setPolicyInclude()
			//			AxisOperation.setWsamappingList()
			//			AxisOperation.setOutputAction()
			//			AxisOperation.addFaultAction()
			//			AxisOperation.setFaultMessages()
			
			// TODO: The WSMToAxisServiceBuilder sets the message receiver, not sure why this is done
			//       since AxisService.addOperation does this as well by setting it to a default
			//       MessageReceiver...it appears that this code is also setting it to a default
			//       receiver..need to understand this

			/*
			String messageReceiverClass = "org.apache.axis2.rpc.receivers.RPCMessageReceiver";
			if(wsmOperation.isOneWay()){
				messageReceiverClass = "org.apache.axis2.rpc.receivers.RPCInOnlyMessageReceiver";
			}
			try{
				MessageReceiver msgReceiver = (MessageReceiver)Class.forName(messageReceiverClass).newInstance();
				axisOperation.setMessageReceiver(msgReceiver);

			}catch(Exception e){
			}
			*/

			parameterDescriptions = createParameterDescriptions();
            faultDescriptions = createFaultDescriptions();
			
			//TODO: Need to process the other annotations that can exist, on the server side
			//      and at the method level.
			//      They are, as follows: 		
			//			WebResultAnnot (181)
			//			HandlerChain
			//			SoapBinding (181)
			//			WebServiceRefAnnot (List) (JAXWS)
			//			WebServiceContextAnnot (JAXWS via injection)
			//			RequestWrapper (JAXWS)
			//			ResponseWrapper (JAXWS)
			
//System.out.println("OperationDescription: Finished setting operation");
			
		}
		
		this.axisOperation = axisOperation;
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
            faultDescriptions = createFaultDescriptions();
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
    	
    	if (!isDBC()) {
    		if (seiMethod != null) {
                returnString = seiMethod.getName();
    		}
    	} else {
    		if (methodComposite != null) {
    			returnString = methodComposite.getMethodName();
    		}
    	}
        
        return returnString;
    }
    
    public String[] getJavaParameters() {
    	
        ArrayList<String> returnParameters = new ArrayList<String>();
        
        if (!isDBC()) {
            if (seiMethod != null) {
                Class[] paramaters = seiMethod.getParameterTypes();
                for (Class param:paramaters) {
                    returnParameters.add(param.getName());
                }
            }
	
        } 
        else {
        	if (methodComposite != null) {
        		
        		Iterator<ParameterDescriptionComposite> iter = 
        			methodComposite.getParameterDescriptionCompositeList().iterator();
        		while (iter.hasNext()) {
        			returnParameters.add(iter.next().getParameterType());
        		}
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
    
    public MethodDescriptionComposite getMethodDescriptionComposite() {
    	return methodComposite;
    }
    
    private boolean isWrappedParameters() {
        // TODO: WSDL may need to be considered in this check as well
        return getSoapBindingParameterStyle() == javax.jws.soap.SOAPBinding.ParameterStyle.WRAPPED;
    }
    
    private ParameterDescription[] createParameterDescriptions() {
    	
    	ArrayList<ParameterDescription> buildParameterList = new ArrayList<ParameterDescription>();
	
    	if (!isDBC()) {
        	Class[] parameters = seiMethod.getParameterTypes();
        	Type[] paramaterTypes = seiMethod.getGenericParameterTypes();
        	Annotation[][] annotations = seiMethod.getParameterAnnotations();
        	
        	for(int i = 0; i < parameters.length; i++) {
        		ParameterDescription paramDesc = new ParameterDescription(i, parameters[i], paramaterTypes[i], annotations[i], this);
        		buildParameterList.add(paramDesc);
        	}
 	
    	} else {
          	ParameterDescriptionComposite pdc = null;
        	Iterator<ParameterDescriptionComposite> iter = 
        						methodComposite.getParameterDescriptionCompositeList().iterator();
        	
        	for (int i = 0; i < methodComposite.getParameterDescriptionCompositeList().size(); i++) {
           		ParameterDescription paramDesc = 
           						new ParameterDescription (	i, 
           													methodComposite.getParameterDescriptionComposite(i), 
           													this);
        		buildParameterList.add(paramDesc);
    		}
    	}
    	
       	return buildParameterList.toArray(new ParameterDescription[buildParameterList.size()]);
  	
    }

    private FaultDescription[] createFaultDescriptions() {
        
        ArrayList<FaultDescription> buildFaultList = new ArrayList<FaultDescription>();
        
        if (!isDBC()) {
            // get exceptions this method "throws"
            Class[] webFaultClasses = seiMethod.getExceptionTypes();

            for(Class wfClass:webFaultClasses) {
                for (Annotation anno:wfClass.getAnnotations()) {
                    if (anno.annotationType() == WebFault.class) {
                        buildFaultList.add(new FaultDescription(wfClass.getCanonicalName(), ((WebFault)anno).faultBean(), (WebFault)anno, this));
                    }
                }
            }
        } else {
            // TODO do I care about methodComposite like the paramDescription does?
        }
        return buildFaultList.toArray(new FaultDescription[0]);
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
    
    //TODO: For now, we are overriding the above method only because it is static, these should
    //be combined at some point
    static QName determineOperationQName(MethodDescriptionComposite mdc) {
    	return new QName(determineOperationName(mdc));
    }
    
    //TODO: Deprecate this after we use only DBC objects
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

    //TODO: For now, we are overriding the above method only because it is static, these should
    //be combined at some point
    private static String determineOperationName(MethodDescriptionComposite mdc) {
        String operationName = null;
        
        WebMethod wmAnnotation = mdc.getWebMethodAnnot();
   		if (wmAnnotation != null && !DescriptionUtils.isEmpty(wmAnnotation.operationName())) {
			operationName = wmAnnotation.operationName();
		}
		else {
			operationName = mdc.getMethodName();
		}

    	return operationName;
    }
    
    public String getWebMethodOperationName() {
        if (webMethodOperationName == null) {
        	if (!isDBC())
        		webMethodOperationName = determineOperationName(seiMethod);
        	else
                webMethodOperationName = determineOperationName(methodComposite); 		
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
        	if (!isDBC()) {
        		requestWrapperAnnotation = seiMethod.getAnnotation(RequestWrapper.class); 
        	} else {
        		requestWrapperAnnotation = methodComposite.getRequestWrapperAnnot(); 
        	}		
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
            	if (isDBC()) {
            		requestWrapperClassName = this.methodComposite.getDeclaringClass();	
            	} else {
            		Class clazz = seiMethod.getDeclaringClass();
            		String packageName = clazz.getPackage().getName();
            		String className = DescriptionUtils.javaMethodtoClassName(seiMethod.getName());
            		requestWrapperClassName = packageName + "." + className;
            	}
            }
        }
        return requestWrapperClassName;
    }
    
    // ===========================================
    // ANNOTATION: ResponseWrapper
    // ===========================================
    ResponseWrapper getResponseWrapper() {
        if (responseWrapperAnnotation == null) {
        	if (!isDBC()) {
        		responseWrapperAnnotation = seiMethod.getAnnotation(ResponseWrapper.class);
        	} else {
            	responseWrapperAnnotation = methodComposite.getResponseWrapperAnnot();      		
        	}
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
            	if (!isDBC()) {
            		Class clazz = seiMethod.getDeclaringClass();
            		String packageName = clazz.getPackage().getName();
            		String className = DescriptionUtils.javaMethodtoClassName(seiMethod.getName());
            		responseWrapperClassName = packageName + "." + className;
            	} else {
            		responseWrapperClassName = methodComposite.getDeclaringClass();
            	}
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
    
    public FaultDescription[] getFaultDescriptions() {
        return faultDescriptions;
    }
    
    public FaultDescription resolveFaultByFaultBeanName(String faultBeanName) {
        for(FaultDescription fd: faultDescriptions) {
            if (faultBeanName.equals(fd.getBeanName()))
                return fd;
        }
        return null;
    }
    
    public FaultDescription resolveFaultByExceptionName(String exceptionClassName) {
        for(FaultDescription fd: faultDescriptions) {
            if (exceptionClassName.equals(fd.getExceptionClassName()))
                return fd;
        }
        return null;
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
        	if (!isDBC()) {
        		webResultAnnotation = seiMethod.getAnnotation(WebResult.class);
        	} else {
                webResultAnnotation = methodComposite.getWebResultAnnot();
        	}
        	
        }
        return webResultAnnotation;
    }
    
    public boolean isWebResultAnnotationSpecified() {
        return getWebResult() != null;
    }

    public boolean isOperationReturningResult() {
    	boolean isResult = false;
    	if (!isOneWay()) {
    		if (!isDBC()) {
    			if (seiMethod.getReturnType() != Void.TYPE) {
    				isResult = true;
    			}
    		} else {
    			if (!DescriptionUtils.isEmpty(methodComposite.getReturnType()) &&
    					!methodComposite.getReturnType().equals("void"))
    				isResult = true;
    		}
    	} 
        return isResult;
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
        	if (!isDBC()) {
        		soapBindingAnnotation = seiMethod.getAnnotation(SOAPBinding.class);
        	} else {
        		soapBindingAnnotation = methodComposite.getSoapBindingAnnot();
        	}
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
    	//TODO: Shouldn't really do it this way...if there is not Oneway annotation, 
    	//      we will always be calling the methods to try to retrieve it, since
    	//      it will always be null, should consider relying on 'isOneWay'
    	
    	if (onewayAnnotation == null) {    		
    		if (isDBC()) {
    			if (methodComposite.isOneWay()) {
    				onewayAnnotation = OneWayAnnot.createOneWayAnnotImpl();
    			}
    		} else
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
    
    private boolean isDBC() {
    	if (methodComposite != null)
    		return true;
    	else
    		return false;
    }
}
