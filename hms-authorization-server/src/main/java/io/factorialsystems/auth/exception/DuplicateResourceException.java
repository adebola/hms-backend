package io.factorialsystems.auth.exception;
import org.springframework.http.HttpStatus;
public class DuplicateResourceException extends BusinessException {
    public DuplicateResourceException(String resourceType, String field, String value) {
        super("DUPLICATE_RESOURCE", resourceType + " with " + field + " '" + value + "' already exists", HttpStatus.CONFLICT);
    }
}
