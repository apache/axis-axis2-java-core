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


import java.io.IOException;
import java.io.InputStream;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;


import org.apache.axis2.attachments.Base64;
import org.apache.axis2.om.OMAttribute;
import org.apache.axis2.om.OMConstants;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMException;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.om.OMText;
import org.apache.axis2.om.OMXMLParserWrapper;
import org.apache.axis2.om.impl.OMOutputImpl;
import org.apache.axis2.om.impl.llom.mtom.MTOMStAXSOAPModelBuilder;
import org.apache.axis2.util.UUIDGenerator;

public class OMTextImpl extends OMNodeImpl implements OMText, OMConstants {
    protected String value = null;

    protected String mimeType;

    protected boolean optimize = false;

    protected boolean isBinary = false;

    /**
     * Field contentID for the mime part used when serialising Binary stuff as
     * MTOM optimised
     */
    private String contentID = null;

    /**
     * Field dataHandler contains the DataHandler
     * Declaring as Object to remove the depedency on 
     * Javax.activation.DataHandler
     */
    private Object dataHandlerObject = null;

    /**
     * Field nameSpace used when serialising Binary stuff as MTOM optimised
     */
    protected OMNamespace ns = new OMNamespaceImpl(
            "http://www.w3.org/2004/08/xop/include", "xop");

    /**
     * Field localName used when serialising Binary stuff as MTOM optimised
     */
    protected String localName = "Include";

    /**
     * Field attributes used when serialising Binary stuff as MTOM optimised
     */
    protected OMAttribute attribute;

    /**
     * Constructor OMTextImpl
     *
     * @param s
     */
    public OMTextImpl(String s) {
        this.value = s;
        this.nodeType = TEXT_NODE;
    }

    /**
     * @param s
     * @param nodeType - OMText can handler CHARACTERS, SPACES, CDATA and ENTITY REFERENCES.
     *                 Constants for this can be found in OMNode.
     */
    public OMTextImpl(String s, int nodeType) {
        this.value = s;
        this.nodeType = nodeType;
    }

    /**
     * Constructor OMTextImpl
     *
     * @param parent
     * @param text
     */
    public OMTextImpl(OMElement parent, String text) {
        super(parent);
        this.value = text;
        done = true;
        this.nodeType = TEXT_NODE;
    }

    /**
     * @param s        -
     *                 base64 encoded String representation of Binary
     * @param mimeType of the Binary
     */
    public OMTextImpl(String s, String mimeType, boolean optimize) {
        this(null, s, mimeType, optimize);
    }

    /**
     * @param parent
     * @param s        -
     *                 base64 encoded String representation of Binary
     * @param mimeType of the Binary
     */
    public OMTextImpl(OMElement parent, String s, String mimeType,
                      boolean optimize) {
        this(parent, s);
        this.mimeType = mimeType;
        this.optimize = optimize;
        this.isBinary = true;
        done = true;
        this.nodeType = TEXT_NODE;
    }

    /**
     * @param dataHandler To send binary optimised content Created programatically.
     */
    public OMTextImpl(Object dataHandler) {
        this(dataHandler, true);
    }

    /**
     * @param dataHandler
     * @param optimize    To send binary content. Created progrmatically.
     */
    public OMTextImpl(Object dataHandler, boolean optimize) {
        this.dataHandlerObject = dataHandler;
        this.isBinary = true;
        this.optimize = optimize;
        done = true;
        this.nodeType = TEXT_NODE;
    }

    /**
     * @param contentID
     * @param parent
     * @param builder   Used when the builder is encountered with a XOP:Include tag
     *                  Stores a reference to the builder and the content-id. Supports
     *                  deffered parsing of MIME messages
     */
    public OMTextImpl(String contentID, OMElement parent,
                      OMXMLParserWrapper builder) {
        super(parent);
        this.contentID = contentID;
        this.optimize = true;
        this.isBinary = true;
        this.builder = builder;
        this.nodeType = TEXT_NODE;
    }

    /**
     * @param omOutput
     * @throws XMLStreamException
     */
    public void serializeWithCache(
            org.apache.axis2.om.impl.OMOutputImpl omOutput)
            throws XMLStreamException {
        serializeLocal(omOutput);

    }

