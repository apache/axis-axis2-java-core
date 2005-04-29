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
package org.apache.axis.om.impl.llom;

import org.apache.axis.om.*;
import org.apache.axis.om.impl.llom.serialize.StreamWriterToContentHandlerConverter;
import org.apache.axis.om.impl.llom.serialize.StreamingOMSerializer;
import org.apache.axis.om.impl.llom.traverse.OMChildrenIterator;
import org.apache.axis.om.impl.llom.traverse.OMChildrenQNameIterator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Class OMElementImpl
 */
public class OMElementImpl extends OMNodeImpl
        implements OMElement, OMConstants {

    protected OMNamespace ns;

    /**
     * Field localName
     */
    protected String localName;
    /**
     * Field firstChild
     */
    protected OMNode firstChild;



    /**
     * Field namespaces
     */
    private HashMap namespaces = null;

    /**
     * Field attributes
     */
    private HashMap attributes = null;

    /**
     * Field log
     */
    private Log log = LogFactory.getLog(getClass());

    /**
     * Field noPrefixNamespaceCounter
     */
    private int noPrefixNamespaceCounter = 0;

    /**
     * Constructor OMElementImpl
     *
     * @param localName
     * @param ns
     * @param parent
     * @param builder
     */
    public OMElementImpl(String localName, OMNamespace ns, OMElement parent,
                         OMXMLParserWrapper builder) {
        super(parent);
        this.localName = localName;
        if (ns != null) {
            setNamespace(handleNamespace(ns));
        }
        this.builder = builder;
        firstChild = null;
    }

    /**
     * @param parent
     * @param parent
     */
    protected OMElementImpl(OMElement parent) {
        super(parent);
        this.done = true;
    }

    /**
     * Constructor OMElementImpl
     *
     * @param localName
     * @param ns
     */
    public OMElementImpl(String localName, OMNamespace ns) {
        super(null);
        this.localName = localName;
        this.done = true;
        if (ns != null) {
            setNamespace(handleNamespace(ns));
        }
    }

    /**
     * Here it is assumed that this QName passed, at least contains the localName for this element
     *
     * @param qname
     * @param parent
     * @throws OMException
     */
    public OMElementImpl(QName qname, OMElement parent) throws OMException {
        super(parent);
        this.localName = qname.getLocalPart();
        this.done = true;
        handleNamespace(qname, parent);
    }

    /**
     * Method handleNamespace
     *
     * @param qname
     * @param parent
     */
    private void handleNamespace(QName qname, OMElement parent) {
        OMNamespace ns;

        // first try to find a namespace from the scope
        String namespaceURI = qname.getNamespaceURI();
        if (!"".equals(namespaceURI)) {
            ns = findNamespace(qname.getNamespaceURI(),
                    qname.getPrefix());
        } else {
            if (parent != null) {
                ns = parent.getNamespace();
            } else {
                throw new OMException(
                        "Element can not be declared without a namespaceURI. Every Element should be namespace qualified");
            }
        }

        /**
         * What is left now is
         *  1. nsURI = null & parent != null, but ns = null
         *  2. nsURI != null, (parent doesn't have an ns with given URI), but ns = null
         */
        if ((ns == null) && !"".equals(namespaceURI)) {
            String prefix = qname.getPrefix();
            if (!"".equals(prefix)) {
                ns = declareNamespace(namespaceURI, prefix);
            } else {
                ns = declareNamespace(namespaceURI, getNextNamespacePrefix());
            }
        }
        if (ns == null) {
            throw new OMException(
                    "Element can not be declared without a namespaceURI. Every Element should be namespace qualified");
        }
        this.setNamespace(ns);
    }

    /**
     * Method handleNamespace
     *
     * @param ns
     * @return
     */
    private OMNamespace handleNamespace(OMNamespace ns) {
        OMNamespace namespace = findNamespace(ns.getName(),
                ns.getPrefix());
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
     * @throws OMException
     */
    public Iterator getChildrenWithName(QName elementQName) throws OMException {
        return new OMChildrenQNameIterator((OMNodeImpl) getFirstChild(),
                elementQName);
    }

    /**
     * Method getFirstChildWithName
     *
     * @param elementQName
     * @return
     * @throws OMException
     */
    public OMElement getFirstChildWithName(QName elementQName) throws OMException {
        OMChildrenQNameIterator omChildrenQNameIterator =
                new OMChildrenQNameIterator((OMNodeImpl) getFirstChild(),
                        elementQName);
        OMNode omNode = null;
        if (omChildrenQNameIterator.hasNext()) {
            omNode = (OMNode) omChildrenQNameIterator.next();
        }

        return ((omNode != null) && (OMNode.ELEMENT_NODE == omNode.getType())) ? (OMElement) omNode : null;

    }

    /**
     * Method addChild
     *
     * @param child
     */
    private void addChild(OMNodeImpl child) {
        if ((firstChild == null) && !done) {
            builder.next();
        }


        child.setParent(this);
        
        child.setPreviousSibling(null);
        child.setNextSibling(firstChild);
        if (firstChild != null) {
            OMNodeImpl firstChildImpl = (OMNodeImpl) firstChild;
            firstChildImpl.setPreviousSibling(child);
        }
        this.firstChild = child;
    }

    /**
     * This will give the next sibling. This can be an OMAttribute for OMAttribute or OMText or OMELement for others.
     *
     * @return
     * @throws org.apache.axis.om.OMException
     * @throws OMException
     */
    public OMNode getNextSibling() throws OMException {
        while (!done) {
            builder.next();
        }
        return super.getNextSibling();
    }

    /**
     * This returns a collection of this element.
     * Children can be of types OMElement, OMText.
     *
     * @return
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
    public OMNamespace declareNamespace(String uri, String prefix) {
        OMNamespaceImpl ns = new OMNamespaceImpl(uri, prefix);
        return declareNamespace(ns);
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
     * @throws OMException
     */
    public OMNamespace findNamespace(String uri, String prefix)
            throws OMException {

        // check in the current element
        OMNamespace namespace = findDeclaredNamespace(uri, prefix);
        if (namespace != null) {
            return namespace;
        }

        // go up to check with ancestors
        if (parent != null) {
            return parent.findNamespace(uri, prefix);
        }
        return null;
    }

    /**
     * This will ckeck for the namespace <B>only</B> in the current Element
     * This can also be used to retrieve the prefix of a known namespace URI
     *
     * @param uri
     * @param prefix
     * @return
     * @throws OMException
     */
    private OMNamespace findDeclaredNamespace(String uri, String prefix)
            throws OMException {
        if (namespaces == null) {
            return null;
        }
        if (prefix == null || "".equals(prefix)) {
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

    /**
     * Method getAllDeclaredNamespaces
     *
     * @return
     */
    public Iterator getAllDeclaredNamespaces() {
        if (namespaces == null) {
            return null;
        }
        return namespaces.values().iterator();
    }

    /**
     * This will help to search for an attribute with a given QName within this Element
     *
     * @param qname
     * @return
     * @throws org.apache.axis.om.OMException
     * @throws OMException
     */
    public OMAttribute getFirstAttribute(QName qname) throws OMException {
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
            return new Iterator(){

                public void remove() {
                    throw new UnsupportedOperationException();

                }

                public boolean hasNext() {
                    return false;  //To change body of implemented methods use File | Settings | File Templates.
                }

                public Object next() {
                    throw new UnsupportedOperationException();
                }
            };
        }
        return attributes.values().iterator();
    }

    public Iterator getAttributes(QName qname) {
        return null;  //ToDO
    }

    /**
     * This will insert attribute to this element. Implementor can decide as to insert this
     * in the front or at the end of set of attributes
     *
     * @param attr
     * @return
     */
    public OMAttribute addAttribute(OMAttribute attr) {
        if (attributes == null) {
            this.attributes = new HashMap(5);
        }
        attributes.put(attr.getQName(), attr);
        return attr;
    }

    /**
     * Method removeAttribute
     *
     * @param attr
     */
    public void removeAttribute(OMAttribute attr) {
        if (attributes != null) {
            attributes.remove(attr.getQName());
        }
    }

    /**
     * Method addAttribute
     *
     * @param attributeName
     * @param value
     * @param ns
     * @return
     */
    public OMAttribute addAttribute(String attributeName, String value,
                                       OMNamespace ns) {
        OMNamespace namespace = null;
        if (ns != null) {
            namespace = findNamespace(ns.getName(), ns.getPrefix());
            if (namespace == null) {
                throw new OMException(
                        "Given OMNamespace(" + ns.getName() + ns.getPrefix()
                        + ") for "
                        + "this attribute is not declared in the scope of this element. First declare the namespace"
                        + " and then use it with the attribute");
            }
        }
        return addAttribute(new OMAttributeImpl(attributeName, ns, value));
    }

    /**
     * Method setBuilder
     *
     * @param wrapper
     */
    public void setBuilder(OMXMLParserWrapper wrapper) {
        this.builder = wrapper;
    }

    /**
     * Method getBuilder
     *
     * @return
     */
    public OMXMLParserWrapper getBuilder() {
        return builder;
    }

    /**
     * This will force the parser to proceed, if parser has not yet finished with the XML input
     */
    public void buildNext() {
        builder.next();
    }

    /**
     * Method getFirstChild
     *
     * @return
     */
    public OMNode getFirstChild() {
        while ((firstChild == null) && !done) {
            buildNext();
        }
        return firstChild;
    }

    /**
     * Method setFirstChild
     *
     * @param firstChild
     */
    public void setFirstChild(OMNode firstChild) {
        this.firstChild = firstChild;
    }

    /**
     * This will remove this information item and its children, from the model completely
     *
     * @throws org.apache.axis.om.OMException
     * @throws OMException
     */
    public OMNode detach() throws OMException {
        if (!done){
            build();
        }else{
            super.detach();
        }
        return this;
    }

    /**
     * Method isComplete
     *
     * @return
     */
    public boolean isComplete() {
        return done;
    }

    /**
     * This is to get the type of node, as this is the super class of all the nodes
     *
     * @return
     * @throws org.apache.axis.om.OMException
     * @throws OMException
     */
    public int getType() throws OMException {
        return OMNode.ELEMENT_NODE;
    }

    /**
     * @see org.apache.axis.om.OMElement#getXMLStreamReader()
     * @return
     */
    public XMLStreamReader getXMLStreamReader() {
        return getXMLStreamReader(true);
    }

    /**
     * @see org.apache.axis.om.OMElement#getXMLStreamReaderWithoutCaching()
     * @return
     */
    public XMLStreamReader getXMLStreamReaderWithoutCaching() {
        return getXMLStreamReader(false);
    }

    /**
     * @param cache
     * @return
     */
    private XMLStreamReader getXMLStreamReader(boolean cache) {
        if ((builder == null) && !cache) {
            throw new UnsupportedOperationException(
                    "This element was not created in a manner to be switched");
        }
        return new OMStAXWrapper(builder, this, cache);
    }

    /**
     * Sets the text of the given element.
     * caution - This method will wipe out all the text elements (and hence any
     * moxed content) before setting the text
     * @param text
     */
    public void setText(String text) {
        
        OMNode child = this.getFirstChild();
        while(child != null){
            if(child.getType() == OMNode.TEXT_NODE){
                child.detach();
            }
            child = child.getNextSibling();
        }

        this.addChild(OMAbstractFactory.getOMFactory().createText(this,text));
    }

    /**
     * select all the text children and concat them to a single string
     * @return
     */
    public String getText() {
        String childText = "";
        OMNode child = this.getFirstChild();
        OMText textNode = null;

        while(child != null){
            if(child.getType() == OMNode.TEXT_NODE){
                textNode = (OMText)child;
                if( textNode.getText() != null && !"".equals(textNode.getText().trim())){
                    childText += textNode.getText().trim();
                }
            }
            child = child.getNextSibling();
        }

        return childText;
    }

    /**
     * Method serializeWithCache
     *
     * @param writer
     * @throws XMLStreamException
     */
    public void serializeWithCache(XMLStreamWriter writer)  throws XMLStreamException {
        serialize(writer,true);
    }
    private void serialize(XMLStreamWriter writer,boolean cache)throws XMLStreamException {

        boolean firstElement = false;
        short builderType = PULL_TYPE_BUILDER;    // default is pull type
        if (builder != null) {
            builderType = this.builder.getBuilderType();
        }
        if ((builderType == PUSH_TYPE_BUILDER)
                && (builder.getRegisteredContentHandler() == null)) {
            builder.registerExternalContentHandler(
                    new StreamWriterToContentHandlerConverter(writer));

            // for now only SAX
        }

        // Special case for the pull type building with cache off
        // The getXMLStreamReader method returns the current elements itself.
        if (!cache) {
            if ((firstChild == null) && (nextSibling == null) && !isComplete()
                    && (builderType == PULL_TYPE_BUILDER)) {
                StreamingOMSerializer streamingOMSerializer =
                        new StreamingOMSerializer();
                streamingOMSerializer.serialize(this.getXMLStreamReader(!cache),
                        writer);
                return;
            }
        }
        if (!cache) {
            if (isComplete()) {

                // serializeWithCache own normally
                serializeNormal(writer, cache);
                if (nextSibling != null) {

                    // serilize next sibling
                    nextSibling.serialize(writer);
                } else {
                    if (parent == null) {
                        return;
                    } else if (parent.isComplete()) {
                        return;
                    } else {

                        // do the special serialization
                        // Only the push serializer is left now
                        builder.setCache(cache);
                        builder.next();
                    }
                }
            } else if (firstChild != null) {
                serializeStartpart(writer);
                log.info("Serializing the Element from " + localName
                        + " the generated OM tree");
                firstChild.serialize(writer);
                serializeEndpart(writer);
            } else {

                // do the special serilization
                // Only the push serializer is left now
                builder.setCache(cache);
                serializeStartpart(writer);
                builder.next();
                serializeEndpart(writer);
            }
        } else {

            // serializeWithCache own normally
            serializeNormal(writer, cache);

            // serializeWithCache the siblings if this is not the first element
            if (!firstElement) {
                OMNode nextSibling = this.getNextSibling();
                if (nextSibling != null) {
                    nextSibling.serializeWithCache(writer);
                }
            }
        }
    }

    /**
     * This was requested during the second Axis2 summit. When one call this method, this will
     * serialise without building the object structure in the memory. Misuse of this method will
     * cause loss of data.So its adviced to use populateYourSelf() method, before this,
     * if you want to preserve data in the stream.
     * @param writer
     * @throws XMLStreamException
     */
    public void serialize(XMLStreamWriter writer) throws XMLStreamException {
        this. serialize(writer,false);
    }



    /**
     * Method serializeStartpart
     *
     * @param writer
     * @throws XMLStreamException
     */
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
                        writer.writeStartElement(prefix, this.getLocalName(),
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
                writer.writeStartElement(this.getLocalName());
//                throw new OMException(
//                        "Non namespace qualified elements are not allowed");
            }
        } else {
            writer.writeStartElement(this.getLocalName());
//            throw new OMException(
//                    "Non namespace qualified elements are not allowed");
        }

        // add the elements attributes
        if (attributes != null) {
            Iterator attributesList = attributes.values().iterator();
            while (attributesList.hasNext()) {
                serializeAttribute((OMAttribute) attributesList.next(), writer);
            }
        }

        // add the namespaces
        Iterator namespaces = this.getAllDeclaredNamespaces();
        if (namespaces != null) {
            while (namespaces.hasNext()) {
                serializeNamespace((OMNamespace) namespaces.next(), writer);
            }
        }
    }

    /**
     * Method serializeEndpart
     *
     * @param writer
     * @throws XMLStreamException
     */
    private void serializeEndpart(XMLStreamWriter writer)
            throws XMLStreamException {
        writer.writeEndElement();
    }

    /**
     * Method serializeNormal
     *
     * @param writer
     * @param cache
     * @throws XMLStreamException
     */
    private void serializeNormal(XMLStreamWriter writer, boolean cache)
            throws XMLStreamException {
        serializeStartpart(writer);
        OMNode firstChild = getFirstChild();//todo
        if (firstChild != null) {
            if (cache){
                firstChild.serializeWithCache(writer);
            }else{
                firstChild.serialize(writer);
            }
        }
        serializeEndpart(writer);
    }

    /**
     * Method serializeAttribute
     *
     * @param attr
     * @param writer
     * @throws XMLStreamException
     */
    protected void serializeAttribute(OMAttribute attr, XMLStreamWriter writer)
            throws XMLStreamException {

        // first check whether the attribute is associated with a namespace
        OMNamespace ns = attr.getNamespace();
        String prefix = null;
        String namespaceName = null;
        if (ns != null) {

            // add the prefix if it's availble
            prefix = ns.getPrefix();
            namespaceName = ns.getName();
            if (prefix != null) {
                writer.writeAttribute(prefix, namespaceName,
                        attr.getLocalName(), attr.getValue());
            } else {
                writer.writeAttribute(namespaceName, attr.getLocalName(),
                        attr.getValue());
            }
        } else {
            writer.writeAttribute(attr.getLocalName(), attr.getValue());
        }
    }

    /**
     * Method serializeNamespace
     *
     * @param namespace
     * @param writer
     * @throws XMLStreamException
     */
    protected void serializeNamespace(
            OMNamespace namespace, XMLStreamWriter writer)
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

    /**
     * Method getNextNamespacePrefix
     *
     * @return
     */
    private String getNextNamespacePrefix() {
        return "ns" + ++noPrefixNamespaceCounter;
    }

    public OMElement getFirstElement() {
        OMNode node = getFirstChild();
        while(node != null){
            if(node.getType() == OMNode.ELEMENT_NODE){
                return (OMElement)node;
            }else{
                node = node.getNextSibling();
            }
        }
        return null;
    }

//    /* (non-Javadoc)
//    * @see org.apache.axis.om.OMElement#getNextSiblingElement()
//    */
//    public OMElement getNextSiblingElement() throws OMException {
//        OMNode node = getNextSibling();
//        while(node != null){
//            if(node.getType() == OMNode.ELEMENT_NODE){
//                return (OMElement)node;
//            }else{
//                node = node.getNextSibling();
//            }
//        }
//        return null;
//    }


    /**
     * Method getLocalName
     *
     * @return
     */
    public String getLocalName() {
        return localName;
    }

    /**
     * Method setLocalName
     *
     * @param localName
     */
    public void setLocalName(String localName) {
        this.localName = localName;
    }

    /**
     * Method getNamespace
     *
     * @return
     * @throws OMException
     */
    public OMNamespace getNamespace() throws OMException {
        if ((ns == null) && (parent != null)) {
            ns = parent.getNamespace();
        }
        if (ns == null) {
            throw new OMException("all elements in a soap message must be namespace qualified");
        }
        return ns;
    }


    /**
     * @param namespace
     */
    public void setNamespace(OMNamespace namespace) {
        this.ns = namespace;
    }

    /**
     * Method getQName
     *
     * @return
     */
    public QName getQName() {
        QName qName = null;

        if (ns != null) {
            qName = new QName(ns.getName(), localName, ns.getPrefix());
        }else{
            qName = new QName(localName);
        }
        return qName;
    }

    /**
     * Discard implementation for
     * @throws OMException
     */
    public void discard() throws OMException {
        if (done){
            this.detach();
        }else{
            builder.discard(this);
        }
    }
}
