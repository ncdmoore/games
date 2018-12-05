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
    private static Map<Class<?>, List<GameEventHandler>> unregistry = new HashMap<>();

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

        if (registry.get(eventClass).contains(eventHandler)) {
            log.warn("Duplicate handler {} for event {}", eventHandler.getClass(), eventClass);
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
    public <E> void stopFutureEvents(final Class<? extends GameEvent> eventClass, final GameEventHandler<E> eventHandler) {

        if (!unregistry.containsKey(eventClass)) {
            List<GameEventHandler> handlers = new ArrayList<>();
            unregistry.put(eventClass, handlers);
        }

        log.info("Stop future events {} for {}", eventClass, eventHandler);
        unregistry.get(eventClass).add(eventHandler);
    }

    /**
     * Fire the given event.
     *
     * @param event the event that fired.
     */
    @SuppressWarnings("unchecked")
    public void fire(final GameEvent event) {

        log.info("fire event: '{}' action: '{}'", event.getClass(), event.getAction());

        List<GameEventHandler> handlers = registry.get(event.getClass());
        handlers.forEach(handler -> handler.notify(event));

        // If the event handler has requested to stop receiving future events then the unregistry will contain an
        // entry for that event handler. Remove that event handler from the registry.
        //
        // Note, that the event handler cannot directly unregister as that causes an exception in that this
        // fire method is still executing and the event handler attempted to modify the handlers in use!
        // Thus, the event handler places itself in the un-registry by calling stopFutureEvents.
        // Once the event handler is completely through executing its code we come here and determine if the
        // event handler should be unregistered.
        //
        // Once an event handler is removed from the registry it is also removed from the un-registry.
        if (unregistry.containsKey(event.getClass())) {
            List<GameEventHandler> unregisteredHandlers = unregistry.get(event.getClass());
            handlers.removeAll(unregisteredHandlers);
            unregisteredHandlers.clear();
            log.info("{} contains {} handlers", event.getClass(), registry.get(event.getClass()).size());
        }
    }
}
