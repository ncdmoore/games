package engima.waratsea.model.ship;

import engima.waratsea.model.ship.data.MovementData;
import lombok.Getter;

/**
 * Ship's movement.
 */
public class Movement implements Component {
    @Getter
    private final String name;

    private final int maxEven;

    private final int maxOdd;

    @Getter
    private int even;

    @Getter
    private int odd;

    /**
     * Constructor.
     *
     * @param data The persisted movement data.
     */
    public Movement(final MovementData data) {
        this.name = "Movement";
        this.maxEven = data.getMaxEven();
        this.maxOdd = data.getMaxOdd();
        this.even = data.getEven();
        this.odd = data.getOdd();

        // An even movement that is knocked out will have a health of -1.
        // Thus a value of 0 for health indicates this is a newly
        // created movement and the health is set to max health.
        if (even == 0) {
            even = maxEven;
        }

        if (odd == 0) {
            odd = maxOdd;
        }

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

    /**
     * Get the movement's health.
     *
     * @return The movement's health.
     */
    public int getHealth() {
        return even + odd;
    }

    /**
     * Get the component's units.
     *
     * @return The component's units.
     */
    @Override
    public String getUnits() {
        return "Even and Odd Total";
    }

    /**
     * Determine if the component is present.
     *
     * @return True if the component is present. False otherwise.
     */
    @Override
    public boolean isPresent() {
        return true;
    }

    /**
     * Get the movement's max health.
     *
     * @return The movement's max health.
     */
    public int getMaxHealth() {
        return maxEven + maxOdd;
    }
}
