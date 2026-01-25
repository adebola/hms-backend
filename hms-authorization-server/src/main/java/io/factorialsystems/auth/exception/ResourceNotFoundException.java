package io.factorialsystems.auth.exception;
import org.springframework.http.HttpStatus;
public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String resourceType, String identifier) {
        super("RESOURCE_NOT_FOUND", resourceType + " not found: " + identifier, HttpStatus.NOT_FOUND);
    }
}
