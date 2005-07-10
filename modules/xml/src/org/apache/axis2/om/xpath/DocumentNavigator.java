package org.apache.axis2.om.xpath;

import org.apache.axis2.om.OMAttribute;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.om.OMNode;
import org.apache.axis2.om.OMText;
import org.apache.axis2.om.OMContainer;
import org.apache.axis2.om.impl.llom.OMDocument;
import org.apache.axis2.om.impl.llom.builder.StAXOMBuilder;
import org.jaxen.BaseXPath;
import org.jaxen.DefaultNavigator;
import org.jaxen.FunctionCallException;
import org.jaxen.JaxenConstants;
import org.jaxen.UnsupportedAxisException;
import org.jaxen.XPath;
import org.jaxen.saxpath.SAXPathException;
import org.jaxen.util.SingleObjectIterator;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.net.URL;
import java.util.Iterator;
import java.io.FileInputStream;

public class DocumentNavigator extends DefaultNavigator {
    /**
     * Returns a parsed form of the given xpath string, which will be suitable
     * for queries on documents that use the same navigator as this one.
     *
     * @param xpath the XPath expression
     * @return a new XPath expression object
     * @throws SAXPathException if the string is not a syntactically
     *                          correct XPath expression
     * @see XPath
     */
    public XPath parseXPath(String xpath) throws SAXPathException {
        return new BaseXPath(xpath, this);
    }

    /**
     * Retrieve the namespace URI of the given element node.
     *
     * @param object the context element node
     * @return the namespace URI of the element node
     */
    public String getElementNamespaceUri(Object object) {
        OMElement attr = (OMElement) object;
        return attr.getQName().getNamespaceURI();
    }

    /**
     * Retrieve the name of the given element node.
     *
     * @param object the context element node
     * @return the name of the element node
     */
    public String getElementName(Object object) {
        OMElement attr = (OMElement) object;
        return attr.getQName().getLocalPart();
    }

    /**
     * Retrieve the QName of the given element node.
     *
     * @param object the context element node
     * @return the QName of the element node
     */
    public String getElementQName(Object object) {
        OMElement attr = (OMElement) object;
        String prefix = null;
        if(attr.getNamespace()!=null) {
            prefix = attr.getNamespace().getPrefix();
        }
        if (prefix == null || "".equals(prefix)) {
            return attr.getQName().getLocalPart();
        }
        return prefix + ":" + attr.getNamespace().getName();
    }

    /**
     * Retrieve the namespace URI of the given attribute node.
     *
     * @param object the context attribute node
     * @return the namespace URI of the attribute node
     */
    public String getAttributeNamespaceUri(Object object) {
        OMAttribute attr = (OMAttribute) object;
        return attr.getQName().getNamespaceURI();
    }

    /**
     * Retrieve the name of the given attribute node.
     *
     * @param object the context attribute node
     * @return the name of the attribute node
     */
    public String getAttributeName(Object object) {
        OMAttribute attr = (OMAttribute) object;
        return attr.getQName().getLocalPart();
    }

    /**
     * Retrieve the QName of the given attribute node.
     *
     * @param object the context attribute node
     * @return the qualified name of the attribute node
     */
    public String getAttributeQName(Object object) {
        OMAttribute attr = (OMAttribute) object;
        String prefix = attr.getNamespace().getPrefix();
        if (prefix == null || "".equals(prefix)) {
            return attr.getQName().getLocalPart();
        }
        return prefix + ":" + attr.getNamespace().getName();
    }

    /**
     * Returns whether the given object is a document node. A document node
     * is the node that is selected by the xpath expression <code>/</code>.
     *
     * @param object the object to test
     * @return <code>true</code> if the object is a document node,
     *         else <code>false</code>
     */
    public boolean isDocument(Object object) {
        return object instanceof OMDocument;
    }

    /**
     * Returns whether the given object is an element node.
     *
     * @param object the object to test
     * @return <code>true</code> if the object is an element node,
     *         else <code>false</code>
     */
    public boolean isElement(Object object) {
        return object instanceof OMElement;
    }

    /**
     * Returns whether the given object is an attribute node.
     *
     * @param object the object to test
     * @return <code>true</code> if the object is an attribute node,
     *         else <code>false</code>
     */
    public boolean isAttribute(Object object) {
        return object instanceof OMAttribute;
    }

    /**
     * Returns whether the given object is a namespace node.
     *
     * @param object the object to test
     * @return <code>true</code> if the object is a namespace node,
     *         else <code>false</code>
     */
    public boolean isNamespace(Object object) {
        return object instanceof OMNamespace;
    }

