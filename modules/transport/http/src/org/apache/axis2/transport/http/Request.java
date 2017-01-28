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

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.axiom.mime.Header;

/**
 * Interface to prepare and execute an HTTP request.
 */
public interface Request {
    void enableHTTP10();
    void setHeader(String name, String value);
    void addHeader(String name, String value);
    Header[] getRequestHeaders();
    void enableAuthentication(HTTPAuthenticator authenticator);
    void setConnectionTimeout(int timeout);
    void setSocketTimeout(int timeout);
    void execute() throws IOException;
    int getStatusCode();
    String getStatusText();
    String getResponseHeader(String name);
    Header[] getResponseHeaders();
    Map<String,String> getCookies();
    InputStream getResponseContent() throws IOException;
    void releaseConnection();
}
