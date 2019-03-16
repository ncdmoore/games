package engima.waratsea.model.ship;

import engima.waratsea.model.ship.data.MovementData;

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

    /**
     * Get the movement persistent data.
     *
     * @return The movement's persistent data.
     */
    public MovementData getData() {
        MovementData data = new MovementData();
        data.setMaxEven(maxEven);
        data.setMaxOdd(maxOdd);
        data.setEven(even);
        data.setOdd(odd);
        return data;
    }
}
