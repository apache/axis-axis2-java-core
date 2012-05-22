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

package org.apache.axis2.transport.http;

public class HTTPTransportConstants {

    //Settings for HTTP proxy configuration.
    public static final String HTTP_PROXY_HOST = "http.proxyHost";
    public static final String HTTP_PROXY_PORT = "http.proxyPort";
    public static final String HTTP_NON_PROXY_HOSTS = "http.nonProxyHosts";

    public static final String ATTR_PROXY = "Proxy";
    public static final String PROXY_HOST_ELEMENT = "ProxyHost";
    public static final String PROXY_PORT_ELEMENT = "ProxyPort";
    public static final String PROXY_USER_ELEMENT = "ProxyUser";
    public static final String PROXY_PASSWORD_ELEMENT = "ProxyPassword";

    public static final String PROXY_CONFIGURATION_NOT_FOUND = "HTTP Proxy is enabled, but proxy configuration element is missing in axis2.xml";
    public static final String PROXY_HOST_ELEMENT_NOT_FOUND = "HTTP Proxy is enabled, but proxy host element is missing in axis2.xml";
    public static final String PROXY_PORT_ELEMENT_NOT_FOUND = "HTTP Proxy is enabled, but proxy port element is missing in axis2.xml";
    public static final String PROXY_HOST_ELEMENT_WITH_EMPTY_VALUE = "HTTP Proxy is enabled, but proxy host value is empty.";
    public static final String PROXY_PORT_ELEMENT_WITH_EMPTY_VALUE = "HTTP Proxy is enabled, but proxy port value is empty.";
    
    //Settings to define HTTPClient version
    public static final String HTTP_CLIENT_VERSION = "http.client.version"; 
    public static final String HTTP_CLIENT_3_X_VERSION = "http.client.version.3x";
    public static final String HTTP_CLIENT_4_X_VERSION = "http.client.version.4x";  
    
    public static final String ANONYMOUS = "anonymous";
    public static final String PROXY_HOST_NAME = "proxy_host";
    public static final String PROXY_PORT = "proxy_port";
    public static final String PROTOCOL_HTTP = "http";
    public static final String PROTOCOL_HTTPS = "https";

}
