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
import java.util.Date;
import java.util.Random;

import javax.activation.DataHandler;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axis2.attachments.Base64;
import org.apache.axis2.attachments.ByteArrayDataSource;
import org.apache.axis2.attachments.IOUtils;
import org.apache.axis2.om.OMAttribute;
import org.apache.axis2.om.OMConstants;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMException;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.om.OMNode;
import org.apache.axis2.om.OMOutput;
import org.apache.axis2.om.OMText;
import org.apache.axis2.om.OMXMLParserWrapper;
import org.apache.axis2.om.impl.llom.mtom.MTOMStAXSOAPModelBuilder;

/**
 * @author <a href="mailto:thilina@opensource.lk">Thilina Gunarathne </a>
 */
public class OMTextImpl extends OMNodeImpl implements OMText, OMConstants {

	protected String value = null;

	protected short textType = TEXT_NODE;

	protected String mimeType;

	protected boolean optimize = false;

	protected boolean isBinary = false;
	
	private static Random rnd = new Random(new Date().getTime());

	/**
	 * Field contentID for the mime part used when serialising Binary stuff as
	 * MTOM optimised
	 */
	private String contentID = null;

	/**
	 * Field dataHandler
	 */
	private DataHandler dataHandler = null;

	/**
	 * Field nameSpace used when serialising Binary stuff as MTOM optimised
	 */
	protected OMNamespace ns = new OMNamespaceImpl(
			"http://www.w3.org/2004/08/xop/Include", "xop");

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
	}

	/**
	 * @param s -
	 *            base64 encoded String representation of Binary
	 * @param mimeType
	 *            of the Binary
	 */
	public OMTextImpl(String s, String mimeType, boolean optimize) {
		this(null,s,mimeType,optimize);
	}

	/**
	 * @param parent
	 * @param s -
	 *            base64 encoded String representation of Binary
	 * @param mimeType
	 *            of the Binary
	 */
	public OMTextImpl(OMElement parent, String s, String mimeType,
			boolean optimize) {
		this(parent, s);
		this.mimeType = mimeType;
		this.optimize = optimize;
		if (this.contentID == null && optimize==true) {
			createContentID();
		}
        done = true;
	}

	/**
	 * @param dataHandler
	 *            To send binary optimised content Created programatically.
	 */
	public OMTextImpl(DataHandler dataHandler) {
		this(dataHandler,true);

	}

	/**
	 * @param dataHandler
	 * @param optimize
	 *            To send binary content. Created progrmatically.
	 */
	public OMTextImpl(DataHandler dataHandler, boolean optimize) {
		this.dataHandler = dataHandler;
		this.isBinary = true;
		this.optimize = optimize;
		if (this.contentID == null && optimize==true) {
			createContentID();
		}
        done = true;

	}

	/**
	 * @param contentID
	 * @param parent
	 * @param builder
	 *            Used when the builder is encountered with a XOP:Include tag
	 *            Stores a reference to the builder and the content-id. Supports
	 *            deffered parsing of MIME messages
	 */
	public OMTextImpl(String contentID, OMElement parent,
			OMXMLParserWrapper builder) {
		super(parent);
		this.contentID = contentID;
		this.optimize = true;
		this.isBinary = true;
		this.builder = builder;
        //done = true;
        
	}

	/**
	 * We use the OMText class to hold comments, text, characterData, CData,
	 * etc., The codes are found in OMNode class
	 * 
	 * @param type
	 */
	public void setTextType(short type) {
		if ((type == TEXT_NODE) || (type == COMMENT_NODE)
				|| (type == CDATA_SECTION_NODE)) {
			this.textType = type;
		} else {
			throw new UnsupportedOperationException("Attempt to set wrong type");
		}
	}

	public int getType() throws OMException {
		return textType;
	}

	/**
	 * @param writer
	 * @throws XMLStreamException
	 */
	public void serializeWithCache(OMOutput omOutput) throws XMLStreamException {
		XMLStreamWriter writer = omOutput.getXmlStreamWriter();
		if (textType == TEXT_NODE) {
			writer.writeCharacters(this.value);
		} else if (textType == COMMENT_NODE) {
			writer.writeComment(this.value);
		} else if (textType == CDATA_SECTION_NODE) {
			writer.writeCData(this.value);
		}
		OMNode nextSibling = this.getNextSibling();
		if (nextSibling != null) {
			nextSibling.serializeWithCache(omOutput);
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
				byte[] data;
				data = new byte[inStream.available()];
				IOUtils.readFully(inStream, data);
				return Base64.encode(data);
			} catch (Exception e) {
				throw new OMException(
						"Cannot read from Stream taken form the Data Handler"
								+ e);
			}
		}

	}

	public boolean isOptimized() {
		return optimize;
	}

	public void doOptimize(boolean value) {
		this.optimize = value;
		if (this.contentID == null && value==true) {
			getContentID();
		}
	}

	/**
	 * @return
	 * @throws org.apache.axis2.om.OMException
	 * @throws OMException
	 */
	public DataHandler getDataHandler() {

		/*
		 * this should return a DataHandler containing the binary data
		 * reperesented by the Base64 strings stored in OMText
		 */
		if (value != null) {
			ByteArrayDataSource dataSource;
			byte[] data = Base64.decode(value);
			if (mimeType != null) {
				dataSource = new ByteArrayDataSource(data, mimeType);
			} else {
				// Assumes type as application/octet-stream
				dataSource = new ByteArrayDataSource(data);
			}
			DataHandler dataHandler = new DataHandler(dataSource);
			return dataHandler;
		} else {
			if (dataHandler == null) {
				dataHandler = ((MTOMStAXSOAPModelBuilder) builder)
						.getDataHandler(contentID);
			}
			return dataHandler;
		}
	}

	public String getLocalName() {
		return localName;
	}

	public java.io.InputStream getInputStream() throws OMException {
		if (isBinary == true) {
			if (dataHandler == null) {
				getDataHandler();
			}
			InputStream inStream;
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
		return this.contentID;
	}

	public boolean isComplete() {
		return done;
	}

	public void serialize(OMOutput omOutput) throws XMLStreamException {
		boolean firstElement = false;

		if (!this.isBinary) {
			serializeWithCache(omOutput);
		} else {
			if (omOutput.doOptimise()) {
				// send binary as MTOM optimised
				this.attribute = new OMAttributeImpl("href",
						new OMNamespaceImpl("", ""), "cid:"
								+ this.contentID.trim());

				this.serializeStartpart(omOutput);
				omOutput.writeOptimised(this);
				omOutput.getXmlStreamWriter().writeEndElement();
			} else {
				omOutput.getXmlStreamWriter().writeCharacters(this.getText());
			}
			// TODO do we need these
			OMNode nextSibling = this.getNextSibling();
			if (nextSibling != null) {
				// serilize next sibling
				nextSibling.serialize(omOutput);
			} else {
				// TODO : See whether following part is really needed
				if (parent == null) {
					return;
				} else if (parent.isComplete()) {
					return;
				} else {
					// do the special serialization
					// Only the push serializer is left now
					builder.next();
				}

			}
		}

	}
	private void createContentID()
	{
//		 We can use a UUID, taken using Apache commons id project.
		// TODO change to UUID
		this.contentID = "2"+String.valueOf(rnd.nextLong())+"@schemas.xmlsoap.org";
	}


	/*
	 * Methods to copy from OMSerialize utils
	 */
	private void serializeStartpart(OMOutput omOutput)
			throws XMLStreamException {
		String nameSpaceName = null;
		String writer_prefix = null;
		String prefix = null;
		XMLStreamWriter writer = omOutput.getXmlStreamWriter();
		if (this.ns != null) {
            nameSpaceName = this.ns.getName();
            writer_prefix = writer.getPrefix(nameSpaceName);
            prefix = this.ns.getPrefix();
          
            if (nameSpaceName != null) {
                if (writer_prefix != null) {
                    writer.writeStartElement(nameSpaceName,
                            this.getLocalName());
                } else {
                    if (prefix != null) {
                        writer.writeStartElement(prefix, this.getLocalName(),
                                nameSpaceName);
                        //TODO FIX ME
                        //writer.writeNamespace(prefix, nameSpaceName);
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
	 * @param writer
	 * @throws XMLStreamException
	 */
	static void serializeAttribute(OMAttribute attr, OMOutput omOutput)
			throws XMLStreamException {

		XMLStreamWriter writer = omOutput.getXmlStreamWriter();
		// first check whether the attribute is associated with a namespace
		OMNamespace ns = attr.getNamespace();
		String prefix = null;
		String namespaceName = null;
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
	 * @param writer
	 * @throws XMLStreamException
	 */
	static void serializeNamespace(OMNamespace namespace, OMOutput omOutput)
			throws XMLStreamException {
		XMLStreamWriter writer = omOutput.getXmlStreamWriter();
		if (namespace != null) {
			String uri = namespace.getName();
			//String prefix = writer.getPrefix(uri);
			String ns_prefix = namespace.getPrefix();
			//if (prefix == null) {
				writer.writeNamespace(ns_prefix, namespace.getName());
				writer.setPrefix(ns_prefix, uri);
			//}
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