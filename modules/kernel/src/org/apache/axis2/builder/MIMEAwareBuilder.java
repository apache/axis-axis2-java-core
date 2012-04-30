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
package org.apache.axis2.builder;

import org.apache.axiom.attachments.Attachments;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;

/**
 * Extension interface for {@link Builder} implementations that can build a message from a MIME
 * multipart message. This interface should be implemented by message builders associated with MIME
 * types that can appear as the root part of a multipart message. This is the case for SwA and MTOM.
 * <p>
 * The {@link #processMIMEMessage(Attachments, String, MessageContext)} method is called by
 * {@link MIMEBuilder} (which is associated by default with the <tt>multipart/related</tt> MIME
 * type) after identifying the target builder based on the content type of the root part.
 */
public interface MIMEAwareBuilder extends Builder {
    /**
     * Process a MIME multipart message and initialize the message context.
     * 
     * @param attachments
     *            the MIME message
     * @param contentType
     *            the content type of the root part, as specified by the <tt>type</tt> parameter in
     *            the content type of the MIME message
     * @param messageContext
     *            the message context
     * @return the SOAP infoset for the given message (which is typically a representation of the
     *         root part of the MIME message)
     * @throws AxisFault
     */
    OMElement processMIMEMessage(Attachments attachments, String contentType,
            MessageContext messageContext) throws AxisFault;
}
