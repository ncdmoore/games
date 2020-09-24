package engima.waratsea.model.game;

import lombok.Getter;

import java.util.List;

/**
 * WW2 at sea contains two combatants or sides.
 */
public enum Side {
    ALLIES("Allies", "Allied"),
    AXIS("Axis", "Axis"),
    NEUTRAL("Neutral", "Neutral");

    @Getter
    private final String value;

    @Getter
    private final String possessive;

    /**
     * Constructs a Side.
     *
     * @param value The string representation of the side.
     * @param possessive The possessive form of the word.
     */
    Side(final String value, final String possessive) {
        this.value = value;
        this.possessive = possessive;
    }

    /**
     * Given a side this method returns the opposing side.
     *
     * @return The opposing side is returned.
     */
    public Side opposite() {
        switch (this) {
            case ALLIES:
                return AXIS;
            case AXIS:
                return ALLIES;
            default:
                return NEUTRAL;
        }
    }

    /**
     * Get the friendly sides for this side.
     *
     * @return The friendly sides.
     */
    public List<Side> getFriendly() {
        switch (this) {
            case ALLIES:
                return List.of(ALLIES, NEUTRAL);
            case AXIS:
                return List.of(AXIS, NEUTRAL);
            default:
                return List.of(NEUTRAL);
        }
    }

    /**
     * The string representation of this enum.
     *
     * @return The string health of the enum.
     */
    @Override
    public String toString() {
        return value;
    }

    /**
     * The lower case string representation of this enum.
     *
     * @return The lower case string representation of this enum.
     */
    public String toLower() {
        return value.toLowerCase();
    }
}
