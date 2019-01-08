package engima.waratsea.model.game.event.ship;

/**
 * Represents the type of ship event actions.
 */
public enum ShipEventAction {
    ATTACKED("attacked"),
    SPOTTED("spotted"),
    DAMAGED_SURFACE_COMBAT("damaged by surface combat"),
    DAMAGED_AIR_ATTACK("damaged by air attack"),
    DAMAGED_SUB_ATTACK("damaged by submarine attack"),
    SUNK("sunk");

    private String value;

    /**
     * Constructor.
     * @param value String value of the enum.
     */
    ShipEventAction(final String value) {
        this.value = value;
    }

    /**
     * Returns the string value of the enum.
     * @return The enum's string value.
     */
    @Override
    public String toString() {
        return value;
    }
}
