package com.valura.auth.database.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;
import java.time.Instant;

@Entity
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "external_id"),
                @UniqueConstraint(columnNames = "user_name"),
                @UniqueConstraint(columnNames = "email")
        })
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", nullable = false)
    private String externalId;

    @Column(name = "user_name", nullable = false)
    private String userName;

    @Column(name = "display_name")
    private String displayName;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private boolean active;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_groups",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "group_id")
    )
    private Set<GroupEntity> groups = new HashSet<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getUserName() {
        return userName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmail() {
        return email;
    }

    public boolean isActive() {
        return active;
    }

    public Set<GroupEntity> getGroups() {
        return groups;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setGroups(Set<GroupEntity> groups) {
        this.groups = groups != null ? groups : new HashSet<>();
    }

    // Helper methods for managing bidirectional relationship
    public void addGroup(GroupEntity group) {
        groups.add(group);
        group.getMembers().add(this);
    }

    public void removeGroup(GroupEntity group) {
        groups.remove(group);
        group.getMembers().remove(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserEntity that = (UserEntity) o;
        return Objects.equals(externalId, that.externalId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(externalId);
    }

    @Override
    public String toString() {
        return "UserEntity{" +
                "id=" + id +
                ", externalId='" + externalId + '\'' +
                ", userName='" + userName + '\'' +
                ", email='" + email + '\'' +
                ", active=" + active +
                ", groupsCount=" + groups.size() +
                '}';
    }
}