/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.axis2.transport.testkit.message;

import org.apache.axiom.attachments.Attachments;
import org.apache.axiom.mime.ContentType;
import org.apache.axiom.mime.MediaType;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;

public class XMLMessage {
    public enum Type {
        SOAP11(MediaType.TEXT_XML),
        SOAP12(MediaType.APPLICATION_SOAP_XML),
        POX(MediaType.APPLICATION_XML),
        SWA(MediaType.MULTIPART_RELATED);
        
        private final MediaType contentType;
        
        private Type(MediaType contentType) {
            this.contentType = contentType;
        }
        
        public MediaType getContentType() {
            return contentType;
        }
    }
    
    private final Type type;
    private final OMElement payload;
    private final Attachments attachments;
    
    public XMLMessage(OMElement payload, Type type, Attachments attachments) {
        this.payload = payload;
        this.type = type;
        this.attachments = attachments;
    }

    public XMLMessage(OMElement payload, Type type) {
        this(payload, type, null);
    }
    
    public Type getType() {
        return type;
    }

    public OMElement getPayload() {
        return payload;
    }
    
    public OMElement getMessageElement() {
        if (type == Type.POX) {
            return payload;
        } else {
            SOAPFactory factory;
            if (type == Type.SOAP11) {
                factory = OMAbstractFactory.getSOAP11Factory();
            } else {
                factory = OMAbstractFactory.getSOAP12Factory();
            }
            SOAPEnvelope envelope = factory.getDefaultEnvelope();
            envelope.getBody().addChild(payload);
            return envelope;
        }
    }

    public Attachments getAttachments() {
        return attachments;
    }
    
    public static Type getTypeFromContentType(ContentType contentType) {
        MediaType baseType = contentType.getMediaType();
        Type type = null;
        for (Type candidate : Type.values()) {
            if (candidate.getContentType().equals(baseType)) {
                type = candidate;
                break;
            }
        }
        return type;
    }
}
