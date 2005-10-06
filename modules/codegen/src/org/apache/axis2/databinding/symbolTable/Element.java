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
public abstract class Element extends TypeEntry {

    /**
     * Create an Element object for an xml construct that references a type that has
     * not been defined yet.  Defer processing until refType is known.
     * 
     * @param pqName  
     * @param refType 
     * @param pNode   
     * @param dims    
     */
    protected Element(QName pqName, TypeEntry refType, Node pNode,
                      String dims) {
        super(pqName, refType, pNode, dims);
    }

    /**
     * Create a Element object for an xml construct that is not a base java type
     * 
     * @param pqName 
     * @param pNode  
     */
    protected Element(QName pqName, Node pNode) {
        super(pqName, pNode);
    }
}
