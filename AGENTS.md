# AGENTS.md — Apache Axis2/Java

## Security Threat Model

See [SECURITY.md](SECURITY.md) for the full threat model, including:

- Project description and architecture
- Roles and trust levels (server admin, service deployer, authenticated/anonymous client)
- Security boundaries: what is and is not a security issue
- Attack surface by component (JSON-RPC, REST/OpenAPI, XML/WSDL, deserialization, file uploads)
- CVE history and lessons learned (deserialization, XXE/SSRF, dependency CVEs)
- Existing hardening measures

## High-Priority Scan Areas

Modern Axis2/Java deployments serve **JSON-RPC over HTTP/HTTPS** as
the primary protocol, with REST/OpenAPI and MCP as additional
interfaces. The scan should weight accordingly: JSON processing,
JSON-RPC dispatch, and the OpenAPI/MCP modules are the primary attack
surface. SOAP/XML and WSDL processing are secondary.

### 1. JSON-RPC Processing (primary production protocol)

JSON-RPC is the primary protocol for production deployments. The JSON
module provides two serialization backends (Gson, Moshi) with enhanced
HTTP/2 variants, JSON-RPC message receivers that dispatch method calls
to service operations, field filtering, pagination, streaming
formatters, and structured error responses. Scan for:
- Method name injection in JSON-RPC dispatch
- Deep nesting / stack exhaustion (CVE-2024-57699 pattern)
- Type confusion in JSON-to-Java object mapping
- Large payload resource exhaustion
- Reflection-based field filtering bypass (`FieldFilteringMessageFormatter`)

Key files:
- `modules/json/src/org/apache/axis2/json/gson/rpc/JsonRpcMessageReceiver.java` (Gson JSON-RPC dispatch)
- `modules/json/src/org/apache/axis2/json/moshi/rpc/JsonRpcMessageReceiver.java` (Moshi JSON-RPC dispatch)
- `modules/json/src/org/apache/axis2/json/gson/rpc/JsonUtils.java` (Gson JSON-RPC utilities)
- `modules/json/src/org/apache/axis2/json/moshi/rpc/JsonUtils.java` (Moshi JSON-RPC utilities)
- `modules/json/src/org/apache/axis2/json/streaming/FieldFilteringMessageFormatter.java` (field selection)
- `modules/json/src/org/apache/axis2/json/rpc/JsonRpcFaultException.java` (error responses)
- `modules/json/src/org/apache/axis2/json/gsonh2/` (enhanced Gson for HTTP/2)
- `modules/json/src/org/apache/axis2/json/moshih2/` (enhanced Moshi for HTTP/2)
- `modules/kernel/src/org/apache/axis2/dispatchers/JSONBasedDefaultDispatcher.java`

### 2. OpenAPI and MCP Modules (new attack surface)

The OpenAPI module auto-generates API schemas and Swagger UI from
deployed services. The MCP module generates tool catalogs for AI
agents. Both expose service metadata and accept configuration that
could be manipulated. Scan for:
- Information disclosure via schema generation (internal class names, field types)
- XSS in Swagger UI handler
- Input validation in MCP tool invocation bridge
- SSRF via MCP stdio bridge connecting to Axis2 endpoints

Key files:
- `modules/openapi/src/main/java/org/apache/axis2/openapi/OpenApiSpecGenerator.java`
- `modules/openapi/src/main/java/org/apache/axis2/openapi/SwaggerUIHandler.java`
- `modules/openapi/src/main/java/org/apache/axis2/openapi/OpenApiModule.java`
- `modules/mcp-bridge/src/main/java/org/apache/axis2/mcp/bridge/McpStdioServer.java`
- `modules/mcp-bridge/src/main/java/org/apache/axis2/mcp/bridge/ToolRegistry.java`

### 3. Deserialization (historically most severe)

The clustering module was removed due to unvalidated `ObjectInputStream`
deserialization on network input. Scan for any remaining paths where
`ObjectInputStream.readObject()` processes data reachable from untrusted
input. The known remaining use is `SafeObjectInputStream` (whitelist-based)
in context externalization — verify the whitelist is complete and not
bypassable.

Key files:
- `modules/kernel/src/org/apache/axis2/context/externalize/SafeObjectInputStream.java`

### 4. HTTP Transport Entry Points

The HTTP transport is the network entry point for all protocols
(JSON-RPC, REST, SOAP). Scan for header injection, request smuggling,
and URI parsing issues.

Key files:
- `modules/transport/http/src/main/java/org/apache/axis2/transport/http/AxisServlet.java`
- `modules/transport/http/src/main/java/org/apache/axis2/transport/http/HTTPWorker.java`

### 5. XML Parsing (XXE/SSRF via third-party libraries, secondary)

wsdl4j and xmlschema-core create their own XML parser factories without
XXE hardening. Axis2 wraps these with `SecureWSDLLocator` and hardened
URI resolvers. Scan for any XML parsing path — especially through
transitive dependencies — that bypasses this wrapping. Note: modern
deployments primarily use JSON-RPC, making WSDL processing a secondary
concern triggered mainly by `?wsdl` metadata requests.

Key files:
- `modules/kernel/src/org/apache/axis2/util/SecureWSDLLocator.java`
- `modules/kernel/src/org/apache/axis2/util/XMLUtils.java`
- `modules/kernel/src/org/apache/axis2/util/DefaultEntityResolver.java`
- `modules/kernel/src/org/apache/axis2/deployment/resolver/AARFileBasedURIResolver.java`
- `modules/kernel/src/org/apache/axis2/deployment/resolver/WarFileBasedURIResolver.java`
- `modules/kernel/src/org/apache/axis2/deployment/resolver/AARBasedWSDLLocator.java`
- `modules/kernel/src/org/apache/axis2/deployment/resolver/WarBasedWSDLLocator.java`

### 6. Multipart/File Upload

Migrated from commons-fileupload 1.x to commons-fileupload2 for
CVE-2023-24998. Verify the migration is complete and no legacy code
paths remain.

Key files:
- `modules/kernel/src/org/apache/axis2/builder/MultipartFormDataBuilder.java`
- `modules/webapp/src/main/java/org/apache/axis2/webapp/AdminActions.java`

### 7. Admin Console (webapp)

The admin console has a history of web vulnerabilities (CVE-2010-3981:
CSRF/XSS). Scan for common web security issues including authentication,
authorization, session management, and input validation.

Key files:
- `modules/webapp/src/main/java/org/apache/axis2/webapp/`
- `modules/webapp/src/main/webapp/axis2-web/`

## Project Structure

```
modules/
  json/         JSON processing: Gson, Moshi, HTTP/2 enhanced variants,
                JSON-RPC receivers, field filtering, pagination, streaming
  kernel/       Core engine: message pipeline, handlers, deployment,
                XML parsing, dispatchers, context
  transport/    Pluggable transports: HTTP, local, JMS, TCP, UDP, mail
  openapi/      OpenAPI schema generation, Swagger UI, MCP catalog
  mcp-bridge/   MCP stdio bridge for AI agent integration
  webapp/       Admin console WAR
  addressing/   WS-Addressing module
  fuzz/         Jazzer fuzz targets (XML, JSON, HTTP headers, URLs)
  samples/      Sample services including Spring Boot deployments
systests/       Integration tests
```

## Testing and Fuzzing

Fuzz targets exist in `modules/fuzz/` covering XML, JSON, HTTP header,
and URL parsers. See `src/site/xdoc/docs/OSS-FUZZ.md` for details.

## Reporting

Security vulnerabilities: **security@apache.org**
