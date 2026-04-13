package com.parlament.service;

import com.parlament.model.UserSession;
import com.parlament.persistence.entity.UserSessionEntity;
import com.parlament.persistence.repo.UserSessionJpaRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

/**
 * Manages user conversation sessions and checkout state.
 */
@Service
public class SessionService {

    private final UserSessionJpaRepository sessionRepo;

    public SessionService(UserSessionJpaRepository sessionRepo) {
        this.sessionRepo = sessionRepo;
    }

    /**
     * Returns the session for a user, creating one if it doesn't exist.
     */
    @Transactional
    public UserSession getOrCreate(long userId) {
        UserSessionEntity entity = sessionRepo.findById(userId).orElseGet(() -> {
            UserSessionEntity e = new UserSessionEntity();
            e.setTelegramUserId(userId);
            e.setState(UserSession.State.IDLE.name());
            e.setUpdatedAt(OffsetDateTime.now());
            return sessionRepo.save(e);
        });

        UserSession session = new UserSession(userId);
        try {
            session.setState(UserSession.State.valueOf(entity.getState()));
        } catch (Exception ignored) {
            session.setState(UserSession.State.IDLE);
        }
        session.setCheckoutName(entity.getCheckoutName());
        session.setCheckoutPhone(entity.getCheckoutPhone());
        session.setCheckoutAddress(entity.getCheckoutAddress());
        return session;
    }

    /**
     * Resets the checkout state for a user session.
     */
    @Transactional
    public void resetCheckout(long userId) {
        UserSessionEntity entity = sessionRepo.findById(userId).orElse(null);
        if (entity == null) return;
        entity.setState(UserSession.State.IDLE.name());
        entity.setCheckoutName(null);
        entity.setCheckoutPhone(null);
        entity.setCheckoutAddress(null);
        entity.setUpdatedAt(OffsetDateTime.now());
        sessionRepo.save(entity);
    }

    /**
     * Returns the current state of a user's session.
     */
    @Transactional(readOnly = true)
    public UserSession.State getState(long userId) {
        return getOrCreate(userId).getState();
    }

    /**
     * Sets the conversation state for a user.
     */
    @Transactional
    public void setState(long userId, UserSession.State state) {
        UserSessionEntity entity = sessionRepo.findById(userId).orElseGet(() -> {
            UserSessionEntity e = new UserSessionEntity();
            e.setTelegramUserId(userId);
            e.setState(UserSession.State.IDLE.name());
            e.setUpdatedAt(OffsetDateTime.now());
            return e;
        });
        entity.setState(state.name());
        entity.setUpdatedAt(OffsetDateTime.now());
        sessionRepo.save(entity);
    }

    @Transactional
    public void setCheckoutName(long userId, String name) {
        UserSessionEntity entity = sessionRepo.findById(userId).orElseGet(() -> {
            UserSessionEntity e = new UserSessionEntity();
            e.setTelegramUserId(userId);
            e.setState(UserSession.State.IDLE.name());
            e.setUpdatedAt(OffsetDateTime.now());
            return e;
        });
        entity.setCheckoutName(name);
        entity.setUpdatedAt(OffsetDateTime.now());
        sessionRepo.save(entity);
    }

    @Transactional
    public void setCheckoutPhone(long userId, String phone) {
        UserSessionEntity entity = sessionRepo.findById(userId).orElseGet(() -> {
            UserSessionEntity e = new UserSessionEntity();
            e.setTelegramUserId(userId);
            e.setState(UserSession.State.IDLE.name());
            e.setUpdatedAt(OffsetDateTime.now());
            return e;
        });
        entity.setCheckoutPhone(phone);
        entity.setUpdatedAt(OffsetDateTime.now());
        sessionRepo.save(entity);
    }

    @Transactional
    public void setCheckoutAddress(long userId, String address) {
        UserSessionEntity entity = sessionRepo.findById(userId).orElseGet(() -> {
            UserSessionEntity e = new UserSessionEntity();
            e.setTelegramUserId(userId);
            e.setState(UserSession.State.IDLE.name());
            e.setUpdatedAt(OffsetDateTime.now());
            return e;
        });
        entity.setCheckoutAddress(address);
        entity.setUpdatedAt(OffsetDateTime.now());
        sessionRepo.save(entity);
    }
}
