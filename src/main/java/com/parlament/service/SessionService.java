package com.parlament.service;

import com.parlament.model.UserSession;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Manages user conversation sessions and checkout state.
 */
@Service
public class SessionService {

    private final Map<Long, UserSession> sessions = new ConcurrentHashMap<>();

    /**
     * Returns the session for a user, creating one if it doesn't exist.
     */
    public UserSession getOrCreate(long userId) {
        return sessions.computeIfAbsent(userId, UserSession::new);
    }

    /**
     * Resets the checkout state for a user session.
     */
    public void resetCheckout(long userId) {
        UserSession session = sessions.get(userId);
        if (session != null) session.resetCheckout();
    }

    /**
     * Returns the current state of a user's session.
     */
    public UserSession.State getState(long userId) {
        return getOrCreate(userId).getState();
    }

    /**
     * Sets the conversation state for a user.
     */
    public void setState(long userId, UserSession.State state) {
        getOrCreate(userId).setState(state);
    }
}
