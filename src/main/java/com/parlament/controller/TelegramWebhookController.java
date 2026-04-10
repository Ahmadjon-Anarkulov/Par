package com.parlament.controller;

import com.parlament.config.BotProperties;
import com.parlament.telegram.TelegramSenderClient;
import com.parlament.telegram.UpdateProcessor;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.objects.Update;

@RestController
public class TelegramWebhookController {

    private static final Logger log = LoggerFactory.getLogger(TelegramWebhookController.class);
    private static final String TELEGRAM_SECRET_HEADER = "X-Telegram-Bot-Api-Secret-Token";

    private final BotProperties props;
    private final UpdateProcessor processor;
    private final TelegramSenderClient senderClient;

    public TelegramWebhookController(BotProperties props, UpdateProcessor processor, TelegramSenderClient senderClient) {
        this.props = props;
        this.processor = processor;
        this.senderClient = senderClient;
    }

    @PostMapping("${bot.webhook.path:/telegram/webhook}")
    public ResponseEntity<Void> onUpdate(@RequestBody Update update, HttpServletRequest request) {
        if (!props.getWebhook().isEnabled() && !"webhook".equalsIgnoreCase(props.getMode())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        String expectedSecret = props.getWebhook().getSecretToken();
        if (expectedSecret != null && !expectedSecret.isBlank()) {
            String provided = request.getHeader(TELEGRAM_SECRET_HEADER);
            if (provided == null || !expectedSecret.equals(provided)) {
                log.warn("Rejected webhook request: invalid secret token header");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        }

        processor.handle(update, senderClient);
        return ResponseEntity.ok().build();
    }
}

