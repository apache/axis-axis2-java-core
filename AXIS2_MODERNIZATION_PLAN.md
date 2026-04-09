---
type: architecture
created: 2026-04-06
status: Active
---

# Axis2/Java Modernization Plan

**BLUF**: Axis2 becomes a multi-protocol service platform — one service implementation
serving JSON-RPC (existing callers), REST (Data API consumers), and MCP (AI agents)
simultaneously. The Spring Boot starter removes adoption tax. OpenAPI generation makes
every Axis2 service AI-discoverable. A native MCP transport eliminates the wrapper layer
entirely. No other Java framework can do all three from the same service deployment.

**Foundation already in place**:
- `springbootdemo-tomcat11` — working reference implementation (Spring Boot 3.x + Axis2 +
  Tomcat 11 + Java 25)
- `axis2-openapi` module — OpenAPI spec served at `/openapi.json`, `/openapi.yaml`,
  `/swagger-ui`
- `modules/transport-h2` — HTTP/2 transport module (proof of concept, tested)
- ThreadPool instance-field fix committed (`27860ddf9f`)
- Java 25 + Tomcat 11.0.20 full end-to-end test passing

---

## Immediate Track — MCP inputSchema + Axis2/C + Apache httpd Demo

**Goal**: Complete the MCP catalog to production quality, port the catalog handler to
Axis2/C, and run a live demo on Apache httpd with mod_axis2. This track runs ahead of
Phases 1–6 because it validates the MCP story end-to-end on real hardware.

### Step B1 — `mcpInputSchema` static parameter support (Java + C)

**Problem**: Every tool in `/openapi-mcp.json` emits `"inputSchema": {}`. Claude has to
guess parameters. This kills usability for financial benchmark tools with 6+ fields.

**Approach (dual strategy)**:

1. **Option 1 — Static declaration in services.xml** (ships first, zero risk):
   Each `<operation>` carries a `mcpInputSchema` parameter whose value is a literal
   JSON Schema string. `OpenApiSpecGenerator.generateMcpCatalogJson()` reads it with
   `getMcpStringParam()` and embeds it verbatim, parsing with Jackson to validate.
   Falls back to `{}` on parse failure with a WARN log.

   ```xml
   <operation name="portfolioVariance">
     <parameter name="mcpInputSchema">{
       "type": "object",
       "required": ["n_assets", "weights", "covariance_matrix"],
       "properties": {
         "n_assets":          {"type": "integer", "minimum": 2, "maximum": 2000},
         "weights":           {"type": "array",   "items": {"type": "number"}},
         "covariance_matrix": {"type": "array",   "items": {"type": "number"}},
         "request_id":        {"type": "string"}
       }
     }</parameter>
   </operation>
   ```

2. **Option 3 — Build-time code generation from C headers** (ships second):
   A Python script (`tools/gen_mcp_schema.py`) reads Axis2/C service header files,
   maps C struct fields to JSON Schema types, and writes `mcpInputSchema` parameters
   directly back into `services.xml`. The C type mapping table:

   | C type | JSON Schema type |
   |--------|-----------------|
   | `int`, `long`, `axis2_int32_t` | `"integer"` |
   | `double`, `float` | `"number"` |
   | `axis2_char_t *`, `char *` | `"string"` |
   | `axis2_bool_t` | `"boolean"` |
   | pointer-to-struct | `"object"` |
   | array pointer + count field | `"array"` |

   The script detects `_request_t` structs, infers which fields are required vs
   optional (required = no default value set in initialiser), and outputs a
   standards-compliant JSON Schema. Services.xml is updated in-place.

   Run: `python3 tools/gen_mcp_schema.py --header financial_benchmark_service.h \
         --services services.xml`

**Java implementation**: `OpenApiSpecGenerator.generateMcpCatalogJson()` — check
`mcpInputSchema` param before falling back to empty schema. Single method change.

**Tests**: `McpCatalogGeneratorTest` — add tests for schema embedding, invalid JSON
graceful fallback, and missing param fallback.

### Step B2 — `mcpAuthScope` per-operation parameter

Operation-level auth scope string embedded in catalog for MCP clients that support
scope-based auth (e.g. `"mcpAuthScope": "read:portfolio"`). Reads via
`getMcpStringParam()`. Omitted from tool node when absent.

### Step B3 — `mcpStreaming` hint

