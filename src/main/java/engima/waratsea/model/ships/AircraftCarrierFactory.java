package engima.waratsea.model.ships;

import engima.waratsea.model.ships.data.ShipData;

/**
 * Factory used by guice to create aircraft carriers.
 */
public interface AircraftCarrierFactory extends ShipFactory {
    /**
     * Creates an aircraft carrier.
     * @param data Ship data read from a JSON file.
     * @return An aircraft carrier initialized with the data from the JSON file.
     */
    Ship create(ShipData data);
}
