package engima.waratsea.model.vessel;

import engima.waratsea.model.game.Side;

public interface Vessel {

    /**
     * Get the name of the vessel.
     *
     * @return The vessel's name.
     */
    String getName();

    /**
     * Get the side of the vessel.
     *
     * @return The vessel's side.
     */
    Side getSide();

    /**
     * Get the vessel's class.
     *
     * @return The vessel's class.
     */
    String getShipClass();

}
