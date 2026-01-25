package io.factorialsystems.auth.repository;
import io.factorialsystems.auth.model.entity.User;
import io.factorialsystems.auth.model.enums.UserStatus;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.*;
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    @Query("SELECT u FROM User u WHERE u.tenant.id = :tenantId AND (u.username = :identifier OR u.email = :identifier)")
    Optional<User> findByTenantIdAndUsernameOrEmail(@Param("tenantId") UUID tenantId, @Param("identifier") String identifier);
    boolean existsByTenantIdAndUsername(UUID tenantId, String username);
    boolean existsByTenantIdAndEmail(UUID tenantId, String email);
    @Query("SELECT u FROM User u WHERE u.tenant.id = :tenantId AND (:status IS NULL OR u.status = :status) AND (:search IS NULL OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<User> searchUsersInTenant(@Param("tenantId") UUID tenantId, @Param("status") UserStatus status, @Param("search") String search, Pageable pageable);
    @Modifying @Query("UPDATE User u SET u.lastLoginAt = :loginTime, u.lastLoginIp = :ip, u.failedLoginAttempts = 0 WHERE u.id = :userId")
    void updateLoginSuccess(@Param("userId") UUID userId, @Param("loginTime") LocalDateTime loginTime, @Param("ip") String ip);
    long countByTenantId(UUID tenantId);
    long countByTenantIdAndStatus(UUID tenantId, UserStatus status);
}
