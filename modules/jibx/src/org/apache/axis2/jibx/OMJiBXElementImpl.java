/*
 * Copyright 2006 The Apache Software Foundation.
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

package org.apache.axis2.jibx;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.om.impl.OMContainerEx;
import org.apache.axiom.om.impl.llom.OMElementImpl;
import org.apache.axiom.om.impl.llom.OMNamespaceImpl;
import org.apache.axiom.om.impl.llom.OMNodeImpl;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallable;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IXMLWriter;
import org.jibx.runtime.JiBXException;
import org.jibx.runtime.impl.StAXWriter;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.Iterator;

/**
 * Element wrapping an object to be serialized using JiBX marshalling.
 */
public class OMJiBXElementImpl extends OMNodeImpl implements OMElement, OMContainerEx
{
    /** Bound object for output. */
    private final IMarshallable outObject;
    
    /** Binding factory for creating marshaller. */
    private final IBindingFactory bindingFactory;
    
    /** Delegate tree generated from output (lazy create, only if needed). */
    private OMElementImpl generatedTree;
    
    /**
     * Constructor from object and binding factory.
     * 
     * @param obj
     * @param factory
     */
    public OMJiBXElementImpl(IMarshallable obj, IBindingFactory factory) {
        super(OMAbstractFactory.getOMFactory());
        outObject = obj;
        bindingFactory = factory;
        setComplete(true);
    }

    /**
     * Get tree built from marshalled output. If the tree has not already been
     * built, this forces the construction.
     * 
     * @return tree built from marshalled output
     */
    private OMElementImpl forceTree() {
        if (generatedTree == null) {
            // leave unimplemented for now
            throw new UnsupportedOperationException("Cannot create object model from data binding object");
        }
        return generatedTree;
    }

    /* (non-Javadoc)
     * @see org.apache.axiom.om.OMElement#getChildElements()
     */
    public Iterator getChildElements() {
        return forceTree().getChildElements();
    }

    /* (non-Javadoc)
     * @see org.apache.axiom.om.OMElement#declareNamespace(java.lang.String, java.lang.String)
     */
    public OMNamespace declareNamespace(String uri, String prefix) {
        return forceTree().declareNamespace(uri, prefix);
    }

    public OMNamespace declareDefaultNamespace(String uri) {
        throw new UnsupportedOperationException();  //To change body of implemented methods use File | Settings | File Templates.
    }

    public OMNamespace getDefaultNamespace() {
        throw new UnsupportedOperationException();  //To change body of implemented methods use File | Settings | File Templates.
    }

    /* (non-Javadoc)
     * @see org.apache.axiom.om.OMElement#declareNamespace(org.apache.axiom.om.OMNamespace)
     */
    public OMNamespace declareNamespace(OMNamespace namespace) {
        return forceTree().declareNamespace(namespace);
    }

    /* (non-Javadoc)
     * @see org.apache.axiom.om.OMElement#findNamespace(java.lang.String, java.lang.String)
     */
    public OMNamespace findNamespace(String uri, String prefix) {
        return forceTree().findNamespace(uri, prefix);
    }

    /* (non-Javadoc)
     * @see org.apache.axiom.om.OMElement#findNamespaceURI(java.lang.String)
     */
    public OMNamespace findNamespaceURI(String prefix) {
        return forceTree().findNamespaceURI(prefix);
    }

    /* (non-Javadoc)
     * @see org.apache.axiom.om.OMElement#getAllDeclaredNamespaces()
     */
    public Iterator getAllDeclaredNamespaces() throws OMException {
        return forceTree().getAllDeclaredNamespaces();
    }

    /* (non-Javadoc)
     * @see org.apache.axiom.om.OMElement#getAllAttributes()
     */
    public Iterator getAllAttributes() {
        return forceTree().getAllAttributes();
    }

    /* (non-Javadoc)
     * @see org.apache.axiom.om.OMElement#getAttribute(javax.xml.namespace.QName)
     */
    public OMAttribute getAttribute(QName qname) {
        return forceTree().getAttribute(qname);
    }

