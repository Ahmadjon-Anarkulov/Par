package com.parlament.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "admin")
public class AdminProperties {
    /**
     * Fallback list of Telegram user IDs that should be promoted to ADMIN on startup
     * if the system currently has no admins.
     */
    private List<Long> initialIds = new ArrayList<>();

    public List<Long> getInitialIds() {
        return initialIds;
    }

    public void setInitialIds(List<Long> initialIds) {
        this.initialIds = initialIds;
    }
}

