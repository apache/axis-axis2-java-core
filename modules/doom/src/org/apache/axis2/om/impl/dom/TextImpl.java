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
package org.apache.axis2.om.impl.dom;

import org.apache.axis2.namespace.Constants;
import org.apache.axis2.util.Base64;
import org.apache.axis2.util.UUIDGenerator;
import org.apache.ws.commons.attachments.DataHandlerUtils;
import org.apache.ws.commons.om.*;
import org.apache.ws.commons.om.impl.OMOutputImpl;
import org.apache.ws.commons.om.impl.mtom.MTOMStAXSOAPModelBuilder;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.InputStream;

public class TextImpl extends CharacterImpl implements Text, OMText {

    private String mimeType;

    private boolean optimize;

    private boolean isBinary;

    /**
     * 
     */
    private String contentID = null;

    /**
     * Field dataHandler contains the DataHandler. Declaring as Object to remove
     * the dependency on Javax.activation.DataHandler
     */
    private Object dataHandlerObject = null;

    /**
     * Field nameSpace is used when serializing Binary stuff as MTOM optimized.
     */
    protected OMNamespace ns = null;

    /**
     * Field localName is used when serializing Binary stuff as MTOM optimized.
     */
    protected String localName = "Include";

    /**
     * Field attribute is used when serializing Binary stuff as MTOM optimized.
     */
    protected OMAttribute attribute;

    /**
     * Creates a text node with the given text required by the OMDOMFactory. The
     * owner document should be set properly when appending this to a DOM tree.
     * 
     * @param text
     */
    public TextImpl(String text, OMFactory factory) {
        super(factory);
        this.textValue = new StringBuffer(text);
        this.done = true;
        this.ns = new NamespaceImpl(Constants.URI_XOP_INCLUDE, "xop", factory);
    }

    /**
     * @param contentID
     * @param parent
     * @param builder
     *            Used when the builder is encountered with a XOP:Include tag
     *            Stores a reference to the builder and the content-id. Supports
     *            deffered parsing of MIME messages
     */
    public TextImpl(String contentID, OMElement parent,
            OMXMLParserWrapper builder, OMFactory factory) {
        super((DocumentImpl) ((ParentNode) parent).getOwnerDocument(), factory);
        this.contentID = contentID;
        this.optimize = true;
        this.isBinary = true;
        this.done = true;
        this.builder = builder;
        this.ns = new NamespaceImpl(Constants.URI_XOP_INCLUDE, "xop", factory);
    }

    public TextImpl(String text, String mimeType, boolean optimize,
            OMFactory factory) {
        this(text, mimeType, optimize, true, factory);
    }

    public TextImpl(String text, String mimeType, boolean optimize,
            boolean isBinary, OMFactory factory) {
        this(text, factory);
        this.mimeType = mimeType;
        this.optimize = optimize;
        this.isBinary = isBinary;
    }

    /**
     * @param dataHandler
     * @param optimize
     *            To send binary content. Created progrmatically.
     */
    public TextImpl(Object dataHandler, boolean optimize, OMFactory factory) {
        super(factory);
        this.dataHandlerObject = dataHandler;
        this.isBinary = true;
        this.optimize = optimize;
        done = true;
        this.ns = new NamespaceImpl(Constants.URI_XOP_INCLUDE, "xop", factory);
    }

    /**
     * @param ownerNode
     */
    public TextImpl(DocumentImpl ownerNode, OMFactory factory) {
        super(ownerNode, factory);
        this.done = true;
        this.ns = new NamespaceImpl(Constants.URI_XOP_INCLUDE, "xop", factory);
    }

    /**
     * @param ownerNode
     * @param value
     */
    public TextImpl(DocumentImpl ownerNode, String value, OMFactory factory) {
        super(ownerNode, value, factory);
        this.done = true;
        this.ns = new NamespaceImpl(Constants.URI_XOP_INCLUDE, "xop", factory);
    }

    /**
     * @param ownerNode
     * @param value
     */
    public TextImpl(DocumentImpl ownerNode, String value, String mimeType,
            boolean optimize, OMFactory factory) {
        this(ownerNode, value, factory);
        this.mimeType = mimeType;
        this.optimize = optimize;
        this.isBinary = true;
        done = true;
    }

