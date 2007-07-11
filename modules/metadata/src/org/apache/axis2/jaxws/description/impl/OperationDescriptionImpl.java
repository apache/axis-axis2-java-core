/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.axis2.jaxws.description.impl;

import org.apache.axis2.addressing.wsdl.WSDL11ActionHelper;
import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisOperationFactory;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.description.AttachmentDescription;
import org.apache.axis2.jaxws.description.AttachmentType;
import org.apache.axis2.jaxws.description.EndpointDescriptionJava;
import org.apache.axis2.jaxws.description.EndpointInterfaceDescription;
import org.apache.axis2.jaxws.description.FaultDescription;
import org.apache.axis2.jaxws.description.OperationDescription;
import org.apache.axis2.jaxws.description.OperationDescriptionJava;
import org.apache.axis2.jaxws.description.OperationDescriptionWSDL;
import org.apache.axis2.jaxws.description.OperationRuntimeDescription;
import org.apache.axis2.jaxws.description.ParameterDescription;
import org.apache.axis2.jaxws.description.ParameterDescriptionJava;
import org.apache.axis2.jaxws.description.builder.DescriptionBuilderComposite;
import org.apache.axis2.jaxws.description.builder.MethodDescriptionComposite;
import org.apache.axis2.jaxws.description.builder.OneWayAnnot;
import org.apache.axis2.jaxws.description.builder.ParameterDescriptionComposite;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebParam.Mode;
import javax.jws.soap.SOAPBinding;
import javax.wsdl.Binding;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.extensions.AttributeExtensible;
import javax.xml.bind.annotation.XmlList;
import javax.xml.namespace.QName;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.Response;
import javax.xml.ws.ResponseWrapper;
import javax.xml.ws.WebFault;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Future;

