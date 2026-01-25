#!/bin/bash

# HMS Backend - Development Startup Script
# This script starts all HMS services for local development

set -e  # Exit on error

echo "========================================"
echo "HMS Platform - Development Startup"
echo "========================================"
echo ""

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Function to check if port is in use
port_in_use() {
    lsof -i ":$1" >/dev/null 2>&1
}

# Check prerequisites
echo "Checking prerequisites..."

if ! command_exists java; then
    echo -e "${RED}Error: Java is not installed${NC}"
    exit 1
fi

if ! command_exists docker; then
    echo -e "${RED}Error: Docker is not installed${NC}"
    exit 1
fi

if ! command_exists docker-compose; then
    echo -e "${RED}Error: Docker Compose is not installed${NC}"
    exit 1
fi

echo -e "${GREEN}✓ All prerequisites met${NC}"
echo ""

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo -e "${YELLOW}Warning: Java version $JAVA_VERSION detected. Java 17+ recommended.${NC}"
fi

# Check if ports are available
echo "Checking ports..."
PORTS=(5432 6379 5672 8080 9000 8081 4200)
PORT_NAMES=("PostgreSQL" "Redis" "RabbitMQ" "Gateway" "Auth Server" "Communications" "Frontend")

for i in "${!PORTS[@]}"; do
    if port_in_use "${PORTS[$i]}"; then
        echo -e "${YELLOW}Warning: Port ${PORTS[$i]} (${PORT_NAMES[$i]}) is already in use${NC}"
    fi
done
echo ""

# Start infrastructure services with Docker Compose
echo "Starting infrastructure services..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

docker-compose up -d postgres redis rabbitmq

echo ""
echo "Waiting for services to be ready..."
sleep 10

# Check if services are healthy
echo ""
echo "Checking service health..."

# Check PostgreSQL
if docker exec hms-postgres pg_isready -U hms_user -d hms_auth >/dev/null 2>&1; then
    echo -e "${GREEN}✓ PostgreSQL is ready${NC}"
else
    echo -e "${RED}✗ PostgreSQL is not ready${NC}"
fi

# Check Redis
if docker exec hms-redis redis-cli ping | grep -q PONG; then
    echo -e "${GREEN}✓ Redis is ready${NC}"
else
    echo -e "${RED}✗ Redis is not ready${NC}"
fi

# Check RabbitMQ
if docker exec hms-rabbitmq rabbitmq-diagnostics ping >/dev/null 2>&1; then
    echo -e "${GREEN}✓ RabbitMQ is ready${NC}"
else
    echo -e "${RED}✗ RabbitMQ is not ready${NC}"
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Prompt to start backend services
echo "Infrastructure services are running!"
echo ""
echo "Next steps:"
echo ""
echo "1. Start Gateway (Terminal 1):"
echo "   ${YELLOW}cd hms-gateway && ./mvnw spring-boot:run${NC}"
echo ""
echo "2. Start Auth Server (Terminal 2):"
echo "   ${YELLOW}cd hms-authorization-server && ./mvnw spring-boot:run${NC}"
echo ""
echo "3. Start Communications Server (Terminal 3):"
echo "   ${YELLOW}cd hms-communications-server && ./mvnw spring-boot:run${NC}"
echo ""
echo "4. Start Frontend (Terminal 4):"
echo "   ${YELLOW}cd hms-frontend && npm start${NC}"
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "Access points:"
echo "  • Frontend:         http://localhost:4200"
echo "  • Gateway:          http://localhost:8080"
echo "  • Auth Server:      http://localhost:9000/auth"
echo "  • Auth Swagger:     http://localhost:9000/auth/swagger-ui.html"
echo "  • RabbitMQ UI:      http://localhost:15672 (guest/guest)"
echo ""
echo "To stop infrastructure services, run:"
echo "  ${YELLOW}docker-compose down${NC}"
echo ""
echo "========================================"
echo "Happy coding! 🚀"
echo "========================================"
