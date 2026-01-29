# Login Testing Guide

## Test Data Created

### Tenant Information
- **Tenant Code**: `TEST1234ABCD5678`
- **Tenant Name**: Test General Hospital
- **Status**: ACTIVE
- **Subscription**: PROFESSIONAL

### OAuth2 Client
- **Client ID**: `testhospital-web-client`
- **Client Secret**: `password` (BCrypt hashed in database)

### Test Users

All users have the password: **`password`**

| Username      | Email                           | Role            | Department        |
|---------------|---------------------------------|-----------------|-------------------|
| superadmin    | superadmin@testhospital.com     | SUPER_ADMIN     | Platform Admin    |
| admin         | admin@testhospital.com          | TENANT_ADMIN    | Hospital Admin    |
| doctor        | doctor@testhospital.com         | DOCTOR          | General Medicine  |
| nurse         | nurse@testhospital.com          | NURSE           | General Medicine  |
| receptionist  | receptionist@testhospital.com   | RECEPTIONIST    | Front Desk        |
| cashier       | cashier@testhospital.com        | CASHIER         | Billing           |
| labtech       | labtech@testhospital.com        | LAB_TECHNICIAN  | Laboratory        |
| pharmacist    | pharmacist@testhospital.com     | PHARMACIST      | Pharmacy          |
| records       | records@testhospital.com        | RECORDS_OFFICER | Medical Records   |

## Setup Instructions

### 1. Execute the SQL Script

The SQL script has been written to your IntelliJ database console at:
```
/Users/adebola/Library/Application Support/JetBrains/IntelliJIdea2025.3/consoles/db/18dd1062-7d19-41e6-9c9e-271054f3afb8/console.sql
```

**To run it:**
1. Open the database console in IntelliJ
2. Connect to your `hms_auth` database
3. Execute the entire script (Ctrl+Enter or Cmd+Enter)
4. Verify the verification queries at the end show the created data

### 2. Frontend Configuration

The frontend environment has been configured with the test tenant:

**File**: `hms-frontend/src/environments/environment.ts`

```typescript
tenant: {
  code: 'TEST1234ABCD5678',
  autoDetectFromSubdomain: true,
}

oauth: {
  clientId: 'testhospital-web-client',
}
```

## Testing the Login

### Start the Backend Services

```bash
# 1. Start Authorization Server
cd hms-authorization-server
./mvnw spring-boot:run

# 2. Start API Gateway (in another terminal)
cd hms-gateway
./mvnw spring-boot:run
```

### Start the Frontend

```bash
cd hms-frontend
npm install  # If not already done
ng serve
```

### Test Login Flow

1. **Navigate to Login Page**
   ```
   http://localhost:4200/login
   ```

2. **Verify UI Changes**
   - ✅ Only username and password fields visible
   - ✅ NO tenant code field
   - ✅ Tenant branding shows "Test General Hospital" (if branding API works)

3. **Login with Test User**

   **Example 1 - Doctor:**
   - Username: `doctor`
   - Password: `password`

   **Example 2 - Admin:**
   - Username: `admin`
   - Password: `password`

4. **Expected Result**
   - ✅ Login successful
   - ✅ Redirected to dashboard
   - ✅ User info displayed correctly

### Verify JWT Token

After successful login, open browser DevTools console and run:

```javascript
// Get access token
const token = localStorage.getItem('hms_access_token');

// Decode token
const payload = JSON.parse(atob(token.split('.')[1]));

// Verify token contents
console.log('Tenant Code:', payload.tenant_code);  // Should be: TEST1234ABCD5678
console.log('Tenant ID:', payload.tenant_id);
console.log('Username:', payload.sub);
console.log('Roles:', payload.roles);
console.log('Permissions:', payload.permissions);
console.log('Expires:', new Date(payload.exp * 1000));
```

**Expected Output:**
```javascript
Tenant Code: TEST1234ABCD5678
Tenant ID: <UUID of test tenant>
Username: doctor (or whatever user you logged in as)
Roles: ["DOCTOR"]
Permissions: ["patient:read", "patient:update", "encounter:create", ...]
Expires: <15 minutes from now>
```

## Test Different Roles

Login with each role to verify permissions:

### 1. SUPER_ADMIN (superadmin/password)
- Should have ALL permissions
- Platform-wide access

### 2. TENANT_ADMIN (admin/password)
- Should have most permissions except tenant activation/suspension
- Hospital-wide access

### 3. DOCTOR (doctor/password)
- Patient read/update
- Prescription create/read/update
- Encounter management
- Lab order creation

