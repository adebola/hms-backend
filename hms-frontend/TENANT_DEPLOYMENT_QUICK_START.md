# Tenant Deployment - Quick Start Guide

## Overview

Users now login with **username + password only** (no tenant code input). Each deployment is pre-configured for a specific tenant.

## Quick Commands

### Build Single Tenant
```bash
cd hms-frontend
./build-tenant.sh HOSPITAL_A
```

### Build Multiple Tenants
```bash
# Edit build-all-tenants.sh to add your tenants
./build-all-tenants.sh
```

### Deploy
```bash
# Copy to server
scp -r dist/HOSPITAL_A/* user@server:/var/www/hospitala.hms.com/
```

## What Changed

### 1. Login Form (User-Facing)
**Before:**
- Tenant Code field ✅ (user enters)
- Username field
- Password field

**After:**
- Username field
- Password field
(Tenant code is now pre-configured per deployment)

### 2. Environment Configuration

**Development** (`environment.ts`):
```typescript
tenant: {
  code: 'DEFAULT',
  autoDetectFromSubdomain: true,  // Auto-detect from URL
}
```

**Production** (per deployment):
```typescript
tenant: {
  code: 'HOSPITAL_A',  // Hard-coded for this deployment
  autoDetectFromSubdomain: false,
}
```

### 3. Build Process

**Old:** One build for all tenants
```bash
ng build --configuration=production
```

**New:** Separate build per tenant
```bash
./build-tenant.sh HOSPITAL_A  # Creates dist/HOSPITAL_A/
./build-tenant.sh CLINIC_B    # Creates dist/CLINIC_B/
```

## Files Changed

| File | Change |
|------|--------|
| `environment.ts` | Added `tenant` config object |
| `environment.prod.ts` | Added `tenant` config object |
| `login.component.ts` | Removed `tenantCode` form field, uses environment config |
| `login.component.html` | Removed tenant code input field |
| `build-tenant.sh` | **NEW** - Build script for tenant deployments |
| `build-all-tenants.sh` | **NEW** - Build multiple tenants at once |

## Testing

### Local Development
```bash
# Run dev server
ng serve

# Login with DEFAULT tenant
# Navigate to http://localhost:4200/login
# Enter username/password only
```

### Production Build Test
```bash
# Build for test tenant
./build-tenant.sh TEST_HOSPITAL

# Verify tenant code embedded
cat dist/TEST_HOSPITAL/main.*.js | grep -o "TEST_HOSPITAL"

# Serve locally
cd dist/TEST_HOSPITAL
python3 -m http.server 8000

# Test at http://localhost:8000
```

## Deployment Architecture

```
┌─────────────────────────────────────────────┐
│  DNS                                         │
│  ├─ hospitala.hms.com → Server IP           │
│  ├─ clinicb.hms.com   → Server IP           │
│  └─ labc.hms.com      → Server IP           │
└─────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────┐
│  Web Server (Nginx)                          │
│  ├─ /var/www/hospitala/ → HOSPITAL_A build │
│  ├─ /var/www/clinicb/   → CLINIC_B build   │
│  └─ /var/www/labc/      → LAB_C build       │
└─────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────┐
│  API Gateway (gateway.hms-platform.com)      │
└─────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────┐
│  Auth Server (validates credentials)         │
└─────────────────────────────────────────────┘
```

## Common Issues

### Issue: "Tenant code is required" error

**Cause:** Old form validation still checking for tenantCode field

**Fix:** Clear browser cache and rebuild

```bash
# Rebuild
./build-tenant.sh TENANT_CODE

# Hard refresh browser (Ctrl+Shift+R or Cmd+Shift+R)
```

### Issue: Wrong tenant branding shows

**Cause:** Build has wrong tenant code

**Fix:** Verify build configuration

```bash
# Check what tenant code is in the build
cat dist/TENANT_CODE/main.*.js | grep -o "TENANT_CODE"

# Rebuild if incorrect
./build-tenant.sh CORRECT_TENANT_CODE
```

### Issue: Cannot login

**Possible causes:**
1. Backend OAuth2 client not registered
2. Wrong API URL in environment
3. Tenant not active in database

**Debug:**
```bash
# Check API connectivity
curl https://gateway.hms-platform.com/api/v1/tenants/TENANT_CODE/branding

# Check browser console for errors
# Open DevTools → Console tab
```

## Security Notes

✅ **Safe to include in frontend:**
- Tenant code (public identifier)
- OAuth2 client ID (public identifier)
- API URL (public endpoint)

❌ **NEVER include in frontend:**
- OAuth2 client secret (stays in backend database)
- Database credentials
- Private keys

## Next Steps

1. **Create OAuth2 clients** for each tenant (backend)
2. **Build deployments** for each tenant
3. **Deploy to web servers**
4. **Configure DNS** for each subdomain
5. **Test login** for each tenant

## Support

For detailed documentation, see:
- Full guide: `TENANT_DEPLOYMENT.md`
- Main project docs: `/hms-backend/CLAUDE.md`
