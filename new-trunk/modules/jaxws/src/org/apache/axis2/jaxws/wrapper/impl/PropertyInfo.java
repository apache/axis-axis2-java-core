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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class PropertyInfo {
	String propertyName;
	PropertyDescriptor descriptor;
	private static Log log = LogFactory.getLog(PropertyInfo.class);
	/**
	 * @param propertyName
	 * @param descriptor
	 */
	public PropertyInfo(String propertyName, PropertyDescriptor descriptor) {
		super();
		
		this.propertyName = propertyName;
		this.descriptor = descriptor;
	}
	
	public String getPropertyName(){
		return this.propertyName;
	}
	
	public Object get(Object targetBean)throws InvocationTargetException, IllegalAccessException{
		Method method = descriptor.getReadMethod();
		return method.invoke(targetBean, null);
	}
	
	public void set(Object targetBean, Object propValue)throws InvocationTargetException, IllegalAccessException, JAXBWrapperException{
		Method method = descriptor.getWriteMethod();
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
	}
	
}
