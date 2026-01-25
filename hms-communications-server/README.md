# HMS Communications Server

Email and SMS communications microservice for the HMS (Health Management System) platform. Provides centralized messaging capabilities with multi-tenant isolation, rate limiting, and integration with third-party providers (Brevo for email, Twilio stub for SMS).

## Features

- **Email Sending**: Transactional email via Brevo API
- **SMS Sending**: Stub implementation (Twilio integration ready)
- **Multi-Tenant Isolation**: Complete data separation per healthcare facility
- **Rate Limiting**: Per-tenant daily limits for emails and SMS
- **RabbitMQ Integration**: Async message processing from other microservices
- **JWT Authentication**: Secure API access with token validation
- **Delivery Tracking**: Comprehensive logging of delivery events
- **Attachment Support**: Email attachments with configurable limits
- **Retry Logic**: Automatic retry with exponential backoff
- **REST API**: Full CRUD operations for message management
- **Swagger Documentation**: Interactive API documentation

## Technology Stack

- **Java 25** with virtual threads (Project Loom)
- **Spring Boot 3.5.9**
- **PostgreSQL** with Flyway migrations
- **Redis** for caching
- **RabbitMQ** for async messaging
- **Brevo API** (SendinBlue) for email delivery
- **JWT** for authentication
- **MapStruct** for DTO mapping
- **Lombok** for boilerplate reduction
- **Docker** for containerization

## Prerequisites

- Java 25+
- Maven 3.9+
- PostgreSQL 15+
- Redis 7+
- RabbitMQ 3.12+
- Brevo account with API key

## Quick Start

### 1. Database Setup

```bash
# Create database
psql -U postgres -c "CREATE DATABASE hms_communications;"

# Run migrations (automatic on startup)
./mvnw flyway:migrate
```

### 2. Configuration

Create `.env` file or set environment variables:

```bash
# Database
DATABASE_URL=jdbc:postgresql://localhost:5432/hms_communications
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=postgres

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# RabbitMQ
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=user
RABBITMQ_PASSWORD=password

# Brevo API
BREVO_API_KEY=your-brevo-api-key-here
BREVO_FROM_EMAIL=noreply@hms-platform.com
BREVO_FROM_NAME=HMS Platform

# JWT (must match auth-server)
JWT_SECRET=your-jwt-secret-shared-with-auth-server
```

### 3. Run Application

```bash
# Using Maven
./mvnw spring-boot:run

# Or build and run JAR
./mvnw clean package
java -jar target/hms-communications-server-1.0.0-SNAPSHOT.jar
```

Application will start on **http://localhost:9001/communications**

### 4. Access Swagger UI

http://localhost:9001/communications/swagger-ui.html

## Docker Support

### Build Image

```bash
docker build -t hms-communications-server:latest .
```

### Run Container

```bash
docker run -p 9001:9001 \
  -e DATABASE_URL=jdbc:postgresql://host.docker.internal:5432/hms_communications \
  -e DATABASE_USERNAME=postgres \
  -e DATABASE_PASSWORD=postgres \
  -e REDIS_HOST=host.docker.internal \
  -e RABBITMQ_HOST=host.docker.internal \
  -e BREVO_API_KEY=your-api-key \
  -e JWT_SECRET=your-jwt-secret \
  hms-communications-server:latest
```

## API Endpoints

### Email APIs

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/email/send` | Send email |
| GET | `/api/v1/email/messages` | List emails (paginated) |
| GET | `/api/v1/email/messages/{id}` | Get email by ID |
| POST | `/api/v1/email/messages/{id}/retry` | Retry failed email |

### SMS APIs

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/sms/send` | Send SMS (stub) |
| GET | `/api/v1/sms/messages` | List SMS (paginated) |
| GET | `/api/v1/sms/messages/{id}` | Get SMS by ID |

### Health Check

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/actuator/health` | Application health status |

## Usage Examples

### Send Email via REST API

```bash
# Get JWT from auth-server first
JWT="your-jwt-token-here"

# Send email
curl -X POST http://localhost:9001/communications/api/v1/email/send \
  -H "Authorization: Bearer $JWT" \
  -H "Content-Type: application/json" \
  -d '{
    "toEmail": "patient@example.com",
    "toName": "John Doe",
    "subject": "Appointment Reminder",
    "htmlContent": "<h1>Appointment Reminder</h1><p>Your appointment is tomorrow at 10:00 AM</p>",
    "textContent": "Appointment Reminder: Your appointment is tomorrow at 10:00 AM"
  }'
```

### Send Email via RabbitMQ (from other microservices)

```java
// In another microservice (e.g., patient-service)
@Autowired
private RabbitTemplate rabbitTemplate;