Boolean `mcpStreaming` parameter marks operations that can stream chunked responses
(e.g. large Monte Carlo results). Adds `"x-streaming": true` to the tool node.
Reads via `getMcpBoolParam()`.

### Step C3 — MCP Resources endpoint

New servlet path `GET /mcp-resources` returns a JSON array of `resource://` URIs:

```json
{
  "resources": [
    {"uri": "resource://axis2/openapi",      "name": "OpenAPI Spec",  "mimeType": "application/json"},
    {"uri": "resource://axis2/field-catalog", "name": "Field Catalog", "mimeType": "application/json"}
  ]
}
```

Individual resource content served at `GET /mcp-resource?uri=resource://axis2/openapi`.
Wired in `OpenApiServlet` as a new path case.

---

### Step D1 — Axis2/C MCP catalog handler

New file: `modules/mcp/mcp_catalog_handler.c`

Walks `axis2_conf_t` service map at request time — same traversal as Java's
`axisConfig.getServices()`. Emits the identical JSON catalog format. Key functions:

```c
// Entry point registered on GET /_mcp/openapi-mcp.json
axis2_status_t mcp_catalog_handler_invoke(
    axis2_handler_t *handler,
    const axutil_env_t *env,
    struct axis2_msg_ctx *msg_ctx);

// Reads axis2_op_t parameter, falls back to axis2_svc_t parameter
static const axis2_char_t *get_mcp_param(
    axis2_op_t *op, axis2_svc_t *svc,
    const axutil_env_t *env,
    const axis2_char_t *param_name,
    const axis2_char_t *default_val);
```

Parameter reading uses `axis2_op_get_param()` / `axis2_svc_get_param()` — the same
two-level lookup as Java. `mcpDescription`, `mcpReadOnly`, `mcpDestructive`,
`mcpIdempotent`, `mcpInputSchema` all supported.

JSON output built with `json_object_new_object()` (json-c) — no string concatenation.

### Step D2 — Axis2/C correlation ID error hardening

New helper: `axis2_json_secure_fault.c`

```c
axis2_char_t *axis2_json_make_secure_fault_message(
    const axutil_env_t *env,
    int is_parse_error);
// Returns "Bad Request [errorRef=<uuid>]" or "Internal Server Error [errorRef=<uuid>]"
// UUID generated from /dev/urandom (16 bytes → hex with hyphens)
// Full context logged to axutil_log before sanitized message returned
```

Applied to `financial_benchmark_service_handler.c` JSON parse error paths and any
`axis2_json_rpc_msg_recv` equivalent in Axis2/C.

### Step D3 — Populate `mcpInputSchema` in all 5 financial benchmark operations

Using Option 1 (hand-authored) immediately; Option 3 code-gen script validates against
it. The 5 operations:

| Operation | Required fields |
|-----------|----------------|
| `portfolioVariance` | `n_assets`, `weights`, `covariance_matrix` |
| `monteCarlo` | `n_simulations`, `n_periods`, `initial_value`, `expected_return`, `volatility` |
| `scenarioAnalysis` | `n_assets`, `assets` |
| `generateTestData` | `n_assets` |
| `metadata` | *(none — GET operation)* |

### Step E — Apache httpd deployment

1. Build `mod_axis2.so` from `axis-axis2-c-core` targeting the host's Apache httpd
2. `httpd.conf` fragment:
   ```apache
   LoadModule axis2_module modules/mod_axis2.so
   Axis2RepoPath /opt/axis2c/repository
   <Location /axis2>
       SetHandler axis2_module
   </Location>
   ```
3. Deploy `FinancialBenchmarkService` to repository
4. Verify:
   ```bash
   curl https://localhost/axis2/_mcp/openapi-mcp.json
   curl -X POST https://localhost/axis2/services/FinancialBenchmarkService/monteCarlo \
        -H 'Content-Type: application/json' \
        -d '{"monteCarlo":[{"arg0":{"n_simulations":10000,"n_periods":252,...}}]}'
   ```
5. Demo: MCP-aware client resolves tools from catalog, calls financial operations

### Immediate Sprint Sequence

```
B1 (Java) → B1 tests → B2/B3 (Java, config-only) → C3 (Java, new servlet path)
     ↓
D1 (Axis2/C catalog handler) → D2 (error hardening) → D3 (services.xml schemas)
     ↓
Option 3 code-gen script (tools/gen_mcp_schema.py)
     ↓
E (Penguin deployment + demo)
```

---

