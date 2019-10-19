package engima.waratsea.model.game;

import lombok.Getter;

/**
 * This is used so that a scenario can set the starting turn.
 */
public enum TurnIndex {
    DAY_1(0) {
        /**
         * Advance to the next index.
         *
         * @return The next index.
         */
        @Override
        public TurnIndex next() {
            return DAY_2;
        }
    },
    DAY_2(1) {
        /**
         * Advance to the next index.
         *
         * @return The next index.
         */
        @Override
        TurnIndex next() {
            return DAY_3;
        }
    },
    DAY_3(2) {
        /**
         * Advance to the next index.
         *
         * @return The next index.
         */
        @Override
        TurnIndex next() {
            return TWILIGHT;
        }
    },
    TWILIGHT(3) {
        /**
         * Advance to the next index.
         *
         * @return The next index.
         */
        @Override
        TurnIndex next() {
            return NIGHT_1;
        }
    },
    NIGHT_1(4) {
        /**
         * Advance to the next index.
         *
         * @return The next index.
         */
        @Override
        TurnIndex next() {
            return NIGHT_2;
        }
    },
    NIGHT_2(5) {
        /**
         * Advance to the next index.
         *
         * @return The next index.
         */
        @Override
        TurnIndex next() {
            return DAY_1;
        }
    };

    @Getter
    private final int value;

    /**
     * Constructor.
     *
     * @param value The integer value.
     */
    TurnIndex(final int value) {
        this.value = value;
    }

    /**
     * Advance to the next index.
     *
     * @return The next index.
     */
    abstract TurnIndex next();
}