public void sendAppointmentReminder(Patient patient, Appointment appointment) {
    Map<String, Object> emailRequest = Map.of(
        "tenantId", patient.getTenantId().toString(),
        "toEmail", patient.getEmail(),
        "toName", patient.getFullName(),
        "subject", "Appointment Reminder",
        "htmlContent", "<h1>Your appointment is scheduled</h1>",
        "textContent", "Your appointment is scheduled"
    );

    rabbitTemplate.convertAndSend(
        "hms.communications.events",
        "email.send",
        emailRequest
    );
}
```

### Send Email with Attachments

```bash
curl -X POST http://localhost:9001/communications/api/v1/email/send \
  -H "Authorization: Bearer $JWT" \
  -H "Content-Type: application/json" \
  -d '{
    "toEmail": "patient@example.com",
    "subject": "Lab Results",
    "htmlContent": "<p>Your lab results are attached</p>",
    "attachments": [
      {
        "filename": "lab_results.pdf",
        "content": "base64-encoded-pdf-content-here",
        "contentType": "application/pdf"
      }
    ]
  }'
```

## Database Schema

### Tables

- **email_messages**: Email records with status tracking
- **sms_messages**: SMS records with status tracking
- **delivery_logs**: Event tracking for all messages
- **tenant_settings**: Per-tenant configuration and rate limits

### Message Status Flow

```
PENDING -> SENT -> DELIVERED -> OPENED -> CLICKED
         |
         v
       FAILED -> (retry) -> SENT
         |
         v
      BOUNCED
```

## Rate Limiting

Default limits per tenant (configurable):
- **Emails**: 1000 per day
- **SMS**: 100 per day

Limits reset daily at midnight UTC.

Exceeded limits return `429 Too Many Requests`.

## Multi-Tenant Security

- All API requests require JWT authentication
- Tenant ID extracted from JWT claims
- All database queries filtered by tenant ID
- TenantContext ensures thread-safe tenant isolation

## RabbitMQ Configuration

### Exchange
- **Name**: `hms.communications.events`
- **Type**: Topic

### Queues
- **Email Queue**: `communications.email.send`
- **SMS Queue**: `communications.sms.send`

### Routing Keys
- **Email**: `email.send`
- **SMS**: `sms.send`

### Retry Strategy
- Manual acknowledgment mode
- Max 3 retry attempts
- Exponential backoff
- Messages discarded after max retries

## Brevo Integration

The service integrates with Brevo (SendinBlue) for email delivery:

1. **Initialization**: API client configured on startup
2. **Sending**: Transactional emails via Brevo REST API
3. **Tracking**: Provider message IDs stored for reference
4. **Webhooks**: Ready for Brevo webhook integration (future)

### Brevo Features Used
- Transactional email API
- Attachment support
- HTML and plain text content
- Custom sender name/email

## SMS Provider (Stub)

SMS functionality is currently a stub implementation. When Twilio integration is added:

1. Configure Twilio credentials in application.yml
2. Implement `TwilioSmsProvider` class
3. Update `SmsService` to call provider
4. Enable webhook endpoints for delivery tracking

## Monitoring & Observability

### Health Checks
- Spring Boot Actuator endpoints
- Database connectivity
- Redis connectivity
- RabbitMQ connectivity

### Logging
- Structured JSON logging (production)
- Request/response logging
- Delivery event logging
- Error tracking with trace IDs

### Metrics (via Actuator)
- JVM metrics
- Database connection pool
- Cache hit/miss rates
- Message processing rates

## Testing

```bash
# Run all tests
./mvnw test

# Run integration tests
./mvnw verify

# Run with Testcontainers (PostgreSQL, Redis, RabbitMQ)
./mvnw test -Dspring.profiles.active=test
```

## Development

### Project Structure
```
hms-communications-server/
├── config/              # Configuration classes
├── controller/          # REST controllers
├── exception/           # Custom exceptions
├── mapper/              # MapStruct mappers
├── model/
│   ├── dto/            # Request/Response DTOs
│   ├── entity/         # JPA entities
│   └── enums/          # Enums
├── repository/         # Spring Data repositories
├── security/           # JWT filter, TenantContext
└── service/            # Business logic
```

### Code Style
- Use `@Getter/@Setter`, NOT `@Data`
- Use `@Builder` for complex objects
- Use `@Slf4j` for logging
- Follow Spring Boot best practices

## Troubleshooting

### Email not sending
1. Check Brevo API key is valid
2. Verify Brevo account is active
3. Check logs for API errors
4. Verify tenant rate limits not exceeded

### RabbitMQ messages not processing
1. Check RabbitMQ connection
2. Verify queue bindings exist
3. Check consumer logs
4. Verify message format is correct

### Authentication failures
1. Verify JWT secret matches auth-server
2. Check token expiration
3. Verify token contains tenant_id claim

## Future Enhancements

- [ ] Twilio SMS integration
- [ ] Email templates with variables
- [ ] Webhook endpoints for delivery events
- [ ] Scheduled/bulk email sending
- [ ] Email preview API
- [ ] Unsubscribe management
- [ ] Email analytics dashboard
- [ ] Multi-provider fallback
- [ ] SMTP fallback option

## License

Proprietary - Factorial Systems HMS Platform

## Support

For issues and questions, contact the development team at Factorial Systems.

---

**Version**: 1.0.0
**Last Updated**: 2026-01-19