    /* (non-Javadoc)
     * @see org.apache.axiom.om.OMElement#getAttributeValue(javax.xml.namespace.QName)
     */
    public String getAttributeValue(QName qname) {
        return forceTree().getAttributeValue(qname);
    }

    /* (non-Javadoc)
     * @see org.apache.axiom.om.OMElement#addAttribute(org.apache.axiom.om.OMAttribute)
     */
    public OMAttribute addAttribute(OMAttribute attr) {
        return forceTree().addAttribute(attr);
    }

    /* (non-Javadoc)
     * @see org.apache.axiom.om.OMElement#addAttribute(java.lang.String, java.lang.String, org.apache.axiom.om.OMNamespace)
     */
    public OMAttribute addAttribute(String attributeName, String value, OMNamespace ns) {
        return forceTree().addAttribute(attributeName, value, ns);
    }

    /* (non-Javadoc)
     * @see org.apache.axiom.om.OMElement#removeAttribute(org.apache.axiom.om.OMAttribute)
     */
    public void removeAttribute(OMAttribute attr) {
        forceTree().removeAttribute(attr);
    }

    /* (non-Javadoc)
     * @see org.apache.axiom.om.OMElement#setBuilder(org.apache.axiom.om.OMXMLParserWrapper)
     */
    public void setBuilder(OMXMLParserWrapper wrapper) {
        throw new UnsupportedOperationException("No builder allowed for element from binding");
    }

    /* (non-Javadoc)
     * @see org.apache.axiom.om.OMElement#getBuilder()
     */
    public OMXMLParserWrapper getBuilder() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.axiom.om.OMElement#setFirstChild(org.apache.axiom.om.OMNode)
     */
    public void setFirstChild(OMNode node) {
        forceTree().setFirstChild(node);
    }

    /* (non-Javadoc)
     * @see org.apache.axiom.om.OMElement#getFirstElement()
     */
    public OMElement getFirstElement() {
        return forceTree().getFirstElement();
    }

    /* (non-Javadoc)
     * @see org.apache.axiom.om.OMElement#getXMLStreamReader()
     */
    public XMLStreamReader getXMLStreamReader() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.axiom.om.OMElement#getXMLStreamReaderWithoutCaching()
     */
    public XMLStreamReader getXMLStreamReaderWithoutCaching() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.axiom.om.OMElement#setText(java.lang.String)
     */
    public void setText(String text) {
        forceTree().setText(text);
    }

