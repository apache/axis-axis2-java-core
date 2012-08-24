/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.json.impl;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.json.impl.utils.JSONType;
import org.apache.axis2.json.impl.utils.JsonConstant;
import org.apache.axis2.json.impl.utils.JsonObject;
import org.apache.axis2.json.impl.utils.XmlNode;
import org.apache.axis2.json.impl.utils.XmlNodeGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.commons.schema.XmlSchema;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Stack;


public class GsonXMLStreamReader implements XMLStreamReader {

    private static final Log log = LogFactory.getLog(GsonXMLStreamReader.class);

    private JsonReader jsonReader;

    private JsonState state = JsonState.StartState;

    private JsonToken tokenType;

    private String localName;

    private String value;

    private boolean isProcessed;

    private ConfigurationContext configContext;

    private QName elementQname;

    private XmlNode mainXmlNode;

    private List<XmlSchema> xmlSchemaList;

    private Queue<JsonObject> queue = new LinkedList<JsonObject>();

    private XmlNodeGenerator xmlNodeGenerator;

    private Stack<JsonObject> stackObj = new Stack<JsonObject>();
    private Stack<JsonObject> miniStack = new Stack<JsonObject>();
    private JsonObject topNestedArrayObj = null;
    private Stack<JsonObject> processedJsonObject = new Stack<JsonObject>();

    private String namespace;


    // Default namespace
    final static private String DEFAULT_NAMESPACE = "http://axis2.apache.org/axis/json";

    final static private String DEFAULT_NAMESPACE_PREFIX = "ns";

    public GsonXMLStreamReader(JsonReader jsonReader) {
        this.jsonReader = jsonReader;
    }

    public GsonXMLStreamReader(JsonReader jsonReader, QName elementQname, List<XmlSchema> xmlSchemaList,
                               ConfigurationContext configContext) {
        this.jsonReader = jsonReader;
        initXmlStreamReader(elementQname, xmlSchemaList, configContext);
    }

    public JsonReader getJsonReader() {
        return jsonReader;
    }

    public void initXmlStreamReader(QName elementQname, List<XmlSchema> xmlSchemaList, ConfigurationContext configContext) {
        this.elementQname = elementQname;
        this.xmlSchemaList = xmlSchemaList;
        this.configContext = configContext;
        process();
        isProcessed = true;

    }

    private void process() {
        Object ob = configContext.getProperty(JsonConstant.XMLNODES);
        if (ob != null) {
            Map<QName, XmlNode> nodeMap = (Map<QName, XmlNode>) ob;
            XmlNode requesNode = nodeMap.get(elementQname);
            if (requesNode != null) {
                xmlNodeGenerator = new XmlNodeGenerator();
                queue = xmlNodeGenerator.getQueue(requesNode);
            } else {
                xmlNodeGenerator = new XmlNodeGenerator(xmlSchemaList, elementQname);
                mainXmlNode = xmlNodeGenerator.getMainXmlNode();
                queue = xmlNodeGenerator.getQueue(mainXmlNode);
                nodeMap.put(elementQname, mainXmlNode);
                configContext.setProperty(JsonConstant.XMLNODES, nodeMap);
            }
        } else {
            Map<QName, XmlNode> newNodeMap = new HashMap<QName, XmlNode>();
            xmlNodeGenerator = new XmlNodeGenerator(xmlSchemaList, elementQname);
            mainXmlNode = xmlNodeGenerator.getMainXmlNode();
            queue = xmlNodeGenerator.getQueue(mainXmlNode);
            newNodeMap.put(elementQname, mainXmlNode);
            configContext.setProperty(JsonConstant.XMLNODES, newNodeMap);
        }
        isProcessed = true;
    }

    @Override
    public Object getProperty(String name) throws IllegalArgumentException {
        return null;
    }

    @Override
    public int next() throws XMLStreamException {
        if (hasNext()) {
            try {
                stateTransition();
            } catch (IOException e) {
                throw new XMLStreamException("I/O error while reading JSON input Stream");
            }
            return getEventType();
        } else {
            throw new NoSuchElementException("There is no any next event");
        }
    }

