package io.factorialsystems.communications.model.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.factorialsystems.communications.model.enums.MessageStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SmsMessageResponse {

    private UUID id;
    private UUID tenantId;
    private String toPhone;
    private String fromPhone;
    private String message;
    private MessageStatus status;
    private String providerId;
    private LocalDateTime sentAt;
    private LocalDateTime deliveredAt;
    private String errorMessage;
    private Integer retryCount;
    private LocalDateTime lastRetryAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
