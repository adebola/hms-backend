# HMS Frontend - Health Management System

Multi-tenant SaaS frontend application for the HMS (Health Management System) platform built with Angular.

## Overview

This is the frontend application for the HMS platform, providing a modern, responsive web interface for healthcare facilities. It features:

- **Multi-tenant architecture** with dynamic tenant branding
- **JWT-based authentication** with automatic token refresh
- **Role-based access control** (RBAC)
- **Responsive design** for desktop and mobile devices
- **Standalone components** (Angular 19+)
- **Type-safe** with TypeScript

## Technology Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| **Angular** | 19.1.0 | Frontend framework |
| **TypeScript** | 5.6.2 | Type-safe JavaScript |
| **RxJS** | 7.8.1 | Reactive programming |
| **SCSS** | - | Styling |

## Project Structure

```
hms-frontend/
├── src/
│   ├── app/
│   │   ├── core/                      # Core module (singletons)
│   │   │   ├── guards/                # Route guards
│   │   │   │   └── auth.guard.ts      # Authentication & authorization guards
│   │   │   ├── interceptors/          # HTTP interceptors
│   │   │   │   └── jwt.interceptor.ts # JWT token injection & error handling
│   │   │   ├── models/                # Data models
│   │   │   │   ├── auth.model.ts      # Authentication models
│   │   │   │   ├── user.model.ts      # User & role models
│   │   │   │   └── tenant.model.ts    # Tenant models
│   │   │   └── services/              # Core services
│   │   │       ├── api.service.ts     # Base HTTP service
│   │   │       ├── auth.service.ts    # Authentication service
│   │   │       └── tenant.service.ts  # Tenant & branding service
│   │   ├── features/                  # Feature modules
│   │   │   ├── auth/                  # Authentication feature
│   │   │   │   └── login/             # Login component
│   │   │   └── dashboard/             # Dashboard feature
│   │   ├── shared/                    # Shared components & utilities
│   │   ├── app.component.ts           # Root component
│   │   ├── app.config.ts              # App configuration
│   │   └── app.routes.ts              # Routing configuration
│   ├── environments/                  # Environment configurations
│   │   ├── environment.ts             # Development environment
│   │   └── environment.prod.ts        # Production environment
│   ├── assets/                        # Static assets
│   ├── styles.scss                    # Global styles
│   ├── index.html                     # HTML entry point
│   └── main.ts                        # TypeScript entry point
├── angular.json                       # Angular CLI configuration
├── package.json                       # NPM dependencies
├── tsconfig.json                      # TypeScript configuration
├── proxy.conf.json                    # Development proxy configuration
└── README.md                          # This file
```

## Prerequisites

Before running the application, ensure you have:

- **Node.js** >= 18.19.0
- **npm** >= 9.0.0
- **HMS Backend Services** running:
  - HMS Gateway (port 8080)
  - HMS Authorization Server (port 9000)

## Installation

1. **Clone the repository** (if not already done):
   ```bash
   cd hms-backend/hms-frontend
   ```

2. **Install dependencies**:
   ```bash
   npm install
   ```

## Development

### Running the Development Server

Start the development server:

```bash
npm start
```

The application will be available at `http://localhost:4200`

The dev server includes:
- Hot reload on code changes
- Proxy configuration to route API calls to gateway (port 8080)
- Source maps for debugging

### Development Proxy

API calls are proxied to the HMS Gateway running on `http://localhost:8080`. This is configured in `proxy.conf.json`:

```json
{
  "/api": {
    "target": "http://localhost:8080",
    "secure": false,
    "changeOrigin": true,
    "logLevel": "debug"
  }
}
```

### Environment Configuration

**Development** (`src/environments/environment.ts`):
```typescript
{
  production: false,
  apiUrl: 'http://localhost:8080',
  apiVersion: 'v1',
  defaultTenantCode: 'DEFAULT'
}
```

**Production** (`src/environments/environment.prod.ts`):
```typescript
{
  production: true,
  apiUrl: 'https://gateway.hms-platform.com',
  apiVersion: 'v1',
  defaultTenantCode: ''
}
```

## Building for Production

### Build the application:

```bash
npm run build:prod
```

Output will be in `dist/hms-frontend/` directory.

### Build optimizations:
- Ahead-of-Time (AOT) compilation
- Tree shaking
- Minification
- Source map generation (optional)

### Deployment:

