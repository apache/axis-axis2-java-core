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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPFault;
import javax.xml.stream.XMLStreamException;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Holder;
import javax.xml.ws.Response;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.SOAPFaultException;

import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.FaultDescription;
import org.apache.axis2.jaxws.description.OperationDescription;
import org.apache.axis2.jaxws.description.ParameterDescription;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.marshaller.MethodMarshaller;
import org.apache.axis2.jaxws.marshaller.impl.MethodMarshallerImpl;
import org.apache.axis2.jaxws.message.Block;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.MessageException;
import org.apache.axis2.jaxws.message.Protocol;
import org.apache.axis2.jaxws.message.XMLFault;
import org.apache.axis2.jaxws.message.XMLFaultReason;
import org.apache.axis2.jaxws.message.databinding.JAXBBlockContext;
import org.apache.axis2.jaxws.message.factory.JAXBBlockFactory;
import org.apache.axis2.jaxws.message.util.XMLFaultUtils;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.axis2.jaxws.util.ClassUtils;
import org.apache.axis2.jaxws.util.ConvertUtils;
import org.apache.axis2.jaxws.util.JavaUtils;
import org.apache.axis2.jaxws.util.XMLRootElementUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.jws.WebParam.Mode;
import javax.jws.soap.SOAPBinding.Style;


/**
 * Static Utilty Classes used by the MethodMarshaller implementations in the alt package.
 */
class MethodMarshallerUtils  {

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
            boolean usePartName) throws MessageException, XMLStreamException {
        
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
    static void toMessage(List<PDElement> pvList, Message message, Set<String> packages, boolean isRPC) throws MessageException {
        
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
            throws MessageException {
        
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
     * @throws MessageException
     * @throws XMLStreamException
     */
    static Object getReturnValue(Set<String> packages, Message message, Class rpcType) 
        throws MessageException, XMLStreamException {
        
        
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
     * @throws MessageException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    static void marshalFaultResponse(Throwable throwable, OperationDescription operationDesc,  Set<String> packages, Message message, boolean isRPC)
     throws MessageException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        
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
        FaultDescription fd = operationDesc.resolveFaultByExceptionName(t.getClass().getName());

        if (t instanceof SOAPFaultException) {
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
            // The exception is a Service Exception.  It may be (A) JAX-WS compliant exception or (B) JAX-WS legacy exception
            
            // TODO Need to add detection and code to differentiate between (A) and (B)
            
            // Get the fault bean object.  Make sure it can be rendered as an element
            Method getFaultInfo = t.getClass().getMethod("getFaultInfo", null);
            Object faultBeanObject = getFaultInfo.invoke(t, null);
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
            
            // Now make a XMLFault containing the detailblock
            xmlfault = new XMLFault(null, new XMLFaultReason(t.getMessage()), detailBlocks);
        } else if (t instanceof WebServiceException) {
            // Category D: WebServiceException
            // The reason is constructed with the getMessage of the exception.  
            // There is no detail
            WebServiceException wse = (WebServiceException) t;
            xmlfault = new XMLFault(null,       // Use the default XMLFaultCode
                    new XMLFaultReason(wse.getMessage()));  // Assumes text is the language supported by the current Locale
        } else {
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
     * @throws MessageException
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws XMLStreamException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     */
    static Throwable demarshalFaultResponse(OperationDescription operationDesc, Set<String> packages,Message message, boolean isRPC) 
        throws MessageException, ClassNotFoundException, IllegalAccessException,
               InstantiationException, XMLStreamException, InvocationTargetException, NoSuchMethodException {
        
        Throwable exception = null;
        // Get the fault from the message and get the detail blocks (probably one)
        XMLFault xmlfault = message.getXMLFault();
        Block[] detailBlocks = xmlfault.getDetailBlocks();
        
        
        if ((operationDesc.getFaultDescriptions().length == 0) || (detailBlocks == null))  {
            // This is a system exception if the method does not throw a checked exception or if 
            // there is nothing in the detail element.
            // Shouldn't this create 
            
            // Create SystemException
            // TODO shouldn't the exception capture the contents of the detail blocks
            exception = createSystemException(xmlfault.getReason().getText());
        } else {
            
            // TODO what if there are multiple blocks in the detail ?
            // We should create a generic fault with the appropriate detail
            
            // Get the JAXB object from the block
            JAXBBlockContext blockContext = new JAXBBlockContext(packages);        
            
            if (isRPC) {
                // RPC is problem ! We have a chicken and egg problem.
                // Since RPC is type based, JAXB needs the declared type
                // to unmarshal the object.  But we don't know the declared
                // type without knowing the name of the type (sigh)
                
                // First get the QName...this might cause a parse
                QName elementName = detailBlocks[0].getQName();
                
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
            
            // Using the faultBeanQName, find the matching faultDescription 
            FaultDescription faultDesc = null;
            for(int i=0; i<operationDesc.getFaultDescriptions().length && faultDesc == null; i++) {
                FaultDescription fd = operationDesc.getFaultDescriptions()[i];
                QName tryQName = new QName(fd.getTargetNamespace(), fd.getName());
                                
                if (faultBeanQName == null || faultBeanQName.equals(tryQName)) {
                    faultDesc = fd;
                    
                }
            }
            if (faultDesc == null) {
                throw ExceptionFactory.makeWebServiceException(Messages.getMessage("MethodMarshallerErr1", faultBeanObject.getClass().toString()));
            }
            
            // Construct the JAX-WS generated exception that holds the faultBeanObject
            Class exceptionClass = loadClass(faultDesc.getExceptionClassName());
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
        // All webservice exception classes are required to have a constructor that takes a (String, bean) argument
        // TODO necessary to be more careful here with instantiating, cassting, etc?
        if (log.isDebugEnabled()) {
            log.debug("Constructing JAX-WS Exception:" + exceptionclass);
        }
        Constructor constructor = exceptionclass.getConstructor(new Class[] { String.class, beanFormalType });
        Object exception = constructor.newInstance(new Object[] { message, bean });

        return (Exception) exception;

    }
    
    /**
     * Create a system exception
     * @param message
     * @return
     */
    private static Exception createSystemException(String message) {
        return ExceptionFactory.makeWebServiceException(message);
    }
    
    
}
