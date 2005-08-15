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
package org.apache.axis2.om.impl.llom;

import org.apache.axis2.om.*;
import org.apache.axis2.om.impl.OMOutputImpl;
import org.apache.axis2.om.impl.llom.serialize.StreamWriterToContentHandlerConverter;
import org.apache.axis2.om.impl.llom.traverse.OMChildrenIterator;
import org.apache.axis2.om.impl.llom.traverse.OMChildrenQNameIterator;
import org.apache.axis2.om.impl.llom.util.EmptyIterator;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Class OMElementImpl
 */
public class OMElementImpl extends OMNodeImpl
        implements OMElement, OMConstants {
    /**
     * Field ns
     */
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
    protected HashMap namespaces = null;

    /**
     * Field attributes
     */
    protected HashMap attributes = null;

    /**
     * Field noPrefixNamespaceCounter
     */
    protected int noPrefixNamespaceCounter = 0;
    private OMNode lastChild;

    /**
     * Constructor OMElementImpl
     *
     * @param localName
     * @param ns
     * @param parent
     * @param builder
     */
    public OMElementImpl(String localName, OMNamespace ns, OMContainer parent,
                         OMXMLParserWrapper builder) {
        super(parent);
        this.localName = localName;
        if (ns != null) {
            setNamespace(ns);
        }
        this.builder = builder;
        firstChild = null;
    }


    /**
     * Constructor OMElementImpl
     *
     * @param localName
     * @param ns
     */
    public OMElementImpl(String localName, OMNamespace ns) {
        this(localName, ns, null);
    }

    /**
     * This is the basic constructor for OMElement. All the other constructors within this class
     * will depend on this.
     *
     * @param localName - this MUST always be not null
     * @param ns - can be null
     * @param parent - this should be an OMContainer
     */
    public OMElementImpl(String localName, OMNamespace ns, OMContainer parent) {
        super(parent);
        if (localName == null || localName.trim().length() == 0) {
            throw new OMException("localname can not be null or empty");
        }
        this.localName = localName;
        this.done = true;
        if (ns != null) {
            setNamespace(ns);
        }
    }

    /**
     * Here it is assumed that this QName passed, at least contains the localName for this element
     *
     * @param qname - this should be valid qname according to javax.xml.namespace.QName
     * @param parent
     * @throws OMException
     */
    public OMElementImpl(QName qname, OMContainer parent) throws OMException {
       this(qname.getLocalPart(), null, parent);
        this.ns = handleNamespace(qname);
    }

    /**
     * Method handleNamespace
     *
     * @param qname
     */
    private OMNamespace handleNamespace(QName qname) {
        OMNamespace ns = null;

        // first try to find a namespace from the scope
        String namespaceURI = qname.getNamespaceURI();
        if (!"".equals(namespaceURI)) {
            ns = findNamespace(qname.getNamespaceURI(),
                    qname.getPrefix());

            /**
             * What is left now is
             *  1. nsURI = null & parent != null, but ns = null
             *  2. nsURI != null, (parent doesn't have an ns with given URI), but ns = null
             */
            if (ns == null) {
                String prefix = qname.getPrefix();
                if (!"".equals(prefix)) {
                    ns = declareNamespace(namespaceURI, prefix);
                } else {
                    ns =
                            declareNamespace(namespaceURI,
                                    getNextNamespacePrefix());
                }
            }
            if (ns != null) {
                this.ns = (ns);
            }
        }else{
           // no namespace URI in the given QName. No need to bother about this ??
        }

        return ns;
    }

    /**
     * Method handleNamespace
     *
     * @param ns
     * @return namespace
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
     * @throws org.apache.axis2.om.OMException
     *
     * @throws OMException
     */
    public Iterator getChildrenWithName(QName elementQName) throws OMException {
        return new OMChildrenQNameIterator(getFirstChild(),
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
                new OMChildrenQNameIterator(getFirstChild(),
                        elementQName);
        OMNode omNode = null;
        if (omChildrenQNameIterator.hasNext()) {
            omNode = (OMNode) omChildrenQNameIterator.next();
        }

        return ((omNode != null) && (OMNode.ELEMENT_NODE == omNode.getType())) ?
                (OMElement) omNode : null;

    }

    /**
     * Method addChild
     *
     * @param child
     */
    private void addChild(OMNodeImpl child) {
        if (firstChild == null) {
            firstChild = child;
            child.setPreviousSibling(null);
        } else {
            child.setPreviousSibling(lastChild);
            lastChild.setNextSibling(child);
        }
        child.setNextSibling(null);
        child.setParent(this);
        lastChild = child;

    }

    /**
     * This will give the next sibling. This can be an OMAttribute for OMAttribute or OMText or OMELement for others.
     *
     * @return
     * @throws org.apache.axis2.om.OMException
     *
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
     * @return children
     */
    public Iterator getChildren() {
        return new OMChildrenIterator(getFirstChild());
    }

    /**
     * Returns a filtered list of children - just the elements.
     *
     * @return an iterator over the child elements
     */
    public Iterator getChildElements() {
        return new OMChildrenIterator(getFirstChild());
    }

    /**
     * THis will create a namespace in the current element scope
     *
     * @param uri
     * @param prefix
     * @return namespace
     */
    public OMNamespace declareNamespace(String uri, String prefix) {
        OMNamespaceImpl ns = new OMNamespaceImpl(uri, prefix);
        return declareNamespace(ns);
    }


    /**
     * @param namespace
     * @return namespace
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
     * @throws org.apache.axis2.om.OMException
     *
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
            //For the OMDocumentImpl there won't be any explicit namespace
            //declarations, so going up the parent chain till the document
            //element should be enough.
            if (parent instanceof OMElement) {
                return ((OMElementImpl) parent).findNamespace(uri, prefix);
            }
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
     * @return iterator
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
     * @throws org.apache.axis2.om.OMException
     *
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
     * @return iterator
     */
    public Iterator getAttributes() {
        if (attributes == null) {
            return new EmptyIterator();
        }
        return attributes.values().iterator();
    }

    /**
     * Return a named attribute if present
     *
     * @param qname the qualified name to search for
     * @return an OMAttribute with the given name if found, or null
     */
    public OMAttribute getAttribute(QName qname) {
        return (OMAttribute)attributes.get(qname);
    }

    /**
     * This will insert attribute to this element. Implementor can decide as to insert this
     * in the front or at the end of set of attributes
     *
     * @param attr
     * @return attribute
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
     * @return attribute
     */
    public OMAttribute addAttribute(String attributeName, String value,
                                    OMNamespace ns) {
        OMNamespace namespace;
        if (ns != null) {
            namespace = findNamespace(ns.getName(), ns.getPrefix());
            if (namespace == null) {
                throw new OMException("Given OMNamespace(" + ns.getName() +
                        ns.getPrefix()
                        + ") for "
                        +
                        "this attribute is not declared in the scope of this element. First declare the namespace"
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
     * @return builder
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
     * @return child
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
        if (firstChild != null) {
            firstChild.setParent(this);
        }
        this.firstChild = firstChild;
//
//        OMNode currentFirstChild = getFirstChild();
//        if (currentFirstChild != null) {
//            currentFirstChild.insertSiblingBefore(firstChild);
//        } else {
//            this.firstChild = firstChild;
//        }
//        if (firstChild != null)
//            firstChild.setParent(this);
    }

    /**
     * This will remove this information item and its children, from the model completely
     *
     * @throws org.apache.axis2.om.OMException
     *
     * @throws OMException
     */
    public OMNode detach() throws OMException {
        if (!done) {
            build();
        } else {
            super.detach();
        }
        return this;
    }

    /**
     * Method isComplete
     *
     * @return boolean
     */
    public boolean isComplete() {
        return done;
    }

    /**
     * This is to get the type of node, as this is the super class of all the nodes
     *
     * @return
     * @throws org.apache.axis2.om.OMException
     *
     * @throws OMException
     */
    public int getType() throws OMException {
        return OMNode.ELEMENT_NODE;
    }

    /**
     * getXMLStreamReader
     *
     * @return
     * @see org.apache.axis2.om.OMElement#getXMLStreamReader()
     */
    public XMLStreamReader getXMLStreamReader() {
        return getXMLStreamReader(true);
    }

    /**
     * getXMLStreamReaderWithoutCaching
     *
     * @return
     * @see org.apache.axis2.om.OMElement#getXMLStreamReaderWithoutCaching()
     */
    public XMLStreamReader getXMLStreamReaderWithoutCaching() {
        return getXMLStreamReader(false);
    }

    /**
     * getXMLStreamReader
     *
     * @param cache
     * @return reader
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
     *
     * @param text
     */
    public void setText(String text) {

        OMNode child = this.getFirstChild();
        while (child != null) {
            if (child.getType() == OMNode.TEXT_NODE) {
                child.detach();
            }
            child = child.getNextSibling();
        }

        this.addChild(OMAbstractFactory.getOMFactory().createText(this, text));
    }

    /**
     * select all the text children and concat them to a single string
     *
     * @return text
     */
    public String getText() {
        String childText = "";
        OMNode child = this.getFirstChild();
        OMText textNode;

        while (child != null) {
            if (child.getType() == OMNode.TEXT_NODE) {
                textNode = (OMText) child;
                if (textNode.getText() != null &&
                        !"".equals(textNode.getText())) {
                    childText += textNode.getText();
                }
            }
            child = child.getNextSibling();
        }

        return childText;
    }
    
    /**
     * Returns the concatanation of TRIMMED values of all 
     * OMText  child nodes of this element
     * This is incuded purely to improve usability
     * @return
     */
    public String getTrimmedText() {
        String childText = "";
        OMNode child = this.getFirstChild();
        OMText textNode;

        while (child != null) {
            if (child.getType() == OMNode.TEXT_NODE) {
                textNode = (OMText) child;
                if (textNode.getText() != null &&
                        !"".equals(textNode.getText().trim())) {
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
     * @param omOutput
     * @throws XMLStreamException
     */
    public void serializeWithCache(OMOutputImpl omOutput) throws XMLStreamException {
        serialize(omOutput, true);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////

    protected void serialize(org.apache.axis2.om.impl.OMOutputImpl omOutput, boolean cache) throws XMLStreamException {

        // select the builder
        short builderType = PULL_TYPE_BUILDER;    // default is pull type
        if (builder != null) {
            builderType = this.builder.getBuilderType();
        }
        if ((builderType == PUSH_TYPE_BUILDER)
                && (builder.getRegisteredContentHandler() == null)) {
            builder.registerExternalContentHandler(
                    new StreamWriterToContentHandlerConverter(omOutput));
        }


        if (!cache) {
            //No caching
            if (this.firstChild != null) {
                OMSerializerUtil.serializeStartpart(this, omOutput);
                firstChild.serialize(omOutput);
                OMSerializerUtil.serializeEndpart(omOutput);
            } else if (!this.done) {
                if (builderType == PULL_TYPE_BUILDER) {
                    OMSerializerUtil.serializeByPullStream(this, omOutput);
                } else {
                    OMSerializerUtil.serializeStartpart(this, omOutput);
                    builder.setCache(cache);
                    builder.next();
                    OMSerializerUtil.serializeEndpart(omOutput);
                }
            } else {
                OMSerializerUtil.serializeNormal(this, omOutput, cache);
            }

            //serilize siblings
            if (this.nextSibling != null) {
                nextSibling.serialize(omOutput);
            } else if (this.parent != null) {
                if (!this.parent.isComplete()) {
                    builder.setCache(cache);
                    builder.next();
                }
            }
        } else {
            //Cached
            OMSerializerUtil.serializeNormal(this, omOutput, cache);
            // serialize the siblings
            OMNode nextSibling = this.getNextSibling();
            if (nextSibling != null) {
                nextSibling.serializeWithCache(omOutput);
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * This was requested during the second Axis2 summit. When one call this method, this will
     * serialize without building the object structure in the memory. Misuse of this method will
     * cause loss of data.So its advised to use populateYourSelf() method, before this,
     * if you want to preserve data in the stream.
     *
     * @param omOutput
     * @throws XMLStreamException
     */
    public void serialize(org.apache.axis2.om.impl.OMOutputImpl omOutput) throws XMLStreamException {
        this.serialize(omOutput, false);
    }


    /**
     * Method getNextNamespacePrefix
     *
     * @return prefix
     */
    private String getNextNamespacePrefix() {
        return "ns" + ++noPrefixNamespaceCounter;
    }

    /**
     * Get first element
     *
     * @return element
     */
    public OMElement getFirstElement() {
        OMNode node = getFirstChild();
        while (node != null) {
            if (node.getType() == OMNode.ELEMENT_NODE) {
                return (OMElement) node;
            } else {
                node = node.getNextSibling();
            }
        }
        return null;
    }

    /**
     * Method getLocalName
     *
     * @return local name
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
        return ns;
    }


    /**
     * Method setNamespace
     *
     * @param namespace
     */
    public void setNamespace(OMNamespace namespace) {
        OMNamespace nsObject = null;
        if (namespace != null) {
            nsObject = handleNamespace(namespace);
        }
        this.ns = nsObject;
    }

    /**
     * Method getQName
     *
     * @return qname
     */
    public QName getQName() {
        QName qName;
        if (ns != null) {
            if (ns.getPrefix() != null) {
                qName = new QName(ns.getName(), localName, ns.getPrefix());
            } else {
                qName = new QName(ns.getName(), localName);
            }
        } else {
            qName = new QName(localName);
        }
        return qName;
    }

    /**
     * Discard implementation
     *
     * @throws OMException
     */
    public void discard() throws OMException {
        if (done) {
            this.detach();
        } else {
            builder.discard(this);
        }
    }
}
