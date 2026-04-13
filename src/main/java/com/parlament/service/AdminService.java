package com.parlament.service;

import com.parlament.config.AdminProperties;
import com.parlament.persistence.entity.Role;
import com.parlament.persistence.entity.TelegramUserEntity;
import com.parlament.persistence.repo.TelegramUserJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminService.class);

    private final TelegramUserJpaRepository telegramUserRepo;
    private final AdminProperties adminProperties;

    public AdminService(TelegramUserJpaRepository telegramUserRepo, AdminProperties adminProperties) {
        this.telegramUserRepo = telegramUserRepo;
        this.adminProperties = adminProperties;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void ensureInitialAdmins() {
        if (telegramUserRepo.existsByRole(Role.ADMIN)) return;

        List<Long> initialIds = adminProperties.getInitialIds();
        if (initialIds == null || initialIds.isEmpty()) {
            log.warn("No admins exist yet, and admin.initial-ids is empty.");
            return;
        }

        OffsetDateTime now = OffsetDateTime.now();
        for (Long id : initialIds) {
            if (id == null) continue;
            TelegramUserEntity user = telegramUserRepo.findById(id).orElseGet(() -> {
                TelegramUserEntity e = new TelegramUserEntity();
                e.setTelegramUserId(id);
                e.setCreatedAt(now);
                e.setUpdatedAt(now);
                return e;
            });
            user.setRole(Role.ADMIN);
            user.setUpdatedAt(now);
            telegramUserRepo.save(user);
            log.info("Bootstrapped ADMIN userId={}", id);
        }

        if (!telegramUserRepo.existsByRole(Role.ADMIN)) {
            log.warn("Tried to bootstrap admins, but still no ADMIN users exist.");
        }
    }

    @Transactional(readOnly = true)
    public boolean isAdmin(long telegramUserId) {
        return telegramUserRepo.findById(telegramUserId)
                .map(TelegramUserEntity::isAdmin)
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public Set<Long> getAdminIds() {
        return telegramUserRepo.findByRole(Role.ADMIN).stream()
                .map(TelegramUserEntity::getTelegramUserId)
                .collect(Collectors.toSet());
    }

    @Transactional
    public void promoteToAdmin(long actorTelegramUserId, long targetTelegramUserId) {
        if (!isAdmin(actorTelegramUserId)) {
            throw new SecurityException("Forbidden: only admins can promote other users.");
        }

        OffsetDateTime now = OffsetDateTime.now();
        TelegramUserEntity target = telegramUserRepo.findById(targetTelegramUserId).orElseGet(() -> {
            TelegramUserEntity e = new TelegramUserEntity();
            e.setTelegramUserId(targetTelegramUserId);
            e.setCreatedAt(now);
            e.setUpdatedAt(now);
            return e;
        });

        if (target.isAdmin()) {
            log.info("Admin action: actor={} attempted to promote existing admin={}", actorTelegramUserId, targetTelegramUserId);
            return;
        }

        target.setRole(Role.ADMIN);
        target.setUpdatedAt(now);
        telegramUserRepo.save(target);
        log.info("Admin action: actor={} promoted user={} to ADMIN", actorTelegramUserId, targetTelegramUserId);
    }
}

