package org.apache.axis.impl.llom;

import org.apache.axis.impl.llom.traverse.OMChildrenIterator;
import org.apache.axis.impl.llom.traverse.OMChildrenQNameIterator;
import org.apache.axis.impl.llom.serialize.StreamingOMSerializer;

import org.apache.axis.impl.llom.util.StreamWriterToContentHandlerConverter;
import org.apache.axis.om.*;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Stack;


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
 */
public class OMElementImpl extends OMNamedNodeImpl implements OMElement, OMConstants {

    private OMNode firstChild;
    protected OMXMLParserWrapper builder;
    private ArrayList namespaces;
    private ArrayList attributes;
    private Stack namespaceStack;

    public OMElementImpl(OMElement parent) {
        super(parent);
        done = true;
    }


    public OMElementImpl(String localName, OMNamespace ns) {
        super(localName, null, null);
        if (ns != null) {
            setNamespace(handleNamespace(ns));
        }
        done = true;
    }

    private OMNamespace handleNamespace(OMNamespace ns) {

        OMNamespace namespace = findInScopeNamespace(ns.getName(), ns.getPrefix());
        if (namespace == null) {
            namespace = declareNamespace(ns);
        }

        return namespace;
    }


    public OMElementImpl(String localName, OMNamespace ns, OMElement parent, OMXMLParserWrapper builder) {
        super(localName, null, parent);
        if (ns != null) {
            setNamespace(handleNamespace(ns));
        }
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
        return new OMChildrenQNameIterator((OMNodeImpl) getFirstChild(), elementQName);
    }

