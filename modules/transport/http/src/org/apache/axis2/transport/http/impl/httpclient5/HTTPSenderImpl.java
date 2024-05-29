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

package org.apache.axis2.transport.http.impl.httpclient5;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.kernel.http.HTTPConstants;
import org.apache.axis2.transport.http.AxisRequestEntity;
import org.apache.axis2.transport.http.HTTPSender;
import org.apache.axis2.transport.http.Request;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.core5.http.config.CharCodingConfig;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.ManagedHttpClientConnectionFactory;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.io.ManagedHttpClientConnection;
import org.apache.hc.core5.http.config.Http1Config;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.http.impl.io.DefaultHttpRequestWriterFactory;
import org.apache.hc.core5.http.impl.io.DefaultHttpResponseParserFactory;
import org.apache.hc.core5.http.io.HttpConnectionFactory;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.util.Timeout;

import java.net.URL;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;

public class HTTPSenderImpl extends HTTPSender {

    private static final Log log = LogFactory.getLog(HTTPSenderImpl.class);

    @Override
    protected Request createRequest(MessageContext msgContext, String methodName, URL url,
            AxisRequestEntity requestEntity) throws AxisFault {

        try {
            RequestImpl requestImpl = new RequestImpl(getHttpClient(msgContext), msgContext, methodName, url.toURI(), requestEntity);
            return requestImpl;
        } catch (Exception ex) {
            throw AxisFault.makeFault(ex);
        }
    }

    private HttpClient getHttpClient(MessageContext msgContext) {
        ConfigurationContext configContext = msgContext.getConfigurationContext();

        HttpClient httpClient = (HttpClient) msgContext
                .getProperty(HTTPConstants.CACHED_HTTP_CLIENT);

        if (httpClient == null) {
            httpClient = (HttpClient) configContext.
                    getProperty(HTTPConstants.CACHED_HTTP_CLIENT);
        }

        if (httpClient != null) {
            return httpClient;
        }

        synchronized (this) {
            httpClient = (HttpClient) msgContext.
                    getProperty(HTTPConstants.CACHED_HTTP_CLIENT);

            if (httpClient == null) {
                httpClient = (HttpClient) configContext
                        .getProperty(HTTPConstants.CACHED_HTTP_CLIENT);
            }

            if (httpClient != null) {
                return httpClient;
            }

            HttpClientConnectionManager connManager = (HttpClientConnectionManager) msgContext
                    .getProperty(HTTPConstants.MULTITHREAD_HTTP_CONNECTION_MANAGER);
            if (connManager == null) {
                connManager = (HttpClientConnectionManager) msgContext
                        .getProperty(HTTPConstants.MULTITHREAD_HTTP_CONNECTION_MANAGER);
            }
            if (connManager == null) {
                // reuse HttpConnectionManager
                synchronized (configContext) {
                    connManager = (HttpClientConnectionManager) configContext
                            .getProperty(HTTPConstants.MULTITHREAD_HTTP_CONNECTION_MANAGER);
                    if (connManager == null) {
                        log.trace("Making new ConnectionManager");
                        SSLContext sslContext = (SSLContext)configContext.getProperty(SSLContext.class.getName());
                        if (sslContext == null) {
                            sslContext = SSLContexts.createDefault();
                        }
                        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                                .register("https", new SSLConnectionSocketFactory(sslContext))
                                .build();

                        Integer tempSoTimeoutProperty = (Integer) msgContext.getProperty(HTTPConstants.SO_TIMEOUT);
                        Integer tempConnTimeoutProperty = (Integer) msgContext
                                .getProperty(HTTPConstants.CONNECTION_TIMEOUT);
                        long timeout = msgContext.getOptions().getTimeOutInMilliSeconds();
                
			Timeout connectTO;
                        if (tempConnTimeoutProperty != null) {
                            // timeout for initial connection
			    connectTO = Timeout.ofMilliseconds(tempConnTimeoutProperty);
                        } else {
			    // httpclient5 / core5 default	
			    connectTO = Timeout.ofMinutes(3);
			}
			Timeout socketTO;
                        if (tempSoTimeoutProperty != null) {
                            // SO_TIMEOUT -- timeout for blocking reads
			    socketTO = Timeout.ofMilliseconds(tempSoTimeoutProperty);
                        } else {
                            // set timeout in client
                            if (timeout > 0) {
			        socketTO = Timeout.ofMilliseconds(timeout);
                            } else {
                                log.error("Invalid timeout value detected: " + timeout + " , using 3 minute default");
			        socketTO = Timeout.ofMilliseconds(180000);
			    }	    
                        }
			SocketConfig socketConfig = SocketConfig.custom()
                            // set timeouts, ignore other defaults
                            .setSoTimeout(socketTO)
                            .setSoKeepAlive(true)
                            .setSoReuseAddress(true)
                            .setTcpNoDelay(true)
                            .setSoLinger(50, TimeUnit.MILLISECONDS)
                            .setSndBufSize(8 * 1024)
                            .setRcvBufSize(8 * 1024)
                            .build();

                        ConnectionConfig connectionConfig = ConnectionConfig.custom().setConnectTimeout(connectTO).build();

                        // Create HTTP/1.1 protocol configuration
                        Http1Config http1Config = Http1Config.custom()
                            .setMaxHeaderCount(500)
                            .setMaxLineLength(4000)
                            .setMaxEmptyLineCount(1)
                            .build();

			final HttpConnectionFactory<ManagedHttpClientConnection> connFactory = new ManagedHttpClientConnectionFactory(
                http1Config, CharCodingConfig.DEFAULT, new DefaultHttpRequestWriterFactory(), new DefaultHttpResponseParserFactory(http1Config));

                        connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry, connFactory);
                        ((PoolingHttpClientConnectionManager)connManager).setMaxTotal(200);
                        ((PoolingHttpClientConnectionManager)connManager).setDefaultMaxPerRoute(200);
                        ((PoolingHttpClientConnectionManager)connManager).setDefaultSocketConfig(socketConfig);
                        ((PoolingHttpClientConnectionManager)connManager).setDefaultConnectionConfig(connectionConfig);
                        configContext.setProperty(
                                HTTPConstants.MULTITHREAD_HTTP_CONNECTION_MANAGER, connManager);
                    }
                }
            }
            /*
             * Create a new instance of HttpClient since the way it is used here
             * it's not fully thread-safe.
             */
            return HttpClientBuilder.create()
                    .setConnectionManager(connManager)
                    .setConnectionManagerShared(true)
                    .build();
        }
    }

}
