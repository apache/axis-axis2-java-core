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
package org.apache.axis2.jaxws.util;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This utility contains code to determine if an Object is 
 * "element enabled" or "type enabled".
 * 
 * Here is an example for illustration:
 *    <element name='e1'>
 *      <complexType>...</complexType>
 *    </element>
 *    
 *    <element name='e2' type='t2' />
 *    <complexType name= 't2'>..
 *
 *    <element name='e3' type='e3' />  <!-- note element and type have same name -->
 *    <complexType name= 'e3'>..
 *
 * JAXB will generate the following objects:  E1, T2, E3
 *   E1 will have an @XMLRootElement annotation.  It is "element" and "type" enabled.
 *   e2 does not have a generated object.  So it will be represented as a JAXBElement
 *     that contains an object T2.  The JAXBElement is "element" enabled. 
 *   T2 represents a complexType.  It is only "type" enabled.
 *   E3 represents the e3 complexType (it does not represent the e3 element).  Thus E3
 *     is "type enabled".  
 *     
 * When JAXB unmarshals an object, it will return an "element" enabled object (either
 * a generatated object with @XMLRootElement or a JAXBElement).
 * Conversely, you must always marshal "element" enabled objects. 
 * @see org.apache.axis2.jaxws.marshaller.impl.alt.PDElement
 * 
 * At the signature level, the values passed as arguments in an SEI operation represent
 * type enabled objects.  Each of the object must be converted to an element enabled object
 * to marshal (or conversely converted to a type enabled object when unmarshalling)
 *
 * -----------------------------------------
 * There are other simular utility methods in this class.  Including utilities to get 
 * a xml name -> bean property map.
 * 
 * -----------------------------------------
 * 
 * @TODO A secondary reason usage of XMLRootElementUtil is to isolate all of the 
 * @XMLRootElement and related annotation queries.  Annotation queries are expensive.
 * A follow-on version of this class will cache the results so that we can improve performance.
 * 
 */
public class XMLRootElementUtil {
    
    private static final Log log = LogFactory.getLog(XMLRootElementUtil.class);

    /**
     * Constructor is intentionally private.  This class only provides static utility methods
     */
    private XMLRootElementUtil() {
        
    }

    /**
     * Return true if this class is element enabled
     * @param clazz
     * @return true if this class has a corresponding xml root element
     */
    public static boolean isElementEnabled(Class clazz){
        if (clazz.equals(JAXBElement.class)) {
            return true;
        }
        
        // If the clazz is a primitive, then it does not have a corresponding root element.
        if (clazz.isPrimitive() || ClassUtils.getWrapperClass(clazz) != null) {
            return false;
        }
        
        // Presence of an annotation means that it can be rendered as a root element
        XmlRootElement root = (XmlRootElement) clazz.getAnnotation(XmlRootElement.class);
        return root !=null;
    }
    
    /**
     * Returns ture if this class is type enabled
     * @param clazz
     * @return
     */
    public static boolean isTypeEnabled(Class clazz) {
        // Primitives, Primitive wrappers, BigDecimal, etc. are all type enabled
        // So are all classes with @XmlRootElement or @XmlType.
        // For now I am only going to assume that the class is type enabled unless it is JAXBElement
        return (!clazz.equals(JAXBElement.class));
    }
    
