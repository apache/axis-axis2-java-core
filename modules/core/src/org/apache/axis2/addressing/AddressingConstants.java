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


package org.apache.axis2.addressing;

/**
 * Interface AddressingConstants
 */
public interface AddressingConstants {

    // ====================== Common Message Addressing Properties ===================
    public static final String WSA_MESSAGE_ID = "MessageID";
    public static final String WSA_RELATES_TO = "RelatesTo";
    public static final String WSA_RELATES_TO_RELATIONSHIP_TYPE = "RelationshipType";
    public static final String WSA_TO = "To";
    public static final String WSA_REPLY_TO = "ReplyTo";
    public static final String WSA_FROM = "From";
    public static final String WSA_FAULT_TO = "FaultTo";
    public static final String WSA_ACTION = "Action";
    public static final String EPR_SERVICE_NAME = "ServiceName";
    public static final String EPR_REFERENCE_PARAMETERS = "ReferenceParameters";

    // ====================== Common EPR Elements ============================
    public static final String EPR_ADDRESS = "Address";
    public static final String WS_ADDRESSING_VERSION = "WSAddressingVersion";
    public static final String WSA_DEFAULT_PREFIX = "wsa";
    public static final String PARAM_SERVICE_GROUP_CONTEXT_ID =
            "ServiceGroupContextIdFromAddressing";
    public static final String IS_ADDR_INFO_ALREADY_PROCESSED = "IsAddressingProcessed";

    public interface Final {

        // ====================== Addressing 1.0 Final Version Constants ====================
        public static final String WSA_NAMESPACE =
                "http://www.w3.org/2005/08/addressing";
        public static final String WSAW_NAMESPACE =
                "http://www.w3.org/2005/08/addressing/wsdl";
        public static final String WSA_RELATES_TO_RELATIONSHIP_TYPE_DEFAULT_VALUE =
                "http://www.w3.org/2005/08/addressing/reply";
        public static final String WSA_IS_REFERENCE_PARAMETER_ATTRIBUTE = "IsReferenceParameter";
        public static final String WSA_ANONYMOUS_URL =
                "http://www.w3.org/2005/08/addressing/anonymous";
        public static final String WSA_NONE_URI =
                "http://www.w3.org/2005/08/addressing/none";
        public static final String WSA_FAULT_ACTION = "http://www.w3.org/2005/08/addressing/fault";
        public static final String WSA_TYPE_ATTRIBUTE_VALUE = "true";
        public static final String WSA_SERVICE_NAME_ENDPOINT_NAME = "EndpointName";
        public static final String WSA_POLICIES = "Policies";
        public static final String WSA_METADATA = "Metadata";

        public static final String WSA_INTERFACE_NAME = "InterfaceName";

        // fault information
        public static final String FAULT_HEADER_PROB_HEADER_QNAME = "ProblemHeaderQName";
        public static final String FAULT_HEADER_PROB_HEADER = "ProblemHeader";
        public static final String FAULT_HEADER_DETAIL = "FaultDetail";
        public static final String FAULT_INVALID_HEADER = "InvalidAddressingHeader";
        public static final String FAULT_ADDRESSING_HEADER_REQUIRED = "MessageAddressingHeaderRequired";

    }


    public interface Submission {

        // ====================== Addressing Submission Version Constants ===================
        public static final String WSA_NAMESPACE =
                "http://schemas.xmlsoap.org/ws/2004/08/addressing";
        public static final String WSA_RELATES_TO_RELATIONSHIP_TYPE_DEFAULT_VALUE = "wsa:Reply";
        public static final String WSA_ANONYMOUS_URL =
                "http://schemas.xmlsoap.org/ws/2004/08/addressing/role/anonymous";

        public static final String EPR_REFERENCE_PROPERTIES = "ReferenceProperties";
    }
}
