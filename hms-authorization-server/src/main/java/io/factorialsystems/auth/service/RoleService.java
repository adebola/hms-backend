package io.factorialsystems.auth.service;

import io.factorialsystems.auth.exception.BusinessException;
import io.factorialsystems.auth.exception.DuplicateResourceException;
import io.factorialsystems.auth.exception.ResourceNotFoundException;
import io.factorialsystems.auth.model.dto.request.CreateRoleRequest;
import io.factorialsystems.auth.model.dto.response.PermissionResponse;
import io.factorialsystems.auth.model.dto.response.RoleResponse;
import io.factorialsystems.auth.model.entity.Permission;
import io.factorialsystems.auth.model.entity.Role;
import io.factorialsystems.auth.model.entity.Tenant;
import io.factorialsystems.auth.repository.PermissionRepository;
import io.factorialsystems.auth.repository.RoleRepository;
import io.factorialsystems.auth.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final TenantRepository tenantRepository;

    @Transactional(readOnly = true)
    public List<RoleResponse> listRoles(UUID tenantId) {
        return roleRepository.findAllRolesForTenant(tenantId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RoleResponse getRole(UUID roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", roleId.toString()));
        return mapToResponse(role);
    }

    @Transactional
    public RoleResponse createCustomRole(UUID tenantId, CreateRoleRequest request) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", tenantId.toString()));

        if (roleRepository.existsByTenantIdAndCode(tenantId, request.getCode())) {
            throw new DuplicateResourceException("Role", "code", request.getCode());
        }

        Role role = Role.builder()
                .tenant(tenant)
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .systemRole(false)
                .build();

        if (request.getPermissionCodes() != null && !request.getPermissionCodes().isEmpty()) {
            List<Permission> permissions = permissionRepository.findByCodeIn(request.getPermissionCodes());
            permissions.forEach(role::addPermission);
        }

        role = roleRepository.save(role);
        log.info("Custom role created: {} for tenant: {}", role.getCode(), tenant.getCode());

        return mapToResponse(role);
    }

    @Transactional
    public RoleResponse updateRole(UUID roleId, CreateRoleRequest request) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", roleId.toString()));

        if (role.isSystemRole()) {
            throw new BusinessException("SYSTEM_ROLE_IMMUTABLE", 
                    "System roles cannot be modified", HttpStatus.FORBIDDEN);
        }

        role.setName(request.getName());
        role.setDescription(request.getDescription());

        role = roleRepository.save(role);
        return mapToResponse(role);
    }

    @Transactional
    public void deleteRole(UUID roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", roleId.toString()));

        if (role.isSystemRole()) {
            throw new BusinessException("SYSTEM_ROLE_IMMUTABLE", 
                    "System roles cannot be deleted", HttpStatus.FORBIDDEN);
        }

        if (!role.getUsers().isEmpty()) {
            throw new BusinessException("ROLE_IN_USE", 
                    "Cannot delete role that is assigned to users", HttpStatus.CONFLICT);
        }

        roleRepository.delete(role);
        log.info("Role deleted: {}", role.getCode());
    }

    @Transactional
    public RoleResponse assignPermissions(UUID roleId, Set<String> permissionCodes) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", roleId.toString()));

        if (role.isSystemRole()) {
            throw new BusinessException("SYSTEM_ROLE_IMMUTABLE", 
                    "System role permissions cannot be modified", HttpStatus.FORBIDDEN);
        }

        List<Permission> permissions = permissionRepository.findByCodeIn(permissionCodes);
        role.getPermissions().clear();
        permissions.forEach(role::addPermission);

        role = roleRepository.save(role);
        return mapToResponse(role);
    }

    private RoleResponse mapToResponse(Role role) {
        return RoleResponse.builder()
                .id(role.getId())
                .code(role.getCode())
                .name(role.getName())
                .description(role.getDescription())
                .systemRole(role.isSystemRole())
                .tenantId(role.getTenant() != null ? role.getTenant().getId() : null)
                .permissions(role.getPermissions().stream()
                        .map(p -> PermissionResponse.builder()
                                .id(p.getId())
                                .code(p.getCode())
                                .resource(p.getResource())
                                .action(p.getAction())
                                .description(p.getDescription())
                                .build())
                        .collect(Collectors.toSet()))
                .build();
    }
}
