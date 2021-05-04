package engima.waratsea.model.squadron;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

import java.math.BigDecimal;

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
    FULL("Full", StepSize.TWO) {
        @Override
        public SquadronStrength reduce() {
            return HALF;
        }

        @Override
        public int getAsSteps() {
            return 2;
        }
    },

    @SerializedName(value = "HALF", alternate = {"Half", "half"})
    HALF("Half", StepSize.ONE) {
        @Override
        public SquadronStrength reduce() {
            return ZERO;
        }

        @Override
        public int getAsSteps() {
            return 1;
        }
    },

    @SerializedName(value = "SIXTH", alternate = {"Sixth", "sixth"})
    SIXTH("1/6", StepSize.ONE_THIRD) { // Sixth is for battleship and cruiser float planes.
        @Override
        public SquadronStrength reduce() {
            return ZERO;
        }

        @Override
        public int getAsSteps() {
            return 0;
        }
    },

    @SerializedName(value = "ZERO", alternate = {"Zero", "zero"})
    ZERO("Zero", StepSize.ZERO) {
        @Override
        public SquadronStrength reduce() {
            return ZERO;
        }

        @Override
        public int getAsSteps() {
            return 0;
        }
    };

    private final String value;

    @Getter
    private final BigDecimal steps;

    /**
     * The constructor.
     *
     * @param value The String value of the squadron strength.
     * @param steps The number of steps that squadron strength maps to.
     */
    SquadronStrength(final String value, final String steps) {
        this.value = value;
        this.steps = new BigDecimal(steps);
    }

    public abstract SquadronStrength reduce();

    public abstract int getAsSteps();

    /**
     * The String representation of the squadron strength.
     *
     * @return The string representation of the squadron strength.
     */
    @Override
    public String toString() {
        return value;
    }
}
