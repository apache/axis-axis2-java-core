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

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.builder.Builder;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.kernel.http.util.URIEncoderDecoder;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;

/** Makes the OMSourcedElement object with the JSONDataSource inside. */

public abstract class AbstractJSONOMBuilder implements Builder {


    public AbstractJSONOMBuilder() {
    }

    /**
     * gives the OMSourcedElement using the incoming JSON stream
     *
     * @param inputStream - incoming message as an input stream
     * @param contentType - content type of the message (eg: application/json)
     * @param messageContext - inflow message context
     * @return OMSourcedElement with JSONDataSource inside
     * @throws AxisFault
     */

    public OMElement processDocument(InputStream inputStream, String contentType,
                                     MessageContext messageContext) throws AxisFault {
        OMFactory factory = OMAbstractFactory.getOMFactory();

        //sets DoingREST to true because, security scenarios needs to handle in REST way
        messageContext.setDoingREST(true);

        Reader reader;
        
        //if the input stream is null, then check whether the HTTP method is GET, if so get the
        // JSON String which is received as a parameter, and make it an input stream

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
            //As the message is received through GET, check for "=" sign and consider the second
            //half as the incoming JSON message
            if ((index = requestURL.indexOf("=")) > 0) {
                jsonString = requestURL.substring(index + 1);
                reader = new StringReader(jsonString);
            } else {
                /*
                 * Resolve https://issues.apache.org/jira/browse/AXIS2-5929
                 * @author MARTI PAMIES SOLA
                 * @since   2022-10-27 
                 * Set JSON message from request URI if not present as parameter.
                 * This allow requests like: .../ResoureName/ResourceID  or .../ResoureName
                 * To be considered as valid and be able to return a specific resoruce or, all this type of resources
                 * If not added this improvment, those request were considered as invalid and an exception was thrown.
                 */
            	String requestParam=requestURL;
            	if (!(requestParam.equals(""))) {
            		jsonString = requestParam;
                    reader = new StringReader(jsonString);
            	}else {
        			throw new AxisFault("No JSON message received through HTTP GET or POST");
            	}
            }
        } else {
            // Not sure where this is specified, but SOAPBuilder also determines the charset
            // encoding like that
            String charSetEncoding = (String)messageContext.getProperty(
                    Constants.Configuration.CHARACTER_SET_ENCODING);
            if (charSetEncoding == null) {
                charSetEncoding = MessageContext.DEFAULT_CHAR_SET_ENCODING;
            }
            try {
                reader = new InputStreamReader(inputStream, charSetEncoding);
            } catch (UnsupportedEncodingException ex) {
                throw AxisFault.makeFault(ex);
            }
        }

        return factory.createOMElement(getDataSource(reader, messageContext));
    }

    protected abstract AbstractJSONDataSource getDataSource(Reader jsonReader, MessageContext messageContext);
}
