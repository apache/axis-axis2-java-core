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

---

## Services

| Service | Path | Auth |
|---------|------|------|
| `LoginService.login` | `POST /axis2-json-api/services/LoginService` | None (public) |
| `TestwsService.testws` | `POST /axis2-json-api/services/TestwsService` | Bearer token |
| `BigDataH2Service.processBigDataSet` | `POST /axis2-json-api/services/BigDataH2Service` | None (public) |
| OpenAPI spec (JSON) | `GET /axis2-json-api/openapi.json` | None |
| OpenAPI spec (YAML) | `GET /axis2-json-api/openapi.yaml` | None |
| Swagger UI | `GET /axis2-json-api/swagger-ui` | None |

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

### 1. Verify OpenAPI docs

```bash
curl http://localhost:8080/axis2-json-api/openapi.json
curl http://localhost:8080/axis2-json-api/openapi.yaml
# Interactive UI:
curl http://localhost:8080/axis2-json-api/swagger-ui
```

### 2. Login (get Bearer token)

```bash
curl -s -X POST http://localhost:8080/axis2-json-api/services/LoginService \
  -H 'Content-Type: application/json' \
  -d '{"login":[{"request":{"email":"user@example.com","credentials":"password"}}]}'
```

Response: `{"loginResponse":{"token":"<JWT>","status":"OK"}}`

### 3. Call protected service

```bash
TOKEN="<JWT from step 2>"
curl -s -X POST http://localhost:8080/axis2-json-api/services/TestwsService \
  -H 'Content-Type: application/json' \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"testws":[{"request":{"name":"World"}}]}'
```

### 4. Call public BigData service

```bash
curl -s -X POST http://localhost:8080/axis2-json-api/services/BigDataH2Service \
  -H 'Content-Type: application/json' \
  -d '{"processBigDataSet":[{"request":{"numRecords":1000}}]}'
```

---

## Axis2 JSON-RPC request format

The top-level key is the **operation name**, and the body is wrapped in an array:

```json
{ "operationName": [{ "request": { ...fields... } }] }
```

---

## Architecture notes

- **No embedded container** — `Axis2Application` extends `SpringBootServletInitializer`, not
  `SpringApplication.run()`. Spring Boot only provides configuration; Tomcat is the server.
- **No `DispatcherServlet`** — `@GetMapping` annotations are dead code in this module. OpenAPI
  endpoints are served by `OpenApiServlet`, a plain `HttpServlet` registered directly in
  `Axis2WebAppInitializer` at `/openapi.json`, `/openapi.yaml`, and `/swagger-ui`.
- **OpenAPI module** — `axis2-openapi-<version>.jar` is copied to
  `WEB-INF/modules/openapi-<version>.mar` by the Maven build. It must be present for
  `GET /openapi.*` and `GET /swagger-ui` to work.
- **Security** — three Spring Security filter chains: OpenApi (Order 2, unauthenticated),
  Login (Order 3, unauthenticated), Token (remaining, requires Bearer JWT).

---

## Relationship to `springbootdemo` (WildFly)

`springbootdemo-tomcat11` is derived from `springbootdemo` (which targets WildFly). The two
modules share the same service logic but differ in:

| Aspect | `springbootdemo` (WildFly) | `springbootdemo-tomcat11` |
|--------|---------------------------|--------------------------|
| Server | WildFly / embedded Undertow | Apache Tomcat 11 |
| Context path | `/axis2-json-api` | `/axis2-json-api` |
| OpenAPI routing | `OpenApiServlet` via `Axis2WebAppInitializer` | Same |
| H2 BigData service | Yes | Yes |
