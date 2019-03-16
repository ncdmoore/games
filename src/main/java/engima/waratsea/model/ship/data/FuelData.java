package engima.waratsea.model.ship.data;

import lombok.Getter;
import lombok.Setter;

/**
 * The fuel data that is persisted.
 */
public class FuelData {
    @Getter
    @Setter
    private int capacity;

    @Getter
    @Setter
    private int level;
}
