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

package org.apache.wsdl.extensions;

import org.apache.axis2.namespace.Constants;

import javax.xml.namespace.QName;

public interface ExtensionConstants {

    /**
     * The Type name for the SOAP Address defined in the Port/Endpoint
     */
    public static final QName SOAP_11_ADDRESS = new QName(
            Constants.URI_WSDL11_SOAP, "address");
    public static final QName SOAP_12_ADDRESS = new QName(
            Constants.URI_WSDL12_SOAP, "address");


    public static final QName SOAP_11_OPERATION = new QName(
            Constants.URI_WSDL11_SOAP, "operation");
     public static final QName SOAP_12_OPERATION = new QName(
            Constants.URI_WSDL12_SOAP, "operation");

    public static final QName SCHEMA = new QName(
            Constants.URI_2001_SCHEMA_XSD, "schema");

    public static final QName SOAP_11_BODY = new QName(
            Constants.URI_WSDL11_SOAP, "body");
    public static final QName SOAP_12_BODY = new QName(
            Constants.URI_WSDL12_SOAP, "body");
    
    public static final QName SOAP_11_HEADER = new QName(
            Constants.URI_WSDL11_SOAP, "header");
     public static final QName SOAP_12_HEADER = new QName(
            Constants.URI_WSDL12_SOAP, "header");

    public static final QName SOAP_11_BINDING = new QName(
            Constants.URI_WSDL11_SOAP, "binding");
     public static final QName SOAP_12_BINDING = new QName(
            Constants.URI_WSDL12_SOAP, "binding");
     
     public static final QName POLICY = new QName(
             Constants.URI_POLICY, "Policy");
     public static final QName POLICY_REFERENCE = new QName(
             Constants.URI_POLICY, "PolicyReference");
}
