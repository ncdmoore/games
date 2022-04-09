package engima.waratsea.model.ship.data;

import lombok.Data;

/**
 * Represents a ships movement data that is persisted.
 */
@Data
public class MovementData {
    private int maxEven;
    private int maxOdd;
    private int even;
    private int odd;
}
