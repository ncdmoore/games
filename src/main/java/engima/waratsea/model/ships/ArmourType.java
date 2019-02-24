package engima.waratsea.model.ships;

import com.google.gson.annotations.SerializedName;

/**
 * Represents a ship's armour type.
 */
public enum ArmourType {
    @SerializedName(value = "HEAVY", alternate = {"Heavy", "heavy"})
    HEAVY,

    @SerializedName(value = "LIGHT", alternate = {"Light", "light"})
    LIGHT,

    @SerializedName(value = "NONE", alternate = {"None", "none"})
    NONE
}
