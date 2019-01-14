package engima.waratsea.model.game.event.airfield;

/**
 * Airfield event actions.
 */
public enum AirfieldEventAction {
    DAMAGE("damage"),
    REPAIR("repair");

    private String value;

    /**
     * Constructor.
     * @param value String value of the enum.
     */
    AirfieldEventAction(final String value) {
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
