package org.apache.axis2.client;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.RelatesTo;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.transport.TransportListener;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
 */

/**
 * Holder for operation client options. This is used by the other classes in
 * this package to configure various aspects of how a client communicates with a
 * service. It exposes a number of predefined properties as part of the API
 * (with specific getXXX and setXXX methods), and also allows for arbitrary
 * named properties to be passed using a properties map with the property name
 * as the key value. Instances of this class can be chained together for
 * property inheritance, so that if a property is not set in one instance it
 * will check its parent for a setting.
 */
public class Options {
    
    /** Default blocking timeout value. */
    public static final int DEFAULT_TIMEOUT_MILLISECONDS = 30 * 1000;

    private Options parent;

    private Map properties = new HashMap();

    // ==========================================================================
    //                  Parameters that can be set via Options
    // ==========================================================================
    private String soapVersionURI; // defaults to
    // SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI;

    private Boolean isExceptionToBeThrownOnSOAPFault; // defaults to true;

    private long timeOutInMilliSeconds = -1; // =
    // DEFAULT_TIMEOUT_MILLISECONDS;

    private Boolean useSeparateListener; // defaults to false

    // Addressing specific properties
    private String action;

    private EndpointReference faultTo;

    private EndpointReference from;

    private TransportListener listener;

    private TransportInDescription transportIn;

    private String transportInProtocol;

    private String messageId;

    // Array of RelatesTo objects
    private List relationships;

    private EndpointReference replyTo;

    private ArrayList referenceParameters;

    /**
     * This is used for sending and receiving messages.
     */
    protected TransportOutDescription transportOut;

    private EndpointReference to;

    //To control , session managment , default is set to true , if user wants he can set that to true
    // There operation clinet will manage session using ServiceGroupID if it is there in the response
    private boolean manageSession = false;

    /**
     * Default constructor
     */
    public Options() {
    }

    /**
     * In normal mode operation, this options will try to fullfil the request
     * from its values. If that is not possible, this options will request those
     * information from its parent.
     *
     * @param parent
     */
    public Options(Options parent) {
        this.parent = parent;
    }

    /**
     * Get WS-Addressing Action / SOAP Action string.
     * 
     * @return action
     */
    public String getAction() {
        if (action == null && parent != null) {
            return parent.getAction();
        }
        return action;
    }

    /**
     * Get WS-Addressing FaultTo endpoint reference.
     * 
     * @return endpoint
     */
    public EndpointReference getFaultTo() {
        if (faultTo == null && parent != null) {
            return parent.getFaultTo();
        }
        return faultTo;
    }

    /**
     * Set WS-Addressing From endpoint reference.
     * 
     * @return endpoint
     */
    public EndpointReference getFrom() {
        if (from == null && parent != null) {
            return parent.getFrom();
        }
        return from;
    }

    /**
     * Get listener used for incoming message.
     * 
     * @return listener
     */
    public TransportListener getListener() {
        if (listener == null && parent != null) {
            return parent.getListener();
        }
        return listener;
    }

    /**
     * Get transport used for incoming message.
     * 
     * @return transport information
     */
    public TransportInDescription getTransportIn() {
        if (transportIn == null && parent != null) {
            return parent.getTransportIn();
        }
        return transportIn;
    }

    /**
     * Get transport protocol used for incoming message.
     * 
     * @return name protocol name ("http", "tcp", etc.)
     */
    public String getTransportInProtocol() {
        if (transportInProtocol == null && parent != null) {
            return parent.getTransportInProtocol();
        }
        return transportInProtocol;
    }

    /**
     * Get WS-Addressing MessageId.
     * 
     * @return uri string
     */
    public String getMessageId() {
        if (messageId == null && parent != null) {
            return parent.getMessageId();
        }

        return messageId;
    }

    /**
     * Get a copy of the general option properties. Because of the way options
     * are stored this does not include properties with specific get/set
     * methods, only the general properties identified by a text string. The
     * returned map merges properties inherited from parent options, if any, to
     * give a complete set of property definitions as seen by users of this
     * options instance. The returned copy is not "live", so changes you make to
     * the copy are not reflected in the actual option settings. However, you
     * can make the modified values take effect with a call to {@link
     * #setProperties(Map)}, 
     * 
     * @return copy of general properties
     */
    public Map getProperties() {
        if (parent == null) {
            return new HashMap(properties);
        } else {
            Map props = parent.getProperties();
            props.putAll(properties);
            return props;
        }
    }

