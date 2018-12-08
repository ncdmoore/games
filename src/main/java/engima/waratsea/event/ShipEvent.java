package engima.waratsea.event;

import engima.waratsea.model.game.Side;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * Indicates that a ship event has occurred in the game.
 */
@Slf4j
public class ShipEvent extends GameEvent {
    private static List<ShipEventHandler> handlers = new ArrayList<>();

    /**
     * Initialize the ship event class. This method clears out all ship event handlers.
     */
    public static void init() {
        handlers.clear();
    }

    /**
     * This is how event handlers register to receive ship event notifications.
     *
     * @param shipEventHandler The ship event handler that is registered.
     */
    public static void register(final ShipEventHandler shipEventHandler) {
        handlers = add(ShipEvent.class, handlers, shipEventHandler);
    }

    /**
     * This is how event handlers unregister for ship event notifications.
     *
     * @param shipEventHandler The ship event handler that is unregistered.
     */
    public static void unregister(final ShipEventHandler shipEventHandler) {
        handlers = remove(ShipEvent.class, handlers, shipEventHandler);
    }

    /**
     * This is how an event is fired and all the event handlers receive
     * notification of the event.
     */
    public void fire() {
        log.info("Fire ship event: {}", action);
        handlers.forEach(h -> h.notify(this));
    }

    @Getter
    @Setter
    private String action;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private Side side;

    @Getter
    @Setter
    private String shipType;

    /**
     * The constructor of ship events.
     */
    public ShipEvent() {
        shipType = "any";
        name = "any";
    }
}
