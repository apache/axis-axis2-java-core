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

package org.apache.axis2.jaxws.wrapper.impl;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javax.xml.bind.JAXBElement;

import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.message.databinding.JAXBUtils;
import org.apache.axis2.jaxws.util.XMLRootElementUtil;
import org.apache.axis2.jaxws.wrapper.JAXBWrapperTool;
import org.apache.axis2.jaxws.wrapper.PropertyInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The JAXBWrapper tool is used to create a JAXB Object from a series of child objects (wrap) or
 * get the child objects from a JAXB Object (unwrap)
 */
public class JAXBWrapperToolImpl implements JAXBWrapperTool {

    private static final Log log = LogFactory.getLog(JAXBWrapperTool.class);
	
	/**
     * unwrap
     * Returns the list of child objects of the jaxb object
     * @param jaxbObject that is the wrapper element (JAXBElement or object with @XMLRootElement)
     * @param childNames list of xml child names as String
     * @return list of Objects in the same order as the element names.  
     */
    public Object[] unWrap(Object jaxbObject, 
            List<String> childNames) throws JAXBWrapperException{
        
        
        if(jaxbObject == null){
            throw new JAXBWrapperException(Messages.getMessage("JAXBWrapperErr1"));
        }
        if(childNames == null){
            throw new JAXBWrapperException(Messages.getMessage("JAXBWrapperErr2"));
        }
        
        // Get the object that will have the property descriptors (i.e. the object representing the complexType)
        Object jaxbComplexTypeObj = (jaxbObject instanceof JAXBElement) ?
                ((JAXBElement)jaxbObject).getValue() : // Type object is the value of the JAXBElement
                    jaxbObject;                        // Or JAXBObject represents both the element and anon complexType
                
        if (log.isDebugEnabled()) {
            log.debug("Invoking unWrap() method with jaxb object:" + jaxbComplexTypeObj.getClass().getName());
            log.debug("The input child xmlnames are: " + toString(childNames));
        }
        // Get the PropertyInfo map.
        // The method makes sure that each child name has a matching jaxb property
        Map<String , PropertyInfo> piMap = createPropertyInfoMap(jaxbComplexTypeObj.getClass(), childNames);
                
        // Get the corresponsing objects from the jaxb bean
        ArrayList<Object> objList = new ArrayList<Object>();
        for(String childName:childNames){
            PropertyInfo propInfo = piMap.get(childName);
            Object object = null;
            try {
                object = propInfo.get(jaxbComplexTypeObj);
            } catch (Throwable e) {
                if (log.isDebugEnabled())  {
                    log.debug("An exception " + e.getClass() + "occurred while trying to call get() on " + propInfo);
                    log.debug("The corresponding xml child name is: " + childName);
                }
                throw new JAXBWrapperException(e);
            } 
            objList.add(object);
        }
        Object[] jaxbObjects = objList.toArray();
        objList = null;
        return jaxbObjects;
                
    }

    /**
     * wrap
     * Creates a jaxb object that is initialized with the child objects.
     * 
     * Note that the jaxbClass must be the class the represents the complexType. (It should never be JAXBElement)
     * 
     * @param jaxbClass 
     * @param childNames list of xml child names as String
     * @param childObjects, component type objects
     */ 
    public Object wrap(Class jaxbClass, 
            List<String> childNames, Map<String, Object> childObjects)
    throws JAXBWrapperException {
        
        
        if(childNames == null|| childObjects == null){
            throw new JAXBWrapperException(Messages.getMessage("JAXBWrapperErr3"));
        }
        if(childNames.size() != childObjects.size()){
            throw new JAXBWrapperException(Messages.getMessage("JAXBWrapperErr4"));
        }
        
        if (log.isDebugEnabled()) {
            log.debug("Invoking unwrap() method to create jaxb object:" + jaxbClass.getName());
            log.debug("The input child xmlnames are: " + toString(childNames));
        }
        
        // Just like unWrap, get the property info map
        Map<String, PropertyInfo> pdTable = createPropertyInfoMap(jaxbClass, childNames);
        
        // The jaxb object always has a default constructor.  Create the object
        Object jaxbObject = null;
        try {
            jaxbObject = jaxbClass.newInstance();
        } catch (Throwable t) {
            if (log.isDebugEnabled())  {
                log.debug("An exception " + t.getClass() + "occurred while trying to create jaxbobject  " + jaxbClass.getName());
            }
            throw new JAXBWrapperException(t);
        }
        
        // Now set each object onto the jaxb object
        for(String childName:childNames){
            PropertyInfo propInfo = pdTable.get(childName);
            Object value = childObjects.get(childName);
            try {
                propInfo.set(jaxbObject, value);
            } catch (Throwable t) {
                
                if (log.isDebugEnabled())  {
                    log.debug("An exception " + t.getClass() + "occurred while trying to call set() on  " + propInfo);
                    log.debug("The corresponding xml child name is: " + childName);
                    String name = (value == null) ? "<null>" : value.getClass().getName();
                    log.debug("The corresponding value object is: " + name);
                }
                throw new JAXBWrapperException(t);
            }
        }
        
        // Return the jaxb object 
        return jaxbObject;
	}
	
