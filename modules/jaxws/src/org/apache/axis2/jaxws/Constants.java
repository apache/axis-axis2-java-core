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

package org.apache.axis2.jaxws;

/**
 * Constants that apply to the JAX-WS implementation.
 *
 */
public interface Constants {
    public static final String ENDPOINT_CONTEXT_MAP =
        "org.apache.axis2.jaxws.addressing.util.EndpointContextMap";
    
    public static final String JAXWS_OUTBOUND_SOAP_HEADERS  = 
        org.apache.axis2.Constants.JAXWS_OUTBOUND_SOAP_HEADERS;
    public static final String JAXWS_INBOUND_SOAP_HEADERS   = 
        org.apache.axis2.Constants.JAXWS_INBOUND_SOAP_HEADERS;
    /**
     * Value that can be set on a MessageContext.  The property value should be a Boolean()
     * 
     * If set to false, then JAXB streaming of the XML body is disabled.
     * A value of false will result in slower performance for unmarshalling JAXB objects
     * but is a loss-less transformation.  
     *  
     * A value of true will cause the JAXB objects to be created when the XML body is initially 
     * parsed, which is more performant, but it may loose some information contained in the 
     * original XML such as namespace prefixes if the XML stream is recreated from the JAXB 
     * objects.
     * 
     * The default value is Boolean(true) if this property is not set.  
     */
    public static final String JAXWS_ENABLE_JAXB_PAYLOAD_STREAMING = 
        "org.apache.axis2.jaxws.enableJAXBPayloadStreaming";
    public static final String MEP_CONTEXT = 
        "org.apache.axis2.jaxws.handler.MEPContext";
    
    /**
     * If a checked exception is thrown by the webservice's webmethod, then
     * the name of the checked exception is placed in the outbound response context.
     */
    public static final String CHECKED_EXCEPTION =
        "org.apache.axis2.jaxws.checkedException";
    
    /**
     * If an exception is thrown by the JAXWS webservice's webmethod, the 
     * Throwable object is placed in the service outbound response context.
     */
    public static final String JAXWS_WEBMETHOD_EXCEPTION = 
        org.apache.axis2.Constants.JAXWS_WEBMETHOD_EXCEPTION;
    
    /**
     * This constant introduces an extension for @BindingType annotation.
     * When the value of BindingType annotation is set to this constant,
     * the javax.xml.ws.Provider java endpoints will cater to SOAP11 and SOAP12
     * messages.
     */
    public static final String SOAP_HTTP_BINDING ="SOAP_HTTP_BINDING";
}
