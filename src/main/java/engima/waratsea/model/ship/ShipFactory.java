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
     * Creates a surface ship.
     *
     * @param data The ship's data.
     * @return The ship.
     */
    @Named("surface")
    Ship createSurfaceShip(ShipData data);

}
