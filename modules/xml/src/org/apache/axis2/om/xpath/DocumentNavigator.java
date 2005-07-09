package org.apache.axis2.om.xpath;

import org.jaxen.DefaultNavigator;
import org.jaxen.*;
import org.jaxen.util.SingleObjectIterator;
import org.jaxen.saxpath.SAXPathException;
import org.apache.axis2.om.impl.llom.OMDocument;
import org.apache.axis2.om.impl.llom.builder.StAXOMBuilder;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMAttribute;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.om.OMNode;
import org.apache.axis2.om.OMText;
import org.apache.axis2.om.OMFactory;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLInputFactory;
import java.util.Iterator;
import java.net.URL;
import java.net.MalformedURLException;

public class DocumentNavigator extends DefaultNavigator {

    /**
     * Returns a parsed form of the given xpath string, which will be suitable
     *  for queries on documents that use the same navigator as this one.
     *
     *  @see XPath
     *
     *  @param xpath the XPath expression
     *
     *  @return a new XPath expression object
     *
     *  @throws SAXPathException if the string is not a syntactically
     *      correct XPath expression
     */
    public XPath parseXPath(String xpath) throws SAXPathException {
        XPath path = new BaseXPath(xpath, this);
        return path;
    }

    /**
     * Retrieve the namespace URI of the given element node.
     *
     *  @param object the context element node
     *
     *  @return the namespace URI of the element node
     */
    public String getElementNamespaceUri(Object object) {
        OMElement attr = (OMElement) object;
        return attr.getQName().getNamespaceURI();
    }

    /**
     * Retrieve the name of the given element node.
     *
     *  @param object the context element node
     *
     *  @return the name of the element node
     */
    public String getElementName(Object object) {
        OMElement attr = (OMElement) object;
        return attr.getQName().getLocalPart();
    }

    /**
     * Retrieve the QName of the given element node.
     *
     *  @param object the context element node
     *
     *  @return the QName of the element node
     */
    public String getElementQName(Object object) {
        OMElement attr = (OMElement) object;
        String prefix = attr.getNamespace().getPrefix();
        if (prefix == null || "".equals(prefix)) {
            return attr.getQName().getLocalPart();
        }
        return prefix + ":" + attr.getNamespace().getName();
    }

    /**
     * Retrieve the namespace URI of the given attribute node.
     *
     *  @param object the context attribute node
     *
     *  @return the namespace URI of the attribute node
     */
    public String getAttributeNamespaceUri(Object object) {
        OMAttribute attr = (OMAttribute) object;
        return attr.getQName().getNamespaceURI();
    }

    /**
     * Retrieve the name of the given attribute node.
     *
     *  @param object the context attribute node
     *
     *  @return the name of the attribute node
     */
    public String getAttributeName(Object object) {
        OMAttribute attr = (OMAttribute) object;
        return attr.getQName().getLocalPart();
    }

    /**
     * Retrieve the QName of the given attribute node.
     *
     *  @param object the context attribute node
     *
     *  @return the qualified name of the attribute node
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
     *  is the node that is selected by the xpath expression <code>/</code>.
     *
     *  @param object the object to test
     *
     *  @return <code>true</code> if the object is a document node,
     *          else <code>false</code>
     */
    public boolean isDocument(Object object) {
        return object instanceof OMDocument;
    }

    /**
     * Returns whether the given object is an element node.
     *
     *  @param object the object to test
     *
     *  @return <code>true</code> if the object is an element node,
     *          else <code>false</code>
     */
    public boolean isElement(Object object) {
        return object instanceof OMElement;
    }

    /**
     * Returns whether the given object is an attribute node.
     *
     *  @param object the object to test
     *
     *  @return <code>true</code> if the object is an attribute node,
     *          else <code>false</code>
     */
    public boolean isAttribute(Object object) {
        return object instanceof OMAttribute;
    }

    /**
     * Returns whether the given object is a namespace node.
     *
     *  @param object the object to test
     *
     *  @return <code>true</code> if the object is a namespace node,
     *          else <code>false</code>
     */
    public boolean isNamespace(Object object) {
        return object instanceof OMNamespace;
    }

    /**
     * Returns whether the given object is a comment node.
     *
     *  @param object the object to test
     *
     *  @return <code>true</code> if the object is a comment node,
     *          else <code>false</code>
     */
    public boolean isComment(Object object) {
        return (object instanceof OMNode) &&
            (((OMText)object).getType() == OMNode.COMMENT_NODE);
    }

    /**
     * Returns whether the given object is a text node.
     *
     *  @param object the object to test
     *
     *  @return <code>true</code> if the object is a text node,
     *          else <code>false</code>
     */
    public boolean isText(Object object) {
        return (object instanceof OMNode) &&
            (((OMText)object).getType() == OMNode.TEXT_NODE);
    }

    /**
     * Returns whether the given object is a processing-instruction node.
     *
     *  @param object the object to test
     *
     *  @return <code>true</code> if the object is a processing-instruction node,
     *          else <code>false</code>
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
     *  @param object the comment node
     *
     *  @return the string-value of the node
     */
    public String getCommentStringValue(Object object) {
        return ((OMText)object).getText();
    }

