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

import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMAttribute;
import org.apache.axis2.om.OMConstants;
import org.apache.axis2.om.OMContainer;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMException;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.om.OMNode;
import org.apache.axis2.om.OMText;
import org.apache.axis2.om.OMXMLParserWrapper;
import org.apache.axis2.om.impl.OMContainerEx;
import org.apache.axis2.om.impl.OMNodeEx;
import org.apache.axis2.om.impl.OMOutputImpl;
import org.apache.axis2.om.impl.llom.builder.StAXOMBuilder;
import org.apache.axis2.om.impl.llom.traverse.OMChildElementIterator;
import org.apache.axis2.om.impl.llom.traverse.OMChildrenIterator;
import org.apache.axis2.om.impl.llom.traverse.OMChildrenQNameIterator;
import org.apache.axis2.om.impl.llom.util.EmptyIterator;
import org.apache.axis2.om.util.ElementHelper;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Class OMElementImpl
 */
public class OMElementImpl extends OMNodeImpl
        implements OMElement, OMConstants, OMContainerEx {
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
    private int lineNumber;

    /**
     * Constructor OMElementImpl.
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
     * Constructor OMElementImpl.
     */
    public OMElementImpl(String localName, OMNamespace ns) {
        this(localName, ns, null);
    }

    /**
     * This is the basic constructor for OMElement. All the other constructors 
     * depends on this.
     *
     * @param localName - this MUST always be not null
     * @param ns        - can be null
     * @param parent    - this should be an OMContainer
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
     * It is assumed that the QName passed contains, at least, the localName for this element.
     *
     * @param qname - this should be valid qname according to javax.xml.namespace.QName
     * @throws OMException
     */
    public OMElementImpl(QName qname, OMContainer parent) throws OMException {
        this(qname.getLocalPart(), null, parent);
        this.ns = handleNamespace(qname);
    }

    /**
     * Method handleNamespace.
     */
    private OMNamespace handleNamespace(QName qname) {
        OMNamespace ns = null;

        // first try to find a namespace from the scope
        String namespaceURI = qname.getNamespaceURI();
        if (namespaceURI != null && namespaceURI.length() > 0) {
            ns = findNamespace(qname.getNamespaceURI(),
                    qname.getPrefix());

            /**
             * What is left now is
             *  1. nsURI = null & parent != null, but ns = null
             *  2. nsURI != null, (parent doesn't have an ns with given URI), but ns = null
             */
            if (ns == null) {
                String prefix = qname.getPrefix();
                ns = declareNamespace(namespaceURI, prefix);
            }
            if (ns != null) {
                this.ns = (ns);
            }
        }

        else

        {
            // no namespace URI in the given QName. No need to bother about this ??
        }

        return ns;
    }

    /**
     * Method handleNamespace.
     *
     * @return Returns namespace.
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
     * Adds child to the element. One can decide whether to append the child or to add to the
     * front of the children list.
     */
    public void addChild(OMNode child) {
        addChild((OMNodeImpl) child);
    }

    /**
     * Searches for children with a given QName and returns an iterator to traverse through
     * the OMNodes.
     * This QName can contain any combination of prefix, localname and URI.
     *
     * @throws OMException
     */
    public Iterator getChildrenWithName(QName elementQName) {
        return new OMChildrenQNameIterator(getFirstOMChild(),
                elementQName);
    }

    /**
     * Method getFirstChildWithName.
     *
     * @throws OMException
     */
    public OMElement getFirstChildWithName(QName elementQName) throws OMException {
        OMChildrenQNameIterator omChildrenQNameIterator =
                new OMChildrenQNameIterator(getFirstOMChild(),
                        elementQName);
        OMNode omNode = null;
        if (omChildrenQNameIterator.hasNext()) {
            omNode = (OMNode) omChildrenQNameIterator.next();
        }

        return ((omNode != null) && (OMNode.ELEMENT_NODE == omNode.getType())) ?
                (OMElement) omNode : null;

    }

    /**
     * Method addChild.
     */
    private void addChild(OMNodeImpl child) {
        //the order of these statements is VERY important
        //Since setting the parent has a detach method inside
        //it strips down all the rerefences to siblings.
        //setting the siblings should take place AFTER setting the parent

        child.setParent(this);

        if (firstChild == null) {
            firstChild = child;
            child.setPreviousOMSibling(null);
        } else {
            child.setPreviousOMSibling(lastChild);
            ((OMNodeEx) lastChild).setNextOMSibling(child);
        }

        child.setNextOMSibling(null);
        lastChild = child;

    }

    /**
     * Gets the next sibling. This can be an OMAttribute or OMText or 
     * OMELement for others.
     *
     * @throws OMException
     */
    public OMNode getNextOMSibling() throws OMException {
        while (!done) {
            int token = builder.next();
            if (token == XMLStreamConstants.END_DOCUMENT) {
                throw new OMException();
            }
        }
        return super.getNextOMSibling();
    }

    /**
     * Returns a collection of this element. Children can be of types OMElement, OMText.
     *
     * @return Returns children.
     */
    public Iterator getChildren() {
        return new OMChildrenIterator(getFirstOMChild());
    }

    /**
     * Returns a filtered list of children - just the elements.
     *
     * @return Returns an iterator of the child elements.
     */
    public Iterator getChildElements() {
        return new OMChildElementIterator(getFirstElement());
    }

    /**
     * Creates a namespace in the current element scope.
     *
     * @return Returns namespace.
     */
    public OMNamespace declareNamespace(String uri, String prefix) {
        OMNamespaceImpl ns = new OMNamespaceImpl(uri, prefix);
        return declareNamespace(ns);
    }

    /**
     * @return Returns namespace.
     */
    public OMNamespace declareNamespace(OMNamespace namespace) {
        if (namespaces == null) {
            this.namespaces = new HashMap(5);
        }
        namespaces.put(namespace.getPrefix(), namespace);
        return namespace;
    }

    /**
     * Finds a namespace with the given uri and prefix, in the scope of the document.
     * Starts to find from the current element and goes up in the hiararchy until one is found.
     * If none is found, returns null.
     */
    public OMNamespace findNamespace(String uri, String prefix) {

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
                namespace = ((OMElementImpl) parent).findNamespace(uri, prefix);
            }
        }

        return namespace;
    }

    public OMNamespace findNamespaceURI(String prefix) {
        OMNamespace ns = (OMNamespace) this.namespaces.get(prefix);
        if (ns == null && this.parent instanceof OMElement) {
            // try with the parent
            ns = ((OMElement) this.parent).findNamespaceURI(prefix);
        }
        return ns;
    }

    /**
     * Checks for the namespace <B>only</B> in the current Element.
     * This is also used to retrieve the prefix of a known namespace URI.
     */
    private OMNamespace findDeclaredNamespace(String uri, String prefix) {


        if (uri == null) {
            return null;
        }

        //If the prefix is available and uri is available and its the xml namespace
        if (prefix != null && prefix.equals(OMConstants.XMLNS_PREFIX) && uri.equals(OMConstants.XMLNS_URI)) {
            return new OMNamespaceImpl(uri, prefix);
        }

        if (namespaces == null) {
            return null;
        }

        if (prefix == null || "".equals(prefix)) {
            Iterator namespaceListIterator = namespaces.values().iterator();

            OMNamespace ns = null;

            while (namespaceListIterator.hasNext()) {
                OMNamespace omNamespace =
                        (OMNamespace) namespaceListIterator.next();
                if (omNamespace.getName() != null &&
                        omNamespace.getName().equals(uri)) {
                       if(ns == null){
                        ns = omNamespace;
                       }else{
                               if(omNamespace.getPrefix() == null || omNamespace.getPrefix().length() == 0){
                                       ns = omNamespace;
                               }
                       }
                }
            }
            return ns;
        } else {
            OMNamespace namespace = (OMNamespace) namespaces.get(prefix);
            if (namespace != null && uri.equalsIgnoreCase(namespace.getName())) {
                return namespace;
            } else {
                return null;
            }
        }
    }


    /**
     * Method getAllDeclaredNamespaces.
     *
     * @return Returns Iterator.
     */
    public Iterator getAllDeclaredNamespaces() {
        if (namespaces == null) {
            return new Iterator() {
                public void remove() {
                    throw new UnsupportedOperationException();
                }

                public boolean hasNext() {
                    return false;
                }

                public Object next() {
                    return null;
                }
            };
        }
        return namespaces.values().iterator();
    }

    /**
     * Returns a List of OMAttributes.
     *
     * @return Returns iterator.
     */
    public Iterator getAllAttributes() {
        if (attributes == null) {
            return new EmptyIterator();
        }
        return attributes.values().iterator();
    }

    /**
     * Returns a named attribute if present.
     *
     * @param qname the qualified name to search for
     * @return Returns an OMAttribute with the given name if found, or null
     */
    public OMAttribute getAttribute(QName qname) {
        return attributes == null ? null : (OMAttribute) attributes.get(qname);
    }

    /**
     * Returns a named attribute's value, if present.
     *
     * @param qname the qualified name to search for
     * @return Returns a String containing the attribute value, or null.
     */
    public String getAttributeValue(QName qname) {
        OMAttribute attr = getAttribute(qname);
        return (attr == null) ? null : attr.getAttributeValue();
    }

    /**
     * Inserts an attribute to this element. Implementor can decide as to insert this
     * in the front or at the end of set of attributes.
     *
     * @return Returns attribute.
     */
    public OMAttribute addAttribute(OMAttribute attr) {
        if (attributes == null) {
            this.attributes = new HashMap(5);
        }
        OMNamespace namespace = attr.getNamespace();
        if (namespace != null && this.findNamespace(namespace.getName(), namespace.getPrefix()) == null) {
            this.declareNamespace(namespace.getName(), namespace.getPrefix());
        }

        attributes.put(attr.getQName(), attr);
        return attr;
    }

    /**
     * Method removeAttribute.
     */
    public void removeAttribute(OMAttribute attr) {
        if (attributes != null) {
            attributes.remove(attr.getQName());
        }
    }

    /**
     * Method addAttribute.
     *
     * @return Returns OMAttribute.
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
     * Method setBuilder.
     */
    public void setBuilder(OMXMLParserWrapper wrapper) {
        this.builder = wrapper;
    }

    /**
     * Method getBuilder.
     *
     * @return Returns OMXMLParserWrapper.
     */
    public OMXMLParserWrapper getBuilder() {
        return builder;
    }

    /**
     * Forces the parser to proceed, if parser has not yet finished with the XML input.
     */
    public void buildNext() {
        builder.next();
    }

    /**
     * Method getFirstOMChild.
     *
     * @return Returns child.
     */
    public OMNode getFirstOMChild() {
        while ((firstChild == null) && !done) {
            buildNext();
        }
        return firstChild;
    }

    /**
     * Method setFirstChild.
     */
    public void setFirstChild(OMNode firstChild) {
        if (firstChild != null) {
            ((OMNodeEx) firstChild).setParent(this);
        }
        this.firstChild = firstChild;
    }

    /**
     * Removes this information item and its children, from the model completely.
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
     * Method isComplete.
     *
     * @return Returns boolean.
     */
    public boolean isComplete() {
        return done;
    }

    /**
     * Gets the type of node, as this is the super class of all the nodes.
     */
    public int getType() {
        return OMNode.ELEMENT_NODE;
    }

    /**
     * Method getXMLStreamReader.
     *
     * @see OMElement#getXMLStreamReader()
     */
    public XMLStreamReader getXMLStreamReader() {
        return getXMLStreamReader(true);
    }

    /**
     * Method getXMLStreamReaderWithoutCaching.
     *
     * @see OMElement#getXMLStreamReaderWithoutCaching()
     */
    public XMLStreamReader getXMLStreamReaderWithoutCaching() {
        return getXMLStreamReader(false);
    }

    /**
     * Method getXMLStreamReader.
     *
     * @return Returns reader.
     */
    private XMLStreamReader getXMLStreamReader(boolean cache) {
        if ((builder == null) && !cache) {
            throw new UnsupportedOperationException(
                    "This element was not created in a manner to be switched");
        }
        if (builder != null && builder.isCompleted() && !cache) {
            throw new UnsupportedOperationException(
                    "The parser is already consumed!");
        }
        return new OMStAXWrapper(builder, this, cache);
    }

    /**
     * Sets the text of the given element.
     * caution - This method will wipe out all the text elements (and hence any
     * mixed content) before setting the text.
     */
    public void setText(String text) {

        OMNode child = this.getFirstOMChild();
        while (child != null) {
            if (child.getType() == OMNode.TEXT_NODE) {
                child.detach();
            }
            child = child.getNextOMSibling();
        }

        this.addChild(OMAbstractFactory.getOMFactory().createText(this, text));
    }

    /**
     * Selects all the text children and concatinates them to a single string.
     *
     * @return Returns String.
     */
    public String getText() {
        String childText = "";
        OMNode child = this.getFirstOMChild();
        OMText textNode;

        while (child != null) {
            if (child.getType() == OMNode.TEXT_NODE) {
                textNode = (OMText) child;
                if (textNode.getText() != null &&
                        !"".equals(textNode.getText())) {
                    childText += textNode.getText();
                }
            }
            child = child.getNextOMSibling();
        }

        return childText;
    }

    /**
     * Returns the concatination string of TRIMMED values of all
     * OMText  child nodes of this element.
     * This is included purely to improve usability.
     */
    public String getTrimmedText() {
        String childText = "";
        OMNode child = this.getFirstOMChild();
        OMText textNode;

        while (child != null) {
            if (child.getType() == OMNode.TEXT_NODE) {
                textNode = (OMText) child;
                if (textNode.getText() != null &&
                        !"".equals(textNode.getText().trim())) {
                    childText += textNode.getText().trim();
                }
            }
            child = child.getNextOMSibling();
        }

        return childText;
    }

    /**
     * Method serialize.
     *
     * @throws XMLStreamException
     */
    public void serialize(OMOutputImpl omOutput) throws XMLStreamException {
        serialize(omOutput, true);
    }