    /**
     * Breaks this node into two nodes at the specified offset, keeping both in
     * the tree as siblings. After being split, this node will contain all the
     * content up to the offset point. A new node of the same type, which
     * contains all the content at and after the offset point, is returned. If
     * the original node had a parent node, the new node is inserted as the next
     * sibling of the original node. When the offset is equal to the length of
     * this node, the new node has no data.
     */
    public Text splitText(int offset) throws DOMException {
        if (this.isReadonly()) {
            throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                    DOMMessageFormatter.formatMessage(
                            DOMMessageFormatter.DOM_DOMAIN,
                            "NO_MODIFICATION_ALLOWED_ERR", null));
        }
        if (offset < 0 || offset > this.textValue.length()) {
            throw new DOMException(DOMException.INDEX_SIZE_ERR,
                    DOMMessageFormatter.formatMessage(
                            DOMMessageFormatter.DOM_DOMAIN, "INDEX_SIZE_ERR",
                            null));
        }
        String newValue = this.textValue.substring(offset);
        this.deleteData(offset, this.textValue.length());

        TextImpl newText = (TextImpl) this.getOwnerDocument().createTextNode(
                newValue);

        if (this.parentNode != null) {
            newText.setParent(this.parentNode);
        }

        this.insertSiblingAfter(newText);

