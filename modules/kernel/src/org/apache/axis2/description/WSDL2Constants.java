package org.apache.axis2.description;

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
*
*/

public interface WSDL2Constants {

    String WSDL_NAMESPACE = "http://www.w3.org/2006/01/wsdl";
    String DEFAULT_WSDL_NAMESPACE_PREFIX = "wsdl2";
    String DESCRIPTION = "description";
    String URI_WSDL2_SOAP = "http://www.w3.org/2006/01/wsdl/soap";
    String URI_WSDL2_HTTP = "http://www.w3.org/2006/01/wsdl/http";
    String URI_WSDL2_EXTENSIONS = "http://www.w3.org/2006/01/wsdl-extensions";
    String SOAP_PREFIX = "wsoap";
    String URI_WSDL2_SOAP_ENV = "http://www.w3.org/2003/05/soap-envelope";
    String SOAP_ENV_PREFIX = "soap";
    String DEFAULT_TARGET_NAMESPACE_PREFIX = "axis2";
    String DOCUMENTATION = "documentation";

    String INTERFACE_LOCAL_NAME = "interface";
    String INTERFACE_PREFIX = "Interface";
    String OPERATION_LOCAL_NAME = "operation";
    String ATTRIBUTE_NAME = "name";
    String ATTRIBUTE_REF = "ref";
    String IN_PUT_LOCAL_NAME = "input";
    String OUT_PUT_LOCAL_NAME = "output";
    String OUT_FAULT = "outfault";
    String IN_FAULT = "infault";
    String ATTRIBUTE_NAME_PATTERN = "pattern";
    String MESSAGE_LABEL = "messageLabel";
    String ATTRIBUTE_ELEMENT = "element";

    String BINDING_LOCAL_NAME = "binding";
    String ENDPOINT_LOCAL_NAME = "endpoint";
    String SOAP_BINDING_PREFIX = "SOAPBinding";
    String HTTP_PROTOCAL = "http://www.w3.org/2003/05/soap/bindings/HTTP";
    String SERVICE_LOCAL_NAME = "service";

    String URI_HTTP_SOAP12 = "http://www.w3.org/2003/05/soap/bindings/HTTP";
    String URI_HTTP_SOAP11 = "http://www.w3.org/2006/01/soap11/bindings/HTTP";
    String URI_WSOAP_MEP = "http://www.w3.org/2003/05/soap/mep/soap-response/";

    String ATTR_WSOAP_PROTOCOL = "wsoap:protocol";
    String ATTR_WSOAP_VERSION = "wsoap:version";
    String ATTR_WSOAP_CODE = "wsoap:code";
    String ATTR_WSOAP_MEP = "wsoap:mep";
    String ATTR_WSOAP_MODULE = "wsoap:module";
    String ATTR_WSOAP_SUBCODES = "wsoap:subcodes";
    String ATTR_WSOAP_HEADER = "wsoap:header";
    String ATTR_WSOAP_ACTION = "wsoap:action";
    String ATTR_WSOAP_ADDRESS = "wsoap:address";

    String ATTR_WHTTP_CONTENT_ENCODING = "whttp:contentEncoding";
    String ATTR_WHTTP_LOCATION = "whttp:location";
    String ATTR_WHTTP_HEADER = "whttp:header";
    String ATTR_WHTTP_METHOD = "whttp:method";
    String ATTR_WHTTP_CODE = "whttp:code";
    String ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR = "whttp:queryParameterSeparator";
    String ATTR_WHTTP_IGNORE_UNCITED = "whttp:ignoreUncited";
    String ATTR_WHTTP_INPUT_SERIALIZATION = "whttp:inputSerialization";
    String ATTR_WHTTP_OUTPUT_SERIALIZATION = "whttp:outputSerialization";
    String ATTR_WHTTP_FAULT_SERIALIZATION = "whttp:faultSerialization";
    String ATTR_WHTTP_AUTHENTICATION_TYPE = "whttp:authenticationType";
    String ATTR_WHTTP_AUTHENTICATION_REALM = "whttp:authenticationRealm";

    String ATTR_WSDLX_SAFE = "wsdlx:safe";

    String SOAP_VERSION_1_1 = "1.1";
    String SOAP_VERSION_1_2 = "1.2";

    String MESSAGE_LABEL_IN = "In";
    String MESSAGE_LABEL_OUT = "Out";

    String HTTP_LOCATION_TABLE = "HTTPLocationTable";

    // This was taken from thye resolution of CR117 (WSDL 2.0 working group)
    // http://www.w3.org/2002/ws/desc/5/cr-issues/issues.html?view=normal#CR117
    // http://lists.w3.org/Archives/Public/www-ws-desc/2007Feb/0039.html
    String LEGAL_CHARACTERS_IN_URL = "-._~!$&()*+,;=:@";
    String TEMPLATE_ENCODE_ESCAPING_CHARACTER = "!";
}
