package org.apache.axis.om;


import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

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
 * One must make sure to insert relevant constructors for the classes that are implementing this interface
 */
public interface OMElement extends OMNamedNode {

    /**
     * This will add child to the element. One must preserve the order of children, in this operation
     * Tip : appending the new child is prefered
     *
     * @param omNode
     */
    public void addChild(OMNode omNode);

    /**
     * This will search for children with a given QName and will return an iterator to traverse through
     * the OMNodes.
     * This QName can contain any combination of prefix, localname and URI
     *
     * @param elementQName
     * @return
     * @throws OMException
     */
    public Iterator getChildrenWithName(QName elementQName) throws OMException;

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
    public OMNamespace declareNamespace(String uri, String prefix);

    /**
     * @param namespace
     * @return
     */
    public OMNamespace declareNamespace(OMNamespace namespace);

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
    public OMNamespace findInScopeNamespace(String uri, String prefix) throws OMException;

    /**
     * This will ckeck for the namespace <B>only</B> in the current Element
     *
     * @param uri
     * @param prefix
     * @return
     * @throws OMException
     */
    public OMNamespace findDeclaredNamespace(String uri, String prefix) throws OMException;

    /**
     * This will provide a list of namespace defined within this Element <B>only</B>
     *
     * @return
     * @throws OMException
     */
    public Iterator getAllDeclaredNamespaces();

    /**
     * This will help to search for an attribute with a given QName within this Element
     *
     * @param qname
     * @return
     * @throws OMException
     */
    public OMAttribute getAttributeWithQName(QName qname) throws OMException;

    /**
     * This will return a List of OMAttributes
     *
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

    public void removeAttribute(OMAttribute attr);

    public void setBuilder(OMXMLParserWrapper wrapper);

    public OMXMLParserWrapper getBuilder();

    /**
     * Set the first child
     *
     * @param node
     */
    public void setFirstChild(OMNode node);

    /**
     * Get the first child
     *
     * @return
     */
    public OMNode getFirstChild();

    /**
     * Returns the pull parser that will generate the pull
     * events relevant to THIS element
     * @param cacheOff
     * @return
     */

    public XMLStreamReader getPullParser(boolean cacheOff);
}
