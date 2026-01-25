package io.factorialsystems.communications.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SendEmailRequest {

    private UUID tenantId;  // Set from JWT, not request body

    @NotBlank(message = "Recipient email is required")
    @Email(message = "Invalid email format")
    private String toEmail;

    private String toName;

    @Email(message = "Invalid from email format")
    private String fromEmail;  // Optional, uses tenant default if not provided

    private String fromName;   // Optional, uses tenant default if not provided

    @NotBlank(message = "Subject is required")
    @Size(max = 500, message = "Subject must not exceed 500 characters")
    private String subject;

    private String htmlContent;  // Either htmlContent or textContent must be provided

    private String textContent;

    private List<EmailAttachmentRequest> attachments;
}
