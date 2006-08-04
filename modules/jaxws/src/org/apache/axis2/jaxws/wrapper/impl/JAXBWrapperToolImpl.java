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
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.WeakHashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.namespace.QName;

import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.wrapper.JAXBWrapperTool;


public class JAXBWrapperToolImpl implements JAXBWrapperTool {

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.wrapped.JAXBWrapperTool#unWrap(java.lang.Object, javax.xml.bind.JAXBContext, java.util.ArrayList)
	 */
	
	/*
	 * create property descriptor using jaxbObject and child Names,
	 * getReader and read the object, form the object array and return them.
	 */
	
	public Object[] unWrap(Object jaxbObject, 
			ArrayList<String> childNames) throws JAXBWrapperException{
		try{
			if(jaxbObject == null){
				throw new JAXBWrapperException(Messages.getMessage("JAXBWrapperErr1"));
			}
			if(childNames == null){
				throw new JAXBWrapperException(Messages.getMessage("JAXBWrapperErr2"));
			}
			ArrayList<Object> objList = new ArrayList<Object>();
			Map<String , PropertyInfo> pdTable = createPropertyDescriptors(jaxbObject.getClass(), childNames);
			for(String childName:childNames){
				PropertyInfo propInfo = pdTable.get(childName);
				Object object = propInfo.get(jaxbObject);
				objList.add(object);
			}
			Object[] jaxbObjects = objList.toArray();
			objList = null;
			return jaxbObjects;
		}catch(IntrospectionException e){
			throw new JAXBWrapperException(e);
		}catch(IllegalAccessException e){
			throw new JAXBWrapperException(e);
		}catch(InvocationTargetException e){
			throw new JAXBWrapperException(e);
		}catch(NoSuchFieldException e){
			throw new JAXBWrapperException(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.wrapped.JAXBWrapperTool#wrap(java.lang.Class, java.lang.String, java.util.ArrayList, java.util.ArrayList)
	 */
	public Object wrap(Class jaxbClass, String jaxbClassName,
			ArrayList<String> childNames, Map<String, Object> childObjects)
			throws JAXBWrapperException {
		
		try{
			if(childNames == null|| childObjects == null){
				throw new JAXBWrapperException(Messages.getMessage("JAXBWrapperErr3"));
			}
			if(childNames.size() != childObjects.size()){
				throw new JAXBWrapperException(Messages.getMessage("JAXBWrapperErr4"));
			}
			Map<String, PropertyInfo> pdTable = createPropertyDescriptors(jaxbClass, childNames);
			Object jaxbObject = jaxbClass.newInstance();
			for(String childName:childNames){
				PropertyInfo propInfo = pdTable.get(childName);
				propInfo.set(jaxbObject, childObjects.get(childName));
			}
			return jaxbObject;
		}catch(IntrospectionException e){
			throw new JAXBWrapperException(e);
		}catch(InstantiationException e){
			throw new JAXBWrapperException(e);
		}catch(IllegalAccessException e){
			throw new JAXBWrapperException(e);
		}catch(InvocationTargetException e){
			throw new JAXBWrapperException(e);
		}catch(NoSuchFieldException e){
			throw new JAXBWrapperException(e);
		}
	}
	
	public JAXBElement wrapAsJAXBElement(Class jaxbClass, String jaxbClassName,
			ArrayList<String> childNames, Map<String, Object> childObjects) throws JAXBWrapperException{
		
		Object obj = wrap( jaxbClass, jaxbClassName, childNames, childObjects);
		JAXBElement<Object> element = new JAXBElement<Object>(new QName(jaxbClassName), jaxbClass, obj);
		return element;
	}
	
	/** creates propertyDescriptor for the childNames using the jaxbClass.  
	 * use Introspector.getBeanInfo().getPropertyDescriptors() to get all the property descriptors. Assert if # of childNames and propertyDescriptor array
	 * length do not match. if they match then get the xmlElement name from jaxbClass using propertyDescriptor's display name. See if the xmlElementName matches the 
	 * childName if not use xmlElement annotation name and create PropertyInfo add childName or xmlElement name there, set propertyDescriptor 
	 * and return Map<ChileName, PropertyInfo>.
	 * @param jaxbClass - Class jaxbClass name
	 * @param childNames - ArrayList<String> of childNames 
	 * @return Map<String, PropertyInfo> - map of ChildNames that map to PropertyInfo that hold the propertyName and PropertyDescriptor.
	 * @throws IntrospectionException, NoSuchFieldException
	 */
	private Map<String, PropertyInfo> createPropertyDescriptors(Class jaxbClass, ArrayList<String> childNames) throws IntrospectionException, NoSuchFieldException, JAXBWrapperException{
		Map<String, PropertyInfo> map = new WeakHashMap<String, PropertyInfo>();
		PropertyDescriptor[] pds = Introspector.getBeanInfo(jaxbClass).getPropertyDescriptors();
		
		Map<String, PropertyDescriptor>  jaxbClassPds = filterDescriptors(pds, jaxbClass);
		Field field[] = jaxbClass.getDeclaredFields();
		if(field.length != childNames.size()){
			throw new JAXBWrapperException(Messages.getMessage("JAXBWrapperErr4", jaxbClass.getName()));
		}
		pds=null;
		
		for(int i=0; i<field.length ;i++){
			PropertyInfo propInfo= null;
			String fieldName = field[i].getName();
			String childName = childNames.get(i);
			PropertyDescriptor pd = jaxbClassPds.get(childName);
			if(pd == null){
				pd = jaxbClassPds.get(fieldName);
				if(pd == null){
					throw new JAXBWrapperException(Messages.getMessage("JAXBWrapperErr4", jaxbClass.getName(), childName));
				}	
			}
			propInfo = new PropertyInfo(fieldName, pd);
			map.put(childName, propInfo);
		}
		jaxbClassPds = null;
		field = null;
		return map;
	}
	
	
	/** Filter PropertyDescriptors that belong to super class, return only the ones that belong to JABXClass
	 * create map of java fieldName and propertyDescriptor, if propertyName different than java fieldName then
	 * check the xmlElementName ensure they are same if not do conver both xmlName and propertyName to lowercase and
	 * ensure they are same if they match then add the corrosponding javaFieldName and PropertyDescriptor in map. If they dont 
	 * match the propertyName belongs to super class and we ignore it.
	 * @param allPds 
	 * @param jaxbClass
	 * @return
	 */
	private Map<String, PropertyDescriptor> filterDescriptors(PropertyDescriptor[] allPds, Class jaxbClass) throws NoSuchFieldException{
		Map<String, PropertyDescriptor> filteredPds = new WeakHashMap<String, PropertyDescriptor>();
		Field[] fields = jaxbClass.getDeclaredFields();
		for(PropertyDescriptor pd:allPds){
			for(Field field:fields){
				if(field.getName().equals(pd.getDisplayName())){
					filteredPds.put(pd.getDisplayName(), pd);
					break;
				}else{
					String xmlName =getXmlElementName(jaxbClass, field.getName());
					if(xmlName.equals(pd.getDisplayName())){
						filteredPds.put(field.getName(), pd);
						break;
					}
					if(xmlName.toLowerCase().equals(pd.getDisplayName().toLowerCase())){
						filteredPds.put(field.getName(), pd);
						break;
					}
				}
			}
		}
		allPds=null;
		return filteredPds;
	}
	
	/**
	 * Get the name of the xml element by looking at the XmlElement annotation.
	 * @param jaxbClass
	 * @param fieldName
	 * @return
	 * @throws NoSuchFieldException
	 */
	private String getXmlElementName(Class jaxbClass, String fieldName)throws NoSuchFieldException{
		Field field = jaxbClass.getDeclaredField(fieldName);
		XmlElement xmlElement =field.getAnnotation(XmlElement.class);
		
		// If XmlElement does not exist, default to using the field name
		if (xmlElement == null) {
			return fieldName;
		}
		return xmlElement.name();
		
	}
}
