package io.github.justlel.models;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Update;
import io.github.justlel.api.ActionsAPIHelper;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * The default UpdatesDispatcher.
 * Each bot created can only be run by calling its {@link #runUpdateListener} method.
 * The job of this class is to make it easier for the end user to manage incoming updates
 * from the bot. The way this is done is by using the method {@link #registerUpdatesHandler},
 * which stores an instance either of a {@link GenericUpdateHandler} or {@link SpecificUpdateHandler}
 * responsible for the management of a specific type of update. The update types can be seen by accessing the {@link UpdatesDispatcher.MessageUpdateTypes}
 * and {@link UpdatesDispatcher.GenericUpdateTypes}. It is also possible to register a handler for all the updates
 * which don't have a specific one set, by using the {@link #registerDefaultUpdatesHandler} method.
 * The updates are mapped to their enum respective by using the {@link #getUpdateType} method.
 *
 * @author justlel
 * @version 1.0
 * @see AbstractUpdateDispatcher
 * @see #getUpdateType
 */
public class UpdatesDispatcher extends AbstractUpdateDispatcher {

    /**
     * HashMap for the handlers of the {@link UpdatesDispatcher.MessageUpdateTypes}.
     * The handlers are stored in two different HashMaps to optimize the mapping to the enum.
     */
    private final HashMap<MessageUpdateTypes, HandlerInterface> messageTypeHandlers = new HashMap<>();
    /**
     * HashMap for the handlers of the {@link UpdatesDispatcher.GenericUpdateTypes}.
     * The handlers are stored in two different HashMaps to optimize the mapping to the enum.
     */
    private final HashMap<GenericUpdateTypes, HandlerInterface> genericTypeHandlers = new HashMap<>();
    /**
     * The default handler for the updates which don't have a specific one set.
     */
    private HandlerInterface defaultUpdatesHandler;

    /**
     * Default constructor.
     */
    public UpdatesDispatcher() {
    }

    /**
     * By using the {@link #getUpdateType} method, the class can manage to
     * find the correct handler for the update type. Once found, the update is
     * handled via the {@link HandlerInterface#handleUpdate} method.
     *
     * @param update The update to be dispatched.
     */
    @Override
    protected void dispatchUpdate(Update update) {
        try {
            UpdateTypes updateType = this.getUpdateType(update);
            if (hasUpdateHandler(updateType)) {
                getUpdateHandler(updateType).handleUpdate(update);
            }
        } catch (Exception ignored) {
        } // TODO: for now, since an exception like this assumes that there's some errors in the code
    }

    /**
     * Maps a generic update to the corresponding enum value.
     * This process is necessary in order to facilitate the handling of different
     * update types for the user, because an {@link Update} object does not
     * give any indication about the nature of the update received,
     * since it just represents a JSON-object as received from the Telegram API.
     * In order to make the mapping more efficient, the updates are divided into two
     * categories: {@link UpdatesDispatcher.MessageUpdateTypes} and {@link UpdatesDispatcher.GenericUpdateTypes}.
     * The first ones are defined by having a non-null {@link Update#message()} field (these usually being
     * message texts, stickers, gifs, etc.), whilst the latter ones have a null {@link Update#message()} field,
     * and are structured differently (these usually being callback queries, inline queries, group updates, etc.).
     * The update types decided are totally arbitrary, but their choice is based on Telegram API's.
     * The mapping looks at the types for which a handler has already been set, and iterates through
     * them by calling the {@link #updateMatchesType} method for each one.
     *
     * @param update The update to be mapped.
     * @return The mapped update type.
     * @throws InvocationTargetException If the {@link #updateMatchesType} method throws an exception.
     * @throws NoSuchMethodException     If the {@link #updateMatchesType} method throws an exception.
     * @throws IllegalAccessException    If the {@link #updateMatchesType} method throws an exception.
     */
    @Nullable
    private UpdateTypes getUpdateType(Update update) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        if (update.message() == null && !this.genericTypeHandlers.isEmpty()) {
            for (GenericUpdateTypes genericUpdateType : this.genericTypeHandlers.keySet()) {
                if (updateMatchesType(update, genericUpdateType))
                    return genericUpdateType;
            }
        } else if (update.message() != null && !this.messageTypeHandlers.isEmpty()) {
            List<MessageUpdateTypes> ambiguousUpdateTypes = List.of(MessageUpdateTypes.PRIVATE_MESSAGE, MessageUpdateTypes.GROUP_MESSAGE, MessageUpdateTypes.SUPERGROUP_MESSAGE, MessageUpdateTypes.COMMAND);
            List<UpdateTypes> unambiguousUpdateTypes = new ArrayList<>();
            for (MessageUpdateTypes messageUpdateType : this.messageTypeHandlers.keySet()) {
                if (!ambiguousUpdateTypes.contains(messageUpdateType)) {
                    unambiguousUpdateTypes.add(messageUpdateType);
                }
            }
            for (UpdateTypes unambiguousUpdateType : unambiguousUpdateTypes) {
                if (this.updateMatchesType(update, unambiguousUpdateType)) {
                    return unambiguousUpdateType;
                }
            }

            for (MessageUpdateTypes ambiguousUpdateType : ambiguousUpdateTypes) {
                boolean secondCheck = false;
                if (update.message().text().startsWith("/"))
                    return MessageUpdateTypes.COMMAND;
                switch (ambiguousUpdateType) {
                    case PRIVATE_MESSAGE -> secondCheck = update.message().chat().type() == Chat.Type.Private;
                    case GROUP_MESSAGE -> secondCheck = update.message().chat().type() == Chat.Type.group;
                    case SUPERGROUP_MESSAGE -> secondCheck = update.message().chat().type() == Chat.Type.supergroup;
                }
                if (secondCheck) {
                    return ambiguousUpdateType;
                }
            }
        }
        return null;
    }

    /**
     * Makes sure that a generic update is of a specific type.
     * The way this works is by obtaining a method name from the updateType parameter,
     * by using the {@link UpdateTypes#getMethodName()} method.
     * If the update contains a message field, then the method previously determined
     * is called from the {@link com.pengrad.telegrambot.model.Message} field, and if
     * it does not return null, then the update type is considered to match.
     * If the update does not contain a message field, the same process is applied,
     * but starting from the {@link com.pengrad.telegrambot.model.Update} object.
     *
     * @param update     The update to be checked.
     * @param updateType The update type to be checked.
     * @return True if the update type matches, false otherwise.
     * @throws NoSuchMethodException     If the method is not found. Typically, it means that the API has been changed.
     * @throws InvocationTargetException If the method to be called throws an exception. Typically, it means that the API has been changed.
     * @throws IllegalAccessException    If the method is not accessible. Typically, it means that the API has been changed, but it should never be thrown.
     */
    private boolean updateMatchesType(Update update, UpdateTypes updateType) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String propertyName = updateType.getMethodName();
        if (updateType instanceof MessageUpdateTypes) {
            return update.message().getClass().getMethod(propertyName).invoke(update.message()) != null;
        } else {
            return update.getClass().getMethod(propertyName).invoke(update) != null;
        }
    }

    /**
     * Register a handler for a specific update type.
     * The handlers are stored in two different maps, one for generic updates, and one for message updates,
     * in order to improve the performance of the updateType mapping.
     * If an handler is already registered for the same update type, it won't be replaced.
     *
     * @param updateType    The update type associated to the handler.
     * @param updateHandler The handler to be registered.
     */
    public void registerUpdatesHandler(UpdateTypes updateType, AbstractUpdateHandler updateHandler) {
        if (updateType instanceof MessageUpdateTypes) {
            this.messageTypeHandlers.putIfAbsent((MessageUpdateTypes) updateType, updateHandler);
        } else if (updateType instanceof GenericUpdateTypes) {
            this.genericTypeHandlers.putIfAbsent((GenericUpdateTypes) updateType, updateHandler);
        }
    }

    /**
     * Register a handler for multiple update types.
     * The handlers are stored in two different maps, one for generic updates, and one for message updates,
     * in order to improve the performance of the updateType mapping.
     *
     * @param updateTypes   The update types associated to the handler.
     * @param updateHandler The handler to be registered.
     */
    public void registerUpdatesHandler(List<UpdateTypes> updateTypes, AbstractUpdateHandler updateHandler) {
        updateTypes.forEach(updateType -> registerUpdatesHandler(updateType, updateHandler));
    }

    /**
     * Return the update handler associated with the given updateType.
     * If no handler is found, the default handler is returned.
     * If the default handler is not defined, null is returned.
     *
     * @param updateType The updateType of which to get the handler.
     * @return The update handler associated with the given updateType, null if not found.
     */
    public @Nullable HandlerInterface getUpdateHandler(UpdateTypes updateType) {
        if (!hasUpdateHandler(updateType) && !hasDefaultHandler()) {
            return null;
        }
        if (hasUpdateHandler(updateType)) {
            if (updateType instanceof MessageUpdateTypes) {
                return this.messageTypeHandlers.get(updateType);
            } else if (updateType instanceof GenericUpdateTypes) {
                return this.genericTypeHandlers.get(updateType);
            }
        }
        return this.defaultUpdatesHandler;
    }

    /**
     * Register a handler for the updateTypes which don't have
     * a specific handler registered.
     *
     * @param updateHandler The default handler to be registered.
     */
    public void registerDefaultUpdatesHandler(AbstractUpdateHandler updateHandler) {
        if (defaultUpdatesHandler == null)
            defaultUpdatesHandler = updateHandler;
    }

    /**
     * Verifies that a specific updateType has a handler registered.
     *
     * @param updateType The updateType of which to check the existence of a handler.
     * @return True if a handler is registered for the given updateType, false otherwise.
     */
    private boolean hasUpdateHandler(UpdateTypes updateType) {
        return messageTypeHandlers.containsKey(updateType) || this.genericTypeHandlers.containsKey(updateType);
    }

    /**
     * Verifies that a default handler is registered.
     *
     * @return True if a default handler is registered, false otherwise.
     */
    private boolean hasDefaultHandler() {
        return this.defaultUpdatesHandler != null;
    }

    /**
     * Register this instance as the updates listener for pengrad's {@link TelegramBot}.
     * Once this method is called, the bot will start listening for updates, which will then
     * be dispatched accordingly by this class.
     * The method also set the TelegramBot instance for the class {@link ActionsAPIHelper}, used
     * to facilitate the calling of API methods.
     *
     * @param telegramBot The TelegramBot instance to use for listening for updates.
     * @throws IllegalAccessException If the TelegramBot instance for the class ActionsAPIHelper is already set. Should never happen.
     */
    public void runUpdateListener(TelegramBot telegramBot) throws IllegalAccessException {
        ActionsAPIHelper.setTelegramBotInstance(telegramBot);
        telegramBot.setUpdatesListener(this);
    }

    /**
     * Enum for the different types of updates.
     * The types listed here are those determined by having a null {@link Update#message()} field.
     */
    public enum GenericUpdateTypes implements UpdateTypes {
        EDITED_MESSAGE("editedMessage"),
        CALLBACK_QUERY("callbackQuery"),
        CHANNEL_POST("channelPost"),
        CHAT_JOIN_REQUEST("chatJoinRequest"),
        CHAT_MEMBER_UPDATED("chatMember"),
        CHOSEN_INLINE_RESULT("chosenInlineResult"),
        EDITED_CHANNEL_POST("editedChannelPost"),
        INLINE_QUERY("inlineQuery"),
        POLL_ANSWER("pollAnswer"),
        PRE_SHIPPING_QUERY("preShippingQuery"),
        CHECKOUT_QUERY("checkoutQuery");

        /**
         * The method name to be called from the {@link Update} object.
         * Differently from the {@link MessageUpdateTypes} enum, many method names
         * are different from the enum value, and thus they cannot be easily obtained
         * from there.
         */
        private final String methodName;


        /**
         * Constructor for the enum.
         *
         * @param methodName The method name to be called from the {@link Update} object.
         */
        GenericUpdateTypes(String methodName) {
            this.methodName = methodName;
        }

        /**
         * @return The method name to be called from the {@link Update} object.
         */
        public String getMethodName() {
            return methodName;
        }
    }

    /**
     * Enum for the different types of updates.
     * The types listed here are those determined by having a non-null {@link Update#message()} field.
     */
    public enum MessageUpdateTypes implements UpdateTypes {
        POLL,
        PHOTO,
        VIDEO,
        ANIMATION,
        AUDIO,
        DICE,
        INVOICE,
        STICKER,
        CHANNEL_CHAT_CREATED,
        CONTACT,
        DELETE_CHAT_PHOTO,
        FORWARD_FROM,
        GAME,
        GROUP_CHAT_CREATED,
        LEFT_CHAT_MEMBER,
        MESSAGE_AUTO_DELETE_TIMER_CHANGED,
        MIGRATE_FROM_CHAT_ID,
        NEW_CHAT_PHOTO,
        NEW_CHAT_TITLE,
        PASSPORT_DATA,
        PINNED_MESSAGE,
        PROXIMITY_ALERT_TRIGGERED,
        SUCCESSFUL_PAYMENT,
        SUPERGROUP_CHAT_CREATED,
        VENUE,
        VIDEO_NOTE,
        WEB_APP_DATA,
        COMMAND,
        PRIVATE_MESSAGE,
        GROUP_MESSAGE,
        SUPERGROUP_MESSAGE;

        /**
         * Getter for methods who can easily be handled by the same update handler.
         * In any case, these updates all contain either a media of some kind, or a text message.
         *
         * @return The update types having either a media or a text message.
         */
        public static List<UpdateTypes> getMediaUpdates() {
            return Arrays.asList(POLL, PHOTO, VIDEO, ANIMATION, AUDIO, DICE,
                    INVOICE, STICKER, VIDEO_NOTE, CONTACT, FORWARD_FROM, GAME,
                    PRIVATE_MESSAGE, GROUP_MESSAGE, SUPERGROUP_MESSAGE
            );
        }

        /**
         * Translates an enum value to the corresponding method name used to recognize the update type.
         * The enum value is lower cased, and the first letter after an underscore is capitalized.
         *
         * @return The method name used to recognize the update type.
         */
        public String getMethodName() {
            StringBuilder sb = new StringBuilder();
            String[] split = this.name().split("_");
            sb.append(split[0].toLowerCase());
            if (split.length == 1)
                return sb.toString().toLowerCase();
            for (int i = 1; i < split.length; i++) {
                sb.append(split[i].substring(0, 1).toUpperCase()).append(split[i].substring(1).toLowerCase());
            }
            return sb.toString();
        }
    }

    public interface UpdateTypes {
        /**
         * Each update type must map to a method to retrieve the method name
         * used to make sure that the update received is of that type.
         *
         * @return The method name used to recognize the update type.
         */
        String getMethodName();
    }
}
