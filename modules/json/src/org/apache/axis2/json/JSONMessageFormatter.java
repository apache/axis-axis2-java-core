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

import org.apache.axis2.context.MessageContext;
import org.codehaus.jettison.mapped.MappedXMLOutputFactory;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.Writer;

/**
 * This JSONMessageFormatter is the formatter for "Mapped" formatted JSON in Axis2. This type of
 * JSON strings are really easy to use in Javascript. Eg:  &lt;out&gt;&lt;in&gt;mapped
 * JSON&lt;/in&gt;&lt;/out&gt; is converted to... {"out":{"in":"mapped JSON"}} WARNING: We do not
 * support "Mapped" JSON Strings with *namespaces* in Axis2. This convention is supported in Axis2,
 * with the aim of making Javascript users' life easy (services written in Javascript). There are
 * no namespaces used in Javascript. If you want to use JSON with namespaces, use the
 * JSONBadgerfishMessageForatter (for "Badgerfish" formatted JSON) which supports JSON with
 * namespaces.
 */


public class JSONMessageFormatter extends AbstractJSONMessageFormatter {
    public JSONMessageFormatter() {
        super(JSONDataSource.class);
    }
    
    //returns the "Mapped" JSON writer
    @Override
    protected XMLStreamWriter getJSONWriter(Writer writer, MessageContext messageContext) throws XMLStreamException {
        return new MappedXMLOutputFactory(JSONUtil.getNS2JNSMap(
                messageContext.getAxisService())).createXMLStreamWriter(writer);
    }
}
