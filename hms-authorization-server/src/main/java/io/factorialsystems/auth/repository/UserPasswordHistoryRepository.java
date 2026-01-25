package io.factorialsystems.auth.repository;
import io.factorialsystems.auth.model.entity.UserPasswordHistory;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.*;
@Repository
public interface UserPasswordHistoryRepository extends JpaRepository<UserPasswordHistory, UUID> {
    @Query("SELECT ph FROM UserPasswordHistory ph WHERE ph.user.id = :userId ORDER BY ph.createdAt DESC")
    List<UserPasswordHistory> findRecentByUserId(@Param("userId") UUID userId);
    @Modifying @Query(value = "DELETE FROM user_password_history WHERE user_id = :userId AND id NOT IN (SELECT id FROM user_password_history WHERE user_id = :userId ORDER BY created_at DESC LIMIT :keepCount)", nativeQuery = true)
    void deleteOldPasswordHistory(@Param("userId") UUID userId, @Param("keepCount") int keepCount);
}
