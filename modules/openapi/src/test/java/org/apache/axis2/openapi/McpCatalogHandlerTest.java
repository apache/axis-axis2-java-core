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
package org.apache.axis2.openapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import junit.framework.TestCase;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.InOutAxisOperation;
import org.apache.axis2.engine.AxisConfiguration;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpUpgradeHandler;
import jakarta.servlet.http.Part;

import javax.xml.namespace.QName;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Unit tests for {@link SwaggerUIHandler#handleMcpCatalogRequest}.
 *
 * <p>Verifies HTTP response headers (Content-Type, CORS, security headers),
 * status code, and body validity for the {@code /openapi-mcp.json} endpoint.
 */
public class McpCatalogHandlerTest extends TestCase {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private SwaggerUIHandler handler;
    private MockHttpServletRequest  mockRequest;
    private MockHttpServletResponse mockResponse;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        AxisConfiguration axisConfig = new AxisConfiguration();
        ConfigurationContext configCtx = new ConfigurationContext(axisConfig);
        handler     = new SwaggerUIHandler(configCtx);
        mockRequest  = new MockHttpServletRequest();
        mockResponse = new MockHttpServletResponse();
    }

    // ── status ────────────────────────────────────────────────────────────────

    public void testMcpCatalogReturnsHttp200() throws Exception {
        handler.handleMcpCatalogRequest(mockRequest, mockResponse);
        assertEquals(200, mockResponse.getStatus());
    }

    // ── Content-Type ──────────────────────────────────────────────────────────

    public void testMcpCatalogContentTypeIsJson() throws Exception {
        handler.handleMcpCatalogRequest(mockRequest, mockResponse);
        String ct = mockResponse.getContentType();
        assertNotNull("Content-Type must be set", ct);
        assertTrue("Content-Type must be application/json", ct.contains("application/json"));
    }

    public void testMcpCatalogContentTypeIncludesUtf8() throws Exception {
        handler.handleMcpCatalogRequest(mockRequest, mockResponse);
        assertTrue("Content-Type must declare UTF-8 charset",
                mockResponse.getContentType().contains("UTF-8"));
    }

    // ── CORS headers ──────────────────────────────────────────────────────────

    public void testMcpCatalogHasCorsOriginHeader() throws Exception {
        handler.handleMcpCatalogRequest(mockRequest, mockResponse);
        assertEquals("*", mockResponse.getHeader("Access-Control-Allow-Origin"));
    }

    public void testMcpCatalogHasCorsMethodsHeader() throws Exception {
        handler.handleMcpCatalogRequest(mockRequest, mockResponse);
        assertNotNull("CORS methods header must be set",
                mockResponse.getHeader("Access-Control-Allow-Methods"));
    }

    public void testMcpCatalogHasCorsHeadersHeader() throws Exception {
        handler.handleMcpCatalogRequest(mockRequest, mockResponse);
        assertNotNull("CORS headers header must be set",
                mockResponse.getHeader("Access-Control-Allow-Headers"));
    }

    // ── body ──────────────────────────────────────────────────────────────────

    public void testMcpCatalogBodyIsValidJson() throws Exception {
        handler.handleMcpCatalogRequest(mockRequest, mockResponse);
        String body = mockResponse.getWriterContent();
        assertNotNull("Response body must not be null", body);
        assertFalse("Response body must not be empty", body.trim().isEmpty());
        JsonNode root = MAPPER.readTree(body);
        assertNotNull("Body must be parseable as JSON", root);
    }

    public void testMcpCatalogBodyHasToolsArray() throws Exception {
        handler.handleMcpCatalogRequest(mockRequest, mockResponse);
        JsonNode root = MAPPER.readTree(mockResponse.getWriterContent());
        assertTrue("Body must have 'tools' key", root.has("tools"));
        assertTrue("tools must be an array", root.get("tools").isArray());
    }

    // ── service discovery ─────────────────────────────────────────────────────

    public void testMcpCatalogReflectsRegisteredService() throws Exception {
        // Register a service and re-create the handler with the updated config
        AxisConfiguration axisConfig = new AxisConfiguration();
        AxisService svc = new AxisService("PaymentService");
        AxisOperation op = new InOutAxisOperation();
        op.setName(QName.valueOf("processPayment"));
        svc.addOperation(op);
        axisConfig.addService(svc);

        ConfigurationContext configCtx = new ConfigurationContext(axisConfig);
        SwaggerUIHandler h = new SwaggerUIHandler(configCtx);
        MockHttpServletResponse resp = new MockHttpServletResponse();

        h.handleMcpCatalogRequest(mockRequest, resp);

        JsonNode tools = MAPPER.readTree(resp.getWriterContent()).path("tools");
        assertTrue("At least one tool must be present", tools.size() > 0);

        boolean found = false;
        for (JsonNode tool : tools) {
            if ("processPayment".equals(tool.path("name").asText())) {
                found = true;
                break;
            }
        }
        assertTrue("processPayment tool must be in catalog", found);
    }

    // ── security headers ──────────────────────────────────────────────────────

    public void testMcpCatalogHasSecurityHeadersIfImplemented() throws Exception {
        handler.handleMcpCatalogRequest(mockRequest, mockResponse);
        // Cache-Control or X-Content-Type-Options may be set by the handler.
        // We don't assert their exact values — just that the request completes
        // successfully with a 200 status and valid JSON body.
        assertEquals(200, mockResponse.getStatus());
        JsonNode root = MAPPER.readTree(mockResponse.getWriterContent());
        assertTrue(root.has("tools"));
    }

    /**
     * The MCP catalog must include Cache-Control: no-cache so that MCP clients
     * always fetch a fresh catalog (service list can change after deployment).
     * Stale catalogs expose clients to "unknown tool" errors.
     */
    public void testMcpCatalogHasCacheControlNoCache() throws Exception {
        handler.handleMcpCatalogRequest(mockRequest, mockResponse);
        String cc = mockResponse.getHeader("Cache-Control");
        assertNotNull("Cache-Control header must be set", cc);
        assertTrue("Cache-Control must contain no-cache or no-store",
                cc.contains("no-cache") || cc.contains("no-store"));
    }

    /**
     * X-Content-Type-Options: nosniff prevents MIME-type sniffing by browsers
     * and some MCP client implementations that embed a WebView.
     */
    public void testMcpCatalogHasXContentTypeOptionsNoSniff() throws Exception {
        handler.handleMcpCatalogRequest(mockRequest, mockResponse);
        String xcto = mockResponse.getHeader("X-Content-Type-Options");
        assertNotNull("X-Content-Type-Options must be set", xcto);
        assertEquals("nosniff", xcto);
    }

    /**
     * CORS Allow-Methods must include GET — the catalog endpoint is a GET-only
     * resource. MCP clients POST to the individual service endpoints listed in
     * the catalog, not to the catalog URL itself.
     */
    public void testMcpCatalogCorsMethodsIncludesGet() throws Exception {
        handler.handleMcpCatalogRequest(mockRequest, mockResponse);
        String methods = mockResponse.getHeader("Access-Control-Allow-Methods");
        assertNotNull("Access-Control-Allow-Methods must be set", methods);
        assertTrue("CORS methods must include GET", methods.contains("GET"));
    }

    // ── new catalog fields reflected in handler response ──────────────────────

    /**
     * The handler response body must contain the {@code _meta} object that
     * documents the Axis2 JSON-RPC transport contract.
     */
    public void testMcpCatalogBodyHasMetaObject() throws Exception {
        handler.handleMcpCatalogRequest(mockRequest, mockResponse);
        JsonNode root = MAPPER.readTree(mockResponse.getWriterContent());
        assertFalse("Response body must contain _meta", root.path("_meta").isMissingNode());
        assertTrue("_meta must be an object", root.path("_meta").isObject());
    }

    public void testMcpCatalogMetaDocumentsAxis2Format() throws Exception {
        handler.handleMcpCatalogRequest(mockRequest, mockResponse);
        JsonNode meta = MAPPER.readTree(mockResponse.getWriterContent()).path("_meta");
        assertFalse("_meta.axis2JsonRpcFormat must be present",
                meta.path("axis2JsonRpcFormat").isMissingNode());
        String fmt = meta.path("axis2JsonRpcFormat").asText();
        assertTrue("Format string must contain 'arg0'", fmt.contains("arg0"));
    }

    /**
     * Tools served via the HTTP handler must carry the payload template and
     * auth annotation — verifies the full stack from handler to generator.
     */
    public void testMcpCatalogToolsHavePayloadTemplateAndAuth() throws Exception {
        // Register a service to get at least one tool
        AxisConfiguration axisConfig = new AxisConfiguration();
        AxisService svc = new AxisService("InventoryService");
        AxisOperation op = new InOutAxisOperation();
        op.setName(javax.xml.namespace.QName.valueOf("getStock"));
        svc.addOperation(op);
        axisConfig.addService(svc);

        ConfigurationContext configCtx = new ConfigurationContext(axisConfig);
        SwaggerUIHandler h = new SwaggerUIHandler(configCtx);
        MockHttpServletResponse resp = new MockHttpServletResponse();
        h.handleMcpCatalogRequest(mockRequest, resp);

        JsonNode tools = MAPPER.readTree(resp.getWriterContent()).path("tools");
        assertTrue("At least one tool must be present", tools.size() > 0);

        JsonNode tool = null;
        for (JsonNode t : tools) {
            if ("getStock".equals(t.path("name").asText())) { tool = t; break; }
        }
        assertNotNull("getStock tool must appear in catalog", tool);
        assertFalse("Tool must have x-axis2-payloadTemplate",
                tool.path("x-axis2-payloadTemplate").isMissingNode());
        assertFalse("Tool must have x-requiresAuth",
                tool.path("x-requiresAuth").isMissingNode());
        assertFalse("Tool must have annotations",
                tool.path("annotations").isMissingNode());
    }

    // ── mocks ─────────────────────────────────────────────────────────────────

    private static class MockHttpServletRequest implements HttpServletRequest {
        @Override public String getScheme()      { return "https"; }
        @Override public String getServerName()  { return "localhost"; }
        @Override public int    getServerPort()  { return 8443; }
        @Override public String getContextPath() { return "/axis2-json-api"; }

        @Override public String getAuthType() { return null; }
        @Override public Cookie[] getCookies() { return new Cookie[0]; }
        @Override public long getDateHeader(String n) { return 0; }
        @Override public String getHeader(String n) { return null; }
        @Override public Enumeration<String> getHeaders(String n) { return null; }
        @Override public Enumeration<String> getHeaderNames() { return null; }
        @Override public int getIntHeader(String n) { return 0; }
        @Override public String getMethod() { return "GET"; }
        @Override public String getPathInfo() { return null; }
        @Override public String getPathTranslated() { return null; }
        @Override public String getQueryString() { return null; }
        @Override public String getRemoteUser() { return null; }
        @Override public boolean isUserInRole(String r) { return false; }
        @Override public Principal getUserPrincipal() { return null; }
        @Override public String getRequestedSessionId() { return null; }
        @Override public String getRequestURI() { return "/axis2-json-api/openapi-mcp.json"; }
        @Override public StringBuffer getRequestURL() { return new StringBuffer(); }
        @Override public String getServletPath() { return ""; }
        @Override public HttpSession getSession(boolean c) { return null; }
        @Override public HttpSession getSession() { return null; }
        @Override public String changeSessionId() { return null; }
        @Override public boolean isRequestedSessionIdValid() { return false; }
        @Override public boolean isRequestedSessionIdFromCookie() { return false; }
        @Override public boolean isRequestedSessionIdFromURL() { return false; }
        @Override public boolean authenticate(HttpServletResponse r) { return false; }
        @Override public void login(String u, String p) { }
        @Override public void logout() { }
        @Override public Collection<Part> getParts() { return null; }
        @Override public Part getPart(String n) { return null; }
        @Override public <T extends HttpUpgradeHandler> T upgrade(Class<T> c) { return null; }
        @Override public Object getAttribute(String n) { return null; }
        @Override public Enumeration<String> getAttributeNames() { return null; }
        @Override public String getCharacterEncoding() { return null; }
        @Override public void setCharacterEncoding(String e) { }
        @Override public int getContentLength() { return 0; }
        @Override public long getContentLengthLong() { return 0; }
        @Override public String getContentType() { return null; }
        @Override public ServletInputStream getInputStream() { return null; }
        @Override public String getParameter(String n) { return null; }
        @Override public Enumeration<String> getParameterNames() { return null; }
        @Override public String[] getParameterValues(String n) { return new String[0]; }
        @Override public Map<String, String[]> getParameterMap() { return null; }
        @Override public String getProtocol() { return "HTTP/2"; }
        @Override public String getRemoteAddr() { return "127.0.0.1"; }
        @Override public String getRemoteHost() { return "localhost"; }
        @Override public void setAttribute(String n, Object o) { }
        @Override public void removeAttribute(String n) { }
        @Override public Locale getLocale() { return Locale.US; }
        @Override public Enumeration<Locale> getLocales() { return null; }
        @Override public boolean isSecure() { return true; }
        @Override public RequestDispatcher getRequestDispatcher(String p) { return null; }
        @Override public int getRemotePort() { return 0; }
        @Override public String getLocalName() { return "localhost"; }
        @Override public String getLocalAddr() { return "127.0.0.1"; }
        @Override public int getLocalPort() { return 8443; }
        @Override public ServletContext getServletContext() { return null; }
        @Override public AsyncContext startAsync() { return null; }
        @Override public AsyncContext startAsync(ServletRequest q, ServletResponse s) { return null; }
        @Override public boolean isAsyncStarted() { return false; }
        @Override public boolean isAsyncSupported() { return false; }
        @Override public AsyncContext getAsyncContext() { return null; }
        @Override public DispatcherType getDispatcherType() { return null; }
        @Override public BufferedReader getReader() { return null; }
        @Override public jakarta.servlet.ServletConnection getServletConnection() { return null; }
        @Override public String getProtocolRequestId() { return null; }
        @Override public String getRequestId() { return null; }
    }

    private static class MockHttpServletResponse implements HttpServletResponse {
        private String contentType;
        private int status = 0;
        private final StringWriter writer = new StringWriter();
        private final PrintWriter printWriter = new PrintWriter(writer);
        private final Map<String, String> headers = new HashMap<>();

        public String getWriterContent() { return writer.toString(); }

        @Override public void setContentType(String t) { this.contentType = t; }
        @Override public String getContentType()       { return contentType; }
        @Override public void setStatus(int sc)        { this.status = sc; }
        @Override public int getStatus()               { return status; }
        @Override public PrintWriter getWriter()       { return printWriter; }
        @Override public void setHeader(String n, String v) { headers.put(n, v); }
        @Override public String getHeader(String n)    { return headers.get(n); }
        @Override public Collection<String> getHeaders(String n) { return null; }
        @Override public Collection<String> getHeaderNames() { return headers.keySet(); }
        @Override public boolean containsHeader(String n) { return headers.containsKey(n); }

        @Override public void addCookie(Cookie c) { }
        @Override public String encodeURL(String u) { return u; }
        @Override public String encodeRedirectURL(String u) { return u; }
        @Override public void sendError(int sc, String m) { this.status = sc; }
        @Override public void sendError(int sc) { this.status = sc; }
        @Override public void sendRedirect(String l) { }
        @Override public void sendRedirect(String l, int sc, boolean cb) { }
        @Override public void setDateHeader(String n, long d) { }
        @Override public void addDateHeader(String n, long d) { }
        @Override public void addHeader(String n, String v) { headers.put(n, v); }
        @Override public void setIntHeader(String n, int v) { }
        @Override public void addIntHeader(String n, int v) { }
        @Override public String getCharacterEncoding() { return "UTF-8"; }
        @Override public ServletOutputStream getOutputStream() { return null; }
        @Override public void setCharacterEncoding(String c) { }
        @Override public void setContentLength(int l) { }
        @Override public void setContentLengthLong(long l) { }
        @Override public void setBufferSize(int s) { }
        @Override public int getBufferSize() { return 0; }
        @Override public void flushBuffer() { }
        @Override public void resetBuffer() { }
        @Override public boolean isCommitted() { return false; }
        @Override public void reset() { }
        @Override public void setLocale(Locale l) { }
        @Override public Locale getLocale() { return null; }
    }
}
