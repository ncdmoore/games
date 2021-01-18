package engima.waratsea.model.aircraft;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

/**
 * The aircraft's altitude rating.
 */
public enum AltitudeType {
    @SerializedName(value = "HIGH", alternate = {"High", "high"})
    HIGH("High"),

    @SerializedName(value = "LOW", alternate = {"Low", "low"})
    LOW("Low"),

    @SerializedName(value = "MEDIUM", alternate = {"Medium", "medium"})
    MEDIUM("Medium");

    @Getter
    private final String value;

    /**
     * Constructor.
     *
     * @param value The String representation of the altitude rating.
     */
    AltitudeType(final String value) {
        this.value = value;
    }

    /**
     * Get the String representation.
     *
     * @return The String representation.
     */
    @Override
    public String toString() {
        return value;
    }
}
