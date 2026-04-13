package com.parlament.persistence.repo;

import com.parlament.persistence.entity.UserSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSessionJpaRepository extends JpaRepository<UserSessionEntity, Long> {
}

