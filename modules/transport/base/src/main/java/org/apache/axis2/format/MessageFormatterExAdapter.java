/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.axis2.format;

import java.io.OutputStream;
import java.net.URL;

import javax.activation.DataSource;

import org.apache.axiom.blob.BlobDataSource;
import org.apache.axiom.blob.Blobs;
import org.apache.axiom.blob.MemoryBlob;
import org.apache.axiom.blob.MemoryBlobOutputStream;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.MessageFormatter;

/**
 * Adapter to add the {@link MessageFormatterEx} interface to an
 * existing {@link MessageFormatter}.
 * It implements the {@link MessageFormatterEx#getDataSource(MessageContext, OMOutputFormat, String)} method
 * using {@link MessageFormatter#writeTo(MessageContext, OMOutputFormat, OutputStream, boolean)} and
 * {@link MessageFormatter#getContentType(MessageContext, OMOutputFormat, String)}.
 */
public class MessageFormatterExAdapter implements MessageFormatterEx {
    private final MessageFormatter messageFormatter;

    public MessageFormatterExAdapter(MessageFormatter messageFormatter) {
        this.messageFormatter = messageFormatter;
    }

    public DataSource getDataSource(MessageContext messageContext,
                                    OMOutputFormat format,
                                    String soapAction) throws AxisFault {
        MemoryBlob blob = Blobs.createMemoryBlob();
        MemoryBlobOutputStream out = blob.getOutputStream();
        writeTo(messageContext, format, out, false);
        out.close();
        return new BlobDataSource(blob, getContentType(messageContext, format, soapAction));
    }

    public String formatSOAPAction(MessageContext messageContext,
                                   OMOutputFormat format,
                                   String soapAction) {
        return messageFormatter.formatSOAPAction(messageContext, format, soapAction);
    }

    public String getContentType(MessageContext messageContext,
                                 OMOutputFormat format,
                                 String soapAction) {
        return messageFormatter.getContentType(messageContext, format, soapAction);
    }

    public URL getTargetAddress(MessageContext messageContext,
                                OMOutputFormat format,
                                URL targetURL) throws AxisFault {
        return messageFormatter.getTargetAddress(messageContext, format, targetURL);
    }

    public void writeTo(MessageContext messageContext,
                        OMOutputFormat format,
                        OutputStream outputStream,
                        boolean preserve) throws AxisFault {
        messageFormatter.writeTo(messageContext, format, outputStream, preserve);
    }
}
