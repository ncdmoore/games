package engima.waratsea.model.base;

import engima.waratsea.model.base.airfield.AirfieldOperation;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.squadron.Squadron;

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

    /**
     * Indicates if this airbase has any squadrons.
     *
     * @return True if any squadron is based at this airbase. False otherwise.
     */
    boolean areSquadronsPresent();

    /**
     * Add a squadron to this air base.
     *
     * @param squadron The squadron that is now based at this airbase.
     * @return True if the squadron was added. False otherwise.
     */
    AirfieldOperation addSquadron(Squadron squadron);

    /**
     * Remove a squadron from this air base.
     *
     * @param squadron The squadron that is removed from this airbase.
     */
    void removeSquadron(Squadron squadron);
}
