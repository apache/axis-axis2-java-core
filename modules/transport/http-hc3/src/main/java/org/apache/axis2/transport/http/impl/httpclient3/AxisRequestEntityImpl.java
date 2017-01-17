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

package org.apache.axis2.transport.http.impl.httpclient3;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.axis2.transport.http.AxisRequestEntity;

import org.apache.commons.httpclient.methods.RequestEntity;

/**
 * This Request Entity is used by the HTTPCommonsTransportSender. This wraps the
 * Axis2 message formatter object.
 */
public class AxisRequestEntityImpl implements RequestEntity {
    private final AxisRequestEntity entity;
    
    public AxisRequestEntityImpl(AxisRequestEntity entity) {
        this.entity = entity;
    }

    @Override
    public boolean isRepeatable() {
        return entity.isRepeatable();
    }

    @Override
    public void writeRequest(OutputStream outStream) throws IOException {
        entity.writeRequest(outStream);
    }

    @Override
    public long getContentLength() {
        return entity.getContentLength();
    }

    @Override
    public String getContentType() {
        return entity.getContentType();
    }
}
