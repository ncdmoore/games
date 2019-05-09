package engima.waratsea.model.ship.data;

import lombok.Getter;
import lombok.Setter;

/**
 * The fuel data that is persisted. The ship's fuel capacity is determined from the board game by multiplying 24 to
 * the board game value.
 */
public class FuelData {
    @Getter
    @Setter
    private int capacity;

    @Getter
    @Setter
    private int level;
}
