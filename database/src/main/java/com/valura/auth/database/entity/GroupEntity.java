package com.valura.auth.database.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;
import java.time.Instant;

@Entity
@Table(name = "groups",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "external_id"),
                @UniqueConstraint(columnNames = "display_name")
        })
public class GroupEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", nullable = false)
    private String externalId;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @ManyToMany(mappedBy = "groups")
    private Set<UserEntity> members = new HashSet<>();

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

    public String getDisplayName() {
        return displayName;
    }

    public Set<UserEntity> getMembers() {
        return members;
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

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setMembers(Set<UserEntity> members) {
        this.members = members != null ? members : new HashSet<>();
    }

    // Helper methods for managing bidirectional relationship
    public void addMember(UserEntity user) {
        members.add(user);
        user.getGroups().add(this);
    }

    public void removeMember(UserEntity user) {
        members.remove(user);
        user.getGroups().remove(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GroupEntity that = (GroupEntity) o;
        return Objects.equals(externalId, that.externalId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(externalId);
    }

    @Override
    public String toString() {
        return "GroupEntity{" +
                "id=" + id +
                ", externalId='" + externalId + '\'' +
                ", displayName='" + displayName + '\'' +
                ", membersCount=" + members.size() +
                '}';
    }
}