    /**
     * Return type enabled object
     * @param obj type or element enabled object
     * @return type enabled object
     */
    public static Object getTypeEnabledObject(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof JAXBElement) {
            return ((JAXBElement) obj).getValue();
        }
        return obj;
    }
    
    /**
     * Return an object that can be marshalled/unmarshalled as an element
     * If the specified object is already element enabled, it is returned.
     * @param namespace
     * @param localPart
     * @param cls either the class or super class of obj
     * @param type element or type enabled object
     * @return
     */
    public static Object getElementEnabledObject(String namespace, String localPart, Class cls, Object obj) {
        if (obj != null && isElementEnabled(obj.getClass())) {
            return obj;
        }
        
        QName qName = new QName(namespace, localPart);
        JAXBElement element = new JAXBElement(qName, cls, obj);
        return element;
    }
    
    
    
    /**
     * @param clazz
     * @return namespace of root element qname or null if this is not object does not represent a root element
     */
    public static QName getXmlRootElementQName(Object obj){
        
        // A JAXBElement stores its name
        if (obj instanceof JAXBElement) {
            return ((JAXBElement) obj).getName();
        }
        
        Class clazz = obj.getClass();
        
        // If the clazz is a primitive, then it does not have a corresponding root element.
        if (clazz.isPrimitive() ||
                ClassUtils.getWrapperClass(clazz) != null) {
            return null;
        }
        
        // See if the object represents a root element
        XmlRootElement root = (XmlRootElement) clazz.getAnnotation(XmlRootElement.class);
        if (root == null) {
            return null;
        }
        
        String namespace = root.namespace();
        String localPart = root.name();
        
        // The namespace may need to be defaulted
        if (namespace == null || namespace.length() == 0 || namespace.equals("##default")) {
            Package pkg = clazz.getPackage();
            XmlSchema schema = (XmlSchema) pkg.getAnnotation(XmlSchema.class);
            if (schema != null) {
                namespace = schema.namespace();
            } else {
                return null;
            }
        }
        return new QName(namespace, localPart);
    }

    
    /**
     * The JAXBClass has a set of bean properties each represented by a PropertyDescriptor
     * Each of the fields of the class has an associated xml name.
     * The method returns a map where the key is the xml name and value is the PropertyDescriptor
     * @param jaxbClass
     * @return map
     */
    public static Map<String, PropertyDescriptor> createPropertyDescriptorMap(Class jaxbClass) throws NoSuchFieldException, IntrospectionException {
        
        if (log.isDebugEnabled()) {
            log.debug("Get the PropertyDescriptor[] for " + jaxbClass);
        }
        
        // TODO This is a very performance intensive search we should cache the calculated map keyed by the jaxbClass
        PropertyDescriptor[] pds = Introspector.getBeanInfo(jaxbClass).getPropertyDescriptors();
        // Make this a weak map in case we want to cache the results
        Map<String, PropertyDescriptor> map = new WeakHashMap<String, PropertyDescriptor>();
        
        // Unfortunately the element names are stored on the fields.
        // Get all of the fields in the class and super classes
        List<Field> fields = new ArrayList<Field>();
        Class cls = jaxbClass;
        while(cls != null) {
            Field[] fieldArray = cls.getDeclaredFields();
            for (Field field:fieldArray) {
                fields.add(field);
            }
            cls = cls.getSuperclass();
        }
        
        // Now match up the fields with the property descriptors...Sigh why didn't JAXB put the @XMLElement annotations on the 
        // property methods!
        for(PropertyDescriptor pd:pds){
            
            // Skip over the class property..it is never represented as an xml element
            if (pd.getName().equals("class")) {
                continue;
            }
            
            // For the current property, find a matching field...so that we can get the xml name
            boolean found = false;
            if (log.isDebugEnabled()) {
                log.debug("  Start: Find xmlname for property:" + pd.getName());
            }
            for(Field field:fields){
                String fieldName = field.getName();
                
                // Use the name of the field and property to find the match
                if (fieldName.equalsIgnoreCase(pd.getDisplayName()) ||
                    fieldName.equalsIgnoreCase(pd.getName())   ) {
                    // Get the xmlElement name for this field
                    String xmlName =getXmlElementName(field.getDeclaringClass(), field);
                    found = true;
                    if (log.isDebugEnabled()) {
                        log.debug("    Found field " + field.getName() + " which has xmlname=" + xmlName);
                    }
                    if (map.get(xmlName) != null) {
                        if (log.isDebugEnabled()) {
                            log.debug("    ALERT: property " + map.get(xmlName).getName() + " already has this same xmlName..this may cause problems.");
                        }
                    }
                    map.put(xmlName, pd);
                    break;
                }
                
                // Unfortunately, sometimes the field name is preceeded by an underscore
                if (fieldName.startsWith("_")) {
                    fieldName = fieldName.substring(1);
                    if (fieldName.equalsIgnoreCase(pd.getDisplayName()) ||
                            fieldName.equalsIgnoreCase(pd.getName())) {
                        // Get the xmlElement name for this field
                        String xmlName =getXmlElementName(field.getDeclaringClass(), field);
                        found = true;
                        if (log.isDebugEnabled()) {
                            log.debug("    Found field " + field.getName() + " which has xmlname=" + xmlName);
                        }
                        if (map.get(xmlName) != null) {
                            if (log.isDebugEnabled()) {
                                log.debug("    ALERT: property " + map.get(xmlName).getName() + " already has this same xmlName..this may cause problems.");
                            }
                        }
                        map.put(xmlName, pd);
                        break;
                    }
                }
            }
            
            // We didn't find a field.  Default the xmlname to the property name
            if (!found) {
                String xmlName = pd.getName();
                if (log.isDebugEnabled()) {
                    log.debug("    A matching field was not found.  Defaulting xmlname to " + xmlName);
                }
                if (map.get(xmlName) != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("    ALERT: property " + map.get(xmlName).getName() + " already has this same xmlName..this may cause problems.");
                    }
                }
                map.put(xmlName, pd);
            }
            if (log.isDebugEnabled()) {
                log.debug("  End: Find xmlname for property:" + pd.getName());
            }
        }
        return map;
    }
    
    /**
     * Get the name of the field by looking at the XmlElement annotation.
     * @param jaxbClass
     * @param fieldName
     * @return
     * @throws NoSuchFieldException
     */
    private static String getXmlElementName(Class jaxbClass, Field field)throws NoSuchFieldException{ 
        XmlElement xmlElement =field.getAnnotation(XmlElement.class);
        
        
        // If XmlElement does not exist, default to using the field name
        if (xmlElement == null || 
            xmlElement.name().equals("##default")) {
            return field.getName();
        }
        return xmlElement.name();
        
    }
    
    
    
}
