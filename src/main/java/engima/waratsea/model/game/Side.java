package engima.waratsea.model.game;

import lombok.Getter;

/**
 * WW2 at sea contains two combatants or sides.
 */
public enum Side {

    ALLIES("Allies", "Allied"),
    AXIS("Axis", "Axis");

    private final String value;

    @Getter
    private final String possesive;

    /**
     * Constructs a Side.
     * @param value The string representation of the side.
     * @param possesive The possesive form of the word.
     */
    Side(final String value, final String possesive) {
        this.value = value;
        this.possesive = possesive;
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
