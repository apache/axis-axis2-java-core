/*
 * Copyright 2004,2005 The Apache Software Foundation.
 * Copyright 2006 International Business Machines Corp.
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

package org.apache.axis2.jaxws.util;

import javax.xml.namespace.QName;

public class Constants {
    public static final String URI_WSDL_SOAP11 = "http://schemas.xmlsoap.org/wsdl/soap/";
    public static final String URI_WSDL_SOAP12 = "http://schemas.xmlsoap.org/wsdl/soap12/";
    public static final String URI_WSDL_SOAP11_BODY = "http://schemas.xmlsoap.org/wsdl/soap/";
    public static final String URI_WSDL_SOAP12_BODY ="http://schemas.xmlsoap.org/wsdl/soap12/";
    public static final String URI_WSDL_SOAP11_HEADER = "http://schemas.xmlsoap.org/wsdl/soap/";
    public static final String URI_WSDL_SOAP12_HEADER = "http://schemas.xmlsoap.org/wsdl/soap12/";
    public static final String URI_WSDL_SOAP11_BINDING = "http://schemas.xmlsoap.org/wsdl/soap/";
    public static final String URI_WSDL_SOAP12_BINDING ="http://schemas.xmlsoap.org/wsdl/soap12/";

    public static final String POLICY = "http://schemas.xmlsoap.org/ws/2004/09/policy";
    public static final String POLICY_REFERENCE = "http://schemas.xmlsoap.org/ws/2004/09/policy";
    
    public static final String SCHEMA = "http://www.w3.org/2001/XMLSchema";
    
    public static final String AXIS2_REPO_PATH = "com.ibm.websphere.webservices.axis2.repo.path";
    public static final String AXIS2_CONFIG_PATH = "com.ibm.websphere.webservices.axis2.config.path";
    public static final String USE_ASYNC_MEP = "com.ibm.websphere.webservices.use.async.mep";
    
    public static final String QOS_WSADDRESSING_ENABLE = "com.ibm.websphere.webservices.qos.wsaddressing.enable";
    public static final String QOS_WSRM_ENABLE = "com.ibm.websphere.webservices.qos.wsrm.enable";
    
    public static final QName QNAME_WSADDRESSING_MODULE = new QName("", "addressing");
    public static final QName QNAME_WSRM_MODULE = new QName("", "sandesha2");
}
