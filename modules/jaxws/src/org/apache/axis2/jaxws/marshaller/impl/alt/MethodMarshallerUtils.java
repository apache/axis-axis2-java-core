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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import javax.jws.WebParam.Mode;
import javax.jws.soap.SOAPBinding.Style;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPFault;
import javax.xml.stream.XMLStreamException;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Holder;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.SOAPFaultException;

import org.apache.axis2.java.security.AccessController;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.FaultDescription;
import org.apache.axis2.jaxws.description.OperationDescription;
import org.apache.axis2.jaxws.description.ParameterDescription;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.message.Block;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.Protocol;
import org.apache.axis2.jaxws.message.XMLFault;
import org.apache.axis2.jaxws.message.XMLFaultReason;
import org.apache.axis2.jaxws.message.databinding.JAXBBlockContext;
import org.apache.axis2.jaxws.message.factory.JAXBBlockFactory;
import org.apache.axis2.jaxws.message.util.XMLFaultUtils;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.axis2.jaxws.runtime.description.marshal.FaultBeanDesc;
import org.apache.axis2.jaxws.runtime.description.marshal.MarshalServiceRuntimeDescription;
import org.apache.axis2.jaxws.runtime.description.marshal.MarshalServiceRuntimeDescriptionFactory;
import org.apache.axis2.jaxws.utility.ClassUtils;
import org.apache.axis2.jaxws.utility.ConvertUtils;
import org.apache.axis2.jaxws.utility.SAAJFactory;
import org.apache.axis2.jaxws.utility.XMLRootElementUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Static Utilty Classes used by the MethodMarshaller implementations in the alt package.
 */
public class MethodMarshallerUtils  {

    private static Log log = LogFactory.getLog(MethodMarshallerUtils.class);
    
    private static JAXBBlockFactory factory = 
        (JAXBBlockFactory)FactoryRegistry.getFactory(JAXBBlockFactory.class);

    /**
     * Intentionally Private.  This is a static utility class
     */
    private MethodMarshallerUtils() {
    }
   
    /**
     * Returns the list of PDElements that need to be marshalled onto the wire
     * 
     * @param marshalDesc
     * @param params ParameterDescription for this operation
     * @param sigArguments arguments 
     * @param isInput indicates if input or output  params(input args on client, output args on server)
     * @param isDocLitWrapped 
     * @param isRPC
     * @return PDElements
     */
    static List<PDElement> getPDElements(MarshalServiceRuntimeDescription marshalDesc,
            ParameterDescription[] params, 
            Object[] sigArguments, 
            boolean isInput, 
            boolean isDocLitWrapped, 
            boolean isRPC) {
        List<PDElement> pdeList = new ArrayList<PDElement>();
        
        int index = 0;
        for (int i=0; i<params.length; i++) {
            ParameterDescription pd = params[i];
         
            if (pd.getMode() == Mode.IN && isInput ||
                pd.getMode() == Mode.INOUT ||
                pd.getMode() == Mode.OUT && !isInput) {
                
                // Get the matching signature argument
                Object value = sigArguments[i];
                
                // Don't consider async handlers, they are are not represented on the wire,
                // thus they don't have a PDElement
                if (isAsyncHandler(value)) {
                    continue;
                }
                
                // Convert from Holder into value
                if (isHolder(value)) {
                    value =((Holder)value).value;
                }
                
                // Get the formal type representing the value
                Class formalType = pd.getParameterActualType();
                
                // The namespace and local name are obtained differently depending on the style/use and header
                QName qName = null;
                if (pd.isHeader()) {
                    // Headers (even rpc) are marshalled with the name defined by the element= attribute on the wsd:part
                    qName = new QName(pd.getTargetNamespace(), pd.getParameterName());                    
                } else if (isDocLitWrapped) {
                    // For doc/lit wrapped, the localName comes from the PartName
                    qName = new QName(pd.getTargetNamespace(), pd.getPartName());    
                } else if (isRPC) {
                    // Per WSI-BP, the namespace uri is unqualified
                    qName = new QName(pd.getPartName());    
                } else {
                    qName = new QName(pd.getTargetNamespace(), pd.getParameterName());  
                }
                
                // Create an Element rendering
                Element element = null;
                if (!marshalDesc.getAnnotationDesc(formalType).hasXmlRootElement()) {
                    element = new Element(value, qName, formalType);
                } else {
                    element = new Element(value, qName);
                }
                
                // The object is now ready for marshalling
                PDElement pde = new PDElement(pd, element);
                pdeList.add(pde);
            }
        }
        
        return pdeList;
    }
    
