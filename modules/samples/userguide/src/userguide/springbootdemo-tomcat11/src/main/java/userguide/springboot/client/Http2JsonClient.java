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
package userguide.springboot.client;

import javax.net.ssl.SSLContext;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.nio.channels.ClosedChannelException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.ConnectionClosedException;
import org.apache.hc.core5.http.ConnectionRequestTimeoutException;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpStreamResetException;
import org.apache.hc.core5.http2.H2ConnectionException;
import org.apache.hc.core5.http2.config.H2Config;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.util.DeadlineTimeoutException;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;

/**
 * Standalone HTTP/2 JSON client for Apache Axis2 services.
 *
 * <p>Uses Apache HttpClient 5 async with HTTP/2 (ALPN over TLS) to call
 * any Axis2 JSON-RPC endpoint. Two execution modes:</p>
 *
 * <ul>
 *   <li>{@link #execute} — returns the full response as a String</li>
 *   <li>{@link #executeStreaming} — writes response bytes to an OutputStream
 *       in 64KB chunks as HTTP/2 DATA frames arrive; memory stays flat</li>
 * </ul>
 *
 * <p>Requires: Java 11+, Apache HttpClient 5.4+ (httpcore5-h2 for HTTP/2).</p>
 *
 * <h3>Production hardening</h3>
 * <p>Beyond the basic ALPN negotiation, this sample includes the connection-pool
 * hardening a long-lived client needs to survive an upstream restart or a dropped
 * network path — lessons learned running an HTTP/2 client against a load-balanced
 * service in production:</p>
 * <ul>
 *   <li><strong>Stale-connection defenses</strong> — validate-after-inactivity, a
 *       bounded connection time-to-live, and background idle/expired eviction. Without
 *       these, a pooled client keeps dispatching requests onto connections killed by an
 *       upstream restart and hangs until the socket timeout.</li>
 *   <li><strong>TCP keepalive</strong> — detects a silently-dead peer (no FIN/RST, e.g.
 *       a hard kill or an idle flow dropped by a NAT gateway / load balancer) in seconds
 *       rather than the multi-hour OS default.</li>
 *   <li><strong>Bounded retry</strong> — a request dispatched onto a dead pooled
 *       connection fails with a recognizable connection-level exception; retrying it once
 *       on a fresh, validated connection recovers transparently. Retry is applied only
 *       while no response bytes have been written to the caller's stream (you cannot
 *       rewind it) and is safe only for idempotent operations.</li>
 *   <li><strong>Fail fast on the response status</strong> — {@link
 *       org.apache.hc.client5.http.async.methods.AbstractBinResponseConsumer#start} sees
 *       the status the moment headers arrive; an intermediary can return error headers and
 *       never terminate the body stream, so react to the status instead of waiting for a
 *       body that may never come.</li>
 * </ul>
 *
 * <h3>Quick start</h3>
 * <pre>
 * // 1. POST JSON-RPC, get response as String
 * String response = Http2JsonClient.execute(
 *     "https://localhost:8443/axis2-json-api/services/FinancialBenchmarkService",
 *     "{\"monteCarlo\":[{\"arg0\":{\"nSimulations\":100000}}]}",
 *     300);
 *
 * // 2. Stream a large response to a file
 * try (FileOutputStream fos = new FileOutputStream("/tmp/result.json")) {
 *     int status = Http2JsonClient.executeStreaming(
 *         "https://localhost:8443/axis2-json-api/services/BigDataH2Service",
 *         "{\"generate\":[{\"arg0\":{\"datasetSize\":52428800}}]}",
 *         300, fos);
 * }
 *
 * // 3. Shutdown when done
 * Http2JsonClient.shutdown();
 * </pre>
 *
 * @see <a href="https://axis.apache.org/axis2/java/core/docs/http2-integration-guide.html">
 *      Axis2 HTTP/2 Integration Guide</a>
 */
public class Http2JsonClient {

    /** Maximum request attempts (initial + retries) for transient connection failures. */
    private static final int MAX_ATTEMPTS = 3;
    /** Delay between retry attempts. */
    private static final long RETRY_DELAY_MS = 1500L;

    private static volatile CloseableHttpAsyncClient sharedClient;

