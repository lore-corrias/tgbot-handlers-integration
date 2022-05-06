package com.justlel.tgbot.models;

import com.pengrad.telegrambot.model.Update;

/**
 * Abstraction of an update handler class.
 * The update handlers are responsible for managing the updates
 * dispatched to them by the {@link UpdatesDispatcher}.
 * Being an abstraction, this class cannot be directly extended; instead, an update handler
 * should be created by extending either of the following classes:
 * {@link GenericUpdateHandler} or {@link SpecificUpdateHandler},
 * depending on whether the update received could be directly handled (Generic, in this case) or it has
 * to be handled in a specific way by a set of sub-handlers (Specific, in this case).
 * For examples of both, see the sample classes.
 *
 * @author justlel
 * @version 1.0
 * @see SpecificUpdateHandler
 * @see GenericUpdateHandler
 */
abstract class AbstractUpdateHandler implements HandlerInterface {

    /**
     * This method is responsible for handling the update once an appropriate handler is found.
     * The handler is provided by the {@link #returnUpdateHandler} method, which is defined by the
     * child classes, and can be implemented in different ways depending on the type of the update handler.
     * This method is implemented from the {@link HandlerInterface} interface.
     *
     * @param update The update to handle.
     */
    public void handleUpdate(Update update) {
        HandlerInterface handler = returnUpdateHandler(update);
        if (handler != null) {
            handler.handleUpdate(update);
        }
    }

    /**
     * This method retrieves the correct handler to be used for handling the update.
     * The method returns an instance of a class that implements the {@link HandlerInterface} interface,
     * and, more specifically, an instance of a class that extends either of the following classes:
     * {@link GenericUpdateHandler} or {@link SpecificUpdateHandler}.
     * If an update handler is generic, then the method can simply return the instance of the generic update handler.
     * However, if the update handler is specific, then the method must return an instance of a handler specific for that
     * update, once it has been dispatched correctly.
     *
     * @param update The update of which to get the handler.
     * @return The handler to be used for handling the update.
     */
    public abstract HandlerInterface returnUpdateHandler(Update update);
}
