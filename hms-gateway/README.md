# HMS Gateway

API Gateway for the HMS (Health Management System) Platform - The single entry point for all HMS microservices.

## Overview

The HMS Gateway is a reactive API Gateway built with Spring Cloud Gateway that provides:

- **Centralized Routing**: Routes requests to all HMS microservices
- **CORS Handling**: Manages cross-origin resource sharing for web clients
- **Request Tracing**: Adds X-Request-ID headers for distributed tracing
- **Request/Response Logging**: Comprehensive logging for observability
- **Error Handling**: Consistent error responses across all services
- **Health Check Aggregation**: Centralized health monitoring

**Security Note**: The gateway does NOT validate JWT tokens. Authentication and authorization are delegated to individual microservices, keeping the gateway stateless and fast.

## Technology Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 25 | Runtime environment with virtual threads |
| Spring Boot | 3.5.9 | Application framework |
| Spring Cloud Gateway | 2025.0.0 | Reactive gateway implementation |
| Spring Cloud | 2025.0.0 | Cloud-native patterns |
| Project Reactor | Built-in | Reactive programming |

## Architecture

```
┌─────────────┐
│   Clients   │ (Web, Mobile, API)
└──────┬──────┘
       │
       ▼
┌─────────────────────────────────────────┐
│          HMS Gateway (Port 8080)        │
│  ┌─────────────────────────────────┐   │
│  │  Request ID Filter              │   │
│  │  Logging Filter                 │   │
│  │  CORS Configuration             │   │
│  │  Error Handler                  │   │
│  └─────────────────────────────────┘   │
└──────┬──────────────────────────────────┘
       │
       ├──────► Authorization Server (9000)
       ├──────► Communications Server (8081)
       ├──────► Patient Service (8082)
       ├──────► Prescription Service (8083)
       ├──────► Billing Service (8084)
       ├──────► Appointment Service (8085)
       ├──────► Lab Service (8086)
       ├──────► Pharmacy Service (8087)
       └──────► Reporting Service (8088)
```

## Service Port Mapping

| Service | Port | Context | Gateway Routes |
|---------|------|---------|----------------|
| **Gateway** | **8080** | / | (entry point) |
| Authorization | 9000 | /auth | `/api/v1/auth/**`, `/api/v1/tenants/**`, `/api/v1/users/**`, `/api/v1/roles/**`, `/api/v1/permissions/**` |
| Communications | 8081 | / | `/api/v1/email/**`, `/api/v1/sms/**` |
| Patient | 8082 | / | `/api/v1/patients/**` |
| Prescription | 8083 | / | `/api/v1/prescriptions/**` (commented out) |
| Billing | 8084 | / | `/api/v1/invoices/**`, `/api/v1/payments/**` (commented out) |
| Appointment | 8085 | / | `/api/v1/appointments/**` (commented out) |
| Lab | 8086 | / | `/api/v1/lab-orders/**`, `/api/v1/lab-results/**` (commented out) |
| Pharmacy | 8087 | / | `/api/v1/inventory/**`, `/api/v1/dispensing/**` (commented out) |
| Reporting | 8088 | / | `/api/v1/reports/**`, `/api/v1/analytics/**` (commented out) |

## Quick Start

### Prerequisites

- Java 25 or higher
- Maven 3.9+
- Running HMS Authorization Server (port 9000)
- Running HMS Communications Server (port 8081) - optional
- Running HMS Patient Service (port 8082) - optional

### Run Locally

```bash
# Clone the repository
cd hms-gateway

# Run the gateway
./mvnw spring-boot:run

# Or with specific profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

The gateway will start on `http://localhost:8080`

### Test the Gateway

```bash
# Check gateway health
curl http://localhost:8080/actuator/health

# Test auth service routing (requires auth server running)
curl http://localhost:8080/api/v1/auth/login

# Check downstream service health
curl http://localhost:8080/health/auth
curl http://localhost:8080/health/communications
curl http://localhost:8080/health/patient
```

## Configuration

### Development (application.yml)

```yaml
server:
  port: 8080

gateway:
  services:
    auth: http://localhost:9000
    communications: http://localhost:8081
    patient: http://localhost:8082
    # ... other services

  cors:
    allowed-origins:
      - http://localhost:4200
      - http://localhost:3000
```

### Production (application-production.yml)

```yaml
gateway:
  services:
    auth: http://hms-authorization-server:9000
    communications: http://hms-communications-server:8081
    # ... Kubernetes DNS names

  cors:
    allowed-origins:
      - https://app.hms-platform.com
      - https://admin.hms-platform.com
```

## Route Configuration

### Active Routes

#### Authorization Service Routes
Routes with path rewriting due to `/auth` context path:

```yaml
- id: auth-authentication
  uri: ${gateway.services.auth}
  predicates:
    - Path=/api/v1/auth/**
  filters:
    - RewritePath=/api/v1/auth/(?<segment>.*), /auth/api/v1/auth/${segment}
```

