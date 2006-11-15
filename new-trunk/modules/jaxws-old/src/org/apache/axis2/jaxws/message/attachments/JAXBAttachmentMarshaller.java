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

import javax.activation.DataHandler;
import javax.xml.bind.attachment.AttachmentMarshaller;

import org.apache.axis2.jaxws.message.Attachment;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.impl.AttachmentImpl;
import org.apache.axis2.util.UUIDGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * An implementation of the JAXB AttachmentMarshaller that is used to 
 * handle binary data from JAXB and create populate the appropriate
 * constructs within the JAX-WS Message Model.
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
        //This should really be set based on whether or not we
        //the SOAP 1.1 or SOAP 1.2 MTOM binding is set.
        return true;
    }

    @Override
    public String addMtomAttachment(byte[] data, int offset, int length, 
            String mimeType, String namespace, String localPart) {
        if (log.isDebugEnabled()) 
            log.debug("Adding MTOM/XOP attachment for element: " + localPart + "{" + namespace + "}");
        return UUIDGenerator.getUUID();
    }

    @Override
    public String addMtomAttachment(DataHandler data, String namespace, String localPart) {
        if (log.isDebugEnabled()) 
            log.debug("Adding MTOM/XOP attachment for element: " + localPart + "{" + namespace + "}");
        
        String cid = UUIDGenerator.getUUID();
        Attachment a = new AttachmentImpl(data, cid);
        message.addAttachment(a);
        return cid;
    }

    @Override
    public String addSwaRefAttachment(DataHandler arg0) {
        throw new UnsupportedOperationException("SwaRef attachments are not supported.");
    }
    
}
