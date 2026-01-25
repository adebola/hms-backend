package io.factorialsystems.auth.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.*;

@Entity @Table(name = "roles") @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Role {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "tenant_id") private Tenant tenant;
    @Column(name = "code", nullable = false, length = 50) private String code;
    @Column(name = "name", nullable = false, length = 100) private String name;
    @Column(name = "description", length = 255) private String description;
    @Column(name = "system_role", nullable = false) @Builder.Default private boolean systemRole = false;
    @ManyToMany(fetch = FetchType.EAGER) @JoinTable(name = "role_permissions", joinColumns = @JoinColumn(name = "role_id"), inverseJoinColumns = @JoinColumn(name = "permission_id"))
    @Builder.Default private Set<Permission> permissions = new HashSet<>();
    @ManyToMany(mappedBy = "roles") @Builder.Default private Set<User> users = new HashSet<>();
    public void addPermission(Permission p) { permissions.add(p); p.getRoles().add(this); }
    public boolean isSystemRole() { return systemRole; }
}
