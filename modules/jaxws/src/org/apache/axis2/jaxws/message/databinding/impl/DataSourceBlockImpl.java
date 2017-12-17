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

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMSourcedElement;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axis2.builder.DataSourceBuilder;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.message.databinding.DataSourceBlock;
import org.apache.axis2.jaxws.message.factory.BlockFactory;
import org.apache.axis2.jaxws.message.impl.BlockImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.activation.DataSource;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.ws.WebServiceException;
import java.io.ByteArrayOutputStream;

/**
 * SourceBlock
 * <p/>
 * Block containing a business object that is a javax.activation.DataSource
 * <p/>
 */
public class DataSourceBlockImpl extends BlockImpl<DataSource,Void> implements DataSourceBlock {

    private static final Log log = LogFactory.getLog(DataSourceBlockImpl.class);

    /**
     * Constructor called from factory
     *
     * @param busObject
     * @param qName
     * @param factory
     */
    DataSourceBlockImpl(DataSource busObject, QName qName, BlockFactory factory)
            throws WebServiceException {
        super(busObject, null, qName, factory);
    }


    /**
     * Constructor called from factory
     *
     * @param reader
     * @param qName
     * @param factory
     */
    public DataSourceBlockImpl(OMElement omElement, QName qName, BlockFactory factory) {
        super(omElement, null, qName, factory);
    }

    @Override
    public OMElement getOMElement() throws XMLStreamException, WebServiceException {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace ns = factory.createOMNamespace("", "");
        return factory.createOMElement(this, "dummy", ns);
    }

    @Override
    protected DataSource _getBOFromOM(OMElement omElement, Void busContext)
        throws XMLStreamException, WebServiceException {
        DataSource busObject;
        
        // Shortcut to get business object from existing data source
        // TODO: incorrect (wrong type)
//        if (omElement instanceof OMSourcedElement) {
//            OMDataSource ds = ((OMSourcedElement) omElement).getDataSource();
//            if (ds instanceof SourceDataSource) {
//                return ((SourceDataSource) ds).getObject();
//            }
//        }
        
        // If the message is a fault, there are some special gymnastics that we have to do
        // to get this working for all of the handler scenarios.  
        boolean hasFault = false;
        if ((parent != null && parent.isFault()) || 
            omElement.getQName().getLocalPart().equals(SOAP11Constants.SOAPFAULT_LOCAL_NAME)) {
            hasFault = true;
        }
        
        // Transform reader into business object
        if (!hasFault) {
            busObject = (DataSourceBuilder.ByteArrayDataSourceEx)((OMSourcedElement)omElement).getDataSource();
        }
        else {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            omElement.serialize(baos);
            busObject = new ByteArrayDataSource(baos.toByteArray(), "UTF-8");
        }
        return busObject;
    }

    @Override
    protected XMLStreamReader _getReaderFromBO(DataSource busObj, Void busContext)
            throws XMLStreamException, WebServiceException {
        try {
            return StAXUtils.createXMLStreamReader(busObj.getInputStream());
        } catch (Exception e) {
            String className = (busObj == null) ? "none" : busObj.getClass().getName();
            throw ExceptionFactory
                    .makeWebServiceException(Messages.getMessage("SourceReadErr", className), e);
        }
    }
    
    @Override
    protected void _outputFromBO(DataSource busObject, Void busContext, XMLStreamWriter writer)
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
    protected DataSource _getBOFromBO(DataSource busObject, Void busContext, boolean consume) {
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