    /**
     * Returns whether the given object is a comment node.
     *
     * @param object the object to test
     * @return <code>true</code> if the object is a comment node,
     *         else <code>false</code>
     */
    public boolean isComment(Object object) {
        return (object instanceof OMNode) &&
                (((OMText) object).getType() == OMNode.COMMENT_NODE);
    }

    /**
     * Returns whether the given object is a text node.
     *
     * @param object the object to test
     * @return <code>true</code> if the object is a text node,
     *         else <code>false</code>
     */
    public boolean isText(Object object) {
        return (object instanceof OMNode) &&
                (((OMNode) object).getType() == OMNode.TEXT_NODE);
    }

    /**
     * Returns whether the given object is a processing-instruction node.
     *
     * @param object the object to test
     * @return <code>true</code> if the object is a processing-instruction node,
     *         else <code>false</code>
     */
    public boolean isProcessingInstruction(Object object) {
        //TODO: Fix this?
        return false;
    }

    /**
     * Retrieve the string-value of a comment node.
     * This may be the empty string if the comment is empty,
     * but must not be null.
     *
     * @param object the comment node
     * @return the string-value of the node
     */
    public String getCommentStringValue(Object object) {
        return ((OMText) object).getText();
    }

    /**
     * Retrieve the string-value of an element node.
     * This may be the empty string if the element is empty,
     * but must not be null.
     *
     * @param object the comment node.
     * @return the string-value of the node.
     */
    public String getElementStringValue(Object object) {
        if (isElement(object)) {
            return getStringValue((OMElement) object, new StringBuffer()).toString();
        }
        return null;
    }

    private StringBuffer getStringValue(OMNode node, StringBuffer buffer) {
        if (isText(node)) {
            buffer.append(((OMText) node).getText());
        } else if (node instanceof OMElement) {
            Iterator children = ((OMElement) node).getChildren();
            while (children.hasNext()) {
                getStringValue((OMNode) children.next(), buffer);
            }
        }
        return buffer;
    }

    /**
     * Retrieve the string-value of an attribute node.
     * This should be the XML 1.0 normalized attribute value.
     * This may be the empty string but must not be null.
     *
     * @param object the attribute node
     * @return the string-value of the node
     */
    public String getAttributeStringValue(Object object) {
        return ((OMAttribute) object).getValue();
    }

    /**
     * Retrieve the string-value of a namespace node.
     * This is generally the namespace URI.
     * This may be the empty string but must not be null.
     *
     * @param object the namespace node
     * @return the string-value of the node
     */
    public String getNamespaceStringValue(Object object) {
        return ((OMNamespace) object).getName();
    }

    /**
     * Retrieve the string-value of a text node.
     * This must not be null and should not be the empty string.
     * The XPath data model does not allow empty text nodes.
     *
     * @param object the text node
     * @return the string-value of the node
     */
    public String getTextStringValue(Object object) {
        return ((OMText) object).getText();
    }

    /**
     * Retrieve the namespace prefix of a namespace node.
     *
     * @param object the namespace node
     * @return the prefix associated with the node
     */
    public String getNamespacePrefix(Object object) {
        return ((OMNamespace) object).getPrefix();
    }

    /**
     * Retrieve an <code>Iterator</code> matching the <code>child</code>
     * XPath axis.
     *
     * @param contextNode the original context node
     * @return an Iterator capable of traversing the axis, not null
     * @throws UnsupportedAxisException if the semantics of the child axis are
     *                                  not supported by this object model
     */
    public Iterator getChildAxisIterator(Object contextNode) throws UnsupportedAxisException {
        if (contextNode instanceof OMContainer) {
            return ((OMContainer) contextNode).getChildren();
        }
        return JaxenConstants.EMPTY_ITERATOR;
    }

    public Iterator getDescendantAxisIterator(Object object) throws UnsupportedAxisException {
        //TODO: Fix this better?
        return super.getDescendantAxisIterator(object);
    }

    /**
     * Retrieve an <code>Iterator</code> matching the <code>attribute</code>
     * XPath axis.
     *
     * @param contextNode the original context node
     * @return an Iterator capable of traversing the axis, not null
     * @throws UnsupportedAxisException if the semantics of the attribute axis are
     *                                  not supported by this object model
     */
    public Iterator getAttributeAxisIterator(Object contextNode) throws UnsupportedAxisException {
        if (isElement(contextNode)) {
            return ((OMElement) contextNode).getAttributes();
        }
        return JaxenConstants.EMPTY_ITERATOR;
    }

