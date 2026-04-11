# MCP Support for Apache Axis2/Java

**Summary**: Axis2/Java gains MCP (Model Context Protocol) support in two phases. Phase A
(practical, immediate) wraps an existing Axis2 deployment with a bridge that reads
`/openapi-mcp.json` and proxies MCP `tools/call` to Axis2 over HTTPS+mTLS. Phase B (native,
novel Apache contribution) implements `axis2-transport-mcp` so Axis2 speaks MCP
directly — no wrapper. One service deployment, three protocols: JSON-RPC, REST, MCP.

MCP is JSON-RPC 2.0. The three required methods are `initialize`, `tools/list`, and
`tools/call`. Everything else (transport: stdio or HTTP/SSE, tool schema format,
capability negotiation) is specified by the MCP protocol document at
modelcontextprotocol.io.

---

## Current State (2026-04-09)

### What exists today

| Artifact | Status | Notes |
|----------|--------|-------|
| `springbootdemo-tomcat11` | ✅ Working | Spring Boot 3.x + Axis2 + Tomcat 11 + Java 25 |
| `axis2-openapi` module | ✅ Working | Serves `/openapi.json`, `/openapi.yaml`, `/swagger-ui` |
| `/openapi-mcp.json` endpoint | ✅ Done | `OpenApiSpecGenerator.generateMcpCatalogJson()` + `SwaggerUIHandler.handleMcpCatalogRequest()` |
| `axis2-mcp-bridge` stdio JAR | ✅ Done | `modules/mcp-bridge/`, produces `*-exe.jar` uber-jar |
| mTLS transport | ✅ Done | Tomcat 8443, `certificateVerification="required"`, IoT CA pattern |
| X.509 Spring Security | ✅ Done | `X509AuthenticationFilter` at `@Order(2)`, CN → `ROLE_X509_CLIENT` |
| A3 end-to-end validation | ✅ Done | `Claude Desktop → bridge → mTLS 8443 → BigDataH2Service` confirmed |
| `axis2-spring-boot-starter` | ❌ Not started | Phase 1 of modernization plan |
| A4 HTTP/SSE transport | ❌ Not started | Post-demo, deferred |
| `axis2-transport-mcp` native | ❌ Not started | Track B — novel Apache contribution |

### Reference implementations

Build, deploy, and test instructions for each container are in the sample READMEs:
- **Tomcat 11**: `modules/samples/userguide/src/userguide/springbootdemo-tomcat11/README.md`
- **WildFly 32/39**: `modules/samples/userguide/src/userguide/springbootdemo-wildfly/README.md`

```
springbootdemo-tomcat11 base URL: https://localhost:8443/axis2-json-api
  - LoginService      (auth, port 8080 only)
  - BigDataH2Service  (streaming/multiplexing demo, accessible via mTLS on 8443)

springbootdemo-wildfly base URL: https://localhost:8443/axis2-json-api
  - LoginService                (JWT auth)
  - FinancialBenchmarkService   (portfolioVariance, monteCarlo VaR, scenarioAnalysis)
  - BigDataH2Service            (HTTP/2 streaming)
  Deployed and validated on WildFly 32.0.1 (2026-04-09)
```

`BigDataH2Service` request format (confirmed working via MCP bridge):
```json
{"processBigDataSet":[{"request":{"datasetId":"test-dataset-001","datasetSize":1048576}}]}
```

---

## Security Architecture

### PKI (IoT CA Pattern)

Certificates live in `/home/robert/repos/axis-axis2-java-core/certs/`. The CA follows
the same pattern as the Kanaha camera project — RSA 4096 CA with RSA 2048 leaf certs,
appropriate for IoT/embedded where certificate management is manual.

| File | Contents | Validity |
|------|----------|---------|
| `ca.key` / `ca.crt` | Root CA, `CN=Axis2 CA, O=Apache Axis2, OU=IoT Services` | 10 years |
| `server.key` / `server.crt` | Server cert, `CN=localhost`, SAN: `DNS:localhost, IP:127.0.0.1` | 2 years |
| `server-keystore.p12` | Tomcat server keystore (server cert + key + CA chain) | — |
| `ca-truststore.p12` | Tomcat truststore (CA cert only) | — |
| `client.key` / `client.crt` | Client cert, `CN=axis2-mcp-bridge`, `extendedKeyUsage=clientAuth` | 2 years |
| `client-keystore.p12` | Bridge client keystore (client cert + key + CA chain) | — |

