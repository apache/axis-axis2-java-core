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

package org.apache.axis2.transport.http.impl.httpclient5;

import org.apache.axis2.transport.http.AxisRequestEntity;
import org.apache.axis2.kernel.http.HTTPConstants;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.function.Supplier;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;

/**
 * This Request Entity is used by the HttpComponentsTransportSender. This wraps the
 * Axis2 message formatter object.
 */
public class AxisRequestEntityImpl implements HttpEntity {
    private final AxisRequestEntity entity;

    public AxisRequestEntityImpl(AxisRequestEntity entity) {
        this.entity = entity;
    }

    @Override
    public String getContentType() {
        return entity.getContentType();
    }

    @Override
    public String getContentEncoding() {
        return null;
    }

    @Override
    public InputStream getContent() throws IOException {
        // Implementations are allowed to throw UnsupportedOperationException and this method is
        // never called for outgoing requests anyway.
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeTo(OutputStream outputStream) throws IOException {
        entity.writeRequest(outputStream);
    }

    @Override
    public boolean isStreaming() {
        return false;
    }

    @Override
    public long getContentLength() {
        return entity.getContentLength();
    }

    @Override
    public boolean isChunked() {
        return entity.isChunked();
    }

    @Override
    public boolean isRepeatable() {
        return entity.isRepeatable();
    }

    @Override
    public Supplier<List<? extends Header>> getTrailers() {
        return null;
    }

    @Override
    public Set<String> getTrailerNames() {
        return null;
    }

    @Override
    public void close() throws IOException {
    }
}
