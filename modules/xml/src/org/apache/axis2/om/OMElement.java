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
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.Iterator;

/**
 * A particular kind of node that represents an element infoset information item.
 *
 * <p>An element has a collection of children, attributes, and namespaces.</p>
 *
 * <p>In contrast with DOM, this interface exposes namespaces separately from the
 * attributes.</p>
 */
public interface OMElement extends OMNode, OMContainer {

    /**
     * Returns a filtered list of children - just the elements.
     *
     * @return an iterator over the child elements
     *
     * @see #getChildren()
     * @see #getChildrenWithName(javax.xml.namespace.QName)
     */ 
    public Iterator getChildElements();

    /**
     * THis will create a namespace in the current element scope
     *
     * @param uri   The namespace to declare in the current scope.  The
     *  caller is expected to ensure that the URI is a valid namespace name.
     * @param prefix    The prefix to associate with the given namespace.
     *  The caller is expected to ensure that this is a valid XML prefix.
     *
     * @return The created namespace information item.
     *
     * @see #declareNamespace(OMNamespace)
     * @see #findNamespace(String, String)
     * @see #getAllDeclaredNamespaces()
     */
    public OMNamespace declareNamespace(String uri, String prefix);

    /**
     * Declare a namespace with the element as its scope.
     *
     * @param namespace The namespace to declare
     *
     * @return The namespace parameter passed.
     *
     * @see #declareNamespace(String, String)
     * @see #findNamespace(String, String)
     * @see #getAllDeclaredNamespaces()
     */
    public OMNamespace declareNamespace(OMNamespace namespace);

    /**
     * This will find a namespace with the given uri and prefix, in the scope of the hierarchy.
     *
     * <p>This will search from the current element and go up the hiararchy until a match is found.
     * If no match is found, return <tt>null</tt>.</p>
     *
     * <p>Either <tt>prefix</tt> or <tt>uri</tt> should be null.  Results are undefined
     * if both are specified.</p>
     *
     * @param uri   The namespace to look for.  If this is specified, <tt>prefix</tt> should be null.
     * @param prefix    The prefix to look for.  If this is specified, <tt>uri</tt> should be null.
     *
     * @return The matching namespace declaration, or <tt>null</tt> if none was found.
     *
     * @see #declareNamespace(String, String)
     * @see #declareNamespace(OMNamespace)
     * @see #getAllDeclaredNamespaces()
     */
    public OMNamespace findNamespace(String uri, String prefix);

    /**
     * Returns an iterator for all of the namespaces declared on this element.
     *
     * <p>If you're interested in all namespaces in scope, you need to call this function
     * for all parent elements as well.  Note that the iterator may be invalidated by
     * any call to either <tt>declareNamespace</tt> function.
     * </p>
     *
     * @return  An iterator over the {@link OMNamespace} items declared on the current element.
     *
     * @see #findNamespace(String, String)
     * @see #declareNamespace(String, String)
     * @see #declareNamespace(OMNamespace)
     */
    public Iterator getAllDeclaredNamespaces() throws OMException;


    /**
     * This will search for an attribute with a given QName within this Element
     *
     * @param qname The attribute name to match.
     * @return The attribute, if found, or <tt>null</tt> otherwise.
     */
    public OMAttribute getFirstAttribute(QName qname);

    /**
     * This will return a List of OMAttributes.
     *
     * <p>Note that the iterator returned by this function will be invalidated by
     * any <tt>addAttribute</tt> call.
     * </p>
     *
     * @return Returns an {@link Iterator} of {@link OMAttribute} items associated with the element.
     *
     * @see #getAttribute
     * @see #addAttribute(OMAttribute)
     * @see #addAttribute(String, String, OMNamespace)
     */
    public Iterator getAllAttributes();

    /**
     * Return a named attribute if present.
     *
     * @param qname the qualified name to search for
     * @return an OMAttribute with the given name if found, or null
     */
    public OMAttribute getAttribute(QName qname);

    /**
     * This will add an attribute to this element.
     *
     * <p>There is no order implied by added attributes.</p>
     *
     * @param attr The attribute to add.
     *
     * @return The passed in attribute.
     */
    public OMAttribute addAttribute(OMAttribute attr);

    /**
     * Add an attribute to the current element.
     *
     * <p>This function does not check to make sure that the given attribute value can be serialized directly
     * as an XML value.  The caller may, for example, pass a string with the character 0x01.
     * @param attributeName The "local name" for the attribute.
     * @param value The string value of the attribute.
     * @param ns  The namespace has to be one of the in scope namespace. i.e. the passed namespace
     *  must be declared in the parent element of this attribute or ancestors of the parent element of the attribute.
     *
     * @return The added attribute.
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
     * Returns the builder object.
     *
     * @return The builder object used to construct the underlying XML infoset on the fly.
     */
    public OMXMLParserWrapper getBuilder();

    /**
     * Set the first child
     *
     * @param node
     */
    public void setFirstChild(OMNode node);

    /**
     * Returns the first child element of the element.
     *
     * @return The first child element of the element, or <tt>null</tt> if none was found.
     */

    public OMElement getFirstElement();


    /**
     * Returns the pull parser that will generate the pull
     * events relevant to THIS element.
     *
     * <p>Caching is on.</p>
     *
     * @return Return an XMLStreamReader relative to this element.
     */
    public XMLStreamReader getXMLStreamReader();

    /**
     * Returns the pull parser that will generate the pull
     * events relevant to THIS element.
     *
     * <p>Caching is off.</p>
     *
     * @return Return an XMLStreamReader relative to this element, with no caching.
     */
    public XMLStreamReader getXMLStreamReaderWithoutCaching();

    /**
     * @param text
     */
    public void setText(String text);

    /**
     * This will return the non-empty text children as a String
     *
     * @return A String representing the concatenation of the child text nodes.
     */
    public String getText();

    /**
     * Returns the local name of the element.
     *
     * @return The local name of the element.
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
     * Get the QName of this node
     *
     * @return The {@link QName} for the element.
     */
    public QName getQName();

    /**
     *  This is a convenience method only. This will basically serialize the given OMElement
     *  to a String but will build the OMTree in the memory
     */
    public String toString();

    /**
     *  This is a convenience method only. This will basically serialize the given OMElement
     *  to a String but will NOT build the OMTree in the memory. So you are at your own risk of
     *  losing information.
     */
    public String toStringWithConsume() throws XMLStreamException;


    /**
     * Turn a prefix:local qname string into a proper QName, evaluating it in the OMElement context
     * unprefixed qnames resolve to the local namespace
     * @param qname prefixed qname string to resolve
     * @return null for any failure to extract a qname.
     */
    QName resolveQName(String qname);
}
