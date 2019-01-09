package engima.waratsea.model.ships;

import lombok.Getter;
import lombok.Setter;

/**
 * A ship's cargoShips.
 */
public class Cargo {
    @Getter
    private final int capacity;

    @Getter
    @Setter
    private int level;

    /**
     * Constructor.
     * @param capacity The cargoShips capacity of the ship.
     */
    public Cargo(final int capacity) {
        this.capacity = capacity;
        this.level = 0;
    }

    /**
     * Load all of the cargoShips.
     */
    public void load() {
        level = capacity;
    }
}
