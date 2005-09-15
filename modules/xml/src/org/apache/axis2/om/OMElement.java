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

package org.apache.axis2.om;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import java.util.Iterator;

/**
 * Interface OMElement
 */
public interface OMElement extends OMNode, OMContainer {
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
     * This will match localName and namespaceURI only. localName only or namespaceURI only can also be given. But
     * the other parameter should be set to null
     * Example : If you want to get all the addressing headers, header.getChildrenWithName(new QName(AddressingNamespace, null))
     * will do.
     *
     * @param elementQName
     * @return
     * @throws OMException
     */
    public Iterator getChildrenWithName(QName elementQName) throws OMException;

    /**
     * This will return the first matched child, return null if none found
     *
     * @param elementQName
     * @return
     * @throws OMException
     */
    public OMElement getFirstChildWithName(QName elementQName) throws OMException;


    /**
     * This returns a collection of this element.
     * Children can be of types OMElement, OMText.
     *
     * @return
     */
    public Iterator getChildren();

    /**
     * Returns a filtered list of children - just the elements.
     *
     * @return an iterator over the child elements
     */ 
    public Iterator getChildElements();

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
     * This will find a namespace with the given uri and prefix, in the scope of the hierarchy.
     * This will start to find from the current element and goes up in the hiararchy until this finds one.
     * If none is found, return null
     *
     * @param uri
     * @param prefix
     * @return
     * @throws OMException
     */
    public OMNamespace findNamespace(String uri, String prefix)
            throws OMException;

    /**
     * @return
     * @throws OMException
     */
    public Iterator getAllDeclaredNamespaces() throws OMException;


    /**
     * This will help to search for an attribute with a given QName within this Element
     *
     * @param qname
     * @return
     * @throws OMException
     */
    public OMAttribute getFirstAttribute(QName qname) throws OMException;

    /**
     * This will return a List of OMAttributes
     *
     * @return
     */
    public Iterator getAttributes();

    /**
     * Return a named attribute if present
     *
     * @param qname the qualified name to search for
     * @return an OMAttribute with the given name if found, or null
     */
    public OMAttribute getAttribute(QName qname);

    /**
     * This will insert attribute to this element. Implementor can decide as to insert this
     * in the front or at the end of set of attributes
     *
     * @param attr
     * @return
     */
    public OMAttribute addAttribute(OMAttribute attr);

    /**
     * @param attributeName
     * @param value
     * @param ns            - the namespace has to be one of the in scope namespace. i.e. the passed namespace
     *                      must be declared in the parent element of this attribute or ancestors of the parent element of the attribute
     * @return
     */
    public OMAttribute addAttribute(String attributeName, String value,
                                    OMNamespace ns);

    /**
     * Method removeAttribute
     *
     * @param attr
     */
    public void removeAttribute(OMAttribute attr);

    /**
     * Method setBuilder
     *
     * @param wrapper
     */
    public void setBuilder(OMXMLParserWrapper wrapper);

    /**
     * Method getBuilder
     *
     * @return
     */
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
     * Convenience extension of the getFirstChild
     *
     * @return
     */

    public OMElement getFirstElement();


    /**
     * Returns the pull parser that will generate the pull
     * events relevant to THIS element. Caching is on
     *
     * @return
     */
    public XMLStreamReader getXMLStreamReader();

    /**
     * Returns the pull parser that will generate the pull
     * events relevant to THIS element.caching is off
     *
     * @return
     */
    public XMLStreamReader getXMLStreamReaderWithoutCaching();

    /**
     * @param text
     */
    public void setText(String text);

    /**
     * This will return the non-empty text children as a String
     *
     * @return
     */
    public String getText();

    /**
     * Method getLocalName
     *
     * @return
     */
    public String getLocalName();

    /**
     * Method setLocalName
     *
     * @param localName
     */
    public void setLocalName(String localName);

    /**
     * @return the OMNamespace object associated with this element
     * @throws OMException
     */
    public OMNamespace getNamespace() throws OMException;

    /**
     * sets the Namespace
     *
     * @param namespace
     */
    public void setNamespace(OMNamespace namespace);

    /**
     * Get the Qname of this node
     *
     * @return
     */
    public QName getQName();


}
