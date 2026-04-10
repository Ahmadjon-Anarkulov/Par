    package com.parlament.config;

    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    import org.springframework.boot.context.event.ApplicationReadyEvent;
    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Configuration;
    import org.springframework.context.event.EventListener;
    import org.springframework.boot.context.properties.EnableConfigurationProperties;
    import org.telegram.telegrambots.meta.TelegramBotsApi;
    import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
    import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
    import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
    import com.parlament.telegram.LongPollingParlamentBot;
    import com.parlament.telegram.TelegramSenderClient;

    @Configuration
    @EnableConfigurationProperties(BotProperties.class)
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
                              TelegramSenderClient senderClient) throws TelegramApiException {

            boolean webhookEnabled = botProperties.getWebhook().isEnabled()
                    || "webhook".equalsIgnoreCase(botProperties.getMode());

            if (webhookEnabled) {
                var webhook = botProperties.getWebhook();
                if (webhook.getPublicUrl() == null || webhook.getPublicUrl().isBlank()) {
                    throw new IllegalStateException("BOT_WEBHOOK_PUBLIC_URL must be set when webhook mode is enabled.");
                }

                String fullUrl = normalizeBaseUrl(webhook.getPublicUrl()) + normalizePath(webhook.getPath());

                // Telegram requires you to set the webhook at runtime.
                senderClient.execute(SetWebhook.builder().url(fullUrl).build());

                log.info("Telegram webhook enabled at {}", fullUrl);
            } else {
                api.registerBot(longPollingBot);
                log.info("Telegram long polling enabled for @{}", botProperties.getUsername());
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
    }

