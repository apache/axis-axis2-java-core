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
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http2.config.H2Config;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.ssl.SSLContexts;
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

    private static volatile CloseableHttpAsyncClient sharedClient;

    /**
     * Get or create the shared HTTP/2 async client.
     *
     * <p>Initialized once, reused for all requests. Uses TLS with
     * TrustAllStrategy for development — replace with proper trust
     * material in production.</p>
     */
    private static synchronized CloseableHttpAsyncClient getClient() throws Exception {
        if (sharedClient == null) {
            H2Config h2Config = H2Config.custom()
                .setMaxConcurrentStreams(100)
                .setPushEnabled(false)
                .setInitialWindowSize(65536)
                .build();

            IOReactorConfig ioConfig = IOReactorConfig.custom()
                .setTcpNoDelay(true)
                .setSoTimeout(Timeout.ofMinutes(5))
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
                    .setMaxConnTotal(50)
                    .setMaxConnPerRoute(10)
                    .build();

            sharedClient = HttpAsyncClients.custom()
                .setH2Config(h2Config)
                .setIOReactorConfig(ioConfig)
                .setConnectionManager(connManager)
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

        org.apache.hc.core5.http.nio.AsyncRequestProducer requestProducer =
            org.apache.hc.core5.http.nio.support.AsyncRequestBuilder.post(url)
                .setEntity(json, ContentType.APPLICATION_JSON)
                .addHeader("Accept", "application/json")
                .build();

        final long[] totalBytes = {0};
        final int[] chunkCount = {0};
        final int[] statusCode = {0};

        org.apache.hc.client5.http.async.methods.AbstractBinResponseConsumer<Integer> consumer =
            new org.apache.hc.client5.http.async.methods.AbstractBinResponseConsumer<Integer>() {

                private byte[] transferBuffer;

                @Override
                protected void start(org.apache.hc.core5.http.HttpResponse response,
                                     org.apache.hc.core5.http.ContentType contentType)
                        throws org.apache.hc.core5.http.HttpException, java.io.IOException {
                    statusCode[0] = response.getCode();
                    // Fail fast on non-2xx — abort before writing error body to the OutputStream
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
                        chunkCount[0]++;
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

        int result;
        try {
            result = future.get(timeoutSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            requestFuture.cancel(true);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw e;
        }

        return result;
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
