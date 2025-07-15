package com.valura.auth.database.repository;

import com.valura.auth.database.entity.GroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<GroupEntity, Long> {
    Optional<GroupEntity> findByExternalId(String externalId);

    Optional<GroupEntity> findByDisplayName(String displayName);

    @Query("SELECT g FROM GroupEntity g WHERE g.displayName LIKE %:term%")
    List<GroupEntity> findByDisplayNameContaining(@Param("term") String term);

    boolean existsByDisplayName(String displayName);

    boolean existsByExternalId(String externalId);

    @Query("SELECT g FROM GroupEntity g JOIN g.members m WHERE m.externalId = :userExternalId")
    List<GroupEntity> findGroupsByUserExternalId(@Param("userExternalId") String userExternalId);

    @Query("SELECT COUNT(m) FROM GroupEntity g JOIN g.members m WHERE g.externalId = :groupExternalId")
    long countMembersByGroupExternalId(@Param("groupExternalId") String groupExternalId);

    void deleteByExternalId(String externalId);
}