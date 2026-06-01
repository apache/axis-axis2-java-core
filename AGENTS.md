# AGENTS.md — Apache Axis2/Java

## Security Threat Model

See [SECURITY.md](SECURITY.md) for the full threat model, including:

- Project description and architecture
- Roles and trust levels (server admin, service deployer, authenticated/anonymous client)
- Security boundaries: what is and is not a security issue
- Attack surface by component (XML parsers, WSDL imports, JSON, deserialization, file uploads, hot-deployment)
- CVE history and lessons learned (deserialization, XXE/SSRF, dependency CVEs)
- Existing hardening measures

## High-Priority Scan Areas

Based on Axis2's CVE history, the following areas deserve the most scrutiny:

### 1. Deserialization (historically most severe)

The clustering module was removed due to unvalidated `ObjectInputStream`
deserialization on network input. Scan for any remaining paths where
`ObjectInputStream.readObject()` processes data reachable from untrusted
input. The known remaining use is `SafeObjectInputStream` (whitelist-based)
in context externalization — verify the whitelist is complete and not
bypassable.

Key files:
- `modules/kernel/src/org/apache/axis2/context/externalize/SafeObjectInputStream.java`

### 2. XML Parsing (XXE/SSRF via third-party libraries)

wsdl4j and xmlschema-core create their own XML parser factories without
XXE hardening. Axis2 wraps these with `SecureWSDLLocator` and hardened
URI resolvers. Scan for any XML parsing path — especially through
transitive dependencies — that bypasses this wrapping.

Key files:
- `modules/kernel/src/org/apache/axis2/util/SecureWSDLLocator.java`
- `modules/kernel/src/org/apache/axis2/util/XMLUtils.java`
- `modules/kernel/src/org/apache/axis2/util/DefaultEntityResolver.java`
- `modules/kernel/src/org/apache/axis2/deployment/resolver/AARFileBasedURIResolver.java`
- `modules/kernel/src/org/apache/axis2/deployment/resolver/WarFileBasedURIResolver.java`
- `modules/kernel/src/org/apache/axis2/deployment/resolver/AARBasedWSDLLocator.java`
- `modules/kernel/src/org/apache/axis2/deployment/resolver/WarBasedWSDLLocator.java`

### 3. JSON Processing

JSON-RPC is the primary protocol for production deployments. Scan the
JSON message builder and dispatcher for:
- Deep nesting / stack exhaustion (CVE-2024-57699 pattern)
- Method name injection in JSON-RPC dispatch
- Type confusion in JSON-to-Java object mapping
- Large payload resource exhaustion

Key files:
- `modules/kernel/src/org/apache/axis2/json/` (JSON builders and formatters)
- `modules/kernel/src/org/apache/axis2/dispatchers/` (JSON-based dispatcher)

### 4. Multipart/File Upload

Migrated from commons-fileupload 1.x to commons-fileupload2 for
CVE-2023-24998. Verify the migration is complete and no legacy code
paths remain.

Key files:
- `modules/kernel/src/org/apache/axis2/builder/MultipartFormDataBuilder.java`
- `modules/webapp/src/main/java/org/apache/axis2/webapp/AdminActions.java`

### 5. HTTP Transport Entry Points

The HTTP transport is the primary attack surface. Scan for header
injection, request smuggling, and URI parsing issues.

Key files:
- `modules/transport/http/src/main/java/org/apache/axis2/transport/http/AxisServlet.java`
- `modules/transport/http/src/main/java/org/apache/axis2/transport/http/HTTPWorker.java`

## Project Structure

```
modules/
  kernel/       Core engine: message pipeline, handlers, deployment,
                XML parsing, dispatchers, context, JSON processing
  transport/    Pluggable transports: HTTP, local, JMS, TCP, UDP, mail
  webapp/       Admin console WAR
  addressing/   WS-Addressing module
  openapi/      OpenAPI + Swagger UI + MCP catalog generation
  fuzz/         Jazzer fuzz targets (XML, JSON, HTTP headers, URLs)
  samples/      Sample services including Spring Boot deployments
systests/       Integration tests
```

## Testing and Fuzzing

Fuzz targets exist in `modules/fuzz/` covering XML, JSON, HTTP header,
and URL parsers. See `src/site/xdoc/docs/OSS-FUZZ.md` for details.
Axis2/C has an active Google OSS-Fuzz integration.

## Reporting

Security vulnerabilities: **security@apache.org**