Keystores are also copied to `/home/robert/apache-tomcat-11.0.20/conf/`.

Password for all PKCS12 files: `changeit`

### Tomcat mTLS Connector (port 8443)

`server.xml` connector in `/home/robert/apache-tomcat-11.0.20/conf/server.xml`:

```xml
<Connector port="8443" protocol="org.apache.coyote.http11.Http11NioProtocol"
           maxThreads="150" SSLEnabled="true">
    <UpgradeProtocol className="org.apache.coyote.http2.Http2Protocol" />
    <SSLHostConfig certificateVerification="required"
                   truststoreFile="conf/ca-truststore.p12"
                   truststorePassword="changeit"
                   truststoreType="PKCS12"
                   protocols="TLSv1.2+">
        <Certificate certificateKeystoreFile="conf/server-keystore.p12"
                     certificateKeystorePassword="changeit"
                     certificateKeystoreType="PKCS12"
                     type="RSA" />
    </SSLHostConfig>
</Connector>
```

Plain HTTP port 8081 is commented out. All traffic goes through 8443.

### Spring Security Filter Chain

The filter chains in `Axis2Application.java` are ordered:

| Order | Chain | Matcher | Auth |
|-------|-------|---------|------|
| 1 | `springSecurityFilterChain` (default) | Everything | JWT |
| 2 | `springSecurityFilterChainMtls` | Port 8443 (`MtlsRequestMatcher`) | X.509 cert |
| 3 | `springSecurityFilterChainOpenApi` | `/openapi.json`, `/openapi.yaml`, `/swagger-ui`, `/openapi-mcp.json` | None |
| 4 | `springSecurityFilterChainLogin` | `/services/LoginService/**` | None |

The `@Order(2)` mTLS chain intercepts all 8443 requests before the JWT chain.
`X509AuthenticationFilter` reads `jakarta.servlet.request.X509Certificate` (set by
Tomcat after the TLS handshake), extracts the CN, and creates an
`UsernamePasswordAuthenticationToken` with `ROLE_X509_CLIENT`. The existing
`GenericAccessDecisionManager.decide()` is a no-op, so any authenticated principal
passes `FilterSecurityInterceptor`.

### X.509 Authentication Flow

```
Client presents cert → Tomcat TLS handshake (certificateVerification=required)
    → Only CA-signed certs pass
    → Tomcat writes cert chain to jakarta.servlet.request.X509Certificate attribute
    → X509AuthenticationFilter.doFilter()
    → Extract CN (e.g., "axis2-mcp-bridge")
    → SecurityContextHolder.getContext().setAuthentication(token)
    → FilterSecurityInterceptor: authenticated → passes
    → Service handler executes
```

---

## Track A — OpenAPI-Driven MCP Bridge

### A1 — `/openapi-mcp.json` endpoint ✅ Done

**Implementation**: `OpenApiSpecGenerator.generateMcpCatalogJson(HttpServletRequest)` iterates
`AxisConfiguration.getServices()` using the same `isSystemService()` / `shouldIncludeService()` /
`shouldIncludeOperation()` filters as the existing OpenAPI path generation. Output:

```json
{
  "tools": [
    {
      "name": "portfolioVariance",
      "description": "Calculate portfolio variance using O(n²) covariance matrix...",
      "inputSchema": {
        "type": "object",
        "required": ["nAssets", "weights", "covarianceMatrix"],
        "properties": {
          "nAssets":          {"type": "integer", "minimum": 2, "maximum": 2000},
          "weights":          {"type": "array", "items": {"type": "number"}},
          "covarianceMatrix": {"type": "array", "items": {"type": "array", "items": {"type": "number"}}},
          "normalizeWeights": {"type": "boolean", "default": false},
          "nPeriodsPerYear":  {"type": "integer", "default": 252}
        }
      },
      "endpoint": "POST /services/FinancialBenchmarkService/portfolioVariance"
    }
  ]
}
```

Tool schemas are populated via `mcpInputSchema` parameters in
`services.xml` — parsed by `generateMcpCatalogJson()` at runtime.

