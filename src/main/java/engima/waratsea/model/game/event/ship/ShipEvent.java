package engima.waratsea.model.game.event.ship;

import engima.waratsea.model.game.event.GameEvent;
import engima.waratsea.model.game.event.GameEventHandler;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.ships.ShipType;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Indicates that a ship event has occurred in the game.
 */
@Slf4j
public class ShipEvent extends GameEvent {
    private static transient List<GameEventHandler<ShipEvent>> handlers = new ArrayList<>();
    private static transient Map<Object, GameEventHandler<ShipEvent>> map = new HashMap<>();

    /**
     * Initialize the ship event class. This method clears out all ship event handlers.
     */
    public static void init() {
        handlers.clear();
        map.clear();
    }

    /**
     * This is how event handlers register to receive ship event notifications.
     *
     * @param handler The object that handles the event.
     * @param shipEventHandler The ship event handler that is registered.
     */
    public static void register(final Object handler, final GameEventHandler<ShipEvent> shipEventHandler) {
        map.put(handler, shipEventHandler);
        handlers = add(ShipEvent.class, handlers, shipEventHandler);
    }

    /**
     * This is how event handlers unregister for ship event notifications.
     *
     * @param handler The ship event handler that is unregistered.
     */
    public static void unregister(final Object handler) {
        if (map.containsKey(handler)) {
            handlers = remove(ShipEvent.class, handlers, map.get(handler));
        }
    }

    @Getter
    @Setter
    private ShipEventAction action;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private Side side;

    @Getter
    @Setter
    private String taskForceName;

    @Getter
    @Setter
    private ShipType shipType;

    /**
     * This is how an event is fired and all the event handlers receive
     * notification of the event.
     */
    public void fire() {
        log.info("Fire ship event: {}", action);
        handlers.forEach(h -> h.notify(this));
    }
}
