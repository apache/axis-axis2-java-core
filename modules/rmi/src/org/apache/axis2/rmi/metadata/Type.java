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
import org.apache.axis2.rmi.metadata.xml.XmlType;
import org.apache.axis2.rmi.metadata.xml.XmlSchema;
import org.apache.axis2.rmi.metadata.xml.XmlImport;
import org.apache.axis2.rmi.exception.MetaDataPopulateException;
import org.apache.axis2.rmi.exception.SchemaGenerationException;
import org.apache.axis2.rmi.util.JavaTypeToQNameMap;
import org.apache.axis2.rmi.util.Constants;

import javax.xml.namespace.QName;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Iterator;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;

public class Type {

    /**
     * java class corresponds to this XmlType object
     */
    private Class javaClass;

    /**
     * list of attribute objects for this java class
     */
    private List attributes;

    /**
     * name of the Type : class name
     */
    private String name;

    /**
     * namespace of the type : depends on the package
     */
    private String namespace;

    /**
     * parent type for this type
     */
    private Type parentType;

    /**
     * xml metadata type correponding to this type object
     */
    private XmlType xmlType;

    private boolean isSchemaGenerated;


    public Type() {
        this.attributes = new ArrayList();
    }

    public Type(Class javaClass) {
        this();
        this.javaClass = javaClass;
    }

    /**
     * popualate the meta data corresponding to this type
     * @param configurator
     */
    public void populateMetaData(Configurator configurator,
                                 Map processedTypeMap)
            throws MetaDataPopulateException {
        // java class should alrady have populated.

        // if javaTypeToQNameMap contains this key then this is an either
        // primitive type or a Simple known type. we don't have to populate
        // the attribues
        try {
            if (!JavaTypeToQNameMap.containsKey(this.javaClass)) {
                this.name = this.javaClass.getName();
                this.name = this.name.substring(this.name.lastIndexOf(".") + 1);
                this.namespace = configurator.getNamespace(this.javaClass.getPackage().getName());

                Class superClass = this.javaClass.getSuperclass();

                // if the supper class is Object class nothing to warry
                if (!superClass.equals(Object.class) && !superClass.equals(Exception.class)) {
                    // then this is an extension class and we have to processit
                    if (!processedTypeMap.containsKey(superClass)) {
                        Type superClassType = new Type(superClass);
                        processedTypeMap.put(superClass, superClassType);
                        superClassType.populateMetaData(configurator, processedTypeMap);
                    }
                    this.setParentType((Type) processedTypeMap.get(superClass));
                }

                // we need informatin only about this class
                // supper class information is processed in the super class type
                BeanInfo beanInfo = Introspector.getBeanInfo(this.javaClass, this.javaClass.getSuperclass());
                PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
                Attribute attribute;
                for (int i = 0; i < propertyDescriptors.length; i++) {
                    // remove the class descriptor
                    attribute = new Attribute(propertyDescriptors[i], this.namespace);
                    attribute.populateMetaData(configurator, processedTypeMap);
                    this.attributes.add(attribute);
                }
            }
        } catch (IntrospectionException e) {
            throw new MetaDataPopulateException(
                    "Error Occured while getting the Bean info of the class " + this.javaClass.getName(), e);
        }

    }

    /**
     * this method sets the xmlType correctly. this method should only be invoked
     * if it has not already processed
     * @param configurator
     * @param schemaMap
     */

    public void generateSchema(Configurator configurator,
                               Map schemaMap)
            throws SchemaGenerationException {

        // here we have to populate the xmlType object properly
        this.isSchemaGenerated = true;
        if (JavaTypeToQNameMap.containsKey(this.javaClass)){
            // i.e. this is a basic type
            // no need to process or add this to schema list
            this.xmlType = new XmlType(JavaTypeToQNameMap.getTypeQName(this.javaClass));
            this.xmlType.setSimpleType(true);
        } else {

            // get the schema to add the complex type
            if (schemaMap.get(this.namespace) == null){
                // create a new namespace for this schema
                schemaMap.put(this.namespace, new XmlSchema(this.namespace));
            }
            XmlSchema xmlSchema = (XmlSchema) schemaMap.get(this.namespace);

            // we have to generate a complex type for this
            this.xmlType = new XmlType(new QName(this.namespace,this.name));
            this.xmlType.setSimpleType(false);

             // set the parent type for this type
            if (this.parentType != null){
                Type parentType = this.parentType;
                if (!parentType.isSchemaGenerated()){
                    parentType.generateSchema(configurator,schemaMap);
                }
                this.xmlType.setParentType(parentType.getXmlType());
                // import the complex type namespace if needed.
                if (!xmlSchema.containsNamespace(this.xmlType.getParentType().getQname().getNamespaceURI())){
                    // if the element namespace does not exists we have to add it
                    if (!this.xmlType.getParentType().getQname().getNamespaceURI().equals(Constants.URI_2001_SCHEMA_XSD)) {
                        XmlImport xmlImport = new XmlImport(this.xmlType.getParentType().getQname().getNamespaceURI());
                        xmlSchema.addImport(xmlImport);
                    }
                    xmlSchema.addNamespace(this.xmlType.getParentType().getQname().getNamespaceURI());
                }

            }

            // add elements of the attributes
            Attribute attribute;
            for (Iterator iter = this.attributes.iterator();iter.hasNext();){
                attribute = (Attribute) iter.next();
                if (!attribute.isSchemaGenerated()){
                    // if it is not already processed process it.
                    attribute.generateSchema(configurator,schemaMap);
                }
                this.xmlType.addElement(attribute.getElement());
                // we have to set the namespaces of these element complex types properly
                QName elementTypeQName = attribute.getElement().getType().getQname();
                if (!xmlSchema.containsNamespace(elementTypeQName.getNamespaceURI())){
                    // if the element namespace does not exists we have to add it
                    if (!elementTypeQName.getNamespaceURI().equals(Constants.URI_2001_SCHEMA_XSD)) {
                        XmlImport xmlImport = new XmlImport(elementTypeQName.getNamespaceURI());
                        xmlSchema.addImport(xmlImport);
                    }
                    xmlSchema.addNamespace(elementTypeQName.getNamespaceURI());
                }

            }
            // finally add this complex type to schema map
            xmlSchema.addComplexType(this.xmlType);

        }

    }

    public void populateAllAttributes(List attributesList){
        // we have to first add the parent details to keep the order.
        if (this.parentType != null){
            this.parentType.populateAllAttributes(attributesList);
        }
        attributesList.addAll(this.attributes);
    }

    public List getAllAttributes(){
        List allAttributesList = new ArrayList();
        populateAllAttributes(allAttributesList);
        return allAttributesList;
    }

    public boolean isSchemaGenerated() {
        return isSchemaGenerated;
    }

    public void setSchemaGenerated(boolean schemaGenerated) {
        isSchemaGenerated = schemaGenerated;
    }

    public Class getJavaClass() {
        return javaClass;
    }

    public void setJavaClass(Class javaClass) {
        this.javaClass = javaClass;
    }

    public List getAttributes() {
        return attributes;
    }

    public void setAttributes(List attributes) {
        this.attributes = attributes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public XmlType getXmlType() {
        return xmlType;
    }

    public void setXmlType(XmlType xmlType) {
        this.xmlType = xmlType;
    }

    public Type getParentType() {
        return parentType;
    }

    public void setParentType(Type parentType) {
        this.parentType = parentType;
    }

}
