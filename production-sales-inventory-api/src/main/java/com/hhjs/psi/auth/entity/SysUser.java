package com.hhjs.psi.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "sys_user")
public class SysUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50, unique = true)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false, length = 80)
    private String nickname;

    @ManyToMany(fetch = LAZY)
    @JoinTable(
            name = "sys_user_role",
            joinColumns = @JoinColumn(name = "sys_user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @OrderBy("sortOrder ASC, code ASC")
    private Set<Role> roles = new LinkedHashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AdminStatus status = AdminStatus.ACTIVE;

    @Column(name = "token_version", nullable = false)
    private Integer tokenVersion = 0;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected SysUser() {
    }

    public static SysUser create(String username, String passwordHash, String nickname) {
        SysUser sysUser = new SysUser();
        sysUser.username = username;
        sysUser.passwordHash = passwordHash;
        sysUser.nickname = nickname;
        sysUser.status = AdminStatus.ACTIVE;
        sysUser.tokenVersion = 0;
        return sysUser;
    }

    public boolean isActive() {
        return AdminStatus.ACTIVE.equals(status);
    }

    public void recordLogin(Instant loginAt) {
        this.lastLoginAt = loginAt;
    }

    public void increaseTokenVersion() {
        this.tokenVersion = this.tokenVersion + 1;
    }

    public void updatePasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
        increaseTokenVersion();
    }

    public void activate() {
        this.status = AdminStatus.ACTIVE;
        increaseTokenVersion();
    }

    public void disable() {
        this.status = AdminStatus.DISABLED;
        increaseTokenVersion();
    }

    public void addRole(Role role) {
        this.roles.add(role);
    }

    public Set<String> getRoleCodes() {
        return roles.stream()
                .filter(Role::isEnabled)
                .sorted(Comparator.comparing(Role::getSortOrder).thenComparing(Role::getCode))
                .map(Role::getCode)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public Set<String> getPermissionCodes() {
        return roles.stream()
                .filter(Role::isEnabled)
                .sorted(Comparator.comparing(Role::getSortOrder).thenComparing(Role::getCode))
                .flatMap(role -> role.getPermissions().stream())
                .filter(Permission::isEnabled)
                .sorted(Comparator.comparing(Permission::getSortOrder).thenComparing(Permission::getCode))
                .map(Permission::getCode)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getNickname() {
        return nickname;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public AdminStatus getStatus() {
        return status;
    }

    public Integer getTokenVersion() {
        return tokenVersion;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
