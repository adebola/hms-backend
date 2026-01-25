package io.factorialsystems.auth.controller;

import io.factorialsystems.auth.model.dto.request.CreateUserRequest;
import io.factorialsystems.auth.model.dto.response.ApiResponse;
import io.factorialsystems.auth.model.dto.response.UserResponse;
import io.factorialsystems.auth.service.UserService;
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
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "User management endpoints")
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAuthority('user:read')")
    @Operation(summary = "List users", description = "List users in the current tenant")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> listUsers(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            Pageable pageable) {
        
        UUID tenantId = UUID.fromString(jwt.getClaimAsString("tenant_id"));
        Page<UserResponse> users = userService.listUsers(tenantId, status, search, pageable);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('user:create')")
    @Operation(summary = "Create user", description = "Create a new user in the current tenant")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateUserRequest request) {
        
        UUID tenantId = UUID.fromString(jwt.getClaimAsString("tenant_id"));
        UserResponse response = userService.createUser(tenantId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "User created successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('user:read')")
    @Operation(summary = "Get user", description = "Get user details by ID")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable UUID id) {
        UserResponse response = userService.getUser(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('user:update')")
    @Operation(summary = "Update user", description = "Update user details")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody CreateUserRequest request) {
        
        UserResponse response = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "User updated successfully"));
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('user:deactivate')")
    @Operation(summary = "Deactivate user", description = "Deactivate a user account")
    public ResponseEntity<ApiResponse<Void>> deactivateUser(@PathVariable UUID id) {
        userService.deactivateUser(id);
        return ResponseEntity.ok(ApiResponse.success(null, "User deactivated successfully"));
    }
}
