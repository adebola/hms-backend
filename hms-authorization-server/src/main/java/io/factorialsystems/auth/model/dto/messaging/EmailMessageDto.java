package io.factorialsystems.auth.model.dto.messaging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

/**
 * DTO for sending email messages via RabbitMQ to the Communications Server.
 * This matches the SendEmailRequest format expected by the communications server.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailMessageDto {

    private UUID tenantId;
    private String toEmail;
    private String toName;
    private String fromEmail;  // Optional, uses tenant default if not provided
    private String fromName;   // Optional, uses tenant default if not provided
    private String subject;
    private String htmlContent;
    private String textContent;
    private List<EmailAttachmentDto> attachments;
}
