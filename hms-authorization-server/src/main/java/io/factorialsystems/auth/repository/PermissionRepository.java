package io.factorialsystems.auth.repository;
import io.factorialsystems.auth.model.entity.Permission;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.util.*;
@Repository
public interface PermissionRepository extends JpaRepository<Permission, UUID> {
    Optional<Permission> findByCode(String code);
    List<Permission> findByCodeIn(Set<String> codes);
    @Query("SELECT DISTINCT p.resource FROM Permission p ORDER BY p.resource") List<String> findAllResources();
}
