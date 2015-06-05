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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.om.OMSourcedElement;
import org.apache.axiom.om.impl.OMMultipartWriter;
import org.apache.axis2.AxisFault;
import org.apache.axis2.jaxws.handler.AttachmentsAdapter;
import org.apache.axis2.jaxws.message.databinding.DataSourceBlock;
import org.apache.axis2.transport.MessageFormatter;
import org.apache.axis2.transport.http.ApplicationXMLFormatter;
import org.apache.axis2.transport.http.util.URLTemplatingUtil;
import org.apache.axis2.util.WrappedDataHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.xml.stream.XMLStreamException;
import javax.xml.ws.handler.MessageContext;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

public class DataSourceFormatter implements MessageFormatter {
    private static final Log log = LogFactory.getLog(ApplicationXMLFormatter.class);
    private final String contentType;

    public DataSourceFormatter(String contentType) {
        this.contentType = contentType;
    }

    public byte[] getBytes(org.apache.axis2.context.MessageContext messageContext, OMOutputFormat format) throws AxisFault {
        throw new UnsupportedOperationException("FIXME");
    }

    public void writeTo(org.apache.axis2.context.MessageContext messageContext, OMOutputFormat format, OutputStream outputStream, boolean preserve) throws AxisFault {
        AttachmentsAdapter attachments = (AttachmentsAdapter) messageContext.getProperty(MessageContext.OUTBOUND_MESSAGE_ATTACHMENTS);
        try {
            if (log.isDebugEnabled()) {
                log.debug("start writeTo()");
            }
            OMElement omElement = messageContext.getEnvelope().getBody().getFirstElement();
            DataSource busObject;
            try {
                busObject = (DataSource)((DataSourceBlock)((OMSourcedElement) omElement).getDataSource()).getBusinessObject(true);
            } catch (XMLStreamException e) {
                throw AxisFault.makeFault(e);
            }
            DataHandler dataHandler = new DataHandler(busObject);
            if (attachments != null && !attachments.isEmpty()) {
                OMMultipartWriter mpw = new OMMultipartWriter(outputStream, format);
                if (!dataHandler.getContentType().equals(contentType)) {
                    dataHandler = new WrappedDataHandler(dataHandler, contentType);
                }
                try {
                    mpw.writePart(dataHandler, format.getRootContentId());
                    for (String cid : attachments.keySet()) {
                        mpw.writePart(attachments.get(cid), cid);
                    }
                    mpw.complete();
                    outputStream.flush();
                } catch (IOException ex) {
                    throw AxisFault.makeFault(ex);
                }
            } else {
                try {
                    dataHandler.writeTo(outputStream);
                    outputStream.flush();
                } catch (IOException e) {
                    throw AxisFault.makeFault(e);
                }
            }
        } finally {
            if (log.isDebugEnabled()) {
                log.debug("end writeTo()");
            }
        }
    }

    public String getContentType(org.apache.axis2.context.MessageContext messageContext, OMOutputFormat format, String soapAction) {
        AttachmentsAdapter attachments = (AttachmentsAdapter) messageContext.getProperty(MessageContext.OUTBOUND_MESSAGE_ATTACHMENTS);
        if (attachments != null && !attachments.isEmpty()) {
            return format.getContentTypeForSwA(contentType);
        }
        return contentType;
    }

    public URL getTargetAddress(org.apache.axis2.context.MessageContext messageContext, OMOutputFormat format, URL targetURL) throws AxisFault {
        // Check whether there is a template in the URL, if so we have to replace then with data
        // values and create a new target URL.
        targetURL = URLTemplatingUtil.getTemplatedURL(targetURL, messageContext, false);
        return targetURL;
    }

    public String formatSOAPAction(org.apache.axis2.context.MessageContext messageContext, OMOutputFormat format, String soapAction) {
        return null;
    }
}
