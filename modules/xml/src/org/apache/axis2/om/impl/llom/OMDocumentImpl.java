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

import java.io.OutputStream;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.apache.axis2.om.OMDocument;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMException;
import org.apache.axis2.om.OMNode;
import org.apache.axis2.om.OMOutputFormat;
import org.apache.axis2.om.OMXMLParserWrapper;
import org.apache.axis2.om.impl.OMContainerEx;
import org.apache.axis2.om.impl.OMNodeEx;
import org.apache.axis2.om.impl.OMOutputImpl;
import org.apache.axis2.om.impl.llom.traverse.OMChildrenIterator;
import org.apache.axis2.om.impl.llom.traverse.OMChildrenQNameIterator;

/**
 * Class OMDocumentImpl
 */
public class OMDocumentImpl implements OMDocument, OMContainerEx {
    /**
     * Field documentElement
     */
    protected OMElement documentElement;

    /**
     * Field firstChild
     */
    protected OMNode firstChild;

    /**
     * Field lastChild
     */
    protected OMNode lastChild;

    /**
     * Field done
     */
    protected boolean done = false;

    /**
     * Field parserWrapper
     */
    protected OMXMLParserWrapper parserWrapper;

    /**
     * Field charSetEncoding
     * Dafult : UTF-8
     */
    protected String charSetEncoding = "UTF-8";

    /**
     * Field xmlVersion
     */
    protected String xmlVersion = "1.0";

    protected String isStandalone;


    /**
     * Default constructor
     */
    public OMDocumentImpl() {
        this.done = true;
    }

    /**
     * @param documentElement
     * @param parserWrapper
     */
    public OMDocumentImpl(OMElement documentElement, OMXMLParserWrapper parserWrapper) {
        this.documentElement = documentElement;
        this.parserWrapper = parserWrapper;
    }

    /**
     * @param parserWrapper
     */
    public OMDocumentImpl(OMXMLParserWrapper parserWrapper) {
        this.parserWrapper = parserWrapper;
    }

    /**
     * Method getDocumentElement
     *
     * @return om element
     */
    public OMElement getOMDocumentElement() {
        while (documentElement == null) {
            parserWrapper.next();
        }
        return documentElement;
    }

    /**
     * Method setDocumentElement
     *
     * @param documentElement
     */
    public void setOMDocumentElement(OMElement documentElement) {
        this.documentElement = documentElement;
    }

    /**
     * this will indicate whether parser has parsed this information item completely or not.
     * If somethings info are not available in the item, one has to check this attribute to make sure that, this
     * item has been parsed completely or not.
     *
     * @return boolean
     */
    public boolean isComplete() {
        return done;
    }

    /**
     * Method setComplete
     *
     * @param state
     */
    public void setComplete(boolean state) {
        this.done = state;
    }

    /**
     * This will force the parser to proceed, if parser has not yet finished with the XML input
     */
    public void buildNext() {
        if (!parserWrapper.isCompleted())
            parserWrapper.next();
    }

    /**
     * This will add child to the element. One can decide whether he append the child or he adds to the
     * front of the children list
     *
     * @param child
     */
    public void addChild(OMNode child) {
    	if(child instanceof OMElement) {
    		if(this.documentElement == null) {
    			addChild((OMNodeImpl) child);
    			this.documentElement = (OMElement)child;
    		} else {
    			throw new OMException("Document element already exists");
    		}
    	} else {
    		addChild((OMNodeImpl) child);
    	}
    }

    /**
     * Method addChild
     *
     * @param child
     */
    private void addChild(OMNodeImpl child) {
        if (firstChild == null) {
            firstChild = child;
            child.setPreviousOMSibling(null);
        } else {
            child.setPreviousOMSibling(lastChild);
            ((OMNodeEx)lastChild).setNextOMSibling(child);
        }
        child.setNextOMSibling(null);
        child.setParent(this);
        lastChild = child;

    }

    /**
     * This returns a collection of this element.
     * Children can be of types OMElement, OMText.
     *
     * @return iterator
     */
    public Iterator getChildren() {
        return new OMChildrenIterator(getFirstOMChild());
    }

    /**
     * This will search for children with a given QName and will return an iterator to traverse through
     * the OMNodes.
     * This QName can contain any combination of prefix, localname and URI
     *
     * @param elementQName
     * @return
     * @throws org.apache.axis2.om.OMException
     */
    public Iterator getChildrenWithName(QName elementQName) {
        return new OMChildrenQNameIterator(getFirstOMChild(),
                elementQName);
    }

