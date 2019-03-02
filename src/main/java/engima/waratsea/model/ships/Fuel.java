package engima.waratsea.model.ships;

import engima.waratsea.model.ships.data.FuelData;
import lombok.Getter;
import lombok.Setter;

/**
 * Ship's fuel.
 */
public class Fuel {

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
}
