---
type: architecture
created: 2026-04-06
status: Active — pre-implementation
---

# MCP Support for Apache Axis2/Java

**BLUF**: Axis2/Java gains MCP (Model Context Protocol) support in two phases. Phase A
(practical, immediate) wraps an existing Axis2 deployment with a bridge that reads
`/openapi-mcp.json` and proxies MCP `tools/call` to Axis2 over HTTP. Phase B (native,
novel Apache contribution) implements `axis2-transport-mcp` so Axis2 speaks MCP
directly — no wrapper. One service deployment, three protocols: JSON-RPC, REST, MCP.

MCP is JSON-RPC 2.0. The three required methods are `initialize`, `tools/list`, and
`tools/call`. Everything else (transport: stdio or HTTP/SSE, tool schema format,
capability negotiation) is specified by the MCP protocol document at
modelcontextprotocol.io.

---

## Current State (2026-04-06)

### What exists today

| Artifact | Status | Notes |
|----------|--------|-------|
| `springbootdemo-tomcat11` | ✅ Working | Spring Boot 3.x + Axis2 + Tomcat 11 + Java 25, end-to-end tested |
| `axis2-openapi` module | ✅ Working | Serves `/openapi.json`, `/openapi.yaml`, `/swagger-ui` |
| `modules/transport-h2` | ✅ Tested | HTTP/2 transport PoC, `BigDataH2Service` confirmed working |
| ThreadPool instance-field fix | ✅ Committed | `27860ddf9f` — static→instance, fixes multi-pool JVM isolation |
| `axis2-spring-boot-starter` | ❌ Not started | Phase 1 of modernization plan |
| `/openapi-mcp.json` endpoint | ❌ Not started | Requires work in `axis2-openapi` module |
| `axis2-mcp-bridge` | ❌ Not started | Thin bridge JAR, reads MCP catalog, proxies to Axis2 |
| `axis2-transport-mcp` | ❌ Not started | Native MCP transport — novel Apache contribution |

### Reference implementation

```
springbootdemo-tomcat11 base URL: http://localhost:8080/axis2-json-api
Services deployed:
  - LoginService      (auth)
  - BigDataH2Service  (streaming/multiplexing demo)
```

`BigDataH2Service` request format (confirmed working):
```json
{"processBigDataSet":[{"request":{"datasetId":"test-dataset-001","datasetSize":1048576}}]}
```

---

## Track A — OpenAPI-Driven MCP Bridge

**When**: Immediate next work session. No dependency on native transport.

**Why first**: The team already knows MCP. This gives them a working demo calling
existing Axis2 services through Claude/Cursor without writing MCP-specific service code.

### A1 — `/openapi-mcp.json` endpoint in `axis2-openapi`

**File**: `modules/openapi/src/main/java/org/apache/axis2/openapi/`

Add `McpToolCatalogServlet` alongside the existing `OpenApiServlet`. Endpoint returns:

```json
{
  "tools": [
    {
      "name": "processBigDataSet",
      "description": "Process a large dataset using HTTP/2 multiplexing",
      "inputSchema": {
        "type": "object",
        "properties": {
          "datasetId": { "type": "string" },
          "datasetSize": { "type": "integer" }
        },
        "required": ["datasetId", "datasetSize"]
      },
      "endpoint": "POST /services/BigDataH2Service/processBigDataSet"
    }
  ]
}
```

Implementation strategy:
- Inject `ConfigurationContext` via `getServletContext().getAttribute(AxisServlet.CONFIGURATION_CONTEXT)`
- Iterate `ctx.getAxisConfiguration().getServices()` → each `AxisService`
- Iterate `service.getOperations()` → each `AxisOperation`
- Use existing `axis2-openapi` reflection infrastructure for schema extraction
- Add optional `@McpTool(description = "...")` annotation for richer descriptions

**Estimated effort**: 2–3 days.

### A2 — `axis2-mcp-bridge` stdio server

**Location**: New module `modules/mcp-bridge/` or standalone JAR.