## Phase 1 — Spring Boot Starter

**Goal**: Reduce Axis2 + Spring Boot integration from a multi-day configuration project
to a single Maven dependency.

**Problem today**: The `springbootdemo-tomcat11` project is a hand-rolled integration
requiring `maven-antrun-plugin` WAR pre-staging, manual `.mar` module file deployment,
explicit `Axis2WebAppInitializer`, and custom security filter chain wiring. Every new
Axis2+Spring Boot project repeats this work and gets it slightly wrong.

### Tasks

1. **Create `axis2-spring-boot-starter` module** in `modules/`
   - Spring Boot autoconfiguration class (`Axis2AutoConfiguration`)
   - Auto-registers `AxisServlet` at configurable path (default `/services/*`)
   - Classpath scanning for `.aar` and `.mar` modules — no manual staging
   - Externalized configuration via `application.properties`:
     ```properties
     axis2.services-path=/services
     axis2.repository-path=classpath:axis2-repository
     axis2.rest.enabled=true
     axis2.openapi.enabled=true
     ```

2. **Spring Security autoconfiguration**
   - Default `SecurityFilterChain` bean wired to Axis2 service paths
   - `RequestAndResponseValidatorFilter` registered automatically
   - Overridable — consuming apps provide their own `SecurityFilterChain` bean to replace

3. **Logging bridge autoconfiguration**
   - Log4j2 → SLF4J bridge wired without manual configuration
   - `log4j2-spring.xml` loaded automatically from classpath

4. **WAR and embedded container support**
   - Works both as embedded (Spring Boot `main()`) and as WAR deployed to Tomcat/WildFly
   - `SpringBootServletInitializer` extension handled by the starter

5. **Starter test suite**
   - Integration test: Spring Boot app with starter dependency, single `@WebService`,
     confirms service reachable at `/services/{name}`
   - Test matrix: Java 21, Java 25 × embedded Tomcat, external Tomcat 11, WildFly 32

### Deliverable
`axis2-spring-boot-starter-2.x.x.jar` — add to `pom.xml`, Axis2 works. Zero XML
configuration required for the common case.

### Dependency
None — builds directly on `springbootdemo-tomcat11` as the reference implementation.

---

## Phase 2 — OpenAPI Generation (springdoc-openapi Bridge)

**Goal**: Every Axis2 service automatically produces an OpenAPI 3.1 spec from Java
annotations. MCP tool definitions are generated from that spec at no additional cost.

**Problem today**: `axis2-openapi` generates a spec from Axis2service descriptors
(`services.xml`), not from Java type annotations. The spec is structural but not
semantically rich — no operation descriptions, no parameter constraints, no response
schemas beyond what Axis2 infers. springdoc-openapi generates far richer specs from
`@Operation`, `@Parameter`, `@ApiResponse` annotations on the Java class itself.

### Tasks

1. **Annotation support on Axis2 `@WebService` classes**
   - Axis2 services annotated with springdoc/Swagger annotations:
     ```java
     @WebService
     public class AssetCalculationsService {

         @Operation(summary = "Get portfolio calculations",
                    description = "Returns PWR, OPS, Kelly weight for all assets in a fund")
         @ApiResponse(responseCode = "200", content = @Content(schema =
                      @Schema(implementation = AssetCalculationsResponse.class)))
         public AssetCalculationsResponse doGetAssetCalculations(
             @Parameter(description = "Fund ID") AssetCalculationsRequest request) { ... }
     }
     ```
   - Annotations processed by `axis2-openapi` during spec generation
   - Falls back to structural inference when annotations absent (backward compatible)

2. **Java type → JSON Schema generation**
   - Request/response POJOs introspected to produce `components/schemas` in the spec
   - Uses Jackson's `JsonSchemaGenerator` or springdoc's `ModelConverter` pipeline
   - Handles: nested objects, arrays, enums, `BigDecimal` as `string` with `format: decimal`

3. **OpenAPI 3.1 output**
   - Upgrade `axis2-openapi` output from OpenAPI 3.0 to OpenAPI 3.1
   - 3.1 required for full JSON Schema compatibility (needed for MCP tool `inputSchema`)

