package engima.waratsea.model.squadron;

import com.google.gson.annotations.SerializedName;

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
    FULL("Full"),

    @SerializedName(value = "HALF", alternate = {"Half", "half"})
    HALF("Half"),

    @SerializedName(value = "SIXTH", alternate = {"Sixth", "sixth"})
    SIXTH("Sixth");

    private String value;

    /**
     * The constructor.
     *
     * @param value The String value of the squadron strength.
     */
    SquadronStrength(final String value) {
        this.value = value;
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
