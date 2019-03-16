package engima.waratsea.model.ship.data;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents a ships movement data that is persisted.
 */
public class MovementData {

    @Getter
    @Setter
    private int maxEven;

    @Getter
    @Setter
    private int maxOdd;

    @Getter
    @Setter
    private int even;

    @Getter
    @Setter
    private int odd;
}
