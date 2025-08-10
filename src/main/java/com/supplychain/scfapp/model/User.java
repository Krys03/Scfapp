package com.supplychain.scfapp.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Set;

@Entity
@Table(
    name = "app_user",
    uniqueConstraints = @UniqueConstraint(columnNames = "username")
)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120, unique = true)
    private String username;

    @Column(nullable = false, length = 200)
    private String password;

    // IMPORTANT : le nom de colonne est "roles" (pluriel) pour matcher la DB existante
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "roles", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private Set<Role> roles;

    @Column(name = "password_changed_at")
    private Instant passwordChangedAt; // null = jamais changé depuis la création

    // --- getters / setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Set<Role> getRoles() { return roles; }
    public void setRoles(Set<Role> roles) { this.roles = roles; }

    public Instant getPasswordChangedAt() { return passwordChangedAt; }
    public void setPasswordChangedAt(Instant passwordChangedAt) { this.passwordChangedAt = passwordChangedAt; }
}
