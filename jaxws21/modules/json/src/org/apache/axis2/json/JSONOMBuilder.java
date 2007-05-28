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

package org.apache.axis2.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.io.ByteArrayInputStream;

import org.apache.axiom.om.*;

import org.apache.axiom.om.impl.OMNamespaceImpl;
import org.apache.axiom.om.impl.llom.OMSourcedElementImpl;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.transport.http.util.URIEncoderDecoder;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.builder.Builder;
import org.apache.axis2.context.MessageContext;

import java.io.IOException;
import java.io.InputStream;

/** Makes the OMSourcedElementImpl object with the JSONDataSource inside. */

public class JSONOMBuilder implements Builder {


    public JSONOMBuilder() {
    }

    //returns the OMSourcedElementImpl with JSONDataSource inside

    public OMElement processDocument(InputStream inputStream, String contentType, MessageContext messageContext) throws AxisFault {
        String localName = "";
        String prefix = "";
        OMNamespace ns = new OMNamespaceImpl("", "");

        OMFactory factory = OMAbstractFactory.getOMFactory();

        //if the input stream is null, then check whether the HTTP method is GET, if so get the JSON String which is received as a parameter, and make it an
        //input stream

        if (inputStream == null) {
            EndpointReference endpointReference = messageContext.getTo();
            if (endpointReference == null) {
                throw new AxisFault("Cannot create DocumentElement without destination EPR");
            }

            String requestURL;
            try {
                requestURL = URIEncoderDecoder.decode(endpointReference.getAddress());
            } catch (UnsupportedEncodingException e) {
                throw AxisFault.makeFault(e);
            }

            String jsonString;
            int index;
            if ((index = requestURL.indexOf("=")) > 0) {
                jsonString = requestURL.substring(index + 1);
                inputStream = new ByteArrayInputStream(jsonString.getBytes());
            } else {
                throw new AxisFault("No JSON message received through HTTP GET or POST");
            }
        }

        try {
            char temp = (char)inputStream.read();
            while (temp != ':') {
                if (temp != ' ' && temp != '{') {
                    localName += temp;
                }
                temp = (char)inputStream.read();
            }

            if (localName.charAt(0) == '"') {
                if (localName.charAt(localName.length() - 1) == '"') {
                    localName = localName.substring(1, localName.length() - 1);
                } else {
                    prefix = localName.substring(1, localName.length()) + ":";
                    localName = "";
                    temp = (char)inputStream.read();
                    while (temp != ':') {
                        if (temp != ' ') {
                            localName += temp;
                        }
                        temp = (char)inputStream.read();
                    }
                    localName = localName.substring(0, localName.length() - 1);
                }
            }
        } catch (IOException e) {
            throw AxisFault.makeFault(e);
        }
        JSONDataSource jsonDataSource = getDataSource(inputStream, prefix, localName);
        return new OMSourcedElementImpl(localName, ns, factory, jsonDataSource);
    }

    protected JSONDataSource getDataSource(InputStream jsonInputStream, String prefix, String localName) {
        return new JSONDataSource(jsonInputStream, "\"" + prefix + localName + "\"");
    }
}
