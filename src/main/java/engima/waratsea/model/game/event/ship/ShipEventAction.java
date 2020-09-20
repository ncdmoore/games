package engima.waratsea.model.game.event.ship;

/**
 * Represents the type of ship event actions. A ship may be attacked, spotted, damaged, sunk, etc.
 */
public enum ShipEventAction {
    ARRIVAL("Arrival"),
    ATTACKED("Attacked"),
    BOMBARDMENT("Bombardment"),
    CARGO_LOADED("Cargo loaded"),
    CARGO_UNLOADED("Cargo unloaded"),
    DAMAGED_HULL("Damaged hull"),
    DAMAGED_PRIMARY("Damaged primary"),
    DAMAGED_SECONDARY("Damaged secondary"),
    DAMAGED_TERTIARY("Damaged tertiary"),
    DAMAGED_ANTI_AIR("Damaged anti-air"),
    DAMAGED_TORPEDO_MOUNT("Damaged torpedo mount"),
    DAMAGED_MOVEMENT_REDUCED("Damaged movement reduced"),
    DAMAGED_DEAD_IN_WATER("Damaged dead in water"),
    MINEFIELD_LAID("Minefield laid"),
    MINEFIELD_CLEARED("Minefield cleared"),
    OUT_OF_FUEL("Out of fuel"),
    SPOTTED("Spotted"),
    SUNK("Sunk");

    private final String value;

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
