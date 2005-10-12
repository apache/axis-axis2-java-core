package org.apache.axis2.databinding.utils;

import org.apache.axis2.databinding.ADBBean;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

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

public class ADBPullParser implements XMLStreamReader {

    private Object[] properties;
    private QName elementQName;

    private ADBPullParser childPullParser;

    private boolean accessingChildPullParser = false;

    // ===== To be used this ADBBean =============
    private boolean isEndElementFinished = false;

    // ===== To be used with Simple Name Value pair ====
    private boolean processingADBNameValuePair = false;
    private boolean nameValuePairStartElementProcessed = false;
    private boolean nameValuePairTextProcessed = false;
    private boolean nameValuePairEndElementProcessed = false;
    private ParserInformation tempParserInfo;
    // ==============================================

    private ParserInformation parserInformation;

    private int currentIndex = 0;


    private ADBPullParser(Object[] properties, QName elementQName) {
        this.properties = properties;
        this.elementQName = elementQName;
    }

    public static XMLStreamReader createPullParser(Object[] properties, QName adbBeansQName) {
        return new ADBPullParser(properties, adbBeansQName);
    }

    public boolean isCompleted() {
        return isEndElementFinished;
    }


    public class ParserInformation {
        String text;
        QName name;

        public ParserInformation(QName name, String text) {
            this.text = text;
            this.name = name;
        }

        public ParserInformation(QName name) {
            this.name = name;
        }


        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public QName getName() {
            return name;
        }

        public void setName(QName name) {
            this.name = name;
        }
    }

    // ----------- XMLStreamReader Methods -------------------------------------------//
    public Object getProperty(String string) throws IllegalArgumentException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int next() throws XMLStreamException {
        int event = 0;
        if (currentIndex >= properties.length + 2) {
            throw new XMLStreamException("End of elements has already been reached. Can not go beyond that");
        }

        if (accessingChildPullParser) {
            if (!childPullParser.isCompleted()) {
                return childPullParser.next();
            } else {
                accessingChildPullParser = false;
                currentIndex += 2;
            }
        }

        if(processingADBNameValuePair && nameValuePairEndElementProcessed){
            processingADBNameValuePair = false;
            currentIndex = currentIndex + 2;
            parserInformation = tempParserInfo;
        }

        if (currentIndex == 0) {
            // then this is just the start element
            currentIndex++;
            parserInformation = new ParserInformation(this.elementQName);
            return XMLStreamConstants.START_ELEMENT;
        } else if (properties.length + 1 == currentIndex) {
            // this is the end of this element
            currentIndex++;
            isEndElementFinished = true;
            return XMLStreamConstants.END_ELEMENT;
        } else {
            if (processingADBNameValuePair) {
                return processADBNameValuePair(null, null);
            }
            Object o = properties[currentIndex - 1];
            if (o instanceof QName) {
                ADBBean adbBean = (ADBBean) properties[currentIndex];
                childPullParser = (ADBPullParser) adbBean.getPullParser((QName) o);
                accessingChildPullParser = true;
                return this.next();
            } else {
                String simplePropertyName = (String) o;
                String simplePropertyValue = (String) properties[currentIndex];
                processingADBNameValuePair = true;
                return processADBNameValuePair(simplePropertyName, simplePropertyValue);
            }
        }

    }

    private int processADBNameValuePair(String simplePropertyName, String simplePropertyValue) {
        int event = 0;
        if (!nameValuePairStartElementProcessed) {
            event = XMLStreamConstants.START_ELEMENT;
            tempParserInfo = parserInformation;
            parserInformation = new ParserInformation(new QName(simplePropertyName), simplePropertyValue);
            nameValuePairStartElementProcessed = true;
            nameValuePairEndElementProcessed = false;
        } else if (nameValuePairStartElementProcessed && !nameValuePairTextProcessed) {
            event = XMLStreamConstants.CHARACTERS;
            nameValuePairTextProcessed = true;
        } else if (nameValuePairTextProcessed) {
            event = XMLStreamConstants.END_ELEMENT;
            nameValuePairEndElementProcessed = true;
            nameValuePairStartElementProcessed = false;
            nameValuePairTextProcessed = false;
        }

        return event;
    }

    public ParserInformation getParserInformation() {
        return parserInformation;
    }

    public boolean hasNext() throws XMLStreamException {
        return !isEndElementFinished;
    }

    private ADBPullParser.ParserInformation getCorrectParserInformation() {
        return accessingChildPullParser ? childPullParser.getParserInformation() : this.parserInformation;
    }

    public String getElementText() throws XMLStreamException {
        ParserInformation parserInfo = getCorrectParserInformation();
        return parserInfo != null ? parserInfo.getText() : "";
    }


    public int getAttributeCount() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getNamespaceCount() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getText() {
        ParserInformation parserInfo = getCorrectParserInformation();
        return parserInfo != null ? parserInfo.getText() : "";
    }

