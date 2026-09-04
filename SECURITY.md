# Security Threat Model — Apache Axis2/Java

## Project Description

Apache Axis2/Java is a SOAP and REST web services engine. It provides a
message-processing pipeline with pluggable transports (HTTP/HTTPS, JMS, TCP,
local), a module system for cross-cutting concerns (WS-Security via Rampart,
WS-Addressing), hot-deployment of service archives (AAR/MAR), and
multi-protocol serving (JSON-RPC, REST/OpenAPI, MCP) from a single service
deployment. It runs inside a Jakarta Servlet container (Tomcat, Jetty,
WildFly) or embedded via Spring Boot.

## Roles and Trust Levels

| Role | Trust Level | Description |
|------|-------------|-------------|
| **Server Administrator** | Fully trusted | Configures `axis2.xml`, deploys/removes modules and services, controls the servlet container. Has filesystem and JVM-level access. |
| **Service Deployer** | Trusted | Drops AAR archives into `WEB-INF/services/`. May be the same person as the administrator, or a CI pipeline. |
| **Module Developer** | Trusted | Builds and deploys MAR module archives that add handlers to the processing pipeline. Code executes with full JVM privileges. |
| **Authenticated Client** | Partially trusted | A remote caller whose identity has been verified by the servlet container, a servlet filter, or a WS-Security module (Rampart). Authorized actions depend on the application. |
| **Anonymous Client** | Untrusted | A remote caller with no credentials. Can reach any endpoint exposed by the HTTP transport. All input is hostile. |

## Security Boundaries

### What IS a security issue

- **Remote Code Execution (RCE)** via the Axis2 framework itself — not
  through user-written service logic.
- **XML External Entity (XXE) injection** — the framework's XML parsers
  resolve external entities or allow DOCTYPE declarations from untrusted
  input, enabling file read, SSRF, or denial of service.
- **Server-Side Request Forgery (SSRF)** — an attacker causes the server
  to make requests to arbitrary internal or external hosts through framework
  features such as WSDL/XSD import resolution, endpoint references, or
  transport senders.
- **Deserialization of untrusted data** — framework-level Java object
  deserialization that can be triggered by remote input without a class
  whitelist. This was historically the most severe class of vulnerability
  in Axis2 (see CVE history below).
- **Denial of Service via parser abuse** — billion-laughs XML bombs, deeply
  nested JSON, or other input that causes unbounded memory or CPU
  consumption inside framework-level parsers.
- **Authentication or authorization bypass** — a flaw in the handler/phase
  pipeline that allows a message to skip an engaged security module.
- **Information disclosure of server internals** — stack traces, class
  names, or configuration details leaked to unauthenticated callers through
  fault messages or metadata endpoints when exposure has been disabled.
- **Path traversal** — a crafted service name, URI component, or WSDL
  import location that allows reading or writing files outside expected
  directories.
- **Multipart/file upload abuse** — unbounded file counts or sizes via
  multipart form data that exhaust server resources.

### What is NOT a security issue

- **Vulnerabilities in user-written services.** SQL injection, broken
  access control, or insecure business logic in a deployed service are the
  service author's responsibility.
- **Missing authentication on endpoints.** Axis2 does not ship built-in
  authentication. Securing endpoints is the responsibility of the servlet
  container, servlet filters, or engaged security modules (Rampart).
- **Hot-deployment with weak filesystem permissions.** If an attacker has
  write access to `WEB-INF/services/` or `WEB-INF/modules/`, they can
  deploy arbitrary code. This is an OS/container configuration issue.
- **Service enumeration via `/services/` listing.** When
  `exposeServiceMetadata` is `true` (the default), service names are
  visible. This is documented behavior controllable via `axis2.xml`.
- **Denial of service at the network level.** SYN floods, slowloris, or
  transport-layer attacks are mitigated by the servlet container, not Axis2.
- **Vulnerabilities *within* optional, external modules.** For example,
  a flaw in Rampart's cryptographic implementation would be handled by
  the Rampart project. However, a flaw in Axis2's handler pipeline that
  allows the Rampart module to be bypassed *is* a vulnerability in Axis2.

