package engima.waratsea.model.game.nation;

import com.google.gson.annotations.SerializedName;

/**
 * The game nations.
 */
public enum Nation {
    @SerializedName(value = "AUSTRALIAN", alternate = {"Australian", "australian"})
    AUSTRALIAN("Australian"),

    @SerializedName(value = "BRITISH", alternate = {"British", "british"})
    BRITISH("British"),

    @SerializedName(value = "FRENCH", alternate = {"French", "french"})
    FRENCH("French"),

    @SerializedName(value = "GERMAN", alternate = {"German", "german"})
    GERMAN("German"),

    @SerializedName(value = "GREEK", alternate = {"Greek", "greek"})
    GREEK("Greek"),

    @SerializedName(value = "ITALIAN", alternate = {"Italian", "italian"})
    ITALIAN("Italian"),

    @SerializedName(value = "POLISH", alternate = {"Polish", "polish"})
    POLISH("Polish");

    private final String value;

    /**
     * Constructor.
     *
     * @param value The string value of the Nation.
     */
    Nation(final String value) {
        this.value = value;
    }

    /**
     * The string representation of the nation.
     *
     * @return The string representation of the nation.
     */
    @Override
    public String toString() {
        return value;
    }
}