**Example**: `GET /api/v1/auth/login` → `GET http://localhost:9000/auth/api/v1/auth/login`

#### Communications Service Routes
Direct forwarding (no path rewriting):

```yaml
- id: communications-email
  uri: ${gateway.services.communications}
  predicates:
    - Path=/api/v1/email/**
```

**Example**: `POST /api/v1/email/send` → `POST http://localhost:8081/api/v1/email/send`

#### Patient Service Routes

```yaml
- id: patient-management
  uri: ${gateway.services.patient}
  predicates:
    - Path=/api/v1/patients/**
```

**Example**: `GET /api/v1/patients/123` → `GET http://localhost:8082/api/v1/patients/123`

### Health Check Routes

Health check aggregation endpoints:

```yaml
- id: health-auth
  uri: ${gateway.services.auth}
  predicates:
    - Path=/health/auth
  filters:
    - RewritePath=/health/auth, /auth/actuator/health
```

**Example**: `GET /health/auth` → `GET http://localhost:9000/auth/actuator/health`

### Adding New Routes

To add a route for a new service:

1. **Add service URL** to `application.yml`:
```yaml
gateway:
  services:
    new-service: http://localhost:8089
```

2. **Add route configuration**:
```yaml
- id: new-service-api
  uri: ${gateway.services.new-service}
  predicates:
    - Path=/api/v1/new-service/**
  filters:
    - DedupeResponseHeader=Access-Control-Allow-Origin Access-Control-Allow-Credentials
```

3. **Add health check route** (optional):
```yaml
- id: health-new-service
  uri: ${gateway.services.new-service}
  predicates:
    - Path=/health/new-service
  filters:
    - RewritePath=/health/new-service, /actuator/health
```

4. **Update production config** (`application-production.yml`):
```yaml
gateway:
  services:
    new-service: http://hms-new-service:8089
```

## Filters

### Global Filters

#### 1. RequestIdFilter
Adds `X-Request-ID` header to every request for distributed tracing.

- **Priority**: Highest (runs first)
- **Behavior**:
  - If client provides `X-Request-ID`, it is preserved
  - Otherwise, generates a new UUID
  - Adds the ID to both request and response headers

#### 2. LoggingFilter
Logs request and response information.

- **Priority**: High (runs after RequestIdFilter)
- **Logs**:
  - Request: Method, path, request ID
  - Response: Status code, latency
  - Errors: Full error details with stack trace
- **Excludes**: Actuator endpoints (to reduce noise)

### Route Filters

#### DedupeResponseHeader
Removes duplicate CORS headers to prevent browser issues.

```yaml
filters:
  - DedupeResponseHeader=Access-Control-Allow-Origin Access-Control-Allow-Credentials
```

#### RewritePath
Rewrites request paths for services with context paths.

```yaml
filters:
  - RewritePath=/api/v1/auth/(?<segment>.*), /auth/api/v1/auth/${segment}
```

## CORS Configuration

CORS is configured globally in `application.yml`:

```yaml
spring:
  cloud:
    gateway:
      server:
        webflux:
          globalcors:
            cors-configurations:
              '[/**]':
                allowedOriginPatterns: "http://localhost:*,http://127.0.0.1:*"
                allowedMethods: "GET, POST, PUT, DELETE, OPTIONS, PATCH"
                allowedHeaders: "*"
                exposedHeaders: "Authorization, Content-Type, X-Request-ID, Location"
                allowCredentials: true
```

**Production**: Update `allowedOriginPatterns` to match your domains:
```yaml
allowedOriginPatterns: "https://*.hms-platform.com"
```

## Error Handling

The `GlobalErrorWebExceptionHandler` provides consistent error responses:

### Error Response Format

```json
{
  "success": false,
  "error": "Service Unavailable",
  "message": "The requested service is currently unavailable. Please try again later.",
  "status": 503,
  "path": "/api/v1/patients/123",
  "requestId": "550e8400-e29b-41d4-a716-446655440000",
  "timestamp": "2026-01-23T10:30:00"
}
```

### Error Status Codes

| Status | Error Type | Cause |
|--------|------------|-------|
| 503 | Service Unavailable | Downstream service is down (ConnectException) |
| 504 | Gateway Timeout | Downstream service timeout (TimeoutException) |
| 404 | Not Found | Route doesn't exist |
| 502 | Bad Gateway | Invalid response from service |
| 500 | Gateway Error | Unexpected error |

## Monitoring & Observability

### Actuator Endpoints

| Endpoint | Purpose |
|----------|---------|
| `/actuator/health` | Gateway health status |
| `/actuator/metrics` | Micrometer metrics |
| `/actuator/prometheus` | Prometheus metrics export |
| `/actuator/gateway/routes` | List all configured routes |
| `/actuator/gateway/routefilters` | List all route filters |
| `/actuator/gateway/globalfilters` | List all global filters |

