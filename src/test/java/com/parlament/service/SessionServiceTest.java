package com.parlament.service;

import com.parlament.model.UserSession;
import com.parlament.persistence.entity.UserSessionEntity;
import com.parlament.persistence.repo.UserSessionJpaRepository;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class SessionServiceTest {

    @Test
    void resetCheckout_setsStateToIdle() {
        Map<Long, UserSessionEntity> store = new HashMap<>();

        // NOTE: Mockito (ByteBuddy) is not compatible with Java 25 in this environment.
        UserSessionJpaRepository repo = (UserSessionJpaRepository) Proxy.newProxyInstance(
                UserSessionJpaRepository.class.getClassLoader(),
                new Class[]{UserSessionJpaRepository.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "findById" -> Optional.ofNullable(store.get((Long) args[0]));
                    case "save" -> {
                        UserSessionEntity e = (UserSessionEntity) args[0];
                        store.put(e.getTelegramUserId(), e);
                        yield e;
                    }
                    default -> null;
                }
        );

        SessionService service = new SessionService(repo);
        long userId = 42L;

        service.setState(userId, UserSession.State.AWAITING_PHONE);
        service.resetCheckout(userId);

        assertThat(service.getState(userId)).isEqualTo(UserSession.State.IDLE);
    }
}

