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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.description.FaultDescription;
import org.apache.axis2.jaxws.description.OperationDescription;
import org.apache.axis2.jaxws.description.ParameterDescription;
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
import org.apache.axis2.jaxws.util.ClassUtils;
import org.apache.axis2.jaxws.util.ConvertUtils;
import org.apache.axis2.jaxws.util.SAAJFactory;
import org.apache.axis2.jaxws.util.XMLRootElementUtil;
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
     * @param params ParameterDescription for this operation
     * @param sigArguments arguments 
     * @param isInput indicates if input or output  params(input args on client, output args on server)
     * @param usePartName indicates whether to use the partName or name for the name of the xml element
     *        partName is used for RPC and doc/lit wrapped, name is used for doc/lit bare
     * @return PDElements
     */
    static List<PDElement> getPDElements(ParameterDescription[] params, Object[] sigArguments, boolean isInput, boolean usePartName) {
        List<PDElement> pvList = new ArrayList<PDElement>();
        
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
                
                // If this value is element enabled, then we are okay
                // Otherwise make an element enabled value
                if (!XMLRootElementUtil.isElementEnabled(formalType)) {
                    String localName = (usePartName) ? pd.getPartName() : pd.getParameterName();
                    value = XMLRootElementUtil.getElementEnabledObject(pd.getTargetNamespace(), localName, formalType, value);
                }
                
                // The object is now ready for marshalling
                PDElement pv = new PDElement(pd, value);
                pvList.add(pv);
            }
        }
        
        return pvList;
    }
    
    /**
     * Return the list of PDElements that is unmarshalled from the wire
     * 
     * @param params ParameterDescription for this operation
     * @param message Message
     * @param packages set of packages needed to unmarshal objects for this operation
     * @param isInput indicates if input or output  params (input on server, output on client)
     * @param usePartName indicates whether to use the partName or name for the name of the xml element
     * @return ParamValues
     */
    static List<PDElement> getPDElements(ParameterDescription[] params, 
            Message message, 
            Set<String> packages, 
            boolean isInput, 
            boolean usePartName) throws XMLStreamException {
        
        List<PDElement> pvList = new ArrayList<PDElement>();
            
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
                if (message.getStyle() == Style.RPC) {
                    context.setRPCType(pd.getParameterActualType());
                }
                
                // Unmarshal the object into a JAXB object or JAXBElement
                if (pd.isHeader()) {

                    // Get the Block from the header
                    String localName = (usePartName) ? pd.getPartName() : pd.getParameterName();
                    block = message.getHeaderBlock(pd.getTargetNamespace(), localName, context, factory);
                } else {
                    block = message.getBodyBlock(index, context, factory);
                    index++;
                }
                
                // The object is now ready for marshalling
                PDElement pv = new PDElement(pd, block.getBusinessObject(true));
                pvList.add(pv);
            }
        }
        
        return pvList;
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
    static Object[] createRequestSignatureArgs(ParameterDescription[] pds, List<PDElement> pvList) 
        throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        Object[] args = new Object[pds.length];
        int pvIndex = 0;
        for (int i=0; i< args.length; i++) {
            // Get the paramValue
            PDElement pv = (pvIndex < pvList.size()) ? pvList.get(pvIndex) : null;
            ParameterDescription pd = pds[i];
            if (pv == null ||
                pv.getParam() != pd) {
                // We have a ParameterDesc but there is not an equivalent PDElement. 
                // Provide the default
                if (pd.isHolderType()) {
                    args[i] = createHolder(pd.getParameterType(), null);
                } else {
                    args[i] = null;
                }
            } else {
          
                // We have a matching paramValue
                Object value = pv.getElementValue();
                pvIndex++;
                
                // The signature wants the object that is rendered as the type
                value = XMLRootElementUtil.getTypeEnabledObject(value);
                
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
     * @param pvList Element Enabled objects
     * @param signatureArgs Signature Arguments (the out/inout holders are updated)
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     */
    static void updateResponseSignatureArgs(ParameterDescription[] pds, List<PDElement> pvList, Object[] signatureArgs) 
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        int pvIndex = 0;
        
        // Each ParameterDescriptor has a correspondinging signatureArg from the 
        // the initial client call.  The pvList contains the response values from the message.
        // Walk the ParameterDescriptor/SignatureArg list and populate the holders with 
        // the match PDElement
        for (int i=0; i< pds.length; i++) {
            // Get the paramValue
            PDElement pv = (pvIndex < pvList.size()) ? pvList.get(pvIndex) : null;
            ParameterDescription pd = pds[i];
            if (pv != null && pv.getParam() == pd) {   
                // We have a matching paramValue
                Object value = pv.getElementValue();
                pvIndex++;
                
                // The signature wants the object that is rendered as the type
                value = XMLRootElementUtil.getTypeEnabledObject(value);
                
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
     * @param pvList element enabled objects
     * @param message Message
     * @param packages Packages needed to do a JAXB Marshal
     * @param isRPC 
     * @throws MessageException
     */
    static void toMessage(List<PDElement> pvList, Message message, Set<String> packages, boolean isRPC) throws WebServiceException {
        
        int index = message.getNumBodyBlocks();
        for (int i=0; i<pvList.size(); i++) {
            PDElement pv = pvList.get(i);
            
            // Create the JAXBBlockContext
            // RPC uses type marshalling, so recored the rpcType
            JAXBBlockContext context = new JAXBBlockContext(packages);
            if (isRPC) {
                context.setRPCType(pv.getParam().getParameterActualType());
            }
                
            // Create a JAXBBlock out of the value.
            // (Note that the PDElement.getValue always returns an object
            // that has an element rendering...ie. it is either a JAXBElement o
            // has @XmlRootElement defined
            Block block = factory.createFrom(pv.getElementValue(), 
                    context, 
                    null);  // The factory will get the qname from the value
            
            if (pv.getParam().isHeader()) {
                // Header block
                QName qname = block.getQName();
                message.setHeaderBlock(qname.getNamespaceURI(), 
                        qname.getLocalPart(),
                        block);
            } else {
                // Body block
                message.setBodyBlock(index, block);
                index++;
            }
        }
    }
    
    /**
     * Marshals the return object to the message (used on server to marshal return object)
     * @param returnValue
     * @param returnType
     * @param returnNS
     * @param returnLocalPart
     * @param packages
     * @param message
     * @param isRPC
     * @throws MessageException
     */
    static void toMessage(Object returnValue, 
            Class returnType, 
            String returnNS, 
            String returnLocalPart, 
            Set<String> packages, 
            Message message, 
            boolean isRPC) 
            throws WebServiceException {
        
        // Create the JAXBBlockContext
        // RPC uses type marshalling, so recored the rpcType
        JAXBBlockContext context = new JAXBBlockContext(packages);
        if (isRPC) {
            context.setRPCType(returnType);
        }
        
        // If this type is an element rendering, then we are okay
        // If it is a type rendering then make a JAXBElement 
        if (!XMLRootElementUtil.isElementEnabled(returnType)) {
            returnValue = XMLRootElementUtil.getElementEnabledObject(returnNS, returnLocalPart,returnType, returnValue);
        }
        
        //  Create a JAXBBlock out of the value.
        Block block = factory.createFrom(returnValue, 
                context, 
                null);  // The factory will get the qname from the value
        message.setBodyBlock(0, block);
    }
    
    /**
     * Unmarshal the return object from the message
     * @param packages
     * @param message
     * @param rpcType RPC Declared Type class (only used for RPC processing
     * @return type enabled object
     * @throws WebService
     * @throws XMLStreamException
     */
    static Object getReturnValue(Set<String> packages, Message message, Class rpcType) 
        throws WebServiceException, XMLStreamException {
        
        
        // The return object is the first block in the body
        JAXBBlockContext context = new JAXBBlockContext(packages);
        if (rpcType != null) {
            context.setRPCType(rpcType);  // RPC is type-based, so the unmarshalled type must be provided
        }
        Block block = message.getBodyBlock(0, context, factory);
        
        // Get the business object.  We want to return the object that represents the type.
        Object returnValue = block.getBusinessObject(true);
        //  The signature wants the object that is rendered as the type
        returnValue = XMLRootElementUtil.getTypeEnabledObject(returnValue);
        return returnValue;
    }
    
    /**
     * Marshaling a fault is essentially the same for rpc/lit and doc/lit.
     * This method is used by all of the MethodMarshallers
     * @param throwable Throwable to marshal
     * @param operationDesc OperationDescription
     * @param packages Packages needed to marshal the object
     * @param message Message
     * @param isRPC
     * @throws WebServiceException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    static void marshalFaultResponse(Throwable throwable, OperationDescription operationDesc,  Set<String> packages, Message message, boolean isRPC)
     throws WebServiceException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        
        // Get the root cause of the throwable object
        Throwable t = ClassUtils.getRootCause(throwable);
        if (log.isDebugEnabled()) {
            log.debug("Marshal Throwable =" + throwable.getClass().getName());
            log.debug("  rootCause =" + t.getClass().getName());
            log.debug("  exception=" + t.toString());
        }

        XMLFault xmlfault = null;
      
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
            
        } else if (fd != null) {
            if (log.isErrorEnabled()) {
                log.debug("Marshal as a Service Exception");
            }
            // The exception is a Service Exception.  It may be (A) JAX-WS compliant exception or (B) JAX-WS legacy exception
            
            // The faultBeanObject is a JAXB object that represents the data of the exception.  It is marshalled in the detail
            // section of the soap fault.  The faultBeanObject is obtained direction from the exception (A) or via 
            // the legacy exception rules (B).
            Object faultBeanObject = null;
       
            if (LegacyExceptionUtil.isLegacyException(t.getClass())) {
                // Legacy Exception case
                faultBeanObject = LegacyExceptionUtil.createFaultBean(t, fd);
            } else {
                // Normal case
                // Get the fault bean object.  
                Method getFaultInfo = t.getClass().getMethod("getFaultInfo", null);
                faultBeanObject = getFaultInfo.invoke(t, null);
            }
            
            if (log.isErrorEnabled()) {
                log.debug("The faultBean type is" + faultBeanObject.getClass().getName());
            }
            // Make sure the faultBeanObject can be marshalled as an element
            if (!XMLRootElementUtil.isElementEnabled(faultBeanObject.getClass())) {
                faultBeanObject = XMLRootElementUtil.getElementEnabledObject(fd.getTargetNamespace(), fd.getName(), 
                        faultBeanObject.getClass(), faultBeanObject);
            }
            
            
            // Create the JAXBBlockContext
            // RPC uses type marshalling, so recored the rpcType
            JAXBBlockContext context = new JAXBBlockContext(packages);
            if (isRPC) {
                context.setRPCType(faultBeanObject.getClass());
            }
            
            // Create a detailblock representing the faultBeanObject
            Block[] detailBlocks = new Block[1];
            detailBlocks[0] = factory.createFrom(faultBeanObject,context,null);
            
            if (log.isErrorEnabled()) {
                log.debug("Create the xmlFault for the Service Exception");
            }
            // Now make a XMLFault containing the detailblock
            xmlfault = new XMLFault(null, new XMLFaultReason(t.getMessage()), detailBlocks);
        } else if (t instanceof WebServiceException) {
            if (log.isErrorEnabled()) {
                log.debug("Marshal as a WebServiceException");
            }
            // Category D: WebServiceException
            // The reason is constructed with the getMessage of the exception.  
            // There is no detail
            WebServiceException wse = (WebServiceException) t;
            xmlfault = new XMLFault(null,       // Use the default XMLFaultCode
                    new XMLFaultReason(wse.getMessage()));  // Assumes text is the language supported by the current Locale
        } else {
            if (log.isErrorEnabled()) {
                log.debug("Marshal as a unchecked System Exception");
            }
            // Category E: Other System Exception
            // The reason is constructed with the toString of the exception.  
            // This places the class name of the exception in the reason
            // There is no detail.
            xmlfault = new XMLFault(null,       // Use the default XMLFaultCode
                    new XMLFaultReason(t.toString()));  // Assumes text is the language supported by the current Locale
        }
        // Add the fault to the message
        message.setXMLFault(xmlfault);
    }
    
    /**
     * Unmarshal the service/system exception from a Message.
     * This is used by all of the marshallers
     * @param operationDesc
     * @param packages
     * @param message
     * @param isRPC
     * @return Throwable
     * @throws WebServiceException
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws XMLStreamException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     */
    static Throwable demarshalFaultResponse(OperationDescription operationDesc, Set<String> packages,Message message, boolean isRPC) 
        throws WebServiceException, ClassNotFoundException, IllegalAccessException,
               InstantiationException, XMLStreamException, InvocationTargetException, NoSuchMethodException {
        
        Throwable exception = null;
        // Get the fault from the message and get the detail blocks (probably one)
        XMLFault xmlfault = message.getXMLFault();
        Block[] detailBlocks = xmlfault.getDetailBlocks();
        
        // Sometimes Axis2 will flow a single detail element with the name "Exception".  
        // "Exception" contains stack information.  This is a violation of the WSI specification; however
        // we still need to work around it.
        boolean isStack = false;
        QName elementName = null;
        if (detailBlocks !=null && detailBlocks.length == 1) {
            elementName = detailBlocks[0].getQName();
            isStack = (elementName.getNamespaceURI().length() == 0 && elementName.getLocalPart().equals("Exception"));
        }
        
        
        if ((operationDesc.getFaultDescriptions().length == 0) || 
                isStack ||
                (detailBlocks == null) || 
                (detailBlocks.length > 1))  {
            // This is a system exception if the method does not throw a checked exception or if 
            // the detail block is missing or contains multiple items.
            exception = createSystemException(xmlfault, message);
        } else {        
            if (log.isErrorEnabled()) {
                log.debug("Ready to demarshal service exception.  The detail entry name is " + elementName);
            }
            // Get the JAXB object from the block
            JAXBBlockContext blockContext = new JAXBBlockContext(packages);        
            
            if (isRPC) {
                // RPC is problem ! We have a chicken and egg problem.
                // Since RPC is type based, JAXB needs the declared type
                // to unmarshal the object.  But we don't know the declared
                // type without knowing the name of the type (sigh)
                
                // Now search the FaultDescriptors to find the right 
                // declared type
                Class rpcType = null;
                for(int i=0; i<operationDesc.getFaultDescriptions().length && rpcType == null; i++) {
                    FaultDescription fd = operationDesc.getFaultDescriptions()[i];
                    String tryName = fd.getName();
                    if (elementName.getLocalPart().equals(tryName)) {
                        rpcType = fd.getClass();
                    }
                }
                // Now set it on the context
                blockContext.setRPCType(rpcType);
                
            }
            
            // TODO This code assumes that the block can be demarshalled as a 
            // known JAXB object.  But what if a detail is flowed that we don't know how to handle.
            // To be on the safe side, we should
            // probably make a copy of the OMBlock and then throw create a SystemException if
            // with the original detail if JAXB unmarshalling fails.
            // (I agree that this would not be performant, but we are on an exception path)
            //
            Block jaxbBlock = factory.createFrom(detailBlocks[0], blockContext);
            Object faultBeanObject = jaxbBlock.getBusinessObject(true); 
            
            // At this point, faultBeanObject is an object that can be rendered as an
            // element.  We want the object that represents the type.  Also get the 
            // name of the element.
            QName faultBeanQName = null;
            if (faultBeanObject instanceof JAXBElement) {
                faultBeanQName = ((JAXBElement)faultBeanObject).getName();
                faultBeanObject = ((JAXBElement)faultBeanObject).getValue();
            } else {
                faultBeanQName = XMLRootElementUtil.getXmlRootElementQName(faultBeanObject);
            }
            
            if (log.isErrorEnabled()) {
                log.debug("Searching for the matching FaultDescription");
            }
            // Using the faultBeanQName, find the matching faultDescription 
            FaultDescription faultDesc = null;
            for(int i=0; i<operationDesc.getFaultDescriptions().length && faultDesc == null; i++) {
                FaultDescription fd = operationDesc.getFaultDescriptions()[i];
                QName tryQName = new QName(fd.getTargetNamespace(), fd.getName());
                if (log.isErrorEnabled()) {
                    log.debug("  FaultDescription qname is (" + tryQName + ") and demarshalled bean qname is (" + faultBeanQName + ")");
                }
                if (faultBeanQName == null || faultBeanQName.equals(tryQName)) {
                    faultDesc = fd;
                    
                }
            }
            if (faultDesc == null) {
                if (log.isErrorEnabled()) {
                    log.debug("A FaultDescription was not found");
                }
                throw ExceptionFactory.makeWebServiceException(Messages.getMessage("MethodMarshallerErr1", faultBeanObject.getClass().toString()));
            }
            
            // Construct the JAX-WS generated exception that holds the faultBeanObject
            Class exceptionClass = loadClass(faultDesc.getExceptionClassName());
            if (log.isErrorEnabled()) {
                log.debug("Found FaultDescription.  The exception name is " + exceptionClass.getName());
            }
            Class faultBeanFormalClass = loadClass(faultDesc.getFaultBean());  // Note that faultBean may not be a bean, it could be a primitive     
            exception =createServiceException(xmlfault.getReason().getText(), exceptionClass, faultBeanObject, faultBeanFormalClass);
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
        // TODO J2W AccessController Needed
        // Don't make this public, its a security exposure
        Class cls = ClassUtils.getPrimitiveClass(className);
        if (cls == null) {
            cls = Class.forName(className, true, Thread.currentThread().getContextClassLoader());
        }
        return cls;
    }
    
    /** Create a JAX-WS Service Exception (Generated Exception)
     * @param message
     * @param exceptionclass
     * @param bean
     * @param beanFormalType
     * @return
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws NoSuchMethodException
     */
    private static Exception createServiceException(String message, Class exceptionclass, Object bean, Class beanFormalType) throws InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchMethodException {
        
        if (log.isDebugEnabled()) {
            log.debug("Constructing JAX-WS Exception:" + exceptionclass);
        }
        Exception exception = null;
        if (LegacyExceptionUtil.isLegacyException(exceptionclass)) {
            // Legacy Exception
            exception = LegacyExceptionUtil.createFaultException(exceptionclass, bean);
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
    
    
}
