package org.apache.axis.impl.llom;

import org.apache.axis.om.*;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.Stack;

/**
 * Copyright 2001-2004 The Apache Software Foundation.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p/>
 */
public class OMTextImpl extends OMNodeImpl implements OMText, OMConstants {

    protected short textType = TEXT_NODE;
    
	public OMTextImpl(){}
    

    public void init(OMElement parent, String text) {
        super.init(parent);
        setValue(text);
        done = true;
    }

    public void init(String s) {
        setValue(s);
    }

    /**
     * We use the OMText class to hold comments, text, characterData, CData, etc.,
     * The codes are found in OMNode class
     *
     * @param type
     */
    public void setTextType(short type) {
        if (type == TEXT_NODE || type == COMMENT_NODE || type == CDATA_SECTION_NODE)
            this.textType = type;
        else
            throw new UnsupportedOperationException("Attempt to set wrong type");
    }

    public short getTextType() {
        return textType;
    }

    public OMNode getFirstChild() throws OMException {
        throw new UnsupportedOperationException();
    }

    public void setFirstChild(OMNode node) throws OMException {
        throw new UnsupportedOperationException();
    }

    /**
     * @return
     * @throws org.apache.axis.om.OMException
     */
    public short getType() throws OMException {
        return textType;
    }

    /**
     * @param writer
     * @param cache
     * @param namespacePrefixStack
     * @throws XMLStreamException
     */
    public void serialize(XMLStreamWriter writer, boolean cache, Stack namespacePrefixStack) throws XMLStreamException {
        boolean isFirst = false;
        if (namespacePrefixStack == null) {
            isFirst = true;
        }
        if (textType == TEXT_NODE)
            writer.writeCharacters(this.value);
        else if (textType == COMMENT_NODE)
            writer.writeComment(this.value);
        else if (textType == CDATA_SECTION_NODE) {
            writer.writeCData(this.value);
        }
        //In this case we have to advance the parser (even when not cached) upto the
        //next *element*. You cannot ask for a pull parser from the text node!!!
        //by advancing the parser
        if (!isFirst) {
            OMNode nextSibling = this.getNextSibling();
            if (nextSibling != null) {
                nextSibling.serialize(writer, cache, namespacePrefixStack);
            }
        }
    }
}
