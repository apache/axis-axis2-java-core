/*
 * Copyright 2004,2005 The Apache Software Foundation.
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
package org.apache.axis2.addressing.om;

import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.miheaders.RelatesTo;
import org.apache.axis2.soap.SOAPHeader;

/**
 * Class AddressingHeaders
 */
public class AddressingHeaders {
    /**
     * Field wsaTo
     */
    private EndpointReference wsaTo;

    /**
     * Field wsaFrom
     */
    private EndpointReference wsaFrom;

    /**
     * Field wsaReply
     */
    private EndpointReference wsaReply;

    /**
     * Field wsaFaultTo
     */
    private EndpointReference wsaFaultTo;

    /**
     * Field action
     */
    private String action;

    /**
     * Field messageId
     */
    private String messageId;

    /**
     * Field relatesTo
     */
    private RelatesTo relatesTo;

    /**
     * Addressing Header MUST have a to and an action
     *
     * @param wsaTo
     * @param action
     */
    public AddressingHeaders(EndpointReference wsaTo, String action) {
        this.wsaTo = wsaTo;
        this.action = action;
    }

     // ------------------- Setters and Getters --------------------------------------

    /**
     * Method getWsaTo
     *
     * @return
     */
    public EndpointReference getWsaTo() {
        return wsaTo;
    }

    /**
     * Method setWsaTo
     *
     * @param wsaTo
     */
    public void setWsaTo(EndpointReference wsaTo) {
        this.wsaTo = wsaTo;
    }

    /**
     * Method getWsaFrom
     *
     * @return
     */
    public EndpointReference getWsaFrom() {
        return wsaFrom;
    }

    /**
     * Method setWsaFrom
     *
     * @param wsaFrom
     */
    public void setWsaFrom(EndpointReference wsaFrom) {
        this.wsaFrom = wsaFrom;
    }

    /**
     * Method getWsaReply
     *
     * @return
     */
    public EndpointReference getWsaReply() {
        return wsaReply;
    }

    /**
     * Method setWsaReply
     *
     * @param wsaReply
     */
    public void setWsaReply(EndpointReference wsaReply) {
        this.wsaReply = wsaReply;
    }

    /**
     * Method getWsaFaultTo
     *
     * @return
     */
    public EndpointReference getWsaFaultTo() {
        return wsaFaultTo;
    }

    /**
     * Method setWsaFaultTo
     *
     * @param wsaFaultTo
     */
    public void setWsaFaultTo(EndpointReference wsaFaultTo) {
        this.wsaFaultTo = wsaFaultTo;
    }

    /**
     * Method getAction
     *
     * @return
     */
    public String getAction() {
        return action;
    }

    /**
     * Method setAction
     *
     * @param action
     */
    public void setAction(String action) {
        this.action = action;
    }

    /**
     * Method getMessageId
     *
     * @return
     */
    public String getMessageId() {
        return messageId;
    }

    /**
     * Method setMessageId
     *
     * @param messageId
     */
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    /**
     * Method getRelatesTo
     *
     * @return
     */
    public RelatesTo getRelatesTo() {
        return relatesTo;
    }

    /**
     * Method setRelatesTo
     *
     * @param relatesTo
     */
    public void setRelatesTo(RelatesTo relatesTo) {
        this.relatesTo = relatesTo;
    }

    // --------------------------------------------------------------------------------------------
}
