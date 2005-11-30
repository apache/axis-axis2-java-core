package org.apache.axis2.client;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.RelatesTo;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.soap.SOAP11Constants;
import org.apache.axis2.transport.TransportListener;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Map;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
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
 *
 * @author : Eran Chinthaka (chinthaka@apache.org)
 */

/**
 * The proposal related for this is here : http://marc.theaimsgroup.com/?l=axis-dev&m=113320384108037&w=2
 * Client can fill this options and give to any class extending from MEPClient. All those classes
 * will be getting parameters using this.
 */
public class Options {

    private Map properties = new HashMap();

    // ==========================================================================
    //    Parameters that can be set via Options
    // ==========================================================================

    private String soapVersionURI = SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI;
    private String soapAction = "";

    private boolean isExceptionToBeThrownOnSOAPFault = true;
    private long timeOutInMilliSeconds = DEFAULT_TIMEOUT_MILLISECONDS;
    private TransportListener listener;

    /**
     * This is used for sending and receiving messages.
     */
    protected TransportOutDescription senderTransport;
    private TransportInDescription listenerTransport;
    private boolean useSeperateListener = false;
    private String listenerTransportProtocol;
    private String senderTrasportProtocol;

    // Addressing specific properties
    private String action;
    private String messageId;
    private EndpointReference to;
    private EndpointReference from;
    private EndpointReference replyTo;
    private EndpointReference faultTo;
    private RelatesTo relatesTo;

    // ==========================================================================

    // ==========================================================================
    //  Constants
    // ==========================================================================
    private static final int DEFAULT_TIMEOUT_MILLISECONDS = 2000;
    // ==========================================================================

    /**
     * Properties you need to pass in to the message context must be set via this.
     * If there is a method to the set this property, within this class, its encouraged to use that method,
     * without duplicating stuff or making room for bugs.
     *
     * @param propertyKey
     * @param property
     */
    public void setProperty(String propertyKey, Object property) {
        properties.put(propertyKey, property);
    }

    /**
     * @param key
     * @return the value realeted to this key. Null, if not found.
     */
    public Object getProperty(String key) {
        return properties.get(key);
    }

    public Map getProperties() {
        return properties;
    }

    public void setProperties(Map properties) {
        this.properties = properties;
    }

    public String getSoapVersionURI() {
        return soapVersionURI;
    }

    public void setSoapVersionURI(String soapVersionURI) {
        this.soapVersionURI = soapVersionURI;
    }

    public String getSoapAction() {
        return soapAction;
    }

    public void setSoapAction(String soapAction) {
        this.soapAction = soapAction;
    }

    /**
     * If there is a SOAP Fault in the body of the incoming SOAP Message, system can be configured to
     * throw an exception with the details extracted from the information from the fault message.
     * This boolean variable will enable that facility. If this is false, the response message will just
     * be returned to the application, irrespective of whether it has a Fault or not.
     */
    public boolean isExceptionToBeThrownOnSOAPFault() {
        return isExceptionToBeThrownOnSOAPFault;
    }

    /**
     * If there is a SOAP Fault in the body of the incoming SOAP Message, system can be configured to
     * throw an exception with the details extracted from the information from the fault message.
     * This boolean variable will enable that facility. If this is false, the response message will just
     * be returned to the application, irrespective of whether it has a Fault or not.
     *
     * @param exceptionToBeThrownOnSOAPFault
     */

    public void setExceptionToBeThrownOnSOAPFault(boolean exceptionToBeThrownOnSOAPFault) {
        isExceptionToBeThrownOnSOAPFault = exceptionToBeThrownOnSOAPFault;
    }

    /**
     * Gets the wait time after which a client times out in a blocking scenario.
     * The default is 2000.
     *
     * @return timeOutInMilliSeconds
     */
    public long getTimeOutInMilliSeconds() {
        return timeOutInMilliSeconds;
    }

    /**
     * This is used in blocking scenario. Client will time out after waiting this amount of time.
     * The default is 2000 and must be provided in multiples of 100.
     *
     * @param timeOutInMilliSeconds
     */
    public void setTimeOutInMilliSeconds(long timeOutInMilliSeconds) {
        this.timeOutInMilliSeconds = timeOutInMilliSeconds;
    }

