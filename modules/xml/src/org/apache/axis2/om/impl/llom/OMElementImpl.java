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

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class OMElementImpl
 */
public class OMElementImpl extends OMNodeImpl
        implements OMElement, OMConstants, OMContainerEx {

    private Log logger = LogFactory.getLog(getClass());
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
     */
    public OMElementImpl(String localName, OMNamespace ns) {
        this(localName, ns, null);
    }

    /**
     * This is the basic constructor for OMElement. All the other constructors within this class
     * will depend on this.
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
     * Here it is assumed that this QName passed, at least contains the localName for this element
     *
     * @param qname - this should be valid qname according to javax.xml.namespace.QName
     * @throws OMException
     */
    public OMElementImpl(QName qname, OMContainer parent) throws OMException {
        this(qname.getLocalPart(), null, parent);
        this.ns = handleNamespace(qname);
    }

    /**
     * Method handleNamespace
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
     * Method handleNamespace
     *
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
     */
    public void addChild(OMNode child) {
        addChild((OMNodeImpl) child);
    }

    /**
     * This will search for children with a given QName and will return an iterator to traverse through
     * the OMNodes.
     * This QName can contain any combination of prefix, localname and URI
     *
     * @throws org.apache.axis2.om.OMException
     *
     * @throws OMException
     */
    public Iterator getChildrenWithName(QName elementQName) {
        return new OMChildrenQNameIterator(getFirstOMChild(),
                elementQName);
    }

    /**
     * Method getFirstChildWithName
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
     * Method addChild
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
     * This will give the next sibling. This can be an OMAttribute for OMAttribute or OMText or OMELement for others.
     *
     * @throws org.apache.axis2.om.OMException
     *
     * @throws OMException
     */
    public OMNode getNextOMSibling() throws OMException {
        while (!done) {
            int token = builder.next();
            if(token == XMLStreamConstants.END_DOCUMENT) {
                throw new OMException();
            }
        }
        return super.getNextOMSibling();
    }

    /**
     * This returns a collection of this element.
     * Children can be of types OMElement, OMText.
     *
     * @return children
     */
    public Iterator getChildren() {
        return new OMChildrenIterator(getFirstOMChild());
    }

    /**
     * Returns a filtered list of children - just the elements.
     *
     * @return an iterator over the child elements
     */
    public Iterator getChildElements() {
        return new OMChildElementIterator(getFirstElement());
    }

    /**
     * THis will create a namespace in the current element scope
     *
     * @return namespace
     */
    public OMNamespace declareNamespace(String uri, String prefix) {
        OMNamespaceImpl ns = new OMNamespaceImpl(uri, prefix);
        return declareNamespace(ns);
    }

    /**
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

//        if (namespace == null && uri != null && prefix != null) {
//            namespace = declareNamespace(uri, prefix);
//        }
        return namespace;
    }

    public OMNamespace findNamespaceURI(String prefix) {
        OMNamespace ns = (OMNamespace) this.namespaces.get(prefix);
        if (ns == null && this.parent instanceof OMElement) {
            // try with the parent
            ns = ((OMElement) this.parent).findNamespaceURI(prefix);
        }
        return ns;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * This will ckeck for the namespace <B>only</B> in the current Element.
     * <p/>
     * This can also be used to retrieve the prefix of a known namespace URI
     */
    private OMNamespace findDeclaredNamespace(String uri, String prefix) {
        
    	
    	if(uri == null) {
    		return null;
    	}
    	
    	//If the prefix is available and uri is available and its the xml namespace
    	if(prefix != null && prefix.equals(OMConstants.XMLNS_PREFIX) && uri.equals(OMConstants.XMLNS_URI)) {
    		return new OMNamespaceImpl(uri, prefix);
    	}
    	
    	if (namespaces == null) {
            return null;
        }
	        
        if (prefix == null || "".equals(prefix)) {
            Iterator namespaceListIterator = namespaces.values().iterator();
            while (namespaceListIterator.hasNext()) {
                OMNamespace omNamespace =
                        (OMNamespace) namespaceListIterator.next();
                if (omNamespace.getName() != null &&
                        omNamespace.getName().equals(uri)) {
                    return omNamespace;
                }
            }
            return null;
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
     * Method getAllDeclaredNamespaces
     *
     * @return iterator
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
     * This will help to search for an attribute with a given QName within this Element
     */
    public OMAttribute getFirstAttribute(QName qname) {
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
    public Iterator getAllAttributes() {
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
        return attributes == null ? null : (OMAttribute) attributes.get(qname);
    }

    /**
     * Return a named attribute's value, if present.
     *
     * @param qname the qualified name to search for
     * @return a String containing the attribute value, or null
     */
    public String getAttributeValue(QName qname) {
        OMAttribute attr = getAttribute(qname);
        return (attr == null) ? null : attr.getAttributeValue();
    }

    /**
     * This will insert attribute to this element. Implementor can decide as to insert this
     * in the front or at the end of set of attributes
     *
     * @return attribute
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
     * Method removeAttribute
     */
    public void removeAttribute(OMAttribute attr) {
        if (attributes != null) {
            attributes.remove(attr.getQName());
        }
    }

    /**
     * Method addAttribute
     *
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
     * Method getFirstOMChild
     *
     * @return child
     */
    public OMNode getFirstOMChild() {
        while ((firstChild == null) && !done) {
            buildNext();
        }
        return firstChild;
    }

    /**
     * Method setFirstChild
     */
    public void setFirstChild(OMNode firstChild) {
        if (firstChild != null) {
            ((OMNodeEx) firstChild).setParent(this);
        }
        this.firstChild = firstChild;
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
     */
    public int getType() {
        return OMNode.ELEMENT_NODE;
    }

    /**
     * getXMLStreamReader
     *
     * @see org.apache.axis2.om.OMElement#getXMLStreamReader()
     */
    public XMLStreamReader getXMLStreamReader() {
        return getXMLStreamReader(true);
    }

    /**
     * getXMLStreamReaderWithoutCaching
     *
     * @see org.apache.axis2.om.OMElement#getXMLStreamReaderWithoutCaching()
     */
    public XMLStreamReader getXMLStreamReaderWithoutCaching() {
        return getXMLStreamReader(false);
    }

    /**
     * getXMLStreamReader
     *
     * @return reader
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
     * moxed content) before setting the text
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
     * select all the text children and concat them to a single string
     *
     * @return text
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
     * Returns the concatanation of TRIMMED values of all
     * OMText  child nodes of this element
     * This is incuded purely to improve usability
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
     * Method serializeAndConsume
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
                //serializeAndConsume children
                Iterator children = this.getChildren();
                while (children.hasNext()) {
                    //A call to the  Serialize or the serializeAndConsume wont make a difference here
                    ((OMNodeEx) children.next()).serializeAndConsume(omOutput);
                }
                OMSerializerUtil.serializeEndpart(omOutput);
            } else {
                //take the XMLStream reader and feed it to the stream serilizer.
                //todo is this right ?????
                OMSerializerUtil.serializeByPullStream(this, omOutput, cache);
            }


        }
    }

////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * This was requested during the second Axis2 summit. When one call this method, this will
     * serializeAndConsume without building the object structure in the memory. Misuse of this method will
     * cause loss of data.So its advised to use populateYourSelf() method, before this,
     * if you want to preserve data in the stream.
     *
     * @throws XMLStreamException
     */
    public void serializeAndConsume(OMOutputImpl omOutput) throws XMLStreamException {
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
     * Method getLocalName
     *
     * @return local name
     */
    public String getLocalName() {
        return localName;
    }

    /**
     * Method setLocalName
     */
    public void setLocalName(String localName) {
        this.localName = localName;
    }

    /**
     * Method getNamespace
     *
     * @throws OMException
     */
    public OMNamespace getNamespace() throws OMException {
        return ns;
    }

    /**
     * Method setNamespace
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
            // can not throw out an exception here. Can't do anything other than logging
            // and swallowing this :(
            logger.error("Can not serialize OM Element " + this.getLocalName(), e);
        }

        return new String(baos.toByteArray());
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

    /**
     * Turn a prefix:local qname string into a proper QName, evaluating it in the OMElement context
     * unprefixed qnames resolve to the local namespace
     *
     * @param qname prefixed qname string to resolve
     * @return null for any failure to extract a qname.
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
}
