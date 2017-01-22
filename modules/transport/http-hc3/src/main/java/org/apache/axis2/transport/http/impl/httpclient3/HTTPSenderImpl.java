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

package org.apache.axis2.transport.http.impl.httpclient3;

import java.net.URL;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.http.AxisRequestEntity;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HTTPSender;
import org.apache.axis2.transport.http.Request;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class HTTPSenderImpl extends HTTPSender {

    private static final Log log = LogFactory.getLog(HTTPSenderImpl.class);

    @Override
    protected Request createRequest(MessageContext msgContext, String methodName, URL url,
            AxisRequestEntity requestEntity) throws AxisFault {
        return new RequestImpl(this, msgContext, methodName, url, requestEntity);
    }

    /**
     * This is used to get the dynamically set time out values from the message
     * context. If the values are not available or invalid then the default
     * values or the values set by the configuration will be used
     * 
     * @param msgContext
     *            the active MessageContext
     * @param httpClient
     */
    protected void initializeTimeouts(MessageContext msgContext, HttpClient httpClient) {
        // If the SO_TIMEOUT of CONNECTION_TIMEOUT is set by dynamically the
        // override the static config
        Integer tempSoTimeoutProperty = (Integer) msgContext.getProperty(HTTPConstants.SO_TIMEOUT);
        Integer tempConnTimeoutProperty = (Integer) msgContext
                .getProperty(HTTPConstants.CONNECTION_TIMEOUT);
        long timeout = msgContext.getOptions().getTimeOutInMilliSeconds();

        if (tempConnTimeoutProperty != null) {
            int connectionTimeout = tempConnTimeoutProperty.intValue();
            // timeout for initial connection
            httpClient.getHttpConnectionManager().getParams()
                    .setConnectionTimeout(connectionTimeout);
        } else {
            // set timeout in client
            if (timeout > 0) {
                httpClient.getHttpConnectionManager().getParams()
                        .setConnectionTimeout((int) timeout);
            }
        }

        if (tempSoTimeoutProperty != null) {
            int soTimeout = tempSoTimeoutProperty.intValue();
            // SO_TIMEOUT -- timeout for blocking reads
            httpClient.getHttpConnectionManager().getParams().setSoTimeout(soTimeout);
            httpClient.getParams().setSoTimeout(soTimeout);
        } else {
            // set timeout in client
            if (timeout > 0) {
                httpClient.getHttpConnectionManager().getParams().setSoTimeout((int) timeout);
                httpClient.getParams().setSoTimeout((int) timeout);
            }
        }
    }

    /**
     * This is used to get the dynamically set time out values from the message
     * context. If the values are not available or invalid then the default
     * values or the values set by the configuration will be used
     * 
     * @param msgContext
     *            the active MessageContext
     * @param httpMethod
     *            method
     */
    protected void setTimeouts(MessageContext msgContext, HttpMethod httpMethod) {
        // If the SO_TIMEOUT of CONNECTION_TIMEOUT is set by dynamically the
        // override the static config
        Integer tempSoTimeoutProperty = (Integer) msgContext.getProperty(HTTPConstants.SO_TIMEOUT);
        Integer tempConnTimeoutProperty = (Integer) msgContext
                .getProperty(HTTPConstants.CONNECTION_TIMEOUT);
        long timeout = msgContext.getOptions().getTimeOutInMilliSeconds();

        if (tempConnTimeoutProperty != null) {
            // timeout for initial connection
            httpMethod.getParams().setParameter("http.connection.timeout", tempConnTimeoutProperty);
        }

        if (tempSoTimeoutProperty != null) {
            // SO_TIMEOUT -- timeout for blocking reads
            httpMethod.getParams().setSoTimeout(tempSoTimeoutProperty);
        } else {
            // set timeout in client
            if (timeout > 0) {
                httpMethod.getParams().setSoTimeout((int) timeout);
            }
        }
    }

    protected HttpClient getHttpClient(MessageContext msgContext) {
        ConfigurationContext configContext = msgContext.getConfigurationContext();

        HttpClient httpClient = (HttpClient) msgContext
                .getProperty(HTTPConstants.CACHED_HTTP_CLIENT);

        if (httpClient == null) {
            httpClient = (HttpClient) configContext.getProperty(HTTPConstants.CACHED_HTTP_CLIENT);
        }

        if (httpClient != null) {
            return httpClient;
        }

        synchronized (this) {
            httpClient = (HttpClient) msgContext.getProperty(HTTPConstants.CACHED_HTTP_CLIENT);

            if (httpClient == null) {
                httpClient = (HttpClient) configContext
                        .getProperty(HTTPConstants.CACHED_HTTP_CLIENT);
            }

            if (httpClient != null) {
                return httpClient;
            }

            HttpConnectionManager connManager = (HttpConnectionManager) msgContext
                    .getProperty(HTTPConstants.MULTITHREAD_HTTP_CONNECTION_MANAGER);
            if (connManager == null) {
                connManager = (HttpConnectionManager) msgContext
                        .getProperty(HTTPConstants.MULTITHREAD_HTTP_CONNECTION_MANAGER);
            }
            if (connManager == null) {
                // reuse HttpConnectionManager
                synchronized (configContext) {
                    connManager = (HttpConnectionManager) configContext
                            .getProperty(HTTPConstants.MULTITHREAD_HTTP_CONNECTION_MANAGER);
                    if (connManager == null) {
                        log.trace("Making new ConnectionManager");
                        connManager = new MultiThreadedHttpConnectionManager();
                        configContext.setProperty(
                                HTTPConstants.MULTITHREAD_HTTP_CONNECTION_MANAGER, connManager);
                    }
                }
            }
            /*
             * Create a new instance of HttpClient since the way it is used here
             * it's not fully thread-safe.
             */
            httpClient = new HttpClient(connManager);

            // Set the default timeout in case we have a connection pool
            // starvation to 30sec
            httpClient.getParams().setConnectionManagerTimeout(30000);

            // Get the timeout values set in the runtime
            initializeTimeouts(msgContext, httpClient);

            return httpClient;
        }
    }

}
