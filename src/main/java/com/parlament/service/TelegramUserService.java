package com.parlament.service;

import com.parlament.persistence.entity.TelegramUserEntity;
import com.parlament.persistence.repo.TelegramUserJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.User;

import java.time.OffsetDateTime;

@Service
public class TelegramUserService {

    private final TelegramUserJpaRepository telegramUserRepo;

    public TelegramUserService(TelegramUserJpaRepository telegramUserRepo) {
        this.telegramUserRepo = telegramUserRepo;
    }

    @Transactional
    public void upsertFromTelegram(User tgUser) {
        if (tgUser == null) return;
        Long id = tgUser.getId();
        if (id == null) return;

        OffsetDateTime now = OffsetDateTime.now();

        TelegramUserEntity entity = telegramUserRepo.findById(id).orElseGet(() -> {
            TelegramUserEntity e = new TelegramUserEntity();
            e.setTelegramUserId(id);
            e.setCreatedAt(now);
            return e;
        });

        entity.setUsername(tgUser.getUserName());
        entity.setFirstName(tgUser.getFirstName());
        entity.setLastName(tgUser.getLastName());
        entity.setUpdatedAt(now);
        telegramUserRepo.save(entity);
    }
}