    /**
     * Retrieve the string-value of an element node.
     * This may be the empty string if the element is empty,
     * but must not be null.
     *
     *  @param object the comment node.
     *
     *  @return the string-value of the node.
     */
    public String getElementStringValue(Object object) {
        if (isElement(object)) {
            return getStringValue((OMElement)object, new StringBuffer()).toString();
        }
        return null;
    }

    private StringBuffer getStringValue (OMNode node, StringBuffer buffer)
    {
        if (isText(node)) {
            buffer.append(((OMText)node).getText());
        } else if(node instanceof OMElement) {
            Iterator children = ((OMElement)node).getChildren();
            while (children.hasNext()) {
                getStringValue((OMNode) children.next(), buffer);
            }
        }
        return buffer;
    }

    /**
     * Retrieve the string-value of an attribute node.
     *  This should be the XML 1.0 normalized attribute value.
     *  This may be the empty string but must not be null.
     *
     *  @param object the attribute node
     *
     *  @return the string-value of the node
     */
    public String getAttributeStringValue(Object object) {
        return ((OMAttribute)object).getValue();
    }

    /**
     * Retrieve the string-value of a namespace node.
     * This is generally the namespace URI.
     * This may be the empty string but must not be null.
     *
     *  @param object the namespace node
     *
     *  @return the string-value of the node
     */
    public String getNamespaceStringValue(Object object) {
        return ((OMNamespace)object).getName();
    }

    /**
     * Retrieve the string-value of a text node.
     * This must not be null and should not be the empty string.
     * The XPath data model does not allow empty text nodes.
     *
     *  @param object the text node
     *
     *  @return the string-value of the node
     */
    public String getTextStringValue(Object object) {
        return ((OMText)object).getText();
    }

    /**
     * Retrieve the namespace prefix of a namespace node.
     *
     *  @param object the namespace node
     *
     *  @return the prefix associated with the node
     */
    public String getNamespacePrefix(Object object) {
        return ((OMNamespace)object).getPrefix();
    }

    /**
     * Retrieve an <code>Iterator</code> matching the <code>child</code>
     *  XPath axis.
     *
     *  @param contextNode the original context node
     *
     *  @return an Iterator capable of traversing the axis, not null
     *
     *  @throws UnsupportedAxisException if the semantics of the child axis are
     *          not supported by this object model
     */
    public Iterator getChildAxisIterator(Object contextNode) throws UnsupportedAxisException {
        if (isElement(contextNode)) {
            return ((OMElement) contextNode).getChildren();
        }
        return JaxenConstants.EMPTY_ITERATOR;
    }

    /**
     * Retrieve an <code>Iterator</code> matching the <code>attribute</code>
     *  XPath axis.
     *
     *  @param contextNode the original context node
     *
     *  @return an Iterator capable of traversing the axis, not null
     *
     *  @throws UnsupportedAxisException if the semantics of the attribute axis are
     *          not supported by this object model
     */
    public Iterator getAttributeAxisIterator(Object contextNode) throws UnsupportedAxisException {
        if (isElement(contextNode)) {
            return ((OMElement)contextNode).getAttributes();
        }
        return JaxenConstants.EMPTY_ITERATOR;
    }

    /**
     * Retrieve an <code>Iterator</code> matching the <code>parent</code> XPath axis.
     *
     *  @param contextNode the original context node
     *
     *  @return an Iterator capable of traversing the axis, not null
     *
     *  @throws UnsupportedAxisException if the semantics of the parent axis are
     *          not supported by this object model
     */
    public Iterator getParentAxisIterator(Object contextNode) throws UnsupportedAxisException {
        if (contextNode instanceof OMNode) {
            return new SingleObjectIterator(((OMNode) contextNode).getParent());
        }
        return JaxenConstants.EMPTY_ITERATOR;
    }

    /**
     * Loads a document from the given URI
     *
     *  @param uri the URI of the document to load
     *
     *  @return the document
     *
      * @throws FunctionCallException if the document could not be loaded
     */
    public Object getDocument(String uri)
        throws FunctionCallException {
        try {
            URL url = new URL(uri);
            XMLStreamReader parser =
                XMLInputFactory.newInstance().createXMLStreamReader(
                                            url.openStream());
            StAXOMBuilder builder =
                    new StAXOMBuilder(parser);
            return builder.getDocumentElement();
        } catch (Exception e) {
            throw new FunctionCallException(e);
        }
    }

    /**
     * Returns the document node that contains the given context node.
     *
     *  @see #isDocument(Object)
     *
     *  @param contextNode the context node
     *
     *  @return the document of the context node
     */
    public Object getDocumentNode(Object contextNode) {
        if(contextNode instanceof OMDocument) {
            return contextNode;
        }
        return getDocumentNode(((OMNode)contextNode).getParent());
    }

    /**
     * Returns the parent of the given context node.
     *
     *  <p>
     *  The parent of any node must either be a document
     *  node or an element node.
     *  </p>
     *
     *  @see #isDocument
     *  @see #isElement
     *
     *  @param contextNode the context node
     *
     *  @return the parent of the context node, or null if this is a document node.
     *
     *  @throws UnsupportedAxisException if the parent axis is not
     *          supported by the model
     */
    public Object getParentNode(Object contextNode) throws UnsupportedAxisException {
        return getDocumentNode(((OMNode)contextNode).getParent());
    }
}

