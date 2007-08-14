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
package org.apache.axis2.jaxws.message;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.ws.http.HTTPBinding;
import javax.xml.ws.soap.SOAPBinding;

/**
 * Protocol Each message has a protocol (soap11, soap12, rest) This enum represents the protocol
 * within the Message sub-component
 */
public enum Protocol {
    soap11, soap12, rest, unknown;

    private static final Log log = LogFactory.getLog(Protocol.class);

    // These namespaces are used in the WSDL document to indentify a 
    // SOAP 1.1 vs. a SOAP 1.2 binding
    private static final String SOAP11_WSDL_BINDING = "http://schemas.xmlsoap.org/wsdl/soap";
    private static final String SOAP12_WSDL_BINDING = "http://schemas.xmlsoap.org/wsdl/soap12";

    /**
     * Return the right value for the Protocol based on the binding URL that was passed in.
     *
     * @param url
     * @return Protocol or null
     */
    public static Protocol getProtocolForBinding(String url) {
        boolean debug = log.isDebugEnabled();
        if (debug) {
            log.debug("Configuring message protocol for binding [" + url + "]");
        }

        if (namespaceEquals(Protocol.SOAP11_WSDL_BINDING, url) ||
                namespaceEquals(SOAPBinding.SOAP11HTTP_BINDING, url) ||
                namespaceEquals(SOAPBinding.SOAP11HTTP_MTOM_BINDING, url)) {
            if (debug) {
                log.debug("SOAP 1.1 protocol configured for message");
            }
            return Protocol.soap11;
        } else if (namespaceEquals(Protocol.SOAP12_WSDL_BINDING, url) ||
                namespaceEquals(SOAPBinding.SOAP12HTTP_BINDING, url) ||
                namespaceEquals(SOAPBinding.SOAP12HTTP_MTOM_BINDING, url)) {
            if (debug) {
                log.debug("SOAP 1.2 protocol configured for message");
            }
            return Protocol.soap12;
        } else if (namespaceEquals(HTTPBinding.HTTP_BINDING, url)) {
            if (debug) {
                log.debug("XML/HTTP protocol configured for message");
            }
            return Protocol.rest;
        } else {
            if (debug) {
                log.debug("Protocol was not found for:" + url);
            }
            return null;
        }
    }

    /*
    * Check to see if the two strings (namespaces) passed in are the same, but
    * also accounts for any trailing "/" characters in the string.
    */
    private static boolean namespaceEquals(String target, String input) {
        if (target.equals(input)) {
            return true;
        } else if ((target + "/").equals(input)) {
            return true;
        }

        return false;
    }
}
