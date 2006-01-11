package org.apache.axis2.schema;

import javax.xml.namespace.QName;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
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

/**
 * This class is used as a holder to pass on the meta information to the bean writer.
 * This meta information is used by the writer to write the databinding conversion code.
 * Note - Metainfholders are not meant to be reused!!!. They are per-class basis and are strictly
 * not thread safe!!!!
 */
public class BeanWriterMetaInfoHolder {


    private boolean ordered = false;
    private boolean anonymous = false;
    private boolean extension = false;
    private String extensionClassName = "";
    private Map elementToSchemaQNameMap = new HashMap();
    private Map elementToJavaClassMap = new HashMap();
    private Map specialTypeFlagMap = new HashMap();
    private Map qNameMaxOccursCountMap = new HashMap();
    private Map qNameMinOccursCountMap = new HashMap();
    private Map qNameOrderMap = new HashMap();

    /**
     * Gets the anonymous status.
     *
     * @return Returns boolean.
     */
    public boolean isAnonymous() {
        return anonymous;
    }

    /**
     * Sets the anonymous flag.
     *
     * @param anonymous
     */
    public void setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
    }

    /**
     * Sets the extensions base class name. Valid only when the isExtension
     * returns true.
     *
     * @return Returns String.
     */
    public String getExtensionClassName() {
        return extensionClassName;
    }

    /**
     * Sets the extensions base class name. Valid only when the isExtension
     * returns true.
     *
     * @param extensionClassName
     */
    public void setExtensionClassName(String extensionClassName) {
        this.extensionClassName = extensionClassName;
    }

    /**
     * Gets the extension status.
     *
     * @return Returns boolean.
     */
    public boolean isExtension() {
        return extension;
    }

    /**
     * Sets the extension status.
     *
     * @param extension
     */
    public void setExtension(boolean extension) {
        this.extension = extension;
    }

    /**
     * Gets the ordered status.
     *
     * @return Returns boolean.
     */
    public boolean isOrdered() {
        return ordered;
    }

    /**
     * Sets the ordered flag. 
     *
     * @param ordered
     */
    public void setOrdered(boolean ordered) {
        this.ordered = ordered;
    }

    /**
     * Registers a mapping.
     *
     * @param qName
     * @param schemaName
     * @param javaClassName
     */
    public void registerMapping(QName qName, QName schemaName, String javaClassName) {
        registerMapping(qName, schemaName, javaClassName, SchemaConstants.ELEMENT_TYPE);
    }

    /**
     * Registers a mapping.
     *
     * @param qName
     * @param schemaName
     * @param javaClassName
     * @param type
     */
    public void registerMapping(QName qName, QName schemaName, String javaClassName, Integer type) {
        this.elementToJavaClassMap.put(qName, javaClassName);
        this.elementToSchemaQNameMap.put(qName, schemaName);
        this.specialTypeFlagMap.put(qName, type);

    }

    /**
     * Gets the schema name for the given QName.
     *
     * @param eltQName
     * @return Returns QName.
     */
    public QName getSchemaQNameForQName(QName eltQName) {
        return (QName) this.elementToSchemaQNameMap.get(eltQName);
    }

    /**
     * Gets the class name for the QName.
     *
     * @param eltQName
     * @return Returns String.
     */
    public String getClassNameForQName(QName eltQName) {
        return (String) this.elementToJavaClassMap.get(eltQName);
    }

    /**
     * Gets whether a given QName is an attribute
     *
     * @param qName
     * @return Returns boolean.
     */
    public boolean getAttributeStatusForQName(QName qName) {
        Integer attribState = (Integer) specialTypeFlagMap.get(qName);
        return attribState != null && attribState.equals(SchemaConstants.ATTRIBUTE_TYPE);
    }

    /**
     * Gets whether a given QName represents a anyType
     *
     * @param qName
     * @return Returns boolean.
     */
    public boolean getAnyStatusForQName(QName qName) {
        Integer anyState = (Integer) specialTypeFlagMap.get(qName);
        return anyState != null && anyState.equals(SchemaConstants.ANY);
    }

    /**
     * Gets whether a given QName refers to an array.
     *
     * @param qName
     * @return Returns boolean.
     */
    public boolean getArrayStatusForQName(QName qName) {
        Integer anyState = (Integer) specialTypeFlagMap.get(qName);
        return anyState != null && anyState.equals(SchemaConstants.ANY_ARRAY_TYPE);
    }

    /**
     * Gets whether a given QName has the any attribute status.
     *
     * @param qName
     * @return Returns boolean.
     */
    public boolean getAnyAttributeStatusForQName(QName qName) {
        Integer anyState = (Integer) specialTypeFlagMap.get(qName);
        return anyState != null && anyState.equals(SchemaConstants.ANY_ATTRIBUTE_TYPE);
    }

    /**
     * Clears the whole set of tables.
     */
    public void clearTables() {
        this.elementToJavaClassMap.clear();
        this.elementToSchemaQNameMap.clear();
        this.elementToSchemaQNameMap.clear();
        this.elementToJavaClassMap.clear();
        this.specialTypeFlagMap.clear();
        this.qNameMaxOccursCountMap.clear();
        this.qNameMinOccursCountMap.clear();
        this.qNameOrderMap.clear();
    }

    /**
     * Adds the minOccurs associated with a QName.
     *
     * @param qName
     * @param minOccurs
     */
    public void addMinOccurs(QName qName, long minOccurs) {
        this.qNameMinOccursCountMap.put(qName, new Long(minOccurs));
    }

    /**
     * Registers a QName for the order.
     *
     * @param qName
     * @param index
     */
    public void registerQNameIndex(QName qName, int index) {
        this.qNameOrderMap.put(new Integer(index), qName);
    }

    /**
     * Adds the minOccurs associated with a QName.
     *
     * @param qName
     * @return Returns long.
     */
    public long getMinOccurs(QName qName) {
        Long l = (Long) this.qNameMinOccursCountMap.get(qName);
        return l != null ? l.longValue() : 1; //default for min is 1
    }

    /**
     * Gets the maxOccurs associated with a QName.
     *
     * @param qName
     * @return Returns long.
     */
    public long getMaxOccurs(QName qName) {
        Long l = (Long) this.qNameMaxOccursCountMap.get(qName);
        return l != null ? l.longValue() : 1; //default for max is 1
    }

    /**
     * Adds the maxOccurs associated with a QName.
     *
     * @param qName
     * @param maxOccurs
     */
    public void addMaxOccurs(QName qName, long maxOccurs) {
        this.qNameMaxOccursCountMap.put(qName, new Long(maxOccurs));
    }

    /**
     * @return Returns Iterator.
     * @deprecated Use #getQNameArray
     */
    public Iterator getElementQNameIterator() {
        return elementToJavaClassMap.keySet().iterator();
    }

    /**
     * Gets the QName array - may not be ordered.
     *
     * @return Returns QName[].
     */
    public QName[] getQNameArray() {
        Set keySet = elementToJavaClassMap.keySet();
        return (QName[]) keySet.toArray(new QName[keySet.size()]);
    }

    /**
     * Gets the ordered QName array - useful in sequences where the order needs to be preserved
     * Note - #registerQNameIndex needs to be called if this is to work properly!
     *
     * @return Returns QName[].
     */
    public QName[] getOrderedQNameArray() {
        //get the keys of the order map
        Set set = qNameOrderMap.keySet();
        int count = set.size();
        Integer[] keys = (Integer[]) set.toArray(new Integer[count]);
        Arrays.sort(keys);

        //Now refill the Ordered QName Array
        QName[] returnQNames = new QName[count];
        for (int i = 0; i < keys.length; i++) {
            returnQNames[i] = (QName) qNameOrderMap.get(keys[i]);

        }
        return returnQNames;
    }

}
