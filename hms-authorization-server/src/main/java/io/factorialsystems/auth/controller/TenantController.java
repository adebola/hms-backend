package io.factorialsystems.auth.controller;

import io.factorialsystems.auth.model.dto.request.TenantRegistrationRequest;
import io.factorialsystems.auth.model.dto.response.ApiResponse;
import io.factorialsystems.auth.model.dto.response.TenantResponse;
import io.factorialsystems.auth.model.enums.TenantStatus;
import io.factorialsystems.auth.service.TenantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tenants")
@RequiredArgsConstructor
@Tag(name = "Tenant Management", description = "Tenant registration and management endpoints")
public class TenantController {

    private final TenantService tenantService;

    @PostMapping("/register")
    @Operation(summary = "Register new tenant", description = "Register a new hospital/facility as a tenant")
    public ResponseEntity<ApiResponse<TenantResponse>> register(
            @Valid @RequestBody TenantRegistrationRequest request) {
        
        TenantResponse response = tenantService.registerTenant(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Tenant registered successfully. Pending verification."));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('tenant:read')")
    @Operation(summary = "List tenants", description = "List all tenants with optional filters")
    public ResponseEntity<ApiResponse<Page<TenantResponse>>> listTenants(
            @RequestParam(required = false) TenantStatus status,
            @RequestParam(required = false) String search,
            Pageable pageable) {
        
        Page<TenantResponse> tenants = tenantService.listTenants(status, search, pageable);
        return ResponseEntity.ok(ApiResponse.success(tenants));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('tenant:read')")
    @Operation(summary = "Get tenant by ID", description = "Get tenant details by ID")
    public ResponseEntity<ApiResponse<TenantResponse>> getTenant(@PathVariable UUID id) {
        TenantResponse response = tenantService.getTenant(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/code/{code}")
    @PreAuthorize("hasAuthority('tenant:read')")
    @Operation(summary = "Get tenant by code", description = "Get tenant details by code")
    public ResponseEntity<ApiResponse<TenantResponse>> getTenantByCode(@PathVariable String code) {
        TenantResponse response = tenantService.getTenantByCode(code);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/my")
    @Operation(summary = "Get my tenant", description = "Get current user's tenant details")
    public ResponseEntity<ApiResponse<TenantResponse>> getMyTenant(
            @AuthenticationPrincipal Jwt jwt) {
        
        String tenantId = jwt.getClaimAsString("tenant_id");
        TenantResponse response = tenantService.getTenant(UUID.fromString(tenantId));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('tenant:activate')")
    @Operation(summary = "Activate tenant", description = "Activate a pending tenant")
    public ResponseEntity<ApiResponse<TenantResponse>> activateTenant(
            @PathVariable UUID id,
            @RequestParam(required = false) String notes) {
        
        TenantResponse response = tenantService.activateTenant(id, notes);
        return ResponseEntity.ok(ApiResponse.success(response, "Tenant activated successfully"));
    }

    @PostMapping("/{id}/suspend")
    @PreAuthorize("hasAuthority('tenant:suspend')")
    @Operation(summary = "Suspend tenant", description = "Suspend an active tenant")
    public ResponseEntity<ApiResponse<TenantResponse>> suspendTenant(
            @PathVariable UUID id,
            @RequestParam String reason) {
        
        TenantResponse response = tenantService.suspendTenant(id, reason);
        return ResponseEntity.ok(ApiResponse.success(response, "Tenant suspended"));
    }
}
