package org.apache.axis.impl.llom.serialize;

import org.apache.axis.om.OMElement;
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
 */
public class XMLSerilazer {
    private static StringBuffer b;
    private XMLStreamReader parser;
    private OMElement startingElement;

    public XMLSerilazer(XMLStreamReader streamingWrapper) {
        this.parser = streamingWrapper;

    }

    public StringBuffer serialize(OMElement element) {

        return b;
    }


    public static String printEvent(XMLStreamReader parser) {

        switch (parser.getEventType()) {
            case XMLStreamReader.START_ELEMENT:
                b.append("<");
                printName(parser, b);
                for (int i = 0; i < parser.getNamespaceCount(); i++) {
                    b.append(" ");
                    String n = parser.getNamespacePrefix(i);
                    if ("xmlns".equals(n)) {
                        b.append("xmlns=\"" + parser.getNamespaceURI(i) + "\"");
                    } else {
                        b.append("xmlns:" + n);
                        b.append("=\"");
                        b.append(parser.getNamespaceURI(i));
                        b.append("\"");
                    }
                }

                for (int i = 0; i < parser.getAttributeCount(); i++) {
                    b.append(" ");
                    printName(parser.getAttributePrefix(i),
                            parser.getAttributeNamespace(i),
                            parser.getAttributeLocalName(i),
                            b);
                    b.append("=\"");
                    b.append(parser.getAttributeValue(i));
                    b.append("\"");
                }

                b.append(">");
                break;
            case XMLStreamReader.END_ELEMENT:
                b.append("</");
                printName(parser, b);
                for (int i = 0; i < parser.getNamespaceCount(); i++) {
                    b.append(" ");
                    String n = parser.getNamespacePrefix(i);
                    if ("xmlns".equals(n)) {
                        b.append("xmlns=\"" + parser.getNamespaceURI(i) + "\"");
                    } else {
                        b.append("xmlns:" + n);
                        b.append("=\"");
                        b.append(parser.getNamespaceURI(i));
                        b.append("\"");
                    }
                }
                b.append(">");
                break;
            case XMLStreamReader.SPACE:
            case XMLStreamReader.CHARACTERS:
                int start = parser.getTextStart();
                int length = parser.getTextLength();
                b.append(new String(parser.getTextCharacters(),
                        start,
                        length));
                break;
            case XMLStreamReader.CDATA:
                b.append("<![CDATA[");
                if (parser.hasText())
                    b.append(parser.getText());
                b.append("]]>");
                break;

            case XMLStreamReader.COMMENT:
                b.append("<!--");
                if (parser.hasText())
                    b.append(parser.getText());
                b.append("-->");
                break;
            case XMLStreamReader.START_DOCUMENT:
//                b.append("<?xml");
//                b.append(" version='" + parser.getVersion() + "'");
//                b.append(" encoding='" + parser.getCharacterEncodingScheme() + "'");
//                if (parser.isStandalone())
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

    private static void printName(XMLStreamReader parser, StringBuffer b) {
        if (parser.hasName()) {
            String prefix = parser.getPrefix();
            String uri = parser.getNamespaceURI();
            String localName = parser.getLocalName();
            printName(prefix, uri, localName, b);
        }
    }

}
