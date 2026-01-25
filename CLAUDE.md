# HMS Platform - Health Management System

## Project Overview

HMS is a **multi-tenant SaaS-based Health Management System** designed to provide comprehensive hospital and healthcare facility management capabilities. The platform uses a **schema-per-tenant** architecture to ensure data isolation and security for each healthcare facility.

## Architecture Philosophy

### Multi-Tenant Strategy
- **Authorization Server**: Single schema for all tenants (centralized authentication & tenant management)
- **Business Services**: Schema-per-tenant model (complete data isolation per healthcare facility)
- **Tenant Isolation**: Each hospital/facility gets its own database schema ensuring HIPAA compliance and data privacy

### Microservices Architecture
The platform follows a microservices architecture with the following services:

#### Current Services
1. **HMS Authorization Server** (✅ Implemented)
   - OAuth 2.0 / OpenID Connect authentication
   - Multi-tenant hospital/facility registration and management
   - User management with RBAC (50+ permissions, 9 system roles)
   - JWT token generation and validation
   - Centralized audit logging
   - Event-driven messaging via RabbitMQ

#### Planned Services
2. **API Gateway** (⏱️ Next Priority)
   - Request routing and load balancing
   - Authentication/authorization enforcement
   - Rate limiting and throttling
   - API composition and aggregation

3. **Patient Service** (⏱️ Next Priority)
   - Patient registration and demographics
   - Patient search and matching
   - Medical history management
   - Family/emergency contacts

## Core Functionality Roadmap

### Phase 1: Foundation (Current Focus)
1. ✅ **Authentication & Authorization** - Completed
   - Multi-tenant user management
   - Role-based access control
   - OAuth 2.0 / JWT tokens

2. **Patient Registration** (Next)
   - Patient demographics
   - Medical history intake
   - Insurance information
   - Consent management

3. **Prescription Management**
   - Electronic prescribing
   - Drug interaction checks
   - Prescription history
   - Refill management

4. **Basic Billing**
   - Service charges
   - Invoice generation
   - Payment processing
   - Insurance claims

5. **User Roles and Access Control**
   - Fine-grained permissions
   - Role templates
   - Custom role creation

### Phase 2: Enhanced Operations
6. **Appointment Scheduling**
   - Calendar management
   - Multi-provider scheduling
   - Appointment reminders
   - Wait list management

7. **Lab Order Management**
   - Lab test ordering
   - Result tracking
   - Reference ranges
   - Result interpretation

8. **Inventory / Pharmacy Stock**
   - Drug inventory tracking
   - Stock alerts
   - Expiry management
   - Reorder automation

9. **Basic Reporting**
   - Operational dashboards
   - Financial reports
   - Clinical metrics
   - Compliance reports

### Phase 3: AI-Powered Features (Future)
10. **Clinical Documentation Assistant**
    - Voice-to-text for doctor notes
    - Clinical note structuring
    - ICD-10 code suggestions

11. **Diagnosis Support**
    - Symptom-based suggestions
    - Differential diagnosis assistance
    - Clinical decision support

12. **Drug Interaction Checker**
    - Real-time interaction alerts
    - Allergy checking
    - Dosage recommendations

13. **Lab Result Interpretation**
    - Automated result flagging
    - Trend analysis
    - Clinical insights

14. **Additional AI Capabilities**
    - Predictive analytics
    - Readmission risk scoring
    - Treatment outcome prediction
    - Medical image analysis

## Technology Stack

### Core Technologies
| Technology | Version | Purpose |
|------------|---------|---------|
| **Java** | 25 | Latest LTS with virtual threads (Project Loom) |
| **Spring Boot** | 3.5.9 | Application framework |
| **Spring Security** | 6.x | Security framework |
| **Spring Authorization Server** | 1.4.3 | OAuth2/OIDC implementation |

### Data Layer
| Technology | Purpose | Usage Strategy |
|------------|---------|----------------|
| **PostgreSQL** | Primary relational database | All services |
| **Spring Data JPA / Hibernate** | ORM and repository abstraction | Default for most services |
| **MyBatis** | Custom SQL mapping | Used selectively when fine-grained SQL control is needed |
| **MongoDB** | Document database | Used selectively for unstructured data (audits, logs, clinical notes) |
| **Flyway** | Database migration and versioning | All services using PostgreSQL |

