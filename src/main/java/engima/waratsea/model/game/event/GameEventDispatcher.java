package engima.waratsea.model.game.event;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is responsible for delivering events to event handlers.
 *
 * @param <E> The type of event.
 */
@Slf4j
public class GameEventDispatcher<E extends GameEvent> {
    private final Map<Object, GameEventHandler<E>> map = new HashMap<>();

    /**
     * Clear the dispatcher.
     */
    public void clear() {
        map.clear();
    }

    /**
     * Register a handler for the given event type.
     *
     * @param key The object that registered for the event.
     * @param handler The object's handler for the event.
     */
    public void register(final Object key, final GameEventHandler<E> handler) {
        log.info("{}: registers", key);
        map.put(key, handler);
    }

    /**
     * This is how event handlers unregister for ship event notifications.
     *
     * @param key The object that registered for the event.
     */
    public void unregister(final Object key) {
        log.info("{}: unregisters", key);
        map.remove(key);
    }

    /**
     * This is how an event is fired and all the event handlers receive
     * notification of the event.
     *
     * @param e The event
     */
    public void fire(final E e) {
        // We need to copy the handler list because a handler may unregister while doing the actual event processing.
        // If we don't copy then we may end up with map's keys being modified while we are trying to iterate over them.
        // This can lead to an memory corruption exception.
        List<GameEventHandler<E>> handlers = new ArrayList<>(map.values());

        handlers.forEach(h -> h.notify(e));
    }
}
