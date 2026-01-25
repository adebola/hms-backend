# HMS - Health Management System Platform

> A comprehensive multi-tenant SaaS platform for healthcare facility management

[![Java](https://img.shields.io/badge/Java-25-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.9-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Angular](https://img.shields.io/badge/Angular-19.1.0-red.svg)](https://angular.io/)
[![License](https://img.shields.io/badge/License-Proprietary-blue.svg)](LICENSE)

## 📋 Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Microservices](#microservices)
- [Technology Stack](#technology-stack)
- [Key Features](#key-features)
- [Getting Started](#getting-started)
- [Project Structure](#project-structure)
- [Development](#development)
- [Deployment](#deployment)
- [API Documentation](#api-documentation)
- [Contributing](#contributing)
- [License](#license)

## 🏥 Overview

HMS (Health Management System) is a modern, cloud-native healthcare management platform built with a microservices architecture. It provides comprehensive tools for hospitals, clinics, and healthcare facilities to manage their operations efficiently.

### What Makes HMS Special?

- **Multi-Tenant SaaS**: Each healthcare facility operates in complete isolation with custom branding
- **Schema-Per-Tenant**: Physical data separation ensuring HIPAA compliance and security
- **Microservices Architecture**: Independently scalable services for optimal performance
- **Modern Stack**: Built with Java 25, Spring Boot 3.5, and Angular 19
- **Event-Driven**: Real-time communication using RabbitMQ
- **API-First**: RESTful APIs with comprehensive OpenAPI documentation

## 🏗️ Architecture

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         Client Layer                            │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐           │
│  │   Web App    │  │  Mobile App  │  │  Third Party │           │
│  │  (Angular)   │  │   (Future)   │  │     APIs     │           │
│  └──────────────┘  └──────────────┘  └──────────────┘           │
└─────────────────────────────┬───────────────────────────────────┘
                              │ HTTPS
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                      API Gateway Layer                          │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │  Spring Cloud Gateway (Port 8080)                         │  │
│  │  • Request Routing     • Rate Limiting                    │  │
│  │  • Load Balancing      • CORS Handling                    │  │
│  │  • Request Tracking    • Path Rewriting                   │  │
│  └───────────────────────────────────────────────────────────┘  │
└─────────────────────────────┬───────────────────────────────────┘
                              │
              ┌───────────────┼───────────────┐
              ▼               ▼               ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Microservices Layer                          │
│                                                                 │
│  ┌─────────────────┐  ┌─────────────────┐  ┌────────────────┐   │
│  │ Auth Server     │  │ Communications  │  │ Patient Svc    │   │
│  │ (Port 9000)     │  │ (Port 8081)     │  │ (Planned)      │   │
│  │                 │  │                 │  │                │   │
│  │ • OAuth2/OIDC   │  │ • Email (Brevo) │  │ • Registration │   │ 
│  │ • JWT Tokens    │  │ • SMS           │  │ • Demographics │   │
│  │ • User Mgmt     │  │ • Notifications │  │ • History      │   │
│  │ • RBAC (50+)    │  │ • Rate Limiting │  │ • Search       │   │
│  │ • Tenant Mgmt   │  │ • Templates     │  │                │   │
│  └─────────────────┘  └─────────────────┘  └────────────────┘   │
│                                                                 │
│  ┌─────────────────┐  ┌─────────────────┐  ┌────────────────┐   │
│  │ Prescription    │  │ Billing Service │  │ Appointment    │   │
│  │ (Planned)       │  │ (Planned)       │  │ (Planned)      │   │
│  └─────────────────┘  └─────────────────┘  └────────────────┘   │
└─────────────────────────────┬───────────────────────────────────┘
                              │
              ┌───────────────┼───────────────┐
              ▼               ▼               ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Infrastructure Layer                         │
│                                                                 │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐              │
│  │ PostgreSQL  │  │   Redis     │  │  RabbitMQ   │              │
│  │ • Auth DB   │  │ • Caching   │  │ • Events    │              │
│  │ • Tenant DBs│  │ • Sessions  │  │ • Messages  │              │
│  └─────────────┘  └─────────────┘  └─────────────┘              │
└─────────────────────────────────────────────────────────────────┘
```

### Multi-Tenant Strategy

**Authorization Server**: Single schema for all tenants (centralized authentication)
```
hms_auth [Single Database]
  ├── tenants (all hospitals)
  ├── users (all users across tenants)
  ├── roles & permissions
  └── oauth2 tokens
```

**Business Services**: Schema-per-tenant (complete data isolation)
```
hms_patient [Multiple Schemas]
  ├── hospital_a_patients
  ├── hospital_b_patients
  └── hospital_c_patients
```

## 🚀 Microservices

### 1. HMS Authorization Server ✅ (Implemented)

**Purpose**: Centralized authentication, authorization, and tenant management

**Port**: 9000
**Context Path**: `/auth`
**Technology**: Spring Boot 3.5, Spring Security, Spring Authorization Server

> **Why Context Path?** The `/auth` context path enables production architectures where traffic can be routed directly to the Authorization Server via Nginx/load balancer, bypassing the Spring Cloud Gateway if needed. This provides flexibility for high-availability scenarios, direct authentication flows, or when the API Gateway is unavailable. **Note**: New services should NOT use context paths unless similar production routing requirements exist.

**Features**:
- OAuth 2.0 / OpenID Connect authentication
- JWT token generation and validation (HMAC-SHA256)
- Multi-tenant user management
- Role-Based Access Control (RBAC)
  - 9 system roles (SUPER_ADMIN, TENANT_ADMIN, DOCTOR, NURSE, etc.)
  - 50+ granular permissions
- Hospital/facility registration and management
- Comprehensive audit logging
- Event publishing via RabbitMQ

**API Endpoints** (via Gateway at port 8080):
- `POST /api/v1/auth/login` - User authentication
- `POST /api/v1/auth/refresh` - Token refresh
- `POST /api/v1/auth/logout` - User logout
- `GET /api/v1/auth/me` - Current user info
- `POST /api/v1/tenants/register` - Tenant registration
- `GET /api/v1/users` - User management

**Routing**:
- **Via Gateway** (recommended): `http://localhost:8080/api/v1/auth/login`
- **Direct Access** (debugging): `http://localhost:9000/auth/api/v1/auth/login`
- Gateway automatically rewrites paths: `/api/v1/auth/**` → `/auth/api/v1/auth/**`

**Database**: PostgreSQL (single schema `hms_auth`)

**Documentation**: [hms-authorization-server/README.md](hms-authorization-server/README.md)

---

### 2. HMS Communications Server ✅ (Implemented)

**Purpose**: Unified communication service for email, SMS, and notifications

**Port**: 8081
**Context Path**: `/` (no context path)
**Technology**: Spring Boot 3.5, Brevo API (Email), RabbitMQ

**Features**:
- Email delivery via Brevo (formerly Sendinblue)
- SMS messaging support
- Template-based messaging
- Rate limiting per tenant
- Delivery tracking and logging
- Asynchronous message processing
- Multi-tenant configuration

**API Endpoints** (via Gateway at port 8080):
- `POST /api/v1/email/send` - Send email
- `POST /api/v1/sms/send` - Send SMS
- `GET /api/v1/email/{id}` - Email delivery status
- `GET /api/v1/sms/{id}` - SMS delivery status

**Routing**:
- **Via Gateway** (recommended): `http://localhost:8080/api/v1/email/send`
- **Direct Access** (debugging): `http://localhost:8081/api/v1/email/send`
- Gateway routes `/api/v1/email/**` and `/api/v1/sms/**` directly (no path rewriting needed)

**Database**: PostgreSQL (tenant-specific schemas)

**Message Queue**: RabbitMQ consumers for async processing

**Documentation**: [hms-communications-server/README.md](hms-communications-server/README.md)

---

### 3. HMS Gateway ✅ (Implemented)

**Purpose**: API Gateway for request routing, load balancing, and cross-cutting concerns

**Port**: 8080
**Technology**: Spring Cloud Gateway, Spring WebFlux

**Features**:
- Dynamic request routing to microservices
- Path rewriting for service context paths
- CORS configuration
- Request ID generation for tracing
- Load balancing
- Centralized logging
- Rate limiting (planned)
- Circuit breaker (planned)

**Routes**:
- `/api/v1/auth/**` → Authorization Server (9000)
- `/api/v1/email/**` → Communications Server (8081)
- `/api/v1/sms/**` → Communications Server (8081)
- Future routes for other services

**Documentation**: [hms-gateway/README.md](hms-gateway/README.md)

---

### 4. HMS Frontend ✅ (Implemented)

**Purpose**: Multi-tenant web application for healthcare facility management

**Port**: 4200 (development)
**Technology**: Angular 19, TypeScript 5.6, RxJS, SCSS

**Features**:
- Responsive, modern UI
- JWT-based authentication with auto-refresh
- Multi-tenant branding (logos, colors, themes)
- Role-based UI access control
- Standalone components (no modules)
- Lazy loading for optimal performance

**Key Pages**:
- Login with tenant selection
- Dashboard with user info
- More features coming soon...

**Documentation**: [hms-frontend/README.md](hms-frontend/README.md)

---

### 5. HMS Patient Service ⏱️ (Planned)

**Purpose**: Patient registration, demographics, and medical history management

**Planned Features**:
- Patient registration and search
- Medical history management
- Family/emergency contacts
- Consent management
- Patient matching algorithms

---

### 6. HMS Prescription Service ⏱️ (Planned)

**Purpose**: Electronic prescribing and medication management

**Planned Features**:
- Electronic prescribing
- Drug interaction checks
- Prescription history
- Refill management
- Integration with pharmacy systems

---

### 7. HMS Billing Service ⏱️ (Planned)

**Purpose**: Billing, invoicing, and payment processing

**Planned Features**:
- Service charges and invoicing
- Payment processing
- Insurance claims management
- Financial reporting

---

### 8. HMS Appointment Service ⏱️ (Planned)

**Purpose**: Appointment scheduling and calendar management

**Planned Features**:
- Multi-provider scheduling
- Calendar management
- Appointment reminders (via Communications Service)
- Wait list management
- Resource allocation

## 💻 Technology Stack

### Backend

| Technology | Version | Purpose |
|------------|---------|---------|
| **Java** | 25 | Core programming language with virtual threads |
| **Spring Boot** | 3.5.9 | Application framework |
| **Spring Cloud Gateway** | 2024.0.1 | API gateway |
| **Spring Security** | 6.x | Security framework |
| **Spring Authorization Server** | 1.4.3 | OAuth2/OIDC provider |
| **PostgreSQL** | 15+ | Primary database |
| **Flyway** | Latest | Database migrations |
| **Redis** | 7+ | Caching layer |
| **RabbitMQ** | 3.12+ | Message broker |
| **Maven** | 3.9+ | Build tool |
| **Lombok** | Latest | Boilerplate reduction |
| **MapStruct** | Latest | DTO mapping |
| **SpringDoc** | Latest | OpenAPI/Swagger documentation |

### Frontend

| Technology | Version | Purpose |
|------------|---------|---------|
| **Angular** | 19.1.0 | Frontend framework |
| **TypeScript** | 5.6.2 | Type-safe JavaScript |
| **RxJS** | 7.8.1 | Reactive programming |
| **SCSS** | - | Styling |

### Infrastructure

| Technology | Purpose |
|------------|---------|
| **Docker** | Containerization |
| **Docker Compose** | Local development orchestration |
| **Kubernetes** | Production orchestration (planned) |
| **Nginx** | Reverse proxy / Load balancer (production) |

## ✨ Key Features

### Security & Compliance
- ✅ OAuth 2.0 / OpenID Connect authentication
- ✅ JWT tokens with automatic refresh
- ✅ Role-Based Access Control (RBAC) with 50+ permissions
- ✅ BCrypt password hashing (strength 12)
- ✅ Password policy enforcement
- ✅ Account lockout after failed attempts
- ✅ Comprehensive audit logging
- ✅ HIPAA compliance considerations
- ✅ Data encryption at rest and in transit

### Multi-Tenancy
- ✅ Complete tenant isolation (schema-per-tenant)
- ✅ Custom branding per healthcare facility
- ✅ Tenant-specific configurations
- ✅ Subscription plan management
- ✅ Independent tenant activation/suspension

### Communication
- ✅ Email delivery via Brevo
- ✅ SMS messaging support
- ✅ Template-based messaging
- ✅ Asynchronous processing
- ✅ Delivery tracking

### Developer Experience
- ✅ RESTful APIs with OpenAPI documentation
- ✅ Comprehensive Swagger UI
- ✅ Event-driven architecture
- ✅ Testcontainers for integration testing
- ✅ Docker support for local development

## 🚦 Getting Started

### Prerequisites

- **Java 25** or higher
- **Node.js 18.19** or higher (for frontend)
- **Docker & Docker Compose**
- **Maven 3.9** or higher
- **PostgreSQL 15** or higher
- **Redis 7** or higher
- **RabbitMQ 3.12** or higher

### Quick Start with Docker Compose (Recommended)

1. **Clone the repository**:
   ```bash
   git clone https://github.com/factorialsystems/hms-backend.git
   cd hms-backend
   ```

2. **Start infrastructure services**:
   ```bash
   docker-compose up -d postgres redis rabbitmq
   ```

3. **Start backend services**:
   ```bash
   # Terminal 1 - Gateway
   cd hms-gateway
   ./mvnw spring-boot:run

   # Terminal 2 - Auth Server
   cd hms-authorization-server
   ./mvnw spring-boot:run

   # Terminal 3 - Communications Server
   cd hms-communications-server
   ./mvnw spring-boot:run
   ```

4. **Start frontend** (optional):
   ```bash
   cd hms-frontend
   npm install
   npm start
   ```

5. **Access the application**:
   - **Frontend Application**: http://localhost:4200
   - **API Gateway** (all API requests): http://localhost:8080
   - **Auth Server Direct** (debugging only): http://localhost:9000/auth
   - **API Documentation**:
     - Auth Server Swagger: http://localhost:9000/auth/swagger-ui.html
     - Communications Swagger: http://localhost:8081/swagger-ui.html

   **Important**: Client applications (frontend, mobile) should ALWAYS use the Gateway (port 8080), not direct service URLs.

### Default Login Credentials

After initial setup, create a test user or use:
- **Tenant Code**: `DEFAULT`
- **Username**: `admin` (configure in database)
- **Password**: (set during tenant registration)

## 📁 Project Structure

```
hms-backend/
├── hms-authorization-server/      # OAuth2 auth & tenant management
│   ├── src/main/java/             # Java source code
│   ├── src/main/resources/        # Application configs
│   ├── src/test/                  # Tests
│   ├── pom.xml                    # Maven dependencies
│   └── README.md                  # Service documentation
│
├── hms-communications-server/     # Email & SMS service
│   ├── src/main/java/
│   ├── src/main/resources/
│   ├── pom.xml
│   └── README.md
│
├── hms-gateway/                   # API Gateway
│   ├── src/main/java/
│   ├── src/main/resources/
│   ├── pom.xml
│   └── README.md
│
├── hms-frontend/                  # Angular web application
│   ├── src/app/                   # Application source
│   ├── src/environments/          # Environment configs
│   ├── package.json               # NPM dependencies
│   └── README.md                  # Frontend documentation
│
├── hms-patient-service/           # (Planned) Patient management
├── hms-prescription-service/      # (Planned) Prescription management
├── hms-billing-service/           # (Planned) Billing & payments
├── hms-appointment-service/       # (Planned) Appointment scheduling
│
├── docker-compose.yml             # Infrastructure services
├── CLAUDE.md                      # Detailed project documentation
└── README.md                      # This file
```

## 🛠️ Development

### Building the Services

**Build all services**:
```bash
# Backend services
cd hms-authorization-server && ./mvnw clean package
cd hms-communications-server && ./mvnw clean package
cd hms-gateway && ./mvnw clean package

# Frontend
cd hms-frontend && npm run build
```

**Run tests**:
```bash
./mvnw test
```

**Run with specific profile**:
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### Environment Profiles

Each service supports multiple profiles:
- **default**: Local development
- **dev**: Development environment
- **production**: Production environment

Configure via `application.yml` and `application-{profile}.yml`

### Database Migrations

Flyway handles database migrations automatically on startup:

```
src/main/resources/db/migration/
├── V1__create_tenant_tables.sql
├── V2__create_user_tables.sql
├── V3__create_role_permission_tables.sql
└── ...
```

### API Documentation

Each service exposes OpenAPI documentation:
- **Auth Server**: http://localhost:9000/auth/swagger-ui.html
- **Communications**: http://localhost:8081/comms/swagger-ui.html
- **Gateway**: Routes to individual services

## 🚢 Deployment

### Docker Deployment

Build Docker images:
```bash
docker build -t hms-authorization-server:latest ./hms-authorization-server
docker build -t hms-communications-server:latest ./hms-communications-server
docker build -t hms-gateway:latest ./hms-gateway
```

### Kubernetes Deployment (Planned)

Helm charts and Kubernetes manifests coming soon.

### Environment Variables

Key environment variables for production:

**Auth Server**:
```env
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/hms_auth
SPRING_DATASOURCE_USERNAME=hms_user
SPRING_DATASOURCE_PASSWORD=<secure-password>
JWT_SECRET_KEY=<256-bit-secret>
REDIS_HOST=redis-host
RABBITMQ_HOST=rabbitmq-host
```

**Communications Server**:
```env
BREVO_API_KEY=<your-brevo-api-key>
RABBITMQ_HOST=rabbitmq-host
```

## 📚 API Documentation

### API Access Patterns

**Important**: All client applications should access APIs through the Gateway, not directly to services.

**Gateway Base URL**: `http://localhost:8080` (development) or `https://gateway.yourdomain.com` (production)

### Swagger/OpenAPI

Interactive API documentation available at:
- **Auth Server**: http://localhost:9000/auth/swagger-ui.html
- **Communications**: http://localhost:8081/swagger-ui.html

*Note: Swagger UI shows direct service endpoints. When calling from client apps, use Gateway URLs (see examples below).*

### Key API Endpoints

All endpoints below are accessed **via Gateway** at `http://localhost:8080`:

**Authentication**:
```
POST   /api/v1/auth/login           - User login
POST   /api/v1/auth/refresh         - Refresh access token
POST   /api/v1/auth/logout          - User logout
GET    /api/v1/auth/me              - Get current user
```

**Tenant Management**:
```
POST   /api/v1/tenants/register     - Register new tenant
GET    /api/v1/tenants              - List tenants
GET    /api/v1/tenants/{id}         - Get tenant details
POST   /api/v1/tenants/{id}/activate - Activate tenant
```

**User Management**:
```
GET    /api/v1/users                - List users
POST   /api/v1/users                - Create user
GET    /api/v1/users/{id}           - Get user details
PUT    /api/v1/users/{id}           - Update user
```

**Communications**:
```
POST   /api/v1/email/send           - Send email
POST   /api/v1/sms/send             - Send SMS
GET    /api/v1/email/{id}           - Email status
```

### API Request Examples

**Login via Gateway** (recommended):
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"tenantCode":"DEFAULT","username":"admin","password":"password"}'
```

**Direct to Auth Server** (debugging only):
```bash
curl -X POST http://localhost:9000/auth/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"tenantCode":"DEFAULT","username":"admin","password":"password"}'
```

**How Gateway Routing Works**:
1. Client sends request to Gateway: `http://localhost:8080/api/v1/auth/login`
2. Gateway matches route: `/api/v1/auth/**`
3. Gateway rewrites path: `/api/v1/auth/login` → `/auth/api/v1/auth/login`
4. Gateway forwards to Auth Server: `http://localhost:9000/auth/api/v1/auth/login`
5. Auth Server processes request and returns response
6. Gateway returns response to client

## 🧪 Testing

### Running Tests

```bash
# Unit tests
./mvnw test

# Integration tests (uses Testcontainers)
./mvnw verify

# Frontend tests
cd hms-frontend
npm test
```

### Test Coverage

Tests include:
- Unit tests for business logic
- Integration tests with Testcontainers
- API endpoint tests
- Security tests
- Authentication flow tests

## 🗺️ Roadmap

### Phase 1: Foundation ✅ (Current)
- [x] Authorization Server
- [x] Communications Server
- [x] API Gateway
- [x] Frontend Application
- [x] Multi-tenant user management
- [x] OAuth 2.0 / JWT authentication

### Phase 2: Core Features (Next)
- [ ] Patient Service
- [ ] Prescription Service
- [ ] Basic Billing
- [ ] Service Discovery (Eureka/Consul)
- [ ] Centralized Logging (ELK Stack)
- [ ] Distributed Tracing (Zipkin)

### Phase 3: Enhanced Operations
- [ ] Appointment Scheduling
- [ ] Lab Order Management
- [ ] Inventory/Pharmacy
- [ ] Reporting Service
- [ ] Mobile application

### Phase 4: AI-Powered Features (Future)
- [ ] Clinical Documentation Assistant
- [ ] Diagnosis Support
- [ ] Drug Interaction Checker
- [ ] Lab Result Interpretation
- [ ] Predictive Analytics

## 🤝 Contributing

We welcome contributions! Please follow these guidelines:

1. **Fork the repository**
2. **Create a feature branch**: `git checkout -b feature/amazing-feature`
3. **Commit your changes**: `git commit -m 'Add amazing feature'`
4. **Push to the branch**: `git push origin feature/amazing-feature`
5. **Open a Pull Request**

### Code Style

- Follow Spring Boot best practices
- Use Lombok for boilerplate reduction (avoid `@Data`)
- Write meaningful commit messages
- Add JavaDoc for public APIs
- Include tests for new features

### Branch Strategy

- `master` - Production-ready code
- `develop` - Integration branch
- `feature/*` - Feature branches
- `bugfix/*` - Bug fix branches

## 📄 License

Proprietary - Factorial Systems HMS Platform

Copyright © 2026 Factorial Systems. All rights reserved.

## 📞 Contact & Support

For questions, issues, or support:

- **Email**: support@factorialsystems.io
- **Documentation**: [CLAUDE.md](CLAUDE.md)
- **Issues**: GitHub Issues

## 🙏 Acknowledgments

Built with:
- [Spring Boot](https://spring.io/projects/spring-boot)
- [Angular](https://angular.io/)
- [PostgreSQL](https://www.postgresql.org/)
- [RabbitMQ](https://www.rabbitmq.com/)
- [Redis](https://redis.io/)
- [Brevo](https://www.brevo.com/)

## 📊 Project Status

| Service | Status | Version | Documentation |
|---------|--------|---------|---------------|
| Authorization Server | ✅ Production Ready | 1.0.0 | [README](hms-authorization-server/README.md) |
| Communications Server | ✅ Production Ready | 1.0.0 | [README](hms-communications-server/README.md) |
| API Gateway | ✅ Production Ready | 1.0.0 | [README](hms-gateway/README.md) |
| Frontend | ✅ Production Ready | 1.0.0 | [README](hms-frontend/README.md) |
| Patient Service | ⏱️ Planned | - | - |
| Prescription Service | ⏱️ Planned | - | - |
| Billing Service | ⏱️ Planned | - | - |
| Appointment Service | ⏱️ Planned | - | - |

---

**Built with ❤️ by Factorial Systems**

*Last Updated: 2026-01-24*
