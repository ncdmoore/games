package engima.waratsea.model.game.event.ship;

/**
 * Represents the type of ship event actions. A ship may be attacked, spotted, damaged, sunk, etc.
 */
public enum ShipEventAction {
    ARRIVAL("arrival"),
    ATTACKED("attacked"),
    BOMBARDMENT("bombardment"),
    CARGO_LOADED("cargo loaded"),
    CARGO_UNLOADED("cargo unloaded"),
    DAMAGED_HULL("damaged hull"),
    DAMAGED_PRIMARY("damaged primary"),
    DAMAGED_SECONDARY("damaged secondary"),
    DAMAGED_TERTIARY("damaged tertiary"),
    DAMAGED_ANTI_AIR("damaged anti-air"),
    DAMAGED_TORPEDO_MOUNT("damaged torpedo mount"),
    DAMAGED_MOVEMENT_REDUCED("damaged movement reduced"),
    DAMAGED_DEAD_IN_WATER("damaged dead in water"),
    MINEFIELD_LAID("minefield laid"),
    MINEFIELD_CLEARED("minefield cleared"),
    OUT_OF_FUEL("out of fuel"),
    SPOTTED("spotted"),
    SUNK("sunk");

    private String value;

    /**
     * Constructor.
     * @param value String value of the enum.
     */
    ShipEventAction(final String value) {
        this.value = value;
    }

    /**
     * Returns the string value of the enum.
     * @return The enum's string value.
     */
    @Override
    public String toString() {
        return value;
    }
}