The built files can be deployed to:
- **Static hosting**: Nginx, Apache, AWS S3, Netlify, Vercel
- **CDN**: CloudFront, Cloudflare
- **Container**: Docker with Nginx

## Authentication Flow

### Login Process

1. **User navigates to login page** (`/login`)
2. **User enters credentials**:
   - Tenant Code (e.g., "DEFAULT", "HOSPITAL_A")
   - Username
   - Password
3. **Frontend calls API**: `POST /api/v1/auth/login`
4. **Backend validates credentials** and returns JWT tokens:
   ```json
   {
     "accessToken": "eyJhbGciOiJIUzI1Ni...",
     "refreshToken": "eyJhbGciOiJIUzI1Ni...",
     "tokenType": "Bearer",
     "expiresIn": 900
   }
   ```
5. **Frontend stores tokens** in localStorage:
   - `hms_access_token`
   - `hms_refresh_token`
   - `hms_token_expiry`
6. **User is redirected** to dashboard or return URL

### Authenticated Requests

All authenticated API requests include the JWT token:

```
Authorization: Bearer eyJhbGciOiJIUzI1Ni...
```

This is handled automatically by the `jwtInterceptor`.

### Token Refresh

When the access token expires (15 minutes by default):

1. **API returns 401 Unauthorized**
2. **JWT Interceptor catches the error**
3. **Calls refresh endpoint**: `POST /api/v1/auth/refresh`
4. **Receives new tokens**
5. **Retries the original request** with new token

If refresh fails, user is logged out and redirected to login.

### Logout

```typescript
authService.logout().subscribe();
```

1. Calls `POST /api/v1/auth/logout` to blacklist tokens
2. Clears tokens from localStorage
3. Redirects to login page

## Multi-Tenancy

### Tenant Code Determination

The application determines the tenant code from multiple sources (in priority order):

1. **URL parameter**: `?tenant=HOSPITAL_A`
2. **Subdomain**: `hospitalA.hms.com` → `HOSPITAL_A`
3. **Cached value**: Previously used tenant code
4. **Environment default**: `DEFAULT` (development only)

### Tenant Branding

Each tenant can have custom branding:

```typescript
interface TenantBranding {
  tenantCode: string;
  facilityName: string;
  logoUrl?: string;
  primaryColor?: string;    // CSS custom property
  secondaryColor?: string;  // CSS custom property
  accentColor?: string;     // CSS custom property
}
```

Branding is applied via CSS custom properties:

```css
:root {
  --primary-color: #4f46e5;
  --secondary-color: #7c3aed;
  --accent-color: #06b6d4;
}
```

Components use these variables for theming:

```scss
.btn-login {
  background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
}
```

## Route Protection

### Auth Guard

Protects routes that require authentication:

```typescript
{
  path: 'dashboard',
  component: DashboardComponent,
  canActivate: [authGuard]
}
```

If not authenticated, redirects to login with return URL.

### Role Guard

Protects routes based on user roles:

```typescript
{
  path: 'admin',
  component: AdminComponent,
  canActivate: [roleGuard],
  data: { roles: ['SUPER_ADMIN', 'TENANT_ADMIN'] }
}
```

### Permission Guard

Protects routes based on specific permissions:

```typescript
{
  path: 'users/create',
  component: CreateUserComponent,
  canActivate: [permissionGuard],
  data: { permissions: ['USER_CREATE'] }
}
```

### Guest Guard

Prevents authenticated users from accessing guest-only routes (e.g., login):

```typescript
{
  path: 'login',
  component: LoginComponent,
  canActivate: [guestGuard]
}
```

## Services

### AuthService

**Authentication and user session management.**

```typescript
// Login
authService.login(tenantCode, username, password).subscribe();

// Logout
authService.logout().subscribe();

// Get current user
authService.getCurrentUser().subscribe(user => {...});

// Check authentication
const isAuth = authService.isAuthenticated();

// Check permissions
const canCreate = authService.hasPermission('USER_CREATE');

// Check roles
const isAdmin = authService.hasRole('TENANT_ADMIN');
```

### TenantService

**Tenant configuration and branding.**

```typescript
// Get tenant branding
tenantService.getTenantBranding(tenantCode).subscribe();

// Set tenant code
tenantService.setTenantCode('HOSPITAL_A');

// Get current tenant code
const code = tenantService.getTenantCode();
```

