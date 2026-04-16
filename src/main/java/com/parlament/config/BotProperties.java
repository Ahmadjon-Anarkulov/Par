package com.parlament.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "bot")
@Validated
public class BotProperties {

    @NotBlank(message = "bot.token must be set")
    @Pattern(regexp = "\\d+:[A-Za-z0-9_-]{35}", message = "bot.token must be a valid Telegram bot token")
    private String token;

    @NotBlank(message = "bot.username must be set")
    private String username;

    /**
     * long_polling | webhook
     */
    private String mode = "long_polling";

    private Webhook webhook = new Webhook();

    public static class Webhook {
        private boolean enabled = false;

        /**
         * Your public base URL. Example: https://<service>.up.railway.app
         * Required only when webhook is enabled.
         */
        private String publicUrl;

        /**
         * Path part for webhook endpoint. Example: /telegram/webhook
         */
        private String path = "/telegram/webhook";

        /**
         * Optional secret token header validation (Telegram: X-Telegram-Bot-Api-Secret-Token).
         */
        @Pattern(regexp = "[A-Za-z0-9_-]*", message = "bot.webhook.secret-token must contain only alphanumeric characters, underscores, and hyphens")
        private String secretToken;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getPublicUrl() {
            return publicUrl;
        }

        public void setPublicUrl(String publicUrl) {
            this.publicUrl = publicUrl;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getSecretToken() {
            return secretToken;
        }

        public void setSecretToken(String secretToken) {
            this.secretToken = secretToken;
        }
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    /**
     * Non-null token for Telegram SDK constructors. Use {@link #isBotConfigured()} before real API calls.
     */
    public String getTokenOrEmpty() {
        return token == null ? "" : token;
    }

    public String getUsername() {
        return username;
    }

    /**
     * Non-null username for Telegram SDK. May be empty when not configured.
     */
    public String getUsernameOrEmpty() {
        return username == null ? "" : username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isBotConfigured() {
        return token != null && !token.isBlank() && username != null && !username.isBlank();
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public Webhook getWebhook() {
        return webhook;
    }

    public void setWebhook(Webhook webhook) {
        this.webhook = webhook;
    }
}
