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

package org.apache.axis2.json.moshi;

import com.squareup.moshi.JsonReader;
import static com.squareup.moshi.JsonReader.Token.NULL;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.json.factory.JSONType;
import org.apache.axis2.json.factory.JsonConstant;
import org.apache.axis2.json.factory.JsonObject;
import org.apache.axis2.json.factory.XmlNode;
import org.apache.axis2.json.factory.XmlNodeGenerator;
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


public class MoshiXMLStreamReader implements XMLStreamReader {

    private static final Log log = LogFactory.getLog(MoshiXMLStreamReader.class);

    private JsonReader jsonReader;

    private JsonState state = JsonState.StartState;

    private JsonReader.Token tokenType;

    private String localName;

    private String value;

    private boolean isProcessed;

    private ConfigurationContext configContext;

    private QName elementQname;

    private XmlNode mainXmlNode;

    private List<XmlSchema> xmlSchemaList;

    private Queue<JsonObject> queue = new LinkedList<JsonObject>();

    private Queue<JsonObject> attribute_queue = new LinkedList<JsonObject>();

    private List<Attribute> attributes;

    private XmlNodeGenerator xmlNodeGenerator;

    private Stack<JsonObject> stackObj = new Stack<JsonObject>();
    private Stack<JsonObject> miniStack = new Stack<JsonObject>();
    private JsonObject topNestedArrayObj = null;
    private Stack<JsonObject> processedJsonObject = new Stack<JsonObject>();

    private String namespace;


    public MoshiXMLStreamReader(JsonReader jsonReader) {
        this.jsonReader = jsonReader;
    }

    public MoshiXMLStreamReader(JsonReader jsonReader, QName elementQname, List<XmlSchema> xmlSchemaList,
                               ConfigurationContext configContext) throws AxisFault {
        this.jsonReader = jsonReader;
        initXmlStreamReader(elementQname, xmlSchemaList, configContext);
    }

    public JsonReader getJsonReader() {
        return jsonReader;
    }

    public void initXmlStreamReader(QName elementQname, List<XmlSchema> xmlSchemaList, ConfigurationContext configContext) throws AxisFault {
        this.elementQname = elementQname;
        this.xmlSchemaList = xmlSchemaList;
        this.configContext = configContext;
        try {
            process();
        } catch (AxisFault axisFault) {
            throw new AxisFault("Error while initializing XMLStreamReader ", axisFault);
        }
        isProcessed = true;

    }

    private void process() throws AxisFault {
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
        log.debug("MoshiXMLStreamReader.process() completed on queue size: " + queue.size());
	// XML Elements
        for (JsonObject jsonobject : queue) {
            log.debug("moshixmlstreamreader.process() found Element to process as jsonobject name: " + jsonobject.getName() + " , type: " + jsonobject.getType());
	}

	// XML attributes
        attribute_queue = xmlNodeGenerator.getAttributeQueue();
        for (JsonObject jsonobject : attribute_queue) {
            log.debug("moshixmlstreamreader.process() found Attribute to process as jsonobject name: " + jsonobject.getName() + " , type: " + jsonobject.getType());
	}
        isProcessed = true;
        log.debug("MoshiXMLStreamReader.process() completed");
    }


    public Object getProperty(String name) throws IllegalArgumentException {
        return null;
    }


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


    public void require(int type, String namespaceURI, String localName) throws XMLStreamException {
        throw new UnsupportedOperationException("Method is not implemented");
    }


    public String getElementText() throws XMLStreamException {
        throw new UnsupportedOperationException("Method is not implemented");
    }


    public int nextTag() throws XMLStreamException {
        throw new UnsupportedOperationException("Method is not implemented");
    }


    public boolean hasNext() throws XMLStreamException {
        try {
            tokenType = jsonReader.peek();
            return !(tokenType == JsonReader.Token.END_DOCUMENT);
        } catch (IOException e) {
            throw new XMLStreamException("Unexpected end of json stream");
        }
    }


    public void close() throws XMLStreamException {
        throw new UnsupportedOperationException("Method is not implemented");
    }


    public String getNamespaceURI(String prefix) {
        if (isStartElement() || isEndElement()) {
            return namespace;
        } else {
            return null;
        }
    }


    public boolean isStartElement() {
        return (state == JsonState.NameName
                || state == JsonState.NameValue
                || state == JsonState.ValueValue_CHAR
                || state == JsonState.EndObjectBeginObject_START);
    }


