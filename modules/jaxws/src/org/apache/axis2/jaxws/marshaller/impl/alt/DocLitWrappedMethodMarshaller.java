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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import javax.jws.WebParam.Mode;
import javax.xml.bind.JAXBElement;
import javax.xml.ws.WebServiceException;

import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.EndpointInterfaceDescription;
import org.apache.axis2.jaxws.description.OperationDescription;
import org.apache.axis2.jaxws.description.OperationDescriptionJava;
import org.apache.axis2.jaxws.description.ParameterDescription;
import org.apache.axis2.jaxws.marshaller.MethodMarshaller;
import org.apache.axis2.jaxws.message.Block;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.MessageException;
import org.apache.axis2.jaxws.message.Protocol;
import org.apache.axis2.jaxws.message.databinding.JAXBBlockContext;
import org.apache.axis2.jaxws.message.factory.JAXBBlockFactory;
import org.apache.axis2.jaxws.message.factory.MessageFactory;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.axis2.jaxws.util.XMLRootElementUtil;
import org.apache.axis2.jaxws.wrapper.JAXBWrapperTool;
import org.apache.axis2.jaxws.wrapper.impl.JAXBWrapperToolImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class DocLitWrappedMethodMarshaller implements MethodMarshaller {

    private static Log log = LogFactory.getLog(DocLitWrappedMethodMarshaller.class);
    
    
    public DocLitWrappedMethodMarshaller() {
        super();
    }

    public Object demarshalResponse(Message message, Object[] signatureArgs, OperationDescription operationDesc)
            throws WebServiceException {
        // Note all exceptions are caught and rethrown with a WebServiceException
        
        EndpointInterfaceDescription ed = operationDesc.getEndpointInterfaceDescription();
        EndpointDescription endpointDesc = ed.getEndpointDescription();
        
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
            
            // Determine if a returnValue is expected.
            // The return value may be an child element
            // The wrapper element 
            // or null
            Object returnValue = null;
            Class returnType = operationDesc.getResultActualType();
            boolean isChildReturn = (operationDesc instanceof OperationDescriptionJava) &&
                ((OperationDescriptionJava) operationDesc).isWebResultAnnotationSpecified();
            boolean isNoReturn = (returnType == void.class);
            
            // In usage=WRAPPED, there will be a single JAXB block inside the body.
            // Get this block
            JAXBBlockContext blockContext = new JAXBBlockContext(packages);        
            JAXBBlockFactory factory = (JAXBBlockFactory)FactoryRegistry.getFactory(JAXBBlockFactory.class);
            Block block = message.getBodyBlock(0, blockContext, factory);
            Object wrapperObject = block.getBusinessObject(true);
            
            // The child elements are within the object that 
            // represents the type
            if (wrapperObject instanceof JAXBElement) {
                wrapperObject = ((JAXBElement) wrapperObject).getValue();
            }
            
            // Use the wrapper tool to get the child objects.
            JAXBWrapperTool wrapperTool = new JAXBWrapperToolImpl();
            
            // Get the list of names for the output parameters
            List<String> names = new ArrayList<String>();
            List<ParameterDescription> pdList = new ArrayList<ParameterDescription>();
            for (int i=0; i<pds.length; i++) {
                ParameterDescription pd = pds[i];
                if (pd.getMode() == Mode.OUT ||
                        pd.getMode() == Mode.INOUT) {
                    names.add(pd.getParameterName());
                    pdList.add(pd);
                }
            }
            
            // The return name is added as the last name
            if (isChildReturn && !isNoReturn) {
                names.add(operationDesc.getResultPartName());
            }
            
            // Get the child objects
            Object[] objects = wrapperTool.unWrap(wrapperObject, names);
            
            // Now create a list of paramValues so that we can populate the signature
            List<PDElement> pvList = new ArrayList<PDElement>();
            for (int i=0; i<pdList.size(); i++) {
                ParameterDescription pd = pdList.get(i);
                Object value = objects[i];
                // The object in the PDElement must be an element
                if (!XMLRootElementUtil.isElementEnabled(pd.getParameterActualType())) {
                    value = XMLRootElementUtil.getElementEnabledObject(pd.getTargetNamespace(),
                            pd.getPartName(), 
                            pd.getParameterActualType(), 
                            value); 
     
                }
                pvList.add(new PDElement(pd, value));
            }
            
            // Populate the response Holders in the signature
            MethodMarshallerUtils.updateResponseSignatureArgs(pds, pvList, signatureArgs);
            
            // Now get the return value
            if (isNoReturn) {
                returnValue = null;
            } else if (isChildReturn) {
                returnValue = objects[objects.length-1];
            } else {
                returnValue = wrapperObject;
            }
            
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
                        
            // In usage=WRAPPED, there will be a single JAXB block inside the body.
            // Get this block
            JAXBBlockContext blockContext = new JAXBBlockContext(packages);        
            JAXBBlockFactory factory = (JAXBBlockFactory)FactoryRegistry.getFactory(JAXBBlockFactory.class);
            Block block = message.getBodyBlock(0, blockContext, factory);
            Object wrapperObject = block.getBusinessObject(true);
            
            // The child elements are within the object that 
            // represents the type
            if (wrapperObject instanceof JAXBElement) {
                wrapperObject = ((JAXBElement) wrapperObject).getValue();
            }
            
            // Use the wrapper tool to get the child objects.
            JAXBWrapperTool wrapperTool = new JAXBWrapperToolImpl();
            
            // Get the list of names for the input parameters
            List<String> names = new ArrayList<String>();
            List<ParameterDescription> pdList = new ArrayList<ParameterDescription>();
            for (int i=0; i<pds.length; i++) {
                ParameterDescription pd = pds[i];
                if (pd.getMode() == Mode.IN ||
                        pd.getMode() == Mode.INOUT) {
                    names.add(pd.getParameterName());
                    pdList.add(pd);
                }
                      
            }
            
            // Get the child objects
            Object[] objects = wrapperTool.unWrap(wrapperObject, names);
            
            // Now create a list of paramValues 
            List<PDElement> pvList = new ArrayList<PDElement>();
            for (int i=0; i<pdList.size(); i++) {
                ParameterDescription pd = pdList.get(i);
                Object value = objects[i];
                // The object in the PDElement must be an element
                if (!XMLRootElementUtil.isElementEnabled(pd.getParameterActualType())) {
                    value = XMLRootElementUtil.getElementEnabledObject(pd.getTargetNamespace(),
                            pd.getPartName(), 
                            pd.getParameterActualType(), 
                            value);
     
                }
                pvList.add(new PDElement(pd, value));
            }
             
            // Build the signature arguments
            Object[] sigArguments = MethodMarshallerUtils.createRequestSignatureArgs(pds, pvList);
            
            return sigArguments;
        } catch(Exception e) {
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
            
            // Create the message 
            MessageFactory mf = (MessageFactory)FactoryRegistry.getFactory(MessageFactory.class);
            Message m = mf.create(protocol);
            
            // In usage=WRAPPED, there will be a single block in the body.
            // The signatureArguments represent the child elements of that block
            // The first step is to convert the signature arguments into a list
            // of parameter values
            List<PDElement> pvList = 
                MethodMarshallerUtils.getPDElements(pds,
                        signatureArgs, 
                        false,  // output
                        true);   // use partNames (which are child names)
                        

            // Now we want to create a single JAXB element that contains the 
            // ParameterValues.  We will use the wrapper tool to do this.
            // Create the inputs to the wrapper tool
            ArrayList<String> nameList = new ArrayList<String>();
            Map<String, Object> objectList = new WeakHashMap<String, Object>();
            
            for(PDElement pv:pvList) {
                String name = pv.getParam().getParameterName();
            
                // The object list contains type rendered objects
                Object value = pv.getElementValue();
                if (value instanceof JAXBElement) {
                    value = ((JAXBElement) value).getValue();
                }
                nameList.add(name);
                objectList.put(name, value);
            }
            
            // Add the return object to the nameList and objectList
            Class returnType = operationDesc.getResultActualType();
            if (returnType != void.class) {
                String name = operationDesc.getResultName();
                nameList.add(name);
                objectList.put(name, returnObject);
            }
            
            // Now create the single JAXB element
            Class cls = MethodMarshallerUtils.loadClass(operationDesc.getResponseWrapperClassName());
            JAXBWrapperTool wrapperTool = new JAXBWrapperToolImpl();
            Object object  = wrapperTool.wrap(cls, nameList, objectList);
            
            // Make sure object can be rendered as an element
            if (!XMLRootElementUtil.isElementEnabled(cls)) {
                object = XMLRootElementUtil.getElementEnabledObject(
                        operationDesc.getResponseWrapperTargetNamespace(), 
                        operationDesc.getResponseWrapperLocalName(), 
                        cls, 
                        object);
            }
            
            
            // Put the object into the message
            JAXBBlockFactory factory = 
                (JAXBBlockFactory)FactoryRegistry.getFactory(JAXBBlockFactory.class);
            
            Block block = factory.createFrom(object, 
                    new JAXBBlockContext(endpointDesc.getPackages()), 
                    null);  // The factory will get the qname from the value
            m.setBodyBlock(0, block);
            
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
            
            // Create the message 
            MessageFactory mf = (MessageFactory)FactoryRegistry.getFactory(MessageFactory.class);
            Message m = mf.create(protocol);
            
            // In usage=WRAPPED, there will be a single block in the body.
            // The signatureArguments represent the child elements of that block
            // The first step is to convert the signature arguments into list
            // of parameter values
            List<PDElement> pvList = MethodMarshallerUtils.getPDElements(pds, 
                    signatureArguments, 
                    true,   // input
                    true);   // use partName (which are the child element names)
            
            // Now we want to create a single JAXB element that contains the 
            // ParameterValues.  We will use the wrapper tool to do this.
            // Create the inputs to the wrapper tool
            ArrayList<String> nameList = new ArrayList<String>();
            Map<String, Object> objectList = new WeakHashMap<String, Object>();
            
            for(PDElement pv:pvList){
                String name = pv.getParam().getParameterName();
            
                // The object list contains type rendered objects
                Object value = pv.getElementValue();
                if (value instanceof JAXBElement) {
                    value = ((JAXBElement) value).getValue();
                }
                nameList.add(name);
                objectList.put(name, value);
            }
            
            // Now create the single JAXB element 
            Class cls = MethodMarshallerUtils.loadClass(operationDesc.getRequestWrapperClassName());
            JAXBWrapperTool wrapperTool = new JAXBWrapperToolImpl();
            Object object  = wrapperTool.wrap(cls, nameList, objectList);
            
            // Make sure object can be rendered as an element
            if (!XMLRootElementUtil.isElementEnabled(cls)) {
                object = XMLRootElementUtil.getElementEnabledObject(
                        operationDesc.getRequestWrapperTargetNamespace(), 
                        operationDesc.getRequestWrapperLocalName(), 
                        cls, 
                        object);
            }
            
            // Put the object into the message
            JAXBBlockFactory factory = 
                (JAXBBlockFactory)FactoryRegistry.getFactory(JAXBBlockFactory.class);
            
            Block block = factory.createFrom(object, 
                    new JAXBBlockContext(endpointDesc.getPackages()), 
                    null);  // The factory will get the qname from the value
            m.setBodyBlock(0, block);
            
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
            Throwable t = MethodMarshallerUtils.demarshalFaultResponse(operationDesc, 
                    endpointDesc.getPackages(), 
                    message, 
                    false);
            return t;
        } catch(Exception e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }

}
