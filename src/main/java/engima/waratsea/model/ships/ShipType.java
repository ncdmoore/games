package engima.waratsea.model.ships;

/**
 * Represents the type of ship.
 */
public enum ShipType {
    AIRCRAFT_CARRIER("Aircraft carrier"),
    BATTLECRUISER("Battlecruiser"),
    BATTLESHIP("Battleship"),
    CRUISER("Cruiser"),
    DESTROYER("Destroyer"),
    DESTROYER_ESCORT("Destroyer Escort"),
    MINELAYER("Minelayer"),
    MINESWEEPER("Minesweeper"),
    OILER("Oiler"),
    SLOOP("Sloop"),
    SEAPLANE_CARRIER("Seaplane carrier"),
    SUBMARINE("Submarine"),
    TRANSPORT("Transport");

    private String value;

    /**
     * Constructor.
     * @param value The string value of the enum.
     */
    ShipType(final String value) {
        this.value = value;
    }

    /**
     * Return the string representation of the enum.
     * @return The string value of the ShipType enum.
     */
    public String toString() {
        return value;
    }
}
