/*
 * Copyright 2001, 2002,2004 The Apache Software Foundation.
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

package org.apache.axis2.transport.jms;

import javax.jms.DeliveryMode;
import javax.jms.Message;
import javax.jms.Session;

/**
 * JMSConstants contains constants that apply to all JMS providers.
 * <p/>
 * <code>JMSConstants</code> contains the constant definitions for
 * interacting with the WSClient.  The most important constants are the
 * <code>HashMap</code> keys for use in the arguments to the
 * <code>send, call, registerListener, unregisterListener</code> methods of
 * <code>JMSEndpoint</code> and the <code>createConnector</code> method of
 * <code>JMSConnectorFactory</code>.
 */

public interface JMSConstants {
    public final static String PROTOCOL = "jms";

    // abbreviated version of all constants (see below for description of each constant)
    // the short name is used in the JMS URL. the full name is used in the Axis call.
    final static String _WAIT_FOR_RESPONSE = "waitForResponse";
    final static String _CLIENT_ID = "clientID";
    final static String _VENDOR = "vendor";
    final static String _DOMAIN = "domain";
    final static String _JMS_CORRELATION_ID = "jmsCorrelationID";
    final static String _JMS_CORRELATION_ID_AS_BYTES = "jmsCorrelationIDAsBytes";
    final static String _JMS_TYPE = "jmsType";
    final static String _TIME_TO_LIVE = "ttl";
    final static String _PRIORITY = "priority";
    final static String _DELIVERY_MODE = "deliveryMode";
    final static String _MESSAGE_SELECTOR = "messageSelector";
    final static String _ACKNOWLEDGE_MODE = "acknowledgeMode";
    final static String _SUBSCRIPTION_NAME = "subscriptionName";
    final static String _UNSUBSCRIBE = "unsubscribe";
    final static String _NO_LOCAL = "noLocal";
    final static String _NUM_RETRIES = "numRetries";
    final static String _NUM_SESSIONS = "numSessions";
    final static String _CONNECT_RETRY_INTERVAL = "connectRetryInterval";
    final static String _INTERACT_RETRY_INTERVAL = "interactRetryInterval";
    final static String _TIMEOUT_TIME = "timeoutTime";
    final static String _MIN_TIMEOUT_TIME = "minTimeoutTime";
    /**
     * Defines a prefix added to each application-specific property in the
     * JMS URL that should be added to the JMS Message when issued.
     */
    final static String _MSG_PROP_PREFIX = "msgProp.";

    public static String JMS_PROPERTY_PREFIX = "transport.jms.";

    /**
     * This is used as a key in the Call properties telling the JMS transport
     * to wait for a response from the service.  The default value is true.
     * If false is specified, the message will be delivered without specifying
     * a ReplyTo.  The client will always return null from invoke unless
     * a client-side exception is thrown (similar to invokeOneWay in semantics)
     * The value must be a <code>java.lang.Boolean</code>.
     * See the javax.jms javadoc for information on this property.
     */
    final static String WAIT_FOR_RESPONSE = JMS_PROPERTY_PREFIX + _WAIT_FOR_RESPONSE;

    /**
     * <code>JMSConnectorFactory</code> parameter valid for either domain.  This should
     * be used as a key in the environment map passed into calls to
     * <code>createConnector</code> in <code>JMSConnectorFactory</code>
     * This is a required property for durable subscribers.
     * The value must be a <code>java.lang.String</code>.
     * See the javax.jms javadoc for information on this property.
     */
    final static String CLIENT_ID = JMS_PROPERTY_PREFIX + _CLIENT_ID;

    // there is no short version
    final static String DESTINATION = JMS_PROPERTY_PREFIX + "Destination";

    final static String VENDOR = JMS_PROPERTY_PREFIX + _VENDOR;
    public final static String JNDI_VENDOR_ID = "JNDI";

    final static String DOMAIN = JMS_PROPERTY_PREFIX + _DOMAIN;

    final static String DOMAIN_QUEUE = "QUEUE";
    final static String DOMAIN_TOPIC = "TOPIC";
    final static String DOMAIN_DEFAULT = DOMAIN_QUEUE;

