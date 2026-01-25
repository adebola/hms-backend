# HMS Auth Server

Authorization Server and Tenant Management for the HMS (Hospital Management System) Platform.

## Overview

The Auth Server provides:
- **OAuth 2.0 / OpenID Connect** authentication and authorization
- **Multi-tenant** hospital/facility management
- **User Management** with role-based access control (RBAC)
- **JWT Token** generation and validation

## Technology Stack

| Technology | Version | Notes |
|------------|---------|-------|
| **Java** | 25 (LTS) | Latest LTS with virtual threads |
| **Spring Boot** | 3.5.9 | Stable release |
| **Spring Security** | 6.x | OAuth2 Authorization Server |
| **Spring Authorization Server** | 1.4.3 | OAuth2/OIDC |
| **Hibernate ORM** | 6.x | JPA support |
| **PostgreSQL** | 15+ | Primary database |
| **Flyway** | Latest | Database migrations |
| **Redis** | 7+ | Caching and token management |
| **RabbitMQ** | 3.12+ | Async event messaging |

## Features

- **Virtual Threads** (Project Loom) enabled for better concurrency
- **Multi-tenant Architecture** with tenant isolation
- **RBAC** with 50+ permissions and 9 system roles
- **Password Security** with BCrypt, history validation, expiry
- **Account Lockout** after failed login attempts
- **Audit Logging** for all authentication events
- **Event-driven Messaging** via RabbitMQ

## Prerequisites

- Java 25+
- Maven 3.9+
- Docker & Docker Compose (for local development)

## Quick Start

### 1. Start Dependencies

```bash
docker-compose up -d
```

This starts:
- PostgreSQL on port 5432
- Redis on port 6379
- RabbitMQ on ports 5672 (AMQP) and 15672 (Management UI)
- MailHog on ports 1025 (SMTP) and 8025 (Web UI)

### 2. Run the Application

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

The server will start on `http://localhost:9000`

### 3. Access APIs

- **Swagger UI**: http://localhost:9000/swagger-ui.html
- **API Docs**: http://localhost:9000/api-docs
- **RabbitMQ UI**: http://localhost:15672 (guest/guest)
- **MailHog UI**: http://localhost:8025

## Project Structure

```
src/main/java/io/factorialsystems/auth/
├── config/                 # Configuration classes
├── controller/             # REST API controllers
├── service/                # Business logic
├── repository/             # Data access
├── model/
│   ├── entity/            # JPA entities
│   ├── dto/               # Request/Response DTOs
│   └── enums/             # Enumerations
├── security/              # JWT and security components
└── exception/             # Exception handling

src/main/resources/
├── application.yml        # Main configuration
├── application-dev.yml    # Development profile
├── application-prod.yml   # Production profile
└── db/migration/          # Flyway SQL migrations
```

## API Endpoints

### Authentication

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/auth/login` | User login |
| POST | `/api/v1/auth/refresh` | Refresh token |
| POST | `/api/v1/auth/logout` | Logout |
| POST | `/api/v1/auth/change-password` | Change password |
| GET | `/api/v1/auth/me` | Get current user |

### Tenant Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/tenants/register` | Register new tenant |
| GET | `/api/v1/tenants` | List tenants (admin) |
| GET | `/api/v1/tenants/{id}` | Get tenant by ID |
| POST | `/api/v1/tenants/{id}/activate` | Activate tenant |
| POST | `/api/v1/tenants/{id}/suspend` | Suspend tenant |

### User Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/users` | List users in tenant |
| POST | `/api/v1/users` | Create user |
| GET | `/api/v1/users/{id}` | Get user by ID |
| PUT | `/api/v1/users/{id}` | Update user |
| POST | `/api/v1/users/{id}/deactivate` | Deactivate user |

### Role & Permission Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/roles` | List roles |
| POST | `/api/v1/roles` | Create custom role |
| GET | `/api/v1/permissions` | List all permissions |

## System Roles

| Role | Description |
|------|-------------|
| `SUPER_ADMIN` | Platform administrator (full access) |
| `TENANT_ADMIN` | Hospital administrator |
| `DOCTOR` | Medical doctor |
| `NURSE` | Nursing staff |
| `RECEPTIONIST` | Front desk staff |
| `CASHIER` | Billing staff |
| `LAB_TECHNICIAN` | Laboratory staff |
| `PHARMACIST` | Pharmacy staff |
| `RECORDS_OFFICER` | Medical records staff |

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SERVER_PORT` | Server port | 9000 |
| `DATABASE_URL` | PostgreSQL connection URL | - |
| `DATABASE_USERNAME` | Database username | - |
| `DATABASE_PASSWORD` | Database password | - |
| `REDIS_HOST` | Redis host | localhost |
| `REDIS_PORT` | Redis port | 6379 |
| `RABBITMQ_HOST` | RabbitMQ host | localhost |
| `RABBITMQ_PORT` | RabbitMQ port | 5672 |
| `JWT_ACCESS_TOKEN_VALIDITY` | Access token validity (minutes) | 15 |
| `JWT_REFRESH_TOKEN_VALIDITY` | Refresh token validity (days) | 7 |

## Running Tests

```bash
# Run all tests
./mvnw test

# Run with coverage
./mvnw test jacoco:report
```

## Building for Production

```bash
# Build JAR
./mvnw clean package -DskipTests

# Build Docker image
docker build -t hms-auth-server:latest .
```

## Security Features

- **BCrypt** password hashing (strength 12)
- **JWT** tokens with RSA signing
- **PKCE** required for public clients
- **Password history** prevents reuse of last 5 passwords
- **Account lockout** after 5 failed login attempts
- **Password expiry** after 90 days (configurable)

## License

Proprietary - HMS Platform

## Contact

For questions or support, contact the development team.
