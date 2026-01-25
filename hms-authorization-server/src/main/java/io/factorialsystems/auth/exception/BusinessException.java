package io.factorialsystems.auth.exception;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import java.util.Map;
@Getter
public class BusinessException extends RuntimeException {
    private final String code;
    private final HttpStatus httpStatus;
    private final Map<String, String> details;
    public BusinessException(String code, String message) { this(code, message, HttpStatus.BAD_REQUEST, null); }
    public BusinessException(String code, String message, HttpStatus httpStatus) { this(code, message, httpStatus, null); }
    public BusinessException(String code, String message, HttpStatus httpStatus, Map<String, String> details) {
        super(message); this.code = code; this.httpStatus = httpStatus; this.details = details;
    }
}
