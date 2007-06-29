/*
 * Copyright 2004,2005 The Apache Software Foundation.
 * Copyright 2006 International Business Machines Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.axis2.jaxws.message.databinding.impl;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axis2.java.security.AccessController;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.attachments.JAXBAttachmentMarshaller;
import org.apache.axis2.jaxws.message.attachments.JAXBAttachmentUnmarshaller;
import org.apache.axis2.jaxws.message.databinding.JAXBBlock;
import org.apache.axis2.jaxws.message.databinding.JAXBBlockContext;
import org.apache.axis2.jaxws.message.databinding.JAXBUtils;
import org.apache.axis2.jaxws.message.databinding.XSDListUtils;
import org.apache.axis2.jaxws.message.factory.BlockFactory;
import org.apache.axis2.jaxws.message.impl.BlockImpl;
import org.apache.axis2.jaxws.utility.JavaUtils;
import org.apache.axis2.jaxws.utility.XMLRootElementUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBIntrospector;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.ws.WebServiceException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.PrivilegedAction;
import java.text.ParseException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * JAXBBlockImpl
 * <p/>
 * A Block containing a JAXB business object (either a JAXBElement or an object with
 * @XmlRootElement)
 */
public class JAXBBlockImpl extends BlockImpl implements JAXBBlock {

    private static final Log log = LogFactory.getLog(JAXBBlockImpl.class);
    
    private static final boolean DEBUG_ENABLED = log.isDebugEnabled();
    
    /**
     * Called by JAXBBlockFactory
     *
     * @param busObject..The business object must be a JAXBElement or an object with an
     *                       @XMLRootElement. This is assertion is validated in the JAXBFactory.
     * @param busContext
     * @param qName          QName must be non-null
     * @param factory
     */
    JAXBBlockImpl(Object busObject, JAXBBlockContext busContext, QName qName, BlockFactory factory)
            throws JAXBException {
        super(busObject,
              busContext,
              qName,
              factory);
    }

    /**
     * Called by JAXBBlockFactory
     *
     * @param omelement
     * @param busContext
     * @param qName      must be non-null
     * @param factory
     */
    JAXBBlockImpl(OMElement omElement, JAXBBlockContext busContext, QName qName,
                  BlockFactory factory) {
        super(omElement, busContext, qName, factory);
    }

    @Override
    protected Object _getBOFromReader(XMLStreamReader reader, Object busContext)
            throws XMLStreamException, WebServiceException {
        // Get the JAXBBlockContext.  All of the necessry information is recorded on it
        JAXBBlockContext ctx = (JAXBBlockContext)busContext;
        try {
            // TODO Re-evaluate Unmarshall construction w/ MTOM
            Unmarshaller u = JAXBUtils.getJAXBUnmarshaller(ctx.getJAXBContext());

            if (log.isDebugEnabled()) {
                log.debug("Adding JAXBAttachmentUnmarshaller to Unmarshaller");
            }
            
            Message msg = getParent();
            
            JAXBAttachmentUnmarshaller aum = new JAXBAttachmentUnmarshaller(msg);
            u.setAttachmentUnmarshaller(aum);
            
            Object jaxb = null;

            // Unmarshal into the business object.
            if (ctx.getProcessType() == null) {
                jaxb = unmarshalByElement(u,
                                          reader); // preferred and always used for style=document
            } else {
            	jaxb = unmarshalByType(u, reader, ctx.getProcessType(), ctx.isxmlList(), ctx.getConstructionType());
            }

            // Successfully unmarshalled the object
            JAXBUtils.releaseJAXBUnmarshaller(ctx.getJAXBContext(), u);
            reader.close();
            return jaxb;
        } catch (JAXBException je) {
            if (log.isDebugEnabled()) {
                try {
                    log.debug("JAXBContext for unmarshal failure:" + ctx.getJAXBContext());
                } catch (Exception e) {
                }
            }
            throw ExceptionFactory.makeWebServiceException(je);
        }
    }
    
