/**
 * Copyright 2001-2004 The Apache Software Foundation.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p/>
 */
package org.apache.axis.om.impl.llom.mtom;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Random;

import javax.activation.DataHandler;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axis.encoding.Base64;
import org.apache.axis.om.OMAttribute;
import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMException;
import org.apache.axis.om.OMNamespace;
import org.apache.axis.om.OMNode;
import org.apache.axis.om.OMXMLParserWrapper;
import org.apache.axis.om.impl.llom.OMAttributeImpl;
import org.apache.axis.om.impl.llom.OMNamespaceImpl;
import org.apache.axis.om.impl.llom.OMNodeImpl;

/**
 * @author Thilina Gunarathne thilina@opensource.lk
 */

public class OMBlobImpl extends OMNodeImpl implements OMBlob {
    /**
     * Field contentID for the mime part
     */
    private String contentID = null;
    /**
     * Field builder
     */
    private OMXMLParserWrapper builder;
    /**
     * Field dataHandler
     */
    private DataHandler dataHandler = null;
    /**
     * Field nameSpace
     */
    protected OMNamespace ns = new OMNamespaceImpl("http://www.w3.org/2004/08/xop/Include", "xop");
    /**
     * Field localName
     */
    protected String localName= "Include";
    /**
     * Field attributes
     */
    protected  OMAttribute attribute;

    
    public OMBlobImpl(DataHandler dataHandler) {
        this.dataHandler = dataHandler;
        if (this.contentID == null) {
            // We can use a UUID, taken using Apache commons id project.
            //          TODO change to UUID
            this.contentID = String.valueOf(new Random(new Date().getTime())
                    .nextLong());
        }
    }
    
    public OMBlobImpl(String contentID, OMElement parent,
            OMXMLParserWrapper builder) {
        super(parent);
        this.builder = builder;
        this.contentID = contentID;
    }
    
    public String getLocalName() {
        return localName;
    }
    
    public java.io.OutputStream getOutputStream() throws IOException {
        if (dataHandler == null) {
            getDataHandler();
        }
        OutputStream outStream = dataHandler.getOutputStream();
        if (!(outStream instanceof java.io.OutputStream)) {
            outStream = new ByteArrayOutputStream();
            Object outObject = dataHandler.getContent();
            ObjectOutputStream objectOutStream = new ObjectOutputStream(
                    outStream);
            objectOutStream.writeObject(outObject);
        }
        return outStream;
    }
    
    public String getValue() throws OMException {
        throw new OMException(
        "Blob contains Binary data. Returns Stream or Datahandler only");
    }
    
    public DataHandler getDataHandler() throws OMException {
        if (dataHandler == null) {
            dataHandler = ((MTOMStAXSOAPModelBuilder) builder)
            .getDataHandler(contentID);
        }
        return dataHandler;
    }
    
    public int getType() throws OMException {
        return OMNode.BLOB_NODE;
    }
    
    public String getContentID() {
        return this.contentID;
    }
    
    public boolean isComplete() {
        return true;
    }
    
    public void serialize(XMLStreamWriter writer)
    throws XMLStreamException {
        boolean firstElement = false;
            //No caching
            if (writer instanceof MTOMXMLStreamWriter) {
                // send as optimised
                MTOMXMLStreamWriter mtomWriter = (MTOMXMLStreamWriter) writer;
                this.attribute= new OMAttributeImpl("href",new OMNamespaceImpl("", ""), "cid:" + this.contentID.trim());
                
                this.serializeStartpart(mtomWriter);             
                mtomWriter.writeOptimised(this);
                mtomWriter.writeEndElement();
            } else {
                // send as non optimised
                ByteArrayOutputStream byteStream;
                    try {
                        byteStream = (ByteArrayOutputStream) this.getOutputStream();
                    
                    writer.writeCharacters(Base64.encode(byteStream
                            .toByteArray()));
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
            
            }
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
                    builder.next();
                }
            }
    }
    
    /*
     * Methods to copy from OMSerialize utils
     */
    private void serializeStartpart(XMLStreamWriter writer)
    throws XMLStreamException {
        String nameSpaceName = null;
        String writer_prefix = null;
        String prefix = null;
        if (this.ns != null) {
            nameSpaceName = this.ns.getName();
            writer_prefix = writer.getPrefix(nameSpaceName);
            prefix = this.ns.getPrefix();
            if (nameSpaceName != null) {
                if (writer_prefix != null) {
                    writer.writeStartElement(nameSpaceName, this.getLocalName());
                } else {
                    if (prefix != null) {
                        writer.writeStartElement(prefix,
                                this.getLocalName(), nameSpaceName);
                        writer.writeNamespace(prefix, nameSpaceName);
                        writer.setPrefix(prefix, nameSpaceName);
                    } else {
                        writer.writeStartElement(nameSpaceName, this.getLocalName());
                        writer.writeDefaultNamespace(nameSpaceName);
                        writer.setDefaultNamespace(nameSpaceName);
                    }
                }
            } else {
                writer.writeStartElement(this.getLocalName());
                //        throw new OMException(
                //                "Non namespace qualified elements are not allowed");
            }
        } else {
            writer.writeStartElement(this.getLocalName());
            //    throw new OMException(
            //            "Non namespace qualified elements are not allowed");
        }
        
        // add the elements attribute "href"
                serializeAttribute(this.attribute, writer);
        
        // add the namespace
                serializeNamespace(this.ns, writer);
       
    }
    
    /**
     * Method serializeAttribute
     * 
     * @param attr
     * @param writer
     * @throws XMLStreamException
     */
    static void serializeAttribute(OMAttribute attr, XMLStreamWriter writer)
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
    static void serializeNamespace(OMNamespace namespace, XMLStreamWriter writer)
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

    /* (non-Javadoc)
     * @see org.apache.axis.om.OMNode#discard()
     */
    public void discard() throws OMException {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.apache.axis.om.OMNode#serializeWithCache(javax.xml.stream.XMLStreamWriter)
     */
    public void serializeWithCache(XMLStreamWriter writer) throws XMLStreamException {
        // TODO Auto-generated method stub
        
    }

  

    /* (non-Javadoc)
     * @see org.apache.axis.om.OMNode#build()
     */
    public void build() {
        // TODO Auto-generated method stub
        
    }
    
}