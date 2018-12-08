package engima.waratsea.event;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the event base class.
 */
@Slf4j
public abstract class GameEvent {

    /**
     * Initialize all game events.
     */
    public static void init() {
        ShipEvent.init();
        TurnEvent.init();
    }

    /**
     * Add an event handler to the list of event handlers.
     *
     * @param clazz The event class.
     * @param list The event handler list.
     * @param handler The event handler that is placed in the event handler list.
     * @param <T> The event handler type.
     * @return The updated list with the event handler added.
     */
    protected static <T> List<T> add(final Class<?> clazz, final List<T> list, final T handler) {

        // This exact event handler already receives events of this type.
        if (list.contains(handler)) {
            log.warn("{}: Duplicate handler {}", clazz, handler.getClass());
            return list;
        }

        List<T> updatedList = new ArrayList<>(list);

        log.info("{}: Register handler {}", clazz.getSimpleName(), handler);
        updatedList.add(handler);
        log.info("{}: Contains {} handlers", clazz.getSimpleName(), updatedList.size());
        return updatedList;
    }

    /**
     * Remove an event handler from the list of event handlers.
     *
     * @param clazz The event class.
     * @param list The event handler list.
     * @param handler The event handler that is removed.
     * @param <T> The event handler type.
     * @return The updated list with the event handler removed.
     */
    protected static <T> List<T> remove(final Class<?> clazz, final List<T> list, final T handler) {
        log.info("{}: Unegister  {}", clazz.getSimpleName(), handler.getClass());

        // Since the event handler can unregister during the processing of the notification
        // we make a copy of the current list of handlers and update the copy. Then we
        // replace the newly updated copy in the registry. This keeps this method from
        // updating/writing to the registry while the fire method below is still using the
        // registry. This avoids a data exception being thrown.
        List<T> updatedList = list;
        if (list.contains(handler)) {
            updatedList = new ArrayList<>(list);
            updatedList.remove(handler);
            log.info("{}: Contains {} handlers", clazz.getSimpleName(), list.size());
        }

        return updatedList;
    }
}
