package io.factorialsystems.communications.controller;

import io.factorialsystems.communications.model.dto.request.SendEmailRequest;
import io.factorialsystems.communications.model.dto.response.ApiResponse;
import io.factorialsystems.communications.model.dto.response.EmailMessageResponse;
import io.factorialsystems.communications.model.enums.MessageStatus;
import io.factorialsystems.communications.security.TenantContext;
import io.factorialsystems.communications.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/email")
@RequiredArgsConstructor
@Tag(name = "Email", description = "Email messaging endpoints")
@SecurityRequirement(name = "bearerAuth")
public class EmailController {

    private final EmailService emailService;

    @PostMapping("/send")
    @Operation(summary = "Send email", description = "Send a transactional email via Brevo")
    public ResponseEntity<ApiResponse<EmailMessageResponse>> sendEmail(
            @Valid @RequestBody SendEmailRequest request) {

        UUID tenantId = TenantContext.getTenantId();
        request.setTenantId(tenantId);

        log.info("Sending email for tenant: {} to: {}", tenantId, request.getToEmail());

        EmailMessageResponse response = emailService.sendEmail(tenantId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Email sent successfully"));
    }

    @GetMapping("/messages")
    @Operation(summary = "List emails", description = "Get paginated list of emails for current tenant")
    public ResponseEntity<ApiResponse<Page<EmailMessageResponse>>> listEmails(
            @Parameter(description = "Filter by status")
            @RequestParam(required = false) MessageStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        UUID tenantId = TenantContext.getTenantId();
        log.debug("Listing emails for tenant: {}, status: {}", tenantId, status);

        Page<EmailMessageResponse> emails = emailService.listEmails(tenantId, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(emails));
    }

    @GetMapping("/messages/{id}")
    @Operation(summary = "Get email", description = "Get email details by ID")
    public ResponseEntity<ApiResponse<EmailMessageResponse>> getEmail(
            @Parameter(description = "Email message ID")
            @PathVariable UUID id) {

        UUID tenantId = TenantContext.getTenantId();
        log.debug("Getting email: {} for tenant: {}", id, tenantId);

        EmailMessageResponse email = emailService.getEmailById(id, tenantId);
        return ResponseEntity.ok(ApiResponse.success(email));
    }

    @PostMapping("/messages/{id}/retry")
    @Operation(summary = "Retry failed email", description = "Retry sending a failed email")
    public ResponseEntity<ApiResponse<Void>> retryEmail(
            @Parameter(description = "Email message ID")
            @PathVariable UUID id) {

        UUID tenantId = TenantContext.getTenantId();
        log.info("Retrying email: {} for tenant: {}", id, tenantId);

        emailService.retryFailedEmail(id, tenantId);
        return ResponseEntity.ok(ApiResponse.success(null, "Email retry initiated"));
    }
}
