package engima.waratsea.model.game;

import lombok.Getter;

/**
 * This is used so that a scenario can set the starting turn.
 */
public enum TurnIndex {
    DAY_1(0, "06:00 - 10:00") {
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
    DAY_2(1, "10:00 - 14:00") {
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
    DAY_3(2, "14:00 - 18:00") {
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
    TWILIGHT(3, "18:00 - 22:00") {
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
    NIGHT_1(4, "22:00 - 02:00") {
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
    NIGHT_2(5, "02:00 - 06:00") {
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

    @Getter
    private final String timeRange;

    /**
     * Constructor.
     *
     * @param value The integer value.
     * @param timeRange The corresponding time range of the index.
     */
    TurnIndex(final int value, final String timeRange) {
        this.value = value;
        this.timeRange = timeRange;
    }

    /**
     * Advance to the next index.
     *
     * @return The next index.
     */
    abstract TurnIndex next();
}
