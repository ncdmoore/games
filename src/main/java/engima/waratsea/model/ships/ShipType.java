package engima.waratsea.model.ships;

import com.google.gson.annotations.SerializedName;

/**
 * Represents the type of ship.
 */
public enum ShipType {
    @SerializedName(value = "AIRCRAFT_CARRIER", alternate = {"Aircraft_Carrier", "aircraft_carrier"})
    AIRCRAFT_CARRIER("Aircraft carrier"),

    @SerializedName(value = "BATTLECRUISER", alternate = {"Battlecruiser", "battlecruiser"})
    BATTLECRUISER("Battlecruiser"),

    @SerializedName(value = "BATTLESHIP", alternate = {"Battleship", "battleship"})
    BATTLESHIP("Battleship"),

    @SerializedName(value = "CRUISER", alternate = {"Cruiser", "cruiser"})
    CRUISER("Cruiser"),

    @SerializedName(value = "DESTROYER", alternate = {"Destroyer", "destroyer"})
    DESTROYER("Destroyer"),

    @SerializedName(value = "DESTROYER_ESCORT", alternate = {"Destroyer_Escort", "destroyer_escort"})
    DESTROYER_ESCORT("Destroyer Escort"),

    @SerializedName(value = "MINELAYER", alternate = {"Minelayer", "minelayer"})
    MINELAYER("Minelayer"),

    @SerializedName(value = "MINESWEEPER", alternate = {"Minesweeper", "minesweeper"})
    MINESWEEPER("Minesweeper"),

    @SerializedName(value = "OILER", alternate = {"Oiler", "oiler"})
    OILER("Oiler"),

    @SerializedName(value = "SLOOP", alternate = {"Sloop", "sloop"})
    SLOOP("Sloop"),

    @SerializedName(value = "SEAPLANE_CARRIER", alternate = {"Seaplane_Carrier", "seaplane_carrier"})
    SEAPLANE_CARRIER("Seaplane carrier"),

    @SerializedName(value = "SUBMARINE", alternate = {"Submarine", "submarine"})
    SUBMARINE("Submarine"),

    @SerializedName(value = "TRANSPORT", alternate = {"Transport", "transport"})
    TRANSPORT("Transport");

    private String value;

    /**
     * Constructor.
     *
     * @param value The string value of the enum.
     */
    ShipType(final String value) {
        this.value = value;
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
