package io.factorialsystems.auth.model.dto.messaging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for email attachments sent via RabbitMQ.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailAttachmentDto {

    private String filename;
    private String content;      // Base64 encoded
    private String contentType;
}