## Architecture and Attack Surface

### Message Processing Pipeline

```
Remote Client (untrusted input)
    |
    v
Servlet Container (TLS termination, optional authentication)
    |
    v
AxisServlet / HTTPWorker              <-- HTTP entry point
    |
    v
MessageContext created                <-- request metadata captured
    |
    v
Transport-In Phase                    <-- transport-level handlers
    |
    v
Dispatchers                           <-- route to service/operation
  (URI, SOAPAction, WS-Addressing, JSON method, HTTP location)
    |
    v
Handler Phases                        <-- global + per-service handlers
  (security modules like Rampart execute here)
    |
    v
Message Builder                       <-- deserialize body
  (SOAP, XML, JSON, MTOM, multipart/form-data)
    |
    v
MessageReceiver                       <-- invoke service method
    |
    v
Response Phases + Transport-Out       <-- serialize response, send
```

### URL Patterns

Axis2 exposes the following URL patterns from the servlet mapping:

| Pattern | Purpose | Trust Requirement |
|---------|---------|-------------------|
| `/services/{ServiceName}` | JSON-RPC and SOAP service invocation | Application-defined |
| `/services/{ServiceName}/{Operation}` | REST-style per-operation invocation | Application-defined |
| `/services/{ServiceName}?wsdl` | WSDL metadata retrieval | Anonymous (if `exposeServiceMetadata=true`) |
| `/services/{ServiceName}?xsd` | XML Schema retrieval | Anonymous (if `exposeServiceMetadata=true`) |
| `/services/` | Service listing | Anonymous (if `exposeServiceMetadata=true`) |
| `/services/{ServiceName}/{name}.xsd` or `.wsdl` | Packaged metadata by file name | Anonymous (if `exposeServiceMetadata=true`) |
| `/openapi.json` | OpenAPI 3.0 schema (if OpenAPI module engaged) | Anonymous; per-service `exposeServiceMetadata` respected |
| `/swagger-ui` | Swagger UI (if OpenAPI module engaged) | Anonymous; per-service `exposeServiceMetadata` respected |
| `/openapi-mcp.json` | MCP tool catalog (if OpenAPI module engaged) | Anonymous; per-service `exposeServiceMetadata` respected |

### Attack Surface by Component

