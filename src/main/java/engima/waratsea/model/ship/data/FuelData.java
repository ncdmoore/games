package engima.waratsea.model.ship.data;

import lombok.Data;

/**
 * The fuel data that is persisted. The ship's fuel capacity is determined from the board game by multiplying 24 to
 * the board game value.
 */
@Data
public class FuelData {
    private int capacity;
    private int level;
}
