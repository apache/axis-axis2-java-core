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

import javax.xml.ws.WebServiceException;

import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.EndpointInterfaceDescription;
import org.apache.axis2.jaxws.description.OperationDescription;
import org.apache.axis2.jaxws.description.ParameterDescription;
import org.apache.axis2.jaxws.marshaller.MethodMarshaller;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.MessageException;
import org.apache.axis2.jaxws.message.Protocol;
import org.apache.axis2.jaxws.message.factory.MessageFactory;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DocLitBareMethodMarshaller implements MethodMarshaller {

    private static Log log = LogFactory.getLog(DocLitBareMethodMarshaller.class);
    
    public DocLitBareMethodMarshaller() {
        super();
    }

    public Object demarshalResponse(Message message, Object[] signatureArgs, OperationDescription operationDesc)
            throws WebServiceException {
        
        EndpointInterfaceDescription ed = operationDesc.getEndpointInterfaceDescription();
        EndpointDescription endpointDesc = ed.getEndpointDescription();
        
        // Note all exceptions are caught and rethrown with a WebServiceException
        try {
            // Sample Document message
            // ..
            // <soapenv:body>
            //    <m:return ... >...</m:param>
            // </soapenv:body>
            //
            // Important points.
            //   1) There is no operation element in the message
            //   2) The data blocks are located underneath the operation element. 
            //   3) The name of the data blocks (m:param) are defined by the schema.
            //      (SOAP indicates that the name of the element is not important, but
            //      for document processing, we will assume that the name corresponds to 
            //      a schema root element)
            //   4) The type of the data block is defined by schema; thus in most cases
            //      an xsi:type will not be present
            ParameterDescription[] pds =operationDesc.getParameterDescriptions();
            Set<String> packages = endpointDesc.getPackages();
              
            // Get the return value.
            Class returnType = operationDesc.getResultActualType();
            Object returnValue = null;
            if (returnType != void.class) {
                returnValue = MethodMarshallerUtils.getReturnValue(packages, message, null);
            }
            
            // Unmarshall the ParamValues from the Message
            List<PDElement> pvList = MethodMarshallerUtils.getPDElements(pds, message, packages, false, false);
            
            // Populate the response Holders
            MethodMarshallerUtils.updateResponseSignatureArgs(pds, pvList, signatureArgs);
            
            return returnValue;
        } catch(Exception e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }

    public Object[] demarshalRequest(Message message, OperationDescription operationDesc)
            throws WebServiceException {
        
        EndpointInterfaceDescription ed = operationDesc.getEndpointInterfaceDescription();
        EndpointDescription endpointDesc = ed.getEndpointDescription();
        
        // Note all exceptions are caught and rethrown with a WebServiceException
        try {
            // Sample Document message
            // ..
            // <soapenv:body>
            //    <m:param .. >...</m:param>
            // </soapenv:body>
            //
            // Important points.
            //   1) There is no operation element under the body.
            //   2) The data blocks are located underneath the body.  
            //   3) The name of the data blocks (m:param) are defined by the schema
            //   4) The type of the data block (data:foo) is defined by schema (and probably
            //      is not present in the message
            ParameterDescription[] pds =operationDesc.getParameterDescriptions();
            Set<String> packages = endpointDesc.getPackages();
            
            
            // Unmarshal the ParamValues from the message
            List<PDElement> pvList = MethodMarshallerUtils.getPDElements(pds, message, packages, true, false);
            
            // Build the signature arguments
            Object[] sigArguments = MethodMarshallerUtils.createRequestSignatureArgs(pds, pvList);
            
            return sigArguments;
        } catch(Exception e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }

    public Message marshalResponse(Object returnObject, 
            Object[] signatureArgs, 
            OperationDescription operationDesc, Protocol protocol)
            throws WebServiceException {
        
        EndpointInterfaceDescription ed = operationDesc.getEndpointInterfaceDescription();
        EndpointDescription endpointDesc = ed.getEndpointDescription();

        // We want to respond with the same protocol as the request,
        // It the protocol is null, then use the Protocol defined by the binding
        if (protocol == null) {
            try {
                protocol = Protocol.getProtocolForBinding(endpointDesc.getBindingType());
            } catch (MessageException e) {
                // TODO better handling than this?
                e.printStackTrace();
            }
        }
        
        // Note all exceptions are caught and rethrown with a WebServiceException
        try {
            // Sample Document message
            // ..
            // <soapenv:body>
            //    <m:return ... >...</m:param>
            // </soapenv:body>
            //
            // Important points.
            //   1) There is no operation element in the message
            //   2) The data blocks are located underneath the operation element. 
            //   3) The name of the data blocks (m:param) are defined by the schema.
            //      (SOAP indicates that the name of the element is not important, but
            //      for document processing, we will assume that the name corresponds to 
            //      a schema root element)
            //   4) The type of the data block is defined by schema; thus in most cases
            //      an xsi:type will not be present
            
            // Get the operation information
            ParameterDescription[] pds =operationDesc.getParameterDescriptions();
            Set<String> packages = endpointDesc.getPackages();
            
            // Create the message 
            MessageFactory mf = (MessageFactory)FactoryRegistry.getFactory(MessageFactory.class);
            Message m = mf.create(protocol);
            
            // Put the return object onto the message
            Class returnType = operationDesc.getResultActualType();
            if (returnType != void.class) {
                MethodMarshallerUtils.toMessage(returnObject, returnType,
                        operationDesc.getResultTargetNamespace(),
                        operationDesc.getResultName(), packages, m, 
                        false); // don't force xsi:type for doc/lit
            }
            
            // Convert the holder objects into a list of JAXB objects for marshalling
            List<PDElement> pvList = MethodMarshallerUtils.getPDElements(pds, 
                    signatureArgs, 
                    false, // output
                    false); // use name (element name) not wsd:part name
                   

            // Put values onto the message
            MethodMarshallerUtils.toMessage(pvList, m, packages, false);
            
            return m;
        } catch(Exception e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }

    public Message marshalRequest(Object[] signatureArguments, OperationDescription operationDesc) throws WebServiceException {
        
        EndpointInterfaceDescription ed = operationDesc.getEndpointInterfaceDescription();
        EndpointDescription endpointDesc = ed.getEndpointDescription();
        Protocol protocol = null;
        try {
            protocol = Protocol.getProtocolForBinding(endpointDesc.getClientBindingID());
        } catch (MessageException e) {
            // TODO better handling than this?
            e.printStackTrace();
        }
        
        // Note all exceptions are caught and rethrown with a WebServiceException
        try {
            // Sample Document message
            // ..
            // <soapenv:body>
            //    <m:param .. >...</m:param>
            // </soapenv:body>
            //
            // Important points.
            //   1) There is no operation element under the body.
            //   2) The data blocks are located underneath the body.  
            //   3) The name of the data blocks (m:param) are defined by the schema
            //   4) The type of the data block (data:foo) is defined by schema (and probably
            //      is not present in the message
            
            
            // Get the operation information
            ParameterDescription[] pds =operationDesc.getParameterDescriptions();
            Set<String> packages = endpointDesc.getPackages();
            
            // Create the message 
            MessageFactory mf = (MessageFactory)FactoryRegistry.getFactory(MessageFactory.class);
            Message m = mf.create(protocol);
            
            // The input object represent the signature arguments.
            // Signature arguments are both holders and non-holders
            // Convert the signature into a list of JAXB objects for marshalling
            List<PDElement> pvList = MethodMarshallerUtils.getPDElements(pds, 
                    signatureArguments, 
                    true,  // input
                    false);// use name (element name) not wsd:part name
                    
            
            // Put values onto the message
            MethodMarshallerUtils.toMessage(pvList, m, packages, false);
            
            return m;
        } catch(Exception e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }

    public Message marshalFaultResponse(Throwable throwable, OperationDescription operationDesc, Protocol protocol) throws WebServiceException {
        
        EndpointInterfaceDescription ed = operationDesc.getEndpointInterfaceDescription();
        EndpointDescription endpointDesc = ed.getEndpointDescription();
        // We want to respond with the same protocol as the request,
        // It the protocol is null, then use the Protocol defined by the binding
        if (protocol == null) {
            try {
                protocol = Protocol.getProtocolForBinding(endpointDesc.getBindingType());
            } catch (MessageException e) {
                // TODO better handling than this?
                e.printStackTrace();
            }
        }
        
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
                    false); // don't force xsi:type for doc/lit
            return m;
        } catch(Exception e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }

    public Throwable demarshalFaultResponse(Message message, OperationDescription operationDesc) throws WebServiceException {
        
        EndpointInterfaceDescription ed = operationDesc.getEndpointInterfaceDescription();
        EndpointDescription endpointDesc = ed.getEndpointDescription();
        
        // Note all exceptions are caught and rethrown with a WebServiceException
        try {
            Throwable t = MethodMarshallerUtils.demarshalFaultResponse(operationDesc, endpointDesc.getPackages(), message, false);
            return t;
        } catch(Exception e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }

}
