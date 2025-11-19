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

package org.apache.axis2.transport.h2.impl.httpclient5;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.kernel.http.HTTPConstants;
import org.apache.axis2.transport.http.AxisRequestEntity;
import org.apache.axis2.transport.http.HTTPSender;
import org.apache.axis2.transport.http.Request;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// HTTP/2 specific imports
import org.apache.hc.client5.http.async.HttpAsyncClient;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.client5.http.nio.AsyncClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.config.CharCodingConfig;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.http.nio.ssl.TlsStrategy;
import org.apache.hc.core5.http.nio.ssl.BasicClientTlsStrategy;
import org.apache.hc.core5.http.ssl.TLS;
import org.apache.hc.core5.http2.HttpVersionPolicy;
import org.apache.hc.core5.http2.config.H2Config;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.util.Timeout;

import java.net.URL;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;

/**
 * H2SenderImpl provides HTTP/2 transport capabilities using HttpClient 5.x async client
 * with multiplexing, connection reuse, and optimized performance for large JSON payloads.
 *
 * Key HTTP/2 features:
 * - Async non-blocking execution with FutureCallback pattern
 * - Connection multiplexing (multiple streams per connection)
 * - Memory-optimized connection pooling for constrained environments
 * - ALPN protocol negotiation
 * - Stream priority and flow control for large payloads
 */
public class H2SenderImpl extends HTTPSender {

    private static final Log log = LogFactory.getLog(H2SenderImpl.class);

    // HTTP/2 client instance from transport sender (Stage 2 enhancement)
    private final CloseableHttpAsyncClient http2Client;

    // HTTP/2 configuration constants for memory-constrained environments (2GB heap)
    private static final int MAX_CONCURRENT_STREAMS = 100;  // vs default 1000
    private static final int INITIAL_WINDOW_SIZE = 32768;   // 32KB for 50MB+ JSON
    private static final int MAX_CONNECTIONS_TOTAL = 50;    // vs default 100
    private static final int MAX_CONNECTIONS_PER_ROUTE = 10; // Route-specific limit

    /**
     * Constructor that accepts HTTP/2 client from transport sender.
     * Stage 2 enhancement for centralized HTTP/2 configuration.
     */
    public H2SenderImpl(CloseableHttpAsyncClient http2Client) {
        this.http2Client = http2Client;
        log.debug("H2SenderImpl initialized with configured HTTP/2 client");
    }

    /**
     * Default constructor for backward compatibility.
     * Creates its own HTTP/2 client if none provided.
     */
    public H2SenderImpl() {
        this.http2Client = null; // Will create own client in getHttpAsyncClient()
        log.debug("H2SenderImpl initialized with default configuration");
    }

    @Override
    protected Request createRequest(MessageContext msgContext, String methodName, URL url,
            AxisRequestEntity requestEntity) throws AxisFault {

        try {
            H2RequestImpl requestImpl = new H2RequestImpl(
                getHttpAsyncClient(msgContext), msgContext, methodName, url.toURI(), requestEntity);
            return requestImpl;
        } catch (Exception ex) {
            throw AxisFault.makeFault(ex);
        }
    }

    /**
     * Creates or retrieves a cached HTTP/2 async client with optimized configuration
     * for large JSON payload processing and memory-constrained environments.
     *
     * Stage 2 Enhancement: Prioritizes HTTP/2 client from transport sender for
     * centralized configuration and connection multiplexing.
     */
    private CloseableHttpAsyncClient getHttpAsyncClient(MessageContext msgContext) {
        // Stage 2: Use HTTP/2 client from transport sender if available
        if (this.http2Client != null) {
            log.trace("Using HTTP/2 client from transport sender (Stage 2 configuration)");
            return this.http2Client;
        }

        // Fallback to cached client lookup for backward compatibility
        ConfigurationContext configContext = msgContext.getConfigurationContext();

        CloseableHttpAsyncClient httpAsyncClient = (CloseableHttpAsyncClient) msgContext
                .getProperty("CACHED_HTTP2_ASYNC_CLIENT");

        if (httpAsyncClient == null) {
            httpAsyncClient = (CloseableHttpAsyncClient) configContext
                    .getProperty("CACHED_HTTP2_ASYNC_CLIENT");
        }

        if (httpAsyncClient != null) {
            // Start client if not already started
            httpAsyncClient.start();
            return httpAsyncClient;
        }

        synchronized (this) {
            httpAsyncClient = (CloseableHttpAsyncClient) msgContext
                    .getProperty("CACHED_HTTP2_ASYNC_CLIENT");

            if (httpAsyncClient == null) {
                httpAsyncClient = (CloseableHttpAsyncClient) configContext
                        .getProperty("CACHED_HTTP2_ASYNC_CLIENT");
            }

            if (httpAsyncClient != null) {
                // Start client if not already started
                httpAsyncClient.start();
                return httpAsyncClient;
            }

            if (httpAsyncClient != null) {
                return httpAsyncClient;
            }

            AsyncClientConnectionManager connManager = (AsyncClientConnectionManager) msgContext
                    .getProperty("MULTITHREAD_HTTP2_CONNECTION_MANAGER");
            if (connManager == null) {
                connManager = (AsyncClientConnectionManager) configContext
                        .getProperty("MULTITHREAD_HTTP2_CONNECTION_MANAGER");
            }

            if (connManager == null) {
                // Create HTTP/2 optimized connection manager
                synchronized (configContext) {
                    connManager = (AsyncClientConnectionManager) configContext
                            .getProperty("MULTITHREAD_HTTP2_CONNECTION_MANAGER");
                    if (connManager == null) {
                        log.trace("Making new HTTP/2 ConnectionManager");
                        connManager = createHttp2ConnectionManager(msgContext, configContext);
                        configContext.setProperty("MULTITHREAD_HTTP2_CONNECTION_MANAGER", connManager);
                    }
                }
            }

            // Create HTTP/2 optimized async client
            httpAsyncClient = createHttp2AsyncClient(connManager);
            httpAsyncClient.start();

            // Cache the client
            configContext.setProperty("CACHED_HTTP2_ASYNC_CLIENT", httpAsyncClient);
        }

        return httpAsyncClient;
    }

