package io.factorialsystems.auth.model.dto.response;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Map;
@Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private ErrorDetails error;
    @Builder.Default private LocalDateTime timestamp = LocalDateTime.now();
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ErrorDetails { private String code; private String message; private Map<String, String> details; private String traceId; }
    public static <T> ApiResponse<T> success(T data) { return ApiResponse.<T>builder().success(true).data(data).build(); }
    public static <T> ApiResponse<T> success(T data, String message) { return ApiResponse.<T>builder().success(true).message(message).data(data).build(); }
    public static <T> ApiResponse<T> error(String code, String message) { return ApiResponse.<T>builder().success(false).error(ErrorDetails.builder().code(code).message(message).build()).build(); }
    public static <T> ApiResponse<T> error(String code, String message, Map<String, String> details) { return ApiResponse.<T>builder().success(false).error(ErrorDetails.builder().code(code).message(message).details(details).build()).build(); }
}
