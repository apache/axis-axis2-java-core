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

import java.util.Iterator;
import java.util.List;

import javax.activation.DataHandler;
import javax.xml.bind.attachment.AttachmentUnmarshaller;

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
        //FIXME: This should really be set based on whether or not we
        //the SOAP 1.1 or SOAP 1.2 MTOM binding is set.
        return true;
    }
    
    @Override
    public byte[] getAttachmentAsByteArray(String cid) {
        if (log.isDebugEnabled())
            log.debug("Attempting to retreive attachment [" + cid + "] as a byte[]");
        return null;
    }

    @Override
    public DataHandler getAttachmentAsDataHandler(String cid) {
        if (log.isDebugEnabled())
            log.debug("Attempting to retreive attachment [" + cid + "] as a DataHandler");
        
        List<Attachment> attachments = message.getAttachments();
        Iterator<Attachment> itr = attachments.iterator();
        while (itr.hasNext()) {
            Attachment a = itr.next();
            if (a.getContentID().equals(cid)) {
                return a.getDataHandler();
            }
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

}
