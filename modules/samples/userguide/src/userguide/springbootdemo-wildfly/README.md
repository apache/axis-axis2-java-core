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

# springbootdemo-wildfly

Axis2 JSON-RPC services deployed as a WAR in **WildFly** (Undertow), using Spring Boot 3.x as
a configuration framework only — there is no embedded container.

Tested with: **WildFly 39.0.1.Final** · **OpenJDK 25** · **Spring Boot 3.4.3**

All Java source is shared from `../springbootdemo-tomcat11/src/main/java` via
`build-helper-maven-plugin`. This module only adds WildFly-specific WEB-INF descriptors.

---

## WildFly-specific files

| File | Purpose |
|------|---------|
| `src/main/webapp/WEB-INF/jboss-deployment-structure.xml` | Excludes conflicting WildFly subsystems (jaxrs, logging, jpa, cdi) and modules (log4j, slf4j, jboss-logging); adds `jdk.unsupported` dependency |
| `src/main/webapp/WEB-INF/jboss-web.xml` | Sets WildFly context root (defaults to `/axis2-json-api` from WAR name) |
| `src/main/webapp/WEB-INF/beans.xml` | `bean-discovery-mode="none"` — prevents Weld CDI from scanning Spring beans |

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
cd modules/samples/userguide/src/userguide/springbootdemo-wildfly
mvn package -DskipTests
```

This produces an exploded WAR at `target/deploy/axis2-json-api/`.

---

## Deploy to WildFly

WildFly's deployment scanner treats a directory ending in `.war` as an exploded WAR.

```bash
# Sync to the WildFly deployments directory (adjust path as needed)
rsync -a --delete target/deploy/axis2-json-api/ ~/wildfly/standalone/deployments/axis2-json-api.war/

# Trigger redeploy (WildFly hot-scans for this marker)
touch ~/wildfly/standalone/deployments/axis2-json-api.war.dodeploy
```

To undeploy first via the CLI (avoids stale classloader state on hot-redeploy):

```bash
~/wildfly/bin/jboss-cli.sh --connect --command='/deployment=axis2-json-api.war:undeploy'
# then rsync + .dodeploy as above
```

### WildFly startup with Java 25

WildFly uses whatever `JAVA_HOME` is set when `standalone.sh` is launched. Set it before
starting:

```bash
export JAVA_HOME=/usr/lib/jvm/java-25-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH
unset JAVA_OPTS   # avoid -XX:+PrintFlagsFinal flooding jboss-cli.sh output
~/wildfly/bin/standalone.sh
```

---

## Known WildFly-specific behaviors

### `loadOnStartup` is ignored for programmatic servlet registration

WildFly / Undertow does **not** honour `setLoadOnStartup(1)` on servlets registered via
`ServletContextInitializer.addServlet()`. The AxisServlet initializes lazily on the first
request. This is normal — Axis2 service loading still works because
`Axis2WebAppInitializer` explicitly sets the `axis2.repository.path` init parameter (see
below).

### `axis2.repository.path` must be set explicitly

`WarBasedAxisConfigurator` normally locates `WEB-INF/services/*.aar` via
`ServletContext.getRealPath("/WEB-INF")`. On WildFly this can silently fail (VFS timing,
lazy init). `Axis2WebAppInitializer.addAxis2Servlet()` bypasses this by calling
`getRealPath()` eagerly at startup time and setting the `axis2.repository.path` servlet
init parameter directly. Without this, only some services load.

### Excluded subsystems

`jboss-deployment-structure.xml` excludes:

- **jaxrs** — RESTEasy conflicts with Axis2 servlet dispatch
- **logging** — WildFly logging conflicts with bundled Log4j2 + log4j-jcl
- **jpa** — no `persistence.xml`; prevents WildFly JPA subsystem activation
- **bean-validation** — Spring handles its own validation
- **cdi** — prevents Weld from managing Spring beans

### WildFly module upgrade notes

When upgrading WildFly (e.g., 32 → 39), clear cached state before redeploying:

```bash
rm -rf ~/wildfly/standalone/configuration/standalone_xml_history/
rm -rf ~/wildfly/standalone/configuration/tmp/
rm -f  ~/wildfly/standalone/deployments/axis2-json-api.war.failed
rm -f  ~/wildfly/standalone/deployments/axis2-json-api.war.deployed
```

The `axis2-json-api.war.failed` marker from an old WildFly version is **not** automatically
retried by a new WildFly instance — it must be removed.

---

## Test flow

See `../springbootdemo-tomcat11/README.md` for the full curl-based test sequence; it applies
identically to WildFly (same context path, same JSON-RPC format, same credentials).

The only difference: use port `8080` on WildFly (same default as Tomcat 11).
