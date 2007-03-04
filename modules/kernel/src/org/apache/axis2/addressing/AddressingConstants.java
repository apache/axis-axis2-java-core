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

import javax.xml.namespace.QName;

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

    // ====================== WSDL Binding Constants ========================
    public static final String USING_ADDRESSING = "UsingAddressing";
    public static final String ANONYMOUS = "Anonymous";

    // ====================== Addressing Requirement Levels ==================
    // These are used to represent the requirement level on WS-Addressing indicated
    // in a services.xml or a WSDL file.
    // ADDRESSING_UNSPECIFIED is the equivalent of no UsingAddressing flag in a
    // WSDL file and the default of the WSAddressingRequred attribute in service.xml
    public static final String ADDRESSING_UNSPECIFIED = "unspecified";
    // ADDRESSING_OPTIONAL is the equivalent of <wsaw:UsingAddressing required="false" />
    // in a WSDL file
    public static final String ADDRESSING_OPTIONAL = "optional";
    // ADDRESSING_REQUIRED is the equivalent of <wsaw:UsingAddressing required="true" />
    // in a WSDL file
    public static final String ADDRESSING_REQUIRED = "required";

    // If this property is set, addressing headers will be replaced from the information in the
    // message context.  
    public static final String REPLACE_ADDRESSING_HEADERS = "ReplaceAddressingHeaders";

    // this property once set to Boolean.TRUE will make the messages to skip Addressing Handler.
    // So you will not see Addressing Headers in the OUT path.
    public static final String DISABLE_ADDRESSING_FOR_OUT_MESSAGES =
            "disableAddressingForOutMessages";

    public static final String ADD_MUST_UNDERSTAND_TO_ADDRESSING_HEADERS =
            "addMustUnderstandToAddressingHeaders";

    /**
     * A property pointing to an ArrayList of OMAttribute objects representing any attributes
     * of the wsa:Action header.
     */
    public static final String ACTION_ATTRIBUTES = "actionAttributes";
    /**
     * A property pointing to an ArrayList of OMAttribute objects representing any attributes
     * of the wsa:MessageID header.
     */
    public static final String MESSAGEID_ATTRIBUTES = "messageidAttributes";

    /**
     * When set to Boolean.TRUE this will cause the addressing out handler to output all
     * populated addressing headers in a message, including any optional ones.
     */
    public static final String INCLUDE_OPTIONAL_HEADERS = "includeOptionalHeaders";

    /**
     * This property, if set to Boolean.TRUE, will mean that the addressing handler allows partially
     * ws-addressed messages to be sent even if they are then invalid rather than throwing a fault.
     * <p/>
     * It is not clear how necessary this property is and it may be removed before the next release if
     * it is not seen to be necessary - davidillsley@apache.org
     */
    public static final String DISABLE_OUTBOUND_ADDRESSING_VALIDATION =
            "disableAddressingOutboundValidation";

    public static final String WSAW_ANONYMOUS_PARAMETER_NAME = "wsawAnonymous";

    // ======================== Common Faults ==============================
    public static final String FAULT_ACTION_NOT_SUPPORTED = "ActionNotSupported";
    public static final String FAULT_ACTION_NOT_SUPPORTED_REASON =
            "The [action] cannot be processed at the receiver.";

    public interface Final {

        // ====================== Addressing 1.0 Final Version Constants ====================
        public static final String WSA_NAMESPACE =
                "http://www.w3.org/2005/08/addressing";
        public static final String WSAW_NAMESPACE =
                "http://www.w3.org/2006/05/addressing/wsdl";
        /**
         * @deprecated use {@link #WSA_DEFAULT_RELATIONSHIP_TYPE} instead.
         */
        public static final String WSA_RELATES_TO_RELATIONSHIP_TYPE_DEFAULT_VALUE =
                "http://www.w3.org/2005/08/addressing/reply";
        public static final String WSA_IS_REFERENCE_PARAMETER_ATTRIBUTE = "IsReferenceParameter";
        public static final String WSA_ANONYMOUS_URL =
                "http://www.w3.org/2005/08/addressing/anonymous";
        public static final String WSA_NONE_URI =
                "http://www.w3.org/2005/08/addressing/none";
        public static final String WSA_FAULT_ACTION =
                "http://www.w3.org/2005/08/addressing/fault";
        public static final String WSA_SOAP_FAULT_ACTION =
                "http://www.w3.org/2005/08/addressing/soap/fault";
        public static final String WSA_TYPE_ATTRIBUTE_VALUE = "true";
        public static final String WSA_SERVICE_NAME_ENDPOINT_NAME = "EndpointName";
        public static final String WSA_POLICIES = "Policies";
        public static final String WSA_METADATA = "Metadata";

        public static final String WSA_INTERFACE_NAME = "InterfaceName";

        public static final String WSA_DEFAULT_RELATIONSHIP_TYPE =
                "http://www.w3.org/2005/08/addressing/reply";

        // fault information
        public static final String FAULT_HEADER_PROB_HEADER_QNAME = "ProblemHeaderQName";
        public static final String FAULT_HEADER_PROB_HEADER = "ProblemHeader";
        public static final String FAULT_HEADER_DETAIL = "FaultDetail";
        public static final String FAULT_INVALID_HEADER = "InvalidAddressingHeader";
        public static final String FAULT_INVALID_HEADER_REASON =
                "A header representing a Message Addressing Property is not valid and the message cannot be processed";
        public static final String FAULT_ADDRESSING_HEADER_REQUIRED =
                "MessageAddressingHeaderRequired";
        public static final String FAULT_ADDRESSING_HEADER_REQUIRED_REASON =
                "A required header representing a Message Addressing Property is not present";
        public static final String FAULT_INVALID_CARDINALITY = "InvalidCardinality";
        public static final String FAULT_ONLY_ANONYMOUS_ADDRESS_SUPPORTED =
                "OnlyAnonymousAddressSupported";
        public static final String FAULT_ONLY_NON_ANONYMOUS_ADDRESS_SUPPORTED =
                "OnlyNonAnonymousAddressSupported";
        public static final String FAULT_PROBLEM_ACTION_NAME = "ProblemAction";

        public static final QName WSAW_USING_ADDRESSING =
                new QName(WSAW_NAMESPACE, USING_ADDRESSING);
        public static final QName WSAW_ANONYMOUS = new QName(WSAW_NAMESPACE, USING_ADDRESSING);
    }


    public interface Submission {

        // ====================== Addressing Submission Version Constants ===================
        public static final String WSA_NAMESPACE =
                "http://schemas.xmlsoap.org/ws/2004/08/addressing";
        /**
         * @deprecated use {@link #WSA_DEFAULT_RELATIONSHIP_TYPE} instead.
         */
        public static final String WSA_RELATES_TO_RELATIONSHIP_TYPE_DEFAULT_VALUE = "wsa:Reply";
        public static final String WSA_DEFAULT_RELATIONSHIP_TYPE = "wsa:Reply";
        public static final String WSA_ANONYMOUS_URL =
                "http://schemas.xmlsoap.org/ws/2004/08/addressing/role/anonymous";

        public static final String EPR_REFERENCE_PROPERTIES = "ReferenceProperties";
        public static final String WSA_FAULT_ACTION =
                "http://schemas.xmlsoap.org/ws/2004/08/addressing/fault";

        // fault information
        public static final String FAULT_INVALID_HEADER = "InvalidMessageInformationHeader";
        public static final String FAULT_INVALID_HEADER_REASON =
                "A message information header is not valid and the message cannot be processed. The validity failure can be either structural or semantic, e.g. a [destination] that is not a URI or a [relationship] to a [message id] that was never issued.";
        public static final String FAULT_ADDRESSING_HEADER_REQUIRED =
                "MessageInformationHeaderRequired";
        public static final String FAULT_ADDRESSING_HEADER_REQUIRED_REASON =
                "A required message information header, To, MessageID, or Action, is not present.";

        public static final QName WSAW_USING_ADDRESSING =
                new QName(WSA_NAMESPACE, USING_ADDRESSING);
    }
}
