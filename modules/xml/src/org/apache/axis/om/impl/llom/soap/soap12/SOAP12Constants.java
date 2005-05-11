package org.apache.axis.om.impl.llom.soap.soap12;

import org.apache.axis.om.impl.llom.soap.SOAPConstants;

/**
 * Copyright 2001-2004 The Apache Software Foundation.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * <p/>
 */
public interface SOAP12Constants extends SOAPConstants{
    /**
     * Eran Chinthaka (chinthaka@apache.org)
     *
     */

    public String SOAP_ENVELOPE_NAMESPACE_URI =
            "http://www.w3.org/2003/05/soap-envelope";

    public static final String SOAP_ENVELOPE_LOCAL_NAME = "Envelope";

    public static final String SOAP_HEADER_LOCAL_NAME = "Header";

    public static final String SOAP_ROLE = "role";
    public static final String SOAP_MUSTUNDERSTAND = "mustUnderstand";
    public static final String SOAP_RELAY = "relay";

    public static final String SOAP_BODY_LOCAL_NAME = "Body";

    // SOAP Faults
    public static final String SOAP_FAULT_LOCAL_NAME = "Fault";

    // SOAP Fault Code
    public static final String SOAP_FAULT_CODE_LOCAL_NAME = "Code";
    public static final String SOAP_FAULT_SUB_CODE_LOCAL_NAME = "SubCode";
    public static final String SOAP_FAULT_VALUE_LOCAL_NAME = "Value";

    // SOAP Fault Codes
    public static final String SOAP_FAULT_VALUE_VERSION_MISMATCH = "env:VersionMismatch";
    public static final String SOAP_FAULT_VALUE_MUST_UNDERSTAND = "env:MustUnderstand";
    public static final String SOAP_FAULT_VALUE_DATA_ENCODING_UKNOWN = "env:DataEncodingUnknown";
    public static final String SOAP_FAULT_VALUE_SENDER = "env:Sender";
    public static final String SOAP_FAULT_VALUE_RECEIVER = "env:Receiver";

    // SOAP Fault Reason
    public static final String SOAP_FAULT_REASON_LOCAL_NAME = "Reason";
    public static final String SOAP_FAULT_TEXT_LOCAL_NAME = "Text";
    public static final String SOAP_FAULT_TEXT_LANG_ATTR_LOCAL_NAME = "lang";
    public static final String SOAP_FAULT_TEXT_LANG_ATTR_NS_URI = "http://www.w3.org/XML/1998/namespace";
    public static final String SOAP_FAULT_TEXT_LANG_ATTR_NS_PREFIX = "xml";

    // SOAP Fault Node
    public static final String SOAP_FAULT_NODE_LOCAL_NAME = "Node";

    // SOAP Fault Detail
    public static final String SOAP_FAULT_DETAIL_LOCAL_NAME = "Detail";
}
