package io.factorialsystems.auth.controller;

import io.factorialsystems.auth.model.dto.request.CreateRoleRequest;
import io.factorialsystems.auth.model.dto.response.ApiResponse;
import io.factorialsystems.auth.model.dto.response.RoleResponse;
import io.factorialsystems.auth.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@Tag(name = "Role Management", description = "Role management endpoints")
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    @PreAuthorize("hasAuthority('role:read')")
    @Operation(summary = "List roles", description = "List all roles available for the tenant")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> listRoles(
            @AuthenticationPrincipal Jwt jwt) {
        
        UUID tenantId = UUID.fromString(jwt.getClaimAsString("tenant_id"));
        List<RoleResponse> roles = roleService.listRoles(tenantId);
        return ResponseEntity.ok(ApiResponse.success(roles));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('role:read')")
    @Operation(summary = "Get role", description = "Get role details by ID")
    public ResponseEntity<ApiResponse<RoleResponse>> getRole(@PathVariable UUID id) {
        RoleResponse response = roleService.getRole(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('role:create')")
    @Operation(summary = "Create custom role", description = "Create a custom role for the tenant")
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateRoleRequest request) {
        
        UUID tenantId = UUID.fromString(jwt.getClaimAsString("tenant_id"));
        RoleResponse response = roleService.createCustomRole(tenantId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Role created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('role:update')")
    @Operation(summary = "Update role", description = "Update a custom role")
    public ResponseEntity<ApiResponse<RoleResponse>> updateRole(
            @PathVariable UUID id,
            @Valid @RequestBody CreateRoleRequest request) {
        
        RoleResponse response = roleService.updateRole(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Role updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('role:delete')")
    @Operation(summary = "Delete role", description = "Delete a custom role")
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable UUID id) {
        roleService.deleteRole(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Role deleted successfully"));
    }

    @PostMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('role:update')")
    @Operation(summary = "Assign permissions", description = "Assign permissions to a role")
    public ResponseEntity<ApiResponse<RoleResponse>> assignPermissions(
            @PathVariable UUID id,
            @RequestBody Set<String> permissionCodes) {
        
        RoleResponse response = roleService.assignPermissions(id, permissionCodes);
        return ResponseEntity.ok(ApiResponse.success(response, "Permissions assigned successfully"));
    }
}
