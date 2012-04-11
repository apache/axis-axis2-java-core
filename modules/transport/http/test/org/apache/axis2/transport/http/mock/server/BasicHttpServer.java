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

package org.apache.axis2.transport.http.mock.server;

import java.util.Map;

/**
 * The interface BasicHttpServer defines basic operations for a HTTPServer that
 * can be used in test cases. Implementation of this interface can be real
 * HTTPserver or can be a mock implementation or wrap 3rd party HTTP server
 * based on test cases requirements.
 * 
 * @since 1.7.0
 */
public interface BasicHttpServer {

    /**
     * Start the HTTP server.
     * 
     * @throws Exception
     *             the exception
     */
    public void start() throws Exception;

    /**
     * Get the port on which the HTTP server is listening.
     * 
     * @return the HTTP port
     */
    public int getPort();
    
    /**
     * Stop the HTTP server.
     * 
     * @throws Exception
     *             the exception
     */
    public void stop() throws Exception;

    /**
     * Return all the HTTP Headers received by the server as a Java Map
     * instance.
     * 
     * @return the headers
     */
    public Map<String, String> getHeaders();

    /**
     * Return HTTP message content received by server as a byte array.
     * 
     * @return the content
     */
    public byte[] getContent();

    /**
     * Gets the HTTP method.
     * 
     * @return the method
     */
    public String getMethod();

    /**
     * Gets the request url.
     * 
     * @return the url
     */
    public String getUrl();

    /**
     * Sets the headers.
     * 
     * @param headers
     *            the headers
     */
    public void setHeaders(Map<String, String> headers);

    /**
     * Sets the content.
     * 
     * @param entityContent
     *            the new content
     */
    public void setContent(byte[] entityContent);

    /**
     * Sets the method.
     * 
     * @param method
     *            the new method
     */
    public void setMethod(String method);

    /**
     * Sets the url.
     * 
     * @param url
     *            the new url
     */
    public void setUrl(String url);

    /**
     * Gets the entity content length.
     * 
     * @return the entity content length
     */
    public int getEntityContentLength();

    /**
     * Sets the response template.
     * 
     * @param responseTemplate
     *            the new response template
     */
    public void setResponseTemplate(String responseTemplate);

    /**
     * Gets the response template.
     * 
     * @return the response template
     */
    public String getResponseTemplate();

    /**
     * Sets the close manully.
     * 
     * @param close
     *            the new close manully
     */
    public void setCloseManully(boolean close);

    public static final String RESPONSE_HTTP_404 = "response.http.404";
    public static final String RESPONSE_HTTP_OK_XML = "response.http.ok.xml";
    public static final String RESPONSE_HTTP_OK_LOOP_BACK = "response.http.ok.loop.back";
    public static final String RESPONSE_HTTP_200 = "response.http.200";
    public static final String RESPONSE_HTTP_201 = "response.http.201";
    public static final String RESPONSE_HTTP_202 = "response.http.202";
    public static final String RESPONSE_HTTP_400 = "response.http.400";
    public static final String RESPONSE_HTTP_500 = "response.http.500";


}