    @Override
    public void require(int type, String namespaceURI, String localName) throws XMLStreamException {
        throw new UnsupportedOperationException("Method is not implemented");
    }

    @Override
    public String getElementText() throws XMLStreamException {
        throw new UnsupportedOperationException("Method is not implemented");
    }

    @Override
    public int nextTag() throws XMLStreamException {
        throw new UnsupportedOperationException("Method is not implemented");
    }

    @Override
    public boolean hasNext() throws XMLStreamException {
        try {
            tokenType = jsonReader.peek();
            if (tokenType == JsonToken.END_DOCUMENT) {
                return false;
            } else {
                return true;
            }
        } catch (IOException e) {
            throw new XMLStreamException("Unexpected end of json stream");
        }
    }

    @Override
    public void close() throws XMLStreamException {
        throw new UnsupportedOperationException("Method is not implemented");
    }

    @Override
    public String getNamespaceURI(String prefix) {
        if (isStartElement() || isEndElement()) {
            return namespace;
        } else {
            return null;
        }
    }

    @Override
    public boolean isStartElement() {
        if (state == JsonState.NameName
                || state == JsonState.NameValue
                || state == JsonState.ValueValue_CHAR
                || state == JsonState.EndObjectBeginObject_START) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean isEndElement() {
        if (state == JsonState.ValueValue_START
                || state == JsonState.EndArrayName
                || state == JsonState.ValueEndObject_END_2
                || state == JsonState.ValueName_START
                || state == JsonState.EndObjectName
                || state == JsonState.EndObjectBeginObject_END
                || state == JsonState.EndArrayEndObject
                || state == JsonState.EndObjectEndObject) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean isCharacters() {
        if (state == JsonState.ValueValue_END
                || state == JsonState.ValueEndArray
                || state == JsonState.ValueEndObject_END_1
                || state == JsonState.ValueName_END) {
            return true;
        } else {
            return false;
        }

    }

    @Override
    public boolean isWhiteSpace() {
        return false;
    }

    @Override
    public String getAttributeValue(String namespaceURI, String localName) {
        throw new UnsupportedOperationException("Method is not implemented");
    }

    @Override
    public int getAttributeCount() {
        if (isStartElement()) {
            return 0; // don't support attributes on tags  in JSON convention
        } else {
            throw new IllegalStateException("Only valid on START_ELEMENT state");
        }
    }

    @Override
    public QName getAttributeName(int index) {
        throw new UnsupportedOperationException("Method is not implemented");
    }

    @Override
    public String getAttributeNamespace(int index) {
        throw new UnsupportedOperationException("Method is not implemented");
    }

    @Override
    public String getAttributeLocalName(int index) {
        throw new UnsupportedOperationException("Method is not implemented");
    }

    @Override
    public String getAttributePrefix(int index) {
        throw new UnsupportedOperationException("Method is not implemented");
    }

    @Override
    public String getAttributeType(int index) {
        throw new UnsupportedOperationException("Method is not implemented");
    }

    @Override
    public String getAttributeValue(int index) {
        throw new UnsupportedOperationException("Method is not implemented");
    }

    @Override
    public boolean isAttributeSpecified(int index) {
        throw new UnsupportedOperationException("Method is not implemented");
    }

    @Override
    public int getNamespaceCount() {
        if (isStartElement() || isEndElement()) {
            return 1; // we have one default namesapce
        } else {
            throw new IllegalStateException("only valid on a START_ELEMENT or END_ELEMENT state");
        }
    }

    @Override
    public String getNamespacePrefix(int index) {
        if (isStartElement() || isEndElement()) {
            return null;
        } else {
            throw new IllegalStateException("only valid on a START_ELEMENT or END_ELEMENT state");
        }
    }

    @Override
    public String getNamespaceURI(int index) {
        if (isStartElement() || isEndElement()) {
            return namespace;
        } else {
            throw new IllegalStateException("only valid on a START_ELEMENT or END_ELEMENT state");
        }
    }

    @Override
    public NamespaceContext getNamespaceContext() {
        throw new UnsupportedOperationException("Method is not implemented");
    }

    @Override
    public int getEventType() {
        if (state == JsonState.StartState) {
            return START_DOCUMENT;
        } else if (isStartElement()) {
            return START_ELEMENT;
        } else if (isCharacters()) {
            return CHARACTERS;
        } else if (isEndElement()) {
            return END_ELEMENT;
        } else if (state == JsonState.EndObjectEndDocument) {
            return END_DOCUMENT;
        } else {
            return 0;  //To change body of implemented methods use File | Settings | File Templates.
        }

    }

    @Override
    public String getText() {
        if (isCharacters()) {
            return value;
        } else {
            return null;
        }
    }

    @Override
    public char[] getTextCharacters() {
        if (isCharacters()) {
            if (value == null) {
                return new char[0];
            } else {
                return value.toCharArray();
            }
        } else {
            throw new IllegalStateException("This is not a valid state");
        }
    }

    @Override
    public int getTextCharacters(int sourceStart, char[] target, int targetStart, int length) throws XMLStreamException {
        throw new UnsupportedOperationException("Method is not implemented");
    }

    @Override
    public int getTextStart() {
        throw new UnsupportedOperationException("Method is not implemented");
    }

    @Override
    public int getTextLength() {
        throw new UnsupportedOperationException("Method is not implemented");
    }

    @Override
    public String getEncoding() {
        return null;
    }

    @Override
    public boolean hasText() {
        if (isCharacters()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Location getLocation() {
        return new Location() {          // Location is unKnown
            @Override
            public int getLineNumber() {
                return -1;
            }

            @Override
            public int getColumnNumber() {
                return -1;
            }

            @Override
            public int getCharacterOffset() {
                return 0;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public String getPublicId() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public String getSystemId() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }
        };
    }

    @Override
    public QName getName() {
        if (isStartElement() || isEndElement()) {
            return new QName(namespace, localName);
        } else {
            throw new IllegalStateException("getName method is valid only for the START_ELEMENT or END_ELEMENT event");
        }

    }

    @Override
    public String getLocalName() {
        int i = getEventType();
        if (i == XMLStreamReader.START_ELEMENT || i == XMLStreamReader.END_ELEMENT) {
            return localName;
        } else {
            throw new IllegalStateException("To get local name state should be START_ELEMENT or END_ELEMENT");
        }
    }

    @Override
    public boolean hasName() {
        return (isStartElement() || isEndElement());
    }

    @Override
    public String getNamespaceURI() {
        if (isStartElement() || isEndElement()) {
            return namespace;
        } else {
            return null;
        }
    }

    @Override
    public String getPrefix() {
        return null;
    }

    @Override
    public String getVersion() {
        return null;
    }

    @Override
    public boolean isStandalone() {
        return false;
    }

    @Override
    public boolean standaloneSet() {
        return false;
    }

    @Override
    public String getCharacterEncodingScheme() {
        return null;
    }

    @Override
    public String getPITarget() {
        throw new UnsupportedOperationException("Method is not implemented");
    }

    @Override
    public String getPIData() {
        throw new UnsupportedOperationException("Method is not implemented");
    }

    private void stateTransition() throws XMLStreamException, IOException {
        if (state == JsonState.StartState) {
            beginObject();
            JsonObject topElement = new JsonObject("StackTopElement", JSONType.NESTED_OBJECT,
                    null, "http://axis2.apache.org/axis/json");
            stackObj.push(topElement);
            readName();
        } else if (state == JsonState.NameName) {
            readName();
        } else if (state == JsonState.NameValue) {
            readValue();
        } else if (state == JsonState.ValueEndObject_END_1) {
            state = JsonState.ValueEndObject_END_2;
            removeStackObj();
        } else if (state == JsonState.ValueEndObject_END_2) {
            readEndObject();
        } else if (state == JsonState.ValueName_END) {
            state = JsonState.ValueName_START;
            removeStackObj();
        } else if (state == JsonState.ValueName_START) {
            readName();
        } else if (state == JsonState.ValueValue_END) {
            state = JsonState.ValueValue_START;
        } else if (state == JsonState.ValueValue_START) {
            value = null;
            state = JsonState.ValueValue_CHAR;
        } else if (state == JsonState.ValueValue_CHAR) {
            readValue();
        } else if (state == JsonState.ValueEndArray) {
            readEndArray();
            removeStackObj();
        } else if (state == JsonState.EndArrayName) {
            readName();
        } else if (state == JsonState.EndObjectEndObject) {
            readEndObject();
        } else if (state == JsonState.EndObjectName) {
            readName();
        } else if (state == JsonState.EndObjectBeginObject_END) {
            state = JsonState.EndObjectBeginObject_START;
            fillMiniStack();
        } else if (state == JsonState.EndObjectBeginObject_START) {
            readBeginObject();
        } else if (state == JsonState.EndArrayEndObject) {
            readEndObject();
        }

    }

    private void removeStackObj() throws XMLStreamException {
        if (!stackObj.empty()) {
            if (topNestedArrayObj == null) {
                stackObj.pop();
            } else {
                if (stackObj.peek().equals(topNestedArrayObj)) {
                    topNestedArrayObj = null;
                    processedJsonObject.clear();
                    stackObj.pop();
                } else {
                    processedJsonObject.push(stackObj.pop());
                }
            }
            if (!stackObj.empty()) {
                localName = stackObj.peek().getName();
            } else {
                localName = "";
            }
        } else {
            System.out.println("stackObj is empty");
            throw new XMLStreamException("Error while processing input JSON stream, JSON request may not valid ," +
                    " it may has more end object characters ");
        }
    }

    private void fillMiniStack() {
        miniStack.clear();
        JsonObject nestedArray = stackObj.peek();
        while (!processedJsonObject.peek().equals(nestedArray)) {
            miniStack.push(processedJsonObject.pop());
        }
    }

    private void readName() throws IOException, XMLStreamException {
        nextName();
        tokenType = jsonReader.peek();
        if (tokenType == JsonToken.BEGIN_OBJECT) {
            beginObject();
            state = JsonState.NameName;
        } else if (tokenType == JsonToken.BEGIN_ARRAY) {
            beginArray();
            tokenType = jsonReader.peek();
            if (tokenType == JsonToken.BEGIN_OBJECT) {
                beginObject();
                state = JsonState.NameName;
            } else {
                state = JsonState.NameValue;
            }
        } else if (tokenType == JsonToken.STRING || tokenType == JsonToken.NUMBER || tokenType == JsonToken.BOOLEAN
                || tokenType == JsonToken.NULL) {
            state = JsonState.NameValue;
        }
    }

    private void readValue() throws IOException {
        nextValue();
        tokenType = jsonReader.peek();
        if (tokenType == JsonToken.NAME) {
            state = JsonState.ValueName_END;
        } else if (tokenType == JsonToken.STRING || tokenType == JsonToken.NUMBER || tokenType == JsonToken.BOOLEAN
                || tokenType == JsonToken.NULL) {
            state = JsonState.ValueValue_END;
        } else if (tokenType == JsonToken.END_ARRAY) {
            state = JsonState.ValueEndArray;
        } else if (tokenType == JsonToken.END_OBJECT) {
            state = JsonState.ValueEndObject_END_1;
        }
    }

    private void readBeginObject() throws IOException, XMLStreamException {
        beginObject();
        readName();
    }

    private void readEndObject() throws IOException, XMLStreamException {
        endObject();
        tokenType = jsonReader.peek();
        if (tokenType == JsonToken.END_OBJECT) {
            removeStackObj();
            state = JsonState.EndObjectEndObject;
        } else if (tokenType == JsonToken.END_ARRAY) {
            readEndArray();
            removeStackObj();
        } else if (tokenType == JsonToken.NAME) {
            removeStackObj();
            state = JsonState.EndObjectName;
        } else if (tokenType == JsonToken.BEGIN_OBJECT) {
            state = JsonState.EndObjectBeginObject_END;
        } else if (tokenType == JsonToken.END_DOCUMENT) {
            removeStackObj();
            state = JsonState.EndObjectEndDocument;
        }
    }

    private void readEndArray() throws IOException {
        endArray();
        tokenType = jsonReader.peek();
        if (tokenType == JsonToken.END_OBJECT) {
            state = JsonState.EndArrayEndObject;
        } else if (tokenType == JsonToken.NAME) {
            state = JsonState.EndArrayName;
        }
    }

    private void nextName() throws IOException, XMLStreamException {
        String name = jsonReader.nextName();
        if (!miniStack.empty()) {
            JsonObject jsonObj = miniStack.peek();
            if (jsonObj.getName().equals(name)) {
                namespace = jsonObj.getNamespaceUri();
                stackObj.push(miniStack.pop());
            } else {
                throw new XMLStreamException(JsonConstant.IN_JSON_MESSAGE_NOT_VALID + "required : " + jsonObj.getName() + " but get : " + name);
            }
        } else if (!queue.isEmpty()) {
            JsonObject jsonObj = queue.peek();
            if (jsonObj.getName().equals(name)) {
                namespace = jsonObj.getNamespaceUri();
                stackObj.push(queue.poll());
            } else {
                throw new XMLStreamException(JsonConstant.IN_JSON_MESSAGE_NOT_VALID + "required : " + jsonObj.getName() + " but get : " + name);
            }
        } else {
            throw new XMLStreamException(JsonConstant.IN_JSON_MESSAGE_NOT_VALID);
        }

        localName = stackObj.peek().getName();
        value = null;
    }

    private String nextValue() {
        try {
            tokenType = jsonReader.peek();

            if (tokenType == JsonToken.STRING) {
                value = jsonReader.nextString();
            } else if (tokenType == JsonToken.BOOLEAN) {
                value = String.valueOf(jsonReader.nextBoolean());
            } else if (tokenType == JsonToken.NUMBER) {
                JsonObject peek = stackObj.peek();
                String valueType = peek.getValueType();
                if (valueType.equals("int")) {
                    value = String.valueOf(jsonReader.nextInt());
                } else if (valueType.equals("long")) {
                    value = String.valueOf(jsonReader.nextLong());
                } else if (valueType.equals("double")) {
                    value = String.valueOf(jsonReader.nextDouble());
                } else if (valueType.equals("float")) {
                    value = String.valueOf(jsonReader.nextDouble());
                }
            } else if (tokenType == JsonToken.NULL) {
                value = null;
            } else {
                log.error("Couldn't read the value, Illegal state exception");
                throw new RuntimeException("Couldn't read the value, Illegal state exception");
            }
        } catch (IOException e) {
            log.error("IO error while reading json stream");
            throw new RuntimeException("IO error while reading json stream");
        }
        return value;
    }

    private void beginObject() throws IOException {
        jsonReader.beginObject();
    }

    private void beginArray() throws IOException {
        jsonReader.beginArray();
        if (stackObj.peek().getType() == JSONType.NESTED_ARRAY) {
            if (topNestedArrayObj == null) {
                topNestedArrayObj = stackObj.peek();
            }
            processedJsonObject.push(stackObj.peek());
        }
    }

    private void endObject() throws IOException {
        jsonReader.endObject();
    }

    private void endArray() throws IOException {
        jsonReader.endArray();
        if (stackObj.peek().equals(topNestedArrayObj)) {
            topNestedArrayObj = null;
        }
    }

    public enum JsonState {
        StartState,
        NameValue,
        NameName,
        ValueValue_END,
        ValueValue_START,
        ValueValue_CHAR,
        ValueEndArray,
        ValueEndObject_END_1,
        ValueEndObject_END_2,
        ValueName_END,
        ValueName_START,
        EndObjectEndObject,
        EndObjectName,
        EndArrayName,
        EndArrayEndObject,
        EndObjectBeginObject_END,
        EndObjectBeginObject_START,
        EndObjectEndDocument,
    }
}
