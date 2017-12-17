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

package org.apache.axis2.datasource.jaxb;

import org.apache.axiom.om.OMException;
import org.apache.axiom.om.impl.MTOMXMLStreamWriter;
import org.apache.axiom.util.UIDGenerator;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.java.security.AccessController;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimePartDataSource;
import javax.xml.bind.attachment.AttachmentMarshaller;
import javax.xml.stream.XMLStreamWriter;

import java.security.PrivilegedAction;

public final class JAXBAttachmentMarshaller extends AttachmentMarshaller {

    private static final Log log = LogFactory.getLog(JAXBAttachmentMarshaller.class);

    private final AttachmentContext context;
    private final XMLStreamWriter writer;
    private static final String APPLICATION_OCTET = "application/octet-stream";
    
    /**
     * Construct the JAXBAttachmentMarshaller that has access to the MessageContext
     * @param msgContext
     * @param writer
     */
    public JAXBAttachmentMarshaller(AttachmentContext context, XMLStreamWriter writer) {
        this.context = context;
        this.writer = writer;
    }

    /**
     * Override isXOPPackaget to calculate the isXOPPackage setting
     */
    public boolean isXOPPackage() {
        boolean value = false;
        
        // For outbound messages, only trigger MTOM if
        // the message is mtom enabled.
        value = context.isMTOMEnabled();
        
        // If the writer is not an MTOM XMLStreamWriter then we don't have
        // any place to store the attachment
        if (!(writer instanceof MTOMXMLStreamWriter)) {
            if (log.isDebugEnabled()) {
                log.debug("The writer is not enabled for MTOM.  " +
                                "MTOM values will not be optimized");
            }
            value = false;
        }
    
        if (log.isDebugEnabled()){ 
            log.debug("isXOPPackage returns " + value);
        }
        return value;        
    }

    
    /* (non-Javadoc)
     * @see javax.xml.bind.attachment.AttachmentMarshaller#addMtomAttachment(byte[], int, int, java.lang.String, java.lang.String, java.lang.String)
     */
    public final String addMtomAttachment(byte[] data, int offset, int length,
                                    String mimeType, String namespace, String localPart) {

        if (offset != 0 || length != data.length) {
            int len = length - offset;
            byte[] newData = new byte[len];
            System.arraycopy(data, offset, newData, 0, len);
            data = newData;
        }
        
        if (mimeType == null || mimeType.length() == 0) {
            mimeType = APPLICATION_OCTET;
        }
        
        if (log.isDebugEnabled()){ 
            log.debug("Adding MTOM/XOP byte array attachment for element: " + 
                      "{" + namespace + "}" + localPart);
        }
        
        String cid = null;
        
        try {
            // Create MIME Body Part
            final InternetHeaders ih = new InternetHeaders();
            final byte[] dataArray = data; 
            ih.setHeader(HTTPConstants.HEADER_CONTENT_TYPE, mimeType);
            final MimeBodyPart mbp = AccessController.doPrivileged(new PrivilegedAction<MimeBodyPart>() {
                public MimeBodyPart run() {
                    try {
                        return new MimeBodyPart(ih, dataArray);
                    } catch (MessagingException e) {
                        throw new OMException(e);
                    }
                }});

            //Create a data source for the MIME Body Part
            MimePartDataSource mpds = AccessController.doPrivileged(new PrivilegedAction<MimePartDataSource>() {
                public MimePartDataSource run() {
                    return new MimePartDataSource(mbp);
                }});
            long dataLength =data.length;
            Integer value = null;
            MessageContext msgContext = context.getMessageContext();
            if (msgContext != null) {
                value = (Integer) msgContext.getProperty(Constants.Configuration.MTOM_THRESHOLD);
            } else if (log.isDebugEnabled()) {
                log.debug("The msgContext is null so the MTOM threshold value can not be determined; it will default to 0.");
            }

            int optimizedThreshold = (value != null) ? value.intValue() : 0;

            if(optimizedThreshold==0 || dataLength > optimizedThreshold){
                DataHandler dataHandler = new DataHandler(mpds);
                cid = addDataHandler(dataHandler, false);
            }

            // Add the content id to the mime body part
            mbp.setHeader(HTTPConstants.HEADER_CONTENT_ID, cid);
        } catch (MessagingException e) {
            throw new OMException(e);
        }

        return cid == null ? null : "cid:" + cid;
    }
    
    
    /* (non-Javadoc)
     * @see javax.xml.bind.attachment.AttachmentMarshaller#addMtomAttachment(javax.activation.DataHandler, java.lang.String, java.lang.String)
     */
    public final String addMtomAttachment(DataHandler data, String namespace, String localPart) {
        if (log.isDebugEnabled()){ 
            log.debug("Adding MTOM/XOP datahandler attachment for element: " + 
                      "{" + namespace + "}" + localPart);
        }
        String cid = addDataHandler(data, false);
        return cid == null ? null : "cid:" + cid;
    }
    
    
    /* (non-Javadoc)
     * @see javax.xml.bind.attachment.AttachmentMarshaller#addSwaRefAttachment(javax.activation.DataHandler)
     */
    public final String addSwaRefAttachment(DataHandler data) {
        if (log.isDebugEnabled()){ 
            log.debug("Adding SWAREF attachment");
        }
        
        String cid = addDataHandler(data, true);
        context.setDoingSWA();
        return "cid:" + cid;
    }
    
    /**
     * Add the DataHandler to the writer and context
     * @param dh
     * @return
     */
    private String addDataHandler(DataHandler dh, boolean isSWA) {
        String cid = null;
        
        // If this is an MTOMXMLStreamWriter then inform the writer 
        // that it must write out this attachment (I guess we should do this
        // even if the attachment is SWAREF ?)
        if (isSWA) {
            if (log.isDebugEnabled()){ 
                log.debug("adding DataHandler for SWA");
            }
            // If old SWA attachments, get the ID and add the attachment to message
            cid = UIDGenerator.generateContentId();
            context.addDataHandler(dh, cid);   
        } else {
            if (log.isDebugEnabled()){ 
                log.debug("adding DataHandler for MTOM");
            }
            if (writer instanceof MTOMXMLStreamWriter) {
                cid = ((MTOMXMLStreamWriter)writer).prepareDataHandler(dh);
                if (cid != null) {
                    if (log.isDebugEnabled()){ 
                        log.debug("The MTOM attachment is written as an attachment part.");
                    }
                    // Remember the attachment on the message.
                    context.addDataHandler(dh, cid);
                } else {
                    if (log.isDebugEnabled()){ 
                        log.debug("The MTOM attachment is inlined.");
                    }
                }
            } else {
                if (log.isDebugEnabled()){ 
                    log.debug("writer is not MTOM capable.  The attachment will be inlined.");
                }
            }
        }
        
        if (log.isDebugEnabled()){ 
            log.debug("   content id=" + cid);
            log.debug("   dataHandler  =" + dh);
        }
        return cid;
    }
}
