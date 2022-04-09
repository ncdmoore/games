package engima.waratsea.model.ship.data;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The persistent cargo data.
 */
@Data
@NoArgsConstructor
public class CargoData {
    private int capacity; // The ship's total cargo capacity. How much cargo a ship can hold.
    private int level; // The current amount of cargo in the ship's holds.
    private String originPort;
}
