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

package org.apache.axis2.builder;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import junit.framework.TestCase;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.AxisFault;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.kernel.http.HTTPConstants;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for the multipart builder's request-size ceilings and temporary file handling.
 *
 * <p>A part larger than the disk threshold is written to a temp file. Before
 * this was addressed, nothing ever deleted those files and they accumulated for
 * the lifetime of the JVM.
 */
public class MultipartFormDataBuilderTest extends TestCase {

    private static final String BOUNDARY = "axis2TestBoundary";

    /** Comfortably above the factory's spill-to-disk threshold. */
    private static final int PART_SIZE = 128 * 1024;

    private File tempDirectory;

    protected void setUp() throws Exception {
        super.setUp();
        tempDirectory = new File(System.getProperty("java.io.tmpdir"));
    }

    /**
     * The ceiling is enforced through the real builder, not merely resolved.
     *
     * <p>Worth stating why this test exists separately from
     * {@link RequestSizeLimitsTest}: that one covers parameter resolution, and
     * it is easy to check the limit by reconstructing an upload object rather
     * than going through {@code processDocument}, which demonstrates nothing
     * about whether an oversized body is actually refused.
     */
    public void testOversizedRequestIsRejected() throws Exception {
        MessageContext messageContext = newMessageContext(buildBody("bigFile", "big.bin"));
        messageContext.getConfigurationContext().getAxisConfiguration().addParameter(
                new Parameter(RequestSizeLimits.MULTIPART_MAX_REQUEST_SIZE,
                        Integer.toString(PART_SIZE / 4)));

        try {
            new MultipartFormDataBuilder().processDocument(null, multipartContentType(),
                    messageContext);
            fail("A body over the configured ceiling should have been refused");
        } catch (AxisFault expected) {
            // The builder wraps the upload failure; what matters is that the
            // oversized body did not get materialised.
        }
    }

    /** A body within the ceiling still goes through untouched. */
    public void testRequestWithinTheLimitIsAccepted() throws Exception {
        MessageContext messageContext = newMessageContext(buildBody("bigFile", "big.bin"));
        messageContext.getConfigurationContext().getAxisConfiguration().addParameter(
                new Parameter(RequestSizeLimits.MULTIPART_MAX_REQUEST_SIZE,
                        Integer.toString(PART_SIZE * 4)));

        assertNotNull(new MultipartFormDataBuilder().processDocument(null,
                multipartContentType(), messageContext));
    }

    /**
     * The shipped default has to be a real number rather than the -1 the sink
     * used to leave in place.
     */
    public void testDefaultCeilingIsFinite() throws Exception {
        MessageContext messageContext = newMessageContext(buildBody("bigFile", "big.bin"));
        long limit = RequestSizeLimits.resolve(messageContext,
                RequestSizeLimits.MULTIPART_MAX_REQUEST_SIZE,
                RequestSizeLimits.DEFAULT_MULTIPART_MAX_REQUEST_SIZE);
        assertTrue("The default request ceiling must be bounded", limit > 0);
        assertEquals(RequestSizeLimits.DEFAULT_MULTIPART_MAX_REQUEST_SIZE, limit);
    }

    /**
     * A form field is fully materialised into the parameter map during the
     * build, so its temp file should be gone by the time the builder returns
     * rather than waiting on the reaper.
     */
    public void testFormFieldTempFileIsDeletedImmediately() throws Exception {
        int before = countUploadTempFiles();

        MultipartFormDataBuilder builder = new MultipartFormDataBuilder();
        builder.processDocument(null, multipartContentType(),
                newMessageContext(buildBody("bigField", null)));

        assertEquals("A form field must not leave a temporary file behind",
                before, countUploadTempFiles());
    }

    /**
     * A file part stays readable through the DataHandler handed to the service,
     * so it cannot be deleted during the build — but it must at least be
     * registered for deletion.
     */
    public void testFilePartIsRegisteredForCleanup() throws Exception {
        int trackedBefore = MultipartTempFileTracker.getTrackedFileCount();

        MultipartFormDataBuilder builder = new MultipartFormDataBuilder();
        OMElement result = builder.processDocument(null, multipartContentType(),
                newMessageContext(buildBody("bigFile", "big.bin")));

        assertNotNull(result);
        assertTrue("The file part's temp file should be registered with the reaper",
                MultipartTempFileTracker.getTrackedFileCount() > trackedBefore);
    }

