package engima.waratsea.event.ship;

import engima.waratsea.event.GameEvent;
import engima.waratsea.event.GameEventHandler;
import engima.waratsea.model.game.Side;
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

    private static final transient String WILDCARD = "*";

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
    private ShipEventType shipType;

    /**
     * This is how an event is fired and all the event handlers receive
     * notification of the event.
     */
    public void fire() {
        log.info("Fire ship event: {}", action);
        handlers.forEach(h -> h.notify(this));
    }

    /**
     * Determines if two ship events are equal.
     *
     * @param firedEvent The other ship event to test for equality.
     * @return True if the ship events are equal. False otherwise.
     */
    public boolean match(final ShipEvent firedEvent) {

        return side == firedEvent.side
                && action == firedEvent.action
                && isShipTypeEqual(firedEvent.shipType)
                && isTaskForceNameEqual(firedEvent.taskForceName);

    }

    /**
     * Determine if the task force names between the two ship events are equal.
     *
     * @param firedTaskForceName The other task force name
     * @return True if the two task force names are equal. False otherwise.
     */
    private boolean isTaskForceNameEqual(final String firedTaskForceName) {
        return taskForceName == null || firedTaskForceName == null                                                      // Non specified task force name matches all.
                || taskForceName.equalsIgnoreCase(firedTaskForceName)
                || taskForceName.equalsIgnoreCase(WILDCARD)
                || firedTaskForceName.equalsIgnoreCase(WILDCARD);
    }

    /**
     * Determine if the ship types between two ship events are equal.
     *
     * @param firedShipType The other ship event's ship type.
     * @return True if the two ship event's ship types are equal. False otherwise.
     */
    private boolean isShipTypeEqual(final ShipEventType firedShipType) {
        return shipType == ShipEventType.ANY
                || firedShipType == ShipEventType.ANY
                || shipType == firedShipType;

    }
}
