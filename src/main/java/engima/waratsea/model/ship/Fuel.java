package engima.waratsea.model.ship;

import engima.waratsea.model.ship.data.FuelData;
import lombok.Getter;
import lombok.Setter;

/**
 * Ship's fuel.
 */
public class Fuel implements Component {
    @Getter
    private final String name;

    private static final int OUT_OF_FUEL = -1;

    @Getter
    private final int capacity;

    @Getter
    @Setter
    private int level;

    /**
     * Constructor.
     *
     * @param data The fuel data that is persisted.
     */
    public Fuel(final FuelData data) {
        this.name = "Fuel";
        this.capacity = data.getCapacity();
        this.level = data.getLevel();

        // This should only happen when a ship is first built at the start of a scenario.
        // The level is unknown so we set it to the capacity. Note, for an out of fuel
        // situation the level is set to -1. Thus, the only time the fuel is 0 is when
        // a ship is first built.
        if (level == 0) {
            level = capacity;
        }
    }

    /**
     * Get the fuel persistent data.
     *
     * @return The fuel's persistent data.
     */
    public FuelData getData() {
        FuelData data = new FuelData();
        data.setCapacity(capacity);
        data.setLevel(level);
        return data;
    }

    /**
     * Get the max capacity of the fuel.
     *
     * @return The max capacity of the fuel.
     */
    public int getMaxHealth() {
        return  capacity;
    }

    /**
     * Get the fuel level.
     *
     * @return The fuel level.
     */
    public int getHealth() {
        return level;
    }

    /**
     * The fuel is always present.
     *
     * @return True.
     */
    public boolean isPresent() {
        return true;
    }
}
