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


package org.apache.axis2;

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

    /**
     * Field APPLICATION_SCOPE
     */
    public static final String SCOPE_APPLICATION = "application";
    public static final String SCOPE_SOAP_SESSION = "soapsession";
    public static final String SCOPE_TRANSPORT_SESSION = "transportsession";
    public static final String SCOPE_REQUEST = "request";

    public static final String HOME_AXIS2 = "axis2.home";
    public static final String CONF_AXIS2 = "axis2.xml";
    public static final String HOME_USER = "user.home";

    /**
     * Field TRANSPORT_TCP
     */
    public static final String TRANSPORT_TCP = "tcp";
    public static final String TRANSPORT_MAIL = "mail";
    public static final String TRANSPORT_LOCAL = "local";
    public static final String TRANSPORT_JMS = "jms";

    /**
     * Field TRANSPORT_HTTP
     */
    public static final String TRANSPORT_HTTP = "http";

    //Parameter name of Service impl class
    public static final String SERVICE_CLASS = "ServiceClass";

    /**
     * Field REQUEST_URL_PREFIX
     */
    public static final String REQUEST_URL_PREFIX = "/services";
    public static final String LOGOUT = "logout";
    public static final String LIST_SERVICE_FOR_MODULE_ENGAGEMENT = "listoperation";
    public static final String LIST_SERVICES = "listServices";
    public static final String LIST_PHASES = "listPhases";
    public static final String LIST_MODULES = "listModules";
    public static final String LIST_GLOABLLY_ENGAGED_MODULES = "globalModules";
    public static final String LIST_CONTEXTS = "listContexts";
    public static final String ENGAGE_MODULE_TO_SERVICE_GROUP = "engageToServiceGroup";
    public static final String ENGAGE_MODULE_TO_SERVICE = "engageToService";
    public static final String ENGAGE_GLOBAL_MODULE = "engagingglobally";
    public static final String ADMIN_LOGIN = "adminlogin";

    /**
     * List service for admin pages
     */
    public static final String ADMIN_LISTSERVICES = "listService";
    public static final String VIEW_GLOBAL_HANDLERS = "viewGlobalHandlers";
    public static final String SELECT_SERVICE_FOR_PARA_EDIT = "selectServiceParaEdit";
    public static final String SELECT_SERVICE = "selectService";
    public static final String EDIR_SERVICE_PARA = "editServicepara";
    public static final String VIEW_SERVICE_HANDLERS = "viewServiceHandlers";
    public static final String USER_NAME = "userName";

    /**
     * Field SINGLE_SERVICE
     */
    public static final String SINGLE_SERVICE = "singleservice";
    public static final String SERVLET_CONTEXT = "servletContext";
    public static final String SERVICE_MAP = "servicemap";
    public static final String SERVICE_HANDLERS = "serviceHandlers";
    public static final String SERVICE_GROUP_MAP = "serviceGroupmap";
    public static final String SERVICE = "service";
    public static final String SELECT_SERVICE_TYPE = "SELECT_SERVICE_TYPE";
    public static final String REMOVE_SERVICE = "removeService";
    public static final String PHASE_LIST = "phaseList";
    public static final String PASSWORD = "password";
    public static final String OPERATION_MAP = "operationmap";
    public static final String MODULE_MAP = "modulemap";
    public static final String MODULE_ADDRESSING = "addressing";
    public static final String LIST_SERVICE_GROUPS = "listServciceGroups";
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
    public static final String WSA_ACTION = "wsamapping";
    public static final String VALUE_TRUE = "true";
    public static final String VALUE_FALSE = "false";
    public static final String TESTING_PATH = "target/test-resources/";
    public static final String TESTING_REPOSITORY = TESTING_PATH + "samples";
    public static final char SERVICE_NAME_SPLIT_CHAR = ':';
    public static final String SERVICE_GROUP_ID = "ServiceGroupId";
    public static final String RESPONSE_WRITTEN = "CONTENT_WRITTEN";

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
    public static final String AXIS2_NAMESPACE_URI =
            "http://ws.apache.org/namespaces/axis2";
    public static final String AXIS2_NAMESPACE_PREFIX = "axis2";
    public static final String ADDRESSING_TO = "WS-Addressing:To";
    public static final String ADDRESSING_REPLY_TO = "WS-Addressing:ReplyTo";
    public static final String ADDRESSING_RELATES_TO = "WS-Addressing:RelatesTo";
    public static final String ADDRESSING_MESSAGE_ID = "WS-Addressing:MessageId";
    public static final String ADDRESSING_FROM = "WS-Addressing:From";
    public static final String ADDRESSING_FAULT_TO = "WS-Addressing:FaultTo";

    //to set and get the property from service context
    public static final String COOKIE_STRING = "Cookie";

    //See

    /**
     * Addressing Constants
     */
    public static final String ADDRESSING_ACTION = "WS-Addressing:Action";

    public static interface Configuration {
        public static final String ENABLE_REST = "enableREST";
        public static final String ENABLE_REST_THROUGH_GET = "restThroughGet";

        // globally enable MTOM
        public static final String ENABLE_MTOM = "enableMTOM";
        public static final String CACHE_ATTACHMENTS = "cacheAttachments";
        public static final String ATTACHMENT_TEMP_DIR = "attachmentDIR";
        public static final String FILE_SIZE_THRESHOLD = "sizeThreshold";
        public static final String HTTP_METHOD_GET = "GET";
        public static final String HTTP_METHOD = "HTTP_METHOD";
        public static final String HTTP_METHOD_POST = "POST";

        // we need to some times send the message as multipart/related, even if there is no MTOM stuff in the envelope.
        public static final String FORCE_MIME = "ForceMimeBoundary";
        public static final String CONTENT_TYPE = "ContentType";
        public static final String IS_USING_SEPARATE_LISTENER = "IsUsingSeparateListener";

        // this property once set to Boolean.TRUE will make the messages to skip Addressing Handler.
        // So you will not see Addressing Headers in the OUT path.
        public static final String DISABLE_ADDRESSING_FOR_OUT_MESSAGES = "disableAddressingForOutMessages";

        public static final String TRANSPORT_IN_URL = "TransportInURL";
    }
}