| Component | Threats | Mitigations |
|-----------|---------|-------------|
| **XML parsers** (AXIOM/StAX, DocumentBuilderFactory) | XXE, billion laughs, entity expansion DoS | DOCTYPE disallowed; external entities disabled; `DefaultEntityResolver` returns empty source |
| **WSDL/XSD import resolution** (wsdl4j, xmlschema-core) | XXE in imported documents; SSRF via `file://`/`gopher://` schemes | `SecureWSDLLocator` pre-validates the client-side path with a hardened SAX parser (HTTP/HTTPS only, 10MB default, connect/read timeouts, relative-path SSRF bypass blocked); `HardenedWSDLLocator` screens the file, archive, classpath and catalog paths, refusing a DOCTYPE without restricting where a document may be loaded from |
| **JSON parser** (Gson) | Deep nesting stack exhaustion, large payload DoS | Fuzz-tested (1.7M+ iterations); Gson nesting limits |
| **JSON-RPC dispatch** | Method name injection; unexpected operation invocation | Method names validated against deployed operations; unknown methods return fault |
| **Multipart/file upload** (commons-fileupload2) | Unbounded file count DoS (CVE-2023-24998 pattern); unbounded body size; temp-file accumulation | commons-fileupload2 enforces the file count limit; `multipartMaxRequestSize` / `multipartMaxFileSize` bound the body; temp files are deleted immediately for form fields and tracked to collection for file parts |
| **Form-urlencoded builder** | Unbounded body read into an in-memory map | `formUrlEncodedMaxRequestSize` bounds the read; the stream fails rather than truncating |
| **Service dispatchers** | Routing to unintended service; header spoofing; a service chosen from message content after the Security phase has already run | Dispatchers validate service existence and unknown services return fault; selecting the service from the SOAP body namespace is off unless `allowContentBasedServiceDispatch` is set, so binding happens before the Security phase runs |
| **Hot-deployment** (DeploymentEngine) | Malicious AAR/MAR deploys arbitrary code | Trust boundary is filesystem access; no signature verification (admin operation) |
| **Context externalization** (SafeObjectInputStream) | Java deserialization gadget chains | No class allowlist is possible: the feature exists to carry application objects (self-managed data, `Parameter` values). Streams Axis2 creates refuse dynamic proxies and honour `org.apache.axis2.context.externalize.serialFilter`; no Axis2 path feeds them from the network, and an integrator who persists or replicates contexts must restrict the stream they own |
| **Metadata endpoints** (`?wsdl`, `?xsd`, `/services/`, `.xsd`/`.wsdl` by name, OpenAPI/MCP) | Service enumeration, schema disclosure | `exposeServiceMetadata` enforced uniformly across the servlet and standalone HTTP paths and the OpenAPI/MCP generators |
| **WS-Addressing response endpoints** (`wsa:ReplyTo`, `wsa:FaultTo`) | SSRF: an inbound header names the destination of a server-initiated send | Non-anonymous response endpoints refused by default (`allowNonAnonymousResponseEndpoints`); when enabled, scheme restricted to HTTPS, destination screened at both the header-parsing and transport-selection layers, and redirects not followed |
| **OpenAPI / Swagger UI surface** | Reflected XSS from request-controlled values; Host reflected into published URLs | Host validated, values encoded for their output context, CSP with a per-response script nonce; the published `servers[].url` is relative unless `openapi.serverBaseUrl` pins it |
| **MTOM/attachment handling** | Large attachment DoS, temp file exhaustion | Streaming processing; `TempFileManager` cleanup |
| **`?fields=` query parameter** (field selection, if enabled) | Reflection-based field filtering on response objects | Field names validated against declared response type; no dynamic class loading |

### Transports

| Transport | Security Notes |
|-----------|----------------|
| HTTP/HTTPS | TLS handled by servlet container. No framework-level auth. Primary production transport. |
| Local (in-JVM) | No network exposure. JVM-level isolation only. |
| JMS | Authentication delegated to JMS broker. |
| TCP | Raw sockets. No encryption or authentication. Trusted networks only. |
| UDP | No encryption, no authentication, no reliability. Trusted networks only. |
| Mail | Depends on mail server authentication. |

## CVE History and Lessons Learned

Axis2's CVE history concentrates in three categories. The scan should
weight these areas accordingly.

### 1. Deserialization of Untrusted Data (most severe)