    /**
     * Key for properties used in the <code>send</code> and <code>call</code>
     * methods.  It is valid for either domain.
     * The value must be a <code>java.lang.String</code>.
     * See the javax.jms javadoc for information on this property.
     */
    final static String JMS_CORRELATION_ID = JMS_PROPERTY_PREFIX + _JMS_CORRELATION_ID;
    /**
     * Key for properties used in the <code>send</code> and <code>call</code>
     * methods.  It is valid for either domain.
     * The value must be a <code>byte[]</code>.
     * See the javax.jms javadoc for information on this property.
     */
    final static String JMS_CORRELATION_ID_AS_BYTES = JMS_PROPERTY_PREFIX + _JMS_CORRELATION_ID_AS_BYTES;
    /**
     * Key for properties used in the <code>send</code> and <code>call</code>
     * methods.  It is valid for either domain.
     * The value must be a <code>java.lang.String</code>.
     * See the javax.jms javadoc for information on this property.
     */
    final static String JMS_TYPE = JMS_PROPERTY_PREFIX + _JMS_TYPE;
    /**
     * Key for properties used in the <code>send</code> and <code>call</code>
     * methods.  It is valid for either domain.
     * The value must be a <code>java.lang.Long</code>.
     * See the javax.jms javadoc for information on this property.
     */
    final static String TIME_TO_LIVE = JMS_PROPERTY_PREFIX + _TIME_TO_LIVE;
    /**
     * Key for properties used in the <code>send</code> and <code>call</code>
     * methods.  It is valid for either domain.
     * The value must be a <code>java.lang.Integer</code>.
     * See the javax.jms javadoc for information on this property.
     */
    final static String PRIORITY = JMS_PROPERTY_PREFIX + _PRIORITY;
    /**
     * Key for properties used in the <code>send</code> and <code>call</code>
     * methods.  It is valid for either domain.
     * The value must be a <code>java.lang.Integer</code> equal to
     * DeliveryMode.NON_PERSISTENT or DeliveryMode.PERSISTENT.
     * See the javax.jms javadoc for information on this property.
     */
    final static String DELIVERY_MODE = JMS_PROPERTY_PREFIX + _DELIVERY_MODE;

    final static String DELIVERY_MODE_PERSISTENT = "Persistent";
    final static String DELIVERY_MODE_NONPERSISTENT = "Nonpersistent";
    final static String DELIVERY_MODE_DISCARDABLE = "Discardable";
    final static int DEFAULT_DELIVERY_MODE = DeliveryMode.NON_PERSISTENT;

    final static int DEFAULT_PRIORITY = Message.DEFAULT_PRIORITY;
    final static long DEFAULT_TIME_TO_LIVE = Message.DEFAULT_TIME_TO_LIVE;

    /**
     * Key for properties used in the <code>registerListener</code>
     * method.  It is valid for either domain.
     * The value must be a <code>java.lang.String</code>.
     * See the javax.jms javadoc for information on this property.
     */
    final static String MESSAGE_SELECTOR = JMS_PROPERTY_PREFIX + _MESSAGE_SELECTOR;
    /**
     * Key for properties used in the <code>registerListener</code>
     * method.  It is valid for either domain.
     * The value must be a <code>java.lang.Integer</code> that is one of
     * Session.AUTO_ACKNOWLEDGE, Session.DUPS_OK_ACKNOWLEDGE,
     * or Session.CLIENT_ACKNOWLEDGE.
     * See the javax.jms javadoc for information on this property.
     */
    final static String ACKNOWLEDGE_MODE = JMS_PROPERTY_PREFIX + _ACKNOWLEDGE_MODE;

    /**
     * value for ACKNOWLEDGE_MODE if left unset.  It is equal to
     * Session.DUPS_OK_ACKNOWLEDGE.
     */
    final static int DEFAULT_ACKNOWLEDGE_MODE = Session.DUPS_OK_ACKNOWLEDGE;

