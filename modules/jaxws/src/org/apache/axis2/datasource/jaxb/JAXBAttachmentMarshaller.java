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

import org.apache.axis2.Constants.Configuration;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.activation.DataHandler;
import javax.xml.stream.XMLStreamWriter;

/**
 * An implementation of the JAXB AttachmentMarshaller that is used to handle binary data from JAXB
 * and create populate the appropriate constructs on the MessageContext
 */
public final class JAXBAttachmentMarshaller extends AbstractJAXBAttachmentMarshaller {

    private static final Log log = LogFactory.getLog(JAXBAttachmentMarshaller.class);

    /**
     * Construct the JAXBAttachmentMarshaller that has access to the MessageContext
     * @param msgContext
     * @param writer
     */
    public JAXBAttachmentMarshaller(MessageContext msgContext, XMLStreamWriter writer) {
        super(msgContext, writer);
    }

    /**
     * @return if MTOM enabled calculated from the context information
     */
    protected boolean isMTOMEnabled() {
        if (msgContext == null) {
            return false;
        } else {
            String value = (String) msgContext.getProperty(Configuration.ENABLE_MTOM);
            return ("true".equalsIgnoreCase(value));
        }  
    }
    
    /**
     * Indicate on the context that SWA attachments are encountered
     */
    protected void setDoingSWA() {
        if (msgContext != null) {
            msgContext.setDoingSwA(true);
            msgContext.setProperty(Configuration.ENABLE_SWA, "true");
        }
    }
    
    /**
     * Inform the context that an Attachment (DataHandler, CID) is written.
     * @param dh
     * @param cid
     */
    protected void addDataHandler(DataHandler dh, String cid) {
        if (msgContext != null) {
            msgContext.addAttachment(cid, dh);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("The msgContext is null.  The attachment is not stored");
                log.debug("   content id=" + cid);
                log.debug("   dataHandler  =" + dh);
            }
        }
    }
}
