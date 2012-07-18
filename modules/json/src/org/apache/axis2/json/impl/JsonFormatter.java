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

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.json.impl.utils.JsonConstant;
import org.apache.axis2.transport.MessageFormatter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URL;


public class JsonFormatter implements MessageFormatter {
    private static final Log log = LogFactory.getLog(JsonFormatter.class);

    public byte[] getBytes(MessageContext messageContext, OMOutputFormat omOutputFormat) throws AxisFault {
        return new byte[0];
    }

    public void writeTo(MessageContext outMsgCtxt, OMOutputFormat omOutputFormat, OutputStream outputStream, boolean b) throws AxisFault {
        JsonWriter writer = null;
        String msg;

        try {
            String charSetEncoding = (String) outMsgCtxt.getProperty(Constants.Configuration.CHARACTER_SET_ENCODING);
            writer = new JsonWriter(new OutputStreamWriter(outputStream, charSetEncoding ));
            Gson gson = new Gson();
            Object retObj = outMsgCtxt.getProperty(JsonConstant.RETURN_OBJECT);
            writer.beginObject();
            writer.name(JsonConstant.RESPONSE);
            Type returnType = (Type) outMsgCtxt.getProperty(JsonConstant.RETURN_TYPE);

            gson.toJson(retObj, returnType, writer);
            writer.endObject();
            writer.flush();
        } catch (UnsupportedEncodingException e) {
            msg = "Exception occur when try to encode output stream usig  " +
                    Constants.Configuration.CHARACTER_SET_ENCODING + " charset";
            log.error(msg , e);
            throw AxisFault.makeFault(e);
        } catch (IOException e) {
            msg = "Exception occur while writting to JsonWriter at the JsonFormatter ";
            log.error(msg, e);
            throw AxisFault.makeFault(e);
        }
    }

    public String getContentType(MessageContext outMsgCtxt, OMOutputFormat omOutputFormat, String s) {
        String contentType = (String)outMsgCtxt.getProperty(Constants.Configuration.CONTENT_TYPE);
        outMsgCtxt.setProperty(Constants.Configuration.CONTENT_TYPE , "application/json-impl");
        return "application/json-impl";
    }

    public URL getTargetAddress(MessageContext messageContext, OMOutputFormat omOutputFormat, URL url) throws AxisFault {
        return null;
    }

    public String formatSOAPAction(MessageContext messageContext, OMOutputFormat omOutputFormat, String s) {
        return null;
    }
}