    /**
     * Specifies the name of a durable subscription
     * Key for properties used in the <code>registerListener</code>
     * method.  It is valid for the PubSub domain.
     * The value must be a <code>java.lang.String</code>.
     */
    final static String SUBSCRIPTION_NAME = JMS_PROPERTY_PREFIX + _SUBSCRIPTION_NAME;
    /**
     * Key for properties used in the <code>registerListener</code>
     * method.  It is valid for the PubSub domain.
     * Specifies that the durable subscription should be unsubscribed
     * (deleted from the broker) when unregistered.
     * The value must be a <code>java.lang.Boolean</code>.
     */
    final static String UNSUBSCRIBE = JMS_PROPERTY_PREFIX + _UNSUBSCRIBE;
    /**
     * Key for properties used in the <code>registerListener</code>
     * method.  It is valid for the PubSub domain.
     * The value must be a <code>java.lang.Boolean</code>.
     */
    final static String NO_LOCAL = JMS_PROPERTY_PREFIX + _NO_LOCAL;

    final static boolean DEFAULT_NO_LOCAL = false;
    final static boolean DEFAULT_UNSUBSCRIBE = false;

    /**
     * Key for properties used in the <code>createConnector</code>
     * method.  It changes the behavior of the wsclient.
     * The value must be a <code>java.lang.Integer</code>.
     */
    final static String NUM_RETRIES = JMS_PROPERTY_PREFIX + _NUM_RETRIES;
    /**
     * Key for properties used in the <code>createConnector</code>
     * method.  It changes the behavior of the wsclient.
     * The value must be a <code>java.lang.Integer</code>.
     */
    final static String NUM_SESSIONS = JMS_PROPERTY_PREFIX + _NUM_SESSIONS;
    /**
     * Key for properties used in the <code>createConnector</code>
     * method.  It changes the behavior of the wsclient.
     * The value must be a <code>java.lang.Long</code>.
     */
    final static String CONNECT_RETRY_INTERVAL = JMS_PROPERTY_PREFIX + _CONNECT_RETRY_INTERVAL;
    /**
     * Key for properties used in the <code>createConnector</code>
     * method.  It changes the behavior of the wsclient.
     * The value must be a <code>java.lang.Long</code>.
     */
    final static String INTERACT_RETRY_INTERVAL = JMS_PROPERTY_PREFIX + _INTERACT_RETRY_INTERVAL;
    /**
     * Key for properties used in the <code>createConnector</code>
     * method.  It changes the behavior of the wsclient.
     * The value must be a <code>java.lang.Long</code>.
     */
    final static String TIMEOUT_TIME = JMS_PROPERTY_PREFIX + _TIMEOUT_TIME;
    /**
     * Key for properties used in the <code>createConnector</code>
     * method.  It changes the behavior of the wsclient.
     * The value must be a <code>java.lang.Long</code>.
     */
    final static String MIN_TIMEOUT_TIME = JMS_PROPERTY_PREFIX + _MIN_TIMEOUT_TIME;

    final static int DEFAULT_NUM_RETRIES = 5;
    final static int DEFAULT_NUM_SESSIONS = 5;

    final static long DEFAULT_CONNECT_RETRY_INTERVAL = 2000;
    final static long DEFAULT_TIMEOUT_TIME = 5000;
    final static long DEFAULT_MIN_TIMEOUT_TIME = 1000;
    final static long DEFAULT_INTERACT_RETRY_INTERVAL = 250;

    // key used to store the JMS connector in the message context
    final static String CONNECTOR = JMS_PROPERTY_PREFIX + "Connector";

    // key used to store the JMS vendor adapter in the message context
    final static String VENDOR_ADAPTER = JMS_PROPERTY_PREFIX + "VendorAdapter";

    // key used to store the JMS URL string in the message context
    final static String JMS_URL = JMS_PROPERTY_PREFIX + "EndpointAddress";

    /**
     * A property that carries a Map of application-specific properties to be
     * added to the JMS messages when issued.
     */
    final static String JMS_APPLICATION_MSG_PROPS =
            JMS_PROPERTY_PREFIX + "msgProps";

    final static String ADAPTER_POSTFIX = "VendorAdapter";
}