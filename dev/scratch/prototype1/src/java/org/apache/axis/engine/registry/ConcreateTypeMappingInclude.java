/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

package org.apache.axis.engine.registry;

import java.util.HashMap;
import java.util.Vector;

import javax.xml.namespace.QName;

/**
 * @author hemapani@opensource.lk
 */
public class ConcreateTypeMappingInclude implements TypeMappingInclude{
    private HashMap javamapping;
    private HashMap xmlmapping;
    private Vector mappingValues;
    
    public ConcreateTypeMappingInclude(){
        javamapping = new HashMap();
        xmlmapping = new HashMap();
        mappingValues = new Vector();
    }
    
    public void addTypeMapping(TypeMapping typeMapping) {
        mappingValues.add(typeMapping); 
        javamapping.put(typeMapping.getJavaType().getName(),typeMapping); 
        xmlmapping.put(typeMapping.getXMLType(),typeMapping);

    }

    public TypeMapping getTypeMapping(Class javaType) {
        return (TypeMapping)javamapping.get(javaType);
    }

    public TypeMapping getTypeMapping(int index) {
        return (TypeMapping)mappingValues.get(index);
    }

    public TypeMapping getTypeMapping(QName xmlType) {
        return (TypeMapping)xmlmapping.get(xmlType);
    }

    public int getTypeMappingCount() {
        return mappingValues.size();
    }

}
