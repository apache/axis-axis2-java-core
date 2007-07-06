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

package org.apache.axis2.jaxws.description.builder;

import org.apache.axis2.wsdl.WSDLConstants;

public class MDQConstants {

    public static final String WSDL_SERVICE_QNAME = "WSDL_SERVICE_QNAME";
    public static final String WSDL_PORT = "WSDL_PORT";
    public static final String WSDL_DEFINITION = WSDLConstants.WSDL_4_J_DEFINITION;
    public static final String WSDL_LOCATION = "WSDL_LOCATION";
    public static final String SERVICE_CLASS = "ServiceClass";
    public static final String WSDL_PORTTYPE_NAME = "WSDL_PORTTYPE_NAME";

    public static final String OBJECT_CLASS_NAME = "java.lang.Object";

    public static final String PROVIDER_SOURCE =
            "javax.xml.ws.Provider<javax.xml.transform.Source>";
    public static final String PROVIDER_SOAP = "javax.xml.ws.Provider<javax.xml.soap.SOAPMessage>";
    public static final String PROVIDER_DATASOURCE =
            "javax.xml.ws.Provider<javax.activation.DataSource>";
    public static final String PROVIDER_STRING = "javax.xml.ws.Provider<java.lang.String>";

    public static final String WSDL_FILE_NAME = "WSDL_FILE_NAME";
    public static final String SCHEMA_DOCS = "SCHEMA_DOCS";
    public static final String WSDL_COMPOSITE = "WSDL_COMPOSITE";

    // Java string that represents a class constructor
    public static final String CONSTRUCTOR_METHOD = "<init>";

    public static final String RETURN_TYPE_FUTURE = "java.util.concurrent.Future";
    public static final String RETURN_TYPE_RESPONSE = "javax.xml.ws.Response";
}
