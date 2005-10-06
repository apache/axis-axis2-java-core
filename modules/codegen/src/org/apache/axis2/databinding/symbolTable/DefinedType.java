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
package org.apache.axis2.databinding.symbolTable;

import org.w3c.dom.Node;

import javax.xml.namespace.QName;

/**
 * This Type is for a QName that is a complex or simple type, these types are
 * always emitted.
 */
public class DefinedType extends Type {

    // cache lookups for our base type

    /** Field extensionBase */
    protected TypeEntry extensionBase;
    
    /** Field searchedForExtensionBase */
    protected boolean searchedForExtensionBase = false;

    /**
     * Constructor DefinedType
     * 
     * @param pqName 
     * @param pNode  
     */
    public DefinedType(QName pqName, Node pNode) {
        super(pqName, pNode);
    }

    /**
     * Constructor DefinedType
     * 
     * @param pqName  
     * @param refType 
     * @param pNode   
     * @param dims    
     */
    public DefinedType(QName pqName, TypeEntry refType, Node pNode,
                       String dims) {
        super(pqName, refType, pNode, dims);
    }

    /**
     * Get a TypeEntry for the base type of this type, if one exists.
     * 
     * @param symbolTable a <code>SymbolTable</code> value
     * @return a <code>TypeEntry</code> value
     */
    public TypeEntry getComplexTypeExtensionBase(SymbolTable symbolTable) {

        if(!searchedForExtensionBase) {
            if (null == extensionBase) {
                extensionBase =
                        SchemaUtils.getComplexElementExtensionBase(getNode(),
                                symbolTable);
            }
            searchedForExtensionBase = true;
        }

        return extensionBase;
    }
}
