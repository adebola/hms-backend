package io.factorialsystems.auth.exception;
import org.springframework.http.HttpStatus;
public class AuthenticationException extends BusinessException {
    public AuthenticationException(String code, String message) { super(code, message, HttpStatus.UNAUTHORIZED); }
    public static AuthenticationException invalidCredentials() { return new AuthenticationException("INVALID_CREDENTIALS", "Invalid username or password"); }
    public static AuthenticationException accountLocked() { return new AuthenticationException("ACCOUNT_LOCKED", "Account is locked"); }
    public static AuthenticationException accountInactive() { return new AuthenticationException("ACCOUNT_INACTIVE", "Account is not active"); }
    public static AuthenticationException tenantInactive() { return new AuthenticationException("TENANT_INACTIVE", "Tenant is not active"); }
    public static AuthenticationException passwordExpired() { return new AuthenticationException("PASSWORD_EXPIRED", "Password has expired"); }
    public static AuthenticationException invalidToken() { return new AuthenticationException("INVALID_TOKEN", "Invalid or expired token"); }
}
