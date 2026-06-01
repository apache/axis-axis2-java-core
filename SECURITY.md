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
| `/openapi.json` | OpenAPI 3.0 schema (if OpenAPI module engaged) | Anonymous |
| `/swagger-ui` | Swagger UI (if OpenAPI module engaged) | Anonymous |
| `/openapi-mcp.json` | MCP tool catalog (if OpenAPI module engaged) | Anonymous |

### Attack Surface by Component

| Component | Threats | Mitigations |
|-----------|---------|-------------|
| **XML parsers** (AXIOM/StAX, DocumentBuilderFactory) | XXE, billion laughs, entity expansion DoS | DOCTYPE disallowed; external entities disabled; `DefaultEntityResolver` returns empty source |
| **WSDL/XSD import resolution** (wsdl4j, xmlschema-core) | XXE in imported documents; SSRF via `file://`/`gopher://` schemes | `SecureWSDLLocator` pre-validates with hardened SAX parser; protocol whitelist (HTTP/HTTPS only); size limit (10MB default); connect/read timeouts; relative-path SSRF bypass blocked |
| **JSON parser** (Gson) | Deep nesting stack exhaustion, large payload DoS | Fuzz-tested (1.7M+ iterations); Gson nesting limits |
| **JSON-RPC dispatch** | Method name injection; unexpected operation invocation | Method names validated against deployed operations; unknown methods return fault |
| **Multipart/file upload** (commons-fileupload2) | Unbounded file count DoS (CVE-2023-24998 pattern) | Migrated from commons-fileupload 1.x to commons-fileupload2 which enforces file count limits |
| **Service dispatchers** | Routing to unintended service; header spoofing | Dispatchers validate service existence; unknown services return fault |
| **Hot-deployment** (DeploymentEngine) | Malicious AAR/MAR deploys arbitrary code | Trust boundary is filesystem access; no signature verification (admin operation) |
| **Context externalization** (SafeObjectInputStream) | Java deserialization gadget chains | Whitelist-based `SafeObjectInputStream`; restricted to known Axis2 context classes |
| **Metadata endpoints** (`?wsdl`, `?xsd`, `/services/`) | Service enumeration, schema disclosure | Controllable via `exposeServiceMetadata` parameter |
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
deserialization gadget chains. Resolved by complete removal of the
clustering module in 40+ files
([AXIS2-6097](https://issues.apache.org/jira/browse/AXIS2-6097)).

**Lesson:** Any `ObjectInputStream.readObject()` on network input is a
critical-severity finding. The remaining use of Java serialization in
Axis2 is `SafeObjectInputStream` for context externalization, which uses
a class whitelist.

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

2. **WSDL import security:** `SecureWSDLLocator` pre-parses imported
   documents before passing them to wsdl4j. Protocol-restricted to
   HTTP/HTTPS. Size-limited. Timeout-protected. Relative-path SSRF
   bypass patched.

3. **Schema import security:** URI resolvers for AAR and WAR deployments
   block HTTP/HTTPS/FTP/JAR/file scheme resolution to prevent SSRF via
   xmlschema-core's `DefaultURIResolver`.

4. **Deserialization whitelist:** `SafeObjectInputStream` restricts Java
   object deserialization to known Axis2 context classes.

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

## Reporting Security Issues

Report vulnerabilities to: **security@apache.org**

Follow the [Apache Security Policy](https://www.apache.org/security/).
All confirmed issues go through coordinated disclosure with CVE assignment.
