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
    public static final String MESSAGE_SCOPE = "message";

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
    public static final String TRANSPORT_MAIL = "mail";

    public static final String TRANSPORT_JMS = "jms";

    /**
     * Field LISTSERVICES
     */
    public static final String LISTSERVICES = "listServices";

    /**
     * List service for adminpagse
     */
    public static final String ADMIN_LISTSERVICES = "listService";

    public static final String LIST_MODULES = "listModules";

    public static final String LIST_GLOABLLY_ENGAGED_MODULES = "globalModules";

    public static final String LIST_PHASES = "listPhases";

    public static final String ENGAGE_GLOBAL_MODULE = "engagingglobally";
    public static final String ENGAGE_MODULE_TO_SERVICE = "engageToService";

    public static final String ADMIN_LOGGING = "adminloging";

    public static final String VIEW_GLOBAL_HANDLERS = "viewGlobalHandlers";
    public static final String SELECT_SERVICE = "selectService";
    public static final String VIEW_SERVICE_HANDLERS = "viewServiceHandlers";

    /**
     * Field SERVICE_MAP
     */
    public static final String SERVICE_MAP = "servicemap";
    /**
     * Field Available modules
     */
    public static final String MODULE_MAP = "modulemap";

    public static final String GLOBAL_HANDLERS = "axisconfig";
    public static final String SERVICE_HANDLERS = "serviceHandlers";

    public static final String PHASE_LIST = "phaseList";

    public static final String ENGAGE_STATUS = "engagestatus";

    /**
     * Errorness servcie
     */
    public static final String ERROR_SERVICE_MAP = "errprservicemap";

    public static final String IS_FAULTY = "Fault";

    public static final String MODULE_ADDRESSING = "addressing";

    public static final String USER_NAME = "userName";
    public static final String PASSWORD = "password";

    /**
     * Field SINGLE_SERVICE
     */
    public static final String SINGLE_SERVICE = "singleservice";

    /**
     * Field METHOD_NAME_ESCAPE_CHARACTOR
     */
    public static final char METHOD_NAME_ESCAPE_CHARACTOR = '?';

    public static final String LOGGED = "Logged";

    public static interface SOAP {
        public static final String SOAP_12_CONTENT_TYPE = "application/soap+xml";
        public static final String SOAP_11_CONTENT_TYPE = "text/xml";
        public static final String MTOM_CONTENT_TYPE = "text/xml";
    }

    public static interface Configuration {
        public static final String DO_REST = "doREST";
        public static final String ENABLE_REST = "eanbleREST";
        public static final String ENABLE_MTOM = "eanbleMTOM";
        public static final String DO_MTOM = "doMTOM";
    }
    public static final String VALUE_TRUE = "true";
    public static final String VALUE_FALSE = "false";
    public static final String CONTAINER_MANAGED = "ContainerManaged";

}
