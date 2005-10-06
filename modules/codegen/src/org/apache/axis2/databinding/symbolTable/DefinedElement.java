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
 * This Type is for a QName that is an element, these types are only emitted if
 * referenced by a ref= or an element=.
 * An element type can be defined inline or it can be defined via
 * a ref/type attribute.
 */
public class DefinedElement extends Element {

    /**
     * Create an element type defined by a ref/type attribute
     * 
     * @param pqName  
     * @param refType 
     * @param pNode   
     * @param dims    
     */
    public DefinedElement(QName pqName, TypeEntry refType, Node pNode,
                          String dims) {
        super(pqName, refType, pNode, dims);
    }

    /**
     * Create an element type defined directly.
     * 
     * @param pqName 
     * @param pNode  
     */
    public DefinedElement(QName pqName, Node pNode) {
        super(pqName, pNode);
    }
}
