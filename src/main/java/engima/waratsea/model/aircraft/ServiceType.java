package engima.waratsea.model.aircraft;

import com.google.gson.annotations.SerializedName;
import lombok.RequiredArgsConstructor;

/**
 * The aircraft's service.
 */
@RequiredArgsConstructor
public enum ServiceType {
    @SerializedName(value = "AIR_FORCE", alternate = {"AIRFORCE", "Air Force", "air force"})
    AIR_FORCE("Air Force"),

    @SerializedName(value = "NAVY", alternate = {"Navy", "navy"})
    NAVY("Navy");

    private final String value;

    /**
     * Returns the String value of this enum.
     *
     * @return The String value.
     */
    @Override
    public String toString() {
        return value;
    }
}
