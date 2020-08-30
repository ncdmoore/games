package engima.waratsea.model.ship;

import engima.waratsea.model.ship.data.FuelData;
import lombok.Getter;
import lombok.Setter;

/**
 * Ship's fuel. The ship's fuel capacity is determined from the board game by multiplying 24 to the board game value.
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
     * Get the component's units.
     *
     * @return The component's units.
     */
    @Override
    public String getUnits() {
        return "tons";
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
