package engima.waratsea.model.game.event.ship;

import engima.waratsea.model.game.AssetType;
import engima.waratsea.model.game.event.Event;
import engima.waratsea.model.game.event.EventDispatcher;
import engima.waratsea.model.game.event.EventHandler;
import engima.waratsea.model.ship.Ship;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Indicates that a ship event has occurred in the game.
 */
@Slf4j
public class ShipEvent extends Event {
    private static final EventDispatcher<ShipEvent> DISPATCHER = new EventDispatcher<>("ShipEvent");

    /**
     * Initialize the ship event class. This method clears out all ship event handlers.
     */
    public static void init() {
        DISPATCHER.clear();
    }

    /**
     * This is how event handlers register to receive ship event notifications.
     *
     * @param handler The object that handles the event.
     * @param shipEventHandler The ship event handler that is registered.
     */
    public static void register(final Object handler, final EventHandler<ShipEvent> shipEventHandler) {
        DISPATCHER.register(handler, shipEventHandler);
    }

    /**
     * This is how event handlers unregister for ship event notifications.
     *
     * @param handler The ship event handler that is unregistered.
     */
    public static void unregister(final Object handler) {
        DISPATCHER.unregister(handler);
    }

    @Getter
    @Setter
    private Ship ship;                      // The ship that experiences the event. The ship damaged, cargo loaded, sunk or spotted.

    @Getter
    @Setter
    private ShipEventAction action;         // The action that the ship experienced. Damaged, cargo loaded, sunk, spotted, etc.

    @Getter
    @Setter
    private AssetType by;                   // The game asset ship, sub or aircraft that caused the event. The asset that did the event. Not all events have a by.

    /**
     * This is how an event is fired and all the event handlers receive
     * notification of the event.
     */
    @Override
    public void fire() {
        log();
        DISPATCHER.fire(this);
    }

    /**
     * Log the event.
     */
    private void log() {
        if (by == null) {
            log.info("Fire event: {} {} at {}", new Object[]{ship.getName(), action, ship.getTaskForce().getReference()});
        } else {
            log.info("Fire event: {} {} at {} by {}", new Object[]{ship.getName(), action, ship.getTaskForce().getReference(), by});
        }
    }
}