/** @see ../OperationDescription */
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
class OperationDescriptionImpl
        implements OperationDescription, OperationDescriptionJava, OperationDescriptionWSDL {
    private EndpointInterfaceDescription parentEndpointInterfaceDescription;
    private AxisOperation axisOperation;
    private QName operationQName;
    private Method seiMethod;
    private MethodDescriptionComposite methodComposite;
    private ParameterDescription[] parameterDescriptions;
    private FaultDescription[] faultDescriptions;
    private static final Log log = LogFactory.getLog(OperationDescriptionImpl.class);
    // ===========================================
    // ANNOTATION related information
    // ===========================================

    // ANNOTATION: @Oneway
    private Oneway onewayAnnotation;
    private Boolean onewayIsOneway;

    // ANNOTATION: @XmlList
    private boolean 			isListType = false;
    
    // ANNOTATION: @RequestWrapper
    private RequestWrapper requestWrapperAnnotation;
    private String requestWrapperTargetNamespace;
    private String requestWrapperLocalName;
    private String requestWrapperClassName;

    // ANNOTATION: @ResponseWrapper
    private ResponseWrapper responseWrapperAnnotation;
    private String responseWrapperLocalName;
    private String responseWrapperTargetNamespace;
    private String responseWrapperClassName;

    // ANNOTATION: @SOAPBinding
    // Note this is the Method-level annotation.  See EndpointInterfaceDescription for the Type-level annotation
    // Also note this annotation is only allowed on methods if SOAPBinding.Style is DOCUMENT and if the method-level
    // annotation is absent, the behavior defined on the Type is used.
    // per JSR-181 MR Sec 4.7 "Annotation: javax.jws.soap.SOAPBinding" pg 28
    private SOAPBinding soapBindingAnnotation;
    // REVIEW: Should this be using the jaxws annotation values or should that be wrappered?
    private javax.jws.soap.SOAPBinding.Style soapBindingStyle;
    public static final javax.jws.soap.SOAPBinding.Style SoapBinding_Style_VALID =
            javax.jws.soap.SOAPBinding.Style.DOCUMENT;
    private javax.jws.soap.SOAPBinding.Use soapBindingUse;
    // Default value per JSR-181 MR Sec 4.7 "Annotation: javax.jws.soap.SOAPBinding" pg 28
    public static final javax.jws.soap.SOAPBinding.Use SOAPBinding_Use_DEFAULT =
            javax.jws.soap.SOAPBinding.Use.LITERAL;
    private javax.jws.soap.SOAPBinding.ParameterStyle soapBindingParameterStyle;
    // Default value per JSR-181 MR Sec 4.7 "Annotation: javax.jws.soap.SOAPBinding" pg 28
    public static final javax.jws.soap.SOAPBinding.ParameterStyle SOAPBinding_ParameterStyle_DEFAULT =
            javax.jws.soap.SOAPBinding.ParameterStyle.WRAPPED;

    // ANNOTATION: @WebMethod
    private WebMethod webMethodAnnotation;
    private String webMethodOperationName;
    // Default value per JSR-181 MR Sec 4.2, pg 17
    public static final String WebMethod_Action_DEFAULT = "";
    private String webMethodAction;
    // Default value per JSR-181 MR sec 4.2, pg 17
    public static final Boolean WebMethod_Exclude_DEFAULT = new Boolean(false);
    private Boolean webMethodExclude;

    // ANNOTATION: @WebParam
    private String[] webParamNames;
    private Mode[] webParamMode;
    private String[] webParamTargetNamespace;


    // ANNOTATION: @WebResult
    private WebResult webResultAnnotation;
    private String webResultName;
    private String webResultPartName;
    // Default value per JSR-181 MR Sec 4.5.1, pg 23
    public static final String WebResult_TargetNamespace_DEFAULT = "";
    private String webResultTargetNamespace;
    // Default value per JSR-181 MR sec 4.5, pg 24
    public static final Boolean WebResult_Header_DEFAULT = new Boolean(false);
    private Boolean webResultHeader;
    
    //  Web Result Attachment Description information
    private boolean             _setAttachmentDesc = false;
    private AttachmentDescription attachmentDesc = null;
    
    private Method serviceImplMethod;
    private boolean serviceImplMethodFound = false;
    // For JAX-WS client async methods, this is the corresponding Sync method; for everything else,
    // this is "this".
    private OperationDescription syncOperationDescription = null;
    // RUNTIME INFORMATION
    Map<String, OperationRuntimeDescription> runtimeDescMap =
            Collections.synchronizedMap(new HashMap<String, OperationRuntimeDescription>());
    private Map<String, AttachmentDescription> partAttachmentMap;
    
    OperationDescriptionImpl(Method method, EndpointInterfaceDescription parent) {
        // TODO: Look for WebMethod anno; get name and action off of it
        parentEndpointInterfaceDescription = parent;
        partAttachmentMap = new HashMap<String, AttachmentDescription>();
        setSEIMethod(method);
		checkForXmlListAnnotation(method.getAnnotations());
        // The operationQName is intentionally unqualified to be consistent with the remaining parts of the system. 
        // Using a qualified name will cause breakage.
        // Don't do --> this.operationQName = new QName(parent.getTargetNamespace(), getOperationName());
        this.operationQName = new QName("", getOperationName());
        if (getEndpointInterfaceDescription().getEndpointDescription() != null) {
            if (!getEndpointInterfaceDescription().getEndpointDescription().getServiceDescription().isServerSide()) {
                axisOperation = createClientAxisOperation();
            }
        }
    }

    OperationDescriptionImpl(AxisOperation operation, EndpointInterfaceDescription parent) {
        parentEndpointInterfaceDescription = parent;
        partAttachmentMap = new HashMap<String, AttachmentDescription>();
        axisOperation = operation;
        this.operationQName = axisOperation.getName();
    }

    OperationDescriptionImpl(MethodDescriptionComposite mdc,
                             EndpointInterfaceDescription parent,
                             AxisOperation axisOperation) {

        parentEndpointInterfaceDescription = parent;
        partAttachmentMap = new HashMap<String, AttachmentDescription>();
        methodComposite = mdc;
        // The operationQName is intentionally unqualified to be consistent with the remaining parts of the system. 
        // Using a qualified name will cause breakage.
        // Don't do --> this.operationQName = new QName(parent.getTargetNamespace(), getOperationName());
        this.operationQName = new QName("", getOperationName());

        webMethodAnnotation = methodComposite.getWebMethodAnnot();

        parameterDescriptions = createParameterDescriptions();
        faultDescriptions = createFaultDescriptions();
		isListType = mdc.isListType();
        buildAttachmentInformation();

        //If an AxisOperation was already created for us by populateService then just use that one
        //Otherwise, create it
        if (axisOperation != null) {
            this.axisOperation = axisOperation;
        } else {
            this.axisOperation = createAxisOperation();
        }
    }

    /**
     * Create an AxisOperation for this Operation.  Note that the ParameterDescriptions must
     * be created before calling this method since, for a DOC/LIT/BARE (aka UNWRAPPED) message, the 
     * ParamaterDescription is used to setup the AxisMessage correctly for use in SOAP Body-based
     * dispatching on incoming DOC/LIT/BARE messages.
     */
    private AxisOperation createClientAxisOperation() {
        AxisOperation newAxisOperation = null;
        try {
            if (isOneWay()) {
                newAxisOperation =
                        AxisOperationFactory.getOperationDescription(WSDL2Constants.MEP_URI_OUT_ONLY);
            } else {
                newAxisOperation =
                        AxisOperationFactory.getOperationDescription(WSDL2Constants.MEP_URI_OUT_IN);
            }
            //TODO: There are several other MEP's, such as: OUT_ONLY, IN_OPTIONAL_OUT, OUT_IN, OUT_OPTIONAL_IN, ROBUST_OUT_ONLY,
            //                                              ROBUST_IN_ONLY
            //      Determine how these MEP's should be handled, if at all
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Unable to build AxisOperation for OperationDescrition; caught exception.",
                          e);
            }
            // TODO: NLS & RAS
            throw ExceptionFactory.makeWebServiceException("Caught exception trying to create AxisOperation",
                                                           e);
        }

        newAxisOperation.setName(determineOperationQName(seiMethod));
        
        getEndpointInterfaceDescriptionImpl().getEndpointDescriptionImpl().getAxisService().addOperation(newAxisOperation);
        
        return newAxisOperation;
    }
    
    /**
     * Create an AxisOperation for this Operation.  Note that the ParameterDescriptions must
     * be created before calling this method since, for a DOC/LIT/BARE (aka UNWRAPPED) message, the 
     * ParamaterDescription is used to setup the AxisMessage correctly for use in SOAP Body-based
     * dispatching on incoming DOC/LIT/BARE messages.
     */
    private AxisOperation createAxisOperation() {
        AxisOperation newAxisOperation = null;
        try {
            if (isOneWay()) {
                newAxisOperation = AxisOperationFactory
                        .getOperationDescription(WSDL2Constants.MEP_URI_IN_ONLY);
            } else {
                newAxisOperation =
                        AxisOperationFactory.getOperationDescription(WSDL2Constants.MEP_URI_IN_OUT);
            }
            //TODO: There are several other MEP's, such as: OUT_ONLY, IN_OPTIONAL_OUT, OUT_IN, OUT_OPTIONAL_IN, ROBUST_OUT_ONLY,
            //                                              ROBUST_IN_ONLY
            //      Determine how these MEP's should be handled, if at all
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug(
                        "Unable to build AxisOperation for OperationDescrition; caught exception.",
                        e);
            }
            // TODO: NLS & RAS
            throw ExceptionFactory
                    .makeWebServiceException("Caught exception trying to create AxisOperation", e);
        }

        newAxisOperation.setName(determineOperationQName(this.methodComposite));
        newAxisOperation.setSoapAction(this.getAction());

        //*************************************************************************************
        //NOTE: assumption here is that all info. need to generate the actions will have to come
        //      from annotations (or default values)
        //*************************************************************************************
        
        String messageExchangePattern = newAxisOperation.getMessageExchangePattern();
        String targetNS = getEndpointInterfaceDescriptionImpl().getTargetNamespace();        
        String portTypeName = getEndpointInterfaceDescriptionImpl().getEndpointDescriptionImpl().getName();
        ArrayList inputActions = new ArrayList();
         
        //We don't have a name at this point, shouldn't matter if we have the MEP
        //String inputName = newAxisOperation.getName().getLocalPart();
        String inputName = null;
        String inputAction = 
                WSDL11ActionHelper.getInputActionFromStringInformation(messageExchangePattern, 
                                                                       targetNS, 
                                                                       portTypeName, 
                                                                       newAxisOperation.getName().getLocalPart(), 
                                                                       inputName);
                
        if (inputAction != null) {
            inputActions.add(inputAction);
            newAxisOperation.setWsamappingList(inputActions);
        }
        
        //Map the action to the operation on the actual axisService
        //TODO: Determine whether this should be done at a higher level in the 
        //      description hierarchy
        getEndpointInterfaceDescriptionImpl().getEndpointDescriptionImpl().
            getAxisService().mapActionToOperation(inputAction, newAxisOperation);
        
        //set the OUTPUT ACTION

        //We don't have a name at this point, shouldn't matter if we have the MEP
        //String outputName = newAxisOperation.getName().getLocalPart();  //REVIEW:
        String outputName = null;
        String outputAction = 
                WSDL11ActionHelper.getOutputActionFromStringInformation(messageExchangePattern,
                                                                        targetNS, 
                                                                        portTypeName, 
                                                                        newAxisOperation.getName().getLocalPart(), 
                                                                        outputName);
        
        if (outputAction != null) {
                newAxisOperation.setOutputAction(outputAction);
        }
        
        //Set the FAULT ACTION
        // Walk the fault information
        FaultDescription[] faultDescs = getFaultDescriptions();
        if (faultDescs != null) {
            for (int i=0; i <faultDescs.length; i++) {
        
                AxisMessage faultMessage = new AxisMessage();
                String faultName = faultDescs[i].getName();
                faultMessage.setName(faultName);
                
                String faultAction = 
                        WSDL11ActionHelper.getFaultActionFromStringInformation( messageExchangePattern, 
                                        portTypeName, 
                                        newAxisOperation.getName().getLocalPart(), 
                                        faultMessage.getName());
                
                if (faultAction != null) {
                        newAxisOperation.addFaultAction(faultMessage.getName(), faultAction);
                }
                newAxisOperation.setFaultMessages(faultMessage);
            }
        }

        //REVIEW: Determine if other axisOperation values may need to be set
        //      Currently, the following values are being set on AxisOperation in 
        //      ServiceBuilder.populateService which we are not setting:
        //          AxisOperation.setPolicyInclude()
        //          AxisOperation.setWsamappingList()
        //          AxisOperation.setOutputAction()
        //          AxisOperation.addFaultAction()
        //          AxisOperation.setFaultMessages()

        // If this is a DOC/LIT/BARE operation, then set the QName of the input AxisMessage to the 
        // part for the first IN or IN/OUT non-header parameter.  If there are no parameters, then don't set
        // anything.  The AxisMessage name is used to do SOAP-body based routing of DOC/LIT/BARE
        // incoming messages.
        if (getSoapBindingStyle() == javax.jws.soap.SOAPBinding.Style.DOCUMENT
                && getSoapBindingUse() == javax.jws.soap.SOAPBinding.Use.LITERAL
                && getSoapBindingParameterStyle() == javax.jws.soap.SOAPBinding.ParameterStyle.BARE)
        {
            ParameterDescription[] paramDescs = getParameterDescriptions();
            if (paramDescs != null && paramDescs.length > 0) {
                for (ParameterDescription paramDesc : paramDescs) {
                    WebParam.Mode paramMode = paramDesc.getMode();
                    if (!paramDesc.isHeader()
                            && (paramMode == WebParam.Mode.IN || paramMode == WebParam.Mode.INOUT))
                    {
                        // We've found the first IN or INOUT non-header parameter, so set the AxisMessage
                        // QName based on this parameter then break out of the loop.
                        AxisMessage axisMessage =
                                newAxisOperation.getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                        String elementName = paramDesc.getParameterName();
                        String partNamespace = paramDesc.getTargetNamespace();
                        if (log.isDebugEnabled()) {
                            log.debug("Setting up annotation based Doc/Lit/Bare operation: " +
                                    newAxisOperation.getName()
                                    + "; axisMessage: " + axisMessage + "; name: "
                                    + elementName + "; partTNS: " + partNamespace);
                        }
                        if (axisMessage == null) {
                            // TODO: RAS & NLS
                            throw ExceptionFactory.makeWebServiceException(
                                    "Could not setup Doc/Lit/Bare operation because input message is null");
                        } else if (DescriptionUtils.isEmpty(partNamespace)) {
                            // TODO: RAS & NLS
                            throw ExceptionFactory.makeWebServiceException(
                                    "Could not setup Doc/Lit/Bare operation because part namespace is empty");
                        } else if (DescriptionUtils.isEmpty(elementName)) {
                            // TODO: RAS & NLS
                            throw ExceptionFactory.makeWebServiceException(
                                    "Could not setup Doc/Lit/Bare operation because name is empty");
                        } else {
                            QName partQName = new QName(partNamespace, elementName);
                            if(log.isDebugEnabled()) {
                                log.debug("Setting AxisMessage element QName for bare mapping: " +
                                        partQName);
                            }
                            axisMessage.setElementQName(partQName);
                        }
                        break;
                    }
                }
            }
        }
        return newAxisOperation;
    }

    /**
     * Adds the AxisOperation corresponding to this OperationDescription to the AxisService if it
     * isn't already there. It also addes the AxisOperation to any other routing mechanisms for
     * that AxisService: - For Doc/Lit/Bare operations it is added to the
     * MessageElementQNameToOperationMapping
     *
     * @param axisService
     */
    void addToAxisService(AxisService axisService) {
        AxisOperation newAxisOperation = getAxisOperation();
        QName axisOpQName = newAxisOperation.getName();
        AxisOperation axisOperation = axisService.getOperation(axisOpQName);
        if (axisOperation == null) {
            axisService.addOperation(newAxisOperation);
            // For a Doc/Lit/Bare operation, we also need to add the element mapping
        }
        if (getSoapBindingStyle() == javax.jws.soap.SOAPBinding.Style.DOCUMENT
                && getSoapBindingUse() == javax.jws.soap.SOAPBinding.Use.LITERAL
                && getSoapBindingParameterStyle() == javax.jws.soap.SOAPBinding.ParameterStyle
                .BARE) {
            AxisMessage axisMessage =
                    null;
            if (axisOperation!=null) {
                axisMessage = axisOperation.getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
            } else {
                axisMessage = newAxisOperation.getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
            }
            if (axisMessage != null) {
                QName elementQName = axisMessage.getElementQName();
                if (!DescriptionUtils.isEmpty(elementQName)) {
                    axisService.addMessageElementQNameToOperationMapping(elementQName,
                            newAxisOperation);
                }
            }
        }
    }

    void setSEIMethod(Method method) {
        if (seiMethod != null) {
            // TODO: This is probably an error, but error processing logic is incorrect
            throw new UnsupportedOperationException(
                    "Can not set an SEI method once it has been set.");
        } else {
            seiMethod = method;
            webMethodAnnotation = seiMethod.getAnnotation(WebMethod.class);
            parameterDescriptions = createParameterDescriptions();
            faultDescriptions = createFaultDescriptions();
        }
    }

    public EndpointInterfaceDescription getEndpointInterfaceDescription() {
        return parentEndpointInterfaceDescription;
    }

    public EndpointInterfaceDescriptionImpl getEndpointInterfaceDescriptionImpl() {
        return (EndpointInterfaceDescriptionImpl)parentEndpointInterfaceDescription;
    }

    public AxisOperation getAxisOperation() {
        // Note that only the sync operations, and not the JAX-WS async client versions of an 
        // operation, will have an AxisOperation associated with it.  For those async operations, 
        // get the AxisOperation associated with the sync method and return that.
        if (axisOperation == null) {
            OperationDescription opDesc = getSyncOperation();
            if (opDesc != null && opDesc != this) {
                return getSyncOperation().getAxisOperation();
            }
        } 
        
        return axisOperation;
    }

    public QName getName() {
        return operationQName;
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

    public String getJavaDeclaringClassName() {
        if (!isDBC() && seiMethod != null) {
            Class clazz = seiMethod.getDeclaringClass();
            return clazz.getCanonicalName();
        } else if (methodComposite != null) {
            return methodComposite.getDeclaringClass();
        }
        return null;
    }

    public String[] getJavaParameters() {

        ArrayList<String> returnParameters = new ArrayList<String>();

        if (!isDBC()) {
            if (seiMethod != null) {
                Class[] paramaters = seiMethod.getParameterTypes();
                for (Class param : paramaters) {
                    returnParameters.add(param.getName());
                }
            }

        } else {
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
     * Note this will return NULL unless the operation was built via introspection on the SEI. In
     * other words, it will return null if the operation was built with WSDL.
     *
     * @return
     */
    public Method getSEIMethod() {
        return seiMethod;
    }

    MethodDescriptionComposite getMethodDescriptionComposite() {
        return methodComposite;
    }

    private boolean isWrappedParameters() {
        return getSoapBindingParameterStyle() == javax.jws.soap.SOAPBinding.ParameterStyle.WRAPPED;
    }

    private ParameterDescription[] createParameterDescriptions() {

        ArrayList<ParameterDescription> buildParameterList = new ArrayList<ParameterDescription>();

        if (!isDBC()) {
            Class[] parameters = seiMethod.getParameterTypes();
            Type[] paramaterTypes = seiMethod.getGenericParameterTypes();
            Annotation[][] annotations = seiMethod.getParameterAnnotations();

            for (int i = 0; i < parameters.length; i++) {
                ParameterDescription paramDesc = new ParameterDescriptionImpl(i, parameters[i],
                                                                              paramaterTypes[i],
                                                                              annotations[i], this);
                buildParameterList.add(paramDesc);
            }

        } else {
            ParameterDescriptionComposite pdc = null;
            Iterator<ParameterDescriptionComposite> iter =
                    methodComposite.getParameterDescriptionCompositeList().iterator();

            for (int i = 0; i < methodComposite.getParameterDescriptionCompositeList().size(); i++)
            {
                ParameterDescription paramDesc =
                        new ParameterDescriptionImpl(i,
                                                     methodComposite.getParameterDescriptionComposite(
                                                             i),
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

            for (Class wfClass : webFaultClasses) {
                // according to JAXWS 3.7, the @WebFault annotation is only used for customizations,
                // so we'll add all declared exceptions
                WebFault wfanno = null;
                for (Annotation anno : wfClass.getAnnotations()) {
                    if (anno.annotationType() == WebFault.class) {
                        wfanno = (WebFault)anno;
                    }
                }
                buildFaultList.add(new FaultDescriptionImpl(wfClass, wfanno, this));
            }
        } else {
            // TODO do I care about methodComposite like the paramDescription does?
            //Call FaultDescriptionImpl for all non-generic exceptions...Need to check a
            // a couple of things
            // 1. If this is a generic exception, ignore it
            // 2. If this is not a generic exception, then find it in the DBC Map
            //       If not found in map, then throw not found exception
            //3. Pass the validated WebFault dbc and possibly the classImpl dbc to FaultDescription
            //4. Possibly set AxisOperation.setFaultMessages array...or something like that

            String[] webFaultClassNames = methodComposite.getExceptions();

            HashMap<String, DescriptionBuilderComposite> dbcMap =
                    getEndpointInterfaceDescriptionImpl().getEndpointDescriptionImpl()
                            .getServiceDescriptionImpl().getDBCMap();

            if (webFaultClassNames != null) {
                for (String wfClassName : webFaultClassNames) {
                    //	Try to find this exception class in the dbc list. If we can't find it
                    //  then just assume that its a generic exception.

                    DescriptionBuilderComposite faultDBC = dbcMap.get(wfClassName);

                    if (faultDBC != null) {
                        // JAXWS 3.7 does not require @WebFault annotation
                        // We found a valid exception composite thats annotated
                        buildFaultList.add(new FaultDescriptionImpl(faultDBC, this));
                    }

                }
            }
        }

        return buildFaultList.toArray(new FaultDescription[0]);
    }

    // =====================================
    // ANNOTATION: WebMethod
    // =====================================
    public WebMethod getAnnoWebMethod() {
        return webMethodAnnotation;
    }

    static QName determineOperationQName(Method javaMethod) {
        return new QName(determineOperationName(javaMethod));
    }

    //TODO: For now, we are overriding the above method only because it is static, these should
    //be combined at some point
    public static QName determineOperationQName(MethodDescriptionComposite mdc) {
        return new QName(determineOperationName(mdc));
    }

    //TODO: Deprecate this after we use only DBC objects
    private static String determineOperationName(Method javaMethod) {

        String operationName = null;
        if (javaMethod == null) {
            return null;
        }

        WebMethod wmAnnotation = javaMethod.getAnnotation(WebMethod.class);
        // Per JSR-181 MR Sec 4.2 "Annotation: javax.jws.WebMethod" pg 17,
        // if @WebMethod specifies and operation name, use that.  Otherwise
        // default is the Java method name
        if (wmAnnotation != null && !DescriptionUtils.isEmpty(wmAnnotation.operationName())) {
            operationName = wmAnnotation.operationName();
        } else {
            operationName = javaMethod.getName();
        }

        return operationName;
    }

    //TODO: For now, we are overriding the above method only because it is static, these should
    //be combined at some point
    private static String determineOperationName(MethodDescriptionComposite mdc) {
        String operationName = null;

        if (mdc == null) {
            return null;
        }
        WebMethod wmAnnotation = mdc.getWebMethodAnnot();
        if (wmAnnotation != null && !DescriptionUtils.isEmpty(wmAnnotation.operationName())) {
            operationName = wmAnnotation.operationName();
        } else {
            operationName = mdc.getMethodName();
        }

        return operationName;
    }

    public String getOperationName() {
        // REVIEW: WSDL/Anno merge
        return getAnnoWebMethodOperationName();
    }

    public String getAnnoWebMethodOperationName() {
        if (webMethodOperationName == null) {
            if (!isDBC() && seiMethod != null) {
                webMethodOperationName = determineOperationName(seiMethod);
            } else if (methodComposite != null) {
                webMethodOperationName = determineOperationName(methodComposite);
            }
        }
        return webMethodOperationName;
    }

    /* (non-Javadoc)
     * @see org.apache.axis2.jaxws.description.OperationDescription#getSyncOperation()
     */
    public OperationDescription getSyncOperation() {

        if (syncOperationDescription != null) {
            // No need to do anything; the sync operation has already been set and will be
            // returned below
        } else if (!isJAXWSAsyncClientMethod()) {
            // The current OpDesc is not an async operation.  Cache it, then return it below.
            syncOperationDescription = this;
        } else {
            // We haven't found a sync opdesc for this operation yet, so try again.  See the 
            // comments in the interface declaration for this method on why this might occur.
            OperationDescription opDesc = null;
            
            String webMethodAnnoName = getOperationName();
            String javaMethodName = getJavaMethodName();
            if (webMethodAnnoName != null && webMethodAnnoName.length() > 0 &&
                    webMethodAnnoName != javaMethodName) {
                EndpointInterfaceDescription eid = getEndpointInterfaceDescription();
                if (eid != null) {
                    //searching for opDesc of sync operation.
                    OperationDescription[] ods = null;
                    ods = eid.getOperationForJavaMethod(webMethodAnnoName);
                    if (ods != null) {
                        for (OperationDescription od : ods) {
                            if (od.getJavaMethodName().equals(webMethodAnnoName)
                                    && !od.isJAXWSAsyncClientMethod()) {
                                opDesc = od;
                                break;
                            }
                        }
                    }
                }
            }
            // Note that opDesc might still be null
            syncOperationDescription = opDesc;
        }
        return syncOperationDescription;
    }

    public String getAction() {
        // REVIEW: WSDL/Anno merge
        return getAnnoWebMethodAction();
    }

    public String getAnnoWebMethodAction() {
        if (webMethodAction == null) {
            if (getAnnoWebMethod() != null &&
                    !DescriptionUtils.isEmpty(getAnnoWebMethod().action())) {
                webMethodAction = getAnnoWebMethod().action();
            } else {
                webMethodAction = WebMethod_Action_DEFAULT;
            }
        }
        return webMethodAction;
    }

    public boolean isExcluded() {
        // REVIEW: WSDL/Annotation merge
        return getAnnoWebMethodExclude();
    }

    public boolean getAnnoWebMethodExclude() {
        if (webMethodExclude == null) {
            // TODO: Validation: if this attribute specified, no other elements allowed per JSR-181 MR Sec 4.2, pg 17
            // TODO: Validation: This element is not allowed on endpoint interfaces
            // Unlike the elements with a String value, if the annotation is present, exclude will always 
            // return a usable value since it will default to FALSE if the element is not present.
            if (getAnnoWebMethod() != null) {
                webMethodExclude = new Boolean(getAnnoWebMethod().exclude());
            } else {
                webMethodExclude = WebMethod_Exclude_DEFAULT;
            }
        }

        return webMethodExclude.booleanValue();
    }

    // ==========================================
    // ANNOTATION: RequestWrapper
    // ==========================================
    public RequestWrapper getAnnoRequestWrapper() {
        if (requestWrapperAnnotation == null) {
            if (!isDBC() && seiMethod != null) {
                requestWrapperAnnotation = seiMethod.getAnnotation(RequestWrapper.class);
            } else if (isDBC() && methodComposite != null) {
                requestWrapperAnnotation = methodComposite.getRequestWrapperAnnot();
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Unable to get RequestWrapper annotation");
                }
            }
        }
        return requestWrapperAnnotation;
    }

    public String getRequestWrapperLocalName() {
        // REVIEW: WSDL/Anno merge
        return getAnnoRequestWrapperLocalName();
    }

    /**
     * For wrapped parameter style (based on the annotation and the WSDL), returns the wrapper
     * value.  For non-wrapped (i.e. bare) parameter style, returns null.
     *
     * @return
     */
    public String getAnnoRequestWrapperLocalName() {
        if (!isWrappedParameters()) {
            // A wrapper is only meaningful for wrapped parameters
            return null;
        }
        if (requestWrapperLocalName == null) {
            if (getAnnoRequestWrapper() != null
                    && !DescriptionUtils.isEmpty(getAnnoRequestWrapper().localName())) {
                requestWrapperLocalName = getAnnoRequestWrapper().localName();
            } else {
                // The default value of localName is the value of operationQName as
                // defined in the WebMethod annotation. [JAX-WS Sec. 7.3, p. 80]
                requestWrapperLocalName = getAnnoWebMethodOperationName();
            }
        }
        return requestWrapperLocalName;
    }

    public String getRequestWrapperTargetNamespace() {
        // REVIEW: WSDL/Anno merge
        return getAnnoRequestWrapperTargetNamespace();
    }

    /**
     * For wrapped parameter style (based on the annotation and the WSDL), returns the wrapper
     * value.  For non-wrapped (i.e. bare) parameter style, returns null.
     *
     * @return
     */
    public String getAnnoRequestWrapperTargetNamespace() {
        if (!isWrappedParameters()) {
            // A wrapper is only meaningful for wrapped parameters
            return null;
        }
        if (requestWrapperTargetNamespace == null) {
            if (getAnnoRequestWrapper() != null &&
                    !DescriptionUtils.isEmpty(getAnnoRequestWrapper().targetNamespace())) {
                requestWrapperTargetNamespace = getAnnoRequestWrapper().targetNamespace();
            } else {
                // The default value for targetNamespace is the target namespace of the SEI. [JAX-WS Sec 7.3, p. 80]
                requestWrapperTargetNamespace =
                        getEndpointInterfaceDescription().getTargetNamespace();
            }
        }
        return requestWrapperTargetNamespace;
    }

    public String getRequestWrapperClassName() {
        // REVIEW: WSDL/Anno merge
        return getAnnoRequestWrapperClassName();
    }

    /**
     * For wrapped parameter style (based on the annotation and the WSDL), returns the wrapper
     * value.  For non-wrapped (i.e. bare) parameter style, returns null.
     *
     * @return
     */
    public String getAnnoRequestWrapperClassName() {
        if (!isWrappedParameters()) {
            // A wrapper is only meaningful for wrapped parameters
            return null;
        }
        if (requestWrapperClassName == null) {
            if (getAnnoRequestWrapper() != null &&
                    !DescriptionUtils.isEmpty(getAnnoRequestWrapper().className())) {
                requestWrapperClassName = getAnnoRequestWrapper().className();
            } else {
                // There is no default for the RequestWrapper class name.  
                // In such cases the programming layer (JAXWS) may use a programming spec specific
                // mechanism to find the class, build the class, or operate without the class.
                requestWrapperClassName = null;
            }
        }
        return requestWrapperClassName;
    }

    // ===========================================
    // ANNOTATION: ResponseWrapper
    // ===========================================
    public ResponseWrapper getAnnoResponseWrapper() {
        if (responseWrapperAnnotation == null) {
            if (!isDBC() && seiMethod != null) {
                responseWrapperAnnotation = seiMethod.getAnnotation(ResponseWrapper.class);
            } else if (isDBC() && methodComposite != null) {
                responseWrapperAnnotation = methodComposite.getResponseWrapperAnnot();
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Unable to get ResponseWrapper annotation");
                }
            }
        }
        return responseWrapperAnnotation;
    }

    public String getResponseWrapperLocalName() {
        return getAnnoResponseWrapperLocalName();
    }

    /**
     * For wrapped parameter style (based on the annotation and the WSDL), returns the wrapper
     * value.  For non-wrapped (i.e. bare) parameter style, returns null.
     *
     * @return
     */
    public String getAnnoResponseWrapperLocalName() {
        if (!isWrappedParameters()) {
            // A wrapper is only meaningful for wrapped parameters
            return null;
        }
        if (responseWrapperLocalName == null) {
            if (getAnnoResponseWrapper() != null &&
                    !DescriptionUtils.isEmpty(getAnnoResponseWrapper().localName())) {
                responseWrapperLocalName = getAnnoResponseWrapper().localName();
            } else {
                // The default value of localName is the value of operationQName as 
                // defined in the WebMethod annotation appended with "Response". [JAX-WS Sec. 7.4, p. 81]
                responseWrapperLocalName = getAnnoWebMethodOperationName() + "Response";
            }
        }
        return responseWrapperLocalName;
    }

    public String getResponseWrapperTargetNamespace() {
        // REVIEW: WSDL/Anno merge
        return getAnnoResponseWrapperTargetNamespace();
    }

    /**
     * For wrapped parameter style (based on the annotation and the WSDL), returns the wrapper
     * value.  For non-wrapped (i.e. bare) parameter style, returns null.
     *
     * @return
     */
    public String getAnnoResponseWrapperTargetNamespace() {
        if (!isWrappedParameters()) {
            // A wrapper is only meaningful for wrapped parameters
            return null;
        }
        if (responseWrapperTargetNamespace == null) {
            if (getAnnoResponseWrapper() != null &&
                    !DescriptionUtils.isEmpty(getAnnoResponseWrapper().targetNamespace())) {
                responseWrapperTargetNamespace = getAnnoResponseWrapper().targetNamespace();
            } else {
                // The default value for targetNamespace is the target namespace of the SEI. [JAX-WS Sec 7.3, p. 80]
                // TODO: Implement getting the TNS from the SEI 
                responseWrapperTargetNamespace =
                        getEndpointInterfaceDescription().getTargetNamespace();
            }
        }
        return responseWrapperTargetNamespace;
    }

    public String getResponseWrapperClassName() {
        // REVIEW: WSDL/Anno merge
        return getAnnoResponseWrapperClassName();
    }

    /**
     * For wrapped parameter style (based on the annotation and the WSDL), returns the wrapper
     * value.  For non-wrapped (i.e. bare) parameter style, returns null.
     *
     * @return
     */
    public String getAnnoResponseWrapperClassName() {
        if (!isWrappedParameters()) {
            // A wrapper is only meaningful for wrapped parameters
            return null;
        }
        if (responseWrapperClassName == null) {
            if (getAnnoResponseWrapper() != null &&
                    !DescriptionUtils.isEmpty(getAnnoResponseWrapper().className())) {
                responseWrapperClassName = getAnnoResponseWrapper().className();
            } else {
                // There is no default for the ResponseWrapper class name.  
                // In such cases the programming layer (JAXWS) may use a programming spec specific
                // mechanism to find the class, build the class, or operate without the class.
                responseWrapperClassName = null;
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

    public FaultDescription resolveFaultByExceptionName(String exceptionClassName) {
        if (faultDescriptions != null) {
            for (FaultDescription fd : faultDescriptions) {
                if (exceptionClassName.equals(fd.getExceptionClassName()))
                    return fd;
            }
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
            for (ParameterDescription paramDesc : parameterDescriptions) {
                if (parameterName.equals(paramDesc.getParameterName())) {
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

    public String[] getParamNames() {
        // REVIEW: WSDL/Anno merge
        return getAnnoWebParamNames();
    }

    public String[] getAnnoWebParamNames() {
        if (webParamNames == null) {
            ArrayList<String> buildNames = new ArrayList<String>();
            ParameterDescription[] paramDescs = getParameterDescriptions();
            for (ParameterDescription currentParamDesc : paramDescs) {
                buildNames.add(currentParamDesc.getParameterName());
            }
            webParamNames = buildNames.toArray(new String[0]);
        }
        return webParamNames;
    }

    public String[] getAnnoWebParamTargetNamespaces() {
        if (webParamTargetNamespace == null) {
            ArrayList<String> buildTargetNS = new ArrayList<String>();
            ParameterDescription[] paramDescs = getParameterDescriptions();
            for (ParameterDescription currentParamDesc : paramDescs) {
                buildTargetNS.add(currentParamDesc.getTargetNamespace());
            }
            webParamTargetNamespace = buildTargetNS.toArray(new String[0]);
        }
        return webParamTargetNamespace;
    }

    public String getAnnoWebParamTargetNamespace(String name) {
        String returnTargetNS = null;
        ParameterDescription paramDesc = getParameterDescription(name);
        if (paramDesc != null) {
            returnTargetNS = paramDesc.getTargetNamespace();
        }
        return returnTargetNS;
    }


    public Mode[] getAnnoWebParamModes() {
        if (webParamMode == null) {
            ArrayList<Mode> buildModes = new ArrayList<Mode>();
            ParameterDescription[] paramDescs = getParameterDescriptions();
            for (ParameterDescription currentParamDesc : paramDescs) {
                // TODO: Consider new ParamDesc.Mode vs WebParam.Mode
                buildModes.add(((ParameterDescriptionJava)currentParamDesc).getAnnoWebParamMode());
            }
            webParamMode = buildModes.toArray(new Mode[0]);
        }
        return webParamMode;
    }

    public boolean isAnnoWebParamHeader(String name) {
        ParameterDescription paramDesc = getParameterDescription(name);
        if (paramDesc != null) {
            return paramDesc.isHeader();
        }
        return false;
    }

    // ===========================================
    // ANNOTATION: WebResult
    // ===========================================
    public WebResult getAnnoWebResult() {
        if (webResultAnnotation == null) {
            if (!isDBC() && seiMethod != null) {
                webResultAnnotation = seiMethod.getAnnotation(WebResult.class);
            } else if (methodComposite != null) {
                webResultAnnotation = methodComposite.getWebResultAnnot();
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Unable to get WebResult annotation");
                }
            }
        }
        return webResultAnnotation;
    }

    public boolean isWebResultAnnotationSpecified() {
        return getAnnoWebResult() != null;
    }

    public boolean isOperationReturningResult() {
        boolean isResult = false;
        if (!isAnnoOneWay()) {
            if (!isDBC() && seiMethod != null) {
                if (seiMethod.getReturnType() != Void.TYPE) {
                    isResult = true;
                }
            } else if (methodComposite != null) {
                if (!DescriptionUtils.isEmpty(methodComposite.getReturnType()) &&
                        !methodComposite.getReturnType().equals("void"))
                    isResult = true;
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("No class to determine if result is returned");
                }
            }
        }
        return isResult;
    }

    public String getResultName() {
        // REVIEW: WSDL/Anno merge
        return getAnnoWebResultName();
    }

    public String getAnnoWebResultName() {
        if (!isOperationReturningResult()) {
            return null;
        }
        if (webResultName == null) {
            if (getAnnoWebResult() != null && !DescriptionUtils.isEmpty(getAnnoWebResult().name()))
            {
                webResultName = getAnnoWebResult().name();
            } else if (getAnnoSoapBindingStyle() == SOAPBinding.Style.DOCUMENT
                    && getAnnoSoapBindingParameterStyle() == SOAPBinding.ParameterStyle.BARE) {
                // Default for operation style DOCUMENT and paramater style BARE per JSR 181 MR Sec 4.5.1, pg 23
                webResultName = getAnnoWebMethodOperationName() + "Response";

            } else {
                // Defeault value is "return" per JSR-181 MR Sec. 4.5.1, p. 22
                webResultName = "return";
            }
        }
        return webResultName;
    }

    public String getResultPartName() {
        // REVIEW: WSDL/Anno merge
        return getAnnoWebResultPartName();
    }

    public String getAnnoWebResultPartName() {
        if (!isOperationReturningResult()) {
            return null;
        }
        if (webResultPartName == null) {
            if (getAnnoWebResult() != null &&
                    !DescriptionUtils.isEmpty(getAnnoWebResult().partName())) {
                webResultPartName = getAnnoWebResult().partName();
            } else {
                // Default is the WebResult.name per JSR-181 MR Sec 4.5.1, pg 23
                webResultPartName = getAnnoWebResultName();
            }
        }
        return webResultPartName;
    }

    public String getResultTargetNamespace() {
        // REVIEW: WSDL/Anno merge
        return getAnnoWebResultTargetNamespace();
    }

    public String getAnnoWebResultTargetNamespace() {
        if (!isOperationReturningResult()) {
            return null;
        }
        if (webResultTargetNamespace == null) {
            if (getAnnoWebResult() != null &&
                    !DescriptionUtils.isEmpty(getAnnoWebResult().targetNamespace())) {
                webResultTargetNamespace = getAnnoWebResult().targetNamespace();
            } else if (getAnnoSoapBindingStyle() == SOAPBinding.Style.DOCUMENT
                    && getAnnoSoapBindingParameterStyle() == SOAPBinding.ParameterStyle.WRAPPED
                    && !getAnnoWebResultHeader()) {
                // Default for operation style DOCUMENT and paramater style WRAPPED and the return value
                // does not map to a header per JSR-181 MR Sec 4.5.1, pg 23-24
                webResultTargetNamespace = WebResult_TargetNamespace_DEFAULT;
            } else {
                // Default is the namespace from the WebService per JSR-181 MR Sec 4.5.1, pg 23-24
                webResultTargetNamespace =
                        ((EndpointDescriptionJava)getEndpointInterfaceDescription()
                                .getEndpointDescription()).getAnnoWebServiceTargetNamespace();
            }

        }
        return webResultTargetNamespace;
    }

    public boolean isResultHeader() {
        // REVIEW: WSDL/Anno merge
        return getAnnoWebResultHeader();
    }

    public boolean getAnnoWebResultHeader() {
        if (!isOperationReturningResult()) {
            return false;
        }
        if (webResultHeader == null) {
            if (getAnnoWebResult() != null) {
                // Unlike the elements with a String value, if the annotation is present, exclude will always 
                // return a usable value since it will default to FALSE if the element is not present.
                webResultHeader = new Boolean(getAnnoWebResult().header());
            } else {
                webResultHeader = WebResult_Header_DEFAULT;
            }
        }
        return webResultHeader.booleanValue();
    }

    // ===========================================
    // ANNOTATION: SOAPBinding
    // ===========================================
    public SOAPBinding getAnnoSoapBinding() {
        // TODO: VALIDATION: Only style of DOCUMENT allowed on Method annotation; remember to check the Type's style setting also
        //       JSR-181 Sec 4.7 p. 28
        if (soapBindingAnnotation == null) {
            if (!isDBC() && seiMethod != null) {
                soapBindingAnnotation = seiMethod.getAnnotation(SOAPBinding.class);
            } else if (isDBC() && methodComposite != null) {
                soapBindingAnnotation = methodComposite.getSoapBindingAnnot();
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Unable to get SOAP Binding annotation");
                }
            }
        }
        return soapBindingAnnotation;
    }

    public javax.jws.soap.SOAPBinding.Style getSoapBindingStyle() {
        // REVIEW: WSDL/Anno merge
        return getAnnoSoapBindingStyle();
    }

    public javax.jws.soap.SOAPBinding.Style getAnnoSoapBindingStyle() {
        if (soapBindingStyle == null) {
            if (getAnnoSoapBinding() != null && getAnnoSoapBinding().style() != null) {
                soapBindingStyle = getAnnoSoapBinding().style();
            } else {
                // Per JSR-181 MR Sec 4.7, pg 28: if not specified, use the Type value.
                soapBindingStyle = getEndpointInterfaceDescription().getSoapBindingStyle();
            }
        }
        return soapBindingStyle;
    }

    public javax.jws.soap.SOAPBinding.Use getSoapBindingUse() {
        // REVIEW: WSDL/Anno merge
        return getAnnoSoapBindingUse();
    }

    public javax.jws.soap.SOAPBinding.Use getAnnoSoapBindingUse() {
        if (soapBindingUse == null) {
            if (getAnnoSoapBinding() != null && getAnnoSoapBinding().use() != null) {
                soapBindingUse = getAnnoSoapBinding().use();
            } else {
                // Per JSR-181 MR Sec 4.7, pg 28: if not specified, use the Type value.
                soapBindingUse = getEndpointInterfaceDescription().getSoapBindingUse();
            }
        }
        return soapBindingUse;
    }

    public javax.jws.soap.SOAPBinding.ParameterStyle getSoapBindingParameterStyle() {
        // REVIEW: WSDL/Anno merge
        return getAnnoSoapBindingParameterStyle();
    }

    public javax.jws.soap.SOAPBinding.ParameterStyle getAnnoSoapBindingParameterStyle() {
        if (soapBindingParameterStyle == null) {
            if (getAnnoSoapBinding() != null && getAnnoSoapBinding().parameterStyle() != null) {
                soapBindingParameterStyle = getAnnoSoapBinding().parameterStyle();
            } else {
                // Per JSR-181 MR Sec 4.7, pg 28: if not specified, use the Type value.
                soapBindingParameterStyle =
                        getEndpointInterfaceDescription().getSoapBindingParameterStyle();
            }
        }
        return soapBindingParameterStyle;
    }

    // ===========================================
    // ANNOTATION: OneWay
    // ===========================================
    public Oneway getAnnoOneway() {
        //TODO: Shouldn't really do it this way...if there is not Oneway annotation, 
        //      we will always be calling the methods to try to retrieve it, since
        //      it will always be null, should consider relying on 'isOneWay'

        if (onewayAnnotation == null) {
            if (isDBC() && methodComposite != null) {
                if (methodComposite.isOneWay()) {
                    onewayAnnotation = OneWayAnnot.createOneWayAnnotImpl();
                }
            } else if (!isDBC() && seiMethod != null) {
                onewayAnnotation = seiMethod.getAnnotation(Oneway.class);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Unable to get OneWay annotation");
                }
            }
        }
        return onewayAnnotation;
    }

    public boolean isOneWay() {
        // REVIEW: WSDL/Anno merge
        return isAnnoOneWay();
    }

    public boolean isAnnoOneWay() {
        if (onewayIsOneway == null) {
            if (getAnnoOneway() != null) {
                // The presence of the annotation indicates the method is oneway
                onewayIsOneway = new Boolean(true);
            } else {
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

    /* (non-Javadoc)
     * @see org.apache.axis2.jaxws.description.OperationDescription#getResultType()
     */
    public Class getResultType() {
        Class returnClass = null;
        if (!isDBC() && getSEIMethod() != null) {
            Method seiMethod = this.getSEIMethod();
            returnClass = seiMethod.getReturnType();
        } else if (methodComposite != null) {
            returnClass = methodComposite.getReturnTypeClass();
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Unable to get result type from null class");
            }
        }
        return returnClass;
    }

    /* (non-Javadoc)
     * @see org.apache.axis2.jaxws.description.OperationDescription#getResultActualType()
     */
    public Class getResultActualType() {
        // TODO: Fix this!  it isn't doing the right thing for DBC as noted below with FIXME comments
        //       This is used to marshall the rsp on the service (dbc) and demarshall on the client (reflection)
        //       But we shouldn't get an async OpDesc on the service since getDispatchableOperation(QN) removes them.

        Class returnType = getResultType();
        if (returnType == null) {
            return null;
        }
        if (isJAXWSAsyncClientMethod()) {
            //pooling implementation
            if (Response.class == returnType) {
                if (!isDBC()) {
                    Type type = seiMethod.getGenericReturnType();
                    ParameterizedType pType = (ParameterizedType)type;
                    Type aType = pType.getActualTypeArguments()[0];
                    if (aType != null && ParameterizedType.class.isInstance(aType)) {
                        return (Class)((ParameterizedType)aType).getRawType();
                    }
                    return (Class)aType;
                } else {
                    // FIXME: This doesn't work for DBC.  That's OK for now because DBC isn't used on the client side
                    //        yet; the client is all Java Reflection.  On the Service side, the Async methods are not used.
                    //        This needs to return T for Response<T>, or List for Response<List<T>>>
                    return returnType;
                }
            }
            //Callback Implementation
            else {
                // FIXME: This doesn't work for DBC.  That's OK for now because DBC isn't used on the client side
                //        yet; the client is all Java Reflection.  On the Service side, the Async methods are not used.
                //        This needs to find and return T for AsyncHandler<T>, or List for AsyncHandler<List<T>>>
                Type[] type = getGenericParameterTypes();
                Class parameters[] = getParameterTypes();
                int i = 0;
                for (Class param : parameters) {
                    if (AsyncHandler.class.isAssignableFrom(param)) {
                        ParameterizedType pType = (ParameterizedType)type[i];
                        Type aType = pType.getActualTypeArguments()[0];
                        if (aType != null && ParameterizedType.class.isInstance(aType)) {
                            return (Class)((ParameterizedType)aType).getRawType();
                        }
                        return (Class)aType;
                    }
                    i++;
                }
            }
        }

        return returnType;
    }


    private Type[] getGenericParameterTypes() {
        if (isDBC()) {
            // FIXME: This doesn't work for DBC.  That's OK for now because DBC isn't used on the client side
            //        yet; the client is all Java Reflection.  On the Service side, the Async methods are not used.
            //        And this method is only used to parse out the JAX-WS Async parameter types to find
            //        AsyncHandler<T>.  The problem with the code that was removed is that a Type can not be
            //        instantiated, so we can't new up a Type inside the PDC.
            throw new UnsupportedOperationException(
                    "OperationDescriptionImpl.getParameterActualGenericType not supported for DBC");
//           Type [] type = new Type[parameterDescriptions.length];
//           for (int i=0; i < parameterDescriptions.length; i++){
//               type[i] = ((ParameterDescriptionImpl) parameterDescriptions[i]).getParameterActualGenericType();
//           }
//           return type;
        } else {
            Type [] type = seiMethod.getGenericParameterTypes();
            return type;
        }
    }

    private Class[] getParameterTypes() {
        if (isDBC()) {
            Class [] parameters = new Class[parameterDescriptions.length];
            for (int i = 0; i < parameterDescriptions.length; i++) {
                parameters[i] = parameterDescriptions[i].getParameterType();
            }
            return parameters;
        } else {
            Class [] parameters = seiMethod.getParameterTypes();
            return parameters;
        }
    }

    /*
    * (non-Javadoc)
    * @see org.apache.axis2.jaxws.description.OperationDescription#isJAXWSAsyncClientMethod()
    */
    public boolean isJAXWSAsyncClientMethod() {
        boolean answer = false;
        String methodName = null;
        String returnTypeName = null;
        if (isDBC()) {
            methodName = getMethodDescriptionComposite().getMethodName();
            returnTypeName = getMethodDescriptionComposite().getReturnType();
        } else {
            Method method = this.getSEIMethod();
            if (method != null) {
                methodName = method.getName();
                returnTypeName = method.getReturnType().getName();
            }
        }
        if (methodName != null && returnTypeName != null) {
            // REVIEW: Not sure the method MUST end with "Async"; I think it can be customized.
            answer = methodName.endsWith("Async")
                    && (returnTypeName.equals(Response.class.getName()) ||
                    returnTypeName.equals(Future.class.getName()));
        }
        return answer;
    }

    /**
     * Return the Service Implementation method for this operation IFF it has been set by a previous
     * call to getMethodFromServiceImpl(Class serviceImplClass).  Otherwise a null is returned.
     *
     * @return
     */
    private Method getMethodFromServiceImpl() {
        return serviceImplMethod;

    }

    public Method getMethodFromServiceImpl(Class serviceImpl) {

        // TODO: This doesn't support overloaded methods in the service impl  This is
        //       DIFFERENT than overloaded WSDL operations (which aren't supported).  We
        //       MUST support overloaded service impl methods as long as they have different
        //       wsdl operation names.  For example:
        //  ServiceImple Class          SEI Class
        //                              @WebMethod.name = Foo1
        //  void foo()                  void foo()
        //                              @WebMethod.name = Foo2
        //  void foo(int)               void foo(int)
        //                              @WebMethod.name = Foo3
        //  void foo(String)            void foo(String)
        //
        //  There will be two OpDescs, Foo1 and Foo2; the incoming wsdl operation will correctly identify
        //  which OpDesc.  However, to return the correct service impl method, we need to compare the
        //  signatures, not just the method names.
        if (!serviceImplMethodFound) {
            Method[] methods = serviceImpl.getMethods();
            String opDescMethodName = getJavaMethodName();
            ParameterDescription[] paramDesc = getParameterDescriptions();
            // TODO: As noted above, a full signature is necessary, not just number of params
            int numberOfParams = 0;
            if (paramDesc != null) {
                numberOfParams = paramDesc.length;
            }

            // Loop through all the methods on the service impl and find the method that maps
            // to this OperationDescripton
            for (Method checkMethod : methods) {
                if (checkMethod.getName().equals(opDescMethodName)) {
                    Class[] methodParams = checkMethod.getParameterTypes();
                    // TODO: As noted above, a full signature is necessary, not just number of params
                    if (methodParams.length == numberOfParams) {
                        if (paramTypesMatch(paramDesc, methodParams)) {
                            serviceImplMethod = checkMethod;
                            break;
                        }
                    }
                }
            }
            serviceImplMethodFound = true;
        }
        return serviceImplMethod;
    }

    /**
     * This method will compare the types of the parameters in a <code>ParameterDescription</code>
     * vs. the type of the arguments in the parameters of a <code>Method</code>.
     *
     * @param paramDescs   - <code>ParameterDescription</code>[]
     * @param methodParams - <code>Class</code>[]
     * @return - <code>boolean</code>
     */
    private boolean paramTypesMatch(ParameterDescription[] paramDescs, Class[]
            methodParams) {
        for (int i = 0; i < paramDescs.length; i++) {
            String mParamType = methodParams[i].getName();
            String pdType = getPDType(paramDescs[i]);
            if (mParamType == null || !mParamType.equals(pdType)) {
                return false;
            }
        }
        return true;
    }

    /**
     * This will get a <code>String</code> representing the parameter class of a
     * <code>ParameterDescription</code>.
     *
     * @param pd - <code>ParameterDescrition</code>
     * @return - <code>String</code>
     */
    private String getPDType(ParameterDescription pd) {
        String type = null;
        if (pd.getParameterType() != null) {
            type = pd.getParameterType().getName();
        } else if (pd.getParameterActualType() != null) {
            type = pd.getParameterActualType().getName();
        }
        return type;
    }

    public OperationRuntimeDescription getOperationRuntimeDesc(String name) {
        // TODO Add toString support
        return runtimeDescMap.get(name);
    }

    public void setOperationRuntimeDesc(OperationRuntimeDescription ord) {
        // TODO Add toString support
        runtimeDescMap.put(ord.getKey(), ord);
    }

    private void checkForXmlListAnnotation(Annotation[] annotations) {
    	for(Annotation annotation : annotations) {
    		if(annotation.annotationType() == XmlList.class) {
    			isListType = true;
    		}
    	}
    }
    
    public boolean isListType() {
    	return isListType;
    }
    
    /**
     * This method will return the namespace for the BindingInput that this operation
     * specifies. It will first look for a namespace on the WSDL Binding object and then 
     * default to the web service's target namespace.
     */
    public String getBindingInputNamespace() {
        String tns = null;
        Binding binding =
                this.getEndpointInterfaceDescriptionImpl()
                    .getEndpointDescriptionImpl()
                    .getWSDLBinding();
        if (binding != null) {
            if (log.isDebugEnabled()) {
                log.debug("Found WSDL binding");
            }
            // this call does not support overloaded WSDL operations as it
            // does not specify the name of the input and output messages
            BindingOperation bindingOp =
                    binding.getBindingOperation(getOperationName(), null, null);
            if (bindingOp != null && bindingOp.getBindingInput() != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Found WSDL binding operation and input");
                }
                tns = getBindingNamespace(bindingOp.getBindingInput());
                if (tns != null && log.isDebugEnabled()) {
                    log.debug("For operation: " + bindingOp.getName()
                            + " returning the following namespace for input message"
                            + " from WSDL: " + tns);
                }
            }
        }
        if (tns == null) {
            tns = getEndpointInterfaceDescription().getTargetNamespace();
            if (log.isDebugEnabled()) {
                log.debug("For binding input returning @WebService.targetNamespace: " + tns);
            }
        }
        return tns;
    }

    /**
     * This method will return the namespace for the BindingOutput that this operation
     * specifies. It will first look for a namespace on the WSDL Binding object and then 
     * default to the web service's target namespace.
     */
    public String getBindingOutputNamespace() {
        String tns = null;
        Binding binding =
                this.getEndpointInterfaceDescriptionImpl()
                    .getEndpointDescriptionImpl()
                    .getWSDLBinding();
        if (binding != null) {
            if (log.isDebugEnabled()) {
                log.debug("Found WSDL binding");
            }
            // this call does not support overloaded WSDL operations as it
            // does not specify the name of the input and output messages
            BindingOperation bindingOp =
                    binding.getBindingOperation(getOperationName(), null, null);
            if (bindingOp != null && bindingOp.getBindingOutput() != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Found WSDL binding operation and output");
                }
                tns = getBindingNamespace(bindingOp.getBindingOutput());
                if (tns != null && log.isDebugEnabled()) {
                    log.debug("For operation: " + bindingOp.getName()
                            + " returning the following namespace for output message"
                            + " from WSDL: " + tns);
                }
            }
        }
        if (tns == null) {
            tns = getEndpointInterfaceDescription().getTargetNamespace();
            if (log.isDebugEnabled()) {
                log.debug("For binding output returning @WebService.targetNamespace: " + tns);
            }
        }
        return tns;
    }

    
    /**
     * This method will retrieve the namespace that is specified by the BindingInput or
     * BindingOutput object.
     */
    private String getBindingNamespace(AttributeExtensible opInfo) {
        if (opInfo instanceof BindingInput) {
            BindingInput input = (BindingInput) opInfo;
            return DescriptionUtils.getNamespaceFromSOAPElement(input.getExtensibilityElements());
        } else if (opInfo instanceof BindingOutput) {
            BindingOutput output = (BindingOutput) opInfo;
            return DescriptionUtils.getNamespaceFromSOAPElement(output.getExtensibilityElements());
        }
        return null;
    }
    
    public AttachmentDescription getResultAttachmentDescription() {
        String partName = this.getResultPartName();
        if (partName != null) {
            if (log.isDebugEnabled()) {
                log.debug("Returning result AttachmentDescription for partName: " + partName);
            }
            return partAttachmentMap.get(partName);
        }
        if (log.isDebugEnabled()) {
            log.debug("Did not find result AttachmentDescription for partName: " + partName);
        }
        return null;
    }
    
    /**
     * This method will drive the building of AttachmentDescription objects for the
     * operation input/output messages in the WSDL.
     *
     */
    private void buildAttachmentInformation() {

        // Only building attachment info if we find a full WSDL
        if (this.getEndpointInterfaceDescriptionImpl()
                .getEndpointDescriptionImpl()
                .isWSDLFullySpecified()) {
            DescriptionUtils.getAttachmentFromBinding(this,
                                                      this.getEndpointInterfaceDescriptionImpl()
                                                          .getEndpointDescriptionImpl()
                                                          .getWSDLBinding());
        }

    }

    /**
     * This will return an AttachmentDescription based on a part name.
     */
    public AttachmentDescription getPartAttachmentDescription(String partName) {
        return partAttachmentMap.get(partName);
    }

    public void addPartAttachmentDescription(String partName, AttachmentDescription attachmentDesc) {
        partAttachmentMap.put(partName, attachmentDesc);
    }
            
    public String toString() {
        final String newline = "\n";
        final String sameline = "; ";
        StringBuffer string = new StringBuffer();
        try {
            string.append(super.toString());
            string.append(newline);
            string.append("Name: " + getName());
            string.append(sameline);
            string.append("Operation Name: " + getOperationName());
            string.append(sameline);
            string.append("Action: " + getAction());
            //
            string.append(newline);
            string.append("Operation excluded: " + (isExcluded() == true));
            string.append(sameline);
            string.append("Is oneway: " + (isOneWay() == true));
            string.append(sameline);
            string.append("Is returning result: " + (isOperationReturningResult() == true));
            string.append(sameline);
            string.append("Is result header: " + (isResultHeader() == true));
            string.append(sameline);
            string.append("Is JAXWS Client Async method: " + (isJAXWSAsyncClientMethod() == true));
            //
            string.append(newline);
            string.append("SOAP Style: " + getSoapBindingStyle());
            string.append(sameline);
            string.append("SOAP Use: " + getSoapBindingUse());
            string.append(sameline);
            string.append("SOAP Paramater Style: " + getSoapBindingParameterStyle());
            //
            string.append(newline);
            string.append("Result name: " + getResultName());
            string.append(sameline);
            string.append("Result part name: " + getResultPartName());
            string.append(sameline);
            string.append("Result type: " + getResultType());
            string.append(sameline);
            string.append("Result actual type: " + getResultActualType());
            if (getResultAttachmentDescription() != null) {
                string.append(newline);
                string.append(getResultAttachmentDescription().toString());
            }
            //
            string.append(newline);
            string.append("Request Wrapper class: " + getRequestWrapperClassName());
            string.append(sameline);
            string.append("Response Wrapper class: " + getResponseWrapperClassName());
            //
            string.append(newline);
            string.append("Java declaring class name: " + getJavaDeclaringClassName());
            string.append(newline);
            string.append("Java method name: " + getJavaMethodName());
            string.append(newline);
            string.append("Java paramaters: " + getJavaParameters());
            string.append(newline);
            string.append("Service Implementation method: " + getMethodFromServiceImpl());
            string.append(newline);
            string.append("Axis Operation: " + getAxisOperation());

            string.append(newline);
            ParameterDescription[] paramDescs = getParameterDescriptions();
            if (paramDescs != null && paramDescs.length > 0) {
                string.append("Number of Parameter Descriptions: " + paramDescs.length);
                for (ParameterDescription paramDesc : paramDescs) {
                    string.append(newline);
                    string.append("Parameter Description: " + paramDesc.toString());
                }
            } else {
                string.append("No Paramater Descriptions");
            }

            string.append(newline);
            FaultDescription[] faultDescs = getFaultDescriptions();
            if (faultDescs != null && faultDescs.length > 0) {
                string.append("Number of Fault Descriptions: " + faultDescs.length);
                for (FaultDescription faultDesc : faultDescs) {
                    string.append(newline);
                    string.append("Fault Description: " + faultDesc.toString());
                }
            } else {
                string.append("No Fault Descriptions");
            }

            if(!partAttachmentMap.isEmpty()) {
                string.append(newline);
                string.append("Number of Attachment Descriptions: "  + partAttachmentMap.size());
                string.append(newline);
                Iterator<AttachmentDescription> adIter = partAttachmentMap.values().iterator();
                while(adIter.hasNext()) {
                        string.append(adIter.next().toString());
                        string.append(newline);
                }
            } else {
                string.append(newline);
                string.append("No Attachment Descriptions");
                string.append(newline);
            }
            
            string.append("RuntimeDescriptions:" + this.runtimeDescMap.size());
            string.append(newline);
            for (OperationRuntimeDescription runtimeDesc : runtimeDescMap.values()) {
                string.append(runtimeDesc.toString());
                string.append(newline);
            }
        }
        catch (Throwable t) {
            string.append(newline);
            string.append("Complete debug information not currently available for " +
                    "OperationDescription");
            return string.toString();
        }
        return string.toString();
    }
}
