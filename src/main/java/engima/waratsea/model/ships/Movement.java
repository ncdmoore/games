package engima.waratsea.model.ships;

import engima.waratsea.model.ships.data.MovementData;

/**
 * Ship's movement.
 */
public class Movement {

    private final int maxEven;

    private final int maxOdd;

    private int even;

    private int odd;

    /**
     * Constructor.
     *
     * @param data The persisted movement data.
     */
    public Movement(final MovementData data) {
        this.maxEven = data.getMaxEven();
        this.maxOdd = data.getMaxOdd();
        this.even = data.getEven();
        this.odd = data.getOdd();
    }

}