**Routing**: `OpenApiServlet.java` dispatches `uri.endsWith("/openapi-mcp.json")` to
`handler.handleMcpCatalogRequest()`. `Axis2WebAppInitializer.java` maps the path.
`Axis2Application.java` `OPENAPI_PATHS` array includes `/openapi-mcp.json` so the
OpenAPI filter chain (`@Order(3)`) handles it without auth.

### A2 — `axis2-mcp-bridge` stdio JAR ✅ Done

**Location**: `modules/mcp-bridge/`

**Key decision**: No MCP Java SDK (Apache 2.0 license constraint — SDK license
uncertain at implementation time). JSON-RPC 2.0 is implemented directly using
Jackson 2.21.1 (Apache 2.0) + Java stdlib `HttpClient`. The three-method
handshake is straightforward enough to hand-roll correctly.

**Classes**:
- `McpBridgeMain` — entry point, parses `--base-url`, `--keystore`, `--truststore` args, builds `SSLContext`, starts registry + server
- `ToolRegistry` — GETs `{baseUrl}/openapi-mcp.json` at startup, builds `List<McpTool>` and `Map<String,McpTool>`
- `McpStdioServer` — blocking stdin read loop, JSON-RPC 2.0 dispatch
- `McpTool` — data class: name, description, inputSchema (JsonNode), endpoint, path

**Build**: maven-shade-plugin 3.6.0 produces `axis2-mcp-bridge-2.0.1-SNAPSHOT-exe.jar`
(classifier: `exe`) with `MainClass=McpBridgeMain`.

**Axis2 JSON-RPC envelope**: `tools/call` wraps arguments as `{toolName: [arguments]}`
before POSTing to the Axis2 endpoint, matching the existing JSON-RPC convention.

**Notifications**: MCP `notifications/initialized` (no `id` field) is silently consumed
with no response, as required by JSON-RPC 2.0.

**Protocol version**: `"2024-11-05"`

**Claude Desktop config** (`~/.config/claude/claude_desktop_config.json`):
```json
{
  "mcpServers": {
    "axis2-demo": {
      "command": "java",
      "args": ["-jar", "/path/to/axis2-mcp-bridge-2.0.1-SNAPSHOT-exe.jar",
               "--base-url",    "https://localhost:8443/axis2-json-api",
               "--keystore",    "/home/robert/repos/axis-axis2-java-core/certs/client-keystore.p12",
               "--truststore",  "/home/robert/repos/axis-axis2-java-core/certs/ca-truststore.p12"]
    }
  }
}
```

### A3 — End-to-end validation ✅ Done

Full chain confirmed working:
```
Claude Desktop → axis2-mcp-bridge stdio → HTTPS+mTLS port 8443
    → Tomcat TLS handshake (client cert CN=axis2-mcp-bridge)
    → X509AuthenticationFilter (authenticated, ROLE_X509_CLIENT)
    → BigDataH2Service.processBigDataSet()
    → real response returned to Claude
```

Tomcat log confirmation:
```
X509AuthenticationFilter: authenticated CN=axis2-mcp-bridge on port 8443
```

### A4 — HTTP/SSE transport (deferred)

Adds persistent server mode (multiple Claude sessions sharing one bridge). Required for
production. Additive — no changes to Axis2 side or tool catalog format.

```
POST /mcp       → JSON-RPC request
GET  /mcp/sse   → SSE stream for server-initiated messages
```

---

## Track B — Native MCP Transport (`axis2-transport-mcp`)

**When**: After Track A is demonstrated. This is the Apache contribution — no other
Java framework has native MCP transport.

**Module location**: `modules/transport-mcp/`

**Interface**: Axis2's `TransportListener` + `TransportSender`.

### Protocol translation

```
MCP tools/call (JSON-RPC 2.0)
         ↓
axis2-transport-mcp
         ↓
Axis2 MessageContext (service name + operation name + payload)
         ↓
Service implementation (same Java class as JSON-RPC and REST callers)
         ↓
Axis2 MessageContext (response payload)
         ↓
axis2-transport-mcp
         ↓
MCP tools/call result (JSON-RPC 2.0)
```

### Sequencing within Track B

