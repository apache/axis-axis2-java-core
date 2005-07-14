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

import org.apache.axis2.om.impl.MIMEOutputUtils;
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

    /**
     * @param xmlWriter if it is guaranteed for not using attachments one can use this
     */
    public OMOutputImpl(XMLStreamWriter xmlWriter) {
        this.xmlWriter = xmlWriter;
    }

    /**
     * @throws FactoryConfigurationError
     * @throws XMLStreamException
     */
    public OMOutputImpl(OutputStream outStream, boolean doOptimise)
            throws XMLStreamException, FactoryConfigurationError {
        this.doOptimize = doOptimise;
        this.outStream = outStream;
        if (doOptimise) {
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

    public XMLStreamWriter getXmlStreamWriter() {
        return xmlWriter;
    }

    public void flush() throws XMLStreamException {
        xmlWriter.flush();
        if (doOptimize) {
            MIMEOutputUtils.complete(outStream, bufferedSoapOutStream,
                    binaryNodeList, mimeBoundary);
        }
    }

    public boolean doOptimise() {
        return doOptimize;
    }

    public static String getContentType(boolean doOptimize) {
        if (doOptimize) {
            return org.apache.axis2.om.impl.MIMEOutputUtils.getContentTypeForMime(mimeBoundary);
        }
        //TODO have to check whether SOAP1.1 & SOAP 1.2
        return null;
    }

    public void writeOptimized(OMText node) {
        binaryNodeList.add(node);
    }

    public void complete() throws XMLStreamException {
    }
}
