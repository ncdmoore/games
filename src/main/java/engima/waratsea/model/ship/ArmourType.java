package engima.waratsea.model.ship;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Represents a ship's armour type.
 */
@RequiredArgsConstructor
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
     * Get the string value of the enum.
     *
     * @return The string value of the enum.
     */
    @Override
    public String toString() {
        return value;
    }
}
