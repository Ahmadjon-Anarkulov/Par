package com.parlament.service;

import com.parlament.model.UserSession;
import com.parlament.persistence.entity.UserSessionEntity;
import com.parlament.persistence.repo.UserSessionJpaRepository;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SessionServiceTest {

    @Test
    void resetCheckout_setsStateToIdle() {
        Map<Long, UserSessionEntity> store = new HashMap<>();

        UserSessionJpaRepository repo = mock(UserSessionJpaRepository.class);
        when(repo.findById(42L)).thenAnswer(inv -> Optional.ofNullable(store.get(42L)));
        when(repo.save(any(UserSessionEntity.class))).thenAnswer(inv -> {
            UserSessionEntity e = inv.getArgument(0);
            store.put(e.getTelegramUserId(), e);
            return e;
        });

        SessionService service = new SessionService(repo);
        long userId = 42L;

        service.setState(userId, UserSession.State.AWAITING_PHONE);
        service.resetCheckout(userId);

        assertThat(service.getState(userId)).isEqualTo(UserSession.State.IDLE);
    }
}