4. **MCP tool definition export endpoint**
   - `GET /openapi-mcp.json` — returns MCP-formatted tool definitions derived from the
     OpenAPI spec:
     ```json
     {
       "tools": [
         {
           "name": "doGetAssetCalculations",
           "description": "Get portfolio calculations — PWR, OPS, Kelly weight",
           "inputSchema": {
             "type": "object",
             "properties": {
               "fundID": { "type": "integer" },
               "departmentID": { "type": "integer" }
             },
             "required": ["fundID", "departmentID"]
           }
         }
       ]
     }
     ```
   - Served by `OpenApiServlet` alongside existing `/openapi.json` and `/swagger-ui`

5. **Starter integration**
   - `axis2.openapi.enabled=true` in starter autoconfiguration activates both
     `/openapi.json` and `/openapi-mcp.json` endpoints automatically

### Deliverable
Any Axis2 service annotated with standard OpenAPI annotations produces a spec and MCP
tool definitions served at known endpoints. An MCP wrapper (Phase 4) can point at
`/openapi-mcp.json` and expose every Axis2 service as an AI tool with zero additional
code.

### Dependency
Phase 1 (starter) — OpenAPI endpoints auto-registered via starter autoconfiguration.

---

## Phase 3 — REST Transport (Dual-Protocol Services)

**Goal**: The same Axis2 `@WebService` class is reachable via both its existing
JSON-RPC path and a new REST path, with proper HTTP method semantics and
resource-oriented URLs.

**Problem today**: Existing Axis2 service URLs (`POST /services/calculationService/
doCalculationJob`) are JSON-RPC over HTTP. New consumers (REST APIs, React
frontends, MCP agents) expect `GET /api/v1/resources/{id}/calculations`. Axis2 has REST
dispatch capability in `axis2.xml` but it has never been activated or documented for
modern Spring Boot deployments.

### Tasks

1. **REST dispatcher activation and configuration**
   - Enable Axis2 REST dispatcher in autoconfiguration:
     ```properties
     axis2.rest.enabled=true
     axis2.rest.base-path=/api/v1
     ```
   - REST dispatcher maps `GET /api/v1/funds/{id}/calculations` →
     `AssetCalculationsService.doGetAssetCalculations(fundId)`

2. **URL template annotation**
   - New `@RestMapping` annotation (or reuse JAX-RS `@GET`/`@Path` if feasible):
     ```java
     @RestMapping(method = "GET", path = "/funds/{fundId}/calculations")
     public AssetCalculationsResponse doGetAssetCalculations(
         @PathParam("fundId") long fundId,
         @QueryParam("fields") String fields) { ... }
     ```
   - Axis2 REST dispatcher resolves path variables and query parameters from the
     URL before invoking the service operation

3. **HTTP method routing**
   - GET → read operations (no side effects)
   - POST → create operations
   - PUT/PATCH → update operations
   - DELETE → delete operations
   - Method constraint enforced by REST dispatcher (405 Method Not Allowed if violated)

4. **Parallel transports — same service, no duplication**
   - JSON-RPC path unchanged: `POST /services/AssetCalculationsService/
     doGetAssetCalculations`
   - REST path added: `GET /api/v1/funds/{id}/calculations`
   - Both routes to the same Java method — no code duplication
   - Handler chain (security, logging, validation) applies to both

5. **OpenAPI spec reflects REST paths**
   - Phase 2 spec generator emits REST paths (not JSON-RPC paths) when `@RestMapping`
     present
   - Both paths optionally included with `x-axis2-jsonrpc-path` extension field for
     tooling that needs the RPC form

6. **Integration tests**
   - Confirm same service reachable at both paths
   - Confirm handler chain (authentication, validation) applies identically to both
   - Test matrix includes Java 21, 25 and Tomcat 11 + embedded Tomcat

### Deliverable
Existing Axis2 services add `@RestMapping` annotations and are immediately available
as REST endpoints alongside their JSON-RPC paths. Existing JSON-RPC services can be
exposed to REST and MCP consumers without rewriting or duplicating service logic.

### Dependency
Phase 1 (starter registers both dispatchers), Phase 2 (REST paths appear in OpenAPI
spec and MCP tool definitions).

---

## Phase 4 — MCP Path 1: OpenAPI-Driven MCP Wrapper

**Goal**: Package a lightweight MCP server that reads an Axis2 service's OpenAPI spec
and exposes every operation as an MCP tool. AI agents (Claude, etc.) can call Axis2
services via MCP with no MCP-specific code in the service itself.

**This is the practical MCP path available immediately after Phase 2.**

### Tasks