1. **stdio first** — simpler, no connection management, validates the
   JSON-RPC 2.0 ↔ MessageContext translation layer end-to-end
2. **HTTP/SSE second** — reuses Axis2's existing HTTP infrastructure, adds
   SSE for progress notifications on long-running service operations

### Tool schema generation

Populated from `axis2-openapi` Phase 2 output. `initialize` response includes
`capabilities.tools` derived from deployed services and their `@McpTool` annotations.

### Starter integration

```properties
axis2.transport.mcp.enabled=true
axis2.transport.mcp.transport=stdio   # or http
axis2.transport.mcp.path=/mcp         # only for http transport
```

### End state

```
Claude Desktop / AI agent  →  MCP (axis2-transport-mcp, native)
                                         ↓
REST clients               →  REST (@RestMapping, Phase 3)  →  Axis2 Service
                                         ↑                      (one Java class)
Existing JSON-RPC callers  →  JSON-RPC (unchanged)
```

---

## Key Design Decisions

**Why stdio first for both tracks**: Simplest MCP transport, zero port conflicts,
works immediately with Claude Desktop and Cursor. Validates the translation layer before
adding HTTP connection management complexity.

**Why OpenAPI as the bridge, not direct Axis2 introspection**: `/openapi-mcp.json`
decouples the bridge from Axis2 internals. The bridge works against any HTTP service
that serves this format — not just Axis2. This is useful for the Apache community
beyond the Axis2 user base.

**Why no MCP Java SDK**: Apache 2.0 license constraint. Jackson (Apache 2.0) + Java
stdlib `HttpClient` implement the three-method JSON-RPC 2.0 protocol without external
dependencies whose license compatibility is uncertain. The protocol is well-specified
enough to hand-roll correctly.

**Why IoT CA pattern**: RSA 4096 CA (10 years) + RSA 2048 leaf certs (2 years) matches
the Kanaha camera project pattern. Appropriate for environments where certificate
management is manual and infrequent. The CA is only on one machine — this is a
development/demo CA, not a production CA.

**Why `certificateVerification="required"` at Tomcat, not Spring Security**: Tomcat
enforces the TLS handshake before any HTTP processing. Invalid client certs are rejected
at the TCP layer — Spring Security never sees them. `X509AuthenticationFilter` only
needs to extract identity from an already-verified cert, not verify it.

**Why not JAX-RS instead of `@RestMapping`**: JAX-RS brings a second framework
dependency and its own lifecycle. `@RestMapping` is a thin annotation processed by
Axis2's existing REST dispatcher — no container dependency, backwards compatible,
opt-in per-operation.

---

## Next Steps

### Track A remaining

| Step | Work | Notes |
|------|------|-------|
| `mcpInputSchema` in services.xml | ✅ Done | All financial benchmark tools + login have full parameter schemas |
| A4 HTTP/SSE | Persistent bridge server mode | Required for production, additive |

### Track B

1. `modules/transport-mcp/` — new module scaffolding
2. stdio transport first (B1) — validates JSON-RPC 2.0 ↔ MessageContext translation
3. HTTP/SSE transport (B2) — reuses Axis2 HTTP infrastructure

### Testing matrix

MCP and OpenAPI support needs validation across the full container/JDK matrix:

| Container | JDK | MCP | OpenAPI | Status |
|-----------|-----|-----|---------|--------|
| WildFly 32 | OpenJDK 21 | ✅ | ✅ | Validated |
| WildFly 39 | OpenJDK 25 | ✅ | ✅ | Validated |
| Tomcat 11 | OpenJDK 21 | ✅ | ✅ | Validated |
| Tomcat 11 | OpenJDK 25 | ✅ | ✅ | Validated |

---

## Dependencies and Build

Track A (`axis2-mcp-bridge`) requires:
- `axis2-openapi` module (for `/openapi-mcp.json`)
- `com.fasterxml.jackson.core:jackson-databind:2.21.1` (Apache 2.0)
- Java 21+ (HttpClient is standard library)
- No Axis2 core dependency — bridge is a separate process

Track B (`axis2-transport-mcp`) requires:
- `axis2-core` / `axis2-kernel` (TransportListener interface)
- `axis2-openapi` (tool schema generation)
- No MCP SDK — same Jackson-only approach as A2
