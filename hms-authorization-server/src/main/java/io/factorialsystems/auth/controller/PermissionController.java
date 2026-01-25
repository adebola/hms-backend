package io.factorialsystems.auth.controller;

import io.factorialsystems.auth.model.dto.response.ApiResponse;
import io.factorialsystems.auth.model.dto.response.PermissionResponse;
import io.factorialsystems.auth.model.entity.Permission;
import io.factorialsystems.auth.repository.PermissionRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
@Tag(name = "Permission Management", description = "Permission listing endpoints")
public class PermissionController {

    private final PermissionRepository permissionRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('permission:read')")
    @Operation(summary = "List permissions", description = "List all available permissions")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> listPermissions() {
        List<PermissionResponse> permissions = permissionRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(permissions));
    }

    @GetMapping("/resources")
    @PreAuthorize("hasAuthority('permission:read')")
    @Operation(summary = "List resources", description = "List all unique permission resources")
    public ResponseEntity<ApiResponse<List<String>>> listResources() {
        List<String> resources = permissionRepository.findAllResources();
        return ResponseEntity.ok(ApiResponse.success(resources));
    }

    private PermissionResponse mapToResponse(Permission permission) {
        return PermissionResponse.builder()
                .id(permission.getId())
                .code(permission.getCode())
                .resource(permission.getResource())
                .action(permission.getAction())
                .description(permission.getDescription())
                .build();
    }
}
