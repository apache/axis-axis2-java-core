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
package org.apache.axis2.om.impl.dom;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLOutputFactory;

import org.apache.axis2.om.OMContainer;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMException;
import org.apache.axis2.om.OMNode;
import org.apache.axis2.om.OMText;
import org.apache.axis2.om.OMXMLParserWrapper;
import org.apache.axis2.om.OMOutputFormat;
import org.apache.axis2.om.impl.OMNodeEx;
import org.apache.axis2.om.impl.OMOutputImpl;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.UserDataHandler;

import java.io.OutputStream;
import java.io.Writer;

public abstract class NodeImpl implements Node, NodeList,OMNodeEx, Cloneable {


    /**
     * Field builder
     */
    protected OMXMLParserWrapper builder;

    /**
     * Field done
     */
    protected boolean done = false;

    /**
     * Field nodeType
     */
    protected int nodeType;




    protected DocumentImpl ownerNode;

    // data

    protected short flags;
    protected final static short OWNED        = 0x1<<1;
    protected final static short FIRSTCHILD   = 0x1<<2;
    protected final static short READONLY     = 0x1<<3;
    protected final static short SPECIFIED    = 0x1<<4;
    protected final static short NORMALIZED   = 0x1<<5;

    //
    // Constructors
    //

    protected NodeImpl(DocumentImpl ownerDocument) {

        this.ownerNode = ownerDocument;
//        this.isOwned(true);

    }

    protected NodeImpl() {
    }



    public void normalize() {
        /* by default we do not have any children,
             ParentNode overrides this behavior */
    }


    public boolean hasAttributes() {
        return false;           // overridden in ElementImpl
    }


    public boolean hasChildNodes() {
        return false; //Override in ParentNode
    }


    public String getLocalName() {
        return null; //Override in AttrImpl and ElementImpl
    }

    public String getNamespaceURI() {
        return null; //Override in AttrImpl and ElementImpl
    }




    public String getNodeValue() throws DOMException {
        return null;
    }


    /*
      * Overidden in ElementImpl and AttrImpl.
      */
    public String getPrefix() {
        return null;
    }

    public void setNodeValue(String arg0) throws DOMException {
        //Don't do anything, to be overridden in SOME Child classes
    }


    public void setPrefix(String prefix) throws DOMException {
        throw new DOMException(DOMException.NAMESPACE_ERR,
              DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,
                 "NAMESPACE_ERR", null));
    }

    /**
     * Finds the document that this Node belongs to (the document in
     * whose context the Node was created). The Node may or may not
     */
    public Document getOwnerDocument() {
        return (Document) this.ownerNode;
    }

    /**
     * Returns the collection of attributes associated with this node,
     * or null if none. At this writing, Element is the only type of node
     * which will ever have attributes.
     *
     * @see ElementImpl
     */
    public NamedNodeMap getAttributes() {
        return null; // overridden in ElementImpl
    }

    /** Gets the first child of this Node, or null if none.
     * <P>
     * By default we do not have any children, ParentNode overrides this.
     * @see ParentNode
     */
    public Node getFirstChild() {
        return null;
    }


    /** Gets the last child of this Node, or null if none.
     * <P>
     * By default we do not have any children, ParentNode overrides this.
     * @see ParentNode
     */
    public Node getLastChild() {
        return null;
    }

    /** Returns the next child of this node's parent, or null if none. */
    public Node getNextSibling() {
        return null;            // default behavior, overriden in ChildNode
    }


    public Node getParentNode() {
        return null;            // overriden by ChildNode
        //Document, DocumentFragment, and Attribute will never have parents.
    }

    /*
     * Same as getParentNode but returns internal type NodeImpl.
     */
    NodeImpl parentNode() {
        return null;
    }

    /** Returns the previous child of this node's parent, or null if none. */
    public Node getPreviousSibling() {
        return null;            // default behavior, overriden in ChildNode
    }

