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
    public static final String BODY_FAULT_LOCAL_NAME = "Fault";

    // Attribute names of a SOAP Envelope
    public static final String ATTR_ACTOR = "actor";
    public static final String ATTR_MUSTUNDERSTAND = "mustUnderstand";

    public static final String SOAPFAULT_LOCAL_NAME = "Fault";
    public static final String SOAPFAULT_CODE_LOCAL_NAME = "faultcode";
    public static final String SOAPFAULT_STRING_LOCAL_NAME = "faultstring";
    public static final String SOAPFAULT_ACTOR_LOCAL_NAME = "faultactor";
    public static final String SOAPFAULT_DETAIL_LOCAL_NAME = "detail";
    public static final String SOAPFAULT_NAMESPACE_URI = SOAP_ENVELOPE_NAMESPACE_URI;
    public static final String SOAPFAULT_NAMESPACE_PREFIX = SOAPENVELOPE_NAMESPACE_PREFIX;

    //OMBuilder constants
    public static final short PUSH_TYPE_BUILDER=0;
    public static final short PULL_TYPE_BUILDER=1;

    public static final String ARRAY_ITEM_NSURI = "http://axis.apache.org/encoding/Arrays";
    public static final String ARRAY_ITEM_LOCALNAME = "item";
    public static final String ARRAY_ITEM_NS_PREFIX = "arrays";
    public static final String ARRAY_ITEM_QNAME = OMConstants.ARRAY_ITEM_NS_PREFIX + ":" +OMConstants.ARRAY_ITEM_LOCALNAME;
}
