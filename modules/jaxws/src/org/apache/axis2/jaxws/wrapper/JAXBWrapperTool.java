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

import java.util.List;
import java.util.Map;

import org.apache.axis2.jaxws.wrapper.impl.JAXBWrapperException;

/**
 * The JAXBWrapper tool is used to create a JAXB Object from a series of child objects (wrap) or
 * get the child objects from a JAXB Object (unwrap)
 */
public interface JAXBWrapperTool {
	/**
     * unwrap
     * Returns the list of child objects of the jaxb object
     * @param jaxbObject that represents the type
     * @param childNames list of xml child names as String
     * @return list of Objects in the same order as the element names.  
     */
   public Object[] unWrap(Object jaxbObject, List<String> childNames) throws JAXBWrapperException;


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
    public Object wrap(Class jaxbClass, List<String> childNames, Map<String, Object> childObjects) throws JAXBWrapperException;
    
    
}

