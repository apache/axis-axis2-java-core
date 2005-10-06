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
 * This class represents a TypeEntry that is a type (complexType, simpleType, etc.
 * 
 * @author Rich Scheuerle  (scheu@us.ibm.com)
 */
public abstract class Type extends TypeEntry {
    private boolean generated;	// true if java class is generated during WSDL->Java processing
    
    /**
     * Create a Type object for an xml construct name that represents a base type
     * 
     * @param pqName 
     */
    protected Type(QName pqName) {
        super(pqName);
    }

    /**
     * Create a TypeEntry object for an xml construct that references a type that has
     * not been defined yet.  Defer processing until refType is known.
     * 
     * @param pqName  
     * @param refType 
     * @param pNode   
     * @param dims    
     */
    protected Type(QName pqName, TypeEntry refType, Node pNode, String dims) {
        super(pqName, refType, pNode, dims);
    }

    /**
     * Create a Type object for an xml construct that is not a base type
     * 
     * @param pqName 
     * @param pNode  
     */
    protected Type(QName pqName, Node pNode) {
        super(pqName, pNode);
    }
    public void setGenerated(boolean b) {
        generated = b;        
    }
    
    public boolean isGenerated() {
        return generated;
    }
}