1. **`axis2-mcp-bridge` module**
   - Thin Spring Boot app (or embeddable library) that:
     - Reads `/openapi-mcp.json` from a configured Axis2 deployment
     - Implements MCP `initialize` handshake, reporting tool capabilities
     - Forwards MCP `tools/call` requests to the corresponding Axis2 REST endpoint
       (Phase 3) or JSON-RPC endpoint (fallback if Phase 3 not deployed)
     - Returns MCP-formatted responses

2. **Transport: HTTP + SSE**
   - MCP HTTP transport: POST to `/mcp` for client→server messages
   - SSE endpoint at `/mcp/events` for server→client streaming
   - Spring's `SseEmitter` for SSE — standard Spring MVC, no Axis2 involvement

3. **Configuration**
   - `axis2.mcp.target-url=http://localhost:8080/axis2-json-api` points bridge at the
     Axis2 deployment
   - Bridge refreshes tool definitions on startup and on `/openapi-mcp.json` change

4. **Starter integration**
   - `axis2.mcp.bridge.enabled=true` in starter spins up the bridge in the same JVM
     as the Axis2 deployment — no separate process needed for simple deployments

5. **Reference implementation**
   - Extend `springbootdemo-tomcat11` with MCP bridge enabled
   - Document: Claude Desktop config pointing at the bridge, example tool call flow

### Deliverable
`axis2-mcp-bridge` — configure, deploy, and every Axis2 service is callable from Claude
Desktop, Claude API tool use, or any MCP-compatible AI agent. No MCP code in service
classes.

### Dependency
Phase 2 (requires `/openapi-mcp.json` endpoint). Phase 3 (REST paths preferred as MCP
call targets, but JSON-RPC fallback works without Phase 3).

---

## Phase 5 — HTTP/2 Transport Publication

**Goal**: `modules/transport-h2` becomes a supported, tested, documented module with
a published performance benchmark.

**Foundation**: The module exists and was tested end-to-end with Java 25 + Tomcat 11.0.20
in `springbootdemo-tomcat11` (`BigDataH2Service` — confirmed working with `datasetId` +
`datasetSize` request fields).

### Tasks

1. **Formal integration test suite for `transport-h2`**
   - Tests against: embedded Tomcat (Spring Boot), external Tomcat 10, Tomcat 11,
     WildFly 32
   - Java versions: 21, 25
   - Large payload test: confirm HTTP/2 multiplexing benefit over HTTP/1.1 at 1MB+
     payloads

2. **Performance benchmark**
   - Baseline: HTTP/1.1 transport, sequential requests, 1MB / 10MB / 50MB payloads
   - Compare: HTTP/2 transport, concurrent requests (multiplexing)
   - Document results in module README

3. **Starter integration**
   - `axis2.transport.h2.enabled=true` activates HTTP/2 transport in the starter
   - Auto-detects servlet container HTTP/2 support (Tomcat 10+, WildFly 32+)

4. **Module graduation**
   - Move from `modules/transport-h2` proof-of-concept to a released artifact at the
     same version as the core modules
   - Javadoc, usage example, known limitations documented

### Deliverable
`axis2-transport-h2-2.x.x.jar` as a supported module. HTTP/2 is a documented, tested
deployment option for Axis2 services with large payloads.

### Dependency
Phase 1 (starter exposes the transport config property). No dependency on Phases 2-4.

---

## Phase 6 — Native MCP Transport (`axis2-transport-mcp`)

**Goal**: Axis2 speaks MCP natively. An MCP client (Claude Desktop, Claude API, any
MCP-compatible agent) connects directly to Axis2 with no intermediate wrapper. One
service deployment, three protocols: JSON-RPC, REST, MCP.

**This is the novel Apache project contribution — no other Java framework has this.**

### Tasks

1. **`axis2-transport-mcp` module**
   - Implements Axis2 `TransportListener` and `TransportSender` interfaces
   - Translates MCP JSON-RPC 2.0 messages ↔ Axis2 `MessageContext`
   - MCP `tools/call` → Axis2 service operation invocation
   - Axis2 response → MCP `tools/call` result

2. **MCP initialize handshake**
   - Axis2 responds to MCP `initialize` with:
     - `serverInfo.name`: service deployment name from `axis2.xml`
     - `capabilities.tools`: populated from deployed services
   - Tool list derived from Phase 2 OpenAPI/MCP tool definitions

