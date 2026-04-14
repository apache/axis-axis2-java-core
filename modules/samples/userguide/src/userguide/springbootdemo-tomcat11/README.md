<!--
  ~ Licensed to the Apache Software Foundation (ASF) under one
  ~ or more contributor license agreements. See the NOTICE file
  ~ distributed with this work for additional information
  ~ regarding copyright ownership. The ASF licenses this file
  ~ to you under the Apache License, Version 2.0 (the
  ~ "License"); you may not use this file except in compliance
  ~ with the License. You may obtain a copy of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing,
  ~ software distributed under the License is distributed on an
  ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~ KIND, either express or implied. See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
  -->

# springbootdemo-tomcat11

Axis2 JSON-RPC services deployed as a WAR in **Apache Tomcat 11**, using Spring Boot 3.x as a
configuration framework only — there is no embedded container.

Tested with: **Tomcat 11.0.20** · **OpenJDK 21** · **Spring Boot 3.4.3**

> **JDK compatibility note:** The WAR is compiled at Java 21 source level (`<java.version>21</java.version>`)
> and has been tested on both **OpenJDK 21** and **OpenJDK 25** under Tomcat 11.

---

## Services

| Service | Operation | Path | Auth |
|---------|-----------|------|------|
| `loginService` | `doLogin` | `POST /axis2-json-api/services/loginService` | None (public) |
| `testws` | `doTestws` | `POST /axis2-json-api/services/testws` | Bearer token |
| `BigDataH2Service` | `processBigDataSet` | `POST /axis2-json-api/services/BigDataH2Service` | Bearer token |
| `FinancialBenchmarkService` | `portfolioVariance` | `POST /axis2-json-api/services/FinancialBenchmarkService` | Bearer token |
| `FinancialBenchmarkService` | `monteCarlo` | `POST /axis2-json-api/services/FinancialBenchmarkService` | Bearer token |
| `FinancialBenchmarkService` | `scenarioAnalysis` | `POST /axis2-json-api/services/FinancialBenchmarkService` | Bearer token |
| OpenAPI spec (JSON) | — | `GET /axis2-json-api/openapi.json` | None |
| OpenAPI spec (YAML) | — | `GET /axis2-json-api/openapi.yaml` | None |
| Swagger UI | — | `GET /axis2-json-api/swagger-ui` | None |
| OpenAPI MCP | — | `GET /axis2-json-api/openapi-mcp.json` | None |

---

## Build

```bash
cd modules/samples/userguide/src/userguide/springbootdemo-tomcat11
mvn package
```

This produces an exploded WAR directory at `target/deploy/axis2-json-api/`.

---

## Deploy to Tomcat 11

```bash
# Copy exploded WAR to Tomcat webapps
cp -r target/deploy/axis2-json-api /path/to/tomcat/webapps/

# Restart Tomcat
/path/to/tomcat/bin/shutdown.sh && /path/to/tomcat/bin/startup.sh
```

### CRITICAL: Directory naming and context path

Tomcat strips the `.war` suffix when deploying a **packaged** `foo.war` file, giving context
path `/foo`. However, when deploying an **exploded directory**, Tomcat uses the directory name
as-is. A directory named `axis2-json-api.war/` deploys at context path `/axis2-json-api.war`,
**not** `/axis2-json-api`.

The Maven build produces `target/deploy/axis2-json-api` (no `.war` suffix) for exactly this
reason. Do not rename the directory before copying to `webapps/`.

---

## Test flow

All tests use **HTTPS/HTTP2 on port 8443** with mTLS client certificates. The Tomcat connector
requires `certificateVerification="required"` — plain HTTP is not available.

Set up the cert variables first:

```bash
CERTS=/path/to/axis-axis2-java-core/certs
CURL_MTLS="curl -s --http2 --cert $CERTS/client.crt --key $CERTS/client.key --cacert $CERTS/ca.crt"
```

### 1. Verify OpenAPI and MCP endpoints

```bash
$CURL_MTLS https://localhost:8443/axis2-json-api/openapi.json
$CURL_MTLS https://localhost:8443/axis2-json-api/openapi.yaml
$CURL_MTLS https://localhost:8443/axis2-json-api/openapi-mcp.json
# Interactive UI:
$CURL_MTLS https://localhost:8443/axis2-json-api/swagger-ui
```

### 2. Login (get Bearer token)

```bash
$CURL_MTLS -X POST https://localhost:8443/axis2-json-api/services/loginService \
  -H 'Content-Type: application/json' \
  -d '{"doLogin":[{"arg0":{"email":"java-dev@axis.apache.org","credentials":"userguide"}}]}'
```

Response: `{"response":{"token":"<TOKEN>","uuid":"<UUID>","status":"OK"}}`

### 3. Call protected service (testws)

`messagein` must pass ESAPI `SafeString` validation (`[A-Za-z0-9.,\-_ ]*` — no `+` or special
characters).

```bash
TOKEN="<token from step 2>"
$CURL_MTLS -X POST https://localhost:8443/axis2-json-api/services/testws \
  -H 'Content-Type: application/json' \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"doTestws":[{"arg0":{"messagein":"hello world"}}]}'
```

### 4. Call BigData service

`datasetSize` is in bytes. Size determines processing path: under 10 MB → standard,
10–50 MB → multiplexing, >50 MB → streaming. Use at least 1 000 000 to get populated results.

```bash
$CURL_MTLS -X POST https://localhost:8443/axis2-json-api/services/BigDataH2Service \
  -H 'Content-Type: application/json' \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"processBigDataSet":[{"arg0":{"datasetId":"test-001","datasetSize":1000000,"processingMode":"streaming","enableMemoryOptimization":true,"analyticsType":"summary"}}]}'
```

