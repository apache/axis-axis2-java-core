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
package org.apache.axis2.om;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.LinkedList;

/**
 * @author <a href="mailto:thilina@opensource.lk">Thilina Gunarathne </a> For
 *         the moment this assumes that transport takes the decision of whether
 *         to optimise or not by looking at whether the MTOM optimise is enabled &
 *         also looking at the OM tree whether it has any optimisable content
 */

public class OMOutput {
    private XMLStreamWriter xmlWriter;

    private boolean doOptimise;

    private OutputStream outStream;

    private XMLStreamWriter writer;

    private LinkedList binaryNodeList;

    private ByteArrayOutputStream bufferedSoapOutStream;

    private static String mimeBoundary = "----=_AxIs2_Def_boundary_=42214532";

    //private String contentType = null;

    /**
     * @param xmlWriter if it is guaranteed for not using attachments one can use this
     */
    public OMOutput(XMLStreamWriter xmlWriter) {
        this.xmlWriter = xmlWriter;
    }

    /**
     * @throws FactoryConfigurationError
     * @throws XMLStreamException
     */
    public OMOutput(OutputStream outStream, boolean doOptimise)
            throws XMLStreamException, FactoryConfigurationError {
        this.doOptimise = doOptimise;
        this.outStream = outStream;
        if (doOptimise) {
            bufferedSoapOutStream = new ByteArrayOutputStream();
            xmlWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(bufferedSoapOutStream);
            binaryNodeList = new LinkedList();
        } else {
            xmlWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(outStream);

        }

    }

    public XMLStreamWriter getXmlStreamWriter() {
        return xmlWriter;
    }

    public void flush() throws XMLStreamException {
        //		if (doOptimise) {
        //			try {
        //				this.complete();
        //			} catch (IOException e) {
        //				//TODO this is just a hack to avoid passing IOException. Must find a
        // better way to handle this
        //				throw new XMLStreamException("Error creating mime parts. Problem with
        // Streams");
        //			} catch (MessagingException e) {
        //				throw new XMLStreamException("Error creating mime Body parts");
        //			}
        //		} else {
        xmlWriter.flush();
        //		}

    }

    public boolean doOptimise() {
        return doOptimise;
    }

    public static String getContentType(boolean doOptimize) {
        if (doOptimize) {
            return MIMEOutputUtils.getContentTypeForMime(mimeBoundary);
        }
        //TODO have to check whether SOAP1.1 & SOAP 1.2
        return null;
    }

    public void writeOptimised(OMText node) {
        binaryNodeList.add(node);
    }

    public void complete() throws XMLStreamException {
        if (doOptimise) {
            xmlWriter.flush();
            MIMEOutputUtils.complete(outStream, bufferedSoapOutStream,
                                     binaryNodeList, mimeBoundary);
        }
    }

    /*private String getMimeBoundary() {
        //TODO have to dynamically generate.
        if (mimeBoundary == null) {
            mimeBoundary = "----=_AxIs2_Def_boundary_=42214532";
        }
        return mimeBoundary;
    }*/

}
