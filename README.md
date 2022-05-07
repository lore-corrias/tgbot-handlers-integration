# Update Handling

An integration to [pengrad's Telegram Bot API](https://github.com/pengrad/java-telegram-bot-api)

- Implementation of an [Update Handling](#update-handling) functionality.
- Addition of a management system for [Yaml config](#yaml-configs) files.
- New [helper class](actions-api) for API requests.

## Download

Integration is only available with maven, for now. Add this to your pom.xml:

```xml
<dependencies>
    <dependency>
        <groupId>io.github.justlel</groupId>
        <artifactId>tgbot-handlers-integration</artifactId>
        <version>1.1.1</version>
    </dependency>
</dependencies>
```

### Update Handling

The most important integration to the base framework is the addiction of the UpdatesDispatcher.\
To facilitate the management of different types of updates, the framework provides a way to register multiple
handlers, which are classes who are given the task of handling the updates. To register update handlers, you must
instantiate
an object of the handler class and register it with the UpdatesDispatcher.
This is an example of a generic update handler, which handles all the updates of a same type:

```java
import ActionsAPIHelper;
import GenericUpdateHandler;
import UpdatesDispatcher;
import com.pengrad.telegrambot.model.Update;

public class MyBot {
    public static void main(String[] args) {
        // Create a new instance of the bot
        TelegramBot bot = new TelegramBot("TOKEN");

        // Create an instance of the dispatcher with the bot
        UpdatesDispatcher dispatcher = new UpdatesDispatcher();

        // Register handlers
        dispatcher.registerUpdatesHandler(UpdatesDispatcher.GenericUpdateTypes.CHAT_MEMBER_UPDATED, new GenericUpdateHandler() {
            @Override
            public void handleUpdate(Update update) {
                ActionsAPIHelper.sendMessage("Welcome!", update.message().chat().id());
            }
        });

        // Run the bot
        dispatcher.runUpdateListener(bot);
    }
}
```

However, you can also register sub-handlers for updates. For example, you can register a handler to manage
all incoming bot commands: in order to do it, create a class that extends the SpecificUpdatesHandler

```java
import HandlerInterface;
import UpdatesDispatcher;

public class CommandsHandler extends SpecificUpdatesHandler<String> {

    public CommandsHandler() {
        super.registerSpecificHandler("/start", (HandlerInterface) update ->
                ActionsAPIHelper.sendMessage("You have started the bot!", update.message().chat().id())
        );
    }

    @Override
    public AbstractUpdateHandler dispatchUpdate(Update update) {
        if (!update.message().text().startsWith("/"))
            return;
        String command = update.message().text().substring(1);
        return super.getSpecificHandler(update.message().text()).handleUpdate(update);
    }
}

public class MyBot {
    public static void main(String[] args) {
        // Create a new instance of the bot
        TelegramBot bot = new TelegramBot("TOKEN");

        // Create an instance of the dispatcher with the bot
        UpdatesDispatcher dispatcher = new UpdatesDispatcher();

        // Instantiate the SpecificUpdatesHandler
        CommandsHandler commandsHandler = new CommandsHandler();
        // Add the commands handler
        dispatcher.registerUpdatesHandler(UpdatesDispatcher.MessageUpdateTypes.COMMAND, commandsHandler);

        // Run the bot
        dispatcher.runUpdateListener(bot);
    }
}
```

It is also possible to register a handler for all the update types which don't have a specific manager set, by using the
method
```registerDefaultUpdatesHandler```.

## Yaml configs

This library also provides a framework to manage configuration files more easily. If you want to use it, you just need
to
create a class that implements the ```YamlInterface```, and loading using the ```YamlManager```.
Here is an example for managing a Yaml file named ```config.yaml```:

```yaml
bot-token: "123456789abcdefghi"
bot-admins: [123456789, 123456789]
```

```java
import com.fasterxml.jackson.annotation.JsonProperty;
import YamlManager;

import java.util.HashMap;

public class MyConfig implements YamlInterface {
    private static String botToken;
    private static List<Long> botAdmins; // properties must be static

    @JsonProperty("bot-token")
    private void setBotToken(String botToken) {
        MyConfig.botToken = botToken;
    }

    @JsonProperty("bot-admins") // specify the property using Jackson
    private void setBotAdmins(List<Long> botAdmins) {
        MyConfig.botAdmins = botAdmins;
    }

    // Empty constructor is required for Jackson to work!
    public MyConfig() {
    }

    public static List<Long> getBotAdmins() {
        return botAdmins;
    }

    public static String getBotToken() {
        return botToken;
    }

    // Return the name of the config file
    @Override
    public String getFileName() {
        return "config.yaml";
    }

    // Define method to validate the config.
    @Override
    public void checkConfigValidity() throws IllegalArgumentException, IOException {
        if (botToken == null || botAdmins == null)
            throw new IllegalArgumentException("Config is not valid");
    }

    // Define the data to be dumped in case of re-writing of the file.
    @Override
    public Object getDumpableData() {
        return new HashMap<>() {{
            put("bot-token", botToken);
            put("bot-admins", botAdmins);
        }};
    }
}

public class MyBot {
    public static void main(String[] args) {
        // Set config directory
        YamlManager.setConfigDirectory("configs");
        // Load the configs
        YamlManager.getInstance().loadYaml(MyConfig.class);
        // Get the bot token
        String botToken = MyConfig.getBotToken();
        // Create a new instance of the bot
        TelegramBot bot = new TelegramBot(botToken);
        // do stuff
    }
}
```

## ActionsAPIHelper

ActionsAPIHelper is a helper class which can help you to fabricate requests to Telegram's APIs.
Its methods are static, and implement some functionalities that are useful and oftentimes redundant (such as
the automatic disabilitation of Web Pages Previews). Examples are shown below:

```java
import ActionsAPIHelper;
import GenericUpdateHandler;
import com.pengrad.telegrambot.response.BaseResponse;

public class MyBot {
    public static void main(String[] args) {
        // Create a new instance of the bot
        TelegramBot bot = new TelegramBot("TOKEN");

        // Create an instance of the dispatcher with the bot
        UpdatesDispatcher dispatcher = new UpdatesDispatcher();

        // Register an handler
        dispatcher.registerDefaultUpdatesHandler(new GenericUpdateHandler() {
            @Override
            public void handleUpdates(Update update) {
                Long messageId = update.message().messageId();
                BaseResponse response = ActionsAPIHelper.sendMessage("Hello world!", messageId);
                if (!response.isOk())
                    System.out.println("Error sending message: " + response.description());
            }
        });

        // Run the bot
        dispatcher.runUpdateListener(bot);
    }
}
```