    /**
     * End to end: once the built message is unreachable, the reaper should
     * actually remove the file from disk.
     */
    public void testFilePartTempFileIsDeletedOnceUnreachable() throws Exception {
        int before = countUploadTempFiles();

        MultipartFormDataBuilder builder = new MultipartFormDataBuilder();
        OMElement result = builder.processDocument(null, multipartContentType(),
                newMessageContext(buildBody("bigFile", "big.bin")));
        assertNotNull(result);
        assertEquals("The file part should still be on disk while it is readable",
                before + 1, countUploadTempFiles());

        // Drop every reference to the item that owns the file, then let the
        // phantom reference the reaper is waiting on become enqueueable.
        result = null;
        builder = null;

        assertTrue("The reaper should have deleted the temporary file",
                awaitTempFileCount(before));
    }

    /**
     * Poll for the temp-file count to fall back to the expected value, nudging
     * the collector each time, since the reaper only acts once the owning item
     * has been collected.
     */
    private boolean awaitTempFileCount(int expected) throws InterruptedException {
        for (int attempt = 0; attempt < 50; attempt++) {
            System.gc();
            if (countUploadTempFiles() <= expected) {
                return true;
            }
            Thread.sleep(100);
        }
        return false;
    }

    private int countUploadTempFiles() {
        String[] names = tempDirectory.list();
        if (names == null) {
            return 0;
        }
        int count = 0;
        for (int i = 0; i < names.length; i++) {
            // commons-fileupload2 names its spill files upload_<uid>_<n>.tmp
            if (names[i].startsWith("upload_") && names[i].endsWith(".tmp")) {
                count++;
            }
        }
        return count;
    }

    private String multipartContentType() {
        return "multipart/form-data; boundary=" + BOUNDARY;
    }

    /**
     * Build a single-part multipart body. Passing a file name makes it a file
     * part rather than a plain form field.
     */
    private byte[] buildBody(String fieldName, String fileName) {
        StringBuilder header = new StringBuilder();
        header.append("--").append(BOUNDARY).append("\r\n");
        header.append("Content-Disposition: form-data; name=\"").append(fieldName).append('"');
        if (fileName != null) {
            header.append("; filename=\"").append(fileName).append('"');
        }
        header.append("\r\n");
        if (fileName != null) {
            header.append("Content-Type: application/octet-stream\r\n");
        }
        header.append("\r\n");

        StringBuilder body = new StringBuilder(header.toString());
        for (int i = 0; i < PART_SIZE; i++) {
            body.append('a');
        }
        body.append("\r\n--").append(BOUNDARY).append("--\r\n");
        return body.toString().getBytes(StandardCharsets.UTF_8);
    }

    private MessageContext newMessageContext(byte[] body) throws Exception {
        AxisConfiguration axisConfiguration = new AxisConfiguration();
        ConfigurationContext configurationContext = new ConfigurationContext(axisConfiguration);
        MessageContext messageContext = configurationContext.createMessageContext();
        messageContext.setProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST, mockRequest(body));
        return messageContext;
    }

    private HttpServletRequest mockRequest(byte[] body) throws IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getContentType()).thenReturn(multipartContentType());
        when(request.getCharacterEncoding()).thenReturn("UTF-8");
        when(request.getContentLength()).thenReturn(body.length);
        when(request.getContentLengthLong()).thenReturn((long) body.length);
        when(request.getInputStream()).thenReturn(new MockServletInputStream(body));
        return request;
    }

    private static class MockServletInputStream extends ServletInputStream {

        private final ByteArrayInputStream delegate;

        MockServletInputStream(byte[] body) {
            this.delegate = new ByteArrayInputStream(body);
        }

        public int read() {
            return delegate.read();
        }

        public int read(byte[] b, int off, int len) {
            return delegate.read(b, off, len);
        }

        public boolean isFinished() {
            return delegate.available() == 0;
        }

        public boolean isReady() {
            return true;
        }

        public void setReadListener(ReadListener readListener) {
            throw new UnsupportedOperationException();
        }
    }
}