///////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////

    protected void serialize(OMOutputImpl omOutput, boolean cache) throws XMLStreamException {

        if (cache) {
            //in this case we don't care whether the elements are built or not
            //we just call the serializeAndConsume methods
            OMSerializerUtil.serializeStartpart(this, omOutput);
            //serialize children
            Iterator children = this.getChildren();
            while (children.hasNext()) {
                ((OMNodeEx) children.next()).serialize(omOutput);
            }
            OMSerializerUtil.serializeEndpart(omOutput);

        } else {
            //Now the caching is supposed to be off. However caching been switched off
            //has nothing to do if the element is already built!
            if (this.done) {
                OMSerializerUtil.serializeStartpart(this, omOutput);
                OMNodeImpl child = (OMNodeImpl) firstChild;
                while(child != null && ((!(child instanceof OMElement)) || child.isComplete())) {
                    child.serializeAndConsume(omOutput);
                    child = child.nextSibling;
                }
                if(child != null) {
                    OMElement element = (OMElement) child;
                    element.getBuilder().setCache(false);
                    OMSerializerUtil.serializeByPullStream(element, omOutput, cache);
                }
                OMSerializerUtil.serializeEndpart(omOutput);
            } else {
                OMSerializerUtil.serializeByPullStream(this, omOutput, cache);
            }


        }
    }