    public boolean isEndElement() {
        return (state == JsonState.ValueValue_START
                || state == JsonState.EndArrayName
                || state == JsonState.ValueEndObject_END_2
                || state == JsonState.ValueName_START
                || state == JsonState.EndObjectName
                || state == JsonState.EndObjectBeginObject_END
                || state == JsonState.EndArrayEndObject
                || state == JsonState.EndObjectEndObject);
    }


    public boolean isCharacters() {
        return (state == JsonState.ValueValue_END
                || state == JsonState.ValueEndArray
                || state == JsonState.ValueEndObject_END_1
                || state == JsonState.ValueName_END);

    }


    public boolean isWhiteSpace() {
        return false;
    }


    public int getAttributeCount() {
	// TODO populate the List of the class Attributes here by readName() 
	// and using the attribute_queue instead of queue if attribute_queue not empty
        if (isStartElement()) {
            return 0; // don't support attributes on tags  in JSON convention
        } else {
            throw new IllegalStateException("Only valid on START_ELEMENT state");
        }
    }


    public String getAttributeLocalName(int index) {
        if ((null == attributes) || (index >= attributes.size())) {
            throw new IndexOutOfBoundsException();
        }
        return attributes.get(index).name.getLocalPart();
    }


    public QName getAttributeName(int index) {
        return attributes.get(index).name;
    }


    public String getAttributePrefix(int index) {
        if ((null == attributes) || (index >= attributes.size())) {
            throw new IndexOutOfBoundsException();
        }
        return null;
    }


    public String getAttributeType(int index) {
        return null;
    }


    public String getAttributeNamespace(int index) {
        return null;
    }


    public String getAttributeValue(int index) {
        if ((null == attributes) || (index >= attributes.size())) {
            throw new IndexOutOfBoundsException();
        }
        return attributes.get(index).value;
    }


    public String getAttributeValue(String namespaceURI, String localName) {
        if ((null == attributes) || (null == localName) || ("".equals(localName))) {
            throw new NoSuchElementException();
        }
        for (Attribute a : attributes) {
            if (localName.equals(a.name.getLocalPart())) {
                return a.value;
            }
        }
        throw new NoSuchElementException();
    }


    public boolean isAttributeSpecified(int index) {
        return (null != attributes) && (attributes.size() >= index);
    }


    public int getNamespaceCount() {
        if (isStartElement() || isEndElement()) {
            return 1; // we have one default namesapce
        } else {
            throw new IllegalStateException("only valid on a START_ELEMENT or END_ELEMENT state");
        }
    }


    public String getNamespacePrefix(int index) {
        if (isStartElement() || isEndElement()) {
            return null;
        } else {
            throw new IllegalStateException("only valid on a START_ELEMENT or END_ELEMENT state");
        }
    }


    public String getNamespaceURI(int index) {
        if (isStartElement() || isEndElement()) {
            return namespace;
        } else {
            throw new IllegalStateException("only valid on a START_ELEMENT or END_ELEMENT state");
        }
    }


    public NamespaceContext getNamespaceContext() {
        throw new UnsupportedOperationException("Method is not implemented");
    }


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


    public String getText() {
        if (isCharacters()) {
            return value;
        } else {
            return null;
        }
    }


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


    public int getTextCharacters(int sourceStart, char[] target, int targetStart, int length) throws XMLStreamException {
        throw new UnsupportedOperationException("Method is not implemented");
    }


    public int getTextStart() {
        throw new UnsupportedOperationException("Method is not implemented");
    }


    public int getTextLength() {
        throw new UnsupportedOperationException("Method is not implemented");
    }


    public String getEncoding() {
        return null;
    }


    public boolean hasText() {
        return isCharacters();
    }


    public Location getLocation() {
        return new Location() {          // Location is unKnown

            public int getLineNumber() {
                return -1;
            }


            public int getColumnNumber() {
                return -1;
            }


            public int getCharacterOffset() {
                return 0;
            }


            public String getPublicId() {
                return null;
            }


            public String getSystemId() {
                return null;
            }
        };
    }


    public QName getName() {
        if (isStartElement() || isEndElement()) {
            return new QName(namespace, localName);
        } else {
            throw new IllegalStateException("getName method is valid only for the START_ELEMENT or END_ELEMENT event");
        }

    }


