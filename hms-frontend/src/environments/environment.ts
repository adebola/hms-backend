export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080',
  apiVersion: 'v1',
  tokenRefreshThreshold: 60000, // Refresh token 1 minute before expiry (in milliseconds)
  enableDebugMode: true,

  // Tenant configuration (hard-coded per deployment)
  tenant: {
    code: 'TEST1234ABCD5678',  // Test tenant code - change per deployment
    autoDetectFromSubdomain: true,  // Override with subdomain if present in dev
  },

  // OAuth2 client info (for reference only, not for authentication)
  oauth: {
    clientId: 'testhospital-web-client',  // Public identifier
    // NO client_secret here - never expose secrets in frontend
  }
};
