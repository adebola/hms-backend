package io.factorialsystems.auth.model.dto.messaging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * DTO for sending SMS messages via RabbitMQ to the Communications Server.
 * This matches the SendSmsRequest format expected by the communications server.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SmsMessageDto {

    private UUID tenantId;
    private String toPhone;
    private String fromPhone;  // Optional, uses tenant default if not provided
    private String message;
}
