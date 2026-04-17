package com.parlament.telegram;

import com.parlament.config.BotProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * Primary TelegramBotSender implementation.
 * Works for both long-polling and webhook modes (just executes API calls).
 */
@Component
@Primary
public class TelegramSenderClient extends TelegramLongPollingBot implements TelegramBotSender {

    private static final Logger log = LoggerFactory.getLogger(TelegramSenderClient.class);

    private final BotProperties props;

    public TelegramSenderClient(BotProperties props) {
        super(props.getTokenOrEmpty());
        this.props = props;
    }

    @Override
    public String getBotUsername() {
        return props.getUsernameOrEmpty();
    }

    /** Not used — updates arrive via UpdateProcessor */
    @Override
    public void onUpdateReceived(Update update) {}

    @Override
    public void sendText(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.warn("sendText failed: {}", e.getMessage());
        }
    }

    @Override
    public void sendPhoto(SendPhoto photo) {
        try {
            execute(photo);
        } catch (TelegramApiException e) {
            log.warn("sendPhoto failed: {}", e.getMessage());
        }
    }

    @Override
    public void sendDocument(SendDocument document) {
        try {
            execute(document);
        } catch (TelegramApiException e) {
            log.warn("sendDocument failed: {}", e.getMessage());
        }
    }

    @Override
    public void editMessage(EditMessageText editMessage) {
        try {
            execute(editMessage);
        } catch (TelegramApiException e) {
            log.warn("editMessage failed: {}", e.getMessage());
        }
    }

    @Override
    public void answerCallback(AnswerCallbackQuery answerCallbackQuery) {
        try {
            execute(answerCallbackQuery);
        } catch (TelegramApiException e) {
            log.warn("answerCallback failed: {}", e.getMessage());
        }
    }
}