	/**
     * Creates a PropertyInfo map.
     * The key to the map is the xml string.
     * The value is a PropertyInfo object...which is used to set and get bean properties 
     * creates propertyDescriptor for the childNames using the jaxbClass.  
	 * use Introspector.getBeanInfo().getPropertyDescriptors() to get all the property descriptors. Assert if # of childNames and propertyDescriptor array
	 * length do not match. if they match then get the xmlElement name from jaxbClass using propertyDescriptor's display name. See if the xmlElementName matches the 
	 * childName if not use xmlElement annotation name and create PropertyInfo add childName or xmlElement name there, set propertyDescriptor 
	 * and return Map<ChileName, PropertyInfo>.
	 * @param jaxbClass - Class jaxbClass name
	 * @param childNames - ArrayList<String> of xml childNames 
	 * @return Map<String, PropertyInfo> - map of ChildNames that map to PropertyInfo that hold the propertyName and PropertyDescriptor.
	 * @throws IntrospectionException, NoSuchFieldException
	 */
	private Map<String, PropertyInfo> createPropertyInfoMap(Class jaxbClass, 
            List<String> xmlChildNames) throws JAXBWrapperException{
		Map<String, PropertyInfo> map = new WeakHashMap<String, PropertyInfo>();
		
		// Get the property descriptor map for this JAXBClass
        Map<String, PropertyDescriptor>  pdMap = null;
        try {
            pdMap = XMLRootElementUtil.createPropertyDescriptorMap(jaxbClass);
        } catch (Throwable t) {
            log.debug("Error occurred to build the PropertyDescriptor map");
            log.debug("  The JAXBClass is:" + jaxbClass.getName());
            throw new JAXBWrapperException(t);
        }
       
		
        // Now create the property info map
        for (int i=0; i<xmlChildNames.size(); i++) {
            PropertyInfo propInfo= null;
            String xmlChildName = xmlChildNames.get(i);
            PropertyDescriptor pd = pdMap.get(xmlChildName);
            if(pd == null){
                // Each xml child name must have a matching property.  
                if (log.isDebugEnabled()) {
                    log.debug("Error occurred trying to match an xml name to a child of a jaxb object");
                    log.debug("  The JAXBClass is:" + jaxbClass.getName());
                    log.debug("  The child name that we are looking for is:" + xmlChildName);
                    log.debug("  The JAXBClass has the following child xml names:" + toString(pdMap.keySet()));
                    log.debug("  Complete list of child names that we are looking for:" + toString(xmlChildNames));
                }
                throw new JAXBWrapperException(Messages.getMessage("JAXBWrapperErr6", jaxbClass.getName(), xmlChildName));
            }
            propInfo = new PropertyInfo(pd);
            map.put(xmlChildName, propInfo);
        }
        
		
		return map;
	}
	
    
    /**
     * @param collection
     * @return list of the names in the collection
     */
    private String toString(Collection<String> collection) {
        String text = "[";
        if (collection == null) {
            return "[]";
        }
        boolean first = true;
        for (String name:collection) {
            if (first) {
                first = false;
                text += name;
            } else {
                text += "," + name;
            }
        }
        return text + "]";
    }
	
	
}