        return newText;
    }

    // /
    // /org.w3c.dom.Node methods
    // /
    public String getNodeName() {
        return "#text";
    }

    public short getNodeType() {
        return Node.TEXT_NODE;
    }

    // /
    // /OMNode methods
    // /

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.ws.commons.om.OMNode#getType()
     */
    public int getType() throws OMException {
        return Node.TEXT_NODE;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.ws.commons.om.OMNode#setType(int)
     */
    public void setType(int nodeType) throws OMException {
        // do not do anything here
        // Its not clear why we should let someone change the type of a node
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.ws.commons.om.OMNode#serialize(org.apache.ws.commons.om.OMOutput)
     */
    public void serialize(OMOutputImpl omOutput) throws XMLStreamException {
        serializeLocal(omOutput);
    }

    public void serializeAndConsume(OMOutputImpl omOutput)
            throws XMLStreamException {
        serializeLocal(omOutput);
    }

    public boolean isOptimized() {
        return this.optimize;
    }

    public void setOptimize(boolean value) {
        this.optimize = value;
    }

    public void discard() throws OMException {
        if (done) {
            this.detach();
        } else {
            builder.discard((OMElement) this.parentNode);
        }
    }

    /**
     * Writes the relevant output.
     * 
     * @param omOutput
     * @throws XMLStreamException
     */
    private void writeOutput(OMOutputImpl omOutput) throws XMLStreamException {
        XMLStreamWriter writer = omOutput.getXmlStreamWriter();
        int type = getType();
        if (type == Node.TEXT_NODE || type == SPACE_NODE) {
            writer.writeCharacters(this.getText());
        } else if (type == Node.CDATA_SECTION_NODE) {
            writer.writeCData(this.getText());
        } else if (type == Node.ENTITY_REFERENCE_NODE) {
            writer.writeEntityRef(this.getText());
        }
    }

    public String getText() {
        if (this.textValue != null) {
            return this.textValue.toString();
        } else {
            try {
                InputStream inStream;
                inStream = this.getInputStream();
                // int x = inStream.available();
                byte[] data;
                StringBuffer text = new StringBuffer();
                do {
                    data = new byte[1024];
                    int len;
                    while ((len = inStream.read(data)) > 0) {
                        byte[] temp = new byte[len];
                        System.arraycopy(data, 0, temp, 0, len);
                        text.append(Base64.encode(temp));
                    }

                } while (inStream.available() > 0);
                return text.toString();
            } catch (Exception e) {
                throw new OMException(e);
            }
        }
    }

    public String getNodeValue() throws DOMException {
        return this.getText();
    }

    public String getContentID() {
        if (contentID == null) {
            contentID = UUIDGenerator.getUUID() + "@apache.org";
        }
        return this.contentID;
    }

    public Object getDataHandler() {
        /*
         * this should return a DataHandler containing the binary data
         * reperesented by the Base64 strings stored in OMText
         */
        if (textValue != null & isBinary) {
            return DataHandlerUtils
                    .getDataHandlerFromText(textValue.toString(), mimeType);
        } else {

            if (dataHandlerObject == null) {
                if (contentID == null) {
                    throw new RuntimeException("ContentID is null");
                }
                dataHandlerObject = ((MTOMStAXSOAPModelBuilder) builder)
                        .getDataHandler(contentID);
            }
            return dataHandlerObject;
        }
    }

    public java.io.InputStream getInputStream() throws OMException {
        if (isBinary) {
            if (dataHandlerObject == null) {
                getDataHandler();
            }
            InputStream inStream;
            javax.activation.DataHandler dataHandler = (javax.activation.DataHandler) dataHandlerObject;
            try {
                inStream = dataHandler.getDataSource().getInputStream();
            } catch (IOException e) {
                throw new OMException(
                        "Cannot get InputStream from DataHandler." + e);
            }
            return inStream;
        } else {
            throw new OMException("Unsupported Operation");
        }
    }

    private void serializeLocal(OMOutputImpl omOutput)
            throws XMLStreamException {
        if (!this.isBinary) {
            writeOutput(omOutput);
        } else {
            if (omOutput.isOptimized()) {
                if (contentID == null) {
                    contentID = omOutput.getNextContentId();
                }
                // send binary as MTOM optimised
                this.attribute = new AttrImpl(this.ownerNode, "href",
                        new NamespaceImpl("", "", this.factory), 
                        "cid:" + getContentID(),
                        this.factory);
                this.serializeStartpart(omOutput);
                omOutput.writeOptimized(this);
                omOutput.getXmlStreamWriter().writeEndElement();
            } else {
                omOutput.getXmlStreamWriter().writeCharacters(this.getText());
            }
        }
    }

    /*
     * Methods to copy from OMSerialize utils.
     */
    private void serializeStartpart(OMOutputImpl omOutput)
            throws XMLStreamException {
        String nameSpaceName;
        String writer_prefix;
        String prefix;
        XMLStreamWriter writer = omOutput.getXmlStreamWriter();
        if (this.ns != null) {
            nameSpaceName = this.ns.getName();
            writer_prefix = writer.getPrefix(nameSpaceName);
            prefix = this.ns.getPrefix();
            if (nameSpaceName != null) {
                if (writer_prefix != null) {
                    writer
                            .writeStartElement(nameSpaceName, this
                                    .getLocalName());
                } else {
                    if (prefix != null) {
                        writer.writeStartElement(prefix, this.getLocalName(),
                                nameSpaceName);
                        // TODO FIX ME
                        // writer.writeNamespace(prefix, nameSpaceName);
                        writer.setPrefix(prefix, nameSpaceName);
                    } else {
                        writer.writeStartElement(nameSpaceName, this
                                .getLocalName());
                        writer.writeDefaultNamespace(nameSpaceName);
                        writer.setDefaultNamespace(nameSpaceName);
                    }
                }
            } else {
                writer.writeStartElement(this.getLocalName());
            }
        } else {
            writer.writeStartElement(this.getLocalName());
        }
        // add the elements attribute "href"
        serializeAttribute(this.attribute, omOutput);
        // add the namespace
        serializeNamespace(this.ns, omOutput);
    }

    /**
     * Method serializeAttribute.
     * 
     * @param attr
     * @param omOutput
     * @throws XMLStreamException
     */
    static void serializeAttribute(OMAttribute attr, OMOutputImpl omOutput)
            throws XMLStreamException {
        XMLStreamWriter writer = omOutput.getXmlStreamWriter();
        // first check whether the attribute is associated with a namespace
        OMNamespace ns = attr.getNamespace();
        String prefix;
        String namespaceName;
        if (ns != null) {
            // add the prefix if it's availble
            prefix = ns.getPrefix();
            namespaceName = ns.getName();
            if (prefix != null) {
                writer.writeAttribute(prefix, namespaceName, attr
                        .getLocalName(), attr.getAttributeValue());
            } else {
                writer.writeAttribute(namespaceName, attr.getLocalName(), attr
                        .getAttributeValue());
            }
        } else {
            writer
                    .writeAttribute(attr.getLocalName(), attr
                            .getAttributeValue());
        }
    }

    /**
     * Method serializeNamespace.
     * 
     * @param namespace
     * @param omOutput
     * @throws XMLStreamException
     */
    static void serializeNamespace(OMNamespace namespace, OMOutputImpl omOutput)
            throws XMLStreamException {
        XMLStreamWriter writer = omOutput.getXmlStreamWriter();
        if (namespace != null) {
            String uri = namespace.getName();
            String ns_prefix = namespace.getPrefix();
            writer.writeNamespace(ns_prefix, namespace.getName());
            writer.setPrefix(ns_prefix, uri);
        }
    }

    public Node cloneNode(boolean deep) {
        TextImpl textImpl = new TextImpl(this.textValue.toString(), this.factory);
        textImpl.setOwnerDocument(this.ownerNode);
        return textImpl;
    }

    /*
     * DOM-Level 3 methods
     */

    public String getWholeText() {
        // TODO TODO
        throw new UnsupportedOperationException("TODO");
    }

    public boolean isElementContentWhitespace() {
        // TODO TODO
        throw new UnsupportedOperationException("TODO");
    }

    public Text replaceWholeText(String arg0) throws DOMException {
        // TODO TODO
        throw new UnsupportedOperationException("TODO");
    }

    public String toString() {
        return (this.textValue != null) ? textValue.toString() : "";
    }

}
