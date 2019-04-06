package engima.waratsea.model.aircraft;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

/**
 * Represents the type of landing an aircraft is capable of performing.
 *
 * CARRIER - lands and takes off from aircraft carriers and land airfields.
 * LAND - lands and takes off only from land airfields.
 * SEAPLANE - lands and takes off from the sea.
 */
public enum LandingType {
    @SerializedName(value = "CARRIER", alternate = {"Carrier", "carrier"})
    CARRIER("Carrier"),

    @SerializedName(value = "LAND", alternate = {"Land", "land"})
    LAND("Land"),

    @SerializedName(value = "SEAPLANE", alternate = {"Seaplane", "seaplane"})
    SEAPLANE("Seaplane");

    @Getter
    private String value;

    /**
     * The constructor.
     *
     * @param value The String value of the landing type.
     */
    LandingType(final String value) {
        this.value = value;
    }
}