    public String getLocalName() {
        int i = getEventType();
        if (i == XMLStreamReader.START_ELEMENT || i == XMLStreamReader.END_ELEMENT) {
            return localName;
        } else {
            throw new IllegalStateException("To get local name state should be START_ELEMENT or END_ELEMENT");
        }
    }


    public boolean hasName() {
        return (isStartElement() || isEndElement());
    }


    public String getNamespaceURI() {
        if (isStartElement() || isEndElement()) {
            return namespace;
        } else {
            return null;
        }
    }


    public String getPrefix() {
        return null;
    }


    public String getVersion() {
        return null;
    }


    public boolean isStandalone() {
        return false;
    }


    public boolean standaloneSet() {
        return false;
    }


    public String getCharacterEncodingScheme() {
        return null;
    }


    public String getPITarget() {
        throw new UnsupportedOperationException("Method is not implemented");
    }


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
        if (tokenType == JsonReader.Token.BEGIN_OBJECT) {
            beginObject();
            state = JsonState.NameName;
        } else if (tokenType == JsonReader.Token.BEGIN_ARRAY) {
            beginArray();
            tokenType = jsonReader.peek();
            if (tokenType == JsonReader.Token.BEGIN_OBJECT) {
                beginObject();
                state = JsonState.NameName;
            } else {
                state = JsonState.NameValue;
            }
        } else if (tokenType == JsonReader.Token.STRING || tokenType == JsonReader.Token.NUMBER || tokenType == JsonReader.Token.BOOLEAN
                || tokenType == JsonReader.Token.NULL) {
            state = JsonState.NameValue;
        }
    }

    private void readValue() throws IOException {
        nextValue();
        tokenType = jsonReader.peek();
        if (tokenType == JsonReader.Token.NAME) {
            state = JsonState.ValueName_END;
        } else if (tokenType == JsonReader.Token.STRING || tokenType == JsonReader.Token.NUMBER || tokenType == JsonReader.Token.BOOLEAN
                || tokenType == JsonReader.Token.NULL) {
            state = JsonState.ValueValue_END;
        } else if (tokenType == JsonReader.Token.END_ARRAY) {
            state = JsonState.ValueEndArray;
        } else if (tokenType == JsonReader.Token.END_OBJECT) {
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
        if (tokenType == JsonReader.Token.END_OBJECT) {
            removeStackObj();
            state = JsonState.EndObjectEndObject;
        } else if (tokenType == JsonReader.Token.END_ARRAY) {
            readEndArray();
            removeStackObj();
        } else if (tokenType == JsonReader.Token.NAME) {
            removeStackObj();
            state = JsonState.EndObjectName;
        } else if (tokenType == JsonReader.Token.BEGIN_OBJECT) {
            state = JsonState.EndObjectBeginObject_END;
        } else if (tokenType == JsonReader.Token.END_DOCUMENT) {
            removeStackObj();
            state = JsonState.EndObjectEndDocument;
        }
    }

    private void readEndArray() throws IOException {
        endArray();
        tokenType = jsonReader.peek();
        if (tokenType == JsonReader.Token.END_OBJECT) {
            state = JsonState.EndArrayEndObject;
        } else if (tokenType == JsonReader.Token.NAME) {
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
                throw new XMLStreamException(JsonConstant.IN_JSON_MESSAGE_NOT_VALID + "expected : " + jsonObj.getName() + " but found : " + name);
            }
        } else if (!queue.isEmpty()) {
            JsonObject jsonObj = queue.peek();
            if (jsonObj.getName().equals(name)) {
                namespace = jsonObj.getNamespaceUri();
                stackObj.push(queue.poll());
            } else {
                throw new XMLStreamException(JsonConstant.IN_JSON_MESSAGE_NOT_VALID + "expected : " + jsonObj.getName() + " but found : " + name);
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

            if (tokenType == JsonReader.Token.STRING) {
                value = jsonReader.nextString();
            } else if (tokenType == JsonReader.Token.BOOLEAN) {
                value = String.valueOf(jsonReader.nextBoolean());
            } else if (tokenType == JsonReader.Token.NUMBER) {
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
            } else if (tokenType == JsonReader.Token.NULL) {
                jsonReader.nextNull();
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

    public boolean isProcessed() {
        return isProcessed;
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

    private static class Attribute {
        private final QName name;
        private final String value;

        Attribute(QName name, String value) {
            this.name = name;
            this.value = value;
        }
    }
}
