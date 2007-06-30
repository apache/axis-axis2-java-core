/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.axis2;

/**
 * Class Constants
 */
public class Constants extends org.apache.axis2.namespace.Constants {

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
     * Field SESSION_SCOPE
     */
    public static final String SESSION_SCOPE = "session";

    /**
     * Field SESSION_CONTEXT_PROPERTY
     */
    public static final String SESSION_CONTEXT_PROPERTY = "SessionContext";

    /**
     * Field PHASE_TRANSPORT
     */
    public static final String PHASE_TRANSPORT = "transport";

    /**
     * Field PHASE_SERVICE
     */
    public static final String PHASE_SERVICE = "service";

    /**
     * Field PHASE_GLOBAL
     */
    public static final String PHASE_GLOBAL = "global";

    /**
     * Field MESSAGE_SCOPE
     */
    public static final String MESSAGE_SCOPE = "message";

    public static final String AXIS_BINDING_OPERATION = "AxisBindingOperation";

    /**
     * To chenge the conetext path from axis2/service to something else
     */
    public static final String PARAM_CONTEXT_ROOT = "contextRoot";
    /**
     * To chenage the service path to somthing else
     */
    public static final String PARAM_SERVICE_PATH = "servicePath";
    //Parameter name for transport session managemntt
    public static final String MANAGE_TRANSPORT_SESSION = "manageTransportSession";

    public static final String HTTP_RESPONSE_STATE = "axis2.http.response.state";
    public static final String HTTP_BASIC_AUTH_REALM = "axis2.authentication.realm";

    /**
     * Field APPLICATION_SCOPE
     */
    public static final String SCOPE_APPLICATION = "application";
    public static final String SCOPE_SOAP_SESSION = "soapsession";
    public static final String SCOPE_TRANSPORT_SESSION = "transportsession";
    public static final String SCOPE_REQUEST = "request";

    public static final String AXIS2_REPO = "axis2.repo";
    public static final String AXIS2_CONF = "axis2.xml";
    public static final String USER_HOME = "user.home";

    /**
     * Field TRANSPORT_TCP
     */
    public static final String TRANSPORT_TCP = "tcp";
    public static final String TRANSPORT_MAIL = "mailto";
    public static final String TRANSPORT_LOCAL = "local";
    public static final String TRANSPORT_JMS = "jms";

    /**
     * Field TRANSPORT_HTTP
     */
    public static final String TRANSPORT_HTTP = "http";

    //Parameter name of Service impl class
    public static final String SERVICE_CLASS = "ServiceClass";
    public static final String SERVICE_OBJECT_SUPPLIER = "ServiceObjectSupplier";
    public static final String SERVICE_TCCL = "ServiceTCCL";

    public static final String TCCL_DEFAULT = "default";
    public static final String TCCL_COMPOSITE = "composite";
    public static final String TCCL_SERVICE = "service";
    public static final String FAULT_NAME = "faultName";

    /**
     * Field REQUEST_URL_PREFIX
     */
    public static final String LIST_PHASES = "listPhases";
    public static final String LIST_MODULES = "listModules";
    public static final String LIST_GLOABLLY_ENGAGED_MODULES = "globalModules";
    public static final String LIST_CONTEXTS = "listContexts";
    public static final String ENGAGE_MODULE_TO_SERVICE_GROUP = "engageToServiceGroup";
    public static final String ENGAGE_MODULE_TO_SERVICE = "engageToService";
    public static final String ENGAGE_GLOBAL_MODULE = "engagingglobally";
    public static final String ADMIN_LOGIN = "adminlogin";
    public static final String AXIS_WEB_CONTENT_ROOT = "/axis2-web/";

    /**
     * List service for admin pages
     */
    public static final String ADMIN_LISTSERVICES = "listService";
    public static final String VIEW_GLOBAL_HANDLERS = "viewGlobalHandlers";
    public static final String SELECT_SERVICE_FOR_PARA_EDIT = "selectServiceParaEdit";
    public static final String SELECT_SERVICE = "selectService";
    public static final String EDIT_SERVICE_PARA = "editServicepara";
    public static final String VIEW_SERVICE_HANDLERS = "viewServiceHandlers";
    public static final String USER_NAME = "userName";
    public static final String ADMIN_SECURITY_DISABLED = "disableAdminSecurity";

    /**
     * Field SINGLE_SERVICE
     */
    public static final String SINGLE_SERVICE = "singleservice";

