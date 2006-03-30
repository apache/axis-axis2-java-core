package org.apache.ws.java2wsdl;
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
*
* @author : Deepal Jayasinghe (deepal@apache.org)
*
*/

public interface Constants {

    String DEFAULT_SOAP_NAMESPACE_PREFIX = "soap";
    String DEFAULT_WSDL_NAMESPACE_PREFIX = "wsdl";
    String DEFAULT_SCHEMA_NAMESPACE_PREFIX = "xs";
    String BINDING_NAME_SUFFIX = "Binding";
    String PORT_TYPE_SUFFIX = "PortType";
    String PORT_NAME_SUFFIX = "Port";
    String DEFAULT_TARGET_NAMESPACE = "http://ws.apache.org/axis2";
    String WSDL_NAMESPACE = "http://schemas.xmlsoap.org/wsdl/";
    String DEFAULT_TARGET_NAMESPACE_PREFIX = "axis2";

    String AXIS2_XSD = "http://org.apache.axis2/xsd";
    String DEFAULT_SOAP_NAMESPACE = "http://schemas.xmlsoap.org/wsdl/soap/";
    String TARGETNAMESPACE_PREFIX = "tns";
    String SCHEMA_NAME_SPACE = "http://www.w3.org/2001/XMLSchema";
    String MESSAGE_LOCAL_NAME = "message";
    String ATTRIBUTE_NAME = "name";
    String REQUEST_MESSAGE = "RequestMessage";
    String RESPONSE_MESSAGE = "ResponseMessage";
    String MESSAGE_SUFFIX = "Message";
    String REQUEST = "Request";
    String RESPONSE = "Response";
    String PORT_TYPE_LOCAL_NAME = "portType";
    String OPERATION_LOCAL_NAME = "operation";
    String IN_PUT_LOCAL_NAME = "input";
    String OUT_PUT_LOCAL_NAME = "output";
    String SERVICE_LOCAL_NAME = "service";
    String BINDING_LOCAL_NAME = "binding";
    String TRANSPORT_URI = "http://schemas.xmlsoap.org/soap/http";
    String PORT = "port";
    String PART_ATTRIBUTE_NAME = "part";
    String ELEMENT_ATTRIBUTE_NAME = "element";

    String SOAP_ADDRESS = "address";
    String LOCATION = "location";
    String TRANSPORT = "transport";
    String STYLE = "style";
    String SOAP_ACTION = "soapAction";
    String SOAP_BODY = "body";
    String SOAP_USE = "use";
    String DOCUMNT = "document";
    String LITERAL = "literal";
    String DEFAULT_LOCATION_URL = "http://localhost:8080/axis2/services/";


    public static final String URI_SOAP11_ENV =
            "http://schemas.xmlsoap.org/soap/envelope/";
    public static final String URI_SOAP12_ENV =
            "http://www.w3.org/2003/05/soap-envelope";

    public static final String URI_LITERAL_ENC = "";

    //
    // SOAP-ENC Namespaces
    //
    public static final String URI_SOAP11_ENC =
            "http://schemas.xmlsoap.org/soap/encoding/";
    public static final String URI_SOAP12_ENC =
            "http://www.w3.org/2003/05/soap-encoding";
    public static final String URI_SOAP12_NOENC =
            "http://www.w3.org/2003/05/soap-envelope/encoding/none";

    // Misc SOAP Namespaces / URIs
    public static final String URI_SOAP11_NEXT_ACTOR =
            "http://schemas.xmlsoap.org/soap/actor/next";
    public static final String URI_SOAP12_NEXT_ROLE =
            "http://www.w3.org/2003/05/soap-envelope/role/next";
    /**
     * @deprecated use URI_SOAP12_NEXT_ROLE
     */
    public static final String URI_SOAP12_NEXT_ACTOR = URI_SOAP12_NEXT_ROLE;

    public static final String URI_SOAP12_RPC =
            "http://www.w3.org/2003/05/soap-rpc";

    public static final String URI_SOAP12_NONE_ROLE =
            "http://www.w3.org/2003/05/soap-envelope/role/none";
    public static final String URI_SOAP12_ULTIMATE_ROLE =
            "http://www.w3.org/2003/05/soap-envelope/role/ultimateReceiver";

    public static final String URI_SOAP11_HTTP =
            "http://schemas.xmlsoap.org/soap/http";
    public static final String URI_SOAP12_HTTP =
            "http://www.w3.org/2003/05/http";

    public static final String NS_URI_XMLNS =
            "http://www.w3.org/2000/xmlns/";

    public static final String NS_URI_XML =
            "http://www.w3.org/XML/1998/namespace";

    //
    // Schema XSD Namespaces
    //
    public static final String URI_1999_SCHEMA_XSD =
            "http://www.w3.org/1999/XMLSchema";
    public static final String URI_2000_SCHEMA_XSD =
            "http://www.w3.org/2000/10/XMLSchema";
    public static final String URI_2001_SCHEMA_XSD =
            "http://www.w3.org/2001/XMLSchema";

    public static final String URI_DEFAULT_SCHEMA_XSD = URI_2001_SCHEMA_XSD;

    interface Java2WSDLConstants {
        String OUTPUT_LOCATION_OPTION = "o";
        String OUTPUT_FILENAME_OPTION = "of";
        String CLASSNAME_OPTION = "cn";
        String CLASSPATH_OPTION = "cp";
        String TARGET_NAMESPACE_OPTION = "tn";
        String TARGET_NAMESPACE_PREFIX_OPTION = "tp";
        String SCHEMA_TARGET_NAMESPACE_OPTION = "stn";
        String SCHEMA_TARGET_NAMESPACE_PREFIX_OPTION = "stp";
        String SERVICE_NAME_OPTION = "sn";
        String STYLE_OPTION = "st";
        String USE_OPTION = "u";
        String LOCATION_OPTION = "l";

        //long option constants
        String OUTPUT_LOCATION_OPTION_LONG = "output";
        String TARGET_NAMESPACE_OPTION_LONG = "targetNamespace";
        String TARGET_NAMESPACE_PREFIX_OPTION_LONG = "targetNamespacePrefix";
        String SERVICE_NAME_OPTION_LONG = "serviceName";
        String CLASSNAME_OPTION_LONG = "className";
        String CLASSPATH_OPTION_LONG = "classPath";
        String OUTPUT_FILENAME_OPTION_LONG = "outputFilename";
        String SCHEMA_TARGET_NAMESPACE_OPTION_LONG = "schemaTargetnamespace";
        String SCHEMA_TARGET_NAMESPACE_PREFIX_OPTION_LONG = "schemaTargetnamespacePrefix";
        String STYLE_OPTION_LONG = "style";
        String USE_OPTION_LONG = "use";
        String LOCATION_OPTION_LONG = "location";


    }

    public static final String SOLE_INPUT = "SOLE_INPUT";

}
