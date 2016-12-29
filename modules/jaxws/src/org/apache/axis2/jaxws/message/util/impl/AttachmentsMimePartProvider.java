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

package org.apache.axis2.jaxws.message.util.impl;

import java.io.IOException;

import javax.activation.DataHandler;

import org.apache.axiom.attachments.Attachments;
import org.apache.axiom.mime.MimePartProvider;

/**
 * Adapts an {@link Attachments} instance to the {@link MimePartProvider} interface.
 */
final class AttachmentsMimePartProvider implements MimePartProvider {
    private final Attachments attachments;

    public AttachmentsMimePartProvider(Attachments attachments) {
        this.attachments = attachments;
    }

    @Override
    public DataHandler getDataHandler(String contentID) throws IOException {
        DataHandler dh = attachments.getDataHandler(contentID);
        if (dh == null) {
            throw new IllegalArgumentException("No attachment found for content ID '" + contentID + "'");
        } else {
            return dh;
        }
    }
}