Startup (Claude Desktop `~/.config/claude/claude_desktop_config.json`):
```json
{
  "mcpServers": {
    "axis2-demo": {
      "command": "java",
      "args": ["-jar", "/path/to/axis2-mcp-bridge.jar",
               "--base-url", "http://localhost:8080/axis2-json-api"]
    }
  }
}
```

Core loop:
1. Fetch `{base-url}/openapi-mcp.json` at startup → build tool registry
2. Read JSON-RPC 2.0 lines from `System.in`
3. `initialize` → return server info + tool capabilities
4. `tools/list` → return registry
5. `tools/call` → `HttpClient.send(POST to Axis2 endpoint)` → return result
6. Write JSON-RPC 2.0 response to `System.out`

Use the official MCP Java SDK to handle `initialize` capability negotiation rather than
hand-rolling it. Community Java port: `io.modelcontextprotocol:sdk` (check Maven Central
for latest coordinates).

**Do not** hand-roll JSON-RPC framing — the initialize/capability handshake has version
negotiation subtleties that the SDK handles correctly.

**Estimated effort**: 3–4 days.

### A3 — Team validation against `springbootdemo-tomcat11`

Quick smoke-test script:
```bash
#!/bin/bash
# Initialize
echo '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2024-11-05","capabilities":{},"clientInfo":{"name":"test","version":"1.0"}}}' \
  | java -jar axis2-mcp-bridge.jar --base-url http://localhost:8080/axis2-json-api

# List tools
echo '{"jsonrpc":"2.0","id":2,"method":"tools/list","params":{}}' \
  | java -jar axis2-mcp-bridge.jar --base-url http://localhost:8080/axis2-json-api

# Call a tool
echo '{"jsonrpc":"2.0","id":3,"method":"tools/call","params":{"name":"processBigDataSet","arguments":{"datasetId":"mcp-test-001","datasetSize":1048576}}}' \
  | java -jar axis2-mcp-bridge.jar --base-url http://localhost:8080/axis2-json-api
```

**Estimated effort**: 1–2 days.

### A4 — HTTP/SSE transport (post-demo, deferred)

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

**Why not JAX-RS instead of `@RestMapping`**: JAX-RS brings a second framework
dependency and its own lifecycle. `@RestMapping` is a thin annotation processed by
Axis2's existing REST dispatcher — no container dependency, backwards compatible,
opt-in per-operation.

**Why not Spring AI or LangChain4j as the MCP layer**: Those are application-layer
frameworks. The goal is transport-level integration so Axis2 services are callable from
*any* MCP client, not just Spring-based applications. `axis2-transport-mcp` is at the
right abstraction level.

---

## Dependencies and Build

Track A (`axis2-mcp-bridge`) requires:
- `axis2-openapi` module (for `/openapi-mcp.json`)
- MCP Java SDK (check coordinates at time of implementation)
- Java 21+ (HttpClient is standard library)
- No Axis2 core dependency — bridge is a separate process

Track B (`axis2-transport-mcp`) requires:
- `axis2-core` / `axis2-kernel` (TransportListener interface)
- `axis2-openapi` (tool schema generation)
- MCP Java SDK (JSON-RPC 2.0 framing)

Both tracks require `axis2-spring-boot-starter` (Phase 1) to wire transport config
via `application.properties`.

---

## Next Immediate Actions

1. **Start A1**: Add `McpToolCatalogServlet` to `modules/openapi/`. Start with
   structural introspection (no annotation support yet) — get `/openapi-mcp.json`
   serving a valid tool list from `springbootdemo-tomcat11`.

2. **Check MCP Java SDK coordinates**: Verify artifact ID and version on Maven Central
   before starting A2. The protocol is at `modelcontextprotocol.io/specification`.

3. **Penguin demo (Track C, parallel)**: The Axis2/C financial benchmark service is
   committed and compiles clean. When Penguin demo prep begins, the service is ready —
   just needs `build_financial_service.sh` run on the Penguin host after Axis2/C install.
