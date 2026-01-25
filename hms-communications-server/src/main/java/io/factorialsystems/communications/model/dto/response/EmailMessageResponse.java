package io.factorialsystems.communications.model.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.factorialsystems.communications.model.enums.MessageStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmailMessageResponse {

    private UUID id;
    private UUID tenantId;
    private String toEmail;
    private String toName;
    private String fromEmail;
    private String fromName;
    private String subject;
    private String htmlContent;
    private String textContent;
    private MessageStatus status;
    private String providerId;
    private List<Map<String, String>> attachments;
    private LocalDateTime sentAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime openedAt;
    private LocalDateTime clickedAt;
    private String errorMessage;
    private Integer retryCount;
    private LocalDateTime lastRetryAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
