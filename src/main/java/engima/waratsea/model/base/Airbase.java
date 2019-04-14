package engima.waratsea.model.base;

import engima.waratsea.model.game.Side;

/**
 * Represents air bases.
 */
public interface Airbase extends Base {

    /**
     * The name of the air base.
     *
     * @return The name of the air base.
     */
    String getName();

    /**
     * The side of the air base.
     *
     * @return The air base side: ALLIES or AXIS.
     */
    Side getSide();

    /**
     * The maximum capacity of the air base.
     *
     * @return The maximum capacity of the air base.
     */
    int getMaxCapacity();

    /**
     * The current capacity of the air base.
     *
     * @return The current capacity of the air base in steps.
     */
    int getCapacity();
}
