package engima.waratsea.model.ships;

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
     * @param capacity The initial fuel capacity of the ship.
     */
    public Fuel(final int capacity) {
        this.capacity = capacity;
        this.level = capacity;
    }
}
