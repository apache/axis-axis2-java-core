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

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.LinkedList;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axis2.om.OMText;
import org.apache.axis2.om.OMConstants;

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

    private String xmlVersion = OMConstants.DEFAULT_XML_VERSION;
    private String charSetEncoding = OMConstants.DEFAULT_CHAR_SET_ENCODING;
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
    


    public void setOutputStream(OutputStream outStream, boolean doOptimize) throws XMLStreamException,
			FactoryConfigurationError {

		this.doOptimize = doOptimize;
		this.outStream = outStream;

		if (charSetEncoding == null) //Default encoding is UTF-8
			this.charSetEncoding = OMConstants.DEFAULT_CHAR_SET_ENCODING;

		if (doOptimize) {
			bufferedSoapOutStream = new ByteArrayOutputStream();
			xmlWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(
					bufferedSoapOutStream, this.charSetEncoding);
			binaryNodeList = new LinkedList();
		} else {
			xmlWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(
					outStream, this.charSetEncoding);
		}
	}

    public void flush() throws XMLStreamException {
        xmlWriter.flush();
        if (doOptimize) {
            MIMEOutputUtils.complete(outStream, bufferedSoapOutStream,
                    binaryNodeList, getMimeBoundary(), getRootContentId(), 
					this.charSetEncoding);
        }
    }

    public boolean isOptimized() {
        return doOptimize;
    }

    public String getOptimizedContentType() {
		return MIMEOutputUtils.getContentTypeForMime(getMimeBoundary(),
				getRootContentId(), this.getCharSetEncoding());
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
			mimeBoundary = "MIMEBoundary"
					+ MIMEOutputUtils.getRandomStringOf18Characters();
		}
		return mimeBoundary;
	}

	public String getRootContentId() {
		if (rootContentId == null) {
			rootContentId = "0."
					+ MIMEOutputUtils.getRandomStringOf18Characters()
					+ "@apache.org";
		}
		return rootContentId;
	}

	public String getNextContentId() {
		nextid++;
		return nextid + "." + MIMEOutputUtils.getRandomStringOf18Characters()
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

    public boolean isIgnoreXMLDeclaration() {
        return ignoreXMLDeclaration;
    }

    public void ignoreXMLDeclaration(boolean ignoreXMLDeclaration) {
        this.ignoreXMLDeclaration = ignoreXMLDeclaration;
    }

}
