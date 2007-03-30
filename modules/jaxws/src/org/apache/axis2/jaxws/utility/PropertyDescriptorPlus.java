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

package org.apache.axis2.jaxws.utility;

import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.wrapper.impl.JAXBWrapperException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.beans.IndexedPropertyDescriptor;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

/**
 * A PropertyDescriptor provides acesss to a bean property.  Values can be queried/changed using the
 * read and writer methods of the PropertyDescriptor.
 * <p/>
 * A PropertyDescriptorPlus object wraps a PropertyDescriptor and supplies enhanced set/get methods
 * that match JAXB semantis.
 * <p/>
 * For example, the set(..) method is smart enough to add lists, arrays and atomic values on JAXB
 * beans.
 * <p/>
 * The PropertyDescriptorPlus object also stores the xmlName of the property.
 *
 * @See XMLRootElementUtil.createPropertyDescriptorMap , which creates the PropertyDescriptorPlus
 * objects
 */
public class PropertyDescriptorPlus {
    PropertyDescriptor descriptor;
    String xmlName = null;

    private static Log log = LogFactory.getLog(PropertyDescriptorPlus.class);

    /**
     * Package protected constructor.  Only created by XMLRootElementUtil.createPropertyDescriptorMap
     *
     * @param propertyName
     * @param descriptor
     * @see XMLRootElementUtil.createPropertyDescriptorMap
     */
    PropertyDescriptorPlus(PropertyDescriptor descriptor, String xmlName) {
        super();
        this.descriptor = descriptor;
        this.xmlName = xmlName;
    }

    /** @return xmlname */
    public String getXmlName() {
        return xmlName;
    }

    /** @return property type */
    public Class getPropertyType() {
        return descriptor.getPropertyType();
    }

    /** @return property name */
    public String getPropertyName() {
        return descriptor.getName();
    }

    /**
     * Get the object
     *
     * @param targetBean
     * @return Object for this property or null
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public Object get(Object targetBean) throws InvocationTargetException, IllegalAccessException {
        Method method = descriptor.getReadMethod();
        return method.invoke(targetBean, null);
    }

    /**
     * Set the object
     *
     * @param targetBean
     * @param propValue
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws JAXBWrapperException
     */
    public void set(Object targetBean, Object propValue)
            throws InvocationTargetException, IllegalAccessException, JAXBWrapperException {

        // No set occurs if the value is null
        if (propValue == null) {
            return;
        }

        // There are 3 different types of setters that can occur.
        // 1) Normal Attomic Setter : setFoo(type)
        // 2) Indexed Array Setter : setFoo(type[])
        // 3) No Setter case if the property is a List<T>.

        Method writeMethod = descriptor.getWriteMethod();
        if (descriptor instanceof IndexedPropertyDescriptor) {
            // Set for indexed  T[]
            setIndexedArray(targetBean, propValue, writeMethod);
        } else if (writeMethod == null) {
            // Set for List<T>
            setList(targetBean, propValue);
        } else {
            // Normal case
            setAtomic(targetBean, propValue, writeMethod);
        }
    }

    /**
     * Set the property value onto targetBean using the writeMethod
     *
     * @param targetBean
     * @param propValue
     * @param writeMethod (set(T))
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws JAXBWrapperException
     */
    private void setAtomic(Object targetBean, Object propValue, Method writeMethod)
            throws InvocationTargetException, IllegalAccessException, JAXBWrapperException {
        // JAXB provides setters for atomic value.
        Object[] object = new Object[] { propValue };
        Class[] paramTypes = writeMethod.getParameterTypes();
        if (paramTypes != null && paramTypes.length == 1) {
            Class paramType = paramTypes[0];
            if (paramType.isPrimitive() && propValue == null) {
                //Ignoring null value for primitive type, this could potentially be the way of a customer indicating to set
                //default values defined in JAXBObject/xmlSchema.
                if (log.isDebugEnabled()) {
                    log.debug(
                            "Ignoring null value for primitive type, this is the way to set default values defined in JAXBObject/xmlSchema. for primitive types");
                }
                return;
            }
        }
        writeMethod.invoke(targetBean, object);
    }

    /**
     * Set the property value using the indexed array setter
     *
     * @param targetBean
     * @param propValue
     * @param writeMethod set(T[])
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws JAXBWrapperException
     */
    private void setIndexedArray(Object targetBean, Object propValue, Method writeMethod)
            throws InvocationTargetException, IllegalAccessException, JAXBWrapperException {

        Class paramType = writeMethod.getParameterTypes()[0];
        Object value = asArray(propValue, paramType);
        // JAXB provides setters for atomic value.
        Object[] object = new Object[] { value };

        writeMethod.invoke(targetBean, value);
    }

    /**
     * Set the property value for the collection case.
     *
     * @param targetBean
     * @param propValue
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws JAXBWrapperException
     */
    private void setList(Object targetBean, Object propValue)
            throws InvocationTargetException, IllegalAccessException, JAXBWrapperException {
        // For the List<T> case, there is no setter. 
        // You are supposed to use the getter to obtain access to the collection and then add the collection

        Collection value = asCollection(propValue, descriptor.getPropertyType());
        Collection collection = (Collection)get(targetBean);

        // Now add our our object to the collection
        collection.clear();
        if (propValue != null) {
            collection.addAll(value);
        }
    }

    /**
     * @param propValue
     * @param destType
     * @return propValue as a Collection
     */
    private static Collection asCollection(Object propValue, Class destType) {
        // TODO Missing function
        // Convert the object into an equivalent object that is a collection
        if (ConvertUtils.isConvertable(propValue, destType)) {
            return (Collection)ConvertUtils.convert(propValue, destType);
        } else {
            String objectClass = (propValue == null) ? "null" : propValue.getClass().getName();
            throw ExceptionFactory.makeWebServiceException(
                    Messages.getMessage("convertProblem", objectClass, destType.getName()));

        }
    }

    /**
     * @param propValue
     * @param destType  T[]
     * @return array of component type
     */
    private static Object asArray(Object propValue, Class destType) {
        if (ConvertUtils.isConvertable(propValue, destType)) {
            return ConvertUtils.convert(propValue, destType);
        } else {
            String objectClass = (propValue == null) ? "null" : propValue.getClass().getName();
            throw ExceptionFactory.makeWebServiceException(
                    Messages.getMessage("convertProblem", objectClass, destType.getName()));

        }
    }

    public String toString() {
        String value = "PropertyDescriptorPlus[";
        value += " name=" + this.getPropertyName();
        value += " type=" + this.getPropertyType().getName();
        value += " propertyDecriptor=" + this.descriptor;
        return value + "]";
    }
}
