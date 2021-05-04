package engima.waratsea.model.aircraft;

import com.google.gson.annotations.SerializedName;

/**
 * The aircraft's service.
 */
public enum ServiceType {
    @SerializedName(value = "AIR_FORCE", alternate = {"AIRFORCE", "Air Force", "air force"})
    AIR_FORCE("Air Force"),

    @SerializedName(value = "NAVY", alternate = {"Navy", "navy"})
    NAVY("Navy");

    private final String value;

    /**
     * Constructor.
     *
     * @param value The String value of this enum.
     */
    ServiceType(final String value) {
        this.value = value;
    }

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
