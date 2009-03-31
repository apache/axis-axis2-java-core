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

package org.apache.axis2.datasource.jaxb;

import org.apache.axiom.om.OMException;
import org.apache.axiom.om.impl.MTOMXMLStreamWriter;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.java.security.AccessController;
import org.apache.axis2.jaxws.message.databinding.JAXBUtils;
import org.apache.axis2.jaxws.message.util.XMLStreamWriterWithOS;
import org.apache.axis2.jaxws.spi.Constants;
import org.apache.axis2.jaxws.utility.JavaUtils;
import org.apache.axis2.jaxws.utility.XMLRootElementUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.attachment.AttachmentMarshaller;
import javax.xml.bind.attachment.AttachmentUnmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.ws.Holder;
import javax.xml.ws.WebServiceException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.PrivilegedAction;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

/*
 * To marshal or unmarshal a JAXB object, the JAXBContext is necessary.
 * In addition, access to the MessageContext and other context objects may be necessary
 * to get classloader information, store attachments etc.
 * 
 * The JAXBDSContext bundles all of this information together.
 */
public class JAXBDSContext {

    private static final Log log = LogFactory.getLog(JAXBDSContext.class);
    public static final boolean DEBUG_ENABLED = log.isDebugEnabled();

    private TreeSet<String> contextPackages;  // List of packages needed by the context
    private String contextPackagesKey;        // Unique key that represents the set of packages
    
    private JAXBContext customerJAXBContext;      // JAXBContext provided by the customer api
    //  JAXBContext loaded by the engine.  It is weakref'd to allow GC
    private WeakReference<JAXBContext> autoJAXBContext = null;   
    private JAXBUtils.CONSTRUCTION_TYPE       // How the JAXBContext is constructed
            constructionType = JAXBUtils.CONSTRUCTION_TYPE.UNKNOWN;
    private MessageContext msgContext;    

    // There are two modes of marshalling and unmarshalling: 
    //   "by java type" and "by schema element".
    // The prefered mode is "by schema element" because it is safe and xml-centric.
    // However there are some circumstances when "by schema element" is not available.
    //    Examples: RPC Lit processing (the wire element is defined by a wsdl:part...not schema)
    //              Doc/Lit Bare "Minimal" Processing (JAXB ObjectFactories are missing...
    //                   and thus we must use "by type" for primitives/String)
    // Please don't use "by java type" processing to get around errors.
    private Class processType = null;
    private boolean isxmlList =false;
    
    private String webServiceNamespace;

    /**
     * Full Constructor JAXBDSContext (most performant)
     *
     * @param packages Set of packages needed by the JAXBContext.
     */
    public JAXBDSContext(TreeSet<String> packages, String packagesKey) {
        this.contextPackages = packages;
        this.contextPackagesKey = packagesKey;
    }

    /**
     * Slightly slower constructor
     *
     * @param packages
     */
    public JAXBDSContext(TreeSet<String> packages) {
        this(packages, packages.toString());
    }

    /**
     * Normal Constructor JAXBBlockContext
     *
     * @param contextPackage
     * @deprecated
     */
    public JAXBDSContext(String contextPackage) {
        this.contextPackages = new TreeSet();
        this.contextPackages.add(contextPackage);
        this.contextPackagesKey = this.contextPackages.toString();
    }

    /**
     * "Dispatch" Constructor 
     * Use this full constructor when the JAXBContent is provided by the
     * customer.
     *
     * @param jaxbContext
     */
    public JAXBDSContext(JAXBContext jaxbContext) {
        this.customerJAXBContext = jaxbContext;
    }

    /** @return Class representing type of the element */
    public TreeSet<String> getContextPackages() {
        return contextPackages;
    }
    
    public JAXBContext getJAXBContext() throws JAXBException {
        return getJAXBContext(null);
    }

