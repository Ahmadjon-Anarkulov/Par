package com.parlament;

import com.parlament.config.TelegramConfig;
import com.parlament.telegram.LongPollingParlamentBot;
import com.parlament.telegram.TelegramSenderClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.telegram.telegrambots.meta.TelegramBotsApi;

@SpringBootApplication
public class ParlamentBotApplication {

    private static final Logger log = LoggerFactory.getLogger(ParlamentBotApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ParlamentBotApplication.class, args);
    }

    /**
     * Запуск Telegram бота после инициализации Spring
     */
    @Bean
    public CommandLineRunner startBot(
            TelegramConfig telegramConfig,
            TelegramBotsApi telegramBotsApi,      // берем из TelegramConfig
            LongPollingParlamentBot longPollingBot,
            TelegramSenderClient senderClient) {

        return args -> {
            try {
                log.info("Starting Telegram bot integration...");
                telegramConfig.startBots(telegramBotsApi, longPollingBot, senderClient);
                log.info("Telegram startup routine finished.");
            } catch (Exception e) {
                log.error("Unexpected error during Telegram startup routine", e);
            }
        };
    }
}