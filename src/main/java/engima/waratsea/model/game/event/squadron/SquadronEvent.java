package engima.waratsea.model.game.event.squadron;

import engima.waratsea.model.game.AssetType;
import engima.waratsea.model.game.event.GameEvent;
import engima.waratsea.model.game.event.GameEventHandler;
import engima.waratsea.model.squadron.Squadron;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Indicates that a squadron event has occurred in the game.
 */
@Slf4j
public class SquadronEvent extends GameEvent {
    private static transient List<GameEventHandler<SquadronEvent>> handlers = new ArrayList<>();
    private static transient Map<Object, GameEventHandler<SquadronEvent>> map = new HashMap<>();

    /**
     * Initialize the squadron event class. This method clears out all squadron event handlers.
     */
    public static void init() {
        handlers.clear();
        map.clear();
    }

    /**
     * This is how event handlers register to receive squadron event notifications.
     *
     * @param handler The object that handles the event.
     * @param squadronEventHandler The squadron event handler that is registered.
     */
    public static void register(final Object handler, final GameEventHandler<SquadronEvent> squadronEventHandler) {
        map.put(handler, squadronEventHandler);
        handlers = add(SquadronEvent.class, handlers, squadronEventHandler);
    }

    /**
     * This is how event handlers unregister for squadron event notifications.
     *
     * @param handler The squadron event handler that is unregistered.
     */
    public static void unregister(final Object handler) {
        if (map.containsKey(handler)) {
            handlers = remove(SquadronEvent.class, handlers, map.get(handler));
        }
    }

    @Getter
    @Setter
    private Squadron squadron;              // The squadron that experiences the event. The squadron damaged, destroyed, etc.

    @Getter
    @Setter
    private SquadronEventAction action;     // The action that the squadron experienced. Damaged, destroyed, etc.

    @Getter
    @Setter
    private AssetType by;                       // The game asset ship, sub or aircraft that caused the event. The asset that did the event. Not all events have a by.

    /**
     * This is how an event is fired and all the event handlers receive
     * notification of the event.
     */
    public void fire() {
        log();
        handlers.forEach(h -> h.notify(this));
    }

    /**
     * Log the event.
     */
    private void log() {
        if (by == null) {
            log.info("Fire event: {} {} at {}", new Object[]{squadron.getName(), action, squadron.getLocation()});
        } else {
            log.info("Fire event: {} {} at {} by {}", new Object[]{squadron.getName(), action, squadron.getLocation(), by});
        }
    }
}
