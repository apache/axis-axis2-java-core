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
package org.apache.axis.om;

/**
 * Interface OMConstants
 */
public interface OMConstants {
    /**
     * Field SOAP_ENVELOPE_NAMESPACE_URI
     */
    public static final String SOAP_ENVELOPE_NAMESPACE_URI =
            "http://schemas.xmlsoap.org/soap/envelope/";

    /**
     * Field SOAPENVELOPE_NAMESPACE_PREFIX
     */
    public static final String SOAPENVELOPE_NAMESPACE_PREFIX = "soapenv";

    /**
     * Field SOAPENVELOPE_LOCAL_NAME
     */
    public static final String SOAPENVELOPE_LOCAL_NAME = "Envelope";

    // Header constants
    /**
     * Field HEADER_NAMESPACEURI
     */
    public static final String HEADER_NAMESPACEURI =
            SOAP_ENVELOPE_NAMESPACE_URI;

    /**
     * Field HEADER_LOCAL_NAME
     */
    public static final String HEADER_LOCAL_NAME = "Header";

    /**
     * Field HEADER_NAMESPACE_PREFIX
     */
    public static final String HEADER_NAMESPACE_PREFIX =
            SOAPENVELOPE_NAMESPACE_PREFIX;

    // Body Constants
    /**
     * Field BODY_NAMESPACE_URI
     */
    public static final String BODY_NAMESPACE_URI = SOAP_ENVELOPE_NAMESPACE_URI;

    /**
     * Field BODY_LOCAL_NAME
     */
    public static final String BODY_LOCAL_NAME = "Body";

    /**
     * Field BODY_NAMESPACE_PREFIX
     */
    public static final String BODY_NAMESPACE_PREFIX =
            SOAPENVELOPE_NAMESPACE_PREFIX;

    /**
     * Field BODY_FAULT_LOCAL_NAME
     */
    public static final String BODY_FAULT_LOCAL_NAME = "Fault";

    // Attribute names of a SOAP Envelope
    /**
     * Field ATTR_ACTOR
     */
    public static final String ATTR_ACTOR = "actor";

    /**
     * Field ATTR_MUSTUNDERSTAND
     */
    public static final String ATTR_MUSTUNDERSTAND = "mustUnderstand";

    /**
     * Field SOAPFAULT_LOCAL_NAME
     */
    public static final String SOAPFAULT_LOCAL_NAME = "Fault";

    /**
     * Field SOAPFAULT_CODE_LOCAL_NAME
     */
    public static final String SOAPFAULT_CODE_LOCAL_NAME = "faultcode";

    /**
     * Field SOAPFAULT_STRING_LOCAL_NAME
     */
    public static final String SOAPFAULT_STRING_LOCAL_NAME = "faultstring";

    /**
     * Field SOAPFAULT_ACTOR_LOCAL_NAME
     */
    public static final String SOAPFAULT_ACTOR_LOCAL_NAME = "faultactor";

    /**
     * Field SOAPFAULT_DETAIL_LOCAL_NAME
     */
    public static final String SOAPFAULT_DETAIL_LOCAL_NAME = "detail";

    /**
     * Field SOAPFAULT_NAMESPACE_URI
     */
    public static final String SOAPFAULT_NAMESPACE_URI =
            SOAP_ENVELOPE_NAMESPACE_URI;

    /**
     * Field SOAPFAULT_NAMESPACE_PREFIX
     */
    public static final String SOAPFAULT_NAMESPACE_PREFIX =
            SOAPENVELOPE_NAMESPACE_PREFIX;

    // OMBuilder constants
    /**
     * Field PUSH_TYPE_BUILDER
     */
    public static final short PUSH_TYPE_BUILDER = 0;

    /**
     * Field PULL_TYPE_BUILDER
     */
    public static final short PULL_TYPE_BUILDER = 1;

    /**
     * Field ARRAY_ITEM_NSURI
     */
    public static final String ARRAY_ITEM_NSURI =
            "http://axis.apache.org/encoding/Arrays";

    /**
     * Field ARRAY_ITEM_LOCALNAME
     */
    public static final String ARRAY_ITEM_LOCALNAME = "item";

    /**
     * Field ARRAY_ITEM_NS_PREFIX
     */
    public static final String ARRAY_ITEM_NS_PREFIX = "arrays";

    /**
     * Field ARRAY_ITEM_QNAME
     */
    public static final String ARRAY_ITEM_QNAME =
            OMConstants.ARRAY_ITEM_NS_PREFIX + ':'
                    + OMConstants.ARRAY_ITEM_LOCALNAME;
}
