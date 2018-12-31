package engima.waratsea.model.game;

/**
 * WW2 at sea contains two combatants or sides.
 */
public enum Side {

    ALLIES("Allies"),
    AXIS("Axis");

    private String value;

    /**
     * Constructs a Side.
     * @param value The string representation of the side.
     */
    Side(final String value) {
        this.value = value;
    }

    /**
     * Given a side this method returns the opposing side.
     * @return The opposing side is returned.
     */
    public Side opposite() {
        return this == ALLIES ? AXIS : ALLIES;
    }

    /**
     * The string representation of this enum.
     * @return The string health of the enum.
     */
    @Override
    public String toString() {
        return value;
    }
}