    /**
     * Get or create the shared HTTP/2 async client.
     *
     * <p>Initialized once, reused for all requests. Uses TLS with
     * TrustAllStrategy for development — replace with proper trust
     * material (and enable hostname verification) in production.</p>
     */
    private static synchronized CloseableHttpAsyncClient getClient() throws Exception {
        if (sharedClient == null) {
            H2Config h2Config = H2Config.custom()
                .setMaxConcurrentStreams(100)
                .setPushEnabled(false)
                .setInitialWindowSize(65536)
                .build();

            // Connection-level config: connect timeout, plus the two stale-connection
            // defenses. validateAfterInactivity re-checks a pooled connection before it is
            // leased if it has been idle longer than the given time; timeToLive caps how
            // long any connection may live, so connections are recycled across upstream
            // restarts / DNS or LB changes regardless of traffic.
            ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setConnectTimeout(Timeout.ofSeconds(30))
                .setValidateAfterInactivity(TimeValue.ofSeconds(10))
                .setTimeToLive(TimeValue.ofMinutes(5))
                .build();

            IOReactorConfig ioConfig = IOReactorConfig.custom()
                .setTcpNoDelay(true)
                .setSoTimeout(Timeout.ofMinutes(5))
                // TCP keepalive — catch a silently-dead peer (no FIN/RST) in ~75-105s
                // instead of hanging to soTimeout. Keep the idle time below the shortest
                // idle-drop on the network path (NAT gateways commonly drop idle flows at
                // ~350s; load-balancer idle timeouts vary and can be as low as 60s). The
                // extended probe knobs need Linux or macOS and Java 11+
                // (jdk.net.ExtendedSocketOptions); soKeepAlive alone falls back to the OS
                // default (~2 hours), which is too slow to be useful.
                .setSoKeepAlive(true)
                .setTcpKeepIdle(45)
                .setTcpKeepInterval(10)
                .setTcpKeepCount(3)
                .build();

            SSLContext sslContext = SSLContexts.custom()
                .loadTrustMaterial(null, TrustAllStrategy.INSTANCE)
                .build();

            PoolingAsyncClientConnectionManager connManager =
                PoolingAsyncClientConnectionManagerBuilder.create()
                    .setTlsStrategy(ClientTlsStrategyBuilder.create()
                        .setSslContext(sslContext)
                        .setHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                        .build())
                    .setDefaultConnectionConfig(connectionConfig)
                    .setMaxConnTotal(50)
                    .setMaxConnPerRoute(10)
                    .build();

            sharedClient = HttpAsyncClients.custom()
                .setH2Config(h2Config)
                .setIOReactorConfig(ioConfig)
                .setConnectionManager(connManager)
                // Background evictor: drains idle/expired connections so a connection
                // killed by an upstream restart dies proactively instead of waiting to be
                // discovered on the next lease.
                .evictExpiredConnections()
                .evictIdleConnections(TimeValue.ofSeconds(90))
                .build();

            sharedClient.start();
        }
        return sharedClient;
    }

    /**
     * POST JSON to an Axis2 service and return the response as a String.
     *
     * <p>Uses the same streaming transport as {@link #executeStreaming},
     * writing to a {@code ByteArrayOutputStream} and converting to String.
     * This avoids {@code SimpleHttpResponse}'s internal buffering overhead
     * while maintaining a simple String return type.</p>
     *
     * @param url            HTTPS endpoint (e.g., {@code https://host:8443/axis2-json-api/services/MyService})
     * @param json           JSON-RPC request body
     * @param timeoutSeconds maximum wait time for the response
     * @return response body as a String
     * @throws Exception on HTTP error or timeout
     */
    public static String execute(String url, String json, int timeoutSeconds) throws Exception {
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        executeStreaming(url, json, timeoutSeconds, baos);
        return baos.toString(StandardCharsets.UTF_8.name());
    }

