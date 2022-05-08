package io.github.justlel.tgbot.models;

import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;

import java.util.List;

/**
 * Abstract representation of an update dispatcher.
 * The class acts as an intermediary between {@link UpdatesListener}, which is capable of dispatching
 * multiple updates at a time, and the child classes, which should handle only one update at a time.
 * Each update passed from the UpdatesListener is thus dispatched to the required handler class.
 *
 * @author justlel
 * @version 1.0
 * @see UpdatesDispatcher
 */
abstract class AbstractUpdateDispatcher implements UpdatesListener {

    /**
     * The list provided by the method should ideally be a singleton, but this is not guaranteed.
     * In fact, the number of updates retrieved depends on the return value of the method.
     * Therefor, each received update is passed to the {@link #dispatchUpdate} method, so that
     * it can be confirmed that all updates have been dispatched. If this last thing is not
     * correctly managed, the bot will continue to receive old, unhandled updates.
     *
     * @param updates The list of updates to be dispatched.
     * @return The number of updates that have been dispatched, in this case, all of them.
     */
    @Override
    public int process(List<Update> updates) {
        for (Update update : updates) {
            dispatchUpdate(update);
        }
        return CONFIRMED_UPDATES_ALL;
    }

    /**
     * The method implemented by the child classes to dispatch the received update.
     * The function doesn't have a return value, because whether the update has been
     * actually handled or not, is not relevant to the dispatcher.
     *
     * @param update The update to be dispatched.
     */
    protected abstract void dispatchUpdate(Update update);
}