    /**
     * Method getFirstOMChild
     *
     * @return first om child
     */
    public OMNode getFirstOMChild() {
        while ((firstChild == null) && !done) {
            buildNext();
        }
        return firstChild;
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
     * Method setFirstChild
     *
     * @param firstChild
     */
    public void setFirstChild(OMNode firstChild) {
        this.firstChild = firstChild;
    }


    /**
     * Returns the character set encoding scheme to be used
     *
     * @return charset
     */
    public String getCharsetEncoding() {
        return charSetEncoding;
    }

    /**
     * Set the character set encoding scheme
     *
     * @param charEncoding
     */
    public void setCharsetEncoding(String charEncoding) {
        this.charSetEncoding = charEncoding;
    }

    public String isStandalone() {
        return isStandalone;
    }

    public void setStandalone(String isStandalone) {
        this.isStandalone = isStandalone;
    }

    public String getXMLVersion() {
        return xmlVersion;
    }

    public void setXMLVersion(String xmlVersion) {
        this.xmlVersion = xmlVersion;
    }

    /**
     * Serialize the docuement with/without the XML declaration
     */
    public void serializeAndConsume(OMOutputImpl omOutput, boolean includeXMLDeclaration) throws XMLStreamException {
        serialize(omOutput, false, includeXMLDeclaration);
    }

    /**
     * Serialize the document with the XML declaration
     */
    public void serializeAndConsume(OMOutputImpl omOutput)
            throws XMLStreamException {
        serialize(omOutput, false, !omOutput.isIgnoreXMLDeclaration());
    }


    /**
     * Serialize the document with cache
     */
    public void serialize(OMOutputImpl omOutput) throws XMLStreamException {
        serialize(omOutput, true, !omOutput.isIgnoreXMLDeclaration());

    }

    /**
     * Serialize the document directly to the outputstream with Caching disabled
     * 
     * @param output
     * @throws XMLStreamException
     */
    public void serializeAndConsume(OutputStream output) throws XMLStreamException {
        OMOutputImpl omOutput = new OMOutputImpl(output, new OMOutputFormat());
        serializeAndConsume(omOutput);
        omOutput.flush();
    }

    /**
     * Serialize the document directly to the outputstream with Caching enabled
     * 
     * @param output
     * @throws XMLStreamException
     */
    public void serialize(OutputStream output) throws XMLStreamException {
        OMOutputImpl omOutput = new OMOutputImpl(output, new OMOutputFormat());
        serialize(omOutput);
        omOutput.flush();
    }

    /**
     * Serialize the document directly to the outputstream with Caching disabled
     * 
     * @param output
     * @param format
     * @throws XMLStreamException
     */
    public void serializeAndConsume(OutputStream output, OMOutputFormat format) throws XMLStreamException {
        OMOutputImpl omOutput = new OMOutputImpl(output, format);
        serializeAndConsume(omOutput);
        omOutput.flush();
    }

    /**
     * Serialize the document directly to the outputstream with Caching enabled
     * 
     * @param output
     * @param format
     * @throws XMLStreamException
     */
    public void serialize(OutputStream output, OMOutputFormat format) throws XMLStreamException {
        OMOutputImpl omOutput = new OMOutputImpl(output, format);
        serialize(omOutput);
        omOutput.flush();
    }

    /**
     * Serialize the document with cache
     */
    public void serialize(OMOutputImpl omOutput, boolean includeXMLDeclaration) throws XMLStreamException {
        serialize(omOutput, true, includeXMLDeclaration);

    }

    protected void serialize(OMOutputImpl omOutput, boolean cache, boolean includeXMLDeclaration) throws XMLStreamException {
        if (includeXMLDeclaration) {
            //Check whether the OMOutput char encoding and OMDocument char
            //encoding matches, if not use char encoding of OMOutput
            String outputCharEncoding = omOutput.getCharSetEncoding();
            if (outputCharEncoding == null || "".equals(outputCharEncoding)) {
                omOutput.getXmlStreamWriter().writeStartDocument(charSetEncoding,
                        xmlVersion);
            } else {
                omOutput.getXmlStreamWriter().writeStartDocument(outputCharEncoding,
                        xmlVersion);
            }
        }

        Iterator children = this.getChildren();

        if (cache) {
            while (children.hasNext()) {
                OMNodeEx omNode = (OMNodeEx) children.next();
                omNode.serialize(omOutput);
            }
        } else {
            while (children.hasNext()) {
                OMNodeEx omNode = (OMNodeEx) children.next();
                omNode.serializeAndConsume(omOutput);
            }
        }
    }


}
