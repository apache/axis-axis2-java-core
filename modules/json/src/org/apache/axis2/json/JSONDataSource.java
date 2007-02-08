package org.apache.axis2.json;
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMDataSource;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMOutputFormat;
import org.codehaus.jettison.mapped.MappedXMLInputFactory;
import org.json.JSONException;
import org.apache.axis2.AxisFault;
import org.json.JSONObject;
import org.json.JSONTokener;

public class JSONDataSource implements OMDataSource {

    private InputStream jsonInputStream;
    private String jsonString;
    private boolean isRead = false;
    protected String localName;

    public JSONDataSource(InputStream jsonInputStream, String localName) {
        this.jsonInputStream = jsonInputStream;
        this.localName = localName;
    }

    //gives json
    public void serialize(OutputStream outputStream, OMOutputFormat omOutputFormat) throws javax.xml.stream.XMLStreamException {
        OutputStreamWriter writer = new OutputStreamWriter(outputStream);
        serialize(writer, omOutputFormat);
        try {
            writer.flush();
        } catch (IOException e) {
            throw new OMException();
        }
    }

    //gives json
    public void serialize(Writer writer, OMOutputFormat omOutputFormat) throws javax.xml.stream.XMLStreamException {
        try {
            JSONTokener jsonTokener = new JSONTokener("{" + localName + ":" + this.getJSONString());
            JSONObject jsonObject = new JSONObject(jsonTokener);
            jsonObject.write(writer);
        } catch (JSONException e) {
            throw new OMException();
        }
    }

    //gives xml
    public void serialize(javax.xml.stream.XMLStreamWriter xmlStreamWriter) throws javax.xml.stream.XMLStreamException {
        XMLStreamReader reader = getReader();
        xmlStreamWriter.writeStartDocument();
        while (reader.hasNext()) {
            int x = reader.next();
            switch (x) {
                case XMLStreamConstants.START_ELEMENT:
                    xmlStreamWriter.writeStartElement(reader.getPrefix(), reader.getLocalName(), reader.getNamespaceURI(reader.getPrefix()));
                    int namespaceCount = reader.getNamespaceCount();
                    for (int i = namespaceCount - 1; i >= 0; i--) {
                        xmlStreamWriter.writeNamespace(reader.getNamespacePrefix(i), reader.getNamespaceURI(i));
                    }
                    int attributeCount = reader.getAttributeCount();
                    for (int i = 0; i < attributeCount; i++) {
                        xmlStreamWriter.writeAttribute(reader.getAttributePrefix(i),
                                reader.getAttributeNamespace(i), reader.getAttributeLocalName(i),
                                reader.getAttributeValue(i));
                    }
                    break;
                case XMLStreamConstants.START_DOCUMENT:
                    break;
                case XMLStreamConstants.CHARACTERS:
                    xmlStreamWriter.writeCharacters(reader.getText());
                    break;
                case XMLStreamConstants.CDATA:
                    xmlStreamWriter.writeCData(reader.getText());
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    xmlStreamWriter.writeEndElement();
                    break;
                case XMLStreamConstants.END_DOCUMENT:
                    xmlStreamWriter.writeEndDocument();
                    break;
                case XMLStreamConstants.SPACE:
                    break;
                case XMLStreamConstants.COMMENT:
                    xmlStreamWriter.writeComment(reader.getText());
                    break;
                case XMLStreamConstants.DTD:
                    xmlStreamWriter.writeDTD(reader.getText());
                    break;
                case XMLStreamConstants.PROCESSING_INSTRUCTION:
                    xmlStreamWriter.writeProcessingInstruction(reader.getPITarget(), reader.getPIData());
                    break;
                case XMLStreamConstants.ENTITY_REFERENCE:
                    xmlStreamWriter.writeEntityRef(reader.getLocalName());
                    break;
                default :
                    throw new OMException();
            }
        }
        xmlStreamWriter.writeEndDocument();
    }

    public javax.xml.stream.XMLStreamReader getReader() throws javax.xml.stream.XMLStreamException {

        HashMap nstojns = new HashMap();
        nstojns.put("", "");

        MappedXMLInputFactory inputFactory = new MappedXMLInputFactory(nstojns);
        String jsonString = "{" + localName + ":" + this.getJSONString();
        return inputFactory.createXMLStreamReader(new JSONTokener(jsonString));         
    }

    protected String getJSONString() {
        if (isRead) {
            return jsonString;
        } else {
            try {
                char temp = (char) jsonInputStream.read();
                jsonString = "";
                while ((int) temp != 65535) {
                    jsonString += temp;
                    temp = (char) jsonInputStream.read();
                }
            } catch (IOException e) {
                throw new OMException();
            }
            isRead = true;
            return jsonString;
        }
    }

    public String getCompleteJOSNString(){
        return localName + getJSONString();
    }
}
