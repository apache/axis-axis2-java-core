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
package org.apache.axis.addressing.om;

import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.addressing.miheaders.RelatesTo;
import org.apache.axis.om.SOAPHeader;

/**
 * This holds the WSA Message Information Headers
 */
public class MessageInformationHeadersCollection {

    /**
     * The address of the intended receiver of the message. This is mandatory
     */
    private EndpointReference to;

    /**
     * Reference of the endpoint where the message originated from
     */
    private EndpointReference from;

    /**
     * Pair of values that indicate how this message related to another message
     */
    private RelatesTo relatesTo;

    /**
     * identifies the intended receiver for replies to the message
     */
    private EndpointReference replyTo;

    /**
     * identifies the intended receiver for faults related to the message
     */
    private EndpointReference faultTo;

    /**
     * Field action
     */
    private String action;

    /**
     * Field messageId
     */
    private String messageId;


    /**
     * Addressing Header MUST have a to and an action
     *
     * @param wsaTo
     * @param action
     */
    public MessageInformationHeadersCollection(EndpointReference wsaTo, String action) {
        this.to = wsaTo;
        this.action = action;
    }

    public MessageInformationHeadersCollection() {
    }

    /**
     * Method toOM
     *
     * @param soapHeader
     */
    public void toOM(SOAPHeader soapHeader) {
    }

    // ------------------- Setters and Getters --------------------------------------

    /**
     * Method getTo
     *
     * @return
     */
    public EndpointReference getTo() {

        return to;
    }

    /**
     * Method setTo
     *
     * @param to
     */
    public void setTo(EndpointReference to) {
        this.to = to;
    }

    /**
     * Method getFrom
     *
     * @return
     */
    public EndpointReference getFrom() {
        return from;
    }

    /**
     * Method setFrom
     *
     * @param from
     */
    public void setFrom(EndpointReference from) {
        this.from = from;
    }

    /**
     * Method getReplyTo
     *
     * @return
     */
    public EndpointReference getReplyTo() {

        return replyTo;
    }

    /**
     * Method setReplyTo
     *
     * @param replyTo
     */
    public void setReplyTo(EndpointReference replyTo) {
        this.replyTo = replyTo;
    }

    /**
     * Method getFaultTo
     *
     * @return
     */
    public EndpointReference getFaultTo() {

        return faultTo;
    }

    /**
     * Method setFaultTo
     *
     * @param faultTo
     */
    public void setFaultTo(EndpointReference faultTo) {
        this.faultTo = faultTo;
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
