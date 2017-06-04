/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.jaxws.message.databinding.impl;

import org.apache.axiom.blob.Blobs;
import org.apache.axiom.blob.MemoryBlob;
import org.apache.axiom.om.OMDataSource;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMSourcedElement;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axis2.datasource.SourceDataSource;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.message.databinding.SourceBlock;
import org.apache.axis2.jaxws.message.factory.BlockFactory;
import org.apache.axis2.jaxws.message.impl.BlockImpl;
import org.apache.axis2.jaxws.utility.ConvertUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.bind.util.JAXBSource;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.WebServiceException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * SourceBlock
 * <p/>
 * Block containing a business object that is a javax.xml.transform.Source.
 * <p/>
 * The javax.xml.transform.Source is an interface.  The actual concrete class may be one of the
 * following: - StreamSource - DOMSource - JAXBSource - SAXSource - StAXSource
 * <p/>
 * During processing of the block, the block is free to change the representation from one source
 * to another.  (i.e. if you initially seed this with a SAXSource, but a later access may give you
 * a StAXSource).
 * <p/>
 * A Source is consumed when read.  The block will make a copy of the source if a non-consumable
 * request is made.
 */
public class SourceBlockImpl extends BlockImpl<Source,Void> implements SourceBlock {

    private static final Log log = LogFactory.getLog(SourceBlockImpl.class);

    /**
     * Constructor called from factory
     *
     * @param busObject
     * @param qName
     * @param factory
     */
    SourceBlockImpl(Source busObject, QName qName, BlockFactory factory)
            throws WebServiceException {
        super(busObject, null, qName, factory);

        // Check validity of Source
        if (busObject instanceof DOMSource ||
                busObject instanceof SAXSource ||
                busObject instanceof StreamSource ||
                busObject instanceof StAXSource ||
                busObject instanceof JAXBSource) {
            // Okay, these are supported Source objects
            if (log.isDebugEnabled()) {
                log.debug("data object is a " + busObject.getClass().getName());
            }
        } else {
            throw ExceptionFactory.makeWebServiceException(
                    Messages.getMessage("SourceNotSupported", busObject.getClass().getName()));
        }
    }


    /**
     * Constructor called from factory
     *
     * @param reader
     * @param qName
     * @param factory
     */
    public SourceBlockImpl(OMElement omElement, QName qName, BlockFactory factory) {
        super(omElement, null, qName, factory);
    }

    @Override
    protected Source _getBOFromOM(OMElement omElement, Void busContext)
        throws XMLStreamException, WebServiceException {
        Source busObject;
        
        // Shortcut to get business object from existing data source
        if (omElement instanceof OMSourcedElement) {
            OMDataSource ds = ((OMSourcedElement) omElement).getDataSource();
            if (ds instanceof SourceDataSource) {
                return ((SourceDataSource) ds).getObject();
            }
        }
        
        // If the message is a fault, there are some special gymnastics that we have to do
        // to get this working for all of the handler scenarios.  
        boolean hasFault = false;
        if ((parent != null && parent.isFault()) || 
            omElement.getQName().getLocalPart().equals(SOAP11Constants.SOAPFAULT_LOCAL_NAME)) {
            hasFault = true;
        }
        
        // Transform reader into business object
        MemoryBlob blob = Blobs.createMemoryBlob();
        OutputStream out = blob.getOutputStream();
        try {
            if (!hasFault) {
                omElement.serializeAndConsume(out);
            } else {
                omElement.serialize(out);
            }
            out.close();
        } catch (IOException ex) {
            throw new XMLStreamException(ex);
        }
        busObject = new StreamSource(blob.getInputStream());
        return busObject;
    }

    @Override
    protected XMLStreamReader _getReaderFromBO(Source busObj, Void busContext)
            throws XMLStreamException, WebServiceException {
        try {
            // TODO not sure if this is always the most performant way to do this.
            /* The following code failed in some (CTS) environments. 
	        if (busObj instanceof DOMSource) {
	            // Let's use our own DOMReader for now...
	            Element element = null;
	            
	            // Business Object msut be a Document or Element
	            Node node = ((DOMSource)busObj).getNode();
	            if(node instanceof Document){
	                element = ((Document)node).getDocumentElement();
	            }else{
	                element = (Element) ((DOMSource)busObj).getNode();
	            }
	            
	            // We had some problems with testers producing DOMSources w/o Namespaces.  
	            // It's easy to catch this here.
	            if (element.getLocalName() == null) {
	                throw new XMLStreamException(ExceptionFactory.
                           makeWebServiceException(Messages.getMessage("JAXBSourceNamespaceErr")));
	            }
	            
	            return new DOMReader(element);
	        } 
            */

            if (busObj instanceof StreamSource) {
                XMLInputFactory f = StAXUtils.getXMLInputFactory();

                return f.createXMLStreamReader(busObj);
            }
            //TODO: For GM we need to only use this approach when absolutely necessary.
            // For example, we don't want to do this if this is a (1.6) StaxSource or if the 
            // installed parser provides a better solution.
            //TODO: Uncomment this code if woodstock parser handles 
            // JAXBSource and SAXSource correctly.
            //return inputFactory.createXMLStreamReader((Source) busObj);
            return _slow_getReaderFromSource(busObj);
        } catch (Exception e) {
            String className = (busObj == null) ? "none" : busObj.getClass().getName();
            throw ExceptionFactory
                    .makeWebServiceException(Messages.getMessage("SourceReadErr", className), e);
        }
    }

    /** Creates an XMLStreamReader from a Source using a slow but proven algorithm. */
    private XMLStreamReader _slow_getReaderFromSource(Source src) throws XMLStreamException {
        if (log.isDebugEnabled()) {
            log.debug("Start _slow_getReaderFromSource");
        }
        byte[] bytes = (byte[]) ConvertUtils.convert(src, byte[].class);
        if (log.isDebugEnabled()) {
            log.debug("Successfully converted to ByteArray");
        }
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        XMLStreamReader reader = StAXUtils.createXMLStreamReader(bais);
        if (log.isDebugEnabled()) {
            log.debug("End _slow_getReaderFromSource =" + reader);
        }
        return reader;
    }

    @Override
    protected void _outputFromBO(Source busObject, Void busContext, XMLStreamWriter writer)
            throws XMLStreamException, WebServiceException {
        // There is no fast way to output the Source to a writer, so get the reader
        // and pass use the default reader->writer.
        if (log.isDebugEnabled()) {
            log.debug("Start _outputFromBO");
        }
        XMLStreamReader reader = _getReaderFromBO(busObject, busContext);
        if (log.isDebugEnabled()) {
            log.debug("Obtained reader=" + reader);
        }
        _outputFromReader(reader, writer);
        if (log.isDebugEnabled()) {
            log.debug("End _outputReaderFromBO");
        }
        // REVIEW Should we call close() on the Source ?
    }


    @Override
    protected Source _getBOFromBO(Source busObject, Void busContext, boolean consume) {
        if (consume) {
            return busObject;
        } else {
            // TODO Missing Impl
            throw ExceptionFactory.makeWebServiceException(
                    Messages.getMessage("SourceMissingSupport", busObject.getClass().getName()));
        }
    }


    @Override
    public boolean isElementData() {
        return false;  // The source could be a text or element etc.
    }

    @Override
    public void close() {
        return; // Nothing to close
    }

    @Override
    public Object getObject() {
        try {
            return getBusinessObject(false);
        } catch (XMLStreamException e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }

    @Override
    public boolean isDestructiveRead() {
        return true;
    }

    @Override
    public boolean isDestructiveWrite() {
        return true;
    }
}
