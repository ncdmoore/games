package engima.waratsea.model.game.event;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class is responsible for delivering events to event handlers.
 *
 * @param <E> The type of event.
 */
@Slf4j
public class GameEventDispatcher<E extends GameEvent> {
    private List<GameEventHandler<E>> handlers = new ArrayList<>();
    private final Map<Object, GameEventHandler<E>> map = new HashMap<>();

    /**
     * Clear the dispatcher.
     */
    public void clear() {
        handlers.clear();
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
        add(handler);
    }

    /**
     * This is how event handlers unregister for ship event notifications.
     *
     * @param key The object that registered for the event.
     */
    public void unregister(final Object key) {
        log.info("{}: unregisters", key);
        if (map.containsKey(key)) {
            remove(map.get(key));
            map.remove(key);
        }
    }

    /**
     * This is how an event is fired and all the event handlers receive
     * notification of the event.
     *
     * @param e The event
     */
    public void fire(final E e) {
        handlers.forEach(h -> h.notify(e));
    }

    /**
     * Add an event handler to the list of event handlers.
     *
     * @param handler The event handler that is placed in the event handler list.
     */
    private void add(final GameEventHandler<E> handler) {
        // This exact event handler already receives events of this type.
        if (handlers.contains(handler)) {
            log.warn("Duplicate handler: {}",  handler.getClass());
            return;
        }

        List<GameEventHandler<E>> updatedList = new ArrayList<>(handlers);

        log.debug("Register handler: {}", handler);
        updatedList.add(handler);
        log.debug("Contains {} handlers", updatedList.size());

        handlers = updatedList;
    }

    /**
     * Remove an event handler from the list of event handlers.
     *
     * @param handler The event handler that is removed.
     */
    private void remove(final GameEventHandler<E> handler) {
        log.debug("Unregister handler: {}", handler);

        // Since the event handler can unregister during the processing of the notification
        // we make a copy of the current list of handlers and update the copy. Then we
        // replace the newly updated copy in the registry. This keeps this method from
        // updating/writing to the registry while the fire method below is still using the
        // registry. This avoids a data exception being thrown.
        handlers = handlers
                .stream()
                .filter(element -> element != handler)
                .collect(Collectors.toList());
    }
}
