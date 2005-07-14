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
    private static String mimeBoundary = "----=_AxIs2_Def_boundary_=42214532";

    public OMOutputImpl() {
    }

    public OMOutputImpl(XMLStreamWriter xmlWriter) {
        this.xmlWriter = xmlWriter;
    }

    public OMOutputImpl(OutputStream outStream, boolean doOptimize)
            throws XMLStreamException, FactoryConfigurationError {
        setOutputStream(outStream, doOptimize);
    }

    public void setOutputStream(OutputStream outStream, boolean doOptimize)
            throws XMLStreamException, FactoryConfigurationError {
        this.doOptimize = doOptimize;
        this.outStream = outStream;
        if (doOptimize) {
            bufferedSoapOutStream = new ByteArrayOutputStream();
            xmlWriter =
                    XMLOutputFactory.newInstance().createXMLStreamWriter(
                            bufferedSoapOutStream);
            binaryNodeList = new LinkedList();
        } else {
            xmlWriter =
                    XMLOutputFactory.newInstance().createXMLStreamWriter(
                            outStream);
        }
    }

    public void flush() throws XMLStreamException {
        xmlWriter.flush();
        if (doOptimize) {
            MIMEOutputUtils.complete(outStream, bufferedSoapOutStream,
                    binaryNodeList, mimeBoundary);
        }
    }

    public boolean isOptimized() {
        return doOptimize;
    }

    public String getOptimizedContentType() {
        return org.apache.axis2.om.impl.MIMEOutputUtils.getContentTypeForMime(mimeBoundary);
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
}
