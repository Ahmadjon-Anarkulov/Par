package com.parlament.persistence.repo;

import com.parlament.persistence.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderJpaRepository extends JpaRepository<OrderEntity, String> {
    List<OrderEntity> findByTelegramUserIdOrderByCreatedAtDesc(Long telegramUserId);
    long countByTelegramUserId(Long telegramUserId);
}

