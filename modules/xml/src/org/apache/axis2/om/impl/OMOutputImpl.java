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
    private boolean doOptimize;
    private OutputStream outStream;
    private LinkedList binaryNodeList;
    private ByteArrayOutputStream bufferedSoapOutStream;
    private String mimeBoundary = null;
    private String rootContentId = null;
    private int nextid = 0;
    private boolean isSoap11 = true;

    /**
     * Field DEFAULT_CHAR_SET_ENCODING specifies the default 
     * character encoding scheme to be used
     */
    public static final String DEFAULT_CHAR_SET_ENCODING = "utf-8";

    private String charSetEncoding;
    private String xmlVersion;
    private boolean ignoreXMLDeclaration = false;


    public OMOutputImpl() {
    }

    public OMOutputImpl(XMLStreamWriter xmlWriter) {
        this.xmlWriter = xmlWriter;
    }

    /**
     * This creates a new OMOutputImpl with default encoding
     * @see OMOutputImpl#DEFAULT_CHAR_SET_ENCODING
     * @param outStream
     * @param doOptimize
     * @throws XMLStreamException
     * @throws FactoryConfigurationError
     */
    public OMOutputImpl(OutputStream outStream, boolean doOptimize)
        throws XMLStreamException, FactoryConfigurationError {
        setOutputStream(outStream, doOptimize);
    }

    public void setOutputStream(OutputStream outStream, boolean doOptimize)
        throws XMLStreamException, FactoryConfigurationError {

        this.doOptimize = doOptimize;
        this.outStream = outStream;

        if (charSetEncoding == null) //Default encoding is UTF-8
            this.charSetEncoding = DEFAULT_CHAR_SET_ENCODING;

        if (doOptimize) {
            bufferedSoapOutStream = new ByteArrayOutputStream();
            xmlWriter =
                XMLOutputFactory.newInstance().createXMLStreamWriter(
                    bufferedSoapOutStream,
                    this.charSetEncoding);
            binaryNodeList = new LinkedList();
        } else {
            xmlWriter =
                XMLOutputFactory.newInstance().createXMLStreamWriter(
                    outStream,
                    this.charSetEncoding);
        }
    }

    public void flush() throws XMLStreamException {
        xmlWriter.flush();
        String SOAPContentType;
        if (doOptimize) {
            if (isSoap11)
            {
                SOAPContentType = SOAP11Constants.SOAP_11_CONTENT_TYPE;
            }
            else
            {
                SOAPContentType = SOAP12Constants.SOAP_12_CONTENT_TYPE;
            }
            MIMEOutputUtils.complete(
                outStream,
                bufferedSoapOutStream,
                binaryNodeList,
                getMimeBoundary(),
                getRootContentId(),
                this.charSetEncoding,SOAPContentType);
        }
    }

    public boolean isOptimized() {
        return doOptimize;
    }

    public String getContentType() {
        String SOAPContentType;
        if (isOptimized()) {
            if (isSoap11)
            {
                SOAPContentType = SOAP11Constants.SOAP_11_CONTENT_TYPE;
            }
            else
            {
                SOAPContentType = SOAP12Constants.SOAP_12_CONTENT_TYPE;
            }
            return MIMEOutputUtils.getContentTypeForMime(
                getMimeBoundary(),
                getRootContentId(),
                this.getCharSetEncoding(),SOAPContentType);
        } else {

            StringBuffer buf = new StringBuffer();
            if (!isSoap11) {
                buf.append(SOAP12Constants.SOAP_12_CONTENT_TYPE);
                buf.append("; charset=" + this.getCharSetEncoding() + ";");
            } else {
                buf.append(SOAP11Constants.SOAP_11_CONTENT_TYPE)
                    .append("; charset=" + this.getCharSetEncoding() + ";");
            }
            return buf.toString();
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
        if (mimeBoundary == null) {
            mimeBoundary =
                "MIMEBoundary"
                    + MIMEOutputUtils.getRandomStringOf18Characters();
        }
        return mimeBoundary;
    }

    public String getRootContentId() {
        if (rootContentId == null) {
            rootContentId =
                "0."
                    + MIMEOutputUtils.getRandomStringOf18Characters()
                    + "@apache.org";
        }
        return rootContentId;
    }

    public String getNextContentId() {
        nextid++;
        return nextid
            + "."
            + MIMEOutputUtils.getRandomStringOf18Characters()
            + "@apache.org";
    }

    /**
     * Returns the character set endocing scheme If the value of the
     * charSetEncoding is not set then the default will be returned
     * 
     * @return
     */
    public String getCharSetEncoding() {
        return this.charSetEncoding;
    }

    public void setCharSetEncoding(String charSetEncoding) {
        this.charSetEncoding = charSetEncoding;
    }

    public String getXmlVersion() {
        return xmlVersion;
    }

    public void setXmlVersion(String xmlVersion) {
        this.xmlVersion = xmlVersion;
    }

    /**
     * @param b
     */
    public void setSoap11(boolean b) {
        isSoap11 = b;
    }

    public boolean isIgnoreXMLDeclaration() {
        return ignoreXMLDeclaration;
    }

    public void ignoreXMLDeclaration(boolean ignoreXMLDeclaration) {
        this.ignoreXMLDeclaration = ignoreXMLDeclaration;
    }


    /**
     * @param b
     */
    public void setDoOptimize(boolean b) {
        doOptimize = b;
    }
}
