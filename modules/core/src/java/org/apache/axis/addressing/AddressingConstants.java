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
package org.apache.axis.addressing;

/**
 * Interface AddressingConstants
 */
public interface AddressingConstants {

    public static final String WSA_NAMESPACE = "http://schemas.xmlsoap.org/ws/2004/08/addressing";
    
    /**
     * Field WSA_MESSAGE_ID
     */
    public static final String WSA_MESSAGE_ID = "MessageID";

    /**
     * Field WSA_RELATES_TO
     */
    public static final String WSA_RELATES_TO = "RelatesTo";

    /**
     * Field WSA_RELATES_TO_RELATIONSHIP_TYPE
     */
    public static final String WSA_RELATES_TO_RELATIONSHIP_TYPE =
            "RelationshipType";

    /**
     * Field WSA_TO
     */
    public static final String WSA_TO = "To";

    /**
     * Field WSA_ACTION
     */
    public static final String WSA_ACTION = "Action";

    /**
     * Field WSA_FROM
     */
    public static final String WSA_FROM = "From";

    /**
     * Field WSA_REPLY_TO
     */
    public static final String WSA_REPLY_TO = "ReplyTo";

    /**
     * Field WSA_FAULT_TO
     */
    public static final String WSA_FAULT_TO = "FaultTo";

    public static final String EPR_ADDRESS = "Address";
    public static final String EPR_REFERENCE_PROPERTIES = "ReferenceProperties";
    public static final String EPR_REFERENCE_PARAMETERS = "ReferenceParameters";
    public static final String EPR_PORT_TYPE = "PortType";
    public static final String EPR_SERVICE_NAME = "ServiceName";
}