Response includes `processedRecordCount`, `http2Optimized`, `memoryOptimized`, and a
`processedRecords` array.

### 5. Financial Benchmark Service

```bash
# Portfolio variance — O(n²) covariance matrix
$CURL_MTLS -X POST https://localhost:8443/axis2-json-api/services/FinancialBenchmarkService \
  -H 'Content-Type: application/json' \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"portfolioVariance":[{"arg0":{"nAssets":2,"weights":[0.6,0.4],"covarianceMatrix":[[0.04,0.006],[0.006,0.09]],"normalizeWeights":false,"nPeriodsPerYear":252}}]}'

# Monte Carlo VaR — GBM simulation
$CURL_MTLS -X POST https://localhost:8443/axis2-json-api/services/FinancialBenchmarkService \
  -H 'Content-Type: application/json' \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"monteCarlo":[{"arg0":{"nSimulations":10000,"nPeriods":252,"initialValue":100.0,"expectedReturn":0.08,"volatility":0.20,"nPeriodsPerYear":252,"randomSeed":42}}]}'

# Scenario analysis — probability-weighted expected return
$CURL_MTLS -X POST https://localhost:8443/axis2-json-api/services/FinancialBenchmarkService \
  -H 'Content-Type: application/json' \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"scenarioAnalysis":[{"arg0":{"assets":[{"assetId":1,"currentPrice":100.0,"positionSize":100,"scenarios":[{"price":120.0,"probability":0.3},{"price":100.0,"probability":0.5},{"price":75.0,"probability":0.2}]}],"useHashLookup":true,"probTolerance":0.001}}]}'
```

---

## Streaming JSON message formatter

Axis2 2.0.1 includes streaming message formatters that prevent reverse proxy
body-size rejections on large HTTP responses. Drop-in replacement — no service
code changes required.

**Problem solved (response-side):** The default formatter buffers the entire
JSON response before writing to the wire. A reverse proxy may return 502 Bad
Gateway on large responses. The streaming formatter flushes every 64 KB
(configurable), so the proxy sees a stream of chunks, never the full body.

**Not solved (request-side):** Large HTTP request bodies (client → server)
are a client-side problem. If a client sends a 620 MB POST and the proxy
rejects it, the fix is client-side: break the request into smaller payloads
or add a pre-send size guard.

| Variant | Class |
|---------|-------|
| GSON | `org.apache.axis2.json.streaming.JSONStreamingMessageFormatter` |
| Moshi | `org.apache.axis2.json.streaming.MoshiStreamingMessageFormatter` |

Enable globally in `axis2.xml`:

```xml
<messageFormatter contentType="application/json"
    class="org.apache.axis2.json.streaming.MoshiStreamingMessageFormatter"/>
```

Optional flush interval tuning per-service in `services.xml`:

```xml
<parameter name="streamingFlushIntervalBytes">131072</parameter>
```

Applies to all services (BigDataH2Service, FinancialBenchmarkService, any
custom service). Tested on WildFly 32 locally and behind a real reverse proxy
(stg-rapi02, HTTP/2 ALPN). Bit-identical results to the non-streaming formatter.

See the [Streaming JSON Message Formatter guide](../../../../../../src/site/xdoc/docs/json-streaming-formatter.xml) for full documentation.

---

## Axis2 JSON-RPC request format

The top-level key is the **operation name**, and the value is an array containing one object
whose key is the argument name (conventionally `arg0`) and whose value is the request POJO:

```json
{ "operationName": [{ "arg0": { ...fields... } }] }
```

This is mandated by `JsonUtils.invokeServiceClass()` in the `axis2-json` module.

---

## Architecture notes

- **No embedded container** — `Axis2Application` extends `SpringBootServletInitializer`, not
  `SpringApplication.run()`. Spring Boot only provides configuration; Tomcat is the server.
- **No `DispatcherServlet`** — `@GetMapping` annotations are dead code in this module. OpenAPI
  endpoints are served by `OpenApiServlet`, a plain `HttpServlet` registered directly in
  `Axis2WebAppInitializer` at `/openapi.json`, `/openapi.yaml`, `/swagger-ui`, and
  `/openapi-mcp.json`.
- **Axis2 repository path** — `Axis2WebAppInitializer` explicitly sets the
  `axis2.repository.path` servlet init parameter using `ServletContext.getRealPath("/WEB-INF")`.
  This is required on both Tomcat and WildFly to ensure `WarBasedAxisConfigurator` finds the
  `WEB-INF/services/*.aar` archives reliably, bypassing any VFS or lazy-init timing issues.
- **OpenAPI module** — `axis2-openapi-<version>.jar` is copied to
  `WEB-INF/modules/openapi-<version>.mar` by the Maven build. It must be present for
  `GET /openapi.*` and `GET /swagger-ui` to work.
- **Security** — three Spring Security filter chains: OpenApi (Order 2, unauthenticated),
  Login (Order 3, unauthenticated), Token (remaining, requires Bearer JWT).

---

## Relationship to `springbootdemo-wildfly`

`springbootdemo-tomcat11` and `springbootdemo-wildfly` share all Java source and resources
via `build-helper-maven-plugin`. The only differences are container-specific:

| Aspect | `springbootdemo-tomcat11` | `springbootdemo-wildfly` |
|--------|--------------------------|--------------------------|
| Server | Apache Tomcat 11 | WildFly 32+ (Undertow) |
| Tested on | Tomcat 11.0.20 / Java 21 and Java 25 | WildFly 39 / Java 25 |
| WAR output | `target/deploy/axis2-json-api/` (no `.war` suffix) | `target/deploy/axis2-json-api/` |
| Extra WEB-INF files | — | `jboss-deployment-structure.xml`, `jboss-web.xml`, `beans.xml` |
| Context path | `/axis2-json-api` | `/axis2-json-api` |