    /**
     * Return the list of PDElements that is unmarshalled from the wire
     * 
     * @param params ParameterDescription for this operation
     * @param message Message
     * @param packages set of packages needed to unmarshal objects for this operation
     * @param isInput indicates if input or output  params (input on server, output on client)
     * @param isUnmarshalByType (discouraged...but must be used for rpc)
     * @return ParamValues
     */
    static List<PDElement> getPDElements(ParameterDescription[] params, 
            Message message, 
            TreeSet<String> packages, 
            boolean isInput, 
            boolean isUnmarshalByType) throws XMLStreamException {
        
        List<PDElement> pdeList = new ArrayList<PDElement>();
        
        // Count 
        int totalBodyBlocks = 0;
        for (int i=0; i<params.length; i++) {
            ParameterDescription pd = params[i];
         
            if (pd.getMode() == Mode.IN && isInput ||
                pd.getMode() == Mode.INOUT ||
                pd.getMode() == Mode.OUT && !isInput) {
                if (!pd.isHeader()) {
                    totalBodyBlocks++;
                }
            }
        }
            
        int index = 0; 
        for (int i=0; i<params.length; i++) {
            ParameterDescription pd = params[i];
         
            if (pd.getMode() == Mode.IN && isInput ||
                pd.getMode() == Mode.INOUT ||
                pd.getMode() == Mode.OUT && !isInput) {
                
                // Don't consider async handlers, they are are not represented on the wire,
                // thus they don't have a PDElement
                // TODO
                //if (isAsyncHandler(param)) {
                //    continue;
                //}
                
                Block block = null;
                JAXBBlockContext context = new JAXBBlockContext(packages);
                
                // RPC is type based, so unfortuately the type of 
                // object must be provided
                if (isUnmarshalByType && !pd.isHeader()) {
                    context.setProcessType(pd.getParameterActualType());
                }
                
                // Unmarshal the object into a JAXB object or JAXBElement
                if (pd.isHeader()) {

                    // Get the Block from the header
                    // NOTE The parameter name is always used to get the header element...even if the style is RPC.
                    String localName = pd.getParameterName();
                    block = message.getHeaderBlock(pd.getTargetNamespace(), localName, context, factory);
                } else {
                    if (totalBodyBlocks > 1) {
                        // You must use this method if there are more than one body block
                        // This method may cause OM expansion
                        block = message.getBodyBlock(index, context, factory);
                    } else {
                        // Use this method if you know there is only one body block.
                        // This method prevents OM expansion.
                        block = message.getBodyBlock(context, factory);
                    }
                    index++;
                }
                
                Element element = new Element(block.getBusinessObject(true), block.getQName());
                PDElement pde = new PDElement(pd, element);
                pdeList.add(pde);
            }
        }
        
        return pdeList;
    }
    
