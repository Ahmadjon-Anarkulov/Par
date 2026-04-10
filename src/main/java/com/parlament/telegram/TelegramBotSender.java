package com.parlament.telegram;

import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;

public interface TelegramBotSender {
    void sendText(SendMessage message);
    void sendPhoto(SendPhoto photo);
    void editMessage(EditMessageText editMessage);
    void answerCallback(AnswerCallbackQuery answerCallbackQuery);
}

