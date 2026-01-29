# Tenant Deployment Checklist

Use this checklist when deploying a new tenant frontend.

## Pre-Deployment

### Backend Setup
- [ ] Tenant registered in authorization server
  ```bash
  # Verify tenant exists
  curl https://gateway.hms-platform.com/api/v1/tenants/code/TENANT_CODE
  ```

- [ ] OAuth2 client created for tenant
  ```bash
  # Should return client info (created automatically on tenant registration)
  # Client ID format: {tenant_code}-web-client (lowercase)
  ```

- [ ] Tenant status is ACTIVE
  ```sql
  SELECT code, name, status FROM tenants WHERE code = 'TENANT_CODE';
  ```

- [ ] Test tenant branding endpoint
  ```bash
  curl https://gateway.hms-platform.com/api/v1/tenants/TENANT_CODE/branding
  ```

## Build Process

### 1. Build Frontend for Tenant
- [ ] Run build script
  ```bash
  cd hms-frontend
  ./build-tenant.sh TENANT_CODE
  ```

- [ ] Verify build completed successfully
  ```bash
  ls -la dist/TENANT_CODE/
  ```

- [ ] Verify tenant code embedded in build
  ```bash
  cat dist/TENANT_CODE/main.*.js | grep -o "TENANT_CODE" | head -1
  ```

- [ ] Check build size (should be < 1MB for main bundle)
  ```bash
  du -sh dist/TENANT_CODE/
  ```

## Infrastructure Setup

### 2. Web Server Configuration

- [ ] Create web server directory
  ```bash
  ssh user@server
  sudo mkdir -p /var/www/tenant.hms.com
  sudo chown www-data:www-data /var/www/tenant.hms.com
  ```

- [ ] Deploy files to server
  ```bash
  # From local machine
  scp -r dist/TENANT_CODE/* user@server:/var/www/tenant.hms.com/
  ```

- [ ] Verify file permissions
  ```bash
  # On server
  sudo chown -R www-data:www-data /var/www/tenant.hms.com
  sudo chmod -R 755 /var/www/tenant.hms.com
  ```

- [ ] Create Nginx virtual host configuration
  ```bash
  # Create config file
  sudo nano /etc/nginx/sites-available/tenant.hms.com

  # See TENANT_DEPLOYMENT.md for configuration example
  ```

- [ ] Enable site and test configuration
  ```bash
  sudo ln -s /etc/nginx/sites-available/tenant.hms.com /etc/nginx/sites-enabled/
  sudo nginx -t
  ```

- [ ] Reload Nginx
  ```bash
  sudo systemctl reload nginx
  ```

### 3. SSL Certificate

