package org.apache.axis.om;

import java.util.Iterator;

/**
 * Copyright 2001-2004 The Apache Software Foundation.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p/>
 * User: Eran Chinthaka - Lanka Software Foundation
 * Date: Oct 4, 2004
 * Time: 11:52:18 AM
 *
 * One must make sure to insert relevant constructors for the classes that are implementing this interface
 */
public interface OMElement extends OMNamedNode {

    /**
     * This will add child to the element. One can decide whether he append the child or he adds to the
     * front of the children list
     *
     * @param omNode
     */
    public void addChild(OMNode omNode);

    /**
     * This will add a child in a specific location given in the index. If there are no preceding elements
     * this will throw an AxisFault.
     * Example if one tries to add child number four, but if there is no third child, throw an exception.
     *
     * @param omNode
     * @param index
     */
    public void addChild(OMNode omNode, int index) throws OMException;

    /**
     * Returns the first child of this element
     * @return
     */
    public OMNode getFirstChild();
    /**
     * This returns a collection of this element.
     * Children can be of types OMElement, OMText.
     */
    public Iterator getChildren();

    /**
     * THis will create a namespace in the current element scope
     *
     * @param uri
     * @param prefix
     * @return
     */
    public OMNamespace createNamespace(String uri, String prefix);

    /**
     * This will find a namespace with the given uri and prefix, in the scope of the docuemnt.
     * This will start to find from the current element and goes up in the hiararchy until this finds one.
     * If none is found, return null
     *
     * @param uri
     * @param prefix
     * @return
     * @throws OMException
     */
    public OMNamespace resolveNamespace(String uri, String prefix) throws OMException;

    /**
     * This will returns the first attribute of the element or null, if none is present
     *
     * @return
     */
    public OMAttribute getFirstAttribute();

    /**
     * This will return a List of OMAttributes
     * @return
     */
    public Iterator getAttributes();

    /**
     * This will insert attribute to this element. Implementor can decide as to insert this
     * in the front or at the end of set of attributes
     *
     * @param attr
     */
    public void insertAttribute(OMAttribute attr);

    /**
     * Remove the attribute from the attribute set
     * @param attr
     */
    public void removeAttribute(OMAttribute attr);

}
