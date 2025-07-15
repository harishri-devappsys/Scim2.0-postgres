package com.valura.auth.database.repository;
import com.valura.auth.database.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByExternalId(String externalId);

    Optional<UserEntity> findByUserName(String userName);

    Optional<UserEntity> findByEmail(String email);

    @Query("SELECT u FROM UserEntity u WHERE " +
            "LOWER(u.userName) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "LOWER(u.displayName) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :term, '%'))")
    Page<UserEntity> searchUsers(@Param("term") String term, Pageable pageable);

    boolean existsByUserName(String userName);

    boolean existsByEmail(String email);

    boolean existsByExternalId(String externalId);

    @Query("SELECT u FROM UserEntity u JOIN u.groups g WHERE g.externalId = :groupExternalId")
    List<UserEntity> findUsersByGroupExternalId(@Param("groupExternalId") String groupExternalId);

    @Query("SELECT u FROM UserEntity u WHERE u.active = :active")
    Page<UserEntity> findByActive(@Param("active") boolean active, Pageable pageable);

    @Query("SELECT COUNT(g) FROM UserEntity u JOIN u.groups g WHERE u.externalId = :userExternalId")
    long countGroupsByUserExternalId(@Param("userExternalId") String userExternalId);

    void deleteByExternalId(String externalId);
}