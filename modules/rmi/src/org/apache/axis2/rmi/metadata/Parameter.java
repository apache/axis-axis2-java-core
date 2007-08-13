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
package org.apache.axis2.rmi.metadata;

import org.apache.axis2.rmi.Configurator;
import org.apache.axis2.rmi.util.Util;
import org.apache.axis2.rmi.util.Constants;
import org.apache.axis2.rmi.metadata.xml.XmlElement;
import org.apache.axis2.rmi.exception.MetaDataPopulateException;
import org.apache.axis2.rmi.exception.SchemaGenerationException;

import java.util.Map;
import java.util.List;

/**
 * this class is used to keep the Parameter details of a java method
 */
public class Parameter {

    /**
     * java class represents this parameter
     */
    private Class javaClass;
    /**
     * parameter name
     */
    private String name;

    /**
     * namespace of this parameter
     * this should always null. i.e no namespace for an parameter
     */
    private String namespace;

    /**
     * XmlType of the parameter
     */
    private Type type;

    /**
     * is this parameter is array type
     */
    private boolean isArray;

    /**
     * xml Element corresponding to this parameter
     */
    private XmlElement element;

    /**
     * whether the schema is generated or not
     */
    private boolean isSchemaGenerated;

    /**
     * type of the parameter class
     */
    private int classType;


    /**
     * default constructor
     */
    public Parameter() {
    }

    public Parameter(Class javaClass, String name, String namespace) {
        this.javaClass = javaClass;
        this.name = name;
        this.namespace = namespace;
    }


    public Parameter(Class javaClass, String name) {
        this(javaClass,name,null);
    }

    public void populateMetaData(Configurator configurator,
                                 Map processedTypeMap)
            throws MetaDataPopulateException {

        Class baseClass;
        try {
            this.classType = Util.getClassType(this.javaClass);
            if ((this.classType & Constants.COLLECTION_TYPE) == Constants.COLLECTION_TYPE) {
                // i.e this is a collection class
                this.isArray = true;
                baseClass = Object.class;
                //TODO: handle map type
            } else {
                // populate the type for this parameter
                this.isArray = this.javaClass.isArray();

                if (this.isArray) {
                    baseClass = this.javaClass.getComponentType();
                } else {
                    baseClass = this.javaClass;
                }
            }

            if (processedTypeMap.containsKey(baseClass)) {
                // i.e we have already process this type
                this.type = (Type) processedTypeMap.get(baseClass);
            } else {
                this.type = new Type(baseClass);
                // we have to do this before calling to populate meta data
                // to avoid cirecular references
                processedTypeMap.put(baseClass, this.type);
                this.type.populateMetaData(configurator, processedTypeMap);
            }
        } catch (IllegalAccessException e) {
            throw new MetaDataPopulateException("Can not instataite class "
                    + this.javaClass.getName(), e);
        } catch (InstantiationException e) {
            throw new MetaDataPopulateException("Can not instataite class "
                    + this.javaClass.getName(), e);
        }
    }

    /**
     * this method sets the XMLElement correctly. this method should be called only
     * if this is not processed
     *
     * @param configurator
     * @param schemaMap
     * @throws org.apache.axis2.rmi.exception.SchemaGenerationException
     *
     */
    public void generateSchema(Configurator configurator,
                               Map schemaMap)
            throws SchemaGenerationException {
        // here we have to send the XmlElement correctly
        this.isSchemaGenerated = true;
        this.element = new XmlElement(!this.isArray && this.type.getJavaClass().isPrimitive());
        this.element.setName(this.name);
        this.element.setNamespace(this.namespace);

        if (!this.type.isSchemaGenerated()) {
            this.type.generateSchema(configurator, schemaMap);
        }
        this.element.setType(this.type.getXmlType());
        this.element.setArray(this.isArray);

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Class getJavaClass() {
        return javaClass;
    }

    public void setJavaClass(Class javaClass) {
        this.javaClass = javaClass;
    }

    public boolean isArray() {
        return isArray;
    }

    public void setArray(boolean array) {
        isArray = array;
    }

    public XmlElement getElement() {
        return element;
    }

    public void setElement(XmlElement element) {
        this.element = element;
    }

    public boolean isSchemaGenerated() {
        return isSchemaGenerated;
    }

    public void setSchemaGenerated(boolean schemaGenerated) {
        isSchemaGenerated = schemaGenerated;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public int getClassType() {
        return classType;
    }

    public void setClassType(int classType) {
        this.classType = classType;
    }
}
