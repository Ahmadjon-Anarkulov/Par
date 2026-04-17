package com.parlament.service;

import com.parlament.model.UserSession;
import com.parlament.persistence.entity.UserSessionEntity;
import com.parlament.persistence.repo.UserSessionJpaRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

/**
 * Manages user conversation sessions and checkout state.
 * All state is persisted to PostgreSQL via UserSessionEntity.
 */
@Service
public class SessionService {

    private static final Logger log = LoggerFactory.getLogger(SessionService.class);

    private final UserSessionJpaRepository sessionRepo;

    public SessionService(UserSessionJpaRepository sessionRepo) {
        this.sessionRepo = sessionRepo;
    }

    // ─────────────────────────── Core ───────────────────────────

    @Transactional
    public UserSession getOrCreate(long userId) {
        UserSessionEntity entity = sessionRepo.findById(userId).orElseGet(() -> {
            UserSessionEntity e = newEntity(userId);
            return sessionRepo.save(e);
        });
        return toDomain(entity);
    }

    @Transactional
    public void resetCheckout(long userId) {
        UserSessionEntity entity = sessionRepo.findById(userId).orElse(null);
        if (entity == null) return;
        entity.setState(UserSession.State.IDLE.name());
        entity.setCheckoutName(null);
        entity.setCheckoutPhone(null);
        entity.setCheckoutAddress(null);
        entity.setCheckoutCountry(null);
        entity.setCheckoutCity(null);
        entity.setAdminTempOrderId(null);
        entity.setUpdatedAt(OffsetDateTime.now());
        sessionRepo.save(entity);
    }

    @Transactional(readOnly = true)
    public UserSession.State getState(long userId) {
        return getOrCreate(userId).getState();
    }

    @Transactional
    public void setState(long userId, UserSession.State state) {
        UserSessionEntity entity = getOrCreateEntity(userId);
        entity.setState(state.name());
        entity.setUpdatedAt(OffsetDateTime.now());
        sessionRepo.save(entity);
    }

    // ─────────────────────────── Checkout fields ───────────────────────────

    @Transactional
    public void setCheckoutName(long userId, String name) {
        UserSessionEntity entity = getOrCreateEntity(userId);
        entity.setCheckoutName(name);
        entity.setUpdatedAt(OffsetDateTime.now());
        sessionRepo.save(entity);
    }

    @Transactional
    public void setCheckoutPhone(long userId, String phone) {
        UserSessionEntity entity = getOrCreateEntity(userId);
        entity.setCheckoutPhone(phone);
        entity.setUpdatedAt(OffsetDateTime.now());
        sessionRepo.save(entity);
    }

    @Transactional
    public void setCheckoutAddress(long userId, String address) {
        UserSessionEntity entity = getOrCreateEntity(userId);
        entity.setCheckoutAddress(address);
        entity.setUpdatedAt(OffsetDateTime.now());
        sessionRepo.save(entity);
    }

    @Transactional
    public void setCheckoutCountry(long userId, String country) {
        UserSessionEntity entity = getOrCreateEntity(userId);
        entity.setCheckoutCountry(country);
        entity.setUpdatedAt(OffsetDateTime.now());
        sessionRepo.save(entity);
    }

    @Transactional
    public void setCheckoutCity(long userId, String city) {
        UserSessionEntity entity = getOrCreateEntity(userId);
        entity.setCheckoutCity(city);
        entity.setUpdatedAt(OffsetDateTime.now());
        sessionRepo.save(entity);
    }

    // ─────────────────────────── Admin temp fields ───────────────────────────

    @Transactional
    public void setAdminTempOrderId(long userId, String orderId) {
        UserSessionEntity entity = getOrCreateEntity(userId);
        entity.setAdminTempOrderId(orderId);
        entity.setUpdatedAt(OffsetDateTime.now());
        sessionRepo.save(entity);
    }

    // ─────────────────────────── Cleanup ───────────────────────────

    @Scheduled(fixedDelay = 86_400_000L)
    @Transactional
    public void cleanupOldSessions() {
        OffsetDateTime cutoff = OffsetDateTime.now().minusDays(30);
        int deleted = sessionRepo.deleteByUpdatedAtBefore(cutoff);
        if (deleted > 0) {
            log.info("Cleaned up {} stale user sessions older than 30 days", deleted);
        }
    }

    // ─────────────────────────── Helpers ───────────────────────────

    private UserSessionEntity getOrCreateEntity(long userId) {
        return sessionRepo.findById(userId).orElseGet(() -> sessionRepo.save(newEntity(userId)));
    }

    private UserSessionEntity newEntity(long userId) {
        UserSessionEntity e = new UserSessionEntity();
        e.setTelegramUserId(userId);
        e.setState(UserSession.State.IDLE.name());
        e.setUpdatedAt(OffsetDateTime.now());
        return e;
    }

    private UserSession toDomain(UserSessionEntity entity) {
        UserSession session = new UserSession(entity.getTelegramUserId());
        try {
            session.setState(UserSession.State.valueOf(entity.getState()));
        } catch (Exception ignored) {
            session.setState(UserSession.State.IDLE);
        }
        session.setCheckoutName(entity.getCheckoutName());
        session.setCheckoutPhone(entity.getCheckoutPhone());
        session.setCheckoutAddress(entity.getCheckoutAddress());
        session.setCheckoutCountry(entity.getCheckoutCountry());
        session.setCheckoutCity(entity.getCheckoutCity());
        session.setAdminTempOrderId(entity.getAdminTempOrderId());
        return session;
    }
}
