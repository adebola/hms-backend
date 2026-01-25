package io.factorialsystems.auth.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity implements Serializable {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @CreatedDate @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;
    @CreatedBy @Column(name = "created_by", length = 100) private String createdBy;
    @LastModifiedDate @Column(name = "updated_at") private LocalDateTime updatedAt;
    @LastModifiedBy @Column(name = "updated_by", length = 100) private String updatedBy;
    @Version @Column(name = "version") private Long version;
}
