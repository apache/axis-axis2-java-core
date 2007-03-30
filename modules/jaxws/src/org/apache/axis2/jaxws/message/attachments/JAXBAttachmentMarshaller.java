/*
 * Copyright 2006 The Apache Software Foundation.
 * Copyright 2006 International Business Machines Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.axis2.jaxws.message.attachments;

import org.apache.axiom.om.util.UUIDGenerator;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.message.Attachment;
import org.apache.axis2.jaxws.message.Message;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimePartDataSource;
import javax.xml.bind.attachment.AttachmentMarshaller;

/**
 * An implementation of the JAXB AttachmentMarshaller that is used to handle binary data from JAXB
 * and create populate the appropriate constructs within the JAX-WS Message Model.
 */
public class JAXBAttachmentMarshaller extends AttachmentMarshaller {

    private static final Log log = LogFactory.getLog(JAXBAttachmentMarshaller.class);

    private Message message;

    public JAXBAttachmentMarshaller() {
        super();
    }

    public void setMessage(Message msg) {
        message = msg;
    }

    @Override
    public boolean isXOPPackage() {

        // FIXME: This should really be set based on whether or not the 
        // we want to send MTOM for this message.  In such cases the
        // transport must identify the message as application/xop+xml
        // (or an equivalent).  Please update this code to match the javadoc.
        // FIXME: This should really be set based on whether or not we
        // the SOAP 1.1 or SOAP 1.2 MTOM binding is set.
        boolean value = true;
        if (log.isDebugEnabled()) {
            log.debug("isXOPPackage returns " + value);
        }
        return value;
    }

    @Override
    public String addMtomAttachment(byte[] data, int offset, int length,
                                    String mimeType, String namespace, String localPart) {

        String cid = UUIDGenerator.getUUID();
        if (log.isDebugEnabled()) {
            log.debug("Adding MTOM/XOP byte array attachment for element: " + "{" + namespace +
                    "}" + localPart);
            log.debug("   content id=" + cid);
            log.debug("   mimeType  =" + mimeType);
        }

        DataHandler dataHandler = null;
        MimeBodyPart mbp = null;

        try {
            //Create mime parts
            InternetHeaders ih = new InternetHeaders();
            ih.setHeader(Attachment.CONTENT_TYPE, mimeType);
            ih.setHeader(Attachment.CONTENT_ID, cid);
            mbp = new MimeBodyPart(ih, data);
        }
        catch (MessagingException me) {
            throw ExceptionFactory
                    .makeWebServiceException(Messages.getMessage("mimeBodyPartError"), me);
        }

        //Create a data source for the byte array
        MimePartDataSource mpds = new MimePartDataSource(mbp);

        dataHandler = new DataHandler(mpds);
        Attachment a = message.createAttachment(dataHandler, cid);
        message.addAttachment(a);

        return cid;
    }

    @Override
    public String addMtomAttachment(DataHandler data, String namespace, String localPart) {
        String cid = UUIDGenerator.getUUID();
        if (log.isDebugEnabled()) {
            log.debug("Adding MTOM/XOP datahandler attachment for element: " + "{" + namespace +
                    "}" + localPart);
            log.debug("   content id=" + cid);
            log.debug("   dataHandler  =" + data);
        }
        Attachment a = message.createAttachment(data, cid);
        message.addAttachment(a);
        return cid;
    }

    @Override
    public String addSwaRefAttachment(DataHandler data) {
        String cid = UUIDGenerator.getUUID();
        if (log.isDebugEnabled()) {
            log.debug("Adding SWAREF attachment");
            log.debug("   content id=" + cid);
            log.debug("   dataHandler  =" + data);
        }

        Attachment a = message.createAttachment(data, cid);
        message.addAttachment(a);
        return cid;
    }

}
