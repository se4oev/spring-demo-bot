package ru.se4oev.springdemobot.service;

import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.se4oev.springdemobot.config.BotConfig;
import ru.se4oev.springdemobot.model.UserService;

import java.util.ArrayList;
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
        commands.add(new BotCommand("/mydata", "get user data"));
        commands.add(new BotCommand("/deletedata", "delete my data"));
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
                case "/start":
                    registerUser(update.getMessage());
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                    break;
                case "/help":
                    sendMessage(chatId, HELP_TEXT);
                    break;
                default:
                    sendMessage(chatId, "Sorry, I don't understand command :( ");
            }
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
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error occurred: {}", e.getMessage());
        }
    }

}
