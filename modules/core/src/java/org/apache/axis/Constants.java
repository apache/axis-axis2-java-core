/*
 * Copyright 2003,2004 The Apache Software Foundation.
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

public class Constants {
    public static final int SOAP_STYLE_RPC_ENCODED = 1000;
    public static final int SOAP_STYLE_RPC_LITERAL = 1001;
    public static final int SOAP_STYLE_DOC_LITRAL_WRAPPED = 1002;

    public static final String APPLICATION_SCOPE = "application";
    public static final String SESSION_SCOPE = "session";
    public static final String GLOBAL_SCOPE = "global";

    public static final String PHASE_SERVICE = "service";
    public static final String PHASE_TRANSPORT = "transport";
    public static final String PHASE_GLOBAL = "global";

    public static final String SESSION_CONTEXT_PROPERTY = "SessionContext";

    public static final String TRANSPORT_TCP = "tcp";
    public static final String TRANSPORT_HTTP = "http";
    public static final String TRANSPORT_SMTP= "smtp";

}
