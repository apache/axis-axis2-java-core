# axis2-openapi — OpenAPI Integration Module

Auto-generates OpenAPI 3.0.1 specifications from deployed Axis2 services and serves Swagger UI at `/swagger-ui`.

## Endpoints

| URL | Description |
|-----|-------------|
| `/openapi.json` | OpenAPI 3.0.1 spec (JSON) |
| `/openapi.yaml` | OpenAPI 3.0.1 spec (YAML) |
| `/swagger-ui` | Interactive Swagger UI |

## Enabling the module

Add to `WEB-INF/conf/axis2.xml`:
```xml
<module ref="openapi"/>
```

Copy `axis2-openapi-<version>.jar` to `WEB-INF/modules/openapi-<version>.mar`.

---

## Configuration

Configuration is loaded in this order (later sources win):

1. `module.xml` parameters
2. `openapi.properties` on the classpath (or the path set by `propertiesFile` module param)
3. Known locations: `META-INF/openapi.properties`, `WEB-INF/openapi.properties`, `openapi-config.properties`
4. System properties (same key names)
5. Programmatic `OpenApiConfiguration` API (highest precedence)

### API information

| Property key | Default | Description |
|---|---|---|
| `openapi.title` | `Apache Axis2 REST API` | Spec `info.title` |
| `openapi.description` | (auto) | Spec `info.description` |
| `openapi.version` | `1.0.0` | Spec `info.version` |
| `openapi.contact.name` | `Apache Axis2` | Contact name |
| `openapi.contact.url` | (Apache URL) | Contact URL |
| `openapi.contact.email` | — | Contact e-mail |
| `openapi.license.name` | `Apache License 2.0` | License name |
| `openapi.license.url` | (Apache URL) | License URL |
| `openapi.termsOfServiceUrl` | — | Terms of service URL |

### Generation flags

| Property key | Default | Description |
|---|---|---|
| `openapi.prettyPrint` | `true` | Indent JSON/YAML output |
| `openapi.readAllResources` | `true` | Include all services unless filtered |
| `openapi.swaggerUi.enabled` | `true` | Serve Swagger UI |
| `openapi.swaggerUi.version` | `4.15.5` | CDN version of Swagger UI bundle |
| `openapi.resourcePackages` | — | Comma-separated Java packages; only services whose `ServiceClass` is in these packages are included (requires `readAllResources=false`) |

---

## Filtering: excluding services and operations

Three independent mechanisms control what appears in the generated spec. All are evaluated **before** inclusion rules — an excluded entity never appears even if it would otherwise match `readAllResources` or `resourcePackages`.

### 1. `ignoredServices` — exclude entire services by name

Matches against `AxisService.getName()` (exact, case-sensitive).

**Properties file:**
```properties
# Comma-separated list of service names to exclude
openapi.ignoredServices=InternalService, DebugService, AdminService
```

**Java API:**
```java
OpenApiConfiguration config = new OpenApiConfiguration();
config.addIgnoredService("InternalService");
config.addIgnoredService("DebugService");
```

### 2. `ignoredOperations` — exclude specific operations

Each entry is one of:

| Format | Effect |
|---|---|
| `ServiceName/operationName` | Excludes that operation on that service only |
| `operationName` | Excludes that operation name on **every** service |

**Properties file:**
```properties
# Targeted: remove one op from one service
# Global: remove an op name from all services
openapi.ignoredOperations=AdminService/nukeDatabase, internalStatus, debugPing
```

**Java API:**
```java
config.addIgnoredOperation("AdminService/nukeDatabase");  // targeted
config.addIgnoredOperation("internalStatus");             // global (all services)
```

### 3. `ignoredRoutes` — exclude by generated path pattern

Matches against the generated path string (e.g. `/services/MyService/myOp`).
Each entry is tested as a Java regex (`String.matches()`) **or** a substring (`String.contains()`).

```properties
openapi.ignoredRoutes=/services/internal/.*, /services/legacy/.*
```

**Java API:**
```java
config.addIgnoredRoute("/services/internal/.*");
```

> **Prefer `ignoredServices` and `ignoredOperations`** over `ignoredRoutes` — they match on logical names rather than generated path strings and are unaffected by future path format changes.

### Precedence within the generator

```
isSystemService()          (hardcoded: Version, AdminService, __)
  → ignoredServices
    → readAllResources / resourceClasses / resourcePackages
      → shouldIncludeOperation() / ignoredOperations
        → isIgnoredRoute() / ignoredRoutes
          → (path added to spec)
```

### Complete `openapi.properties` example

Place on the classpath as `openapi.properties` (or `META-INF/openapi.properties`):

```properties
# API identity
openapi.title=My Financial API
openapi.version=2.1.0
openapi.description=Internal portfolio management services

# Contact / license
openapi.contact.name=Platform Team
openapi.contact.email=platform@example.com
openapi.license.name=Proprietary

# Service filtering
openapi.ignoredServices=LegacySOAPService, InternalHealthCheck
openapi.ignoredOperations=AdminService/resetDatabase, debugEcho

# UI
openapi.prettyPrint=true
openapi.swaggerUi.enabled=true
openapi.swaggerUi.version=4.15.5
```

---

## Programmatic configuration (Java)

```java
OpenApiConfiguration config = new OpenApiConfiguration();

// API info
config.setTitle("My API");
config.setVersion("2.0.0");

// Exclude services
config.addIgnoredService("InternalService");

// Exclude operations
config.addIgnoredOperation("AdminService/nukeDatabase"); // this service only
config.addIgnoredOperation("debugPing");                 // all services

// Pass to the generator
OpenApiSpecGenerator generator = new OpenApiSpecGenerator(configContext, config);
String json = generator.generateOpenApiJson(httpRequest);
String yaml = generator.generateOpenApiYaml(httpRequest);
```

---

## Known limitations

- **Request body schema** — all operations are typed as `object` because Axis2 JSON-RPC services use `JsonRpcMessageReceiver` and have no annotation-level parameter metadata. Schema details must be added via `OpenApiCustomizer` or by serving a static schema file.
- **GET operations** — all operations are mapped to `POST`; override via `OpenApiCustomizer` if GET endpoints are needed.
- **YAML format** — delegates to `io.swagger.v3.core.util.Yaml`, which uses `jackson-dataformat-yaml` internally; no additional dependency required.
