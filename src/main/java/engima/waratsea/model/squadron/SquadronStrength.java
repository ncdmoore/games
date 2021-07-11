package engima.waratsea.model.squadron;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

/**
 * Represents the strength of a squadron.
 *
 * Squadrons are composed of steps. Two steps equal a squadron at full strength.
 * A squadron at half strength  is composed of only a single step.
 *
 * Seaplanes on cruisers and battleships are equal to a squadron at 1/6 th strength.
 */
public enum SquadronStrength {
    @SerializedName(value = "FULL", alternate = {"Full", "full"})
    FULL("Full", 2, 12) {
        @Override
        public SquadronStrength reduce() {
            return HALF;
        }
    },

    @SerializedName(value = "HALF", alternate = {"Half", "half"})
    HALF("Half", 1, 6) {
        @Override
        public SquadronStrength reduce() {
            return ZERO;
        }
    },

    @SerializedName(value = "SIXTH", alternate = {"Sixth", "sixth"})
    SIXTH("1/6", 0, 2) { // Sixth is for battleship and cruiser float planes.
        @Override
        public SquadronStrength reduce() {
            return ZERO;
        }
    },

    @SerializedName(value = "ZERO", alternate = {"Zero", "zero"})
    ZERO("Zero", 0, 0) {
        @Override
        public SquadronStrength reduce() {
            return ZERO;
        }
    };

    private final String value;

    @Getter
    private final int steps;

    @Getter
    private final int aircraft;

    private static final int AIRCRAFT_PER_STEP_SIZE = 6;

    /**
     * The constructor.
     *
     * @param value The String value of the squadron strength.
     * @param steps The number of steps.
     * @param aircraft The number of aircraft.
     */
    SquadronStrength(final String value, final int steps, final int aircraft) {
        this.value = value;
        this.steps = steps;
        this.aircraft = aircraft;
    }

    public abstract SquadronStrength reduce();

    /**
     * The String representation of the squadron strength.
     *
     * @return The string representation of the squadron strength.
     */
    @Override
    public String toString() {
        return value;
    }

    /**
     * Get the number of steps given the number of aircraft.
     *
     * @param aircraft The number of aircraft.
     * @return The steps size given the number of aircraft.
     */
    public static int calculateSteps(final int aircraft) {
        return aircraft / AIRCRAFT_PER_STEP_SIZE;
    }
}
