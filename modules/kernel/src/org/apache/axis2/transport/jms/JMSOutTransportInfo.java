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

import org.apache.axis2.transport.OutTransportInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import java.util.Hashtable;

/**
 * The JMS OutTransportInfo
 */
public class JMSOutTransportInfo implements OutTransportInfo {

    private static final Log log = LogFactory.getLog(JMSOutTransportInfo.class);

    private ConnectionFactory connectionFactory = null;
    private String connectionFactoryUser = null;
    private String connectionFactoryPassword = null;
    private Destination destination = null;

    private String contentType = null;

    /**
     * Creates an instance using the given connection factory and destination
     *
     * @param connectionFactory the connection factory
     * @param dest              the destination
     */
    JMSOutTransportInfo(ConnectionFactory connectionFactory, Destination dest) {
        this.connectionFactory = connectionFactory;
        this.destination = dest;
    }

    /**
     * Creates an instance using the given connection factory and destination
     *
     * @param connectionFactory the connection factory
     * @param dest              the destination
     */
    JMSOutTransportInfo(ConnectionFactory connectionFactory, String connectionFactoryUser, String connectionFactoryPassword, Destination dest) {
        this.connectionFactory = connectionFactory;
        this.connectionFactoryUser = connectionFactoryUser;
        this.connectionFactoryPassword = connectionFactoryPassword;
        this.destination = dest;
    }

    /**
     * Creates and instance using the given URL
     *
     * @param url the URL
     */
    JMSOutTransportInfo(String url) {
        if (!url.startsWith(JMSConstants.JMS_PREFIX)) {
            handleException("Invalid JMS URL : " + url +
                    " Must begin with the prefix " + JMSConstants.JMS_PREFIX);
        } else {
            Context context = null;
            Hashtable props = JMSUtils.getProperties(url);
            try {
                context = new InitialContext(props);
            } catch (NamingException e) {
                handleException("Could not get the initial context", e);
            }

            connectionFactory = getConnectionFactory(context, props);
            connectionFactoryUser = getConnectionFactoryUser(context, props);
            connectionFactoryPassword = getConnectionFactoryPass(context, props);
            destination = getDestination(context, url);
        }
    }

    /**
     * Get the referenced ConnectionFactory using the properties from the context
     *
     * @param context the context to use for lookup
     * @param props   the properties which contains the JNDI name of the factory
     * @return the connection factory
     */
    private ConnectionFactory getConnectionFactory(Context context, Hashtable props) {
        try {

            String conFacJndiName = (String) props.get(JMSConstants.CONFAC_JNDI_NAME_PARAM);
            if (conFacJndiName != null) {
                return (ConnectionFactory) context.lookup(conFacJndiName);
            } else {
                throw new NamingException(
                        "JMS Connection Factory JNDI name cannot be determined from url");
            }
        } catch (NamingException e) {
            handleException("Cannot get JMS Connection factory with props : " + props, e);
        }
        return null;
    }

    /**
     * Get the referenced ConnectionFactory Username (if supplied) using the properties from the context
     *
     * @param context the context to use for lookup
     * @param props   the properties which contains the JNDI name of the factory username
     * @return the connection factory username (or null if one is not in the JNDI tree)
     */
    private String getConnectionFactoryUser(Context context, Hashtable props) {
        try {

            String conFacJndiUser = (String) props.get(JMSConstants.CONFAC_JNDI_NAME_USER);
            if (conFacJndiUser != null) {
                return (String) context.lookup(conFacJndiUser);
            } else {
            	return null;
            }
        } catch (NamingException e) {
            handleException("Cannot get JMS Connection factory username with props : " + props, e);
        }
        return null;
    }

    /**
     * Get the referenced ConnectionFactory Password (if supplied) using the properties from the context
     *
     * @param context the context to use for lookup
     * @param props   the properties which contains the JNDI name of the factory password
     * @return the connection factory password (or null if one is not in the JNDI tree)
     */
    private String getConnectionFactoryPass(Context context, Hashtable props) {
        try {

            String conFacJndiPass = (String) props.get(JMSConstants.CONFAC_JNDI_NAME_PASS);
            if (conFacJndiPass != null) {
                return (String) context.lookup(conFacJndiPass);
            } else {
            	return null;
            }
        } catch (NamingException e) {
            handleException("Cannot get JMS Connection factory password with props : " + props, e);
        }
        return null;
    }

    /**
     * Get the JMS destination specified by the given URL from the context
     *
     * @param context the Context to lookup
     * @param url     URL
     * @return the JMS destination, or null if it does not exist
     */
    private Destination getDestination(Context context, String url) {
        String destinationName = JMSUtils.getDestination(url);
        try {
            return (Destination) context.lookup(destinationName);

        } catch (NameNotFoundException e) {
            log.warn("Cannot get or lookup JMS destination : " + destinationName +
                    " from url : " + url + " : " + e.getMessage());

        } catch (NamingException e) {
            handleException("Cannot get JMS destination : " + destinationName +
                    " from url : " + url, e);
        }
        return null;
    }


    private void handleException(String s) {
        log.error(s);
        throw new AxisJMSException(s);
    }

    private void handleException(String s, Exception e) {
        log.error(s, e);
        throw new AxisJMSException(s, e);
    }

    public Destination getDestination() {
        return destination;
    }

    public ConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    public String getConnectionFactoryPassword() {
		return connectionFactoryPassword;
	}

	public String getConnectionFactoryUser() {
		return connectionFactoryUser;
	}

	public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
