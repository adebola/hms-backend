# Debugging Login 404 Error

## Error Details
```
Error Code: 404
Message: Http failure response for http://localhost:8080/api/v1/auth/login: 404 Not Found
```

## Expected Request Flow

```
Frontend (Angular)
    ↓
    POST http://localhost:8080/api/v1/auth/login
    ↓
API Gateway (Port 8080)
    ↓ (rewrites path)
    POST http://localhost:9000/auth/api/v1/auth/login
    ↓
Authorization Server (Port 9000, Context: /auth)
    ↓
    Handles at: /api/v1/auth/login (controller mapping)
```

## Diagnostic Steps

### Step 1: Verify Authorization Server is Running

```bash
# Test if auth server is accessible
curl http://localhost:9000/auth/actuator/health

# Expected response:
# {"status":"UP"}
```

**If this fails:**
- Auth server is not running
- Start it with: `cd hms-authorization-server && ./mvnw spring-boot:run`

### Step 2: Test Auth Server Login Endpoint Directly

```bash
# Test login endpoint directly on auth server
curl -X POST http://localhost:9000/auth/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "tenantCode": "TEST1234ABCD5678",
    "username": "doctor",
    "password": "password"
  }'

# Expected response (200 OK):
# {
#   "success": true,
#   "message": "Login successful",
#   "data": {
#     "accessToken": "eyJhbGci...",
#     "refreshToken": "eyJhbGci...",
#     "expiresIn": 900,
#     "tokenType": "Bearer"
#   }
# }
```

**If this fails with 404:**
- Controller mapping issue
- Check if AuthController is loaded

**If this succeeds:**
- Auth server is working
- Issue is with Gateway routing

### Step 3: Verify API Gateway is Running

```bash
# Test if gateway is accessible
curl http://localhost:8080/actuator/health

# Expected response:
# {"status":"UP"}
```

**If this fails:**
- Gateway is not running
- Start it with: `cd hms-gateway && ./mvnw spring-boot:run`

### Step 4: Test Login Through Gateway

```bash
# Test login through gateway (this is what Angular does)
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "tenantCode": "TEST1234ABCD5678",
    "username": "doctor",
    "password": "password"
  }'

# Expected response (200 OK):
# Same as Step 2
```

**If this fails with 404:**
- Gateway routing issue
- Check gateway logs for routing details

### Step 5: Check Gateway Routing Configuration

Verify the gateway configuration in `hms-gateway/src/main/resources/application.yml`:

```yaml
# Line 38-44
- id: auth-authentication
  uri: ${gateway.services.auth}
  predicates:
    - Path=/api/v1/auth/**
  filters:
    - RewritePath=/api/v1/auth/(?<segment>.*), /auth/api/v1/auth/$\{segment}
```

And service URL configuration:

```yaml
# Line 245-247
gateway:
  services:
    auth: http://localhost:9000
```

### Step 6: Check Gateway Logs

When you make the request through the gateway, check the console output for:

```
DEBUG io.factorialsystems.gateway - Incoming request: POST /api/v1/auth/login
DEBUG org.springframework.cloud.gateway - Route matched: auth-authentication
DEBUG org.springframework.cloud.gateway - Rewritten path: /auth/api/v1/auth/login
```

## Common Issues and Solutions

### Issue 1: Auth Server Not Running

**Symptoms:**
- `curl http://localhost:9000/auth/actuator/health` fails

**Solution:**
```bash
cd hms-authorization-server
./mvnw spring-boot:run
```

Wait for startup message:
```
Started AuthorizationServerApplication in X.XXX seconds
```

### Issue 2: Gateway Not Running

**Symptoms:**
- `curl http://localhost:8080/actuator/health` fails

**Solution:**
```bash
cd hms-gateway
./mvnw spring-boot:run
```

Wait for startup message:
```
Started GatewayApplication in X.XXX seconds
```

### Issue 3: Database Not Running

**Symptoms:**
- Auth server logs show database connection errors

**Solution:**
```bash
# If using Docker
docker start postgres-container-name

# Or start PostgreSQL service
# macOS (Homebrew):
brew services start postgresql@15

# Linux:
sudo systemctl start postgresql
```

### Issue 4: Test Data Not Created

**Symptoms:**
- Auth server is running
- Login returns 401 Unauthorized (not 404)

