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

import org.apache.axiom.blob.Blobs;
import org.apache.axiom.blob.MemoryBlob;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.MessageFormatter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

/**
 * This Request Entity is used by the HTTPCommonsTransportSender. This wraps the
 * Axis2 message formatter object.
 */
public final class AxisRequestEntity  {

    private MessageFormatter messageFormatter;

    private final boolean chunked;
    
    private final boolean gzip;

    private MessageContext messageContext;

    private final MemoryBlob content;

    private final boolean preserve;

    private OMOutputFormat format;

    private final String contentType;

    /**
     * Method calls to this request entity are delegated to the following Axis2
     * message formatter object.
     *
     * @param messageFormatter
     * @throws AxisFault 
     */
    AxisRequestEntity(MessageFormatter messageFormatter,
                      MessageContext msgContext, OMOutputFormat format, String contentType,
                      boolean chunked, boolean gzip, boolean preserve) throws AxisFault {
        this.messageFormatter = messageFormatter;
        this.messageContext = msgContext;
        this.chunked = chunked;
        this.gzip = gzip;
        this.preserve = preserve;
        this.format = format;
        this.contentType = contentType;
        if (chunked) {
            content = null;
        } else {
            content = Blobs.createMemoryBlob();
            OutputStream out = content.getOutputStream();
            try {
                internalWriteRequest(out);
                out.close();
            } catch (IOException ex) {
                throw AxisFault.makeFault(ex);
            }
        }
    }

    public boolean isRepeatable() {
        // If chunking is disabled, we don't preserve the original SOAPEnvelope, but we store the
        // serialized SOAPEnvelope in a byte array, which means that the entity can be written
        // repeatedly.
        return preserve || !chunked;
    }

    public void writeRequest(OutputStream outStream) throws IOException {
        if (chunked) {
            internalWriteRequest(outStream);
        } else {
            content.writeTo(outStream);
        }
    }
    
    private void internalWriteRequest(OutputStream outStream) throws IOException {
        if (gzip) {
            outStream = new GZIPOutputStream(outStream);
        }
        try {
            messageFormatter.writeTo(messageContext, format, outStream, preserve);
            if (gzip) {
                ((GZIPOutputStream) outStream).finish();
            }
            outStream.flush();
        } catch (IOException e) {
            throw AxisFault.makeFault(e);
        }

    }

    public long getContentLength() {
        if (chunked) {
            return -1;
        } else {
            return content.getSize();
        }
    }

    public String getContentType() {
        return contentType;
    }

    public boolean isChunked() {
        return chunked;
    }
}
