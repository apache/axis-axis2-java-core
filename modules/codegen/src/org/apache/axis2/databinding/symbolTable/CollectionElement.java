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
 * This Element is for a QName that is a 'collection'.
 * For example,
 * <element ref="bar" maxOccurs="unbounded" />
 * We need a way to indicate in the symbol table that a foo is
 * 'collection of bars',  In such cases a collection element is
 * added with the special QName  <name>[<minOccurs>, <maxOccurs>]
 */
public class CollectionElement extends DefinedElement implements CollectionTE {

    /**
     * Constructor CollectionElement
     * 
     * @param pqName  
     * @param refType 
     * @param pNode   
     * @param dims    
     */
    public CollectionElement(QName pqName, TypeEntry refType, Node pNode,
                             String dims) {
        super(pqName, refType, pNode, dims);
    }
}
