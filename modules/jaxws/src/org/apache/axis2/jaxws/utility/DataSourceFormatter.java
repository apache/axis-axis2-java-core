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

package org.apache.axis2.jaxws.utility;

import org.apache.axis2.transport.http.ApplicationXMLFormatter;
import org.apache.axis2.AxisFault;
import org.apache.axiom.om.OMOutputFormat;

import java.io.OutputStream;
import java.net.URL;

public class DataSourceFormatter extends ApplicationXMLFormatter {
    private final String contentType;

    public DataSourceFormatter(String contentType) {
        this.contentType = contentType;
    }

    public byte[] getBytes(org.apache.axis2.context.MessageContext messageContext, OMOutputFormat format) throws AxisFault {
        return super.getBytes(messageContext, format);
    }

    public void writeTo(org.apache.axis2.context.MessageContext messageContext, OMOutputFormat format, OutputStream outputStream, boolean preserve) throws AxisFault {
        super.writeTo(messageContext, format, outputStream, preserve);
    }

    public String getContentType(org.apache.axis2.context.MessageContext messageContext, OMOutputFormat format, String soapAction) {
        return contentType;
    }

    public URL getTargetAddress(org.apache.axis2.context.MessageContext messageContext, OMOutputFormat format, URL targetURL) throws AxisFault {
        return super.getTargetAddress(messageContext, format, targetURL);
    }

    public String formatSOAPAction(org.apache.axis2.context.MessageContext messageContext, OMOutputFormat format, String soapAction) {
        return super.formatSOAPAction(messageContext, format, soapAction);
    }
}
