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
package org.apache.axis;

/**
 * Class Constants
 */
public class Constants {
    /**
     * Field SOAP_STYLE_RPC_ENCODED
     */
    public static final int SOAP_STYLE_RPC_ENCODED = 1000;

    /**
     * Field SOAP_STYLE_RPC_LITERAL
     */
    public static final int SOAP_STYLE_RPC_LITERAL = 1001;

    /**
     * Field SOAP_STYLE_DOC_LITERAL_WRAPPED
     */
    public static final int SOAP_STYLE_DOC_LITERAL_WRAPPED = 1002;

    /**
     * Field APPLICATION_SCOPE
     */
    public static final String APPLICATION_SCOPE = "application";

    /**
     * Field SESSION_SCOPE
     */
    public static final String SESSION_SCOPE = "session";

    /**
     * Field GLOBAL_SCOPE
     */
    public static final String GLOBAL_SCOPE = "global";

    /**
     * Field PHASE_SERVICE
     */
    public static final String PHASE_SERVICE = "service";

    /**
     * Field PHASE_TRANSPORT
     */
    public static final String PHASE_TRANSPORT = "transport";

    /**
     * Field PHASE_GLOBAL
     */
    public static final String PHASE_GLOBAL = "global";

    /**
     * Field SESSION_CONTEXT_PROPERTY
     */
    public static final String SESSION_CONTEXT_PROPERTY = "SessionContext";

    /**
     * Field TRANSPORT_TCP
     */
    public static final String TRANSPORT_TCP = "tcp";

    /**
     * Field TRANSPORT_HTTP
     */
    public static final String TRANSPORT_HTTP = "http";

    /**
     * Field TRANSPORT_SMTP
     */
    public static final String TRANSPORT_SMTP = "smtp";

    /**
     * Field LISTSERVICES
     */
    public static final String LISTSERVICES = "listServices";

    /**
     * Field SERVICE_MAP
     */
    public static final String SERVICE_MAP = "servicemap";

    /**
     * Field SINGLE_SERVICE
     */
    public static final String SINGLE_SERVICE = "singleservice";

    /**
     * Field METHOD_NAME_ESCAPE_CHARACTOR
     */
    public static final char METHOD_NAME_ESCAPE_CHARACTOR = '#';
}
