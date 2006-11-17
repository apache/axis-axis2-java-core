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
import javax.xml.stream.XMLStreamException;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Holder;
import javax.xml.ws.Response;
import javax.xml.ws.WebServiceException;

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
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.axis2.jaxws.util.ClassUtils;
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
     * @param forceXSIType if upgrading a type to an element
     * @return PDElements
     */
    static List<PDElement> getPDElements(ParameterDescription[] params, Object[] sigArguments, boolean isInput, boolean usePartName, boolean forceXSIType) {
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
                    value = XMLRootElementUtil.getElementEnabledObject(pd.getTargetNamespace(), localName, formalType, value, forceXSIType);
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
            Set<Package> packages, 
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
                
                // Unmarshal the object into a JAXB object or JAXBElement
                Block block = null;
                if (pd.isHeader()) {

                    // Get the Block from the header
                    if (message.getStyle() == Style.RPC) {
                        // TODO add xsi type
                    }
                    String localName = (usePartName) ? pd.getPartName() : pd.getParameterName();
                    block = message.getHeaderBlock(pd.getTargetNamespace(), localName, new JAXBBlockContext(packages), factory);
                } else {
                    if (message.getStyle() == Style.RPC) {
                        // TODO add xsi type
                    }
                    block = message.getBodyBlock(index, new JAXBBlockContext(packages), factory);
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
     * @throws MessageException
     */
    static void toMessage(List<PDElement> pvList, Message message, Set<Package> packages) throws MessageException {
        
        int index = message.getNumBodyBlocks();
        for (int i=0; i<pvList.size(); i++) {
            PDElement pv = pvList.get(i);
            
            // Create a JAXBBlock out of the value.
            // (Note that the PDElement.getValue always returns an object
            // that has an element rendering...ie. it is either a JAXBElement o
            // has @XmlRootElement defined
            Block block = factory.createFrom(pv.getElementValue(), 
                    new JAXBBlockContext(packages), 
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
     * @param forceXSIType if upgrading type to an element
     * @throws MessageException
     */
    static void toMessage(Object returnValue, 
            Class returnType, 
            String returnNS, 
            String returnLocalPart, 
            Set<Package> packages, 
            Message message, 
            boolean forceXSIType) 
            throws MessageException {
        // If this type is an element rendering, then we are okay
        // If it is a type rendering then make a JAXBElement 
        if (!XMLRootElementUtil.isElementEnabled(returnType)) {
            returnValue = XMLRootElementUtil.getElementEnabledObject(returnNS, returnLocalPart,returnType, returnValue, forceXSIType);
        }
        
        //  Create a JAXBBlock out of the value.
        Block block = factory.createFrom(returnValue, 
                new JAXBBlockContext(packages), 
                null);  // The factory will get the qname from the value
        message.setBodyBlock(0, block);
    }
    
    /**
     * Unmarshal the return object from the message
     * @param packages
     * @param message
     * @return type enabled object
     * @throws MessageException
     * @throws XMLStreamException
     */
    static Object getReturnValue(Set<Package> packages, Message message) 
        throws MessageException, XMLStreamException {
        
        
        // The return object is the first block in the body
        if (message.getStyle() == Style.RPC) {
            // TODO add xsi type
        }
        Block block = message.getBodyBlock(0, new JAXBBlockContext(packages), factory);
        
        // Get the business object.  We want to return the object that represents the type.
        Object returnValue = block.getBusinessObject(true);
        //  The signature wants the object that is rendered as the type
        returnValue = XMLRootElementUtil.getTypeEnabledObject(returnValue);
        return returnValue;
    }
    
    /**
     * Utility method to get the Class representing the actual return type
     * 
     * @param operationDesc
     * @return actual return type
     */
    static Class getActualReturnType(OperationDescription operationDesc){
        Method seiMethod = operationDesc.getSEIMethod();
        Class returnType = seiMethod.getReturnType();
        if(isAsync(operationDesc)){
            //pooling implementation
            if(Response.class == returnType){
                Type type = seiMethod.getGenericReturnType();
                ParameterizedType pType = (ParameterizedType) type;
                return (Class)pType.getActualTypeArguments()[0];    
            }
            //Callback Implementation
            else{
                Type[] type = seiMethod.getGenericParameterTypes();
                Class parameters[]= seiMethod.getParameterTypes();
                int i=0;
                for(Class param:parameters){
                    if(AsyncHandler.class.isAssignableFrom(param)){
                        ParameterizedType pType = (ParameterizedType)type[i];
                        return (Class)pType.getActualTypeArguments()[0];
                    }
                    i++;
                }
            }
            
        }
        
        return returnType;  
    }
    
    /**
     * Marshaling a fault is essentially the same for rpc/lit and doc/lit.
     * This method is used by all of the MethodMarshallers
     * @param throwable Throwable to marshal
     * @param operationDesc OperationDescription
     * @param packages Packages needed to marshal the object
     * @param message Message
     * @param forceXSIType if the faultbean 
     * @throws MessageException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    static void marshalFaultResponse(Throwable throwable, OperationDescription operationDesc,  Set<Package> packages, Message message, 
            boolean forceXSIType)
     throws MessageException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        
        // Get the root cause of the throwable object
        if (log.isDebugEnabled()) {
            log.debug("Marshal Throwable =" + throwable.getClass().getName());
            log.debug("  exception=" + throwable.toString());
        }
        Throwable t = ClassUtils.getRootCause(throwable);

        XMLFault xmlfault = null;
      
        // Get the FaultDescriptor matching this Exception.
        // If FaultDescriptor is found, this is a JAX-B Service Exception.
        // If not found, this is a System Exception
        FaultDescription fd = operationDesc.resolveFaultByExceptionName(t.getClass().getName());

        String text = null;
        if (fd != null) {
            // Service Exception.  
            
            // Get the fault bean object.  Make sure it can be rendered as an element
            Method getFaultInfo = t.getClass().getMethod("getFaultInfo", null);
            Object faultBeanObject = getFaultInfo.invoke(t, null);
            if (!XMLRootElementUtil.isElementEnabled(faultBeanObject.getClass())) {
                faultBeanObject = XMLRootElementUtil.getElementEnabledObject(fd.getTargetNamespace(), fd.getName(), 
                        faultBeanObject.getClass(), faultBeanObject, forceXSIType);
            }
            
            // Create a detailblock representing the faultBeanObject
            JAXBBlockContext context = new JAXBBlockContext(packages);
            Block[] detailBlocks = new Block[1];
            detailBlocks[0] = factory.createFrom(faultBeanObject,context,null);
            
            // Now make a XMLFault containing the detailblock
            text = t.getMessage();
            xmlfault = new XMLFault(null, new XMLFaultReason(text), detailBlocks);
        } else {
            // System Exception
            xmlfault = new XMLFault(null,       // Use the default XMLFaultCode
                    new XMLFaultReason(text));  // Assumes text is the language supported by the current Locale
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
    static Throwable demarshalFaultResponse(OperationDescription operationDesc, Set<Package> packages,Message message, boolean isRPC) 
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
                //TODO add xsi:type
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
            Class faultBeanFormalClass = loadClass(faultDesc.getFaultBean());            
            exception =createServiceException(xmlfault.getReason().getText(), exceptionClass, faultBeanObject, faultBeanFormalClass);
        }
        return exception;
    }
    
    /**
     * @param operationDesc
     * @return if asyc operation
     */
    static boolean isAsync(OperationDescription operationDesc){
        Method method = operationDesc.getSEIMethod();
        if(method == null){
            return false;
        }
        String methodName = method.getName();
        Class returnType = method.getReturnType();
        return methodName.endsWith("Async") && (returnType.isAssignableFrom(Response.class) || returnType.isAssignableFrom(Future.class));
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
        return Class.forName(className, true, Thread.currentThread().getContextClassLoader());
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
