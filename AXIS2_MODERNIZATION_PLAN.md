---
type: architecture
created: 2026-04-06
updated: 2026-05-04
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
  `/swagger-ui`, `/openapi-mcp.json`
- `modules/transport-h2` — HTTP/2 transport module (proof of concept, tested)
- `modules/spring-boot-starter` — functional Spring Boot autoconfiguration
- ThreadPool instance-field fix committed (`27860ddf9f`)
- Java 25 + Tomcat 11.0.20 full end-to-end test passing

---

## Completion Status

| Phase | Status | Notes |
|-------|--------|-------|
| Immediate Track (B1-B3, C3, D1-D3) | **DONE** | MCP catalog, inputSchema, authScope, streaming, resources, error hardening |
| Phase 1 — Spring Boot Starter | **DONE** | Full autoconfiguration: AxisServlet, AAR/MAR scanning, OpenAPI+MCP endpoints, SOAP/JSON mode. JWT security intentionally deferred (deployment-specific). |
| Phase A — Error Contracts | **DONE** | Axis2JsonErrorResponse, JsonRpcFaultException, 422/429/503 HTTP status codes, OpenAPI ErrorResponse schema, MCP _meta.errorContract |
| Phase B — MCP Schema Completion | **DONE** | Java type introspection for request/response schemas in both OpenAPI and MCP catalog. mcpAuthScope, mcpStreaming parameters. |
| Phase 2 — OpenAPI Annotation Bridge | PARTIAL | POJO introspection done. springdoc @Operation/@Parameter annotation processing not yet started. |
| Phase 3 — REST Transport | NOT STARTED | Dual-protocol JSON-RPC + REST from same service |
| Phase 4 — MCP Bridge | NOT STARTED | OpenAPI-driven MCP wrapper |
| Phase 5 — HTTP/2 Publication | NOT STARTED | transport-h2 graduation to supported module |
| Phase 6 — Native MCP Transport | DEFERRED | Bridge approach sufficient; prove MCP catalog adoption first |
| Phase 7 — Community | NOT STARTED | Blog post, migration guide |
| **NEW** Phase JPA — JPA/Hibernate Schema Generation | **DONE** | See below |
| **NEW** Phase PG — Offset/Limit Pagination | **DONE** | See below |
| **NEW** Phase TX — Transaction Demarcation Module | PLANNED | See below |

---

## NEW: Phase JPA — JPA/Hibernate Schema Generation (`axis2-jpa-schema`)

**Goal**: Auto-generate OpenAPI `components/schemas` from JPA-annotated entity classes
AND Hibernate XML mapping files (`.hbm.xml`). This makes Axis2 the first framework that
can serve OpenAPI schemas for any Hibernate project regardless of mapping style.

**Bang: 8/10 | Effort: days**

### Why both annotation AND hbm.xml support

The original pickup doc rejected "OpenAPI from hbm.xml" as too narrow. That was correct
for hbm.xml alone — but the real opportunity is broader:

- **JPA annotations** (`@Entity`, `@Column`, `@ManyToOne`) — the standard for new
  projects and the pattern used by typical financial applications (dozens of entities,
  Spring Data JPA repositories, Jakarta Persistence)
- **hbm.xml** — still in production at many organizations (legacy mapping
  files, Hibernate 3.0 DTD, backtick-quoted SQL Server columns). These projects won't
  rewrite to annotations just for OpenAPI support.

Supporting both from one module covers ~95% of Hibernate deployments.

### Design

Two metadata extraction strategies, one unified schema output:

```
┌──────────────────────┐     ┌──────────────────────┐
│  JPA Annotation      │     │  HBM.XML Parser      │
│  Introspector        │     │  (DOM/SAX)            │
│                      │     │                       │
│  @Entity             │     │  <class name="...">   │
│  @Column             │     │  <property>           │
│  @ManyToOne          │     │  <many-to-one>        │
│  @Id/@GeneratedValue │     │  <id>/<generator>     │
│  @Transient          │     │  <version>            │
└──────────┬───────────┘     └──────────┬────────────┘
           │                            │
           ▼                            ▼
   ┌───────────────────────────────────────┐
   │  EntitySchemaModel (unified)          │
   │  - entityName, tableName              │
   │  - fields: name, type, nullable,      │
   │    readOnly, relationship             │
   │  - idFields, versionField             │
   │  - ignoredFields (audit/system)       │
   └──────────────────┬────────────────────┘
                      │
                      ▼
   ┌───────────────────────────────────────┐
   │  OpenAPI Schema Generator             │
   │  - Read schema (all fields)           │
   │  - Write schema (excludes @Id with    │
   │    @GeneratedValue, @IgnoreChanges,   │
   │    version fields)                    │
   │  - $ref for relationships             │
   └───────────────────────────────────────┘
```

### JPA annotation mapping

| JPA Annotation | Schema Effect |
|----------------|---------------|
| `@Entity` | Top-level schema object |
| `@Table(name=...)` | Schema title / x-table-name |
| `@Column(nullable=false)` | Added to `required` array |
| `@Column(length=255)` | `maxLength: 255` |
| `@Id` | Marked in schema description |
| `@Id` + `@GeneratedValue` | `readOnly: true` in write schema |
| `@ManyToOne` / `@OneToOne` | `$ref` to related entity schema |
| `@OneToMany` / `@ManyToMany` | `type: array`, `items: {$ref: ...}` |
| `@Transient` | Excluded from schema |
| `@Version` | Excluded from write schema |
| `@Enumerated` | `type: string`, `enum: [...]` |
| `BigDecimal` (ID context) | `type: integer` |
| `Double` / `Float` | `type: number` |
| `Byte` (boolean context) | `type: boolean` |
| `Timestamp` / `Date` | `type: string, format: date-time` |

### HBM.XML mapping

| HBM Element/Attribute | Schema Effect |
|----------------------|---------------|
| `<class name="..." table="...">` | Top-level schema object |
| `<property not-null="true">` | Added to `required` array |
| `<property type="java.lang.String">` | `type: string` |
| `<property type="java.lang.Long">` | `type: integer` |
| `<property type="java.lang.Boolean">` | `type: boolean` |
| `<property type="timestamp">` | `type: string, format: date-time` |
| `<id>` + `<generator>` | `readOnly: true` in write schema |
| `<version>` | Excluded from write schema |
| `<many-to-one class="...">` | `$ref` to related entity schema |
| `<set>` / `<bag>` with `<one-to-many>` | `type: array`, `items: {$ref: ...}` |
| `<component>` | Inline object properties (flattened) |
| Column with `sql-type="nvarchar(max)"` | `type: string` (no maxLength) |

### Custom annotation support

Projects may use custom annotations that carry business semantics:

| Custom Annotation | Schema Effect |
|-------------------|---------------|
| `@IgnoreChanges` | Excluded from write schema (audit fields) |
| `@IncludeInUpdate` | Included in write schema (explicitly) |

The module accepts a configurable list of "exclude from write" annotation class names
so any project can declare their own audit-field markers.

### Integration with OpenApiSpecGenerator

Plugs into the existing `addComponents()` method. When `axis2-jpa-schema` is on the
classpath and a service's parameter or return type is an `@Entity` class (or has a
companion `.hbm.xml`), the JPA introspector generates the schema instead of the
generic POJO introspector.

### Key files

- `modules/jpa-schema/src/main/java/org/apache/axis2/jpa/schema/JpaSchemaGenerator.java`
- `modules/jpa-schema/src/main/java/org/apache/axis2/jpa/schema/EntitySchemaModel.java`
- `modules/jpa-schema/src/main/java/org/apache/axis2/jpa/schema/AnnotationIntrospector.java`
- `modules/jpa-schema/src/main/java/org/apache/axis2/jpa/schema/HbmXmlIntrospector.java`

### Dependency

None — optional module. Works standalone or with the Spring Boot starter.

---

## NEW: Phase TX — Transaction Demarcation Module (`axis2-tx`)

**Goal**: A `.mar` module that wraps service invocations in JTA transactions. Deploy the
module, set `<parameter name="transactional">true</parameter>` in services.xml, and
every operation on that service gets automatic begin/commit/rollback.

**Bang: 7/10 | Effort: days**

### How it works