    /**
     * @return get the JAXBContext
     * @throws JAXBException
     */
    public JAXBContext getJAXBContext(ClassLoader cl) throws JAXBException {
        if (customerJAXBContext != null) {
            return customerJAXBContext;
        }
        
        // Get the weakly cached JAXBContext
        JAXBContext jc = null;
        if (autoJAXBContext != null) {
            jc = autoJAXBContext.get();
        }
        
        if (jc == null) {
            if (log.isDebugEnabled()) {
                log.debug(
                        "A JAXBContext did not exist, creating a new one with the context packages.");
            }
            Holder<JAXBUtils.CONSTRUCTION_TYPE> constructType =
                    new Holder<JAXBUtils.CONSTRUCTION_TYPE>();
            Map<String, Object> properties = null;
            
            /*
             * We set the default namespace to the web service namespace to fix an
             * obscur bug.
             * 
             * If the class representing a JAXB data object does not define a namespace
             * (via an annotation like @XmlType or via ObjectFactory or schema gen information)
             * then the namespace information is defaulted.
             * 
             * The xjc tool defaults the namespace information to unqualified.
             * However the wsimport tool defaults the namespace to the namespace of the
             * webservice.
             * 
             * To "workaround" this issue, a default namespace equal to the webservice
             * namespace is set on the JAXB marshaller.  This has the effect of changing the
             * "unqualified namespaces" into the namespace used by the webservice.
             * 
             */
            if (this.webServiceNamespace != null) {
                properties = new HashMap<String, Object>();
                properties.put(JAXBUtils.DEFAULT_NAMESPACE_REMAP, this.webServiceNamespace);
            }
            jc = JAXBUtils.getJAXBContext(contextPackages, constructType, 
                                          contextPackagesKey, cl, properties);
            constructionType = constructType.value;
            autoJAXBContext = new WeakReference<JAXBContext>(jc);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Using an existing JAXBContext");
            }
        }
        return jc;
    }

    public void setWebServiceNamespace(String namespace) {
        this.webServiceNamespace = namespace;
    }
    
    /** @return RPC Declared Type */
    public Class getProcessType() {
        return processType;
    }

    /**
     * Set RPC Declared Type.  The use of use this property if the message is style=document is
     * discouraged.
     *
     * @param type
     */
    public void setProcessType(Class type) {
        processType = type;
    }

    public JAXBUtils.CONSTRUCTION_TYPE getConstructionType() {
        return constructionType;
    }

    public boolean isxmlList() {
        return isxmlList;
    }

    public void setIsxmlList(boolean isxmlList) {
        this.isxmlList = isxmlList;
    }
    
    public MessageContext getMessageContext() {
        return msgContext;
    }

    public void setMessageContext(MessageContext messageContext) {
        this.msgContext = messageContext;
    }
    
    public ClassLoader getClassLoader() {
        MessageContext context = getMessageContext();
        if (context != null) {
            return (ClassLoader) context.getProperty(Constants.CACHE_CLASSLOADER);
        }
        return null;
    }
    
    /**
     * Create an AttachmentMarshaller to marshal MTOM/SWA Attachments
     * @param writer
     * @return
     */
    protected AttachmentMarshaller createAttachmentMarshaller(XMLStreamWriter writer) {
        return new JAXBAttachmentMarshaller(getMessageContext(), writer);
    }
    
    /**
     * Create an Attachment unmarshaller for unmarshalling MTOM/SWA Attachments
     * @return AttachmentUnmarshaller
     */
    protected AttachmentUnmarshaller createAttachmentUnmarshaller(XMLStreamReader reader) {
        return new JAXBAttachmentUnmarshaller(getMessageContext(), reader);
    }

    /**
     * Unmarshal the xml into a JAXB object
     * @param reader
     * @return
     * @throws JAXBException
     */
    public Object unmarshal(XMLStreamReader reader) throws JAXBException {

        // There may be a preferred classloader that should be used
        ClassLoader cl = getClassLoader();
        
        Unmarshaller u = JAXBUtils.getJAXBUnmarshaller(getJAXBContext(cl));

        
        // Create an attachment unmarshaller
        AttachmentUnmarshaller aum = createAttachmentUnmarshaller(reader);

        if (aum != null) {
            if (DEBUG_ENABLED) {
                log.debug("Adding JAXBAttachmentUnmarshaller to Unmarshaller");
            } 
            u.setAttachmentUnmarshaller(aum);
        }

        Object jaxb = null;

        // Unmarshal into the business object.
        if (getProcessType() == null) {
            jaxb = unmarshalByElement(u, reader);   // preferred and always used for
                                                    // style=document
        } else {
            jaxb = unmarshalByType(u,
                                   reader,
                                   getProcessType(),
                                   isxmlList(),
                                   getConstructionType());
        }

        // Successfully unmarshalled the object
        JAXBUtils.releaseJAXBUnmarshaller(getJAXBContext(cl), u);
        
        // Don't close the reader.  The reader is owned by the caller, and it
        // may contain other xml instance data (other than this JAXB object)
        // reader.close();
        return jaxb;
    }
    
    /**
     * Marshal the jaxb object
     * @param obj
     * @param writer
     * @param am AttachmentMarshaller, optional Attachment
     */
    public void marshal(Object obj, 
                        XMLStreamWriter writer) throws JAXBException {
            // There may be a preferred classloader that should be used
            ClassLoader cl = getClassLoader();
            
            
            // Very easy, use the Context to get the Marshaller.
            // Use the marshaller to write the object.
            Marshaller m = JAXBUtils.getJAXBMarshaller(getJAXBContext(cl));
            
            AttachmentMarshaller am = createAttachmentMarshaller(writer);
            if (am != null) {
                if (DEBUG_ENABLED) {
                    log.debug("Adding JAXBAttachmentMarshaller to Marshaller");
                }
                m.setAttachmentMarshaller(am);
            }


            // Marshal the object
            if (getProcessType() == null) {
                marshalByElement(obj, 
                                 m, 
                                 writer, 
                                 true);
                                 //!am.isXOPPackage());
            } else {
                marshalByType(obj,
                              m,
                              writer,
                              getProcessType(),
                              isxmlList(),
                              getConstructionType());
            }

            // Successfully marshalled the data
            JAXBUtils.releaseJAXBMarshaller(getJAXBContext(cl), m);
    }
    
    
    /**
     * Preferred way to marshal objects.
     * 
     * @param b Object that can be rendered as an element and the element name is known by the
     * Marshaller
     * @param m Marshaller
     * @param writer XMLStreamWriter
     */
    private static void marshalByElement(final Object b, final Marshaller m, 
                                         final XMLStreamWriter writer,
                                         final boolean optimize) {
        AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                // Marshalling directly to the output stream is faster than marshalling through the
                // XMLStreamWriter. 
                // Take advantage of this optimization if there is an output stream.
                try {
                    if (!optimize) {
                        log.trace(JavaUtils.stackToString());
                        getOutputStream(writer);
                    }
                    OutputStream os = (optimize) ? getOutputStream(writer) : null;
                    if (os != null) {
                        if (DEBUG_ENABLED) {
                            log.debug("Invoking marshalByElement.  " +
                                        "Marshaling to an OutputStream. " +
                                      "Object is "
                                      + getDebugName(b));
                        }
                        writer.flush();
                        m.marshal(b, os);
                    } else {
                        if (DEBUG_ENABLED) {
                            log.debug("Invoking marshalByElement.  " +
                                        "Marshaling to an XMLStreamWriter. " +
                                      "Object is "
                                      + getDebugName(b));
                        }
                        m.marshal(b, writer);
                    }
                } catch (OMException e) {
                    throw e;
                }
                catch (Throwable t) {
                    throw new OMException(t);
                }
                return null;
            }});
    }
    
    private static String getDebugName(Object o) {
        return (o == null) ? "null" : o.getClass().getCanonicalName();
    }

    /**
     * If the writer is backed by an OutputStream, then return the OutputStream
     * @param writer
     * @return OutputStream or null
     */
    private static OutputStream getOutputStream(XMLStreamWriter writer) throws XMLStreamException {
        if (log.isDebugEnabled()) {
            log.debug("XMLStreamWriter is " + writer);
        }
        OutputStream os = null;
        if (writer.getClass() == MTOMXMLStreamWriter.class) {
            os = ((MTOMXMLStreamWriter) writer).getOutputStream();
            if (log.isDebugEnabled()) {
                log.debug("OutputStream accessible from MTOMXMLStreamWriter is " + os);
            }
        }
        if (writer.getClass() == XMLStreamWriterWithOS.class) {
            os = ((XMLStreamWriterWithOS) writer).getOutputStream();
            if (log.isDebugEnabled()) {
                log.debug("OutputStream accessible from XMLStreamWriterWithOS is " + os);
            }
        }
        return os;
    }
    
    /**
     * The root element being read is defined by schema/JAXB; however its contents are known by
     * schema/JAXB. Therefore we use unmarshal by the declared type (This method is used to
     * unmarshal rpc elements)
     * 
     * @param u Unmarshaller
     * @param reader XMLStreamReader
     * @param type Class
     * @return Object
     * @throws WebServiceException
     */
    public static Object unmarshalByType(final Unmarshaller u, final XMLStreamReader reader,
                                          final Class type, final boolean isList,
                                          final JAXBUtils.CONSTRUCTION_TYPE ctype)
        throws WebServiceException {

        if (DEBUG_ENABLED) {
            log.debug("Invoking unmarshalByType.");
        }

        return AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                try {
                    // Unfortunately RPC is type based. Thus a
                    // declared type must be used to unmarshal the xml.
                    Object jaxb;
                   
                    if (!isList) {
                        // case: We are not unmarshalling an xsd:list but an Array.

                        if (type.isArray()) {
                            // If the context is created using package
                            // we will not have common arrays or type array in the context
                            // but there is not much we can do about it so seralize it as
                            // usual
                            if (ctype == JAXBUtils.CONSTRUCTION_TYPE.BY_CONTEXT_PATH) {
                                jaxb = u.unmarshal(reader, type);
                            }
                            // list on client array on server, Can happen only in start from java
                            // case.
                            else if ((ctype == JAXBUtils.CONSTRUCTION_TYPE.BY_CLASS_ARRAY)) {

                                // The type could be any Object or primitive
                            	
                            	//process primitives first
                            	//first verify if we have a primitive type associated in the array.
                            	//array could be single dimension or multi dimension.
                            	Class cType = type.getComponentType();
                            	while(cType.isArray()){
                            		cType = cType.getComponentType();
                            	}
                            	if(cType.isPrimitive()){
                            		jaxb = u.unmarshal(reader, type);
                            	}
                            	// process non primitive                       	
                                // I will first unmarshall the xmldata to a String[]
                                // Then use the unmarshalled jaxbElement to create
                                // proper type Object Array.
                            	
                            	else{
                            		jaxb = unmarshalArray(reader, u, type);
                            	}
                                
                            } else {
                                
                                jaxb = u.unmarshal(reader, type);
                                
                            }

                        } else if (type.isEnum()) {
                            // When JAXBContext is created using a context path, it will not 
                            // include Enum classes.
                            // These classes have @XmlEnum annotation but not @XmlType/@XmlElement,
                            // so the user will see MarshallingEx, class not known to ctxt.
                            // 
                            // This is a jax-b defect, for now this fix is in place to pass CTS.
                            // This only fixes the
                            // situation where the enum is the top-level object (e.g., message-part
                            // in rpc-lit scenario)
                            //
                            // Sample of what enum looks like:
                            // @XmlEnum public enum EnumString {
                            // @XmlEnumValue("String1") STRING_1("String1"),
                            // @XmlEnumValue("String2") STRING_2("String2");
                            //
                            // public static getValue(String){} <-- resolves a "value" to an emum
                            // object
                            // ... }
                            if (DEBUG_ENABLED) {
                                log.debug("unmarshalByType. Unmarshalling " + type.getName()
                                        + " as Enum");
                            }

                            JAXBElement<String> enumValue = u.unmarshal(reader, String.class);

                            if (enumValue != null) {
                                Method m =
                                        type.getMethod("fromValue", new Class[] { String.class });
                                jaxb = m.invoke(null, new Object[] { enumValue.getValue() });
                            } else {
                                jaxb = null;
                            }
                        }
                        //Normal case: We are not unmarshalling a xsd:list or Array
                        else {
                            jaxb = u.unmarshal(reader, type);
                        }

                    } else {
                        // If this is an xsd:list, we need to return the appropriate
                        // list or array (see NOTE above)
                        // First unmarshal as a String
                        //Second convert the String into a list or array
                        
                        jaxb = unmarshalAsListOrArray(reader, u, type);
                        
                    }
                    return jaxb;
                } catch (OMException e) {
                    throw e;
                } catch (Throwable t) {
                    throw new OMException(t);
                }
            }
        });
    }

    private static Object unmarshalArray(final XMLStreamReader reader, 
                                         final Unmarshaller u, 
                                         Class type)
       throws Exception {
        try {
            if (DEBUG_ENABLED) {
                log.debug("Invoking unmarshalArray");
            }
            Object jaxb = AccessController.doPrivileged(new PrivilegedAction() {
                public Object run() {
                    try {
                        return u.unmarshal(reader, String[].class);
                    } catch (OMException e) {
                        throw e;
                    } catch (Throwable t) {
                        throw new OMException(t);
                    }
                }
            });

            Object typeObj = getTypeEnabledObject(jaxb);

            // Now convert String Array in to the required Type Array.
            if (typeObj instanceof String[]) {
                String[] strArray = (String[]) typeObj;
                Object obj = XSDListUtils.fromStringArray(strArray, type);
                QName qName =
                    XMLRootElementUtil.getXmlRootElementQNameFromObject(jaxb);
                jaxb = new JAXBElement(qName, type, obj);
            }

            return jaxb;
        } catch (OMException e) {
            throw e;
        } catch (Throwable t) {
            throw new OMException(t);
        }
    }
   
    /**
     * convert the String into a list or array
     * @param <T>
     * @param jaxb
     * @param type
     * @return
     * @throws IllegalAccessException
     * @throws ParseException
     * @throws NoSuchMethodException
     * @throws InstantiationException
     * @throws DatatypeConfigurationException
     * @throws InvocationTargetException
     */
    public static Object unmarshalAsListOrArray(final XMLStreamReader reader, 
                                                final Unmarshaller u, 
                                                 Class type)
        throws IllegalAccessException, ParseException,NoSuchMethodException,
        InstantiationException,
        DatatypeConfigurationException,InvocationTargetException,JAXBException {
        
        
            if (DEBUG_ENABLED) {
                log.debug("Invoking unmarshaArray");
            }
            
            // If this is an xsd:list, we need to return the appropriate
            // list or array (see NOTE above)
            // First unmarshal as a String
            Object jaxb = null;
            try {
                jaxb = AccessController.doPrivileged(new PrivilegedAction() {
                    public Object run() {
                        try {
                            return u.unmarshal(reader, String.class);
                        } catch (OMException e) {
                            throw e;
                        } catch (Throwable t) {
                            throw new OMException(t);
                        }
                    }
                });
            } catch (OMException e) {
                throw e;
            } catch (Throwable t) {
                throw new OMException(t);
            }
            //Second convert the String into a list or array
            if (getTypeEnabledObject(jaxb) instanceof String) {
                QName qName = XMLRootElementUtil.getXmlRootElementQNameFromObject(jaxb);
                Object obj = XSDListUtils.fromXSDListString((String) getTypeEnabledObject(jaxb), type);
                return new JAXBElement(qName, type, obj);
            } else {
                return jaxb;
            }

    }

    /**
     * Return type enabled object
     *
     * @param obj type or element enabled object
     * @return type enabled object
     */
    static Object getTypeEnabledObject(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof JAXBElement) {
            return ((JAXBElement) obj).getValue();
        }
        return obj;
    }

    /**
     * Marshal objects by type
     * 
     * @param b Object that can be rendered as an element, but the element name is not known to the
     * schema (i.e. rpc)
     * @param m Marshaller
     * @param writer XMLStreamWriter
     * @param type
     */
    private static void marshalByType(final Object b, final Marshaller m,
                                      final XMLStreamWriter writer, final Class type,
                                      final boolean isList, final JAXBUtils.CONSTRUCTION_TYPE ctype)
        throws WebServiceException {
        AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                try {

                    // NOTE
                    // Example:
                    // <xsd:simpleType name="LongList">
                    // <xsd:list>
                    // <xsd:simpleType>
                    // <xsd:restriction base="xsd:unsignedInt"/>
                    // </xsd:simpleType>
                    // </xsd:list>
                    // </xsd:simpleType>
                    // <element name="myLong" nillable="true" type="impl:LongList"/>
                    //
                    // LongList will be represented as an int[]
                    // On the wire myLong will be represented as a list of integers
                    // with intervening whitespace
                    // <myLong>1 2 3</myLong>
                    //
                    // Unfortunately, we are trying to marshal by type. Therefore
                    // we want to marshal an element (foo) that is unknown to schema.
                    // If we use the normal marshal code, the wire will look like
                    // this (which is incorrect):
                    // <foo><item>1</item><item>2</item><item>3</item></foo>
                    //
                    // The solution is to detect this situation and marshal the
                    // String instead. Then we get the correct wire format:
                    // <foo>1 2 3</foo>
                    Object jbo = b;
                    if(DEBUG_ENABLED){
                    	log.debug("check if marshalling type list or array object type = "+ (( b!=null )? b.getClass().getName():"null"));
                    }
                    if (isList) {                   	
                        if (DEBUG_ENABLED) {
                            log.debug("marshalling type which is a List");
                        }
                        
                        // This code assumes that the JAXBContext does not understand
                        // the array or list. In such cases, the contents are converted
                        // to a String and passed directly.
                        
                        if (ctype == JAXBUtils.CONSTRUCTION_TYPE.BY_CONTEXT_PATH) {
                            QName qName = XMLRootElementUtil.getXmlRootElementQNameFromObject(b);
                            String text = XSDListUtils.toXSDListString(getTypeEnabledObject(b));
                            if (DEBUG_ENABLED) {
                                log.debug("marshalling [context path approach] " +
                                                "with xmllist text = " + text);
                            }
                            jbo = new JAXBElement(qName, String.class, text);
                        } else if (ctype == JAXBUtils.CONSTRUCTION_TYPE.BY_CLASS_ARRAY) {
                            // Some versions of JAXB have array/list processing built in.
                            // This code is a safeguard because apparently some versions
                            // of JAXB don't.
                            QName qName = XMLRootElementUtil.getXmlRootElementQNameFromObject(b);
                            String text = XSDListUtils.toXSDListString(getTypeEnabledObject(b));
                            if (DEBUG_ENABLED) {
                                log.debug("marshalling [class array approach] " +
                                                "with xmllist text = " + text);
                            }
                            jbo = new JAXBElement(qName, String.class, text); 
                        }
                    }
                    // When JAXBContext is created using a context path, it will not include Enum
                    // classes.
                    // These classes have @XmlEnum annotation but not @XmlType/@XmlElement, so the
                    // user will see MarshallingEx, class not known to ctxt.
                    // 
                    // This is a jax-b defect, for now this fix is in place to pass CTS. This only
                    // fixes the
                    // situation where the enum is the top-level object (e.g., message-part in
                    // rpc-lit scenario)
                    //
                    // Sample of what enum looks like:
                    // @XmlEnum public enum EnumString {
                    // @XmlEnumValue("String1") STRING_1("String1"),
                    // @XmlEnumValue("String2") STRING_2("String2");
                    // ... }
                    if (type.isEnum()) {
                        if (b != null) {
                            if (DEBUG_ENABLED) {
                                log.debug("marshalByType. Marshaling " + type.getName()
                                        + " as Enum");
                            }
                            JAXBElement jbe = (JAXBElement) b;
                            String value = XMLRootElementUtil.getEnumValue((Enum) jbe.getValue());

                            jbo = new JAXBElement(jbe.getName(), String.class, value);
                        }
                    }

                    if (DEBUG_ENABLED) {
                        log.debug("Invoking marshalByType.  " +
                                        "Marshaling to an XMLStreamWriter. Object is "
                                + getDebugName(b));
                    }
                    
                    /// TODO 
                    // For the cases like enum and list, should we 
                    // intercept exceptions and try a different approach ?
                    m.marshal(jbo, writer);

                } catch (OMException e) {
                    throw e;
                } catch (Throwable t) {
                    throw new OMException(t);
                }
                return null;
            }
        });
    }

    /**
     * Preferred way to unmarshal objects
     * 
     * @param u Unmarshaller
     * @param reader XMLStreamReader
     * @return Object that represents an element
     * @throws WebServiceException
     */
    public static Object unmarshalByElement(final Unmarshaller u, final XMLStreamReader reader)
        throws WebServiceException {
        try {
            if (DEBUG_ENABLED) {
                log.debug("Invoking unMarshalByElement");
            }
            return AccessController.doPrivileged(new PrivilegedAction() {
                public Object run() {
                    try {
                        return u.unmarshal(reader);
                    } catch (OMException e) {
                        throw e;
                    } catch (Throwable t) {
                        throw new OMException(t);
                    }
                }
            });

        } catch (OMException e) {
            throw e;
        } catch (Throwable t) {
            throw new OMException(t);
        }
    }
}
