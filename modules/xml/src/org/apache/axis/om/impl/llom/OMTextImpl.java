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

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axis.om.OMConstants;
import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMException;
import org.apache.axis.om.OMNode;
import org.apache.axis.om.OMText;

/**
 * Class OMTextImpl
 */
public class OMTextImpl extends OMNodeImpl implements OMText, OMConstants {

    protected String value;
    protected short textType = TEXT_NODE;

    /**
     * Constructor OMTextImpl
     *
     * @param parent
     * @param text
     */
    public OMTextImpl(OMElement parent, String text) {
        super(parent);
        this.value = text;
        done = true;
    }

    /**
     * Constructor OMTextImpl
     *
     * @param s
     */
    public OMTextImpl(String s) {
        this.value = s;
    }



    /**
     * @return
     * @throws org.apache.axis.om.OMException
     * @throws OMException
     */
    public int getType() throws OMException {
        return textType;
    }

    /**
     * @param writer
     * @throws XMLStreamException
     */
    public void serializeWithCache(XMLStreamWriter writer)
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
            nextSibling.serializeWithCache(writer);
        }
    }

    public void serialize(XMLStreamWriter writer) throws XMLStreamException {
        this.serializeWithCache(writer);
    }

    /**
     * Slightly different implementation of the discard method 
     * @throws OMException
     */
    public void discard() throws OMException {
          if (done){
              this.detach();
          }else{
              builder.discard(this.parent);
          }
    }

    /**
     * Returns the value
     * @return
     */
    public String getText() {
        return this.value;
    }

    public boolean isOptimized() {
        return false;  //Todo
    }
}
