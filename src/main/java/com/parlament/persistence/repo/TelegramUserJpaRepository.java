package com.parlament.persistence.repo;

import com.parlament.persistence.entity.TelegramUserEntity;
import com.parlament.persistence.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TelegramUserJpaRepository extends JpaRepository<TelegramUserEntity, Long> {
    List<TelegramUserEntity> findByRole(Role role);
    boolean existsByRole(Role role);
    Page<TelegramUserEntity> findAll(Pageable pageable);
}
