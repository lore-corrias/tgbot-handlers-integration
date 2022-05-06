package com.justlel.tgbot.models;

import com.pengrad.telegrambot.model.Update;

/**
 * Interface to be implemented by all update handlers.
 * Its only method, {@link #handleUpdate}, will be called by {@link AbstractUpdateHandler}
 * once the update has been dispatched to the correct handler.
 *
 * @author justlel
 * @version 1.0
 */
public interface HandlerInterface {

    /**
     * Once the update is dispatched, it will be passed to this method, which will
     * take care of executing the correct actions specified for the update.
     *
     * @param update The update to be handled.
     */
    void handleUpdate(Update update);
}
