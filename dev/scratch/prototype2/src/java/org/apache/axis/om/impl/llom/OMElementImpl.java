package org.apache.axis.om.impl.llom;

import java.util.HashMap;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axis.om.OMAttribute;
import org.apache.axis.om.OMConstants;
import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMException;
import org.apache.axis.om.OMFactory;
import org.apache.axis.om.OMNamespace;
import org.apache.axis.om.OMNode;
import org.apache.axis.om.OMText;
import org.apache.axis.om.OMXMLParserWrapper;
import org.apache.axis.om.impl.llom.serialize.StreamWriterToContentHandlerConverter;
import org.apache.axis.om.impl.llom.serialize.StreamingOMSerializer;
import org.apache.axis.om.impl.llom.traverse.OMChildrenIterator;
import org.apache.axis.om.impl.llom.traverse.OMChildrenQNameIterator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
public class OMElementImpl
        extends OMNamedNodeImpl
        implements OMElement, OMConstants {
    protected OMNode firstChild;
    protected OMXMLParserWrapper builder;

    // (prefix,uri) one element "can" have same nsURI with different prefixes
    private HashMap namespaces = null;

    private HashMap attributes = null;
    private Log log = LogFactory.getLog(getClass());

    private int noPrefixNamespaceCounter = 0;

    public OMElementImpl(String localName,
                         OMNamespace ns,
                         OMElement parent,
                         OMXMLParserWrapper builder) {
        super(localName, null, parent);
        if (ns != null) {
            setNamespace(handleNamespace(ns));
        }
        this.builder = builder;

        firstChild = null;
    }

    /**
     * @param localName
     * @param ns
     * @param parent
     */
    protected OMElementImpl(OMElement parent) {
        super(parent);
        this.done = true;
    }

    public OMElementImpl(String localName, OMNamespace ns) {
        super(localName, null, null);
        this.done = true;
        if (ns != null) {
            setNamespace(handleNamespace(ns));
        }

    }

    /**
     * Here it is assumed that this QName passed, at least contains the localName for this element
     *
     * @param qname
     */
    public OMElementImpl(QName qname, OMElement parent) throws OMException {
        super(qname.getLocalPart(), null, parent);
        this.done = true;
        handleNamespace(qname, parent);

    }

    private void handleNamespace(QName qname, OMElement parent) {
        OMNamespace ns;

        // first try to find a namespace from the scope
        String namespaceURI = qname.getNamespaceURI();
        if (!"".equals(namespaceURI) ) {
            ns = findInScopeNamespace(qname.getNamespaceURI(), qname.getPrefix());
        } else {
            if (parent != null) {
                ns = parent.getNamespace();
            } else {
                throw new OMException("Element can not be declared without a namespaceURI. Every Element should be namespace qualified");
            }
        }

        /**
         * What is left now is
         *  1. nsURI = null & parent != null, but ns = null
         *  2. nsURI != null, (parent doesn't have an ns with given URI), but ns = null
         */

        if (ns == null && !"".equals(namespaceURI)) {
            String prefix = qname.getPrefix();
            if (!"".equals(prefix)) {
                ns = declareNamespace(namespaceURI, prefix);
            } else {
                ns = declareNamespace(namespaceURI, getNextNamespacePrefix());
            }
        }

        if (ns == null) {
            throw new OMException("Element can not be declared without a namespaceURI. Every Element should be namespace qualified");
        }

        this.setNamespace(ns);
    }

    private OMNamespace handleNamespace(OMNamespace ns) {

        OMNamespace namespace =
                findInScopeNamespace(ns.getName(), ns.getPrefix());
        if (namespace == null) {
            namespace = declareNamespace(ns);
        }

        return namespace;
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
    public Iterator getChildrenWithName(QName elementQName)
            throws OMException {
        return new OMChildrenQNameIterator((OMNodeImpl) getFirstChild(),
                elementQName);
    }

    public OMNode getChildWithName(QName elementQName) throws OMException {
        OMChildrenQNameIterator omChildrenQNameIterator =
                new OMChildrenQNameIterator((OMNodeImpl) getFirstChild(),
                        elementQName);
        OMNode omNode = null;
        if (omChildrenQNameIterator.hasNext()) {
            omNode = (OMNode) omChildrenQNameIterator.next();
        }

        return omNode;
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
        OMNamespaceImpl ns = new OMNamespaceImpl(uri, prefix);
        return declareNamespace(ns);
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
            this.namespaces = new HashMap(5);
        }
        namespaces.put(namespace.getPrefix(), namespace);
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
    public OMNamespace findInScopeNamespace(String uri, String prefix)
            throws OMException {

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
    public OMNamespace findDeclaredNamespace(String uri, String prefix)
            throws OMException {
        if (namespaces == null) {
            return null;
        }
        if (prefix == null) {
            Iterator namespaceListIterator = namespaces.values().iterator();
            while (namespaceListIterator.hasNext()) {
                OMNamespace omNamespace =
                        (OMNamespace) namespaceListIterator.next();
                if (omNamespace.getName().equals(uri)) {
                    return omNamespace;
                }
            }
            return null;
        } else {
            return (OMNamespace) namespaces.get(prefix);
        }
    }

    public Iterator getAllDeclaredNamespaces() {
        if (namespaces == null) {
            return null;
        }
        return namespaces.values().iterator();
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
        return (OMAttribute) attributes.get(qname);
    }

    /**
     * This will return a List of OMAttributes
     *
     * @return
     */
    public Iterator getAttributes() {
        if (attributes == null) {
            return null;
        }
        return attributes.values().iterator();
    }

    /**
     * This will insert attribute to this element. Implementor can decide as to insert this
     * in the front or at the end of set of attributes
     *
     * @param attr
     */
    public OMAttribute insertAttribute(OMAttribute attr) {
        if (attributes == null) {
            this.attributes = new HashMap(5);
        }

        attributes.put(attr.getQName(), attr);

        return attr;
    }

    public void removeAttribute(OMAttribute attr) {
        if (attributes != null) {
            attributes.remove(attr.getQName());
        }
    }

    public OMAttribute insertAttribute(String attributeName,
                                       String value,
                                       OMNamespace ns) {
        OMNamespace namespace = null;
        if (ns != null) {
            namespace = findInScopeNamespace(ns.getName(), ns.getPrefix());
            if (namespace == null) {
                throw new OMException("Given OMNamespace("
                        + ns.getName()
                        + ns.getPrefix()
                        + ") for "
                        + "this attribute is not declared in the scope of this element. First declare the namespace"
                        + " and then use it with the attribute");
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
        while (firstChild == null && !done)
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

    public void serialize(XMLStreamWriter writer, boolean cache)
            throws XMLStreamException {
        boolean firstElement = false;

        short builderType = PULL_TYPE_BUILDER; //default is pull type
        if (builder != null)
            builderType = this.builder.getBuilderType();
        if (builderType == PUSH_TYPE_BUILDER
                && builder.getRegisteredContentHandler() == null) {
            builder.registerExternalContentHandler(new StreamWriterToContentHandlerConverter(writer));
            //for now only SAX
        }

        //Special case for the pull type building with cache off
        //The getPullParser method returns the current elements itself.
        if (!cache) {
            if (firstChild == null
                    && nextSibling == null
                    && !isComplete()
                    && builderType == PULL_TYPE_BUILDER) {
                StreamingOMSerializer streamingOMSerializer =
                        new StreamingOMSerializer();
                streamingOMSerializer.serialize(this.getPullParser(!cache),
                        writer);
                return;
            }
        }

        if (!cache) {
            if (isComplete()) {
                //serialize own normally
                serializeNormal(writer, cache);

                if (nextSibling != null) {
                    //serilize next sibling
                    nextSibling.serialize(writer, cache);
                } else {
                    if (parent == null) {
                        return;
                    } else if (parent.isComplete()) {
                        return;
                    } else {
                        //do the special serialization
                        //Only the push serializer is left now
                        builder.setCache(cache);
                        builder.next();
                    }

                }
            } else if (firstChild != null) {
                serializeStartpart(writer);
                log.info("Serializing the Element from " + localName + " the generated OM tree");
                firstChild.serialize(writer, cache);
                serializeEndpart(writer);
            } else {
                //do the special serilization
                //Only the push serializer is left now
                builder.setCache(cache);
                serializeStartpart(writer);
                builder.next();
                serializeEndpart(writer);
            }

        } else {
            //serialize own normally
            serializeNormal(writer, cache);
            //serialize the siblings if this is not the first element
            if (!firstElement) {
                OMNode nextSibling = this.getNextSibling();
                if (nextSibling != null) {
                    nextSibling.serialize(writer, cache);
                }
            }
        }

    }

    private void serializeStartpart(XMLStreamWriter writer)
            throws XMLStreamException {

        String nameSpaceName = null;
        String writer_prefix = null;
        String prefix = null;

        if (ns != null) {
            nameSpaceName = ns.getName();
            writer_prefix = writer.getPrefix(nameSpaceName);
            prefix = ns.getPrefix();
            if (nameSpaceName != null) {
                if (writer_prefix != null) {
                    writer.writeStartElement(nameSpaceName,
                            this.getLocalName());
                } else {
                    if (prefix != null) {
                        writer.writeStartElement(prefix,
                                this.getLocalName(),
                                nameSpaceName);
                        writer.writeNamespace(prefix, nameSpaceName);
                        writer.setPrefix(prefix, nameSpaceName);
                    } else {
                        writer.writeStartElement(nameSpaceName,
                                this.getLocalName());
                        writer.writeDefaultNamespace(nameSpaceName);
                        writer.setDefaultNamespace(nameSpaceName);
                    }
                }

            } else {
                throw new OMException("Non namespace qualified elements are not allowed");

            }

        } else {
            throw new OMException("Non namespace qualified elements are not allowed");
        }

        //add the elements attributes
        if (attributes != null) {
            Iterator attributesList = attributes.values().iterator();
            while (attributesList.hasNext()) {
                serializeAttribute((OMAttribute) attributesList.next(), writer);
            }
        }

        //add the namespaces
        Iterator namespaces = this.getAllDeclaredNamespaces();
        if (namespaces != null) {
            while (namespaces.hasNext()) {
                serializeNamespace((OMNamespace) namespaces.next(), writer);
            }
        }
    }

    private void serializeEndpart(XMLStreamWriter writer)
            throws XMLStreamException {
        writer.writeEndElement();
    }

    private void serializeNormal(XMLStreamWriter writer, boolean cache)
            throws XMLStreamException {
        serializeStartpart(writer);

        OMNode firstChild = getFirstChild();
        if (firstChild != null) {
            firstChild.serialize(writer, cache);
        }

        serializeEndpart(writer);

    }

    protected void serializeAttribute(OMAttribute attr, XMLStreamWriter writer)
            throws XMLStreamException {
        //first check whether the attribute is associated with a namespace
        OMNamespace ns = attr.getNamespace();
        String prefix = null;
        String namespaceName = null;
        if (ns != null) {
            //add the prefix if it's availble
            prefix = ns.getPrefix();
            namespaceName = ns.getName();
            if (prefix != null)
                writer.writeAttribute(prefix,
                        namespaceName,
                        attr.getLocalName(),
                        attr.getValue());
            else
                writer.writeAttribute(namespaceName,
                        attr.getLocalName(),
                        attr.getValue());
        } else {
            writer.writeAttribute(attr.getLocalName(), attr.getValue());
        }
    }

    protected void serializeNamespace(OMNamespace namespace,
                                      XMLStreamWriter writer)
            throws XMLStreamException {

        if (namespace != null) {
            String uri = namespace.getName();
            String prefix = writer.getPrefix(uri);
            String ns_prefix = namespace.getPrefix();
            if (prefix == null) {
                writer.writeNamespace(ns_prefix, namespace.getName());
                writer.setPrefix(ns_prefix, uri);
            }
        }
    }

    private String getNextNamespacePrefix() {
        return "ns" + ++noPrefixNamespaceCounter;
    }

}
