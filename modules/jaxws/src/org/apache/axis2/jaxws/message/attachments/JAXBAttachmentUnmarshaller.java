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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import javax.activation.DataHandler;
import javax.xml.bind.attachment.AttachmentUnmarshaller;

import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.message.Attachment;
import org.apache.axis2.jaxws.message.Message;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * JAXBAttachmentUnmarshaller
 * 
 * An implementation of the <link>javax.xml.bind.attachment.AttachmentUnmarshaller</link>
 * that is used for deserializing XOP elements into their corresponding
 * binary data packages.
 */
public class JAXBAttachmentUnmarshaller extends AttachmentUnmarshaller {

    private static final Log log = LogFactory.getLog(JAXBAttachmentUnmarshaller.class);
    
    private Message message;
    
    @Override
    public boolean isXOPPackage() {
        // FIXME: This should really be set based on whether or not the 
        // incoming message is "application/xop+xml".  Please read the
        // javadoc for this method.
        boolean value = true;
        if (log.isDebugEnabled()){ 
            log.debug("isXOPPackage returns " + value);
        }
        return value;
    }
    
    @Override
    public byte[] getAttachmentAsByteArray(String cid) {
        if (log.isDebugEnabled()) {
            log.debug("Attempting to retrieve attachment [" + cid + "] as a byte[]");
        }
        DataHandler dh = getAttachmentAsDataHandler(cid);
        if (dh != null) {
            try {
                return convert(dh);
            } catch (IOException ioe) {
                if (log.isDebugEnabled()) {
                    log.debug("Exception occurred while getting the byte[] " + ioe);
                }
                throw ExceptionFactory.makeWebServiceException(ioe);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("returning null byte[]");
        }
        return null;
    }

    @Override
    public DataHandler getAttachmentAsDataHandler(String cid) {
        if (log.isDebugEnabled()) {
            log.debug("Attempting to retrieve attachment [" + cid + "] as a DataHandler");
        }
        
        List<Attachment> attachments = message.getAttachments();
        Iterator<Attachment> itr = attachments.iterator();
        while (itr.hasNext()) {
            Attachment a = itr.next();
            if (a.getContentID().equals(cid)) {
                return a.getDataHandler();
            }
        }
        
        if (log.isDebugEnabled()) {
            log.debug("A dataHandler was not found for [" + cid + "]");
        }
        return null;
    }
    
    /**
     * Set the message that holds the attachment data.
     * @param msg
     */
    public void setMessage(Message msg) {
        message = msg;
    }

    /**
     * Read the bytes from the DataHandler
     * @param dh
     * @return byte[]
     * @throws IOException
     */
    private byte[] convert(DataHandler dh) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("Reading byte[] from DataHandler " + dh);
        }
        InputStream is = dh.getInputStream();
        if (log.isDebugEnabled()) {
            log.debug("DataHandler InputStream " + is);
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] b = new byte[1024];
        int num = is.read(b);
        if (log.isDebugEnabled()) {
            if (num <=0) {
                log.debug("DataHandler InputStream contains no data. num=" + num);
            }
        }
        while (num > 0) {
            baos.write(b, 0, num);
            num = is.read(b);
        }
        return baos.toByteArray();
    }
}
