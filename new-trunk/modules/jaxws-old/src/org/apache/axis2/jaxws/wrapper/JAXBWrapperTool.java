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

package org.apache.axis2.jaxws.wrapper;

import java.util.ArrayList;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;

import org.apache.axis2.jaxws.wrapper.impl.JAXBWrapperException;


public interface JAXBWrapperTool {
	/**
     * unwrap
     * Returns the list of child elements of the jaxb object
     * @param javab Object that is the wrapper element
     * @param jaxbContext JAXBContext
     * @param childNames list of xml child names as String
     * @return list of Objects in the same order as the element names.
     */
   public Object[] unWrap(Object jaxbObject, ArrayList<String> childNames) throws JAXBWrapperException;


    /**
     * wrap
     * Creates a jaxb object that is initialized with the child objects
     * @param javabClass Class of the JAXB object to return
     * @param jaxbContext JAXBContext
     * @param childObjects, component objects
     * @param childNames list of xml child names as String
     * @return list of Objects in the same order as the element names.
     */ 
    public Object wrap(Class jaxbClass, String jaxbClassName, ArrayList<String> childNames, Map<String, Object> childObjects) throws JAXBWrapperException;
    
    /**
     * wrapAsJAXBElement
     * Creates a JAXBElement that is initialized with the child objects and can be serialsed to xml later.
     * @param javabClass Class of the JAXB object to return
     * @param jaxbContext JAXBContext
     * @param childObjects, component objects
     * @param childNames list of xml child names as String
     * @return JAXBElement;
     */
    public JAXBElement wrapAsJAXBElement(Class jaxbClass, String jaxbClassName,
			ArrayList<String> childNames, Map<String, Object> childObjects) throws JAXBWrapperException;
		
}

