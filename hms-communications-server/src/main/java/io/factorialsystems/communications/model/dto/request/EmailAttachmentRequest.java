package io.factorialsystems.communications.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailAttachmentRequest {

    @NotBlank(message = "Filename is required")
    private String filename;

    @NotBlank(message = "Content is required")
    private String content;  // Base64 encoded

    @NotBlank(message = "Content type is required")
    private String contentType;
}