    /**
     * @deprecated Please use org.apache.axis2.transport.http.HTTPConstants.MC_HTTP_SERVLETCONTEXT
     */
    public static final String SERVLET_CONTEXT = "transport.http.servletContext";
    /**
     * @deprecated Please use org.apache.axis2.transport.http.HTTPConstants.MC_HTTP_SERVLETREQUEST
     */
    public static final String HTTP_SERVLET_REQUEST = "transport.http.servletRequest";

    public static final String SERVICE_MAP = "servicemap";
    public static final String SERVICE_ROOT = "serviceRoot";
    public static final String SERVICE_PATH = "servicePath";
    public static final String SERVICE_HANDLERS = "serviceHandlers";
    public static final String SERVICE_GROUP_MAP = "serviceGroupmap";
    public static final String SERVICE = "service";
    public static final String SELECT_SERVICE_TYPE = "SELECT_SERVICE_TYPE";
    public static final String IN_ACTIVATE_SERVICE = "inActivateService";
    public static final String ACTIVATE_SERVICE = "activateService";
    public static final String PHASE_LIST = "phaseList";
    public static final String PASSWORD = "password";
    public static final String OPERATION_MAP = "operationmap";
    public static final String MODULE_MAP = "modulemap";
    public static final String MODULE_ADDRESSING = "addressing";
    public static final String LIST_SERVICE_GROUPS = "listServiceGroups";
    public static final String LIST_OPERATIONS_FOR_THE_SERVICE = "listOperations";
    public static final String IS_FAULTY = "Fault";
    public static final String GLOBAL_HANDLERS = "axisconfig";

    /**
     * Keys for service/module error maps
     */
    public static final String ERROR_SERVICE_MAP = "errprservicemap";
    public static final String ERROR_MODULE_MAP = "errormodulesmap";
    public static final String ENGAGE_STATUS = "engagestatus";
    public static final String CONFIG_CONTEXT = "config_context";
    public static final String WSDL_CONTENT = "wsdl";
    public static final String ACTION_MAPPING = "actionMapping";
    public static final String OUTPUT_ACTION_MAPPING = "outputActionMapping";
    public static final String FAULT_ACTION_MAPPING = "faultActionMapping";
    public static final String FAULT_ACTION_NAME = "faultName";
    public static final String VALUE_TRUE = "true";
    public static final String VALUE_FALSE = "false";
    public static final String TESTING_PATH = "target/test-resources/";
    public static final String TESTING_REPOSITORY = TESTING_PATH + "samples";
    public static final char SERVICE_NAME_SPLIT_CHAR = ':';
    public static final String SERVICE_GROUP_ID = "ServiceGroupId";
    public static final String RESPONSE_WRITTEN = "RESPONSE_WRITTEN";
    //To have a floag if the replyTo is not annon one
    public static final String DIFFERENT_EPR = "DIFFERENT_EPR";

    /**
     * This can be set in the MessageContext to give an response code the transport should use when sending it out.
     */
    public static final String RESPONSE_CODE = "RESPONSE_CODE";

    /**
     * Transport Info
     */
    public static final String OUT_TRANSPORT_INFO = "OutTransportInfo";

    /**
     * Field METHOD_NAME_ESCAPE_CHARACTER
     */
    public static final char METHOD_NAME_ESCAPE_CHARACTER = '?';
    public static final String LOGGED = "Logged";
    public static final String CONTAINER_MANAGED = "ContainerManaged";

    public static final String FAULT_INFORMATION_FOR_HEADERS = "FaultHeaders";

    /**
     * @deprecated Please use org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING
     */
    public static final String COOKIE_STRING = "Cookie";
    public static final String SESSION_COOKIE = "axis_session";
    public static final String SESSION_COOKIE_JSESSIONID = "JSESSIONID";
    public static final String CUSTOM_COOKIE_ID = "customCookieID";

    /**
     * Addressing Constants
     */
    public static final String ADDRESSING_ACTION = "WS-Addressing:Action";
    public static final String HTTP_FRONTEND_HOST_URL = "httpFrontendHostUrl";
    public static final String DEFAULT_REST_PATH = "rest";
    public static final String DEFAULT_SERVICES_PATH = "services";

    /**
     * Field Builder Selector
     */
    public static final String BUILDER_SELECTOR = "builderselector";