    /**
     * @param busObj
     * @param busContext
     * @return
     * @throws XMLStreamException
     * @throws WebServiceException
     */
    private byte[] _getBytesFromBO(Object busObj, Object busContext, String encoding)
        throws XMLStreamException, WebServiceException {
        ByteArrayOutputStream baos = new  ByteArrayOutputStream();
        
        XMLStreamWriter writer = StAXUtils.createXMLStreamWriter(baos, encoding);
        
        // Write the business object to the writer
        _outputFromBO(busObj, busContext, writer);
        
        // Flush the writer
        writer.flush();
        writer.close();
        return baos.toByteArray();
    }


    @Override
    protected XMLStreamReader _getReaderFromBO(Object busObj, Object busContext)
            throws XMLStreamException, WebServiceException {
        ByteArrayInputStream baos = new ByteArrayInputStream(_getBytesFromBO(busObj, busContext, "utf-8"));
        return StAXUtils.createXMLStreamReader(baos, "utf-8");
    }

    @Override
    protected void _outputFromBO(Object busObject, Object busContext, XMLStreamWriter writer)
            throws XMLStreamException, WebServiceException {
        JAXBBlockContext ctx = (JAXBBlockContext)busContext;
        try {
            // Very easy, use the Context to get the Marshaller.
            // Use the marshaller to write the object.
            Marshaller m = JAXBUtils.getJAXBMarshaller(ctx.getJAXBContext());
            
            
            if (log.isDebugEnabled()) {
                log.debug("Adding JAXBAttachmentMarshaller to Marshaller");
            }
            
            Message msg = getParent();
            
            // Pool
            JAXBAttachmentMarshaller am = new JAXBAttachmentMarshaller(msg, writer);
            m.setAttachmentMarshaller(am);
            
            
            // Marshal the object
            if (ctx.getProcessType() == null) {
                marshalByElement(busObject, m, writer);
            } else {
            	marshalByType(busObject, m, writer, ctx.getProcessType(), ctx.isxmlList(), ctx.getConstructionType());
            }

            // Successfully marshalled the data
            JAXBUtils.releaseJAXBMarshaller(ctx.getJAXBContext(), m);
        } catch (JAXBException je) {
            if (log.isDebugEnabled()) {
                try {
                    log.debug("JAXBContext for marshal failure:" + ctx.getJAXBContext());
                } catch (Exception e) {
                }
            }
            throw ExceptionFactory.makeWebServiceException(je);
        }
    }

    /**
     * Get the QName from the jaxb object
     *
     * @param jaxb
     * @param jbc
     * @throws WebServiceException
     */
    private static QName getQName(Object jaxb, JAXBBlockContext ctx) throws JAXBException {
        JAXBIntrospector jbi = JAXBUtils.getJAXBIntrospector(ctx.getJAXBContext());
        QName qName = jbi.getElementName(jaxb);
        JAXBUtils.releaseJAXBIntrospector(ctx.getJAXBContext(), jbi);
        return qName;
    }