- [ ] Obtain SSL certificate (Let's Encrypt)
  ```bash
  sudo certbot --nginx -d tenant.hms.com
  ```

- [ ] Verify SSL certificate
  ```bash
  curl -I https://tenant.hms.com
  ```

- [ ] Test SSL grade (optional)
  ```
  https://www.ssllabs.com/ssltest/analyze.html?d=tenant.hms.com
  ```

### 4. DNS Configuration

- [ ] Create DNS A record
  ```
  tenant.hms.com  →  A  →  YOUR_SERVER_IP
  ```

- [ ] Verify DNS propagation
  ```bash
  nslookup tenant.hms.com
  dig tenant.hms.com
  ```

- [ ] Wait for DNS propagation (5-30 minutes)

## Testing

### 5. Functional Testing

- [ ] Access login page
  ```
  https://tenant.hms.com/login
  ```

- [ ] Verify tenant branding loads
  - [ ] Facility logo displays (if configured)
  - [ ] Facility name shows in header
  - [ ] Custom colors applied (if configured)
  - [ ] Page title shows "{Facility Name} - HMS"

- [ ] Test login flow
  - [ ] Only username/password fields visible (no tenant code field)
  - [ ] Enter valid credentials
  - [ ] Login succeeds
  - [ ] Redirects to dashboard

- [ ] Verify JWT token content
  ```javascript
  // In browser console after login
  const token = localStorage.getItem('hms_access_token');
  const payload = JSON.parse(atob(token.split('.')[1]));
  console.log('Tenant Code:', payload.tenant_code);  // Should match TENANT_CODE
  console.log('Tenant ID:', payload.tenant_id);
  console.log('User:', payload.sub);
  console.log('Roles:', payload.roles);
  console.log('Permissions:', payload.permissions);
  ```

- [ ] Test navigation
  - [ ] Dashboard loads
  - [ ] Sidebar menu works
  - [ ] User profile accessible

- [ ] Test logout
  - [ ] Logout button works
  - [ ] Redirects to login page
  - [ ] Tokens cleared from localStorage

### 6. Error Handling

- [ ] Test invalid credentials
  - [ ] Shows appropriate error message
  - [ ] Does not crash

- [ ] Test network errors
  - [ ] Temporary disconnect backend
  - [ ] Verify error message shown
  - [ ] Reconnect and verify recovery

- [ ] Test expired token
  - [ ] Wait for token expiry (or manipulate expiry time)
  - [ ] Verify auto-logout or refresh

### 7. Cross-Browser Testing

- [ ] Chrome/Edge (Chromium)
- [ ] Firefox
- [ ] Safari (if applicable)
- [ ] Mobile browsers (Chrome Mobile, Safari iOS)

### 8. Performance Testing

- [ ] Page load time < 3 seconds
  ```
  # Use browser DevTools → Network tab
  ```

- [ ] Lighthouse audit score > 90
  ```
  # Use Chrome DevTools → Lighthouse
  ```

- [ ] Check bundle sizes
  ```
  # In dist/TENANT_CODE/
  ls -lh *.js *.css
  ```

## Security Verification

### 9. Security Checks

- [ ] HTTPS enforced (HTTP redirects to HTTPS)
  ```bash
  curl -I http://tenant.hms.com
  # Should return 301/302 redirect to https://
  ```

- [ ] No client secrets in frontend
  ```bash
  # Search build files for "secret"
  grep -r "client_secret" dist/TENANT_CODE/
  # Should return no results
  ```

- [ ] No hardcoded passwords
  ```bash
  grep -r "password" dist/TENANT_CODE/
  # Should only find form field names, no actual passwords
  ```

- [ ] Security headers configured
  ```bash
  curl -I https://tenant.hms.com
  # Should see: X-Frame-Options, X-Content-Type-Options, etc.
  ```

- [ ] CORS configured correctly
  ```bash
  # Test from browser console
  fetch('https://gateway.hms-platform.com/api/v1/tenants/TENANT_CODE/branding')
  ```

## Post-Deployment

### 10. Monitoring Setup

- [ ] Configure uptime monitoring
  ```
  # Use: UptimeRobot, Pingdom, or similar
  Monitor URL: https://tenant.hms.com/login
  ```

- [ ] Configure error monitoring
  ```
  # Use: Sentry, LogRocket, or similar
  # Add monitoring script to index.html if needed
  ```

- [ ] Set up log aggregation
  ```bash
  # Configure Nginx logs to send to ELK/Splunk
  ```

### 11. Documentation

- [ ] Document deployment details
  - [ ] Tenant code
  - [ ] OAuth2 client ID
  - [ ] Server IP/hostname
  - [ ] DNS records
  - [ ] SSL certificate expiry date
  - [ ] Deployment date
  - [ ] Deployment version/commit hash

- [ ] Add to tenant registry
  ```markdown
  | Tenant Code | Name | URL | Deployed | Version |
  |-------------|------|-----|----------|---------|
  | TENANT_CODE | Hospital Name | https://tenant.hms.com | 2026-01-27 | v1.0.0 |
  ```

### 12. User Communication

- [ ] Notify tenant administrator
  ```
  Subject: HMS System - Login Portal Ready

  Your HMS login portal is now live at:
  https://tenant.hms.com/login

  Users can login with their username and password.
  No tenant code is required.

  Support contact: support@yourcompany.com
  ```

- [ ] Provide training materials (if needed)
- [ ] Share user guide/documentation link

## Maintenance

### 13. Post-Deployment Monitoring (First 24 Hours)

- [ ] Monitor error rates
- [ ] Check successful login count
- [ ] Verify no authentication failures (beyond expected incorrect password)
- [ ] Monitor server resource usage (CPU, memory, disk)

### 14. Regular Maintenance

- [ ] Schedule SSL certificate renewal reminder (90 days)
- [ ] Plan frontend version updates
- [ ] Monitor build size over time
- [ ] Review and update tenant branding as needed

## Rollback Plan

If deployment fails:

- [ ] Switch DNS back to old deployment (if applicable)
- [ ] Restore previous Nginx configuration
  ```bash
  sudo rm /etc/nginx/sites-enabled/tenant.hms.com
  sudo systemctl reload nginx
  ```
- [ ] Investigate issues
- [ ] Fix and redeploy

## Success Criteria

Deployment is successful when:

- ✅ Login page loads without errors
- ✅ Users can login with username/password only
- ✅ Tenant branding displays correctly
- ✅ JWT tokens include correct tenant context
- ✅ All core features work (navigation, logout, etc.)
- ✅ HTTPS working with valid certificate
- ✅ No console errors in browser
- ✅ Performance meets targets (load time < 3s)

---

## Quick Reference

### Tenant Info Template

```
Tenant Code: HOSPITAL_A
Facility Name: General Hospital A
URL: https://hospitala.hms.com
OAuth2 Client ID: hospital-a-web-client
Server: server.example.com (192.168.1.100)
Deployed: 2026-01-27
Version: 1.0.0
SSL Expiry: 2026-04-27
```

### Emergency Contacts

```
Backend Team: backend@yourcompany.com
DevOps Team: devops@yourcompany.com
Support Team: support@yourcompany.com
On-Call: +1-XXX-XXX-XXXX
```

### Common Commands

```bash
# Rebuild tenant
./build-tenant.sh TENANT_CODE

# Redeploy
scp -r dist/TENANT_CODE/* user@server:/var/www/tenant.hms.com/

# Check logs
ssh user@server
sudo tail -f /var/log/nginx/access.log
sudo tail -f /var/log/nginx/error.log

# Restart Nginx
sudo systemctl restart nginx

# Renew SSL
sudo certbot renew
```

---

**Last Updated:** 2026-01-27
**Version:** 1.0
