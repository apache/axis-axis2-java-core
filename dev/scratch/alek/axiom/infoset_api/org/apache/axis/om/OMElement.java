/*
 * Copyright 2004 The Apache Software Foundation.
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
package org.apache.axis.om;

import org.apache.xml.utils.QName;

/**
 * Represents
 * <a href="http://www.w3.org/TR/xml-infoset/#infoitem.element">Element Information Item</a>
 * except for in-scope namespaces that can be reconstructed by visiting this element parent,
 * checking its namespaces, then grandparent and so on. For convenience there are
 * methods to resolve namespace prefix for given namespace name.
 *
 * <br />NOTE: this representaiton is optimized for streaming - iterator approach that
 * allows gradual visiting of nodes is preferred over indexed access.
 *
 * @version $Revision: 1.23 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public interface OMElement extends OMContainer, OMContained, Cloneable{
    public static final String NO_NAMESPACE = "";

    //JDK15 covariant public OMElement clone() throws CloneNotSupportedException
    /**
     * Method clone
     *
     * @return   an Object
     *
     * @exception   CloneNotSupportedException
     *
     */
    public Object clone() throws CloneNotSupportedException;

    //----------------------------------------------------------------------------------------------
    // general properties

    /**
     * XML Infoset [base URI] property
     *
     * @return   a String
     *
     */
    public String getBaseUri();

    /**
     * XML Infoset [base URI] property
     *
     * @param    baseUri             a  String
     *
     */
    public void setBaseUri(String baseUri);

    /**
     * Get top most container that is either XmlDocument or XmlElement (may be event this element!!!)
     */
    public OMContainer getRoot();

    /**
     * XML Infoset [parent] property.
     * If current element is not child of containing parent XmlElement or XmlDocument
     * then builder exception will be thrown
     */
    public OMContainer getParent();

    /**
     * Method setParent
     *
     * @param    parent              a  XmlContainer
     *
     */
    public void setParent(OMContainer parent);

    /**
     * Return namespace of current element
     * (XML Infoset [namespace name] and [prefix] properties combined)
     * null is only returned if
     * element was created without namespace
     * */
    public OMNamespace getNamespace();

    /**
     * Return namespace name (XML Infoset [namespace name]property
     * or null if element has no namespace
     */
    public String getNamespaceName();

    /**
     * Set namespace ot use for theis element.
     * Note: namespace prefix is <b>always</b> ignored.
     */
    public void setNamespace(OMNamespace namespace);

    //    public String getPrefix();
    //    public void setPrefix(String prefix);

    /**
     * XML Infoset [local name] property.
     *
     * @return   a String
     *
     */
    public String getLocalName();

    /**
     * XML Infoset [local name] property.
     *
     * @param    name                a  String
     *
     */
    public void setLocalName(String name);



    /**
     * This will add child to the element. One must preserve the order of children, in this operation
     * Tip : appending the new child is prefered
     * @param omNode
     */
    public void addChild(Object child);

    /**
     * This will search for children with a given QName and will return an iterator to traverse through
     * the OMNodes.
     * This QName can contain any combination of prefix, localname and URI
     * @param elementQName
     * @return
     * @throws OMException
     */
    public Iterable getChildrenWithName(QName elementQName) throws OMException;

    /**
     * This returns a collection of this element.
     * Children can be of types OMElement, OMText.
     */
    public Iterable getChildren();

    //----------------------------------------------------------------------------------------------
    // namespaces

    /**
     * Add namespace to current element (both prefix and namespace name must be not null)
     */
    public void declareNamespace(OMNamespace namespace);

    /**
     * Method hasNamespaceDeclarations
     *
     * @return   a boolean
     */
    public boolean hasNamespaceDeclarations();

    /**
     * This will find a namespace with the given uri and prefix, in the scope of the docuemnt.
     * Find namespace corresponding to namespace prefix and namespace name (at least one must be not null)
     * checking first current elemen and if not found continue in parent (if element has parent)
     * and so on.
     */
    public OMNamespace resolveNamespace(String uri, String prefix) throws OMException;

    /**
     * Create new unattached namespace with null prefix (namespace name must be not null).
     */
    public OMNamespace newNamespace(String namespaceName);

    /**
     * Create new namespace with prefix and namespace name (both must be not null).
     */
    public OMNamespace newNamespace(String prefix, String namespaceName);

    /**
     * Method removeAllNamespaceDeclarations
     *
     */
    public void removeAllNamespaceDeclarations();


    //----------------------------------------------------------------------------------------------
    // attributes

    /**
     * This return an attribute with a given QName within this Element or null of none found.
     * @param qname
     * @return
     * @throws OMException
     */
    public OMAttribute getAttribute(QName attributeName) throws OMException;

    /**
     * This will return a List of OMAttributes
     *
     * @return
     */
    public Iterable getAttributes();

    /**
     * Check if this element has any attributes.
     *
     * @return   a boolean
     *
     */
    public boolean hasAttributes();

    /**
     * This will insert attribute to this element
     * (note attributes order is undefined in XML Infoset!).
     *
     * @param attr
     */
    public void setAttribute(OMAttribute attr);

    /**
     * Remove attribute
     *
     * @param attr
     */
    public void removeAttribute(OMAttribute attr);
    public void removeAttribute(QName attributeName);

    /**
     * Method removeAllAttributes
     *
     */
    public void removeAllAttributes();
}