3. **Transport: stdio (ships first)**
   - stdio transport is simpler than HTTP/SSE — no persistent connection management
   - Axis2 can be launched as a subprocess; Claude Desktop communicates via stdin/stdout
   - Validates the JSON-RPC 2.0 translation layer before adding HTTP complexity

4. **Transport: HTTP + SSE (ships second)**
   - MCP HTTP transport layer on top of Axis2's existing HTTP infrastructure
   - SSE for server-initiated messages (progress notifications for long-running calcs)
   - Reuses Axis2's HTTP transport configuration (port, TLS, thread pool)

5. **Tool schema generation**
   - Uses Phase 2 JSON Schema generation to populate `inputSchema` for each tool
   - Tool descriptions from `@Operation` annotations (Phase 2)

6. **Starter integration**
   - `axis2.transport.mcp.enabled=true`
   - `axis2.transport.mcp.transport=stdio|http`
   - HTTP transport: `axis2.transport.mcp.path=/mcp`

7. **Integration test**
   - MCP client (test harness, not full Claude) sends `initialize` + `tools/list` +
     `tools/call` sequence
   - Verifies correct JSON-RPC 2.0 framing, correct tool invocation, correct response

### Deliverable
`axis2-transport-mcp-2.x.x.jar` — configure, and Axis2 becomes a native MCP server.
Phase 4 wrapper becomes optional (useful for external deployments; native transport
preferred for co-located services).

### Dependency
Phase 2 (tool schema generation), Phase 1 (starter wires the transport). Phase 3 (REST)
independent — MCP transport calls service operations directly, REST paths not required.

---

## Phase 7 — Community and Positioning

**Goal**: The Apache community and downstream projects know these capabilities exist,
understand the multi-protocol positioning, and have concrete migration guides.

### Tasks

1. **Apache blog post: "Axis2 as a Multi-Protocol Service Platform"**
   - Covers: dual-protocol JSON-RPC + REST from one service, OpenAPI generation,
     MCP transport, Spring Boot starter
   - Uses the calculation orchestration deployment (anonymized DPTv2) as the case study:
     sub-200ms portfolio calculations, CDC cache, Node.js bridge
   - Positions Axis2's handler chain as the differentiator for production orchestration
     workloads

2. **ThreadPool fix release note**
   - Explicit note in 2.x changelog: `ThreadPool.shutDown` was a static field; changed
     to instance field. Affects any deployment running multiple Axis2 engine instances
     in the same JVM. Multi-pool deployments should upgrade.

3. **`springbootdemo-tomcat11` as canonical reference**
   - The existing module becomes the official Spring Boot + Axis2 + Tomcat 11
     reference implementation in the distribution
   - README updated to reflect starter usage once Phase 1 ships

4. **Migration guide: JSON-RPC → Dual-Protocol**
   - Step-by-step: add `@RestMapping` annotations, enable REST dispatcher, verify
     both paths, update OpenAPI spec, expose MCP tools
   - Targets teams running Axis2 JSON-RPC who want REST and MCP without rewriting

---

## Summary Timeline

| Phase | Deliverable | Key Dependency |
|---|---|---|
| **1** | `axis2-spring-boot-starter` | springbootdemo-tomcat11 reference |
| **2** | OpenAPI annotation bridge + MCP tool export | Phase 1 |
| **3** | REST transport + dual-protocol services | Phase 1, Phase 2 |
| **4** | `axis2-mcp-bridge` (OpenAPI-driven MCP wrapper) | Phase 2 |
| **5** | `transport-h2` published module + benchmarks | Phase 1 |
| **6** | `axis2-transport-mcp` native MCP transport | Phase 2, Phase 1 |
| **7** | Community posts, migration guide, reference impl | All phases |

Phases 1 and 2 are the critical path — everything else depends on them. Phases 3, 4,
and 5 can proceed in parallel after Phase 2. Phase 6 requires Phase 2 but is otherwise
independent of Phases 3, 4, and 5.

---

## End State

A single Axis2 service deployment, configured once, serves:

```
Claude Desktop / AI agent  →  MCP (native transport, Phase 6)
                                         ↓
Data API / React frontend  →  REST (Phase 3)    ──►  Axis2 Service
                                         ↑           (one implementation)
Existing JSON-RPC callers  →  JSON-RPC (unchanged)
```

With OpenAPI spec and MCP tool definitions auto-generated (Phase 2) and a Spring Boot
starter (Phase 1) that makes the whole stack a single Maven dependency.
