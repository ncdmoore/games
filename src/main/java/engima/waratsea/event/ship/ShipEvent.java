package engima.waratsea.event.ship;

import engima.waratsea.event.GameEvent;
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
    private static transient List<ShipEventHandler> handlers = new ArrayList<>();
    private static final transient String WILDCARD = "*";

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
     * @param other The other ship event to test for equality.
     * @return True if the ship events are equal. False otherwise.
     */
    @Override
    public boolean equals(final Object other) {
        if (other instanceof ShipEvent) {

            ShipEvent otherShipEvent = (ShipEvent) other;

            return side == otherShipEvent.side
                    && action == otherShipEvent.action
                    && isShipTypeEqual(otherShipEvent.shipType)
                    && isTaskForceNameEqual(otherShipEvent.taskForceName);

        } else {
            return false;
        }
    }

    /**
     * Defined just to make findbugs happy.
     *
     * @return The super classes hashcode.
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * Determine if the task force names between the two ship events are equal.
     *
     * @param otherTaskForceName The other task force name
     * @return True if the two task force names are equal. False otherwise.
     */
    private boolean isTaskForceNameEqual(final String otherTaskForceName) {
        return taskForceName == null || otherTaskForceName == null                                                      // Non specified task force name matches all.
                || taskForceName.equalsIgnoreCase(otherTaskForceName)
                || taskForceName.equalsIgnoreCase(WILDCARD)
                || otherTaskForceName.equalsIgnoreCase(WILDCARD);
    }

    /**
     * Determine if the ship types between two ship events are equal.
     *
     * @param otherShipType The other ship event's ship type.
     * @return True if the two ship event's ship types are equal. False otherwise.
     */
    private boolean isShipTypeEqual(final ShipEventType otherShipType) {
        return shipType == ShipEventType.ANY
                || otherShipType == ShipEventType.ANY
                || shipType == otherShipType;

    }
}
