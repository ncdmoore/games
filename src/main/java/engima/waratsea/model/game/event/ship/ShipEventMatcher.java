package engima.waratsea.model.game.event.ship;

import engima.waratsea.model.game.Side;
import engima.waratsea.model.ships.ShipType;
import lombok.Getter;
import lombok.Setter;

/**
 * This class is used to match ship events. An entity that is looking for a particular ship event type can use this
 * class to detect if wanted event has occurred.
 */
public class ShipEventMatcher {

    private static final transient String WILDCARD = "*";

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
    private String shipType;

    /**
     * Determines if two ship events are equal.
     *
     * @param firedEvent The other ship event to test for equality.
     * @return True if the ship events are equal. False otherwise.
     */
    public boolean match(final ShipEvent firedEvent) {

        return side == firedEvent.getSide()
                && action == firedEvent.getAction()
                && isShipTypeEqual(firedEvent.getShipType())
                && isTaskForceNameEqual(firedEvent.getTaskForceName());
    }

    /**
     * Returns a text string indicating under what conditions the ship event is matched.
     * @return The text explanation string.
     */
    public String getExplanation() {

        String ship = shipType.equalsIgnoreCase("*") ? "ship" : ShipType.valueOf(shipType).toString();

        String explanation;
        if (taskForceName.equalsIgnoreCase(WILDCARD)) {
            explanation = "if an " + side.getPossesive() + " " + ship + " " + action + ".";
        } else {
            explanation = "if an " + side.getPossesive() + " " + ship + " in " + taskForceName + " " + action + ".";
        }

        return explanation;
    }

    /**
     * Determine if the task force names between the two ship events are equal.
     *
     * @param firedTaskForceName The other task force name
     * @return True if the two task force names are equal. False otherwise.
     */
    private boolean isTaskForceNameEqual(final String firedTaskForceName) {
        return taskForceName == null                                                                                    // Non specified task force name matches all.
                || taskForceName.equalsIgnoreCase(firedTaskForceName)
                || taskForceName.equalsIgnoreCase(WILDCARD);
    }

    /**
     * Determine if the ship types between two ship events are equal.
     *
     * @param firedShipType The other ship event's ship type.
     * @return True if the two ship event's ship types are equal. False otherwise.
     */
    private boolean isShipTypeEqual(final ShipType firedShipType) {
        return shipType.equalsIgnoreCase(WILDCARD)                                                                      // If wildcard then event ship type does not matter.
                || ShipType.valueOf(shipType) == firedShipType;
    }
}
