package com.parlament.persistence.repo;

import com.parlament.persistence.entity.UserSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;

public interface UserSessionJpaRepository extends JpaRepository<UserSessionEntity, Long> {

    @Modifying
    @Query("DELETE FROM UserSessionEntity e WHERE e.updatedAt < :cutoff")
    int deleteByUpdatedAtBefore(@Param("cutoff") OffsetDateTime cutoff);
}

