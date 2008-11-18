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

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisOperationFactory;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.java.security.AccessController;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.EndpointInterfaceDescription;
import org.apache.axis2.jaxws.description.EndpointInterfaceDescriptionJava;
import org.apache.axis2.jaxws.description.EndpointInterfaceDescriptionWSDL;
import org.apache.axis2.jaxws.description.OperationDescription;
import org.apache.axis2.jaxws.description.ServiceDescriptionWSDL;
import org.apache.axis2.jaxws.description.builder.DescriptionBuilderComposite;
import org.apache.axis2.jaxws.description.builder.MDQConstants;
import org.apache.axis2.jaxws.description.builder.MethodDescriptionComposite;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.wsdl.Definition;
import javax.wsdl.PortType;
import javax.xml.namespace.QName;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/** @see ../EndpointInterfaceDescription */
class EndpointInterfaceDescriptionImpl
        implements EndpointInterfaceDescription, EndpointInterfaceDescriptionJava,
        EndpointInterfaceDescriptionWSDL {
    private EndpointDescriptionImpl parentEndpointDescription;
    private ArrayList<OperationDescription> operationDescriptions =
            new ArrayList<OperationDescription>();
    private Map<QName, List<OperationDescription>> dispatchableOperations;
    private DescriptionBuilderComposite dbc;

    //Logging setup
    private static final Log log = LogFactory.getLog(EndpointInterfaceDescriptionImpl.class);

    // ===========================================
    // ANNOTATION related information
    // ===========================================

    // ANNOTATION: @WebService
    private WebService webServiceAnnotation;
    private String webServiceTargetNamespace;
    private String webService_Name;


    // ANNOTATION: @SOAPBinding
    // Note this is the Type-level annotation.  See OperationDescription for the Method-level annotation
    private SOAPBinding soapBindingAnnotation;
    private javax.jws.soap.SOAPBinding.Style soapBindingStyle;
    // Default value per JSR-181 MR Sec 4.7 "Annotation: javax.jws.soap.SOAPBinding" pg 28
    public static final javax.jws.soap.SOAPBinding.Style SOAPBinding_Style_DEFAULT =
            javax.jws.soap.SOAPBinding.Style.DOCUMENT;
    private javax.jws.soap.SOAPBinding.Use soapBindingUse;
    // Default value per JSR-181 MR Sec 4.7 "Annotation: javax.jws.soap.SOAPBinding" pg 28
    public static final javax.jws.soap.SOAPBinding.Use SOAPBinding_Use_DEFAULT =
            javax.jws.soap.SOAPBinding.Use.LITERAL;
    private javax.jws.soap.SOAPBinding.ParameterStyle soapParameterStyle;
    // Default value per JSR-181 MR Sec 4.7 "Annotation: javax.jws.soap.SOAPBinding" pg 28
    public static final javax.jws.soap.SOAPBinding.ParameterStyle SOAPBinding_ParameterStyle_DEFAULT =
            javax.jws.soap.SOAPBinding.ParameterStyle.WRAPPED;

    /**
     * Add the operationDescription to the list of operations.  Note that we can not create the
     * list of dispatchable operations at this points.
     * @see #initializeDispatchableOperationsList()
     * 
     * @param operation The operation description to add to this endpoint interface
     */
    void addOperation(OperationDescription operation) {
        operationDescriptions.add(operation);
    }

    /**
     * Construct a service requester (aka client-side) EndpointInterfaceDescription for the
     * given SEI class.  This constructor is used if hierachy is being built fully from annotations
     * and not WSDL.
     * @param sei
     * @param parent
     */
    EndpointInterfaceDescriptionImpl(Class sei, EndpointDescriptionImpl parent) {
        parentEndpointDescription = parent;
        dbc = new DescriptionBuilderComposite();
        dbc.setCorrespondingClass(sei);

        // Per JSR-181 all methods on the SEI are mapped to operations regardless
        // of whether they include an @WebMethod annotation.  That annotation may
        // be present to customize the mapping, but is not required (p14)
        for (Method method : getSEIMethods(dbc.getCorrespondingClass())) {
            OperationDescription operation = new OperationDescriptionImpl(method, this);
            addOperation(operation);
        } 
    }

    /**
     * Construct a service requester (aka client-side) EndpointInterfaceDescrption for
     * an SEI represented by an AxisService.  This constructor is used if the hierachy is
     * being built fully from WSDL.  The AxisService and underlying AxisOperations were built
     * based on the WSDL, so we will use them to create the necessary objects.
     *
     * @param parent
     */
    EndpointInterfaceDescriptionImpl(EndpointDescriptionImpl parent) {
        parentEndpointDescription = parent;
        dbc = new DescriptionBuilderComposite();
        AxisService axisService = parentEndpointDescription.getAxisService();
        if (axisService != null) {
            ArrayList publishedOperations = axisService.getPublishedOperations();
            Iterator operationsIterator = publishedOperations.iterator();
            while (operationsIterator.hasNext()) {
                AxisOperation axisOperation = (AxisOperation)operationsIterator.next();
                addOperation(new OperationDescriptionImpl(axisOperation, this));
            }
        }
    }
    /**
     * Construct as Provider-based endpoint which does not have specific WSDL operations.  Since there
     * are no specific WSDL operations in this case, there will be a single generic operation that
     * will accept any incoming operation.
     * 
     * @param dbc
     * @param parent
     */
    EndpointInterfaceDescriptionImpl(DescriptionBuilderComposite dbc, 
                                     EndpointDescriptionImpl parent) {
        if (log.isDebugEnabled()) {
            log.debug("Creating a EndpointInterfaceDescription for a generic WSDL-less provider");
        }
        parentEndpointDescription = parent;
        this.dbc = dbc;
        
        // Construct the generic provider AxisOperation to use then construct
        // an OperactionDescription for it.
        AxisOperation genericProviderAxisOp = null;
        try {
            genericProviderAxisOp = 
                AxisOperationFactory.getOperationDescription(WSDLConstants.WSDL20_2006Constants.MEP_URI_IN_OUT);
        } catch (AxisFault e) {
            throw ExceptionFactory.makeWebServiceException(Messages.getMessage("eiDescrImplErr"),e);
        }
        
        genericProviderAxisOp.setName(new QName(JAXWS_NOWSDL_PROVIDER_OPERATION_NAME));
        OperationDescription opDesc = new OperationDescriptionImpl(genericProviderAxisOp, this);
        
        addOperation(opDesc);
        AxisService axisService = getEndpointDescription().getAxisService();
        axisService.addOperation(genericProviderAxisOp);
    }

    /**
     * Build an EndpointInterfaceDescription from a DescriptionBuilderComposite.  This EID has
     * WSDL operations associated with it.  It could represent an SEI-based endpoint built from
     * WSDL or annotations, OR it could represent a Provider-based enpoint built from WSDL.  It will
     * not represent a Provider-based endpoint built without WSDL (which does not know about
     * specific WSDL operations). For that type of EID, see:
     * @see  #EndpointInterfaceDescriptionImpl(DescriptionBuilderComposite dbc, EndpointDescriptionImpl parent)
     * @param dbc
     * @param isClass
     * @param parent
     */
    EndpointInterfaceDescriptionImpl(DescriptionBuilderComposite dbc,
                                     boolean isClass,
                                     EndpointDescriptionImpl parent) {

        parentEndpointDescription = parent;
        this.dbc = dbc;

        getEndpointDescription().getAxisService()
                .setTargetNamespace(getEndpointDescriptionImpl().getTargetNamespace());

        // Per JSR-181 all methods on the SEI are mapped to operations regardless
        // of whether they include an @WebMethod annotation.  That annotation may
        // be present to customize the mapping, but is not required (p14)

        //We are processing the SEI composite
        //For every MethodDescriptionComposite in this list, call OperationDescription 
        //constructor for it, then add this operation

        //Retrieve the relevent method composites for this dbc (and those in the superclass chain)
        Iterator<MethodDescriptionComposite> iter = retrieveReleventMethods(dbc);

        if (log.isDebugEnabled())
            log.debug("EndpointInterfaceDescriptionImpl: Finished retrieving methods");
        MethodDescriptionComposite mdc = null;

        while (iter.hasNext()) {
            mdc = iter.next();

            mdc.setDeclaringClass(dbc.getClassName());

            // Only add if it is a method that would be or is in the WSDL i.e. 
            // don't create an OperationDescriptor for the MDC representing the
            // constructor
            if (DescriptionUtils.createOperationDescription(mdc.getMethodName())) {
                //First check if this operation already exists on the AxisService, if so
                //then use that in the description hierarchy

                AxisService axisService = getEndpointDescription().getAxisService();
                AxisOperation axisOperation = axisService
                        .getOperation(OperationDescriptionImpl.determineOperationQName(mdc));

                OperationDescription operation =
                        new OperationDescriptionImpl(mdc, this, axisOperation);

                if (axisOperation == null) {
                    // This axisOperation did not already exist on the AxisService, and so was created
                    // with the OperationDescription, so we need to add the operation to the service
                    ((OperationDescriptionImpl)operation).addToAxisService(axisService);
                }

                if (log.isDebugEnabled())
                    log.debug("EID: Just added operation= " + operation.getOperationName());
                addOperation(operation);
            }

        }

        if (log.isDebugEnabled())
            log.debug("EndpointInterfaceDescriptionImpl: Finished Adding operations");

    }


    private static Method[] getSEIMethods(Class sei) {
        // Per JSR-181 all methods on the SEI are mapped to operations regardless
        // of whether they include an @WebMethod annotation.  That annotation may
        // be present to customize the mapping, but is not required (p14)
        Method[] seiMethods = sei.getMethods();
        ArrayList methodList = new ArrayList();
        if (sei != null) {
            for (Method method : seiMethods) {

                if (method.getDeclaringClass().getName().equals("java.lang.Object")) {
                    continue;
                }
                methodList.add(method);
                if (!Modifier.isPublic(method.getModifiers())) {
                    // JSR-181 says methods must be public (p14)
                    throw ExceptionFactory.makeWebServiceException(Messages.getMessage("seiMethodsErr"));
                }
                // TODO: other validation per JSR-181
            }

        }
        return (Method[])methodList.toArray(new Method[methodList.size()]);
//        return seiMethods;
    }

    /**
     * Update a previously created EndpointInterfaceDescription with information from an annotated
     * SEI.  This should only be necessary when the this was created with WSDL. In this case, the
     * information from the WSDL is augmented based on the annotated SEI.
     *
     * @param sei
     */
    void updateWithSEI(Class sei) {
        Class seiClass = dbc.getCorrespondingClass();
        if (seiClass != null && seiClass != sei) {
            throw ExceptionFactory.makeWebServiceException(new UnsupportedOperationException(Messages.getMessage("seiProcessingErr")));
        }
        else if (seiClass != null && seiClass == sei) {
            // We've already done the necessary updates for this SEI
            return;
        }
        else if (sei != null) {
            seiClass = sei;
            dbc.setCorrespondingClass(sei);
            // Update (or possibly add) the OperationDescription for each of the methods on the SEI.
            for (Method seiMethod : getSEIMethods(seiClass)) {

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

                // Get the QName for this java method and then update (or add) the appropriate OperationDescription
                // See comments below for imporant notes about the current implementation.
                // NOTE ON OVERLOADED OPERATIONS
                // Axis2 does NOT currently support overloading WSDL operations.
                QName seiOperationQName =
                        OperationDescriptionImpl.determineOperationQName(seiMethod);
                OperationDescription[] updateOpDesc = getOperation(seiOperationQName);
                if (updateOpDesc == null || updateOpDesc.length == 0) {
                    // This operation wasn't defined in the WSDL.  Note that the JAX-WS async methods
                    // which are defined on the SEI are not defined as operations in the WSDL.
                    // Although they usually specific the same OperationName as the WSDL operation, 
                    // there may be cases where they do not.
                    OperationDescription operation = new OperationDescriptionImpl(seiMethod, this);
                    addOperation(operation);
                } else {
                    // Currently Axis2 does not support overloaded operations.  That means that even if the WSDL
                    // defined overloaded operations, there would still only be a single AxisOperation, and it
                    // would be the last operation encounterd.
                    // HOWEVER the generated JAX-WS async methods (see above) may (will always?) have the same
                    // operation name and so will come down this path; they need to be added.
                    // TODO: When Axis2 starts supporting overloaded operations, then this logic will need to be changed

                    // Loop through all the opdescs; if one doesn't currently have a java method set, set it
                    // If all have java methods set, then add a new one.  Assume we'll need to add a new one.
                    boolean addOpDesc = true;
                    for (OperationDescription checkOpDesc : updateOpDesc) {
                        if (checkOpDesc.getSEIMethod() == null) {
                            // TODO: Should this be checking (somehow) that the signature matches?  Probably not an issue until overloaded WSDL ops are supported.
                            
                            //Make sure that this is not one of the 'async' methods associated with
                            //this operation. If it is, let it be created as its own opDesc.
                            if (!DescriptionUtils.isAsync(seiMethod)) {
                                ((OperationDescriptionImpl) checkOpDesc).setSEIMethod(seiMethod);
                                addOpDesc = false;
                                break;
                            }
                        }
                    }
                    if (addOpDesc) {
                        OperationDescription operation =
                                new OperationDescriptionImpl(seiMethod, this);
                        addOperation(operation);
                    }
                }
            }
        }
    }

    /**
     * Return the OperationDescriptions corresponding to a particular Java method name. Note that an
     * array is returned because a method could be overloaded.
     *
     * @param javaMethodName String representing a Java Method Name
     * @return
     */
    public OperationDescription[] getOperationForJavaMethod(String javaMethodName) {
        if (DescriptionUtils.isEmpty(javaMethodName)) {
            return null;
        }

        ArrayList<OperationDescription> matchingOperations = new ArrayList<OperationDescription>();
        for (OperationDescription operation : getOperations()) {
            if (javaMethodName.equals(operation.getJavaMethodName())) {
                matchingOperations.add(operation);
            }
        }

        if (matchingOperations.size() == 0)
            return null;
        else
            return matchingOperations.toArray(new OperationDescription[0]);
    }

    /**
     * Return the OperationDesription (only one) corresponding to the OperationName passed in.
     *
     * @param operationName
     * @return
     */
    public OperationDescription getOperation(String operationName) {
        if (DescriptionUtils.isEmpty(operationName)) {
            return null;
        }

        OperationDescription matchingOperation = null;
        for (OperationDescription operation : getOperations()) {
            if (operationName.equals(operation.getOperationName())) {
                matchingOperation = operation;
                break;
            }
        }
        return matchingOperation;
    }

    public OperationDescription[] getOperations() {
        return operationDescriptions.toArray(new OperationDescription[0]);
    }

    EndpointDescriptionImpl getEndpointDescriptionImpl() {
        return (EndpointDescriptionImpl)parentEndpointDescription;
    }

    public EndpointDescription getEndpointDescription() {
        return parentEndpointDescription;
    }

    /**
     * Return an array of Operations given an operation QName.  Note that an array is returned since
     * a WSDL operation may be overloaded per JAX-WS.
     *
     * @param operationQName
     * @return
     */
    public OperationDescription[] getOperation(QName operationQName) {
        OperationDescription[] returnOperations = null;
        if (!DescriptionUtils.isEmpty(operationQName)) {
            ArrayList<OperationDescription> matchingOperations =
                    new ArrayList<OperationDescription>();
            OperationDescription[] allOperations = getOperations();
            for (OperationDescription operation : allOperations) {
                if (operation.getName().getLocalPart().equals(operationQName.getLocalPart())) {
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

    /* (non-Javadoc)
    * @see org.apache.axis2.jaxws.description.EndpointInterfaceDescription#getDispatchableOperation(QName operationQName)
    */
    public OperationDescription[] getDispatchableOperation(QName operationQName) {
        // REVIEW: Can this be synced at a more granular level?  Can't sync on dispatchableOperations because
        //         it may be null, but also the initialization must finish before next thread sees 
        //         dispatachableOperations != null
        synchronized(this) {
            if (dispatchableOperations == null) {
                initializeDispatchableOperationsList();
            }
        }

        // Note that OperationDescriptionImpl creates operation qname with empty namespace. Thus 
        // using only the localPart to get dispatchable operations.
    	QName key = new QName("",operationQName.getLocalPart());
    	List<OperationDescription> operations = dispatchableOperations.get(key);
    	if(operations!=null){
    		return operations.toArray(new OperationDescription[operations.size()]);
    	}
    	return new OperationDescription[0];
    }
    /* (non-Javadoc)
     * @see org.apache.axis2.jaxws.description.EndpointInterfaceDescription#getDispatchableOperations()
     */
    public OperationDescription[] getDispatchableOperations() {
        OperationDescription[] returnOperations = null;

        // REVIEW: Can this be synced at a more granular level?  Can't sync on dispatchableOperations because
        //         it may be null, but also the initialization must finish before next thread sees 
        //         dispatachableOperations != null
        synchronized(this) {
            if (dispatchableOperations == null) {
                initializeDispatchableOperationsList();
            }
        }
        Collection<List<OperationDescription>> dispatchableValues = dispatchableOperations.values();
        Iterator<List<OperationDescription>> iteratorValues = dispatchableValues.iterator();
        ArrayList<OperationDescription> allDispatchableOperations = new ArrayList<OperationDescription>();
        while (iteratorValues.hasNext()) {
            List<OperationDescription> opDescList = iteratorValues.next();
            allDispatchableOperations.addAll(opDescList);
        }
        if (allDispatchableOperations.size() > 0) {
            returnOperations = allDispatchableOperations.toArray(new OperationDescription[allDispatchableOperations.size()]);
        }
        return returnOperations;
    }

    /**
     * Create the list of dispatchable operations from the list of all the operations.  A 
     * dispatchable operation is one that can be invoked on the endpoint, so it DOES NOT include:
     * - JAXWS Client Async methods
     * - Methods that have been excluded via WebMethod.exclude annotation
     *
     * Note: We have to create the list of dispatchable operations in a lazy way; we can't
     * create it as the operations are added via addOperations() because on the client
     * that list is built in two parts; first using AxisOperations from the WSDL, which will
     * not have any annotation information (such as WebMethod.exclude).  That list will then
     *  be updated with SEI information, which is the point annotation information becomes
     *  available.
     */
    private void initializeDispatchableOperationsList() {
        dispatchableOperations = new HashMap<QName, List<OperationDescription>>();
        OperationDescription[] opDescs = getOperations();
        for (OperationDescription opDesc : opDescs) {
          if (!opDesc.isJAXWSAsyncClientMethod() && !opDesc.isExcluded()) {
              List<OperationDescription> dispatchableOperationsWithName = dispatchableOperations.get(opDesc.getName());
              if(dispatchableOperationsWithName == null) {
                  dispatchableOperationsWithName = new ArrayList<OperationDescription>();
                  dispatchableOperations.put(opDesc.getName(), dispatchableOperationsWithName);
              }
              dispatchableOperationsWithName.add(opDesc);
          }
        }
    }

    /**
     * Return an OperationDescription for the corresponding SEI method.  Note that this ONLY works
     * if the OperationDescriptions were created from introspecting an SEI.  If the were created
     * with a WSDL then use the getOperation(QName) method, which can return > 1 operation.
     *
     * @param seiMethod The java.lang.Method from the SEI for which an OperationDescription is
     *                  wanted
     * @return
     */
    public OperationDescription getOperation(Method seiMethod) {
        OperationDescription returnOperation = null;
        if (seiMethod != null) {
            OperationDescription[] allOperations = getOperations();
            for (OperationDescription operation : allOperations) {
                if (operation.getSEIMethod() != null && operation.getSEIMethod().equals(seiMethod))
                {
                    returnOperation = operation;
                }
            }
        }
        return returnOperation;
    }

    public Class getSEIClass() {
        return dbc.getCorrespondingClass();
    }
    // Annotation-realted getters

    // ========================================
    // SOAP Binding annotation realted methods
    // ========================================
    public SOAPBinding getAnnoSoapBinding() {
        if (soapBindingAnnotation == null) {
            soapBindingAnnotation = dbc.getSoapBindingAnnot();
        }
        return soapBindingAnnotation;
    }

    public javax.jws.soap.SOAPBinding.Style getSoapBindingStyle() {
        return getAnnoSoapBindingStyle();
    }

    public javax.jws.soap.SOAPBinding.Style getAnnoSoapBindingStyle() {
        if (soapBindingStyle == null) {
            if (getAnnoSoapBinding() != null && getAnnoSoapBinding().style() != null) {
                soapBindingStyle = getAnnoSoapBinding().style();
            } else {
                soapBindingStyle = SOAPBinding_Style_DEFAULT;
            }
        }
        return soapBindingStyle;
    }

    public javax.jws.soap.SOAPBinding.Use getSoapBindingUse() {
        return getAnnoSoapBindingUse();
    }

    public javax.jws.soap.SOAPBinding.Use getAnnoSoapBindingUse() {
        if (soapBindingUse == null) {
            if (getAnnoSoapBinding() != null && getAnnoSoapBinding().use() != null) {
                soapBindingUse = getAnnoSoapBinding().use();
            } else {
                soapBindingUse = SOAPBinding_Use_DEFAULT;
            }
        }
        return soapBindingUse;
    }

    public javax.jws.soap.SOAPBinding.ParameterStyle getSoapBindingParameterStyle() {
        return getAnnoSoapBindingParameterStyle();
    }

    public javax.jws.soap.SOAPBinding.ParameterStyle getAnnoSoapBindingParameterStyle() {
        if (soapParameterStyle == null) {
            if (getAnnoSoapBinding() != null && getAnnoSoapBinding().parameterStyle() != null) {
                soapParameterStyle = getAnnoSoapBinding().parameterStyle();
            } else {
                soapParameterStyle = SOAPBinding_ParameterStyle_DEFAULT;
            }
        }
        return soapParameterStyle;
    }

    /*
    * Returns a non-null (possibly empty) list of MethodDescriptionComposites
    */
    Iterator<MethodDescriptionComposite> retrieveReleventMethods(DescriptionBuilderComposite dbc) {

        /*
        * Depending on whether this is an implicit SEI or an actual SEI, Gather up and build a
        * list of MDC's. If this is an actual SEI, then starting with this DBC, build a list of all
        * MDC's that are public methods in the chain of extended classes.
        * If this is an implicit SEI, then starting with this DBC,
        *  1. If a false exclude is found, then take only those that have false excludes
        *  2. Assuming no false excludes, take all public methods that don't have exclude == true
        *  3. For each super class, if 'WebService' present, take all MDC's according to rules 1&2
        *    But, if WebService not present, grab only MDC's that are annotated.
        */
        if (log.isTraceEnabled()) {
            log.trace("retrieveReleventMethods: Enter");
        }

        ArrayList<MethodDescriptionComposite> retrieveList =
                new ArrayList<MethodDescriptionComposite>();

        if (dbc.isInterface()) {
            if(log.isDebugEnabled()) {
                log.debug("Removing overridden methods for interface: " + dbc.getClassName() + 
                          " with super interface: " + dbc.getSuperClassName());
            }
            
            // make sure we retrieve all the methods, then remove the overridden
            // methods that exist in the base interface
            retrieveList = retrieveSEIMethodsChain(dbc);
            retrieveList = removeOverriddenMethods(retrieveList, dbc);
            
        } else {
            //this is an implied SEI...rules are more complicated

            retrieveList = retrieveImplicitSEIMethods(dbc);

            //Now, continue to build this list with relevent methods in the chain of
            //superclasses. If the logic for processing superclasses is the same as for
            //the original SEI, then we can combine this code with above code. But, its possible
            //the logic is different for superclasses...keeping separate for now.
            DescriptionBuilderComposite tempDBC = dbc;

            while (!DescriptionUtils.isEmpty(tempDBC.getSuperClassName())) {

                //verify that this superclass name is not
                //      java.lang.object, if so, then we're done processing
                if (DescriptionUtils.javifyClassName(tempDBC.getSuperClassName())
                        .equals(MDQConstants.OBJECT_CLASS_NAME))
                    break;

                DescriptionBuilderComposite superDBC =
                        getEndpointDescriptionImpl().getServiceDescriptionImpl().getDBCMap()
                                .get(tempDBC.getSuperClassName());

                if (log.isTraceEnabled())
                    log.trace("superclass name for this DBC is:" + tempDBC.getSuperClassName());

                //Verify that we can find the SEI in the composite list
                if (superDBC == null) {
                    throw ExceptionFactory.makeWebServiceException(Messages.getMessage("seiNotFoundErr"));
                }

                //If the superclass contains a WebService annotation then retrieve its methods
                //as we would for the impl class, otherwise ignore the methods of this
                //superclass
                if (superDBC.getWebServiceAnnot() != null) {
                    //Now, gather the list of Methods just like we do for the lowest subclass
                    retrieveList.addAll(retrieveImplicitSEIMethods(superDBC));
                }
                tempDBC = superDBC;
            } //Done with implied SEI's superclasses
            retrieveList = removeOverriddenMethods(retrieveList, dbc);
        }//Done with implied SEI's

        return retrieveList.iterator();
    }


    /**
     * This method will establish a <code>HashMap</code> that represents a class name of a composite
     * and an integer value for the entry. The integer represents the classes level in the Java
     * hierarchy. 0 represents the most basic class with n representing the highest level class.
     *
     * @param dbc - <code>DescriptionBuilderComposite</code>
     * @return - <code>HashMap</code>
     */
    private HashMap<String, Integer> getClassHierarchy(DescriptionBuilderComposite dbc) {
        HashMap<String, DescriptionBuilderComposite> dbcMap = getEndpointDescriptionImpl().
                getServiceDescriptionImpl().getDBCMap();
        HashMap<String, Integer> hierarchyMap = new HashMap<String, Integer>();
        if (log.isDebugEnabled()) {
            log.debug("Putting class at base level: " + dbc.getClassName());
        }
        hierarchyMap.put(dbc.getClassName(), Integer.valueOf(0));
        DescriptionBuilderComposite superDBC = dbcMap.get((dbc.getSuperClassName()));
        int i = 1;
        while (superDBC != null && !superDBC.getClassName().equals("java.lang.Object")) {
            hierarchyMap.put(superDBC.getClassName(), Integer.valueOf(i));
            if (log.isDebugEnabled()) {
                log.debug("Putting class: " + superDBC.getClassName() + " at hierarchy rank: " +
                        i);
            }
            i++;
            superDBC = dbcMap.get(superDBC.getSuperClassName());
        }
        return hierarchyMap;
    }
    
    /**
     * This method drives the establishment of the hierarchy of interfaces for an SEI.
     */
    private Map<String, Integer> getInterfaceHierarchy(DescriptionBuilderComposite dbc) {
        if(log.isDebugEnabled()) {
            log.debug("Getting interface hierarchy for: " + dbc.getClassName());
        }
        Map<String, Integer> hierarchyMap = new HashMap<String, Integer>();
        hierarchyMap.put(dbc.getClassName(), 0);
        return getInterfaceHierarchy(dbc.getInterfacesList(), 
                                     hierarchyMap, 
                                     1);
    }

    /**
     * Recursive method that builds the hierarchy of interfaces. This begins with an
     * SEI and walks all of its super interfaces.
     */
    private Map<String, Integer> getInterfaceHierarchy(List<String> interfaces,
                                                           Map<String, Integer> hierarchyMap,
                                                           int level) {
        HashMap<String, DescriptionBuilderComposite> dbcMap = getEndpointDescriptionImpl().
            getServiceDescriptionImpl().getDBCMap();
        
        // walk through all of the interfaces
        if(interfaces != null
                &&
                !interfaces.isEmpty()) {
            for(String interfaze : interfaces) {
                DescriptionBuilderComposite interDBC = dbcMap.get(interfaze);
                if(interDBC != null) {
                    if(log.isDebugEnabled()) {
                        log.debug("Inserting super interface " + interDBC.getClassName() + 
                                  " at level " + level);
                    }
                    hierarchyMap.put(interDBC.getClassName(), level);
                    return getInterfaceHierarchy(interDBC.getInterfacesList(), hierarchyMap, level++);
                }
            }
        }
        return hierarchyMap;
    }

    /**
     * This method will loop through each method that was previously determined as being relevant to
     * the current composite. It will then drive the call to determine if this represents a method
     * that has been overridden. If it represents an overriding method declaration it will remove
     * the inherited methods from the list leaving only the most basic method declaration.
     *
     * @param methodList - <code>ArrayList</code> list of relevant methods
     * @param dbc        - <code>DescriptionBuilderComposite</code> current composite
     * @return - <code>ArrayList</code>
     */
    private ArrayList<MethodDescriptionComposite> removeOverriddenMethods(
            ArrayList<MethodDescriptionComposite>
                    methodList, DescriptionBuilderComposite dbc) {
        Map<String, Integer> hierarchyMap = dbc.isInterface() ? getInterfaceHierarchy(dbc) : 
            getClassHierarchy(dbc);
        ArrayList<MethodDescriptionComposite> returnMethods =
                new ArrayList<MethodDescriptionComposite>();
        for (int i = 0; i < methodList.size(); i++) {
            if (notFound(returnMethods, methodList.get(i))) {
                returnMethods.add(getBaseMethod(methodList.get(i), i, methodList, hierarchyMap));
            }

        }
        return returnMethods;
    }

    /**
     * This method will loop through each method we have already identified as a base method and
     * compare the current method.
     *
     * @param mdcList - <code>ArrayList</code> identified base methods
     * @param mdc     - <code>MethodDescriptionComposite</code> current method
     * @return - boolean
     */
    private boolean notFound(ArrayList<MethodDescriptionComposite> mdcList,
                             MethodDescriptionComposite mdc) {
        for (MethodDescriptionComposite method : mdcList) {
            if (mdc.compare(method)) {
                return false;
            }
        }
        return true;
    }

    /**
     * This method is responsible for determining the most basic level of a method declaration in
     * the <code>DescriptionBuilderComposite</code> hierarchy.
     *
     * @param mdc          - <code>MethodDescriptionComposite</code> current method
     * @param index        - <code>int</code> current location in method list
     * @param methodList   - <code>List</code> list of methods available on this composite
     * @param hierarchyMap - <code>HashMap</code> map that represents the hierarchy of the current
     *                     <code>DescriptionBuilderComposite</code>
     * @return - <code>MethodDescriptionComposite</code> most basic method declaration
     */
    private static MethodDescriptionComposite getBaseMethod(MethodDescriptionComposite mdc,
                                                            int index,
                                                            ArrayList<MethodDescriptionComposite> methodList,
                                                            Map<String, Integer>
                                                                    hierarchyMap) {
        int baseLevel = hierarchyMap.get(mdc.getDeclaringClass());
        if (log.isDebugEnabled()) {
            log.debug("Base method: " + mdc.getMethodName() + " initial level: " + baseLevel);
        }
        for (; index < methodList.size(); index++) {
            MethodDescriptionComposite compareMDC = methodList.get(index);
            // If the two methods are the same method that means we have found an inherited
            // overridden case
            if (mdc.equals(compareMDC)) {
                if (log.isDebugEnabled()) {
                    log.debug("Found equivalent methods: " + mdc.getMethodName());
                }
                // get the declaration level of the method we are comparing to
                int compareLevel = hierarchyMap.get(compareMDC.getDeclaringClass());
                // if the method was declared by a class in a lower level of the hierarchy it
                // becomes the method that we will compare other methods to
                if (compareLevel < baseLevel) {
                    if (log.isDebugEnabled()) {
                        log.debug("Found method lower in hierarchy chain: " +
                                compareMDC.getMethodName()
                                + " of class: " + compareMDC.getMethodName());
                    }
                    mdc = compareMDC;
                    baseLevel = compareLevel;
                }
            }
        }
        return mdc;
    }

    /*
    * This is called when we know that this DBC is an implicit SEI
    */
    private ArrayList<MethodDescriptionComposite> retrieveImplicitSEIMethods(
            DescriptionBuilderComposite dbc) {

        ArrayList<MethodDescriptionComposite> retrieveList =
                new ArrayList<MethodDescriptionComposite>();

        retrieveList = DescriptionUtils.getMethodsWithFalseExclusions(dbc);

        //If this list is empty, then there are no false exclusions, so gather
        //all composites that don't have exclude == true
        //If the list is not empty, then it means we found at least one method with 'exclude==false'
        //so the list should contain only those methods
        if (retrieveList == null || retrieveList.size() == 0) {
            Iterator<MethodDescriptionComposite> iter = null;
            List<MethodDescriptionComposite> mdcList = dbc.getMethodDescriptionsList();

            if (mdcList != null) {
                iter = dbc.getMethodDescriptionsList().iterator();
                while (iter.hasNext()) {
                    MethodDescriptionComposite mdc = iter.next();

                    if (!DescriptionUtils.isExcludeTrue(mdc)) {
                        mdc.setDeclaringClass(dbc.getClassName());
                        retrieveList.add(mdc);
                    }
                }
            }
        }

        return retrieveList;
    }

    private ArrayList<MethodDescriptionComposite> retrieveSEIMethods(
            DescriptionBuilderComposite dbc) {

        //Rules for retrieving Methods on an SEI (or a superclass of an SEI) are simple
        //Just retrieve all methods regardless of WebMethod annotations
        ArrayList<MethodDescriptionComposite> retrieveList =
                new ArrayList<MethodDescriptionComposite>();

        Iterator<MethodDescriptionComposite> iter = null;
        List<MethodDescriptionComposite> mdcList = dbc.getMethodDescriptionsList();

        if (mdcList != null) {
            iter = dbc.getMethodDescriptionsList().iterator();
            while (iter.hasNext()) {
                MethodDescriptionComposite mdc = iter.next();
                mdc.setDeclaringClass(dbc.getClassName());
                retrieveList.add(mdc);
            }
        }

        return retrieveList;
    }

    private ArrayList<MethodDescriptionComposite> retrieveSEIMethodsChain(
            DescriptionBuilderComposite tmpDBC) {

        DescriptionBuilderComposite dbc = tmpDBC;
        ArrayList<MethodDescriptionComposite> retrieveList =
                new ArrayList<MethodDescriptionComposite>();

        retrieveList = retrieveSEIMethods(dbc);

        //Since this is an interface, anything that is in the extends clause will actually appear
        // in the interfaces list instead.
        Iterator<String> iter = null;
        List<String> interfacesList = dbc.getInterfacesList();
        if (interfacesList != null) {
            iter = dbc.getInterfacesList().iterator();

            while (iter.hasNext()) {

                String interfaceName = iter.next();
                DescriptionBuilderComposite superInterface =
                        getEndpointDescriptionImpl().getServiceDescriptionImpl().getDBCMap()
                                .get(interfaceName);

                retrieveList.addAll(retrieveSEIMethodsChain(superInterface));
            }
        }

        return retrieveList;
    }

    private Definition getWSDLDefinition() {
        return ((ServiceDescriptionWSDL)getEndpointDescription().getServiceDescription())
                .getWSDLDefinition();
    }

    public PortType getWSDLPortType() {
        PortType portType = null;
//        EndpointDescriptionWSDL endpointDescWSDL = (EndpointDescriptionWSDL) getEndpointDescription();
//        Binding wsdlBinding = endpointDescWSDL.getWSDLBinding();
//        if (wsdlBinding != null) {
//            portType = wsdlBinding.getPortType();
//        }
        Definition wsdlDefn = getWSDLDefinition();
        if (wsdlDefn != null) {
            String tns = getEndpointDescription().getTargetNamespace();
            String localPart = getEndpointDescription().getName();
            if (localPart != null) {
                portType = wsdlDefn.getPortType(new QName(tns, localPart));
            }
        }
        return portType;
    }


    public String getTargetNamespace() {
        return getAnnoWebServiceTargetNamespace();
    }

    public WebService getAnnoWebService() {
        if (webServiceAnnotation == null) {
            webServiceAnnotation = dbc.getWebServiceAnnot();
        }
        return webServiceAnnotation;
    }

    public String getAnnoWebServiceTargetNamespace() {
        if (webServiceTargetNamespace == null) {
            if (getAnnoWebService() != null
                    && !DescriptionUtils.isEmpty(getAnnoWebService().targetNamespace())) {
                webServiceTargetNamespace = getAnnoWebService().targetNamespace();
            } else {
                // Default value per JSR-181 MR Sec 4.1 pg 15 defers to "Implementation defined, 
                // as described in JAX-WS 2.0, section 3.2" which is JAX-WS 2.0 Sec 3.2, pg 29.
                webServiceTargetNamespace =
                    DescriptionUtils.makeNamespaceFromPackageName(
                            DescriptionUtils.getJavaPackageName(dbc.getClassName()),
                            "http");
            }
        }
        return webServiceTargetNamespace;
    }

    public String getAnnoWebServiceName() {
        if (webService_Name == null) {

            if (getAnnoWebService() != null
                    && !DescriptionUtils.isEmpty(getAnnoWebService().name())) {
                webService_Name = getAnnoWebService().name();
            } else {
                // Per the JSR 181 Specification, the default
                // is the simple name of the class.
                webService_Name = DescriptionUtils.getSimpleJavaClassName(dbc.getClassName());
            }
        }
        return webService_Name;
    }

    public String getName() {
        return getAnnoWebServiceName();
    }

    public QName getPortType() {
        String name = getName();
        String tns = getTargetNamespace();
        return new QName(tns, name);
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
            string.append("PortType: " + getPortType());
            //
            string.append(newline);
            string.append("SOAP Style: " + getSoapBindingStyle());
            string.append(sameline);
            string.append("SOAP Use: " + getSoapBindingUse());
            string.append(sameline);
            string.append("SOAP Paramater Style: " + getSoapBindingParameterStyle());
            //
            string.append(newline);
            OperationDescription[] operations = getOperations();
            if (operations != null && operations.length > 0) {
                string.append("Number of operations: " + operations.length);
                for (OperationDescription operation : operations) {
                    string.append(newline);
                    string.append("Operation: " + operation.toString());
                }
            } else {
                string.append("OperationDescription array is null");
            }
        }
        catch (Throwable t) {
            string.append(newline);
            string.append("Complete debug information not currently available for " +
                    "EndpointInterfaceDescription");
            return string.toString();
        }
        return string.toString();
    }
    /**
     * Get an annotation.  This is wrappered to avoid a Java2Security violation.
     * @param cls Class that contains annotation 
     * @param annotation Class of requrested Annotation
     * @return annotation or null
     */
    private static Annotation getAnnotation(final Class cls, final Class annotation) {
        return (Annotation) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                return cls.getAnnotation(annotation);
            }
        });
    }
}
