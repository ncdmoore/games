package engima.waratsea.model.ships;

import engima.waratsea.model.ships.data.ShipData;

/**
 * Factory used by guice to create surface ships.
 */
public interface SurfaceShipFactory extends ShipFactory {
    /**
     * Creates a surface ship.
     * @param data Ship data read from a JSON file.
     * @return A surface ship initialized with the data from the JSON file.
     */
    Ship create(ShipData data);
}
