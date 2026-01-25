# HMS Frontend - Quick Start Guide

Get the HMS frontend up and running in 5 minutes!

## Prerequisites Checklist

- [ ] Node.js >= 18.19.0 installed
- [ ] npm >= 9.0.0 installed
- [ ] HMS Gateway running on port 8080
- [ ] HMS Authorization Server running on port 9000

## Quick Setup

### 1. Install Dependencies

```bash
cd hms-frontend
npm install
```

Wait for dependencies to install (~2 minutes).

### 2. Start Development Server

```bash
npm start
```

The app will automatically open at `http://localhost:4200`

### 3. Login

Use these **default credentials** (for development):

| Field | Value |
|-------|-------|
| **Tenant Code** | `DEFAULT` |
| **Username** | `admin` |
| **Password** | Check your database or create a test user |

### 4. Verify Everything Works

After login, you should see:
- ✅ Dashboard loads successfully
- ✅ Your user information displayed
- ✅ Roles and permissions shown
- ✅ Facility branding applied (if configured)

## Quick Commands

| Task | Command |
|------|---------|
| Start dev server | `npm start` |
| Build for production | `npm run build:prod` |
| Stop dev server | `Ctrl + C` |

## Test Multi-Tenancy (Optional)

### Create a Second Tenant

Use Postman or curl to create a new tenant:

```bash
POST http://localhost:8080/api/v1/tenants/register

{
  "tenantCode": "HOSP_A",
  "facilityName": "Hospital A",
  "facilityType": "HOSPITAL",
  "facilityLevel": "TERTIARY",
  "address": {
    "street": "123 Main St",
    "city": "Lagos",
    "state": "Lagos",
    "postalCode": "100001",
    "country": "Nigeria"
  },
  "contactPerson": "John Doe",
  "contactEmail": "admin@hospitala.com",
  "contactPhone": "+234-800-0000",
  "subscriptionPlan": "PROFESSIONAL",
  "adminUser": {
    "username": "admin",
    "email": "admin@hospitala.com",
    "firstName": "John",
    "lastName": "Doe",
    "password": "Test@123456"
  }
}
```

### Login with New Tenant

1. Logout from current session
2. Login with:
   - Tenant Code: `HOSP_A`
   - Username: `admin`
   - Password: `Test@123456`

## Common Issues

### Issue: Can't connect to backend

**Solution**: Verify backend services are running:
```bash
# Check Gateway
curl http://localhost:8080/actuator/health

# Check Auth Server
curl http://localhost:9000/auth/actuator/health
```

### Issue: CORS errors in console

**Solution**: Ensure Gateway CORS is configured for `http://localhost:4200`

### Issue: Login fails with 401

**Possible causes**:
1. Wrong credentials → Check database
2. Tenant not activated → Activate tenant via API
3. User account locked → Check user status

### Issue: npm install fails

**Solution**:
```bash
# Clear npm cache
npm cache clean --force

# Delete node_modules and package-lock.json
rm -rf node_modules package-lock.json

# Reinstall
npm install
```

## Project Structure (Simplified)

```
hms-frontend/
├── src/
│   ├── app/
│   │   ├── core/               # Services, guards, interceptors
│   │   ├── features/
│   │   │   ├── auth/login/     # Login page
│   │   │   └── dashboard/      # Dashboard page
│   │   └── app.routes.ts       # Routing
│   └── environments/           # Environment configs
├── package.json
└── README.md                   # Full documentation
```

## Next Steps

1. **Explore the dashboard** - See your user info and permissions
2. **Test logout** - Verify it clears session and redirects
3. **Read the full README** - Learn about architecture and advanced features
4. **Start building features** - Add new components and services

## Important URLs

| Service | URL |
|---------|-----|
| Frontend | http://localhost:4200 |
| Gateway | http://localhost:8080 |
| Auth Server | http://localhost:9000 |
| Auth Server Swagger | http://localhost:9000/auth/swagger-ui.html |

## Getting Help

- Check the full README.md for detailed documentation
- Review the architecture plan (if available)
- Contact the development team

Happy coding! 🚀