Axis2 already has the plumbing:
- `Axis2UserTransaction` in `modules/kernel` wraps JTA `UserTransaction`
- `TransactionConfiguration` provides JNDI-based transaction manager lookup
- `AbstractMessageReceiver.receive()` sets `SET_ROLLBACK_ONLY` on fault
- `AbstractTemplatedHandler` provides `shouldInvoke()` / `doInvoke()` separation
- Handler `flowComplete()` is called in reverse order after processing (for cleanup)

The module injects a `TransactionHandler` into the `OperationInPhase`:

```
InFlow phases:
  Transport → Addressing → Security → PreDispatch → Dispatch
    → OperationInPhase:
        [TransactionHandler.invoke()]     ← BEGIN transaction
        [other handlers]
        [MessageReceiver.receive()]       ← service method runs
    → OperationOutPhase

OutFlow / flowComplete:
    [TransactionHandler.flowComplete()]   ← COMMIT or ROLLBACK
```

### module.xml

```xml
<module name="axis2-tx" class="org.apache.axis2.tx.TransactionModule">
    <InFlow>
        <handler name="TransactionHandler"
                 class="org.apache.axis2.tx.TransactionHandler">
            <order phase="OperationInPhase" phaseFirst="true"/>
        </handler>
    </InFlow>
</module>
```

### TransactionHandler behavior

- `shouldInvoke()`: checks service-level `transactional` parameter
- `doInvoke()`: begins JTA transaction, stores ref in MessageContext
- `flowComplete()`: if `SET_ROLLBACK_ONLY` or AxisFault occurred → rollback; else → commit

### Configuration in services.xml

```xml
<service name="PortfolioAssetService">
    <parameter name="transactional">true</parameter>
    <parameter name="transactionTimeout">30</parameter>
    <!-- operations... -->
</service>
```

### Key files

- `modules/tx/src/main/java/org/apache/axis2/tx/TransactionModule.java`
- `modules/tx/src/main/java/org/apache/axis2/tx/TransactionHandler.java`
- `modules/tx/src/META-INF/module.xml`

### Dependency

Requires JTA API on classpath (provided by WildFly, Tomcat with Atomikos/Narayana, etc.).

---

## DONE: Phase PG — Offset/Limit Pagination (`PaginatedResponse`)

**Goal**: Provide a standard pagination envelope that works with existing DAO layers
using `setFirstResult(offset)` / `setMaxResults(limit)` (the standard JPA/Hibernate
query pattern), giving frontend grids and API consumers the metadata they need to
render paging controls or implement infinite scroll.

**Bang: 7/10 | Effort: hours**

### Why offset/limit (not cursor)

Most enterprise applications paginate with integer offsets because:

1. **DAO compatibility** — existing service layers pass `firstResult`/`maxResult` to
   Hibernate's Query API. Cursor pagination requires a stable sort key and stateful
   server-side tokens — added complexity with no benefit when the query is already
   offset-based.

2. **Frontend grid compatibility** — SmartClient, AG Grid, React Table, and MUI
   DataGrid all natively speak offset/limit via `startRow`/`endRow` or
   `page`/`pageSize`. Cursor tokens require client-side adaptation.

3. **Total count is cheap** — offset-based APIs can include `totalCount` (from a
   parallel `SELECT COUNT(*)`) to enable "Showing 1–50 of 1,247" UI patterns.
   Cursor APIs typically omit totals because they are expensive for the cursor model.

4. **Virtual scrolling** — grids that load the next chunk as the user scrolls use
   `hasMore` to decide whether to fetch. This maps directly to
   `offset + limit < totalCount`.

Cursor pagination remains valuable for append-only feeds (activity logs, message
streams) where offset instability is a real problem. The framework does not preclude
adding cursor support later — it simply does not ship cursor tokens by default because
the common case does not need them.

### What NOT to build

