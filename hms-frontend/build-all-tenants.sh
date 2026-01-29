#!/bin/bash
# Build script for multiple tenants at once

set -e  # Exit on error

echo "========================================="
echo "HMS Frontend - Multi-Tenant Build"
echo "========================================="

# Define tenants (TENANT_CODE:CLIENT_ID)
# Add your tenants here
tenants=(
  "HOSPITAL_A:hospital-a-web-client"
  "CLINIC_B:clinic-b-web-client"
  "LAB_C:lab-c-web-client"
)

total=${#tenants[@]}
current=0

echo "Building $total tenant deployments..."
echo ""

for entry in "${tenants[@]}"; do
  current=$((current + 1))
  IFS=':' read -r tenant_code client_id <<< "$entry"

  echo "[$current/$total] Building $tenant_code..."
  ./build-tenant.sh "$tenant_code" "$client_id"
  echo ""
done

echo "========================================="
echo "All tenant builds complete!"
echo "========================================="
echo "Built tenants:"
for entry in "${tenants[@]}"; do
  IFS=':' read -r tenant_code _ <<< "$entry"
  echo "  - $tenant_code → dist/$tenant_code/"
done
echo ""
echo "Next steps:"
echo "1. Deploy each tenant to their respective web servers"
echo "2. Configure DNS for each subdomain"
echo "3. Test login for each tenant"
echo "========================================="