    /**
     * POST JSON to an Axis2 service and stream the response to an OutputStream.
     *
     * <p>Instead of buffering the entire response in memory, writes 64KB chunks
     * to the provided OutputStream as HTTP/2 DATA frames arrive. Memory stays
     * flat regardless of response size.</p>
     *
     * <p>When paired with Axis2's
     * {@code MoshiStreamingMessageFormatter} (AXIS2-6103), data flows
     * end-to-end in 64KB chunks: server flushes → HTTP/2 DATA frames →
     * this callback → your OutputStream.</p>
     *
     * <p>A request that fails with a transient connection-level exception (the
     * signature of a dead pooled connection after an upstream restart) is retried
     * on a fresh connection, but only while no bytes have yet been written to
     * {@code outputStream} — a partially written stream cannot be rewound. Retry
     * is therefore safe only for idempotent operations.</p>
     *
     * @param url            HTTPS endpoint
     * @param json           JSON-RPC request body
     * @param timeoutSeconds maximum wait time for the response
     * @param outputStream   destination for response bytes
     * @return HTTP status code
     * @throws Exception on HTTP error or timeout
     */
    public static int executeStreaming(String url, String json,
                                       int timeoutSeconds, OutputStream outputStream) throws Exception {
        CloseableHttpAsyncClient client = getClient();
        Exception lastFailure = null;

        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            org.apache.hc.core5.http.nio.AsyncRequestProducer requestProducer =
                org.apache.hc.core5.http.nio.support.AsyncRequestBuilder.post(url)
                    .setEntity(json, ContentType.APPLICATION_JSON)
                    .addHeader("Accept", "application/json")
                    .build();

            final long[] totalBytes = {0};
            final int[] statusCode = {0};

            org.apache.hc.client5.http.async.methods.AbstractBinResponseConsumer<Integer> consumer =
                new org.apache.hc.client5.http.async.methods.AbstractBinResponseConsumer<Integer>() {

                    private byte[] transferBuffer;

                    @Override
                    protected void start(org.apache.hc.core5.http.HttpResponse response,
                                         org.apache.hc.core5.http.ContentType contentType)
                            throws org.apache.hc.core5.http.HttpException, java.io.IOException {
                        statusCode[0] = response.getCode();
                        // Fail fast on non-2xx — abort before writing error body to the OutputStream.
                        // The status is available as soon as headers arrive, so we never wait for an
                        // error body that an intermediary might never terminate.
                        if (statusCode[0] < 200 || statusCode[0] >= 300) {
                            throw new java.io.IOException("HTTP " + statusCode[0] + " from streaming request");
                        }
                    }

                    @Override
                    protected int capacityIncrement() {
                        return 64 * 1024;
                    }

                    @Override
                    protected void data(java.nio.ByteBuffer src, boolean endOfStream)
                            throws java.io.IOException {
                        int len = src.remaining();
                        if (len > 0) {
                            if (src.hasArray()) {
                                outputStream.write(src.array(), src.arrayOffset() + src.position(), len);
                                src.position(src.position() + len);
                            } else {
                                if (transferBuffer == null || transferBuffer.length < len) {
                                    transferBuffer = new byte[len];
                                }
                                src.get(transferBuffer, 0, len);
                                outputStream.write(transferBuffer, 0, len);
                            }
                            outputStream.flush();
                            totalBytes[0] += len;
                        }
                    }

                    @Override
                    protected Integer buildResult() { return statusCode[0]; }

                    @Override
                    public void releaseResources() { }
                };

            CompletableFuture<Integer> future = new CompletableFuture<>();
            Future<Integer> requestFuture = client.execute(requestProducer, consumer,
                new FutureCallback<Integer>() {
                    @Override public void completed(Integer r) { future.complete(r); }
                    @Override public void failed(Exception ex) { future.completeExceptionally(ex); }
                    @Override public void cancelled() { future.cancel(true); }
                });

            try {
                return future.get(timeoutSeconds, TimeUnit.SECONDS);
            } catch (Exception e) {
                requestFuture.cancel(true);
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                    throw e;
                }
                lastFailure = e;
                // Retry only if: attempts remain, nothing was written to the caller's stream
                // yet (we cannot rewind it), and the failure looks like a transient/stale
                // connection rather than a real error. A first-attempt Future.get() timeout is
                // treated as retryable once — it is the signature of a dead pooled connection.
                boolean retryable = isRetryableConnectionException(e)
                        || (attempt == 0 && e instanceof TimeoutException);
                if (attempt + 1 < MAX_ATTEMPTS && totalBytes[0] == 0 && retryable) {
                    Thread.sleep(RETRY_DELAY_MS);
                    continue;
                }
                throw e;
            }
        }
        throw lastFailure;  // unreachable: the final attempt returns or throws
    }

    /**
     * True for transient connection-level failures that are safe to retry on a fresh
     * connection — TCP resets/refusals/timeouts and the exceptions a dead pooled HTTP/2
     * connection throws (GOAWAY race, stream reset, connection-level error, closed
     * channel, pool-lease timeout). NOT for TLS handshake failures or application errors,
     * which are not transient.
     */
    private static boolean isRetryableConnectionException(Throwable ex) {
        Throwable cause = ex;
        while (cause != null) {
            if (cause instanceof SocketException
                    || cause instanceof ConnectException
                    || cause instanceof SocketTimeoutException
                    || cause instanceof ConnectionClosedException   // also covers the GOAWAY-race RequestNotExecutedException
                    || cause instanceof HttpStreamResetException    // also covers H2StreamResetException
                    || cause instanceof H2ConnectionException
                    || cause instanceof ClosedChannelException
                    || cause instanceof DeadlineTimeoutException
                    || cause instanceof ConnectionRequestTimeoutException) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    /**
     * Shut down the shared HTTP/2 client. Call once at application exit.
     */
    public static synchronized void shutdown() {
        if (sharedClient != null) {
            try {
                sharedClient.close();
            } catch (Exception e) {
                System.err.println("Error shutting down HTTP/2 client: " + e.getMessage());
            } finally {
                sharedClient = null;
            }
        }
    }
}
