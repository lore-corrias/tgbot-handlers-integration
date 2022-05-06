package com.justlel.tgbot.models;


import com.pengrad.telegrambot.model.Update;

/**
 * This class is an abstraction of a generic update handler.
 * A generic update handler is defined as an update handler that has no need
 * to dispatch a received update to a second specific handler. For example,
 * the updates received having a type like {@link com.justlel.tgbot.models.UpdatesDispatcher.GenericUpdateTypes#CHAT_JOIN_REQUEST}
 * can all easily be handled by the same update handler, since the update object has pretty much nothing specific to it.
 * On the other hand, an update of the type like {@link com.justlel.tgbot.models.UpdatesDispatcher.MessageUpdateTypes#COMMAND}
 * is generally handled depending on the command that is being received, and as such it would be easier to have a sub set of
 * handlers specific for the command received. There is no correlation between the update type and the type of handler to be
 * used, as this difference is purely arbitrary. For example, a specific handler could be used for the generic type
 * {@link com.justlel.tgbot.models.UpdatesDispatcher.GenericUpdateTypes#CHANNEL_POST}, in order to behave differently depending on the message received.
 * A generic update handler, having no sub-handlers, has the method {@link #returnUpdateHandler} return nothing but itself,
 * as it is the handler for all the update received matching the update type it is handling, regardless of their differences.
 *
 * @author justlel
 * @version 1.0
 */
public abstract class GenericUpdateHandler extends AbstractUpdateHandler implements HandlerInterface {

    /**
     * Since the update handler has no sub-handlers, this method will return the instance of this class,
     * as it is the handler defined for all the updates received matching the update type it is handling.
     *
     * @param update The update of which to get the handler.
     * @return The instance of this class.
     */
    @Override
    public HandlerInterface returnUpdateHandler(Update update) {
        return this;
    }
}
