package io.factorialsystems.auth.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.*;

@Entity @Table(name = "permissions") @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Permission {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name = "code", nullable = false, unique = true, length = 100) private String code;
    @Column(name = "resource", nullable = false, length = 50) private String resource;
    @Column(name = "action", nullable = false, length = 50) private String action;
    @Column(name = "description", length = 255) private String description;
    @ManyToMany(mappedBy = "permissions") @Builder.Default private Set<Role> roles = new HashSet<>();
}
