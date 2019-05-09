package engima.waratsea.model.aircraft;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

/**
 * Defines all the aircraft types in the game.
 */
public enum AircraftType {
    @SerializedName(value = "FIGHTER", alternate = {"Fighter", "fighter"})
    FIGHTER("Fighter", AircraftBaseType.FIGHTER, "F"),

    @SerializedName(value = "BOMBER", alternate = {"Bomber", "bomber"})
    BOMBER("Bomber", AircraftBaseType.BOMBER, "B"),

    @SerializedName(value = "POOR_NAVAL_BOMBER", alternate = {"Poor_Naval_Bomber", "poor_naval_bomber"})
    POOR_NAVAL_BOMBER("Bomber", AircraftBaseType.BOMBER, "B"),

    @SerializedName(value = "DIVE_BOMBER", alternate = {"Dive_Bomber", "dive_bomber", "Dive Bomber", "dive bomber"})
    DIVE_BOMBER("Dive Bomber", AircraftBaseType.BOMBER, "DB"),

    @SerializedName(value = "TORPEDO_BOMBER", alternate = {"Torpedo_Bomber", "torpedo_bomber", "Torpedo Bomber", "torpedo bomber"})
    TORPEDO_BOMBER("Torpedo Bomber", AircraftBaseType.BOMBER, "TB"),

    @SerializedName(value = "RECONNAISSANCE", alternate = {"Reconnaissance", "reconnaissance"})
    RECONNAISSANCE("Reconnaissance", AircraftBaseType.RECON, "R");

    private String value;

    @Getter
    private AircraftBaseType baseType;

    @Getter
    private String designation;

    /**
     * Constructor.
     *
     * @param value The string value of the enum.
     * @param baseType The base aircraft baseType.
     * @param designation The aircraft designation. F for fighter, B for bomber, etc.
     */
    AircraftType(final String value, final AircraftBaseType baseType, final String designation) {
        this.value = value;
        this.baseType = baseType;
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
