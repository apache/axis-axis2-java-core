# Apache Axis2 - Swagger REST Server Sample

This sample demonstrates how to implement a financial data services REST API using Apache Axis2 with comprehensive OpenAPI/Swagger documentation.

## Overview

This project showcases:
- Axis2 REST services with JSON support
- OpenAPI 3.0.1 specification generation
- Swagger UI integration
- Financial domain services (funds, assets, calculations)
- Authentication and authorization
- Comprehensive error handling

## API Endpoints

- `/bigdataservice/login` - Authentication (`AuthenticationService`, the only service currently implemented)
- `/swagger-ui` - Interactive API documentation

`src/main/resources/openapi/financial-api-schema.json` also describes fund, asset,
calculation and user operations (`/bigdataservice/funds/summary`, `/bigdataservice/assets/calculate`,
`/bigdataservice/user/info`, `/bigdataservice/user/permissions`). Those are
schema only; the sample does not implement them.

## Building and Running

```bash
mvn clean package
# Deploy the generated WAR to your servlet container
```

The OpenAPI specification will be available at:
- JSON: `/openapi.json`
- YAML: `/openapi.yaml`
- MCP tool catalog: `/openapi-mcp.json`
- Swagger UI: `/swagger-ui/`