    /**
     * Writes the relevant output
     *
     * @param omOutput
     * @throws XMLStreamException
     */
    private void writeOutput(OMOutputImpl omOutput) throws XMLStreamException {
        XMLStreamWriter writer = omOutput.getXmlStreamWriter();
        int type = getType();
        if (type == TEXT_NODE || type == SPACE_NODE) {
            writer.writeCharacters(this.getText());
        } else if (type == CDATA_SECTION_NODE) {
            writer.writeCData(this.getText());
        } else if (type == ENTITY_REFERENCE_NODE) {
            writer.writeEntityRef(this.getText());
        }
    }

    /**
     * Returns the value
     */
    public String getText() throws OMException {
        if (this.value != null) {
            return this.value;
        } else {
            try {
                InputStream inStream;
                inStream = this.getInputStream();
                //int x = inStream.available();
                byte[] data;
                StringBuffer text = new StringBuffer();
                do {
                	data = new byte[1024];
                	int len;
                	while((len = inStream.read(data)) > 0) {
                		byte[] temp = new byte[len];
                		System.arraycopy(data,0,temp,0,len);
                		text.append(Base64.encode(temp));
                	}

                } while (inStream.available() > 0);
                return text.toString();
            } catch (Exception e) {
                throw new OMException(e);
            }
        }
    }

    public boolean isOptimized() {
        return optimize;
    }

    public void setOptimize(boolean value) {
        this.optimize = value;
	if (value)
	{
	     isBinary = true;
	}
    }

    
    /**
     * To get the datahandler
     * @return javax.activation.DataHandler
     */
    public Object getDataHandler() {
        /*
         * this should return a DataHandler containing the binary data
         * reperesented by the Base64 strings stored in OMText
         */
        if (value != null & isBinary) {
            return org.apache.axis2.attachments.DataHandlerUtils.getDataHandlerFromText(value,mimeType);
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

    public String getLocalName() {
        return localName;
    }

    public java.io.InputStream getInputStream() throws OMException {
        if (isBinary) {
            if (dataHandlerObject == null) {
                getDataHandler();
            }
            InputStream inStream;
            javax.activation.DataHandler dataHandler = (javax.activation.DataHandler)dataHandlerObject;
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

    public String getContentID() {
        if (contentID == null) {
            contentID = UUIDGenerator.getUUID()
                    + "@apache.org";
        }
        return this.contentID;
    }

    public boolean isComplete() {
        return done;
    }

    public void serialize(org.apache.axis2.om.impl.OMOutputImpl omOutput)
            throws XMLStreamException {
        serializeLocal(omOutput);
    }

    private void serializeLocal(OMOutputImpl omOutput) throws XMLStreamException {
        if (!this.isBinary) {
            writeOutput(omOutput);
        } else {
            if (omOutput.isOptimized()) {
                if (contentID == null) {
                    contentID = omOutput.getNextContentId();
                }
                // send binary as MTOM optimised
                this.attribute = new OMAttributeImpl("href",
                        new OMNamespaceImpl("", ""), "cid:" + getContentID());
                this.serializeStartpart(omOutput);
                omOutput.writeOptimized(this);
                omOutput.getXmlStreamWriter().writeEndElement();
            } else {
                omOutput.getXmlStreamWriter().writeCharacters(this.getText());
            } 
        }
    }

    /*
     * Methods to copy from OMSerialize utils
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
                        //TODO FIX ME
                        //writer.writeNamespace(prefix, nameSpaceName);
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
     * Method serializeAttribute
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
                        .getLocalName(), attr.getValue());
            } else {
                writer.writeAttribute(namespaceName, attr.getLocalName(), attr
                        .getValue());
            }
        } else {
            writer.writeAttribute(attr.getLocalName(), attr.getValue());
        }
    }

    /**
     * Method serializeNamespace
     *
     * @param namespace
     * @param omOutput
     * @throws XMLStreamException
     */
    static void serializeNamespace(OMNamespace namespace,
                                   org.apache.axis2.om.impl.OMOutputImpl omOutput)
            throws XMLStreamException {
        XMLStreamWriter writer = omOutput.getXmlStreamWriter();
        if (namespace != null) {
            String uri = namespace.getName();
            String ns_prefix = namespace.getPrefix();
            writer.writeNamespace(ns_prefix, namespace.getName());
            writer.setPrefix(ns_prefix, uri);
        }
    }

    /**
     * Slightly different implementation of the discard method
     *
     * @throws OMException
     */
    public void discard() throws OMException {
        if (done) {
            this.detach();
        } else {
            builder.discard((OMElement) this.parent);
        }
    }
}