    public TransportListener getListener() {
        return listener;
    }

    public void setListener(TransportListener listener) {
        this.listener = listener;
    }

    public TransportOutDescription getSenderTransport() {
        return senderTransport;
    }

    public void setSenderTransport(TransportOutDescription senderTransport) {
        this.senderTransport = senderTransport;
    }

    /**
     * Sets the transport to be used for sending the SOAP Message
     *
     * @param senderTransport
     * @throws org.apache.axis2.AxisFault if the transport is not found
     */
    public void setSenderTransport(String senderTransport, AxisConfiguration axisConfiguration) throws AxisFault {
        this.senderTransport =
                axisConfiguration.getTransportOut(new QName(senderTransport));
        if (senderTransport == null) {
            throw new AxisFault(Messages.getMessage("unknownTransport", senderTransport));
        }
    }

    public String getSenderTrasportProtocol() {
        return senderTrasportProtocol;
    }

    public void setSenderTransportProtocol(String senderTrasportProtocol) throws AxisFault {
        this.senderTrasportProtocol = senderTrasportProtocol;
    }

    public TransportInDescription getListenerTransport() {
        return listenerTransport;
    }

    public void setListenerTransport(TransportInDescription listenerTransport) {
        this.listenerTransport = listenerTransport;
    }

    public boolean isUseSeperateListener() {
        return useSeperateListener;
    }

    /**
     * Used to specify whether the two SOAP Messages are be sent over same channel
     * or over separate channels.The value of this variable depends on the transport specified.
     * For e.g., if the transports are different this is true by default.
     * HTTP transport supports both cases while SMTP transport supports only two channel case.
     *
     * @param useSeperateListener
     */
    public void setUseSeperateListener(boolean useSeperateListener) {
        this.useSeperateListener = useSeperateListener;
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

    public EndpointReference getTo() {
        return to;
    }

    public void setTo(EndpointReference to) {
        this.to = to;
    }

    public EndpointReference getFrom() {
        return from;
    }

    public void setFrom(EndpointReference from) {
        this.from = from;
    }

    public EndpointReference getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(EndpointReference replyTo) {
        this.replyTo = replyTo;
    }

    public EndpointReference getFaultTo() {
        return faultTo;
    }

    public void setFaultTo(EndpointReference faultTo) {
        this.faultTo = faultTo;
    }

    public RelatesTo getRelatesTo() {
        return relatesTo;
    }

    public void setRelatesTo(RelatesTo relatesTo) {
        this.relatesTo = relatesTo;
    }

    public String getListenerTransportProtocol() {
        return listenerTransportProtocol;
    }


    /**
     * Sets transport information to the call. The senarios supported are as follows:
     * <blockquote><pre>
     * [senderTransport, listenerTransport, useSeperateListener]
     * http, http, true
     * http, http, false
     * http,smtp,true
     * smtp,http,true
     * smtp,smtp,true
     * </pre></blockquote>
     *
     * @param senderTransport
     * @param listenerTransport
     * @param useSeperateListener
     * @throws AxisFault
     */

    public void setTransportInfo(String senderTransport, String listenerTransport,
                                 boolean useSeperateListener)
            throws AxisFault {
        //here we check for a legal combination, for and example if the sendertransport is http and listner
        //transport is smtp the invocation must using seperate transport
        if (!useSeperateListener) {
            boolean isTransportsEqual =
                    senderTransport.equals(listenerTransport);
            boolean isATwoWaytransport =
                    Constants.TRANSPORT_HTTP.equals(senderTransport)
                            || Constants.TRANSPORT_TCP.equals(senderTransport);
            if ((!isTransportsEqual || !isATwoWaytransport)) {
                throw new AxisFault(Messages.getMessage("useSeparateListenerLimited"));
            }
        } else {
            this.useSeperateListener = useSeperateListener;
        }

        this.listenerTransportProtocol = listenerTransport;
        this.senderTrasportProtocol = senderTransport;
    }
}
