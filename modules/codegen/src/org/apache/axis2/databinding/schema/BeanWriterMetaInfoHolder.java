package org.apache.axis2.databinding.schema;

import javax.xml.namespace.QName;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
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
 * Note - Metainfholders are not meant to be reused!!!. They are per-class basis
 */
public class BeanWriterMetaInfoHolder {



    private boolean ordered = false;
    private boolean extension = false;
    private String extensionClassName = "";
    private Map elementToSchemaQNameMap = new HashMap();
    private Map elementToJavaClassMap = new HashMap();
    private Map specialTypeFlagMap = new HashMap();

    public String getExtensionClassName() {
        return extensionClassName;
    }

    public void setExtensionClassName(String extensionClassName) {
        this.extensionClassName = extensionClassName;
    }

    public boolean isExtension() {
        return extension;
    }

    public void setExtension(boolean extension) {
        this.extension = extension;
    }

    public boolean isOrdered() {
        return ordered;
    }

    public void setOrdered(boolean ordered) {
        this.ordered = ordered;
    }

    public void registerMapping(QName qName,QName schemaName,String javaClassName){
        registerMapping(qName,schemaName,javaClassName,SchemaConstants.ELEMENT_TYPE);
    }

    public void registerMapping(QName qName,QName schemaName,String javaClassName,Integer type){
        this.elementToJavaClassMap.put(qName,javaClassName);
        this.elementToSchemaQNameMap.put(qName,schemaName);
        this.specialTypeFlagMap.put(qName,type);

    }

    public QName getSchemaQNameForQName(QName eltQName){
        return (QName)this.elementToSchemaQNameMap.get(eltQName);
    }

    public String getJavaClassNameForQName(QName eltQName){
        return (String)this.elementToJavaClassMap.get(eltQName);
    }

    public boolean getAttributeStatusForQName(QName qName){
        Integer attribState = (Integer) specialTypeFlagMap.get(qName);
        return attribState != null && attribState.equals(SchemaConstants.ATTRIBUTE_TYPE);
    }

    public boolean getAnyStatusForQName(QName qName){
        Integer anyState = (Integer) specialTypeFlagMap.get(qName);
        return anyState != null && anyState.equals(SchemaConstants.ANY_TYPE);
    }

    public boolean getArrayStatusForQName(QName qName){
        Integer anyState = (Integer) specialTypeFlagMap.get(qName);
        return anyState != null && anyState.equals(SchemaConstants.ANY_ARRAY_TYPE);
    }

    public void clearTables(){
        this.elementToJavaClassMap.clear();
        this.elementToSchemaQNameMap.clear();

    }

    public Iterator getElementQNameIterator(){
        return elementToJavaClassMap.keySet().iterator();
    }


}