    /**
     * Creates the request signature arguments (server) from a list
     * of element eabled object (PDEements)
     * @param pds ParameterDescriptions for this Operation
     * @param pvList Element enabled object
     * @return Signature Args
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     */
    static Object[] createRequestSignatureArgs(ParameterDescription[] pds, List<PDElement> pdeList) 
        throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        Object[] args = new Object[pds.length];
        int pdeIndex = 0;
        for (int i=0; i< args.length; i++) {
            // Get the paramValue
            PDElement pde = (pdeIndex < pdeList.size()) ? pdeList.get(pdeIndex) : null;
            ParameterDescription pd = pds[i];
            if (pde == null ||
                pde.getParam() != pd) {
                // We have a ParameterDesc but there is not an equivalent PDElement. 
                // Provide the default
                if (pd.isHolderType()) {
                    args[i] = createHolder(pd.getParameterType(), null);
                } else {
                    args[i] = null;
                }
            } else {
          
                // We have a matching paramValue.  Get the type object that represents the type
                Object value = pde.getElement().getTypeValue();
                pdeIndex++;
                
                // Now that we have the type, there may be a mismatch
                // between the type (as defined by JAXB) and the formal
                // parameter (as defined by JAXWS).  Frequently this occurs
                // with respect to T[] versus List<T>.  
                // Use the convert utility to silently do any conversions
                if (ConvertUtils.isConvertable(value, pd.getParameterActualType())) {
                    value = ConvertUtils.convert(value, pd.getParameterActualType());
                } else {
                    String objectClass = (value == null) ? "null" : value.getClass().getName();
                    throw ExceptionFactory.makeWebServiceException(
                            Messages.getMessage("convertProblem", objectClass, pd.getParameterActualType().getName()));
                }
                
                // The signature may want a holder representation
                if (pd.isHolderType()) {
                    args[i] = createHolder(pd.getParameterType(), value);
                } else {
                    args[i] = value;
                }
            }
         
        }
        return args;
    }
    
    /**
     * Update the signature arguments on the client with the unmarshalled element enabled objects (pvList)
     * @param pds ParameterDescriptions
     * @param pdeList Element Enabled objects
     * @param signatureArgs Signature Arguments (the out/inout holders are updated)
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     */
    static void updateResponseSignatureArgs(ParameterDescription[] pds, List<PDElement> pdeList, Object[] signatureArgs) 
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        int pdeIndex = 0;
        
        // Each ParameterDescriptor has a correspondinging signatureArg from the 
        // the initial client call.  The pvList contains the response values from the message.
        // Walk the ParameterDescriptor/SignatureArg list and populate the holders with 
        // the match PDElement
        for (int i=0; i< pds.length; i++) {
            // Get the param value
            PDElement pde = (pdeIndex < pdeList.size()) ? pdeList.get(pdeIndex) : null;
            ParameterDescription pd = pds[i];
            if (pde != null && pde.getParam() == pd) {   
                // We have a matching paramValue.  Get the value that represents the type
                Object value = pde.getElement().getTypeValue();
                pdeIndex++;
                
                // Now that we have the type, there may be a mismatch
                // between the type (as defined by JAXB) and the formal
                // parameter (as defined by JAXWS).  Frequently this occurs
                // with respect to T[] versus List<T>.  
                // Use the convert utility to silently do any conversions
                if (ConvertUtils.isConvertable(value, pd.getParameterActualType())) {
                    value = ConvertUtils.convert(value, pd.getParameterActualType());
                } else {
                    String objectClass = (value == null) ? "null" : value.getClass().getName();
                    throw ExceptionFactory.makeWebServiceException(
                            Messages.getMessage("convertProblem", objectClass, pd.getParameterActualType().getName()));
                }
                
                // TODO Assert that this ParameterDescriptor must represent
                // an OUT or INOUT and must have a non-null holder object to 
                // store the value
                if (isHolder(signatureArgs[i])) {
                    ((Holder) signatureArgs[i]).value = value;
                }
            }    
        }
    }
    
    /**
     * Marshal the element enabled objects (pvList) to the Message
     * @param pdeList element enabled objects
     * @param message Message
     * @param packages Packages needed to do a JAXB Marshal
     * @param isMarshalByType (discouraged...but must be used for rpc)
     * @throws MessageException
     */
    static void toMessage(List<PDElement> pdeList, 
            Message message, 
            TreeSet<String> packages, 
            boolean isMarshalByType) throws WebServiceException {
        
        int totalBodyBlocks = 0;
        for (int i=0; i<pdeList.size(); i++) {
            PDElement pde = pdeList.get(i);
            if (!pde.getParam().isHeader()) {
                totalBodyBlocks++;
            }
        }
        
        int index = message.getNumBodyBlocks();
        for (int i=0; i<pdeList.size(); i++) {
            PDElement pde = pdeList.get(i);
            
            // Create the JAXBBlockContext
            // RPC uses type marshalling, so use the rpcType
            JAXBBlockContext context = new JAXBBlockContext(packages);
            if (isMarshalByType && !pde.getParam().isHeader()) {
                context.setProcessType(pde.getParam().getParameterActualType());
            }
                
            // Create a JAXBBlock out of the value.
            // (Note that the PDElement.getValue always returns an object
            // that has an element rendering...ie. it is either a JAXBElement o
            // has @XmlRootElement defined
            Block block = factory.createFrom(pde.getElement().getElementValue(),
                    context, 
                    pde.getElement().getQName());  
            
            if (pde.getParam().isHeader()) {
                // Header block
                QName qname = block.getQName();
                message.setHeaderBlock(qname.getNamespaceURI(), 
                        qname.getLocalPart(),
                        block);
            } else {
                // Body block
                if (totalBodyBlocks <= 1) {
                    // If there is only one block, use the following "more performant" method
                    message.setBodyBlock(block);
                } else {
                    message.setBodyBlock(index, block);
                }
                index++;
            }
        }
    }
    
    /**
     * Marshals the return object to the message (used on server to marshal return object)
     * @param returnElement element
     * @param returnType
     * @param marshalDesc
     * @param message
     * @param isMarshalByType..we must do this for RPC...discouraged otherwise
     * @param isHeader
     * @throws MessageException
     */
    static void toMessage(Element returnElement, 
            Class returnType, 
            MarshalServiceRuntimeDescription marshalDesc,
            Message message, 
            boolean isMarshalByType,
            boolean isHeader)
            throws WebServiceException {
        
        // Create the JAXBBlockContext
        // RPC uses type marshalling, so recored the rpcType
        JAXBBlockContext context = new JAXBBlockContext(marshalDesc.getPackages());
        if (isMarshalByType && !isHeader) {
            context.setProcessType(returnType);
        }
        
        //  Create a JAXBBlock out of the value.
        Block block = factory.createFrom(returnElement.getElementValue(), 
                context, 
                returnElement.getQName());  
        
        if (isHeader) {
            message.setHeaderBlock(returnElement.getQName().getNamespaceURI(), returnElement.getQName().getLocalPart(), block);
        } else {
            message.setBodyBlock(block);
        }
    }
    
    /**
     * Unmarshal the return object from the message
     * @param packages
     * @param message
     * @param unmarshalType Used only to indicate unmarshaling by type...only necessary for rpc
     * @param isHeader
     * @param headerNS (only needed if isHeader)
     * @param headerLocalPart (only needed if isHeader)
     * @return Element
     * @throws WebService
     * @throws XMLStreamException
     */
    static Element getReturnElement(TreeSet<String> packages, 
            Message message, 
            Class unmarshalType,  // set to null unless style=rpce
            boolean isHeader,
            String headerNS, 
            String headerLocalPart)
        throws WebServiceException, XMLStreamException {
        
        
        // The return object is the first block in the body
        JAXBBlockContext context = new JAXBBlockContext(packages);
        if (unmarshalType != null && !isHeader) {
            context.setProcessType(unmarshalType);  
        }
        Block block = null;
        if (isHeader) {
            block = message.getHeaderBlock(headerNS, headerLocalPart, context, factory);
        } else {
            block = message.getBodyBlock(context, factory);
        }
        
        // Get the business object.  We want to return the object that represents the type.
        Element returnElement = new Element(block.getBusinessObject(true), block.getQName());
        return returnElement;
    }
    
    /**
     * Marshaling a fault is essentially the same for rpc/lit and doc/lit.
     * This method is used by all of the MethodMarshallers
     * @param throwable Throwable to marshal
     * @param operationDesc OperationDescription
     * @param packages Packages needed to marshal the object
     * @param message Message
     */
    static void marshalFaultResponse(Throwable throwable, 
            MarshalServiceRuntimeDescription marshalDesc,
            OperationDescription operationDesc,  
            Message message) {
        // Get the root cause of the throwable object
        Throwable t = ClassUtils.getRootCause(throwable);
        if (log.isDebugEnabled()) {
            log.debug("Marshal Throwable =" + throwable.getClass().getName());
            log.debug("  rootCause =" + t.getClass().getName());
            log.debug("  exception=" + t.toString());
        }
        
        XMLFault xmlfault = null;
        
        try {
             
            // There are 5 different categories of exceptions.  Each category has a little different marshaling code.
            // A) Service Exception that matches the JAX-WS specification (chapter 2.5 of the spec)
            // B) Service Exception that matches the JAX-WS "legacy" exception (chapter 3.7 of the spec)
            // C) SOAPFaultException
            // D) WebServiceException
            // E) Other runtime exceptions (i.e. NullPointerException)
            
            // Get the FaultDescriptor matching this Exception.
            // If FaultDescriptor is found, this is a JAX-B Service Exception.
            // If not found, this is a System Exception
            FaultDescription fd = operationDesc.resolveFaultByExceptionName(t.getClass().getCanonicalName());
            
            if (fd != null) {
                if (log.isErrorEnabled()) {
                    log.debug("Marshal as a Service Exception");
                }
                // Create the JAXB Context
                JAXBBlockContext context = new JAXBBlockContext(marshalDesc.getPackages());
                
                // The exception is a Service Exception.  It may be (A) JAX-WS compliant exception or (B) JAX-WS legacy exception
                
                // The faultBeanObject is a JAXB object that represents the data of the exception.  It is marshalled in the detail
                // section of the soap fault.  The faultBeanObject is obtained direction from the exception (A) or via 
                // the legacy exception rules (B).
                Object faultBeanObject = null;
                
                FaultBeanDesc faultBeanDesc = marshalDesc.getFaultBeanDesc(fd);
                String faultInfo = fd.getFaultInfo();
                if (faultInfo == null || faultInfo.length() == 0) {
                    // Legacy Exception case
                    faultBeanObject = LegacyExceptionUtil.createFaultBean(t, fd, marshalDesc);
                    
                    // If using the exception as the fault bean, then force marshalling by type
                    if (faultBeanObject == t) {
                        context.setProcessType(t.getClass());
                    }
                } else {
                    // Normal case
                    // Get the fault bean object.  
                    Method getFaultInfo = t.getClass().getMethod("getFaultInfo", null);
                    faultBeanObject = getFaultInfo.invoke(t, null);
                }
                
                if (log.isErrorEnabled()) {
                    log.debug("The faultBean type is" + faultBeanObject.getClass().getName());
                }
                
                QName faultBeanQName = new QName(faultBeanDesc.getFaultBeanNamespace(), faultBeanDesc.getFaultBeanLocalName());
                // Make sure the faultBeanObject can be marshalled as an element
                if (!marshalDesc.getAnnotationDesc(faultBeanObject.getClass()).hasXmlRootElement()) {
                    faultBeanObject = new JAXBElement(faultBeanQName, faultBeanObject.getClass(), faultBeanObject);
                }
                
                
               
                
                // Create a detailblock representing the faultBeanObject
                Block[] detailBlocks = new Block[1];
                detailBlocks[0] = factory.createFrom(faultBeanObject,context,faultBeanQName);
                
                if (log.isErrorEnabled()) {
                    log.debug("Create the xmlFault for the Service Exception");
                }
                // Get the fault text using algorithm defined in JAX-WS 10.2.2.3
                String text = t.getMessage();
                if (text == null || text.length() == 0) {
                    text = t.toString();
                }
                // Now make a XMLFault containing the detailblock
                xmlfault = new XMLFault(null, new XMLFaultReason(text), detailBlocks);
            }  else {
                xmlfault = createXMLFaultFromSystemException(t);
            }
        } catch (Throwable e) {
            // If an exception occurs while demarshalling an exception, then rinse and repeat with a system exception
            if (log.isDebugEnabled()) {
                log.debug("An exception (" + e + ") occurred while marshalling exception (" + t + ")");
            }
            WebServiceException wse = ExceptionFactory.makeWebServiceException(e);
            xmlfault = createXMLFaultFromSystemException(wse);
        }
            
        // Add the fault to the message
        message.setXMLFault(xmlfault);
    }
    
    /**
     * This method is used by WebService Impl and Provider to create
     * an XMLFault (for marshalling) from an exception that is a non-service exception
     * @param t Throwable that represents a Service Exception
     * @return XMLFault
     */
    public static XMLFault createXMLFaultFromSystemException(Throwable t) {
        
        try {
            XMLFault xmlfault = null;
            if (t instanceof SOAPFaultException) {
                if (log.isErrorEnabled()) {
                    log.debug("Marshal SOAPFaultException");
                }
                // Category C: SOAPFaultException 
                // Construct the xmlFault from the SOAPFaultException's Fault
                SOAPFaultException sfe = (SOAPFaultException) t;
                SOAPFault soapFault = sfe.getFault();
                if (soapFault == null) {
                    // No fault ?  I will treat this like category E
                    xmlfault = new XMLFault(null,       // Use the default XMLFaultCode
                            new XMLFaultReason(t.toString()));  // Assumes text is the language supported by the current Locale
                } else {
                    xmlfault = XMLFaultUtils.createXMLFault(soapFault);
                }
                
            } else if (t instanceof WebServiceException) {
                if (log.isErrorEnabled()) {
                    log.debug("Marshal as a WebServiceException");
                }
                // Category D: WebServiceException
                // The reason is constructed with the getMessage of the exception.  
                // There is no detail
                WebServiceException wse = (WebServiceException) t;
                
                // Get the fault text using algorithm defined in JAX-WS 10.2.2.3
                String text = wse.getMessage();
                if (text == null || text.length() == 0) {
                    text = wse.toString();
                }
                xmlfault = new XMLFault(null,       // Use the default XMLFaultCode
                        new XMLFaultReason(text));  // Assumes text is the language supported by the current Locale
            } else {
                if (log.isErrorEnabled()) {
                    log.debug("Marshal as a unchecked System Exception");
                }
                // Category E: Other System Exception
                // The reason is constructed with the toString of the exception.  
                // This places the class name of the exception in the reason
                // There is no detail.
                // Get the fault text using algorithm defined in JAX-WS 10.2.2.3
                String text = t.getMessage();
                if (text == null || text.length() == 0) {
                    text = t.toString();
                }
                xmlfault = new XMLFault(null,       // Use the default XMLFaultCode
                        new XMLFaultReason(text));  // Assumes text is the language supported by the current Locale
            }
            return xmlfault;
        } catch (Throwable e) {
            try {
                // If an exception occurs while demarshalling an exception, then rinse and repeat with a webservice exception
                if (log.isDebugEnabled()) {
                    log.debug("An exception (" + e + ") occurred while marshalling exception (" + t + ")");
                }
                // Get the fault text using algorithm defined in JAX-WS 10.2.2.3
                String text = e.getMessage();
                if (text == null || text.length() == 0) {
                    text = e.toString();
                }
                WebServiceException wse = ExceptionFactory.makeWebServiceException(e);
                
                return new XMLFault(null,       // Use the default XMLFaultCode
                        new XMLFaultReason(text));  // Assumes text is the language supported by the current Locale
            } catch (Exception e2) {
                // Exception while creating Exception for Exception
                throw ExceptionFactory.makeWebServiceException(e2);
            }
        }
    }
            
    /**
     * Unmarshal the service/system exception from a Message.
     * This is used by all of the marshallers
     * @param operationDesc
     * @param marshalDesc
     * @param message
     * @return Throwable
     * @throws WebServiceException
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws XMLStreamException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     */
    static Throwable demarshalFaultResponse(OperationDescription operationDesc, 
            MarshalServiceRuntimeDescription marshalDesc,
            Message message) 
        throws WebServiceException, ClassNotFoundException, IllegalAccessException,
               InstantiationException, XMLStreamException, InvocationTargetException, NoSuchMethodException {
        
        Throwable exception = null;
        // Get the fault from the message and get the detail blocks (probably one)
        XMLFault xmlfault = message.getXMLFault();
        Block[] detailBlocks = xmlfault.getDetailBlocks();
        
        // If there is only one block, get the element name of that block.
        QName elementQName = null;
        if (detailBlocks !=null && detailBlocks.length == 1) {
            elementQName = detailBlocks[0].getQName();
        }
        
        // Use the element name to find the matching FaultDescriptor
        FaultDescription faultDesc = null;
        if (elementQName != null) {
            for(int i=0; i<operationDesc.getFaultDescriptions().length && faultDesc == null; i++) {
                FaultDescription fd = operationDesc.getFaultDescriptions()[i];
                FaultBeanDesc faultBeanDesc = marshalDesc.getFaultBeanDesc(fd);
                QName tryQName = new QName(faultBeanDesc.getFaultBeanNamespace(), 
                                           faultBeanDesc.getFaultBeanLocalName());
                if (log.isErrorEnabled()) {
                    log.debug("  FaultDescription qname is (" + tryQName + ") and detail element qname is (" + elementQName + ")");
                }
                if (elementQName.equals(tryQName)) {
                    faultDesc = fd;
                }
            }
        }
        
        if (faultDesc == null && elementQName != null) {
            // If not found, retry the search using just the local name
            for(int i=0; i<operationDesc.getFaultDescriptions().length && faultDesc == null; i++) {
                FaultDescription fd = operationDesc.getFaultDescriptions()[i];
                FaultBeanDesc faultBeanDesc = marshalDesc.getFaultBeanDesc(fd);
                String tryName = faultBeanDesc.getFaultBeanLocalName();
                if (elementQName.getLocalPart().equals(tryName)) {
                    faultDesc = fd;
                }
            }
        }
        
        
        if (faultDesc == null) {
            // This is a system exception if the method does not throw a checked exception or if 
            // the detail block is missing or contains multiple items.
            exception = createSystemException(xmlfault, message);
        } else {        
            if (log.isErrorEnabled()) {
                log.debug("Ready to demarshal service exception.  The detail entry name is " + elementQName);
            }
            FaultBeanDesc faultBeanDesc = marshalDesc.getFaultBeanDesc(faultDesc);
            boolean isLegacy = (faultDesc.getFaultInfo() == null || faultDesc.getFaultInfo().length() == 0);
            
            // Get the JAXB object from the block
            JAXBBlockContext blockContext = new JAXBBlockContext(marshalDesc.getPackages());        
            
            // Note that faultBean may not be a bean, it could be a primitive 
            Class faultBeanFormalClass = loadClass(faultBeanDesc.getFaultBeanClassName());     
            
            // Get the jaxb block and business object
            Block jaxbBlock = factory.createFrom(detailBlocks[0], blockContext);
            Object faultBeanObject = jaxbBlock.getBusinessObject(true); 
            
            // At this point, faultBeanObject is an object that can be rendered as an
            // element.  We want the object that represents the type.
            if (faultBeanObject instanceof JAXBElement) {
                faultBeanObject = ((JAXBElement)faultBeanObject).getValue();
            } 
            
            if (log.isErrorEnabled()) {
                log.debug("Unmarshalled the detail element into a JAXB object");
            }
            
            // Construct the JAX-WS generated exception that holds the faultBeanObject
            Class exceptionClass = loadClass(faultDesc.getExceptionClassName());
            if (log.isErrorEnabled()) {
                log.debug("Found FaultDescription.  The exception name is " + exceptionClass.getName());
            }
            exception =createServiceException(xmlfault.getReason().getText(), 
                    exceptionClass, 
                    faultBeanObject, 
                    faultBeanFormalClass, 
                    marshalDesc, 
                    isLegacy);
        }
        return exception;
    }
    
    
   
    
    /**
     * @param value
     * @return if async handler
     */
    static boolean isAsyncHandler(Object value){
        return (value instanceof AsyncHandler);
    }
    
    /**
     * @param value
     * @return true if value is holder
     */
    static boolean isHolder(Object value){
        return value!=null && Holder.class.isAssignableFrom(value.getClass());
    }
  
    /** Crate a Holder 
     * @param <T>
     * @param paramType
     * @param value
     * @return
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws ClassNotFoundException
     */
    static <T> Holder<T> createHolder(Class paramType, T value) throws IllegalAccessException, InstantiationException, ClassNotFoundException{
        if(Holder.class.isAssignableFrom(paramType)){
            Class holderClazz = loadClass(paramType.getName());
            Holder<T> holder = (Holder<T>) holderClazz.newInstance();
            holder.value = value;
            return holder;
        }
        return null;
    }
    
    /** 
     * Load the class
     * @param className
     * @return loaded class
     * @throws ClassNotFoundException
     */
    static Class loadClass(String className)throws ClassNotFoundException{
        // Don't make this public, its a security exposure
        Class cls = ClassUtils.getPrimitiveClass(className);
        if (cls == null) {
            cls = forName(className, true, getContextClassLoader());
        }
        return cls;
    }
    
    /**
     * Return the class for this name
     * @return Class
     */
    private static Class forName(final String className, final boolean initialize, final ClassLoader classLoader) {
        // NOTE: This method must remain private because it uses AccessController
        Class cl = null;
        try {
            cl = (Class) AccessController.doPrivileged(
                    new PrivilegedExceptionAction() {
                        public Object run() throws ClassNotFoundException {
                            // Class.forName does not support primitives
                            Class cls = ClassUtils.getPrimitiveClass(className); 
                            if (cls == null) {
                                cls = Class.forName(className, initialize, classLoader);   
                            } 
                            return cls;                        
                        }
                    }
                  );  
        } catch (PrivilegedActionException e) {
            if (log.isDebugEnabled()) {
                log.debug("Exception thrown from AccessController: " + e);
            }
            throw ExceptionFactory.makeWebServiceException(e.getException());
        }
        
        return cl;
    }
    
    /**
     * @return ClassLoader
     */
    private static ClassLoader getContextClassLoader() {
        // NOTE: This method must remain private because it uses AccessController
        ClassLoader cl = null;
        try {
            cl = (ClassLoader) AccessController.doPrivileged(
                    new PrivilegedExceptionAction() {
                        public Object run() throws ClassNotFoundException {
                            return Thread.currentThread().getContextClassLoader();      
                        }
                    }
                  );  
        } catch (PrivilegedActionException e) {
            if (log.isDebugEnabled()) {
                log.debug("Exception thrown from AccessController: " + e);
            }
            throw ExceptionFactory.makeWebServiceException(e.getException());
        }
        
        return cl;
    }
    
    
    /** Create a JAX-WS Service Exception (Generated Exception)
     * @param message
     * @param exceptionclass
     * @param bean
     * @param beanFormalType
     * @parma marshalDesc is used to get cached information about the exception class and bean
     * @return
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws NoSuchMethodException
     */
    private static Exception createServiceException(String message, 
            Class exceptionclass, 
            Object bean, 
            Class beanFormalType, 
            MarshalServiceRuntimeDescription marshalDesc,
            boolean isLegacyException) throws InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchMethodException {
        
        if (log.isDebugEnabled()) {
            log.debug("Constructing JAX-WS Exception:" + exceptionclass);
        }
        Exception exception = null;
        if (isLegacyException) {
            // Legacy Exception
            exception = LegacyExceptionUtil.createFaultException(exceptionclass, bean, marshalDesc);
        } else {
            // Normal case, use the contstructor to create the exception
            Constructor constructor = exceptionclass.getConstructor(new Class[] { String.class, beanFormalType });
            exception = (Exception) constructor.newInstance(new Object[] { message, bean });
        }

        return exception;

    }
    
    /**
     * Create a system exception
     * @param message
     * @return
     */
    public static ProtocolException createSystemException(XMLFault xmlFault, Message message) {
        ProtocolException e = null;
        Protocol protocol = message.getProtocol();
        String text = xmlFault.getReason().getText();
        
        if (protocol == Protocol.soap11 || protocol == Protocol.soap12) {
            // Throw a SOAPFaultException
            if (log.isDebugEnabled()) {
                log.debug("Constructing SOAPFaultException for " + text);
            }
            String protocolNS = (protocol == Protocol.soap11) ? 
                    SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE : 
                        SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE;
            try {
                // The following set of instructions is used to avoid 
                // some unimplemented methods in the Axis2 SAAJ implementation
                javax.xml.soap.MessageFactory mf = SAAJFactory.createMessageFactory(protocolNS);
                SOAPBody body = mf.createMessage().getSOAPBody();
                SOAPFault soapFault = XMLFaultUtils.createSAAJFault(xmlFault, body);
                e = new SOAPFaultException(soapFault);
            } catch (Exception ex) {
                // Exception occurred during exception processing.
                // TODO Probably should do something better here
                if (log.isDebugEnabled()) {
                    log.debug("Exception occurred during fault processing:", ex);
                }
                e = ExceptionFactory.makeProtocolException(text, null);
            }
        } else if (protocol == Protocol.rest) {
            if (log.isDebugEnabled()) {
                log.debug("Constructing ProtocolException for " + text);
            }
            // TODO Is there an explicit exception for REST
            e = ExceptionFactory.makeProtocolException(text, null);
        } else if (protocol == Protocol.unknown) {
            // REVIEW What should happen if there is no protocol
            if (log.isDebugEnabled()) {
                log.debug("Constructing ProtocolException for " + text);
            }
            e = ExceptionFactory.makeProtocolException(text, null);
        }
        return e;
    }
    
    /**
     * @param ed
     * @return
     */
    static MarshalServiceRuntimeDescription getMarshalDesc(EndpointDescription ed) {
        ServiceDescription sd = ed.getServiceDescription();
        return MarshalServiceRuntimeDescriptionFactory.get(sd);
    }
    
}
