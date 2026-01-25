package io.factorialsystems.communications.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SendSmsRequest {

    private UUID tenantId;  // Set from JWT, not request body

    @NotBlank(message = "Recipient phone number is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format (E.164)")
    private String toPhone;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format (E.164)")
    private String fromPhone;  // Optional, uses tenant default if not provided

    @NotBlank(message = "Message is required")
    @Size(max = 1600, message = "Message must not exceed 1600 characters")
    private String message;
}
