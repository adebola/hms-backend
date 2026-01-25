package io.factorialsystems.auth.repository;
import io.factorialsystems.auth.model.entity.Role;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.*;
@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {
    @Query("SELECT r FROM Role r WHERE r.systemRole = true") List<Role> findSystemRoles();
    @Query("SELECT r FROM Role r WHERE r.systemRole = true AND r.code = :code") Optional<Role> findSystemRoleByCode(@Param("code") String code);
    @Query("SELECT r FROM Role r WHERE r.tenant.id = :tenantId OR r.systemRole = true") List<Role> findAllRolesForTenant(@Param("tenantId") UUID tenantId);
    boolean existsByTenantIdAndCode(UUID tenantId, String code);
    List<Role> findByTenantId(UUID tenantId);
}
