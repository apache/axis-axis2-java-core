package org.apache.axis.addressing.om;

import org.apache.axis.addressing.EndpointReferenceType;
import org.apache.axis.addressing.miheaders.RelatesTo;
import org.apache.axis.om.SOAPHeader;

/**
 * Copyright 2001-2004 The Apache Software Foundation.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class AddressingHeaders {

    private EndpointReferenceType wsaTo;
    private EndpointReferenceType wsaFrom;
    private EndpointReferenceType wsaReply;
    private EndpointReferenceType wsaFaultTo;
    private String action;
    private String messageId;
    private RelatesTo relatesTo;

    /**
     * Addressing Header MUST have a to and an action
     * @param wsaTo
     * @param action
     */
    public AddressingHeaders(EndpointReferenceType wsaTo, String action) {
        this.wsaTo = wsaTo;
        this.action = action;
    }

    public void toOM(SOAPHeader soapHeader){
        
    }

    // ------------------- Setters and Getters --------------------------------------
    public EndpointReferenceType getWsaTo() {
        return wsaTo;
    }

    public void setWsaTo(EndpointReferenceType wsaTo) {
        this.wsaTo = wsaTo;
    }

    public EndpointReferenceType getWsaFrom() {
        return wsaFrom;
    }

    public void setWsaFrom(EndpointReferenceType wsaFrom) {
        this.wsaFrom = wsaFrom;
    }

    public EndpointReferenceType getWsaReply() {
        return wsaReply;
    }

    public void setWsaReply(EndpointReferenceType wsaReply) {
        this.wsaReply = wsaReply;
    }

    public EndpointReferenceType getWsaFaultTo() {
        return wsaFaultTo;
    }

    public void setWsaFaultTo(EndpointReferenceType wsaFaultTo) {
        this.wsaFaultTo = wsaFaultTo;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public RelatesTo getRelatesTo() {
        return relatesTo;
    }

    public void setRelatesTo(RelatesTo relatesTo) {
        this.relatesTo = relatesTo;
    }


    // --------------------------------------------------------------------------------------------



}
