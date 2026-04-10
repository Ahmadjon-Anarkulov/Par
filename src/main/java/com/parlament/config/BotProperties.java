package com.parlament.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "bot")
public class BotProperties {

    @NotBlank
    private String token;

    @NotBlank
    private String username;

    /**
     * long_polling | webhook
     */
    @NotBlank
    private String mode = "long_polling";

    @Valid
    private Webhook webhook = new Webhook();

    public static class Webhook {
        private boolean enabled = false;

        /**
         * Your public base URL. Example: https://<service>.up.railway.app
         */
        private String publicUrl;

        /**
         * Path part for webhook endpoint. Example: /telegram/webhook
         */
        private String path = "/telegram/webhook";

        /**
         * Optional secret token header validation (Telegram: X-Telegram-Bot-Api-Secret-Token).
         */
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

