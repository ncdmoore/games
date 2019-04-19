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
    FULL("Full", 2),

    @SerializedName(value = "HALF", alternate = {"Half", "half"})
    HALF("Half", 1),

    @SerializedName(value = "SIXTH", alternate = {"Sixth", "sixth"})
    SIXTH("1/6", 0.33); // Sixth is for battleship and cruiser float planes.

    private String value;

    @Getter
    private double steps;

    /**
     * The constructor.
     *
     * @param value The String value of the squadron strength.
     * @param steps The number of steps that squadron strength maps to.
     */
    SquadronStrength(final String value, final double steps) {
        this.value = value;
        this.steps = steps;
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
