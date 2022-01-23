package engima.waratsea.model.aircraft;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * The aircraft's altitude rating.
 */
@AllArgsConstructor
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
     * Get the String representation.
     *
     * @return The String representation.
     */
    @Override
    public String toString() {
        return value;
    }
}
