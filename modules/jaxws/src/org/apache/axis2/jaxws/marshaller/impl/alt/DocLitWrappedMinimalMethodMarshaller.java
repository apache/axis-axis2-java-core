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

package org.apache.axis2.jaxws.marshaller.impl.alt;

import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.EndpointInterfaceDescription;
import org.apache.axis2.jaxws.description.OperationDescription;
import org.apache.axis2.jaxws.description.ParameterDescription;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.marshaller.MethodMarshaller;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.Protocol;
import org.apache.axis2.jaxws.message.factory.MessageFactory;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.axis2.jaxws.runtime.description.marshal.MarshalServiceRuntimeDescription;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jws.soap.SOAPBinding.Style;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;
import java.util.List;
import java.util.TreeSet;

/**
 * The Doc/Lit Wrapped Minimal Marshaller is used when 1) The web service is Doc/Lit Wrapped, and 2)
 * The wrapper and fault bean objects are missing (hence the term 'Minimal')
 */
public class DocLitWrappedMinimalMethodMarshaller implements MethodMarshaller {

    private static Log log = LogFactory.getLog(DocLitWrappedMinimalMethodMarshaller.class);

    public DocLitWrappedMinimalMethodMarshaller() {
        super();
    }

    public Message marshalRequest(Object[] signatureArguments, OperationDescription operationDesc)
            throws WebServiceException {

        EndpointInterfaceDescription ed = operationDesc.getEndpointInterfaceDescription();
        EndpointDescription endpointDesc = ed.getEndpointDescription();
        Protocol protocol = Protocol.getProtocolForBinding(endpointDesc.getClientBindingID());

        // Note all exceptions are caught and rethrown with a WebServiceException
        try {

            // Sample Document message
            // ..
            // <soapenv:body>
            //    <m:operation>
            //      <param>hello</param>
            //    </m:operation>
            // </soapenv:body>
            //
            // Important points.
            //   1) There is no operation element under the body.
            //   2) The data blocks are located underneath the body.  
            //   3) The name of the data block (m:operation) is defined by the schema and match the name of the operation.
            //      This is called the wrapper element.  The wrapper element has a corresponding JAXB element pojo.
            //   4) The parameters (m:param) are child elements of the wrapper element.
            //   5) NOTE: For doc/literal wrapped "minimal", the wrapper JAXB element pojo is missing.

            // Get the operation information
            ParameterDescription[] pds = operationDesc.getParameterDescriptions();
            MarshalServiceRuntimeDescription marshalDesc =
                    MethodMarshallerUtils.getMarshalDesc(endpointDesc);
            TreeSet<String> packages = marshalDesc.getPackages();

            // TODO This needs more work.  We need to check inside holders of input params.  We also
            // may want to exclude header params from this check
            //Validate input parameters for operation and make sure no input parameters are null.
            //As per JAXWS Specification section 3.6.2.3 if a null value is passes as an argument 
            //to a method then an implementation MUST throw WebServiceException.
            if (pds.length > 0) {
                if (signatureArguments == null) {
                	throw ExceptionFactory.makeWebServiceException(
                			Messages.getMessage("NullParamErr1",operationDesc.getJavaMethodName()));
                }
                if (signatureArguments != null) {
                    for (Object argument : signatureArguments) {
                        if (argument == null) {
                        	throw ExceptionFactory.makeWebServiceException(
                        			Messages.getMessage("NullParamErr1",operationDesc.getJavaMethodName()));
                        }
                    }
                }
            }

            // Create the message 
            MessageFactory mf = (MessageFactory)FactoryRegistry.getFactory(MessageFactory.class);
            Message m = mf.create(protocol);

            // Indicate the style and wrapper element name.  This triggers the message to
            // put the data blocks underneath the wrapper element
            m.setStyle(Style.DOCUMENT);
            m.setIndirection(1);
            m.setOperationElement(getRequestWrapperQName(operationDesc));

            // The input object represent the signature arguments.
            // Signature arguments are both holders and non-holders
            // Convert the signature into a list of JAXB objects for marshalling
            List<PDElement> pdeList =
                    MethodMarshallerUtils.getPDElements(marshalDesc,
                                                        pds,
                                                        signatureArguments,
                                                        true,  // input
                                                        true,  // doc/lit wrapped
                                                        true); // false

            // We want to use "by Java Type" marshalling for 
            // all objects
            for (PDElement pde : pdeList) {
                ParameterDescription pd = pde.getParam();
                Class type = pd.getParameterActualType();
                pde.setByJavaTypeClass(type);
            }

            // Put values onto the message
            MethodMarshallerUtils.toMessage(pdeList, m, packages);
            
            // Enable SWA for nested SwaRef attachments
            if (operationDesc.hasRequestSwaRefAttachments()) {
                m.setDoingSWA(true);
            }

            return m;
        } catch (Exception e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }

    public Object[] demarshalRequest(Message message, OperationDescription operationDesc)
            throws WebServiceException {

        EndpointInterfaceDescription ed = operationDesc.getEndpointInterfaceDescription();
        EndpointDescription endpointDesc = ed.getEndpointDescription();

        // Note all exceptions are caught and rethrown with a WebServiceException
        try {
            // Sample RPC message
            // ..
            // <soapenv:body>
            //    <m:op xmlns:m="urn://api">
            //       <param xsi:type="data:foo" >...</param>
            //    </m:op>
            // </soapenv:body>
            //
            // Important points.
            //   1) RPC has an operation element under the body.  This is the name of the
            //      wsdl operation.
            //   2) The data blocks are located underneath the operation element.  (In doc/lit
            //      the data elements are underneath the body.
            //   3) The name of the data blocks (param) are defined by the wsdl:part not the
            //      schema.  Note that it is unqualified per WSI-BP
            //   4) The type of the data block (data:foo) is defined by schema (thus there is 
            //      JAXB type rendering.  
            //   5) We always send an xsi:type, but other vendor's may not.
            // Get the operation information
            ParameterDescription[] pds = operationDesc.getParameterDescriptions();
            MarshalServiceRuntimeDescription marshalDesc =
                    MethodMarshallerUtils.getMarshalDesc(endpointDesc);
            TreeSet<String> packages = marshalDesc.getPackages();

            // Indicate that the style is Document, but the blocks are underneath
            // the wrapper element
            message.setStyle(Style.DOCUMENT);
            message.setIndirection(1);

            // We want to use "by Java Type" unmarshalling for 
            // all objects
            Class[] javaTypes = new Class[pds.length];
            for (int i = 0; i < pds.length; i++) {
                ParameterDescription pd = pds[i];
                javaTypes[i] = pd.getParameterActualType();
            }

            // Unmarshal the ParamValues from the Message
            List<PDElement> pvList = MethodMarshallerUtils.getPDElements(pds,
                                                                         message,
                                                                         packages,
                                                                         true, // input
                                                                         false,
                                                                         javaTypes);  // sigh...unmarshal by type because there is no wrapper

            // Build the signature arguments
            Object[] sigArguments = MethodMarshallerUtils.createRequestSignatureArgs(pds, pvList);

            // TODO This needs more work.  We need to check inside holders of input params.  We also
            // may want to exclude header params from this check
            //Validate input parameters for operation and make sure no input parameters are null.
            //As per JAXWS Specification section 3.6.2.3 if a null value is passes as an argument 
            //to a method then an implementation MUST throw WebServiceException.
            if (sigArguments != null) {
                for (Object argument : sigArguments) {
                    if (argument == null) {
                        throw ExceptionFactory.makeWebServiceException(
                    			Messages.getMessage("NullParamErr2",operationDesc.getJavaMethodName()));
                    }
                }
            }
            return sigArguments;
        } catch (Exception e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }


    public Message marshalResponse(Object returnObject, Object[] signatureArgs,
                                   OperationDescription operationDesc, Protocol protocol)
            throws WebServiceException {


        EndpointInterfaceDescription ed = operationDesc.getEndpointInterfaceDescription();
        EndpointDescription endpointDesc = ed.getEndpointDescription();
        // We want to respond with the same protocol as the request,
        // It the protocol is null, then use the Protocol defined by the binding
        if (protocol == null) {
            protocol = Protocol.getProtocolForBinding(endpointDesc.getBindingType());
        }

        // Note all exceptions are caught and rethrown with a WebServiceException
        try {
            // Sample RPC message
            // ..
            // <soapenv:body>
            //    <m:opResponse xmlns:m="urn://api">
            //       <param xsi:type="data:foo" >...</param>
            //    </m:op>
            // </soapenv:body>
            //
            // Important points.
            //   1) RPC has an operation element under the body.  This is the name of the
            //      wsdl operation.
            //   2) The data blocks are located underneath the operation element.  (In doc/lit
            //      the data elements are underneath the body.
            //   3) The name of the data blocks (param) are defined by the wsdl:part not the
            //      schema.  Note that it is unqualified.
            //   4) The type of the data block (data:foo) is defined by schema (thus there is 
            //      JAXB type rendering.  Since we are using JAXB to marshal the data, 
            //      we always generate an xsi:type attribute.  This is an implemenation detail
            //      and is not defined by any spec.

            // Get the operation information
            ParameterDescription[] pds = operationDesc.getParameterDescriptions();
            MarshalServiceRuntimeDescription marshalDesc =
                    MethodMarshallerUtils.getMarshalDesc(endpointDesc);
            TreeSet<String> packages = marshalDesc.getPackages();

            // Create the message 
            MessageFactory mf = (MessageFactory)FactoryRegistry.getFactory(MessageFactory.class);
            Message m = mf.create(protocol);

            // Indicate the style and wrapper element name.  This triggers the message to
            // put the data blocks underneath the operation element
            m.setStyle(Style.DOCUMENT);
            m.setIndirection(1);
            QName responseOp = getResponseWrapperQName(operationDesc);
            m.setOperationElement(responseOp);

            // Put the return object onto the message
            Class returnType = operationDesc.getResultActualType();
            String returnNS = null;
            String returnLocalPart = null;
            if (operationDesc.isResultHeader()) {
                returnNS = operationDesc.getResultTargetNamespace();
                returnLocalPart = operationDesc.getResultName();
            } else {
                returnNS = operationDesc.getResultTargetNamespace();
                returnLocalPart = operationDesc.getResultPartName();
            }

            if (returnType != void.class) {

                // TODO should we allow null if the return is a header?
                //Validate input parameters for operation and make sure no input parameters are null.
                //As per JAXWS Specification section 3.6.2.3 if a null value is passes as an argument 
                //to a method then an implementation MUST throw WebServiceException.
                if (returnObject == null) {
                    throw ExceptionFactory.makeWebServiceException(
                			Messages.getMessage("NullParamErr3",operationDesc.getJavaMethodName()));

                }
                Element returnElement = null;
                QName returnQName = new QName(returnNS, returnLocalPart);
                if (marshalDesc.getAnnotationDesc(returnType).hasXmlRootElement()) {
                    returnElement = new Element(returnObject, returnQName);
                } else {
                    returnElement = new Element(returnObject, returnQName, returnType);
                }
                MethodMarshallerUtils.toMessage(returnElement,
                                                returnType,
                                                operationDesc.isListType(),
                                                marshalDesc,
                                                m,
                                                returnType, // force marshal by type
                                                operationDesc.isResultHeader());
            }

            // Convert the holder objects into a list of JAXB objects for marshalling
            List<PDElement> pdeList =
                    MethodMarshallerUtils.getPDElements(marshalDesc,
                                                        pds,
                                                        signatureArgs,
                                                        false,  // output
                                                        true,   // doc/lit wrapped
                                                        false); // not rpc

            // We want to use "by Java Type" marshalling for 
            // all objects
            for (PDElement pde : pdeList) {
                ParameterDescription pd = pde.getParam();
                Class type = pd.getParameterActualType();
                pde.setByJavaTypeClass(type);
            }

            // TODO Should we check for null output body values?  Should we check for null output header values ?
            // Put values onto the message
            MethodMarshallerUtils.toMessage(pdeList, m, packages);
            
            // Enable SWA for nested SwaRef attachments
            if (operationDesc.hasResponseSwaRefAttachments()) {
                m.setDoingSWA(true);
            }

            return m;
        } catch (Exception e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }


    public Object demarshalResponse(Message message, Object[] signatureArgs,
                                    OperationDescription operationDesc)
            throws WebServiceException {

        EndpointInterfaceDescription ed = operationDesc.getEndpointInterfaceDescription();
        EndpointDescription endpointDesc = ed.getEndpointDescription();

        // Note all exceptions are caught and rethrown with a WebServiceException
        try {
            // Sample RPC message
            // ..
            // <soapenv:body>
            //    <m:opResponse xmlns:m="urn://api">
            //       <param xsi:type="data:foo" >...</param>
            //    </m:op>
            // </soapenv:body>
            //
            // Important points.
            //   1) RPC has an operation element under the body.  This is the name of the
            //      wsdl operation.
            //   2) The data blocks are located underneath the operation element.  (In doc/lit
            //      the data elements are underneath the body.
            //   3) The name of the data blocks (param) are defined by the wsdl:part not the
            //      schema.  Note that it is unqualified per WSI-BP
            //   4) The type of the data block (data:foo) is defined by schema (thus there is 
            //      JAXB type rendering.  
            //   5) We always send an xsi:type, but other vendor's may not.
            // Get the operation information
            ParameterDescription[] pds = operationDesc.getParameterDescriptions();
            MarshalServiceRuntimeDescription marshalDesc =
                    MethodMarshallerUtils.getMarshalDesc(endpointDesc);
            TreeSet<String> packages = marshalDesc.getPackages();

            // Indicate that the style is Document. 
            message.setStyle(Style.DOCUMENT);
            message.setIndirection(1);

            // Get the return value.
            Class returnType = operationDesc.getResultActualType();
            Object returnValue = null;
            boolean hasReturnInBody = false;
            if (returnType != void.class) {
                // If the webresult is in the header, we need the name of the header so that we can find it.
                Element returnElement = null;
                if (operationDesc.isResultHeader()) {
                    returnElement = MethodMarshallerUtils.getReturnElement(packages,
                                                                           message,
                                                                           returnType,
                                                                           operationDesc.isListType(),
                                                                           true,  // is a header
                                                                           operationDesc.getResultTargetNamespace(),
                                                                           // header ns
                                                                           operationDesc.getResultPartName(),     // header local part
                                                                           MethodMarshallerUtils.numOutputBodyParams(pds) > 0);

                } else {
                    returnElement = MethodMarshallerUtils.getReturnElement(packages,
                                                                           message,
                                                                           returnType,
                                                                           operationDesc.isListType(),
                                                                           false,
                                                                           null,
                                                                           null,
                                                                           MethodMarshallerUtils.numOutputBodyParams(pds) > 0);
                    hasReturnInBody = true;

                }
                returnValue = returnElement.getTypeValue();
                // TODO should we allow null if the return is a header?
                //Validate input parameters for operation and make sure no input parameters are null.
                //As per JAXWS Specification section 3.6.2.3 if a null value is passes as an argument 
                //to a method then an implementation MUST throw WebServiceException.
                if (returnValue == null) {
                	throw ExceptionFactory.makeWebServiceException(
                			Messages.getMessage("NullParamErr3",operationDesc.getJavaMethodName()));
                }
            }

            // We want to use "by Java Type" unmarshalling for 
            // all objects
            Class[] javaTypes = new Class[pds.length];
            for (int i = 0; i < pds.length; i++) {
                ParameterDescription pd = pds[i];
                Class type = pd.getParameterActualType();
                javaTypes[i] = type;
            }

            // Unmarshall the ParamValues from the Message
            List<PDElement> pvList = MethodMarshallerUtils.getPDElements(pds,
                                                                         message,
                                                                         packages,
                                                                         false, // output
                                                                         hasReturnInBody,
                                                                         javaTypes); // unmarshal by type

            // TODO Should we check for null output body values?  Should we check for null output header values ?

            // Populate the response Holders
            MethodMarshallerUtils.updateResponseSignatureArgs(pds, pvList, signatureArgs);

            return returnValue;
        } catch (Exception e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }

    public Message marshalFaultResponse(Throwable throwable,
                                        OperationDescription operationDesc, Protocol protocol)
            throws WebServiceException {

        EndpointInterfaceDescription ed = operationDesc.getEndpointInterfaceDescription();
        EndpointDescription endpointDesc = ed.getEndpointDescription();
        MarshalServiceRuntimeDescription marshalDesc =
                MethodMarshallerUtils.getMarshalDesc(endpointDesc);
        TreeSet<String> packages = marshalDesc.getPackages();

        // We want to respond with the same protocol as the request,
        // It the protocol is null, then use the Protocol defined by the binding
        if (protocol == null) {
            protocol = Protocol.getProtocolForBinding(endpointDesc.getBindingType());
        }

        // Note all exceptions are caught and rethrown with a WebServiceException
        try {
            // Create the message 
            MessageFactory mf = (MessageFactory)FactoryRegistry.getFactory(MessageFactory.class);
            Message m = mf.create(protocol);

            // Put the fault onto the message
            MethodMarshallerUtils.marshalFaultResponse(throwable,
                                                       marshalDesc,
                                                       operationDesc,
                                                       m);
            return m;
        } catch (Exception e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }

    public Throwable demarshalFaultResponse(Message message, OperationDescription operationDesc)
            throws WebServiceException {

        EndpointInterfaceDescription ed = operationDesc.getEndpointInterfaceDescription();
        EndpointDescription endpointDesc = ed.getEndpointDescription();
        MarshalServiceRuntimeDescription marshalDesc =
                MethodMarshallerUtils.getMarshalDesc(endpointDesc);

        // Note all exceptions are caught and rethrown with a WebServiceException
        try {
            Throwable t = MethodMarshallerUtils
                    .demarshalFaultResponse(operationDesc, marshalDesc, message);
            return t;
        } catch (Exception e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }

    /**
     * @param opDesc
     * @return request wrapper qname
     */
    private static QName getRequestWrapperQName(OperationDescription opDesc) {

        QName qName = opDesc.getName();

        String localPart = opDesc.getRequestWrapperLocalName();
        String uri = opDesc.getRequestWrapperTargetNamespace();
        String prefix = "dlwmin";  // Prefer using an actual prefix


        qName = new QName(uri, localPart, prefix);
        return qName;
    }

    /**
     * @param opDesc
     * @return request wrapper qname
     */
    private static QName getResponseWrapperQName(OperationDescription opDesc) {

        QName qName = opDesc.getName();

        String localPart = opDesc.getResponseWrapperLocalName();
        String uri = opDesc.getResponseWrapperTargetNamespace();
        String prefix = "dlwmin";  // Prefer using an actual prefix


        qName = new QName(uri, localPart, prefix);
        return qName;
    }

}