    /**
     * Get named property value.
     * 
     * @param key
     * @return the value related to this key. <code>null</code>, if not found.
     */
    public Object getProperty(String key) {
        Object myPropValue = properties.get(key);
        if (myPropValue == null && parent != null) {
            return parent.getProperty(key);
        }
        return myPropValue;
    }

    /**
     * Get WS-Addressing RelatesTo item with a specified type. If there are
     * multiple RelatesTo items defined with the same type, the one returned
     * by this method is arbitrary - if you need to handle this case, you can
     * instead use the {@link #getRelationships()} to retrieve all the items
     * and check for multiple matches.
     * 
     * @param type relationship type (URI)
     * @return item of specified type
     */
    public RelatesTo getRelatesTo(String type) {
        if (relationships == null && parent != null) {
            return parent.getRelatesTo(type);
        }
        for(int i=0;relationships != null && i<relationships.size();i++) {
            RelatesTo relatesTo = (RelatesTo) relationships.get(i);
            String relationshipType = relatesTo.getRelationshipType();
            if(relationshipType.equals(type)) {
                return relatesTo;
            }
        }
        return null;
    }

    /**
     * Get WS-Addressing RelatesTo item which has the specific type
     * "http://www.w3.org/2005/08/addressing/reply"
     * 
     * @return item
     */
    public RelatesTo getRelatesTo() {
        if (relationships == null && parent != null) {
            return parent.getRelatesTo();
        }
        for(int i=0;relationships != null && i<relationships.size();i++) {
            RelatesTo relatesTo = (RelatesTo) relationships.get(i);
            String relationshipType = relatesTo.getRelationshipType();
            if (relationshipType.equals(AddressingConstants.Final.WSA_DEFAULT_RELATIONSHIP_TYPE)
                || relationshipType.equals(AddressingConstants.Submission.WSA_RELATES_TO_RELATIONSHIP_TYPE_DEFAULT_VALUE)) {
                return relatesTo;
            }
        }
        return null;
    }

    /**
     * Get all WS-Addressing RelatesTo items.
     * 
     * @return array of items
     */
    public RelatesTo[] getRelationships() {
        if (relationships == null && parent != null) {
            return parent.getRelationships();
        }
        if(relationships == null) {
            return null;                                 
        }
        return (RelatesTo[]) relationships.toArray(new RelatesTo[relationships.size()]);
    }

    /**
     * Set WS-Addressing RelatesTo items.
     * 
     * @param list
     */
    public void setRelationships(RelatesTo[] list) {
        relationships = list == null ? null : Arrays.asList(list);
    }

    /**
     * Get WS-Addressing ReplyTo endpoint reference.
     * 
     * @return endpoint
     */
    public EndpointReference getReplyTo() {
        if (replyTo == null && parent != null) {
            return parent.getReplyTo();
        }
        return replyTo;
    }

    /**
     * Get outbound transport description.
     * 
     * @return description
     */
    public TransportOutDescription getTransportOut() {
        if (transportOut == null && parent != null) {
            return parent.getTransportOut();
        }

        return transportOut;
    }

    /**
     * Get SOAP version being used.
     * 
     * @return version
     */
    public String getSoapVersionURI() {
        if (soapVersionURI == null && parent != null) {
            return parent.getSoapVersionURI();
        }

        return soapVersionURI == null ? SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI
                : soapVersionURI;
    }

    /**
     * Gets the wait time after which a client times out in a blocking scenario.
     * The default is Options#DEFAULT_TIMEOUT_MILLISECONDS
     *
     * @return timeOutInMilliSeconds
     */
    public long getTimeOutInMilliSeconds() {
        if (timeOutInMilliSeconds == -1 && parent != null) {
            return parent.getTimeOutInMilliSeconds();
        }

        return timeOutInMilliSeconds == -1 ? DEFAULT_TIMEOUT_MILLISECONDS
                : timeOutInMilliSeconds;
    }

    /**
     * Get WS-Addressing To endpoint reference.
     * 
     * @return endpoint
     */
    public EndpointReference getTo() {
        if (to == null && parent != null) {
            return parent.getTo();
        }

        return to;
    }

