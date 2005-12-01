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

package org.apache.axis2.om.impl;

import org.apache.axis2.om.OMText;
import org.apache.axis2.om.OMOutputFormat;
import org.apache.axis2.soap.SOAP11Constants;
import org.apache.axis2.soap.SOAP12Constants;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.LinkedList;


/**
 * For the moment this assumes that transport takes the decision of whether
 * to optimise or not by looking at whether the MTOM optimise is enabled &
 * also looking at the OM tree whether it has any optimisable content
 */
public class OMOutputImpl {
    private XMLStreamWriter xmlWriter;
    private OutputStream outStream;
    private LinkedList binaryNodeList;
    private ByteArrayOutputStream bufferedSoapOutStream;
    private OMOutputFormat format = new OMOutputFormat();

    public OMOutputImpl() {
    }

    public OMOutputImpl(XMLStreamWriter xmlWriter) {
        this.xmlWriter = xmlWriter;
    }

    /**
     * This creates a new OMOutputImpl with default encoding
     *
     * @param outStream
     * @param doOptimize
     * @throws XMLStreamException
     * @throws FactoryConfigurationError
     * @see OMOutputFormat#DEFAULT_CHAR_SET_ENCODING
     */
    public OMOutputImpl(OutputStream outStream, boolean doOptimize)
            throws XMLStreamException, FactoryConfigurationError {
        setOutputStream(outStream, doOptimize);
    }

    public void setOutputStream(OutputStream outStream, boolean doOptimize)
            throws XMLStreamException, FactoryConfigurationError {

        format.setDoOptimize(doOptimize);
        this.outStream = outStream;

        if (format.getCharSetEncoding() == null) //Default encoding is UTF-8
            format.setCharSetEncoding(OMOutputFormat.DEFAULT_CHAR_SET_ENCODING);

        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        //factory.setProperty("javax.xml.stream.isRepairingNamespaces", Boolean.TRUE);
        if (doOptimize) {
            bufferedSoapOutStream = new ByteArrayOutputStream();
            xmlWriter = factory.createXMLStreamWriter(bufferedSoapOutStream,
                    format.getCharSetEncoding());
            binaryNodeList = new LinkedList();
        } else {
            xmlWriter = factory.createXMLStreamWriter(outStream,
                    format.getCharSetEncoding());
        }
    }

    public void flush() throws XMLStreamException {
        xmlWriter.flush();
        String SOAPContentType;
        if (format.isOptimized()) {
            if (format.isSOAP11()) {
                SOAPContentType = SOAP11Constants.SOAP_11_CONTENT_TYPE;
            } else {
                SOAPContentType = SOAP12Constants.SOAP_12_CONTENT_TYPE;
            }
            MIMEOutputUtils.complete(
                    outStream,
                    bufferedSoapOutStream,
                    binaryNodeList,
                    getMimeBoundary(),
                    getRootContentId(),
                    format.getCharSetEncoding(), SOAPContentType);
        }
    }

    public boolean isOptimized() {
        return format.isOptimized();
    }

    public String getContentType() {
        String SOAPContentType;
        if (isOptimized()) {
            if (format.isSOAP11()) {
                SOAPContentType = SOAP11Constants.SOAP_11_CONTENT_TYPE;
            } else {
                SOAPContentType = SOAP12Constants.SOAP_12_CONTENT_TYPE;
            }
            return MIMEOutputUtils.getContentTypeForMime(
                    getMimeBoundary(),
                    getRootContentId(),
                    this.getCharSetEncoding(), SOAPContentType);
        } else {
            if (!format.isSOAP11()) {
                return SOAP12Constants.SOAP_12_CONTENT_TYPE;
            } else {
                return SOAP11Constants.SOAP_11_CONTENT_TYPE;
            }
        }
    }

    public void writeOptimized(OMText node) {
        binaryNodeList.add(node);
    }

    public void setXmlStreamWriter(XMLStreamWriter xmlWriter) {
        this.xmlWriter = xmlWriter;
    }

    public XMLStreamWriter getXmlStreamWriter() {
        return xmlWriter;
    }

    public String getMimeBoundary() {
        return format.getMimeBoundary();
    }

    public String getRootContentId() {
        return format.getRootContentId();
    }

    public String getNextContentId() {
        return format.getNextContentId();
    }

    /**
     * Returns the character set endocing scheme If the value of the
     * charSetEncoding is not set then the default will be returned
     *
     * @return encoding
     */
    public String getCharSetEncoding() {
        return format.getCharSetEncoding();
    }

    public void setCharSetEncoding(String charSetEncoding) {
        format.setCharSetEncoding(charSetEncoding);
    }

    public String getXmlVersion() {
        return format.getXmlVersion();
    }

    public void setXmlVersion(String xmlVersion) {
        format.setXmlVersion(xmlVersion);
    }

    /**
     * @param b
     */
    public void setSoap11(boolean b) {
        format.setSSOAP11(b);
    }

    public boolean isIgnoreXMLDeclaration() {
        return format.isIgnoreXMLDeclaration();
    }

    public void setIgnoreXMLDeclaration(boolean ignoreXMLDeclaration) {
        format.setIgnoreXMLDeclaration(ignoreXMLDeclaration);
    }


    /**
     * @param b
     */
    public void setDoOptimize(boolean b) {
        format.setDoOptimize(b);
    }
    
    public OMOutputFormat getOutputFormat() {
        return format;
    }
    
    public void setOutputFormat(OMOutputFormat format) {
        this.format = format;        
    }
}
