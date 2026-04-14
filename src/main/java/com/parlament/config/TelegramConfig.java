package com.parlament.config;

import com.parlament.telegram.LongPollingParlamentBot;
import com.parlament.telegram.TelegramSenderClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.regex.Pattern;

@Configuration
@EnableConfigurationProperties({BotProperties.class, AdminProperties.class})
public class TelegramConfig {

    private static final Logger log = LoggerFactory.getLogger(TelegramConfig.class);

    private final BotProperties botProperties;

    public TelegramConfig(BotProperties botProperties) {
        this.botProperties = botProperties;
    }

    @Bean
    public TelegramBotsApi telegramBotsApi() throws TelegramApiException {
        return new TelegramBotsApi(DefaultBotSession.class);
    }

    public void startBots(TelegramBotsApi api,
                          LongPollingParlamentBot longPollingBot,
                          TelegramSenderClient senderClient) {

        if (!botProperties.isBotConfigured()) {
            log.warn("Telegram bot is not configured (missing bot.token/bot.username). Skipping Telegram startup.");
            return;
        }

        boolean webhookEnabled = botProperties.getWebhook().isEnabled()
                || "webhook".equalsIgnoreCase(botProperties.getMode());

        if (webhookEnabled) {
            var webhook = botProperties.getWebhook();
            if (webhook.getPublicUrl() == null || webhook.getPublicUrl().isBlank()) {
                log.warn("Webhook mode is enabled but bot.webhook.public-url is empty. Skipping SetWebhook.");
                return;
            }

            String fullUrl = normalizeBaseUrl(webhook.getPublicUrl()) + normalizePath(webhook.getPath());

            try {
                SetWebhook.SetWebhookBuilder b = SetWebhook.builder().url(fullUrl);
                String secret = webhook.getSecretToken();
                if (secret != null && !secret.isBlank()) {
                    if (TELEGRAM_SECRET_TOKEN_PATTERN.matcher(secret).matches()) {
                        b.secretToken(secret);
                    } else {
                        log.warn("bot.webhook.secret-token has invalid characters for Telegram API; omitting secretToken on SetWebhook");
                    }
                }
                senderClient.execute(b.build());
                log.info("Telegram webhook registered at {}", fullUrl);
            } catch (TelegramApiException e) {
                log.warn("Failed to register Telegram webhook at {}: {}", fullUrl, e.getMessage(), e);
            }
            return;
        }

        try {
            api.registerBot(longPollingBot);
            log.info("Telegram long polling enabled for @{}", botProperties.getUsername());
        } catch (TelegramApiException e) {
            log.warn("Failed to register long polling bot: {}", e.getMessage(), e);
        }
    }

    private static String normalizeBaseUrl(String baseUrl) {
        if (baseUrl.endsWith("/")) return baseUrl.substring(0, baseUrl.length() - 1);
        return baseUrl;
    }

    private static String normalizePath(String path) {
        if (path == null || path.isBlank()) return "/telegram/webhook";
        if (path.startsWith("/")) return path;
        return "/" + path;
    }

    public BotProperties getBotProperties() {
        return botProperties;
    }

    private static final Pattern TELEGRAM_SECRET_TOKEN_PATTERN = Pattern.compile("[A-Za-z0-9_-]+");
}

