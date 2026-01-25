export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080',
  apiVersion: 'v1',
  tokenRefreshThreshold: 60000, // Refresh token 1 minute before expiry (in milliseconds)
  defaultTenantCode: 'DEFAULT', // For development
  enableDebugMode: true
};
