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
package org.apache.axis2.om.impl.llom;

import org.apache.axis2.om.OMAttribute;
import org.apache.axis2.om.OMNamespace;

import javax.xml.namespace.QName;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class OMAttributeImpl
 */
public class OMAttributeImpl implements OMAttribute {
    /**
     * Field localName
     */
    private String localName;

    /**
     * Field value
     */
    private String value;

    /**
     * Field namespace
     */
    private OMNamespace namespace;

    /**
     * Field QUOTE_ENTITY
     */
    private static String QUOTE_ENTITY = "&quot;";

    /**
     * Field matcher
     */
    private static Matcher matcher = Pattern.compile("\"").matcher(null);

    /**
     * Constructor OMAttributeImpl
     *
     * @param localName
     * @param ns
     * @param value
     */
    public OMAttributeImpl(String localName, OMNamespace ns, String value) {
        setLocalName(localName);
        setValue(value);
        setOMNamespace(ns);
    }

    /**
     * Method getQName
     *
     * @return
     */
    public QName getQName() {
        String namespaceName = (namespace != null)
                ? namespace.getName()
                : null;
        return new QName(namespaceName, localName);
    }

    // -------- Getters and Setters

    /**
     * Method getLocalName
     *
     * @return
     */
    public String getLocalName() {
        return localName;
    }

    /**
     * Method setLocalName
     *
     * @param localName
     */
    public void setLocalName(String localName) {
        this.localName = localName;
    }

    /**
     * Method getValue
     *
     * @return
     */
    public String getValue() {
        return value;
    }

    /**
     * Method setValue
     *
     * @param value
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Method setOMNamespace
     *
     * @param omNamespace
     */
    public void setOMNamespace(OMNamespace omNamespace) {
        this.namespace = omNamespace;
    }

    /**
     * Method getNamespace
     *
     * @return
     */
    public OMNamespace getNamespace() {
        return namespace;
    }
}