    /**
     * If there is a SOAP Fault in the body of the incoming SOAP Message, system
     * can be configured to throw an exception with the details extracted from
     * the information from the fault message. This boolean variable will enable
     * that facility. If this is false, the response message will just be
     * returned to the application, irrespective of whether it has a Fault or
     * not.
     * 
     * @return <code>true</code> if exception to be thrown
     */
    public boolean isExceptionToBeThrownOnSOAPFault() {
        if (isExceptionToBeThrownOnSOAPFault == null && parent != null) {
            isExceptionToBeThrownOnSOAPFault = parent.isExceptionToBeThrownOnSOAPFault;
        }

        return isExceptionToBeThrownOnSOAPFault == null
                || isExceptionToBeThrownOnSOAPFault.booleanValue();
    }

    /**
     * Check whether the two SOAP Messages are be sent over same channel or over
     * separate channels. Only duplex transports such as http and tcp support a
     * <code>false</code> value.

     * @return separate channel flag
     */
    public boolean isUseSeparateListener() {
        if (useSeparateListener == null && parent != null) {
            useSeparateListener = parent.useSeparateListener;
        }

        return useSeparateListener != null
                && useSeparateListener.booleanValue();
    }

    /**
     * Get parent instance providing default property values.
     * 
     * @return parent (<code>null</code> if none)
     */
    public Options getParent() {
        return parent;
    }

    /**
     * Set parent instance providing default property values.
     * 
     * @param parent (<code>null</code> if none)
     */
    public void setParent(Options parent) {
        this.parent = parent;
    }

    /**
     * Set WS-Addressing Action / SOAP Action string.
     *
     * @param action
     */
    public void setAction(String action) {
        this.action = action;
    }

    /**
     * If there is a SOAP Fault in the body of the incoming SOAP Message, system
     * can be configured to throw an exception with the details extracted from
     * the information from the fault message. This boolean variable will enable
     * that facility. If this is false, the response message will just be
     * returned to the application, irrespective of whether it has a Fault or
     * not.
     *
     * @param exceptionToBeThrownOnSOAPFault
     */
    public void setExceptionToBeThrownOnSOAPFault(
            boolean exceptionToBeThrownOnSOAPFault) {
        isExceptionToBeThrownOnSOAPFault = Boolean
                .valueOf(exceptionToBeThrownOnSOAPFault);
    }

    /**
     * Set WS-Addressing FaultTo endpoint reference.
     * 
     * @param faultTo endpoint
     */
    public void setFaultTo(EndpointReference faultTo) {
        this.faultTo = faultTo;
    }

    /**
     * Set WS-Addressing From endpoint reference.
     * 
     * @param from endpoint
     */
    public void setFrom(EndpointReference from) {
        this.from = from;
    }

    /**
     * Set listener used for incoming message.
     * 
     * @param listener
     */
    public void setListener(TransportListener listener) {
        this.listener = listener;
    }

    /**
     * Set transport used for incoming message.
     * 
     * @param transportIn
     */
    public void setTransportIn(TransportInDescription transportIn) {
        this.transportIn = transportIn;
    }

    /**
     * Set transport protocol used for incoming message.
     * 
     * @param transportInProtocol ("http", "tcp", etc.)
     */
    public void setTransportInProtocol(String transportInProtocol) {
        this.transportInProtocol = transportInProtocol;
    }

    /**
     * Set WS-Addressing MessageId.
     * 
     * @param messageId URI string
     */
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    /**
     * Set the general property definitions. Due to the way properties are
     * stored, this will not effect the values of predefined properties with
     * specific get/set methods.
     *
     * @param properties
     */
    public void setProperties(Map properties) {
        this.properties = properties;
    }

    /**
     * General properties you need to pass in to the message context must be set
     * via this method. This method can only be used for properties which do not
     * have specific get/set methods.
     *
     * @param propertyKey
     * @param property
     */
    public void setProperty(String propertyKey, Object property) {
        properties.put(propertyKey, property);
    }

    /**
     * Add WS-Addressing RelatesTo item.
     * 
     * @param relatesTo
     */
    public void addRelatesTo(RelatesTo relatesTo) {
        if(relationships == null) {
            relationships = new ArrayList(5);
        }
        relationships.add(relatesTo);
    }

    /**
     * Set WS-Addressing ReplyTo endpoint.
     * 
     * @param replyTo endpoint
     */
    public void setReplyTo(EndpointReference replyTo) {
        this.replyTo = replyTo;
    }

    /**
     * Set transport used for outgoing message.
     * 
     * @param transportOut
     */
    public void setTransportOut(TransportOutDescription transportOut) {
        this.transportOut = transportOut;
    }

