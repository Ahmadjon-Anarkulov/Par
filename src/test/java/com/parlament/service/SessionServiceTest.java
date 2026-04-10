package com.parlament.service;

import com.parlament.model.UserSession;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SessionServiceTest {

    @Test
    void resetCheckout_setsStateToIdle() {
        SessionService service = new SessionService();
        long userId = 42L;

        service.setState(userId, UserSession.State.AWAITING_PHONE);
        service.resetCheckout(userId);

        assertThat(service.getState(userId)).isEqualTo(UserSession.State.IDLE);
    }
}

