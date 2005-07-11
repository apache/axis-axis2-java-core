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
package org.apache.wsdl.impl;

import org.apache.wsdl.Component;
import org.apache.wsdl.WSDLConstants;
import org.apache.wsdl.WSDLExtensibilityAttribute;
import org.apache.wsdl.WSDLExtensibilityElement;
import org.w3c.dom.Document;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Chathura Herath
 */
public class ComponentImpl implements WSDLConstants, Component {
    /**
     * Field componentProperties
     */
    protected HashMap componentProperties = new HashMap();

    /**
     * List of Element
     */
    protected List elments;

    /**
     * Field documentation
     */
    protected Document documentation = null;

    /**
     * Field Namespace Qualified elements that can be sticked in the component.
     */
    private List elements = null;

    /**
     * Field Namespace Qualified attrebutes that can be sticked int eh
     * component.
     */
    private List attributes = null;

    /**
     * Returns the Documentation Element as a <code>Document</code>.
     *
     * @return documentation
     */
    public Document getDocumentation() {
        return documentation;
    }

    /**
     * Will set the Documentation element for the Component.
     *
     * @param documentation Component Docuemntation
     */
    public void setDocumentation(Document documentation) {
        this.documentation = documentation;
    }

    /**
     * Returns the properties that are specific to this WSDL Component.
     *
     * @return
     */
    public HashMap getComponentProperties() {
        return componentProperties;
    }

    /**
     * Sets the properties of the Component if any.
     *
     * @param properties
     */
    public void setComponentProperties(HashMap properties) {
        this.componentProperties = properties;
    }

    /**
     * Will set the property keyed with the relavent key
     *
     * @param key Key in the map
     * @param obj Object to be put
     */
    public void setComponentProperty(Object key, Object obj) {
        this.componentProperties.put(key, obj);
    }

    /**
     * Gets the component property
     *
     * @param key key for the map search.
     * @return
     */
    public Object getComponentProperty(Object key) {
        return this.componentProperties.get(key);
    }

    /**
     * Adds the <code>Element</code> to this Component.
     *
     * @param element
     */
    public void addExtensibilityElement(WSDLExtensibilityElement element) {
        if (null == this.elements) {
            this.elements = new LinkedList();
        }
        if (null == element)
            return;
        this.elements.add(element);

    }

    /**
     * Returns the Extensibility Elements of this component;
     *
     * @return List of <code>Element</code> s
     */
    public List getExtensibilityElements() {
        if (null == this.elements) {
            this.elements = new LinkedList();
        }
        return this.elements;
    }

    /**
     * Adds the <code>ExtensibilityAttribute</code> as a attrebute of this
     * Component.
     *
     * @param attribute <code>ExtensibilityAttribute</code>
     */
    public void addExtensibleAttributes(WSDLExtensibilityAttribute attribute) {
        if (null == this.attributes) {
            this.attributes = new LinkedList();
        }
        if (null == attribute)
            return;
        this.attributes.add(attribute);
    }

    /**
     * Returns a <code>List</code> of ExtensibleAttributes of this component.
     *
     * @return <code>List</code>
     */
    public List getExtensibilityAttributes() {
        if (null == this.attributes) {
            this.attributes = new LinkedList();
        }
        return this.attributes;
    }
}