package engima.waratsea.model.ship;

import engima.waratsea.model.ship.data.CargoData;
import lombok.Getter;
import lombok.Setter;

/**
 * A ship's cargo.
 *
 *  Large Transport game cargo capacity is 3.
 *  Small Transport game cargo capacity is 2.
 *  Destroyer Transport APD game cargo capacity is 1.
 *  Destroyer game cargo capacity is 1/3
 *  Cruiser game cargo capacity is 1.
 *  Sea plane carrier game cargo capacity is 2.
 *
 *  To avoid fractions the ship's cargo capacity is multipled by 3. Thus,
 *
 *  Large Transport   = 9 cargo capacity.
 *  Small Transport   = 6 cargo capacity.
 *  APD               = 3 cargo capacity.
 *  Destroyer         = 1 cargo capacity.
 *  Cruiser           = 3 cargo capacity.
 *  Sea Plane Carrier = 6 cargo capacity.
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

    /**
     * The cargo's units.
     *
     * @return The cargo's units.
     */
    @Override
    public String getUnits() {
        return "tons";
    }
}