### ApiService

**Base HTTP service for API calls.**

```typescript
// GET request
apiService.get<User[]>('users').subscribe();

// POST request
apiService.post<User>('users', userData).subscribe();

// PUT request
apiService.put<User>('users/123', userData).subscribe();

// DELETE request
apiService.delete('users/123').subscribe();
```

All API calls are automatically prefixed with:
- Base URL: from environment
- API version: `/api/v1`
- Authentication header: `Authorization: Bearer <token>`

## Testing the Application

### Prerequisites

Ensure backend services are running:

```bash
# Terminal 1 - Start Gateway
cd hms-gateway
./mvnw spring-boot:run

# Terminal 2 - Start Auth Server
cd hms-authorization-server
./mvnw spring-boot:run
```

### Test Login

1. **Start the frontend**:
   ```bash
   cd hms-frontend
   npm start
   ```

2. **Navigate to** `http://localhost:4200`

3. **Login with test credentials**:
   - **Tenant Code**: `DEFAULT`
   - **Username**: `admin` (or as configured in your database)
   - **Password**: (check your database or create a test user)

4. **Verify**:
   - Login successful → Dashboard loads
   - User info displayed correctly
   - Roles and permissions shown
   - Logout works correctly

### Test Multi-Tenancy

1. **Register a second tenant** via API or Postman:
   ```bash
   POST http://localhost:8080/api/v1/tenants/register
   ```

2. **Login with second tenant code**

3. **Verify tenant isolation**:
   - Different branding applied
   - User can only see their tenant's data
   - JWT token contains correct tenant_id

## Troubleshooting

### CORS Errors

**Issue**: CORS policy blocking API requests

**Solution**: Ensure HMS Gateway has CORS configured:
```yaml
cors:
  allowed-origins: "http://localhost:4200"
  allowed-methods: "*"
  allowed-headers: "*"
```

### 401 Unauthorized Errors

**Issue**: API returns 401 even with valid token

**Possible causes**:
1. Token expired → Should auto-refresh
2. Token blacklisted → Logout and login again
3. Server secret changed → Clear localStorage and login again

### Token Not Attached to Requests

**Issue**: Authorization header missing

**Check**:
1. Token exists in localStorage (`hms_access_token`)
2. JWT interceptor is registered in app.config.ts
3. Request URL matches API pattern (not external URL)

### Login Redirect Loop

**Issue**: Redirects between /login and /dashboard

**Possible causes**:
1. Token stored but invalid → Clear localStorage
2. Auth guard not working → Check route configuration
3. Guest guard misconfigured → Check guard logic

### Tenant Branding Not Applied

**Issue**: Custom colors/logo not showing

**Check**:
1. Tenant branding API endpoint works: `GET /api/v1/tenants/{code}/branding`
2. CSS custom properties set in browser DevTools
3. Component uses `var(--primary-color)` syntax

## API Endpoints Used

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/auth/login` | User login |
| POST | `/api/v1/auth/refresh` | Refresh access token |
| POST | `/api/v1/auth/logout` | User logout |
| GET | `/api/v1/auth/me` | Get current user info |
| POST | `/api/v1/auth/change-password` | Change password |
| GET | `/api/v1/tenants/{code}/branding` | Get tenant branding |
| GET | `/api/v1/users` | List users (authenticated) |

## Future Enhancements

- [ ] User profile management
- [ ] Password reset flow
- [ ] Two-factor authentication (2FA)
- [ ] Remember me functionality
- [ ] Social login (Google, Microsoft)
- [ ] Dark mode support
- [ ] Internationalization (i18n)
- [ ] Progressive Web App (PWA)
- [ ] Mobile app (Ionic/Capacitor)

## Scripts

| Command | Description |
|---------|-------------|
| `npm start` | Start development server |
| `npm run build` | Build for production |
| `npm run watch` | Build and watch for changes |
| `npm test` | Run unit tests |
| `npm run lint` | Lint code |

## Contributing

1. Follow Angular style guide
2. Use standalone components
3. Maintain type safety (no `any` types)
4. Write meaningful commit messages
5. Test authentication flow thoroughly

## License

Proprietary - Factorial Systems HMS Platform

## Support

For issues or questions, contact the development team at Factorial Systems.

---

**Last Updated**: 2026-01-24
**Version**: 1.0.0
**Angular Version**: 19.1.0
