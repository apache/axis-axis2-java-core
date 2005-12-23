/*
 * Copyright 2004,2005 The Apache Software Foundation.
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

package org.apache.axis2.wsdl.databinding;

import org.apache.axis2.om.OMElement;

import javax.xml.namespace.QName;
import java.util.Map;

public interface TypeMapper {

    /**
     * Default class name is the OMElement ?
     */
    public static final String DEFAULT_CLASS_NAME = OMElement.class.getName();

    /**
     * returns whether the mapping is the object type or the normal class name type
     * @return
     */
    public boolean isObjectMappingPresent();

    /**
     * Get the type mapping class name
     *
     * @param qname name of the XML element to be mapped
     * @return a string that represents the particular type
     */
    public String getTypeMappingName(QName qname);

    /**
     * Get the type mapping Object
     *
     * @param qname name of the XML element to be mapped
     * @return an Object that represents the particular class in a pre specified form.
     *         it can be a specific format to the databinding framework used
     *         This allows tight integrations with the databinding framework, allowing the emitter
     *         to write the databinding classes in his own way
     */
    public Object getTypeMappingObject(QName qname);

    /**
     * Get the parameter name
     *
     * @param qname name of the XML element to get a parameter
     * @return a unique parameter name
     */
    public String getParameterName(QName qname);

    /**
     * Adds a type mapping name to the type mapper
     *
     * @param qname
     * @param value
     * @see #getTypeMappingName(javax.xml.namespace.QName)
     */
    public void addTypeMappingName(QName qname, String value);

    /**
     * Adds a type mapping object to the type mapper
     *
     * @param qname the xml Qname that this type refers to
     * @param value the type mapping object
     * @see #getTypeMappingObject(javax.xml.namespace.QName)
     */
    public void addTypeMappingObject(QName qname, Object value);

    /**
     * @return a map containing all type mapping names
     * i.e. Qname to  classname
     */
    public Map getAllMappedNames();

    /**
     * @return a map containing all type mapping model objects
     * i.e. Qname to model objects
     */
    public Map getAllMappedObjects();
}