//    public Node cloneNode(boolean deep) {
//    	if(this instanceof OMElement) {
//    		return (Node)((OMElement)this).cloneOMElement();
//    	} else if(this instanceof OMText ){
//    		return ((TextImpl)this).cloneText();
//    	} else {
//    		throw new UnsupportedOperationException("Only elements can be cloned right now");
//    	}
//    }
//    
    public Node cloneNode(boolean deep) {
        NodeImpl newnode;
        try {
            newnode = (NodeImpl)clone();
        }
        catch (CloneNotSupportedException e) {
            throw new RuntimeException("**Internal Error**" + e);
        }
        newnode.ownerNode      = this.ownerNode;
        newnode.isOwned(false);

        newnode.isReadonly(false);

        return newnode;
    }


    /*
      * (non-Javadoc)
      * 
      * @see org.w3c.dom.Node#getChildNodes()
      */
    public NodeList getChildNodes() {
        return this;
    }

    public boolean isSupported(String feature, String version)
    {
        throw new UnsupportedOperationException();
        //TODO
    }


    /*
      * (non-Javadoc)
      * 
      * @see org.w3c.dom.Node#appendChild(org.w3c.dom.Node)
      */
    public Node appendChild(Node newChild) throws DOMException {
        return insertBefore(newChild, null);
    }

    /*
      * (non-Javadoc)
      * 
      * @see org.w3c.dom.Node#removeChild(org.w3c.dom.Node)
      */
    public Node removeChild(Node oldChild) throws DOMException {
        throw new DOMException(DOMException.NOT_FOUND_ERR, DOMMessageFormatter
                .formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_FOUND_ERR",
                        null));
    }

    /*
      * (non-Javadoc)
      * 
      * @see org.w3c.dom.Node#insertBefore(org.w3c.dom.Node, org.w3c.dom.Node)
      */
    public Node insertBefore(Node newChild, Node refChild) throws DOMException {
        //Overridden in ParentNode
        throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,
                DOMMessageFormatter.formatMessage(
                        DOMMessageFormatter.DOM_DOMAIN,
                        "HIERARCHY_REQUEST_ERR", null));

    }

    /*
      * (non-Javadoc)
      * 
      * @see org.w3c.dom.Node#replaceChild(org.w3c.dom.Node, org.w3c.dom.Node)
      */
    public Node replaceChild(Node newChild, Node oldChild) throws DOMException {
        throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,
                DOMMessageFormatter.formatMessage(
                        DOMMessageFormatter.DOM_DOMAIN,
                        "HIERARCHY_REQUEST_ERR", null));
    }



    //
    // NodeList methods
    //

    /**
     * NodeList method: Returns the number of immediate children of this node.
     * <P>
     * By default we do not have any children, ParentNode overrides this.
     * @see ParentNode
     *
     * @return Returns int.
     */
    public int getLength() {
        return 0;
    }

    /**
     * NodeList method: Returns the Nth immediate child of this node, or
     * null if the index is out of bounds.
     * <P>
     * By default we do not have any children, ParentNode overrides this.
     * @see ParentNode
     *
     * @return Returns org.w3c.dom.Node
     * @param index
     */
    public Node item(int index) {
        return null;
    }




    /*
    * Flags setters and getters
    */


    final boolean isOwned() {
        return (flags & OWNED) != 0;
    }

    final void isOwned(boolean value) {
        flags = (short) (value ? flags | OWNED : flags & ~OWNED);
    }

    final boolean isFirstChild() {
        return (flags & FIRSTCHILD) != 0;
    }

    final void isFirstChild(boolean value) {
        flags = (short) (value ? flags | FIRSTCHILD : flags & ~FIRSTCHILD);
    }

    final boolean isReadonly() {
        return (flags & READONLY) != 0;
    }

    final void isReadonly(boolean value) {
        flags = (short) (value ? flags | READONLY : flags & ~READONLY);
    }

    final boolean isSpecified() {
        return (flags & SPECIFIED) != 0;
    }

    final void isSpecified(boolean value) {
        flags = (short) (value ? flags | SPECIFIED : flags & ~SPECIFIED);
    }

    final boolean isNormalized() {
        return (flags & NORMALIZED) != 0;
    }

    final void isNormalized(boolean value) {
        // See if flag should propagate to parent.
        if (!value && isNormalized() && ownerNode != null) {
            ownerNode.isNormalized(false);
        }
        flags = (short) (value ?  flags | NORMALIZED : flags & ~NORMALIZED);
    }

    ///
    ///OM Methods
    ///

    /* (non-Javadoc)
      * @see org.apache.axis.om.OMNode#getParent()
      */
    public OMContainer getParent() throws OMException {
        return null; // overriden by ChildNode
        //Document, DocumentFragment, and Attribute will never have parents.
    }

    /* (non-Javadoc)
      * @see org.apache.axis.om.OMNode#isComplete()
      */
    public boolean isComplete() {
        return this.done;
    }

    public void setComplete(boolean state) {
        this.done = state;

    }

    /**
     * There no concept of caching in this OM-DOM implementation.
     */
    public void serializeWithCache(OMOutputImpl omOutput) throws XMLStreamException {
        this.serialize(omOutput);
    }

    /* (non-Javadoc)
      * @see org.apache.axis.om.OMNode#insertSiblingAfter(org.apache.axis.om.OMNode)
      */
    public void insertSiblingAfter(OMNode sibling) throws OMException {
        //Overridden in ChildNode
        throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,
                DOMMessageFormatter.formatMessage(
                        DOMMessageFormatter.DOM_DOMAIN,
                        "HIERARCHY_REQUEST_ERR", null));

    }

    /* (non-Javadoc)
      * @see org.apache.axis.om.OMNode#insertSiblingBefore(org.apache.axis.om.OMNode)
      */
    public void insertSiblingBefore(OMNode sibling) throws OMException {
        //Overridden in ChildNode
        throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,
                DOMMessageFormatter.formatMessage(
                        DOMMessageFormatter.DOM_DOMAIN,
                        "HIERARCHY_REQUEST_ERR", null));

    }



    /**
     * Default behavior returns null, overriden in ChildNode.
     */
    public OMNode getPreviousOMSibling() {
        return null;
    }

    /**
     * Default behavior returns null, overriden in ChildNode.
     */
    public OMNode getNextOMSibling() {
        return null;
    }

    public void setPreviousOMSibling(OMNode previousSibling) {
        throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,
                DOMMessageFormatter.formatMessage(
                        DOMMessageFormatter.DOM_DOMAIN,
                        "HIERARCHY_REQUEST_ERR", null));
    }

    public void setNextOMSibling(OMNode previousSibling) {
        throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,
                DOMMessageFormatter.formatMessage(
                        DOMMessageFormatter.DOM_DOMAIN,
                        "HIERARCHY_REQUEST_ERR", null));
    }

    /**
     * Builds next element.
     */
    public void build() {
        while (!done)
            this.builder.next();
    }

    /**
     * Sets the owner document.
     * @param document
     */
    protected void setOwnerDocument(DocumentImpl document) {
        this.ownerNode = document;
        this.isOwned(true);
    }

    public void serialize(XMLStreamWriter xmlWriter) throws XMLStreamException {
        OMOutputImpl omOutput = new OMOutputImpl(xmlWriter);
        serialize(omOutput);
        omOutput.flush();
    }

    public void serializeAndConsume(XMLStreamWriter xmlWriter) throws XMLStreamException {
        OMOutputImpl omOutput = new OMOutputImpl(xmlWriter);
        serializeAndConsume(omOutput);
        omOutput.flush();
    }

    public OMNode detach() {
        throw new OMException(
                    "Elements that doesn't have a parent can not be detached");
    }

    /*
      * DOM-Level 3 methods 
      */

    public String getBaseURI() {
        // TODO TODO
        throw new UnsupportedOperationException("TODO");
    }

    public short compareDocumentPosition(Node arg0) throws DOMException {
        // TODO TODO
        throw new UnsupportedOperationException("TODO");
    }

    public String getTextContent() throws DOMException {
        // TODO TODO
        throw new UnsupportedOperationException("TODO");
    }

    public void setTextContent(String arg0) throws DOMException {
        // TODO TODO
        throw new UnsupportedOperationException("TODO");
    }

    public boolean isSameNode(Node arg0) {
        // TODO TODO
        throw new UnsupportedOperationException("TODO");
    }

    public String lookupPrefix(String arg0) {
        // TODO TODO
        throw new UnsupportedOperationException("TODO");
    }

    public boolean isDefaultNamespace(String arg0) {
        // TODO TODO
        throw new UnsupportedOperationException("TODO");
    }

    public String lookupNamespaceURI(String arg0) {
        // TODO TODO
        throw new UnsupportedOperationException("TODO");
    }

    public boolean isEqualNode(Node arg0) {
        // TODO TODO
        throw new UnsupportedOperationException("TODO");
    }

    public Object getFeature(String arg0, String arg1) {
        // TODO TODO
        throw new UnsupportedOperationException("TODO");
    }

    public Object setUserData(String arg0, Object arg1, UserDataHandler arg2) {
        // TODO TODO
        throw new UnsupportedOperationException("TODO");
    }

    public Object getUserData(String arg0) {
        // TODO TODO
        throw new UnsupportedOperationException("TODO");
    }

    public void serialize(OutputStream output) throws XMLStreamException {
        serialize(XMLOutputFactory.newInstance().createXMLStreamWriter(output));
    }

    public void serialize(Writer writer) throws XMLStreamException {
        serialize(XMLOutputFactory.newInstance().createXMLStreamWriter(writer));
    }

    public void serializeAndConsume(OutputStream output) throws XMLStreamException {
        serializeAndConsume(XMLOutputFactory.newInstance().createXMLStreamWriter(output));
    }

    public void serializeAndConsume(Writer writer) throws XMLStreamException {
        serializeAndConsume(XMLOutputFactory.newInstance().createXMLStreamWriter(writer));
    }

    public void serialize(OutputStream output, OMOutputFormat format) throws XMLStreamException {    
        OMOutputImpl omOutput = new  OMOutputImpl(output, format);
        serialize(omOutput);
        omOutput.flush();
    }

    public void serialize(Writer writer, OMOutputFormat format) throws XMLStreamException {
        OMOutputImpl omOutput = new  OMOutputImpl(XMLOutputFactory.newInstance().createXMLStreamWriter(writer));
        omOutput.setOutputFormat(format);
        serialize(omOutput);
        omOutput.flush();
    }

    public void serializeAndConsume(OutputStream output, OMOutputFormat format) throws XMLStreamException {
        OMOutputImpl omOutput = new  OMOutputImpl(output, format);
        serializeAndConsume(omOutput);
        omOutput.flush();
    }

    public void serializeAndConsume(Writer writer, OMOutputFormat format) throws XMLStreamException {
        OMOutputImpl omOutput = new  OMOutputImpl(XMLOutputFactory.newInstance().createXMLStreamWriter(writer));
        omOutput.setOutputFormat(format);
        serializeAndConsume(omOutput);
        omOutput.flush();
    }
}