    /**
     * Retrieve an <code>Iterator</code> matching the <code>namespace</code>
     * XPath axis.
     *
     * @param contextNode the original context node
     * @return an Iterator capable of traversing the axis, not null
     * @throws UnsupportedAxisException if the semantics of the namespace axis are
     *                                  not supported by this object model
     */
    public Iterator getNamespaceAxisIterator(Object contextNode) throws UnsupportedAxisException {
        //TODO: Fix this better?
        return super.getNamespaceAxisIterator(contextNode);
    }

    /**
     * Retrieve an <code>Iterator</code> matching the <code>self</code> xpath
     * axis.
     *
     * @param contextNode the original context node
     * @return an Iterator capable of traversing the axis, not null
     * @throws UnsupportedAxisException if the semantics of the self axis are
     *                                  not supported by this object model
     */
    public Iterator getSelfAxisIterator(Object contextNode) throws UnsupportedAxisException {
        //TODO: Fix this better?
        return super.getSelfAxisIterator(contextNode);
    }

    /**
     * Retrieve an <code>Iterator</code> matching the
     * <code>descendant-or-self</code> XPath axis.
     *
     * @param contextNode the original context node
     * @return an Iterator capable of traversing the axis, not null
     * @throws UnsupportedAxisException if the semantics of the descendant-or-self axis are
     *                                  not supported by this object model
     */
    public Iterator getDescendantOrSelfAxisIterator(Object contextNode) throws UnsupportedAxisException {
        //TODO: Fix this better?
        return super.getDescendantOrSelfAxisIterator(contextNode);
    }

    /**
     * Retrieve an <code>Iterator</code> matching the
     * <code>ancestor-or-self</code> XPath axis.
     *
     * @param contextNode the original context node
     * @return an Iterator capable of traversing the axis, not null
     * @throws UnsupportedAxisException if the semantics of the ancestor-or-self axis are
     *                                  not supported by this object model
     */
    public Iterator getAncestorOrSelfAxisIterator(Object contextNode) throws UnsupportedAxisException {
        //TODO: Fix this better?
        return super.getAncestorOrSelfAxisIterator(contextNode);
    }

    /**
     * Retrieve an <code>Iterator</code> matching the <code>parent</code> XPath axis.
     *
     * @param contextNode the original context node
     * @return an Iterator capable of traversing the axis, not null
     * @throws UnsupportedAxisException if the semantics of the parent axis are
     *                                  not supported by this object model
     */
    public Iterator getParentAxisIterator(Object contextNode) throws UnsupportedAxisException {
        if (contextNode instanceof OMNode) {
            return new SingleObjectIterator(((OMNode) contextNode).getParent());
        }
        return JaxenConstants.EMPTY_ITERATOR;
    }

    /**
     * Retrieve an <code>Iterator</code> matching the <code>ancestor</code>
     * XPath axis.
     *
     * @param contextNode the original context node
     * @return an Iterator capable of traversing the axis, not null
     * @throws UnsupportedAxisException if the semantics of the ancestor axis are
     *                                  not supported by this object model
     */
    public Iterator getAncestorAxisIterator(Object contextNode) throws UnsupportedAxisException {
        //TODO: Fix this better?
        return super.getAncestorAxisIterator(contextNode);
    }

    /**
     * Retrieve an <code>Iterator</code> matching the
     * <code>following-sibling</code> XPath axis.
     *
     * @param contextNode the original context node
     * @return an Iterator capable of traversing the axis, not null
     * @throws UnsupportedAxisException if the semantics of the following-sibling axis are
     *                                  not supported by this object model
     */
    public Iterator getFollowingSiblingAxisIterator(Object contextNode) throws UnsupportedAxisException {
        //TODO: Fix this better?
        return super.getFollowingSiblingAxisIterator(contextNode);
    }

    /**
     * Retrieve an <code>Iterator</code> matching the
     * <code>preceding-sibling</code> XPath axis.
     *
     * @param contextNode the original context node
     * @return an Iterator capable of traversing the axis, not null
     * @throws UnsupportedAxisException if the semantics of the preceding-sibling axis are
     *                                  not supported by this object model
     */
    public Iterator getPrecedingSiblingAxisIterator(Object contextNode) throws UnsupportedAxisException {
        //TODO: Fix this better?
        return super.getPrecedingSiblingAxisIterator(contextNode);
    }

    /**
     * Retrieve an <code>Iterator</code> matching the <code>following</code>
     * XPath axis.
     *
     * @param contextNode the original context node
     * @return an Iterator capable of traversing the axis, not null
     * @throws UnsupportedAxisException if the semantics of the following axis are
     *                                  not supported by this object model
     */
    public Iterator getFollowingAxisIterator(Object contextNode) throws UnsupportedAxisException {
        //TODO: Fix this better?
        return super.getFollowingAxisIterator(contextNode);
    }