**Data Access Strategy**: Each service will use the appropriate data access technology based on its specific needs:
- **Auth Server**: Hibernate/JPA (straightforward CRUD operations)
- **Complex Query Services**: MyBatis (when SQL control is critical)
- **Unstructured Data Services**: MongoDB (flexible schema requirements)

### Infrastructure
| Technology | Purpose |
|------------|---------|
| **Redis** | Caching and session management |
| **RabbitMQ** | Asynchronous messaging and events |
| **Docker** | Containerization |
| **Kubernetes** | Orchestration (planned) |

### Development Tools
| Technology | Purpose |
|------------|---------|
| **Maven** | Build and dependency management |
| **Lombok** | Boilerplate reduction |
| **MapStruct** | DTO mapping |
| **Testcontainers** | Integration testing |
| **SpringDoc** | API documentation (OpenAPI 3.0) |

## System Roles

The platform includes 9 predefined system roles:

| Role | Description | Key Permissions |
|------|-------------|-----------------|
| `SUPER_ADMIN` | Platform administrator | Full access across all tenants |
| `TENANT_ADMIN` | Hospital administrator | Full access within tenant |
| `DOCTOR` | Medical doctor | Clinical operations, prescribing |
| `NURSE` | Nursing staff | Patient care, vitals, medications |
| `RECEPTIONIST` | Front desk staff | Patient registration, appointments |
| `CASHIER` | Billing staff | Payment processing, invoicing |
| `LAB_TECHNICIAN` | Laboratory staff | Lab orders, results entry |
| `PHARMACIST` | Pharmacy staff | Prescription dispensing, inventory |
| `RECORDS_OFFICER` | Medical records staff | Record management, archival |

## Security & Compliance

### Authentication & Authorization
- OAuth 2.0 / OpenID Connect
- JWT tokens with RSA signing
- Access tokens: 15 minutes (configurable)
- Refresh tokens: 7 days (configurable)
- PKCE required for public clients

### Password Security
- BCrypt hashing (strength 12)
- Minimum 8 characters
- Requires: uppercase, lowercase, digit, special character
- Password history (last 5 passwords)
- Password expiry: 90 days (configurable)
- Account lockout after 5 failed attempts

### Audit & Compliance
- Comprehensive audit logging
- Authentication event tracking
- HIPAA compliance considerations
- Data encryption at rest and in transit

## Project Structure

```
hms-backend/
├── hms-authorization-server/    # OAuth2 Auth & Tenant Management
├── hms-gateway/                  # API Gateway (planned)
├── hms-patient-service/          # Patient Management (planned)
├── hms-prescription-service/     # Prescription Management (planned)
├── hms-billing-service/          # Billing & Payments (planned)
├── hms-appointment-service/      # Scheduling (planned)
├── hms-lab-service/              # Lab Orders & Results (planned)
├── hms-pharmacy-service/         # Inventory & Dispensing (planned)
├── hms-reporting-service/        # Reports & Analytics (planned)
└── hms-common/                   # Shared libraries (planned)
```

## Authorization Server Details

### Current Implementation
- **Port**: 9000
- **Context Path**: `/auth`
- **Database**: `hms_auth` (PostgreSQL)
- **Virtual Threads**: Enabled (Project Loom)
- **Caching**: Redis with 5-minute TTL
- **Messaging**: RabbitMQ for event-driven communication

### API Endpoints

#### Authentication
- `POST /api/v1/auth/login` - User login
- `POST /api/v1/auth/refresh` - Refresh access token
- `POST /api/v1/auth/logout` - User logout
- `POST /api/v1/auth/change-password` - Change password
- `GET /api/v1/auth/me` - Get current user info

#### Tenant Management
- `POST /api/v1/tenants/register` - Register new hospital/facility
- `GET /api/v1/tenants` - List all tenants (admin only)
- `GET /api/v1/tenants/{id}` - Get tenant details
- `POST /api/v1/tenants/{id}/activate` - Activate tenant
- `POST /api/v1/tenants/{id}/suspend` - Suspend tenant

#### User Management
- `GET /api/v1/users` - List users in tenant
- `POST /api/v1/users` - Create user
- `GET /api/v1/users/{id}` - Get user details
- `PUT /api/v1/users/{id}` - Update user
- `POST /api/v1/users/{id}/deactivate` - Deactivate user

