package io.github.justlel.samplebot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import io.github.justlel.api.ActionsAPIHelper;
import io.github.justlel.models.GenericUpdateHandler;
import io.github.justlel.models.HandlerInterface;
import io.github.justlel.models.SpecificUpdateHandler;
import io.github.justlel.models.UpdatesDispatcher;


public class SampleBot {

    // TODO: Add an example on how to use a configuration file

    public static void main(String[] args) throws IllegalAccessException {
        // Obtaining the telegram bot token
        String telegramToken;
        if (args.length == 0 && System.getenv("TELEGRAM_BOT_TOKEN") == null)
            throw new IllegalAccessException("You must provide a bot token!");
        else if (args.length > 0)
            telegramToken = args[0];
        else
            telegramToken = System.getenv("TELEGRAM_BOT_TOKEN");

        // Set up the bot and the updates dispatcher
        TelegramBot bot = new TelegramBot(telegramToken);
        UpdatesDispatcher dispatcher = new UpdatesDispatcher();

        // Creating a handler for all the different commands of the bot
        SpecificUpdateHandler<String> commandsHandler = new SpecificUpdateHandler<>() {
            @Override
            public HandlerInterface returnUpdateHandler(Update update) {
                return super.getSpecificHandler(update.message().text().substring(1)); // the update will be dispatched basing on the command sent
            }
        };
        // Register a specific handler for the command /start
        commandsHandler.registerSpecificHandler("start", new HandlerInterface() {
            @Override
            public void handleUpdate(Update update) {
                // Build an inline keyboard
                InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
                keyboard.addRow(new InlineKeyboardButton("Click this button!").callbackData("click-me")); // setting the callback data for later use

                ActionsAPIHelper.sendMessage("Hi, welcome to the bot! Click the button below me for a surprise!", update.message().chat().id(), update.message().messageId(), keyboard);
            }
        });
        // Add the command handler to the updates dispatcher
        dispatcher.registerUpdatesHandler(UpdatesDispatcher.MessageUpdateTypes.COMMAND, commandsHandler);

        // Register an handler for all other kinds of media messages.
        dispatcher.registerUpdatesHandler(UpdatesDispatcher.MessageUpdateTypes.getMediaUpdates(), new GenericUpdateHandler() {
            @Override
            public void handleUpdate(Update update) {
                ActionsAPIHelper.sendMessage("The message you just sent:", update.message().chat().id());
                ActionsAPIHelper.forwardMessage(update.message().chat().id(), update.message().chat().id(), update.message().messageId()); // forward the message back to the user
            }
        });

        // Register a handler for all the possible callback queries.
        SpecificUpdateHandler<String> callbackQueriesHandler = new SpecificUpdateHandler<>() {
            @Override
            public HandlerInterface returnUpdateHandler(Update update) {
                return super.getSpecificHandler(update.callbackQuery().data()); // the update will be dispatched basing on the callback query data
            }
        };
        // Register a specific handler for the callback query "click-me"
        callbackQueriesHandler.registerSpecificHandler("click-me", new HandlerInterface() {
            @Override
            public void handleUpdate(Update update) {
                ActionsAPIHelper.answerCallbackQuery(update.callbackQuery().id(), "No surprised, you've been fooled!"); // answer the callback query of the start keyboard
            }
        });
        // Add the callback handler to the updates dispatcher
        dispatcher.registerUpdatesHandler(UpdatesDispatcher.GenericUpdateTypes.CALLBACK_QUERY, callbackQueriesHandler);

        // Start the bot!
        dispatcher.runUpdateListener(bot);
    }
}
