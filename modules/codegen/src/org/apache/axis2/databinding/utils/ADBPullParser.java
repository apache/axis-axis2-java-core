package org.apache.axis2.databinding.utils;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.ArrayList;

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

public class ADBPullParser implements XMLStreamReader{

    private ArrayList propertyList;
    private QName elementQName;

    private ADBPullParser(ArrayList propertyList, QName elementQName) {
        this.propertyList = propertyList;
        this.elementQName = elementQName;
    }

    public static XMLStreamReader createPullParser(ArrayList propertyList, QName adbBeansQName) {
       return new ADBPullParser(propertyList, adbBeansQName);
    }

    public Object getProperty(String string) throws IllegalArgumentException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int next() throws XMLStreamException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void require(int i, String string, String string1) throws XMLStreamException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getElementText() throws XMLStreamException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int nextTag() throws XMLStreamException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean hasNext() throws XMLStreamException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void close() throws XMLStreamException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getNamespaceURI(String string) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isStartElement() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isEndElement() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isCharacters() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isWhiteSpace() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getAttributeValue(String string, String string1) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getAttributeCount() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public QName getAttributeName(int i) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getAttributeNamespace(int i) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getAttributeLocalName(int i) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getAttributePrefix(int i) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getAttributeType(int i) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getAttributeValue(int i) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isAttributeSpecified(int i) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getNamespaceCount() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getNamespacePrefix(int i) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getNamespaceURI(int i) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public NamespaceContext getNamespaceContext() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getEventType() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getText() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public char[] getTextCharacters() {
        return new char[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getTextCharacters(int i, char[] chars, int i1, int i2) throws XMLStreamException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getTextStart() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getTextLength() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getEncoding() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean hasText() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Location getLocation() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public QName getName() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getLocalName() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean hasName() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getNamespaceURI() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getPrefix() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getVersion() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isStandalone() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean standaloneSet() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getCharacterEncodingScheme() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getPITarget() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getPIData() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
