package com.parlament.telegram;

import com.parlament.config.BotProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * Long-polling bot implementation.
 *
 * If you expect high load, consider:
 * - per-user rate limiting (bucket4j) on update processing
 * - async execution for outbound calls (queue/executor) to avoid blocking update threads
 */
@Component
public class LongPollingParlamentBot extends TelegramLongPollingBot implements TelegramBotSender {

    private static final Logger log = LoggerFactory.getLogger(LongPollingParlamentBot.class);

    private final BotProperties props;
    private final UpdateProcessor processor;

    public LongPollingParlamentBot(BotProperties props, UpdateProcessor processor) {
        super(props.getTokenOrEmpty());
        this.props = props;
        this.processor = processor;
    }

    @Override
    public String getBotUsername() {
        return props.getUsernameOrEmpty();
    }

    @Override
    public void onUpdateReceived(Update update) {
        processor.handle(update, this);
    }

    @Override
    public void sendText(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.warn("Failed to send message: {}", e.getMessage(), e);
        }
    }

    @Override
    public void sendPhoto(SendPhoto photo) {
        try {
            execute(photo);
        } catch (TelegramApiException e) {
            log.warn("Failed to send photo: {}", e.getMessage(), e);
        }
    }

    @Override
    public void editMessage(EditMessageText editMessage) {
        try {
            execute(editMessage);
        } catch (TelegramApiException e) {
            log.warn("Failed to edit message: {}", e.getMessage(), e);
        }
    }

    @Override
    public void answerCallback(AnswerCallbackQuery answerCallbackQuery) {
        try {
            execute(answerCallbackQuery);
        } catch (TelegramApiException e) {
            log.warn("Failed to answer callback: {}", e.getMessage(), e);
        }
    }
}

