#!/bin/bash

echo "=== HMS Services Diagnostic ==="
echo ""

echo "1. Checking Authorization Server (Port 9000)..."
if curl -s http://localhost:9000/auth/actuator/health > /dev/null 2>&1; then
    echo "   ✅ Auth Server is UP"
    curl -s http://localhost:9000/auth/actuator/health | jq '.' 2>/dev/null || curl -s http://localhost:9000/auth/actuator/health
else
    echo "   ❌ Auth Server is DOWN"
    echo "      Start with: cd hms-authorization-server && ./mvnw spring-boot:run"
fi

echo ""
echo "2. Checking API Gateway (Port 8080)..."
if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo "   ✅ Gateway is UP"
    curl -s http://localhost:8080/actuator/health | jq '.' 2>/dev/null || curl -s http://localhost:8080/actuator/health
else
    echo "   ❌ Gateway is DOWN"
    echo "      Start with: cd hms-gateway && ./mvnw spring-boot:run"
fi

echo ""
echo "3. Testing Auth Server Login Endpoint (Direct)..."
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST http://localhost:9000/auth/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"tenantCode":"TEST1234ABCD5678","username":"doctor","password":"password"}' 2>/dev/null)

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | head -n-1)

if [ "$HTTP_CODE" = "200" ]; then
    echo "   ✅ Login endpoint working (200 OK)"
    echo "   Response preview:"
    echo "$BODY" | jq '.data | {accessToken: .accessToken[0:50], expiresIn}' 2>/dev/null || echo "$BODY" | head -c 200
elif [ "$HTTP_CODE" = "401" ]; then
    echo "   ⚠️  Login endpoint found but credentials invalid (401)"
    echo "      Check if test data was created (see LOGIN_TESTING_GUIDE.md)"
    echo "   Response: $BODY"
elif [ "$HTTP_CODE" = "404" ]; then
    echo "   ❌ Login endpoint not found (404)"
    echo "      Check AuthController mapping"
elif [ "$HTTP_CODE" = "000" ] || [ -z "$HTTP_CODE" ]; then
    echo "   ❌ Cannot connect to auth server"
    echo "      Ensure auth server is running on port 9000"
else
    echo "   ⚠️  Unexpected response: $HTTP_CODE"
    echo "   Response: $BODY"
fi

echo ""
echo "4. Testing Gateway Routing (via Gateway)..."
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"tenantCode":"TEST1234ABCD5678","username":"doctor","password":"password"}' 2>/dev/null)

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | head -n-1)

if [ "$HTTP_CODE" = "200" ]; then
    echo "   ✅ Gateway routing working (200 OK)"
    echo "   Response preview:"
    echo "$BODY" | jq '.data | {accessToken: .accessToken[0:50], expiresIn}' 2>/dev/null || echo "$BODY" | head -c 200
elif [ "$HTTP_CODE" = "401" ]; then
    echo "   ⚠️  Gateway routing working but credentials invalid (401)"
    echo "      Check if test data was created (see LOGIN_TESTING_GUIDE.md)"
    echo "   Response: $BODY"
elif [ "$HTTP_CODE" = "404" ]; then
    echo "   ❌ Gateway routing failed (404)"
    echo "      This is the error you're seeing in Angular!"
    echo ""
    echo "   Possible causes:"
    echo "   - Gateway route not configured correctly"
    echo "   - Path rewrite not working"
    echo "   - Auth server endpoint mismatch"
    echo ""
    echo "   Check gateway logs for routing details"
elif [ "$HTTP_CODE" = "000" ] || [ -z "$HTTP_CODE" ]; then
    echo "   ❌ Cannot connect to gateway"
    echo "      Ensure gateway is running on port 8080"
else
    echo "   ⚠️  Unexpected response: $HTTP_CODE"
    echo "   Response: $BODY"
fi

echo ""
echo "5. Checking PostgreSQL Database..."
if pg_isready -h localhost -p 5432 > /dev/null 2>&1; then
    echo "   ✅ PostgreSQL is running"
else
    echo "   ⚠️  PostgreSQL check failed (pg_isready not found or DB not running)"
    echo "      If auth server started successfully, DB is probably OK"
