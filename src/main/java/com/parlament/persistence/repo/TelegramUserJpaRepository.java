package com.parlament.persistence.repo;

import com.parlament.persistence.entity.TelegramUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TelegramUserJpaRepository extends JpaRepository<TelegramUserEntity, Long> {
}

