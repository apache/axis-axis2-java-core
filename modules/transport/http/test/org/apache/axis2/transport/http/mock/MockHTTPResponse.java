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

package org.apache.axis2.transport.http.mock;

import java.io.ByteArrayOutputStream;
import java.util.Map;

/**
 * The Interface MockHTTPResponse.
 * 
 * @since 1.7.0
 */
public interface MockHTTPResponse {

    /**
     * Gets all the headers as a Map of <Header-Name, Header-Value>.
     * 
     * This method can be used in test cases to retrieve all headers written to
     * the HttpServletResponse.
     * 
     * @return the headers
     */
    public Map<String, String> getHeaders();

    /**
     * HTTP response write to a internal ByteArrayOutputStream and possible to
     * retrieve written content using this method.
     * 
     * @return tByteArrayOutputStream
     */
    public ByteArrayOutputStream getByteArrayOutputStream();

}
