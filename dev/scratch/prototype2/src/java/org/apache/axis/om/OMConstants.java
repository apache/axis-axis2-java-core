package org.apache.axis.om;

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
 */
public interface OMConstants {

    public static final String SOAP_ENVELOPE_NAMESPACE_URI = "http://schemas.xmlsoap.org/soap/envelope/";
    public static final String SOAPENVELOPE_NAMESPACE_PREFIX = "soapenv";
    public static final String SOAPENVELOPE_LOCAL_NAME = "Envelope";

    // Header constants
    public static final String HEADER_NAMESPACEURI = SOAP_ENVELOPE_NAMESPACE_URI;
    public static final String HEADER_LOCAL_NAME = "Header";
    public static final String HEADER_NAMESPACE_PREFIX = SOAPENVELOPE_NAMESPACE_PREFIX;

    // Body Constants
    public static final String BODY_NAMESPACE_URI = SOAP_ENVELOPE_NAMESPACE_URI;
    public static final String BODY_LOCAL_NAME = "Body";
    public static final String BODY_NAMESPACE_PREFIX = SOAPENVELOPE_NAMESPACE_PREFIX;

    // Attribute names of a SOAP Envelope
    public static final String ATTR_ACTOR = "actor";
    public static final String ATTR_MUSTUNDERSTAND = "mustUnderstand";

    public static final String SOAPFAULT_LOCAL_NAME = "Fault";
    public static final String SOAPFAULT_NAMESPACE_URI = SOAP_ENVELOPE_NAMESPACE_URI;
    public static final String SOAPFAULT_NAMESPACE_PREFIX = SOAPENVELOPE_NAMESPACE_PREFIX;
}
