package org.apache.axis.om.impl;

import org.apache.axis.om.*;
import org.apache.axis.om.impl.util.OMChildrenIterator;
import org.apache.axis.om.impl.util.OMChildrenQNameIterator;
import org.apache.xml.utils.QName;

import java.io.PrintStream;
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
 * Date: Oct 5, 2004
 * Time: 1:16:10 PM
 */
public class OMElementImpl extends OMNamedNodeImpl implements OMElement {
    private OMNodeImpl firstChild;
    OMXmlPullParserWrapper builder;
    OMAttributeImpl firstAttribute;
    OMNamespaceImpl firstNamespace;

    public OMElementImpl(OMElement parent) {
        super(parent);
    }

    public OMElementImpl(String localName, OMNamespace ns) {
        super(localName, ns, null);
        done = true;
    }

    public OMElementImpl(String localName, OMNamespace ns, OMElement parent, OMXmlPullParserWrapper builder) {
        super(localName, ns, parent);
        this.builder = builder;

    }

    /**
     * This will add child to the element. One can decide whether he append the child or he adds to the
     * front of the children list
     *
     * @param child
     */
    public void addChild(OMNode child) {
        addChild((OMNodeImpl) child);
    }

    /**
     * This will search for children with a given QName and will return an iterator to traverse through
     * the OMNodes.
     * This QName can contain any combination of prefix, localname and URI
     *
     * @param elementQName
     * @return
     * @throws org.apache.axis.om.OMException
     */
    public Iterator getChildrenWithName(QName elementQName) throws OMException {
        return new OMChildrenQNameIterator(getFirstChild(), elementQName);
    }

    private void addChild(OMNodeImpl child) {
        if (firstChild == null && !done)
            builder.next();
        child.setPreviousSibling(null);
        child.setNextSibling(firstChild);
        if (firstChild != null)
            firstChild.setPreviousSibling(child);
        child.setParent(this);
        child.setComplete(true);
        firstChild = child;
    }

    /**
     * This will give the next sibling. This can be an OMAttribute for OMAttribute or OMText or OMELement for others.
     *
     * @return
     * @throws org.apache.axis.om.OMException
     */
    public OMNode getNextSibling() throws OMException {
        while (!done)
            builder.next();
        return super.getNextSibling();
    }

    /**
     * This returns a collection of this element.
     * Children can be of types OMElement, OMText.
     */
    public Iterator getChildren() {
        return new OMChildrenIterator(getFirstChild());
    }

    /**
     * THis will create a namespace in the current element scope
     *
     * @param uri
     * @param prefix
     * @return
     */
    public OMNamespace createNamespace(String uri, String prefix) {
        OMNamespaceImpl ns = new OMNamespaceImpl(uri, prefix);
        ns.setNextSibling(firstNamespace);
        firstNamespace = ns;
        return ns;
    }

    /**
     * This will find a namespace with the given uri and prefix, in the scope of the docuemnt.
     * This will start to find from the current element and goes up in the hiararchy until this finds one.
     * If none is found, return null
     *
     * @param uri
     * @param prefix
     * @return
     * @throws org.apache.axis.om.OMException
     */
    public OMNamespace resolveNamespace(String uri, String prefix) throws OMException {
        OMNamespace ns = firstNamespace;
        while (ns != null) {
            if (ns.equals(uri, prefix))
                return ns;
            ns = (OMNamespace) ns.getNextSibling();
        }
        if (parent != null)
            return parent.resolveNamespace(uri, prefix);
        return null;
    }

    /**
     * This will help to search for an attribute with a given QName within this Element
     *
     * @param qname
     * @return
     * @throws org.apache.axis.om.OMException
     */
    public Iterator getAttributeWithQName(QName qname) throws OMException {
        return new OMChildrenQNameIterator((OMNodeImpl) getFirstAttribute(), qname);
    }

    /**
     * This will returns the first attribute of the element or null, if none is present
     *
     * @return
     */
    public OMAttribute getFirstAttribute() {
        return firstAttribute;
    }

    /**
     * This will return a List of OMAttributes
     *
     * @return
     */
    public Iterator getAttributes() {
       return new OMChildrenIterator(getFirstAttribute());
    }

    /**
     * This will insert attribute to this element. Implementor can decide as to insert this
     * in the front or at the end of set of attributes
     *
     * @param attr
     */
    public void insertAttribute(OMAttribute attr) {

        OMAttributeImpl attrImpl = (OMAttributeImpl) attr;
        attrImpl.setPreviousSibling(null);
        attrImpl.setNextSibling(firstAttribute);
        if (firstAttribute != null)
            firstAttribute.setPreviousSibling(attrImpl);
        attrImpl.setParent(this);
        firstAttribute = attrImpl;
    }

    public void removeAttribute(OMAttribute attr) {
        attr.detach();
    }

    /**
     * This will force the parser to proceed, if parser has not yet finished with the XML input
     */
    public void buildNext() {
        builder.next();
    }

    public OMNodeImpl getFirstChild() {
        if (firstChild == null && !done)
            buildNext();
        return firstChild;
    }


    public void setFirstChild(OMNodeImpl firstChild) {
        this.firstChild = firstChild;
    }

    public void print(PrintStream s) throws OMException {
        s.print('<');
        super.print(s);

        OMNodeImpl node = firstAttribute;
        while (node != null) {
            s.print(" ");
            node.print(s);
            node = (OMNodeImpl) node.getNextSibling();
        }

        node = firstNamespace;
        while (node != null) {
            s.print(" ");
            node.print(s);
            node = (OMNodeImpl) node.getNextSibling();
        }

        node = getFirstChild();
        if (node != null) {
            s.print('>');
            while (node != null) {
                node.print(s);
                node = (OMNodeImpl) node.getNextSibling();
            }
            s.print('<');
            s.print('/');
            super.print(s);
        } else
            s.print('/');
        s.print('>');
    }

    /**
     * This will remove this information item and its children, from the model completely
     *
     * @throws org.apache.axis.om.OMException
     */
    public void detach() throws OMException {
        if (done)
            super.detach();
        else
            builder.discard(this);
    }

    public void setFirstAttribute(OMAttributeImpl firstAttribute) {
        this.firstAttribute = firstAttribute;
    }


    public boolean isComplete() {
        return done;
    }

    /**
     * This will return the literal value of the node.
     * OMText --> the text
     * OMElement --> local name of the element in String format
     * OMAttribute --> the value of the attribue
     *
     * @return
     * @throws org.apache.axis.om.OMException
     */
    public String getValue() throws OMException {
        return localName;
    }

    /**
     * This is to get the type of node, as this is the super class of all the nodes
     *
     * @return
     * @throws org.apache.axis.om.OMException
     */
    public short getType() throws OMException {
        return OMNode.ELEMENT_NODE;    
    }
}
