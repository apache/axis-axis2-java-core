package org.apache.axis.om.serialise;

import org.apache.axis.om.OMElement;
import org.apache.axis.om.StreamingWrapper;

import javax.xml.stream.XMLStreamReader;

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
 * User: Eran Chinthaka - Lanka Software Foundation
 * Date: Nov 18, 2004
 * Time: 11:29:48 AM
 */
public class XMLSerilazer {
    private static StringBuffer b;
    private StreamingWrapper streamingWrapper;
    private OMElement startingElement;

    public XMLSerilazer(StreamingWrapper streamingWrapper) {
        this.streamingWrapper = streamingWrapper;

    }

    public StringBuffer serialize(OMElement element){

        return b;
    }




    public static String printEvent(StreamingWrapper streamingWrapper) {

        switch (streamingWrapper.getEventType()) {
            case XMLStreamReader.START_ELEMENT:
                b.append("<");
                printName(streamingWrapper, b);
                for (int i = 0; i < streamingWrapper.getNamespaceCount(); i++) {
                    b.append(" ");
                    String n = streamingWrapper.getNamespacePrefix(i);
                    if ("xmlns".equals(n)) {
                        b.append("xmlns=\"" + streamingWrapper.getNamespaceURI(i) + "\"");
                    } else {
                        b.append("xmlns:" + n);
                        b.append("=\"");
                        b.append(streamingWrapper.getNamespaceURI(i));
                        b.append("\"");
                    }
                }

                for (int i = 0; i < streamingWrapper.getAttributeCount(); i++) {
                    b.append(" ");
                    printName(streamingWrapper.getAttributePrefix(i),
                            streamingWrapper.getAttributeNamespace(i),
                            streamingWrapper.getAttributeLocalName(i),
                            b);
                    b.append("=\"");
                    b.append(streamingWrapper.getAttributeValue(i));
                    b.append("\"");
                }

                b.append(">");
                break;
            case XMLStreamReader.END_ELEMENT:
                b.append("</");
                printName(streamingWrapper, b);
                for (int i = 0; i < streamingWrapper.getNamespaceCount(); i++) {
                    b.append(" ");
                    String n = streamingWrapper.getNamespacePrefix(i);
                    if ("xmlns".equals(n)) {
                        b.append("xmlns=\"" + streamingWrapper.getNamespaceURI(i) + "\"");
                    } else {
                        b.append("xmlns:" + n);
                        b.append("=\"");
                        b.append(streamingWrapper.getNamespaceURI(i));
                        b.append("\"");
                    }
                }
                b.append(">");
                break;
            case XMLStreamReader.SPACE:
            case XMLStreamReader.CHARACTERS:
                int start = streamingWrapper.getTextStart();
                int length = streamingWrapper.getTextLength();
                b.append(new String(streamingWrapper.getTextCharacters(),
                        start,
                        length));
                break;
            case XMLStreamReader.CDATA:
                b.append("<![CDATA[");
                if (streamingWrapper.hasText())
                    b.append(streamingWrapper.getText());
                b.append("]]>");
                break;

            case XMLStreamReader.COMMENT:
                b.append("<!--");
                if (streamingWrapper.hasText())
                    b.append(streamingWrapper.getText());
                b.append("-->");
                break;
            case XMLStreamReader.START_DOCUMENT:
//                b.append("<?xml");
//                b.append(" version='" + streamingWrapper.getVersion() + "'");
//                b.append(" encoding='" + streamingWrapper.getCharacterEncodingScheme() + "'");
//                if (streamingWrapper.isStandalone())
//                    b.append(" standalone='yes'");
//                else
//                    b.append(" standalone='no'");
//                b.append("?>");
                break;

        }
        return b.toString();
    }

    private static void printName(String prefix,
                                  String uri,
                                  String localName,
                                  StringBuffer b) {
        if (uri != null && !("".equals(uri))) b.append("['" + uri + "']:");
        if (prefix != null && !("".equals(prefix))) b.append(prefix + ":");
        if (localName != null) b.append(localName);
    }

    private static void printName(StreamingWrapper streamingWrapper, StringBuffer b) {
        if (streamingWrapper.hasName()) {
            String prefix = streamingWrapper.getPrefix();
            String uri = streamingWrapper.getNamespaceURI();
            String localName = streamingWrapper.getLocalName();
            printName(prefix, uri, localName, b);
        }
    }

}
