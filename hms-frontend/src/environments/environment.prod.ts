export const environment = {
  production: true,
  apiUrl: 'https://gateway.hms-platform.com',
  apiVersion: 'v1',
  tokenRefreshThreshold: 60000,
  enableDebugMode: false,

  // Tenant configuration (hard-coded per deployment)
  tenant: {
    code: 'DEFAULT',  // IMPORTANT: Change this for each tenant deployment
    autoDetectFromSubdomain: false,  // Use hard-coded value only in production
  },

  // OAuth2 client info (for reference only, not for authentication)
  oauth: {
    clientId: 'default-web-client',  // Public identifier
    // NO client_secret here - never expose secrets in frontend
  }
};
