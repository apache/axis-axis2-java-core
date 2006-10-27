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
import javax.xml.bind.JAXBIntrospector;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

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
	private JAXBIntrospector introspector = null;
	
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
				// TODO Need J2W AccessController
				// TODO Need to cache this
				jaxbContext = JAXBContext.newInstance(new Class[]{type});
			} else {
				// TODO This may be overkill.
				jaxbContext = JAXBContext.newInstance(new Class[]{type});
			}
		}
		return jaxbContext;
	}



	/**
	 * @return Unmarshaller
	 * @throws JAXBException
	 */
	public Unmarshaller getUnmarshaller() throws JAXBException {
		// TODO A New unmarahller is always created.  We should consider how to recognize if when a marshaller can be reused.
		
		Unmarshaller unmarshaller = null;
		JAXBContext jc = getJAXBContext();
		if (!useJAXBElement) {
			// TODO Caching
			unmarshaller = jc.createUnmarshaller();
	     } else {
			// TODO There may be a way to share JAXBElement unmarshallers ?
			unmarshaller = jc.createUnmarshaller();
		}
		// TODO Additional options for unmarshaller ?
			
		// TODO Should we set up MTOM Attachment handler here ?
		
		return unmarshaller;
	}
	
	/**
	 * @return Marshaller
	 * @throws JAXBException
	 */
	public Marshaller getMarshaller() throws JAXBException {
		// TODO A New marahller is always created.  We should consider how to recognize if when a marshaller can be reused.
		Marshaller marshaller = null;
		JAXBContext jc = getJAXBContext();
		if (!useJAXBElement) {
			// TODO Caching
			marshaller = jc.createMarshaller();
		} else {
			// TODO There may be a way to share these ?
			marshaller = jc.createMarshaller();
		}
		// TODO Additional options for marshaller ?
		marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE); // No PIs

		// TODO Should we set up MTOM Attachment handler here ?
		return marshaller;
	}
	
	/**
	 * @return Intospector
	 * @throws JAXBException
	 */
	public JAXBIntrospector getIntrospector() throws JAXBException {
		if (introspector == null) {
			JAXBContext jc = getJAXBContext();
			if (!useJAXBElement) {
				// TODO Caching
				introspector = jc.createJAXBIntrospector();
			} else {
				// TODO There may be a way to share these ?
				introspector = jc.createJAXBIntrospector();
			}
			// TODO Additional options for unmarshaller ?
			
			// TODO Should we set up MTOM Attachment handler here ?
		}
		return introspector;
	}
}