### Metrics

Key metrics exposed via Micrometer:

- `http.server.requests` - Request count, latency, status codes
- `gateway.requests` - Per-route request metrics
- `jvm.memory.used` - JVM memory usage
- `jvm.gc.pause` - Garbage collection pauses

### Example Metrics Queries

```bash
# Get all metrics
curl http://localhost:8080/actuator/metrics

# Get specific metric
curl http://localhost:8080/actuator/metrics/http.server.requests

# View configured routes
curl http://localhost:8080/actuator/gateway/routes
```

## Docker

### Build Docker Image

```bash
docker build -t hms-gateway:latest .
```

### Run Docker Container

```bash
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=production \
  -e GATEWAY_SERVICES_AUTH=http://hms-auth-server:9000 \
  -e GATEWAY_SERVICES_COMMUNICATIONS=http://hms-comm-server:8081 \
  hms-gateway:latest
```

### Docker Compose

```yaml
services:
  gateway:
    image: hms-gateway:latest
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: production
      GATEWAY_SERVICES_AUTH: http://auth-server:9000
      GATEWAY_SERVICES_COMMUNICATIONS: http://comm-server:8081
    depends_on:
      - auth-server
      - comm-server
    healthcheck:
      test: ["CMD", "wget", "--no-verbose", "--tries=1", "--spider", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 3s
      retries: 3
```

## Testing

### Run Tests

```bash
# Run all tests
./mvnw test

# Run with coverage
./mvnw test jacoco:report
```

### Integration Tests

The `GatewayRoutingTests` class tests:

- Route configuration loading
- Route resolution for each service
- Request ID header generation
- CORS header presence
- Health endpoint availability

## Troubleshooting

### Common Issues

#### 1. Service Unavailable (503)

**Symptom**: All requests return 503
**Cause**: Downstream service is not running
**Solution**: Start the required service (e.g., auth server on port 9000)

```bash
# Check if service is running
curl http://localhost:9000/auth/actuator/health

# Start the service
cd hms-authorization-server
./mvnw spring-boot:run
```

#### 2. Gateway Timeout (504)

**Symptom**: Requests timeout after 30 seconds
**Cause**: Downstream service is slow or hanging
**Solution**: Investigate the downstream service logs

#### 3. CORS Errors

**Symptom**: Browser blocks requests with CORS error
**Cause**: Origin not in allowed list
**Solution**: Add your origin to `application.yml`:

```yaml
gateway:
  cors:
    allowed-origins:
      - http://your-origin:port
```

#### 4. Route Not Found (404)

**Symptom**: Request returns 404
**Cause**: No route matches the request path
**Solution**:
1. Check route configuration in `application.yml`
2. Verify route predicates match your path
3. View configured routes: `curl http://localhost:8080/actuator/gateway/routes`

### Debug Logging

Enable debug logging to troubleshoot routing issues:

```yaml
logging:
  level:
    io.factorialsystems.gateway: DEBUG
    org.springframework.cloud.gateway: DEBUG
    reactor.netty: DEBUG
```

## Performance Tuning

### JVM Options

The gateway runs with optimized JVM settings for reactive workloads:

```bash
JAVA_OPTS="-XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:+UseZGC \
    -XX:+ZGenerational \
    -Dreactor.netty.ioWorkerCount=4"
```

### Virtual Threads

Virtual threads (Project Loom) are enabled for better concurrency:

```yaml
spring:
  threads:
    virtual:
      enabled: true
```

## Security Considerations

1. **No JWT Validation**: The gateway does NOT validate JWT tokens. Each service must validate tokens independently.

2. **CORS Configuration**: Update CORS allowed origins for production to prevent unauthorized access.

3. **Exposed Headers**: Only expose necessary headers to clients.

4. **HTTPS**: In production, use HTTPS for all communication. Configure TLS termination at load balancer or ingress.

5. **Rate Limiting**: Consider adding rate limiting for production (future enhancement).

## Future Enhancements

- [ ] Service Discovery (Eureka/Consul) - Dynamic service registration
- [ ] Circuit Breaker (Resilience4j) - Fault tolerance
- [ ] Rate Limiting - Per tenant/user rate limiting
- [ ] Request/Response Caching - Cache GET requests
- [ ] API Versioning Support - Handle /v1, /v2 routes
- [ ] Authentication Plugin - Optional gateway-level auth
- [ ] WebSocket Support - For real-time features

## Contributing

When modifying the gateway:

1. Update routes in `application.yml` and `application-production.yml`
2. Update this README with new routes
3. Add integration tests for new routes
4. Update the service port mapping table
5. Follow the existing naming conventions for route IDs

## License

Proprietary - Factorial Systems HMS Platform

## Support

For issues or questions:
- Check the troubleshooting section above
- Review the logs at debug level
- Contact the HMS development team

---

**Last Updated**: 2026-01-23
**Version**: 1.0.0
**Maintainer**: Factorial Systems
