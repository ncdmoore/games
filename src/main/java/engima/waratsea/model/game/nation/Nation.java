package engima.waratsea.model.game.nation;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

/**
 * The game nations.
 */
public enum Nation {
    @SerializedName(value = "AUSTRALIAN", alternate = {"Australian", "australian"})
    AUSTRALIAN("Australian", "HMAS"),

    @SerializedName(value = "BRITISH", alternate = {"British", "british"})
    BRITISH("British", "HMS"),

    @SerializedName(value = "FRENCH", alternate = {"French", "french"})
    FRENCH("French", ""),

    @SerializedName(value = "GERMAN", alternate = {"German", "german"})
    GERMAN("German", "KMS"),

    @SerializedName(value = "GREEK", alternate = {"Greek", "greek"})
    GREEK("Greek", "HS"),

    @SerializedName(value = "ITALIAN", alternate = {"Italian", "italian"})
    ITALIAN("Italian", "RN"),

    @SerializedName(value = "POLISH", alternate = {"Polish", "polish"})
    POLISH("Polish", "ORP");

    private final String value;

    @Getter
    private final String shipPrefix;

    /**
     * Constructor.
     *
     * @param value The string value of the Nation.
     * @param shipPrefix The prefix before a ship name.
     */
    Nation(final String value, final String shipPrefix) {
        this.value = value;
        this.shipPrefix = shipPrefix;
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
