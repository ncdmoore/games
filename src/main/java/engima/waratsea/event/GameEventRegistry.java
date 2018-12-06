package engima.waratsea.event;

import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is the event register which keeps track of all event handlers.
 */
@Slf4j
@Singleton
public class GameEventRegistry {
    private static Map<Class<?>, List<GameEventHandler>> registry = new HashMap<>();

    /**
     * Register for an event.
     * @param eventClass -
     * @param eventHandler -
     * @param <E> -
     */
    public <E extends GameEvent> void register(final Class<? extends GameEvent> eventClass, final GameEventHandler<E> eventHandler) {

        if (!registry.containsKey(eventClass)) {
            List<GameEventHandler> handlers = new ArrayList<>();
            registry.put(eventClass, handlers);
        }

        // This exact event handler already receives events of this type.
        if (registry.get(eventClass).contains(eventHandler)) {
            log.warn("Duplicate handler {} for event {}", eventHandler.getClass(), eventClass);
            return;
        }

        log.info("Register {} for {}", eventClass, eventHandler);
        registry.get(eventClass).add(eventHandler);
        log.info("{} contains {} handlers", eventClass, registry.get(eventClass).size());

    }

    /**
     * Unregister for an event.
     * @param eventClass -
     * @param eventHandler -
     * @param <E> -
     */
    public <E> void unregister(final Class<? extends GameEvent> eventClass, final GameEventHandler<E> eventHandler) {

        log.info("Unegister {} for {}", eventClass, eventHandler);

        if (registry.containsKey(eventClass)) {
           List<GameEventHandler> handlers = registry.get(eventClass);

           // Since the event handler can unregister during the processing of the notification
           // we make a copy of the current list of handlers and update the copy. Then we
           // replace the newly updated copy in the registry. This keeps this method from
           // updating/writing to the registry while the fire method below is still using the
           // registry. This avoids a data exception being thrown.
           if (handlers.contains(eventHandler)) {
               List<GameEventHandler> updatedHandlers = new ArrayList<>(handlers);
               updatedHandlers.remove(eventHandler);
               registry.put(eventClass, updatedHandlers);
               log.info("{} contains {} handlers", eventClass, registry.get(eventClass).size());
           }
       }
    }

    /**
     * Fire the given event.
     *
     * @param event the event that fired.
     */
    @SuppressWarnings("unchecked")
    public void fire(final GameEvent event) {

        log.info("fire event: '{}'", event.getClass());

        List<GameEventHandler> handlers = registry.get(event.getClass());
        handlers.forEach(handler -> handler.notify(event));
    }
}
