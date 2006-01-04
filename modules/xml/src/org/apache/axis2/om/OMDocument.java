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

import javax.xml.stream.XMLStreamException;
import java.io.OutputStream;


public interface OMDocument extends OMContainer {

    /**
     * Field XML_10 XML Version 1.0
     */
    public final static String XML_10 = "1.0";

    /**
     * Field XML_11 XML Version 1.1
     */
    public final static String XML_11 = "1.1";

    /**
     * Returns the document element.
     * @return Returns OMElement.
     */
    public OMElement getOMDocumentElement();

    /**
     * Sets the document element of the XML document.
     * @param rootElement
     */
    public void setOMDocumentElement(OMElement rootElement);

    /**
     * Returns the XML version.
     * @return Returns String.
     */
    public String getXMLVersion();

    /**
     * Sets the XML version.
     * @see org.apache.axis2.om.impl.llom.OMDocumentImpl#XML_10 XML 1.0
     * @see org.apache.axis2.om.impl.llom.OMDocumentImpl#XML_11 XML 1.1
     * @param version
     */
    public void setXMLVersion(String version);

    /**
     * Returns the character set encoding scheme.
     * @return Returns String.
     */
    public String getCharsetEncoding();

    /**
     * Sets the character set encoding scheme to be used.
     * @param charsetEncoding
     */
    public void setCharsetEncoding(String charsetEncoding);

    /**
     * XML standalone value.
     * This will be yes, no or null (if not available)
     * @return Returns boolean.
     */
    public String isStandalone();
    public void setStandalone(String isStandalone);

    /**
     * Serializes the OMDocument.
     * @param output
     * @param format
     * @throws XMLStreamException
     */
    public void serializeAndConsume(OutputStream output, OMOutputFormat format) throws XMLStreamException;

    /**
     * Builds the OM node/tree and then serializes the document.
     * @param output
     * @param format
     * @throws XMLStreamException
     */
    public void serialize(OutputStream output, OMOutputFormat format) throws XMLStreamException;

    /**
     * Serializes the OMDocument.
     * @param output
     * @throws XMLStreamException
     */
    public void serializeAndConsume(OutputStream output) throws XMLStreamException;

    /**
     * Serializes the document with cache on.
     * @param output
     * @throws XMLStreamException
     */
    public void serialize(OutputStream output) throws XMLStreamException;
}