**Solution:**
- Execute the SQL script in IntelliJ database console
- See: `LOGIN_TESTING_GUIDE.md` for instructions

### Issue 5: CORS Error (Not 404)

**Symptoms:**
- Browser console shows CORS error
- Network tab shows OPTIONS request fails

**Solution:**
- Already configured in gateway (lines 23-31 in application.yml)
- Verify Angular is running on `http://localhost:4200`

## Quick Diagnostic Script

Run this in your terminal to check all services:

```bash
#!/bin/bash

echo "=== HMS Services Diagnostic ==="
echo ""

echo "1. Checking Authorization Server (Port 9000)..."
if curl -s http://localhost:9000/auth/actuator/health > /dev/null 2>&1; then
    echo "   ✅ Auth Server is UP"
else
    echo "   ❌ Auth Server is DOWN"
    echo "      Start with: cd hms-authorization-server && ./mvnw spring-boot:run"
fi

echo ""
echo "2. Checking API Gateway (Port 8080)..."
if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo "   ✅ Gateway is UP"
else
    echo "   ❌ Gateway is DOWN"
    echo "      Start with: cd hms-gateway && ./mvnw spring-boot:run"
fi

echo ""
echo "3. Testing Auth Server Login Endpoint..."
RESPONSE=$(curl -s -w "%{http_code}" -o /dev/null -X POST http://localhost:9000/auth/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"tenantCode":"TEST1234ABCD5678","username":"doctor","password":"password"}')

if [ "$RESPONSE" = "200" ]; then
    echo "   ✅ Login endpoint working (200 OK)"
elif [ "$RESPONSE" = "401" ]; then
    echo "   ⚠️  Login endpoint found but credentials invalid (401)"
    echo "      Check if test data was created (see LOGIN_TESTING_GUIDE.md)"
elif [ "$RESPONSE" = "404" ]; then
    echo "   ❌ Login endpoint not found (404)"
    echo "      Check AuthController mapping"
elif [ "$RESPONSE" = "000" ]; then
    echo "   ❌ Cannot connect to auth server"
    echo "      Ensure auth server is running"
else
    echo "   ⚠️  Unexpected response: $RESPONSE"
fi

echo ""
echo "4. Testing Gateway Routing..."
RESPONSE=$(curl -s -w "%{http_code}" -o /dev/null -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"tenantCode":"TEST1234ABCD5678","username":"doctor","password":"password"}')

if [ "$RESPONSE" = "200" ]; then
    echo "   ✅ Gateway routing working (200 OK)"
elif [ "$RESPONSE" = "401" ]; then
    echo "   ⚠️  Gateway routing working but credentials invalid (401)"
    echo "      Check if test data was created (see LOGIN_TESTING_GUIDE.md)"
elif [ "$RESPONSE" = "404" ]; then
    echo "   ❌ Gateway routing failed (404)"
    echo "      Check gateway configuration and logs"
elif [ "$RESPONSE" = "000" ]; then
    echo "   ❌ Cannot connect to gateway"
    echo "      Ensure gateway is running"
else
    echo "   ⚠️  Unexpected response: $RESPONSE"
fi

echo ""
echo "=== Diagnostic Complete ==="
```

Save this as `diagnose-services.sh` and run:

```bash
chmod +x diagnose-services.sh
./diagnose-services.sh
```

## Most Likely Causes (in order)

1. **Auth Server not running** (90% of cases)
   - Start with: `cd hms-authorization-server && ./mvnw spring-boot:run`

2. **Gateway not running** (8% of cases)
   - Start with: `cd hms-gateway && ./mvnw spring-boot:run`

3. **Database not running** (1% of cases)
   - Start PostgreSQL

4. **Routing configuration issue** (1% of cases)
   - Verify gateway application.yml

## Next Steps

After running the diagnostics:

1. **If auth server is down**: Start it first
2. **If gateway is down**: Start it second
3. **If both are up but login fails**: Check the response from Step 2 (direct auth server test)
4. **If direct auth test works but gateway test fails**: Check gateway logs for routing errors

## Verification

Once fixed, you should be able to:

1. Login from Angular UI at `http://localhost:4200/login`
2. See successful login with redirect to dashboard
3. JWT token stored in localStorage

Let me know which step fails and I'll help you troubleshoot further!
