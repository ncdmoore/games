package engima.waratsea.model.ship;

import engima.waratsea.model.ship.data.CargoData;
import lombok.Getter;
import lombok.Setter;

/**
 * A ship's cargo.
 */
public class Cargo implements Component {
    @Getter
    private final String name;

    @Getter
    private final int capacity; // The ship's total cargo capacity. How much cargo a ship can hold.

    @Getter
    @Setter
    private int level; // The current amount of cargo in the ship's holds.


    /**
     * Constructor.
     *
     * @param data The cargo capacity of the ship.
     */
    public Cargo(final CargoData data) {
        this.name = "Cargo";
        this.capacity = data.getCapacity();
        this.level = 0;
    }

    /**
     * Get the cargo's persistent data.
     *
     * @return The cargo's persistent data.
     */
    public CargoData getData() {
        CargoData data = new CargoData();
        data.setCapacity(capacity);
        data.setLevel(level);

        return data;
    }

    /**
     * Load all of the cargoShips.
     **/
    public void load() {
        level = capacity;
    }

    /**
     * Determine if the component is present.
     *
     * @return True if the component is present. False otherwise.
     */
    @Override
    public boolean isPresent() {
        return capacity != 0;
    }

    /**
     * Get the max health of the component.
     *
     * @return The component max health.
     */
    @Override
    public int getMaxHealth() {
        return capacity;
    }

    /**
     * Get the health of the component.
     *
     * @return The component health.
     */
    @Override
    public int getHealth() {
        return level;
    }
}
