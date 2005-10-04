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

package org.apache.axis2.om.impl.llom;

import org.apache.axis2.om.OMContainer;
import org.apache.axis2.om.OMDocType;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMException;
import org.apache.axis2.om.OMNode;
import org.apache.axis2.om.impl.OMOutputImpl;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class OMDocTypeImpl extends OMNodeImpl implements OMDocType {
    protected String value;

    /**
     * Constructor OMDocTypeImpl
     *
     * @param parentNode
     * @param contentText
     */
    public OMDocTypeImpl(OMContainer parentNode, String contentText) {
        super(parentNode);
        this.value = contentText;
        nodeType = OMNode.DTD_NODE;
    }

    /**
     * Constructor OMDocTypeImpl
     *
     * @param parentNode
     */
    public OMDocTypeImpl(OMContainer parentNode) {
        this(parentNode, null);
    }

    /**
     * Serialize the node with caching
     *
     * @param omOutput
     * @throws XMLStreamException
     * @see #serialize(org.apache.axis2.om.impl.OMOutputImpl)
     */
    public void serialize(OMOutputImpl omOutput) throws XMLStreamException {
        XMLStreamWriter writer = omOutput.getXmlStreamWriter();
        writer.writeDTD(this.value);
    }

    /**
     * Serialize the node without caching
     *
     * @param omOutput
     * @throws XMLStreamException
     * @see #serializeAndConsume(org.apache.axis2.om.impl.OMOutputImpl)
     */
    public void serializeAndConsume(org.apache.axis2.om.impl.OMOutputImpl omOutput) throws XMLStreamException {
        serialize(omOutput);
    }

    /**
     * get the value of this DocType
     *
     * @return string
     */
    public String getValue() {
        return value;
    }

    /**
     * set the value of this DocType
     *
     * @param text
     */
    public void setValue(String text) {
        this.value = text;
    }

    /**
     * discard this node
     *
     * @throws OMException
     */
    public void discard() throws OMException {
        if (done) {
            this.detach();
        } else {
            builder.discard((OMElement) this.parent);
        }
    }
}