**Clustering module (removed):**
A previous version of Axis2 included a clustering module for multi-node
coordination using Apache Tribes. This module exposed a network listener
that deserialized Java objects from untrusted network streams without
validation, enabling Remote Code Execution (RCE) via standard
deserialization gadget chains. This affected all releases through 2.0.0
on Apache Tomcat, but only when the Tribes-based clustering feature was
manually enabled (it was off by default). Assigned
[CVE-2026-66713](https://www.cve.org/CVERecord?id=CVE-2026-66713) and
resolved by complete removal of the clustering module in 40+ files
([AXIS2-6097](https://issues.apache.org/jira/browse/AXIS2-6097)) in
release 2.0.1.

**Lesson:** Any `ObjectInputStream.readObject()` on network input is a
critical-severity finding. The remaining use of Java serialization in
Axis2 is `SafeObjectInputStream` for context externalization. It has no
class allowlist and cannot have one: the data it carries includes
application objects by design. Nothing in Axis2 feeds it from the
network -- see item 6 below for what it does enforce, and for what an
integrator who persists contexts has to do themselves.

### 2. XML Parsing (XXE/SSRF)

**CVE-2010-1632:** DTD/XXE in MTOMBuilder flow. Fixed by strictly
forbidding DOCTYPE declarations in SOAP and XML requests.

**WSDL import parsing:** wsdl4j 1.6.3 creates its own
`DocumentBuilderFactory` without XXE hardening. Axis2 mitigates this
with `SecureWSDLLocator` (pre-validates imported documents with a
hardened SAX parser, rejects DOCTYPE, protocol-whitelists to HTTP/HTTPS,
enforces size limits and timeouts) and hardened URI resolvers for
xmlschema-core imports.

**Lesson:** Third-party XML libraries (wsdl4j, xmlschema-core) create
their own parser factories that bypass framework-level hardening. Every
XML parsing path — including transitive ones through dependency
libraries — must be audited.

### 3. Dependency Vulnerabilities

| CVE | Dependency | Issue | Resolution |
|-----|-----------|-------|------------|
| CVE-2010-3981 | Admin console | CSRF/XSS | Fixed in 1.7.3 |
| CVE-2012-6153, CVE-2014-3577 | Apache HttpClient | Various | Updated dependency in 1.7.4 |
| CVE-2016-1000031 | commons-fileupload | RCE via DiskFileItem | Updated dependency in 1.7.6 |
| CVE-2023-24998 | commons-fileupload 1.x | Unbounded file count DoS | Migrated to commons-fileupload2 in 2.0.0 |

**Lesson:** Dependency-level CVEs are the most frequent class. The
migration from `commons-fileupload` 1.x to `commons-fileupload2` in
2.0.0 was specifically driven by CVE-2023-24998.

## Existing Security Hardening

1. **XML parsing:** All `DocumentBuilderFactory` and `SAXParserFactory`
   instances created by the framework disable DTDs and external entities
   (`XMLUtils.java`, `SecureWSDLLocator.java`, `DefaultEntityResolver.java`).

2. **WSDL import security (extended in 2.0.2):** wsdl4j parses with its own
   unhardened parser and fetches the whole import chain itself, so every
   document has to be screened before it reaches wsdl4j.

   - `SecureWSDLLocator` screens the client-side path
     (`createClientSideAxisService`): protocol-restricted to HTTP/HTTPS,
     size-limited, timeout-protected, relative-path SSRF bypass patched.
   - `HardenedWSDLLocator` screens the paths that load a WSDL from a file, an
     archive, the classpath or a catalog, where an HTTP-only fetcher could not
     be used: JAX-WS WSDL loading, the runtime reload wrapper, the deployment
     builder's resolver path, and the codegen entry point. It refuses a DOCTYPE
     in the document or in anything it imports, bounds size and applies
     timeouts, but does not decide where a document comes from -- a delegate
     locator keeps its own resolution behaviour, and a bare relative path still
     loads, as wsdl4j accepts. Screening must not narrow what can be loaded, or
     it breaks ordinary deployments rather than attacks.
   - Not screened: `WSDL11ToAxisServiceBuilder.readInTheWSDLFile` when no
     resolver is supplied parses the top document with the hardened
     `XMLUtils.newDocument`, but wsdl4j fetches any `wsdl:import` itself. The
     deployment callers do supply a resolver; a caller that does not, with a
     remote base URI, is outside what is screened.

3. **Schema import security:** URI resolvers for AAR and WAR deployments
   block HTTP/HTTPS/FTP/JAR/file scheme resolution to prevent SSRF via
   xmlschema-core's `DefaultURIResolver`.

4. **Deserialization of externalized contexts (2.0.2):** There is no class
   allowlist, and one cannot be shipped: context externalization exists to
   carry application objects -- `SelfManagedDataHolder` holds whatever a
   service put there, and a `Parameter` value may be any serializable
   object -- so a list of Axis2's own context classes would refuse the data
   the feature is for. Earlier revisions of this document claimed such an
   allowlist; it never existed. What holds without knowing the
   application's types:

   - The streams Axis2 creates refuse **dynamic proxies**. Axis2 never
     writes one, and a proxy over an attacker-chosen invocation handler is
     the entry point of the classic gadget chains. Override with
     `org.apache.axis2.context.externalize.allowProxies=true`.
   - `org.apache.axis2.context.externalize.serialFilter` takes a JEP 290
     pattern applied to those streams only, for an integrator who does know
     their own types. An unparseable pattern, or one defining no filter, is
     an error rather than a silent no-op. The JVM-wide `jdk.serialFilter`
     applies as well.
   - Not covered: where the writer chose object form, `SafeObjectInputStream`
     calls `readObject()` on the `ObjectInput` its caller supplied. That
     stream belongs to the caller and has already read objects by then, so no
     filter can be installed on it. Restricting it is the caller's to do.

   No Axis2 code path feeds these streams from the network. The risk arrives
   when an integrator persists or replicates contexts, and deserializing
   attacker-influenced bytes stays dangerous whatever is configured here.

5. **Clustering removed:** The entire clustering module (Tribes-based
   inter-node communication with unvalidated deserialization) has been
   removed from the codebase.

6. **File upload limits:** Migration to commons-fileupload2 enforces
   file count limits, preventing CVE-2023-24998-style DoS.

7. **Fault detail suppression:** `sendStacktraceDetailsWithFaults`
   defaults to `false`.

8. **Fuzz testing:** Jazzer-based fuzzers cover XML, JSON, HTTP header,
   and URL parsers. 45M+ iterations with zero crashes or security
   findings. See `src/site/xdoc/docs/OSS-FUZZ.md`. Axis2/C has an
   active OSS-Fuzz integration.

9. **WS-Addressing response endpoints (2.0.2):** A non-anonymous
   `wsa:ReplyTo` or `wsa:FaultTo` makes the server open a connection to an
   address the caller chose. Unless WS-Security is engaged to bind that
   endpoint reference to a trusted issuer, the WS-Addressing specification
   leaves it to the receiver to decide whether to honour it, so Axis2 now
   declines by default. `allowNonAnonymousResponseEndpoints` is `false`;
   replies and faults travel back down the inbound connection only. Apache
   CXF made the same choice in
   `org.apache.cxf.ws.addressing.decoupled.enabled`.

   Deployments that genuinely use decoupled responses — the separate-listener
   "Dual" clients, or a third-party callback endpoint — set it to `true`, and
   should also set `httpFrontendHostUrl` so the generated reply address is the
   real external URL rather than the local one. With the feature enabled:

   - `allowedResponseEndpointSchemes` permits HTTPS only. Widen it to name a
     transport actually used for replies.
   - The destination is screened both where the inbound header is parsed and
     where a server-side response acquires its transport, so the check cannot
     be reached around by setting the endpoint reference another way.
   - Link-local, wildcard and multicast destinations are always refused.
     `blockPrivateNetworkResponseEndpoints` additionally refuses loopback and
     private ranges; it is off by default because a callback inside the same
     private network is how most decoupled deployments are wired.
   - The address checks are address-family agnostic: a bracketed IPv6 literal
     is classified, not treated as an unrecognised host, and the IPv4-mapped
     form (`[::ffff:169.254.169.254]`) is refused as the address it reaches
     rather than as a separate spelling. IPv6 unique-local (`fc00::/7`) is
     covered explicitly, since `InetAddress.isSiteLocalAddress` answers only
     for the deprecated `fec0::/10`.
   - Redirects are not followed, so a reply endpoint cannot hand the sender a
     destination the policy already refused.
   - Name resolution is bounded (`responseEndpointResolveTimeoutMillis`) and
     runs on a capped pool, so a slow resolver cannot tie up request threads.

   Known limitation: the destination is resolved once to check it and again to
   connect, so a hostile DNS server could answer differently the second time.
   Closing that requires connecting to a pinned address, which the transport
   does not currently support. Operators in cloud environments should pair
   these settings with network egress controls.

10. **Request body ceilings (2.0.2):** The message builders read the transport
    stream directly, so a servlet container's post-size limit never sees the
    body. `multipartMaxRequestSize` and `multipartMaxFileSize` (100 MB),
    `formUrlEncodedMaxRequestSize` (2 MB), `mtomMaxRequestSize` (100 MB, the
    whole `multipart/related` body, so MTOM and SwA) and `soapMaxRequestSize`
    (100 MB, a plain SOAP or POX body) bound them; `-1` restores the previous
    unbounded behaviour, and any may be set per service.

    Every builder that reads the stream has to be bounded, not just the ones
    whose limits were reported: the caller picks which builder runs by choosing
    the Content-Type, so a ceiling on the form builders alone is avoided by
    sending `multipart/related` instead.
    Multipart temp files are now deleted rather than accumulating: form-field
    parts as soon as their text is read, file parts once the item backing the
    `DataHandler` is unreachable.

    Both ceilings are enforced against bytes actually read, not against a
    declared `Content-Length`, so a chunked request body is bounded on the same
    terms as a declared one. This is worth stating because the reverse is the
    easy mistake: a limit that screens the header before the read is no limit
    at all for `Transfer-Encoding: chunked`, which declares no length. The
    form-urlencoded builder wraps the stream in `BoundedInputStream`; the
    multipart path relies on commons-fileupload2, which pairs its
    `Content-Length` fast path with a streaming guard.

11. **OpenAPI and Swagger UI output (2.0.2):** Request-controlled values are
    validated and encoded for the context they are written into, the served
    page carries a Content-Security-Policy with a per-response script nonce,
    and the published `servers[].url` is relative — resolved by the client
    against wherever it fetched the document — rather than derived from the
    request Host. `openapi.serverBaseUrl` pins an absolute URL where a
    deployment needs one.

12. **Uniform metadata exposure (2.0.2):** `exposeServiceMetadata` is now
    honoured by every anonymous metadata route: the `?wsdl`, `?wsdl2` and
    `?xsd` queries as before, plus the `.xsd`/`.wsdl` file routes on both the
    servlet and standalone HTTP paths, the named-WSDL route, the
    OpenAPI/Swagger/MCP generators, the `/services/` listing on both render
    paths, WS-MEX `GetMetadata`, and the ping module's service-level ping. A
    service with exposure disabled is skipped rather than refused, so it stays
    indistinguishable from one that is not deployed.

    The gate is `AxisService.isMetadataExposed()`. It lives in the kernel
    because modules answer anonymous metadata requests too and cannot reach a
    transport's private copy of the check -- which is how the listing, WS-MEX
    and ping came to be exempt from a control the query routes enforced.

    Separately, the `?xsd=` route reaches a service's packaged META-INF with
    the request's value, and that directory holds `services.xml`, whose
    parameters name keystores and password-callback classes. Only schema and
    WSDL documents are servable, enforced inside the shared stream helper so
    every caller inherits it rather than repeating it.

    A hidden service is answered exactly as an undeployed one, on every route,
    body included: the query routes no longer send 403 where an absent service
    gets 404, which was an existence oracle. There is no `403` left in either
    HTTP path's metadata handling. RFC 9110 section 15.5.4 sanctions answering
    404 to conceal a forbidden resource's existence, which is what this does.

13. **Content-based service dispatch (2.0.2):** The inflow phase order is
    Transport, Addressing, Security, PreDispatch, Dispatch, and `DispatchPhase`
    installs only the phases that follow it, so a service bound during Dispatch
    is bound after the Security phase has run against no service and Security is
    never revisited. Selecting a service from the namespace of the SOAP body's
    first element therefore let a caller whose request URI named no service reach
    a service whose engaged security modules had not run for it.
    `allowContentBasedServiceDispatch` defaults to `false`. Dispatch by request
    URI, SOAPAction and WS-Addressing binds the service before the Security phase
    and is unaffected.

## Reporting Security Issues

Report vulnerabilities to: **security@apache.org**

Follow the [Apache Security Policy](https://www.apache.org/security/).
All confirmed issues go through coordinated disclosure with CVE assignment.
