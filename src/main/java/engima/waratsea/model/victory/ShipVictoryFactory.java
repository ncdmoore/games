package engima.waratsea.model.victory;

import engima.waratsea.model.game.Side;
import engima.waratsea.model.victory.data.ShipVictoryData;

/**
 * Factory used by guice to ship victory conditions.
 */
public interface ShipVictoryFactory {

    /**
     * Create's the ship victory condition.
     * @param data Ship victory data from a JSON file.
     * @param side The side of the ship victory conditions. ALLIES or AXIS.
     * @return The ship victory object.
     */
    ShipVictory create(ShipVictoryData data, Side side);
}