    private void addChild(OMNodeImpl child) {
        if (firstChild == null && !done)
            builder.next();
        child.setPreviousSibling(null);
        child.setNextSibling(firstChild);
        if (firstChild != null) {
            OMNodeImpl firstChildImpl = (OMNodeImpl) firstChild;
            firstChildImpl.setPreviousSibling(child);
        }
        child.setParent(this);
//        child.setComplete(true);
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

    // --------------------- Namespace Methods ------------------------------------------------------------
    /**
     * THis will create a namespace in the current element scope
     *
     * @param uri
     * @param prefix
     * @return
     */
    public OMNamespace declareNamespace(String uri, String prefix) {

        if (namespaces == null) {
            namespaces = new ArrayList(5);
            // the default size of the ArrayList is 10. But I think this is too much as on average number of namespaces is
            // much more than 10. So I selected 5. Hope this is ok as an initial value. -- Eran Chinthaka 13/12/2004

        }
        OMNamespaceImpl ns = new OMNamespaceImpl(uri, prefix);
        namespaces.add(ns);
        return ns;
    }

    //TODO  correct this
    public void setValue(String value) {
        OMText txt = OMFactory.newInstance().createText(value);
        this.addChild(txt);
    }

    /**
     * @param namespace
     * @return
     */
    public OMNamespace declareNamespace(OMNamespace namespace) {
        if (namespaces == null) {
            namespaces = new ArrayList(5);
            // the default size of the ArrayList is 10. But I think this is too much as on average number of namespaces is
            // much more than 10. So I selected 5. Hope this is ok as an initial value. -- Eran Chinthaka 13/12/2004

        }
        namespaces.add(namespace);
        return namespace;
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
    public OMNamespace findInScopeNamespace(String uri, String prefix) throws OMException {

        // check in the current element
        OMNamespace namespace = findDeclaredNamespace(uri, prefix);
        if (namespace != null) {
            return namespace;
        }
        // go up to check with ancestors
        if (parent != null)
            return parent.findInScopeNamespace(uri, prefix);
        return null;
    }

    /**
     * This will ckeck for the namespace <B>only</B> in the current Element
     *
     * @param uri
     * @param prefix
     * @return
     * @throws OMException
     */
    public OMNamespace findDeclaredNamespace(String uri, String prefix) throws OMException {

        if (namespaces == null) {
            return null;
        }
        // check in the current element
        ListIterator namespaceListIterator = namespaces.listIterator();
        if (prefix == null) {
            while (namespaceListIterator.hasNext()) {
                OMNamespace omNamespace = (OMNamespace) namespaceListIterator.next();
                if (omNamespace.getName().equals(uri)) {
                    return omNamespace;
                }
            }
        } else {
            while (namespaceListIterator.hasNext()) {
                OMNamespace omNamespace = (OMNamespace) namespaceListIterator.next();
                if (omNamespace.equals(uri, prefix)) {
                    return omNamespace;
                }
            }

        }
        return null;

    }

    public Iterator getAllDeclaredNamespaces() {
        if (namespaces == null) {

            // no namespace declared in this element.
            // return a null iterator
            // have to look in to this later
            return new Iterator() {
                public void remove() {
                    throw new UnsupportedOperationException();
                }

                public boolean hasNext() {
                    return false;
                }

                public Object next() {
                    throw new UnsupportedOperationException();
                }
            };
        }

        return namespaces.listIterator();
    }

    // ---------------------------------------------------------------------------------------------------------------

    /**
     * This will help to search for an attribute with a given QName within this Element
     *
     * @param qname
     * @return
     * @throws org.apache.axis.om.OMException
     */
    public OMAttribute getAttributeWithQName(QName qname) throws OMException {

        if (attributes == null) {
            return null;
        }

        ListIterator attrIter = attributes.listIterator();
        OMAttribute omAttribute = null;
        while (attrIter.hasNext()) {
            omAttribute = (OMAttribute) attrIter.next();
            if (omAttribute.getQName().equals(qname)) {
                return omAttribute;
            }
        }

        return null;
    }

    /**
     * This will return a List of OMAttributes
     *
     * @return
     */
    public Iterator getAttributes() {
        if (attributes == null) {
            attributes = new ArrayList(1);
        }
        return attributes.listIterator();
    }

    /**
     * This will insert attribute to this element. Implementor can decide as to insert this
     * in the front or at the end of set of attributes
     *
     * @param attr
     */
    public OMAttribute insertAttribute(OMAttribute attr) {
        if (attributes == null) {
            attributes = new ArrayList(5);
        }
        attributes.add(attr);

        return attr;
    }

    public void removeAttribute(OMAttribute attr) {
        if (attributes.indexOf(attr) != -1) {
            attributes.remove(attr);
        }
    }

    public OMAttribute insertAttribute(String attributeName, String value, OMNamespace ns) {
        OMNamespace namespace = null;
        if (ns != null) {
            namespace = findInScopeNamespace(ns.getName(), ns.getPrefix());
            if (namespace == null) {
                throw new OMException("Given OMNamespace(" + ns.getName() + ns.getPrefix() + ") for " +
                        "this attribute is not declared in the scope of this element. First declare the namespace" +
                        " and then use it with the attribute");
            }
        }
        return insertAttribute(new OMAttributeImpl(attributeName, ns, value));
    }

    public void setBuilder(OMXMLParserWrapper wrapper) {
        this.builder = wrapper;
    }

    public OMXMLParserWrapper getBuilder() {
        return builder;
    }

    /**
     * This will force the parser to proceed, if parser has not yet finished with the XML input
     */
    public void buildNext() {
        builder.next();
    }

    public OMNode getFirstChild() {
        if (firstChild == null && !done)
            buildNext();
        return firstChild;
    }


    public void setFirstChild(OMNode firstChild) {
        this.firstChild = firstChild;
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
        throw new UnsupportedOperationException();
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

    /**
     *
     */
    public XMLStreamReader getPullParser(boolean cacheOff) {
        if (builder == null && cacheOff)
            throw new UnsupportedOperationException("This element was not created in a manner to be switched");
        return new OMStAXWrapper(builder, this, cacheOff);
    }


    public void serialize(XMLStreamWriter writer, boolean cache, Stack namespacePrefixStack) throws XMLStreamException {
        boolean firstElement = false;

        if (namespacePrefixStack == null) {
            this.namespaceStack = new Stack();
            firstElement = true;
        } else {
            this.namespaceStack = namespacePrefixStack;
        }

        int namespaceCount = 0;

        short builderType = PULL_TYPE_BUILDER; //default is pull type
        if (builder != null)
            builderType = this.builder.getBuilderType();
        if (builderType == PUSH_TYPE_BUILDER) {
            builder.registerExternalContentHandler(new StreamWriterToContentHandlerConverter(writer)); //for now only SAX
        }

        //Special case for the pull type building with cache off
        //The getPullParser method returns the current elements itself.
        if (!cache) {
            if (firstChild == null && nextSibling == null && !isComplete() && builderType == PULL_TYPE_BUILDER) {
                StreamingOMSerializer streamingOMSerializer = new StreamingOMSerializer();
                streamingOMSerializer.setNamespacePrefixStack(namespaceStack);
                streamingOMSerializer.serialize(this.getPullParser(!cache), writer);
                return;
            }
        }

        if (!cache) {
            if (isComplete()) {
                //serialize own normally
                serializeNormal(writer, cache);

                if (nextSibling != null) {
                    //serilize next sibling
                    nextSibling.serialize(writer, cache, namespaceStack);
                } else {
                    if (parent == null) {
                        return;
                    } else if (parent.isComplete()) {
                        return;
                    } else {
                        //do the special serialization
                        //Only the push serializer is left now
                        builder.next();
                    }


                }
            } else if (firstChild != null) {
                namespaceCount = serializeStartpart(writer);
                firstChild.serialize(writer, cache, namespaceStack);
                serializeEndpart(writer, namespaceCount);
            } else {
                //do the special serilization
                //Only the push serializer is left now
                builder.next();
            }


        } else {
            //serialize own normally
            serializeNormal(writer, cache);
            //serialize the siblings if this is not the first element
            if (!firstElement){
                if (this.getNextSibling() != null) {
                    this.getNextSibling().serialize(writer, cache, namespaceStack);
                }
            }
        }


    }

    private int serializeStartpart(XMLStreamWriter writer) throws XMLStreamException {
        int nsPushCount = 0;
        String prefix = null;
        String nameSpaceName = null;

        if (ns != null) {
            prefix = ns.getPrefix();
            nameSpaceName = ns.getName();
            if (prefix != null) {
                writer.writeStartElement(prefix, this.getLocalName(), nameSpaceName);
                if (serializeNamespace(ns, writer)) nsPushCount++;
            } else {
                writer.writeStartElement(nameSpaceName, this.getLocalName());

            }
        }
        //add the elements attributes
        if (attributes != null) {
            int attCount = attributes.size();
            for (int i = 0; i < attCount; i++) {
                serializeAttribute((OMAttribute) attributes.get(i), writer);
            }
        }
        //add the namespaces
        Iterator namespaces = this.getAllDeclaredNamespaces();
        while (namespaces.hasNext()) {
            if (serializeNamespace((OMNamespace) namespaces.next(), writer)) nsPushCount++;
        }

        return nsPushCount;
    }

    private void serializeEndpart(XMLStreamWriter writer, int namespaceCount) throws XMLStreamException {

        for (int i = 0; i < namespaceCount; i++) {
            namespaceStack.pop();
        }

        writer.writeEndElement();
        writer.flush();
    }

    private void serializeNormal(XMLStreamWriter writer, boolean cache) throws XMLStreamException {

        int namespaceCount = serializeStartpart(writer);

        if (getFirstChild() != null) {
            getFirstChild().serialize(writer, cache, namespaceStack);
        }

        serializeEndpart(writer, namespaceCount);

    }

    protected void serializeAttribute(OMAttribute attr, XMLStreamWriter writer) throws XMLStreamException {
        //first check whether the attribute is associated with a namespace
        OMNamespace ns = attr.getNamespace();
        String prefix = null;
        String namespaceName = null;
        if (ns != null) {
            //add the prefix if it's availble
            prefix = ns.getPrefix();
            namespaceName = ns.getName();
            if (prefix != null)
                writer.writeAttribute(prefix, namespaceName, attr.getLocalName(), attr.getValue());
            else
                writer.writeAttribute(namespaceName, attr.getLocalName(), attr.getValue());
        } else {
            writer.writeAttribute(attr.getLocalName(), attr.getValue());
        }
    }

    protected boolean serializeNamespace(OMNamespace namespace, XMLStreamWriter writer) throws XMLStreamException {
        boolean nsWritten = false;
        if (namespace != null) {
            String prefix = namespace.getPrefix();
            if (!namespaceStack.contains(prefix)) {
                writer.writeNamespace(prefix, namespace.getName());
                namespaceStack.push(prefix);
                nsWritten = true;
            }
        }

        return nsWritten;
    }

}
