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

    // ==========================================================================
    // ==========================================================================
    // Constants
    // ==========================================================================
    public static final String COPY_PROPERTIES = "CopyProperties";
    public static final int DEFAULT_TIMEOUT_MILLISECONDS = 5000;

    // ==========================================================================

    private Options delegate = null;
    private Map properties = new HashMap();

    // ==========================================================================
    // Parameters that can be set via Options
    // ==========================================================================
    private String soapVersionURI = SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI;
    private String soapAction = "";
    private boolean isExceptionToBeThrownOnSOAPFault = true;
    private long timeOutInMilliSeconds = DEFAULT_TIMEOUT_MILLISECONDS;
    private boolean useSeparateListener = false;

    // Addressing specific properties
    private String action;
    private EndpointReference faultTo;
    private EndpointReference from;
    private TransportListener listener;
    private TransportInDescription listenerTransport;
    private String listenerTransportProtocol;
    private String messageId;
    private RelatesTo relatesTo;
    private EndpointReference replyTo;

    /**
     * This is used for sending and receiving messages.
     */
    protected TransportOutDescription senderTransport;
    private String senderTransportProtocol;
    private EndpointReference to;

    /**
     * Default constructor
     */
    public Options() {
    }

    /**
     * Any setting in the delegate always wins.
     *
     * @param delegate
     */
    public Options(Options delegate) {
        this.delegate = delegate;
    }

    public String getAction() {
        if (delegate != null) {
            String ret = delegate.getAction();

            if (ret != null) {
                return ret;
            }
        }

        return action;
    }

    public EndpointReference getFaultTo() {
        if (delegate != null) {
            EndpointReference ret = delegate.getFaultTo();

            if (ret != null) {
                return ret;
            }
        }

        return faultTo;
    }

    public EndpointReference getFrom() {
        if (delegate != null) {
            EndpointReference ret = delegate.getFrom();

            if (ret != null) {
                return ret;
            }
        }

        return from;
    }

    public TransportListener getListener() {
        if (delegate != null) {
            TransportListener ret = delegate.getListener();

            if (ret != null) {
                return ret;
            }
        }

        return listener;
    }

    public TransportInDescription getListenerTransport() {
        if (delegate != null) {
            TransportInDescription ret = delegate.getListenerTransport();

            if (ret != null) {
                return ret;
            }
        }

        return listenerTransport;
    }

    public String getListenerTransportProtocol() {
        if (delegate != null) {
            String ret = delegate.getListenerTransportProtocol();

            if (ret != null) {
                return ret;
            }
        }

        return listenerTransportProtocol;
    }

    public String getMessageId() {
        if (delegate != null) {
            String ret = delegate.getMessageId();

            if (ret != null) {
                return ret;
            }
        }

        return messageId;
    }

    public Map getProperties() {
        if (delegate != null) {
            Map properties = delegate.getProperties();

            if (properties.size() > 0) {
                HashMap ret = new HashMap(properties);

                ret.putAll(properties);

                return ret;
            }
        }

        return properties;
    }

    /**
     * @param key
     * @return the value realeted to this key. Null, if not found.
     */
    public Object getProperty(String key) {
        if (delegate != null) {
            Object ret = delegate.getProperty(key);

            if (ret != null) {
                return ret;
            }
        }

        return properties.get(key);
    }

    public RelatesTo getRelatesTo() {
        if (delegate != null) {
            RelatesTo ret = delegate.getRelatesTo();

            if (ret != null) {
                return ret;
            }
        }

        return relatesTo;
    }

    public EndpointReference getReplyTo() {
        if (delegate != null) {
            EndpointReference ret = delegate.getReplyTo();

            if (ret != null) {
                return ret;
            }
        }

        return replyTo;
    }

    public TransportOutDescription getSenderTransport() {
        if (delegate != null) {
            TransportOutDescription ret = delegate.getSenderTransport();

            if (ret != null) {
                return ret;
            }
        }

        return senderTransport;
    }

    public String getSenderTransportProtocol() {
        if (delegate != null) {
            String ret = delegate.getSenderTransportProtocol();

            if (ret != null) {
                return ret;
            }
        }

        return senderTransportProtocol;
    }

    public String getSoapAction() {
        if (delegate != null) {
            String ret = delegate.getSoapAction();

            if (ret != null) {
                return ret;
            }
        }

        return soapAction;
    }

    public String getSoapVersionURI() {
        if (delegate != null) {
            String ret = delegate.getSoapVersionURI();

            if (ret != null) {
                return ret;
            }
        }

        return soapVersionURI;
    }

    /**
     * Gets the wait time after which a client times out in a blocking scenario.
     * The default is 2000.
     *
     * @return timeOutInMilliSeconds
     */
    public long getTimeOutInMilliSeconds() {
        if (delegate != null) {
            return delegate.getTimeOutInMilliSeconds();
        }

        return timeOutInMilliSeconds;
    }

    public EndpointReference getTo() {
        if (delegate != null) {
            EndpointReference ret = delegate.getTo();

            if (ret != null) {
                return ret;
            }
        }

        return to;
    }

    /**
     * If there is a SOAP Fault in the body of the incoming SOAP Message, system can be configured to
     * throw an exception with the details extracted from the information from the fault message.
     * This boolean variable will enable that facility. If this is false, the response message will just
     * be returned to the application, irrespective of whether it has a Fault or not.
     */
    public boolean isExceptionToBeThrownOnSOAPFault() {
        if (delegate != null) {
            return delegate.isExceptionToBeThrownOnSOAPFault();
        }

        return isExceptionToBeThrownOnSOAPFault;
    }

    public boolean isUseSeparateListener() {
        if (delegate != null) {
            return delegate.isUseSeparateListener();
        }

        return useSeparateListener;
    }

    public void setAction(String action) {
        this.action = action;
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

    public void setFaultTo(EndpointReference faultTo) {
        this.faultTo = faultTo;
    }

    public void setFrom(EndpointReference from) {
        this.from = from;
    }

    public void setListener(TransportListener listener) {
        this.listener = listener;
    }

    public void setListenerTransport(TransportInDescription listenerTransport) {
        this.listenerTransport = listenerTransport;
    }

    public void setListenerTransportProtocol(String listenerTransportProtocol) {
        this.listenerTransportProtocol = listenerTransportProtocol;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    /**
     * This will set the properties to the context. But in setting that one may need to "copy" all
     * the properties from the source properties to the target properties. To enable this we introduced
     * a property (org.apache.axis2.client.Options#COPY_PROPERTIES) so that if set to Boolean(true),
     * this code will copy the whole thing, without just referencing to the source.
     *
     * @param properties
     */
    public void setProperties(Map properties) {
        this.properties = properties;
    }

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

    public void setRelatesTo(RelatesTo relatesTo) {
        this.relatesTo = relatesTo;
    }

    public void setReplyTo(EndpointReference replyTo) {
        this.replyTo = replyTo;
    }

    public void setSenderTransport(TransportOutDescription senderTransport) {
        this.senderTransport = senderTransport;
    }

    /**
     * Sets the transport to be used for sending the SOAP Message
     *
     * @param senderTransport
     * @throws AxisFault if the transport is not found
     */
    public void setSenderTransport(String senderTransport, AxisConfiguration axisConfiguration)
            throws AxisFault {
        this.senderTransport = axisConfiguration.getTransportOut(new QName(senderTransport));

        if (senderTransport == null) {
            throw new AxisFault(Messages.getMessage("unknownTransport", senderTransport));
        }
    }

    public void setSoapAction(String soapAction) {
        this.soapAction = soapAction;
    }

    public void setSoapVersionURI(String soapVersionURI) {
        this.soapVersionURI = soapVersionURI;
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

    public void setTo(EndpointReference to) {
        this.to = to;
    }

    /**
     * Sets transport information to the call. The senarios supported are as follows:
     * <blockquote><pre>
     * [senderTransport, listenerTransport, useSeparateListener]
     * http, http, true
     * http, http, false
     * http,smtp,true
     * smtp,http,true
     * smtp,smtp,true
     * </pre></blockquote>
     *
     * @param senderTransport
     * @param listenerTransport
     * @param useSeparateListener
     * @throws AxisFault
     * @deprecated Use setListenerTransportProtocol(String) and useSeparateListener(boolean) instead.
     *             You do not need to setSenderTransportProtocol(String) as sender transport can be inferred from the
     *             to EPR. But still you can setSenderTransport(TransportOutDescription).
     */
    public void setTransportInfo(String senderTransport, String listenerTransport,
                                 boolean useSeparateListener)
            throws AxisFault {

        // here we check for a legal combination, for and example if the sendertransport is http and listner
        // transport is smtp the invocation must using separate transport
        if (!useSeparateListener) {
            boolean isTransportsEqual = senderTransport.equals(listenerTransport);
            boolean isATwoWaytransport = Constants.TRANSPORT_HTTP.equals(senderTransport)
                    || Constants.TRANSPORT_TCP.equals(senderTransport);

            if ((!isTransportsEqual || !isATwoWaytransport)) {
                throw new AxisFault(Messages.getMessage("useSeparateListenerLimited"));
            }
        } else {
            setUseSeparateListener(useSeparateListener);
        }

        setListenerTransportProtocol(listenerTransport);
        this.senderTransportProtocol = senderTransport;
    }

    /**
     * Used to specify whether the two SOAP Messages are be sent over same channel
     * or over separate channels.The value of this variable depends on the transport specified.
     * For e.g., if the transports are different this is true by default.
     * HTTP transport supports both cases while SMTP transport supports only two channel case.
     *
     * @param useSeparateListener
     */
    public void setUseSeparateListener(boolean useSeparateListener) {
        this.useSeparateListener = useSeparateListener;
    }
}