    /**
     * Retrieve an <code>Iterator</code> matching the <code>preceding</code> XPath axis.
     *
     * @param contextNode the original context node
     * @return an Iterator capable of traversing the axis, not null
     * @throws UnsupportedAxisException if the semantics of the preceding axis are
     *                                  not supported by this object model
     */
    public Iterator getPrecedingAxisIterator(Object contextNode) throws UnsupportedAxisException {
        //TODO: Fix this better?
        return super.getPrecedingAxisIterator(contextNode);
    }

    /**
     * Loads a document from the given URI
     *
     * @param uri the URI of the document to load
     * @return the document
     * @throws FunctionCallException if the document could not be loaded
     */
    public Object getDocument(String uri)
            throws FunctionCallException {
        try {
            XMLStreamReader parser;
            if(uri.indexOf(':')==-1) {
                parser = XMLInputFactory.newInstance().createXMLStreamReader(
                        new FileInputStream(uri));
            }  else {
                URL url = new URL(uri);
                parser = XMLInputFactory.newInstance().createXMLStreamReader(
                        url.openStream());
            }
            StAXOMBuilder builder =
                    new StAXOMBuilder(parser);
            return builder.getDocumentElement();
        } catch (Exception e) {
            throw new FunctionCallException(e);
        }
    }

    /**
     * Returns the element whose ID is given by elementId.
     * If no such element exists, returns null.
     * Attributes with the name "ID" are not of type ID unless so defined.
     * Implementations that do not know whether attributes are of type ID or
     * not are expected to return null.
     *
     * @param contextNode a node from the document in which to look for the
     *                    id
     * @param elementId   id to look for
     * @return element whose ID is given by elementId, or null if no such
     *         element exists in the document or if the implementation
     *         does not know about attribute types
     */
    public Object getElementById(Object contextNode, String elementId) {
        //TODO: Fix this better?
        return super.getElementById(contextNode, elementId);
    }

    /**
     * Returns the document node that contains the given context node.
     *
     * @param contextNode the context node
     * @return the document of the context node
     * @see #isDocument(Object)
     */
    public Object getDocumentNode(Object contextNode) {
        if (contextNode instanceof OMDocument) {
            return contextNode;
        }
        return getDocumentNode(((OMNode) contextNode).getParent());
    }

    /**
     * Translate a namespace prefix to a namespace URI, <b>possibly</b>
     * considering a particular element node.
     * <p/>
     * <p/>
     * Strictly speaking, prefix-to-URI translation should occur
     * irrespective of any element in the document.  This method
     * is provided to allow a non-conforming ease-of-use enhancement.
     * </p>
     *
     * @param prefix  the prefix to translate
     * @param element the element to consider during translation
     * @return the namespace URI associated with the prefix
     */
    public String translateNamespacePrefixToUri(String prefix, Object element) {
        //TODO: Fix this better?
        return super.translateNamespacePrefixToUri(prefix, prefix);
    }

    /**
     * Retrieve the target of a processing-instruction.
     *
     * @param object the context processing-instruction node
     * @return the target of the processing-instruction node
     */
    public String getProcessingInstructionTarget(Object object) {
        //TODO: Fix this better?
        return super.getProcessingInstructionTarget(object);
    }

    /**
     * Retrieve the data of a processing-instruction.
     *
     * @param object the context processing-instruction node
     * @return the data of the processing-instruction node
     */
    public String getProcessingInstructionData(Object object) {
        //TODO: Fix this better?
        return super.getProcessingInstructionData(object);
    }

    /**
     * Returns a number that identifies the type of node that the given
     * object represents in this navigator.
     *
     * @param node ????
     * @return ????
     * @see org.jaxen.pattern.Pattern
     */
    public short getNodeType(Object node) {
        //TODO: Fix this better?
        return super.getNodeType(node);
    }

    /**
     * Returns the parent of the given context node.
     * <p/>
     * <p/>
     * The parent of any node must either be a document
     * node or an element node.
     * </p>
     *
     * @param contextNode the context node
     * @return the parent of the context node, or null if this is a document node.
     * @throws UnsupportedAxisException if the parent axis is not
     *                                  supported by the model
     * @see #isDocument
     * @see #isElement
     */
    public Object getParentNode(Object contextNode) throws UnsupportedAxisException {
        if(contextNode instanceof OMDocument)
            return null;
        return getDocumentNode(((OMNode) contextNode).getParent());
    }
}