    /**
     * Set transport used for outgoing message.
     *
     * @param senderTransport transport name in Axis2 configuration
     * ("http", "tcp", etc.)
     * @param axisConfiguration
     * @throws AxisFault if the transport is not found
     */
    public void setSenderTransport(String senderTransport,
                                   AxisConfiguration axisConfiguration) throws AxisFault {
        this.transportOut = axisConfiguration.getTransportOut(new QName(
                senderTransport));

        if (senderTransport == null) {
            throw new AxisFault(Messages.getMessage("unknownTransport",
                    senderTransport));
        }
    }

    /**
     * Set the SOAP version to be used.
     * 
     * @param soapVersionURI
     * @see org.apache.axis2.namespace.Constants#URI_SOAP11_ENV
     * @see org.apache.axis2.namespace.Constants#URI_SOAP12_ENV
     */
    public void setSoapVersionURI(String soapVersionURI) {
        this.soapVersionURI = soapVersionURI;
    }

    /**
     * This is used in blocking scenario. Client will time out after waiting
     * this amount of time. The default is 2000 and must be provided in
     * multiples of 100.
     *
     * @param timeOutInMilliSeconds
     */
    public void setTimeOutInMilliSeconds(long timeOutInMilliSeconds) {
        this.timeOutInMilliSeconds = timeOutInMilliSeconds;
    }

    /**
     * Set WS-Addressing To endpoint.
     * 
     * @param to endpoint
     */
    public void setTo(EndpointReference to) {
        this.to = to;
    }

    /**
     * Sets transport information to the call. The senarios supported are as
     * follows: <blockquote>
     * <p/>
     * <pre>
     *  [senderTransport, listenerTransport, useSeparateListener]
     *  http, http, true
     *  http, http, false
     *  http, smtp, true
     *  smtp, http, true
     *  smtp, smtp, true
     *  tcp,  tcp,  true
     *  tcp,  tcp,  false
     *  etc.
     * </pre>
     * <p/>
     * </blockquote>
     *
     * @param senderTransport
     * @param listenerTransport
     * @param useSeparateListener
     * @throws AxisFault
     * @deprecated Use setTransportInProtocol(String) and
     *             useSeparateListener(boolean) instead. You do not need to
     *             setSenderTransportProtocol(String) as sender transport can be
     *             inferred from the to EPR. But still you can
     *             setTransportOut(TransportOutDescription).
     */
    public void setTransportInfo(String senderTransport,
                                 String listenerTransport, boolean useSeparateListener)
            throws AxisFault {

        // here we check for a legal combination, for and example if the
        // sendertransport is http and listner
        // transport is smtp the invocation must using separate transport
        if (!useSeparateListener) {
            boolean isTransportsEqual = senderTransport
                    .equals(listenerTransport);
            boolean isATwoWaytransport = Constants.TRANSPORT_HTTP
                    .equals(senderTransport)
                    || Constants.TRANSPORT_TCP.equals(senderTransport);

            if ((!isTransportsEqual || !isATwoWaytransport)) {
                throw new AxisFault(Messages
                        .getMessage("useSeparateListenerLimited"));
            }
        } else {
            setUseSeparateListener(useSeparateListener);
        }

        setTransportInProtocol(listenerTransport);
    }

    /**
     * Used to specify whether the two SOAP Messages are be sent over same
     * channel or over separate channels. The value of this variable depends on
     * the transport specified. For e.g., if the transports are different this
     * is true by default. HTTP transport supports both cases while SMTP
     * transport supports only two channel case.
     *
     * @param useSeparateListener
     */
    public void setUseSeparateListener(boolean useSeparateListener) {
        this.useSeparateListener = Boolean.valueOf(useSeparateListener);
    }

    /**
     * Add WS-Addressing ReferenceParameter child element. Multiple child
     * may be used.
     * TODO Add get method, implement handling.
     * 
     * @param referenceParameter
     */
    public void addReferenceParameter(OMElement referenceParameter) {
        if (referenceParameters == null) {
            referenceParameters = new ArrayList(5);
        }

        referenceParameters.add(referenceParameter);
    }

    /**
     * Check if session management is enabled.
     * 
     * @return <code>true</code> if enabled
     */
    public boolean isManageSession() {
        return manageSession;
    }

    /**
     * Set session management enabled state. When session management is enabled,
     * the engine will automatically send session data (such as the service
     * group id, or HTTP cookies) as part of requests.
     * 
     * @param manageSession <code>true</code> if enabling sessions
     */
    public void setManageSession(boolean manageSession) {
        this.manageSession = manageSession;
    }
}