    public boolean hasText() {
        ParserInformation parserInfo = getCorrectParserInformation();
        return parserInfo != null && parserInfo.getText() != null && !"".equals(parserInformation.getText());
    }

    public QName getName() {
        ParserInformation parserInfo = getCorrectParserInformation();
        return parserInfo != null ? parserInfo.getName() : null;
    }

    public String getLocalName() {
        ParserInformation parserInfo = getCorrectParserInformation();
        return parserInfo != null ? parserInfo.getName().getLocalPart() : null;
    }

    public boolean hasName() {
        ParserInformation parserInfo = getCorrectParserInformation();
        return parserInfo != null && parserInfo.getName() != null;
    }

    public String getNamespaceURI() {
        ParserInformation parserInfo = getCorrectParserInformation();
        return parserInfo != null && parserInfo.getName() != null ? parserInfo.getName().getNamespaceURI() : "";
    }

    public String getPrefix() {
        ParserInformation parserInfo = getCorrectParserInformation();
        return parserInfo != null ? parserInfo.getName().getPrefix() : null;
    }


    // -------- un-implemented methods ----------
    public void require(int i, String string, String string1) throws XMLStreamException {
        throw new UnsupportedOperationException("Yet to be implemented !!");
    }

    public int nextTag() throws XMLStreamException {
        throw new UnsupportedOperationException("Yet to be implemented !!");
    }

    public void close() throws XMLStreamException {
        throw new UnsupportedOperationException("Yet to be implemented !!");
    }

    public String getNamespaceURI(String string) {
        throw new UnsupportedOperationException("Yet to be implemented !!");
    }

    public boolean isStartElement() {
        throw new UnsupportedOperationException("Yet to be implemented !!");
    }

    public boolean isEndElement() {
        throw new UnsupportedOperationException("Yet to be implemented !!");
    }

    public boolean isCharacters() {
        throw new UnsupportedOperationException("Yet to be implemented !!");
    }

    public boolean isWhiteSpace() {
        throw new UnsupportedOperationException("Yet to be implemented !!");
    }

    public String getAttributeValue(String string, String string1) {
        throw new UnsupportedOperationException("Yet to be implemented !!");
    }

    public QName getAttributeName(int i) {
        throw new UnsupportedOperationException("Yet to be implemented !!");
    }

    public String getAttributeNamespace(int i) {
        throw new UnsupportedOperationException("Yet to be implemented !!");
    }

    public String getAttributeLocalName(int i) {
        throw new UnsupportedOperationException("Yet to be implemented !!");
    }

    public String getAttributePrefix(int i) {
        throw new UnsupportedOperationException("Yet to be implemented !!");
    }

    public String getAttributeType(int i) {
        throw new UnsupportedOperationException("Yet to be implemented !!");
    }

    public String getAttributeValue(int i) {
        throw new UnsupportedOperationException("Yet to be implemented !!");
    }

    public boolean isAttributeSpecified(int i) {
        throw new UnsupportedOperationException("Yet to be implemented !!");
    }

    public String getNamespacePrefix(int i) {
        throw new UnsupportedOperationException("Yet to be implemented !!");
    }

    public String getNamespaceURI(int i) {
        throw new UnsupportedOperationException("Yet to be implemented !!");
    }

    public NamespaceContext getNamespaceContext() {
        throw new UnsupportedOperationException("Yet to be implemented !!");
    }

    public int getEventType() {
        throw new UnsupportedOperationException("Yet to be implemented !!");
    }

    public char[] getTextCharacters() {
        throw new UnsupportedOperationException("Yet to be implemented !!");
    }

    public int getTextCharacters(int i, char[] chars, int i1, int i2) throws XMLStreamException {
        throw new UnsupportedOperationException("Yet to be implemented !!");
    }

    public int getTextStart() {
        throw new UnsupportedOperationException("Yet to be implemented !!");

    }

    public int getTextLength() {
        throw new UnsupportedOperationException("Yet to be implemented !!");
    }

    public String getEncoding() {
        throw new UnsupportedOperationException("Yet to be implemented !!");
    }

    public Location getLocation() {
        throw new UnsupportedOperationException("Yet to be implemented !!");
    }

    public String getVersion() {
        throw new UnsupportedOperationException("Yet to be implemented !!");
    }

    public boolean isStandalone() {
        throw new UnsupportedOperationException("Yet to be implemented !!");
    }

    public boolean standaloneSet() {
        throw new UnsupportedOperationException("Yet to be implemented !!");
    }

    public String getCharacterEncodingScheme() {
        throw new UnsupportedOperationException("Yet to be implemented !!");
    }

    public String getPITarget() {
        throw new UnsupportedOperationException("Yet to be implemented !!");
    }

    public String getPIData() {
        throw new UnsupportedOperationException("Yet to be implemented !!");
    }

    // --------------------------------------------------------------------------------------------------//

}
