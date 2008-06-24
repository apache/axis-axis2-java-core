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

package org.apache.axis2.transport.jms;

import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;

import javax.xml.namespace.QName;

public class JMSConstants {

    /**
     * The prefix indicating an Axis JMS URL
     */
    public static final String JMS_PREFIX = "jms:/";

    /**
     * The Parameter name indicating a JMS destination for requests
     */
    public static final String DEST_PARAM = "transport.jms.Destination";

    /**
     * The Parameter name indicating the response JMS destination
     */
    public static final String REPLY_PARAM = "transport.jms.ReplyDestination";

    /**
     * The Parameter name indicating the JMS destination type
     */
    public static final String DEST_TYPE_PARAM = "transport.jms.DestinationType";

    /**
     * The Parameter name indicating the JMS destination type
     */
    public static final String DEST_TYPE_TOPIC = "Topic";

    /**
     * The Parameter name indicating the JMS destination type
     */
    public static final String DEST_TYPE_QUEUE = "Queue";

    /**
     * The Parameter name of an Axis2 service, indicating the JMS connection
     * factory which should be used to listen for messages for it. This is
     * the local (Axis2) name of the connection factory and not a JNDI name
     */
    public static final String CONFAC_PARAM = "transport.jms.ConnectionFactory";

    /**
     * The Parameter name indicating the JMS connection factory JNDI name
     */
    public static final String CONFAC_JNDI_NAME_PARAM = "transport.jms.ConnectionFactoryJNDIName";

    /**
     * The Parameter name indicating the JMS connection factory username (useful for WebsphereMQ CLIENT connections)
     * n.b. This is not the actual username, it is the JNDI name of the variable that will hold the username
     */
    public static final String CONFAC_JNDI_NAME_USER = "transport.jms.ConnectionFactoryJNDIUser";

    /**
     * The Parameter name indicating the JMS connection factory password (useful for WebsphereMQ CLIENT connections)
     * n.b. This is not the actual password, it is the JNDI name of the variable that will hold the password
     */
    public static final String CONFAC_JNDI_NAME_PASS = "transport.jms.ConnectionFactoryJNDIPass";

    /**
     * The Parameter name indicating the operation to dispatch non SOAP/XML messages
     */
    public static final String OPERATION_PARAM = "transport.jms.Operation";
    /**
     * The Parameter name indicating the wrapper element for non SOAP/XML messages
     */
    public static final String WRAPPER_PARAM = "transport.jms.Wrapper";
    /**
     * The default operation name to be used for non SOAP/XML messages
     * if the operation cannot be determined
     */
    public static final QName DEFAULT_OPERATION = new QName("urn:mediate");
    /**
     * The name of the element which wraps non SOAP/XML content into a SOAP envelope
     */
    public static final QName DEFAULT_WRAPPER =
            new QName(Constants.AXIS2_NAMESPACE_URI, "jmsMessage");

    /**
     * The local (Axis2) JMS connection factory name of the default connection
     * factory to be used, if a service does not explicitly state the connection
     * factory it should be using by a Parameter named JMSConstants.CONFAC_PARAM
     */
    public static final String DEFAULT_CONFAC_NAME = "default";

    /**
     * A MessageContext property or client Option stating the JMS message type
     */
    public static final String JMS_MESSAGE_TYPE = "JMS_MESSAGE_TYPE";
    /**
     * The message type indicating a BytesMessage. See JMS_MESSAGE_TYPE
     */
    public static final String JMS_BYTE_MESSAGE = "JMS_BYTE_MESSAGE";
    /**
     * The message type indicating a TextMessage. See JMS_MESSAGE_TYPE
     */
    public static final String JMS_TEXT_MESSAGE = "JMS_TEXT_MESSAGE";
    /**
     * A MessageContext property or client Option stating the JMS correlation id
     */
    public static final String JMS_COORELATION_ID = "JMS_COORELATION_ID";
    /**
     * A MessageContext property or client Option stating the time to wait for a response JMS message
     */
    public static final String JMS_WAIT_REPLY = "JMS_WAIT_REPLY";
    /**
     * The JMS message property specifying the SOAP Action
     */
    public static final String SOAPACTION = "SOAPAction";
    /**
     * The JMS message property specifying the content type
     */
    public static final String CONTENT_TYPE = "contentType";
    /**
     * The default JMS time out waiting for a reply
     */
    public static final long DEFAULT_JMS_TIMEOUT = Options.DEFAULT_TIMEOUT_MILLISECONDS;

    public static final String ACTIVEMQ_DYNAMIC_QUEUE = "dynamicQueues/";
    public static final String ACTIVEMQ_DYNAMIC_TOPIC = "dynamicTopics/";
}
