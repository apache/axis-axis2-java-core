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

import org.apache.axiom.om.OMDataSource;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.om.OMException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.json.JSONException;
import org.codehaus.jettison.badgerfish.BadgerFishXMLInputFactory;
import org.codehaus.jettison.mapped.MappedXMLInputFactory;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamConstants;
import java.io.*;
import java.util.HashMap;

public class JSONBadgerfishDataSource extends JSONDataSource{

    public JSONBadgerfishDataSource(InputStream jsonInputStream, String localName){
        super(jsonInputStream, localName);
    }

    public javax.xml.stream.XMLStreamReader getReader() throws javax.xml.stream.XMLStreamException {

        BadgerFishXMLInputFactory inputFactory = new BadgerFishXMLInputFactory();
        return inputFactory.createXMLStreamReader(new JSONTokener("{" + localName + ":" + this.getJSONString()));

    }
}
