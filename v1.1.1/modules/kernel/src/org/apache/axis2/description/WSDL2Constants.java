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

    String BINDING_LOCAL_NAME = "binding ";
    String SOAP_BINDING_PREFIX = "SOAPBinding";
    String HTTP_PROTOCAL = "http://www.w3.org/2003/05/soap/bindings/HTTP";
    String SERVICE_LOCAL_NAME = "service";
    
    String MESSAGE_LABEL_IN = "In";
    String MESSAGE_LABEL_OUT = "Out";
}
