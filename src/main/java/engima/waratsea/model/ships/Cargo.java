package engima.waratsea.model.ships;

import lombok.Getter;
import lombok.Setter;

/**
 * A ship's cargo.
 */
public class Cargo {
    @Getter
    private final int capacity; // The ship's total cargo capacity. How much cargo a ship can hold.

    @Getter
    @Setter
    private int level; // The current amount of cargo in the ship's holds.

    @Getter
    @Setter
    private String originPort;

    /**
     * Constructor.
     *
     * @param capacity The cargo capacity of the ship.
     */
    public Cargo(final int capacity) {
        this.capacity = capacity;
        this.level = 0;
    }

    /**
     * Load all of the cargoShips.
     *
     * @param port The port where the cargo is loaded.
     */
    public void load(final String port) {
        level = capacity;
        originPort = port;
    }
}
