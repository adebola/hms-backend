#!/bin/bash
# Build script for tenant-specific deployments

set -e  # Exit on error

TENANT_CODE=$1
CLIENT_ID=$2
OUTPUT_DIR="dist/${TENANT_CODE}"

if [ -z "$TENANT_CODE" ]; then
  echo "Usage: ./build-tenant.sh <TENANT_CODE> [CLIENT_ID]"
  echo "Example: ./build-tenant.sh HOSPITAL_A hospital-a-web-client"
  echo ""
  echo "TENANT_CODE: Tenant code for deployment (e.g., HOSPITAL_A, CLINIC_B)"
  echo "CLIENT_ID: OAuth2 client ID (optional, defaults to lowercase tenant code + '-web-client')"
  exit 1
fi

# Default client ID if not provided
if [ -z "$CLIENT_ID" ]; then
  CLIENT_ID="${TENANT_CODE,,}-web-client"  # lowercase tenant code
fi

echo "========================================="
echo "HMS Frontend - Tenant-Specific Build"
echo "========================================="
echo "Tenant Code: $TENANT_CODE"
echo "Client ID: $CLIENT_ID"
echo "Output Directory: $OUTPUT_DIR"
echo "========================================="

# Backup original production environment file
echo "Backing up original environment.prod.ts..."
cp src/environments/environment.prod.ts src/environments/environment.prod.ts.backup

# Create tenant-specific environment file
echo "Creating tenant-specific environment configuration..."
cat > src/environments/environment.prod.ts <<EOF
export const environment = {
  production: true,
  apiUrl: 'https://gateway.hms-platform.com',
  apiVersion: 'v1',
  tokenRefreshThreshold: 60000,
  enableDebugMode: false,

  // Tenant configuration (hard-coded for this deployment)
  tenant: {
    code: '${TENANT_CODE}',
    autoDetectFromSubdomain: false,  // Use hard-coded value only
  },

  // OAuth2 client info (for reference only, not for authentication)
  oauth: {
    clientId: '${CLIENT_ID}',
    // NO client_secret here - never expose secrets in frontend
  }
};
EOF

# Build with production configuration
echo "Building frontend for tenant: $TENANT_CODE..."
ng build --configuration=production \
  --output-path="${OUTPUT_DIR}" \
  --base-href="/"

# Restore original environment file
echo "Restoring original environment.prod.ts..."
mv src/environments/environment.prod.ts.backup src/environments/environment.prod.ts

echo ""
echo "========================================="
echo "Build complete!"
echo "========================================="
echo "Output: ${OUTPUT_DIR}"
echo "Deploy to: https://${TENANT_CODE,,}.hms.com"
echo ""
echo "Next steps:"
echo "1. Copy ${OUTPUT_DIR} to your web server"
echo "2. Configure DNS: ${TENANT_CODE,,}.hms.com"
echo "3. Test login at: https://${TENANT_CODE,,}.hms.com/login"
echo "========================================="
