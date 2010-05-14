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

package org.apache.axis2.jaxws.message.attachments;

import org.apache.axiom.om.OMAttachmentAccessor;
import org.apache.axiom.util.stax.XMLStreamReaderUtils;
import org.apache.axis2.datasource.jaxb.JAXBAttachmentUnmarshallerMonitor;
import org.apache.axis2.jaxws.message.Message;

import javax.activation.DataHandler;
import javax.xml.stream.XMLStreamReader;

/**
 * JAXBAttachmentUnmarshaller
 * <p/>
 * An implementation of the <link>javax.xml.bind.attachment.AttachmentUnmarshaller</link> that is
 * used for deserializing XOP elements into their corresponding binary data packages.
 */
public class JAXBAttachmentUnmarshaller extends org.apache.axis2.datasource.jaxb.JAXBAttachmentUnmarshaller {

    private Message message;
    private XMLStreamReader xmlStreamReader;

    public JAXBAttachmentUnmarshaller(Message message, XMLStreamReader xmlStreamReader) {
        super(null, xmlStreamReader);
        this.message = message;
    }

    protected DataHandler getDataHandler(String cid) {
        
        // Get the attachment from the message using the cid
        if (message != null) {
            DataHandler dh = message.getDataHandler(cid);
            if (dh != null) {
                JAXBAttachmentUnmarshallerMonitor.addBlobCID(cid);
            }
            return dh;
        }
        
        XMLStreamReader attachmentAccessor = 
            XMLStreamReaderUtils.getWrappedXMLStreamReader(xmlStreamReader, OMAttachmentAccessor.class);
        
        if (attachmentAccessor != null &&
            attachmentAccessor instanceof OMAttachmentAccessor) {
            DataHandler dh = 
                ((OMAttachmentAccessor) attachmentAccessor).getDataHandler(cid);
            if (dh != null) {
                JAXBAttachmentUnmarshallerMonitor.addBlobCID(cid);
            }
            return dh;
        }
        return null;
    }

    
}
