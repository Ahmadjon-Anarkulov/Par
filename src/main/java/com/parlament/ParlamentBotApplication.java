package com.parlament;

import com.parlament.config.TelegramConfig;
import com.parlament.telegram.LongPollingParlamentBot;
import com.parlament.telegram.TelegramSenderClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.telegram.telegrambots.meta.TelegramBotsApi;

@SpringBootApplication
public class ParlamentBotApplication {
    public static void main(String[] args) {
        SpringApplication.run(ParlamentBotApplication.class, args);
    }
    // Или лучше использовать CommandLineRunner
    @Bean
    public CommandLineRunner startBot(TelegramConfig telegramConfig,
                                      TelegramBotsApi telegramBotsApi,
                                      LongPollingParlamentBot longPollingBot,
                                      TelegramSenderClient senderClient) {
        return args -> {
            telegramConfig.startBots(telegramBotsApi, longPollingBot, senderClient);
        };
    }
}

