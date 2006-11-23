/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *      
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.axis2.jaxws.marshaller.impl.alt;

import java.util.List;
import java.util.Set;

import javax.jws.soap.SOAPBinding.Style;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;

import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.OperationDescription;
import org.apache.axis2.jaxws.description.ParameterDescription;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.marshaller.MethodMarshaller;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.Protocol;
import org.apache.axis2.jaxws.message.factory.MessageFactory;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class RPCLitMethodMarshaller implements MethodMarshaller {

    private static Log log = LogFactory.getLog(RPCLitMethodMarshaller.class);
    protected ServiceDescription serviceDesc = null;
    protected EndpointDescription endpointDesc = null;
    protected OperationDescription operationDesc = null;
    protected Protocol protocol = Protocol.soap11;
    
    
    public RPCLitMethodMarshaller(ServiceDescription serviceDesc, EndpointDescription endpointDesc, OperationDescription operationDesc, Protocol protocol) {
        super();
        this.serviceDesc = serviceDesc;
        this.endpointDesc = endpointDesc;
        this.operationDesc = operationDesc;
        this.protocol = protocol;
    }

    public Message marshalRequest(Object[] signatureArguments) throws WebServiceException {
        // Note all exceptions are caught and rethrown with a WebServiceException
        try {
            
            // Sample RPC message
            // ..
            // <soapenv:body>
            //    <m:op xmlns:m="urn://api">
            //       <m:param xsi:type="data:foo" xmlns:data="urn://mydata" >...</m:param>
            //    </m:op>
            // </soapenv:body>
            //
            // Important points.
            //   1) RPC has an operation element under the body.  This is the name of the
            //      wsdl operation.
            //   2) The data blocks are located underneath the operation element.  (In doc/lit
            //      the data elements are underneath the body.
            //   3) The name of the data blocks (m:param) are defined by the wsdl:part not the
            //      schema.  
            //   4) The type of the data block (data:foo) is defined by schema (thus there is 
            //      JAXB type rendering.  Since we are using JAXB to marshal the data, 
            //      we always generate an xsi:type attribute.  This is an implemenation detail
            //      and is not defined by any spec.
            
            
            // Get the operation information
            ParameterDescription[] pds =operationDesc.getParameterDescriptions();
            Set<Package> packages = endpointDesc.getPackages();
            
            // Create the message 
            MessageFactory mf = (MessageFactory)FactoryRegistry.getFactory(MessageFactory.class);
            Message m = mf.create(protocol);
            
            // Indicate the style and operation element name.  This triggers the message to
            // put the data blocks underneath the operation element
            m.setStyle(Style.RPC);
            m.setOperationElement(operationDesc.getName());
            
            // The input object represent the signature arguments.
            // Signature arguments are both holders and non-holders
            // Convert the signature into a list of JAXB objects for marshalling
            List<PDElement> pvList = 
                MethodMarshallerUtils.getPDElements(pds, 
                        signatureArguments,
                        true,  // input
                        true); // use partName since this is rpc/lit
                        
            
            // Put values onto the message
            MethodMarshallerUtils.toMessage(pvList, m, packages, true);
            
            return m;
        } catch(Exception e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }
    
    public Object[] demarshalRequest(Message message)
        throws WebServiceException {
        // Note all exceptions are caught and rethrown with a WebServiceException
        try {
            // Sample RPC message
            // ..
            // <soapenv:body>
            //    <m:op xmlns:m="urn://api">
            //       <m:param xsi:type="data:foo" xmlns:data="urn://mydata" >...</m:param>
            //    </m:op>
            // </soapenv:body>
            //
            // Important points.
            //   1) RPC has an operation element under the body.  This is the name of the
            //      wsdl operation.
            //   2) The data blocks are located underneath the operation element.  (In doc/lit
            //      the data elements are underneath the body.
            //   3) The name of the data blocks (m:param) are defined by the wsdl:part not the
            //      schema.  
            //   4) The type of the data block (data:foo) is defined by schema (thus there is 
            //      JAXB type rendering.  
            //   5) We always send an xsi:type, but other vendor's may not.
            // Get the operation information
            ParameterDescription[] pds =operationDesc.getParameterDescriptions();
            Set<Package> packages = endpointDesc.getPackages();
            
            // Indicate that the style is RPC.  This is important so that the message understands
            // that the data blocks are underneath the operation element
            message.setStyle(Style.RPC);
            
            // Unmarshal the ParamValues from the Message
            List<PDElement> pvList = MethodMarshallerUtils.getPDElements(pds, message, packages, true, true);
            
            // Build the signature arguments
            Object[] sigArguments = MethodMarshallerUtils.createRequestSignatureArgs(pds, pvList);
            
            return sigArguments;
        } catch(Exception e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }

   

    public Message marshalResponse(Object returnObject, Object[] signatureArgs)
            throws WebServiceException {
        // Note all exceptions are caught and rethrown with a WebServiceException
        try {
            // Sample RPC message
            // ..
            // <soapenv:body>
            //    <m:opResponse xmlns:m="urn://api">
            //       <m:param xsi:type="data:foo" xmlns:data="urn://mydata" >...</m:param>
            //    </m:op>
            // </soapenv:body>
            //
            // Important points.
            //   1) RPC has an operation element under the body.  This is the name of the
            //      wsdl operation.
            //   2) The data blocks are located underneath the operation element.  (In doc/lit
            //      the data elements are underneath the body.
            //   3) The name of the data blocks (m:param) are defined by the wsdl:part not the
            //      schema.  
            //   4) The type of the data block (data:foo) is defined by schema (thus there is 
            //      JAXB type rendering.  Since we are using JAXB to marshal the data, 
            //      we always generate an xsi:type attribute.  This is an implemenation detail
            //      and is not defined by any spec.
            
            // Get the operation information
            ParameterDescription[] pds =operationDesc.getParameterDescriptions();
            Set<Package> packages = endpointDesc.getPackages();
            
            // Create the message 
            MessageFactory mf = (MessageFactory)FactoryRegistry.getFactory(MessageFactory.class);
            Message m = mf.create(protocol);
            
            // Indicate the style and operation element name.  This triggers the message to
            // put the data blocks underneath the operation element
            m.setStyle(Style.RPC);
            
            // TODO Is there an annotation for the operation element response ?
            String localPart = operationDesc.getName().getLocalPart() + "Response";
            QName responseOp = new QName(operationDesc.getName().getNamespaceURI(), localPart);
            m.setOperationElement(responseOp);
            
            // Put the return object onto the message
            Class returnType = MethodMarshallerUtils.getActualReturnType(operationDesc);
            if (returnType != void.class) {
                MethodMarshallerUtils.toMessage(returnObject, 
                        returnType, 
                        operationDesc.getResultTargetNamespace(),
                        operationDesc.getResultPartName(), 
                        packages, 
                        m,
                        true); // forceXSI since this is rpc/lit
            }
            
            // Convert the holder objects into a list of JAXB objects for marshalling
            List<PDElement> pvList = 
                MethodMarshallerUtils.getPDElements(pds, 
                        signatureArgs, 
                        false,  // output
                        true);   // use partName since this is rpc/lit

            // Put values onto the message
            MethodMarshallerUtils.toMessage(pvList, m, packages, true);
            
            return m;
        } catch(Exception e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }

    
    public Object demarshalResponse(Message message, Object[] signatureArgs)
          throws WebServiceException {
        // Note all exceptions are caught and rethrown with a WebServiceException
        try {
            // Sample RPC message
            // ..
            // <soapenv:body>
            //    <m:opResponse xmlns:m="urn://api">
            //       <m:param xsi:type="data:foo" xmlns:data="urn://mydata" >...</m:param>
            //    </m:op>
            // </soapenv:body>
            //
            // Important points.
            //   1) RPC has an operation element under the body.  This is the name of the
            //      wsdl operation.
            //   2) The data blocks are located underneath the operation element.  (In doc/lit
            //      the data elements are underneath the body.
            //   3) The name of the data blocks (m:param) are defined by the wsdl:part not the
            //      schema.  
            //   4) The type of the data block (data:foo) is defined by schema (thus there is 
            //      JAXB type rendering.  
            //   5) We always send an xsi:type, but other vendor's may not.
            // Get the operation information
            ParameterDescription[] pds =operationDesc.getParameterDescriptions();
            Set<Package> packages = endpointDesc.getPackages();
            
            // Indicate that the style is RPC.  This is important so that the message understands
            // that the data blocks are underneath the operation element
            message.setStyle(Style.RPC);
            
            // Get the return value.
            Class returnType = MethodMarshallerUtils.getActualReturnType(operationDesc);
            Object returnValue = null;
            if (returnType != void.class) {
                returnValue = MethodMarshallerUtils.getReturnValue(packages, message, returnType);
            }
            
            // Unmarshall the ParamValues from the Message
            List<PDElement> pvList = MethodMarshallerUtils.getPDElements(pds, message, packages, false, true);
            
            // Populate the response Holders
            MethodMarshallerUtils.updateResponseSignatureArgs(pds, pvList, signatureArgs);
            
            return returnValue;
        } catch(Exception e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }

    public Message marshalFaultResponse(Throwable throwable) throws WebServiceException {
        // Note all exceptions are caught and rethrown with a WebServiceException
        try {
            // Create the message 
            MessageFactory mf = (MessageFactory)FactoryRegistry.getFactory(MessageFactory.class);
            Message m = mf.create(protocol);
            
            // Put the fault onto the message
            MethodMarshallerUtils.marshalFaultResponse(throwable, 
                    operationDesc, 
                    endpointDesc.getPackages(), 
                    m, 
                    true);  // isRPC=true
            return m;
        } catch(Exception e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }

    public Throwable demarshalFaultResponse(Message message) throws WebServiceException {
        // Note all exceptions are caught and rethrown with a WebServiceException
        try {
            Throwable t = MethodMarshallerUtils.demarshalFaultResponse(operationDesc, endpointDesc.getPackages(), message,  true); 
            return t;
        } catch(Exception e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }

}
