package org.apache.axis2.databinding.schema;

import javax.xml.namespace.QName;
import java.util.*;
import java.lang.reflect.Array;
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
 * This is a class used as a holder to pass on the meta information to the bean writer
 * This meta information will be used by the writer to write the databinding conversion code
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
     * get the anon status
     * @return
     */
    public boolean isAnonymous() {
        return anonymous;
    }

    /**
     * Set the anonymous flag
     * @param anonymous
     */
    public void setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
    }

    /**
     * set the extensions base class name. Valid only when the isExtension
     * retruns true
     * @param extensionClassName
     * @return
     */
    public String getExtensionClassName() {
        return extensionClassName;
    }

    /**
     * set the extensions base class name. Valid only when the isExtension
     * retruns true
     * @param extensionClassName
     */
    public void setExtensionClassName(String extensionClassName) {
        this.extensionClassName = extensionClassName;
    }

    /**
     * get the extension status
     * @return
     */
    public boolean isExtension() {
        return extension;
    }

    /**
     * set the extension status
     * @param extension
     */
    public void setExtension(boolean extension) {
        this.extension = extension;
    }

    /**
     * ge the ordered statu
     * @return
     */
    public boolean isOrdered() {
        return ordered;
    }

    /**
     * set the ordered flag. this marks whether the
     * items are ordered or not
     * @param ordered
     */
    public void setOrdered(boolean ordered) {
        this.ordered = ordered;
    }

    /**
     * Register a mapping
     * @param qName
     * @param schemaName
     * @param javaClassName
     */
    public void registerMapping(QName qName,QName schemaName,String javaClassName){
        registerMapping(qName,schemaName,javaClassName,SchemaConstants.ELEMENT_TYPE);
    }

    /**
     * Register a mapping
     * @param qName
     * @param schemaName
     * @param javaClassName
     * @param type
     */
    public void registerMapping(QName qName,QName schemaName,String javaClassName,Integer type){
        this.elementToJavaClassMap.put(qName,javaClassName);
        this.elementToSchemaQNameMap.put(qName,schemaName);
        this.specialTypeFlagMap.put(qName,type);

    }

    /**
     * Get the schema name for the given QName
     * @param eltQName
     * @return
     */
    public QName getSchemaQNameForQName(QName eltQName){
        return (QName)this.elementToSchemaQNameMap.get(eltQName);
    }

    /**
     * get the class name for the QName
     * @param eltQName
     * @return
     */
    public String getClassNameForQName(QName eltQName){
        return (String)this.elementToJavaClassMap.get(eltQName);
    }

    /**
     * Get whether a given QName is an attribute
     * @param qName
     * @return
     */
    public boolean getAttributeStatusForQName(QName qName){
        Integer attribState = (Integer) specialTypeFlagMap.get(qName);
        return attribState != null && attribState.equals(SchemaConstants.ATTRIBUTE_TYPE);
    }

    /**
     * Get whether a given QName represents a anyType
     * @param qName
     * @return
     */
    public boolean getAnyStatusForQName(QName qName){
        Integer anyState = (Integer) specialTypeFlagMap.get(qName);
        return anyState != null && anyState.equals(SchemaConstants.ANY_TYPE);
    }

    /**
     *  Get whether a given QName refers to an array
     * @param qName
     * @return
     */
    public boolean getArrayStatusForQName(QName qName){
        Integer anyState = (Integer) specialTypeFlagMap.get(qName);
        return anyState != null && anyState.equals(SchemaConstants.ANY_ARRAY_TYPE);
    }

    /**
     * Get whether a given QName has the any attribute status
     * @param qName
     * @return
     */
    public boolean getAnyAttributeStatusForQName(QName qName){
        Integer anyState = (Integer) specialTypeFlagMap.get(qName);
        return anyState != null && anyState.equals(SchemaConstants.ANY_ATTRIBUTE_TYPE);
    }
    /**
     *  Clears the whole set of tables.
     */
    public void clearTables(){
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
     * add the maxOccurs associated with a QName
     * @param qName
     * @param minOccurs
     */
    public void addMinOccurs(QName qName, long minOccurs){
        this.qNameMinOccursCountMap.put(qName,new Long(minOccurs));
    }

    /**
     * register a QName for the order
     * @param qName
     * @param index
     */
    public void registerQNameIndex(QName qName, int index){
        this.qNameOrderMap.put(new Integer(index),qName);
    }

    /**
     * Add the minOccurs associated with a QName
     * @param qName
     * @return
     */
    public long getMinOccurs(QName qName){
        Long l =(Long) this.qNameMinOccursCountMap.get(qName);
        return l!=null?l.longValue():1; //default for min is 1
    }

    /**
     * get the maxOccurs associated with a QName
     * @param qName
     * @return
     */
    public long getMaxOccurs(QName qName){
        Long l =(Long) this.qNameMaxOccursCountMap.get(qName);
        return l!=null?l.longValue():1; //default for max is 1
    }

    /**
     * Add the maxOccurs associated with a QName
     * @param qName
     * @param maxOccurs
     */
    public void addMaxOccurs(QName qName, long maxOccurs){
        this.qNameMaxOccursCountMap.put(qName,new Long(maxOccurs));
    }

    /**
     * @deprecated Use #getQNameArray
     * @return
     */
    public Iterator getElementQNameIterator(){
        return elementToJavaClassMap.keySet().iterator();
    }

    /**
     * get the QName array - may not be ordered
     * @return
     */
    public QName[] getQNameArray(){
        Set keySet =elementToJavaClassMap.keySet();
        return (QName[])keySet.toArray(new QName[keySet.size()]);
    }

    /**
     * Get the ordered QName array - useful in sequences where the order needs to be preserved
     * Note - #registerQNameIndex needs to be called if this is to work properly!
     * @return
     */
    public QName[] getOrderedQNameArray(){
        //get the keys of the order map
        Set set = qNameOrderMap.keySet();
        int count = set.size();
        Integer[] keys =(Integer[]) set.toArray(new Integer[count]);
        Arrays.sort(keys);

        //Now refill the Ordered QName Array
        QName[] returnQNames = new QName[count];
        for (int i = 0; i < keys.length; i++) {
            returnQNames[i] = (QName)qNameOrderMap.get(keys[i]);

        }
        return returnQNames;
    }

}
