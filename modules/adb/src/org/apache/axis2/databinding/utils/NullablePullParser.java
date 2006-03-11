package org.apache.axis2.databinding.utils;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.Location;
import javax.xml.namespace.QName;
import javax.xml.namespace.NamespaceContext;
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

public class NullablePullParser implements XMLStreamReader {

    private QName outerQName = null;

    private static final int INIT_STATE = 0;
    private static final int START_ELEMENT_STATE = 1;
    private static final int END_ELEMENT_STATE = 2;
    private static final int FINISHED_STATE = 3;

    private static final QName NIL_QNAME = new QName("http://www.w3.org/2001/XMLSchema-instance","nil","xsi");
    private static final String NIL_VALUE_TRUE ="true";

    private int currentState = INIT_STATE;

    public NullablePullParser(QName outerQName) {
        this.outerQName = outerQName;
    }

    public Object getProperty(String string) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    public int next() throws XMLStreamException {
        int returnEvent = START_DOCUMENT;
        switch(currentState){
            case INIT_STATE:
                currentState = START_ELEMENT_STATE;
                returnEvent =  START_ELEMENT;
                break;
            case START_ELEMENT_STATE:
                currentState = END_ELEMENT_STATE;
                returnEvent =  END_ELEMENT;
                break;
            case END_ELEMENT_STATE:
                currentState = FINISHED_STATE;
                returnEvent =  END_DOCUMENT;
                break;
            case FINISHED_STATE:
                throw new XMLStreamException("parser completed!");
        }

        return returnEvent;
    }

    public void require(int i, String string, String string1) throws XMLStreamException {
        //nothing
    }

    public String getElementText() throws XMLStreamException {
        return null;
    }

    public int nextTag() throws XMLStreamException {
        throw new UnsupportedOperationException();
    }

    public boolean hasNext() throws XMLStreamException {
        return (currentState!=END_ELEMENT_STATE);

    }

    public void close() throws XMLStreamException {
        //do nothing
    }

    public String getNamespaceURI(String string) {
        if (outerQName.getPrefix()!=null &&
                outerQName.getPrefix().equals(string)){
            return outerQName.getNamespaceURI();
        }else{
            return null;
        }
    }

    public boolean isStartElement() {
        return (currentState==START_ELEMENT_STATE);
    }

    public boolean isEndElement() {
        return (currentState==END_ELEMENT_STATE);
    }

    public boolean isCharacters() {
        return false;
    }

    public boolean isWhiteSpace() {
        return false;
    }

    public String getAttributeValue(String string, String string1) {
        if (string== null){//null namespace - ignore it
            if (NIL_QNAME.getLocalPart().equals(string1)){
                return NIL_VALUE_TRUE;
            }
        }
        return null;
    }

    public int getAttributeCount() {
        return 1;
    }

    public QName getAttributeName(int i) {
        return (i==1)? NIL_QNAME:null;
    }

    public String getAttributeNamespace(int i) {
        return (i==1)? NIL_QNAME.getNamespaceURI():null;
    }

    public String getAttributeLocalName(int i) {
        return (i==1)? NIL_QNAME.getLocalPart():null;
    }

    public String getAttributePrefix(int i) {
        return (i==1)? NIL_QNAME.getPrefix():null;
    }

    public String getAttributeType(int i) {
        throw new UnsupportedOperationException();
    }

    public String getAttributeValue(int i) {
        return (i==1)? NIL_VALUE_TRUE:null;
    }

    public boolean isAttributeSpecified(int i) {
        return (i == 1);
    }

    public int getNamespaceCount() {
        return 0;
    }

    public String getNamespacePrefix(int i) {
        return null;
    }

    public String getNamespaceURI(int i) {
        return null;
    }

    public NamespaceContext getNamespaceContext() {
        throw new UnsupportedOperationException();
    }

    public int getEventType() {
        int returnEvent = START_DOCUMENT;
        switch(currentState){
            case INIT_STATE:
                returnEvent =  START_DOCUMENT;
                break;
            case START_ELEMENT_STATE:
                returnEvent =  START_ELEMENT;
                break;
            case END_ELEMENT_STATE:
                returnEvent =  END_ELEMENT;
                break;
            case FINISHED_STATE:
               returnEvent = END_DOCUMENT;
        }
       return returnEvent;
    }

    public String getText() {
        return null;
    }

    public char[] getTextCharacters() {
        return new char[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getTextCharacters(int i, char[] chars, int i1, int i2) throws XMLStreamException {
        return 0;
    }

    public int getTextStart() {
        return 0;
    }

    public int getTextLength() {
        return 0;
    }

    public String getEncoding() {
        return null;
    }

    public boolean hasText() {
        return false;
    }

    public Location getLocation() {
        throw new UnsupportedOperationException();
    }

    public QName getName() {
        return outerQName;
    }

    public String getLocalName() {
        return outerQName.getLocalPart();
    }

    public boolean hasName() {
        return true;
    }

    public String getNamespaceURI() {
        return outerQName.getNamespaceURI();
    }

    public String getPrefix() {
        return outerQName.getPrefix();
    }

    public String getVersion() {
        throw new UnsupportedOperationException();
    }

    public boolean isStandalone() {
        throw new UnsupportedOperationException();
    }

    public boolean standaloneSet() {
        throw new UnsupportedOperationException();
    }

    public String getCharacterEncodingScheme() {
        throw new UnsupportedOperationException();
    }

    public String getPITarget() {
        throw new UnsupportedOperationException();
    }

    public String getPIData() {
        throw new UnsupportedOperationException();
    }
}