### 4. NURSE (nurse/password)
- Patient read/update
- Encounter read/update
- Prescription read-only
- Lab results read

### 5. RECEPTIONIST (receptionist/password)
- Patient create/read/update
- Appointment management
- Billing read-only

### 6. CASHIER (cashier/password)
- Billing create/read/update
- Payment processing
- Financial reports

### 7. LAB_TECHNICIAN (labtech/password)
- Lab order read/update
- Result entry
- Patient read-only

### 8. PHARMACIST (pharmacist/password)
- Prescription read/dispense
- Inventory management
- Drug ordering

### 9. RECORDS_OFFICER (records/password)
- Patient records read/update
- Encounter read-only
- Report generation

## API Testing (Optional)

### Test Login API Directly

```bash
# Login with doctor account
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "tenantCode": "TEST1234ABCD5678",
    "username": "doctor",
    "password": "password"
  }'
```

**Expected Response:**
```json
{
  "accessToken": "eyJhbGciOiJSUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJSUzI1NiIs...",
  "expiresIn": 900,
  "tokenType": "Bearer"
}
```

### Test Tenant Branding API

```bash
# Get tenant branding
curl http://localhost:8080/api/v1/tenants/TEST1234ABCD5678/branding
```

**Expected Response:**
```json
{
  "facilityName": "Test General Hospital",
  "logoUrl": "https://via.placeholder.com/200x200?text=TEST+HOSPITAL",
  "primaryColor": null,
  "secondaryColor": null,
  "accentColor": null,
  "customStyles": null
}
```

## Troubleshooting

### Issue: "Invalid tenant code"

**Cause:** Tenant not created in database

**Solution:**
1. Run the SQL script in IntelliJ
2. Verify tenant exists:
   ```sql
   SELECT * FROM tenants WHERE code = 'TEST1234ABCD5678';
   ```

### Issue: "Invalid credentials"

**Possible causes:**
1. User not created
2. Password hash incorrect
3. User status not ACTIVE

**Solution:**
1. Verify user exists:
   ```sql
   SELECT username, email, status FROM users
   WHERE tenant_id = (SELECT id FROM tenants WHERE code = 'TEST1234ABCD5678');
   ```
2. Check user status is 'ACTIVE'
3. Re-run SQL script if needed

### Issue: "No tenant code field on login page"

**Expected:** This is correct! The tenant code field has been removed.

**Verification:** Check that only username and password fields are shown.

### Issue: "Cannot connect to server"

**Solution:**
1. Verify backend services are running:
   - Auth Server: http://localhost:9000/auth/actuator/health
   - Gateway: http://localhost:8080/actuator/health

2. Check proxy configuration in `hms-frontend/proxy.conf.json`

### Issue: JWT token missing tenant_code

**Cause:** Backend not populating token correctly

**Solution:**
1. Check AuthService implementation
2. Verify TenantService is resolving tenant correctly
3. Check token customizer in authorization server

## Clean Up Test Data

If you need to remove the test data:

```sql
-- Delete in correct order (respecting foreign keys)
DELETE FROM oauth2_registered_client WHERE client_id = 'testhospital-web-client';
DELETE FROM user_roles WHERE user_id IN (SELECT id FROM users WHERE tenant_id IN (SELECT id FROM tenants WHERE code = 'TEST1234ABCD5678'));
DELETE FROM users WHERE tenant_id IN (SELECT id FROM tenants WHERE code = 'TEST1234ABCD5678');
DELETE FROM tenants WHERE code = 'TEST1234ABCD5678';
```

## Success Criteria

- ✅ Login page shows only username/password (no tenant code)
- ✅ Can login with any test user using password 'password'
- ✅ JWT token includes correct tenant_code: TEST1234ABCD5678
- ✅ Dashboard loads after successful login
- ✅ User permissions match expected role permissions
- ✅ Logout works correctly
- ✅ Tenant branding loads (if API implemented)

## Next Steps

After successful testing:

1. **Production Deployment**
   - Use `build-tenant.sh` script to build for real tenants
   - Create production OAuth2 clients with strong secrets
   - Create actual user accounts with strong passwords

2. **Security**
   - Change all test passwords
   - Use password strength requirements
   - Enable MFA for privileged accounts

3. **Additional Testing**
   - Test all role permissions thoroughly
   - Test password change functionality
   - Test session timeout and refresh token flow
   - Test account lockout after failed login attempts

---

**Happy Testing!** 🎉

If you encounter any issues, check:
- Backend logs in console
- Browser DevTools console for frontend errors
- Network tab for failed API calls
