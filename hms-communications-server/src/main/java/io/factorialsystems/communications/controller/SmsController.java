package io.factorialsystems.communications.controller;

import io.factorialsystems.communications.model.dto.request.SendSmsRequest;
import io.factorialsystems.communications.model.dto.response.ApiResponse;
import io.factorialsystems.communications.model.dto.response.SmsMessageResponse;
import io.factorialsystems.communications.model.enums.MessageStatus;
import io.factorialsystems.communications.security.TenantContext;
import io.factorialsystems.communications.service.SmsService;
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
@RequestMapping("/api/v1/sms")
@RequiredArgsConstructor
@Tag(name = "SMS", description = "SMS messaging endpoints (stub implementation)")
@SecurityRequirement(name = "bearerAuth")
public class SmsController {

    private final SmsService smsService;

    @PostMapping("/send")
    @Operation(summary = "Send SMS (stub)", description = "Send SMS - currently stub implementation, Twilio integration pending")
    public ResponseEntity<ApiResponse<SmsMessageResponse>> sendSms(
            @Valid @RequestBody SendSmsRequest request) {

        UUID tenantId = TenantContext.getTenantId();
        request.setTenantId(tenantId);

        log.info("Sending SMS for tenant: {} to: {} (STUB)", tenantId, request.getToPhone());

        SmsMessageResponse response = smsService.sendSms(tenantId, request);
        return ResponseEntity.ok(ApiResponse.success(response,
                "SMS recorded (stub mode - actual sending not yet implemented)"));
    }

    @GetMapping("/messages")
    @Operation(summary = "List SMS messages", description = "Get paginated list of SMS messages for current tenant")
    public ResponseEntity<ApiResponse<Page<SmsMessageResponse>>> listSms(
            @Parameter(description = "Filter by status")
            @RequestParam(required = false) MessageStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        UUID tenantId = TenantContext.getTenantId();
        log.debug("Listing SMS for tenant: {}, status: {}", tenantId, status);

        Page<SmsMessageResponse> smsMessages = smsService.listSms(tenantId, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(smsMessages));
    }

    @GetMapping("/messages/{id}")
    @Operation(summary = "Get SMS message", description = "Get SMS message details by ID")
    public ResponseEntity<ApiResponse<SmsMessageResponse>> getSms(
            @Parameter(description = "SMS message ID")
            @PathVariable UUID id) {

        UUID tenantId = TenantContext.getTenantId();
        log.debug("Getting SMS: {} for tenant: {}", id, tenantId);

        SmsMessageResponse sms = smsService.getSmsById(id, tenantId);
        return ResponseEntity.ok(ApiResponse.success(sms));
    }
}