    /**
     * Property name for inbound fault processor to set a fault on the message
     * context to be thrown by the client code in favour of a simple translation
     * from SOAPFault to AxisFault
     */
    public static final String INBOUND_FAULT_OVERRIDE = "inboundFaultOverride";

    public static interface Configuration {
        public static final String ENABLE_REST = "enableREST";
        public static final String ENABLE_REST_THROUGH_GET = "restThroughGet";

        public static final String ARTIFACTS_TEMP_DIR = "artifactsDIR";

        //Attachment configurations
        public static final String ENABLE_MTOM = "enableMTOM";
        public static final String CACHE_ATTACHMENTS = "cacheAttachments";
        public static final String ATTACHMENT_TEMP_DIR = "attachmentDIR";
        public static final String FILE_SIZE_THRESHOLD = "sizeThreshold";
        public static final String ENABLE_SWA = "enableSwA";
        public static final String MIME_BOUNDARY = "mimeBoundary";
        public static final String MM7_COMPATIBLE = "MM7Compatible";
        public static final String MM7_INNER_BOUNDARY = "MM7InnerBoundary";
        public static final String MM7_PART_CID = "MM7PartCID";


        public static final String HTTP_METHOD_GET = "GET";
        public static final String HTTP_METHOD_DELETE = "DELETE";
        public static final String HTTP_METHOD_PUT = "PUT";
        public static final String HTTP_METHOD = "HTTP_METHOD";
        public static final String HTTP_METHOD_POST = "POST";

        public static final String CONTENT_TYPE = "ContentType";

        public static final String CONFIG_CONTEXT_TIMOUT_INTERVAL = "ConfigContextTimeoutInterval";

        public static final String TRANSPORT_IN_URL = "TransportInURL";

        public static final String URL_PARAMETER_LIST = "URLParameterList";
        public static final String URL_HTTP_LOCATION_PARAMS_LIST = "HTTPLocationParamsList";

        public static final String SEND_STACKTRACE_DETAILS_WITH_FAULTS =
                "sendStacktraceDetailsWithFaults";

        public static final String DRILL_DOWN_TO_ROOT_CAUSE_FOR_FAULT_REASON =
                "drillDownToRootCauseForFaultReason";

        public static final String DISABLE_REST = "disableREST";

        // this will contain the keys of all the properties that will be in the message context
        public static final String TRANSPORT_URL = "TransportURL";

        /**
         * @deprecated please use org.apache.axis2.addressing.AddressingConstants.DISABLE_ADDRESSING_FOR_OUT_MESSAGES
         */
        public static final String DISABLE_ADDRESSING_FOR_OUT_MESSAGES =
                "disableAddressingForOutMessages";

        // if this property is set to Boolean.TRUE then the SOAPAction header, if present,
        // will NOT be set to the value of Options.getAction(). The empty value, "", will
        // be used instead.
        public static final String DISABLE_SOAP_ACTION = "disableSoapAction";

        /**
         * Field CHARACTER_SET_ENCODING
         */
        public static final String CHARACTER_SET_ENCODING = "CHARACTER_SET_ENCODING";

        /**
         * If this is set to a Boolean 'true' value, the replyTo value will not be replaced in
         * an OutIn invocation. This is useful for modules that hope to get the reply message in
         * its own manner.
         */
        public static final String USE_CUSTOM_LISTENER = "UseCustomListener";

        /**
         * If this is set to a Boolean 'true' value, then OutIn operations will always be treated
         * as async. This is useful for modules that layer async behaviour on top of sync channels.
         */
        public static final String USE_ASYNC_OPERATIONS = "UseAsyncOperations";

        /**
         * This is used to specify the message format which the message needs to be serializes.
         *
         * @see org.apache.axis2.transport.MessageFormatter
         */
        public static final String MESSAGE_TYPE = "messageType";
        
        public static final String SOAP_RESPONSE_MEP = "soapResponseMEP";
        
        /**
         * This will be used as a key for storing transport information.
         */
        public static final String TRANSPORT_INFO_MAP = "TransportInfoMap";
        
        /**
         * If this is set to a Boolean 'true' value, then RequestResponseTransport instances will
         * not be signalled by the Dispatch phase. This is useful for modules that add wish to
         * send extra messages in the backchannel.
         */
        public static final String DISABLE_RESPONSE_ACK = "DisableResponseAck";
        
    }
}
