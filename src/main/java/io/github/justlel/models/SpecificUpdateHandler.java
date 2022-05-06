package io.github.justlel.models;

import com.pengrad.telegrambot.model.Update;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is an abstraction of a specific update handler.
 * A specific update handler is defined as a handler that behaves differently depending
 * on changes of a specific field/s of the update object, and doesn't therefore behave
 * on a general bases. For example, a specific update handler could be used to handle
 * updates of a type like {@link UpdatesDispatcher.MessageUpdateTypes#COMMAND},
 * as they are all defined by the same UpdateType, and are yet managed differently depending on the command received.
 * Because of this, a specific update handler registers different handlers, all of which are indexed by a specific identifier.
 * The type of the identifier is determined by the parameter T, while the dispatching of the update is managed by the
 * method {@link #dispatchUpdate}, as defined by the child classes.
 * To register a handler for a specific update, use the {@link #registerSpecificHandler} method.
 * To see an example of Generic update handler, check the class {@link GenericUpdateHandler}.
 *
 * @param <T> The type of the identifier for the update handler.
 * @author justlel
 * @version 1.0
 * @see GenericUpdateHandler
 */
public abstract class SpecificUpdateHandler<T> extends AbstractUpdateHandler {

    /**
     * The map of specific handlers.
     * The key is the identifier, of type T, and the value is the handler of the update.
     */
    private final Map<T, HandlerInterface> specificHandlers = new HashMap<>();


    /**
     * Updates the contents of the specific handlers map.
     *
     * @param identifier      The identifier of the handler.
     * @param specificHandler The handler to be registered.
     */
    public void registerSpecificHandler(T identifier, HandlerInterface specificHandler) {
        specificHandlers.putIfAbsent(identifier, specificHandler);
    }

    /**
     * Updates the contents of the specific handlers map.
     * This method can be used to map updates with a different identifier to one same handler.
     *
     * @param identifiers      A list of identifiers to be mapped to the handler.
     * @param specificHandlers The handler to be registered.
     */
    public void registerSpecificHandler(List<T> identifiers, HandlerInterface specificHandlers) {
        identifiers.forEach(identifier -> registerSpecificHandler(identifier, specificHandlers));
    }

    /**
     * Returns the specific handler for the given identifier.
     *
     * @param identifier The identifier of the handler.
     * @return The handler of the update.
     */
    public HandlerInterface getSpecificHandler(T identifier) {
        return specificHandlers.get(identifier);
    }

    /**
     * Updates the contents of the specific handlers map, removing the handler with the given identifier.
     *
     * @param identifier The identifier of the handler to be removed.
     */
    public void removeSpecificHandler(T identifier) {
        specificHandlers.remove(identifier);
    }

    /**
     * Dispatch the update received to a specific sub-handler.
     * The process of dispatching is defined by the child classes, as long as
     * the overridden method returns an UpdateHandler that is a child of {@link AbstractUpdateHandler},
     * or null if no handler is found.
     *
     * @param update The update to be dispatched.
     * @return The handler responsible for managing the dispatched update.
     */
    public abstract @Nullable AbstractUpdateHandler dispatchUpdate(Update update);

    /**
     * Overrides the method {@link AbstractUpdateHandler#returnUpdateHandler}.
     * Since the updates received have to be dispatched to a specific handler,
     * this method always returns the handler given by the {@link #dispatchUpdate} method.
     *
     * @param update The update of which to get the handler.
     * @return The handler responsible for managing the dispatched update.
     */
    @Override
    public AbstractUpdateHandler returnUpdateHandler(Update update) {
        return dispatchUpdate(update);
    }
}
