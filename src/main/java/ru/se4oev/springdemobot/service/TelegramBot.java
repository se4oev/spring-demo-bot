package ru.se4oev.springdemobot.service;

import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.se4oev.springdemobot.config.BotConfig;
import ru.se4oev.springdemobot.model.UserService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by karpenko on 25.08.2022.
 * Description:
 */
@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    private final UserService userService;
    private final BotConfig config;
    private final static String HELP_TEXT = """
            This bot is created to demonstrate.\s
            You can execute commands from main menu.\s
            Type /start to start use the bot.\s""";

    public TelegramBot(BotConfig config, UserService userService) {
        this.config = config;
        this.userService = userService;
        List<BotCommand> commands = new ArrayList<>();
        commands.add(new BotCommand("/start", "get a welcome message"));
        commands.add(new BotCommand("/myData", "get user data"));
        commands.add(new BotCommand("/deleteData", "delete my data"));
        commands.add(new BotCommand("/help", "list of commands"));
        commands.add(new BotCommand("/settings", "set your preferences"));
        try {
            this.execute(new SetMyCommands(commands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e ) {
            log.error(e.getMessage());
        }
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public String getBotUsername() {
        return config.getName();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String message = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            log.info("Receive message {} \n from: {}", update.getMessage(), update.getMessage().getChat());
            switch (message) {
                case "/start" -> {
                    registerUser(update.getMessage());
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                }
                case "/help" -> sendMessage(chatId, HELP_TEXT);
                case "/register" -> {
                    register(chatId);
                }
                default -> sendMessage(chatId, "Sorry, I don't understand command :( ");
            }
        } else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            int messageId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            if (callbackData.equals("YES_BUTTON")) {
                String text = "You pressed yes button!";
                EditMessageText editMessageText = new EditMessageText();
                editMessageText.setChatId(String.valueOf(chatId));
                editMessageText.setText(text);
                editMessageText.setMessageId(messageId);
                try {
                    execute(editMessageText);
                } catch (TelegramApiException e) {
                    log.error("Error occurred: {}", e.getMessage());
                }
            } else if (callbackData.equals("NO_BUTTON")) {
                String text = "You pressed no button";
                EditMessageText editMessageText = new EditMessageText();
                editMessageText.setChatId(String.valueOf(chatId));
                editMessageText.setText(text);
                editMessageText.setMessageId(messageId);
                try {
                    execute(editMessageText);
                } catch (TelegramApiException e) {
                    log.error("Error occurred: {}", e.getMessage());
                }
            }
        }
    }

    private void register(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Do you really want to register?");
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        InlineKeyboardButton btnYes = new InlineKeyboardButton();
        btnYes.setText("Yes");
        btnYes.setCallbackData("YES_BUTTON");
        InlineKeyboardButton btnNo = new InlineKeyboardButton();
        btnNo.setText("No");
        btnNo.setCallbackData("NO_BUTTON");
        List<InlineKeyboardButton> buttons = new ArrayList<>(Arrays.asList(btnYes, btnNo));
        rows.add(buttons);
        markup.setKeyboard(rows);
        message.setReplyMarkup(markup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error occurred: {}", e.getMessage());
        }
    }

    private void startCommandReceived(long chatId, String name) {
        String answer = EmojiParser.parseToUnicode("Hi, " + name + ", nice to meet you!" + " :blush:");
        sendMessage(chatId, answer);
    }

    private void registerUser(Message message) {
        userService.registerUser(message);
    }

    private void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage(String.valueOf(chatId), text);

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> rows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("weather");
        row.add("get random joke");
        rows.add(row);

        row = new KeyboardRow();
        row.add("register");
        row.add("check my data");
        row.add("delete my data");

        rows.add(row);

        keyboardMarkup.setKeyboard(rows);

        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error occurred: {}", e.getMessage());
        }
    }

}