#### Role & Permission Management
- `GET /api/v1/roles` - List all roles
- `POST /api/v1/roles` - Create custom role
- `GET /api/v1/permissions` - List all permissions (50+)

### Database Schema
- **Tenant tables**: Tenant registration, facility details, subscription plans
- **User tables**: Users, password history, user roles
- **Role/Permission tables**: System roles, custom roles, permissions, role-permission mappings
- **OAuth2 tables**: Authorization codes, access tokens, refresh tokens, client registrations
- **Audit tables**: Authentication events, authorization events, tenant activities

## Development Setup

### Prerequisites
- Java 25+
- Maven 3.9+
- Docker & Docker Compose
- PostgreSQL 15+
- Redis 7+
- RabbitMQ 3.12+

### Quick Start
```bash
# Start infrastructure services
docker-compose up -d

# Run authorization server
cd hms-authorization-server
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### Accessing Services
- **Auth Server**: http://localhost:9000/auth
- **Swagger UI**: http://localhost:9000/auth/swagger-ui.html
- **API Docs**: http://localhost:9000/auth/api-docs
- **RabbitMQ Management**: http://localhost:15672 (guest/guest)

## Design Decisions

### Why Schema-Per-Tenant?
1. **Data Isolation**: Complete physical separation of tenant data
2. **Security**: No risk of cross-tenant data leakage
3. **Compliance**: Easier to meet HIPAA and regulatory requirements
4. **Customization**: Each tenant can have schema modifications if needed
5. **Backup/Restore**: Independent backup and restore per tenant

### Why Single Schema for Auth Server?
1. **Centralized Management**: Single source of truth for authentication
2. **Performance**: Faster authentication lookups
3. **Cross-Tenant Operations**: Platform admins can manage multiple tenants
4. **Simplified Token Management**: Single token validation service

### Why Spring Boot + Microservices?
1. **Scalability**: Independent scaling of services
2. **Technology Flexibility**: Different services can use different tech if needed
3. **Team Autonomy**: Different teams can own different services
4. **Fault Isolation**: Service failures don't cascade
5. **Deployment Flexibility**: Independent deployment cycles

### Why PostgreSQL + MongoDB?
1. **PostgreSQL**: Structured clinical data, transactions, ACID compliance
2. **MongoDB**: Unstructured clinical notes, flexible schemas, document storage
3. **Polyglot Persistence**: Right tool for the right data

### Why Java 25 + Virtual Threads?
1. **Virtual Threads**: Better concurrency for I/O-bound operations
2. **Performance**: Handle thousands of concurrent requests efficiently
3. **Simplicity**: Write synchronous-looking code with async performance
4. **Modern JVM**: Latest language features and performance improvements

### Context Path Strategy for Microservices

**Authorization Server Context Path**: The Authorization Server uses `/auth` context path for specific production routing requirements.

**Why Authorization Server Has Context Path `/auth`**:
1. **Production Flexibility**: Enables direct routing to auth server via Nginx/load balancer, bypassing Spring Cloud Gateway when needed
2. **High Availability**: Allows failover scenarios where authentication continues even if API Gateway is unavailable
3. **Direct Authentication Flows**: Some enterprise integrations may require direct OAuth2 flows without gateway intermediation
4. **Service Identification**: Clear URL namespace separation in production logs and monitoring tools

**Example Production Architecture**:
```
Nginx (Port 443)
  ├─ /api/v1/auth/** → Auth Server (9000/auth) [direct, bypasses gateway]
  ├─ /api/v1/** → API Gateway (8080) → Other Services
  └─ Fallback: All through gateway
```

**IMPORTANT: New Services Should NOT Use Context Paths**

When creating new microservices (Patient, Prescription, Billing, etc.):
- ❌ **DO NOT** add `server.servlet.context-path` configuration
- ✅ **DO** use controllers with `/api/v1/{resource}` mappings directly
- ✅ **DO** rely on API Gateway for all routing

**Example - Correct New Service Configuration**:
```yaml
# patient-service/application.yml
server:
  port: 8082
  # NO context-path configuration

# Controller
@RestController
@RequestMapping("/api/v1/patients")  # Direct mapping, no context prefix
public class PatientController {
    // Endpoints accessed via gateway: http://gateway:8080/api/v1/patients
}
```

**Why No Context Paths for New Services**:
1. **Simplicity**: Cleaner URLs and routing configuration
2. **Gateway-First**: All services should be accessed through API Gateway in production
3. **Consistent Architecture**: Single point of entry simplifies security, monitoring, and rate limiting
4. **Microservices Best Practice**: Services shouldn't need to know their deployment context
5. **Easier Development**: Local URLs match gateway URLs (no path rewriting complexity)

**When to Consider Context Paths** (rare exceptions):
- Service requires direct production access (like auth server)
- Legacy system integration constraints
- Regulatory requirement for isolated authentication endpoint
- Explicitly discussed and approved by architecture team

## Testing Strategy

### Testing Layers
1. **Unit Tests**: Business logic testing
2. **Integration Tests**: Database and external service integration (Testcontainers)
3. **API Tests**: REST endpoint testing
4. **Security Tests**: Authentication and authorization testing
5. **Performance Tests**: Load and stress testing (planned)

### Testing Tools
- JUnit 5
- Mockito
- Spring Boot Test
- Testcontainers (PostgreSQL, Redis, RabbitMQ)
- REST Assured (API testing)

## Deployment Strategy (Planned)

### Environments
1. **Development**: Local Docker Compose
2. **Staging**: Kubernetes cluster
3. **Production**: Kubernetes cluster with HA

### CI/CD Pipeline
1. Build and test (Maven)
2. Security scanning
3. Docker image creation
4. Push to container registry
5. Deploy to Kubernetes
6. Health checks and smoke tests

## Monitoring & Observability (Planned)

1. **Logging**: ELK Stack (Elasticsearch, Logstash, Kibana)
2. **Metrics**: Prometheus + Grafana
3. **Tracing**: Zipkin or Jaeger
4. **Health Checks**: Spring Actuator endpoints
5. **Alerting**: PagerDuty or similar

## Future Considerations

### Scalability
- Database sharding for large tenants
- Read replicas for reporting
- CDN for static assets
- Multi-region deployment

### AI/ML Integration
- Model serving infrastructure
- Real-time inference endpoints
- Model versioning and A/B testing
- Feedback loops for model improvement

### Interoperability
- HL7 FHIR API support
- Integration with external labs
- Integration with pharmacies
- Integration with insurance providers

## Development Notes

### Current Status
- ✅ Authorization Server fully implemented
- ✅ Multi-tenant user management working
- ✅ OAuth 2.0 / JWT authentication working
- ✅ RBAC with 50+ permissions and 9 system roles
- ✅ Audit logging implemented
- ✅ Event-driven messaging with RabbitMQ

### Next Steps
1. ~~Implement API Gateway~~ ✅ Completed
2. Implement Patient Service with schema-per-tenant
   - **IMPORTANT**: Do NOT add `server.servlet.context-path` - see "Context Path Strategy" in Design Decisions
3. ~~Add Docker Compose for all infrastructure services~~ ✅ Completed
4. Set up CI/CD pipeline
5. Add comprehensive integration tests

### Important Reminders for New Services
- ❌ **DO NOT** add context paths (`server.servlet.context-path`) to new services
- ✅ **DO** use direct `/api/v1/{resource}` controller mappings
- ✅ **DO** access all services via Gateway (port 8080) in production
- ✅ **DO** refer to "Context Path Strategy for Microservices" section in Design Decisions

### Known Limitations
- ~~Docker Compose file not yet created~~ ✅ Completed
- No service discovery yet (will add Eureka or Consul when needed)
- No distributed tracing yet (Zipkin/Jaeger planned)
- No centralized logging yet (ELK Stack planned)

## Contributing

### Code Style
- Follow Spring Boot best practices
- Use Lombok for boilerplate reduction
  - **IMPORTANT**: Do NOT use `@Data` annotation - it's not safe (exposes sensitive data in toString, etc.)
  - Use individual annotations: `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder` as needed
  - Only include the specific annotations required for each class
- Use MapStruct for DTO mapping
- Write meaningful commit messages
- Add JavaDoc for public APIs

### Branch Strategy
- `master` - production-ready code
- `develop` - integration branch
- Feature branches: `feature/feature-name`
- Bug fixes: `bugfix/bug-name`

## License

Proprietary - Factorial Systems HMS Platform

## Contact

For questions or support, contact the development team at Factorial Systems.

---

**Last Updated**: 2026-01-19
**Document Version**: 1.0