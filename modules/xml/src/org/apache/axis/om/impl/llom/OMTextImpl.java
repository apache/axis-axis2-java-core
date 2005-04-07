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
package org.apache.axis.om.impl.llom;

import org.apache.axis.om.OMConstants;
import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMException;
import org.apache.axis.om.OMNode;
import org.apache.axis.om.OMText;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * Class OMTextImpl
 */
public class OMTextImpl extends OMNodeImpl implements OMText, OMConstants {
    /**
     * Field textType
     */
    protected short textType = TEXT_NODE;

    /**
     * Constructor OMTextImpl
     *
     * @param parent
     * @param text
     */
    public OMTextImpl(OMElement parent, String text) {
        super(parent);
        setValue(text);
        done = true;
    }

    /**
     * Constructor OMTextImpl
     *
     * @param s
     */
    public OMTextImpl(String s) {
        setValue(s);
    }

    /**
     * We use the OMText class to hold comments, text, characterData, CData, etc.,
     * The codes are found in OMNode class
     *
     * @param type
     */
    public void setTextType(short type) {
        if ((type == TEXT_NODE) || (type == COMMENT_NODE)
                || (type == CDATA_SECTION_NODE)) {
            this.textType = type;
        } else {
            throw new UnsupportedOperationException(
                    "Attempt to set wrong type");
        }
    }

    /**
     * Method getTextType
     *
     * @return
     */
    public short getTextType() {
        return textType;
    }

    /**
     * Method getFirstChild
     *
     * @return
     * @throws OMException
     */
    public OMNode getFirstChild() throws OMException {
        throw new UnsupportedOperationException();
    }

    /**
     * Method setFirstChild
     *
     * @param node
     * @throws OMException
     */
    public void setFirstChild(OMNode node) throws OMException {
        throw new UnsupportedOperationException();
    }

    /**
     * @return
     * @throws org.apache.axis.om.OMException
     * @throws OMException
     */
    public short getType() throws OMException {
        return textType;
    }

    /**
     * @param writer
     * @param cache
     * @throws XMLStreamException
     */
    public void serialize(XMLStreamWriter writer, boolean cache)
            throws XMLStreamException {
        if (textType == TEXT_NODE) {
            writer.writeCharacters(this.value);
        } else if (textType == COMMENT_NODE) {
            writer.writeComment(this.value);
        } else if (textType == CDATA_SECTION_NODE) {
            writer.writeCData(this.value);
        }
        OMNode nextSibling = this.getNextSibling();
        if (nextSibling != null) {
            nextSibling.serialize(writer, cache);
        }
    }

    public void serialize(XMLStreamWriter writer) throws XMLStreamException {
        this.serialize(writer, false);
    }
}
