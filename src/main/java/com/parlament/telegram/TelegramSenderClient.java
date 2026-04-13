package com.parlament.telegram;

import com.parlament.config.BotProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Primary;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * A lightweight sender that can execute Telegram API calls.
 *
 * Used for webhook mode, where updates come through HTTP controller rather than Telegram long polling.
 */
@Component
@Primary
public class TelegramSenderClient extends TelegramLongPollingBot implements TelegramBotSender {

    private static final Logger log = LoggerFactory.getLogger(TelegramSenderClient.class);

    private final BotProperties props;

    public TelegramSenderClient(BotProperties props) {
        super(props.getToken());
        this.props = props;
    }

    @Override
    public String getBotUsername() {
        return props.getUsername();
    }

    @Override
    public void onUpdateReceived(Update update) {
        // not used
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

