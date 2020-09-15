package engima.waratsea.model.game;

import engima.waratsea.model.game.rules.Rules;
import lombok.Getter;


/**
 * Represents the stages of a day.
 */
public enum TurnType {
    DAY("Day") {     // A daylight turn.
        /**
         * Get the turn type's true type. DAY turns are always DAY turns.
         *
         * @param rules The game rules.
         * @param month The game month.
         * @return DAY.
         */
        @Override
        public TurnType getTrue(final Rules rules, final int month) {
            return DAY;
        }
    },

    TWILIGHT("Twilight") { // A twilight turn.
        /**
         * Get the turn type's true type. Twilight turns are either treated as DAY or NIGHT turns.
         * Depending on the game type and the game month.
         *
         * @param rules The game rules.
         * @param month The game month.
         * @return The true type of turn either DAY or NIGHT.
         */
        @Override
        public TurnType getTrue(final Rules rules, final int month) {
            return rules.getTwilightTurnType(month);
        }
    },
    NIGHT("Night") {    // A night turn.
        /**
         * Get the turn type's true type. Twilight turns are either treated as DAY or NIGHT turns.
         *
         * @param rules The game rules.
         * @param month The game month.
         * @return The true type of turn either DAY or NIGHT.
         */
        @Override
        public TurnType getTrue(final Rules rules, final int month) {
            return NIGHT;
        }

    };

    @Getter
    private final String value;

    /**
     * Constructor.
     *
     * @param value The String representation of this enum.
     */
    TurnType(final String value) {
        this.value = value;
    }

    /**
     * Get the turn type's true type. Twilight turns are either treated as DAY or NIGHT turns.
     *
     * @param rules The game rules.
     * @param month The game month.
     * @return The true type of turn either DAY or NIGHT.
     */
    public abstract TurnType getTrue(Rules rules, int month);

    /**
     * Get the String representation of this enum.
     *
     * @return The String representation of this enum.
     */
    @Override
    public String toString() {
        return value;
    }

    /**
     * Get the lower case string representation.
     *
     * @return The lower case string representation.
     */
    public String toLower() {
        return toString().toLowerCase();
    }

}
