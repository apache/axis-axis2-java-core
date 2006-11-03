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
package org.apache.axis2.jaxws.message.databinding;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

/*
 * A JAXBBlockContext controls access to the JAXB Context/Marshal/Unmarshal code.
 * In addition the JAXBBlockContext contains additional contextural information needed
 * by the JAX-WS component (i.e. the type of the object)
 * 
 * This class is immutable after construction.
 */
public class JAXBBlockContext {

	private Class type = null;
	private JAXBContext jaxbContext = null;
	private boolean useJAXBElement = false;
	
	/**
	 * Normal Constructor JAXBBlockContext
	 * @param type Class object that represents the actual type of the object.
	 * @param useJAXBElement boolean indicating whether the object should be rendered
	 * as a JAXBElement.
	 * 
	 * Example: if the object is a primitive (type=int.class) then 
	 * useJAXBElement must be set to true because int is not a JAXB object.
	 * 
	 * Example: if the object is a JAXB object you would normally set useJAXBElement
	 * to false.  However if the JAXB object does not have a corresponding root element,
	 * then useJAXBElement hould be set to false.
	 */
	public JAXBBlockContext(Class type, boolean useJAXBElement) {
		this(type, useJAXBElement, null);
	}

	/**
	 * "Dispatch" Constructor
	 * Use this full constructor when the JAXBContent is provided by
	 * the customer.  
	 * @param type
	 * @param useJAXBElement
	 * @param jaxbContext
	 */
	public JAXBBlockContext(Class type, boolean useJAXBElement, JAXBContext jaxbContext) {
		this.type = type;
		this.useJAXBElement = useJAXBElement;
		this.jaxbContext = jaxbContext;
	}

	/**
	 * @return Class representing type of the element
	 */
	public Class getType() {
		return type;
	}

	/**
	 * @return indicate if object should be rendered as JAXBElement
	 */
	public boolean isUseJAXBElement() {
		return useJAXBElement;
	}

	/**
	 * @return get the JAXBContext
	 * @throws JAXBException
	 */
	public JAXBContext getJAXBContext() throws JAXBException {
		if (jaxbContext == null) {	
			if (!useJAXBElement) {
				jaxbContext = JAXBUtils.getJAXBContext(type);
			} else {
				jaxbContext = JAXBUtils.getJAXBContext(type);
			}
		}
		return jaxbContext;
	}
}
