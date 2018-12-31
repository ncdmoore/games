package engima.waratsea.model.ships;

import engima.waratsea.model.ships.data.ShipData;

/**
 * Creates ships.
 */
public interface ShipFactory {
    /**
     * Creates a ship.
     * @param data The ship's data.
     * @return The ship.
     */
    Ship create(ShipData data);

}