    /**
     * Creates HTTP/2 optimized connection manager with async capabilities
     */
    private AsyncClientConnectionManager createHttp2ConnectionManager(
            MessageContext msgContext, ConfigurationContext configContext) {

        SSLContext sslContext = (SSLContext) configContext.getProperty(SSLContext.class.getName());
        if (sslContext == null) {
            sslContext = SSLContexts.createDefault();
        }

        // Configure TLS strategy for HTTP/2 ALPN negotiation
        TlsStrategy tlsStrategy = new BasicClientTlsStrategy(sslContext);

        // Setup timeout configurations
        Integer tempSoTimeoutProperty = (Integer) msgContext.getProperty(HTTPConstants.SO_TIMEOUT);
        Integer tempConnTimeoutProperty = (Integer) msgContext
                .getProperty(HTTPConstants.CONNECTION_TIMEOUT);
        long timeout = msgContext.getOptions().getTimeOutInMilliSeconds();

        Timeout connectTO = tempConnTimeoutProperty != null
            ? Timeout.ofMilliseconds(tempConnTimeoutProperty)
            : Timeout.ofMinutes(3);

        Timeout socketTO;
        if (tempSoTimeoutProperty != null) {
            socketTO = Timeout.ofMilliseconds(tempSoTimeoutProperty);
        } else if (timeout > 0) {
            socketTO = Timeout.ofMilliseconds(timeout);
        } else {
            log.error("Invalid timeout value detected: " + timeout + " , using 3 minute default");
            socketTO = Timeout.ofMilliseconds(180000);
        }

        // Configure I/O reactor for HTTP/2 async operations
        IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
                .setSoTimeout(socketTO)
                .setTcpNoDelay(true)
                .setSoKeepAlive(true)
                .build();

        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setConnectTimeout(connectTO)
                .build();

        // Create async connection manager optimized for HTTP/2
        PoolingAsyncClientConnectionManager poolingConnManager =
            PoolingAsyncClientConnectionManagerBuilder.create()
                .setTlsStrategy(tlsStrategy)
                .setMaxConnTotal(MAX_CONNECTIONS_TOTAL)
                .setMaxConnPerRoute(MAX_CONNECTIONS_PER_ROUTE)
                .setDefaultConnectionConfig(connectionConfig)
                .build();

        return poolingConnManager;
    }

    /**
     * Creates HTTP/2 async client with optimized configuration for large JSON payloads
     */
    private CloseableHttpAsyncClient createHttp2AsyncClient(AsyncClientConnectionManager connManager) {

        // Configure HTTP/2 specific settings
        H2Config h2Config = H2Config.custom()
                .setMaxConcurrentStreams(MAX_CONCURRENT_STREAMS)
                .setPushEnabled(false)  // Server push disabled for web services
                .setInitialWindowSize(INITIAL_WINDOW_SIZE)  // Optimized for 50MB+ JSON
                .build();

        return HttpAsyncClients.custom()
                .setConnectionManager(connManager)
                .setConnectionManagerShared(true)
                .setVersionPolicy(HttpVersionPolicy.FORCE_HTTP_2)  // Force HTTP/2
                .setH2Config(h2Config)
                .build();
    }
}