package engima.waratsea.model.ship;

import com.google.inject.name.Named;
import engima.waratsea.model.ship.data.ShipData;

/**
 * Creates ships.
 */
public interface ShipFactory {
    /**
     * Creates an aircraft carrier.
     *
     * @param data The ship's data.
     * @return The ship.
     */
    @Named("aircraft")
    Ship createAircraftCarrier(ShipData data);

    /**
     * Creates a capital ship. Battleship, Battlecruiser, cruiser.
     *
     * @param data The ship's data.
     * @return The ship.
     */
    @Named("capital")
    Ship createCapitalShip(ShipData data);

    /**
     * Creates a surface ship. Destroyer, Destroyer escort, transport, etc...
     *
     * @param data The ship's data.
     * @return The ship.
     */
    @Named("surface")
    Ship createSurfaceShip(ShipData data);

}
