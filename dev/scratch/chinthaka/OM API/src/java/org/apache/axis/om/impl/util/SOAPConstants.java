package org.apache.axis.om.impl.util;

/**
 * Copyright 2001-2004 The Apache Software Foundation.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p/>
 * User: Eran Chinthaka - Lanka Software Foundation
 * Date: Nov 8, 2004
 * Time: 3:38:10 PM
 */
public class SOAPConstants {

    public static final String soapEnvelopeNamespaceURI = "http://schemas.xmlsoap.org/soap/envelope/";
    public static final String soapEnvelopeNamespacePrefix = "soapenv";

    // Header constants
    public static final String headerNamespaceURI = soapEnvelopeNamespaceURI;
    public static final String headerLocalName = "Header";
    public static final String headerNamespacePrefix = soapEnvelopeNamespacePrefix;

    // Body Constants
    public static final String bodyNamespaceURI = soapEnvelopeNamespaceURI;
    public static final String bodyLocalName = "Body";
    public static final String bodyNamespacePrefix = soapEnvelopeNamespacePrefix;

    // Attribute names of a SOAP Envelope
    public static final String attrActor = "actor";
    public static final String attrMustUnderstand = "mustUnderstand";
}
