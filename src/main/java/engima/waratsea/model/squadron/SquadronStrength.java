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
    FULL("Full", StepSize.FULL),

    @SerializedName(value = "HALF", alternate = {"Half", "half"})
    HALF("Half", StepSize.HALF),

    @SerializedName(value = "SIXTH", alternate = {"Sixth", "sixth"})
    SIXTH("1/6", StepSize.ONE_THIRD); // Sixth is for battleship and cruiser float planes.

    private String value;

    @Getter
    private BigDecimal steps;

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
