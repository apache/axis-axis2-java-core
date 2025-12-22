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

- `/bigdataservice/login` - Authentication
- `/bigdataservice/funds` - Fund data operations
- `/bigdataservice/assets` - Asset calculations
- `/bigdataservice/user` - User management
- `/swagger-ui` - Interactive API documentation

## Building and Running

```bash
mvn clean package
# Deploy the generated WAR to your servlet container
```

The OpenAPI specification will be available at:
- JSON: `/openapi.json`
- YAML: `/openapi.yaml`
- Swagger UI: `/swagger-ui/`