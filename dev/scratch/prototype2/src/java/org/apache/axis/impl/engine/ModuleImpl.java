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

package org.apache.axis.impl.engine;

import org.apache.axis.impl.registry.AbstractEngineElement;
import org.apache.axis.registry.Flow;
import org.apache.axis.registry.Module;
import org.apache.axis.registry.TypeMapping;

import javax.xml.namespace.QName;

public class ModuleImpl extends AbstractEngineElement implements Module {
    private Flow in;
    private Flow out;
    private Flow fault;
    private QName name;

    public ModuleImpl(QName name) {
        this.name = name;
    }

    public Flow getFaultFlow() {
        return fault;
    }

    public Flow getInFlow() {
        return in;
    }

    public Flow getOutFlow() {
        return out;
    }

    public void setFaultFlow(Flow flow) {
        this.fault = flow;
    }

    public void setInFlow(Flow flow) {
        this.in = flow;
    }

    public void setOutFlow(Flow flow) {
        this.out = flow;
    }

    /* (non-Javadoc)
     * @see org.apache.axis.registry.TypeMappingInclude#addTypeMapping(org.apache.axis.registry.TypeMapping)
     */
    public void addTypeMapping(TypeMapping typeMapping) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.apache.axis.registry.TypeMappingInclude#getTypeMapping(java.lang.Class)
     */
    public TypeMapping getTypeMapping(Class javaType) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.axis.registry.TypeMappingInclude#getTypeMapping(int)
     */
    public TypeMapping getTypeMapping(int index) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.axis.registry.TypeMappingInclude#getTypeMapping(javax.xml.namespace.QName)
     */
    public TypeMapping getTypeMapping(QName xmlType) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.axis.registry.TypeMappingInclude#getTypeMappingCount()
     */
    public int getTypeMappingCount() {
        // TODO Auto-generated method stub
        return 0;
    }

    public QName getName() {
        return name;
    }


}
