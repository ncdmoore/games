package engima.waratsea.model.victory;

import engima.waratsea.model.game.Side;
import engima.waratsea.model.victory.data.RequiredShipVictoryData;

/**
 * Factory used by guice to create required ship victory conditions.
 */
public interface RequiredShipVictoryFactory {

    /**
     * Create's the ship victory condition.
     * @param data Ship victory data from a JSON file.
     * @param side The side of the ship victory conditions. ALLIES or AXIS.
     * @return The ship victory object.
     */
    RequiredShipVictory create(RequiredShipVictoryData data, Side side);
}
