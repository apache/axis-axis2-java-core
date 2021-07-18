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

import okio.BufferedSource;
import okio.Okio;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.builder.Builder;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.json.factory.JsonConstant;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class JsonBuilder implements Builder {
    Log log = LogFactory.getLog(JsonBuilder.class);
    public OMElement processDocument(InputStream inputStream, String s, MessageContext messageContext) throws AxisFault {
        messageContext.setProperty(JsonConstant.IS_JSON_STREAM , true);
        JsonReader jsonReader;
        String charSetEncoding=null;
        if (inputStream != null) {
            try {
                charSetEncoding = (String) messageContext.getProperty(Constants.Configuration.CHARACTER_SET_ENCODING);
                if (charSetEncoding != null && charSetEncoding.indexOf("UTF-8") == -1) {
                    log.warn("JsonBuilder.processDocument() detected encoding that is not UTF-8: " +charSetEncoding+ " , Moshi JsonReader internally invokes new JsonUtf8Reader()");
                }
                BufferedSource source = Okio.buffer(Okio.source(inputStream));
                jsonReader = JsonReader.of(source);
                jsonReader.setLenient(true);
                MoshiXMLStreamReader moshiXMLStreamReader = new MoshiXMLStreamReader(jsonReader);
                messageContext.setProperty(JsonConstant.MOSHI_XML_STREAM_READER, moshiXMLStreamReader);
            } catch (Exception e) {
                log.error("Exception occurred while writting to JsonWriter from JsonFormatter: " + e.getMessage(), e);
                throw new AxisFault("Bad Request");
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Inputstream is null, This is possible with GET request");
            }
        }
        log.debug("JsonBuilder.processDocument() has completed, returning default envelope");
        // dummy envelope
        SOAPFactory soapFactory = OMAbstractFactory.getSOAP11Factory();
        return soapFactory.getDefaultEnvelope();
    }

}