    public void setText(QName text) {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.apache.axiom.om.OMElement#getText()
     */
    public String getText() {
        return forceTree().getText();
    }

    public QName getTextAsQName() {
        throw new UnsupportedOperationException();  //To change body of implemented methods use File | Settings | File Templates.
    }

    /* (non-Javadoc)
     * @see org.apache.axiom.om.OMElement#getLocalName()
     */
    public String getLocalName() {
        return bindingFactory.getElementNames()[outObject.JiBX_getIndex()];
    }

    /* (non-Javadoc)
     * @see org.apache.axiom.om.OMElement#setLocalName(java.lang.String)
     */
    public void setLocalName(String localName) {
        throw new UnsupportedOperationException("Cannot set name on element from binding");
    }

    /* (non-Javadoc)
     * @see org.apache.axiom.om.OMElement#getNamespace()
     */
    public OMNamespace getNamespace() throws OMException {
        int index = outObject.JiBX_getIndex();
        return new OMNamespaceImpl(bindingFactory.getElementNamespaces()[index],
            "");
    }

    /* (non-Javadoc)
     * @see org.apache.axiom.om.OMElement#setNamespace(org.apache.axiom.om.OMNamespace)
     */
    public void setNamespace(OMNamespace namespace) {
        throw new UnsupportedOperationException("Cannot set namespace on element from binding");
    }

    public void setNamespaceWithNoFindInCurrentScope(OMNamespace namespace) {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.apache.axiom.om.OMElement#getQName()
     */
    public QName getQName() {
        int index = outObject.JiBX_getIndex();
        return new QName(bindingFactory.getElementNamespaces()[index],
            bindingFactory.getElementNames()[index]);
    }

    /* (non-Javadoc)
     * @see org.apache.axiom.om.OMElement#toStringWithConsume()
     */
    public String toStringWithConsume() throws XMLStreamException {
        StringWriter writer = new StringWriter();
        serializeAndConsume(writer);
        return writer.toString();
    }

    /* (non-Javadoc)
     * @see org.apache.axiom.om.OMElement#resolveQName(java.lang.String)
     */
    public QName resolveQName(String qname) {
        return forceTree().resolveQName(qname);
    }

    /* (non-Javadoc)
     * @see org.apache.axiom.om.OMElement#cloneOMElement()
     */
    public OMElement cloneOMElement() {
        return forceTree().cloneOMElement();
    }

    /* (non-Javadoc)
     * @see org.apache.axiom.om.OMElement#setLineNumber(int)
     */
    public void setLineNumber(int lineNumber) {
        forceTree().setLineNumber(lineNumber);
    }

    /* (non-Javadoc)
     * @see org.apache.axiom.om.OMElement#getLineNumber()
     */
    public int getLineNumber() {
        return forceTree().getLineNumber();
    }

    /* (non-Javadoc)
     * @see org.apache.axiom.om.OMNode#discard()
     */
    public void discard() throws OMException {
        if (generatedTree != null) {
            generatedTree.discard();
        }
    }

    /* (non-Javadoc)
     * @see org.apache.axiom.om.OMNode#getType()
     */
    public int getType() {
        return OMNode.ELEMENT_NODE;
    }

    /* (non-Javadoc)
     */
    public void internalSerialize(javax.xml.stream.XMLStreamWriter writer) throws XMLStreamException {
        forceTree().internalSerialize(writer);
    }


    /* (non-Javadoc)
     * @see org.apache.axiom.om.OMNode#serialize(javax.xml.stream.XMLStreamWriter)
     */
    public void serialize(XMLStreamWriter xmlWriter) throws XMLStreamException {
        try {
            IXMLWriter writer = new StAXWriter(bindingFactory.getNamespaces(),
                xmlWriter);
            IMarshallingContext ctx = bindingFactory.createMarshallingContext();
            ctx.setXmlWriter(writer);
            outObject.marshal(ctx);
        } catch (JiBXException e) {
            throw new XMLStreamException("Error in JiBX marshalling", e);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.axiom.om.OMNode#serialize(java.io.OutputStream)
     */
    public void serialize(OutputStream output) throws XMLStreamException {
        try {
            IMarshallingContext ctx = bindingFactory.createMarshallingContext();
            ctx.setOutput(output, "UTF-8");
            outObject.marshal(ctx);
        } catch (JiBXException e) {
            throw new XMLStreamException("Error in JiBX marshalling", e);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.axiom.om.OMNode#serialize(java.io.Writer)
     */
    public void serialize(Writer writer) throws XMLStreamException {
        try {
            IMarshallingContext ctx = bindingFactory.createMarshallingContext();
            ctx.setOutput(writer);
            outObject.marshal(ctx);
        } catch (JiBXException e) {
            throw new XMLStreamException("Error in JiBX marshalling", e);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.axiom.om.OMNode#serialize(java.io.OutputStream, org.apache.axiom.om.OMOutputFormat)
     */
    public void serialize(OutputStream output, OMOutputFormat format) throws XMLStreamException {
        try {
            IMarshallingContext ctx = bindingFactory.createMarshallingContext();
            ctx.setOutput(output, format.getCharSetEncoding());
            outObject.marshal(ctx);
        } catch (JiBXException e) {
            throw new XMLStreamException("Error in JiBX marshalling", e);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.axiom.om.OMNode#serialize(java.io.Writer, org.apache.axiom.om.OMOutputFormat)
     */
    public void serialize(Writer writer, OMOutputFormat format) throws XMLStreamException {
        try {
            IMarshallingContext ctx = bindingFactory.createMarshallingContext();
            ctx.setOutput(writer);
            outObject.marshal(ctx);
        } catch (JiBXException e) {
            throw new XMLStreamException("Error in JiBX marshalling", e);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.axiom.om.OMNode#serializeAndConsume(javax.xml.stream.XMLStreamWriter)
     */
    public void serializeAndConsume(javax.xml.stream.XMLStreamWriter xmlWriter) throws XMLStreamException {
        try {
            IXMLWriter writer = new StAXWriter(bindingFactory.getNamespaces(),
                xmlWriter);
            IMarshallingContext ctx = bindingFactory.createMarshallingContext();
            ctx.setXmlWriter(writer);
            outObject.marshal(ctx);
        } catch (JiBXException e) {
            throw new XMLStreamException("Error in JiBX marshalling", e);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.axiom.om.OMNode#serializeAndConsume(java.io.OutputStream)
     */
    public void serializeAndConsume(OutputStream output) throws XMLStreamException {
        try {
            IMarshallingContext ctx = bindingFactory.createMarshallingContext();
            ctx.setOutput(output, "UTF-8");
            outObject.marshal(ctx);
        } catch (JiBXException e) {
            throw new XMLStreamException("Error in JiBX marshalling", e);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.axiom.om.OMNode#serializeAndConsume(java.io.Writer)
     */
    public void serializeAndConsume(Writer writer) throws XMLStreamException {
        try {
            IMarshallingContext ctx = bindingFactory.createMarshallingContext();
            ctx.setOutput(writer);
            outObject.marshal(ctx);
        } catch (JiBXException e) {
            throw new XMLStreamException("Error in JiBX marshalling", e);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.axiom.om.OMNode#serializeAndConsume(java.io.OutputStream, org.apache.axiom.om.OMOutputFormat)
     */
    public void serializeAndConsume(OutputStream output, OMOutputFormat format) throws XMLStreamException {
        try {
            IMarshallingContext ctx = bindingFactory.createMarshallingContext();
            ctx.setOutput(output, format.getCharSetEncoding());
            outObject.marshal(ctx);
        } catch (JiBXException e) {
            throw new XMLStreamException("Error in JiBX marshalling", e);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.axiom.om.OMNode#serializeAndConsume(java.io.Writer, org.apache.axiom.om.OMOutputFormat)
     */
    public void serializeAndConsume(Writer writer, OMOutputFormat format) throws XMLStreamException {
        try {
            IMarshallingContext ctx = bindingFactory.createMarshallingContext();
            ctx.setOutput(writer);
            outObject.marshal(ctx);
        } catch (JiBXException e) {
            throw new XMLStreamException("Error in JiBX marshalling", e);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.axiom.om.OMContainer#addChild(org.apache.axiom.om.OMNode)
     */
    public void addChild(OMNode omNode) {
        forceTree().addChild(omNode);
    }

    /* (non-Javadoc)
     * @see org.apache.axiom.om.OMContainer#getChildrenWithName(javax.xml.namespace.QName)
     */
    public Iterator getChildrenWithName(QName elementQName) {
        return forceTree().getChildrenWithName(elementQName);
    }

    /* (non-Javadoc)
     * @see org.apache.axiom.om.OMContainer#getFirstChildWithName(javax.xml.namespace.QName)
     */
    public OMElement getFirstChildWithName(QName elementQName) throws OMException {
        return forceTree().getFirstChildWithName(elementQName);
    }

    /* (non-Javadoc)
     * @see org.apache.axiom.om.OMContainer#getChildren()
     */
    public Iterator getChildren() {
        // the transport code will call this to check for "optimizable" binary,
        //  so this fakes it in order to avoid having to construct a full tree
        //  for a very bad reason
        return Collections.EMPTY_LIST.iterator();
    }

    /* (non-Javadoc)
     * @see org.apache.axiom.om.OMContainer#getFirstOMChild()
     */
    public OMNode getFirstOMChild() {
        return forceTree().getFirstOMChild();
    }

    /* (non-Javadoc)
     * @see org.apache.axiom.om.OMContainer#buildNext()
     */
    public void buildNext() {
        forceTree().buildNext();
    }
}