- No cursor tokens (UUID-based cursors add complexity most deployments don't need)
- No GraphQL relay-style `edges` / `pageInfo` / `Connection` types
- No Spring Data `Pageable` / `Page<T>` integration (many projects don't use Spring Data)

### Wire format

```json
{
  "response": {
    "data": [ ... ],
    "pagination": {
      "offset": 100,
      "limit": 50,
      "totalCount": 1247,
      "hasMore": true
    }
  }
}
```

### Classes

**`PaginatedResponse<T>`** — generic response wrapper with `data` (list) and
`pagination` (metadata). Factory methods:
- `PaginatedResponse.of(items, offset, limit, totalCount)` — standard paged response
- `PaginatedResponse.unpaginated(items)` — wrap a full result set with no pagination

**`PaginationRequest`** — request-side helper with safe defaults:
- `offset` defaults to 0, negative values clamped
- `limit` defaults to 50, capped at configurable `maxLimit` (default 2000)
- Services can embed these fields in their request POJO or accept separately

### Frontend integration pattern

```typescript
// React / TypeScript — infinite scroll
const { data, pagination } = await fetchPage(offset, limit);
setItems(prev => [...prev, ...data]);
if (pagination.hasMore) {
    setNextOffset(pagination.offset + pagination.limit);
}

// React / TypeScript — page controls
const totalPages = Math.ceil(pagination.totalCount / pagination.limit);
const currentPage = Math.floor(pagination.offset / pagination.limit) + 1;
```

### Serialization compatibility

`PaginatedResponse` is a plain POJO — serializes identically across all four Axis2
JSON formatters (Gson, Moshi, EnhancedGson/H2, EnhancedMoshi/H2) with no custom
adapters. The `data` list elements serialize as their runtime type regardless of
generic type erasure.

### Key files

- `modules/json/src/org/apache/axis2/json/gson/rpc/PaginatedResponse.java`
- `modules/json/src/org/apache/axis2/json/gson/rpc/PaginationRequest.java`
- `modules/json/test/org/apache/axis2/json/gson/rpc/PaginatedResponseTest.java` (22 tests)

### Dependency

None — zero-dependency POJO pattern. Works with any JSON formatter.

---

## Immediate Track — MCP inputSchema + Axis2/C + Apache httpd Demo

**Status: DONE** — all steps completed.

### Step B1 — `mcpInputSchema` static parameter support (Java + C)

**DONE.** Both Option 1 (static declaration in services.xml) and Option 2 (auto-generated
from Java type introspection) are implemented and tested. `OpenApiSpecGenerator.generateMcpCatalogJson()`
reads `mcpInputSchema` param with Jackson validation, falls back to auto-introspection
via `generateSchemaFromServiceClass()`, then falls back to empty schema.

### Step B2 — `mcpAuthScope` per-operation parameter

**DONE.** Reads via `getMcpStringParam()`, emits `x-authScope` in tool node. Tests cover
operation-level, service-level, and absent cases.

### Step B3 — `mcpStreaming` hint

**DONE.** Boolean `mcpStreaming` parameter adds `x-streaming: true`. Absent/false
suppresses the field entirely (compact catalog).

### Step C3 — MCP Resources endpoint

**DONE.** `generateMcpResourcesJson()` returns `resources/list` response with URI,
name, description, mimeType, and metadata (wsdlUrl, operations, requiresAuth) for
each deployed service.

### Step D1 — Axis2/C MCP catalog handler

Axis2/C implementation — tracked separately in axis-axis2-c-core repo.

### Step D2 — Axis2/C correlation ID error hardening

Axis2/C implementation — tracked separately.

### Step D3 — Populate `mcpInputSchema` in all 5 financial benchmark operations

**DONE.** All three Java financial benchmark operations have explicit `mcpInputSchema`
in services.xml with full JSON Schema definitions.

---

## Phase 1 — Spring Boot Starter

**Status: DONE.**

Delivered as `modules/spring-boot-starter` with:
- `Axis2AutoConfiguration` — master switch, respects `axis2.enabled`
- `Axis2RepositoryAutoConfiguration` — stages axis2.xml (SOAP or JSON mode templates)
- `Axis2ServletAutoConfiguration` — registers AxisServlet with configurable path
- `Axis2OpenApiAutoConfiguration` — registers OpenAPI/MCP servlet endpoints
- `Axis2Properties` — externalized configuration via `application.properties`
- Built-in `axis2-soap.xml` and `axis2-json.xml` templates
- WildFly VFS compatibility
- Full test suite

**Not included (intentional):** JWT/Spring Security autoconfiguration — deployment-specific,
consuming apps provide their own `SecurityFilterChain`.

---

## Phase 2 — OpenAPI Generation (springdoc-openapi Bridge)

**Status: PARTIAL.**

**Done:**
- POJO introspection for request/response types → `components/schemas`
- Structured error response schema (`ErrorResponse`) with 422/429/503 responses
- MCP tool definitions with `inputSchema` auto-generated from Java types
- Request body and 200 response schemas linked via `$ref`

**Remaining:**
- springdoc/Swagger annotation processing (`@Operation`, `@Parameter`, `@ApiResponse`)
- OpenAPI 3.1 output (currently 3.0.1)

### Dependency
Phase 1 (starter) — **satisfied**.

---

## Phase 3 — REST Transport (Dual-Protocol Services)

**Status: NOT STARTED.**

**Goal**: Same Axis2 `@WebService` class reachable via both JSON-RPC and REST paths.

### Tasks

1. REST dispatcher activation in autoconfiguration
2. `@RestMapping` annotation for URL templates
3. HTTP method routing (GET/POST/PUT/DELETE → 405 enforcement)
4. Parallel transports — same service, no code duplication
5. OpenAPI spec reflects REST paths when `@RestMapping` present

### Dependency
Phase 1 (starter), Phase 2 (REST paths in OpenAPI spec).

---

## Phase 4 — MCP Path 1: OpenAPI-Driven MCP Wrapper

**Status: NOT STARTED.**

Thin Spring Boot app that reads `/openapi-mcp.json` and implements MCP protocol
(initialize, tools/list, tools/call). Forwards calls to Axis2 endpoints.

### Dependency
Phase 2 (requires `/openapi-mcp.json` endpoint — **satisfied**).

---

## Phase 5 — HTTP/2 Transport Publication

**Status: NOT STARTED.**

Graduate `modules/transport-h2` to supported module with formal test suite,
performance benchmarks, and starter integration.

### Dependency
Phase 1 (starter) — **satisfied**.

---

## Phase 6 — Native MCP Transport (`axis2-transport-mcp`)

**Status: DEFERRED.**

Bridge approach (Phase 4) sufficient. Prove MCP catalog adoption before investing
in native transport. Build when there's demand.

---

## Phase 7 — Community and Positioning

**Status: NOT STARTED.**

Apache blog post, migration guide, reference implementation documentation.

---

## Summary Timeline (Updated)

| Phase | Deliverable | Status |
|---|---|---|
| Immediate Track | MCP catalog, inputSchema, resources | **DONE** |
| **1** | `axis2-spring-boot-starter` | **DONE** |
| **A** | Error contracts (422/429/503) | **DONE** |
| **B** | MCP schema completion | **DONE** |
| **JPA** | JPA/Hibernate → OpenAPI schema generation | **DONE** |
| **PG** | Offset/limit pagination envelope | **DONE** |
| **TX** | Transaction demarcation .mar module | PLANNED |
| **2** | OpenAPI annotation bridge (springdoc) | PARTIAL |
| **3** | REST transport + dual-protocol | NOT STARTED |
| **4** | MCP bridge wrapper | NOT STARTED |
| **5** | HTTP/2 transport publication | NOT STARTED |
| **6** | Native MCP transport | DEFERRED |
| **7** | Community posts, migration guide | NOT STARTED |

---

## End State

A single Axis2 service deployment, configured once, serves:

```
Claude Desktop / AI agent  →  MCP (bridge Phase 4, or native Phase 6)
                                         ↓
Data API / React frontend  →  REST (Phase 3)    ──►  Axis2 Service
                                         ↑           (one implementation)
Existing JSON-RPC callers  →  JSON-RPC (unchanged)
```

With:
- OpenAPI spec and MCP tool definitions auto-generated (Phase 2 + JPA)
- JPA entity schemas for any Hibernate project (Phase JPA)
- Automatic transaction management (Phase TX)
- Spring Boot starter (Phase 1) — single Maven dependency
- Structured error contracts with proper HTTP status codes (Phase A)