////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * This method serializes and consumes without building the object structure in memory. 
     * Misuse of this method will cause loss of data. So it is advised to use 
     * populateYourSelf() method, before calling this method, if one wants to 
     * preserve data in the stream. This was requested during the second Axis2 summit. 
     *
     * @throws XMLStreamException
     */
    public void serializeAndConsume(OMOutputImpl omOutput) throws XMLStreamException {
        this.serialize(omOutput, false);
    }

    /**
     * Gets first element.
     *
     * @return Returns element.
     */
    public OMElement getFirstElement() {
        OMNode node = getFirstOMChild();
        while (node != null) {
            if (node.getType() == OMNode.ELEMENT_NODE) {
                return (OMElement) node;
            } else {
                node = node.getNextOMSibling();
            }
        }
        return null;
    }

    /**
     * Method getLocalName.
     *
     * @return Returns local name.
     */
    public String getLocalName() {
        return localName;
    }

    /**
     * Method setLocalName.
     */
    public void setLocalName(String localName) {
        this.localName = localName;
    }

    /**
     * Method getNamespace.
     *
     * @throws OMException
     */
    public OMNamespace getNamespace() throws OMException {
        return ns;
    }

    /**
     * Method setNamespace.
     */
    public void setNamespace(OMNamespace namespace) {
        OMNamespace nsObject = null;
        if (namespace != null) {
            nsObject = handleNamespace(namespace);
        }
        this.ns = nsObject;
    }

    /**
     * Method getQName.
     *
     * @return Returns QName.
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

    public String toStringWithConsume() throws XMLStreamException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        this.serializeAndConsume(baos);
        return new String(baos.toByteArray());
    }

    public String toString() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            this.serialize(baos);
        } catch (XMLStreamException e) {
            throw new RuntimeException("Can not serialize OM Element " + this.getLocalName(), e);
        }
        return new String(baos.toByteArray());
    }

    /**
     * Method discard.
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

    /**
     * Converts a prefix:local qname string into a proper QName, evaluating it 
     * in the OMElement context. Unprefixed qnames resolve to the local namespace.
     *
     * @param qname prefixed qname string to resolve
     * @return Returns null for any failure to extract a qname.
     */
    public QName resolveQName(String qname) {
        ElementHelper helper = new ElementHelper(this);
        return helper.resolveQName(qname);
    }

    public OMElement cloneOMElement() {
        OMElement clonedElement = new StAXOMBuilder(this.getXMLStreamReader(true)).getDocumentElement();
        clonedElement.build();
        return clonedElement;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public int getLineNumber() {
        return lineNumber;
    }
}
