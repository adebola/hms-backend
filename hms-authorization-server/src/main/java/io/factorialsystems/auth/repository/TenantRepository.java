package io.factorialsystems.auth.repository;
import io.factorialsystems.auth.model.entity.Tenant;
import io.factorialsystems.auth.model.enums.TenantStatus;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.*;
@Repository
public interface TenantRepository extends JpaRepository<Tenant, UUID> {
    Optional<Tenant> findByCode(String code);
    Optional<Tenant> findBySlug(String slug);
    Optional<Tenant> findByEmail(String email);
    boolean existsByCode(String code);
    boolean existsBySlug(String slug);
    boolean existsByEmail(String email);
    @Query("SELECT t FROM Tenant t WHERE (:status IS NULL OR t.status = :status) AND (:search IS NULL OR LOWER(t.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(t.code) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Tenant> searchTenants(@Param("status") TenantStatus status, @Param("search") String search, Pageable pageable);
}
