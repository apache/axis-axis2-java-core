package org.apache.axis.impl.llom.serialize;

import org.apache.axis.impl.llom.exception.OMStreamingException;
import org.apache.axis.om.*;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Iterator;

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
 */
public class SimpleOMSerializer {
//determines whether the seriliased output contains new line characters
    private boolean newLines = true;

    public void setNewLines(boolean newLines) {
        this.newLines = newLines;
    }

    public void serialize(Object node, OutputStream stream) {

        String output = serialize(node);
        OutputStreamWriter streamWriter = new OutputStreamWriter(stream);
        BufferedWriter writer = new BufferedWriter(streamWriter);

        try {
            writer.write(output);
            writer.flush();

        } catch (IOException e) {
            throw new OMStreamingException(e);
        }
    }

    private String serialize(Object o) {
        String returnString = "";
        OMNode node = (OMNode) o;
        short nodeType = node.getType();
        if (nodeType == OMNode.ELEMENT_NODE) {
            returnString = serializeElement((OMElement) node);
//        }else if (nodeType == OMNode.DOCUMENT_NODE){
//            returnString = serializeDocment((OMMessage)node);
        } else if (nodeType == OMNode.ATTRIBUTE_NODE) {
            returnString = serializeAttribute((OMAttribute) node);
        } else if (nodeType == OMNode.TEXT_NODE || nodeType == OMNode.COMMENT_NODE || nodeType == OMNode.CDATA_SECTION_NODE) {
            returnString = serializeText((OMText) node);
        }
        return returnString;
    }

//    private String serializeDocment(OMMessage doc){
//        return serializeElement(doc.getEnvelope());
//    }

    /**
     * @param element
     * @return
     */
    private String serializeElement(OMElement element) {

        //flag to say whther this element is prefixed or not
        boolean prefixed = false;
        String prefix = "";

        //first serialize the element itself
        String returnText = "<";

        //add the namespace prefix
        OMNamespace ns = element.getNamespace();
        if (ns != null) {
            //add the prefix if it's availble
            prefix = ns.getPrefix();
            if (prefix != null) {
                returnText = returnText + prefix + ":";
                prefixed = true;
            }
        }
        //add the local name
        returnText = returnText + element.getLocalName();

        //add the elements attributes
        Iterator attributes = element.getAttributes();
        while (attributes.hasNext()) {
            returnText = returnText + " " + serializeAttribute((OMAttribute) attributes.next());
        }

        //add the namespaces
        returnText = returnText + " " + serializeNamespace(element.getNamespace());

        returnText = returnText + ">";
        //add the children
        Iterator children = element.getChildren();
        while (children.hasNext()) {
            Object node = children.next();
            if (node != null) {
                returnText = returnText + serialize(node);
            }
            //add the line feed if specified

        }


        //add the closing tag
        if (prefixed)
            returnText = returnText + "</" + prefix + ":" + element.getLocalName() + ">";
        else
            returnText = returnText + "</" + element.getLocalName() + ">";

        if (newLines)
            returnText = returnText + "\n";

        return returnText;
    }

    /**
     * @param text
     * @return
     */
    private String serializeText(OMText text) {
        short type = text.getType();
        String returnText = null;
        if (type == OMNode.COMMENT_NODE) {
            returnText = "<!--" + text.getValue() + "-->";
        } else if (type == OMNode.CDATA_SECTION_NODE) {
            returnText = "<![CDATA[" + text.getValue() + "]]>";
        } else {
            returnText = text.getValue();
        }
        return returnText;
    }

    /**
     * @param attr
     * @return
     */
    private String serializeAttribute(OMAttribute attr) {
        String returnText = "";
        //first check whether the attribute is associated with a namespace
        OMNamespace ns = attr.getNamespace();
        if (ns != null) {
            //add the prefix if it's availble
            String prefix = ns.getPrefix();
            if (prefix != null)
                returnText = returnText + prefix + ":";
        }
        //add the local name and the value
        returnText = returnText + attr.getLocalName() + "=\"" + attr.getValue() + "\"";
        return returnText;
    }


    private String serializeNamespace(OMNamespace namespace) {
        String returnText = "";

        if (namespace != null) {
            //add the prefix if it's availble
            String prefix = namespace.getPrefix();

            if (prefix != null)
                returnText = returnText + "xmlns:" + prefix + "=";
            else
                returnText = returnText + "xmlns=";

            returnText = returnText + "\"" + namespace.getName() + "\"";
        }
        //add the local name and the value

        return returnText;
    }


}
