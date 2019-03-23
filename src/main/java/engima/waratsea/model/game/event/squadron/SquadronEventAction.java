package engima.waratsea.model.game.event.squadron;

/**
 * Represents the type of squadron event actions. A squadron may be damaged, destroyed, etc.
 */
public enum SquadronEventAction {
    ARRIVAL("arrival"),
    DAMAGED("damaged"),
    DESTROYED("destroyed");

    private String value;

    /**
     * Constructor.
     *
     * @param value String value of the enum.
     */
    SquadronEventAction(final String value) {
        this.value = value;
    }

    /**
     * Returns the string value of the enum.
     *
     * @return The enum's string value.
     */
    @Override
    public String toString() {
        return value;
    }
}
