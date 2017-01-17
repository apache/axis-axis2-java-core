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

import org.apache.axiom.om.OMOutputFormat;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.MessageFormatter;
import org.apache.axis2.util.JavaUtils;

import javax.xml.stream.FactoryConfigurationError;
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

    private MessageContext messageContext;

    private byte[] bytes;

    private final boolean preserve;

    private OMOutputFormat format;

    private final String contentType;

    /**
     * Method calls to this request entity are delegated to the following Axis2
     * message formatter object.
     *
     * @param messageFormatter
     */
    AxisRequestEntity(MessageFormatter messageFormatter,
                      MessageContext msgContext, OMOutputFormat format, String contentType,
                      boolean chunked, boolean preserve) {
        this.messageFormatter = messageFormatter;
        this.messageContext = msgContext;
        this.chunked = chunked;
        this.preserve = preserve;
        this.format = format;
        this.contentType = contentType;
    }

    public boolean isRepeatable() {
        // If chunking is disabled, we don't preserve the original SOAPEnvelope, but we store the
        // serialized SOAPEnvelope in a byte array, which means that the entity can be written
        // repeatedly.
        return preserve || !chunked;
    }

    public void writeRequest(OutputStream outStream) throws IOException {
        Object gzip = messageContext.getOptions().getProperty(HTTPConstants.MC_GZIP_REQUEST);
        if (gzip != null && JavaUtils.isTrueExplicitly(gzip) && chunked) {
            outStream = new GZIPOutputStream(outStream);
        }
        try {
            if (chunked) {
                messageFormatter.writeTo(messageContext, format, outStream, preserve);
            } else {
                if (bytes == null) {
                    bytes = messageFormatter.getBytes(messageContext, format);
                }
                outStream.write(bytes);
            }
            if (outStream instanceof GZIPOutputStream) {
                ((GZIPOutputStream) outStream).finish();
            }
            outStream.flush();
        } catch (FactoryConfigurationError e) {
            throw AxisFault.makeFault(e);
        } catch (IOException e) {
            throw AxisFault.makeFault(e);
        }

    }

    public long getContentLength() {
        if (chunked) {
            return -1;
        }
        if (bytes == null) {
            try {
                bytes = messageFormatter.getBytes(messageContext, format);
            } catch (AxisFault e) {
                return -1;
            }
        }
        return bytes.length;
    }

    public String getContentType() {
        return contentType;
    }

    public boolean isChunked() {
        return chunked;
    }
}