fi

echo ""
echo "=== Summary ==="
echo ""

# Determine the issue
AUTH_UP=false
GATEWAY_UP=false
if curl -s http://localhost:9000/auth/actuator/health > /dev/null 2>&1; then
    AUTH_UP=true
fi
if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
    GATEWAY_UP=true
fi

if [ "$AUTH_UP" = false ]; then
    echo "⚠️  PRIMARY ISSUE: Authorization Server is not running"
    echo ""
    echo "   To fix:"
    echo "   1. Open a new terminal"
    echo "   2. cd hms-authorization-server"
    echo "   3. ./mvnw spring-boot:run"
    echo "   4. Wait for 'Started AuthorizationServerApplication' message"
    echo ""
elif [ "$GATEWAY_UP" = false ]; then
    echo "⚠️  PRIMARY ISSUE: API Gateway is not running"
    echo ""
    echo "   To fix:"
    echo "   1. Open a new terminal"
    echo "   2. cd hms-gateway"
    echo "   3. ./mvnw spring-boot:run"
    echo "   4. Wait for 'Started GatewayApplication' message"
    echo ""
else
    # Both running, check which test failed
    AUTH_TEST=$(curl -s -w "%{http_code}" -o /dev/null -X POST http://localhost:9000/auth/api/v1/auth/login \
      -H "Content-Type: application/json" \
      -d '{"tenantCode":"TEST1234ABCD5678","username":"doctor","password":"password"}')

    GATEWAY_TEST=$(curl -s -w "%{http_code}" -o /dev/null -X POST http://localhost:8080/api/v1/auth/login \
      -H "Content-Type: application/json" \
      -d '{"tenantCode":"TEST1234ABCD5678","username":"doctor","password":"password"}')

    if [ "$AUTH_TEST" = "404" ]; then
        echo "⚠️  PRIMARY ISSUE: Auth server endpoint not found"
        echo ""
        echo "   The endpoint /auth/api/v1/auth/login doesn't exist on auth server"
        echo "   Check if AuthController is loaded and mapped correctly"
        echo ""
    elif [ "$GATEWAY_TEST" = "404" ]; then
        echo "⚠️  PRIMARY ISSUE: Gateway routing not working"
        echo ""
        echo "   Auth server works directly but gateway returns 404"
        echo "   Check gateway routing configuration in application.yml"
        echo ""
        echo "   Expected route configuration:"
        echo "   - Path predicate: /api/v1/auth/**"
        echo "   - Rewrite filter: /api/v1/auth/{segment} -> /auth/api/v1/auth/{segment}"
        echo ""
    elif [ "$AUTH_TEST" = "401" ] || [ "$GATEWAY_TEST" = "401" ]; then
        echo "⚠️  Services are running but credentials are invalid"
        echo ""
        echo "   Run the SQL script to create test data:"
        echo "   1. Open IntelliJ database console"
        echo "   2. Connect to hms_auth database"
        echo "   3. Execute the script at: "
        echo "      ~/.../IntelliJIdea.../consoles/db/.../console.sql"
        echo ""
        echo "   See LOGIN_TESTING_GUIDE.md for details"
        echo ""
    elif [ "$AUTH_TEST" = "200" ] && [ "$GATEWAY_TEST" = "200" ]; then
        echo "✅ Everything is working!"
        echo ""
        echo "   All services are running and login works."
        echo "   You should be able to login from Angular now."
        echo ""
        echo "   If Angular still shows 404, check:"
        echo "   - Browser console for actual error"
        echo "   - Network tab in DevTools"
        echo "   - Angular proxy.conf.json configuration"
        echo ""
    else
        echo "⚠️  Unexpected state"
        echo ""
        echo "   Auth server response: $AUTH_TEST"
        echo "   Gateway response: $GATEWAY_TEST"
        echo ""
        echo "   Check server logs for errors"
        echo ""
    fi
fi

echo "=== Diagnostic Complete ==="
echo ""
echo "For more help, see: hms-frontend/DEBUG_LOGIN_404.md"
