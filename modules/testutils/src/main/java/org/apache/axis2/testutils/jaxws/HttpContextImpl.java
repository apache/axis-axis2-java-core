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
package org.apache.axis2.testutils.jaxws;

import java.util.Set;

import jakarta.xml.ws.spi.http.HttpContext;
import jakarta.xml.ws.spi.http.HttpHandler;

final class HttpContextImpl extends HttpContext {
    HttpHandler getHandler() {
        return handler;
    }

    @Override
    public String getPath() {
        return "/";
    }

    @Override
    public Object getAttribute(String name) {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> getAttributeNames() {
        // TODO
        throw new UnsupportedOperationException();
    }
}
