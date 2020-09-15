package engima.waratsea.model.ship;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

/**
 * Represents a ship's armour type.
 */
public enum ArmourType {
    @SerializedName(value = "HEAVY", alternate = {"Heavy", "heavy"})
    HEAVY("Heavy"),

    @SerializedName(value = "LIGHT", alternate = {"Light", "light"})
    LIGHT("Light"),

    @SerializedName(value = "NONE", alternate = {"None", "none"})
    NONE("None");

    @Getter
    private final String value;

    /**
     * The constructor.
     *
     * @param value The string value of the enum.
     */
    ArmourType(final String value) {
        this.value = value;
    }

    /**
     * Get the string value of the enum.
     *
     * @return The string value of the enum.
     */
    @Override
    public String toString() {
        return value;
    }
}
