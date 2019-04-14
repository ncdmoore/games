package engima.waratsea.model.aircraft;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

/**
 * Defines all the aircraft types in the game.
 */
public enum AircraftType {
    @SerializedName(value = "FIGHTER", alternate = {"Fighter", "fighter"})
    FIGHTER("Fighter", "F"),

    @SerializedName(value = "BOMBER", alternate = {"Bomber", "bomber"})
    BOMBER("Bomber", "B"),

    @SerializedName(value = "POOR_NAVAL_BOMBER", alternate = {"Poor_Naval_Bomber", "poor_naval_bomber"})
    POOR_NAVAL_BOMBER("Bomber", "B"),

    @SerializedName(value = "DIVE_BOMBER", alternate = {"Dive_Bomber", "dive_bomber", "Dive Bomber", "dive bomber"})
    DIVE_BOMBER("Dive Bomber", "DB"),

    @SerializedName(value = "TORPEDO_BOMBER", alternate = {"Torpedo_Bomber", "torpedo_bomber", "Torpedo Bomber", "torpedo bomber"})
    TORPEDO_BOMBER("Torpedo Bomber", "TB"),

    @SerializedName(value = "RECONNAISSANCE", alternate = {"Reconnaissance", "reconnaissance"})
    RECONNAISSANCE("Reconnaissance", "R");

    private String value;

    @Getter
    private String designation;

    /**
     * Constructor.
     *
     * @param value The string value of the enum.
     * @param designation The aircraft designation. F for fighter, B for bomber, etc.
     */
    AircraftType(final String value, final String designation) {
        this.value = value;
        this.designation = designation;
    }

    /**
     * Return the string representation of the enum.
     *
     * @return The string value of the ShipType enum.
     */
    public String toString() {
        return value;
    }
}
