package org.apache.axis2.databinding;

import org.apache.axiom.om.OMDataSource;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.om.util.StAXUtils;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.namespace.QName;
import java.io.OutputStream;
import java.io.Writer;
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

public abstract class ADBDataSource implements OMDataSource {
    protected QName parentQName;
    private ADBBean bean;

    /**
     * Constructor taking in an ADBBean
     * @param bean
     */
    protected ADBDataSource(ADBBean bean,QName parentQName) {
        this.bean = bean;
        this.parentQName = parentQName;
    }


    /**
     * @see OMDataSource#serialize(java.io.OutputStream, org.apache.axiom.om.OMOutputFormat)
     * @param output
     * @param format
     * @throws XMLStreamException
     */
    public void serialize(OutputStream output, OMOutputFormat format) throws XMLStreamException {
        XMLStreamWriter xmlStreamWriter = StAXUtils.createXMLStreamWriter(output);
        serialize(xmlStreamWriter);
        xmlStreamWriter.flush();
    }

    /**
     * @see OMDataSource#serialize(java.io.Writer, org.apache.axiom.om.OMOutputFormat)
     * @param writer
     * @param format
     * @throws XMLStreamException
     */
    public void serialize(Writer writer, OMOutputFormat format) throws XMLStreamException {
        serialize(StAXUtils.createXMLStreamWriter(writer));
    }

    /**
     * This needs to be generated inside the ADB bean
     * @see OMDataSource#serialize(javax.xml.stream.XMLStreamWriter)
     * @param xmlWriter
     * @throws XMLStreamException
     */
    public abstract void serialize(XMLStreamWriter xmlWriter)
            throws XMLStreamException;


    /**
     * @see org.apache.axiom.om.OMDataSource#getReader()
     * @throws XMLStreamException
     */
    public XMLStreamReader getReader() throws XMLStreamException {
        // since only ADBBeans related to elements can be serialized
        // we are safe in passing null here. 
        return bean.getPullParser(parentQName);
    }

}
