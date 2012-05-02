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

package org.apache.axis2.json;

import org.apache.axiom.om.OMException;
import org.apache.axiom.om.ds.AbstractPullOMDataSource;
import org.codehaus.jettison.AbstractXMLInputFactory;
import org.codehaus.jettison.json.JSONTokener;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.Reader;
import java.io.BufferedReader;

/**
 * JSONDataSource keeps the JSON String inside and consumes it when needed. This is to be kept in
 * the OMSourcedElement and can be used either to expand the tree or get the JSON String
 * directly without expanding. This uses the "Mapped" JSON convention.
 */

public abstract class AbstractJSONDataSource extends AbstractPullOMDataSource {

    private Reader jsonReader;
    private String jsonString;
    private boolean isRead = false;

    public AbstractJSONDataSource(Reader jsonReader) {
        this.jsonReader = jsonReader;
    }

    public final XMLStreamReader getReader() throws XMLStreamException {
        return getXMLInputFactory().createXMLStreamReader(new JSONTokener(getJSONString()));
    }

    protected abstract AbstractXMLInputFactory getXMLInputFactory() throws XMLStreamException;
    
    public boolean isDestructiveRead() {
        // TODO: for the moment the data source in not destructive (because it reads the entire message into memory before processing it), but this will change...
        return false;
    }

    @Override
    public Object getObject() {
        return getJSONString();
    }

    //returns the json string by consuming the JSON input stream.
    private String getJSONString() {
        if (isRead) {
            return jsonString;
        } else {
            try {
                BufferedReader br = new BufferedReader(jsonReader);
                StringBuilder sb = new StringBuilder(512);
                char[] tempBuf = new char[512];
                int readLen;

                while((readLen = br.read(tempBuf)) != -1) {
                    sb.append(tempBuf, 0, readLen);
                }
                jsonString = sb.toString();
            } catch (IOException e) {
                throw new OMException();
            }
            isRead = true;
            return jsonString;
        }
    }
}
