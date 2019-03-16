package engima.waratsea.model.ship.data;

import lombok.Getter;
import lombok.Setter;

/**
 * The persistent cargo data.
 */
public class CargoData {
    @Getter
    @Setter
    private int capacity; // The ship's total cargo capacity. How much cargo a ship can hold.

    @Getter
    @Setter
    private int level; // The current amount of cargo in the ship's holds.

    @Getter
    @Setter
    private String originPort;

    /**
     * Default constructor.
     */
    public CargoData() {
        capacity = 0;
        level = 0;
    }
}
