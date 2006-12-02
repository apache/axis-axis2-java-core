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

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * A PropertyInfo is constructed with a PropertyDescriptor and
 * exposes get/set methods to access the object on a bean that matches the PropertyDescriptor
 *
 */
public class PropertyInfo {
	PropertyDescriptor descriptor;
	private static Log log = LogFactory.getLog(PropertyInfo.class);
	/**
	 * @param propertyName
	 * @param descriptor
	 */
	public PropertyInfo(PropertyDescriptor descriptor) {
		super();
		this.descriptor = descriptor;
	}
	
	
	/**
     * Get the object 
	 * @param targetBean
	 * @return Object for this property or null
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 */
	public Object get(Object targetBean)throws InvocationTargetException, IllegalAccessException{
		Method method = descriptor.getReadMethod();
		return method.invoke(targetBean, null);
	}
	
	/** 
     * Set the object
	 * @param targetBean
	 * @param propValue
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws JAXBWrapperException
	 */
	public void set(Object targetBean, Object propValue)throws InvocationTargetException, IllegalAccessException, JAXBWrapperException{
		Method method = descriptor.getWriteMethod();
        // JAXB provides setters for atomic values.
        // For non-atomic values (i.e. lists, collections), there is no setter.
		if (method != null) {
            // Method exists, this is the atomic case
		    Object[] object = new Object[]{propValue};
		    Class[] paramTypes = method.getParameterTypes();
		    if(paramTypes !=null && paramTypes.length ==1){
		        Class paramType = paramTypes[0];
		        if(paramType.isPrimitive() && propValue == null){
		            //Ignoring null value for primitive type, this could potentially be the way of a customer indicating to set
		            //default values defined in JAXBObject/xmlSchema.
		            if(log.isDebugEnabled()){
		                log.debug("Ignoring null value for primitive type, this is the way to set default values defined in JAXBObject/xmlSchema. for primitive types");
		            }
		            return;
                }
			}
            method.invoke(targetBean, object);
		} else {
            // There is no setter, we will assume that this is the Collection case
            // Get the collection. (If there is no collection, the JAXB bean will construct a new one; thus 
            // collection will always be non-null.)
		    Collection collection = (Collection) get(targetBean);
            
            // Now add our our object to the collection
            collection.clear();
            if (propValue != null) {
                collection.addAll((Collection) propValue);
            }
        }
		
	}
	
}