    /**
     * Preferred way to marshal objects.
     *
     * @param b      Object that can be rendered as an element and the element name is known by the
     *               Marshaller
     * @param m      Marshaller
     * @param writer XMLStreamWriter
     */
    private static void marshalByElement(Object b, Marshaller m, XMLStreamWriter writer)
            throws WebServiceException {
        // TODO Log and trace here would be helpful
        try {
            m.marshal(b, writer);
        } catch (Exception e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }

    /**
     * Preferred way to unmarshal objects
     *
     * @param u      Unmarshaller
     * @param reader XMLStreamReader
     * @return Object that represents an element
     * @throws WebServiceException
     */
    private static Object unmarshalByElement(final Unmarshaller u, final XMLStreamReader reader)
            throws WebServiceException {
        // TODO Log and trace here would be helpful
        try {
        	 if(log.isDebugEnabled()){
        	    log.debug("Invoking unMarshalByElement");
        	 }
        	 return AccessController.doPrivileged(new PrivilegedAction() {
        		 public Object run() {
        			 try {
        				 return u.unmarshal(reader);
        			 } catch (Exception e) {
        				 throw ExceptionFactory.makeWebServiceException(e);
        			 }
        		 }
        	 });

        } catch (Exception e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }

    /**
     * Marshal objects by type
     *
     * @param b      Object that can be rendered as an element, but the element name is not known to
     *               the schema (i.e. rpc)
     * @param m      Marshaller
     * @param writer XMLStreamWriter
     * @param type
     */
    private static void marshalByType(final Object b, final Marshaller m, 
    		final XMLStreamWriter writer, final Class type, final boolean isList, final JAXBUtils.CONSTRUCTION_TYPE ctype)
            throws WebServiceException {
        // TODO Log and trace here would be helpful
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
                        
			if (isList || (type!=null && type.isArray())) {
				if(log.isDebugEnabled()){
					log.debug("marshalling type which is a List or Array");
				}
				//We conver to xsdListString only if the type is not known
				// to the context. In case a jaxbcontext is created from package
				// the array types or list are not know to the context.
				if(ctype == JAXBUtils.CONSTRUCTION_TYPE.BY_CONTEXT_PATH){
					QName qName = XMLRootElementUtil
					.getXmlRootElementQNameFromObject(b);
					String text = XSDListUtils
					.toXSDListString(getTypeEnabledObject(b));
					jbo = new JAXBElement(qName, String.class, text);
				}
				else if(ctype == JAXBUtils.CONSTRUCTION_TYPE.BY_CLASS_ARRAY){
					//do nothing common array types should be know to the jaxbcontext.
					//so do not use xsdListString conversion.
				}
			}
			
			// When JAXBContext is created using a context path, it will not include Enum classes
			// These classes have @XmlEnum annotation but not @XmlType/@XmlElement, so the user
			// will see MarshallingEx, class not known to ctxt.
			// 
			// This is a jax-b defect, for now this fix is in place to pass CTS. This only fixes the
			// situation where the enum is the top-level object (e.g., message-part in rpc-lit scenario)
			//
			// Sample of what enum looks like:
			// @XmlEnum public enum EnumString {
			//    @XmlEnumValue("String1") STRING_1("String1"),
			//    @XmlEnumValue("String2") STRING_2("String2");
			//  ... }
			if (type.isEnum()){
				if (b != null){
					if (DEBUG_ENABLED) {
						log	.debug("marshalByType. Marshaling " + type.getName() + " as Enum");
					}
					JAXBElement jbe = (JAXBElement) b;
					String value = XMLRootElementUtil.getEnumValue((Enum) jbe.getValue());
					
					jbo = new JAXBElement(jbe.getName(), String.class, value);
				}
			}
			
			if (DEBUG_ENABLED) {
				log.debug("Invoking marshalByType.  Marshaling to an XMLStreamWriter. Object is "
								+ getDebugName(b));
			}
			m.marshal(jbo, writer);

		} catch (Exception e) {
			throw ExceptionFactory.makeWebServiceException(e);
		}
		return null;
		}});
    }

    /**
     * The root element being read is defined by schema/JAXB; however its contents are known by
     * schema/JAXB. Therefore we use unmarshal by the declared type (This method is used to
     * unmarshal rpc elements)
     *
     * @param u      Unmarshaller
     * @param reader XMLStreamReader
     * @param type   Class
     * @return Object
     * @throws WebServiceException
     */
    private static Object unmarshalByType(final Unmarshaller u,
			final XMLStreamReader reader, final Class type, final boolean isList, final JAXBUtils.CONSTRUCTION_TYPE ctype) throws WebServiceException {

        if(DEBUG_ENABLED){
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

        				if(type.isArray()){
        					//If the context is created using package
        					//we will not have common arrays or type array in the context
        					//so let use a differet way to unmarshal this type
        					if(ctype == JAXBUtils.CONSTRUCTION_TYPE.BY_CONTEXT_PATH){
        						jaxb=unmarshalAsListOrArray(reader, u, type);
        					}
        					//list on client array on server, Can happen only in start from java case. 
        					else if((ctype == JAXBUtils.CONSTRUCTION_TYPE.BY_CLASS_ARRAY)){
        						//The type could be any Object or primitive
        						//I will first unmarshall the xmldata to a String[]
        						//Then use the unmarshalled jaxbElement to create
        						//proper type Object Array.
        						jaxb = u.unmarshal(reader, String[].class);
        						Object typeObj = getTypeEnabledObject(jaxb);
        						//Now convert String Array in to the required Type Array.
        						if (getTypeEnabledObject(typeObj) instanceof String[]) {
        							String[] strArray = (String[])typeObj;
        							String strTokens = new String();
        							for(String str:strArray){
        								strTokens = strTokens + " "+str;
        							}
        							QName qName = XMLRootElementUtil
        							.getXmlRootElementQNameFromObject(jaxb);
        							Object obj = XSDListUtils.fromXSDListString(
        									strTokens, type);
        							jaxb = new JAXBElement(qName, type, obj);
        						}
        					}
        					else{
        						jaxb = u.unmarshal(reader, type);
        					}

        				}
        				else if (type.isEnum()){
        					// When JAXBContext is created using a context path, it will not include Enum classes
        					// These classes have @XmlEnum annotation but not @XmlType/@XmlElement, so the user
        					// will see MarshallingEx, class not known to ctxt.
        					// 
        					// This is a jax-b defect, for now this fix is in place to pass CTS. This only fixes the
        					// situation where the enum is the top-level object (e.g., message-part in rpc-lit scenario)
        					//
        					// Sample of what enum looks like:
        					// @XmlEnum public enum EnumString {
        					//    @XmlEnumValue("String1") STRING_1("String1"),
        					//    @XmlEnumValue("String2") STRING_2("String2");
        					//
        					// public static getValue(String){} <-- resolves a "value" to an emum object
        					//  ... }
        					if (DEBUG_ENABLED) {
        						log     .debug("unmarshalByType. Unmarshalling " + type.getName() + " as Enum");
        					}

        					JAXBElement<String> enumValue = u.unmarshal(reader, String.class);

        					if (enumValue != null) {
        						Method m = type.getMethod("fromValue", new Class[]{String.class});
        						jaxb = m.invoke(null, new Object[] {enumValue.getValue()});
        					} else {
        						jaxb = null;
        					}
        				}
        				//Normal case: We are not unmarshalling a xsd:list or Array
        				else{
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
        		} catch (Exception e) {
        			throw ExceptionFactory.makeWebServiceException(e);
        		}
        	}});
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
    private static Object unmarshalAsListOrArray(XMLStreamReader reader, Unmarshaller u, Class type)
    throws IllegalAccessException, ParseException, NoSuchMethodException,
    InstantiationException, DatatypeConfigurationException, InvocationTargetException, JAXBException {
        //If this is an xsd:list, we need to return the appropriate
        // list or array (see NOTE above)
        // First unmarshal as a String
        Object  jaxb = u.unmarshal(reader, String.class);
        //Second convert the String into a list or array
        if (getTypeEnabledObject(jaxb) instanceof String) {
            QName qName = XMLRootElementUtil.getXmlRootElementQNameFromObject(jaxb);
            Object obj = XSDListUtils.fromXSDListString((String) getTypeEnabledObject(jaxb), type);
            return new JAXBElement(qName, type, obj);
        }else{
            return jaxb;
        }
        
}

    /**
     * Detect if t represents an xsd:list
     *
     * @param t
     * @return
     * @deprecated - Code to check if element is list has been moved to JABXBlockContext.
     */
    private static boolean isXSDList(Class t) {
        // TODO This code returns true if the 
        // class is an array or List.  The correct solution
        // is to probably pass this information into the
        // JAXBBlockContext.  I noticed that JAX-WS marks
        // each xsd:list param/return with an @XmlList annotation.

        // 
        // Example:
        // <xsd:simpleType name="LongList">
        //   <xsd:list>
        //     <xsd:simpleType>
        //       <xsd:restriction base="xsd:unsignedInt"/>
        //     </xsd:simpleType>
        //   </xsd:list>
        // </xsd:simpleType>
        return (t.isArray() || List.class.isAssignableFrom(t));
    }

    public boolean isElementData() {
        return true;
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
            return ((JAXBElement)obj).getValue();
        }
        return obj;
    }
    
	private static String getDebugName(Object o) {
		return (o == null) ? "null" : o.getClass().getCanonicalName();
	}
    
}
