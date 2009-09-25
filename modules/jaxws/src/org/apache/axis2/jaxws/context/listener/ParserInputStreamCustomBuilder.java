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
package org.apache.axis2.jaxws.context.listener;

import java.io.InputStream;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMContainer;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMSourcedElement;
import org.apache.axiom.om.ds.ParserInputStreamDataSource;
import org.apache.axiom.om.impl.builder.CustomBuilder;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axis2.jaxws.message.databinding.ParsedEntityReader;
import org.apache.axis2.jaxws.message.factory.ParsedEntityReaderFactory;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
/**
 * A custom builder to that does the following:
 * 1) Uses the parser to fetch the inputStream if parser supports reading of element contents
 * 2) Use the inputStream to create a DataSource backed by the InputStream read from Parser.
 * 3) Use the OMFactory to create OMSourcedElement, OMSourcedElement is backed by ParsedEntityDataSource.
 */

public class ParserInputStreamCustomBuilder implements CustomBuilder {
    private static final Log log = 
        LogFactory.getLog(ParserInputStreamCustomBuilder.class);

    private String encoding = null;

    /**
     * Constructor
     * @param encoding 
     */
    public ParserInputStreamCustomBuilder(String encoding) {
        this.encoding = (encoding == null) ? "utf-8" :encoding;
    }
    /*
     * (non-Javadoc)
     * @see org.apache.axiom.om.impl.builder.CustomBuilder#create(java.lang.String, java.lang.String, org.apache.axiom.om.OMContainer, javax.xml.stream.XMLStreamReader, org.apache.axiom.om.OMFactory)
     */
    public OMElement create(String namespace, 
        String localPart, 
        OMContainer parent,
        XMLStreamReader reader, 
        OMFactory factory) throws OMException {

        if (log.isDebugEnabled()) {
            log.debug("create namespace = " + namespace);
            log.debug("  localPart = " + localPart);
            log.debug("  reader = " + reader.getClass());
        }
        /*
         * 1) Use the the parser to fetch the inputStream
         * 2) Use the inputStream to create a DataSource, delay reading of content as much as you can.
         * 3) Use the OMFactory to create OMSourcedElement, OMSourcedElement is backed by ParsedEntityDataSource.
         */
        try{
            ParsedEntityReaderFactory perf = (ParsedEntityReaderFactory)FactoryRegistry.getFactory(ParsedEntityReaderFactory.class);
            ParsedEntityReader entityReader = perf.getParsedEntityReader();
            //Do not user custom builder if Parser does not have ability to read sub content.
            if(!entityReader.isParsedEntityStreamAvailable()){
                return null;
            }
            // Create an OMSourcedElement backed by the ParsedData
            InputStream parsedStream = getPayloadContent(reader, entityReader);
            if(parsedStream == null){
                //cant read content from EntityReader, returning null.
                return null;
            }
            //read the payload. Lets move the parser forward.
            if(reader.hasNext()){
                reader.next();
            }
            if(namespace == null){
                //lets look for ns in reader
                namespace = reader.getNamespaceURI();
                if(namespace == null){
                    //still cant find the namespace, just set it to "";
                    namespace = "";
                }
            }
            OMNamespace ns = factory.createOMNamespace(namespace, reader.getPrefix());
            InputStream payload = ContextListenerUtils.createPayloadElement(parsedStream, ns, localPart, parent);

            ParserInputStreamDataSource ds = new ParserInputStreamDataSource(payload, encoding);
            OMSourcedElement om = null;
            if (parent instanceof SOAPHeader && factory instanceof SOAPFactory) {
                om = ((SOAPFactory)factory).createSOAPHeaderBlock(localPart, ns, ds);
            } else {
                om = factory.createOMElement(ds, localPart, ns);
            }           
            //Add the new OMSourcedElement ot the parent
            parent.addChild(om); 
            /*
            //Lets Mark the body as complete so Serialize calls dont fetch data from parser for body content.
            if(parent instanceof SOAPBodyImpl){
                ((SOAPBodyImpl)parent).setComplete(true);
            }
            */
            return om;
        } catch (OMException e) {
            throw e;
        } catch (Throwable t) {
            throw new OMException(t);
        }
    }

    public OMElement create(String namespace, 
        String localPart, 
        OMContainer parent,
        XMLStreamReader reader, 
        OMFactory factory,
        InputStream payload) throws OMException {

        if (log.isDebugEnabled()) {
            log.debug("create namespace = " + namespace);
            log.debug("  localPart = " + localPart);
            log.debug("  reader = " + reader.getClass());
        }
        /*
         * 1) Use the the parser to fetch the inputStream
         * 2) Use the inputStream to create a DataSource, delay reading of content as much as you can.
         * 3) Use the OMFactory to create OMSourcedElement, OMSourcedElement is backed by ParsedEntityDataSource.
         */
        try{
            if(namespace == null){
                //lets look for ns in reader
                namespace = reader.getNamespaceURI();
                if(namespace == null){
                    //still cant find the namespace, just set it to "";
                    namespace = "";
                }
            }
            OMNamespace ns = factory.createOMNamespace(namespace, reader.getPrefix());
            ParserInputStreamDataSource ds = new ParserInputStreamDataSource(payload, encoding);
            OMSourcedElement om = null;
            if (parent instanceof SOAPHeader && factory instanceof SOAPFactory) {
                om = ((SOAPFactory)factory).createSOAPHeaderBlock(localPart, ns, ds);
            } else {
                om = factory.createOMElement(ds, localPart, ns);
            }           
            //Add the new OMSourcedElement ot the parent
            parent.addChild(om); 
            return om;
        } catch (OMException e) {
            throw e;
        } catch (Throwable t) {
            throw new OMException(t);
        }
    }

    /*
     * Read content from entityReader.
     */
    private InputStream getPayloadContent(XMLStreamReader parser, ParsedEntityReader entityReader){
        int event = parser.getEventType();
        //Make sure its start element event.
        if(log.isDebugEnabled()){
            log.debug("checking if event is START_ELEMENT");
        }
        InputStream parsedStream = null;
        if(event == XMLStreamConstants.START_ELEMENT){
            if(log.isDebugEnabled()){
                log.debug("event is START_ELEMENT");
            }
            parsedStream = entityReader.readParsedEntityStream(parser);
            if(parsedStream!=null){
                if(log.isDebugEnabled()){
                    log.debug("Read Parsed EntityStream");
                }
            }
        }
        return parsedStream;
    }
}
